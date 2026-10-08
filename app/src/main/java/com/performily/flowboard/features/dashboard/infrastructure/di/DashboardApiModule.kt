package com.performily.flowboard.features.dashboard.infrastructure.di

import com.performily.flowboard.features.dashboard.infrastructure.remote.DashboardAttendanceService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** Provides the Retrofit services used by the dashboard. */
@Module
@InstallIn(SingletonComponent::class)
object DashboardApiModule {

    /** Creates the Attendance summary service. */
    @Provides
    @Singleton
    fun provideDashboardAttendanceService(retrofit: Retrofit): DashboardAttendanceService =
        retrofit.create(DashboardAttendanceService::class.java)
}
