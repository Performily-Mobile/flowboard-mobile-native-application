package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.PayslipFileStorage
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile
import javax.inject.Inject

/**
 * Replaces the file of a payslip that was already uploaded.
 *
 * The payslip goes back to "Por publicar" so HR reviews the new file. A paid payslip cannot be
 * replaced. When the backend rejects the replacement, the stored copy is deleted.
 */
class ReplacePayslipFileUseCase @Inject constructor(
    private val storage: PayslipFileStorage,
    private val repository: PayslipRepository
) {

    suspend operator fun invoke(existing: Payslip, file: PayrollSystemFile): Result<Payslip> {
        if (!existing.canBeReplaced) {
            return Result.failure(IllegalStateException("La boleta de ${existing.period.label} ya está pagada y no se puede reemplazar."))
        }
        return storage.store(file).fold(
            onSuccess = { storageUrl ->
                repository.replaceFile(
                    payslipId = existing.id,
                    file = file.toFileReference(storageUrl),
                    issueDate = file.issueDate,
                    netAmount = file.netAmount
                ).onFailure { storage.delete(storageUrl) }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
