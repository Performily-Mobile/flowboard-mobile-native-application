package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.repository.VacationBalanceRepository
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Adjustment form field.
 *
 * Identifies the form field a validation error belongs to.
 */
enum class AdjustmentField {
    DAYS,
    REASON
}

/**
 * Adjustment validation error.
 *
 * Carries the affected field so the screen can flag it where it belongs.
 *
 * @property field the form field that failed validation.
 */
class AdjustmentValidationException(val field: AdjustmentField, message: String) : IllegalArgumentException(message)

/**
 * Manual vacation balance adjustment (MA-63, US42).
 *
 * Requires a reason, records who performed it and can never leave the balance negative.
 * Validation failures are reported as [AdjustmentValidationException] with a Spanish message.
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
        val normalized = days.stripTrailingZeros()
        val error: AdjustmentValidationException? = when {
            days.signum() <= 0 ->
                AdjustmentValidationException(AdjustmentField.DAYS, "Ingresa una cantidad mayor que cero.")
            normalized.scale() > MAX_DECIMALS ->
                AdjustmentValidationException(AdjustmentField.DAYS, "Usa como máximo 2 decimales.")
            normalized.precision() - normalized.scale() > MAX_INTEGER_DIGITS ->
                AdjustmentValidationException(AdjustmentField.DAYS, "La cantidad admite hasta 4 dígitos enteros.")
            cleanReason.isEmpty() ->
                AdjustmentValidationException(AdjustmentField.REASON, "Ingresa el motivo del ajuste.")
            cleanReason.length > MAX_REASON ->
                AdjustmentValidationException(AdjustmentField.REASON, "El motivo admite hasta $MAX_REASON caracteres.")
            operation == AdjustmentOperation.DEDUCT && days > balance.availableDays ->
                AdjustmentValidationException(AdjustmentField.DAYS, "No puede descontar más de los días disponibles.")
            else -> null
        }
        if (error != null) return Result.failure(error)
        return repository.adjust(balance.employeeId, operation, days, cleanReason, currentEmployee.currentEmployeeId().value)
    }

    private companion object {
        const val MAX_REASON = 250
        const val MAX_DECIMALS = 2
        const val MAX_INTEGER_DIGITS = 4
    }
}
