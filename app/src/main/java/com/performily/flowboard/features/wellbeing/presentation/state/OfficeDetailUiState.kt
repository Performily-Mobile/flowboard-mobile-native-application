package com.performily.flowboard.features.wellbeing.presentation.state

import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus

/**
 * Form of the "Link device" sheet (MA-73).
 *
 * @property inventoryError error while loading the inventory, shown instead of an empty inventory message
 */
data class LinkDeviceForm(
    val code: String = "",
    val codeError: String? = null,
    val inventory: List<Device> = emptyList(),
    val isLoadingInventory: Boolean = false,
    val isLinking: Boolean = false,
    val inventoryError: String? = null
)

/**
 * MA-72 - Indicators of an office, and MA-81 - no readings.
 *
 * @property errorMessage error of the last load; when [status] is present it means the data is stale
 * @property snackbarMessage message for the snackbar; the screen shows it and clears it
 */
data class OfficeDetailUiState(
    val officeId: Long? = null,
    val isLoading: Boolean = false,
    val status: OfficeStatus? = null,
    val errorMessage: String? = null,
    val isLinkSheetVisible: Boolean = false,
    val linkForm: LinkDeviceForm = LinkDeviceForm(),
    val deviceToUnlink: Device? = null,
    val isUnlinking: Boolean = false,
    val snackbarMessage: String? = null
)
