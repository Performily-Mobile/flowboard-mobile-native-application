package com.performily.flowboard.features.wellbeing.domain.entity

import com.performily.flowboard.features.wellbeing.domain.valueobject.OfficeLocation

/**
 * Espacio de trabajo de la organización cuyas condiciones ambientales se miden.
 * El área es un texto libre (por ejemplo "Almacén"): Wellbeing no depende de Workspace.
 */
data class Office(
    val id: Long,
    val name: String,
    val area: String?,
    val location: OfficeLocation,
    val active: Boolean
)
