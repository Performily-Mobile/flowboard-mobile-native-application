package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus
import com.performily.flowboard.features.wellbeing.domain.repository.OfficeRepository
import javax.inject.Inject

/** MA-72 / MA-81 · Indicadores ambientales de un espacio (US50). */
class GetOfficeStatusUseCase @Inject constructor(private val repository: OfficeRepository) {
    suspend operator fun invoke(officeId: Long): Result<OfficeStatus> = repository.getOfficeStatus(officeId)
}
