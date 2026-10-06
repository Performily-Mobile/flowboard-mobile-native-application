package com.performily.flowboard.features.attendance.domain.valueobject

enum class AttendanceStatus {
    ON_TIME,
    LATE,
    ABSENT,
    JUSTIFIED,
    INCOMPLETE
}

fun AttendanceStatus.label(): String = when (this) {
    AttendanceStatus.ON_TIME -> "Puntual"
    AttendanceStatus.LATE -> "Tardanza"
    AttendanceStatus.ABSENT -> "Inasistencia"
    AttendanceStatus.JUSTIFIED -> "Justificada"
    AttendanceStatus.INCOMPLETE -> "Incompleto"
}
