package com.performily.flowboard.features.payroll.domain.valueobject

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.Money
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * PDF payslip issued by the payroll system of the organization, before it is stored.
 *
 * Flowboard does not calculate anything: the employee, the issue date and the net amount come in
 * the name of the file exported by the payroll system (translation of the ACL):
 *
 *     <employeeId>_<yyyy-MM-dd>_<netAmount>.pdf     for example: 12_2026-09-30_1420.00.pdf
 *
 * @property sourceUri local reference of the picked file, for example a content URI
 * @property fileName name of the file
 * @property contentType content type of the file
 * @property sizeInBytes size of the file in bytes
 * @property employeeId employee the payslip belongs to
 * @property issueDate date the payslip was issued
 * @property netAmount net amount issued by the payroll system
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
         * Validates the picked file and reads its data.
         *
         * The error messages are shown as they are to the user.
         *
         * @param sourceUri local reference of the picked file
         * @param fileName name of the file
         * @param contentType content type reported by the provider
         * @param sizeInBytes size of the file in bytes
         * @return the payslip file, or a failure with a message for the user
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
                val (employeeIdText, issueDate, netAmount) = match.destructured
                val employeeNumber = employeeIdText.toLongOrNull()
                    ?: throw IllegalArgumentException("$fileName tiene un identificador de colaborador no válido.")
                PayrollSystemFile(
                    sourceUri = sourceUri,
                    fileName = fileName,
                    contentType = contentType,
                    sizeInBytes = sizeInBytes,
                    employeeId = EmployeeId(employeeNumber),
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
