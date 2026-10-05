package com.performily.flowboard.features.workspace.infrastructure.remote

data class RegisterEmployeeRequestDto(
    val firstName: String,
    val lastName: String,
    val identityDocumentType: String,
    val identityDocumentNumber: String,
    val birthDate: String,
    val email: String,
    val phoneNumber: String,
    val street: String?,
    val district: String?,
    val province: String?,
    val department: String?,
    val contractType: String,
    val hireDate: String,
    val contractEndDate: String?,
    val areaId: Long,
    val positionId: Long,
    val directManagerId: Long?
)
