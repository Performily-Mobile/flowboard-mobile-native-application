package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.PayslipFileStorage
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile
import javax.inject.Inject

/**
 * Carga la boleta de un colaborador en un período.
 * Regla: una boleta por colaborador y período. Si ya existe, no se sube nada y se devuelve
 * Duplicate para que RR.HH. decida si la reemplaza.
 */
class UploadPayslipUseCase @Inject constructor(
    private val storage: PayslipFileStorage,
    private val repository: PayslipRepository
) {

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
                ).map { PayslipUploadResult.Uploaded(it) }
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
