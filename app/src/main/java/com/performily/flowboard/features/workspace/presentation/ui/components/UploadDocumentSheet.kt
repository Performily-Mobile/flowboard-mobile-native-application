package com.performily.flowboard.features.workspace.presentation.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentUpload
import com.performily.flowboard.features.workspace.presentation.state.SelectedFile
import com.performily.flowboard.features.workspace.presentation.state.UploadDocumentForm
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDocumentSheet(
    form: UploadDocumentForm,
    isSaving: Boolean,
    errorMessage: String?,
    onDocumentTypeChange: (DocumentType) -> Unit,
    onFileSelected: (SelectedFile) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { context.describeFile(it) }?.let(onFileSelected)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Subir documento", style = MaterialTheme.typography.titleLarge)
            SelectField(
                label = "Tipo de documento",
                options = DocumentType.entries,
                selected = form.documentType,
                optionLabel = { it.label() },
                onSelect = onDocumentTypeChange,
                isError = form.documentTypeError != null,
                supportingText = form.documentTypeError,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = { picker.launch(DocumentUpload.ALLOWED_CONTENT_TYPES.toTypedArray()) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(FlowboardIcons.AttachFile, contentDescription = null)
                Text(
                    text = form.file?.name ?: "Seleccionar archivo",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                text = form.fileError
                    ?: form.file?.let { "${it.contentType.toFormatLabel()} · ${it.sizeInBytes.toSizeLabel()}" }
                    ?: "PDF, JPG o PNG de hasta 5 MB.",
                style = MaterialTheme.typography.bodySmall,
                color = if (form.fileError != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            SheetActions(
                confirmLabel = if (isSaving) "Subiendo..." else "Subir",
                enabled = !isSaving && form.documentType != null && form.file != null && form.fileError == null,
                onConfirm = onSave,
                onCancel = onDismiss
            )
        }
    }
}

/** Lee nombre, tipo y tamaño del archivo elegido en el selector del sistema. */
private fun Context.describeFile(uri: Uri): SelectedFile? {
    val contentType = contentResolver.getType(uri) ?: "application/octet-stream"
    var name: String? = null
    var size = 0L
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = cursor.getString(nameIndex)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
    return SelectedFile(
        uri = uri.toString(),
        name = name ?: uri.lastPathSegment ?: "documento",
        contentType = contentType,
        sizeInBytes = size
    )
}

private fun String.toFormatLabel(): String = when (this) {
    "application/pdf" -> "PDF"
    "image/jpeg" -> "JPG"
    "image/png" -> "PNG"
    else -> this
}

private fun Long.toSizeLabel(): String {
    val kb = this / 1024.0
    return if (kb < 1024) {
        String.format(Locale.US, "%.0f KB", kb)
    } else {
        String.format(Locale.US, "%.1f MB", kb / 1024)
    }
}
