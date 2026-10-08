package com.performily.flowboard.features.request.infrastructure.remote


data class RequestTypeDto(
    val id: Long,
    val name: String,
    val description: String?,
    val requiresAttachment: Boolean,
    val balanceDeduction: String,
    val active: Boolean,
    val fields: List<RequestFieldDto>?
)

data class RequestFieldDto(
    val id: Long,
    val key: String,
    val label: String,
    val dataType: String,
    val required: Boolean,
    val displayOrder: Int
)


data class CreateRequestTypeRequestDto(
    val name: String,
    val description: String?,
    val requiresAttachment: Boolean,
    val balanceDeduction: String,
    val fields: List<CreateRequestFieldRequestDto>
)

data class CreateRequestFieldRequestDto(
    val key: String,
    val label: String,
    val dataType: String,
    val required: Boolean,
    val displayOrder: Int
)


data class RequestMessageDto(
    val message: String?
)
