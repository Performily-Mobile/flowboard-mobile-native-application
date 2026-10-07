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
 * Temporary payslip storage, following the same approach as Workspace.
 *
 * Copies the PDF to the internal storage of the app and returns its local URI. When the real
 * storage is defined, another implementation of [PayslipFileStorage] is created and the
 * `@Binds` in `PayrollRepositoryModule` is changed.
 */
class LocalPayslipFileStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PayslipFileStorage {

    /**
     * Copies the picked file to the internal storage.
     *
     * The size reported by the picker may not be the real one, so the copied file is validated
     * (not empty and not larger than the maximum size) and removed if it is not valid.
     *
     * @param file the validated payslip file to store
     * @return the local URI of the copy, or a failure with a message for the user
     */
    override suspend fun store(file: PayrollSystemFile): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
            val target = File(directory, "${System.currentTimeMillis()}_${file.fileName.sanitized()}")
            try {
                val input = context.contentResolver.openInputStream(Uri.parse(file.sourceUri))
                    ?: error("No se pudo leer ${file.fileName}.")
                input.use { source -> target.outputStream().use { source.copyTo(it) } }
                val written = target.length()
                check(written > 0L) { "${file.fileName} está vacío." }
                check(written <= PayrollSystemFile.MAX_SIZE_IN_BYTES) { "${file.fileName} supera los 5 MB." }
            } catch (exception: Exception) {
                target.delete()
                throw exception
            }
            Uri.fromFile(target).toString()
        }
    }

    /**
     * Deletes a copy created by [store], only when it is inside the payslips directory of the app.
     *
     * @param storageUrl local URI returned by [store]
     */
    override suspend fun delete(storageUrl: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val path = Uri.parse(storageUrl).path
                if (path != null) {
                    val stored = File(path)
                    val directory = File(context.filesDir, DIRECTORY)
                    if (stored.parentFile?.absolutePath == directory.absolutePath) stored.delete()
                }
            }
        }
    }

    private fun String.sanitized(): String = replace(Regex("[^A-Za-z0-9._-]"), "_")

    private companion object {
        const val DIRECTORY = "payslips"
    }
}
