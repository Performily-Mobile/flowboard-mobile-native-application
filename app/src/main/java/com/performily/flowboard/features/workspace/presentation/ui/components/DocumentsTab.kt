package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory

@Composable
fun DocumentsTab(
    documents: List<EmployeeDocument>,
    selectedCategory: DocumentCategory?,
    onCategoryChange: (DocumentCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(label = "Todos", selected = selectedCategory == null) { onCategoryChange(null) }
            CategoryChip("Contratos", selectedCategory == DocumentCategory.CONTRACT) {
                onCategoryChange(DocumentCategory.CONTRACT)
            }
            CategoryChip("Constancias", selectedCategory == DocumentCategory.CERTIFICATE) {
                onCategoryChange(DocumentCategory.CERTIFICATE)
            }
            CategoryChip("Personales", selectedCategory == DocumentCategory.PERSONAL) {
                onCategoryChange(DocumentCategory.PERSONAL)
            }
        }

        if (documents.isEmpty()) {
            Text(
                text = "No hay documentos en el expediente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        documents.forEach { document ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = document.documentType.label(), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${document.documentType.category.label()} · Cargado el ${document.uploadedAt.toDisplay()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { runCatching { uriHandler.openUri(document.file.storageUrl) } }) {
                        Icon(FlowboardIcons.Download, contentDescription = "Descargar")
                    }
                }
                HorizontalDivider(color = Divider)
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}
