package com.performily.flowboard.features.payroll.infrastructure.di

import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollPeriodService
import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollWorkspaceService
import com.performily.flowboard.features.payroll.infrastructure.remote.PayslipService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PayrollApiModule {

    @Provides
    @Singleton
    fun providePayslipService(retrofit: Retrofit): PayslipService =
        retrofit.create(PayslipService::class.java)

    @Provides
    @Singleton
    fun providePayrollPeriodService(retrofit: Retrofit): PayrollPeriodService =
        retrofit.create(PayrollPeriodService::class.java)

    @Provides
    @Singleton
    fun providePayrollWorkspaceService(retrofit: Retrofit): PayrollWorkspaceService =
        retrofit.create(PayrollWorkspaceService::class.java)
}
