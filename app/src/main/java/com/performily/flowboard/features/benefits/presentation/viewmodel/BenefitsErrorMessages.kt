package com.performily.flowboard.features.benefits.presentation.viewmodel

import com.performily.flowboard.core.network.ApiException

/** Lo que se estaba haciendo cuando falló, para elegir el texto correcto. */
internal enum class BenefitsAction {
    LOAD,
    CREATE_TYPE,
    CHANGE_TYPE_STATUS,
    ASSIGN_TO_EMPLOYEE,
    ASSIGN_TO_AREA,
    DELIVER,
    ADJUST
}

/**
 * Traduce los errores del backend de Benefits a los textos del prototipo.
 * El backend responde en inglés y con códigos como BENEFIT_TYPE_CONFLICT o
 * BUSINESS_RULE_VIOLATION; aquí se reconocen por el código y por el mensaje.
 */
internal fun Throwable.benefitsMessage(action: BenefitsAction, fallback: String): String {
    val api = this as? ApiException ?: return message?.takeIf { it.isNotBlank() } ?: fallback
    val code = api.code.orEmpty()
    val text = api.message

    if (code.endsWith("CONFLICT")) {
        return when (action) {
            BenefitsAction.CREATE_TYPE -> "Ya existe un beneficio con este nombre."
            BenefitsAction.ASSIGN_TO_EMPLOYEE -> "El colaborador ya tiene este beneficio en un periodo que se cruza."
            BenefitsAction.ASSIGN_TO_AREA -> "Todos los colaboradores activos del área ya tienen este beneficio en el periodo."
            else -> text
        }
    }
    return when {
        text.contains("is not active", ignoreCase = true) -> "Este beneficio está inactivo y no se puede asignar."
        text.contains("ACTIVE employees", ignoreCase = true) -> "Solo se puede asignar a colaboradores activos."
        text.contains("no active employees", ignoreCase = true) ||
            text.contains("no employees to receive", ignoreCase = true) -> "El área no tiene colaboradores activos."
        text.contains("already delivered", ignoreCase = true) -> "La entrega de este beneficio ya fue registrada."
        text.contains("cancelled assignment", ignoreCase = true) -> "Esta asignación fue anulada y no se puede entregar."
        text.contains("before the validity starts", ignoreCase = true) ->
            "La fecha de entrega no puede ser anterior al inicio de la vigencia."
        text.contains("cannot be in the future", ignoreCase = true) -> "La fecha no puede ser futura."
        text.contains("leave the balance negative", ignoreCase = true) ||
            text.contains("Not enough vacation days", ignoreCase = true) ->
            "No puede descontar más de los días disponibles."
        text.contains("not valid for a benefit", ignoreCase = true) -> "Las unidades deben ser un número entero."
        code.endsWith("NOT_FOUND") && action == BenefitsAction.LOAD -> "No se encontró la información solicitada."
        else -> text.takeIf { it.isNotBlank() } ?: fallback
    }
}
