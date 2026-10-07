package com.performily.flowboard.features.benefits.infrastructure.remote

data class BenefitTypeDto(
    val id: Long,
    val name: String,
    val description: String?,
    val unit: String,
    val hasBalance: Boolean,
    val active: Boolean
)

data class CreateBenefitTypeRequestDto(
    val name: String,
    val description: String?,
    val hasBalance: Boolean,
    val unit: String
)
