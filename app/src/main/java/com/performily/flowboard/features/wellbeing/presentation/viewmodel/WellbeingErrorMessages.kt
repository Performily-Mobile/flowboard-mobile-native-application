package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import com.performily.flowboard.core.network.ApiException

/**
 * Translates the Wellbeing backend error codes into the user-facing Spanish texts.
 *
 * For VALIDATION_ERROR and BUSINESS_RULE_VIOLATION the backend sends the concrete reason in
 * [ApiException.details]; when no dedicated text exists that reason is used and, as a last
 * resort, the given fallback.
 *
 * @param fallback text shown when the error carries no usable message
 * @return the message to display to the user
 */
internal fun Throwable.wellbeingMessage(fallback: String): String {
    val api = this as? ApiException
    val details = api?.details?.takeIf { it.isNotBlank() }
    return when (api?.code) {
        "OFFICE_CONFLICT" -> "Ya existe un espacio con este nombre."
        "OFFICE_NOT_FOUND" -> "Este espacio ya no existe."
        "DEVICE_NOT_FOUND" -> "Ese código no existe en el inventario."
        "DEVICE_CONFLICT" -> deviceConflictMessage(details)
        "BUSINESS_RULE_VIOLATION" -> businessRuleMessage(details) ?: fallback
        "VALIDATION_ERROR" -> details ?: fallback
        else -> message?.takeIf { it.isNotBlank() } ?: fallback
    }
}

/**
 * Builds the message for a device that is already linked.
 *
 * The backend details look like "Device X is already linked to <office>"; the office name is
 * included in the message when it can be extracted.
 *
 * @param details backend reason, if any
 */
private fun deviceConflictMessage(details: String?): String {
    val office = details?.substringAfter("is already linked to ", "")?.trim().orEmpty()
    return if (office.isNotEmpty()) {
        "Este dispositivo ya está vinculado a $office."
    } else {
        "Este dispositivo ya está vinculado a otro espacio."
    }
}

/**
 * Translates the known business rule reasons of the backend.
 *
 * @param details backend reason, if any
 * @return the Spanish text, the raw reason when it is unknown, or null when there is no reason
 */
private fun businessRuleMessage(details: String?): String? = when {
    details == null -> null
    details.contains("active offices") -> "Solo se pueden vincular dispositivos a espacios activos."
    details.contains("not linked to this office") -> "Ese dispositivo no está vinculado a este espacio."
    else -> details
}
