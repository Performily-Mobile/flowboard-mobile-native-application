package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.presentation.ui.components.AdjustBalanceSheet
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsIcons
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.SectionHeader
import com.performily.flowboard.features.benefits.presentation.ui.components.VacationMovementRow
import com.performily.flowboard.features.benefits.presentation.viewmodel.VacationBalanceDetailViewModel

/** MA-63 · Saldo de vacaciones de un colaborador, con el ajuste manual de RR.HH. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacationBalanceDetailScreen(
    employeeId: Long,
    employeeName: String,
    onBack: () -> Unit,
    viewModel: VacationBalanceDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(employeeId) { viewModel.load(employeeId) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Saldo de vacaciones")
                        Text(
                            text = state.balance?.employeeName ?: employeeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.balance != null) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::showAdjustSheet,
                    icon = { Icon(BenefitsIcons.Edit, contentDescription = null) },
                    text = { Text("Ajustar saldo") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        val balance = state.balance
        when {
            state.isLoading -> BenefitsLoading(modifier = contentModifier)
            balance == null -> BenefitsMessageState(
                title = "No se pudo cargar el saldo",
                message = state.errorMessage ?: "Inténtalo nuevamente.",
                actionLabel = "Reintentar",
                onAction = viewModel::retry,
                modifier = contentModifier
            )
            else -> BalanceDetail(balance = balance, modifier = contentModifier)
        }
    }

    if (state.isAdjustSheetVisible) {
        AdjustBalanceSheet(
            form = state.adjustmentForm,
            onOperationChange = viewModel::onOperationChange,
            onDaysChange = viewModel::onDaysChange,
            onReasonChange = viewModel::onReasonChange,
            onSave = viewModel::saveAdjustment,
            onDismiss = viewModel::dismissAdjustSheet
        )
    }
}

@Composable
private fun BalanceDetail(balance: VacationBalance, modifier: Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Acumulados", BenefitsFormatters.number(balance.accruedDays), highlighted = false, modifier = Modifier.weight(1f))
                StatCard("Usados", BenefitsFormatters.number(balance.usedDays), highlighted = false, modifier = Modifier.weight(1f))
                StatCard("Disponibles", BenefitsFormatters.number(balance.availableDays), highlighted = true, modifier = Modifier.weight(1f))
            }
        }
        item { SectionHeader("Movimientos") }
        if (balance.movements.isEmpty()) {
            item {
                Text(
                    text = "Todavía no hay movimientos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(balance.movements, key = { it.id }) { movement ->
            VacationMovementRow(movement = movement, daysInSubtitle = true)
        }
    }
}

/** Tarjeta de Acumulados / Usados / Disponibles (los disponibles en el color principal). */
@Composable
private fun StatCard(label: String, value: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
