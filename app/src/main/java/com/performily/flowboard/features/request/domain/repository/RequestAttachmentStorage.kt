package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.features.request.domain.valueobject.AttachmentUpload


interface RequestAttachmentStorage {
    suspend fun store(upload: AttachmentUpload): Result<String>
}
