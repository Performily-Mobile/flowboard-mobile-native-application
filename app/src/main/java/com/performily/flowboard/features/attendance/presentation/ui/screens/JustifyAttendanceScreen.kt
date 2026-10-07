package com.performily.flowboard.features.attendance.presentation.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceInfoBanner
import com.performily.flowboard.features.attendance.presentation.viewmodel.JustifyAttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JustifyAttendanceScreen(
    attendanceRecordId: Long,
    workDate: java.time.LocalDate,
    onClose: () -> Unit,
    viewModel: JustifyAttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var typeMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(attendanceRecordId, workDate) {
        viewModel.initialize(attendanceRecordId, workDate)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { context.describeEvidence(it) }?.let { selected ->
            viewModel.onEvidenceSelected(selected.first, selected.second)
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Justificar inasistencia") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(FlowboardIcons.Close, contentDescription = "Cerrar") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(state.workDateLabel, style = MaterialTheme.typography.titleMedium)
                        Text("Sin marcación de entrada ni salida", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Inasistencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                }
            }

            androidx.compose.foundation.layout.Box {
                OutlinedTextField(
                    value = state.justificationType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de justificación") },
                    trailingIcon = { Icon(FlowboardIcons.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Box(
                    Modifier.matchParentSize().clickable { typeMenuExpanded = true }
                )
                DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    listOf("Descanso médico", "Permiso personal", "Otro").forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                typeMenuExpanded = false
                                viewModel.onTypeChange(type)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.reason,
                onValueChange = viewModel::onReasonChange,
                label = { Text("Motivo de la inasistencia") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Sustento", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = { picker.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(FlowboardIcons.AttachFile, contentDescription = null)
                Text(state.evidenceName ?: "Adjuntar certificado o sustento")
            }
            state.evidenceName?.let {
                Text("Archivo seleccionado: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            AttendanceInfoBanner("Se crea una solicitud de justificación para tu jefe directo. Sigue su estado en Mis solicitudes.")

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSaving && state.reason.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Enviando..." else "Enviar justificación")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}


private fun Context.describeEvidence(uri: Uri): Pair<String, String>? {
    val contentType = contentResolver.getType(uri).orEmpty()
    if (contentType !in setOf("application/pdf", "image/jpeg", "image/png")) return null

    var name = uri.lastPathSegment ?: "sustento"
    var size = 0L
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex >= 0) name = cursor.getString(nameIndex)
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
        }
    }
    if (size > 5 * 1024 * 1024) return null
    return name to uri.toString()
}
