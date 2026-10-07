package com.performily.flowboard.features.payroll.domain.entity

import com.performily.flowboard.features.payroll.domain.valueobject.PayPeriod
import java.time.LocalDate

/** Mes de planilla con su fecha programada de pago. Hay un solo PayrollPeriod por PayPeriod. */
data class PayrollPeriod(
    val id: Long,
    val period: PayPeriod,
    val scheduledPaymentDate: LocalDate
)
