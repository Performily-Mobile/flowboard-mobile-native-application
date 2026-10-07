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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MA-72 / MA-81 - Indicators of an office (US50) and management of its devices:
 * link (US48, MA-73) and unlink.
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

    private var refreshJob: Job? = null
    private var inventoryJob: Job? = null

    /**
     * Loads the status of the office.
     *
     * Reloads of the same office keep whatever is already on screen, including a previous
     * error, so periodic polling never resets the screen to a spinner.
     *
     * @param officeId office to load
     */
    fun load(officeId: Long) {
        if (_state.value.officeId != officeId) {
            _state.value = OfficeDetailUiState(officeId = officeId, isLoading = true)
        }
        refresh()
    }

    /** Reloads after the user taps "Reintentar". */
    fun retry() {
        _state.update { it.copy(isLoading = it.status == null, errorMessage = null) }
        refresh()
    }

    /**
     * Requests the office status, cancelling any request still in flight so an old
     * response cannot overwrite a newer one.
     */
    private fun refresh() {
        val officeId = _state.value.officeId ?: return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            getOfficeStatus(officeId)
                .onSuccess { status ->
                    _state.update { it.copy(isLoading = false, status = status, errorMessage = null) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = exception.wellbeingMessage("No se pudo cargar el espacio."))
                    }
                }
        }
    }

    /** Opens the link sheet and loads the inventory, reporting a failure instead of showing an empty inventory. */
    fun showLinkSheet() {
        _state.update { it.copy(isLinkSheetVisible = true, linkForm = LinkDeviceForm(isLoadingInventory = true)) }
        inventoryJob?.cancel()
        inventoryJob = viewModelScope.launch {
            getInventoryDevices()
                .onSuccess { inventory ->
                    _state.update {
                        it.copy(linkForm = it.linkForm.copy(inventory = inventory, isLoadingInventory = false, inventoryError = null))
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            linkForm = it.linkForm.copy(
                                inventory = emptyList(),
                                isLoadingInventory = false,
                                inventoryError = exception.wellbeingMessage("No se pudo cargar el inventario.")
                            )
                        )
                    }
                }
        }
    }

    /** Closes the link sheet and cancels the pending inventory request. */
    fun dismissLinkSheet() {
        inventoryJob?.cancel()
        _state.update { it.copy(isLinkSheetVisible = false) }
    }

    fun onLinkCodeChange(value: String) =
        _state.update { it.copy(linkForm = it.linkForm.copy(code = value.uppercase(), codeError = null)) }

    /** Links the typed device; ignored while another link request is running. */
    fun onLink() {
        val officeId = _state.value.officeId ?: return
        if (_state.value.linkForm.isLinking) return
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

    fun requestUnlink(device: Device) = _state.update { it.copy(deviceToUnlink = device) }

    fun dismissUnlink() = _state.update { it.copy(deviceToUnlink = null) }

    /** Unlinks the selected device; ignored while another unlink request is running. */
    fun confirmUnlink() {
        val officeId = _state.value.officeId ?: return
        val device = _state.value.deviceToUnlink ?: return
        if (_state.value.isUnlinking) return
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
