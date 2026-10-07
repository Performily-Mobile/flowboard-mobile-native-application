package com.performily.flowboard.features.attendance.infrastructure.di

import com.performily.flowboard.features.attendance.infrastructure.remote.AttendanceService
import com.performily.flowboard.features.attendance.infrastructure.remote.AttendanceWorkspaceService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttendanceApiModule {

    @Provides
    @Singleton
    fun provideAttendanceService(retrofit: Retrofit): AttendanceService =
        retrofit.create(AttendanceService::class.java)

    @Provides
    @Singleton
    fun provideAttendanceWorkspaceService(retrofit: Retrofit): AttendanceWorkspaceService =
        retrofit.create(AttendanceWorkspaceService::class.java)
}
