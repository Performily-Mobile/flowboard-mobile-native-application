package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.repository.DocumentFileStorage
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentUpload
import javax.inject.Inject

/**
 * Sube el archivo al almacenamiento y registra sus metadatos en el expediente.
 */
class AttachEmployeeDocumentUseCase @Inject constructor(
    private val storage: DocumentFileStorage,
    private val repository: EmployeeRepository
) {

    suspend operator fun invoke(
        employeeId: EmployeeId,
        documentType: DocumentType,
        upload: DocumentUpload
    ): Result<EmployeeDocument> {
        return storage.store(upload).fold(
            onSuccess = { storageUrl ->
                val file = FileReference(
                    fileName = upload.fileName,
                    contentType = upload.contentType,
                    sizeInBytes = upload.sizeInBytes,
                    storageUrl = storageUrl
                )
                repository.attachDocument(employeeId, documentType, file)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
