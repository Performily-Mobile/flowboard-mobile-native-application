package com.performily.flowboard.features.payroll.infrastructure.mapper

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollEmployee
import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollAreaDto
import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollEmployeeDto

/** Traduce el modelo de Workspace al modelo que necesita Payroll (ACL). */
object PayrollEmployeeMapper {

    fun toDomain(dto: PayrollEmployeeDto): PayrollEmployee {
        val firstName = dto.firstName?.trim().orEmpty()
        val lastName = dto.lastName?.trim().orEmpty()
        return if (firstName.isEmpty() && lastName.isEmpty()) {
            PayrollEmployee(EmployeeId(dto.id), firstName = dto.fullName?.trim().orEmpty(), lastName = "", areaId = dto.areaId)
        } else {
            PayrollEmployee(EmployeeId(dto.id), firstName = firstName, lastName = lastName, areaId = dto.areaId)
        }
    }

    fun toDomain(dto: PayrollAreaDto): PayrollArea =
        PayrollArea(id = dto.id, name = dto.name, active = dto.active)
}
