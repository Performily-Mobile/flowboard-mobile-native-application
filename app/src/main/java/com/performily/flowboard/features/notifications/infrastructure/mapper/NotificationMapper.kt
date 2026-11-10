package com.performily.flowboard.features.notifications.infrastructure.mapper

import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.notifications.domain.entity.AppNotification
import com.performily.flowboard.features.notifications.domain.valueobject.NotificationCategory
import com.performily.flowboard.features.notifications.domain.valueobject.NotificationTarget
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Turns the recent facts of the other bounded contexts into notices. */
object NotificationMapper {

    /** Facts older than this number of days are not turned into notices. */
    private const val WINDOW_DAYS = 60L

    /** A benefit is reported as about to expire this number of days before its end date. */
    private const val EXPIRY_NOTICE_DAYS = 15L

    private val DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-PE"))

    /**
     * Notices for the own requests that were approved, rejected or returned for review.
     *
     * @param requests latest own requests.
     * @param now current moment.
     */
    fun fromRequests(requests: List<Request>, now: LocalDateTime): List<AppNotification> =
        requests.mapNotNull { request ->
            val change = request.lastChange ?: return@mapNotNull null
            if (change.occurredAt.isBefore(now.minusDays(WINDOW_DAYS))) return@mapNotNull null
            val type = "«${request.requestTypeName}»"
            val actor = change.actorName?.takeIf { it.isNotBlank() } ?: request.approverName
            val comment = change.comment?.trim()?.takeIf { it.isNotEmpty() }
            val (title, message) = when (request.status) {
                RequestStatus.APPROVED -> "Solicitud aprobada" to "$actor aprobó tu solicitud $type."
                RequestStatus.REJECTED -> "Solicitud rechazada" to
                    ("$actor rechazó tu solicitud $type." + (comment?.let { " Motivo: $it" } ?: ""))
                RequestStatus.UNDER_REVIEW -> "Solicitud devuelta" to
                    ("$actor te pidió revisar tu solicitud $type." + (comment?.let { " Comentario: $it" } ?: ""))
                RequestStatus.IN_PROGRESS, RequestStatus.CANCELLED -> return@mapNotNull null
            }
            AppNotification(
                id = "request-${request.id}-${request.status.name}-${change.id}",
                category = NotificationCategory.REQUEST,
                title = title,
                message = message,
                occurredAt = change.occurredAt,
                target = NotificationTarget.Request(request.id)
            )
        }

    /**
     * One notice with the number of requests waiting for the user to approve them.
     *
     * @param page first page of the pending approvals.
     */
    fun fromPendingApprovals(page: RequestPage): List<AppNotification> {
        val count = page.totalItems.toInt()
        val newest = page.items.maxByOrNull { it.submittedAt }
        if (count <= 0 || newest == null) return emptyList()
        return listOf(
            AppNotification(
                id = "approvals-$count-${newest.id}",
                category = NotificationCategory.APPROVAL,
                title = if (count == 1) "1 solicitud por aprobar" else "$count solicitudes por aprobar",
                message = "La más reciente es de ${newest.requesterName}: «${newest.requestTypeName}».",
                occurredAt = newest.submittedAt,
                target = NotificationTarget.PendingApprovals
            )
        )
    }

    /**
     * Notices for the own payslips that were published, paid or observed recently.
     *
     * @param payslips published payslips of the user.
     * @param today current date.
     */
    fun fromPayslips(payslips: List<Payslip>, today: LocalDate): List<AppNotification> {
        val since = today.minusDays(WINDOW_DAYS)
        return payslips.flatMap { payslip ->
            val period = payslip.period.inlineLabel
            val target = NotificationTarget.Payslip(payslip.id)
            val recent = !payslip.issueDate.isBefore(since)
            buildList {
                if (recent) {
                    add(
                        AppNotification(
                            id = "payslip-${payslip.id}-published",
                            category = NotificationCategory.PAYSLIP,
                            title = "Boleta disponible",
                            message = "Ya puedes ver tu boleta de $period.",
                            occurredAt = payslip.issueDate.atStartOfDay(),
                            target = target
                        )
                    )
                }
                val payment = payslip.payment
                val paidOn = payment.paidOn
                if (payment.status == PaymentStatus.PAID && paidOn != null && !paidOn.isBefore(since)) {
                    add(
                        AppNotification(
                            id = "payslip-${payslip.id}-paid",
                            category = NotificationCategory.PAYSLIP,
                            title = "Sueldo depositado",
                            message = "Se depositó el neto de tu boleta de $period.",
                            occurredAt = paidOn.atStartOfDay(),
                            target = target
                        )
                    )
                }
                if (payment.status == PaymentStatus.OBSERVED && recent) {
                    add(
                        AppNotification(
                            id = "payslip-${payslip.id}-observed",
                            category = NotificationCategory.PAYSLIP,
                            title = "Depósito observado",
                            message = "Hay una observación en el pago de $period" +
                                (payment.observationReason?.let { ": $it" } ?: "."),
                            occurredAt = payslip.issueDate.atStartOfDay(),
                            target = target
                        )
                    )
                }
            }
        }
    }

    /**
     * Notices for the own benefits that were assigned or delivered recently, or are about to expire.
     *
     * @param benefits current and delivered benefits of the user.
     * @param today current date.
     */
    fun fromBenefits(benefits: EmployeeBenefits, today: LocalDate): List<AppNotification> {
        val since = today.minusDays(WINDOW_DAYS)
        val current = benefits.current.flatMap { assignment ->
            val name = "«${assignment.benefitTypeName}»"
            val noticeFrom = assignment.endDate.minusDays(EXPIRY_NOTICE_DAYS)
            buildList {
                if (assignment.status == AssignmentStatus.ASSIGNED &&
                    !assignment.startDate.isBefore(since) && !assignment.startDate.isAfter(today)
                ) {
                    add(
                        AppNotification(
                            id = "benefit-${assignment.id}-assigned",
                            category = NotificationCategory.BENEFIT,
                            title = "Nuevo beneficio",
                            message = "Se te asignó $name.",
                            occurredAt = assignment.startDate.atStartOfDay(),
                            target = NotificationTarget.MyBenefits
                        )
                    )
                }
                if (!assignment.endDate.isBefore(today) && !noticeFrom.isAfter(today)) {
                    add(
                        AppNotification(
                            id = "benefit-${assignment.id}-expiring",
                            category = NotificationCategory.BENEFIT,
                            title = "Beneficio por vencer",
                            message = "$name vence el ${DAY.format(assignment.endDate)}.",
                            occurredAt = maxOf(noticeFrom, assignment.startDate).atStartOfDay(),
                            target = NotificationTarget.MyBenefits
                        )
                    )
                }
            }
        }
        val delivered = benefits.delivered.mapNotNull { assignment ->
            val deliveredOn = assignment.delivery?.deliveredOn ?: return@mapNotNull null
            if (deliveredOn.isBefore(since)) return@mapNotNull null
            AppNotification(
                id = "benefit-${assignment.id}-delivered",
                category = NotificationCategory.BENEFIT,
                title = "Beneficio entregado",
                message = "Recibiste «${assignment.benefitTypeName}» el ${DAY.format(deliveredOn)}.",
                occurredAt = deliveredOn.atStartOfDay(),
                target = NotificationTarget.MyBenefits
            )
        }
        return current + delivered
    }
}
