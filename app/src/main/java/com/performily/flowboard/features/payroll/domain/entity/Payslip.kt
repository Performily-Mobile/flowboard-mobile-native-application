package com.performily.flowboard.features.payroll.domain.entity

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.payroll.domain.valueobject.PayPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentDetails
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.domain.valueobject.PublicationStatus
import java.time.LocalDate

/**
 * Aggregate root: boleta de un colaborador en un período de planilla.
 * Repositorio y estado de pago: nada se calcula, solo se guarda el neto que emite el sistema de planilla.
 */
data class Payslip(
    val id: Long,
    val employeeId: EmployeeId,
    val payrollPeriodId: Long,
    val period: PayPeriod,
    val file: FileReference,
    val issueDate: LocalDate,
    val netAmount: Money,
    val publicationStatus: PublicationStatus,
    val payment: PaymentDetails
) {
    val isPublished: Boolean get() = publicationStatus == PublicationStatus.PUBLISHED

    val isUnderReview: Boolean get() = publicationStatus == PublicationStatus.UNDER_REVIEW

    /** El estado de pago solo se registra en boletas publicadas. */
    val canUpdatePayment: Boolean get() = isPublished

    /** Una boleta pagada ya no se puede reemplazar. */
    val canBeReplaced: Boolean get() = payment.status != PaymentStatus.PAID
}
