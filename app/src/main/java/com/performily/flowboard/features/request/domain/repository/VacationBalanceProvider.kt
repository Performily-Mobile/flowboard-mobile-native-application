package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.features.request.domain.entity.VacationAvailability


interface VacationBalanceProvider {
    suspend fun getMyAvailability(): Result<VacationAvailability>
    suspend fun getAvailability(employeeId: Long): Result<VacationAvailability>
}
