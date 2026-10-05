package com.performily.flowboard.features.workspace.domain.entity

import com.performily.flowboard.features.workspace.domain.valueobject.AssignmentChangeType
import java.time.LocalDate

data class JobAssignment(
    val id: Long,
    val areaId: Long,
    val areaName: String,
    val positionId: Long,
    val positionTitle: String,
    val changeType: AssignmentChangeType,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val current: Boolean
)
