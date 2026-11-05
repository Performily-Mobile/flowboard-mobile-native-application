package com.performily.flowboard.features.iam.infrastructure.di

import com.performily.flowboard.features.iam.domain.repository.IamRepository
import com.performily.flowboard.features.iam.infrastructure.repository.IamRepositoryImpl
import com.performily.flowboard.features.iam.infrastructure.remote.IamService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module @InstallIn(SingletonComponent::class)
abstract class IamRepositoryModule {
    @Binds abstract fun bindIamRepository(repository: IamRepositoryImpl): IamRepository
}
@Module @InstallIn(SingletonComponent::class)
object IamApiModule {
    @Provides fun provideIamApi(retrofit: Retrofit): IamService = retrofit.create(IamService::class.java)
}
