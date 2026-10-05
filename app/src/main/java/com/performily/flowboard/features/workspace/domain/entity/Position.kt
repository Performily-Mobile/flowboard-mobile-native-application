package com.performily.flowboard.features.workspace.domain.entity

import com.performily.flowboard.core.domain.Money

data class Position(
    val id: Long,
    val title: String,
    val areaId: Long,
    val areaName: String,
    val referenceSalary: Money,
    val active: Boolean
) {
    fun belongsTo(areaId: Long): Boolean = this.areaId == areaId
}
