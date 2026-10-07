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
 * Aggregate root: payslip of an employee in a payroll period.
 *
 * Repository and payment status: nothing is calculated, only the net amount issued by the payroll
 * system is stored.
 *
 * @property employeeName name of the employee sent by the backend, used when Workspace does not respond
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
    val payment: PaymentDetails,
    val employeeName: String? = null
) {
    val isPublished: Boolean get() = publicationStatus == PublicationStatus.PUBLISHED

    val isUnderReview: Boolean get() = publicationStatus == PublicationStatus.UNDER_REVIEW

    /**
     * Whether the payment status can be registered; it is only registered on published payslips.
     */
    val canUpdatePayment: Boolean get() = isPublished

    /**
     * Whether the file can be replaced; a paid payslip can no longer be replaced.
     */
    val canBeReplaced: Boolean get() = payment.status != PaymentStatus.PAID
}
