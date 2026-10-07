package com.performily.flowboard.features.request.infrastructure.mapper

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.entity.NewRequestField
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestField
import com.performily.flowboard.features.request.domain.entity.RequestFieldValue
import com.performily.flowboard.features.request.domain.entity.RequestHistoryEntry
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import com.performily.flowboard.features.request.infrastructure.remote.CreateRequestFieldRequestDto
import com.performily.flowboard.features.request.infrastructure.remote.CreateRequestTypeRequestDto
import com.performily.flowboard.features.request.infrastructure.remote.FieldValueDto
import com.performily.flowboard.features.request.infrastructure.remote.RequestAttachmentDto
import com.performily.flowboard.features.request.infrastructure.remote.RequestDto
import com.performily.flowboard.features.request.infrastructure.remote.RequestTypeDto
import com.performily.flowboard.features.request.infrastructure.remote.ResolveRequestRequestDto
import com.performily.flowboard.features.request.infrastructure.remote.ResubmitRequestRequestDto
import com.performily.flowboard.features.request.infrastructure.remote.SubmitRequestRequestDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object RequestMapper {

    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    // ---------- Tipos de solicitud ----------

    fun toDomain(dto: RequestTypeDto): RequestType = RequestType(
        id = dto.id,
        name = dto.name,
        description = dto.description?.takeIf { it.isNotBlank() },
        requiresAttachment = dto.requiresAttachment,
        balanceDeduction = RequestEnumMapper.balanceDeduction(dto.balanceDeduction),
        active = dto.active,
        fields = dto.fields.orEmpty()
            .map { field ->
                RequestField(
                    id = field.id,
                    key = field.key,
                    label = field.label,
                    dataType = RequestEnumMapper.dataType(field.dataType),
                    required = field.required,
                    displayOrder = field.displayOrder
                )
            }
            .sortedBy { it.displayOrder }
    )

    fun toCreateRequest(
        name: String,
        description: String?,
        requiresAttachment: Boolean,
        balanceDeduction: BalanceDeduction,
        fields: List<NewRequestField>
    ) = CreateRequestTypeRequestDto(
        name = name,
        description = description,
        requiresAttachment = requiresAttachment,
        balanceDeduction = balanceDeduction.name,
        fields = fields.mapIndexed { index, field ->
            CreateRequestFieldRequestDto(
                key = field.key,
                label = field.label,
                dataType = field.dataType.name,
                required = field.required,
                displayOrder = index + 1
            )
        }
    )

    // ---------- Solicitudes ----------

    fun toDomain(dto: RequestDto): Request {
        val startDate = RequestEnumMapper.date(dto.startDate)
        val endDate = RequestEnumMapper.date(dto.endDate)
        val period = if (startDate != null && endDate != null) {
            runCatching {
                RequestPeriod(
                    startDate = startDate,
                    endDate = endDate,
                    startTime = RequestEnumMapper.time(dto.startTime),
                    endTime = RequestEnumMapper.time(dto.endTime)
                )
            }.getOrNull()
        } else {
            null
        }
        return Request(
            id = dto.id,
            requesterId = dto.requesterId,
            requestTypeId = dto.requestTypeId,
            requestTypeName = dto.requestTypeName.orEmpty(),
            status = RequestEnumMapper.status(dto.status),
            period = period,
            requestedDays = dto.requestedDays,
            approverType = RequestEnumMapper.approverType(dto.approverType),
            approverEmployeeId = dto.approverEmployeeId,
            submittedAt = RequestEnumMapper.dateTime(dto.submittedAt) ?: LocalDateTime.MIN,
            fieldValues = dto.fieldValues.orEmpty()
                .filter { !it.value.isNullOrBlank() }
                .map { RequestFieldValue(key = it.key, value = it.value.orEmpty()) },
            attachments = dto.attachments.orEmpty().mapNotNull { attachment ->
                runCatching {
                    FileReference(
                        fileName = attachment.fileName,
                        contentType = attachment.contentType,
                        sizeInBytes = attachment.sizeInBytes ?: 1L,
                        storageUrl = attachment.storageUrl
                    )
                }.getOrNull()
            },
            history = dto.history.orEmpty()
                .map { entry ->
                    RequestHistoryEntry(
                        id = entry.id,
                        previousStatus = entry.previousStatus?.let(RequestEnumMapper::status),
                        newStatus = RequestEnumMapper.status(entry.newStatus),
                        actorId = entry.actorId,
                        actorName = null,
                        comment = entry.comment?.takeIf { it.isNotBlank() },
                        occurredAt = RequestEnumMapper.dateTime(entry.occurredAt) ?: LocalDateTime.MIN
                    )
                }
                .sortedWith(compareBy<RequestHistoryEntry> { it.occurredAt }.thenBy { it.id })
        )
    }

    fun toSubmitRequest(
        requesterId: Long,
        requestTypeId: Long,
        period: RequestPeriod?,
        fieldValues: List<RequestFieldValue>,
        attachments: List<FileReference>
    ) = SubmitRequestRequestDto(
        requesterId = requesterId,
        requestTypeId = requestTypeId,
        startDate = period?.startDate?.toString(),
        endDate = period?.endDate?.toString(),
        startTime = period?.startTime?.format(timeFormat),
        endTime = period?.endTime?.format(timeFormat),
        fieldValues = fieldValues.map(::toDto),
        attachments = attachments.map(::toDto)
    )

    fun toResolveRequest(actorId: Long, comment: String? = null) =
        ResolveRequestRequestDto(actorId = actorId, comment = comment)

    fun toResubmitRequest(
        actorId: Long,
        fieldValues: List<RequestFieldValue>,
        attachments: List<FileReference>,
        comment: String?
    ) = ResubmitRequestRequestDto(
        actorId = actorId,
        fieldValues = fieldValues.map(::toDto),
        attachments = attachments.map(::toDto),
        comment = comment
    )

    private fun toDto(value: RequestFieldValue) = FieldValueDto(key = value.key, value = value.value)

    private fun toDto(file: FileReference) = RequestAttachmentDto(
        fileName = file.fileName,
        contentType = file.contentType,
        sizeInBytes = file.sizeInBytes,
        storageUrl = file.storageUrl
    )
}
