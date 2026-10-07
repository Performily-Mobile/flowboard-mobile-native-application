package com.performily.flowboard.features.wellbeing.infrastructure.remote

data class CreateOfficeRequestDto(
    val name: String,
    val area: String?,
    val address: String,
    val floor: String,
    val reference: String?
)
