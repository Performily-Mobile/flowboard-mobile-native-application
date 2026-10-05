package com.performily.flowboard.features.workspace.infrastructure.remote

data class EmployeeDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String?,
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
    val status: String,
    val terminationReason: String?,
    val terminationDate: String?,
    val areaId: Long,
    val areaName: String?,
    val positionId: Long,
    val positionTitle: String?,
    val directManagerId: Long?,
    val updatedAt: String?
)
