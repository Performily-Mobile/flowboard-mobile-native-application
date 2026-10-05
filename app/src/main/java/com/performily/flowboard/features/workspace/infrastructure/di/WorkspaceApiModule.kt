package com.performily.flowboard.features.workspace.infrastructure.di

import com.performily.flowboard.features.workspace.infrastructure.remote.AreaService
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeDocumentService
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeJobAssignmentService
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeService
import com.performily.flowboard.features.workspace.infrastructure.remote.PositionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkspaceApiModule {

    @Provides
    @Singleton
    fun provideEmployeeService(retrofit: Retrofit): EmployeeService =
        retrofit.create(EmployeeService::class.java)

    @Provides
    @Singleton
    fun provideEmployeeJobAssignmentService(retrofit: Retrofit): EmployeeJobAssignmentService =
        retrofit.create(EmployeeJobAssignmentService::class.java)

    @Provides
    @Singleton
    fun provideEmployeeDocumentService(retrofit: Retrofit): EmployeeDocumentService =
        retrofit.create(EmployeeDocumentService::class.java)

    @Provides
    @Singleton
    fun provideAreaService(retrofit: Retrofit): AreaService =
        retrofit.create(AreaService::class.java)

    @Provides
    @Singleton
    fun providePositionService(retrofit: Retrofit): PositionService =
        retrofit.create(PositionService::class.java)
}
