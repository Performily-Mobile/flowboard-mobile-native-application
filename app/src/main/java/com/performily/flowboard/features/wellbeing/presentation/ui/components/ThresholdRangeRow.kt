package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator

/**
 * MA-74 · Una fila de la tabla de umbrales: el indicador y su rango Mín–Máx.
 * Si la fila tiene un problema (superposición, vacío, máx ≤ mín) se marca en rojo
 * y el mensaje aparece debajo.
 */
@Composable
fun ThresholdRangeRow(
    indicator: HealthIndicator,
    min: String,
    max: String,
    error: String?,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(96.dp)) {
                IndicatorChip(indicator = indicator)
            }
            NumberField(
                label = "Mín",
                value = min,
                isError = error != null,
                onValueChange = onMinChange,
                modifier = Modifier.weight(1f)
            )
            NumberField(
                label = "Máx",
                value = max,
                isError = error != null,
                onValueChange = onMaxChange,
                modifier = Modifier.weight(1f)
            )
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 104.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        // Solo dígitos, un punto decimal y el signo menos (temperaturas bajo cero).
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() || it == '.' || it == '-' }) },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}
