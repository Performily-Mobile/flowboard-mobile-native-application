package com.performily.flowboard.features.wellbeing.domain.repository

import com.performily.flowboard.features.wellbeing.domain.entity.Office
import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus

interface OfficeRepository {
    suspend fun getOffices(): Result<List<OfficeStatus>>
    suspend fun getOfficeStatus(officeId: Long): Result<OfficeStatus>
    suspend fun createOffice(name: String, area: String?, address: String, floor: String, reference: String?): Result<Office>
}
