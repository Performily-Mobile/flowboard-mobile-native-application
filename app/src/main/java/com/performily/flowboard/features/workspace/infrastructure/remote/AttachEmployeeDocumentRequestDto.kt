package com.performily.flowboard.features.workspace.infrastructure.remote

data class AttachEmployeeDocumentRequestDto(
    val documentType: String,
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long,
    val storageUrl: String
)
