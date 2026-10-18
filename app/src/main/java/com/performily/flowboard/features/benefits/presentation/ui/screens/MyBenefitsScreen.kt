package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardTabs
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitCard
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsBanner
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitChip
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitListRow
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsColors
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.IconCircle
import com.performily.flowboard.features.benefits.presentation.ui.components.SectionHeader
import com.performily.flowboard.features.benefits.presentation.viewmodel.MyBenefitsViewModel

/**
 * Number of delivered benefits shown in the "Vigentes" section.
 *
 * The latest delivered ones are listed there too, as in MA-58.
 */
private const val RECENT_DELIVERED = 2

/**
 * My benefits screen (MA-58, MA-59).
 *
 * Lists current and delivered benefits. The tabs slide away while the list scrolls down.
 *
 * @param onBack navigates back.
 * @param viewModel view model of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBenefitsScreen(
    onBack: () -> Unit,
    viewModel: MyBenefitsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis beneficios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val benefits = state.benefits
        val errorMessage = state.errorMessage
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            showHeaderKey = listOf(
                state.selectedTab,
                benefits?.current?.size,
                benefits?.delivered?.size,
                errorMessage != null
            ),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    FlowboardTabs(
                        labels = listOf("Vigentes (${state.currentCount})", "Entregados (${state.deliveredCount})"),
                        selectedIndex = state.selectedTab,
                        onSelect = { viewModel.onTabSelected(it) }
                    )
                    if (benefits != null && errorMessage != null) {
                        BenefitsBanner(
                            message = errorMessage,
                            icon = FlowboardIcons.Warning,
                            containerColor = BenefitsColors.WarningBanner,
                            contentColor = BenefitsColors.OnWarningBanner,
                            actionLabel = "Reintentar",
                            onAction = viewModel::load
                        )
                    }
                }
            }
        ) { contentPadding ->
            val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
            when {
                state.isLoading -> BenefitsLoading(modifier = stateModifier)
                benefits == null -> BenefitsMessageState(
                    title = "No se pudieron cargar tus beneficios",
                    message = state.errorMessage ?: "Inténtalo nuevamente.",
                    modifier = stateModifier,
                    actionLabel = "Reintentar",
                    onAction = viewModel::load
                )
                state.selectedTab == 0 -> CurrentTab(benefits, contentPadding)
                else -> DeliveredTab(benefits.delivered, contentPadding)
            }
        }
    }
}

/**
 * List padding that leaves room for the collapsing header.
 *
 * @param contentPadding padding given by the collapsing header layout.
 */
private fun listPadding(contentPadding: PaddingValues): PaddingValues = PaddingValues(
    top = contentPadding.calculateTopPadding() + 8.dp,
    bottom = Spacing.xl,
    start = Spacing.screenHorizontal,
    end = Spacing.screenHorizontal
)

/**
 * "Vigentes" tab: current benefits followed by the latest delivered ones.
 *
 * @param benefits benefits of the employee.
 * @param contentPadding padding given by the collapsing header layout.
 */
@Composable
private fun CurrentTab(benefits: EmployeeBenefits, contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = listPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
    ) {
        if (benefits.current.isEmpty()) {
            item {
                BenefitsMessageState(
                    title = "No tienes beneficios vigentes",
                    message = "Cuando RR.HH. te asigne un beneficio aparecerá aquí."
                )
            }
        }
        items(benefits.current, key = { it.id }) { assignment -> CurrentBenefitCard(assignment) }

        val recent = benefits.delivered.take(RECENT_DELIVERED)
        if (recent.isNotEmpty()) {
            item { SectionHeader("Entregados recientemente", modifier = Modifier.padding(top = Spacing.m)) }
            items(recent, key = { "recent-${it.id}" }) { assignment -> DeliveredRow(assignment) }
        }
    }
}

/**
 * "Entregados" tab: every delivered benefit.
 *
 * @param delivered delivered benefits.
 * @param contentPadding padding given by the collapsing header layout.
 */
@Composable
private fun DeliveredTab(delivered: List<BenefitAssignment>, contentPadding: PaddingValues) {
    if (delivered.isEmpty()) {
        BenefitsMessageState(
            title = "Aún no tienes beneficios entregados",
            message = "Cuando RR.HH. registre la entrega de un beneficio, lo verás aquí.",
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = listPadding(contentPadding)
    ) {
        items(delivered, key = { it.id }) { assignment -> DeliveredRow(assignment) }
    }
}

/**
 * Card of a current benefit (MA-58).
 */
@Composable
private fun CurrentBenefitCard(assignment: BenefitAssignment) {
    BenefitCard {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            IconCircle(icon = FlowboardIcons.Gift)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(assignment.benefitTypeName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Periodo: ${BenefitsFormatters.period(assignment.startDate, assignment.endDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = BenefitsFormatters.quantity(assignment.quantity, assignment.unit),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            BenefitChip(AssignmentStatus.ASSIGNED.label, BenefitsColors.Assigned)
        }
    }
}

/**
 * Row of a delivered benefit (MA-59).
 *
 * Example: "Entregado el 15/07/2026 · S/ 1,240.00".
 */
@Composable
private fun DeliveredRow(assignment: BenefitAssignment) {
    val deliveredOn = assignment.delivery?.deliveredOn
    val detail = listOfNotNull(
        deliveredOn?.let { "Entregado el ${BenefitsFormatters.date(it)}" },
        BenefitsFormatters.quantity(assignment.quantity, assignment.unit)
    ).joinToString(" · ")
    BenefitListRow(
        title = assignment.benefitTypeName,
        subtitle = detail,
        modifier = Modifier.fillMaxWidth(),
        leading = { IconCircle(icon = FlowboardIcons.Gift) },
        trailing = { BenefitChip(AssignmentStatus.DELIVERED.label, BenefitsColors.Delivered) }
    )
}
