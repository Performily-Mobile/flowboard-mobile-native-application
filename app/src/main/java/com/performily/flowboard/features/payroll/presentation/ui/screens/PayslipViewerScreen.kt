package com.performily.flowboard.features.payroll.presentation.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.payroll.domain.entity.PayslipDetail
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.presentation.ui.components.MyPayslipFormatters
import com.performily.flowboard.features.payroll.presentation.ui.components.PaymentStatusChip
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollMessageState
import com.performily.flowboard.features.payroll.presentation.ui.components.toDisplay
import com.performily.flowboard.features.payroll.presentation.viewmodel.PayslipViewerViewModel
import com.performily.flowboard.core.designsystem.component.SkeletonDetail

/**
 * Viewer of one payslip of the employee (MA-66): net amount, payment status, employer data and a
 * button that opens the PDF.
 *
 * @param payslipId identifier of the payslip to show.
 * @param onBack called when the back button is tapped.
 * @param viewModel payslip viewer view model.
 */
@Composable
fun PayslipViewerScreen(
    payslipId: Long,
    onBack: () -> Unit,
    viewModel: PayslipViewerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(payslipId) { viewModel.load(payslipId) }

    LaunchedEffect(state.linkToOpen) {
        val link = state.linkToOpen ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            viewModel.onLinkOpened()
        } catch (exception: ActivityNotFoundException) {
            viewModel.onOpenFailed()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Boleta de pago",
                subtitle = state.detail?.payslip?.period?.label,
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.detail != null) {
                FlowboardBottomActionBar {
                    Button(
                        onClick = viewModel::openFile,
                        enabled = !state.isOpeningFile,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        if (state.isOpeningFile) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(FlowboardIcons.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(Spacing.s))
                        Text("Abrir boleta (PDF)")
                    }
                }
            }
        }
    ) { paddingValues ->
        val detail = state.detail
        when {
            state.isLoading -> SkeletonDetail(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                showTabs = false
            )
            detail == null -> PayrollMessageState(
                title = "No se pudo cargar la boleta",
                message = state.errorMessage.orEmpty(),
                modifier = Modifier.padding(paddingValues),
                actionLabel = "Reintentar",
                onAction = viewModel::retry
            )
            else -> PayslipContent(detail = detail, modifier = Modifier.padding(paddingValues))
        }
    }
}

/** Scrollable body of the viewer: highlighted net amount and the list of details. */
@Composable
private fun PayslipContent(detail: PayslipDetail, modifier: Modifier = Modifier) {
    val payslip = detail.payslip
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = payslip.period.label, style = MaterialTheme.typography.labelLarge)
                    PaymentStatusChip(status = payslip.payment.status)
                }
                Text(text = "Neto a pagar", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = payslip.netAmount.toDisplay(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = MyPayslipFormatters.paymentSummary(payslip.payment),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Text(text = "Datos de la boleta", style = MaterialTheme.typography.titleMedium)
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                val rows = buildList {
                    payslip.employeeName?.let { add("Colaborador" to it) }
                    detail.companyLegalName?.let { add("Empresa" to it) }
                    detail.companyTaxId?.let { add("RUC" to it) }
                    add("Período" to payslip.period.label)
                    add("Fecha de emisión" to MyPayslipFormatters.date(payslip.issueDate))
                    when (payslip.payment.status) {
                        PaymentStatus.PAID -> payslip.payment.paidOn?.let {
                            add("Fecha de depósito" to MyPayslipFormatters.date(it))
                        }
                        PaymentStatus.OBSERVED -> payslip.payment.observationReason?.let {
                            add("Motivo de la observación" to it)
                        }
                        PaymentStatus.PENDING -> Unit
                    }
                    add("Archivo" to "${payslip.file.fileName} · ${MyPayslipFormatters.fileSize(payslip.file.sizeInBytes)}")
                }
                Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
                    rows.forEachIndexed { index, (label, value) ->
                        if (index > 0) HorizontalDivider(color = Divider)
                        DetailRow(label = label, value = value)
                    }
                }
            }
        }
    }
}

/** Row with a label on the left and its value on the right. */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}
