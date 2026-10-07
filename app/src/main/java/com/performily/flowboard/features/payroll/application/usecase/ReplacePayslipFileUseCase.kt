package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.PayslipFileStorage
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile
import javax.inject.Inject

/**
 * Reemplaza el archivo de una boleta ya cargada. La boleta vuelve a "Por publicar"
 * para que RR.HH. revise el archivo nuevo. Una boleta pagada no se puede reemplazar.
 */
class ReplacePayslipFileUseCase @Inject constructor(
    private val storage: PayslipFileStorage,
    private val repository: PayslipRepository
) {

    suspend operator fun invoke(existing: Payslip, file: PayrollSystemFile): Result<Payslip> {
        if (!existing.canBeReplaced) {
            return Result.failure(IllegalStateException("La boleta de ${file.fileName} ya está pagada y no se puede reemplazar."))
        }
        return storage.store(file).fold(
            onSuccess = { storageUrl ->
                repository.replaceFile(
                    payslipId = existing.id,
                    file = file.toFileReference(storageUrl),
                    issueDate = file.issueDate,
                    netAmount = file.netAmount
                )
            },
            onFailure = { Result.failure(it) }
        )
    }
}
