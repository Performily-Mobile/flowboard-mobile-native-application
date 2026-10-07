package com.performily.flowboard.features.request.infrastructure.repository

import android.content.Context
import android.net.Uri
import com.performily.flowboard.features.request.domain.repository.RequestAttachmentStorage
import com.performily.flowboard.features.request.domain.valueobject.AttachmentUpload
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LocalRequestAttachmentStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) : RequestAttachmentStorage {

    override suspend fun store(upload: AttachmentUpload): Result<String> = withContext(Dispatchers.IO) {
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
        const val DIRECTORY = "request-attachments"
    }
}
