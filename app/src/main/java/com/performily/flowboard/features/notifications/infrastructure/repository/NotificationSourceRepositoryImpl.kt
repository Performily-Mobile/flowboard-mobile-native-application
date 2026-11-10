package com.performily.flowboard.features.notifications.infrastructure.repository

import com.performily.flowboard.features.benefits.application.usecase.GetMyBenefitsUseCase
import com.performily.flowboard.features.notifications.domain.entity.AppNotification
import com.performily.flowboard.features.notifications.domain.entity.NotificationFeed
import com.performily.flowboard.features.notifications.domain.repository.NotificationSourceRepository
import com.performily.flowboard.features.notifications.infrastructure.mapper.NotificationMapper
import com.performily.flowboard.features.payroll.application.usecase.GetMyPayslipsUseCase
import com.performily.flowboard.features.request.application.usecase.GetMyRequestsUseCase
import com.performily.flowboard.features.request.application.usecase.GetPendingApprovalsUseCase
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Reads the own requests, pending approvals, payslips and benefits through the use cases of their
 * bounded contexts, in parallel, and turns them into notices.
 */
class NotificationSourceRepositoryImpl @Inject constructor(
    private val getMyRequests: GetMyRequestsUseCase,
    private val getPendingApprovals: GetPendingApprovalsUseCase,
    private val getMyPayslips: GetMyPayslipsUseCase,
    private val getMyBenefits: GetMyBenefitsUseCase
) : NotificationSourceRepository {

    /**
     * A source that fails is skipped, so the other notices are still shown; the feed fails only
     * when no source could be read.
     */
    override suspend fun getNotifications(): Result<NotificationFeed> = coroutineScope {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val requests = async { getMyRequests() }
        val approvals = async { getPendingApprovals() }
        val payslipsThisYear = async { getMyPayslips(today.year) }
        val payslipsLastYear = async { getMyPayslips(today.year - 1) }
        val benefits = async { getMyBenefits() }

        val results: List<Result<List<AppNotification>>> = listOf(
            requests.await().map { NotificationMapper.fromRequests(it.items, now) },
            approvals.await().map { NotificationMapper.fromPendingApprovals(it) },
            payslipsThisYear.await().map { NotificationMapper.fromPayslips(it, today) },
            payslipsLastYear.await().map { NotificationMapper.fromPayslips(it, today) },
            benefits.await().map { NotificationMapper.fromBenefits(it, today) }
        )
        val read = results.mapNotNull { it.getOrNull() }
        if (read.isEmpty()) {
            Result.failure(results.firstNotNullOf { it.exceptionOrNull() })
        } else {
            Result.success(
                NotificationFeed(
                    notifications = read.flatten().distinctBy { it.id },
                    hasMissingSources = read.size < results.size
                )
            )
        }
    }
}
