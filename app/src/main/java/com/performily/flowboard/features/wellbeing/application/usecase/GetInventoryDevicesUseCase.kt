package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.repository.DeviceRepository
import javax.inject.Inject

/** MA-73 · Dispositivos disponibles en inventario para vincular (US48). */
class GetInventoryDevicesUseCase @Inject constructor(private val repository: DeviceRepository) {
    suspend operator fun invoke(): Result<List<Device>> =
        repository.getInventoryDevices().map { devices -> devices.sortedBy { it.code } }
}
