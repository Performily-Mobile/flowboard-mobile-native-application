package com.performily.flowboard.features.wellbeing.infrastructure.di

import com.performily.flowboard.features.wellbeing.domain.repository.DeviceRepository
import com.performily.flowboard.features.wellbeing.domain.repository.OfficeRepository
import com.performily.flowboard.features.wellbeing.domain.repository.ReadingRepository
import com.performily.flowboard.features.wellbeing.domain.repository.ThresholdRepository
import com.performily.flowboard.features.wellbeing.infrastructure.repository.DeviceRepositoryImpl
import com.performily.flowboard.features.wellbeing.infrastructure.repository.OfficeRepositoryImpl
import com.performily.flowboard.features.wellbeing.infrastructure.repository.ReadingRepositoryImpl
import com.performily.flowboard.features.wellbeing.infrastructure.repository.ThresholdRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WellbeingRepositoryModule {

    @Binds
    fun bindOfficeRepository(impl: OfficeRepositoryImpl): OfficeRepository

    @Binds
    fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    fun bindThresholdRepository(impl: ThresholdRepositoryImpl): ThresholdRepository

    @Binds
    fun bindReadingRepository(impl: ReadingRepositoryImpl): ReadingRepository
}
