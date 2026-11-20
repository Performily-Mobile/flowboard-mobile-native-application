package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * Pull-to-refresh container for the lists of the app.
 *
 * Screens only report whether they are loading; the spinner shows from the pull until that load
 * finishes (and at least [MIN_SPINNER_MILLIS], so a reload served from the cache still gives
 * feedback), so a list does not need a separate "refreshing" flag in its state.
 *
 * @param isLoading true while the screen is loading its data.
 * @param onRefresh called when the user pulls down to reload.
 * @param modifier modifier applied to the container.
 * @param content list or layout that can be pulled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowboardRefreshBox(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var pulled by remember { mutableStateOf(false) }
    val loading by rememberUpdatedState(isLoading)
    LaunchedEffect(isLoading) {
        if (!isLoading) pulled = false
    }
    LaunchedEffect(pulled) {
        if (pulled) {
            delay(MIN_SPINNER_MILLIS)
            if (!loading) pulled = false
        }
    }
    PullToRefreshBox(
        isRefreshing = pulled,
        onRefresh = {
            pulled = true
            onRefresh()
        },
        modifier = modifier,
        content = content
    )
}

/** Shortest time the spinner stays visible after a pull. */
private const val MIN_SPINNER_MILLIS = 600L
