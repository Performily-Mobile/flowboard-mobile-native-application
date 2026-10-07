package com.performily.flowboard.features.request.domain.entity

import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType


data class RequestType(
    val id: Long,
    val name: String,
    val description: String?,
    val requiresAttachment: Boolean,
    val balanceDeduction: BalanceDeduction,
    val active: Boolean,
    val fields: List<RequestField>
) {
    val deductsVacationDays: Boolean get() = balanceDeduction == BalanceDeduction.VACATION_DAYS

    val deductsBalance: Boolean get() = balanceDeduction != BalanceDeduction.NONE

    fun fieldLabel(key: String): String = fields.firstOrNull { it.key == key }?.label ?: key
}


data class RequestField(
    val id: Long,
    val key: String,
    val label: String,
    val dataType: FieldDataType,
    val required: Boolean,
    val displayOrder: Int
)

data class NewRequestField(
    val key: String,
    val label: String,
    val dataType: FieldDataType,
    val required: Boolean
)
