package com.performily.flowboard.features.request.presentation.viewmodel

import com.performily.flowboard.core.network.ApiException

internal enum class RequestAction {
    LOAD,
    SUBMIT,
    CANCEL,
    RESUBMIT,
    RESOLVE,
    CREATE_TYPE,
    CHANGE_TYPE_STATUS,
    DELETE_TYPE
}


internal fun Throwable.requestMessage(action: RequestAction, fallback: String): String {
    val api = this as? ApiException ?: return message?.takeIf { it.isNotBlank() } ?: fallback
    val code = api.code.orEmpty()
    val text = api.message

    if (code.endsWith("CONFLICT")) {
        return when (action) {
            RequestAction.SUBMIT -> "Ya tienes una solicitud para esos días."
            RequestAction.CREATE_TYPE -> "Ya existe un tipo de solicitud con este nombre."
            else -> text
        }
    }
    return when {
        action == RequestAction.DELETE_TYPE && (code == "BUSINESS_RULE_VIOLATION" || text.contains("has requests", true)) ->
            "Este tipo tiene solicitudes asociadas y no se puede eliminar."
        text.contains("not ACTIVE", ignoreCase = true) || text.contains("is not active", ignoreCase = true) ->
            if (action == RequestAction.SUBMIT) "Este tipo de solicitud está inactivo." else "El colaborador no está activo."
        text.contains("own request", ignoreCase = true) -> "No puedes aprobar tu propia solicitud."
        text.contains("assigned approver", ignoreCase = true) -> "Solo el aprobador asignado puede resolver esta solicitud."
        text.contains("Only IN_PROGRESS", ignoreCase = true) || text.contains("already", ignoreCase = true) ->
            "La solicitud ya fue atendida. Actualiza la lista."
        text.contains("Only UNDER_REVIEW", ignoreCase = true) -> "La solicitud ya no está en revisión."
        text.contains("attachment", ignoreCase = true) -> "Adjunta el documento de sustento."
        text.contains("required", ignoreCase = true) && action == RequestAction.SUBMIT ->
            "Completa los campos obligatorios."
        text.contains("whole days", ignoreCase = true) -> "Este tipo de solicitud necesita un periodo de días completos."
        text.contains("Not enough vacation days", ignoreCase = true) -> "El colaborador no tiene días suficientes."
        code.endsWith("NOT_FOUND") && action == RequestAction.LOAD -> "No se encontró la solicitud."
        else -> text.takeIf { it.isNotBlank() } ?: fallback
    }
}
