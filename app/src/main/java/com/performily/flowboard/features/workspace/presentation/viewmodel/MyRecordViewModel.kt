package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeDocumentsUseCase
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory
import com.performily.flowboard.features.workspace.presentation.state.MyRecordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRecordViewModel @Inject constructor(
    private val currentEmployeeProvider: CurrentEmployeeProvider,
    private val getEmployeeDocuments: GetEmployeeDocumentsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyRecordUiState())
    val state: StateFlow<MyRecordUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getEmployeeDocuments(currentEmployeeProvider.currentEmployeeId())
                .onSuccess { documents -> _state.update { it.copy(isLoading = false, documents = documents) } }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun onDocumentFilterChange(category: DocumentCategory?) = _state.update { it.copy(documentFilter = category) }
}
