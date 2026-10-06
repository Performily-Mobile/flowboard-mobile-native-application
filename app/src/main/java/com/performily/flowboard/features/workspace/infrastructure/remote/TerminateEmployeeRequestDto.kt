package com.performily.flowboard.features.workspace.infrastructure.remote

data class TerminateEmployeeRequestDto(
    val reason: String,
    val terminationDate: String
)
