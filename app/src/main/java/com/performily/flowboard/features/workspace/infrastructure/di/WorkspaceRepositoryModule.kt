package com.performily.flowboard.features.workspace.infrastructure.di

import com.performily.flowboard.features.workspace.domain.repository.AreaRepository
import com.performily.flowboard.features.workspace.domain.repository.DocumentFileStorage
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.repository.PositionRepository
import com.performily.flowboard.features.workspace.infrastructure.repository.AreaRepositoryImpl
import com.performily.flowboard.features.workspace.infrastructure.repository.EmployeeRepositoryImpl
import com.performily.flowboard.features.workspace.infrastructure.repository.LocalDocumentFileStorage
import com.performily.flowboard.features.workspace.infrastructure.repository.PositionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WorkspaceRepositoryModule {

    @Binds
    fun bindEmployeeRepository(impl: EmployeeRepositoryImpl): EmployeeRepository

    @Binds
    fun bindAreaRepository(impl: AreaRepositoryImpl): AreaRepository

    @Binds
    fun bindPositionRepository(impl: PositionRepositoryImpl): PositionRepository

    // TEMPORAL: reemplazar por la implementación de Firebase Storage cuando se defina.
    @Binds
    fun bindDocumentFileStorage(impl: LocalDocumentFileStorage): DocumentFileStorage
}
