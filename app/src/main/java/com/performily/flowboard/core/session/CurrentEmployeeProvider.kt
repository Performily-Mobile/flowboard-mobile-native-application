package com.performily.flowboard.core.session

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.iam.domain.entity.AuthSession
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Shared signed-in identity. No hardcoded employee ID or role. */
@Singleton
class CurrentEmployeeProvider @Inject constructor(private val iam: IamRepository) {
    @Volatile
    private var lastSession: AuthSession? = null

    fun currentEmployeeId(): EmployeeId = EmployeeId(session().employeeId)
    fun currentRole(): UserRole =
        if ((iam.session.value ?: lastSession)?.isHr == true) UserRole.HUMAN_RESOURCES else UserRole.EMPLOYEE

    /**
     * Signed-in session, or the last one while the app returns to the login screen,
     * so a load that was already running does not crash the app.
     */
    private fun session(): AuthSession =
        (iam.session.value ?: lastSession)?.also { lastSession = it }
            ?: error("No authenticated employee")
}
