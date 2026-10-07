package com.performily.flowboard.features.request.domain.entity

import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType

/**
 * Tipo de solicitud del catálogo (MA-54) con los campos de su formulario.
 * Solo los activos aparecen al crear una solicitud (MA-38).
 */
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

    /** Un tipo que descuenta saldo necesita un periodo de días completos (regla del backend). */
    val deductsBalance: Boolean get() = balanceDeduction != BalanceDeduction.NONE

    fun fieldLabel(key: String): String = fields.firstOrNull { it.key == key }?.label ?: key
}

/** Campo del formulario dinámico de un tipo de solicitud. */
data class RequestField(
    val id: Long,
    val key: String,
    val label: String,
    val dataType: FieldDataType,
    val required: Boolean,
    val displayOrder: Int
)

/** Campo que se arma en "Nuevo tipo de solicitud" antes de guardarlo (MA-55). */
data class NewRequestField(
    val key: String,
    val label: String,
    val dataType: FieldDataType,
    val required: Boolean
)
