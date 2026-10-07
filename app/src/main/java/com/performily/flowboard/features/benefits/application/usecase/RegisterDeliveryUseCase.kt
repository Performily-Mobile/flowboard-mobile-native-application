package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.repository.BenefitAssignmentRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * MA-62 · Registra la entrega de un beneficio (US39). Solo una vez por asignación y
 * con fecha entre el inicio de la vigencia y hoy. Quien registra es el usuario actual de RR.HH.
 */
class RegisterDeliveryUseCase @Inject constructor(
    private val repository: BenefitAssignmentRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(
        assignment: BenefitAssignment,
        deliveredOn: LocalDate,
        notes: String?,
        today: LocalDate = LocalDate.now()
    ): Result<BenefitAssignment> {
        if (!assignment.canBeDelivered) {
            return Result.failure(IllegalStateException("Este beneficio ya fue entregado o anulado."))
        }
        if (deliveredOn.isAfter(today)) {
            return Result.failure(IllegalArgumentException("La fecha de entrega no puede ser futura."))
        }
        if (deliveredOn.isBefore(assignment.startDate)) {
            return Result.failure(
                IllegalArgumentException("La fecha de entrega no puede ser anterior al inicio de la vigencia.")
            )
        }
        return repository.registerDelivery(
            assignmentId = assignment.id,
            deliveredOn = deliveredOn,
            registeredById = currentEmployee.currentEmployeeId().value,
            notes = notes?.trim()?.takeIf { it.isNotEmpty() }
        )
    }
}
