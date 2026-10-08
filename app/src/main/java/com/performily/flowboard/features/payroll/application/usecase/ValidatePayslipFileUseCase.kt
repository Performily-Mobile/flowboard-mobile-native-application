package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile
import javax.inject.Inject

/**
 * Validates a picked file (PDF only, up to 5 MB, name given by the payroll system) and reads its data.
 *
 * The error messages are shown as they are to the user.
 */
class ValidatePayslipFileUseCase @Inject constructor() {

    /**
     * Validates the file and reads the employee, issue date and net amount from its name.
     *
     * @param sourceUri local reference of the picked file, for example a content URI
     * @param fileName name of the file
     * @param contentType content type reported by the provider
     * @param sizeInBytes size of the file in bytes
     * @return the payslip file, or a failure with a message for the user
     */
    operator fun invoke(
        sourceUri: String,
        fileName: String,
        contentType: String,
        sizeInBytes: Long
    ): Result<PayrollSystemFile> =
        PayrollSystemFile.from(sourceUri, fileName, contentType, sizeInBytes)
}
