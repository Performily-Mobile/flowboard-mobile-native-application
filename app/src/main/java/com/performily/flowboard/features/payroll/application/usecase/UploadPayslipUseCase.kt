package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.PayslipFileStorage
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile
import javax.inject.Inject

/**
 * Uploads the payslip of an employee in a period.
 *
 * Rule: one payslip per employee and period. If it already exists, nothing is uploaded and
 * [PayslipUploadResult.Duplicate] is returned so HR decides whether to replace it. When the backend
 * rejects the payslip, the stored copy of the file is deleted so no orphan file remains.
 */
class UploadPayslipUseCase @Inject constructor(
    private val storage: PayslipFileStorage,
    private val repository: PayslipRepository
) {

    /**
     * Uploads the file for its employee unless that employee already has a payslip in the period.
     *
     * @param payrollPeriodId id of the payroll period
     * @param file the validated payslip file
     * @param payslipsInPeriod payslips already uploaded in the period
     * @return the uploaded payslip, or the existing one when it is a duplicate
     */
    suspend operator fun invoke(
        payrollPeriodId: Long,
        file: PayrollSystemFile,
        payslipsInPeriod: List<Payslip>
    ): Result<PayslipUploadResult> {
        payslipsInPeriod.firstOrNull { it.employeeId == file.employeeId }?.let { existing ->
            return Result.success(PayslipUploadResult.Duplicate(existing))
        }
        return storage.store(file).fold(
            onSuccess = { storageUrl ->
                repository.uploadPayslip(
                    employeeId = file.employeeId,
                    payrollPeriodId = payrollPeriodId,
                    file = file.toFileReference(storageUrl),
                    issueDate = file.issueDate,
                    netAmount = file.netAmount
                )
                    .onFailure { storage.delete(storageUrl) }
                    .map { PayslipUploadResult.Uploaded(it) }
            },
            onFailure = { Result.failure(it) }
        )
    }
}

internal fun PayrollSystemFile.toFileReference(storageUrl: String): FileReference =
    FileReference(
        fileName = fileName,
        contentType = contentType,
        sizeInBytes = sizeInBytes,
        storageUrl = storageUrl
    )
