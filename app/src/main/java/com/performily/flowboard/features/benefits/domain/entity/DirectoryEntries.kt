package com.performily.flowboard.features.benefits.domain.entity

/** Área a la que se puede asignar un beneficio. Viene de Workspace. */
data class AreaOption(
    val id: Long,
    val name: String,
    val activeEmployees: Long
)

/** Colaborador activo al que se puede asignar un beneficio. Viene de Workspace. */
data class EmployeeOption(
    val id: Long,
    val name: String,
    val areaName: String
)
