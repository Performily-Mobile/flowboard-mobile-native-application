package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import com.performily.flowboard.core.network.ApiException

/**
 * Traduce los códigos de error del backend de Wellbeing a los textos del prototipo.
 * Si el código no tiene un texto propio, se usa el mensaje que envía el backend.
 */
internal fun Throwable.wellbeingMessage(fallback: String): String = when ((this as? ApiException)?.code) {
    "OFFICE_CONFLICT" -> "Ya existe un espacio con este nombre."
    "OFFICE_NOT_FOUND" -> "Este espacio ya no existe."
    "DEVICE_NOT_FOUND" -> "Ese código no existe en el inventario."
    "DEVICE_CONFLICT" -> "Este dispositivo ya está vinculado a otro espacio."
    else -> message?.takeIf { it.isNotBlank() } ?: fallback
}
