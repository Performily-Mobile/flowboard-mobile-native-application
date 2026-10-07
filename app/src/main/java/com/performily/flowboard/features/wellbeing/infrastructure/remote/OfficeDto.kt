package com.performily.flowboard.features.wellbeing.infrastructure.remote

/** Respuesta de POST /offices (OfficeResource del backend). */
data class OfficeDto(
    val id: Long,
    val name: String,
    val area: String?,
    val address: String,
    val floor: String,
    val reference: String?,
    val active: Boolean
)
