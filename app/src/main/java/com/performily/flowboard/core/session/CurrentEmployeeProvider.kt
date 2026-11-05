package com.performily.flowboard.core.session

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Shared signed-in identity. No hardcoded employee ID or role. */
@Singleton
class CurrentEmployeeProvider @Inject constructor(private val iam: IamRepository) {
    fun currentEmployeeId(): EmployeeId = EmployeeId(requireNotNull(iam.session.value) { "No authenticated employee" }.employeeId)
    fun currentRole(): UserRole = if (iam.session.value?.isHr == true) UserRole.HUMAN_RESOURCES else UserRole.EMPLOYEE
}
