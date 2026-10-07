package com.performily.flowboard.features.wellbeing.infrastructure.di

import com.performily.flowboard.features.wellbeing.infrastructure.remote.DeviceService
import com.performily.flowboard.features.wellbeing.infrastructure.remote.OfficeService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WellbeingApiModule {

    @Provides
    @Singleton
    fun provideOfficeService(retrofit: Retrofit): OfficeService =
        retrofit.create(OfficeService::class.java)

    @Provides
    @Singleton
    fun provideDeviceService(retrofit: Retrofit): DeviceService =
        retrofit.create(DeviceService::class.java)
}
