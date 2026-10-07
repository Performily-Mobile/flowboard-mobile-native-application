package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus
import com.performily.flowboard.features.wellbeing.domain.repository.OfficeRepository
import javax.inject.Inject

/** MA-70 · Espacios de trabajo con su indicador general, ordenados por nombre (US47, US50). */
class GetOfficesUseCase @Inject constructor(private val repository: OfficeRepository) {
    suspend operator fun invoke(): Result<List<OfficeStatus>> =
        repository.getOffices().map { offices -> offices.sortedBy { it.office.name.lowercase() } }
}
