package com.performily.flowboard.features.request.infrastructure.di

import com.performily.flowboard.features.request.infrastructure.remote.RequestService
import com.performily.flowboard.features.request.infrastructure.remote.RequestTypeService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object RequestApiModule {

    @Provides
    @Singleton
    fun provideRequestService(retrofit: Retrofit): RequestService =
        retrofit.create(RequestService::class.java)

    @Provides
    @Singleton
    fun provideRequestTypeService(retrofit: Retrofit): RequestTypeService =
        retrofit.create(RequestTypeService::class.java)
}
