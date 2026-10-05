package com.performily.flowboard.features.workspace.infrastructure.remote

data class EmployeeDocumentDto(
    val id: Long,
    val documentType: String,
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long,
    val storageUrl: String,
    val uploadedAt: String
)
