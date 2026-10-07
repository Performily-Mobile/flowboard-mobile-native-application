package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.request.application.usecase.ChangeRequestTypeStatusUseCase
import com.performily.flowboard.features.request.application.usecase.DeleteRequestTypeUseCase
import com.performily.flowboard.features.request.application.usecase.GetRequestTypesUseCase
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.presentation.state.RequestTypesSnackbar
import com.performily.flowboard.features.request.presentation.state.RequestTypesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class RequestTypesViewModel @Inject constructor(
    private val getRequestTypes: GetRequestTypesUseCase,
    private val changeRequestTypeStatus: ChangeRequestTypeStatusUseCase,
    private val deleteRequestType: DeleteRequestTypeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RequestTypesUiState())
    val state: StateFlow<RequestTypesUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = it.types.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            getRequestTypes(activeOnly = false)
                .onSuccess { types -> _state.update { it.copy(isLoading = false, types = types) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.requestMessage(RequestAction.LOAD, "No se pudieron cargar los tipos de solicitud.")
                        )
                    }
                }
        }
    }

    fun onCreated(message: String) {
        _state.update { it.copy(snackbar = RequestTypesSnackbar(message)) }
        load()
    }

    fun toggleStatus(type: RequestType) {
        if (_state.value.isWorking) return
        _state.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            changeRequestTypeStatus(type)
                .onSuccess { updated ->
                    _state.update { state ->
                        state.copy(
                            isWorking = false,
                            types = state.types.map { if (it.id == updated.id) updated else it },
                            snackbar = RequestTypesSnackbar(
                                if (updated.active) "\"${updated.name}\" activado." else "\"${updated.name}\" desactivado."
                            )
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isWorking = false,
                            snackbar = RequestTypesSnackbar(
                                exception.requestMessage(RequestAction.CHANGE_TYPE_STATUS, "No se pudo cambiar el estado.")
                            )
                        )
                    }
                }
        }
    }

    fun requestDelete(type: RequestType) = _state.update { it.copy(typeToDelete = type) }

    fun dismissDelete() = _state.update { it.copy(typeToDelete = null) }

    fun confirmDelete() {
        val type = _state.value.typeToDelete ?: return
        _state.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            deleteRequestType(type)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            isWorking = false,
                            typeToDelete = null,
                            types = state.types.filterNot { it.id == type.id },
                            snackbar = RequestTypesSnackbar("\"${type.name}\" eliminado.")
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isWorking = false,
                            typeToDelete = null,
                            snackbar = RequestTypesSnackbar(
                                message = exception.requestMessage(RequestAction.DELETE_TYPE, "No se pudo eliminar el tipo."),
                                // Si tiene solicitudes, se ofrece desactivarlo (MA-54).
                                typeToDeactivate = type.takeIf { type.active }
                            )
                        )
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbar = null) }
}
