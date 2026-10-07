package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.AssignmentBatch
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.repository.BenefitAssignmentRepository
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentTarget
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

/**
 * Assignment form field.
 *
 * Identifies the part of the assignment form a validation error belongs to.
 */
enum class AssignmentField {
    TYPE,
    QUANTITY,
    DATES
}

data class AssignmentValidationError(val field: AssignmentField, val message: String)

/**
 * Assigns a benefit to one employee or to a whole area (MA-61, US38).
 *
 * Mirrors the backend validation: active type, positive quantity with up to 2 decimals and
 * 10 integer digits (a whole number when measured in units) and an end date not before the start date.
 */
class AssignBenefitUseCase @Inject constructor(private val repository: BenefitAssignmentRepository) {

    suspend operator fun invoke(
        benefitType: BenefitType,
        target: AssignmentTarget,
        quantity: BigDecimal,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<AssignmentBatch> {
        val error = validate(benefitType, quantity, startDate, endDate)
        if (error != null) return Result.failure(IllegalArgumentException(error.message))
        return repository.assign(benefitType.id, target, quantity, startDate, endDate)
    }

    companion object {
        private const val MAX_DECIMALS = 2
        private const val MAX_INTEGER_DIGITS = 10

        /**
         * Validates an assignment request.
         *
         * @return the first problem found together with its field, or null when the assignment is valid.
         */
        fun validate(
            benefitType: BenefitType,
            quantity: BigDecimal,
            startDate: LocalDate,
            endDate: LocalDate
        ): AssignmentValidationError? {
            if (!benefitType.active) {
                return AssignmentValidationError(AssignmentField.TYPE, "Este beneficio está inactivo y no se puede asignar.")
            }
            if (quantity.signum() <= 0) {
                return AssignmentValidationError(AssignmentField.QUANTITY, "La cantidad debe ser mayor que cero.")
            }
            val normalized = quantity.stripTrailingZeros()
            if (normalized.scale() > MAX_DECIMALS) {
                return AssignmentValidationError(AssignmentField.QUANTITY, "Usa como máximo 2 decimales.")
            }
            if (normalized.precision() - normalized.scale() > MAX_INTEGER_DIGITS) {
                return AssignmentValidationError(AssignmentField.QUANTITY, "La cantidad admite hasta 10 dígitos enteros.")
            }
            if (benefitType.unit == BenefitUnit.UNITS && normalized.scale() > 0) {
                return AssignmentValidationError(AssignmentField.QUANTITY, "Las unidades deben ser un número entero.")
            }
            if (endDate.isBefore(startDate)) {
                return AssignmentValidationError(AssignmentField.DATES, "La fecha final no puede ser anterior a la inicial.")
            }
            return null
        }
    }
}
