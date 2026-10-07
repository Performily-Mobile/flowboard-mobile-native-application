package com.performily.flowboard.features.attendance.infrastructure.di

import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.infrastructure.repository.AttendanceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AttendanceRepositoryModule {

    @Binds
    fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository
}
