package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.benefits.domain.entity.SyncedVacationBalance
import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitCard
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsBanner
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsColors
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsIcons
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.SectionHeader
import com.performily.flowboard.features.benefits.presentation.ui.components.VacationMovementRow
import com.performily.flowboard.features.benefits.presentation.viewmodel.MyVacationBalanceViewModel

/**
 * MA-57 · Saldo de vacaciones del colaborador. Sin conexión muestra los datos
 * guardados con el aviso "Sin conexión. Mostrando datos sincronizados el …".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVacationBalanceScreen(
    onBack: () -> Unit,
    onRequestVacation: () -> Unit,
    viewModel: MyVacationBalanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saldo de vacaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        val synced = state.synced
        when {
            state.isLoading -> BenefitsLoading(modifier = contentModifier)
            synced == null -> BenefitsMessageState(
                title = "No se pudo cargar tu saldo",
                message = state.errorMessage ?: "Inténtalo nuevamente.",
                actionLabel = "Reintentar",
                onAction = viewModel::load,
                modifier = contentModifier
            )
            else -> BalanceContent(
                synced = synced,
                onRetry = viewModel::load,
                onRequestVacation = onRequestVacation,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun BalanceContent(
    synced: SyncedVacationBalance,
    onRetry: () -> Unit,
    onRequestVacation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance = synced.balance
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (synced.fromCache) {
            item {
                BenefitsBanner(
                    message = "Sin conexión. Mostrando datos sincronizados ${BenefitsFormatters.syncedAt(synced.syncedAt)}.",
                    icon = BenefitsIcons.WifiOff,
                    containerColor = BenefitsColors.OfflineBanner,
                    contentColor = BenefitsColors.OnOfflineBanner,
                    actionLabel = "Reintentar",
                    onAction = onRetry
                )
            }
        }
        item { BalanceCard(balance = balance, onRequestVacation = onRequestVacation) }
        item { SectionHeader("Movimientos") }
        if (balance.movements.isEmpty()) {
            item {
                Text(
                    text = "Todavía no hay movimientos en tu saldo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(balance.movements, key = { it.id }) { movement -> VacationMovementRow(movement = movement) }
    }
}

/** Tarjeta principal: días disponibles, barra de lo usado y botón para solicitar. */
@Composable
private fun BalanceCard(balance: VacationBalance, onRequestVacation: () -> Unit) {
    BenefitCard {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                text = BenefitsFormatters.number(balance.availableDays),
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "días disponibles",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        LinearProgressIndicator(
            progress = { balance.usedRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainer,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Acumulados ${BenefitsFormatters.number(balance.accruedDays)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Usados ${BenefitsFormatters.number(balance.usedDays)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = onRequestVacation,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Icon(FlowboardIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Solicitar vacaciones", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
