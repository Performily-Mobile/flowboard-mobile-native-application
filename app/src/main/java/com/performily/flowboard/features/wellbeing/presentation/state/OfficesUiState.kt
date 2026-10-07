package com.performily.flowboard.features.wellbeing.presentation.state

import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus

/** MA-70 · Espacios. */
data class OfficesUiState(
    val isLoading: Boolean = false,
    /** true después de la primera respuesta del backend (aunque la lista venga vacía). */
    val hasLoaded: Boolean = false,
    val offices: List<OfficeStatus> = emptyList(),
    val errorMessage: String? = null
)
