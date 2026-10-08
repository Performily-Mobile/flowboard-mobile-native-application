package com.performily.flowboard.features.request.infrastructure.remote

data class RequestDto(
    val id: Long,
    val requesterId: Long,
    val requestTypeId: Long,
    val requestTypeName: String?,
    val status: String,
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val requestedDays: Int,
    val approverType: String,
    val approverEmployeeId: Long?,
    val submittedAt: String,
    val fieldValues: List<FieldValueDto>?,
    val attachments: List<RequestAttachmentDto>?,
    val history: List<RequestHistoryDto>?
)

data class FieldValueDto(
    val key: String,
    val value: String?
)

data class RequestAttachmentDto(
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long?,
    val storageUrl: String
)

data class RequestHistoryDto(
    val id: Long,
    val previousStatus: String?,
    val newStatus: String,
    val actorId: Long?,
    val comment: String?,
    val occurredAt: String
)

data class SubmitRequestRequestDto(
    val requesterId: Long,
    val requestTypeId: Long,
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val fieldValues: List<FieldValueDto>,
    val attachments: List<RequestAttachmentDto>
)


data class ResolveRequestRequestDto(
    val actorId: Long,
    val comment: String?
)


data class ResubmitRequestRequestDto(
    val actorId: Long,
    val fieldValues: List<FieldValueDto>,
    val attachments: List<RequestAttachmentDto>,
    val comment: String?
)
