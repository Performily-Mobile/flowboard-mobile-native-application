package com.performily.flowboard.features.benefits.domain.valueobject

/** A quién se asigna un beneficio (MA-61): a un colaborador o a todos los activos de un área. */
sealed interface AssignmentTarget {
    data class Employee(val employeeId: Long) : AssignmentTarget
    data class Area(val areaId: Long) : AssignmentTarget
}
