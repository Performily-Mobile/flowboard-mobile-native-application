package com.performily.flowboard.features.payroll.infrastructure.remote

/** PayslipDownloadUrlResource of the backend (GET /payslips/me/{payslipId}/download-url). */
data class PayslipDownloadUrlDto(
    val downloadUrl: String,
    val fileName: String?,
    val contentType: String?,
    val expiresAt: String?
)
