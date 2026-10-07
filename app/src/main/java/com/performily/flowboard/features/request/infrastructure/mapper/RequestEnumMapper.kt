package com.performily.flowboard.features.request.infrastructure.mapper

import com.performily.flowboard.features.request.domain.valueobject.ApproverType
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


internal object RequestEnumMapper {

    fun status(value: String): RequestStatus =
        RequestStatus.entries.firstOrNull { it.name == value } ?: RequestStatus.IN_PROGRESS

    fun approverType(value: String): ApproverType =
        ApproverType.entries.firstOrNull { it.name == value } ?: ApproverType.DIRECT_MANAGER

    fun balanceDeduction(value: String): BalanceDeduction =
        BalanceDeduction.entries.firstOrNull { it.name == value } ?: BalanceDeduction.NONE

    fun dataType(value: String): FieldDataType =
        FieldDataType.entries.firstOrNull { it.name == value } ?: FieldDataType.TEXT

    fun date(value: String?): LocalDate? = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    
    fun time(value: String?): LocalTime? = value?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

    fun dateTime(value: String?): LocalDateTime? = value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
}
