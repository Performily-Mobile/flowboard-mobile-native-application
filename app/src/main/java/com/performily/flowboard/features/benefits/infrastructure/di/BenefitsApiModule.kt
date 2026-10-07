package com.performily.flowboard.features.benefits.infrastructure.di

import com.performily.flowboard.features.benefits.infrastructure.remote.BenefitService
import com.performily.flowboard.features.benefits.infrastructure.remote.BenefitTypeService
import com.performily.flowboard.features.benefits.infrastructure.remote.VacationBalanceService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BenefitsApiModule {

    @Provides
    @Singleton
    fun provideBenefitTypeService(retrofit: Retrofit): BenefitTypeService =
        retrofit.create(BenefitTypeService::class.java)

    @Provides
    @Singleton
    fun provideBenefitService(retrofit: Retrofit): BenefitService =
        retrofit.create(BenefitService::class.java)

    @Provides
    @Singleton
    fun provideVacationBalanceService(retrofit: Retrofit): VacationBalanceService =
        retrofit.create(VacationBalanceService::class.java)
}
