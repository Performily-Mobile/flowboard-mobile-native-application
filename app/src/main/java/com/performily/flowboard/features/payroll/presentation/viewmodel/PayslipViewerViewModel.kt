package com.performily.flowboard.features.payroll.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.payroll.application.usecase.GetMyPayslipDetailUseCase
import com.performily.flowboard.features.payroll.application.usecase.GetMyPayslipDownloadLinkUseCase
import com.performily.flowboard.features.payroll.presentation.state.PayslipViewerUiState
import com.performily.flowboard.features.payroll.presentation.state.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Holds the state of the payslip viewer: the payslip detail and the opening of its file. */
@HiltViewModel
class PayslipViewerViewModel @Inject constructor(
    private val getMyPayslipDetail: GetMyPayslipDetailUseCase,
    private val getDownloadLink: GetMyPayslipDownloadLinkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PayslipViewerUiState())
    val state: StateFlow<PayslipViewerUiState> = _state.asStateFlow()

    private var payslipId: Long? = null

    /**
     * Loads the payslip. It does nothing when that payslip is already shown.
     *
     * @param id identifier of the payslip to show.
     */
    fun load(id: Long) {
        if (payslipId == id && _state.value.detail != null) return
        payslipId = id
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getMyPayslipDetail(id).fold(
                onSuccess = { detail ->
                    _state.update { it.copy(detail = detail, isLoading = false) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
            )
        }
    }

    /** Retries the load after a failure. */
    fun retry() {
        payslipId?.let { load(it) }
    }

    /**
     * Requests the temporary link of the file.
     *
     * A link that is not a web address belongs to the phone that stored the file, so the user gets
     * a message instead of a link that cannot be opened.
     */
    fun openFile() {
        val id = payslipId ?: return
        if (_state.value.isOpeningFile) return
        viewModelScope.launch {
            _state.update { it.copy(isOpeningFile = true) }
            getDownloadLink(id).fold(
                onSuccess = { link ->
                    _state.update {
                        if (link.isWebLink) {
                            it.copy(isOpeningFile = false, linkToOpen = link)
                        } else {
                            it.copy(isOpeningFile = false, message = FILE_UNAVAILABLE)
                        }
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isOpeningFile = false, message = error.toUserMessage()) }
                }
            )
        }
    }

    /** Called once the screen has launched the link, so it is not opened again. */
    fun onLinkOpened() {
        _state.update { it.copy(linkToOpen = null) }
    }

    /** Called when no app on the phone can open the link. */
    fun onOpenFailed() {
        _state.update { it.copy(linkToOpen = null, message = NO_APP_TO_OPEN) }
    }

    /** Called once the snackbar has shown the message. */
    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private companion object {
        const val FILE_UNAVAILABLE = "El archivo de esta boleta aún no está disponible para abrirse en tu dispositivo."
        const val NO_APP_TO_OPEN = "No hay una aplicación en tu dispositivo que pueda abrir la boleta."
    }
}
