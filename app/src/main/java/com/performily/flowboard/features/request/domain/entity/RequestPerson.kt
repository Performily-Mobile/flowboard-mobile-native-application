package com.performily.flowboard.features.request.domain.entity


data class RequestPerson(
    val id: Long,
    val name: String,
    val jobDescription: String,
    val directManagerId: Long?
)
