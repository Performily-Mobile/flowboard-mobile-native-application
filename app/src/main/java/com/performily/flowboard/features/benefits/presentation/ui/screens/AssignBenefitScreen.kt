package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import com.performily.flowboard.features.benefits.presentation.state.AssignBenefitUiState
import com.performily.flowboard.features.benefits.presentation.state.AssignTargetMode
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsBanner
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsColors
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsDateField
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsSegmented
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsSelectField
import com.performily.flowboard.features.benefits.presentation.viewmodel.AssignBenefitViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

/**
 * Assign benefit screen (MA-61).
 *
 * Full-screen form without the app bottom navigation; the submit button sits in a bottom action
 * bar that stays above the system navigation bar and the keyboard.
 *
 * @param onClose closes the form.
 * @param onAssigned called with the result message once the benefit is assigned.
 * @param viewModel view model of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignBenefitScreen(
    onClose: () -> Unit,
    onAssigned: (message: String) -> Unit,
    viewModel: AssignBenefitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.resultMessage) {
        state.resultMessage?.let(onAssigned)
    }

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Asignar beneficio",
                onClose = onClose
            )
        },
        bottomBar = {
            if (!state.isLoading && state.loadError == null) {
                FlowboardBottomActionBar {
                    Column(modifier = Modifier.weight(1f)) {
                        state.errorMessage?.let { message ->
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = Spacing.s)
                            )
                        }
                        Button(
                            onClick = viewModel::onSubmit,
                            enabled = state.canSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(state.submitLabel)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        when {
            state.isLoading -> BenefitsLoading(modifier = contentModifier)
            state.loadError != null -> BenefitsMessageState(
                title = "No se pudo abrir el formulario",
                message = state.loadError.orEmpty(),
                actionLabel = "Reintentar",
                onAction = viewModel::load,
                modifier = contentModifier
            )
            state.benefitTypes.isEmpty() -> BenefitsMessageState(
                title = "No hay beneficios activos",
                message = "Activa o crea un tipo de beneficio en el catálogo para poder asignarlo.",
                modifier = contentModifier
            )
            else -> AssignForm(state = state, viewModel = viewModel, modifier = contentModifier)
        }
    }
}

/**
 * Fields of the assignment: benefit type, target (employee or area), quantity and period.
 *
 * @param state screen state.
 * @param viewModel view model that receives the changes.
 * @param modifier modifier applied to the form.
 */
@Composable
private fun AssignForm(state: AssignBenefitUiState, viewModel: AssignBenefitViewModel, modifier: Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        BenefitsSelectField(
            label = "Tipo de beneficio",
            options = state.benefitTypes,
            selected = state.selectedType,
            optionLabel = { it.name },
            onSelect = viewModel::onTypeSelected,
            modifier = Modifier.fillMaxWidth()
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text("Asignar a", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BenefitsSegmented(
                options = AssignTargetMode.entries,
                selected = state.mode,
                optionLabel = { it.label },
                onSelect = viewModel::onModeSelected
            )
        }

        if (state.mode == AssignTargetMode.AREA) {
            BenefitsSelectField(
                label = "Área",
                options = state.areas,
                selected = state.selectedArea,
                optionLabel = { it.name },
                onSelect = viewModel::onAreaSelected,
                supportingText = areaHelper(state),
                isError = state.previewError != null,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            BenefitsSelectField(
                label = "Colaborador",
                options = state.employees,
                selected = state.selectedEmployee,
                optionLabel = { "${it.name} · ${it.areaName}" },
                onSelect = viewModel::onEmployeeSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }

        val unit = state.selectedType?.unit ?: BenefitUnit.MONEY
        OutlinedTextField(
            value = state.quantity,
            onValueChange = viewModel::onQuantityChange,
            label = { Text(unit.quantityLabel) },
            prefix = if (unit == BenefitUnit.MONEY) { { Text("S/ ") } } else null,
            suffix = when (unit) {
                BenefitUnit.DAYS -> { { Text("días") } }
                BenefitUnit.UNITS -> { { Text("unidades") } }
                BenefitUnit.MONEY -> null
            },
            isError = state.quantityError != null,
            supportingText = state.quantityError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (unit == BenefitUnit.UNITS) KeyboardType.Number else KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            BenefitsDateField(
                label = "Desde",
                value = state.startDate,
                onValueChange = viewModel::onStartDateChange,
                modifier = Modifier.weight(1f)
            )
            BenefitsDateField(
                label = "Hasta",
                value = state.endDate,
                onValueChange = viewModel::onEndDateChange,
                minDate = state.startDate,
                isError = state.dateError != null,
                modifier = Modifier.weight(1f)
            )
        }
        state.dateError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        val preview = state.preview
        if (state.mode == AssignTargetMode.AREA && preview != null && preview.alreadyAssigned > 0) {
            val skipped = preview.alreadyAssigned
            BenefitsBanner(
                message = if (preview.toAssign == 0) {
                    "Todos los colaboradores activos del área ya tienen este beneficio en el periodo."
                } else if (skipped == 1) {
                    "1 colaborador ya tiene este beneficio en el periodo y será omitido."
                } else {
                    "$skipped colaboradores ya tienen este beneficio en el periodo y serán omitidos."
                },
                icon = FlowboardIcons.Warning,
                containerColor = BenefitsColors.WarningBanner,
                contentColor = BenefitsColors.OnWarningBanner
            )
        }
    }
}

/**
 * Builds the helper text of the area selector.
 *
 * Example: "Se asignará a los 48 colaboradores activos del área."
 */
private fun areaHelper(state: AssignBenefitUiState): String? {
    val area = state.selectedArea ?: return null
    state.previewError?.let { return it }
    if (state.isLoadingPreview) return "Calculando colaboradores…"
    val active = state.preview?.activeEmployees?.toLong() ?: area.activeEmployees
    return when (active) {
        0L -> "El área no tiene colaboradores activos."
        1L -> "Se asignará al único colaborador activo del área."
        else -> "Se asignará a los $active colaboradores activos del área."
    }
}
