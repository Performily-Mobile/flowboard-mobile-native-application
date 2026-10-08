package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.repository.DeviceRepository
import javax.inject.Inject

/** MA-73 · Vincula un dispositivo del inventario a un espacio (US48). */
class LinkDeviceUseCase @Inject constructor(private val repository: DeviceRepository) {
    suspend operator fun invoke(officeId: Long, deviceCode: String): Result<Device> {
        val code = deviceCode.trim().uppercase()
        if (code.isEmpty()) {
            return Result.failure(IllegalArgumentException("Ingresa el código del dispositivo."))
        }
        return repository.linkDevice(officeId, code)
    }
}
