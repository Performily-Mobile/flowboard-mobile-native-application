package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardTabs

/**
 * Tabs that switch between the three HR attendance views.
 *
 * They use the shared [FlowboardTabs] selector, so changing views looks the same as in
 * "Solicitudes" and "Organización", and filters stay as chips with a different meaning.
 *
 * @param selectedIndex position of the current view: 0 records, 1 summary by area, 2 hours and overtime.
 * @param onSelected called with the position of the view the user taps.
 * @param modifier modifier applied to the tabs.
 */
@Composable
fun AttendanceSectionTabs(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowboardTabs(
        labels = listOf("Registros", "Por área", "Horas extra"),
        selectedIndex = selectedIndex,
        onSelect = onSelected,
        modifier = modifier
    )
}
