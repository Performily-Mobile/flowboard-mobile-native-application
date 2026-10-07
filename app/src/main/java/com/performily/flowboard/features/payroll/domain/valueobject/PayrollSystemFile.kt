package com.performily.flowboard.features.payroll.domain.valueobject

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.Money
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Boleta en PDF emitida por el sistema de planilla de la organización, antes de guardarse.
 *
 * Flowboard no calcula nada: el colaborador, la fecha de emisión y el monto neto vienen
 * en el nombre del archivo que exporta el sistema de planilla (traducción del ACL):
 *
 *     <idColaborador>_<AAAA-MM-DD>_<neto>.pdf     por ejemplo: 12_2026-09-30_1420.00.pdf
 *
 * sourceUri es la referencia local del archivo elegido (por ejemplo, content://...).
 */
data class PayrollSystemFile(
    val sourceUri: String,
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long,
    val employeeId: EmployeeId,
    val issueDate: LocalDate,
    val netAmount: Money
) {
    companion object {
        const val PDF_CONTENT_TYPE = "application/pdf"
        const val MAX_SIZE_IN_BYTES: Long = 5L * 1024 * 1024
        const val NAME_FORMAT = "ID_AAAA-MM-DD_NETO.pdf"

        private val NAME_PATTERN = Regex("""^(\d+)_(\d{4}-\d{2}-\d{2})_(\d+(?:\.\d{1,2})?)\.pdf$""", RegexOption.IGNORE_CASE)

        /**
         * Valida el archivo elegido y lee sus datos. Los mensajes de error se muestran tal cual al usuario.
         */
        fun from(sourceUri: String, fileName: String, contentType: String, sizeInBytes: Long): Result<PayrollSystemFile> =
            runCatching {
                require(sourceUri.isNotBlank()) { "Selecciona un archivo." }
                require(contentType == PDF_CONTENT_TYPE && fileName.endsWith(".pdf", ignoreCase = true)) {
                    "$fileName no es un formato permitido. Sube archivos PDF."
                }
                require(sizeInBytes > 0) { "$fileName está vacío." }
                require(sizeInBytes <= MAX_SIZE_IN_BYTES) { "$fileName supera los 5 MB." }
                val match = NAME_PATTERN.matchEntire(fileName)
                    ?: throw IllegalArgumentException(
                        "$fileName no sigue el formato del sistema de planilla ($NAME_FORMAT)."
                    )
                val (employeeId, issueDate, netAmount) = match.destructured
                PayrollSystemFile(
                    sourceUri = sourceUri,
                    fileName = fileName,
                    contentType = contentType,
                    sizeInBytes = sizeInBytes,
                    employeeId = EmployeeId(employeeId.toLong()),
                    issueDate = parseDate(fileName, issueDate),
                    netAmount = Money(BigDecimal(netAmount))
                )
            }

        private fun parseDate(fileName: String, value: String): LocalDate = try {
            LocalDate.parse(value).also {
                require(!it.isAfter(LocalDate.now())) { "$fileName tiene una fecha de emisión futura." }
            }
        } catch (exception: DateTimeParseException) {
            throw IllegalArgumentException("$fileName tiene una fecha de emisión no válida.")
        }
    }
}
