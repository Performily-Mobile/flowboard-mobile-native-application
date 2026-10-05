package com.performily.flowboard.features.workspace.presentation.ui.components

import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.workspace.domain.valueobject.AssignmentChangeType
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocumentType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.toDisplay(): String = format(dateFormatter)

fun LocalDateTime.toDisplay(): String = format(dateFormatter)

fun EmploymentStatus.label(): String = when (this) {
    EmploymentStatus.ACTIVE -> "Activo"
    EmploymentStatus.SUSPENDED -> "Suspendido"
    EmploymentStatus.TERMINATED -> "Cesado"
}

fun ContractType.label(): String = when (this) {
    ContractType.INDEFINITE -> "Plazo indeterminado"
    ContractType.FIXED_TERM -> "Plazo fijo"
    ContractType.PART_TIME -> "Tiempo parcial"
    ContractType.INTERNSHIP -> "Prácticas"
}

fun IdentityDocumentType.label(): String = when (this) {
    IdentityDocumentType.DNI -> "DNI"
    IdentityDocumentType.CE -> "CE"
    IdentityDocumentType.PASSPORT -> "Pasaporte"
}

fun AssignmentChangeType.label(): String = when (this) {
    AssignmentChangeType.HIRE -> "Ingreso"
    AssignmentChangeType.REASSIGNMENT -> "Reasignación"
    AssignmentChangeType.REINSTATEMENT -> "Reincorporación"
}

fun DocumentCategory.label(): String = when (this) {
    DocumentCategory.CONTRACT -> "Contrato"
    DocumentCategory.CERTIFICATE -> "Constancia"
    DocumentCategory.PERSONAL -> "Personal"
}

fun DocumentType.label(): String = when (this) {
    DocumentType.IDENTITY_DOCUMENT -> "Documento de identidad"
    DocumentType.EMPLOYMENT_CONTRACT -> "Contrato de trabajo"
    DocumentType.CONTRACT_ADDENDUM -> "Adenda de contrato"
    DocumentType.PENSION_AFFILIATION -> "Afiliación previsional"
    DocumentType.HEALTH_INSURANCE -> "Seguro de salud"
    DocumentType.EDUCATION_CERTIFICATE -> "Certificado de estudios"
    DocumentType.CRIMINAL_RECORD_CERTIFICATE -> "Certificado de antecedentes"
    DocumentType.WARNING_LETTER -> "Carta de amonestación"
    DocumentType.COMMENDATION_LETTER -> "Carta de felicitación"
    DocumentType.CERTIFICATE_OF_EMPLOYMENT -> "Constancia de trabajo"
}

fun Money.toDisplay(): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatted = DecimalFormat("#,##0.00", symbols).format(amount)
    val symbol = if (currency == Money.DEFAULT_CURRENCY) "S/" else currency
    return "$symbol $formatted"
}
