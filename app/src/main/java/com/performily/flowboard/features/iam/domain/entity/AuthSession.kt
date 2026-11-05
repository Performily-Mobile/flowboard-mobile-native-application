package com.performily.flowboard.features.iam.domain.entity

data class AuthSession(val token: String, val role: String, val employeeId: Long, val mustChangePassword: Boolean, val username: String = "") {
    val isHr get() = role == "HR_STAFF"
}
