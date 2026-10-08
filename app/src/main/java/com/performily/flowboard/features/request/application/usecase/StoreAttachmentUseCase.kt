package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.repository.RequestAttachmentStorage
import com.performily.flowboard.features.request.domain.valueobject.AttachmentUpload
import javax.inject.Inject


class StoreAttachmentUseCase @Inject constructor(
    private val storage: RequestAttachmentStorage
) {
    suspend operator fun invoke(
        sourceUri: String,
        fileName: String,
        contentType: String,
        sizeInBytes: Long
    ): Result<FileReference> {
        val upload = runCatching { AttachmentUpload(sourceUri, fileName, contentType, sizeInBytes) }
            .getOrElse { return Result.failure(it) }
        return storage.store(upload).mapCatching { url ->
            FileReference(
                fileName = upload.fileName,
                contentType = upload.contentType,
                sizeInBytes = upload.sizeInBytes,
                storageUrl = url
            )
        }
    }
}
