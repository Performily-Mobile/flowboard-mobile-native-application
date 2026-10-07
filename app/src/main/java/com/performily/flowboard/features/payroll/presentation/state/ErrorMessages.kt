package com.performily.flowboard.features.payroll.presentation.state

import com.performily.flowboard.core.network.ApiException

/**
 * Builds the message shown to the user for a failure.
 *
 * Uses the concrete reason sent by the backend when there is one, then the exception message,
 * and finally a generic text, so the result is never empty.
 *
 * @receiver the failure to describe
 * @return a non-empty message in Spanish or the one provided by the backend
 */
fun Throwable.toUserMessage(): String =
    (this as? ApiException)?.details?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: "Ocurrió un error inesperado."
