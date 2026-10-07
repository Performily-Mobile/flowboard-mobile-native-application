package com.performily.flowboard.features.payroll.presentation.state

/** Archivo elegido en el selector del sistema, antes de validarse. */
data class SelectedPayslipFile(
    val uri: String,
    val name: String,
    val contentType: String,
    val sizeInBytes: Long
)
