@file:Suppress("DEPRECATION")

package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale
import java.time.ZoneId
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val THEME_PREFS = "app_settings"
private const val THEME_KEY = "theme_mode"
private const val COUNTDOWN_FORMAT_KEY = "countdown_format"
private const val LOCAL_PROFILE_NAME_KEY = "local_profile_name"
private const val LOCAL_PROFILE_PHOTO_URI_KEY = "local_profile_photo_uri"
private const val LOCAL_PROFILE_NAME_MAX_LENGTH = 15
private var hasShownRecipeLaunchIntroThisProcess = false

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CountdownFormat { DAYS_ONLY, MONTHS_AND_DAYS, YEARS_MONTHS_DAYS }

enum class SelectionPurpose { DELETE, CATEGORY }

private data class AutoDeletePreset(
    val days: Long,
    val label: String
)

private enum class AutoDeleteUnit(val label: String, val daysMultiplier: Long) {
    DAYS("Days", 1L),
    WEEKS("Weeks", 7L),
    MONTHS("Months", 30L)
}

private suspend fun LazyListState.animateScrollToTopSlowly(
    fallbackItemSizePx: Float,
    durationMillis: Int = 560
) {
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) return

    val averageVisibleItemSizePx =
        layoutInfo.visibleItemsInfo
            .map { it.size }
            .average()
            .takeIf { !it.isNaN() && it > 0.0 }
            ?.toFloat()
            ?: fallbackItemSizePx

    val estimatedDistancePx =
        (firstVisibleItemIndex * averageVisibleItemSizePx) + firstVisibleItemScrollOffset

    if (estimatedDistancePx <= 0f) {
        scrollToItem(0)
        return
    }

    scroll {
        var previousValue = 0f
        animate(
            initialValue = 0f,
            targetValue = estimatedDistancePx,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            val delta = value - previousValue
            previousValue = value
            scrollBy(-delta)
        }
    }

    scrollToItem(0)
}

fun loadThemeMode(context: Context): ThemeMode {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS,
        Context.MODE_PRIVATE)
    val raw = prefs.getString(THEME_KEY, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name

    return when (raw.lowercase()) {
        "system" -> ThemeMode.SYSTEM
        "light"  -> ThemeMode.LIGHT
        "dark"   -> ThemeMode.DARK
        else     -> runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }
}

fun saveThemeMode(context: Context, mode: ThemeMode) {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS,
        Context.MODE_PRIVATE)
    prefs.edit { putString(THEME_KEY, mode.name) }
}

fun loadCountdownFormat(context: Context): CountdownFormat {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    val raw = prefs.getString(COUNTDOWN_FORMAT_KEY, CountdownFormat.DAYS_ONLY.name)
        ?: CountdownFormat.DAYS_ONLY.name

    return when (raw.lowercase(Locale.ROOT)) {
        "days_only" -> CountdownFormat.DAYS_ONLY
        "months_and_days" -> CountdownFormat.MONTHS_AND_DAYS
        "years_months_days" -> CountdownFormat.YEARS_MONTHS_DAYS
        else -> runCatching { CountdownFormat.valueOf(raw) }
            .getOrDefault(CountdownFormat.DAYS_ONLY)
    }
}

fun saveCountdownFormat(context: Context, format: CountdownFormat) {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    prefs.edit { putString(COUNTDOWN_FORMAT_KEY, format.name) }
}

private fun loadLocalProfileName(context: Context): String {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    return prefs.getString(LOCAL_PROFILE_NAME_KEY, "").orEmpty().trim()
        .take(LOCAL_PROFILE_NAME_MAX_LENGTH)
}

private fun saveLocalProfileName(
    context: Context,
    name: String
) {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    prefs.edit {
        putString(LOCAL_PROFILE_NAME_KEY, name.trim().take(LOCAL_PROFILE_NAME_MAX_LENGTH))
    }
}

private fun loadLocalProfilePhotoUri(context: Context): String {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    return prefs.getString(LOCAL_PROFILE_PHOTO_URI_KEY, "").orEmpty().trim()
}

private fun saveLocalProfilePhotoUri(
    context: Context,
    photoUri: String
) {
    val prefs = context.applicationContext.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    prefs.edit {
        if (photoUri.isBlank()) {
            remove(LOCAL_PROFILE_PHOTO_URI_KEY)
        } else {
            putString(LOCAL_PROFILE_PHOTO_URI_KEY, photoUri.trim())
        }
    }
}

private fun countdownFormatLabel(format: CountdownFormat): String {
    return when (format) {
        CountdownFormat.DAYS_ONLY -> "Days only"
        CountdownFormat.MONTHS_AND_DAYS -> "Months + days"
        CountdownFormat.YEARS_MONTHS_DAYS -> "Years + months + days"
    }
}

private fun countdownFormatDescription(format: CountdownFormat): String {
    return when (format) {
        CountdownFormat.DAYS_ONLY -> "Show the full day count, like 60 days left."
        CountdownFormat.MONTHS_AND_DAYS -> "Show a shorter format like 2m or 2m 5d."
        CountdownFormat.YEARS_MONTHS_DAYS -> "Show longer countdowns like 2y 3m 7d."
    }
}

private fun autoDeleteDurationLabel(days: Long): String {
    return when (days) {
        1L -> "1 day"
        7L -> "1 week"
        14L -> "2 weeks"
        30L -> "1 month"
        90L -> "3 months"
        180L -> "6 months"
        360L -> "12 months"
        else -> when {
            days % 30L == 0L -> "${days / 30L} months"
            days % 7L == 0L -> "${days / 7L} weeks"
            else -> "$days days"
        }
    }
}

@Composable
private fun rememberCountdownFormatPreference(): CountdownFormat {
    val context = LocalContext.current
    val appCtx = context.applicationContext
    val prefs = remember(appCtx) {
        appCtx.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    }
    var countdownFormat by remember { mutableStateOf(loadCountdownFormat(appCtx)) }

    DisposableEffect(prefs, appCtx) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == COUNTDOWN_FORMAT_KEY) {
                    countdownFormat = loadCountdownFormat(appCtx)
                }
            }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return countdownFormat
}

private enum class GlassTone { CHROME, CARD, SEARCH, ACCENT }

private data class GlassPalette(
    val chrome: Color,
    val card: Color,
    val search: Color,
    val accent: Color,
    val border: Color,
    val highlight: Color,
    val tint: Color
)

@Composable
private fun rememberGlassPalette(): GlassPalette {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    return remember(scheme, isDark) {
        if (isDark) {
            GlassPalette(
                chrome = scheme.surfaceContainerHigh.copy(alpha = 0.60f),
                card = scheme.surfaceVariant.copy(alpha = 0.46f),
                search = scheme.surfaceContainer.copy(alpha = 0.64f),
                accent = scheme.primary.copy(alpha = 0.20f),
                border = scheme.outlineVariant.copy(alpha = 0.92f),
                highlight = Color.White.copy(alpha = 0.10f),
                tint = scheme.primary.copy(alpha = 0.11f)
            )
        } else {
            GlassPalette(
                chrome = Color.White.copy(alpha = 0.64f),
                card = scheme.surfaceVariant.copy(alpha = 0.50f),
                search = Color.White.copy(alpha = 0.74f),
                accent = scheme.primary.copy(alpha = 0.14f),
                border = scheme.outlineVariant.copy(alpha = 0.82f),
                highlight = Color.White.copy(alpha = 0.46f),
                tint = scheme.primary.copy(alpha = 0.05f)
            )
        }
    }
}

@Composable
private fun glassToneColor(tone: GlassTone): Color {
    val palette = rememberGlassPalette()
    return when (tone) {
        GlassTone.CHROME -> palette.chrome
        GlassTone.CARD -> palette.card
        GlassTone.SEARCH -> palette.search
        GlassTone.ACCENT -> palette.accent
    }
}

@Composable
private fun glassBorderStroke(alpha: Float = 1f): BorderStroke {
    val palette = rememberGlassPalette()
    return BorderStroke(1.dp, palette.border.copy(alpha = alpha))
}

@Composable
private fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    tone: GlassTone = GlassTone.CARD,
    containerColor: Color? = null,
    borderColor: Color? = null,
    showDecorativeOverlays: Boolean = true,
    shadowElevation: Dp = if (tone == GlassTone.CHROME) 18.dp else 10.dp,
    content: @Composable () -> Unit
) {
    val palette = rememberGlassPalette()

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor ?: glassToneColor(tone),
        tonalElevation = 0.dp,
        shadowElevation = shadowElevation,
        border = BorderStroke(1.dp, borderColor ?: palette.border)
    ) {
        Box {
            if (showDecorativeOverlays) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    palette.highlight,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    palette.tint,
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(900f, 700f)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    palette.tint.copy(alpha = palette.tint.alpha * 0.55f)
                                )
                            )
                        )
                )
            }

            content()
        }
    }
}

@Composable
private fun FrostedGlassFill(
    modifier: Modifier = Modifier,
    primaryBlur: Dp = 100.dp,
    accentBlur: Dp = 28.dp,
    content: @Composable () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(primaryBlur)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.75f),
                                Color.White.copy(alpha = 0.75f)
                            )
                        }
                    )
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(accentBlur)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (isDarkTheme) 0.10f else 0.07f
                            ),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(700f, 420f)
                    )
                )
        )

        content()
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    tone: GlassTone = GlassTone.CARD,
    containerColor: Color? = null,
    shadowElevation: Dp = 8.dp,
    showDecorativeOverlays: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier =
        if (onClick != null) {
            modifier
                .clip(shape)
                .clickable(onClick = onClick)
        } else {
            modifier
        }

    GlassSurface(
        modifier = clickableModifier,
        shape = shape,
        tone = tone,
        containerColor = containerColor,
        showDecorativeOverlays = showDecorativeOverlays,
        shadowElevation = shadowElevation,
        content = content
    )
}

@Composable
private fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    tone: GlassTone = GlassTone.CHROME,
    containerColor: Color? = null,
    showDecorativeOverlays: Boolean = true,
    shadowElevation: Dp = 8.dp,
    primaryBlur: Dp = 56.dp,
    accentBlur: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier =
        if (onClick != null) {
            modifier
                .clip(shape)
                .clickable(onClick = onClick)
        } else {
            modifier
        }

    GlassSurface(
        modifier = clickableModifier,
        shape = shape,
        tone = tone,
        containerColor = containerColor,
        showDecorativeOverlays = showDecorativeOverlays,
        shadowElevation = shadowElevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            FrostedGlassFill(
                modifier = Modifier.matchParentSize(),
                primaryBlur = primaryBlur,
                accentBlur = accentBlur
            ) {}
            content()
        }
    }
}

@Composable
private fun BlackGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    shadowElevation: Dp = 8.dp,
    primaryBlur: Dp = 32.dp,
    accentBlur: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    FrostedGlassCard(
        modifier = modifier,
        shape = shape,
        tone = GlassTone.CHROME,
        containerColor = Color.Transparent,
        showDecorativeOverlays = false,
        shadowElevation = shadowElevation,
        primaryBlur = primaryBlur,
        accentBlur = accentBlur,
        onClick = onClick,
        content = content
    )
}

@Composable
fun MatchingPillCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(25.dp),
    shadowElevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    ExactFrostedPillCard(
        modifier = modifier,
        shape = shape,
        shadowElevation = shadowElevation,
        onClick = onClick,
        content = content
    )
}

@Composable
private fun ExactFrostedPillCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(25.dp),
    shadowElevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier =
        if (onClick != null) {
            modifier
                .clip(shape)
                .clickable(onClick = onClick)
        } else {
            modifier
        }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    GlassSurface(
        modifier = clickableModifier,
        shape = shape,
        tone = GlassTone.CHROME,
        containerColor = Color.Transparent,
        showDecorativeOverlays = false,
        shadowElevation = shadowElevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors =
                                if (isDarkTheme) {
                                    listOf(
                                        Color.Black.copy(alpha = 1f),
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 1f),
                                        Color.White.copy(alpha = 0.75f)
                                    )
                                }
                        )
                    )
            )

            FrostedGlassFill(
                modifier = Modifier.matchParentSize(),
                primaryBlur = if (isDarkTheme) 40.dp else 48.dp,
                accentBlur = if (isDarkTheme) 12.dp else 14.dp
            ) {}

            content()
        }
    }
}

@Composable
private fun PantryListGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(25.dp),
    shadowElevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier =
        if (onClick != null) {
            modifier
                .clip(shape)
                .clickable(onClick = onClick)
        } else {
            modifier
        }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val palette = rememberGlassPalette()
    val topHighlight =
        if (isDarkTheme) {
            Color.White.copy(alpha = 0.05f)
        } else {
            Color.White.copy(alpha = 0.24f)
        }
    val baseStart =
        if (isDarkTheme) {
            Color.Black.copy(alpha = 0.92f)
        } else {
            Color.White.copy(alpha = 0.94f)
        }
    val baseEnd =
        if (isDarkTheme) {
            Color.Black.copy(alpha = 0.72f)
        } else {
            Color.White.copy(alpha = 0.82f)
        }
    val accentTint =
        MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.06f else 0.04f)
    val bottomDepth =
        MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.03f else 0.02f)
    val borderColor = palette.border.copy(alpha = if (isDarkTheme) 0.84f else 0.92f)

    Box(
        modifier = clickableModifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .drawWithCache {
                    val baseBrush =
                        Brush.verticalGradient(
                            colors = listOf(baseStart, baseEnd)
                        )
                    val topGlowBrush =
                        Brush.verticalGradient(
                            colors = listOf(topHighlight, Color.Transparent)
                        )
                    val accentBrush =
                        Brush.linearGradient(
                            colors = listOf(accentTint, Color.Transparent),
                            start = Offset(0f, 0f),
                            end = Offset(size.width * 1.15f, size.height * 0.78f)
                        )
                    val depthBrush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, bottomDepth)
                        )

                    onDrawBehind {
                        drawRect(baseBrush)
                        drawRect(topGlowBrush)
                        drawRect(accentBrush)
                        drawRect(depthBrush)
                    }
                }
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape
                )
        ) {
            content()
        }
    }
}

@Composable
private fun SelectionControlsWarmup() {
    Box(
        modifier = Modifier
            .size(1.dp)
            .graphicsLayer {
                alpha = 0f
                scaleX = 0f
                scaleY = 0f
            }
    ) {
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Checkbox(
                modifier = Modifier.graphicsLayer {
                    alpha = 1f
                    scaleX = 1f
                    scaleY = 1f
                },
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}

@Composable
private fun HomeFloatingButtons(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onAddPositioned: ((Rect) -> Unit)? = null,
    onAiPositioned: ((Rect) -> Unit)? = null,
    onAddClick: () -> Unit,
    onAiClick: () -> Unit
) {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    var keyboardVisible by remember { mutableStateOf(false) }
    var showForKeyboard by remember { mutableStateOf(true) }

    LaunchedEffect(density) {
        snapshotFlow { imeInsets.getBottom(density) > 0 }
            .collect { keyboardVisible = it }
    }

    LaunchedEffect(keyboardVisible) {
        if (keyboardVisible) {
            showForKeyboard = false
        } else {
            delay(24)
            showForKeyboard = true
        }
    }

    AnimatedVisibility(
        visible = visible && showForKeyboard,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 160,
                easing = LinearOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 120,
                easing = FastOutLinearInEasing
            )
        )
    ) {
        val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

        Column(
            modifier = Modifier
                .padding(bottom = 75.dp)
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            EditCategoriesStyledFab(
                onClick = onAiClick,
                icon = Icons.Default.AutoAwesome,
                contentDescription = "Food AI",
                backgroundTint = MaterialTheme.colorScheme.primary,
                iconTint = MaterialTheme.colorScheme.onPrimary,
                tintAlpha = if (isDarkTheme) 0.72f else 0.54f,
                onPositioned = onAiPositioned,
                fabSize = 42.dp
            )

            EditCategoriesStyledFab(
                onClick = onAddClick,
                icon = Icons.Default.Add,
                contentDescription = "Add",
                backgroundTint = MaterialTheme.colorScheme.primary,
                iconTint = MaterialTheme.colorScheme.onPrimary,
                tintAlpha = if (isDarkTheme) 0.76f else 0.62f,
                onPositioned = onAddPositioned
            )
        }
    }
}

@Composable
private fun EditCategoriesStyledFab(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    backgroundTint: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    tintAlpha: Float = 0.62f,
    onPositioned: ((Rect) -> Unit)? = null,
    fabSize: Dp = 50.dp
) {
    val shape = RoundedCornerShape(50.dp)
    val positionedModifier =
        if (onPositioned == null) {
            Modifier
        } else {
            Modifier.onGloballyPositioned { coordinates ->
                onPositioned(coordinates.boundsInRoot())
            }
        }

    EditCategoriesBackgroundSurface(
        modifier = modifier
            .size(fabSize)
            .then(positionedModifier)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        shadowElevation = 14.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(backgroundTint.copy(alpha = tintAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint
            )
        }
    }
}

@Composable
fun GlassAlertDialog(
    onDismissRequest: () -> Unit,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null
) {
    val animatedImeShiftPx = rememberAnimatedImeShiftPx(label = "glassAlertDialogImeShift")
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dialogShape = RoundedCornerShape(28.dp)
    val dialogModifier =
        Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = -animatedImeShiftPx }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val dialogContent: @Composable () -> Unit = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    title?.let {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.onSurface
                        ) {
                            ProvideTextStyle(
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                it()
                            }
                        }
                    }

                    if (text != null) {
                        if (title != null) {
                            Spacer(Modifier.height(12.dp))
                        }
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                                text()
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dismissButton?.let {
                            it()
                        }
                        confirmButton()
                    }
                }
            }

            if (isDarkTheme) {
                BlackGlassCard(
                    modifier = dialogModifier,
                    shape = dialogShape,
                    shadowElevation = 16.dp,
                    primaryBlur = 100.dp,
                    accentBlur = 28.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(dialogShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.96f),
                                        Color.Black.copy(alpha = 0.84f)
                                    )
                                )
                            )
                    ) {
                        dialogContent()
                    }
                }
            } else {
                ExactFrostedPillCard(
                    modifier = dialogModifier,
                    shape = dialogShape,
                    shadowElevation = 16.dp,
                    content = dialogContent
                )
            }
        }
    }
}

@Composable
fun DialogTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = if (color == Color.Unspecified) {
            MaterialTheme.colorScheme.onSurface
        } else {
            color
        }
    )
}

@Composable
fun DialogBodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = if (color == Color.Unspecified) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            color
        }
    )
}

@Composable
fun DialogDestructiveText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun trashIconTint(): Color {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDarkTheme) Color(0xFFFF6B63) else MaterialTheme.colorScheme.error
}

@Composable
private fun DelayedDestructiveConfirmButton(
    itemCount: Int,
    finalButtonText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    threshold: Int = 10,
    cooldownMillis: Long = 3000L
) {
    val countdownSeconds = remember(cooldownMillis) {
        max(1, ((cooldownMillis + 999L) / 1000L).toInt())
    }
    val requiresCooldown = itemCount >= threshold
    var secondsRemaining by remember(
        itemCount,
        finalButtonText,
        threshold,
        cooldownMillis
    ) {
        mutableIntStateOf(if (requiresCooldown) countdownSeconds else 0)
    }

    LaunchedEffect(
        itemCount,
        finalButtonText,
        threshold,
        cooldownMillis
    ) {
        if (!requiresCooldown) {
            secondsRemaining = 0
            return@LaunchedEffect
        }

        for (second in countdownSeconds downTo 1) {
            secondsRemaining = second
            delay(1000)
        }
        secondsRemaining = 0
    }

    val isCoolingDown = requiresCooldown && secondsRemaining > 0

    TextButton(
        onClick = onConfirm,
        enabled = !isCoolingDown,
        modifier = modifier
    ) {
        if (isCoolingDown) {
            Text(
                text = "$finalButtonText in ${secondsRemaining}s",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.64f)
            )
        } else {
            DialogDestructiveText(finalButtonText)
        }
    }
}

enum class OnboardingSpotlightTarget {
    HOME_ADD_FAB,
    HOME_AI_FAB,
    HOME_SEARCH_SORT,
    PANTRY_ITEM,
    CATEGORY_CONTROLS,
    HISTORY_TAB,
    PROFILE_TAB
}

private enum class OnboardingSwipeHint {
    LEFT,
    RIGHT
}

private data class OnboardingStep(
    val target: OnboardingSpotlightTarget,
    val title: String,
    val body: String,
    val swipeHint: OnboardingSwipeHint? = null
)

private val firstLaunchOnboardingSteps = listOf(
    OnboardingStep(
        target = OnboardingSpotlightTarget.HOME_ADD_FAB,
        title = "Add pantry items",
        body = "Use the plus button to add food, expiry date, category, and quantity."
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.HOME_AI_FAB,
        title = "AI Recipe",
        body = "Use the AI button for quick recipe ideas."
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.HOME_SEARCH_SORT,
        title = "Search, sort, and select",
        body = "Use the header icons to search your pantry, sort, and select multiple foods."
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.PANTRY_ITEM,
        title = "Read a food card",
        body = "Each card shows the expiry countdown. If you buy the same food with the same date again, the app merges it into the same batch."
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.PANTRY_ITEM,
        title = "Swipe right to edit",
        body = "Swipe a food card to the right to edit its name, date, category, or quantity.",
        swipeHint = OnboardingSwipeHint.RIGHT
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.PANTRY_ITEM,
        title = "Swipe left to delete",
        body = "Swipe a food card to the left to delete that item.",
        swipeHint = OnboardingSwipeHint.LEFT
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.CATEGORY_CONTROLS,
        title = "Categories",
        body = "Use the category row to filter your pantry, and tap the pencil to edit your categories."
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.HISTORY_TAB,
        title = "History",
        body = "History groups old, deleted, expired, and used foods so you can quickly add them back later."
    ),
    OnboardingStep(
        target = OnboardingSpotlightTarget.PROFILE_TAB,
        title = "Profile",
        body = "Profile has account sync, settings, pantry transfer, help, and more."
    )
)

private fun Rect.expandedBy(px: Float): Rect {
    return Rect(
        left = left - px,
        top = top - px,
        right = right + px,
        bottom = bottom + px
    )
}

@Composable
private fun FirstLaunchOnboardingOverlay(
    visible: Boolean,
    stepIndex: Int,
    targetBounds: Map<OnboardingSpotlightTarget, Rect>,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (!visible) return

    val safeStepIndex = stepIndex.coerceIn(firstLaunchOnboardingSteps.indices)
    val step = firstLaunchOnboardingSteps[safeStepIndex]
    val targetRect = targetBounds[step.target] ?: return
    val density = LocalDensity.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val blockerInteraction = remember { MutableInteractionSource() }
    val spotlightPaddingPx = with(density) { 14.dp.toPx() }
    val spotlightCornerPx = with(density) { 30.dp.toPx() }
    val spotlightStrokePx = with(density) { 2.dp.toPx() }
    val cardGapPx = with(density) { 18.dp.toPx() }
    val estimatedCardHeightPx = with(density) { 198.dp.toPx() }
    val scrimColor = Color.Black.copy(alpha = if (isDarkTheme) 0.58f else 0.46f)
    val spotlightBorderColor =
        MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.62f else 0.46f)
    val spotlightHaloColor = Color.White.copy(alpha = if (isDarkTheme) 0.12f else 0.22f)

    BackHandler {
        // Keep onboarding active until the user explicitly taps Skip or Done.
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = blockerInteraction,
                indication = null,
                onClick = {}
            )
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
        ) {
            drawRect(color = scrimColor)

            val spotlightRect = targetRect.expandedBy(spotlightPaddingPx)

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(spotlightRect.left, spotlightRect.top),
                size = Size(spotlightRect.width, spotlightRect.height),
                cornerRadius = CornerRadius(spotlightCornerPx, spotlightCornerPx),
                blendMode = BlendMode.Clear
            )

            drawRoundRect(
                color = spotlightBorderColor,
                topLeft = Offset(spotlightRect.left, spotlightRect.top),
                size = Size(spotlightRect.width, spotlightRect.height),
                cornerRadius = CornerRadius(spotlightCornerPx, spotlightCornerPx),
                style = Stroke(width = spotlightStrokePx)
            )

            drawRoundRect(
                color = spotlightHaloColor,
                topLeft = Offset(spotlightRect.left - spotlightPaddingPx, spotlightRect.top - spotlightPaddingPx),
                size = Size(
                    spotlightRect.width + spotlightPaddingPx * 2f,
                    spotlightRect.height + spotlightPaddingPx * 2f
                ),
                cornerRadius = CornerRadius(
                    spotlightCornerPx + spotlightPaddingPx,
                    spotlightCornerPx + spotlightPaddingPx
                ),
                style = Stroke(width = spotlightStrokePx)
            )
        }

        if (step.swipeHint != null) {
            val arrowWidthPx = with(density) { 116.dp.toPx() }
            val arrowHeightPx = with(density) { 38.dp.toPx() }
            val arrowSidePaddingPx = with(density) { 24.dp.toPx() }
            val screenWidthPx = with(density) { maxWidth.toPx() }
            val rawArrowLeftPx = (targetRect.left + targetRect.right) / 2f - arrowWidthPx / 2f
            val arrowLeft = with(density) {
                rawArrowLeftPx
                    .coerceIn(arrowSidePaddingPx, screenWidthPx - arrowWidthPx - arrowSidePaddingPx)
                    .toDp()
            }
            val arrowTop = with(density) {
                ((targetRect.top + targetRect.bottom) / 2f - arrowHeightPx / 2f).toDp()
            }

            OnboardingSwipeArrow(
                direction = step.swipeHint,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = arrowLeft, y = arrowTop)
            )
        }

        val screenHeightPx = with(density) { maxHeight.toPx() }
        val minCardTopPx = with(density) { 22.dp.toPx() }
        val maxCardTopPx = max(
            minCardTopPx,
            screenHeightPx - estimatedCardHeightPx - minCardTopPx
        )
        val targetCenterY = (targetRect.top + targetRect.bottom) / 2f
        val rawCardTopPx =
            if (targetCenterY < screenHeightPx * 0.52f) {
                targetRect.bottom + cardGapPx
            } else {
                targetRect.top - estimatedCardHeightPx - cardGapPx
            }
        val cardTop = with(density) {
            rawCardTopPx
                .coerceIn(minCardTopPx, maxCardTopPx)
                .toDp()
        }
        val animatedCardTop by animateDpAsState(
            targetValue = cardTop,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "firstLaunchOnboardingCardTop"
        )

        ExactFrostedPillCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(0, with(density) { animatedCardTop.roundToPx() })
                }
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = FastOutSlowInEasing
                    )
                ),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = step.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${safeStepIndex + 1} of ${firstLaunchOnboardingSteps.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(onClick = onSkip) {
                        Text("Skip")
                    }

                    Spacer(Modifier.width(6.dp))

                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(if (safeStepIndex == firstLaunchOnboardingSteps.lastIndex) "Done" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingSwipeArrow(
    direction: OnboardingSwipeHint,
    modifier: Modifier = Modifier
) {
    val isRight = direction == OnboardingSwipeHint.RIGHT
    val arrowColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .width(116.dp)
            .height(38.dp)
    ) {
        val centerY = size.height / 2f
        val startX = if (isRight) size.width * 0.08f else size.width * 0.92f
        val endX = if (isRight) size.width * 0.88f else size.width * 0.12f
        val headDirection = if (isRight) -1f else 1f
        val headLength = size.height * 0.46f
        val headSpread = size.height * 0.34f

        drawLine(
            color = Color.Black.copy(alpha = 0.18f),
            start = Offset(startX, centerY + 2.5f),
            end = Offset(endX, centerY + 2.5f),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = arrowColor.copy(alpha = 0.95f),
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = arrowColor.copy(alpha = 0.95f),
            start = Offset(endX, centerY),
            end = Offset(endX + (headDirection * headLength), centerY - headSpread),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = arrowColor.copy(alpha = 0.95f),
            start = Offset(endX, centerY),
            end = Offset(endX + (headDirection * headLength), centerY + headSpread),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun AppBackdrop(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    Box(
        modifier = modifier.drawWithCache {
            if (isDark) {
                onDrawBehind {
                    drawRect(Color.Black)
                }
            } else {
                val glowRadius = max(size.width, size.height) * 0.85f
                val baseBrush =
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFDFEFF),
                            scheme.background,
                            Color(0xFFF2F7FD)
                        )
                    )
                val primaryGlow =
                    Brush.radialGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.08f),
                        radius = glowRadius
                    )
                val tertiaryGlow =
                    Brush.radialGradient(
                        colors = listOf(
                            scheme.tertiary.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.92f, size.height * 0.88f),
                        radius = glowRadius
                    )

                onDrawBehind {
                    drawRect(baseBrush)
                    drawRect(primaryGlow)
                    drawRect(tertiaryGlow)
                }
            }
        }
    )
}

@Composable
fun SlimTopBar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 3.dp)
    ) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            tone = GlassTone.CHROME
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                } else {
                    Spacer(Modifier.width(48.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.weight(1f))
                actions()
            }
        }
    }
}

@Composable
fun AppTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MyApplicationTheme(
        darkTheme = dark,
        dynamicColor = false,
    ) {
        ApplySystemBarStyle(isDark = dark)
        content()
    }
}

@Composable
private fun ApplySystemBarStyle(isDark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }
}

@Composable
private fun OverrideStatusBarColor(
    color: Color,
    isDarkIcons: Boolean
) {
    val view = LocalView.current
    val defaultLightStatusBars = MaterialTheme.colorScheme.background.luminance() > 0.5f
    if (view.isInEditMode) return

    DisposableEffect(view, color, isDarkIcons) {
        val window = (view.context as Activity).window
        val insetsController = WindowInsetsControllerCompat(window, view)

        window.statusBarColor = color.toArgb()
        insetsController.isAppearanceLightStatusBars = isDarkIcons

        onDispose {
            window.statusBarColor = Color.Transparent.toArgb()
            insetsController.isAppearanceLightStatusBars = defaultLightStatusBars
        }
    }
}

@Composable
private fun OverrideNavigationBarColor(
    color: Color,
    isDarkIcons: Boolean
) {
    val view = LocalView.current
    val defaultLightNavigationBars = MaterialTheme.colorScheme.background.luminance() > 0.5f
    if (view.isInEditMode) return

    DisposableEffect(view, color, isDarkIcons) {
        val window = (view.context as Activity).window
        val insetsController = WindowInsetsControllerCompat(window, view)

        window.navigationBarColor = color.toArgb()
        insetsController.isAppearanceLightNavigationBars = isDarkIcons

        onDispose {
            window.navigationBarColor = Color.Transparent.toArgb()
            insetsController.isAppearanceLightNavigationBars = defaultLightNavigationBars
        }
    }
}

@Composable
private fun OverlayBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val view = LocalView.current
    val currentOnBack = rememberUpdatedState(onBack)

    DisposableEffect(view, enabled) {
        if (!enabled) {
            onDispose {}
        } else {
            val activity = view.context as? Activity
            if (activity == null) {
                onDispose {}
            } else {
                val callback = OnBackInvokedCallback {
                    currentOnBack.value()
                }

                activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_OVERLAY,
                    callback
                )

                onDispose {
                    activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(callback)
                }
            }
        }
    }
}

@Composable
private fun rememberAnimatedImeShiftPx(
    multiplier: Float = 0.5f,
    label: String
): Float {
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density).toFloat()
    val targetShiftPx = imeBottomPx * multiplier

    val animatedShiftPx by animateFloatAsState(
        targetValue = targetShiftPx,
        animationSpec = tween(
            durationMillis = 260,
            easing = LinearOutSlowInEasing
        ),
        label = label
    )

    return animatedShiftPx
}

@Immutable
data class FoodItem(
    val name: String,
    val expiry: String,
    val category: String? = null,
    val quantity: Int = MIN_FOOD_QUANTITY
)

private const val MIN_FOOD_QUANTITY = 1
private const val MAX_FOOD_QUANTITY = 999

private data class FoodBatchKey(
    val nameKey: String,
    val expiryKey: String
)

private data class SortedFoodSnapshot(
    val item: FoodItem,
    val expiryBucket: Int,
    val expiryDistance: Int,
    val nameKey: String,
    val categoryBucket: Int,
    val categoryKey: String
)

private enum class PantrySortMode(val label: String) {
    NEAREST_EXPIRY("Nearest expiry"),
    NAME_A_TO_Z("A-Z"),
    CATEGORY("Category")
}

private data class HistoryEntry(
    val name: String,
    val lastUsedAt: Long = System.currentTimeMillis()
)

private data class HistoryDateSection(
    val title: String,
    val entries: List<HistoryEntry>
)

private data class ProfileStats(
    val foodsSaved: Int,
    val foodsExpired: Int,
    val mostUsedCategory: String,
    val wasteAvoided: Int
)

private data class ExpiringFoodHint(
    val name: String,
    val daysLeft: Int
)

private enum class RecipeChatRole { USER, ASSISTANT }

private data class RecipeChatMessage(
    val role: RecipeChatRole,
    val text: String,
    val resolvedIngredients: List<String> = emptyList(),
    val recipes: List<RecipeSuggestion> = emptyList(),
    val isError: Boolean = false
)

private data class SavedRecipe(
    val recipe: RecipeSuggestion,
    val savedAt: Long = System.currentTimeMillis()
)

private class RecipeScreenSessionState {
    var expiringPromptDismissed by mutableStateOf(false)
    var messagesJson by mutableStateOf("[]")
    var previousIngredientsJson by mutableStateOf("[]")
    var shownRecipeKeysJson by mutableStateOf("[]")
    var conversationId by mutableStateOf("recipe-session-${System.currentTimeMillis()}")
}

private val RecipeScreenSessionStateSaver = listSaver<RecipeScreenSessionState, String>(
    save = { state ->
        listOf(
            state.expiringPromptDismissed.toString(),
            state.messagesJson,
            state.previousIngredientsJson,
            state.conversationId,
            state.shownRecipeKeysJson
        )
    },
    restore = { saved ->
        RecipeScreenSessionState().apply {
            expiringPromptDismissed = saved.getOrNull(0)?.toBooleanStrictOrNull() ?: false
            messagesJson = saved.getOrNull(1) ?: "[]"
            previousIngredientsJson = saved.getOrNull(2) ?: "[]"
            conversationId = saved.getOrNull(3) ?: "recipe-session-${System.currentTimeMillis()}"
            shownRecipeKeysJson = saved.getOrNull(4) ?: "[]"
        }
    }
)

@Composable
private fun rememberRecipeScreenSessionState(): RecipeScreenSessionState {
    return rememberSaveable(saver = RecipeScreenSessionStateSaver) {
        RecipeScreenSessionState()
    }
}

private const val FOOD_PREFS = "food_prefs"
private const val FOOD_LIST_KEY = "food_list"
private const val HISTORY_LIST_KEY = "history_list"
private const val CATEGORIES_LIST_KEY = "categories_list"
private const val SAVED_RECIPES_KEY = "saved_recipes"
private const val HISTORY_AUTO_DELETE_ENABLED_KEY = "history_auto_delete_enabled"
private const val HISTORY_RETENTION_DAYS_KEY = "history_retention_days"
private const val EXPIRED_FOOD_AUTO_REMOVE_ENABLED_KEY = "expired_food_auto_remove_enabled"
private const val EXPIRED_FOOD_AUTO_REMOVE_DAYS_KEY = "expired_food_auto_remove_days"
private const val ONBOARDING_COMPLETED_KEY = "first_launch_onboarding_completed"
private const val ONBOARDING_DEMO_SEEDED_KEY = "first_launch_demo_seeded"
private const val ONBOARDING_DEMO_FOOD_NAME = "Example"
private const val ONBOARDING_DEMO_CATEGORY_NAME = "Example"
private const val NOTIF_FIRST_PROMPT_SHOWN_KEY = "notif_first_prompt_shown"
private const val DEFAULT_HISTORY_RETENTION_DAYS = 30L
private const val MIN_HISTORY_RETENTION_DAYS = 30L
private const val MAX_HISTORY_RETENTION_DAYS = 360L
private const val DEFAULT_EXPIRED_FOOD_AUTO_REMOVE_DAYS = 7L
private const val MIN_EXPIRED_FOOD_AUTO_REMOVE_DAYS = 1L
private const val MAX_EXPIRED_FOOD_AUTO_REMOVE_DAYS = 180L
private const val DAY_IN_MILLIS = 86_400_000L
private const val SWIPE_DELETE_EXIT_DURATION_MS = 420
private const val SWIPE_DELETE_REMOVE_DELAY_MS = 420L
private const val SWIPE_RESET_DURATION_MS = 220
private const val SWIPE_ITEM_SPACING_DP = 10
private const val SWIPE_ITEM_PLACEMENT_DURATION_MS = 320
private const val BOTTOM_TAB_TRANSITION_GUARD_MS = 460L
private const val EDIT_CATEGORIES_SHEET_EXIT_DURATION_MS = 250
private const val EDIT_CATEGORIES_SHEET_ENTER_DURATION_MS = 260
private const val AI_EXPIRING_FOOD_WINDOW_DAYS = 2

private val historyAutoDeletePresets = listOf(
    AutoDeletePreset(30L, "1 month"),
    AutoDeletePreset(90L, "3 months")
)

private val expiredFoodAutoRemovePresets = listOf(
    AutoDeletePreset(3L, "3 days"),
    AutoDeletePreset(7L, "1 week"),
    AutoDeletePreset(30L, "1 month")
)

private const val BARCODE_CACHE_KEY = "barcode_cache"
private const val EXTRA_SCANNED_BARCODE = "extra_scanned_barcode"
private const val EXTRA_BARCODE_SCAN_MESSAGE = "extra_barcode_scan_message"

private fun shortListScrollRunway(itemCount: Int): Dp =
    when {
        itemCount >= 18 -> 0.dp
        itemCount >= 12 -> 6.dp
        itemCount >= 8 -> 14.dp
        itemCount >= 5 -> 24.dp
        itemCount >= 3 -> 36.dp
        itemCount > 0 -> 48.dp
        else -> 0.dp
    }

@Composable
private fun listBottomSafePadding(
    itemCount: Int,
    hasFab: Boolean
): Dp {
    val density = LocalDensity.current
    val navigationBottomInset =
        with(density) {
            WindowInsets.navigationBars.getBottom(this).toDp()
        }
    val overlayClearance = if (hasFab) 92.dp else 16.dp

    return navigationBottomInset + overlayClearance + shortListScrollRunway(itemCount)
}

private fun Modifier.blurWhen(radius: Dp): Modifier =
    if (radius > 0.dp) blur(radius) else this

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomTabEnterTransition(): EnterTransition {
    val from = bottomBarRouteIndex(initialState.destination.route)
    val to = bottomBarRouteIndex(targetState.destination.route)
    val movingLeft = to > from
    val slideDirection =
        if (movingLeft) {
            AnimatedContentTransitionScope.SlideDirection.Left
        } else {
            AnimatedContentTransitionScope.SlideDirection.Right
        }

    return fadeIn(
        animationSpec = tween(
            durationMillis = 430,
            easing = LinearOutSlowInEasing
        )
    ) + slideIntoContainer(
        towards = slideDirection,
        animationSpec = tween(
            durationMillis = 430,
            easing = FastOutSlowInEasing
        )
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomTabExitTransition(): ExitTransition {
    val from = bottomBarRouteIndex(initialState.destination.route)
    val to = bottomBarRouteIndex(targetState.destination.route)
    val movingLeft = to > from
    val slideDirection =
        if (movingLeft) {
            AnimatedContentTransitionScope.SlideDirection.Left
        } else {
            AnimatedContentTransitionScope.SlideDirection.Right
        }

    return fadeOut(
        animationSpec = tween(
            durationMillis = 380,
            easing = FastOutSlowInEasing
        )
    ) + slideOutOfContainer(
        towards = slideDirection,
        animationSpec = tween(
            durationMillis = 430,
            easing = FastOutSlowInEasing
        )
    )
}

private data class BarcodeCacheEntry(
    val barcode: String,
    val name: String,
    val source: String,
    val savedAt: Long = System.currentTimeMillis()
)

private data class BarcodeLookupResult(
    val barcode: String,
    val productName: String,
    val source: String,
    val wasCached: Boolean
)

private data class OffLookupResponse(
    val status: Int? = null,
    val product: OffLookupProduct? = null
)

private data class OffLookupProduct(
    val product_name: String? = null,
    val product_name_en: String? = null,
    val product_name_ar: String? = null,
    val brands: String? = null
)

private fun normalizeBarcode(raw: String): String {
    return raw.trim().replace(" ", "")
}

private fun loadBarcodeCache(
    prefs: android.content.SharedPreferences,
    gson: Gson
): MutableMap<String, BarcodeCacheEntry> {
    val json = prefs.getString(BARCODE_CACHE_KEY, null) ?: return mutableMapOf()
    val type = object : TypeToken<MutableMap<String, BarcodeCacheEntry>>() {}.type
    return runCatching {
        gson.fromJson<MutableMap<String, BarcodeCacheEntry>>(json, type)
    }.getOrElse { mutableMapOf() }
}

private fun saveBarcodeCache(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    map: Map<String, BarcodeCacheEntry>
) {
    prefs.edit { putString(BARCODE_CACHE_KEY, gson.toJson(map)) }
}

private fun saveBarcodeNameToCache(
    context: Context,
    barcode: String,
    productName: String,
    source: String
) {
    val cleanedBarcode = normalizeBarcode(barcode)
    val cleanedName = productName.trim().replace(Regex("\\s+"), " ")
    if (cleanedBarcode.isBlank() || cleanedName.isBlank()) return

    val prefs = context.applicationContext.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    val gson = Gson()
    val cache = loadBarcodeCache(prefs, gson)
    cache[cleanedBarcode] = BarcodeCacheEntry(
        barcode = cleanedBarcode,
        name = cleanedName,
        source = source
    )
    saveBarcodeCache(prefs, gson, cache)
}

private fun getBarcodeNameFromCache(
    context: Context,
    barcode: String
): BarcodeLookupResult? {
    val cleanedBarcode = normalizeBarcode(barcode)
    if (cleanedBarcode.isBlank()) return null

    val prefs = context.applicationContext.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    val gson = Gson()
    val cache = loadBarcodeCache(prefs, gson)
    val entry = cache[cleanedBarcode] ?: return null

    return BarcodeLookupResult(
        barcode = entry.barcode,
        productName = entry.name,
        source = entry.source,
        wasCached = true
    )
}

private fun buildBestProductName(
    rawName: String?,
    rawNameEn: String? = null,
    rawNameAr: String? = null,
    brand: String? = null,
    barcode: String? = null
): String? {
    val chosenName = listOf(
        rawNameAr,
        rawNameEn,
        rawName
    ).firstOrNull { !it.isNullOrBlank() }
        ?.replace(Regex("\\s+"), " ")
        ?.trim()
        .orEmpty()

    val firstBrand = brand
        ?.split(",")
        ?.firstOrNull()
        ?.replace(Regex("\\s+"), " ")
        ?.trim()
        .orEmpty()

    val combined = when {
        chosenName.isBlank() -> firstBrand
        firstBrand.isBlank() -> chosenName
        chosenName.startsWith(firstBrand, ignoreCase = true) -> chosenName
        else -> "$firstBrand $chosenName"
    }.replace(Regex("\\s+"), " ").trim()

    if (combined.isBlank()) return null

    val cleanedBarcode = barcode?.trim().orEmpty()

    if (cleanedBarcode.isNotBlank() && combined == cleanedBarcode) return null
    if (firstBrand.isNotBlank() && combined.equals(firstBrand, ignoreCase = true)) return null
    if (combined.length < 4) return null

    return combined
}

private suspend fun lookupFromOpenFoodFacts(barcode: String): BarcodeLookupResult? {
    return withContext(Dispatchers.IO) {
        val url = URL(
            "https://world.openfoodfacts.net/api/v2/product/$barcode" +
                    "?fields=product_name,product_name_en,product_name_ar,brands"
        )

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3500
            readTimeout = 3500
        }

        try {
            if (connection.responseCode !in 200..299) return@withContext null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val response = Gson().fromJson(body, OffLookupResponse::class.java) ?: return@withContext null
            if (response.status != 1) return@withContext null

            val product = response.product ?: return@withContext null

            val bestName = buildBestProductName(
                rawName = product.product_name,
                rawNameEn = product.product_name_en,
                rawNameAr = product.product_name_ar,
                brand = product.brands,
                barcode = barcode
            ) ?: return@withContext null

            BarcodeLookupResult(
                barcode = barcode,
                productName = bestName,
                source = "Open Food Facts",
                wasCached = false
            )
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}

private suspend fun lookupBarcodeName(
    context: Context,
    barcode: String
): BarcodeLookupResult? {
    val cleanedBarcode = normalizeBarcode(barcode)
    if (cleanedBarcode.isBlank()) return null

    getBarcodeNameFromCache(context, cleanedBarcode)?.let { return it }

    val providers = listOf<suspend (String) -> BarcodeLookupResult?> { code ->
        withTimeoutOrNull(
            3500
        ) { lookupFromOpenFoodFacts(code) }
    }
    for (provider in providers) {
        val result = provider(cleanedBarcode)
        if (result != null) {
            saveBarcodeNameToCache(
                context = context,
                barcode = cleanedBarcode,
                productName = result.productName,
                source = result.source
            )
            return result.copy(wasCached = false)
        }
    }

    return null
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                continuation.resume(future.get())
            },
            ContextCompat.getMainExecutor(this)
        )
    }

private fun normalizeFoodName(raw: String): String {
    return raw
        .trim()
        .lowercase(Locale.US)
        .replace(Regex("\\s+"), " ")
        .replace(Regex("[^\\p{L}\\p{N} ]"), "")
}

private fun cleanFoodLabel(raw: String): String {
    return raw.trim().replace(Regex("\\s+"), " ")
}

private fun cleanFoodCategory(raw: String?): String? {
    return raw?.trim()?.replace(Regex("\\s+"), " ")?.takeIf { it.isNotBlank() }
}

private fun cleanFoodExpiry(raw: String): String {
    val trimmed = raw.trim()
    return runCatching {
        formatExpiryDate(LocalDate.parse(trimmed, EXPIRY_FORMATTER))
    }.getOrDefault(trimmed)
}

private fun sanitizeFoodQuantity(quantity: Int): Int {
    return quantity.coerceIn(MIN_FOOD_QUANTITY, MAX_FOOD_QUANTITY)
}

private fun foodBatchKey(name: String, expiry: String): FoodBatchKey {
    return FoodBatchKey(
        nameKey = normalizeFoodName(name),
        expiryKey = cleanFoodExpiry(expiry)
    )
}

private fun FoodItem.batchKey(): FoodBatchKey {
    return foodBatchKey(name, expiry)
}

private fun FoodItem.sanitized(): FoodItem {
    return copy(
        name = cleanFoodLabel(name),
        expiry = cleanFoodExpiry(expiry),
        category = cleanFoodCategory(category),
        quantity = sanitizeFoodQuantity(quantity)
    )
}

private fun mergeFoodBatches(existing: FoodItem, incoming: FoodItem): FoodItem {
    val cleanedExisting = existing.sanitized()
    val cleanedIncoming = incoming.sanitized()
    val mergedCategory = cleanedExisting.category ?: cleanedIncoming.category

    return cleanedExisting.copy(
        category = mergedCategory,
        quantity = sanitizeFoodQuantity(cleanedExisting.quantity + cleanedIncoming.quantity)
    )
}

private fun sanitizeAndMergeFoodList(list: List<FoodItem>): MutableList<FoodItem> {
    val batches = LinkedHashMap<FoodBatchKey, FoodItem>()

    list.forEach { food ->
        val sanitized = food.sanitized()
        if (sanitized.name.isBlank() || sanitized.expiry.isBlank()) return@forEach

        val key = sanitized.batchKey()
        val existing = batches[key]
        batches[key] = if (existing == null) {
            sanitized
        } else {
            mergeFoodBatches(existing, sanitized)
        }
    }

    return batches.values.toMutableList()
}

private fun findMatchingFoodBatchIndex(
    list: List<FoodItem>,
    candidate: FoodItem,
    ignoredItem: FoodItem? = null
): Int {
    val key = candidate.batchKey()
    return list.indexOfFirst { item ->
        item != ignoredItem && item.batchKey() == key
    }
}

private fun upsertFoodBatch(
    list: MutableList<FoodItem>,
    food: FoodItem,
    oldFood: FoodItem? = null
): FoodItem {
    val sanitized = food.sanitized()

    if (oldFood != null) {
        val oldIndex = list.indexOf(oldFood)
        if (oldIndex != -1 && oldFood.batchKey() == sanitized.batchKey()) {
            list[oldIndex] = sanitized
            return sanitized
        }
        if (oldIndex != -1) {
            list.removeAt(oldIndex)
        }
    }

    val existingIndex = findMatchingFoodBatchIndex(list, sanitized)
    if (existingIndex != -1) {
        val merged = mergeFoodBatches(list[existingIndex], sanitized)
        list[existingIndex] = merged
        return merged
    }

    list.add(sanitized)
    return sanitized
}

private fun isOnboardingDemoFood(food: FoodItem): Boolean {
    return normalizeFoodName(food.name) == normalizeFoodName(ONBOARDING_DEMO_FOOD_NAME) &&
            normalizeFoodName(food.category.orEmpty()) == normalizeFoodName(ONBOARDING_DEMO_CATEGORY_NAME)
}

private fun cleanHistoryName(raw: String): String {
    return raw.trim().replace(Regex("\\s+"), " ")
}

private fun loadFoodListRaw(prefs: android.content.SharedPreferences, gson: Gson): MutableList<FoodItem> {
    val json = prefs.getString(FOOD_LIST_KEY, null) ?: return mutableListOf()
    val type = object : TypeToken<MutableList<FoodItem>>() {}.type
    return runCatching { gson.fromJson<MutableList<FoodItem>>(json, type) }.getOrElse { mutableListOf() }
}

private fun loadExpiredFoodAutoRemoveEnabled(prefs: android.content.SharedPreferences): Boolean {
    return prefs.getBoolean(EXPIRED_FOOD_AUTO_REMOVE_ENABLED_KEY, false)
}

private fun clampHistoryRetentionDays(days: Long): Long {
    return days.coerceIn(MIN_HISTORY_RETENTION_DAYS, MAX_HISTORY_RETENTION_DAYS)
}

private fun clampExpiredFoodAutoRemoveDays(days: Long): Long {
    return days.coerceIn(MIN_EXPIRED_FOOD_AUTO_REMOVE_DAYS, MAX_EXPIRED_FOOD_AUTO_REMOVE_DAYS)
}

private fun readLongPreference(
    prefs: android.content.SharedPreferences,
    key: String,
    defaultValue: Long
): Long {
    return runCatching {
        prefs.getLong(key, defaultValue)
    }.getOrElse {
        prefs.getInt(key, defaultValue.toInt()).toLong()
    }
}

private fun loadExpiredFoodAutoRemoveDays(prefs: android.content.SharedPreferences): Long {
    return clampExpiredFoodAutoRemoveDays(
        readLongPreference(
            prefs = prefs,
            key = EXPIRED_FOOD_AUTO_REMOVE_DAYS_KEY,
            defaultValue = DEFAULT_EXPIRED_FOOD_AUTO_REMOVE_DAYS
        )
    )
}

private fun saveExpiredFoodAutoRemoveEnabled(
    prefs: android.content.SharedPreferences,
    enabled: Boolean
) {
    prefs.edit { putBoolean(EXPIRED_FOOD_AUTO_REMOVE_ENABLED_KEY, enabled) }
}

private fun saveExpiredFoodAutoRemoveDays(
    prefs: android.content.SharedPreferences,
    days: Long
) {
    prefs.edit { putLong(EXPIRED_FOOD_AUTO_REMOVE_DAYS_KEY, clampExpiredFoodAutoRemoveDays(days)) }
}

private fun foodExpiryDate(food: FoodItem): LocalDate? {
    return runCatching {
        LocalDate.parse(food.expiry, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrNull()
}

private fun isExpiredLongerThan(
    food: FoodItem,
    now: LocalDate,
    thresholdDays: Long
): Boolean {
    val expiryDate = foodExpiryDate(food) ?: return false
    return ChronoUnit.DAYS.between(expiryDate, now) > thresholdDays
}

private fun filterExpiredFoodsIfNeeded(
    prefs: android.content.SharedPreferences,
    list: List<FoodItem>,
    now: LocalDate = LocalDate.now()
): List<FoodItem> {
    if (!loadExpiredFoodAutoRemoveEnabled(prefs)) return list

    val thresholdDays = loadExpiredFoodAutoRemoveDays(prefs)
    return list.filterNot { food ->
        isExpiredLongerThan(food, now, thresholdDays)
    }
}

private fun saveFoodListRaw(prefs: android.content.SharedPreferences, gson: Gson, list: List<FoodItem>) {
    val json = gson.toJson(list)
    if (prefs.getString(FOOD_LIST_KEY, null) == json) return
    prefs.edit { putString(FOOD_LIST_KEY, json) }
}

private fun loadFoodList(prefs: android.content.SharedPreferences, gson: Gson): MutableList<FoodItem> {
    val rawFoods = loadFoodListRaw(prefs, gson)
    val foods = sanitizeAndMergeFoodList(rawFoods)
    val filteredFoods = filterExpiredFoodsIfNeeded(prefs, foods)
    if (filteredFoods != rawFoods) {
        saveFoodListRaw(prefs, gson, filteredFoods)
    }
    return filteredFoods.toMutableList()
}

private fun saveFoodList(prefs: android.content.SharedPreferences, gson: Gson, list: List<FoodItem>) {
    saveFoodListRaw(
        prefs,
        gson,
        filterExpiredFoodsIfNeeded(prefs, sanitizeAndMergeFoodList(list))
    )
}

private fun loadStringList(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    key: String
): MutableList<String> {
    val json = prefs.getString(key, null) ?: return mutableListOf()
    val type = object : TypeToken<MutableList<String>>() {}.type
    return runCatching { gson.fromJson<MutableList<String>>(json, type) }.getOrElse { mutableListOf() }
}

private fun saveStringList(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    key: String,
    list: List<String>
) {
    val json = gson.toJson(list)
    if (prefs.getString(key, null) == json) return
    prefs.edit { putString(key, json) }
}

private suspend fun saveFoodListAsync(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    list: List<FoodItem>
) {
    val json = withContext(Dispatchers.Default) {
        gson.toJson(filterExpiredFoodsIfNeeded(prefs, sanitizeAndMergeFoodList(list)))
    }
    withContext(Dispatchers.IO) {
        if (prefs.getString(FOOD_LIST_KEY, null) != json) {
            prefs.edit { putString(FOOD_LIST_KEY, json) }
        }
    }
}

private fun reminderDaysLabel(days: Int): String {
    return if (days == 1) "1 day" else "$days days"
}

private suspend fun saveStringListAsync(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    key: String,
    list: List<String>
) {
    val json = withContext(Dispatchers.Default) { gson.toJson(list) }
    withContext(Dispatchers.IO) {
        if (prefs.getString(key, null) != json) {
            prefs.edit { putString(key, json) }
        }
    }
}

private fun <T> replaceListContentsIfChanged(
    target: MutableList<T>,
    updated: List<T>
) {
    val isSame =
        target.size == updated.size &&
                target.indices.all { index -> target[index] == updated[index] }

    if (isSame) return

    target.clear()
    target.addAll(updated)
}

private fun loadHistoryAutoDeleteEnabled(prefs: android.content.SharedPreferences): Boolean {
    return prefs.getBoolean(HISTORY_AUTO_DELETE_ENABLED_KEY, false)
}

private fun loadHistoryRetentionDays(prefs: android.content.SharedPreferences): Long {
    return clampHistoryRetentionDays(
        readLongPreference(
            prefs = prefs,
            key = HISTORY_RETENTION_DAYS_KEY,
            defaultValue = DEFAULT_HISTORY_RETENTION_DAYS
        )
    )
}

private fun saveHistoryAutoDeleteEnabled(
    prefs: android.content.SharedPreferences,
    enabled: Boolean
) {
    prefs.edit { putBoolean(HISTORY_AUTO_DELETE_ENABLED_KEY, enabled) }
}

private fun saveHistoryRetentionDays(
    prefs: android.content.SharedPreferences,
    days: Long
) {
    prefs.edit { putLong(HISTORY_RETENTION_DAYS_KEY, clampHistoryRetentionDays(days)) }
}

private fun activeHistoryRetentionDays(prefs: android.content.SharedPreferences): Long? {
    return if (loadHistoryAutoDeleteEnabled(prefs)) {
        loadHistoryRetentionDays(prefs)
    } else {
        null
    }
}

private fun historyCutoffMillis(
    now: Long = System.currentTimeMillis(),
    retentionDays: Long
): Long {
    return now - (retentionDays * DAY_IN_MILLIS)
}

private fun normalizeHistoryEntries(
    entries: List<HistoryEntry>,
    now: Long = System.currentTimeMillis(),
    retentionDays: Long? = null
): MutableList<HistoryEntry> {
    val cutoff = retentionDays?.let { historyCutoffMillis(now, it) }
    val deduped = LinkedHashMap<String, HistoryEntry>()

    entries.forEach { entry ->
        val cleanedName = cleanHistoryName(entry.name)
        if (cleanedName.isBlank()) return@forEach

        val normalizedKey = normalizeFoodName(cleanedName)
        if (normalizedKey.isBlank()) return@forEach

        val sanitized = HistoryEntry(
            name = cleanedName,
            lastUsedAt = entry.lastUsedAt.takeIf { it > 0L } ?: now
        )

        if (cutoff != null && sanitized.lastUsedAt < cutoff) return@forEach

        val existing = deduped[normalizedKey]
        if (existing == null || sanitized.lastUsedAt > existing.lastUsedAt) {
            deduped[normalizedKey] = sanitized
        }
    }

    return deduped.values
        .sortedByDescending { it.lastUsedAt }
        .toMutableList()
}

private fun loadHistoryEntries(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    now: Long = System.currentTimeMillis()
): MutableList<HistoryEntry> {
    val json = prefs.getString(HISTORY_LIST_KEY, null) ?: return mutableListOf()

    val historyType = object : TypeToken<MutableList<HistoryEntry>>() {}.type
    val savedEntries = runCatching {
        gson.fromJson<MutableList<HistoryEntry>>(json, historyType)
    }.getOrNull()

    if (savedEntries != null) {
        return normalizeHistoryEntries(savedEntries, now, activeHistoryRetentionDays(prefs))
    }

    val legacyType = object : TypeToken<MutableList<String>>() {}.type
    val legacyEntries = runCatching {
        gson.fromJson<MutableList<String>>(json, legacyType)
    }.getOrElse { mutableListOf() } ?: mutableListOf()

    val migratedEntries = legacyEntries.mapIndexed { index, name ->
        HistoryEntry(
            name = name,
            lastUsedAt = now - index.toLong()
        )
    }

    return normalizeHistoryEntries(migratedEntries, now, activeHistoryRetentionDays(prefs))
}

private fun saveHistoryEntries(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    entries: List<HistoryEntry>
) {
    prefs.edit { putString(HISTORY_LIST_KEY, gson.toJson(entries)) }
}

private fun loadAndSyncHistoryEntries(
    prefs: android.content.SharedPreferences,
    gson: Gson
): MutableList<HistoryEntry> {
    val entries = loadHistoryEntries(prefs, gson)
    if (prefs.contains(HISTORY_LIST_KEY)) {
        saveHistoryEntries(prefs, gson, entries)
    }
    return entries
}

private fun historyEntryLocalDate(
    lastUsedAt: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDate {
    return Instant.ofEpochMilli(lastUsedAt)
        .atZone(zoneId)
        .toLocalDate()
}

private fun historySectionTitle(
    entryDate: LocalDate,
    today: LocalDate,
    weekFields: WeekFields
): String {
    return when {
        entryDate == today -> "Today"
        entryDate == today.minusDays(1) -> "Yesterday"
        entryDate.get(weekFields.weekBasedYear()) == today.get(weekFields.weekBasedYear()) &&
                entryDate.get(weekFields.weekOfWeekBasedYear()) == today.get(weekFields.weekOfWeekBasedYear()) ->
            "This week"
        entryDate.month == today.month && entryDate.year == today.year -> "This month"
        else -> "Earlier"
    }
}

private fun buildHistoryDateSections(
    entries: List<HistoryEntry>,
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): List<HistoryDateSection> {
    if (entries.isEmpty()) return emptyList()

    val weekFields = WeekFields.of(locale)
    val groupedEntries = linkedMapOf<String, MutableList<HistoryEntry>>()

    entries.forEach { entry ->
        val title = historySectionTitle(
            entryDate = historyEntryLocalDate(entry.lastUsedAt, zoneId),
            today = today,
            weekFields = weekFields
        )
        groupedEntries.getOrPut(title) { mutableListOf() }.add(entry)
    }

    return groupedEntries.map { (title, sectionEntries) ->
        HistoryDateSection(
            title = title,
            entries = sectionEntries
        )
    }
}

private fun buildProfileStats(
    foods: List<FoodItem>,
    today: LocalDate = LocalDate.now()
): ProfileStats {
    val pantryFoods = foods.filterNot(::isOnboardingDemoFood)
    val foodsSaved = pantryFoods.sumOf { sanitizeFoodQuantity(it.quantity) }
    val foodsExpired = pantryFoods.sumOf { food ->
        val expiryDate = foodExpiryDate(food)
        if (expiryDate != null && expiryDate.isBefore(today)) {
            sanitizeFoodQuantity(food.quantity)
        } else {
            0
        }
    }
    val categoryTotals = linkedMapOf<String, Int>()

    pantryFoods.forEach { food ->
        val category = cleanFoodCategory(food.category) ?: return@forEach
        categoryTotals[category] = (categoryTotals[category] ?: 0) + sanitizeFoodQuantity(food.quantity)
    }

    val mostUsedCategory =
        categoryTotals.entries
            .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            ?.key
            ?: "None"

    return ProfileStats(
        foodsSaved = foodsSaved,
        foodsExpired = foodsExpired,
        mostUsedCategory = mostUsedCategory,
        wasteAvoided = (foodsSaved - foodsExpired).coerceAtLeast(0)
    )
}

private fun recordFoodNameInHistory(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    history: MutableList<HistoryEntry>,
    name: String,
    now: Long = System.currentTimeMillis()
) {
    val cleaned = cleanHistoryName(name)
    val key = normalizeFoodName(cleaned)
    if (key.isBlank()) return

    val updatedEntries = history
        .filterNot { normalizeFoodName(it.name) == key }
        .toMutableList()
        .apply {
            add(
                0,
                HistoryEntry(
                    name = cleaned,
                    lastUsedAt = now
                )
            )
        }

    val normalizedEntries = normalizeHistoryEntries(
        entries = updatedEntries,
        now = now,
        retentionDays = activeHistoryRetentionDays(prefs)
    )
    history.clear()
    history.addAll(normalizedEntries)
    saveHistoryEntries(prefs, gson, normalizedEntries)
}

private fun addFoodNameToHistory(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    name: String
) {
    val history = loadHistoryEntries(prefs, gson)
    recordFoodNameInHistory(prefs, gson, history, name)
}

private suspend fun applyAutoDeleteRulesOnAppLoad(context: Context) {
    withContext(Dispatchers.IO) {
        val prefs = context.applicationContext.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
        val gson = Gson()

        if (loadHistoryAutoDeleteEnabled(prefs) && prefs.contains(HISTORY_LIST_KEY)) {
            loadAndSyncHistoryEntries(prefs, gson)
        }

        if (loadExpiredFoodAutoRemoveEnabled(prefs)) {
            loadFoodList(prefs, gson)
        }
    }
}

private fun shouldShowFirstLaunchOnboarding(context: Context): Boolean {
    val prefs = context.applicationContext.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    return !prefs.getBoolean(ONBOARDING_COMPLETED_KEY, false)
}

private fun markFirstLaunchOnboardingComplete(context: Context) {
    val prefs = context.applicationContext.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    prefs.edit { putBoolean(ONBOARDING_COMPLETED_KEY, true) }
}

private fun formatExpiryDate(date: LocalDate): String {
    return String.format(
        Locale.US,
        "%02d/%02d/%04d",
        date.dayOfMonth,
        date.monthValue,
        date.year
    )
}

private fun seedFirstLaunchDemoFoodIfNeeded(context: Context) {
    val appContext = context.applicationContext
    val prefs = appContext.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    if (prefs.getBoolean(ONBOARDING_DEMO_SEEDED_KEY, false)) return

    val gson = Gson()
    val demoKey = normalizeFoodName(ONBOARDING_DEMO_FOOD_NAME)
    val demoCategoryKey = normalizeFoodName(ONBOARDING_DEMO_CATEGORY_NAME)

    val categories = loadStringList(prefs, gson, CATEGORIES_LIST_KEY)
    if (categories.none { normalizeFoodName(it) == demoCategoryKey }) {
        categories.add(ONBOARDING_DEMO_CATEGORY_NAME)
        saveStringList(prefs, gson, CATEGORIES_LIST_KEY, categories)
    }

    val foods = loadFoodList(prefs, gson)
    val demoIndex = foods.indexOfFirst { normalizeFoodName(it.name) == demoKey }

    if (demoIndex >= 0) {
        val existingDemo = foods[demoIndex]
        if (existingDemo.category != ONBOARDING_DEMO_CATEGORY_NAME) {
            foods[demoIndex] = existingDemo.copy(category = ONBOARDING_DEMO_CATEGORY_NAME)
            saveFoodList(prefs, gson, foods)
        }
    } else {
        foods.add(
            FoodItem(
                name = ONBOARDING_DEMO_FOOD_NAME,
                expiry = formatExpiryDate(LocalDate.now().plusDays(3)),
                category = ONBOARDING_DEMO_CATEGORY_NAME
            )
        )
        saveFoodList(prefs, gson, foods)
    }

    val history = loadHistoryEntries(prefs, gson)
    recordFoodNameInHistory(prefs, gson, history, ONBOARDING_DEMO_FOOD_NAME)

    prefs.edit { putBoolean(ONBOARDING_DEMO_SEEDED_KEY, true) }
}

private fun removeFoodNameFromHistory(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    history: MutableList<HistoryEntry>,
    name: String
) {
    val key = normalizeFoodName(name)
    if (key.isBlank()) return

    val removed = history.removeAll { normalizeFoodName(it.name) == key }
    if (removed) {
        saveHistoryEntries(prefs, gson, history)
    }
}

private val EXPIRY_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d/M/yyyy", Locale.US)

private fun isValidFutureExpiryDate(input: String): Boolean {
    return try {
        val parsedDate = LocalDate.parse(input, EXPIRY_FORMATTER)
        parsedDate.isAfter(LocalDate.now())
    } catch (_: Exception) {
        false
    }
}

private fun openExpiryDatePicker(
    context: Context,
    onDatePicked: (String) -> Unit
) {
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }

    val year = tomorrow.get(Calendar.YEAR)
    val month = tomorrow.get(Calendar.MONTH)
    val day = tomorrow.get(Calendar.DAY_OF_MONTH)

    val minDateMillis = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    val dialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            onDatePicked(
                String.format(
                    Locale.US,
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )
            )
        },
        year,
        month,
        day
    )

    dialog.datePicker.minDate = minDateMillis
    dialog.show()
}

private fun parseExpiryOrDefault(value: String): Triple<Int, Int, Int> {
    return try {
        val parsed = LocalDate.parse(value, EXPIRY_FORMATTER)
        Triple(parsed.dayOfMonth, parsed.monthValue, parsed.year)
    } catch (_: Exception) {
        val tomorrow = LocalDate.now().plusDays(1)
        Triple(tomorrow.dayOfMonth, tomorrow.monthValue, tomorrow.year)
    }
}

private fun daysInMonth(month: Int, year: Int): Int {
    return try {
        java.time.YearMonth.of(year, month).lengthOfMonth()
    } catch (_: Exception) {
        31
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPickerColumn(
    values: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val pickerTextColor = if (isDarkTheme) Color.White else Color.Black
    val pickerDividerColor =
        if (isDarkTheme) Color.White.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.42f)

    val itemHeight = 48.dp
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(values.indices)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val density = LocalDensity.current
    val itemHeightPx = remember(density) { with(density) { itemHeight.roundToPx() } }
    var suppressSelectionCallback by remember { mutableStateOf(false) }

    val centeredIndex by remember {
        derivedStateOf {
            val extra =
                if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0

            (listState.firstVisibleItemIndex + extra).coerceIn(values.indices)
        }
    }

    LaunchedEffect(centeredIndex) {
        if (!suppressSelectionCallback && centeredIndex != selectedIndex) {
            onSelectedIndexChange(centeredIndex)
        }
    }

    LaunchedEffect(selectedIndex, values.size) {
        val safeIndex = selectedIndex.coerceIn(values.indices)
        if (!listState.isScrollInProgress && centeredIndex != safeIndex) {
            suppressSelectionCallback = true
            try {
                listState.animateScrollToItem(safeIndex)
            } finally {
                suppressSelectionCallback = false
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3)
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(values.size) { index ->
                val isSelected = index == centeredIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = values[index],
                        color = pickerTextColor.copy(alpha = if (isSelected) 1f else 0.78f),
                        fontSize = if (isSelected) 20.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(pickerDividerColor)
            )

            Spacer(modifier = Modifier.height(46.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(pickerDividerColor)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpiryWheelPickerDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parsed = remember(initialValue) { parseExpiryOrDefault(initialValue) }

    var selectedDay by remember(parsed) { mutableIntStateOf(parsed.first) }
    var selectedMonth by remember(parsed) { mutableIntStateOf(parsed.second) }
    var selectedYear by remember(parsed) { mutableIntStateOf(parsed.third) }

    val currentYear = remember { LocalDate.now().year }
    val yearValues = remember(currentYear) {
        (currentYear..(currentYear + 73)).map { it.toString() }
    }

    val maxDay = remember(selectedMonth, selectedYear) {
        daysInMonth(selectedMonth, selectedYear)
    }

    LaunchedEffect(selectedMonth, selectedYear) {
        if (selectedDay > maxDay) {
            selectedDay = maxDay
        }
    }

    val dayValues = remember(maxDay) {
        (1..maxDay).map { String.format(Locale.US, "%02d", it) }
    }

    val monthValues = remember {
        (1..12).map { String.format(Locale.US, "%02d", it) }
    }
    val today = remember { LocalDate.now() }
    val selectedDate = remember(selectedDay, selectedMonth, selectedYear) {
        LocalDate.of(selectedYear, selectedMonth, selectedDay)
    }
    val isInvalidSelectedDate = !selectedDate.isAfter(today)

    var animateIn by remember { mutableStateOf(false) }

    val dialogScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.92f,
        animationSpec = tween(220),
        label = "dialogScale"
    )

    val dialogAlpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(220),
        label = "dialogAlpha"
    )

    LaunchedEffect(Unit) {
        animateIn = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ExactFrostedPillCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        scaleX = dialogScale
                        scaleY = dialogScale
                        alpha = dialogAlpha
                    },
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Select Expiry Date",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WheelPickerColumn(
                            values = dayValues,
                            selectedIndex = (selectedDay - 1).coerceIn(dayValues.indices),
                            onSelectedIndexChange = { selectedDay = it + 1 },
                            modifier = Modifier.width(82.dp)
                        )

                        WheelPickerColumn(
                            values = monthValues,
                            selectedIndex = (selectedMonth - 1).coerceIn(monthValues.indices),
                            onSelectedIndexChange = { selectedMonth = it + 1 },
                            modifier = Modifier.width(82.dp)
                        )

                        WheelPickerColumn(
                            values = yearValues,
                            selectedIndex = (selectedYear - currentYear).coerceIn(yearValues.indices),
                            onSelectedIndexChange = { selectedYear = currentYear + it },
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isInvalidSelectedDate,
                        enter = smoothVerticalRevealEnter(),
                        exit = smoothVerticalRevealExit()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Please choose a future date.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        TextButton(
                            enabled = !isInvalidSelectedDate,
                            onClick = {
                                val pickedDate = String.format(
                                    Locale.US,
                                    "%02d/%02d/%04d",
                                    selectedDay,
                                    selectedMonth,
                                    selectedYear
                                )

                                onConfirm(pickedDate)
                                onDismiss()
                            }
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpiryDateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onCalendarClick: () -> Unit
) {
    var showWheelPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expiry Date") },
                placeholder = { Text("DD/MM/YYYY") },
                singleLine = true
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showWheelPicker = true }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Open calendar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showWheelPicker) {
        ExpiryWheelPickerDialog(
            initialValue = value,
            onDismiss = { showWheelPicker = false },
            onConfirm = { pickedDate ->
                onValueChange(pickedDate)
                showWheelPicker = false
            }
        )
    }
}

private fun daysUntil(expiry: String): Int? = try {
    val target = LocalDate.parse(expiry, EXPIRY_FORMATTER)
    ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
} catch (_: Exception) {
    null
}

private fun pantrySortSnapshot(item: FoodItem): SortedFoodSnapshot {
    val daysLeft = daysUntil(item.expiry)
    val cleanedCategory = item.category.orEmpty().trim()

    return SortedFoodSnapshot(
        item = item,
        expiryBucket = when {
            daysLeft == null -> 2
            daysLeft < 0 -> 0
            else -> 1
        },
        expiryDistance = when {
            daysLeft == null -> Int.MAX_VALUE
            daysLeft < 0 -> abs(daysLeft)
            else -> daysLeft
        },
        nameKey = item.name.trim().lowercase(Locale.US),
        categoryBucket = if (cleanedCategory.isBlank()) 1 else 0,
        categoryKey = cleanedCategory.lowercase(Locale.US)
    )
}

private fun sortPantryFoods(
    foods: List<FoodItem>,
    sortMode: PantrySortMode
): List<FoodItem> {
    val snapshots = foods.map(::pantrySortSnapshot)

    val comparator =
        when (sortMode) {
            PantrySortMode.NEAREST_EXPIRY -> compareBy<SortedFoodSnapshot>(
                SortedFoodSnapshot::expiryBucket,
                SortedFoodSnapshot::expiryDistance,
                SortedFoodSnapshot::nameKey,
                SortedFoodSnapshot::categoryKey
            )

            PantrySortMode.NAME_A_TO_Z -> compareBy<SortedFoodSnapshot>(
                SortedFoodSnapshot::nameKey,
                SortedFoodSnapshot::expiryBucket,
                SortedFoodSnapshot::expiryDistance,
                SortedFoodSnapshot::categoryKey
            )

            PantrySortMode.CATEGORY -> compareBy<SortedFoodSnapshot>(
                SortedFoodSnapshot::categoryBucket,
                SortedFoodSnapshot::categoryKey,
                SortedFoodSnapshot::expiryBucket,
                SortedFoodSnapshot::expiryDistance,
                SortedFoodSnapshot::nameKey
            )
        }

    return snapshots
        .sortedWith(comparator)
        .map(SortedFoodSnapshot::item)
}

private data class CountdownStyle(val bg: Color, val fg: Color)

private fun countdownTextColor(background: Color): Color {
    return if (background.luminance() > 0.58f) Color.Black else Color.White
}

private fun compactCountdownText(
    daysLeft: Int,
    format: CountdownFormat
): String {
    if (daysLeft <= 0) return "0d"

    fun compactParts(years: Int = 0, months: Int = 0, days: Int = 0): String {
        val parts = buildList {
            if (years > 0) add("${years}y")
            if (months > 0) add("${months}m")
            if (days > 0) add("${days}d")
        }

        return if (parts.isNotEmpty()) parts.joinToString(" ") else "0d"
    }

    return when (format) {
        CountdownFormat.DAYS_ONLY ->
            if (daysLeft == 1) "1 day left" else "$daysLeft days left"

        CountdownFormat.MONTHS_AND_DAYS -> {
            val months = daysLeft / 30
            val days = daysLeft % 30
            compactParts(months = months, days = days)
        }

        CountdownFormat.YEARS_MONTHS_DAYS -> {
            val years = daysLeft / 365
            val afterYears = daysLeft % 365
            val months = afterYears / 30
            val days = afterYears % 30
            compactParts(years = years, months = months, days = days)
        }
    }
}

private fun countdownText(
    daysLeft: Int?,
    format: CountdownFormat
): String {
    return when {
        daysLeft == null -> "--"
        daysLeft < 0 -> "Expired"
        daysLeft == 0 -> "Expires Today"
        else -> compactCountdownText(daysLeft, format)
    }
}

private fun countdownStyle(daysLeft: Int?): CountdownStyle {
    val bg = when {
        daysLeft == null -> Color(0xFF9CA8B7)
        daysLeft < 0 -> Color(0xFFA9505A)
        daysLeft == 0 -> Color(0xFFFF3D2E)
        daysLeft == 1 -> Color(0xFFFF5B45)
        daysLeft == 2 -> Color(0xFFFF7A33)
        daysLeft == 3 -> Color(0xFFFFA025)
        daysLeft == 4 -> Color(0xFFFFDB25)
        daysLeft == 5 -> Color(0xFF8DE83D)
        daysLeft == 6 -> Color(0xFF78DA45)
        daysLeft == 7 -> Color(0xFF3DCC5C)
        else -> Color(0xFF9CA8B7)
    }

    return CountdownStyle(bg = bg, fg = countdownTextColor(bg))
}

@Composable
private fun ExpiryCountdownBadge(
    expiry: String,
    countdownFormat: CountdownFormat
) {
    val daysLeft = remember(expiry) { daysUntil(expiry) }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val text = remember(daysLeft, countdownFormat) {
        countdownText(daysLeft, countdownFormat)
    }

    val style = remember(daysLeft) { countdownStyle(daysLeft) }
    val shape = RoundedCornerShape(50.dp)
    val isNeutralBadge = daysLeft == null || daysLeft > 7
    val topAlpha = remember(daysLeft, isDarkTheme) {
        when {
            daysLeft == null || daysLeft > 7 -> if (isDarkTheme) 0.62f else 0.60f
            daysLeft < 0 -> if (isDarkTheme) 0.86f else 0.76f
            daysLeft <= 1 -> if (isDarkTheme) 0.90f else 0.95f
            daysLeft <= 4 -> if (isDarkTheme) 0.84f else 0.90f
            else -> if (isDarkTheme) 0.80f else 0.86f
        }
    }
    val bottomAlpha = remember(topAlpha, isDarkTheme) {
        (topAlpha - if (isDarkTheme) 0.10f else 0.12f).coerceAtLeast(0.52f)
    }
    val accentTop = remember(style.bg, topAlpha) { style.bg.copy(alpha = topAlpha) }
    val accentBottom = remember(style.bg, bottomAlpha) { style.bg.copy(alpha = bottomAlpha) }
    val textColor = if (isDarkTheme) Color.White else Color(0xFF414141)
    val borderTint = remember(style.bg, daysLeft, isDarkTheme) {
        style.bg.copy(
            alpha = when {
                daysLeft == null || daysLeft > 7 -> if (isDarkTheme) 0.30f else 0.26f
                daysLeft <= 1 -> if (isDarkTheme) 0.66f else 0.60f
                else -> if (isDarkTheme) 0.54f else 0.50f
            }
        )
    }

    Box(
        modifier = Modifier
            .clip(shape)
            .drawWithCache {
                val baseBrush =
                    Brush.verticalGradient(
                        colors = listOf(
                            accentTop.copy(alpha = if (isNeutralBadge) accentTop.alpha * 0.72f else accentTop.alpha),
                            accentBottom
                        )
                    )
                val highlightBrush =
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDarkTheme) 0.10f else 0.16f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.95f, size.height * 0.55f)
                    )

                onDrawBehind {
                    drawRect(baseBrush)
                    drawRect(highlightBrush)
                }
            }
            .border(
                width = 1.dp,
                color = borderTint,
                shape = shape
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FoodQuantityBadge(quantity: Int) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val shape = RoundedCornerShape(50.dp)
    val tintAlpha = if (isDarkTheme) 0.22f else 0.13f

    Box(
        modifier = Modifier
            .clip(shape)
            .background(scheme.primary.copy(alpha = tintAlpha))
            .border(
                width = 1.dp,
                color = scheme.primary.copy(alpha = if (isDarkTheme) 0.34f else 0.22f),
                shape = shape
            )
    ) {
        Text(
            text = "Qty x${sanitizeFoodQuantity(quantity)}",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CompactSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    showClearButton: Boolean = true,
    placeholderText: String = "Search foods...",
    onTap: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    var imeWasVisibleWhileFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused, isImeVisible) {
        if (!isFocused) {
            imeWasVisibleWhileFocused = false
            return@LaunchedEffect
        }

        if (isImeVisible) {
            imeWasVisibleWhileFocused = true
            return@LaunchedEffect
        }

        if (imeWasVisibleWhileFocused) {
            focusManager.clearFocus(force = true)
            imeWasVisibleWhileFocused = false
        }
    }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (onTap != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onTap
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(50.dp),
        tone = GlassTone.SEARCH,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (value.isBlank()) {
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(textFieldModifier)
                        .onFocusChanged { isFocused = it.isFocused }
                )
            }

            if (showClearButton && value.isNotBlank()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPillsLabel() {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = null,
            tint = labelColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Categories",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = labelColor
        )
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = rememberGlassPalette()
    val bg = if (selected) palette.accent else palette.card.copy(alpha = 0.52f)

    val fg = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val outline = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    else palette.border

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .border(1.dp, outline, RoundedCornerShape(50.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun EditCategoriesChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = rememberGlassPalette()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .border(1.dp, palette.border, RoundedCornerShape(50.dp))
            .background(palette.card.copy(alpha = 0.52f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit categories",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EditCategoriesBackgroundFill(
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isDarkTheme) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF141A20),
                                Color(0xFF0D1115),
                                Color(0xFF080A0D)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(140f, 80f),
                            radius = 520f
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.035f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.14f)
                            )
                        )
                    )
            )
        } else {
            AppBackdrop(Modifier.matchParentSize())
        }
    }
}

@Composable
private fun EditCategoriesBackgroundSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    shadowElevation: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        shadowElevation = shadowElevation
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
        ) {
            EditCategoriesBackgroundFill(Modifier.matchParentSize())
            content()
        }
    }
}

@Composable
private fun SelectionActionChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val palette = rememberGlassPalette()
    val background = if (enabled) {
        palette.card.copy(alpha = 0.52f)
    } else {
        palette.card.copy(alpha = 0.35f)
    }
    val borderColor = if (enabled) palette.border else palette.border.copy(alpha = 0.55f)
    val foreground = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = foreground,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                color = foreground,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HistoryTopBar(
    showSearchBar: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onShowSearchBar: () -> Unit,
    @SuppressLint("ModifierParameter") searchTextFieldModifier: Modifier,
    onSearchBarTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 3.dp)
    ) {
        EditCategoriesBackgroundSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onShowSearchBar) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Show search"
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = smoothVerticalRevealEnter(),
                    exit = smoothVerticalRevealExit()
                ) {
                    Column {
                        Spacer(Modifier.height(10.dp))

                        CompactSearchBar(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            textFieldModifier = searchTextFieldModifier,
                            placeholderText = "Search history...",
                            onTap = onSearchBarTap
                        )

                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCategoriesBottomSheet(
    show: Boolean,
    categories: List<String>,
    initialCategories: List<String>,
    onBeginDismiss: () -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    if (!show) return

    val scope = rememberCoroutineScope()
    var isVisible by remember(show) { mutableStateOf(false) }
    var isDismissing by remember(show) { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.94f,
        animationSpec = tween(
            durationMillis = if (isVisible) {
                EDIT_CATEGORIES_SHEET_ENTER_DURATION_MS
            } else {
                EDIT_CATEGORIES_SHEET_EXIT_DURATION_MS
            },
            easing = if (isVisible) {
                LinearOutSlowInEasing
            } else {
                FastOutLinearInEasing
            }
        ),
        label = "editCategoriesCardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isVisible) {
                EDIT_CATEGORIES_SHEET_ENTER_DURATION_MS
            } else {
                180
            },
            easing = if (isVisible) {
                LinearOutSlowInEasing
            } else {
                FastOutLinearInEasing
            }
        ),
        label = "editCategoriesCardAlpha"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isVisible) {
            tween(
                durationMillis = EDIT_CATEGORIES_SHEET_ENTER_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "editCategoriesScrimAlpha"
    )

    LaunchedEffect(show) {
        if (show) {
            isVisible = true
        }
    }

    fun dismissSheet() {
        if (isDismissing) return

        isDismissing = true
        onBeginDismiss()
        isVisible = false

        scope.launch {
            delay(EDIT_CATEGORIES_SHEET_EXIT_DURATION_MS.toLong())
            onDismiss()
        }
    }

    BackHandler(onBack = ::dismissSheet)

    Dialog(
        onDismissRequest = ::dismissSheet,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.28f * scrimAlpha))
            )

            val cardShape = RoundedCornerShape(30.dp)

            EditCategoriesBackgroundSurface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 28.dp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = cardAlpha
                    },
                shape = cardShape,
                shadowElevation = 14.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Edit Categories", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = ::dismissSheet) {
                                Text("Cancel")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 500.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Button(
                                    onClick = onAddClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                ) {
                                    Text("+ Add Category")
                                }
                            }

                            items(categories, key = { it }) { cat ->
                                val isDefault =
                                    initialCategories.any { it.equals(cat, ignoreCase = true) }
                                val deleteTint = trashIconTint()

                                ExactFrostedPillCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(25.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (!isDefault) {
                                            IconButton(onClick = { onDeleteClick(cat) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete category",
                                                    tint = deleteTint
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkCategorySelectionSheet(
    show: Boolean,
    categories: List<String>,
    onBeginDismiss: () -> Unit,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    if (!show) return

    val scope = rememberCoroutineScope()
    var isVisible by remember(show) { mutableStateOf(false) }
    var isDismissing by remember(show) { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.94f,
        animationSpec = tween(
            durationMillis = if (isVisible) {
                EDIT_CATEGORIES_SHEET_ENTER_DURATION_MS
            } else {
                EDIT_CATEGORIES_SHEET_EXIT_DURATION_MS
            },
            easing = if (isVisible) {
                LinearOutSlowInEasing
            } else {
                FastOutLinearInEasing
            }
        ),
        label = "bulkCategoryCardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isVisible) {
                EDIT_CATEGORIES_SHEET_ENTER_DURATION_MS
            } else {
                180
            },
            easing = if (isVisible) {
                LinearOutSlowInEasing
            } else {
                FastOutLinearInEasing
            }
        ),
        label = "bulkCategoryCardAlpha"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isVisible) {
            tween(
                durationMillis = EDIT_CATEGORIES_SHEET_ENTER_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "bulkCategoryScrimAlpha"
    )

    LaunchedEffect(show) {
        if (show) {
            isVisible = true
        }
    }

    fun dismissSheet() {
        if (isDismissing) return

        isDismissing = true
        onBeginDismiss()
        isVisible = false

        scope.launch {
            delay(EDIT_CATEGORIES_SHEET_EXIT_DURATION_MS.toLong())
            onDismiss()
        }
    }

    fun selectCategory(category: String?) {
        onCategorySelected(category)
        dismissSheet()
    }

    BackHandler(onBack = ::dismissSheet)

    Dialog(
        onDismissRequest = ::dismissSheet,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.28f * scrimAlpha))
                    .clickable { dismissSheet() }
            )

            val cardShape = RoundedCornerShape(30.dp)
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 28.dp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = cardAlpha
                    },
                shape = cardShape,
                color = Color.Transparent,
                shadowElevation = 14.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                ) {
                    if (isDarkTheme) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF141A20),
                                            Color(0xFF0D1115),
                                            Color(0xFF080A0D)
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                            Color.Transparent
                                        ),
                                        center = Offset(140f, 80f),
                                        radius = 520f
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.035f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.14f)
                                        )
                                    )
                                )
                        )
                    } else {
                        AppBackdrop(Modifier.matchParentSize())
                    }

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Add to category", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = ::dismissSheet) {
                                Text("Cancel")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 500.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                ExactFrostedPillCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(25.dp),
                                    shadowElevation = 4.dp,
                                    onClick = { selectCategory(null) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "None",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }

                            items(categories, key = { it }) { cat ->
                                ExactFrostedPillCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(25.dp),
                                    shadowElevation = 4.dp,
                                    onClick = { selectCategory(cat) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPantryTopBar(
    modifier: Modifier = Modifier,
    isSelecting: Boolean,
    selectedItemsCount: Int,
    hasVisibleItems: Boolean,
    allVisibleItemsSelected: Boolean,

    showSearchBar: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onShowSearchBar: () -> Unit,
    searchTextFieldModifier: Modifier,
    onSearchBarTap: () -> Unit,

    categories: List<String>,
    selectedFilterCategory: String,
    onFilterChange: (String) -> Unit,
    selectedSortMode: PantrySortMode,
    onSortChange: (PantrySortMode) -> Unit,

    onEnterSelectionMode: () -> Unit,
    onToggleSelectAll: () -> Unit,

    onBulkAddToCategoryClick: () -> Unit,
    onBulkCustomCategoryClick: () -> Unit,

    onAddCategoryClick: () -> Unit,
    tutorialActive: Boolean = false,
    onTutorialTargetPositioned: (OnboardingSpotlightTarget, Rect) -> Unit = { _, _ -> }
) {
    val enabled = selectedItemsCount > 0
    val selectionTitle =
        if (selectedItemsCount == 1) "1 item selected"
        else "$selectedItemsCount items selected"
    val categoryRowState = rememberLazyListState()
    var showSortMenu by remember { mutableStateOf(false) }
    val categoryTopSpacing by animateDpAsState(
        targetValue = if (showSearchBar) 4.dp else 10.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "homeCategoryTopSpacing"
    )
    val tutorialHeaderActionsModifier =
        if (tutorialActive) {
            Modifier.onGloballyPositioned { coordinates ->
                onTutorialTargetPositioned(
                    OnboardingSpotlightTarget.HOME_SEARCH_SORT,
                    coordinates.boundsInRoot()
                )
            }
        } else {
            Modifier
        }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 3.dp)
    ) {
        EditCategoriesBackgroundSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (isSelecting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectionTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                        TextButton(
                            onClick = onToggleSelectAll,
                            enabled = hasVisibleItems,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (allVisibleItemsSelected) "Clear all" else "Select all",
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionActionChip(
                            modifier = Modifier.weight(1f),
                            text = "Add to category",
                            icon = Icons.Default.Category,
                            contentDescription = "Add to category",
                            enabled = enabled,
                            onClick = onBulkAddToCategoryClick
                        )

                        SelectionActionChip(
                            modifier = Modifier.weight(1f),
                            text = "Custom category",
                            icon = Icons.Default.Edit,
                            contentDescription = "Custom category",
                            enabled = enabled,
                            onClick = onBulkCustomCategoryClick
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Pantry",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            modifier = tutorialHeaderActionsModifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onShowSearchBar) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Show search"
                                )
                            }

                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Show sort options",
                                        tint = if (selectedSortMode == PantrySortMode.NEAREST_EXPIRY) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    PantrySortMode.entries.forEach { mode ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = mode.label,
                                                    fontWeight = if (selectedSortMode == mode) {
                                                        FontWeight.SemiBold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                                )
                                            },
                                            onClick = {
                                                showSortMenu = false
                                                onSortChange(mode)
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = {
                                showSortMenu = false
                                onEnterSelectionMode()
                            }) {
                                Icon(
                                    Icons.Default.Checklist,
                                    contentDescription = "Enter selection mode"
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !isSelecting && showSearchBar,
                    enter = smoothVerticalRevealEnter(),
                    exit = smoothVerticalRevealExit()
                ) {
                    Column {
                        Spacer(Modifier.height(10.dp))

                        CompactSearchBar(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            textFieldModifier = searchTextFieldModifier,
                            onTap = onSearchBarTap
                        )

                        Spacer(Modifier.height(10.dp))
                    }
                }

                if (!isSelecting) {
                    Column {
                        Spacer(Modifier.height(categoryTopSpacing))
                        CategoryPillsLabel()
                        Spacer(Modifier.height(7.dp))

                        LazyRow(
                            state = categoryRowState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                val tutorialCategoryModifier =
                                    if (tutorialActive) {
                                        Modifier.onGloballyPositioned { coordinates ->
                                            onTutorialTargetPositioned(
                                                OnboardingSpotlightTarget.CATEGORY_CONTROLS,
                                                coordinates.boundsInRoot()
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }

                                EditCategoriesChip(
                                    modifier = tutorialCategoryModifier,
                                    onClick = { onAddCategoryClick() }
                                )
                            }

                            item {
                                CategoryChip(
                                    text = "All",
                                    selected = selectedFilterCategory == "All",
                                    onClick = { onFilterChange("All") }
                                )
                            }

                            items(categories, key = { it }) { cat ->
                                CategoryChip(
                                    text = cat,
                                    selected = selectedFilterCategory.equals(cat, ignoreCase = true),
                                    onClick = { onFilterChange(cat) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<String>,
    initialCategories: List<String>,
    selectedCategory: String?,
    isCustom: Boolean,
    onPickCategory: (String?) -> Unit,
    onPickCustom: () -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (isCustom) "Custom..." else (selectedCategory ?: "None"),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Select category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ScrollableCategoryMenuContent(
                    categories = categories,
                    showCustomOption = true,
                    onPickNone = {
                        expanded = false
                        onPickCategory(null)
                    },
                    onPickCustom = {
                        expanded = false
                        onPickCustom()
                    },
                    onPickCategory = { cat ->
                        expanded = false
                        onPickCategory(cat)
                    }
                )
            }
        }
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
private fun ScrollableCategoryMenuContent(
    categories: List<String>,
    showCustomOption: Boolean,
    onPickNone: () -> Unit,
    onPickCustom: (() -> Unit)? = null,
    onPickCategory: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val maxMenuHeight = 180.dp
    val thumbHeight = 36.dp

    val maxThumbOffsetPx = with(density) { (maxMenuHeight - thumbHeight).toPx() }
    val thumbOffsetPx =
        if (scrollState.maxValue == 0) 0f
        else (scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * maxThumbOffsetPx

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxMenuHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(end = 10.dp)
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = onPickNone
            )

            if (showCustomOption) {
                DropdownMenuItem(
                    text = { Text("Custom...") },
                    onClick = { onPickCustom?.invoke() }
                )
            }

            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = { onPickCategory(cat) }
                )
            }
        }

        if (scrollState.maxValue > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = with(density) { thumbOffsetPx.toDp() }, end = 2.dp)
                    .width(3.dp)
                    .height(thumbHeight)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
            )
        }
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
private fun LazyListFastScroller(
    listState: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    if (itemCount <= 0) return

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var lastDragIndex by remember { mutableIntStateOf(-1) }

    val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
    val visibleCount = visibleItemsInfo.size
    if (visibleCount == 0 || itemCount <= visibleCount) return

    val minThumbHeightPx = with(density) { 42.dp.roundToPx().toFloat() }
    val averageItemSizePx = visibleItemsInfo
        .map { it.size }
        .average()
        .toFloat()
        .takeIf { it > 0f && !it.isNaN() }
        ?: 1f

    val lastStartIndex = (itemCount - visibleCount).coerceAtLeast(1)
    val currentIndexEstimate = (
            listState.firstVisibleItemIndex.toFloat() +
                    (listState.firstVisibleItemScrollOffset / averageItemSizePx)
            ).coerceIn(0f, lastStartIndex.toFloat())

    val progress = (currentIndexEstimate / lastStartIndex.toFloat()).coerceIn(0f, 1f)
    val thumbHeightPx = if (trackHeightPx == 0) {
        minThumbHeightPx
    } else {
        max(
            minThumbHeightPx,
            trackHeightPx * (visibleCount.toFloat() / itemCount.toFloat())
        )
    }
    val maxThumbOffsetPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)
    val thumbOffsetPx = progress * maxThumbOffsetPx
    val thumbHeightDp = with(density) { thumbHeightPx.toDp() }

    val trackAlpha by animateFloatAsState(
        targetValue = if (isDragging || listState.isScrollInProgress) 0.18f else 0.10f,
        animationSpec = tween(180),
        label = "fastScrollTrackAlpha"
    )
    val thumbAlpha by animateFloatAsState(
        targetValue = if (isDragging || listState.isScrollInProgress) 0.90f else 0.52f,
        animationSpec = tween(180),
        label = "fastScrollThumbAlpha"
    )

    Box(
        modifier = modifier
            .width(18.dp)
            .fillMaxHeight()
            .onSizeChanged { trackHeightPx = it.height }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = trackAlpha))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                .fillMaxWidth()
                .height(thumbHeightDp)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val newOffset = (thumbOffsetPx + delta).coerceIn(0f, maxThumbOffsetPx)
                        val newProgress =
                            if (maxThumbOffsetPx == 0f) 0f else newOffset / maxThumbOffsetPx
                        val targetIndex = (newProgress * lastStartIndex)
                            .roundToInt()
                            .coerceIn(0, lastStartIndex)

                        if (targetIndex != lastDragIndex) {
                            lastDragIndex = targetIndex
                            scope.launch {
                                listState.scrollToItem(targetIndex)
                            }
                        }
                    },
                    onDragStarted = {
                        isDragging = true
                        lastDragIndex = -1
                    },
                    onDragStopped = {
                        isDragging = false
                        lastDragIndex = -1
                    }
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = thumbAlpha))
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        val appCtx = applicationContext
        createExpiryNotificationChannel(appCtx)

        val notifPrefs = appCtx.getSharedPreferences(NOTIF_PREFS, MODE_PRIVATE)
        lifecycleScope.launch(Dispatchers.Default) {
            if (notifPrefs.getBoolean(DAILY_ENABLED_KEY, false)) {
                scheduleDailyExpiryWork(appCtx)
            }
            if (shouldShowFirstLaunchOnboarding(appCtx)) {
                seedFirstLaunchDemoFoodIfNeeded(appCtx)
            }
        }

        setContent {
            val appCtx = this@MainActivity.applicationContext
            var themeMode by remember { mutableStateOf(loadThemeMode(appCtx)) }

            AppTheme(themeMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppBackdrop(Modifier.matchParentSize())
                    AppNav(
                        themeMode = themeMode,
                        onThemeChange = { mode ->
                            themeMode = mode
                            saveThemeMode(appCtx, mode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AskNotificationPermissionOnFirstLaunch() {
    val context = LocalContext.current
    val appCtx = context.applicationContext

    val prefs = remember {
        appCtx.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->

        }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyPrompted = prefs.getBoolean(NOTIF_FIRST_PROMPT_SHOWN_KEY, false)

            val alreadyGranted =
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!alreadyPrompted && !alreadyGranted) {
                prefs.edit { putBoolean(NOTIF_FIRST_PROMPT_SHOWN_KEY, true) }
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

private data class BottomBarItem(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomBarItems = listOf(
    BottomBarItem(
        route = Route.Home.r,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomBarItem(
        route = Route.History.r,
        label = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BottomBarItem(
        route = Route.Profile.r,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

private fun bottomBarSelectedRoute(route: String?): String? {
    return when {
        route == null -> null
        route.startsWith(Route.Profile.r) -> Route.Profile.r
        else -> route
    }
}

private fun bottomBarRouteIndex(route: String?): Int {
    val selectedRoute = bottomBarSelectedRoute(route)
    val currentIndex = bottomBarItems.indexOfFirst { it.route == selectedRoute }
    if (currentIndex >= 0) return currentIndex

    val homeIndex = bottomBarItems.indexOfFirst { it.route == Route.Home.r }
    return if (homeIndex >= 0) homeIndex else 0
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNav(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val navController = rememberNavController()
    val accountSession = rememberAccountSessionPreference()
    val recipeScreenSessionState = rememberRecipeScreenSessionState()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    var showForm by rememberSaveable { mutableStateOf(false) }
    var blurBackgroundForOverlay by rememberSaveable { mutableStateOf(false) }
    var hideBottomBar by rememberSaveable { mutableStateOf(false) }
    var aiImmersiveMode by rememberSaveable { mutableStateOf(false) }
    var showFirstLaunchOnboarding by rememberSaveable {
        mutableStateOf(shouldShowFirstLaunchOnboarding(appContext))
    }
    var onboardingStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var onboardingTargetBounds by remember {
        mutableStateOf(emptyMap<OnboardingSpotlightTarget, Rect>())
    }
    val layoutDir = LocalLayoutDirection.current
    val firstLaunchOnboardingVisible =
        showFirstLaunchOnboarding && (current == null || current == Route.Home.r)

    fun finishFirstLaunchOnboarding() {
        markFirstLaunchOnboardingComplete(appContext)
        showFirstLaunchOnboarding = false
        onboardingStepIndex = 0
        onboardingTargetBounds = emptyMap()
    }

    if (!showFirstLaunchOnboarding) {
        AskNotificationPermissionOnFirstLaunch()
    }
    AccountCloudSyncEffect(accountSession)
    LaunchedEffect(appContext) {
        applyAutoDeleteRulesOnAppLoad(appContext)
    }
    LaunchedEffect(current) {
        if (current != Route.Recipe.r && aiImmersiveMode) {
            aiImmersiveMode = false
        }
    }

    val shouldHideBottomBar =
        (current == Route.Home.r && hideBottomBar) ||
                (current == Route.Recipe.r && aiImmersiveMode)
    val isHomeBottomBarTransition = current == Route.Home.r
    val bottomBarOffset by animateDpAsState(
        targetValue = if (shouldHideBottomBar) 136.dp else 0.dp,
        animationSpec = if (isHomeBottomBarTransition) {
            tween(
                durationMillis = if (shouldHideBottomBar) 160 else 140,
                easing = FastOutSlowInEasing
            )
        } else {
            tween(
                durationMillis = if (shouldHideBottomBar) 380 else 260,
                easing = FastOutSlowInEasing
            )
        },
        label = "appBottomBarOffset"
    )
    val bottomBarAlpha by animateFloatAsState(
        targetValue = if (shouldHideBottomBar) 0f else 1f,
        animationSpec = if (isHomeBottomBarTransition) {
            tween(
                durationMillis = if (shouldHideBottomBar) 130 else 120,
                easing = FastOutSlowInEasing
            )
        } else {
            tween(
                durationMillis = if (shouldHideBottomBar) 320 else 220,
                easing = FastOutSlowInEasing
            )
        },
        label = "appBottomBarAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.blurWhen(
                if (showForm || blurBackgroundForOverlay) 16.dp else 0.dp
            ),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                BottomBar(
                    modifier = Modifier
                        .offset {
                            IntOffset(0, bottomBarOffset.roundToPx())
                        }
                        .graphicsLayer { alpha = bottomBarAlpha },
                    navController = navController,
                    currentRoute = current,
                    onTabPressed = { route ->
                        aiImmersiveMode = route == Route.Recipe.r
                    },
                    tutorialActive = firstLaunchOnboardingVisible,
                    onTutorialTargetPositioned = { target, bounds ->
                        if (onboardingTargetBounds[target] != bounds) {
                            onboardingTargetBounds =
                                onboardingTargetBounds.toMutableMap().apply {
                                    put(target, bounds)
                                }
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.r,
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = padding.calculateStartPadding(layoutDir),
                        end = padding.calculateEndPadding(layoutDir)
                    ),

                enterTransition = { bottomTabEnterTransition() },

                exitTransition = { bottomTabExitTransition() },

                popEnterTransition = { bottomTabEnterTransition() },

                popExitTransition = { bottomTabExitTransition() },

                sizeTransform = { null }
            ) {
                composable(Route.Home.r) {
                    HomeScreen(
                        showForm = showForm,
                        onShowFormChange = { showForm = it },
                        onSelectionModeChange = { hideBottomBar = it },
                        onOverlayVisibilityChange = { blurBackgroundForOverlay = it },
                        onOpenAi = {
                            aiImmersiveMode = true
                            navController.safeNavigate(Route.Recipe.r)
                        },
                        tutorialActive = firstLaunchOnboardingVisible,
                        onTutorialTargetPositioned = { target, bounds ->
                            if (onboardingTargetBounds[target] != bounds) {
                                onboardingTargetBounds =
                                    onboardingTargetBounds.toMutableMap().apply {
                                        put(target, bounds)
                                    }
                            }
                        }
                    )
                }

                composable(Route.Profile.r) { ProfileScreen(navController) }
                composable(Route.Account.r) { AccountScreen(navController) }
                composable(Route.Settings.r) { SettingsScreen(navController) }
                composable(Route.About.r) { AboutScreen(navController) }
                composable(Route.Help.r) { HelpScreen(navController) }
                composable(Route.Privacy.r) { PrivacyScreen(navController) }
                composable(Route.History.r) {
                    HistoryScreen(
                        onOverlayVisibilityChange = { blurBackgroundForOverlay = it }
                    )
                }
                composable(
                    route = Route.Recipe.r,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 260,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 180,
                                easing = FastOutLinearInEasing
                            )
                        )
                    },
                    popEnterTransition = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    },
                    popExitTransition = {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 160,
                                easing = FastOutLinearInEasing
                            )
                        )
                    }
                ) {
                    RecipeScreen(
                        sessionState = recipeScreenSessionState,
                        immersiveMode = aiImmersiveMode,
                        onExitImmersiveMode = {
                            navController.safeNavigate(Route.Home.r)
                        }
                    )
                }

                composable(Route.Theme.r) {
                    ThemeScreen(
                        navController = navController,
                        currentMode = themeMode,
                        onModeChange = onThemeChange
                    )
                }

                composable(Route.CountdownFormat.r) {
                    CountdownFormatScreen(navController)
                }

                composable(Route.Notifications.r) {
                    NotificationsScreen(navController)
                }

                composable(Route.AutoDelete.r) {
                    AutoDeleteScreen(navController)
                }
            }
        }

        FirstLaunchOnboardingOverlay(
            visible = firstLaunchOnboardingVisible,
            stepIndex = onboardingStepIndex,
            targetBounds = onboardingTargetBounds,
            onNext = {
                if (onboardingStepIndex >= firstLaunchOnboardingSteps.lastIndex) {
                    finishFirstLaunchOnboarding()
                } else {
                    onboardingStepIndex += 1
                }
            },
            onSkip = ::finishFirstLaunchOnboarding
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOverlayVisibilityChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE) }
    val gson = remember { Gson() }
    val density = LocalDensity.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val historySearchFocus = remember { FocusRequester() }

    var search by rememberSaveable { mutableStateOf("") }
    var showSearchBar by rememberSaveable { mutableStateOf(false) }

    val history = remember {
        mutableStateListOf<HistoryEntry>().apply {
            addAll(loadAndSyncHistoryEntries(prefs, gson))
        }
    }
    val pantryFoods = remember {
        mutableStateListOf<FoodItem>().apply {
            addAll(loadFoodList(prefs, gson))
        }
    }

    val categories = remember {
        mutableStateListOf<String>().apply {
            addAll(loadStringList(prefs, gson, CATEGORIES_LIST_KEY))
        }
    }

    DisposableEffect(prefs, gson) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    HISTORY_LIST_KEY,
                    HISTORY_AUTO_DELETE_ENABLED_KEY,
                    HISTORY_RETENTION_DAYS_KEY ->
                        replaceListContentsIfChanged(history, loadHistoryEntries(prefs, gson))

                    FOOD_LIST_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_ENABLED_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_DAYS_KEY ->
                        replaceListContentsIfChanged(pantryFoods, loadFoodList(prefs, gson))

                    CATEGORIES_LIST_KEY -> replaceListContentsIfChanged(
                        categories,
                        loadStringList(prefs, gson, CATEGORIES_LIST_KEY)
                    )
                }
            }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val filtered by remember {
        derivedStateOf {
            val q = search.trim()
            val src = history.toList()
            if (q.isBlank()) src else src.filter { it.name.contains(q, ignoreCase = true) }
        }
    }
    val historySections by remember {
        derivedStateOf { buildHistoryDateSections(filtered) }
    }
    val listState = rememberLazyListState()
    val historyBottomSafePadding = listBottomSafePadding(
        itemCount = filtered.size,
        hasFab = false
    )
    val isHistorySearchKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var historySearchKeyboardWasVisible by remember { mutableStateOf(false) }

    var quickAddName by remember { mutableStateOf<String?>(null) }
    var quickAddExpiry by remember { mutableStateOf("") }
    var quickAddCategory by remember { mutableStateOf<String?>(null) }
    var quickAddQuantityText by remember { mutableStateOf(MIN_FOOD_QUANTITY.toString()) }
    var quickAddError by remember { mutableStateOf<String?>(null) }
    var pendingDeleteHistoryEntry by remember { mutableStateOf<HistoryEntry?>(null) }
    val shouldBlurBackground =
        quickAddName != null ||
                pendingDeleteHistoryEntry != null

    SideEffect {
        onOverlayVisibilityChange(shouldBlurBackground)
    }

    DisposableEffect(Unit) {
        onDispose { onOverlayVisibilityChange(false) }
    }

    fun closeHistorySearch() {
        focusManager.clearFocus(force = true)
        keyboard?.hide()
        search = ""
        showSearchBar = false
        historySearchKeyboardWasVisible = false
    }

    OverlayBackHandler(enabled = showSearchBar) {
        closeHistorySearch()
    }

    BackHandler(
        enabled = showSearchBar &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                !isHistorySearchKeyboardVisible
    ) {
        closeHistorySearch()
    }

    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            historySearchFocus.requestFocus()
            keyboard?.show()
        } else {
            historySearchKeyboardWasVisible = false
        }
    }

    LaunchedEffect(showSearchBar, isHistorySearchKeyboardVisible) {
        if (!showSearchBar) return@LaunchedEffect

        if (isHistorySearchKeyboardVisible) {
            historySearchKeyboardWasVisible = true
            return@LaunchedEffect
        }

        if (historySearchKeyboardWasVisible) {
            closeHistorySearch()
        }
    }

    val historyTopBarDensity = LocalDensity.current
    val defaultHistoryTopBarHeightPx = with(historyTopBarDensity) { 84.dp.roundToPx() }
    var historyTopBarHeightPx by remember { mutableIntStateOf(defaultHistoryTopBarHeightPx) }
    val historyContentTopPadding = with(historyTopBarDensity) { historyTopBarHeightPx.toDp() } + 8.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.ime
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = historyContentTopPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (search.isNotBlank()) "No history found" else "No history yet")
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        overscrollEffect = null,
                        contentPadding = PaddingValues(
                            top = historyContentTopPadding,
                            bottom = 80.dp
                        )
                    ) {
                        historySections.forEach { section ->
                            item(
                                key = "history-section-${section.title}",
                                contentType = "history_section_header"
                            ) {
                                HistorySectionHeader(title = section.title)
                            }

                            items(
                                items = section.entries,
                                key = { normalizeFoodName(it.name) },
                                contentType = { "history_food_item" }
                            ) { entry ->
                                HistoryFoodCard(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = tween(
                                            durationMillis = SWIPE_ITEM_PLACEMENT_DURATION_MS,
                                            easing = FastOutSlowInEasing
                                        )
                                    ),
                                    name = entry.name,
                                    onQuickAdd = {
                                        quickAddName = entry.name
                                        quickAddExpiry = ""
                                        quickAddCategory = null
                                        quickAddQuantityText = MIN_FOOD_QUANTITY.toString()
                                        quickAddError = null
                                    },
                                    onDelete = {
                                        pendingDeleteHistoryEntry = entry
                                    }
                                )
                            }
                        }
                    }
                }

                HistoryTopBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .onSizeChanged { historyTopBarHeightPx = it.height },
                    showSearchBar = showSearchBar,
                    searchQuery = search,
                    onSearchQueryChange = { search = it },
                    onShowSearchBar = {
                        if (!showSearchBar) {
                            showSearchBar = true
                        } else {
                            historySearchFocus.requestFocus()
                            keyboard?.show()
                        }
                    },
                    searchTextFieldModifier = Modifier.focusRequester(historySearchFocus),
                    onSearchBarTap = {
                        historySearchFocus.requestFocus()
                        keyboard?.show()
                    }
                )
            }
        }
    }

    pendingDeleteHistoryEntry?.let { entry ->
        GlassAlertDialog(
            onDismissRequest = { pendingDeleteHistoryEntry = null },
            title = { Text("Delete item?") },
            text = { Text("Are you sure you want to delete \"${entry.name}\" from history?") },
            confirmButton = {
                TextButton(onClick = {
                    if (quickAddName == entry.name) {
                        quickAddName = null
                        quickAddExpiry = ""
                        quickAddCategory = null
                        quickAddQuantityText = MIN_FOOD_QUANTITY.toString()
                        quickAddError = null
                    }
                    removeFoodNameFromHistory(prefs, gson, history, entry.name)
                    pendingDeleteHistoryEntry = null
                }) {
                    DialogDestructiveText("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteHistoryEntry = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (quickAddName != null) {
        GlassAlertDialog(
            onDismissRequest = {
                quickAddName = null
                quickAddQuantityText = MIN_FOOD_QUANTITY.toString()
                quickAddError = null
            },
            title = { Text("Quick Add", style = MaterialTheme.typography.titleLarge) },
            text = {
                val quickAddWillMerge =
                    quickAddName != null &&
                            quickAddExpiry.isNotBlank() &&
                            pantryFoods.any { food ->
                                food.batchKey() == foodBatchKey(
                                    quickAddName.orEmpty(),
                                    quickAddExpiry
                                )
                            }

                Column {
                    Text("Food: ${quickAddName!!}", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    ExpiryDateInputField(
                        value = quickAddExpiry,
                        onValueChange = {
                            quickAddExpiry = it
                            quickAddError = null
                        },
                        onCalendarClick = {
                            openExpiryDatePicker(context) { pickedDate ->
                                quickAddExpiry = pickedDate
                                quickAddError = null
                            }
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = quickAddQuantityText,
                        onValueChange = { value ->
                            quickAddQuantityText = value.filter { it.isDigit() }.take(3)
                            quickAddError = null
                        },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    var catExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = !catExpanded }
                    ) {
                        OutlinedTextField(
                            value = quickAddCategory ?: "None",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            ScrollableCategoryMenuContent(
                                categories = categories,
                                showCustomOption = false,
                                onPickNone = {
                                    quickAddCategory = null
                                    catExpanded = false
                                },
                                onPickCategory = { cat ->
                                    quickAddCategory = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }

                    if (quickAddError != null) {
                        Spacer(Modifier.height(8.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(quickAddError!!, color = MaterialTheme.colorScheme.error)
                    } else if (quickAddWillMerge) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Same batch found. Add will increase its quantity.",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = quickAddName!!.trim()

                        if (quickAddExpiry.isBlank()) {
                            quickAddError = "Pick an expiry date first."
                            return@TextButton
                        }

                        if (!isValidFutureExpiryDate(quickAddExpiry)) {
                            quickAddError = "Enter a valid future date."
                            return@TextButton
                        }

                        val quantity = quickAddQuantityText.toIntOrNull()
                        if (quantity == null || quantity !in MIN_FOOD_QUANTITY..MAX_FOOD_QUANTITY) {
                            quickAddError = "Enter a quantity from $MIN_FOOD_QUANTITY to $MAX_FOOD_QUANTITY."
                            return@TextButton
                        }

                        val newFood =
                            FoodItem(
                                name = name,
                                expiry = quickAddExpiry,
                                category = quickAddCategory,
                                quantity = quantity
                            )
                        upsertFoodBatch(pantryFoods, newFood)
                        saveFoodList(prefs, gson, pantryFoods)

                        recordFoodNameInHistory(prefs, gson, history, name)

                        quickAddName = null
                        quickAddExpiry = ""
                        quickAddCategory = null
                        quickAddQuantityText = MIN_FOOD_QUANTITY.toString()
                        quickAddError = null
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    quickAddName = null
                    quickAddQuantityText = MIN_FOOD_QUANTITY.toString()
                    quickAddError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistorySectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant.copy(alpha = 0.86f)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(scheme.outlineVariant.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun PantryFoodCard(
    modifier: Modifier = Modifier,
    food: FoodItem,
    countdownFormat: CountdownFormat,
    isSelecting: Boolean,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = RoundedCornerShape(25.dp)
    val layoutDir = LocalLayoutDirection.current
    val isRtl = layoutDir == LayoutDirection.Rtl
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val thresholdPx = with(density) { 90.dp.toPx() }
    val maxDragPx = with(density) { 350.dp.toPx() }
    val revealPx = with(density) { 60.dp.toPx() }

    val offsetX = remember(food) { Animatable(0f) }
    var itemVisible by remember(food) { mutableStateOf(true) }

    val isEditDragNow = if (!isRtl) offsetX.value > 0f else offsetX.value < 0f
    val isDeleteDragNow = if (!isRtl) offsetX.value < 0f else offsetX.value > 0f

    val reveal = (abs(offsetX.value) / revealPx).coerceIn(0f, 1f)
    val bgAlpha = 0.15f + (0.85f * reveal)
    val deleteTint = trashIconTint()

    val bg = when {
        isEditDragNow -> MaterialTheme.colorScheme.secondaryContainer
        isDeleteDragNow -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }

    val tint = when {
        isEditDragNow -> MaterialTheme.colorScheme.onSecondaryContainer
        isDeleteDragNow -> deleteTint
        else -> MaterialTheme.colorScheme.onSurface
    }

    val icon = when {
        isEditDragNow -> Icons.Default.Edit
        isDeleteDragNow -> Icons.Default.Delete
        else -> null
    }

    val label = when {
        isEditDragNow -> "Edit"
        isDeleteDragNow -> "Delete"
        else -> ""
    }

    val align = when {
        isEditDragNow -> Alignment.CenterStart
        isDeleteDragNow -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }

    val checkboxGapWidth = if (isSelecting) 10.dp else 0.dp
    val checkboxSlotWidth = if (isSelecting) 36.dp else 0.dp

    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = itemVisible,
        exit =
            shrinkVertically(
                animationSpec = tween(
                    durationMillis = SWIPE_DELETE_EXIT_DURATION_MS,
                    easing = FastOutSlowInEasing
                )
            ) +
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = SWIPE_DELETE_EXIT_DURATION_MS,
                            easing = LinearOutSlowInEasing
                        )
                    ) +
                    scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = SWIPE_DELETE_EXIT_DURATION_MS,
                            easing = FastOutSlowInEasing
                        )
                    )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SWIPE_ITEM_SPACING_DP.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(bg.copy(alpha = bgAlpha))
                    .padding(horizontal = 24.dp),
                contentAlignment = align
            ) {
                if (icon != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = tint.copy(alpha = reveal)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            label,
                            color = tint.copy(alpha = reveal),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        enabled = !isSelecting,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                val newValue = (offsetX.value + delta).coerceIn(-maxDragPx, maxDragPx)
                                offsetX.snapTo(newValue)
                            }
                        },
                        onDragStopped = {
                            val current = offsetX.value
                            val farEnough = abs(current) >= thresholdPx

                            if (!isSelecting && farEnough) {
                                val isEdit = if (!isRtl) current > 0f else current < 0f

                                if (isEdit) {
                                    onEdit()
                                } else {
                                    onDelete()
                                }
                            }

                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = SWIPE_RESET_DURATION_MS,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                        }
                    )
            ) {
                PantryListGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    shadowElevation = 6.dp,
                    onClick = if (isSelecting) {
                        { onSelectionChange(!isSelected) }
                    } else {
                        null
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = food.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "Expiry: ${food.expiry}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (food.quantity > MIN_FOOD_QUANTITY) {
                                Spacer(modifier = Modifier.height(6.dp))
                                FoodQuantityBadge(quantity = food.quantity)
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ExpiryCountdownBadge(
                                expiry = food.expiry,
                                countdownFormat = countdownFormat
                            )

                            Spacer(modifier = Modifier.width(checkboxGapWidth))

                            Box(
                                modifier = Modifier
                                    .width(checkboxSlotWidth),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (isSelecting) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = onSelectionChange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFoodCard(
    modifier: Modifier = Modifier,
    name: String,
    onQuickAdd: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = RoundedCornerShape(25.dp)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val thresholdPx = with(density) { 90.dp.toPx() }
    val maxDragPx = with(density) { 350.dp.toPx() }
    val revealPx = with(density) { 60.dp.toPx() }

    val offsetX = remember(name) { Animatable(0f) }
    var itemVisible by remember(name) { mutableStateOf(true) }

    val isDeleteDragNow = offsetX.value < 0f

    val reveal = (abs(offsetX.value) / revealPx).coerceIn(0f, 1f)
    val bgAlpha = 0.15f + (0.85f * reveal)
    val deleteTint = trashIconTint()
    val bg = if (isDeleteDragNow) MaterialTheme.colorScheme.errorContainer else Color.Transparent
    val tint =
        if (isDeleteDragNow) deleteTint
        else MaterialTheme.colorScheme.onSurface
    val align = Alignment.CenterEnd

    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = itemVisible,
        exit =
            shrinkVertically(
                animationSpec = tween(
                    durationMillis = SWIPE_DELETE_EXIT_DURATION_MS,
                    easing = FastOutSlowInEasing
                )
            ) +
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = SWIPE_DELETE_EXIT_DURATION_MS,
                            easing = LinearOutSlowInEasing
                        )
                    ) +
                    scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = SWIPE_DELETE_EXIT_DURATION_MS,
                            easing = FastOutSlowInEasing
                        )
                    )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SWIPE_ITEM_SPACING_DP.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(bg.copy(alpha = bgAlpha))
                    .padding(horizontal = 24.dp),
                contentAlignment = align
            ) {
                if (isDeleteDragNow) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = tint.copy(alpha = reveal)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Delete",
                            color = tint.copy(alpha = reveal),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            offsetX.value.roundToInt(),
                            0
                        )
                    }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                val newValue =
                                    (offsetX.value + delta).coerceIn(-maxDragPx, 0f)
                                offsetX.snapTo(newValue)
                            }
                        },
                        onDragStopped = {
                            val current = offsetX.value
                            val farEnough = abs(current) >= thresholdPx
                            val isDelete = current < 0f

                            if (farEnough && isDelete) {
                                onDelete()
                            }

                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = SWIPE_RESET_DURATION_MS,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                        }
                    )
            ) {
                PantryListGlassCard(
                    shape = cardShape,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        IconButton(onClick = onQuickAdd) {
                            Icon(Icons.Default.Add, contentDescription = "Quick add")
                        }
                    }
                }
            }
        }
    }
}

private fun loadRecipeChatMessages(
    gson: Gson,
    json: String
): List<RecipeChatMessage> {
    if (json.isBlank()) return emptyList()
    val type = object : TypeToken<List<RecipeChatMessage>>() {}.type
    return runCatching {
        gson.fromJson<List<RecipeChatMessage>>(json, type)
    }.getOrDefault(emptyList())
}

private fun loadRecipeIngredientContext(
    gson: Gson,
    json: String
): List<String> {
    if (json.isBlank()) return emptyList()
    val type = object : TypeToken<List<String>>() {}.type
    return runCatching {
        gson.fromJson<List<String>>(json, type)
    }.getOrDefault(emptyList())
}

private fun normalizeRecipeText(raw: String): String {
    return raw.trim().lowercase(Locale.US).replace(Regex("\\s+"), " ")
}

private fun normalizeRecipeTitleKey(raw: String): String {
    return raw
        .trim()
        .lowercase(Locale.US)
        .replace("&", " and ")
        .replace(Regex("[^a-z0-9]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun recipeIdentityKey(recipe: RecipeSuggestion): String {
    return normalizeRecipeTitleKey(recipe.title).ifBlank {
        recipeStorageKey(recipe)
    }
}

private fun recipeStorageKey(recipe: RecipeSuggestion): String {
    return listOf(
        recipe.title,
        recipe.usedIngredients.joinToString("|"),
        recipe.missedIngredients.joinToString("|"),
        recipe.quickGuide.joinToString("|")
    ).joinToString("::", transform = ::normalizeRecipeText)
}

private fun cleanRecipeList(items: List<String>): List<String> {
    return items
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.isNotBlank() }
}

private fun cleanSavedRecipe(recipe: RecipeSuggestion): RecipeSuggestion? {
    val title = recipe.title.trim().replace(Regex("\\s+"), " ")
    if (title.isBlank()) return null

    val usedIngredients = cleanRecipeList(recipe.usedIngredients)
    val missedIngredients = cleanRecipeList(recipe.missedIngredients)
    val quickGuide = cleanRecipeList(recipe.quickGuide)

    return RecipeSuggestion(
        title = title,
        usedIngredientCount = usedIngredients.size,
        missedIngredientCount = missedIngredients.size,
        usedIngredients = usedIngredients,
        missedIngredients = missedIngredients,
        quickGuide = quickGuide
    )
}

private fun normalizeSavedRecipes(savedRecipes: List<SavedRecipe>): MutableList<SavedRecipe> {
    val deduped = LinkedHashMap<String, SavedRecipe>()

    savedRecipes
        .sortedByDescending { it.savedAt }
        .forEach { saved ->
            val cleanedRecipe = cleanSavedRecipe(saved.recipe) ?: return@forEach
            val key = recipeIdentityKey(cleanedRecipe)
            deduped.putIfAbsent(
                key,
                SavedRecipe(
                    recipe = cleanedRecipe,
                    savedAt = saved.savedAt
                )
            )
        }

    return deduped.values.toMutableList()
}

private fun recipeIdentityKeysFromMessages(messages: List<RecipeChatMessage>): Set<String> {
    return messages
        .flatMap { it.recipes }
        .map(::recipeIdentityKey)
        .filter { it.isNotBlank() }
        .toSet()
}

private fun savedRecipeIdentityKeys(savedRecipes: List<SavedRecipe>): Set<String> {
    return savedRecipes
        .map { recipeIdentityKey(it.recipe) }
        .filter { it.isNotBlank() }
        .toSet()
}

private fun savedRecipeTitles(savedRecipes: List<SavedRecipe>): List<String> {
    return savedRecipes
        .map { it.recipe.title.trim() }
        .filter { it.isNotBlank() }
}

private fun shownRecipeTitles(messages: List<RecipeChatMessage>): List<String> {
    return messages
        .flatMap { it.recipes }
        .map { it.title.trim() }
        .filter { it.isNotBlank() }
}

private fun filterRepeatedRecipes(
    batch: RecipeSuggestionBatch,
    blockedRecipeKeys: Set<String>,
    limit: Int
): RecipeSuggestionBatch {
    val seenThisBatch = mutableSetOf<String>()
    val freshRecipes = batch.recipes
        .filter { recipe ->
            val key = recipeIdentityKey(recipe)
            key.isNotBlank() &&
                    key !in blockedRecipeKeys &&
                    seenThisBatch.add(key)
        }
        .take(limit.coerceIn(1, 3))

    return batch.copy(recipes = freshRecipes)
}

private fun loadSavedRecipes(
    prefs: android.content.SharedPreferences,
    gson: Gson
): MutableList<SavedRecipe> {
    val json = prefs.getString(SAVED_RECIPES_KEY, null) ?: return mutableListOf()
    val type = object : TypeToken<List<SavedRecipe>>() {}.type
    val savedRecipes = runCatching {
        gson.fromJson<List<SavedRecipe>>(json, type)
    }.getOrDefault(emptyList())

    return normalizeSavedRecipes(savedRecipes)
}

private fun saveSavedRecipes(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    savedRecipes: List<SavedRecipe>
) {
    val normalized = normalizeSavedRecipes(savedRecipes)
    val json = gson.toJson(normalized)
    if (prefs.getString(SAVED_RECIPES_KEY, null) == json) return
    prefs.edit { putString(SAVED_RECIPES_KEY, json) }
}

private suspend fun saveSavedRecipesAsync(
    prefs: android.content.SharedPreferences,
    gson: Gson,
    savedRecipes: List<SavedRecipe>
) {
    val json = withContext(Dispatchers.Default) {
        gson.toJson(normalizeSavedRecipes(savedRecipes))
    }

    withContext(Dispatchers.IO) {
        if (prefs.getString(SAVED_RECIPES_KEY, null) != json) {
            prefs.edit { putString(SAVED_RECIPES_KEY, json) }
        }
    }
}

private fun compactIngredientSummary(
    items: List<String>,
    maxVisible: Int = 4
): String {
    val cleaned = items
        .map { it.trim() }
        .filter { it.isNotBlank() }

    if (cleaned.isEmpty()) return ""

    val visible = cleaned.take(maxVisible)
    val hiddenCount = cleaned.size - visible.size

    return buildString {
        append(visible.joinToString(", "))
        if (hiddenCount > 0) {
            append(", +$hiddenCount more")
        }
    }
}

private fun expiringFoodLabel(daysLeft: Int): String {
    return when (daysLeft) {
        0 -> "today"
        1 -> "in 1 day"
        else -> "in $daysLeft days"
    }
}

private fun expiringFoodsWindowLabel(windowDays: Int): String {
    return if (windowDays == 1) "1 day" else "$windowDays days"
}

private fun expiringFoodListAnswer(
    foods: List<ExpiringFoodHint>,
    windowDays: Int
): String {
    if (foods.isEmpty()) {
        return "No foods are expiring in the next ${expiringFoodsWindowLabel(windowDays)}."
    }

    return buildString {
        append("Foods expiring soon:")
        foods.forEachIndexed { index, food ->
            append("\n")
            append(index + 1)
            append(". ")
            append(food.name)
            append(": ")
            append(expiringFoodLabel(food.daysLeft))
        }
    }
}

private fun buildRecipeAssistantMessage(batch: RecipeSuggestionBatch): RecipeChatMessage {
    val ingredientSummary = compactIngredientSummary(batch.resolvedIngredients)
    val headline = when {
        batch.recipes.isEmpty() && ingredientSummary.isNotBlank() ->
            "I couldn't find strong recipe cards using $ingredientSummary yet."

        batch.recipes.isEmpty() ->
            "I couldn't find recipe cards right now."

        ingredientSummary.isNotBlank() ->
            "Recipe ideas using $ingredientSummary"

        else -> "Recipe ideas"
    }

    return RecipeChatMessage(
        role = RecipeChatRole.ASSISTANT,
        text = headline,
        resolvedIngredients = batch.resolvedIngredients,
        recipes = batch.recipes
    )
}

private fun wantsExpiringFoodsList(request: String): Boolean {
    val normalized = request.trim().lowercase(Locale.US)
    if (normalized.isBlank()) return false

    val expiringIntent = listOf(
        "expiring soon",
        "expire soon",
        "expires soon",
        "expiry soon",
        "soon to expire",
        "about to expire",
        "almost expired",
        "close to expiring",
        "near expiry",
        "near expiration",
        "going bad",
        "go bad",
        "what expires",
        "what is expiring",
        "which foods are expiring",
        "what foods are expiring",
        "show expiring",
        "list expiring"
    ).any(normalized::contains)

    if (!expiringIntent) return false

    val recipeIntent = listOf(
        "recipe", "recipes", "cook", "make", "meal", "meals",
        "breakfast", "lunch", "dinner", "snack"
    ).any(normalized::contains)
    val listIntent = listOf(
        "which", "what", "show", "list", "tell me"
    ).any(normalized::contains)

    return listIntent || !recipeIntent
}

private fun looksFoodRelatedRecipeRequest(
    request: String,
    pantryIngredients: List<String>
): Boolean {
    val normalized = request.trim().lowercase(Locale.US)
    if (normalized.isBlank()) return false

    val foodKeywords = listOf(
        "recipe", "recipes", "cook", "cooking", "meal", "meals", "ingredient",
        "ingredients", "food", "foods", "eat", "eating", "breakfast", "lunch",
        "dinner", "snack", "dessert", "bake", "baking", "fry", "fried", "boil",
        "grill", "roast", "salad", "soup", "sandwich", "wrap", "pasta", "rice",
        "egg", "eggs", "chicken", "beef", "fish", "vegetable", "veggie", "fruit",
        "bread", "milk", "cheese", "sauce", "spice", "pantry", "expiry",
        "expiring", "expire", "expired", "use up", "make with", "what can i make"
    )

    if (foodKeywords.any(normalized::contains)) return true

    val pantryTokens = pantryIngredients
        .flatMap { ingredient ->
            ingredient.lowercase(Locale.US).split(Regex("[^a-z0-9]+"))
        }
        .filter { it.length >= 3 }
        .distinct()

    if (pantryTokens.any { token ->
            Regex("\\b${Regex.escape(token)}\\b").containsMatchIn(normalized)
        }
    ) {
        return true
    }

    val ingredientLikeSegments = normalized
        .split(Regex("[,\\n/&+]+"))
        .map { it.trim() }
        .count { segment ->
            val tokenCount = segment
                .split(Regex("\\s+"))
                .count { token -> token.length >= 3 }
            tokenCount in 1..4
        }

    return ingredientLikeSegments >= 2
}

private fun wantsRecipesFromPantryList(request: String): Boolean {
    val normalized = request.trim().lowercase(Locale.US)
    if (normalized.isBlank()) return false

    val recipeIntent = listOf(
        "recipe", "recipes", "cook", "meal", "meals", "make", "what can i make",
        "easy", "quick", "dinner", "lunch", "breakfast", "snack"
    ).any(normalized::contains)

    if (!recipeIntent) return false

    return listOf(
        "my list",
        "food list",
        "foods in my list",
        "food in my list",
        "my pantry",
        "pantry list",
        "saved foods",
        "available foods",
        "foods i have",
        "food i have",
        "what i have",
        "from my foods",
        "using my foods",
        "foods in home",
        "home list"
    ).any(normalized::contains)
}

private fun looksLikeFoodIngredientInput(
    request: String,
    pantryIngredients: List<String>
): Boolean {
    val normalized = request.trim().lowercase(Locale.US)
    if (normalized.isBlank()) return false

    val knownFoodTokens = setOf(
        "apple", "avocado", "banana", "bean", "beans", "beef", "bread", "broccoli",
        "butter", "cabbage", "carrot", "cheese", "chicken", "chili", "corn",
        "cucumber", "egg", "eggs", "fish", "flour", "garlic", "grape", "ham",
        "honey", "lettuce", "lemon", "lime", "mango", "meat", "milk", "mushroom",
        "noodle", "noodles", "oat", "oats", "oil", "onion", "orange", "pasta",
        "peanut", "potato", "potatoes", "rice", "salmon", "salt", "sausage",
        "shrimp", "spinach", "sugar", "tomato", "tomatoes", "tuna", "turkey",
        "vegetable", "vegetables", "veggie", "veggies", "yogurt"
    )

    val pantryTokens = pantryIngredients
        .flatMap { ingredient ->
            ingredient.lowercase(Locale.US).split(Regex("[^a-z0-9]+"))
        }
        .filter { it.length >= 3 }
        .distinct()

    val requestTokens = normalized
        .split(Regex("[^a-z0-9]+"))
        .filter { it.length >= 3 }

    return requestTokens.any { token ->
        token in knownFoodTokens || token in pantryTokens
    }
}

private fun isSingleFoodLikeRequest(
    request: String,
    pantryIngredients: List<String>
): Boolean {
    val normalized = request.trim().lowercase(Locale.US)
    if (normalized.isBlank()) return false

    val requestTokens = normalized
        .split(Regex("[^a-z0-9]+"))
        .filter { it.length >= 3 }

    if (requestTokens.size != 1) return false

    val token = requestTokens.single()
    val pantryTokens = pantryIngredients
        .flatMap { ingredient ->
            ingredient.lowercase(Locale.US).split(Regex("[^a-z0-9]+"))
        }
        .filter { it.length >= 3 }
        .distinct()

    val foodSuffixes = listOf(
        "nut", "nuts", "berry", "berries", "bean", "beans", "seed", "seeds",
        "melon", "melons", "pepper", "peppers", "cheese", "fruit", "fruits"
    )

    return token in pantryTokens ||
            looksLikeFoodIngredientInput(token, pantryIngredients) ||
            foodSuffixes.any(token::endsWith)
}

private fun recipePromptPlaceholder(): String {
    return "Give me any foods like eggs, rice, milk..."
}

private fun buildExpiringFoodsRequest(expiringFoods: List<ExpiringFoodHint>): String {
    return "Suggest recipes using these expiring foods: ${
        expiringFoods.joinToString(", ") { it.name }
    }."
}

@Composable
private fun RecipeIntroCard(hasPantryFoods: Boolean) {
    RecipeChatMessageRow(isUser = false) { bubbleModifier ->
        RecipeChatBubble(
            modifier = bubbleModifier,
            isUser = false
        ) {
            Text(
                text = "Food AI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (hasPantryFoods) {
                    "Ask me to make recipes using foods in your list, or type a few ingredients."
                } else {
                    "Add some foods in Home then ask for recipes."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipeQuickSuggestionChips(
    hasPantryFoods: Boolean,
    hasExpiringFoods: Boolean,
    isLoading: Boolean,
    onShowExpiringFoods: () -> Unit,
    onUsePantryFoods: () -> Unit,
    onQuickBreakfast: () -> Unit,
    onOnlyOneRecipe: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            RecipeQuickSuggestionChip(
                text = "Show expiring foods",
                enabled = hasExpiringFoods && !isLoading,
                onClick = onShowExpiringFoods
            )
        }

        item {
            RecipeQuickSuggestionChip(
                text = "Use foods in my list",
                enabled = hasPantryFoods && !isLoading,
                onClick = onUsePantryFoods
            )
        }

        item {
            RecipeQuickSuggestionChip(
                text = "Quick breakfast",
                enabled = !isLoading,
                onClick = onQuickBreakfast
            )
        }

        item {
            RecipeQuickSuggestionChip(
                text = "Only 1 recipe",
                enabled = !isLoading,
                onClick = onOnlyOneRecipe
            )
        }
    }
}

@Composable
private fun RecipeQuickSuggestionChip(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val palette = rememberGlassPalette()
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val chipShape = RoundedCornerShape(50.dp)
    val containerColor =
        if (enabled) {
            palette.card.copy(alpha = if (isDarkTheme) 0.58f else 0.68f)
        } else {
            palette.card.copy(alpha = if (isDarkTheme) 0.28f else 0.36f)
        }
    val borderColor =
        if (enabled) {
            palette.border.copy(alpha = if (isDarkTheme) 0.82f else 0.92f)
        } else {
            palette.border.copy(alpha = if (isDarkTheme) 0.38f else 0.48f)
        }
    val textColor =
        if (enabled) {
            scheme.onSurfaceVariant
        } else {
            scheme.onSurfaceVariant.copy(alpha = if (isDarkTheme) 0.46f else 0.54f)
        }

    Box(
        modifier = Modifier
            .clip(chipShape)
            .border(1.dp, borderColor, chipShape)
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecipeUserMessageCard(text: String) {
    RecipeChatMessageRow(isUser = true) { bubbleModifier ->
        RecipeChatBubble(
            modifier = bubbleModifier,
            isUser = true
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun recipeChatBubbleShape(isUser: Boolean): RoundedCornerShape {
    return if (isUser) {
        RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 24.dp,
            bottomEnd = 8.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 8.dp,
            bottomEnd = 24.dp
        )
    }
}

@Composable
private fun RecipeChatMessageRow(
    isUser: Boolean,
    content: @Composable (Modifier) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        content(
            Modifier
                .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart)
                .fillMaxWidth(0.86f)
                .widthIn(max = 360.dp)
        )
    }
}

@Composable
private fun RecipeChatBubble(
    modifier: Modifier = Modifier,
    isUser: Boolean,
    isError: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!isUser && !isError) {
        PantryListGlassCard(
            modifier = modifier,
            shape = RoundedCornerShape(25.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = content
            )
        }
        return
    }

    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val containerColor =
        when {
            isError -> scheme.errorContainer.copy(alpha = if (isDarkTheme) 0.84f else 0.94f)
            isUser -> scheme.primaryContainer.copy(alpha = if (isDarkTheme) 0.92f else 0.97f)
            else -> {
                if (isDarkTheme) {
                    scheme.surfaceContainerHigh.copy(alpha = 0.9f)
                } else {
                    scheme.surfaceContainerHighest.copy(alpha = 0.985f)
                }
            }
        }
    val contentColor =
        when {
            isError -> scheme.onErrorContainer
            isUser -> scheme.onPrimaryContainer
            else -> scheme.onSurface
        }
    val borderColor =
        when {
            isError -> scheme.error.copy(alpha = if (isDarkTheme) 0.32f else 0.22f)
            isUser -> scheme.primary.copy(alpha = if (isDarkTheme) 0.24f else 0.12f)
            else -> scheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.32f else 0.36f)
        }
    val bubbleShadowElevation =
        when {
            isDarkTheme -> 0.dp
            isUser -> 0.dp
            else -> 3.dp
        }

    Surface(
        modifier = modifier,
        shape = recipeChatBubbleShape(isUser),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = bubbleShadowElevation,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
private fun RecipeSuggestionCard(
    recipe: RecipeSuggestion,
    isSaved: Boolean = false,
    onToggleSaved: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val cardShape = RoundedCornerShape(22.dp)
    val recipeCardOverlay =
        if (isDarkTheme) {
            scheme.surfaceContainerHigh.copy(alpha = 0.38f)
        } else {
            scheme.surface.copy(alpha = 0.26f)
        }
    val recipeCardBorder =
        if (isDarkTheme) {
            scheme.primary.copy(alpha = 0.22f)
        } else {
            scheme.outlineVariant.copy(alpha = 0.26f)
        }
    val titleColor =
        if (isDarkTheme) scheme.primary.copy(alpha = 0.96f) else scheme.onSurface
    val usesLabelColor =
        if (isDarkTheme) scheme.primary.copy(alpha = 0.94f) else scheme.primary
    val addLabelColor =
        if (isDarkTheme) scheme.tertiary.copy(alpha = 0.92f) else scheme.tertiary
    val howLabelColor =
        if (isDarkTheme) scheme.primary.copy(alpha = 0.95f) else scheme.primary
    val mainTextColor =
        if (isDarkTheme) scheme.onSurface.copy(alpha = 0.94f) else scheme.onSurface
    val secondaryTextColor =
        if (isDarkTheme) scheme.onSurfaceVariant.copy(alpha = 0.90f) else scheme.onSurfaceVariant
    val recipeCardBase =
        if (isDarkTheme) {
            listOf(
                scheme.surfaceContainerHigh.copy(alpha = 0.98f),
                scheme.surfaceContainer.copy(alpha = 0.92f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.98f),
                scheme.surfaceContainerLowest.copy(alpha = 0.92f)
            )
        }
    val recipeCardGlow = scheme.primary.copy(alpha = if (isDarkTheme) 0.055f else 0.04f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .drawWithCache {
                val baseBrush = Brush.verticalGradient(recipeCardBase)
                val glowBrush = Brush.linearGradient(
                    colors = listOf(recipeCardGlow, Color.Transparent),
                    start = Offset.Zero,
                    end = Offset(size.width * 0.92f, size.height * 0.72f)
                )

                onDrawBehind {
                    drawRect(baseBrush)
                    drawRect(glowBrush)
                }
            }
            .background(recipeCardOverlay, cardShape)
            .border(1.dp, recipeCardBorder, cardShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    modifier = Modifier.weight(1f)
                )

                if (onToggleSaved != null) {
                    val savePillShape = RoundedCornerShape(50.dp)
                    val savePillColor =
                        if (isSaved) scheme.primary else scheme.onSurfaceVariant
                    Button(
                        onClick = onToggleSaved,
                        shape = savePillShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(
                            1.dp,
                            savePillColor.copy(alpha = if (isDarkTheme) 0.38f else 0.26f)
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = savePillColor.copy(alpha = if (isDarkTheme) 0.14f else 0.09f),
                            contentColor = savePillColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = if (isSaved) "Saved" else "Save",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            val usesLine = compactIngredientSummary(recipe.usedIngredients, maxVisible = 5)
            if (usesLine.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                RecipeInfoLine(
                    label = "Uses",
                    text = usesLine,
                    labelColor = usesLabelColor,
                    textColor = mainTextColor
                )
            }

            val addLine = compactIngredientSummary(recipe.missedIngredients, maxVisible = 3)
            if (addLine.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                RecipeInfoLine(
                    label = "Add",
                    text = addLine,
                    labelColor = addLabelColor,
                    textColor = secondaryTextColor
                )
            }

            if (recipe.quickGuide.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                RecipeInfoLine(
                    label = "How",
                    text = "Quick steps",
                    labelColor = howLabelColor,
                    textColor = mainTextColor
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    recipe.quickGuide.forEachIndexed { index, step ->
                        RecipeStepLine(
                            index = index + 1,
                            text = step,
                            accentColor = howLabelColor,
                            textColor = secondaryTextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeInfoLine(
    label: String,
    text: String,
    labelColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(labelColor.copy(alpha = 0.14f))
                .border(1.dp, labelColor.copy(alpha = 0.22f), RoundedCornerShape(50.dp))
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = labelColor
            )
        }

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = textColor
        )
    }
}

@Composable
private fun RecipeStepLine(
    index: Int,
    text: String,
    accentColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .width(24.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(accentColor.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                modifier = Modifier
                    .padding(vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = accentColor
            )
        }

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = textColor
        )
    }
}

@Composable
private fun RecipeAssistantTextCard(
    message: RecipeChatMessage,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val headlineColor =
        if (!message.isError && isDarkTheme) {
            scheme.primary.copy(alpha = 0.94f)
        } else {
            LocalContentColor.current
        }

    RecipeChatMessageRow(isUser = false) { bubbleModifier ->
        RecipeChatBubble(
            modifier = bubbleModifier.then(modifier),
            isUser = false,
            isError = message.isError
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = headlineColor,
                fontWeight = if (message.isError) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecipeAssistantRecipeCard(
    recipe: RecipeSuggestion,
    isSaved: Boolean,
    onToggleSaved: () -> Unit
) {
    RecipeChatMessageRow(isUser = false) { bubbleModifier ->
        RecipeSuggestionCard(
            recipe = recipe,
            isSaved = isSaved,
            onToggleSaved = onToggleSaved,
            modifier = bubbleModifier
        )
    }
}

@Composable
private fun RecipeAssistantMessageCard(message: RecipeChatMessage) {
    RecipeAssistantTextCard(message)
}

@Composable
private fun RecipeLoadingCard() {
    RecipeChatMessageRow(isUser = false) { bubbleModifier ->
        RecipeChatBubble(
            modifier = bubbleModifier,
            isUser = false
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Food AI is building recipe cards...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun RecipeJumpToTopButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val containerColor =
        if (isDarkTheme) {
            scheme.surfaceContainerHighest.copy(alpha = 0.88f)
        } else {
            Color.White.copy(alpha = 0.94f)
        }
    val borderColor = scheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.78f else 0.58f)

    GlassSurface(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        tone = GlassTone.CHROME,
        containerColor = containerColor,
        borderColor = borderColor,
        showDecorativeOverlays = false,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Go to top of recipe chat",
                tint = scheme.onSurface.copy(alpha = 0.86f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun RecipeJumpToBottomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val containerColor =
        if (isDarkTheme) {
            scheme.surfaceContainerHighest.copy(alpha = 0.88f)
        } else {
            Color.White.copy(alpha = 0.94f)
        }
    val borderColor = scheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.78f else 0.58f)

    GlassSurface(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        tone = GlassTone.CHROME,
        containerColor = containerColor,
        borderColor = borderColor,
        showDecorativeOverlays = false,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Go to latest recipe message",
                tint = scheme.onSurface.copy(alpha = 0.86f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun RecipeJumpControls(
    showJumpToTop: Boolean,
    showJumpToBottom: Boolean,
    onJumpToTop: () -> Unit,
    onJumpToBottom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonEnter = fadeIn(
        animationSpec = tween(
            durationMillis = 130,
            easing = LinearOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.88f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        )
    )
    val buttonExit = fadeOut(
        animationSpec = tween(
            durationMillis = 110,
            easing = FastOutLinearInEasing
        )
    ) + scaleOut(
        targetScale = 0.88f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutLinearInEasing
        )
    )

    Row(
        modifier = modifier.padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecipeJumpControlSlot(
            visible = showJumpToTop,
            enter = buttonEnter,
            exit = buttonExit
        ) {
            RecipeJumpToTopButton(onClick = onJumpToTop)
        }

        RecipeJumpControlSlot(
            visible = showJumpToBottom,
            enter = buttonEnter,
            exit = buttonExit
        ) {
            RecipeJumpToBottomButton(onClick = onJumpToBottom)
        }
    }
}

@Composable
private fun RecipeJumpControlSlot(
    visible: Boolean,
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = enter,
            exit = exit
        ) {
            content()
        }
    }
}

@Composable
private fun RecipeNewChatButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val containerColor =
        if (isDarkTheme) {
            scheme.surfaceContainerHighest.copy(alpha = 0.86f)
        } else {
            Color.White.copy(alpha = 0.92f)
        }
    val borderColor = scheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.72f else 0.54f)

    GlassSurface(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        tone = GlassTone.CHROME,
        containerColor = containerColor,
        borderColor = borderColor,
        showDecorativeOverlays = false,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Start new AI chat",
                tint = scheme.primary.copy(alpha = 0.88f),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "New chat",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun RecipeSavedRecipesButton(
    savedCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val containerColor =
        if (isDarkTheme) {
            scheme.surfaceContainerHighest.copy(alpha = 0.86f)
        } else {
            Color.White.copy(alpha = 0.92f)
        }
    val borderColor = scheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.72f else 0.54f)

    GlassSurface(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        tone = GlassTone.CHROME,
        containerColor = containerColor,
        borderColor = borderColor,
        showDecorativeOverlays = false,
        shadowElevation = 2.dp
    ) {
        Text(
            text = if (savedCount > 0) "Saved recipes $savedCount" else "Saved recipes",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface.copy(alpha = 0.88f)
        )
    }
}

@Composable
private fun SavedRecipeListCard(
    recipe: RecipeSuggestion,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val deleteTint = trashIconTint()

    ExactFrostedPillCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = if (expanded) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = { onExpandedChange(!expanded) }) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (expanded) {
                            "Hide recipe details"
                        } else {
                            "Show recipe details"
                        },
                        tint = scheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                scheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.34f else 0.54f)
                            )
                    )
                    Spacer(Modifier.height(12.dp))
                    SavedRecipeDetails(recipe = recipe)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onRemove,
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = deleteTint.copy(alpha = if (isDarkTheme) 0.42f else 0.30f)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = deleteTint.copy(
                                    alpha = if (isDarkTheme) 0.18f else 0.12f
                                ),
                                contentColor = deleteTint
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Text(
                                text = "Delete recipe",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedRecipeDetails(
    recipe: RecipeSuggestion
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val usesLabelColor =
        if (isDarkTheme) scheme.primary.copy(alpha = 0.94f) else scheme.primary
    val addLabelColor =
        if (isDarkTheme) scheme.tertiary.copy(alpha = 0.92f) else scheme.tertiary
    val howLabelColor =
        if (isDarkTheme) scheme.primary.copy(alpha = 0.95f) else scheme.primary
    val mainTextColor =
        if (isDarkTheme) scheme.onSurface.copy(alpha = 0.94f) else scheme.onSurface
    val secondaryTextColor =
        if (isDarkTheme) scheme.onSurfaceVariant.copy(alpha = 0.90f) else scheme.onSurfaceVariant
    val usesLine = compactIngredientSummary(recipe.usedIngredients, maxVisible = 5)
    val addLine = compactIngredientSummary(recipe.missedIngredients, maxVisible = 3)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (usesLine.isNotBlank()) {
            RecipeInfoLine(
                label = "Uses",
                text = usesLine,
                labelColor = usesLabelColor,
                textColor = mainTextColor
            )
        }

        if (addLine.isNotBlank()) {
            if (usesLine.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
            }
            RecipeInfoLine(
                label = "Add",
                text = addLine,
                labelColor = addLabelColor,
                textColor = secondaryTextColor
            )
        }

        if (recipe.quickGuide.isNotEmpty()) {
            if (usesLine.isNotBlank() || addLine.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
            }
            RecipeInfoLine(
                label = "How",
                text = "Quick steps",
                labelColor = howLabelColor,
                textColor = mainTextColor
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                recipe.quickGuide.forEachIndexed { index, step ->
                    RecipeStepLine(
                        index = index + 1,
                        text = step,
                        accentColor = howLabelColor,
                        textColor = secondaryTextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedRecipesDialog(
    savedRecipes: List<SavedRecipe>,
    onDismiss: () -> Unit,
    onRemove: (RecipeSuggestion) -> Unit
) {
    var expandedRecipeKey by remember(savedRecipes) { mutableStateOf<String?>(null) }
    var pendingDeleteRecipe by remember { mutableStateOf<RecipeSuggestion?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            EditCategoriesBackgroundSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 14.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saved recipes", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onDismiss) {
                            Text("Close")
                        }
                    }

                    if (savedRecipes.isEmpty()) {
                        Text(
                            text = "Saved recipes will show here after you tap Save on an AI recipe card.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = savedRecipes,
                                key = { recipeIdentityKey(it.recipe) }
                            ) { savedRecipe ->
                                val savedRecipeKey = recipeIdentityKey(savedRecipe.recipe)
                                SavedRecipeListCard(
                                    recipe = savedRecipe.recipe,
                                    expanded = expandedRecipeKey == savedRecipeKey,
                                    onExpandedChange = { shouldExpand ->
                                        expandedRecipeKey = if (shouldExpand) savedRecipeKey else null
                                    },
                                    onRemove = { pendingDeleteRecipe = savedRecipe.recipe }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteRecipe?.let { recipeToDelete ->
        GlassAlertDialog(
            onDismissRequest = { pendingDeleteRecipe = null },
            title = { DialogTitleText("Delete saved recipe?") },
            text = {
                DialogBodyText(
                    "Remove \"${recipeToDelete.title}\" from your saved recipes?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val recipeKey = recipeIdentityKey(recipeToDelete)
                        if (expandedRecipeKey == recipeKey) {
                            expandedRecipeKey = null
                        }
                        pendingDeleteRecipe = null
                        onRemove(recipeToDelete)
                    }
                ) {
                    DialogDestructiveText("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRecipe = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RecipePromptBar(
    value: String,
    placeholder: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    val barContainerColor =
        if (isDarkTheme) {
            scheme.surfaceContainerHighest.copy(alpha = 0.97f)
        } else {
            Color.White.copy(alpha = 0.985f)
        }
    val barBorderColor =
        scheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.90f else 0.76f)
    val sendEnabled = value.trim().isNotBlank() && !isLoading
    val sendButtonShape = RoundedCornerShape(50.dp)
    val sendButtonContainerColor =
        if (sendEnabled) {
            scheme.primary.copy(alpha = if (isDarkTheme) 0.82f else 0.68f)
        } else {
            scheme.onSurface.copy(alpha = if (isDarkTheme) 0.12f else 0.08f)
        }
    val sendButtonContentColor =
        if (sendEnabled) {
            scheme.onPrimary
        } else {
            scheme.onSurfaceVariant.copy(alpha = 0.72f)
        }

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        tone = GlassTone.CHROME,
        containerColor = barContainerColor,
        borderColor = barBorderColor,
        showDecorativeOverlays = false,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = { if (it.length <= 220) onValueChange(it) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSend() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .width(42.dp)
                    .clip(sendButtonShape)
                    .background(sendButtonContainerColor)
                    .clickable(enabled = sendEnabled, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Send recipe request",
                    tint = sendButtonContentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AiRecipeLaunchIntro(
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "AI Recipe",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = scheme.primary
            )
            Text(
                text = "Making your life easy",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurfaceVariant.copy(alpha = 0.82f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecipeScreen(
    sessionState: RecipeScreenSessionState,
    immersiveMode: Boolean = false,
    onExitImmersiveMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val appCtx = context.applicationContext
    val density = LocalDensity.current
    val gson = remember { Gson() }
    val sharedPrefs = remember(appCtx) {
        appCtx.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val isRecipeKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var keyboardDismissBackGuard by remember { mutableStateOf(false) }
    val recipePromptBottomPadding = if (immersiveMode) 8.dp else 82.dp

    val pantryFoods = remember {
        mutableStateListOf<FoodItem>().apply {
            addAll(loadFoodList(sharedPrefs, gson))
        }
    }
    val savedRecipes = remember {
        mutableStateListOf<SavedRecipe>().apply {
            addAll(loadSavedRecipes(sharedPrefs, gson))
        }
    }

    var promptText by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var pendingLatestMessageScroll by remember { mutableIntStateOf(0) }
    var aiRequestVersion by remember { mutableIntStateOf(0) }
    var showJumpToTop by remember { mutableStateOf(false) }
    var showJumpToBottom by remember { mutableStateOf(false) }
    var showNewChatDialog by rememberSaveable { mutableStateOf(false) }
    var showSavedRecipesDialog by rememberSaveable { mutableStateOf(false) }
    var showLaunchIntro by rememberSaveable {
        mutableStateOf(!hasShownRecipeLaunchIntroThisProcess)
    }
    val messagesJson = sessionState.messagesJson
    val previousIngredientsJson = sessionState.previousIngredientsJson
    val shownRecipeKeysJson = sessionState.shownRecipeKeysJson
    val conversationId = sessionState.conversationId
    val messages = remember(messagesJson) {
        loadRecipeChatMessages(gson, messagesJson)
    }
    val previousIngredients = remember(previousIngredientsJson) {
        loadRecipeIngredientContext(gson, previousIngredientsJson)
    }
    val shownRecipeKeys = remember(shownRecipeKeysJson) {
        loadRecipeIngredientContext(gson, shownRecipeKeysJson)
            .filter { it.isNotBlank() }
            .toSet()
    }
    val savedRecipeKeys by remember {
        derivedStateOf { savedRecipeIdentityKeys(savedRecipes) }
    }
    val aiEligiblePantryFoods by remember {
        derivedStateOf { pantryFoods.filterNot(::isOnboardingDemoFood) }
    }
    val pantryIngredientNames by remember {
        derivedStateOf { aiEligiblePantryFoods.map { it.name } }
    }
    val soonExpiringFoods by remember(aiEligiblePantryFoods) {
        derivedStateOf {
            aiEligiblePantryFoods
                .mapNotNull { food ->
                    val daysLeft = daysUntil(food.expiry)
                    if (daysLeft != null && daysLeft in 0..AI_EXPIRING_FOOD_WINDOW_DAYS) {
                        ExpiringFoodHint(food.name, daysLeft)
                    } else {
                        null
                    }
                }
                .sortedWith(
                    compareBy<ExpiringFoodHint>({ it.daysLeft }, { it.name.lowercase(Locale.US) })
                )
        }
    }
    val visibleMessageItemCount = messages.sumOf { message ->
        if (message.role == RecipeChatRole.ASSISTANT && message.recipes.isNotEmpty()) {
            message.recipes.size + 2
        } else {
            1
        }
    }
    val hasAiPantryFoods = aiEligiblePantryFoods.isNotEmpty()
    val totalVisibleItems =
        1 +
                (if (messages.isEmpty()) 1 else 0) +
                visibleMessageItemCount +
                (if (isLoading) 1 else 0)
    val recipeListBottomPadding by animateDpAsState(
        targetValue = if (immersiveMode) 132.dp else 176.dp,
        animationSpec = tween(
            durationMillis = if (immersiveMode) 180 else 170,
            easing = FastOutSlowInEasing
        ),
        label = "recipeListBottomPadding"
    )
    fun dismissRecipeKeyboard() {
        keyboardDismissBackGuard = true
        focusManager.clearFocus(force = true)
        keyboard?.hide()
        scope.launch {
            delay(300)
            keyboardDismissBackGuard = false
        }
    }
    OverlayBackHandler(enabled = isRecipeKeyboardVisible) {
        dismissRecipeKeyboard()
    }
    BackHandler(enabled = isRecipeKeyboardVisible) {
        dismissRecipeKeyboard()
    }
    OverlayBackHandler(enabled = immersiveMode && !isRecipeKeyboardVisible && !keyboardDismissBackGuard) {
        onExitImmersiveMode()
    }
    BackHandler(enabled = immersiveMode && !isRecipeKeyboardVisible && !keyboardDismissBackGuard) {
        onExitImmersiveMode()
    }

    DisposableEffect(sharedPrefs, gson) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    FOOD_LIST_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_ENABLED_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_DAYS_KEY ->
                        replaceListContentsIfChanged(pantryFoods, loadFoodList(sharedPrefs, gson))

                    SAVED_RECIPES_KEY ->
                        replaceListContentsIfChanged(savedRecipes, loadSavedRecipes(sharedPrefs, gson))
                }
            }

        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { savedRecipes.toList() }
            .drop(1)
            .collectLatest { recipes ->
                saveSavedRecipesAsync(sharedPrefs, gson, recipes)
            }
    }

    LaunchedEffect(pendingLatestMessageScroll) {
        if (pendingLatestMessageScroll > 0) {
            val measuredLastItemIndex = listState.layoutInfo.totalItemsCount - 1
            val lastItemIndex = if (measuredLastItemIndex >= 0) {
                measuredLastItemIndex
            } else {
                (totalVisibleItems - 1).coerceAtLeast(0)
            }
            listState.scrollToItem(lastItemIndex, Int.MAX_VALUE)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.isScrollInProgress,
                listState.canScrollBackward,
                listState.canScrollForward
            )
        }.collectLatest { (isScrolling, canJumpToTop, canJumpToBottom) ->
            if (!canJumpToTop) showJumpToTop = false
            if (!canJumpToBottom) showJumpToBottom = false

            if (isScrolling) {
                if (canJumpToTop) showJumpToTop = true
                if (canJumpToBottom) showJumpToBottom = true
            } else {
                delay(1600)
                showJumpToTop = false
                showJumpToBottom = false
            }
        }
    }

    fun persistMessages(updated: List<RecipeChatMessage>) {
        sessionState.messagesJson = gson.toJson(updated)
    }

    fun persistPreviousIngredients(updated: List<String>) {
        sessionState.previousIngredientsJson = gson.toJson(updated)
    }

    fun persistShownRecipeKeys(updated: Set<String>) {
        sessionState.shownRecipeKeysJson = gson.toJson(updated.toList())
    }

    fun removeSavedRecipe(recipe: RecipeSuggestion) {
        val key = recipeIdentityKey(recipe)
        val index = savedRecipes.indexOfFirst { recipeIdentityKey(it.recipe) == key }
        if (index != -1) {
            savedRecipes.removeAt(index)
        }
    }

    fun toggleSavedRecipe(recipe: RecipeSuggestion) {
        val cleanedRecipe = cleanSavedRecipe(recipe) ?: return
        val key = recipeIdentityKey(cleanedRecipe)
        val index = savedRecipes.indexOfFirst { recipeIdentityKey(it.recipe) == key }

        if (index != -1) {
            savedRecipes.removeAt(index)
        } else {
            savedRecipes.add(0, SavedRecipe(recipe = cleanedRecipe))
        }
    }

    fun resetRecipeAi() {
        aiRequestVersion += 1
        isLoading = false
        promptText = ""
        showJumpToTop = false
        showJumpToBottom = false
        sessionState.messagesJson = "[]"
        sessionState.previousIngredientsJson = "[]"
        sessionState.expiringPromptDismissed = false
        sessionState.conversationId = "recipe-session-${System.currentTimeMillis()}"
        keyboard?.hide()
        scope.launch {
            listState.scrollToItem(0)
        }
    }

    fun showExpiringFoodsList(rawRequest: String = "Show foods expiring soon.") {
        val trimmed = rawRequest.trim().ifBlank { "Show foods expiring soon." }
        val assistantMessage = RecipeChatMessage(
            role = RecipeChatRole.ASSISTANT,
            text = expiringFoodListAnswer(
                foods = soonExpiringFoods,
                windowDays = AI_EXPIRING_FOOD_WINDOW_DAYS
            )
        )
        persistMessages(
            messages +
                    RecipeChatMessage(
                        role = RecipeChatRole.USER,
                        text = trimmed
                    ) +
                    assistantMessage
        )
        promptText = ""
        keyboard?.hide()
        pendingLatestMessageScroll += 1
    }

    fun sendRecipePrompt(
        rawRequest: String,
        useExpiringFoods: Boolean = false
    ) {
        val trimmed = rawRequest.trim()
        if (trimmed.isBlank() || isLoading) return

        if (!useExpiringFoods && wantsExpiringFoodsList(trimmed)) {
            showExpiringFoodsList(trimmed)
            return
        }

        if (!useExpiringFoods && requestedRecipeLimitTooHigh(trimmed)) {
            val tooManyRecipeMessages = messages +
                    RecipeChatMessage(
                        role = RecipeChatRole.USER,
                        text = trimmed
                    ) +
                    RecipeChatMessage(
                        role = RecipeChatRole.ASSISTANT,
                        text = "Sorry, I can only give 1, 2, or 3 recipes at a time.",
                        isError = true
                    )
            persistMessages(tooManyRecipeMessages)
            promptText = ""
            keyboard?.hide()
            pendingLatestMessageScroll += 1
            return
        }

        val ingredientEditFollowUp =
            !useExpiringFoods && looksLikeIngredientEditFollowUp(
                request = trimmed,
                pantryIngredients = pantryIngredientNames,
                previousIngredients = previousIngredients
            )
        val wantsMoreFromPrevious =
            !useExpiringFoods &&
                    previousIngredients.isNotEmpty() &&
                    !ingredientEditFollowUp &&
                    wantsMoreRecipeIdeas(trimmed)

        if (
            !useExpiringFoods &&
            !wantsMoreFromPrevious &&
            !ingredientEditFollowUp &&
            !looksFoodRelatedRecipeRequest(trimmed, pantryIngredientNames)
        ) {
            val invalidPromptMessage =
                if (isSingleFoodLikeRequest(trimmed, pantryIngredientNames)) {
                    "Can you give me some more foods?"
                } else if (looksLikeFoodIngredientInput(trimmed, pantryIngredientNames)) {
                    "Can you give me some more food items?"
                } else {
                    "I can only help with food-related recipe requests."
                }
            val invalidMessages = messages +
                    RecipeChatMessage(
                        role = RecipeChatRole.USER,
                        text = trimmed
                    ) +
                    RecipeChatMessage(
                        role = RecipeChatRole.ASSISTANT,
                        text = invalidPromptMessage,
                        isError = true
                    )
            persistMessages(invalidMessages)
            promptText = ""
            keyboard?.hide()
            pendingLatestMessageScroll += 1
            return
        }

        val frozenExpiringFoods = soonExpiringFoods
        val frozenPantryIngredients = pantryIngredientNames
        val blockedRecipeKeys =
            shownRecipeKeys + recipeIdentityKeysFromMessages(messages) + savedRecipeIdentityKeys(savedRecipes)
        val blockedRecipeTitles = (shownRecipeTitles(messages) + savedRecipeTitles(savedRecipes))
            .distinctBy(::normalizeRecipeTitleKey)
            .take(12)
        val useFullPantryList = !useExpiringFoods && wantsRecipesFromPantryList(trimmed)
        val requestedLimit = requestedRecipeLimit(trimmed) ?: 3
        val requestedRecipeWord = if (requestedLimit == 1) "recipe" else "recipes"

        if (useFullPantryList && frozenPantryIngredients.isEmpty()) {
            val emptyPantryMessages = messages +
                    RecipeChatMessage(
                        role = RecipeChatRole.USER,
                        text = trimmed
                    ) +
                    RecipeChatMessage(
                        role = RecipeChatRole.ASSISTANT,
                        text = "Add foods in Home first, then ask me to make recipes from your list.",
                        isError = true
                    )
            persistMessages(emptyPantryMessages)
            promptText = ""
            keyboard?.hide()
            pendingLatestMessageScroll += 1
            return
        }

        val baseRequestText = if (useExpiringFoods) {
            buildExpiringFoodsRequest(frozenExpiringFoods)
        } else if (useFullPantryList) {
            "Please suggest $requestedLimit easy and quick $requestedRecipeWord using foods from my pantry list."
        } else if (wantsMoreFromPrevious) {
            "Please suggest $requestedLimit more easy and quick $requestedRecipeWord using the same food products as before."
        } else {
            trimmed
        }
        val requestText =
            if (!useExpiringFoods && blockedRecipeTitles.isNotEmpty()) {
                "$baseRequestText Do not suggest these already shown or saved recipe names again: ${
                    blockedRecipeTitles.joinToString(", ")
                }."
            } else {
                baseRequestText
            }

        val userMessage = RecipeChatMessage(
            role = RecipeChatRole.USER,
            text = if (useExpiringFoods) {
                "Use foods in my list that are expiring soon."
            } else {
                trimmed
            }
        )
        val baselineMessages = messages + userMessage

        persistMessages(baselineMessages)
        promptText = ""
        keyboard?.hide()

        if (useExpiringFoods) {
            sessionState.expiringPromptDismissed = true
        }

        isLoading = true
        aiRequestVersion += 1
        val requestVersion = aiRequestVersion
        pendingLatestMessageScroll += 1

        scope.launch {
            val result = runCatching {
                if (useExpiringFoods) {
                    val recipes = RecipeAiService.findRecipesByIngredients(
                        ingredients = frozenExpiringFoods.map { it.name },
                        limit = requestedLimit
                    )
                    RecipeSuggestionBatch(
                        resolvedIngredients = frozenExpiringFoods.map { it.name },
                        recipes = recipes
                    )
                } else {
                    RecipeAiService.generateRecipeSuggestions(
                        request = requestText,
                        pantryIngredients = frozenPantryIngredients,
                        previousIngredients = previousIngredients,
                        contextId = conversationId,
                        limit = requestedLimit
                    )
                }
            }

            val assistantMessage = result.fold(
                onSuccess = { batch ->
                    val freshBatch = filterRepeatedRecipes(
                        batch = batch,
                        blockedRecipeKeys = blockedRecipeKeys,
                        limit = requestedLimit
                    )

                    if (freshBatch.recipes.isNotEmpty() && freshBatch.resolvedIngredients.isNotEmpty()) {
                        persistPreviousIngredients(freshBatch.resolvedIngredients)
                    }
                    if (freshBatch.recipes.isNotEmpty()) {
                        persistShownRecipeKeys(
                            shownRecipeKeys + freshBatch.recipes.map(::recipeIdentityKey)
                        )
                    }

                    if (batch.recipes.isNotEmpty() && freshBatch.recipes.isEmpty()) {
                        RecipeChatMessage(
                            role = RecipeChatRole.ASSISTANT,
                            text = "I only found recipes that were already shown or saved, so I skipped them. Try a different chip or different foods for fresh ideas."
                        )
                    } else {
                        buildRecipeAssistantMessage(freshBatch)
                    }
                },
                onFailure = { error ->
                    RecipeChatMessage(
                        role = RecipeChatRole.ASSISTANT,
                        text = error.message
                            ?: "I couldn't reach the recipe assistant right now. Please try again.",
                        isError = true
                    )
                }
            )

            if (requestVersion != aiRequestVersion) return@launch

            persistMessages(baselineMessages + assistantMessage)
            isLoading = false
        }
    }

    LaunchedEffect(showLaunchIntro) {
        if (showLaunchIntro) {
            hasShownRecipeLaunchIntroThisProcess = true
            delay(760)
            showLaunchIntro = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !showLaunchIntro,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 220,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = ExitTransition.None
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    overscrollEffect = null,
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = recipeListBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (messages.isNotEmpty() || isLoading || savedRecipes.isNotEmpty()) {
                        item(
                            key = "recipe-new-chat-header",
                            contentType = "recipe-header"
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RecipeSavedRecipesButton(
                                    savedCount = savedRecipes.size,
                                    onClick = { showSavedRecipesDialog = true }
                                )

                                if (messages.isNotEmpty() || isLoading) {
                                    Spacer(Modifier.width(8.dp))
                                    RecipeNewChatButton(onClick = { showNewChatDialog = true })
                                }
                            }
                        }
                    }

                    item(
                        key = "ai-quick-suggestion-chips",
                        contentType = "ai-quick-suggestion-chips"
                    ) {
                        RecipeQuickSuggestionChips(
                            hasPantryFoods = hasAiPantryFoods,
                            hasExpiringFoods = soonExpiringFoods.isNotEmpty(),
                            isLoading = isLoading,
                            onShowExpiringFoods = {
                                showExpiringFoodsList()
                            },
                            onUsePantryFoods = {
                                sendRecipePrompt(
                                    "Suggest recipes using foods in my list."
                                )
                            },
                            onQuickBreakfast = {
                                sendRecipePrompt(
                                    if (hasAiPantryFoods) {
                                        "Suggest 3 quick breakfast recipes using foods in my list."
                                    } else {
                                        "Suggest 3 quick breakfast recipes."
                                    }
                                )
                            },
                            onOnlyOneRecipe = {
                                sendRecipePrompt(
                                    if (hasAiPantryFoods) {
                                        "Suggest only 1 easy recipe using foods in my list."
                                    } else {
                                        "Suggest only 1 easy recipe."
                                    }
                                )
                            }
                        )
                    }

                    if (messages.isEmpty()) {
                        item(
                            key = "ai-intro-card",
                            contentType = "ai-intro-card"
                        ) {
                            RecipeIntroCard(hasPantryFoods = pantryFoods.isNotEmpty())
                        }
                    }

                    messages.forEachIndexed { messageIndex, message ->
                        when (message.role) {
                            RecipeChatRole.USER -> {
                                item(
                                    key = "recipe-message-$messageIndex-${message.role}-${message.text.hashCode()}",
                                    contentType = "recipe-user-message"
                                ) {
                                    RecipeUserMessageCard(message.text)
                                }
                            }

                            RecipeChatRole.ASSISTANT -> {
                                if (message.recipes.isEmpty()) {
                                    item(
                                        key = "recipe-message-$messageIndex-${message.role}-${message.text.hashCode()}",
                                        contentType = "recipe-assistant-text"
                                    ) {
                                        RecipeAssistantMessageCard(message)
                                    }
                                } else {
                                    item(
                                        key = "recipe-message-$messageIndex-${message.role}-${message.text.hashCode()}",
                                        contentType = "recipe-assistant-text"
                                    ) {
                                        RecipeAssistantTextCard(message)
                                    }

                                    itemsIndexed(
                                        items = message.recipes,
                                        key = { recipeIndex, recipe ->
                                            "recipe-card-$messageIndex-$recipeIndex-${recipe.title.hashCode()}"
                                        },
                                        contentType = { _, _ -> "recipe-card" }
                                    ) { _, recipe ->
                                        RecipeAssistantRecipeCard(
                                            recipe = recipe,
                                            isSaved = savedRecipeKeys.contains(recipeIdentityKey(recipe)),
                                            onToggleSaved = { toggleSavedRecipe(recipe) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item(
                            key = "recipe-loading",
                            contentType = "recipe-loading"
                        ) {
                            RecipeLoadingCard()
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(bottom = recipePromptBottomPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RecipeJumpControls(
                        showJumpToTop = showJumpToTop,
                        showJumpToBottom = showJumpToBottom,
                        onJumpToTop = {
                            showJumpToTop = false
                            scope.launch {
                                listState.scrollToItem(0)
                            }
                        },
                        onJumpToBottom = {
                            showJumpToBottom = false
                            scope.launch {
                                val measuredLastItemIndex = listState.layoutInfo.totalItemsCount - 1
                                val lastItemIndex = if (measuredLastItemIndex >= 0) {
                                    measuredLastItemIndex
                                } else {
                                    (totalVisibleItems - 1).coerceAtLeast(0)
                                }
                                listState.scrollToItem(lastItemIndex, Int.MAX_VALUE)
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RecipePromptBar(
                            value = promptText,
                            placeholder = recipePromptPlaceholder(),
                            isLoading = isLoading,
                            onValueChange = { promptText = it },
                            onSend = { sendRecipePrompt(promptText) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showLaunchIntro,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 180,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 180,
                    easing = FastOutLinearInEasing
                )
            )
        ) {
            AiRecipeLaunchIntro()
        }
    }

    if (showNewChatDialog) {
        GlassAlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { DialogTitleText("New chat?") },
            text = {
                DialogBodyText(
                    "If you create a new chat, all the current recipes in this chat will be gone. Are you sure you want to start a new chat?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNewChatDialog = false
                        resetRecipeAi()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSavedRecipesDialog) {
        SavedRecipesDialog(
            savedRecipes = savedRecipes,
            onDismiss = { showSavedRecipesDialog = false },
            onRemove = { recipe ->
                removeSavedRecipe(recipe)
            }
        )
    }
}

@Composable
fun HomeScreen(
    showForm: Boolean,
    onShowFormChange: (Boolean) -> Unit,
    onSelectionModeChange: (Boolean) -> Unit,
    onOverlayVisibilityChange: (Boolean) -> Unit,
    onOpenAi: () -> Unit,
    tutorialActive: Boolean = false,
    onTutorialTargetPositioned: (OnboardingSpotlightTarget, Rect) -> Unit = { _, _ -> }
) {
    OverrideStatusBarColor(
        color = MaterialTheme.colorScheme.surface,
        isDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    )

    FoodEntryScreen(
        showForm = showForm,
        onShowFormChange = onShowFormChange,
        onSelectionModeChange = onSelectionModeChange,
        onOverlayVisibilityChange = onOverlayVisibilityChange,
        onOpenAi = onOpenAi,
        tutorialActive = tutorialActive,
        onTutorialTargetPositioned = onTutorialTargetPositioned
    )
}

@Composable
fun CategoryScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appCtx = context.applicationContext
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val accountSession = rememberAccountSessionPreference()
    val statsPrefs = remember(appCtx) {
        appCtx.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    }
    val statsGson = remember { Gson() }
    val profileFoods = remember {
        mutableStateListOf<FoodItem>().apply {
            addAll(loadFoodList(statsPrefs, statsGson))
        }
    }
    val profileStats by remember {
        derivedStateOf { buildProfileStats(profileFoods) }
    }
    var localProfileName by rememberSaveable { mutableStateOf(loadLocalProfileName(context)) }
    var localProfilePhotoUri by rememberSaveable { mutableStateOf(loadLocalProfilePhotoUri(context)) }
    var showEditLocalNameDialog by rememberSaveable { mutableStateOf(false) }
    var draftLocalProfileName by rememberSaveable { mutableStateOf(localProfileName) }
    val isGoogleProfile = accountSession?.provider == AccountProvider.GOOGLE
    val canUseLocalProfilePhoto = accountSession?.provider != AccountProvider.GOOGLE
    val localPhotoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult

            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            val savedUri = uri.toString()
            localProfilePhotoUri = savedUri
            saveLocalProfilePhotoUri(context, savedUri)
        }
    val profileName =
        when {
            accountSession != null -> accountSession.displayName
                ?.takeIf { it.isNotBlank() }
                ?: formatProfileNameFromEmail(accountSession.email)
            localProfileName.isNotBlank() -> localProfileName
            else -> "My Profile"
        }
    @Suppress("UNNECESSARY_SAFE_CALL") val profileSubtitle =
        when {
            isGoogleProfile -> accountSession?.email ?: ""
            accountSession != null -> "Signed in with ${accountSession.email}"
            localProfileName.isNotBlank() -> "This name stays on this device."
            else -> "Add a name to personalize your profile on this device."
        }
    val profileTopContentPadding = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    } + 8.dp
    val profileBottomSafePadding = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp() + 84.dp
    }

    OverrideStatusBarColor(
        color = Color.Transparent,
        isDarkIcons = colorScheme.background.luminance() > 0.5f
    )
    OverrideNavigationBarColor(
        color = Color.Transparent,
        isDarkIcons = colorScheme.background.luminance() > 0.5f
    )

    DisposableEffect(statsPrefs, statsGson) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    FOOD_LIST_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_ENABLED_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_DAYS_KEY ->
                        replaceListContentsIfChanged(profileFoods, loadFoodList(statsPrefs, statsGson))
                }
            }

        statsPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { statsPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        overscrollEffect = null,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = profileTopContentPadding,
            end = 16.dp,
            bottom = profileBottomSafePadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            @Suppress("UNNECESSARY_SAFE_CALL")
            ProfileHeaderCard(
                name = profileName,
                subtitle = profileSubtitle,
                photoUrl = if (isGoogleProfile) accountSession?.photoUrl else null,
                localPhotoUri = if (canUseLocalProfilePhoto) localProfilePhotoUri else null,
                showEditName = accountSession == null,
                canEditPhoto = canUseLocalProfilePhoto,
                onEditPhoto = {
                    localPhotoPickerLauncher.launch(arrayOf("image/*"))
                },
                onEditName = {
                    draftLocalProfileName = localProfileName
                    showEditLocalNameDialog = true
                }
            )
        }

        item { ProfileSectionTitle("Stats") }
        item { ProfileStatsCard(profileStats) }

        item { ProfileSectionTitle("Manage") }
        item {
            ProfileActionRow(
                title = "Account",
                subtitle = "Sign in, sync, and move your pantry between devices",
                icon = Icons.Filled.Person
            ) {
                navController.navigate(Route.Account.r)
            }
        }
        item {
            ProfileActionRow(
                title = "Settings",
                subtitle = "Theme, countdown format, and notifications",
                icon = Icons.Filled.Settings
            ) {
                navController.navigate(Route.Settings.r)
            }
        }

        item { ProfileSectionTitle("Transfer") }
        item { PantryTransferCard() }

        item { ProfileSectionTitle("More") }
        item {
            ProfileActionRow(
                title = "Help & support",
                subtitle = "Tips for pantry, history, and AI recipes",
                icon = Icons.Filled.Help
            ) {
                navController.navigate(Route.Help.r)
            }
        }
        item {
            ProfileActionRow(
                title = "Privacy",
                subtitle = "How your pantry and AI data are handled",
                icon = Icons.Filled.Lock
            ) {
                navController.navigate(Route.Privacy.r)
            }
        }
        item {
            ProfileActionRow(
                title = "About",
                subtitle = "App details and version ${BuildConfig.VERSION_NAME}",
                icon = Icons.Filled.Info
            ) {
                navController.navigate(Route.About.r)
            }
        }
    }

    if (showEditLocalNameDialog) {
        val trimmedDraftName = draftLocalProfileName.trim().take(LOCAL_PROFILE_NAME_MAX_LENGTH)
        GlassAlertDialog(
            onDismissRequest = { showEditLocalNameDialog = false },
            title = { DialogTitleText(if (localProfileName.isBlank()) "Add name" else "Edit name") },
            text = {
                Column {
                    DialogBodyText("Choose the name you want to show on your profile.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = draftLocalProfileName,
                        onValueChange = {
                            draftLocalProfileName = it.take(LOCAL_PROFILE_NAME_MAX_LENGTH)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        localProfileName = trimmedDraftName
                        saveLocalProfileName(context, trimmedDraftName)
                        showEditLocalNameDialog = false
                    },
                    enabled = trimmedDraftName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLocalNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    name: String,
    subtitle: String,
    photoUrl: String?,
    localPhotoUri: String?,
    showEditName: Boolean,
    canEditPhoto: Boolean,
    onEditPhoto: () -> Unit,
    onEditName: () -> Unit
) {
    MatchingPillCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileHeaderAvatar(
                name = name,
                photoUrl = photoUrl,
                localPhotoUri = localPhotoUri,
                editable = canEditPhoto,
                onEditPhoto = onEditPhoto,
                modifier = Modifier.size(68.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .let { baseModifier ->
                                if (showEditName) {
                                    baseModifier.clickable(onClick = onEditName)
                                } else {
                                    baseModifier
                                }
                            }
                    )

                    if (showEditName) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(30.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderAvatar(
    name: String,
    photoUrl: String?,
    localPhotoUri: String?,
    editable: Boolean,
    onEditPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var profileBitmap by remember(photoUrl, localPhotoUri) { mutableStateOf<Bitmap?>(null) }
    val initials = profileInitials(name)
    val avatarShape = RoundedCornerShape(50.dp)

    LaunchedEffect(photoUrl, localPhotoUri) {
        profileBitmap =
            when {
                !localPhotoUri.isNullOrBlank() -> {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver
                                .openInputStream(localPhotoUri.toUri())
                                ?.use(BitmapFactory::decodeStream)
                        }.getOrNull()
                    }
                }

                !photoUrl.isNullOrBlank() -> {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val connection = (URL(photoUrl).openConnection() as HttpURLConnection).apply {
                                connectTimeout = 4_000
                                readTimeout = 4_000
                                doInput = true
                            }
                            connection.connect()
                            connection.inputStream.use(BitmapFactory::decodeStream)
                        }.getOrNull()
                    }
                }

                else -> null
            }
    }

    Box(
        modifier = modifier
            .let { baseModifier ->
                if (editable) {
                    baseModifier.clickable(onClick = onEditPhoto)
                } else {
                    baseModifier
                }
            }
            .clip(avatarShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = profileBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (initials.isNotBlank()) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }

        if (editable) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

private fun formatProfileNameFromEmail(email: String): String {
    val localPart = email.substringBefore("@").trim()
    if (localPart.isBlank()) return "My Profile"

    return localPart
        .split(Regex("[._-]+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
            }
        }
        .ifBlank { "My Profile" }
}

private fun profileInitials(name: String): String {
    val parts = name
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts.first().take(1).uppercase(Locale.US)
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase(Locale.US)
    }
}

@Composable
private fun ProfileStatsCard(stats: ProfileStats) {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pantry progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatTile(
                    label = "Foods saved",
                    value = stats.foodsSaved.toString(),
                    icon = Icons.Filled.Checklist,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatTile(
                    label = "Foods expired",
                    value = stats.foodsExpired.toString(),
                    icon = Icons.Filled.DateRange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatTile(
                    label = "Most used category",
                    value = stats.mostUsedCategory,
                    icon = Icons.Filled.Category,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatTile(
                    label = "Waste avoided",
                    value = stats.wasteAvoided.toString(),
                    icon = Icons.Filled.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .heightIn(min = 92.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(scheme.primary.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = scheme.primary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(18.dp)
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
    )
}

@Composable
private fun ProfileActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileInfoScreen(
    navController: NavHostController,
    title: String,
    headline: String,
    body: String,
    footer: String? = null
) {
    Scaffold(
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MatchingPillCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    footer?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountScreen(navController: NavHostController) {
    AccountSyncScreen(navController)
}

@Composable
fun AboutScreen(navController: NavHostController) {
    ProfileInfoScreen(
        navController = navController,
        title = "About",
        headline = "Food Expiry Tracker",
        body = "Use this app to:\n• track expiry dates\n• organize your pantry\n• review item history\n• get quick recipe ideas",
        footer = "Version ${BuildConfig.VERSION_NAME}"
    )
}

@Composable
fun HelpScreen(navController: NavHostController) {
    ProfileInfoScreen(
        navController = navController,
        title = "Help & Support",
        headline = "Quick help",
        body = "Home: add foods with expiry dates, categories, barcodes, and quantities. Use the header to search, sort, and select items.\n\nFood cards: swipe right to edit, swipe left to delete. Matching duplicate batches merge together.\n\nFood AI: tap the sparkle button for recipe chips, saved recipes, and recipe ideas from your pantry.\n\nHistory: grouped old items are easy to search and add back.\n\nProfile: manage account sync, settings, pantry transfer, help, privacy, and app info.",
        footer = "If something looks off, try closing and reopening the app."
    )
}

@Composable
fun PrivacyScreen(navController: NavHostController) {
    ProfileInfoScreen(
        navController = navController,
        title = "Privacy",
        headline = "Your data",
        body = "Your pantry data stays on this device unless you sign in and sync.\nAI only sends the recipe prompt you choose to submit.\nNotifications use the expiry dates saved on your device.",
        footer = "You can use the app without creating an account."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val countdownFormat = rememberCountdownFormatPreference()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SlimTopBar(
                title = "Settings",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingRow(
                title = "Theme"
            ) {
                navController.navigate(Route.Theme.r)
            }
            SettingRow(
                title = "Countdown format",
                subtitle = countdownFormatLabel(countdownFormat)
            ) {
                navController.navigate(Route.CountdownFormat.r)
            }
            SettingRow(
                title = "Notifications"
            ) {
                navController.navigate(Route.Notifications.r)
            }
            SettingRow(
                title = "Auto delete",
                subtitle = "History and expired foods"
            ) {
                navController.navigate(Route.AutoDelete.r)
            }
        }
    }
}

@Composable
fun AutoDeleteScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appCtx = context.applicationContext
    val prefs = remember(appCtx) {
        appCtx.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE)
    }
    val gson = remember { Gson() }

    var historyEnabled by rememberSaveable {
        mutableStateOf(loadHistoryAutoDeleteEnabled(prefs))
    }
    var historyDays by rememberSaveable {
        mutableLongStateOf(loadHistoryRetentionDays(prefs))
    }
    var expiredFoodsEnabled by rememberSaveable {
        mutableStateOf(loadExpiredFoodAutoRemoveEnabled(prefs))
    }
    var expiredFoodDays by rememberSaveable {
        mutableLongStateOf(loadExpiredFoodAutoRemoveDays(prefs))
    }
    var customTarget by remember {
        mutableStateOf<AutoDeleteCustomTarget?>(null)
    }

    fun applyHistoryCleanup() {
        if (historyEnabled) {
            loadAndSyncHistoryEntries(prefs, gson)
        }
    }

    fun applyExpiredFoodCleanup() {
        if (expiredFoodsEnabled) {
            loadFoodList(prefs, gson)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SlimTopBar(
                title = "Auto delete",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AutoDeleteSectionCard(
                title = "History auto-delete",
                description = if (historyEnabled) {
                    "Remove history entries not used for ${autoDeleteDurationLabel(historyDays)}."
                } else {
                    "Keep history until you remove it."
                },
                enabled = historyEnabled,
                selectedDays = historyDays,
                presets = historyAutoDeletePresets,
                selectedSummary = "Older than ${autoDeleteDurationLabel(historyDays)}",
                onEnabledChange = { checked ->
                    historyEnabled = checked
                    saveHistoryAutoDeleteEnabled(prefs, checked)
                    applyHistoryCleanup()
                },
                onPresetSelected = { days ->
                    historyDays = days
                    saveHistoryRetentionDays(prefs, days)
                    applyHistoryCleanup()
                },
                onCustomClick = {
                    customTarget = AutoDeleteCustomTarget.HISTORY
                }
            )

            AutoDeleteSectionCard(
                title = "Auto-remove expired foods",
                description = if (expiredFoodsEnabled) {
                    "Remove from List after expired for ${autoDeleteDurationLabel(expiredFoodDays)}."
                } else {
                    "Expired foods stay until you remove them."
                },
                enabled = expiredFoodsEnabled,
                selectedDays = expiredFoodDays,
                presets = expiredFoodAutoRemovePresets,
                selectedSummary = "Expired for ${autoDeleteDurationLabel(expiredFoodDays)}",
                onEnabledChange = { checked ->
                    expiredFoodsEnabled = checked
                    saveExpiredFoodAutoRemoveEnabled(prefs, checked)
                    applyExpiredFoodCleanup()
                },
                onPresetSelected = { days ->
                    expiredFoodDays = days
                    saveExpiredFoodAutoRemoveDays(prefs, days)
                    applyExpiredFoodCleanup()
                },
                onCustomClick = {
                    customTarget = AutoDeleteCustomTarget.EXPIRED_FOODS
                }
            )
        }
    }

    customTarget?.let { target ->
        val isHistoryTarget = target == AutoDeleteCustomTarget.HISTORY
        AutoDeleteCustomDialog(
            title = if (isHistoryTarget) "Custom history cleanup" else "Custom expired foods cleanup",
            initialDays = if (isHistoryTarget) historyDays else expiredFoodDays,
            minDays = if (isHistoryTarget) MIN_HISTORY_RETENTION_DAYS else MIN_EXPIRED_FOOD_AUTO_REMOVE_DAYS,
            maxDays = if (isHistoryTarget) MAX_HISTORY_RETENTION_DAYS else MAX_EXPIRED_FOOD_AUTO_REMOVE_DAYS,
            onDismiss = { customTarget = null },
            onConfirm = { days ->
                if (isHistoryTarget) {
                    historyDays = days
                    saveHistoryRetentionDays(prefs, days)
                    applyHistoryCleanup()
                } else {
                    expiredFoodDays = days
                    saveExpiredFoodAutoRemoveDays(prefs, days)
                    applyExpiredFoodCleanup()
                }
                customTarget = null
            }
        )
    }
}

private enum class AutoDeleteCustomTarget {
    HISTORY,
    EXPIRED_FOODS
}

@Composable
private fun AutoDeleteSectionCard(
    title: String,
    description: String,
    enabled: Boolean,
    selectedDays: Long,
    presets: List<AutoDeletePreset>,
    selectedSummary: String,
    onEnabledChange: (Boolean) -> Unit,
    onPresetSelected: (Long) -> Unit,
    onCustomClick: () -> Unit
) {
    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter = smoothVerticalRevealEnter(),
                exit = smoothVerticalRevealExit()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = selectedSummary,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    AutoDeletePresetPicker(
                        selectedDays = selectedDays,
                        presets = presets,
                        onSelected = onPresetSelected,
                        onCustomClick = onCustomClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoDeletePresetPicker(
    selectedDays: Long,
    presets: List<AutoDeletePreset>,
    onSelected: (Long) -> Unit,
    onCustomClick: () -> Unit
) {
    val customSelected = presets.none { it.days == selectedDays }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 2.dp)
    ) {
        items(presets, key = { it.label }) { option ->
            AutoDeleteChoiceChip(
                label = option.label,
                selected = option.days == selectedDays,
                onClick = { onSelected(option.days) }
            )
        }
        item {
            AutoDeleteChoiceChip(
                label = "Custom",
                selected = customSelected,
                onClick = onCustomClick
            )
        }
    }
}

@Composable
private fun AutoDeleteChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(50.dp)

    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(shape)
            .background(
                if (selected) {
                    scheme.primary.copy(alpha = 0.18f)
                } else {
                    scheme.surfaceVariant.copy(alpha = 0.18f)
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    scheme.primary.copy(alpha = 0.48f)
                } else {
                    scheme.outlineVariant.copy(alpha = 0.42f)
                },
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

private fun preferredAutoDeleteUnit(days: Long): AutoDeleteUnit {
    return when {
        days % AutoDeleteUnit.MONTHS.daysMultiplier == 0L -> AutoDeleteUnit.MONTHS
        days % AutoDeleteUnit.WEEKS.daysMultiplier == 0L -> AutoDeleteUnit.WEEKS
        else -> AutoDeleteUnit.DAYS
    }
}

private fun minimumAmountForUnit(minDays: Long, unit: AutoDeleteUnit): Int {
    return ((minDays + unit.daysMultiplier - 1L) / unit.daysMultiplier).toInt().coerceAtLeast(1)
}

private fun maximumAmountForUnit(maxDays: Long, unit: AutoDeleteUnit): Int {
    return (maxDays / unit.daysMultiplier).toInt().coerceAtLeast(1)
}

private fun amountForDays(
    days: Long,
    unit: AutoDeleteUnit,
    minDays: Long,
    maxDays: Long
): Int {
    val unitMin = minimumAmountForUnit(minDays, unit)
    val unitMax = maximumAmountForUnit(maxDays, unit)
    val roundedUp = ((days + unit.daysMultiplier - 1L) / unit.daysMultiplier).toInt()
    return roundedUp.coerceIn(unitMin, unitMax)
}

@Composable
private fun AutoDeleteCustomDialog(
    title: String,
    initialDays: Long,
    minDays: Long,
    maxDays: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var unit by rememberSaveable { mutableStateOf(preferredAutoDeleteUnit(initialDays)) }
    var amount by rememberSaveable {
        mutableIntStateOf(amountForDays(initialDays, unit, minDays, maxDays))
    }
    var amountText by rememberSaveable { mutableStateOf(amount.toString()) }

    val minAmount = minimumAmountForUnit(minDays, unit)
    val maxAmount = maximumAmountForUnit(maxDays, unit)
    val parsedAmount = amountText.toIntOrNull()
    val effectiveAmount = parsedAmount?.coerceIn(minAmount, maxAmount)
    val selectedDays = effectiveAmount
        ?.let { (it.toLong() * unit.daysMultiplier).coerceIn(minDays, maxDays) }

    fun updateAmount(nextAmount: Int) {
        val safeAmount = nextAmount.coerceIn(minAmount, maxAmount)
        amount = safeAmount
        amountText = safeAmount.toString()
    }

    fun updateAmountText(raw: String) {
        val digitsOnly = raw.filter { it.isDigit() }.take(3)
        val typedAmount = digitsOnly.toIntOrNull()

        if (typedAmount == null) {
            amountText = digitsOnly
            return
        }

        val safeAmount = typedAmount.coerceIn(minAmount, maxAmount)
        amount = safeAmount
        amountText = safeAmount.toString()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MatchingPillCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 420.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Choose when cleanup should happen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        enabled = (effectiveAmount ?: amount) > minAmount,
                        onClick = { updateAmount((effectiveAmount ?: amount) - 1) }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .widthIn(min = 76.dp, max = 112.dp)
                                .height(52.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = amountText,
                                onValueChange = ::updateAmountText,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            text = unit.label.lowercase(Locale.US),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        enabled = (effectiveAmount ?: amount) < maxAmount,
                        onClick = { updateAmount((effectiveAmount ?: amount) + 1) }
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AutoDeleteUnit.entries.forEach { option ->
                        AutoDeleteChoiceChip(
                            label = option.label,
                            selected = unit == option,
                            onClick = {
                                val currentDays = selectedDays ?: initialDays
                                val nextAmount = amountForDays(currentDays, option, minDays, maxDays)
                                unit = option
                                amount = nextAmount
                                amountText = nextAmount.toString()
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = selectedDays?.let {
                        "Selected: ${autoDeleteDurationLabel(it)}"
                    } ?: "Type a number from $minAmount to $maxAmount.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = selectedDays != null,
                        onClick = { selectedDays?.let(onConfirm) },
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    MatchingPillCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    navController: NavHostController,
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SlimTopBar(
                title = "Theme",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ThemeOption("System", "Follow your phone setting",
                selected = currentMode == ThemeMode.SYSTEM
            ) { onModeChange(ThemeMode.SYSTEM) }

            ThemeOption("Light", "Always light mode",
                selected = currentMode == ThemeMode.LIGHT
            ) { onModeChange(ThemeMode.LIGHT) }

            ThemeOption("Dark", "Always dark mode",
                selected = currentMode == ThemeMode.DARK
            ) { onModeChange(ThemeMode.DARK) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownFormatScreen(navController: NavHostController) {
    val context = LocalContext.current
    val currentFormat = rememberCountdownFormatPreference()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SlimTopBar(
                title = "Countdown Format",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            CountdownFormat.entries.forEach { option ->
                ThemeOption(
                    title = countdownFormatLabel(option),
                    subtitle = countdownFormatDescription(option),
                    selected = currentFormat == option
                ) {
                    saveCountdownFormat(context, option)
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    MatchingPillCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appCtx = context.applicationContext

    val prefs = remember {
        appCtx.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
    }

    var daysBefore by rememberSaveable {
        mutableIntStateOf(
            prefs.getInt(DAYS_BEFORE_KEY, 3)
                .coerceIn(MIN_DAYS_BEFORE_REMINDER, MAX_DAYS_BEFORE_REMINDER)
        )
    }
    var dailyEnabled by rememberSaveable {
        mutableStateOf(prefs.getBoolean(DAILY_ENABLED_KEY, false))
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                dailyEnabled = true
                prefs.edit { putBoolean(DAILY_ENABLED_KEY, true) }
                scheduleDailyExpiryWork(appCtx)
            } else {
                dailyEnabled = false
                prefs.edit { putBoolean(DAILY_ENABLED_KEY, false) }
            }
        }

    @SuppressLint("ObsoleteSdkInt")
    fun hasNotifPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun saveDays(newValue: Int) {
        daysBefore = newValue.coerceIn(MIN_DAYS_BEFORE_REMINDER, MAX_DAYS_BEFORE_REMINDER)
        prefs.edit { putInt(DAYS_BEFORE_KEY, daysBefore) }

        if (dailyEnabled) scheduleDailyExpiryWork(appCtx)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SlimTopBar(
                title = "Notifications",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Notify me before food expires",
                style = MaterialTheme.typography.titleMedium
            )

            MatchingPillCard(
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily reminder", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Sends notification daily for foods expiring within ${reminderDaysLabel(daysBefore)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = dailyEnabled,
                        onCheckedChange = { checked ->
                            if (!checked) {
                                dailyEnabled = false
                                prefs.edit { putBoolean(DAILY_ENABLED_KEY, false) }
                                cancelDailyExpiryWork(appCtx)
                            } else {

                                if (hasNotifPermission()) {
                                    dailyEnabled = true
                                    prefs.edit { putBoolean(DAILY_ENABLED_KEY, true) }
                                    scheduleDailyExpiryWork(appCtx)
                                } else {

                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        }
                    )
                }
            }

            MatchingPillCard(
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Days before", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Choose how early to start reminders, from $MIN_DAYS_BEFORE_REMINDER to $MAX_DAYS_BEFORE_REMINDER days.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { saveDays(daysBefore - 1) },
                            enabled = daysBefore > MIN_DAYS_BEFORE_REMINDER
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minus")
                        }
                        Text("$daysBefore", style = MaterialTheme.typography.titleLarge)
                        IconButton(
                            onClick = { saveDays(daysBefore + 1) },
                            enabled = daysBefore < MAX_DAYS_BEFORE_REMINDER
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Plus")
                        }
                    }
                }
            }

            Button(
                onClick = { runExpiryWorkNow(appCtx) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send test notification now")
            }
        }
    }
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    currentRoute: String?,
    onTabPressed: (String) -> Unit = {},
    tutorialActive: Boolean = false,
    onTutorialTargetPositioned: (OnboardingSpotlightTarget, Rect) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    var isTabTransitionLocked by remember { mutableStateOf(false) }
    val selectedRoute = bottomBarSelectedRoute(currentRoute)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        EditCategoriesBackgroundSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(50.dp),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                bottomBarItems.forEach { item ->
                    val tutorialTarget = when (item.route) {
                        Route.History.r -> OnboardingSpotlightTarget.HISTORY_TAB
                        Route.Profile.r -> OnboardingSpotlightTarget.PROFILE_TAB
                        else -> null
                    }
                    val tutorialModifier =
                        if (tutorialActive && tutorialTarget != null) {
                            Modifier.onGloballyPositioned { coordinates ->
                                onTutorialTargetPositioned(
                                    tutorialTarget,
                                    coordinates.boundsInRoot()
                                )
                            }
                        } else {
                            Modifier
                        }

                    PillNavItem(
                        modifier = Modifier
                            .weight(1f)
                            .then(tutorialModifier),
                        selected = selectedRoute == item.route,
                        label = item.label,
                        selectedIcon = item.selectedIcon,
                        unselectedIcon = item.unselectedIcon,
                        onClick = {
                            if (isTabTransitionLocked) return@PillNavItem

                            onTabPressed(item.route)

                            if (selectedRoute == item.route) return@PillNavItem

                            isTabTransitionLocked = true
                            navController.safeNavigate(item.route)

                            scope.launch {
                                delay(BOTTOM_TAB_TRANSITION_GUARD_MS)
                                isTabTransitionLocked = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PillNavItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val palette = rememberGlassPalette()
    val selectedBackground by animateColorAsState(
        targetValue = if (selected) palette.accent else Color.Transparent,
        animationSpec = tween(220),
        label = "navSelectedBackground"
    )

    val contentTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "navContentTint"
    )

    val itemScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.97f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navItemScale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = itemScale
                scaleY = itemScale
            }
            .clip(RoundedCornerShape(50.dp))
            .background(selectedBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = contentTint,
                modifier = Modifier.size(23.dp)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 10.5.sp,
                lineHeight = 11.sp,
                maxLines = 1,
                color = contentTint,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


private fun NavHostController.safeNavigate(route: String) {
    if (currentDestination?.route == route) return

    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) { saveState = true }
    }
}

@SuppressLint("Range")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodEntryScreen(
    showForm: Boolean,
    onShowFormChange: (Boolean) -> Unit,
    onSelectionModeChange: (Boolean) -> Unit,
    onOverlayVisibilityChange: (Boolean) -> Unit,
    onOpenAi: () -> Unit,
    tutorialActive: Boolean = false,
    onTutorialTargetPositioned: (OnboardingSpotlightTarget, Rect) -> Unit = { _, _ -> }
) {
    var foodName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf(MIN_FOOD_QUANTITY.toString()) }

    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var tempCustomCategory by remember { mutableStateOf("") }
    var customCategoryExistsError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val countdownFormat = rememberCountdownFormatPreference()
    val sharedPrefs = remember { context.getSharedPreferences(FOOD_PREFS, Context.MODE_PRIVATE) }
    val gson = remember { Gson() }
    val homeSearchFocus = remember { FocusRequester() }

    val initialCategories = emptyList<String>()

    val categories = remember {
        mutableStateListOf<String>().apply {
            addAll(loadStringList(sharedPrefs, gson, CATEGORIES_LIST_KEY).ifEmpty { initialCategories })
        }
    }


    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var isCustomCategory by rememberSaveable { mutableStateOf(false) }
    var customCategory by rememberSaveable { mutableStateOf("") }

    var showBulkCategorySheet by remember { mutableStateOf(false) }

    var showBulkCustomCategoryDialog by remember { mutableStateOf(false) }
    var bulkCustomCategoryName by remember { mutableStateOf("") }
    var bulkCustomCategoryExistsError by remember { mutableStateOf(false) }

    fun commitCustomCategory() {
        val cleaned = customCategory.trim()
        if (cleaned.isBlank()) return

        val exists = categories.any { it.equals(cleaned, ignoreCase = true) }
        if (!exists) categories.add(cleaned)

        selectedCategory = categories.first { it.equals(cleaned, ignoreCase = true) }
        isCustomCategory = false
        customCategory = ""
    }

    fun finalCategoryOrNull(): String? {
        return if (isCustomCategory) customCategory.trim().takeIf { it.isNotBlank() }
        else selectedCategory?.trim()?.takeIf { it.isNotBlank() }
    }

    fun deleteCategory(cat: String) {

        if (initialCategories.any { it.equals(cat, ignoreCase = true) }) return

        val match = categories.firstOrNull { it.equals(cat, ignoreCase = true) } ?: return
        categories.remove(match)


        if (selectedCategory?.equals(cat, ignoreCase = true) == true) {
            selectedCategory = categories.firstOrNull()
            isCustomCategory = false
            customCategory = ""
        }
    }

    var showEditCategoriesSheet by rememberSaveable { mutableStateOf(false) }
    var editCategoriesBackdropVisible by rememberSaveable { mutableStateOf(false) }
    var showAddCategorySheetDialog by rememberSaveable { mutableStateOf(false) }
    var newCategoryName by rememberSaveable { mutableStateOf("") }
    var addCategoryExistsError by rememberSaveable { mutableStateOf(false) }

    var showQuickAddCategoryDialog by remember { mutableStateOf(false) }
    var quickCategoryName by remember { mutableStateOf("") }

    fun isDuplicateCategoryName(input: String): Boolean {
        val cleaned = input.trim()
        if (cleaned.isBlank()) return false

        return categories.any { it.equals(cleaned, ignoreCase = true) }
    }

    fun addCategoryOnly(name: String): String? {
        val cleaned = name.trim()
        if (cleaned.isBlank()) return null
        if (isDuplicateCategoryName(cleaned)) return null

        categories.add(cleaned)
        return cleaned
    }

    var showError by remember { mutableStateOf(false) }
    var batchMergeNotice by remember { mutableStateOf(false) }

    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var pendingDelete by remember { mutableStateOf<FoodItem?>(null) }
    var pendingDeleteSelected by remember { mutableStateOf(false) }
    var pendingDeleteCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val shouldBlurBackground =
        showEditCategoriesSheet ||
                editCategoriesBackdropVisible ||
                showBulkCategorySheet ||
                showAddCategorySheetDialog ||
                showBulkCustomCategoryDialog ||
                showCustomCategoryDialog ||
                showQuickAddCategoryDialog ||
                pendingDelete != null ||
                pendingDeleteSelected ||
                pendingDeleteCategory != null

    val nameFocus = remember { FocusRequester() }

    val formScope = rememberCoroutineScope()

    var isLookingUpBarcode by remember { mutableStateOf(false) }
    var barcodeLookupMessage by remember { mutableStateOf<String?>(null) }
    var lastScannedBarcode by rememberSaveable { mutableStateOf<String?>(null) }

    var isSelecting by remember { mutableStateOf(false) }
    LaunchedEffect(isSelecting) {
        onSelectionModeChange(isSelecting)
    }

    DisposableEffect(Unit) {
        onDispose {
            onSelectionModeChange(false)
            onOverlayVisibilityChange(false)
        }
    }

    SideEffect {
        onOverlayVisibilityChange(shouldBlurBackground)
    }

    val selectedItems = remember { mutableStateListOf<FoodItem>() }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showPantrySearchBar by rememberSaveable { mutableStateOf(false) }
    val isHomeSearchKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var homeSearchKeyboardWasVisible by remember { mutableStateOf(false) }
    var selectedFilterCategory by rememberSaveable { mutableStateOf("All") }
    var selectedSortModeName by rememberSaveable {
        mutableStateOf(PantrySortMode.NEAREST_EXPIRY.name)
    }
    var savedListIndex by rememberSaveable { mutableIntStateOf(0) }
    var savedListOffset by rememberSaveable { mutableIntStateOf(0) }
    var filterChangeJob by remember { mutableStateOf<Job?>(null) }
    var sortScrollRequest by remember { mutableIntStateOf(0) }
    val selectedSortMode =
        PantrySortMode.entries.firstOrNull { it.name == selectedSortModeName }
            ?: PantrySortMode.NEAREST_EXPIRY

    fun closePantrySearch() {
        focusManager.clearFocus(force = true)
        keyboard?.hide()
        searchQuery = ""
        showPantrySearchBar = false
        homeSearchKeyboardWasVisible = false
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = savedListIndex,
        initialFirstVisibleItemScrollOffset = savedListOffset
    )
    val slowTopScrollFallbackPx = with(density) { 112.dp.toPx() }

    fun updatePantryFilter(newFilter: String, animateScroll: Boolean = true) {
        val wantsJumpToTop = newFilter.equals("All", ignoreCase = true)
        if (selectedFilterCategory.equals(newFilter, ignoreCase = true)) {
            if (
                wantsJumpToTop &&
                animateScroll &&
                (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0)
            ) {
                filterChangeJob?.cancel()
                filterChangeJob = formScope.launch {
                    listState.animateScrollToTopSlowly(slowTopScrollFallbackPx)
                }
            }
            return
        }

        filterChangeJob?.cancel()
        filterChangeJob = formScope.launch {
            if (
                animateScroll &&
                !wantsJumpToTop &&
                (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0)
            ) {
                listState.animateScrollToItem(0)
            }
            selectedFilterCategory = newFilter

            if (wantsJumpToTop) {
                withFrameNanos { }
                if (animateScroll) {
                    if (
                        listState.firstVisibleItemIndex != 0 ||
                        listState.firstVisibleItemScrollOffset != 0
                    ) {
                        listState.animateScrollToTopSlowly(slowTopScrollFallbackPx)
                    }
                } else {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    fun updatePantrySort(newSortMode: PantrySortMode) {
        if (selectedSortMode == newSortMode) return

        selectedSortModeName = newSortMode.name
        sortScrollRequest += 1
    }

    LaunchedEffect(sortScrollRequest) {
        if (sortScrollRequest == 0) return@LaunchedEffect

        withFrameNanos { }
        if (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0) {
            listState.animateScrollToTopSlowly(slowTopScrollFallbackPx)
        }
    }

    LaunchedEffect(searchQuery) {
        if (
            searchQuery.isNotBlank() &&
            (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0)
        ) {
            listState.scrollToItem(0)
        }
    }
    OverlayBackHandler(enabled = showPantrySearchBar && !isSelecting) {
        closePantrySearch()
    }

    BackHandler(
        enabled = showPantrySearchBar &&
                !isSelecting &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                !isHomeSearchKeyboardVisible
    ) {
        closePantrySearch()
    }

    LaunchedEffect(showPantrySearchBar, isSelecting) {
        if (showPantrySearchBar && !isSelecting) {
            homeSearchFocus.requestFocus()
            keyboard?.show()
        } else {
            homeSearchKeyboardWasVisible = false
        }
    }

    LaunchedEffect(showPantrySearchBar, isHomeSearchKeyboardVisible, isSelecting) {
        if (!showPantrySearchBar || isSelecting) return@LaunchedEffect

        if (isHomeSearchKeyboardVisible) {
            homeSearchKeyboardWasVisible = true
            return@LaunchedEffect
        }

        if (homeSearchKeyboardWasVisible) {
            closePantrySearch()
        }
    }
    var fabVisible by rememberSaveable { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner, showPantrySearchBar, isSelecting) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !showPantrySearchBar && !isSelecting) {
                fabVisible = true
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling) {
                    val index = listState.firstVisibleItemIndex
                    val offset = listState.firstVisibleItemScrollOffset

                    if (savedListIndex != index) savedListIndex = index
                    if (savedListOffset != offset) savedListOffset = offset
                }
            }
    }

    DisposableEffect(listState) {
        onDispose {
            savedListIndex = listState.firstVisibleItemIndex
            savedListOffset = listState.firstVisibleItemScrollOffset
        }
    }

    LaunchedEffect(listState) {
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset
        var currentFabVisible = fabVisible

        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                listState.isScrollInProgress
            )
        }.collect { (index, offset, isScrolling) ->
            if (!isScrolling) {
                prevIndex = index
                prevOffset = offset
                return@collect
            }

            val scrollingUp = if (index != prevIndex) index < prevIndex else offset < prevOffset
            val newFabVisible = scrollingUp || (index == 0 && offset == 0)

            if (currentFabVisible != newFabVisible) {
                currentFabVisible = newFabVisible
                fabVisible = newFabVisible
            }

            prevIndex = index
            prevOffset = offset
        }
    }

    BackHandler(enabled = isSelecting) {
        isSelecting = false
        selectedItems.clear()
    }

    LaunchedEffect(showForm) {
        if (showForm) nameFocus.requestFocus()
    }

    val foodList = remember {
        mutableStateListOf<FoodItem>().apply {
            addAll(loadFoodList(sharedPrefs, gson))
        }
    }

    DisposableEffect(sharedPrefs, gson) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    FOOD_LIST_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_ENABLED_KEY,
                    EXPIRED_FOOD_AUTO_REMOVE_DAYS_KEY ->
                        replaceListContentsIfChanged(foodList, loadFoodList(sharedPrefs, gson))

                    CATEGORIES_LIST_KEY -> replaceListContentsIfChanged(
                        categories,
                        loadStringList(sharedPrefs, gson, CATEGORIES_LIST_KEY).ifEmpty { initialCategories }
                    )
                }
            }

        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun applyCategoryToSelected(newCategory: String?) {
        val chosen = newCategory?.trim()?.takeIf { it.isNotBlank() }

        val toUpdate = selectedItems.toList()

        toUpdate.forEach { old ->
            val idx = foodList.indexOf(old)
            if (idx != -1) {
                foodList[idx] = old.copy(category = chosen)
            }
        }

        selectedItems.clear()
        isSelecting = false
    }

    fun hasMatchingFoodBatch(inputName: String, inputExpiry: String): Boolean {
        val cleanedName = inputName.trim()
        val cleanedExpiry = inputExpiry.trim()
        if (cleanedName.isBlank() || cleanedExpiry.isBlank()) return false

        val key = foodBatchKey(cleanedName, cleanedExpiry)
        return foodList.any { item ->
            item != editingItem &&
                    item.batchKey() == key
        }
    }

    fun clearBarcodeLookupUi() {
        isLookingUpBarcode = false
        barcodeLookupMessage = null
        lastScannedBarcode = null
    }

    fun closeFoodForm() {
        onShowFormChange(false)
        editingItem = null
        foodName = ""
        expiryDate = ""
        quantityText = MIN_FOOD_QUANTITY.toString()

        selectedCategory = null
        isCustomCategory = false
        customCategory = ""

        showError = false
        batchMergeNotice = false
        clearBarcodeLookupUi()
    }

    fun openEditFoodForm(food: FoodItem) {
        editingItem = food
        foodName = food.name
        expiryDate = food.expiry
        quantityText = sanitizeFoodQuantity(food.quantity).toString()

        val cat = food.category?.trim().orEmpty()
        when {
            cat.isBlank() -> {
                isCustomCategory = false
                selectedCategory = null
                customCategory = ""
            }

            categories.any { it.equals(cat, ignoreCase = true) } -> {
                isCustomCategory = false
                selectedCategory = categories.first { it.equals(cat, ignoreCase = true) }
                customCategory = ""
            }

            else -> {
                isCustomCategory = true
                selectedCategory = null
                customCategory = cat
            }
        }

        showError = false
        batchMergeNotice = false
        clearBarcodeLookupUi()
        onShowFormChange(true)
    }

    fun deleteFoodItem(food: FoodItem) {
        foodList.remove(food)
        selectedItems.remove(food)

        if (editingItem == food) {
            closeFoodForm()
        }
    }

    fun saveCurrentFoodFromForm(): Boolean {
        val missing = foodName.isBlank() || expiryDate.isBlank()
        val invalidDate = expiryDate.isNotBlank() && !isValidFutureExpiryDate(expiryDate)
        val quantity = quantityText.toIntOrNull()
        val invalidQuantity = quantity == null || quantity !in MIN_FOOD_QUANTITY..MAX_FOOD_QUANTITY

        showError = missing || invalidDate || invalidQuantity
        batchMergeNotice = !missing && !invalidDate && hasMatchingFoodBatch(foodName, expiryDate)

        if (missing || invalidDate || invalidQuantity) return false
        val savedQuantity = quantity

        if (isCustomCategory) commitCustomCategory()
        val updated = FoodItem(
            name = foodName.trim(),
            expiry = expiryDate,
            category = finalCategoryOrNull(),
            quantity = savedQuantity
        )

        val old = editingItem
        val wasSelected = old != null && selectedItems.remove(old)
        val savedFood =
            if (old == null) {
                upsertFoodBatch(foodList, updated)
            } else {
                upsertFoodBatch(foodList, updated, oldFood = old)
            }
        if (wasSelected) {
            selectedItems.add(savedFood)
        }

        addFoodNameToHistory(sharedPrefs, gson, savedFood.name)

        lastScannedBarcode?.let { scannedBarcode ->
            saveBarcodeNameToCache(
                context = context,
                barcode = scannedBarcode,
                productName = savedFood.name,
                source = "User confirmed"
            )
        }

        closeFoodForm()
        return true
    }

    val barcodeScannerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val scannedBarcode =
                result.data?.getStringExtra(EXTRA_SCANNED_BARCODE)?.trim().orEmpty()
            val scanMessage =
                result.data?.getStringExtra(EXTRA_BARCODE_SCAN_MESSAGE)?.trim().orEmpty()

            if (result.resultCode == Activity.RESULT_OK && scannedBarcode.isNotBlank()) {
                lastScannedBarcode = scannedBarcode
                isLookingUpBarcode = true
                barcodeLookupMessage = "Looking up product..."

                formScope.launch {
                    val lookup = lookupBarcodeName(context, scannedBarcode)

                    isLookingUpBarcode = false

                    if (lookup != null) {
                        foodName = lookup.productName.take(50)
                        barcodeLookupMessage = "Found product"
                    } else {
                        barcodeLookupMessage = "No product found for this barcode."
                    }

                    showError = false
                    batchMergeNotice = hasMatchingFoodBatch(foodName, expiryDate)
                }
            } else if (scanMessage.isNotBlank()) {
                isLookingUpBarcode = false
                barcodeLookupMessage = scanMessage
            }
        }

    val sortedFoodList by remember(selectedSortMode) {
        derivedStateOf {
            sortPantryFoods(foodList, selectedSortMode)
        }
    }

    val filteredFoodList by remember(sortedFoodList, searchQuery, selectedFilterCategory) {
        derivedStateOf {
            val q = searchQuery.trim()

            val effectiveFilter =
                if (selectedFilterCategory == "All" || categories.any { it.equals(selectedFilterCategory, true) })
                    selectedFilterCategory
                else
                    "All"

            val byCategory =
                if (effectiveFilter == "All") {
                    sortedFoodList
                } else {
                    sortedFoodList.filter {
                        (it.category ?: "").equals(effectiveFilter, ignoreCase = true)
                    }
                }

            if (q.isEmpty()) byCategory
            else byCategory.filter { it.name.contains(q, ignoreCase = true) }
        }
    }
    val allVisibleItemsSelected by remember(filteredFoodList) {
        derivedStateOf {
            filteredFoodList.isNotEmpty() && filteredFoodList.all { selectedItems.contains(it) }
        }
    }
    val isHomeFabVisible =
        (!isSelecting && fabVisible && !showForm) || (isSelecting && selectedItems.isNotEmpty())
    val pantryBottomSafePadding = listBottomSafePadding(
        itemCount = filteredFoodList.size,
        hasFab = isHomeFabVisible
    )

    LaunchedEffect(Unit) {
        snapshotFlow { foodList.toList() }
            .drop(1)
            .collectLatest { list ->
                saveFoodListAsync(sharedPrefs, gson, list)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { categories.toList() }
            .drop(1)
            .collectLatest { list ->
                saveStringListAsync(sharedPrefs, gson, CATEGORIES_LIST_KEY, list)
            }
    }

    val overlayDensity = LocalDensity.current
    val defaultTopBarHeightPx = with(overlayDensity) { 170.dp.roundToPx() }
    var pantryTopBarHeightPx by remember { mutableIntStateOf(defaultTopBarHeightPx) }
    val pantryContentTopPadding = with(overlayDensity) { pantryTopBarHeightPx.toDp() } + 8.dp
    val tutorialPantryTarget =
        if (tutorialActive) {
            val demoKey = normalizeFoodName(ONBOARDING_DEMO_FOOD_NAME)
            filteredFoodList.firstOrNull { normalizeFoodName(it.name) == demoKey }
                ?: filteredFoodList.firstOrNull()
        } else {
            null
        }
    var shouldWarmSelectionControls by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        shouldWarmSelectionControls = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (shouldWarmSelectionControls) {
            SelectionControlsWarmup()
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.ime,
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                if (!isSelecting) {
                    HomeFloatingButtons(
                        visible = fabVisible && !showForm,
                        onAddPositioned = if (tutorialActive) {
                            { bounds ->
                                onTutorialTargetPositioned(
                                    OnboardingSpotlightTarget.HOME_ADD_FAB,
                                    bounds
                                )
                            }
                        } else {
                            null
                        },
                        onAiPositioned = if (tutorialActive) {
                            { bounds: Rect ->
                                onTutorialTargetPositioned(
                                    OnboardingSpotlightTarget.HOME_AI_FAB,
                                    bounds
                                )
                            }
                        } else {
                            null
                        },
                        onAddClick = {
                            if (!showForm) {
                                editingItem = null
                                foodName = ""
                                expiryDate = ""
                                quantityText = MIN_FOOD_QUANTITY.toString()

                                selectedCategory = null
                                isCustomCategory = false
                                customCategory = ""

                                showError = false
                                batchMergeNotice = false
                                clearBarcodeLookupUi()
                                onShowFormChange(true)
                            }
                        },
                        onAiClick = {
                            onOpenAi()
                        }
                    )

                } else if (selectedItems.isNotEmpty()) {
                    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    val deleteFabRed = trashIconTint()
                    val deleteFabOverlay = if (isDarkTheme) Color(0xFF9F1F1F) else Color(0xFFB71C1C)

                    EditCategoriesStyledFab(
                        onClick = { pendingDeleteSelected = true },
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete Selected",
                        backgroundTint = deleteFabOverlay,
                        iconTint = deleteFabRed,
                        tintAlpha = if (isDarkTheme) 0.78f else 0.64f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp, bottom = 10.dp)
                    )
                }
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {

                    if (filteredFoodList.isEmpty() && searchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = pantryContentTopPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No food found")
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                overscrollEffect = null,
                                contentPadding = PaddingValues(
                                    top = pantryContentTopPadding,
                                    bottom = 80.dp
                                )
                            ) {
                                items(
                                    items = filteredFoodList,
                                    key = { "${it.name}|${it.expiry}|${it.category ?: ""}" },
                                    contentType = { "pantry_food_item" }
                                ) { food ->
                                    val tutorialItemModifier =
                                        if (tutorialActive && food == tutorialPantryTarget) {
                                            Modifier.onGloballyPositioned { coordinates ->
                                                onTutorialTargetPositioned(
                                                    OnboardingSpotlightTarget.PANTRY_ITEM,
                                                    coordinates.boundsInRoot()
                                                )
                                            }
                                        } else {
                                            Modifier
                                        }

                                    PantryFoodCard(
                                        modifier = Modifier
                                            .then(tutorialItemModifier)
                                            .animateItem(
                                                fadeInSpec = null,
                                                fadeOutSpec = null,
                                                placementSpec = tween(
                                                    durationMillis = SWIPE_ITEM_PLACEMENT_DURATION_MS,
                                                    easing = FastOutSlowInEasing
                                                )
                                            ),
                                        food = food,
                                        countdownFormat = countdownFormat,
                                        isSelecting = isSelecting,
                                        isSelected = selectedItems.contains(food),
                                        onSelectionChange = { checked ->
                                            if (checked) {
                                                if (!selectedItems.contains(food)) {
                                                    selectedItems.add(food)
                                                }
                                            } else {
                                                selectedItems.remove(food)
                                            }
                                        },
                                        onEdit = { openEditFoodForm(food) },
                                        onDelete = { pendingDelete = food }
                                    )
                                }
                            }
                        }
                    }
                }

                MyPantryTopBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .onSizeChanged { pantryTopBarHeightPx = it.height },
                    isSelecting = isSelecting,
                    selectedItemsCount = selectedItems.size,
                    hasVisibleItems = filteredFoodList.isNotEmpty(),
                    allVisibleItemsSelected = allVisibleItemsSelected,

                    showSearchBar = showPantrySearchBar,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onShowSearchBar = {
                        if (!showPantrySearchBar) {
                            showPantrySearchBar = true
                        } else {
                            homeSearchFocus.requestFocus()
                            keyboard?.show()
                        }
                    },
                    searchTextFieldModifier = Modifier.focusRequester(homeSearchFocus),
                    onSearchBarTap = {
                        homeSearchFocus.requestFocus()
                        keyboard?.show()
                    },

                    categories = categories,
                    selectedFilterCategory = selectedFilterCategory,
                    onFilterChange = { updatePantryFilter(it) },
                    selectedSortMode = selectedSortMode,
                    onSortChange = { updatePantrySort(it) },

                    onEnterSelectionMode = {
                        closePantrySearch()
                        isSelecting = true
                        selectedItems.clear()
                    },
                    onToggleSelectAll = {
                        if (filteredFoodList.isNotEmpty()) {
                            if (allVisibleItemsSelected) {
                                selectedItems.removeAll(filteredFoodList.toSet())
                            } else {
                                filteredFoodList.forEach { item ->
                                    if (!selectedItems.contains(item)) {
                                        selectedItems.add(item)
                                    }
                                }
                            }
                        }
                    },

                    onBulkAddToCategoryClick = {
                        showBulkCategorySheet = true
                    },

                    onBulkCustomCategoryClick = {
                        bulkCustomCategoryName = ""
                        bulkCustomCategoryExistsError = false
                        showBulkCustomCategoryDialog = true
                    },

                    onAddCategoryClick = {
                        editCategoriesBackdropVisible = true
                        showEditCategoriesSheet = true
                    },
                    tutorialActive = tutorialActive,
                    onTutorialTargetPositioned = onTutorialTargetPositioned
                )
            }
        }

        EditCategoriesBottomSheet(
            show = showEditCategoriesSheet,
            categories = categories,
            initialCategories = initialCategories,
            onBeginDismiss = {
                editCategoriesBackdropVisible = false
            },
            onDismiss = {
                editCategoriesBackdropVisible = false
                showEditCategoriesSheet = false
            },
            onAddClick = {
                newCategoryName = ""
                addCategoryExistsError = false
                showAddCategorySheetDialog = true
            },
            onDeleteClick = { cat ->
                pendingDeleteCategory = cat
            }
        )

        BulkCategorySelectionSheet(
            show = showBulkCategorySheet,
            categories = categories,
            onBeginDismiss = {},
            onDismiss = {
                showBulkCategorySheet = false
            },
            onCategorySelected = { selectedCategoryName ->
                applyCategoryToSelected(selectedCategoryName)
            }
        )
    }


    pendingDelete?.let { item ->
        GlassAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete item?") },
            text = { Text("Are you sure you want to delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    foodList.remove(item)
                    selectedItems.remove(item)

                    if (editingItem == item) {
                        editingItem = null
                        onShowFormChange(true)
                        foodName = ""
                        expiryDate = ""
                        showError = false
                    }

                    pendingDelete = null
                }) {
                    DialogDestructiveText("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingDeleteCategory?.let { cat ->
        GlassAlertDialog(
            onDismissRequest = { pendingDeleteCategory = null },
            title = { Text("Delete category?") },
            text = { Text("Are you sure you want to delete \"$cat\"? (Items in this category won't be deleted.)") },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedFilterCategory.equals(cat, ignoreCase = true)) {
                        updatePantryFilter("All")
                    }

                    for (i in foodList.indices) {
                        val item = foodList[i]
                        if ((item.category ?: "").equals(cat, ignoreCase = true)) {
                            foodList[i] = item.copy(category = null)
                        }
                    }
                    for (i in selectedItems.indices) {
                        val item = selectedItems[i]
                        if ((item.category ?: "").equals(cat, ignoreCase = true)) {
                            selectedItems[i] = item.copy(category = null)
                        }
                    }

                    deleteCategory(cat)
                    pendingDeleteCategory = null
                }) { DialogDestructiveText("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCategory = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddCategorySheetDialog) {
        val addCategoryFocus = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            delay(150)
            addCategoryFocus.requestFocus()
            keyboard?.show()
        }

        GlassAlertDialog(
            onDismissRequest = {
                showAddCategorySheetDialog = false
                addCategoryExistsError = false
            },
            title = { Text("Add Category", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = {
                            newCategoryName = it
                            addCategoryExistsError = isDuplicateCategoryName(it)
                        },
                        singleLine = true,
                        isError = addCategoryExistsError,
                        label = { Text("Category name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(addCategoryFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val cleaned = newCategoryName.trim()
                                if (cleaned.isBlank()) return@KeyboardActions

                                if (isDuplicateCategoryName(cleaned)) {
                                    addCategoryExistsError = true
                                    return@KeyboardActions
                                }

                                val added = addCategoryOnly(cleaned)
                                if (added != null) {
                                    addCategoryExistsError = false
                                    showAddCategorySheetDialog = false
                                    newCategoryName = ""
                                }
                            }
                        )
                    )

                    if (addCategoryExistsError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Category already exists!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleaned = newCategoryName.trim()
                    if (cleaned.isBlank()) return@TextButton

                    if (isDuplicateCategoryName(cleaned)) {
                        addCategoryExistsError = true
                        return@TextButton
                    }

                    val added = addCategoryOnly(cleaned)
                    if (added != null) {
                        addCategoryExistsError = false
                        showAddCategorySheetDialog = false
                        newCategoryName = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddCategorySheetDialog = false
                    addCategoryExistsError = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showBulkCustomCategoryDialog) {
        val bulkCustomFocus = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            delay(150)
            bulkCustomFocus.requestFocus()
            keyboard?.show()
        }

        GlassAlertDialog(
            onDismissRequest = {
                showBulkCustomCategoryDialog = false
                bulkCustomCategoryExistsError = false
            },
            title = { Text("Custom Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = bulkCustomCategoryName,
                        onValueChange = {
                            bulkCustomCategoryName = it
                            bulkCustomCategoryExistsError = isDuplicateCategoryName(it)
                        },
                        singleLine = true,
                        isError = bulkCustomCategoryExistsError,
                        label = { Text("Category name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(bulkCustomFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val cleaned = bulkCustomCategoryName.trim()
                                if (cleaned.isBlank()) return@KeyboardActions

                                if (isDuplicateCategoryName(cleaned)) {
                                    bulkCustomCategoryExistsError = true
                                    return@KeyboardActions
                                }

                                val added = addCategoryOnly(cleaned)
                                if (added != null) {
                                    bulkCustomCategoryExistsError = false
                                    applyCategoryToSelected(added)
                                    showBulkCustomCategoryDialog = false
                                }
                            }
                        )
                    )

                    if (bulkCustomCategoryExistsError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Category already exists!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleaned = bulkCustomCategoryName.trim()
                    if (cleaned.isBlank()) return@TextButton

                    if (isDuplicateCategoryName(cleaned)) {
                        bulkCustomCategoryExistsError = true
                        return@TextButton
                    }

                    val added = addCategoryOnly(cleaned)
                    if (added != null) {
                        bulkCustomCategoryExistsError = false
                        applyCategoryToSelected(added)
                        showBulkCustomCategoryDialog = false
                    }
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBulkCustomCategoryDialog = false
                    bulkCustomCategoryExistsError = false
                }) { Text("Cancel") }
            }
        )
    }

    if (pendingDeleteSelected) {
        GlassAlertDialog(
            onDismissRequest = { pendingDeleteSelected = false },
            title = { Text("Delete selected items?") },
            text = {
                Text("Are you sure you want to delete ${selectedItems.size} items.")
            },
            confirmButton = {
                DelayedDestructiveConfirmButton(
                    itemCount = selectedItems.size,
                    finalButtonText = "Delete",
                    onConfirm = {
                        val toDelete = selectedItems.toList()
                        foodList.removeAll(toDelete)

                        selectedItems.clear()
                        isSelecting = false
                        pendingDeleteSelected = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSelected = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomCategoryDialog) {

        if (showQuickAddCategoryDialog) {
            GlassAlertDialog(
                onDismissRequest = { showQuickAddCategoryDialog = false },
                title = { Text("Add Category", style = MaterialTheme.typography.titleLarge) },
                text = {
                    OutlinedTextField(
                        value = quickCategoryName,
                        onValueChange = { quickCategoryName = it },
                        singleLine = true,
                        label = { Text("Category name") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val added = addCategoryOnly(quickCategoryName)
                                if (added != null) {
                                    updatePantryFilter(added)
                                    showQuickAddCategoryDialog = false
                                }
                            }
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val added = addCategoryOnly(quickCategoryName)
                        if (added != null) {
                            updatePantryFilter(added)
                            showQuickAddCategoryDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuickAddCategoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        val customFocus = remember { FocusRequester() }

        val keyboard = LocalSoftwareKeyboardController.current

        LaunchedEffect(showCustomCategoryDialog) {
            if (showCustomCategoryDialog) {
                delay(150)
                customFocus.requestFocus()
                keyboard?.show()
            }
        }

        GlassAlertDialog(
            onDismissRequest = {
                showCustomCategoryDialog = false
                customCategoryExistsError = false
            },
            title = { Text("Custom Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempCustomCategory,
                        onValueChange = {
                            tempCustomCategory = it
                            customCategoryExistsError = isDuplicateCategoryName(it)
                        },
                        singleLine = true,
                        isError = customCategoryExistsError,
                        label = { Text("Category name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(customFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val cleaned = tempCustomCategory.trim()
                                if (cleaned.isBlank()) return@KeyboardActions

                                if (isDuplicateCategoryName(cleaned)) {
                                    customCategoryExistsError = true
                                    return@KeyboardActions
                                }

                                customCategory = cleaned
                                isCustomCategory = true
                                commitCustomCategory()
                                customCategoryExistsError = false
                                showCustomCategoryDialog = false
                            }
                        )
                    )

                    if (customCategoryExistsError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Category already exists!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleaned = tempCustomCategory.trim()
                    if (cleaned.isBlank()) return@TextButton

                    if (isDuplicateCategoryName(cleaned)) {
                        customCategoryExistsError = true
                        return@TextButton
                    }

                    customCategory = cleaned
                    isCustomCategory = true
                    commitCustomCategory()
                    customCategoryExistsError = false
                    showCustomCategoryDialog = false
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCustomCategoryDialog = false
                    customCategoryExistsError = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showForm) {
        val animatedImeShiftPx = rememberAnimatedImeShiftPx(label = "addFoodImeShift")

        Dialog(
            onDismissRequest = { closeFoodForm() },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ExactFrostedPillCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationY = -animatedImeShiftPx }
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 14.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (editingItem == null) "Add Food" else "Edit Food",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = foodName,
                                onValueChange = {
                                    if (it.length <= 50) {
                                        foodName = it
                                        if (showError) showError = false
                                        batchMergeNotice = hasMatchingFoodBatch(foodName, expiryDate)
                                    }
                                },
                                label = { Text("Food Name") },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(nameFocus),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { saveCurrentFoodFromForm() }
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    barcodeLookupMessage = null
                                    isLookingUpBarcode = false
                                    barcodeScannerLauncher.launch(
                                        Intent(context, BarcodeScannerActivity::class.java)
                                    )
                                },
                                enabled = !isLookingUpBarcode,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_barcode_scanner_24),
                                    contentDescription = "Scan barcode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (isLookingUpBarcode || !barcodeLookupMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isLookingUpBarcode) {
                                    "Looking up product..."
                                } else {
                                    barcodeLookupMessage.orEmpty()
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        ExpiryDateInputField(
                            value = expiryDate,
                            onValueChange = {
                                expiryDate = it
                                if (showError) showError = false
                                batchMergeNotice = hasMatchingFoodBatch(foodName, it)
                            },
                            onCalendarClick = {
                                openExpiryDatePicker(context) { pickedDate ->
                                    expiryDate = pickedDate
                                    if (showError) showError = false
                                    batchMergeNotice = hasMatchingFoodBatch(foodName, pickedDate)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { value ->
                                quantityText = value.filter { it.isDigit() }.take(3)
                                if (showError) showError = false
                            },
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { saveCurrentFoodFromForm() }
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CategoryDropdown(
                            categories = categories,
                            initialCategories = initialCategories,
                            selectedCategory = selectedCategory,
                            isCustom = isCustomCategory,
                            onPickCategory = {
                                isCustomCategory = false
                                selectedCategory = it
                                customCategory = ""
                                if (showError) showError = false
                            },
                            onPickCustom = {
                                showCustomCategoryDialog = true
                                tempCustomCategory = ""
                                customCategoryExistsError = false
                            },
                            onDeleteCategory = { cat ->
                                pendingDeleteCategory = cat
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { closeFoodForm() }) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {

                                    saveCurrentFoodFromForm()
                                }
                            ) {
                                Text("Done")
                            }
                        }

                        if (showError) {
                            when {
                                foodName.isEmpty() && expiryDate.isEmpty() ->
                                    Text(
                                        "Please enter food name and expiry date!",
                                        color = MaterialTheme.colorScheme.error
                                    )

                                foodName.isEmpty() ->
                                    Text(
                                        "Please enter food name!",
                                        color = MaterialTheme.colorScheme.error
                                    )

                                expiryDate.isEmpty() ->
                                    Text(
                                        "Please enter expiry date!",
                                        color = MaterialTheme.colorScheme.error
                                    )

                                !isValidFutureExpiryDate(expiryDate) ->
                                    Text(
                                        "Please enter a valid future date!",
                                        color = MaterialTheme.colorScheme.error
                                    )

                                quantityText.toIntOrNull() == null ||
                                        quantityText.toIntOrNull() !in MIN_FOOD_QUANTITY..MAX_FOOD_QUANTITY ->
                                    Text(
                                        "Please enter a quantity from $MIN_FOOD_QUANTITY to $MAX_FOOD_QUANTITY.",
                                        color = MaterialTheme.colorScheme.error
                                    )
                            }
                        } else if (batchMergeNotice) {
                            Text(
                                "Same batch found. Saving will increase its quantity.",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

class BarcodeScannerActivity : ComponentActivity() {

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                showScannerContent()
            } else {
                val permissionMessage =
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        "Camera permission is needed to scan barcodes."
                    } else {
                        "Enable camera access in Settings to scan barcodes."
                    }

                setResult(
                    Activity.RESULT_CANCELED,
                    Intent().putExtra(EXTRA_BARCODE_SCAN_MESSAGE, permissionMessage)
                )
                Toast.makeText(
                    this,
                    permissionMessage,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            showScannerContent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun showScannerContent() {
        setContent {
            val appCtx = applicationContext
            AppTheme(mode = loadThemeMode(appCtx)) {
                BarcodeScannerScreen(
                    onClose = { finish() },
                    onBarcodeScanned = { barcode ->
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(EXTRA_SCANNED_BARCODE, barcode)
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
private fun BarcodeGuideCorners(modifier: Modifier = Modifier) {
    val guideColor = Color(0xFF79C9FF)

    Canvas(modifier = modifier) {
        val frameWidth = size.width * 0.78f
        val frameHeight = size.height * 0.22f
        val centerX = size.width / 2f
        val centerY = size.height * 0.46f

        val left = centerX - frameWidth / 2f
        val top = centerY - frameHeight / 2f
        val right = centerX + frameWidth / 2f
        val bottom = centerY + frameHeight / 2f

        val stroke = 4.dp.toPx()
        val cornerLength = min(frameWidth, frameHeight) * 0.30f

        drawLine(
            color = guideColor,
            start = Offset(left, top),
            end = Offset(left + cornerLength, top),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = guideColor,
            start = Offset(left, top),
            end = Offset(left, top + cornerLength),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = guideColor,
            start = Offset(right - cornerLength, top),
            end = Offset(right, top),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = guideColor,
            start = Offset(right, top),
            end = Offset(right, top + cornerLength),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = guideColor,
            start = Offset(left, bottom - cornerLength),
            end = Offset(left, bottom),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = guideColor,
            start = Offset(left, bottom),
            end = Offset(left + cornerLength, bottom),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = guideColor,
            start = Offset(right - cornerLength, bottom),
            end = Offset(right, bottom),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = guideColor,
            start = Offset(right, bottom - cornerLength),
            end = Offset(right, bottom),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
private fun BarcodeScannerScreen(
    onClose: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_ITF
            )
            .build()

        BarcodeScanning.getClient(options)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val hasReturnedResult = remember { AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(previewView) {
        val cameraProvider = context.getCameraProvider()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val mediaImage = imageProxy.image

            if (mediaImage == null || hasReturnedResult.get()) {
                imageProxy.close()
                return@setAnalyzer
            }

            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val value = barcodes
                        .firstNotNullOfOrNull { it.rawValue?.trim()?.takeIf(String::isNotBlank) }

                    if (value != null && hasReturnedResult.compareAndSet(false, true)) {
                        onBarcodeScanned(value)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        } catch (_: Exception) {
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        BarcodeGuideCorners(
            modifier = Modifier.matchParentSize()
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.Black.copy(alpha = 0.55f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Scan barcode",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hold the food barcode inside the camera view.",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Text("Close")
        }
    }
}
