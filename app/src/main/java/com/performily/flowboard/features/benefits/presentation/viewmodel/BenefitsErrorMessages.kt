package com.performily.flowboard.features.benefits.presentation.viewmodel

import com.performily.flowboard.core.network.ApiException

/**
 * Action being performed when a failure happened.
 *
 * Used to pick the right user-facing text.
 */
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
 * Translates Benefits backend errors into the Spanish texts of the prototype.
 *
 * The backend answers with a generic `message` ("Business rule violation"), the concrete reason
 * in English in `details` and a code such as BENEFITTYPE_CONFLICT or BUSINESS_RULE_VIOLATION.
 * Errors are recognized by the code and by the text of `details` + `message`. The "no active
 * employees" case is checked before "ACTIVE employees" because the former also contains the latter.
 * Generic backend codes fall back to the action text, and errors without a code already arrive in
 * Spanish from the network layer.
 *
 * @param action what was being done when the failure happened.
 * @param fallback text used when no specific message applies.
 * @return the Spanish message to show to the user.
 */
internal fun Throwable.benefitsMessage(action: BenefitsAction, fallback: String): String {
    val api = this as? ApiException ?: return message?.takeIf { it.isNotBlank() } ?: fallback
    val code = api.code.orEmpty()
    val text = listOfNotNull(api.details, api.message).joinToString(" ")

    if (code.endsWith("CONFLICT")) {
        return when (action) {
            BenefitsAction.CREATE_TYPE -> "Ya existe un beneficio con este nombre."
            BenefitsAction.ASSIGN_TO_EMPLOYEE -> "El colaborador ya tiene este beneficio en un periodo que se cruza."
            BenefitsAction.ASSIGN_TO_AREA -> "Todos los colaboradores activos del área ya tienen este beneficio en el periodo."
            else -> fallback
        }
    }

    val known = when {
        text.contains("is not active", ignoreCase = true) -> "Este beneficio está inactivo y no se puede asignar."
        text.contains("no active employees", ignoreCase = true) ||
            text.contains("no employees to receive", ignoreCase = true) -> "El área no tiene colaboradores activos."
        text.contains("only be assigned to ACTIVE employees", ignoreCase = true) ||
            text.contains("ACTIVE employees", ignoreCase = true) -> "Solo se puede asignar a colaboradores activos."
        text.contains("already delivered", ignoreCase = true) -> "La entrega de este beneficio ya fue registrada."
        text.contains("cancelled assignment", ignoreCase = true) -> "Esta asignación fue anulada y no se puede entregar."
        text.contains("before the validity starts", ignoreCase = true) ->
            "La fecha de entrega no puede ser anterior al inicio de la vigencia."
        text.contains("cannot be in the future", ignoreCase = true) -> "La fecha no puede ser futura."
        text.contains("leave the balance negative", ignoreCase = true) ||
            text.contains("Not enough vacation days", ignoreCase = true) ->
            "No puede descontar más de los días disponibles."
        text.contains("not valid for a benefit", ignoreCase = true) -> "Las unidades deben ser un número entero."
        text.contains("at most 2 decimals", ignoreCase = true) -> "Usa como máximo 2 decimales."
        text.contains("must be greater than 0", ignoreCase = true) ||
            text.contains("cannot be zero", ignoreCase = true) -> "La cantidad debe ser mayor que cero."
        text.contains("End date cannot be before", ignoreCase = true) -> "La fecha final no puede ser anterior a la inicial."
        text.contains("reason is required", ignoreCase = true) -> "Ingresa el motivo del ajuste."
        text.contains("reason cannot exceed", ignoreCase = true) -> "El motivo admite hasta 250 caracteres."
        text.contains("name is required", ignoreCase = true) -> "Ingresa el nombre del beneficio."
        text.contains("name cannot exceed", ignoreCase = true) -> "El nombre admite hasta 80 caracteres."
        else -> null
    }
    if (known != null) return known

    return when {
        code.endsWith("NOT_FOUND") ->
            if (action == BenefitsAction.LOAD) "No se encontró la información solicitada." else "No se encontró el registro solicitado."
        code == "VALIDATION_ERROR" || code == "BUSINESS_RULE_VIOLATION" || code == "UNEXPECTED_ERROR" -> fallback
        else -> api.message.takeIf { it.isNotBlank() } ?: fallback
    }
}
