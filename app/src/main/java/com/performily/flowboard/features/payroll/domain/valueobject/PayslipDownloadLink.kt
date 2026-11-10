package com.performily.flowboard.features.payroll.domain.valueobject

import java.time.LocalDateTime

/**
 * Temporary link the backend generates to open the file of a payslip.
 *
 * @property url address of the file.
 * @property fileName name of the file, for example "boleta-2026-08.pdf".
 * @property contentType media type of the file.
 * @property expiresAt moment the link stops working, or null when the backend did not send it.
 */
data class PayslipDownloadLink(
    val url: String,
    val fileName: String,
    val contentType: String,
    val expiresAt: LocalDateTime?
) {

    /**
     * Whether the link can be opened from another device: only web addresses can, because a local
     * path belongs to the phone that stored the file.
     */
    val isWebLink: Boolean
        get() = url.startsWith("https://") || url.startsWith("http://")
}
