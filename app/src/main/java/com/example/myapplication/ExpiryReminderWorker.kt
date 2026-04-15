package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


const val NOTIF_PREFS = "notif_settings"
const val DAYS_BEFORE_KEY = "days_before"
const val DAILY_ENABLED_KEY = "daily_enabled"
const val MIN_DAYS_BEFORE_REMINDER = 1
const val MAX_DAYS_BEFORE_REMINDER = 7


private const val WORK_NAME = "expiry_daily_work"
private const val CHANNEL_ID = "expiry_reminders"
private const val NOTIFICATION_ID = 1001


private const val FOOD_PREFS = "food_prefs"
private const val FOOD_LIST_KEY = "food_list"

private val EXPIRY_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d/M/yyyy", Locale.US)

class ExpiryReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val ctx = applicationContext

        val notifPrefs = ctx.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
        val dailyEnabled = notifPrefs.getBoolean(DAILY_ENABLED_KEY, false)
        val force = inputData.getBoolean("force", false)


        if (!dailyEnabled && !force) return Result.success()

        val daysBefore =
            notifPrefs.getInt(DAYS_BEFORE_KEY, 3)
                .coerceIn(MIN_DAYS_BEFORE_REMINDER, MAX_DAYS_BEFORE_REMINDER)

        val foodPrefs = ctx.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
        val json = foodPrefs.getString(FOOD_LIST_KEY, null) ?: return Result.success()

        val type = object : TypeToken<List<FoodItem>>() {}.type
        val foods: List<FoodItem> =
            runCatching { Gson().fromJson<List<FoodItem>>(json, type) }.getOrDefault(emptyList())

        val expiring = foods.mapNotNull { food ->
            val d = daysUntil(food.expiry)
            if (d != null && d in 0..daysBefore) (food to d) else null
        }.sortedBy { it.second }

        if (expiring.isEmpty()) return Result.success()

        createExpiryNotificationChannel(ctx)

        val title =
            if (expiring.size == 1) "Food expires soon"
            else "${expiring.size} foods expire soon"

        val inbox = NotificationCompat.InboxStyle()
        expiring.take(5).forEach { (food, d) ->
            inbox.addLine("${food.name} — ${daysText(d)}")
        }
        if (expiring.size > 5) inbox.addLine("…and ${expiring.size - 5} more")

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("Tap to open your pantry")
            .setStyle(inbox)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    private fun daysUntil(expiry: String): Int? = try {
        val target = LocalDate.parse(expiry, EXPIRY_FORMATTER)
        ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
    } catch (_: Exception) {
        null
    }

    private fun daysText(days: Int): String = when (days) {
        0 -> "today"
        1 -> "1 day"
        else -> "$days days"
    }
}


fun createExpiryNotificationChannel(context: Context) {

    val channel = NotificationChannel(
        CHANNEL_ID,
        "Expiry reminders",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Daily reminders for foods close to expiry"
    }

    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.createNotificationChannel(channel)
}


fun scheduleDailyExpiryWork(context: Context) {
    val initialDelayMs = delayUntilHour(9)

    val req = PeriodicWorkRequestBuilder<ExpiryReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        req
    )
}

fun cancelDailyExpiryWork(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
}


fun runExpiryWorkNow(context: Context) {
    val req = OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
        .setInputData(workDataOf("force" to true))
        .build()

    WorkManager.getInstance(context).enqueue(req)
}

private fun delayUntilHour(hour24: Int): Long {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour24)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }
    return target.timeInMillis - now.timeInMillis
}
