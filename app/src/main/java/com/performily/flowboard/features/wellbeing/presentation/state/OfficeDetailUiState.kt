package com.performily.flowboard.features.wellbeing.presentation.state

import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus

/** Formulario de la hoja "Vincular dispositivo" (MA-73). */
data class LinkDeviceForm(
    val code: String = "",
    val codeError: String? = null,
    val inventory: List<Device> = emptyList(),
    val isLoadingInventory: Boolean = false,
    val isLinking: Boolean = false
)

/** MA-72 · Indicadores de un espacio y MA-81 · Sin lecturas. */
data class OfficeDetailUiState(
    val officeId: Long? = null,
    val isLoading: Boolean = false,
    val status: OfficeStatus? = null,
    val errorMessage: String? = null,
    val isLinkSheetVisible: Boolean = false,
    val linkForm: LinkDeviceForm = LinkDeviceForm(),
    val deviceToUnlink: Device? = null,
    val isUnlinking: Boolean = false,
    /** Mensaje para el snackbar; la pantalla lo muestra y lo limpia. */
    val snackbarMessage: String? = null
)
