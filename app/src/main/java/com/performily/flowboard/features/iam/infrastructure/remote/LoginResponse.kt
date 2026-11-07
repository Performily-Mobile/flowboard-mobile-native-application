package com.performily.flowboard.features.iam.infrastructure.remote

data class LoginResponse(val token: String, val role: String, val employeeId: Long, val mustChangePassword: Boolean = false)
