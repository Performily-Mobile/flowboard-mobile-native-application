package com.performily.flowboard.features.workspace.domain.entity

data class Area(
    val id: Long,
    val name: String,
    val description: String?,
    val active: Boolean,
    val activeEmployees: Long
) {
    val canBeDeactivated: Boolean get() = active && activeEmployees == 0L
}
