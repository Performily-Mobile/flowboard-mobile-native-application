package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.core.network.ApiException
import com.performily.flowboard.features.workspace.application.usecase.CreateAreaUseCase
import com.performily.flowboard.features.workspace.application.usecase.CreatePositionUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetAreasUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetPositionsUseCase
import com.performily.flowboard.features.workspace.presentation.state.AreaForm
import com.performily.flowboard.features.workspace.presentation.state.OrganizationUiState
import com.performily.flowboard.features.workspace.presentation.state.PositionForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class OrganizationViewModel @Inject constructor(
    private val getAreas: GetAreasUseCase,
    private val getPositions: GetPositionsUseCase,
    private val createArea: CreateAreaUseCase,
    private val createPosition: CreatePositionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizationUiState())
    val state: StateFlow<OrganizationUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val areasRequest = async { getAreas() }
            val positionsRequest = async { getPositions() }
            val areas = areasRequest.await()
            val positions = positionsRequest.await()
            _state.update {
                it.copy(
                    isLoading = false,
                    areas = areas.getOrDefault(it.areas),
                    positions = positions.getOrDefault(it.positions),
                    errorMessage = (areas.exceptionOrNull() ?: positions.exceptionOrNull())?.message
                )
            }
        }
    }

    fun onTabSelected(index: Int) = _state.update { it.copy(selectedTab = index) }

    fun showAreaSheet() = _state.update { it.copy(isAreaSheetVisible = true, areaForm = AreaForm()) }

    fun showPositionSheet() = _state.update { it.copy(isPositionSheetVisible = true, positionForm = PositionForm()) }

    fun dismissSheets() = _state.update { it.copy(isAreaSheetVisible = false, isPositionSheetVisible = false) }

    fun onAreaNameChange(value: String) =
        _state.update { it.copy(areaForm = it.areaForm.copy(name = value, nameError = null)) }

    fun onAreaDescriptionChange(value: String) =
        _state.update { it.copy(areaForm = it.areaForm.copy(description = value)) }

    fun onPositionAreaChange(areaId: Long) =
        _state.update { it.copy(positionForm = it.positionForm.copy(areaId = areaId, areaError = null)) }

    fun onPositionTitleChange(value: String) =
        _state.update { it.copy(positionForm = it.positionForm.copy(title = value, titleError = null)) }

    fun onPositionSalaryChange(value: String) =
        _state.update { it.copy(positionForm = it.positionForm.copy(referenceSalary = value, salaryError = null)) }

    fun saveArea() {
        val form = _state.value.areaForm
        val duplicated = _state.value.areas.any { it.name.equals(form.name.trim(), ignoreCase = true) }
        if (duplicated) {
            _state.update { it.copy(areaForm = form.copy(nameError = "Ya existe un área con este nombre.")) }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            createArea(form.name, form.description)
                .onSuccess {
                    _state.update { it.copy(isSaving = false, isAreaSheetVisible = false) }
                    load()
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isSaving = false, areaForm = it.areaForm.copy(nameError = exception.readableMessage()))
                    }
                }
        }
    }

    fun savePosition() {
        val form = _state.value.positionForm
        val areaId = form.areaId
        val amount = parseAmount(form.referenceSalary)
        if (areaId == null || amount == null) {
            _state.update {
                it.copy(
                    positionForm = form.copy(
                        areaError = if (areaId == null) "Selecciona un área." else null,
                        salaryError = if (amount == null) "Ingresa un monto válido." else null
                    )
                )
            }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val money = runCatching { Money(amount) }
            money.exceptionOrNull()?.let { exception ->
                _state.update {
                    it.copy(isSaving = false, positionForm = it.positionForm.copy(salaryError = exception.message))
                }
                return@launch
            }
            createPosition(form.title, areaId, money.getOrThrow())
                .onSuccess {
                    _state.update { it.copy(isSaving = false, isPositionSheetVisible = false) }
                    load()
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isSaving = false, positionForm = it.positionForm.copy(titleError = exception.readableMessage()))
                    }
                }
        }
    }

    private fun parseAmount(raw: String): BigDecimal? =
        raw.replace("S/", "").replace(",", "").trim().toBigDecimalOrNull()

    private fun Throwable.readableMessage(): String =
        (this as? ApiException)?.message ?: message ?: "No se pudo guardar."
}
