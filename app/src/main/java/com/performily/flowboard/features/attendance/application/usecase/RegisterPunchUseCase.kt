package com.performily.flowboard.features.attendance.application.usecase

import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.PunchType
import javax.inject.Inject

class RegisterPunchUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(type: PunchType): Result<Long> = repository.registerPunch(type)
}
