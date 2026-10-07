package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.Office
import com.performily.flowboard.features.wellbeing.domain.repository.OfficeRepository
import javax.inject.Inject

/** MA-71 · Registro de un espacio de trabajo (US47). */
class CreateOfficeUseCase @Inject constructor(private val repository: OfficeRepository) {
    suspend operator fun invoke(
        name: String,
        area: String?,
        address: String,
        floor: String,
        reference: String?
    ): Result<Office> = repository.createOffice(
        name = name.trim(),
        area = area?.trim()?.takeIf { it.isNotEmpty() },
        address = address.trim(),
        floor = floor.trim(),
        reference = reference?.trim()?.takeIf { it.isNotEmpty() }
    )
}
