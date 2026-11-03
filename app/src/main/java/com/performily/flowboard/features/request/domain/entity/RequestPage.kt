package com.performily.flowboard.features.request.domain.entity

/**
 * Una página de solicitudes, con los datos para pedir la siguiente.
 *
 * @property items solicitudes de esta página.
 * @property page número de página, empieza en 0.
 * @property size tamaño de página pedido.
 * @property totalItems cantidad total de solicitudes en todas las páginas.
 * @property totalPages cantidad de páginas.
 * @property hasNext true si hay una página después de esta.
 */
data class RequestPage(
    val items: List<Request>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean
) {
    companion object {
        /** Tamaño de página por defecto, igual al del backend. */
        const val DEFAULT_SIZE = 20
    }
}
