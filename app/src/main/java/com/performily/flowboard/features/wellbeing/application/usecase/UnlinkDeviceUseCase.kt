package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.repository.DeviceRepository
import javax.inject.Inject

/** Desvincula un dispositivo de un espacio; vuelve al inventario (US48). */
class UnlinkDeviceUseCase @Inject constructor(private val repository: DeviceRepository) {
    suspend operator fun invoke(officeId: Long, deviceCode: String): Result<Device> =
        repository.unlinkDevice(officeId, deviceCode)
}
