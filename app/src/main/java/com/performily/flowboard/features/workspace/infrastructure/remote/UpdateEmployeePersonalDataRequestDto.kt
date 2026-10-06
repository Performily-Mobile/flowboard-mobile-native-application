package com.performily.flowboard.features.workspace.infrastructure.remote

data class UpdateEmployeePersonalDataRequestDto(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val email: String,
    val phoneNumber: String,
    val street: String?,
    val district: String?,
    val province: String?,
    val department: String?
)
