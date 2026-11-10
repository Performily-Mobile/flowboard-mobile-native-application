package com.performily.flowboard.features.payroll.infrastructure.mapper

import com.performily.flowboard.features.payroll.domain.entity.PayslipDetail
import com.performily.flowboard.features.payroll.domain.valueobject.PayslipDownloadLink
import com.performily.flowboard.features.payroll.infrastructure.remote.PayslipDetailDto
import com.performily.flowboard.features.payroll.infrastructure.remote.PayslipDownloadUrlDto
import java.time.LocalDateTime

/** Maps the employee payslip responses of the backend to the payroll domain. */
object MyPayslipMapper {

    private const val DEFAULT_CONTENT_TYPE = "application/pdf"

    /**
     * Maps the payslip detail, dropping blank employer data.
     *
     * @param dto detail as sent by the backend.
     * @return the domain detail.
     */
    fun toDetail(dto: PayslipDetailDto): PayslipDetail = PayslipDetail(
        payslip = PayslipMapper.toDomain(dto.payslip),
        companyLegalName = dto.companyLegalName?.trim()?.takeIf { it.isNotEmpty() },
        companyTaxId = dto.companyTaxId?.trim()?.takeIf { it.isNotEmpty() }
    )

    /**
     * Maps the temporary download link; an expiration date that cannot be read becomes null.
     *
     * @param dto link as sent by the backend.
     * @return the domain link.
     */
    fun toDownloadLink(dto: PayslipDownloadUrlDto): PayslipDownloadLink = PayslipDownloadLink(
        url = dto.downloadUrl,
        fileName = dto.fileName.orEmpty(),
        contentType = dto.contentType ?: DEFAULT_CONTENT_TYPE,
        expiresAt = dto.expiresAt?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
    )
}
