package com.performily.flowboard.features.payroll.infrastructure.mapper

import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PayPeriod
import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollPeriodDto
import java.time.LocalDate

object PayrollPeriodMapper {

    fun toDomain(dto: PayrollPeriodDto): PayrollPeriod = PayrollPeriod(
        id = dto.id,
        period = PayPeriod(dto.year, dto.month),
        scheduledPaymentDate = LocalDate.parse(dto.scheduledPaymentDate)
    )
}
