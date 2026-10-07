package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.features.request.domain.entity.VacationAvailability

/**
 * Lo que Request necesita de Benefits: los días de vacaciones disponibles, para
 * avisar si no alcanza el saldo (MA-40) y mostrar el saldo tras aprobar (MA-41, MA-48).
 */
interface VacationBalanceProvider {
    suspend fun getMyAvailability(): Result<VacationAvailability>
    suspend fun getAvailability(employeeId: Long): Result<VacationAvailability>
}
