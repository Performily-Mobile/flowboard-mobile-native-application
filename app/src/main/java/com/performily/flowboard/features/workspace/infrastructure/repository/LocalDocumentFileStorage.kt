package com.performily.flowboard.features.workspace.infrastructure.repository

import android.content.Context
import android.net.Uri
import com.performily.flowboard.features.workspace.domain.repository.DocumentFileStorage
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentUpload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Implementación TEMPORAL del almacenamiento de documentos.
 * Copia el archivo a la memoria interna de la app y devuelve su URI local.
 * Cuando se defina el almacenamiento real (por ejemplo, Firebase Storage),
 * se crea otra implementación de DocumentFileStorage y se cambia el @Binds
 * en WorkspaceRepositoryModule. Nada más del proyecto cambia.
 */
class LocalDocumentFileStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DocumentFileStorage {

    override suspend fun store(upload: DocumentUpload): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
            val target = File(directory, "${System.currentTimeMillis()}_${upload.fileName.sanitized()}")
            val input = context.contentResolver.openInputStream(Uri.parse(upload.sourceUri))
                ?: error("No se pudo leer el archivo seleccionado.")
            input.use { source -> target.outputStream().use { source.copyTo(it) } }
            Uri.fromFile(target).toString()
        }
    }

    private fun String.sanitized(): String = replace(Regex("[^A-Za-z0-9._-]"), "_")

    private companion object {
        const val DIRECTORY = "employee-documents"
    }
}
