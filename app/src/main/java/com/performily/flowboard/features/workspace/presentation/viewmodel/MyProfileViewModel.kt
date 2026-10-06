package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.workspace.application.usecase.GetCurrentEmployeeUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeByIdUseCase
import com.performily.flowboard.features.workspace.presentation.state.MyProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val getCurrentEmployee: GetCurrentEmployeeUseCase,
    private val getEmployeeById: GetEmployeeByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyProfileUiState())
    val state: StateFlow<MyProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getCurrentEmployee()
                .onSuccess { employee ->
                    val manager = employee.directManagerId?.let { getEmployeeById(it).getOrNull() }
                    _state.update { it.copy(isLoading = false, employee = employee, directManager = manager) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }
}
