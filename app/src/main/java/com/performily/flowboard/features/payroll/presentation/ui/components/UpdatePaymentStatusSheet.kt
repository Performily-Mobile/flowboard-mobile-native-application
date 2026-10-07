package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentDetails
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.presentation.state.PaymentSheetState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Opciones del segmented button, en el orden de MA-69. */
private val PAYMENT_CHOICES = listOf(PaymentStatus.PAID, PaymentStatus.OBSERVED)

/**
 * MA-69 · Hoja "Actualizar estado de pago".
 * Pagado pide la fecha de depósito (la exige el backend); Observado pide el motivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePaymentStatusSheet(
    sheet: PaymentSheetState,
    onChoiceChange: (PaymentStatus) -> Unit,
    onPaidOnChange: (LocalDate) -> Unit,
    onReasonChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { SheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Actualizar estado de pago",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = sheet.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PAYMENT_CHOICES.forEachIndexed { index, choice ->
                    SegmentedButton(
                        selected = sheet.choice == choice,
                        onClick = { onChoiceChange(choice) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = PAYMENT_CHOICES.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            activeBorderColor = MaterialTheme.colorScheme.outline,
                            inactiveBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        icon = {
                            SegmentedButtonDefaults.Icon(
                                active = sheet.choice == choice,
                                activeContent = {
                                    Icon(
                                        imageVector = FlowboardIcons.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                    )
                                }
                            )
                        }
                    ) {
                        Text(
                            text = choice.label(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            when (sheet.choice) {
                PaymentStatus.OBSERVED -> OutlinedTextField(
                    value = sheet.reason,
                    onValueChange = { if (it.length <= PaymentDetails.REASON_MAX_LENGTH) onReasonChange(it) },
                    label = { Text("Motivo de la observación") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                )
                PaymentStatus.PAID -> PaidOnField(value = sheet.paidOn, onValueChange = onPaidOnChange)
                else -> Unit
            }

            sheet.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Button(
                    onClick = onSave,
                    enabled = sheet.canSave,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) { Text(if (sheet.isSaving) "Guardando..." else "Guardar") }
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 16.dp)
            .size(width = 32.dp, height = 4.dp)
            .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
    )
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Fecha de depósito (no puede ser futura). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaidOnField(value: LocalDate?, onValueChange: (LocalDate) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value?.format(dateFormatter).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de depósito") },
            trailingIcon = { Icon(FlowboardIcons.Calendar, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }
    if (showDialog) {
        val today = LocalDate.now()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (value ?: today).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(today)

                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onValueChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        showDialog = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = state)
        }
    }
}
