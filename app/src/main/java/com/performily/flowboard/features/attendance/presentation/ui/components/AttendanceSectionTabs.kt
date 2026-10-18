package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardTabs

/**
 * Sections of the HR Attendance tab: records, report by area and hours.
 *
 * @param selectedIndex position of the section currently shown.
 * @param onSelected called with the position of the section the user taps.
 * @param modifier modifier applied to the tabs.
 */
@Composable
fun AttendanceSectionTabs(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowboardTabs(
        labels = listOf("Registros", "Resumen por área", "Horas y sobretiempo"),
        selectedIndex = selectedIndex,
        onSelect = onSelected,
        modifier = modifier
    )
}
