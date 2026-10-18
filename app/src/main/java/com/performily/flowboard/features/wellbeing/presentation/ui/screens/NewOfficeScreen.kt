package com.performily.flowboard.features.wellbeing.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.wellbeing.presentation.state.OfficeAreaOptions
import com.performily.flowboard.features.wellbeing.presentation.viewmodel.NewOfficeViewModel

/**
 * New space form (MA-71), full screen without the app bottom navigation.
 *
 * The create button sits in a bottom action bar that stays above the system navigation bar and
 * the keyboard.
 *
 * @param onClose closes the form.
 * @param onCreated called with the id of the created space.
 * @param viewModel view model of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOfficeScreen(
    onClose: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: NewOfficeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.createdOfficeId) {
        state.createdOfficeId?.let(onCreated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo espacio") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(FlowboardIcons.Close, contentDescription = "Cerrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
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
                            Text("Crear espacio")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            FormField(
                label = "Nombre del espacio",
                value = state.name,
                onValueChange = viewModel::onNameChange,
                error = state.nameError,
                placeholder = "Ej. Oficina Lima Centro"
            )
            AreaField(selected = state.area, onSelect = viewModel::onAreaChange)
            FormField(
                label = "Dirección",
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                error = state.addressError
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                FormField(
                    label = "Piso",
                    value = state.floor,
                    onValueChange = viewModel::onFloorChange,
                    error = state.floorError,
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = "Referencia (opcional)",
                    value = state.reference,
                    onValueChange = viewModel::onReferenceChange,
                    modifier = Modifier.weight(2f)
                )
            }
            Text(
                text = "Después de crearlo podrás vincular los dispositivos de medición.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Single-line text field of the form with its validation error.
 *
 * @param label label of the field.
 * @param value current text.
 * @param onValueChange called on every change.
 * @param modifier modifier applied to the field.
 * @param error validation error, if any.
 * @param placeholder hint shown while the field is empty.
 */
@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * "Área" dropdown with the options of the prototype.
 *
 * @param selected area currently selected, if any.
 * @param onSelect called with the chosen area.
 */
@Composable
private fun AreaField(selected: String?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Área (opcional)") },
            trailingIcon = { Icon(FlowboardIcons.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OfficeAreaOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    trailingIcon = if (option == selected) {
                        { Icon(FlowboardIcons.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

