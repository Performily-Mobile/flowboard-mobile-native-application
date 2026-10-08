package com.performily.flowboard.features.benefits.domain.entity

import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit

/** Tipo de beneficio del catálogo (US37). Solo los activos se pueden asignar. */
data class BenefitType(
    val id: Long,
    val name: String,
    val description: String?,
    val unit: BenefitUnit,
    val hasBalance: Boolean,
    val active: Boolean
)
