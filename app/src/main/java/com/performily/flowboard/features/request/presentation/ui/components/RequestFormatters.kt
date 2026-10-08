package com.performily.flowboard.features.request.presentation.ui.components

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestField
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.valueobject.ApproverType
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


internal object RequestFormatters {
    private val symbols = DecimalFormatSymbols(Locale.US)
    private val numberFormat = DecimalFormat("#,##0.##", symbols)
    private val moneyFormat = DecimalFormat("#,##0.00", symbols)
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val shortDateFormat = DateTimeFormatter.ofPattern("dd/MM")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun date(date: LocalDate): String = date.format(dateFormat)

    fun shortDate(date: LocalDate): String = date.format(shortDateFormat)

    fun time(time: LocalTime): String = time.format(timeFormat)

  
    fun dateTime(dateTime: LocalDateTime): String = "${date(dateTime.toLocalDate())} · ${time(dateTime.toLocalTime())}"

 
    fun shortDateTime(dateTime: LocalDateTime): String =
        "${shortDate(dateTime.toLocalDate())} · ${time(dateTime.toLocalTime())}"

   
    fun code(requestId: Long): String = "S-%04d".format(requestId)

   
    fun initials(name: String): String =
        name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }


    fun shortName(name: String): String = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString(" ")

    fun number(value: BigDecimal): String = numberFormat.format(value)


    fun days(value: Int): String = if (value == 1) "1 día" else "$value días"

    fun days(value: BigDecimal): String =
        if (value.compareTo(BigDecimal.ONE) == 0) "1 día" else "${number(value)} días"

 
    fun hours(value: Double): String {
        val text = numberFormat.format(value)
        return if (value == 1.0) "1 hora" else "$text horas"
    }

 
    fun shortPeriod(period: RequestPeriod?): String {
        if (period == null) return "Sin fecha"
        return when {
            period.hasHours -> "${shortDate(period.startDate)} · ${hours(period.hours)}"
            period.startDate == period.endDate -> "${shortDate(period.startDate)} · ${days(period.days)}"
            else -> "${shortDate(period.startDate)} al ${shortDate(period.endDate)} · ${days(period.days)}"
        }
    }


    fun shortRange(period: RequestPeriod?): String {
        if (period == null) return "Sin fecha"
        return if (period.startDate == period.endDate) shortDate(period.startDate)
        else "${shortDate(period.startDate)} al ${shortDate(period.endDate)}"
    }

    fun longPeriod(period: RequestPeriod?): String {
        if (period == null) return "Sin fecha"
        val start = period.startTime
        val end = period.endTime
        return when {
            start != null && end != null -> "${date(period.startDate)} · ${time(start)} a ${time(end)}"
            period.startDate == period.endDate -> date(period.startDate)
            else -> "${date(period.startDate)} al ${date(period.endDate)}"
        }
    }


    fun longPeriodWithAmount(period: RequestPeriod?): String {
        if (period == null) return "Sin fecha"
        val amount = if (period.hasHours) hours(period.hours) else days(period.days)
        return "${longPeriod(period)} · $amount"
    }

  
    fun amount(period: RequestPeriod?): String = when {
        period == null -> "—"
        period.hasHours -> hours(period.hours)
        else -> days(period.days)
    }


    fun sentAgo(submittedAt: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val elapsed = Duration.between(submittedAt, now)
        val text = when {
            elapsed.toMinutes() < 1 -> "hace un momento"
            elapsed.toMinutes() < 60 -> "hace ${elapsed.toMinutes()} min"
            elapsed.toHours() < 24 -> "hace ${elapsed.toHours()} h"
            elapsed.toDays() == 1L -> "hace 1 día"
            else -> "hace ${elapsed.toDays()} días"
        }
        return "Enviada $text"
    }

    fun approvalSummary(request: Request): String = listOfNotNull(
        request.requestTypeName,
        request.period?.let { shortPeriod(it) },
        "adjunto".takeIf { request.attachments.isNotEmpty() }
    ).joinToString(" · ")

   
    fun myRequestDetail(request: Request): String {
        val approver = shortName(request.approverName)
        val change = request.lastChange
        return when (request.status) {
            RequestStatus.IN_PROGRESS -> "Aprobador: $approver · Enviada el ${shortDate(request.submittedAt.toLocalDate())}"
            RequestStatus.UNDER_REVIEW -> "Aprobador: $approver · Se pidió más información"
            RequestStatus.APPROVED ->
                "Aprobada por ${change?.actorName?.let(::shortName) ?: approver}" +
                    (change?.let { " el ${shortDate(it.occurredAt.toLocalDate())}" } ?: "")
            RequestStatus.REJECTED -> change?.comment?.let { "Rechazada: $it" } ?: "Rechazada por $approver"
            RequestStatus.CANCELLED -> "Cancelada" + (change?.let { " el ${shortDate(it.occurredAt.toLocalDate())}" } ?: "")
        }
    }


    fun approverLine(request: Request): String =
        if (request.approverType == ApproverType.HR_STAFF) "Aprobador: RR.HH."
        else "Aprobador: ${shortName(request.approverName)}"


    fun typeHint(type: RequestType, availableDays: BigDecimal?): String = when {
        type.balanceDeduction == BalanceDeduction.VACATION_DAYS ->
            "Descuenta de tu saldo" + (availableDays?.let { " · ${days(it)} disponibles" } ?: "")
        type.balanceDeduction == BalanceDeduction.BENEFIT_BALANCE -> "Usa tu saldo de beneficio"
        type.requiresAttachment -> "Requiere documento adjunto"
        else -> type.description ?: "Sin descuento de saldo"
    }


    fun typeCatalogDetail(type: RequestType): String {
        val main = if (type.requiresAttachment && type.balanceDeduction == BalanceDeduction.NONE) {
            "Adjunto obligatorio"
        } else {
            type.balanceDeduction.catalogLabel
        }
        val fields = type.fields.size
        return "$main · $fields ${if (fields == 1) "campo" else "campos"}"
    }


    fun fieldDetail(dataType: FieldDataType, required: Boolean): String =
        "${dataType.label} · ${if (required) "Obligatorio" else "Opcional"}"

    fun fieldValue(field: RequestField?, value: String): String {
        val dataType = field?.dataType ?: return value
        return runCatching {
            when (dataType) {
                FieldDataType.DATE -> date(LocalDate.parse(value))
                FieldDataType.TIME -> time(LocalTime.parse(value))
                FieldDataType.BOOLEAN -> if (value.equals("true", ignoreCase = true)) "Sí" else "No"
                FieldDataType.MONEY -> "S/ ${moneyFormat.format(BigDecimal(value))}"
                else -> value
            }
        }.getOrDefault(value)
    }

    fun fileDetail(file: FileReference): String = "${formatLabel(file.contentType)} · ${size(file.sizeInBytes)}"

    fun formatLabel(contentType: String): String = when (contentType) {
        "application/pdf" -> "PDF"
        "image/jpeg" -> "JPG"
        "image/png" -> "PNG"
        else -> contentType
    }

    fun size(bytes: Long): String {
        val kb = bytes / 1024.0
        return if (kb < 1024) String.format(Locale.US, "%.0f KB", kb) else String.format(Locale.US, "%.1f MB", kb / 1024)
    }
}
