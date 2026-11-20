package com.performily.flowboard.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Counts up from 0 to [target] the first time it appears, then follows later changes smoothly.
 *
 * Text that is not a whole number (such as "—" when a section failed, or "46.2 h") is returned as
 * is, so callers can pass any formatted value.
 *
 * @param target formatted value to show.
 * @return the text to draw in the current frame.
 */
@Composable
fun animatedNumber(target: String): String {
    val number = target.toIntOrNull() ?: return target
    var started by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val shown by animateIntAsState(
        targetValue = if (started) number else 0,
        animationSpec = tween(durationMillis = COUNT_DURATION_MILLIS, easing = FastOutSlowInEasing),
        label = "animatedNumber"
    )
    return shown.toString()
}

/** Duration of the count-up animation. */
private const val COUNT_DURATION_MILLIS = 700
