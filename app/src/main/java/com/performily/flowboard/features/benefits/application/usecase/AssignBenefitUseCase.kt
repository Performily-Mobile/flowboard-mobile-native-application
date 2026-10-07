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
 * MA-61 · Asigna un beneficio a un colaborador o a toda un área (US38).
 * Valida lo mismo que el backend: tipo activo, cantidad positiva con hasta 2
 * decimales (entera si se mide en unidades) y vigencia con fin posterior al inicio.
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
        if (error != null) return Result.failure(IllegalArgumentException(error))
        return repository.assign(benefitType.id, target, quantity, startDate, endDate)
    }

    companion object {
        /** Devuelve el mensaje del primer problema, o null si la asignación es válida. */
        fun validate(benefitType: BenefitType, quantity: BigDecimal, startDate: LocalDate, endDate: LocalDate): String? = when {
            !benefitType.active -> "Este beneficio está inactivo y no se puede asignar."
            quantity.signum() <= 0 -> "La cantidad debe ser mayor que cero."
            quantity.stripTrailingZeros().scale() > 2 -> "Usa como máximo 2 decimales."
            benefitType.unit == BenefitUnit.UNITS && quantity.stripTrailingZeros().scale() > 0 ->
                "Las unidades deben ser un número entero."
            endDate.isBefore(startDate) -> "La fecha final no puede ser anterior a la inicial."
            else -> null
        }
    }
}
