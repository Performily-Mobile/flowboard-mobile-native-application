package com.performily.flowboard.features.wellbeing.presentation.state

/** Opciones del campo "Área" del prototipo (MA-71). */
val OfficeAreaOptions = listOf("Oficina", "Almacén", "Tienda", "Sala de reuniones", "Planta", "Otro")

/** MA-71 · Nuevo espacio. */
data class NewOfficeUiState(
    val name: String = "",
    val area: String? = null,
    val address: String = "",
    val floor: String = "",
    val reference: String = "",
    val nameError: String? = null,
    val addressError: String? = null,
    val floorError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    /** Cuando tiene valor, el espacio se creó y la pantalla navega a su detalle. */
    val createdOfficeId: Long? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting && name.isNotBlank() && address.isNotBlank() && floor.isNotBlank()
}
