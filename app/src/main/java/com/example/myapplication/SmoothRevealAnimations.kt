package com.example.myapplication

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment

internal fun smoothVerticalRevealEnter(
    expandFrom: Alignment.Vertical = Alignment.Top
): EnterTransition {
    return expandVertically(
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        expandFrom = expandFrom,
        clip = false
    ) + slideInVertically(
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        initialOffsetY = { -it / 5 }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 160,
            delayMillis = 70,
            easing = LinearOutSlowInEasing
        )
    )
}

internal fun smoothVerticalRevealExit(
    shrinkTowards: Alignment.Vertical = Alignment.Top
): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = 90,
            easing = FastOutLinearInEasing
        )
    ) + slideOutVertically(
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutLinearInEasing
        ),
        targetOffsetY = { -it / 6 }
    ) + shrinkVertically(
        animationSpec = tween(
            durationMillis = 240,
            easing = FastOutSlowInEasing
        ),
        shrinkTowards = shrinkTowards,
        clip = false
    )
}
