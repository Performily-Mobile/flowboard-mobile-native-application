package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeByIdUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeDocumentsUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetJobAssignmentsUseCase
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory
import com.performily.flowboard.features.workspace.presentation.state.EmployeeDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeDetailViewModel @Inject constructor(
    private val getEmployeeById: GetEmployeeByIdUseCase,
    private val getJobAssignments: GetJobAssignmentsUseCase,
    private val getEmployeeDocuments: GetEmployeeDocumentsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EmployeeDetailUiState())
    val state: StateFlow<EmployeeDetailUiState> = _state.asStateFlow()

    fun load(employeeId: Long) {
        val id = EmployeeId(employeeId)
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val assignmentsRequest = async { getJobAssignments(id) }
            val documentsRequest = async { getEmployeeDocuments(id) }

            getEmployeeById(id)
                .onSuccess { employee ->
                    val managerName = employee.directManagerId?.let { managerId ->
                        getEmployeeById(managerId).getOrNull()?.let { manager ->
                            "${manager.name.fullName} · ${manager.positionTitle}"
                        }
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            employee = employee,
                            directManagerName = managerName,
                            jobAssignments = assignmentsRequest.await().getOrDefault(emptyList()),
                            documents = documentsRequest.await().getOrDefault(emptyList())
                        )
                    }
                }
                .onFailure { exception ->
                    assignmentsRequest.cancel()
                    documentsRequest.cancel()
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun onTabSelected(index: Int) = _state.update { it.copy(selectedTab = index) }

    fun onDocumentFilterChange(category: DocumentCategory?) = _state.update { it.copy(documentFilter = category) }
}
