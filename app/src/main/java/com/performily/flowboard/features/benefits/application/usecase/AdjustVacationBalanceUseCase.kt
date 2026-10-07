package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.repository.VacationBalanceRepository
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import java.math.BigDecimal
import javax.inject.Inject

/**
 * MA-63 · Ajuste manual del saldo (US42). Necesita motivo, queda con el nombre de
 * quien lo hizo y no puede dejar el saldo en negativo.
 */
class AdjustVacationBalanceUseCase @Inject constructor(
    private val repository: VacationBalanceRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(
        balance: VacationBalance,
        operation: AdjustmentOperation,
        days: BigDecimal,
        reason: String
    ): Result<VacationBalance> {
        val cleanReason = reason.trim()
        return when {
            days.signum() <= 0 -> Result.failure(IllegalArgumentException("Ingresa una cantidad mayor que cero."))
            days.scale() > 2 -> Result.failure(IllegalArgumentException("Usa como máximo 2 decimales."))
            cleanReason.isEmpty() -> Result.failure(IllegalArgumentException("Ingresa el motivo del ajuste."))
            cleanReason.length > MAX_REASON -> Result.failure(IllegalArgumentException("El motivo admite hasta  caracteres."))
            operation == AdjustmentOperation.DEDUCT && days > balance.availableDays ->
                Result.failure(IllegalArgumentException("No puede descontar más de los días disponibles."))
            else -> repository.adjust(balance.employeeId, operation, days, cleanReason, currentEmployee.currentEmployeeId().value)
        }
    }

    private companion object {
        const val MAX_REASON = 250
    }
}
