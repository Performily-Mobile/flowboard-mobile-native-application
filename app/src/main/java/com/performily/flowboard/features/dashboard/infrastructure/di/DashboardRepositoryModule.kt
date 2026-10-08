package com.performily.flowboard.features.dashboard.infrastructure.di

import com.performily.flowboard.features.dashboard.domain.repository.HrDashboardRepository
import com.performily.flowboard.features.dashboard.infrastructure.repository.HrDashboardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the dashboard repository interface to its implementation. */
@Module
@InstallIn(SingletonComponent::class)
interface DashboardRepositoryModule {

    /** Binds [HrDashboardRepositoryImpl] as the [HrDashboardRepository]. */
    @Binds
    fun bindHrDashboardRepository(impl: HrDashboardRepositoryImpl): HrDashboardRepository
}
