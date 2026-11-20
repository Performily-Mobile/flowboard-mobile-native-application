package com.performily.flowboard.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Meaning of a status shown to the user.
 *
 * Every status badge, banner and indicator of the app picks one of these tones instead of a raw
 * color, so "Activo", "Aprobada" and "Pagado" share the same green and the colors follow the
 * light and dark themes.
 */
enum class StatusTone {
    SUCCESS,
    WARNING,
    DANGER,
    INFO,
    NEUTRAL
}

/**
 * Container and content colors of each [StatusTone].
 *
 * Material 3 has no success or warning roles, so the app adds them as an extended palette
 * that keeps a readable contrast between each container and its content.
 */
@Immutable
data class StatusColors(
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val neutralContainer: Color,
    val onNeutralContainer: Color
) {

    /**
     * Background color of a tone.
     *
     * @param tone meaning of the status.
     * @return the container color of the tone.
     */
    fun container(tone: StatusTone): Color = when (tone) {
        StatusTone.SUCCESS -> successContainer
        StatusTone.WARNING -> warningContainer
        StatusTone.DANGER -> dangerContainer
        StatusTone.INFO -> infoContainer
        StatusTone.NEUTRAL -> neutralContainer
    }

    /**
     * Text and icon color of a tone, drawn over [container].
     *
     * @param tone meaning of the status.
     * @return the content color of the tone.
     */
    fun content(tone: StatusTone): Color = when (tone) {
        StatusTone.SUCCESS -> onSuccessContainer
        StatusTone.WARNING -> onWarningContainer
        StatusTone.DANGER -> onDangerContainer
        StatusTone.INFO -> onInfoContainer
        StatusTone.NEUTRAL -> onNeutralContainer
    }
}

/** Status colors of the light theme. */
val LightStatusColors = StatusColors(
    successContainer = Color(0xFFCDEBC5),
    onSuccessContainer = Color(0xFF0F3A0B),
    warningContainer = Color(0xFFFFE2BC),
    onWarningContainer = Color(0xFF4F3200),
    dangerContainer = ErrorContainer,
    onDangerContainer = OnErrorContainer,
    infoContainer = PrimaryContainer,
    onInfoContainer = OnPrimaryContainer,
    neutralContainer = SurfaceContainerHighest,
    onNeutralContainer = OnSurfaceVariant
)

/**
 * Status colors of the dark theme. The containers are deep and less saturated than the light ones,
 * so a long list of badges (for example many absences) does not glare on a dark background.
 */
val DarkStatusColors = StatusColors(
    successContainer = Color(0xFF1F4D1A),
    onSuccessContainer = Color(0xFFCDEBC5),
    warningContainer = Color(0xFF5C3B00),
    onWarningContainer = Color(0xFFFFE2BC),
    dangerContainer = Color(0xFF5C1D19),
    onDangerContainer = Color(0xFFFFDAD6),
    infoContainer = PrimaryContainerDark,
    onInfoContainer = OnPrimaryContainerDark,
    neutralContainer = SurfaceContainerHighestDark,
    onNeutralContainer = OnSurfaceVariantDark
)

/** Status colors of the current theme, provided by [FlowboardTheme]. */
val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

/** Status colors of the current theme, read next to the Material color scheme. */
val MaterialTheme.statusColors: StatusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalStatusColors.current
