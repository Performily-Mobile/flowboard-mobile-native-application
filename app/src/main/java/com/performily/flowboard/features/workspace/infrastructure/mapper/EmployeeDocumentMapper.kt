package com.performily.flowboard.features.workspace.infrastructure.mapper

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import com.performily.flowboard.features.workspace.infrastructure.remote.AttachEmployeeDocumentRequestDto
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeDocumentDto
import java.time.LocalDateTime

object EmployeeDocumentMapper {

    fun toDomain(dto: EmployeeDocumentDto): EmployeeDocument {
        return EmployeeDocument(
            id = dto.id,
            documentType = DocumentType.valueOf(dto.documentType),
            file = FileReference(
                fileName = dto.fileName,
                contentType = dto.contentType,
                sizeInBytes = dto.sizeInBytes,
                storageUrl = dto.storageUrl
            ),
            uploadedAt = LocalDateTime.parse(dto.uploadedAt)
        )
    }

    fun toAttachRequest(documentType: DocumentType, file: FileReference): AttachEmployeeDocumentRequestDto =
        AttachEmployeeDocumentRequestDto(
            documentType = documentType.name,
            fileName = file.fileName,
            contentType = file.contentType,
            sizeInBytes = file.sizeInBytes,
            storageUrl = file.storageUrl
        )
}
