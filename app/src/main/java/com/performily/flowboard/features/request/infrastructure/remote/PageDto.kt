package com.performily.flowboard.features.request.infrastructure.remote

/**
 * Página que devuelven los listados del backend.
 */
data class PageDto<T>(
    val items: List<T>?,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean
)
