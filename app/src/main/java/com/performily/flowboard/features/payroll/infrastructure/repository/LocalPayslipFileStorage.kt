package com.performily.flowboard.features.payroll.infrastructure.repository

import android.content.Context
import android.net.Uri
import com.performily.flowboard.features.payroll.domain.repository.PayslipFileStorage
import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Implementación TEMPORAL del almacenamiento de boletas (mismo enfoque que Workspace).
 * Copia el PDF a la memoria interna de la app y devuelve su URI local.
 * Cuando se defina el almacenamiento real, se crea otra implementación de PayslipFileStorage
 * y se cambia el @Binds en PayrollRepositoryModule.
 */
class LocalPayslipFileStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PayslipFileStorage {

    override suspend fun store(file: PayrollSystemFile): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
            val target = File(directory, "${System.currentTimeMillis()}_${file.fileName.sanitized()}")
            val input = context.contentResolver.openInputStream(Uri.parse(file.sourceUri))
                ?: error("No se pudo leer ${file.fileName}.")
            input.use { source -> target.outputStream().use { source.copyTo(it) } }
            Uri.fromFile(target).toString()
        }
    }

    private fun String.sanitized(): String = replace(Regex("[^A-Za-z0-9._-]"), "_")

    private companion object {
        const val DIRECTORY = "payslips"
    }
}
