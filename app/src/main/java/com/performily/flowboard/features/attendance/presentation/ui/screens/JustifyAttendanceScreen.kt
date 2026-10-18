package com.performily.flowboard.features.attendance.presentation.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceInfoBanner
import com.performily.flowboard.features.attendance.presentation.viewmodel.JustifyAttendanceViewModel
import java.time.LocalDate

/**
 * Form to justify an absence: type, reason and an optional evidence file.
 *
 * The send button lives in a bottom action bar, so it stays above the system navigation bar
 * and the keyboard.
 *
 * @param attendanceRecordId id of the absent attendance record.
 * @param workDate date of the absence.
 * @param onClose called when the user closes the form or the justification is sent.
 * @param viewModel view model that holds the form state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JustifyAttendanceScreen(
    attendanceRecordId: Long,
    workDate: LocalDate,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            FlowboardBottomActionBar {
                Button(
                    onClick = viewModel::submit,
                    enabled = !state.isSaving && state.reason.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(if (state.isSaving) "Enviando..." else "Enviar justificación")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(state.workDateLabel, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Sin marcación de entrada ni salida",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("Inasistencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                }
            }

            Box {
                OutlinedTextField(
                    value = state.justificationType,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Tipo de justificación") },
                    trailingIcon = { Icon(FlowboardIcons.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable { typeMenuExpanded = true }
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

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text("Sustento", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = { picker.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(FlowboardIcons.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        state.evidenceName ?: "Adjuntar certificado o sustento",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = Spacing.s)
                    )
                }
            }

            AttendanceInfoBanner("Se crea una solicitud de justificación para tu jefe directo. Sigue su estado en Mis solicitudes.")

            state.errorMessage?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * Reads the name of a picked evidence file and checks its type and size.
 *
 * @receiver context used to query the content resolver.
 * @param uri content URI of the picked file.
 * @return the file name and URI, or null when the file is not a PDF, JPEG or PNG or is larger than 5 MB.
 */
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
