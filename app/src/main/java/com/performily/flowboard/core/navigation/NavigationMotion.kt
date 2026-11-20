package com.performily.flowboard.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

/**
 * Screen transitions of the app: the Material 3 shared X axis pattern.
 *
 * Opening a screen slides it in a little from the right while it fades in, and going back does the
 * opposite. It replaces the default 700 ms cross-fade of Navigation Compose, which made every screen
 * change feel slow, with a 300 ms motion that also tells the user whether they went forward or back.
 */
internal object NavigationMotion {

    private const val DURATION_MILLIS = 300
    private const val FADE_MILLIS = 150

    /** Material 3 emphasized easing. */
    private val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Share of the screen width a screen travels while it slides. */
    private const val SLIDE_FRACTION = 10

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(tween(DURATION_MILLIS, easing = Emphasized)) { it / SLIDE_FRACTION } +
            fadeIn(tween(DURATION_MILLIS, delayMillis = FADE_MILLIS / 2))
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(tween(DURATION_MILLIS, easing = Emphasized)) { -it / SLIDE_FRACTION } +
            fadeOut(tween(FADE_MILLIS))
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(tween(DURATION_MILLIS, easing = Emphasized)) { -it / SLIDE_FRACTION } +
            fadeIn(tween(DURATION_MILLIS, delayMillis = FADE_MILLIS / 2))
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(tween(DURATION_MILLIS, easing = Emphasized)) { it / SLIDE_FRACTION } +
            fadeOut(tween(FADE_MILLIS))
    }
}
