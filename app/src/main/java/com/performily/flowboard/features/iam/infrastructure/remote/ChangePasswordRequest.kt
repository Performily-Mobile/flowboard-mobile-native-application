package com.performily.flowboard.features.iam.infrastructure.remote

data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
