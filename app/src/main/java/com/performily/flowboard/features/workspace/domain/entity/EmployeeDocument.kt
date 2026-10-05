package com.performily.flowboard.features.workspace.domain.entity

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import java.time.LocalDateTime

data class EmployeeDocument(
    val id: Long,
    val documentType: DocumentType,
    val file: FileReference,
    val uploadedAt: LocalDateTime
)
