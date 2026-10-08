package com.performily.flowboard.features.payroll.infrastructure.mapper

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.valueobject.PayPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentDetails
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.domain.valueobject.PublicationStatus
import com.performily.flowboard.features.payroll.infrastructure.remote.MarkPayslipAsObservedRequestDto
import com.performily.flowboard.features.payroll.infrastructure.remote.MarkPayslipAsPaidRequestDto
import com.performily.flowboard.features.payroll.infrastructure.remote.PayslipDto
import com.performily.flowboard.features.payroll.infrastructure.remote.ReplacePayslipFileRequestDto
import com.performily.flowboard.features.payroll.infrastructure.remote.UploadPayslipRequestDto
import java.time.LocalDate

object PayslipMapper {

    private const val DEFAULT_CONTENT_TYPE = "application/pdf"

    /**
     * Maps a payslip received from the backend to the domain entity.
     *
     * The backend does not expose the location of the file, so the storage URL stays empty: the
     * file is downloaded through a temporary link. Server data is rebuilt without the creation
     * rules (future dates), because the clock or time zone of the phone may differ from the server.
     *
     * @param dto the payslip as sent by the backend
     * @return the domain payslip
     */
    fun toDomain(dto: PayslipDto): Payslip = Payslip(
        id = dto.id,
        employeeId = EmployeeId(dto.employeeId),
        payrollPeriodId = dto.payrollPeriodId,
        period = PayPeriod(dto.periodYear, dto.periodMonth),
        file = FileReference(
            fileName = dto.fileName,
            contentType = dto.contentType ?: DEFAULT_CONTENT_TYPE,
            sizeInBytes = dto.sizeInBytes,
            storageUrl = ""
        ),
        issueDate = LocalDate.parse(dto.issueDate),
        netAmount = Money(dto.netAmount, dto.currency ?: Money.DEFAULT_CURRENCY),
        publicationStatus = PublicationStatus.valueOf(dto.publicationStatus),
        payment = toPaymentDetails(dto),
        employeeName = dto.employeeName?.trim()?.takeIf { it.isNotEmpty() }
    )

    private fun toPaymentDetails(dto: PayslipDto): PaymentDetails =
        when (PaymentStatus.valueOf(dto.paymentStatus)) {
            PaymentStatus.PENDING -> PaymentDetails.pending()
            PaymentStatus.PAID -> PaymentDetails(PaymentStatus.PAID, paidOn = LocalDate.parse(requireNotNull(dto.paidOn)))
            PaymentStatus.OBSERVED -> PaymentDetails.observed(requireNotNull(dto.observationReason))
        }

    fun toUploadRequest(
        employeeId: EmployeeId,
        payrollPeriodId: Long,
        file: FileReference,
        issueDate: LocalDate,
        netAmount: Money
    ): UploadPayslipRequestDto = UploadPayslipRequestDto(
        employeeId = employeeId.value,
        payrollPeriodId = payrollPeriodId,
        fileName = file.fileName,
        contentType = file.contentType,
        sizeInBytes = file.sizeInBytes,
        storageUrl = file.storageUrl,
        issueDate = issueDate.toString(),
        netAmount = netAmount.amount,
        currency = netAmount.currency
    )

    fun toReplaceFileRequest(
        file: FileReference,
        issueDate: LocalDate,
        netAmount: Money
    ): ReplacePayslipFileRequestDto = ReplacePayslipFileRequestDto(
        fileName = file.fileName,
        contentType = file.contentType,
        sizeInBytes = file.sizeInBytes,
        storageUrl = file.storageUrl,
        issueDate = issueDate.toString(),
        netAmount = netAmount.amount,
        currency = netAmount.currency
    )

    fun toMarkAsPaidRequest(paidOn: LocalDate): MarkPayslipAsPaidRequestDto =
        MarkPayslipAsPaidRequestDto(paidOn = paidOn.toString())

    fun toMarkAsObservedRequest(reason: String): MarkPayslipAsObservedRequestDto =
        MarkPayslipAsObservedRequestDto(reason = reason)
}
