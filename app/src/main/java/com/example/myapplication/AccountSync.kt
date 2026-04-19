package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val ACCOUNT_PREFS = "account_sync"
private const val ACCOUNT_SESSION_KEY = "account_session"
private const val ACCOUNT_LAST_SYNCED_AT_KEY = "account_last_synced_at"
private const val ACCOUNT_LOCAL_MUTATION_AT_KEY = "account_local_mutation_at"
private const val ACCOUNT_LAST_SYNC_ERROR_KEY = "account_last_sync_error"
private const val ACCOUNT_AUTO_SYNC_ENABLED_KEY = "account_auto_sync_enabled"
private const val ACCOUNT_AUTO_SYNC_MODE_KEY = "account_auto_sync_mode"

private const val ACCOUNT_FOOD_PREFS = "food_prefs"
private const val ACCOUNT_THEME_PREFS = "app_settings"
private const val ACCOUNT_NOTIF_PREFS = "notif_settings"

private const val ACCOUNT_FOOD_LIST_KEY = "food_list"
private const val ACCOUNT_HISTORY_LIST_KEY = "history_list"
private const val ACCOUNT_CATEGORIES_LIST_KEY = "categories_list"
private const val ACCOUNT_BARCODE_CACHE_KEY = "barcode_cache"
private const val ACCOUNT_THEME_KEY = "theme_mode"
private const val ACCOUNT_COUNTDOWN_FORMAT_KEY = "countdown_format"
private const val ACCOUNT_DAYS_BEFORE_KEY = "days_before"
private const val ACCOUNT_DAILY_ENABLED_KEY = "daily_enabled"

private const val CLOUD_SYNC_VERSION = 1
private const val ACCOUNT_AUTO_SYNC_WORK_NAME = "account_auto_sync_work"
private const val EMAIL_PASSWORD_INCORRECT_MESSAGE = "Email or password is incorrect."

private val accountGson = Gson()
private val accountAuthPillShape = RoundedCornerShape(30.dp)
private val accountAuthMaxWidth = 520.dp
private val accountAuthControlHeight = 56.dp

enum class AccountProvider { EMAIL, GOOGLE }

data class AccountSession(
    val provider: AccountProvider,
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val idToken: String,
    val refreshToken: String
)

private data class CloudSyncSnapshot(
    val foodListJson: String = "[]",
    val historyJson: String = "[]",
    val categoriesJson: String = "[]",
    val barcodeCacheJson: String = "{}",
    val themeMode: String = ThemeMode.SYSTEM.name,
    val countdownFormat: String = CountdownFormat.DAYS_ONLY.name,
    val daysBeforeReminder: Int = 3,
    val dailyNotificationsEnabled: Boolean = false
)

private data class CloudSyncEnvelope(
    val version: Int = CLOUD_SYNC_VERSION,
    val updatedAtMs: Long = System.currentTimeMillis(),
    val snapshot: CloudSyncSnapshot
)

private data class IdentityAuthResponse(
    val localId: String? = null,
    val email: String? = null,
    val idToken: String? = null,
    val refreshToken: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null
)

private data class IdentityRefreshResponse(
    val user_id: String? = null,
    val id_token: String? = null,
    val refresh_token: String? = null
)

private data class ServiceErrorEnvelope(
    val error: ServiceErrorBody? = null
)

private data class ServiceErrorBody(
    val message: String? = null
)

private data class FirestoreDocumentResponse(
    val fields: Map<String, FirestoreValue>? = null
)

private data class FirestoreValue(
    val stringValue: String? = null,
    val integerValue: String? = null
)

private enum class ResumeAction {
    NONE,
    RESTORED_REMOTE,
    PUSHED_LOCAL
}

private data class ResumeResult(
    val session: AccountSession,
    val action: ResumeAction
)

private data class SyncMeta(
    val lastSyncedAt: Long,
    val lastError: String?
)

private enum class AccountStatusPlacement {
    EMAIL_AUTH,
    GOOGLE_AUTH,
    SYNC_ACTION
}

private enum class AccountAutoSyncMode {
    DAILY,
    WEEKLY,
    MONTHLY
}

private fun accountPrefs(context: Context): SharedPreferences {
    return context.applicationContext.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE)
}

private fun loadAccountSession(context: Context): AccountSession? {
    val json = accountPrefs(context).getString(ACCOUNT_SESSION_KEY, null) ?: return null
    return runCatching { accountGson.fromJson(json, AccountSession::class.java) }.getOrNull()
}

private fun saveAccountSession(context: Context, session: AccountSession?) {
    accountPrefs(context).edit {
        if (session == null) {
            remove(ACCOUNT_SESSION_KEY)
        } else {
            putString(ACCOUNT_SESSION_KEY, accountGson.toJson(session))
        }
    }
}

private fun clearAccountSession(context: Context) {
    accountPrefs(context).edit {
        remove(ACCOUNT_SESSION_KEY)
        remove(ACCOUNT_LAST_SYNC_ERROR_KEY)
        remove(ACCOUNT_LAST_SYNCED_AT_KEY)
        remove(ACCOUNT_LOCAL_MUTATION_AT_KEY)
    }
    cancelAccountAutoSyncWork(context.applicationContext)
}

private fun loadSyncMeta(context: Context): SyncMeta {
    val prefs = accountPrefs(context)
    return SyncMeta(
        lastSyncedAt = prefs.getLong(ACCOUNT_LAST_SYNCED_AT_KEY, 0L),
        lastError = prefs.getString(ACCOUNT_LAST_SYNC_ERROR_KEY, null)
    )
}

private fun saveLastSyncedAt(context: Context, timestampMs: Long) {
    accountPrefs(context).edit {
        putLong(ACCOUNT_LAST_SYNCED_AT_KEY, timestampMs)
        putLong(ACCOUNT_LOCAL_MUTATION_AT_KEY, timestampMs)
        remove(ACCOUNT_LAST_SYNC_ERROR_KEY)
    }
}

private fun markLocalMutation(context: Context, timestampMs: Long = System.currentTimeMillis()) {
    accountPrefs(context).edit {
        putLong(ACCOUNT_LOCAL_MUTATION_AT_KEY, timestampMs)
    }
}

private fun loadLocalMutationAt(context: Context): Long {
    return accountPrefs(context).getLong(ACCOUNT_LOCAL_MUTATION_AT_KEY, 0L)
}

private fun saveLastSyncError(context: Context, message: String?) {
    accountPrefs(context).edit {
        if (message.isNullOrBlank()) {
            remove(ACCOUNT_LAST_SYNC_ERROR_KEY)
        } else {
            putString(ACCOUNT_LAST_SYNC_ERROR_KEY, message)
        }
    }
}

private fun loadAccountAutoSyncEnabled(context: Context): Boolean {
    return accountPrefs(context).getBoolean(ACCOUNT_AUTO_SYNC_ENABLED_KEY, true)
}

private fun saveAccountAutoSyncEnabled(
    context: Context,
    enabled: Boolean
) {
    accountPrefs(context).edit {
        putBoolean(ACCOUNT_AUTO_SYNC_ENABLED_KEY, enabled)
    }
}

private fun loadAccountAutoSyncMode(context: Context): AccountAutoSyncMode {
    val raw = accountPrefs(context).getString(
        ACCOUNT_AUTO_SYNC_MODE_KEY,
        AccountAutoSyncMode.DAILY.name
    ) ?: AccountAutoSyncMode.DAILY.name

    return when (raw) {
        "APP_OPEN_ONLY" -> AccountAutoSyncMode.DAILY
        else -> runCatching { AccountAutoSyncMode.valueOf(raw) }
            .getOrDefault(AccountAutoSyncMode.DAILY)
    }
}

private fun saveAccountAutoSyncMode(
    context: Context,
    mode: AccountAutoSyncMode
) {
    accountPrefs(context).edit {
        putString(ACCOUNT_AUTO_SYNC_MODE_KEY, mode.name)
    }
}

fun isCloudAccountConfigured(): Boolean {
    return BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()
}

fun isGoogleAccountConfigured(): Boolean {
    return isCloudAccountConfigured() &&
            BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
}

@Composable
fun rememberAccountSessionPreference(): AccountSession? {
    val context = LocalContext.current
    val prefs = remember(context) { accountPrefs(context) }
    var session by remember { mutableStateOf(loadAccountSession(context)) }

    DisposableEffect(prefs, context) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == ACCOUNT_SESSION_KEY) {
                session = loadAccountSession(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return session
}

@Composable
private fun rememberAccountSyncMeta(): SyncMeta {
    val context = LocalContext.current
    val prefs = remember(context) { accountPrefs(context) }
    var meta by remember { mutableStateOf(loadSyncMeta(context)) }

    DisposableEffect(prefs, context) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (
                key == ACCOUNT_LAST_SYNCED_AT_KEY ||
                key == ACCOUNT_LAST_SYNC_ERROR_KEY
            ) {
                meta = loadSyncMeta(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return meta
}

@Composable
private fun rememberAccountAutoSyncEnabledPreference(): Boolean {
    val context = LocalContext.current
    val prefs = remember(context) { accountPrefs(context) }
    var autoSyncEnabled by remember { mutableStateOf(loadAccountAutoSyncEnabled(context)) }

    DisposableEffect(prefs, context) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == ACCOUNT_AUTO_SYNC_ENABLED_KEY) {
                autoSyncEnabled = loadAccountAutoSyncEnabled(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return autoSyncEnabled
}

@Composable
private fun rememberAccountAutoSyncModePreference(): AccountAutoSyncMode {
    val context = LocalContext.current
    val prefs = remember(context) { accountPrefs(context) }
    var autoSyncMode by remember { mutableStateOf(loadAccountAutoSyncMode(context)) }

    DisposableEffect(prefs, context) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == ACCOUNT_AUTO_SYNC_MODE_KEY) {
                autoSyncMode = loadAccountAutoSyncMode(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return autoSyncMode
}

private fun requireCloudSetup() {
    check(isCloudAccountConfigured()) {
        "Cloud accounts are not configured yet. Add FIREBASE_API_KEY and FIREBASE_PROJECT_ID in local.properties first."
    }
}

private fun cloudDocumentUrl(uid: String): String {
    return "https://firestore.googleapis.com/v1/projects/${BuildConfig.FIREBASE_PROJECT_ID}/databases/(default)/documents/users/$uid"
}

private fun captureLocalSnapshot(context: Context): CloudSyncEnvelope {
    val foodPrefs = context.applicationContext.getSharedPreferences(ACCOUNT_FOOD_PREFS, Context.MODE_PRIVATE)
    val themePrefs = context.applicationContext.getSharedPreferences(ACCOUNT_THEME_PREFS, Context.MODE_PRIVATE)
    val notifPrefs = context.applicationContext.getSharedPreferences(ACCOUNT_NOTIF_PREFS, Context.MODE_PRIVATE)

    return CloudSyncEnvelope(
        updatedAtMs = System.currentTimeMillis(),
        snapshot = CloudSyncSnapshot(
            foodListJson = foodPrefs.getString(ACCOUNT_FOOD_LIST_KEY, "[]") ?: "[]",
            historyJson = foodPrefs.getString(ACCOUNT_HISTORY_LIST_KEY, "[]") ?: "[]",
            categoriesJson = foodPrefs.getString(ACCOUNT_CATEGORIES_LIST_KEY, "[]") ?: "[]",
            barcodeCacheJson = foodPrefs.getString(ACCOUNT_BARCODE_CACHE_KEY, "{}") ?: "{}",
            themeMode = themePrefs.getString(ACCOUNT_THEME_KEY, ThemeMode.SYSTEM.name)
                ?: ThemeMode.SYSTEM.name,
            countdownFormat = themePrefs.getString(
                ACCOUNT_COUNTDOWN_FORMAT_KEY,
                CountdownFormat.DAYS_ONLY.name
            ) ?: CountdownFormat.DAYS_ONLY.name,
            daysBeforeReminder = notifPrefs.getInt(ACCOUNT_DAYS_BEFORE_KEY, 3),
            dailyNotificationsEnabled = notifPrefs.getBoolean(ACCOUNT_DAILY_ENABLED_KEY, false)
        )
    )
}

private fun restoreLocalSnapshot(context: Context, envelope: CloudSyncEnvelope) {
    val snapshot = envelope.snapshot
    val foodPrefs = context.applicationContext.getSharedPreferences(ACCOUNT_FOOD_PREFS, Context.MODE_PRIVATE)
    val themePrefs = context.applicationContext.getSharedPreferences(ACCOUNT_THEME_PREFS, Context.MODE_PRIVATE)
    val notifPrefs = context.applicationContext.getSharedPreferences(ACCOUNT_NOTIF_PREFS, Context.MODE_PRIVATE)

    foodPrefs.edit {
        putString(ACCOUNT_FOOD_LIST_KEY, snapshot.foodListJson)
        putString(ACCOUNT_HISTORY_LIST_KEY, snapshot.historyJson)
        putString(ACCOUNT_CATEGORIES_LIST_KEY, snapshot.categoriesJson)
        putString(ACCOUNT_BARCODE_CACHE_KEY, snapshot.barcodeCacheJson)
    }

    themePrefs.edit {
        putString(ACCOUNT_THEME_KEY, snapshot.themeMode)
        putString(ACCOUNT_COUNTDOWN_FORMAT_KEY, snapshot.countdownFormat)
    }

    notifPrefs.edit {
        putInt(ACCOUNT_DAYS_BEFORE_KEY, snapshot.daysBeforeReminder)
        putBoolean(ACCOUNT_DAILY_ENABLED_KEY, snapshot.dailyNotificationsEnabled)
    }

    if (snapshot.dailyNotificationsEnabled) {
        scheduleDailyExpiryWork(context.applicationContext)
    } else {
        cancelDailyExpiryWork(context.applicationContext)
    }

    saveLastSyncedAt(context, envelope.updatedAtMs)
}

private fun parseIdentityErrorCode(body: String?): String {
    return runCatching {
        accountGson.fromJson(body, ServiceErrorEnvelope::class.java).error?.message
    }.getOrNull().orEmpty()
}

private fun friendlyIdentityError(body: String?): String {
    val code = parseIdentityErrorCode(body)

    return when (code) {
        "EMAIL_EXISTS" -> "That email is already in use."
        "INVALID_PASSWORD" -> EMAIL_PASSWORD_INCORRECT_MESSAGE
        "EMAIL_NOT_FOUND" -> EMAIL_PASSWORD_INCORRECT_MESSAGE
        "INVALID_LOGIN_CREDENTIALS" -> EMAIL_PASSWORD_INCORRECT_MESSAGE
        "USER_DISABLED" -> "That account is disabled."
        "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many attempts right now. Try again in a moment."
        "OPERATION_NOT_ALLOWED" -> "Enable Email/Password in Firebase Authentication first."
        "CONFIGURATION_NOT_FOUND" -> "Enable Google in Firebase Authentication and set the web client ID for this build."
        "INVALID_IDP_RESPONSE" -> "Google sign-in did not return a usable account token."
        else -> "Sign in failed. Try again."
    }
}

private fun friendlyCloudSyncError(body: String?): String {
    val code = runCatching {
        accountGson.fromJson(body, ServiceErrorEnvelope::class.java).error?.message
    }.getOrNull().orEmpty()

    return when {
        code.contains("PERMISSION_DENIED") ->
            "Cloud sync needs Firestore rules that allow each signed-in user to read and write only their own document."
        code.contains("UNAUTHENTICATED") ->
            "Your account session expired. Please sign in again."
        else ->
            "Cloud sync couldn't reach your online pantry data."
    }
}

private suspend fun executeJsonRequest(
    url: String,
    method: String,
    bodyJson: String? = null,
    bearerToken: String? = null,
    contentType: String = "application/json"
): String = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = method
        connectTimeout = 15_000
        readTimeout = 15_000
        doInput = true
        setRequestProperty("Accept", "application/json")
        bearerToken?.let { setRequestProperty("Authorization", "Bearer $it") }
        if (bodyJson != null) {
            doOutput = true
            setRequestProperty("Content-Type", contentType)
        }
    }

    bodyJson?.let { json ->
        connection.outputStream.bufferedWriter().use { writer ->
            writer.write(json)
        }
    }

    val stream = if (connection.responseCode in 200..299) {
        connection.inputStream
    } else {
        connection.errorStream
    }

    val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    if (connection.responseCode !in 200..299) {
        throw IOException(responseText.ifBlank { "HTTP ${connection.responseCode}" })
    }

    responseText
}

private fun IdentityAuthResponse.toAccountSession(provider: AccountProvider): AccountSession {
    return AccountSession(
        provider = provider,
        uid = localId.orEmpty(),
        email = email.orEmpty(),
        displayName = displayName,
        photoUrl = photoUrl,
        idToken = idToken.orEmpty(),
        refreshToken = refreshToken.orEmpty()
    )
}

private suspend fun signUpWithEmailPassword(
    email: String,
    password: String
): AccountSession {
    requireCloudSetup()
    val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=${BuildConfig.FIREBASE_API_KEY}"
    val body = accountGson.toJson(
        mapOf(
            "email" to email,
            "password" to password,
            "returnSecureToken" to true
        )
    )

    return try {
        val responseText = executeJsonRequest(url = url, method = "POST", bodyJson = body)
        accountGson.fromJson(responseText, IdentityAuthResponse::class.java)
            .toAccountSession(AccountProvider.EMAIL)
    } catch (error: IOException) {
        throw IllegalStateException(friendlyIdentityError(error.message))
    }
}

private suspend fun signInWithEmailPassword(
    email: String,
    password: String
): AccountSession {
    requireCloudSetup()
    val url =
        "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${BuildConfig.FIREBASE_API_KEY}"
    val body = accountGson.toJson(
        mapOf(
            "email" to email,
            "password" to password,
            "returnSecureToken" to true
        )
    )

    return try {
        val responseText = executeJsonRequest(url = url, method = "POST", bodyJson = body)
        accountGson.fromJson(responseText, IdentityAuthResponse::class.java)
            .toAccountSession(AccountProvider.EMAIL)
    } catch (error: IOException) {
        throw IllegalStateException(friendlyIdentityError(error.message))
    }
}

private suspend fun exchangeGoogleIdToken(googleIdToken: String): AccountSession {
    requireCloudSetup()
    val encodedToken =
        URLEncoder.encode(googleIdToken, StandardCharsets.UTF_8.toString())
    val postBody = "id_token=$encodedToken&providerId=google.com"
    val url =
        "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=${BuildConfig.FIREBASE_API_KEY}"
    val body = accountGson.toJson(
        mapOf(
            "postBody" to postBody,
            "requestUri" to "http://localhost",
            "returnIdpCredential" to true,
            "returnSecureToken" to true
        )
    )

    return try {
        val responseText = executeJsonRequest(url = url, method = "POST", bodyJson = body)
        accountGson.fromJson(responseText, IdentityAuthResponse::class.java)
            .toAccountSession(AccountProvider.GOOGLE)
    } catch (error: IOException) {
        throw IllegalStateException(friendlyIdentityError(error.message))
    }
}

private suspend fun refreshAccountSession(current: AccountSession): AccountSession {
    requireCloudSetup()
    val url = "https://securetoken.googleapis.com/v1/token?key=${BuildConfig.FIREBASE_API_KEY}"
    val formBody =
        "grant_type=refresh_token&refresh_token=${
            URLEncoder.encode(current.refreshToken, StandardCharsets.UTF_8.toString())
        }"

    return try {
        val responseText = executeJsonRequest(
            url = url,
            method = "POST",
            bodyJson = formBody,
            contentType = "application/x-www-form-urlencoded"
        )
        val refreshed = accountGson.fromJson(responseText, IdentityRefreshResponse::class.java)
        current.copy(
            uid = refreshed.user_id ?: current.uid,
            idToken = refreshed.id_token ?: current.idToken,
            refreshToken = refreshed.refresh_token ?: current.refreshToken
        )
    } catch (error: IOException) {
        throw IllegalStateException("Your session expired. Please sign in again.")
    }
}

private suspend fun fetchRemoteSnapshot(session: AccountSession): CloudSyncEnvelope? {
    requireCloudSetup()

    return try {
        val responseText = executeJsonRequest(
            url = cloudDocumentUrl(session.uid),
            method = "GET",
            bearerToken = session.idToken
        )
        val document = accountGson.fromJson(responseText, FirestoreDocumentResponse::class.java)
        val payloadJson = document.fields?.get("payloadJson")?.stringValue ?: return null
        accountGson.fromJson(payloadJson, CloudSyncEnvelope::class.java)
    } catch (error: IOException) {
        val body = error.message.orEmpty()
        if (body.contains("NOT_FOUND")) {
            null
        } else {
            throw IllegalStateException(friendlyCloudSyncError(body))
        }
    }
}

private suspend fun pushSnapshotToCloud(
    context: Context,
    session: AccountSession
): AccountSession {
    requireCloudSetup()
    val refreshedSession = refreshAccountSession(session)
    val envelope = captureLocalSnapshot(context)
    val body = accountGson.toJson(
        mapOf(
            "fields" to buildMap<String, Any> {
                put("email", mapOf("stringValue" to refreshedSession.email))
                put("provider", mapOf("stringValue" to refreshedSession.provider.name))
                put("updatedAtMs", mapOf("integerValue" to envelope.updatedAtMs.toString()))
                put("payloadJson", mapOf("stringValue" to accountGson.toJson(envelope)))
                refreshedSession.displayName
                    ?.takeIf { it.isNotBlank() }
                    ?.let { put("displayName", mapOf("stringValue" to it)) }
                refreshedSession.photoUrl
                    ?.takeIf { it.isNotBlank() }
                    ?.let { put("photoUrl", mapOf("stringValue" to it)) }
            }
        )
    )

    try {
        executeJsonRequest(
            url = cloudDocumentUrl(refreshedSession.uid),
            method = "PATCH",
            bearerToken = refreshedSession.idToken,
            bodyJson = body
        )
        saveAccountSession(context, refreshedSession)
        saveLastSyncedAt(context, envelope.updatedAtMs)
        return refreshedSession
    } catch (error: IOException) {
        throw IllegalStateException(friendlyCloudSyncError(error.message))
    }
}

private suspend fun bootstrapSignedInAccount(
    context: Context,
    session: AccountSession
): ResumeResult {
    val refreshed = refreshAccountSession(session)
    saveAccountSession(context, refreshed)

    val remote = fetchRemoteSnapshot(refreshed)
    return if (remote != null) {
        restoreLocalSnapshot(context, remote)
        ResumeResult(refreshed, ResumeAction.RESTORED_REMOTE)
    } else {
        val synced = pushSnapshotToCloud(context, refreshed)
        ResumeResult(synced, ResumeAction.PUSHED_LOCAL)
    }
}

private suspend fun resumeCloudSync(
    context: Context,
    session: AccountSession
): ResumeResult {
    val refreshed = refreshAccountSession(session)
    saveAccountSession(context, refreshed)

    val lastSyncedAt = loadSyncMeta(context).lastSyncedAt
    val localMutationAt = loadLocalMutationAt(context)
    val remote = fetchRemoteSnapshot(refreshed)

    return when {
        remote != null && remote.updatedAtMs > lastSyncedAt && remote.updatedAtMs > localMutationAt -> {
            restoreLocalSnapshot(context, remote)
            ResumeResult(refreshed, ResumeAction.RESTORED_REMOTE)
        }
        localMutationAt > lastSyncedAt -> {
            val synced = pushSnapshotToCloud(context, refreshed)
            ResumeResult(synced, ResumeAction.PUSHED_LOCAL)
        }
        else -> {
            saveLastSyncedAt(context, System.currentTimeMillis())
            ResumeResult(refreshed, ResumeAction.NONE)
        }
    }
}

private suspend fun restoreCloudDataNow(
    context: Context,
    session: AccountSession
): AccountSession {
    val refreshed = refreshAccountSession(session)
    saveAccountSession(context, refreshed)
    val remote = fetchRemoteSnapshot(refreshed)
        ?: throw IllegalStateException("No cloud pantry data was found for this account yet.")
    restoreLocalSnapshot(context, remote)
    return refreshed
}

private suspend fun clearCredentialState(context: Context) {
    try {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    } catch (_: ClearCredentialException) {
    }
}

private fun AccountAutoSyncMode.intervalDays(): Long? {
    return when (this) {
        AccountAutoSyncMode.DAILY -> 1L
        AccountAutoSyncMode.WEEKLY -> 7L
        AccountAutoSyncMode.MONTHLY -> 30L
    }
}

private fun statusMessageDisplayDurationMillis(message: String): Long {
    val wordCount = message
        .trim()
        .split(Regex("\\s+"))
        .count { it.isNotBlank() }

    return (4200L + (wordCount * 260L)).coerceIn(5200L, 9000L)
}

private fun scheduleAccountAutoSyncWork(
    context: Context,
    mode: AccountAutoSyncMode
) {
    val repeatIntervalDays = mode.intervalDays() ?: return

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val request =
        PeriodicWorkRequestBuilder<AccountAutoSyncWorker>(repeatIntervalDays, TimeUnit.DAYS)
            .setInitialDelay(repeatIntervalDays, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        ACCOUNT_AUTO_SYNC_WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}

private fun cancelAccountAutoSyncWork(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(ACCOUNT_AUTO_SYNC_WORK_NAME)
}

class AccountAutoSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!isCloudAccountConfigured()) return Result.success()

        val session = loadAccountSession(applicationContext) ?: return Result.success()

        return runCatching {
            val result = resumeCloudSync(applicationContext, session)
            saveAccountSession(applicationContext, result.session)
            saveLastSyncError(applicationContext, null)
            Result.success()
        }.getOrElse { failure ->
            if (failure.message?.contains("sign in again", ignoreCase = true) == true) {
                clearAccountSession(applicationContext)
            }
            saveLastSyncError(
                applicationContext,
                failure.message ?: "Cloud sync couldn't run in the background."
            )
            Result.success()
        }
    }
}

private suspend fun launchGoogleSignIn(context: Context): AccountSession {
    check(isGoogleAccountConfigured()) {
        "Google sign-in is not configured yet. Add GOOGLE_WEB_CLIENT_ID in local.properties and enable Google in Firebase Authentication."
    }

    val activity = context.findActivity()
        ?: throw IllegalStateException("Google sign-in needs an activity context.")
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = CredentialManager.create(activity).getCredential(
            context = activity,
            request = request
        )
        val credential = result.credential
        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            exchangeGoogleIdToken(googleCredential.idToken)
        } else {
            throw IllegalStateException("Google sign-in did not return a Google account credential.")
        }
    } catch (_: GoogleIdTokenParsingException) {
        throw IllegalStateException("Google sign-in returned an unreadable ID token.")
    } catch (_: GetCredentialCancellationException) {
        throw IllegalStateException("Google sign-in was canceled.")
    } catch (error: GetCredentialException) {
        throw IllegalStateException(
            "Google sign-in failed: ${error.javaClass.simpleName} - ${error.message ?: "no message"}"
        )
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
fun AccountCloudSyncEffect(session: AccountSession?) {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val autoSyncEnabled = rememberAccountAutoSyncEnabledPreference()
    val autoSyncMode = rememberAccountAutoSyncModePreference()

    DisposableEffect(context, session?.uid, autoSyncEnabled, autoSyncMode) {
        if (session == null || !isCloudAccountConfigured() || !autoSyncEnabled) {
            cancelAccountAutoSyncWork(context)
        } else {
            scheduleAccountAutoSyncWork(context, autoSyncMode)
        }
        onDispose { }
    }

    DisposableEffect(context, lifecycleOwner, session?.uid, session?.refreshToken, autoSyncEnabled) {
        if (session == null || !isCloudAccountConfigured()) {
            onDispose {}
        } else {
            val foodPrefs = context.getSharedPreferences(ACCOUNT_FOOD_PREFS, Context.MODE_PRIVATE)
            val themePrefs = context.getSharedPreferences(ACCOUNT_THEME_PREFS, Context.MODE_PRIVATE)
            val notifPrefs = context.getSharedPreferences(ACCOUNT_NOTIF_PREFS, Context.MODE_PRIVATE)
            var foregroundSyncJob: Job? = null

            fun runForegroundSync() {
                foregroundSyncJob?.cancel()
                foregroundSyncJob = scope.launch {
                    runCatching {
                        val result = resumeCloudSync(context, loadAccountSession(context) ?: session)
                        saveAccountSession(context, result.session)
                        saveLastSyncError(context, null)
                    }.onFailure { failure ->
                        if (failure.message?.contains("sign in again", ignoreCase = true) == true) {
                            clearAccountSession(context)
                        }
                        saveLastSyncError(
                            context,
                            failure.message ?: "Cloud sync couldn't reconnect."
                        )
                    }
                }
            }

            val foodListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (
                    key == ACCOUNT_FOOD_LIST_KEY ||
                    key == ACCOUNT_HISTORY_LIST_KEY ||
                    key == ACCOUNT_CATEGORIES_LIST_KEY ||
                    key == ACCOUNT_BARCODE_CACHE_KEY
                ) {
                    markLocalMutation(context)
                }
            }
            val themeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (
                    key == ACCOUNT_THEME_KEY ||
                    key == ACCOUNT_COUNTDOWN_FORMAT_KEY
                ) {
                    markLocalMutation(context)
                }
            }
            val notifListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (
                    key == ACCOUNT_DAYS_BEFORE_KEY ||
                    key == ACCOUNT_DAILY_ENABLED_KEY
                ) {
                    markLocalMutation(context)
                }
            }

            foodPrefs.registerOnSharedPreferenceChangeListener(foodListener)
            themePrefs.registerOnSharedPreferenceChangeListener(themeListener)
            notifPrefs.registerOnSharedPreferenceChangeListener(notifListener)

            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START && autoSyncEnabled) {
                    runForegroundSync()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            if (autoSyncEnabled) {
                runForegroundSync()
            }

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                foodPrefs.unregisterOnSharedPreferenceChangeListener(foodListener)
                themePrefs.unregisterOnSharedPreferenceChangeListener(themeListener)
                notifPrefs.unregisterOnSharedPreferenceChangeListener(notifListener)
                foregroundSyncJob?.cancel()
            }
        }
    }
}

@Composable
fun AccountSyncScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val session = rememberAccountSessionPreference()
    val syncMeta = rememberAccountSyncMeta()
    val autoSyncEnabled = rememberAccountAutoSyncEnabledPreference()
    val autoSyncMode = rememberAccountAutoSyncModePreference()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var busyAction by rememberSaveable { mutableStateOf<String?>(null) }
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var statusPlacement by rememberSaveable { mutableStateOf<AccountStatusPlacement?>(null) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(statusMessage, statusPlacement) {
        val message = statusMessage?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (statusPlacement == AccountStatusPlacement.EMAIL_AUTH) {
            return@LaunchedEffect
        }

        delay(statusMessageDisplayDurationMillis(message))
        if (statusMessage == message) {
            statusMessage = null
            statusPlacement = null
        }
    }

    val canUseEmailPassword =
        email.isNotBlank() &&
                password.length >= 6 &&
                busyAction == null &&
                isCloudAccountConfigured()
    val canUseGoogle = busyAction == null && isGoogleAccountConfigured()

    fun createAccount() {
        statusMessage = null
        statusPlacement = null
        busyAction = "create"
        scope.launch {
            runCatching {
                val signedIn = signUpWithEmailPassword(email.trim(), password)
                val result = bootstrapSignedInAccount(appContext, signedIn)
                saveAccountSession(appContext, result.session)
            }.onFailure { failure ->
                statusMessage = failure.message
                statusPlacement = AccountStatusPlacement.EMAIL_AUTH
            }
            busyAction = null
        }
    }

    fun signIn() {
        statusMessage = null
        statusPlacement = null
        busyAction = "signin"
        scope.launch {
            runCatching {
                val signedIn = signInWithEmailPassword(email.trim(), password)
                val result = bootstrapSignedInAccount(appContext, signedIn)
                saveAccountSession(appContext, result.session)
            }.onFailure { failure ->
                statusMessage = failure.message
                statusPlacement = AccountStatusPlacement.EMAIL_AUTH
            }
            busyAction = null
        }
    }

    fun continueWithGoogle() {
        statusMessage = null
        statusPlacement = null
        busyAction = "google"
        scope.launch {
            runCatching {
                val signedIn = launchGoogleSignIn(context)
                val result = bootstrapSignedInAccount(appContext, signedIn)
                saveAccountSession(appContext, result.session)
            }.onFailure { failure ->
                statusMessage = failure.message
                statusPlacement = AccountStatusPlacement.GOOGLE_AUTH
            }
            busyAction = null
        }
    }

    fun syncNow() {
        statusMessage = null
        statusPlacement = null
        busyAction = "sync"
        scope.launch {
            runCatching {
                val currentSession = session ?: return@runCatching
                val synced = pushSnapshotToCloud(appContext, currentSession)
                saveAccountSession(appContext, synced)
            }.onFailure { failure ->
                statusMessage = failure.message
                statusPlacement = AccountStatusPlacement.SYNC_ACTION
            }
            busyAction = null
        }
    }

    fun restoreCloud() {
        statusMessage = null
        statusPlacement = null
        busyAction = "restore"
        scope.launch {
            runCatching {
                val currentSession = session ?: return@runCatching
                val refreshed = restoreCloudDataNow(appContext, currentSession)
                saveAccountSession(appContext, refreshed)
            }.onFailure { failure ->
                statusMessage = failure.message
                statusPlacement = AccountStatusPlacement.SYNC_ACTION
            }
            busyAction = null
        }
    }

    fun updateAutoSyncEnabled(enabled: Boolean) {
        saveAccountAutoSyncEnabled(appContext, enabled)
    }

    fun updateAutoSyncMode(mode: AccountAutoSyncMode) {
        saveAccountAutoSyncMode(appContext, mode)
    }

    fun clearEmailAuthMessageIfNeeded() {
        if (statusPlacement == AccountStatusPlacement.EMAIL_AUTH) {
            statusMessage = null
            statusPlacement = null
        }
    }

    ScaffoldWithTopBar(title = "Account", navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = accountAuthMaxWidth)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (session == null) {
                    SignedOutAccountContent(
                        email = email,
                        onEmailChange = { updatedEmail ->
                            if (updatedEmail != email) {
                                clearEmailAuthMessageIfNeeded()
                            }
                            email = updatedEmail
                        },
                        password = password,
                        onPasswordChange = { updatedPassword ->
                            if (updatedPassword != password) {
                                clearEmailAuthMessageIfNeeded()
                            }
                            password = updatedPassword
                        },
                        canUseEmailPassword = canUseEmailPassword,
                        canUseGoogle = canUseGoogle,
                        showSetupHint = !isCloudAccountConfigured(),
                        busyAction = busyAction,
                        emailAuthMessage = statusMessage?.takeIf {
                            statusPlacement == AccountStatusPlacement.EMAIL_AUTH
                        },
                        googleAuthMessage = statusMessage?.takeIf {
                            statusPlacement == AccountStatusPlacement.GOOGLE_AUTH
                        },
                        onContinueWithGoogle = ::continueWithGoogle,
                        onLogIn = ::signIn,
                        onSignUp = ::createAccount
                    )
                } else {
                    SignedInAccountContent(
                        session = session,
                        lastSyncedAt = syncMeta.lastSyncedAt,
                        autoSyncEnabled = autoSyncEnabled,
                        autoSyncMode = autoSyncMode,
                        busyAction = busyAction,
                        syncActionMessage = statusMessage?.takeIf {
                            statusPlacement == AccountStatusPlacement.SYNC_ACTION
                        },
                        autoSyncErrorMessage = syncMeta.lastError,
                        onAutoSyncEnabledChange = ::updateAutoSyncEnabled,
                        onAutoSyncModeChange = ::updateAutoSyncMode,
                        onSyncNow = ::syncNow,
                        onRestoreCloud = ::restoreCloud,
                        onLogOut = { showLogoutDialog = true }
                    )
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }

    if (showLogoutDialog && session != null) {
        GlassAlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { DialogTitleText("Log out?") },
            text = {
                DialogBodyText("Your cloud data stays in the account. This only signs this device out.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        busyAction = "logout"
                        scope.launch {
                            clearCredentialState(appContext)
                            clearAccountSession(appContext)
                            busyAction = null
                        }
                    }
                ) {
                    DialogDestructiveText("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
private fun SignedOutAccountContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    canUseEmailPassword: Boolean,
    canUseGoogle: Boolean,
    showSetupHint: Boolean,
    busyAction: String?,
    emailAuthMessage: String?,
    googleAuthMessage: String?,
    onContinueWithGoogle: () -> Unit,
    onLogIn: () -> Unit,
    onSignUp: () -> Unit
) {
    val hasEmailAuthError = !emailAuthMessage.isNullOrBlank()
    var displayedEmailAuthMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(emailAuthMessage) {
        if (!emailAuthMessage.isNullOrBlank()) {
            displayedEmailAuthMessage = emailAuthMessage
        }
    }

    val showSignUpHint = displayedEmailAuthMessage == EMAIL_PASSWORD_INCORRECT_MESSAGE
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sync your pantry",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Sign in to keep your pantry and settings together.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(14.dp))

        if (showSetupHint) {
            AccountSetupHint()
            Spacer(Modifier.height(14.dp))
        }

        AccountPillTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Email",
            isError = hasEmailAuthError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(14.dp))

        AccountPillTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Password",
            isError = hasEmailAuthError,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(Modifier.height(14.dp))

        AnimatedVisibility(
            visible = hasEmailAuthError,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                displayedEmailAuthMessage?.takeIf { it.isNotBlank() }?.let { message ->
                    AccountInlineStatusMessage(
                        message = message,
                        isError = true
                    )
                }

                if (showSignUpHint) {
                    Text(
                        text = "Don't have an account? Sign Up.",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryPillButton(
                text = "Log in",
                busy = busyAction == "signin",
                enabled = canUseEmailPassword,
                onClick = onLogIn,
                modifier = Modifier.weight(1f)
            )
            SecondaryPillButton(
                text = "Sign up",
                busy = busyAction == "create",
                enabled = canUseEmailPassword,
                onClick = onSignUp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        AccountSectionDivider()

        Spacer(Modifier.height(14.dp))

        GooglePillButton(
            text = "Continue with Google",
            busy = busyAction == "google",
            enabled = canUseGoogle,
            onClick = onContinueWithGoogle,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = !googleAuthMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                googleAuthMessage?.takeIf { it.isNotBlank() }?.let { message ->
                    AccountInlineStatusMessage(
                        message = message,
                        isError = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SignedInAccountContent(
    session: AccountSession,
    lastSyncedAt: Long,
    autoSyncEnabled: Boolean,
    autoSyncMode: AccountAutoSyncMode,
    busyAction: String?,
    syncActionMessage: String?,
    autoSyncErrorMessage: String?,
    onAutoSyncEnabledChange: (Boolean) -> Unit,
    onAutoSyncModeChange: (AccountAutoSyncMode) -> Unit,
    onSyncNow: () -> Unit,
    onRestoreCloud: () -> Unit,
    onLogOut: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MatchingPillCard(
            modifier = Modifier.fillMaxWidth(),
            shape = accountAuthPillShape,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = session.displayName?.takeIf { it.isNotBlank() } ?: "Signed in",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = session.email,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = when (session.provider) {
                        AccountProvider.EMAIL -> "Email + password account"
                        AccountProvider.GOOGLE -> "Google account"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                lastSyncedAt.takeIf { it > 0L }?.let {
                    Text(
                        text = "Last synced ${formatSyncTime(it)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryPillButton(
                text = "Sync now",
                busy = busyAction == "sync",
                enabled = busyAction == null,
                onClick = onSyncNow,
                modifier = Modifier.weight(1f)
            )
            SecondaryPillButton(
                text = "Load backup",
                busy = busyAction == "restore",
                enabled = busyAction == null,
                onClick = onRestoreCloud,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(
            visible = !syncActionMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            syncActionMessage?.takeIf { it.isNotBlank() }?.let { message ->
                AccountInlineStatusMessage(
                    message = message,
                    isError = true
                )
            }
        }

        AccountAutoSyncToggleCard(
            enabled = autoSyncEnabled,
            controlsEnabled = busyAction == null,
            onCheckedChange = onAutoSyncEnabledChange
        )

        AccountSyncScheduleCard(
            autoSyncEnabled = autoSyncEnabled,
            selectedMode = autoSyncMode,
            enabled = busyAction == null,
            onModeSelected = onAutoSyncModeChange
        )

        AnimatedVisibility(
            visible = !autoSyncErrorMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            autoSyncErrorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                AccountInlineStatusMessage(
                    message = message,
                    isError = true
                )
            }
        }

        AccountLogoutCard(
            enabled = busyAction == null,
            onLogOut = onLogOut
        )
    }
}

@Composable
private fun AccountLogoutCard(
    enabled: Boolean,
    onLogOut: () -> Unit
) {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shape = accountAuthPillShape,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sign out of this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onLogOut,
                enabled = enabled,
                shape = accountAuthPillShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = if (enabled) 0.42f else 0.18f)
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.42f)
                )
            ) {
                Text(
                    text = "Log out",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AccountAutoSyncToggleCard(
    enabled: Boolean,
    controlsEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shape = accountAuthPillShape,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (enabled) {
                        "Sync when the app opens and back up in the background."
                    } else {
                        "Turn this on to sync automatically when the app opens."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = enabled,
                enabled = controlsEnabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun AccountSyncScheduleCard(
    autoSyncEnabled: Boolean,
    selectedMode: AccountAutoSyncMode,
    enabled: Boolean,
    onModeSelected: (AccountAutoSyncMode) -> Unit
) {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shape = accountAuthPillShape,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sync schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (autoSyncEnabled) {
                    "Choose how often background sync should run. Daily is safer."
                } else {
                    "Pick the background schedule to use when Auto sync is on."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountAutoSyncModeButton(
                    text = "Daily",
                    supportingText = "Safer",
                    selected = selectedMode == AccountAutoSyncMode.DAILY,
                    enabled = enabled,
                    onClick = { onModeSelected(AccountAutoSyncMode.DAILY) },
                    modifier = Modifier.weight(1f)
                )
                AccountAutoSyncModeButton(
                    text = "Weekly",
                    selected = selectedMode == AccountAutoSyncMode.WEEKLY,
                    enabled = enabled,
                    onClick = { onModeSelected(AccountAutoSyncMode.WEEKLY) },
                    modifier = Modifier.weight(1f)
                )
                AccountAutoSyncModeButton(
                    text = "Monthly",
                    selected = selectedMode == AccountAutoSyncMode.MONTHLY,
                    enabled = enabled,
                    onClick = { onModeSelected(AccountAutoSyncMode.MONTHLY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AccountAutoSyncModeButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerColor =
        if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.24f else 0.14f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkTheme) 0.46f else 0.88f)
        }
    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.44f else 0.28f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.28f else 0.16f)
        }

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(60.dp),
        shape = accountAuthPillShape,
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AccountSetupHint() {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shape = accountAuthPillShape,
        shadowElevation = 2.dp
    ) {
        Text(
            text = "This build still needs Firebase account setup in local.properties and Firebase Authentication.",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AccountStatusPill(
    message: String,
    isError: Boolean
) {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shape = accountAuthPillShape,
        shadowElevation = 2.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun AccountInlineStatusMessage(
    message: String,
    isError: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier
                .padding(top = 1.dp)
                .size(15.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun AccountPillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val fieldContainerColor =
        MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkTheme) 0.48f else 0.92f)
    val errorBorderColor =
        MaterialTheme.colorScheme.error.copy(alpha = if (isDarkTheme) 0.74f else 0.62f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isError) errorBorderColor else Color.Transparent,
                shape = accountAuthPillShape
            ),
        singleLine = true,
        placeholder = { Text(placeholder) },
        shape = accountAuthPillShape,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            errorTextColor = textColor,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            errorPlaceholderColor = placeholderColor,
            focusedContainerColor = fieldContainerColor,
            unfocusedContainerColor = fieldContainerColor,
            errorContainerColor = fieldContainerColor,
            disabledContainerColor = fieldContainerColor.copy(alpha = 0.72f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            errorCursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun SoftPillButton(
    text: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(accountAuthControlHeight),
        shape = accountAuthPillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkTheme) 0.5f else 0.94f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        ActionLabel(text = text, busy = busy)
    }
}

@Composable
private fun GooglePillButton(
    text: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val googleContainerColor =
        if (isDarkTheme) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        }
    val googleBorder =
        if (isDarkTheme) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
            )
        } else {
            null
        }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(accountAuthControlHeight),
        shape = accountAuthPillShape,
        border = googleBorder,
        contentPadding = PaddingValues(horizontal = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = googleContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(20.dp)
                )
            }
            Text(
                text = text,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AccountSectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    )
}

@Composable
private fun PrimaryPillButton(
    text: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(accountAuthControlHeight),
        shape = accountAuthPillShape
    ) {
        ActionLabel(text = text, busy = busy)
    }
}

@Composable
private fun SecondaryPillButton(
    text: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(accountAuthControlHeight),
        shape = accountAuthPillShape
    ) {
        ActionLabel(text = text, busy = busy)
    }
}

@Composable
private fun ActionLabel(
    text: String,
    busy: Boolean
) {
    if (busy) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(16.dp)
                    .height(16.dp),
                strokeWidth = 2.dp
            )
            Text(
                text
            )
        }
    } else {
        Text(text)
    }
}

@Composable
private fun ScaffoldWithTopBar(
    title: String,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SlimTopBar(
                title = title,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

private fun formatSyncTime(timestampMs: Long): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.getDefault())
    return formatter.format(
        Instant.ofEpochMilli(timestampMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )
}
