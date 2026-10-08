package com.performily.flowboard.features.benefits.domain.repository

import com.performily.flowboard.features.benefits.domain.entity.SyncedVacationBalance
import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import java.math.BigDecimal

interface VacationBalanceRepository {
    /** Saldo del propio colaborador. Sin conexión devuelve la última copia guardada (MA-57). */
    suspend fun getMyBalance(employeeId: Long): Result<SyncedVacationBalance>
    suspend fun getBalances(areaId: Long?): Result<List<VacationBalance>>
    suspend fun getBalance(employeeId: Long): Result<VacationBalance>
    suspend fun adjust(employeeId: Long, operation: AdjustmentOperation, days: BigDecimal, reason: String, authorId: Long): Result<VacationBalance>
}
