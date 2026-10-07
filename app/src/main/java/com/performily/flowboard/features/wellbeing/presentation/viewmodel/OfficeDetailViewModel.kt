package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.wellbeing.application.usecase.GetInventoryDevicesUseCase
import com.performily.flowboard.features.wellbeing.application.usecase.GetOfficeStatusUseCase
import com.performily.flowboard.features.wellbeing.application.usecase.LinkDeviceUseCase
import com.performily.flowboard.features.wellbeing.application.usecase.UnlinkDeviceUseCase
import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.presentation.state.LinkDeviceForm
import com.performily.flowboard.features.wellbeing.presentation.state.OfficeDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MA-72 / MA-81 · Indicadores de un espacio (US50) y gestión de sus
 * dispositivos: vincular (US48, MA-73) y desvincular.
 */
@HiltViewModel
class OfficeDetailViewModel @Inject constructor(
    private val getOfficeStatus: GetOfficeStatusUseCase,
    private val getInventoryDevices: GetInventoryDevicesUseCase,
    private val linkDevice: LinkDeviceUseCase,
    private val unlinkDevice: UnlinkDeviceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OfficeDetailUiState())
    val state: StateFlow<OfficeDetailUiState> = _state.asStateFlow()

    /** Carga el estado del espacio. Las recargas no ocultan lo que ya se ve en pantalla. */
    fun load(officeId: Long) {
        val sameOffice = _state.value.officeId == officeId && _state.value.status != null
        _state.update {
            if (sameOffice) it.copy(errorMessage = null)
            else OfficeDetailUiState(officeId = officeId, isLoading = true)
        }
        refresh()
    }

    private fun refresh() {
        val officeId = _state.value.officeId ?: return
        viewModelScope.launch {
            getOfficeStatus(officeId)
                .onSuccess { status -> _state.update { it.copy(isLoading = false, status = status) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = exception.wellbeingMessage("No se pudo cargar el espacio."))
                    }
                }
        }
    }

    // ---------- Vincular dispositivo (MA-73) ----------

    fun showLinkSheet() {
        _state.update { it.copy(isLinkSheetVisible = true, linkForm = LinkDeviceForm(isLoadingInventory = true)) }
        viewModelScope.launch {
            val inventory = getInventoryDevices().getOrDefault(emptyList())
            _state.update { it.copy(linkForm = it.linkForm.copy(inventory = inventory, isLoadingInventory = false)) }
        }
    }

    fun dismissLinkSheet() = _state.update { it.copy(isLinkSheetVisible = false) }

    fun onLinkCodeChange(value: String) =
        _state.update { it.copy(linkForm = it.linkForm.copy(code = value.uppercase(), codeError = null)) }

    fun onLink() {
        val officeId = _state.value.officeId ?: return
        val code = _state.value.linkForm.code
        _state.update { it.copy(linkForm = it.linkForm.copy(isLinking = true, codeError = null)) }
        viewModelScope.launch {
            linkDevice(officeId, code)
                .onSuccess { device ->
                    _state.update {
                        it.copy(
                            isLinkSheetVisible = false,
                            linkForm = LinkDeviceForm(),
                            snackbarMessage = "Dispositivo ${device.code} vinculado."
                        )
                    }
                    refresh()
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            linkForm = it.linkForm.copy(
                                isLinking = false,
                                codeError = exception.wellbeingMessage("No se pudo vincular el dispositivo.")
                            )
                        )
                    }
                }
        }
    }

    // ---------- Desvincular ----------

    fun requestUnlink(device: Device) = _state.update { it.copy(deviceToUnlink = device) }

    fun dismissUnlink() = _state.update { it.copy(deviceToUnlink = null) }

    fun confirmUnlink() {
        val officeId = _state.value.officeId ?: return
        val device = _state.value.deviceToUnlink ?: return
        _state.update { it.copy(isUnlinking = true) }
        viewModelScope.launch {
            unlinkDevice(officeId, device.code)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isUnlinking = false,
                            deviceToUnlink = null,
                            snackbarMessage = "Dispositivo ${device.code} desvinculado. Volvió al inventario."
                        )
                    }
                    refresh()
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isUnlinking = false,
                            deviceToUnlink = null,
                            snackbarMessage = exception.wellbeingMessage("No se pudo desvincular el dispositivo.")
                        )
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }
}
