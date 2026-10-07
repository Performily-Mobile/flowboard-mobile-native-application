package com.performily.flowboard.features.request.domain.entity

/**
 * Datos mínimos de un colaborador que Request necesita mostrar: nombre, puesto y
 * jefe directo. Vienen de Workspace a través de la capa anticorrupción.
 *
 * @property jobDescription "Analista contable · Administración" (MA-48)
 */
data class RequestPerson(
    val id: Long,
    val name: String,
    val jobDescription: String,
    val directManagerId: Long?
)
