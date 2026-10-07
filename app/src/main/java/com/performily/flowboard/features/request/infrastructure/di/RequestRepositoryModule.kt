package com.performily.flowboard.features.request.infrastructure.di

import com.performily.flowboard.features.request.domain.repository.RequestAttachmentStorage
import com.performily.flowboard.features.request.domain.repository.RequestEmployeeDirectory
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import com.performily.flowboard.features.request.domain.repository.RequestTypeRepository
import com.performily.flowboard.features.request.domain.repository.VacationBalanceProvider
import com.performily.flowboard.features.request.infrastructure.acl.BenefitsVacationBalanceAdapter
import com.performily.flowboard.features.request.infrastructure.acl.WorkspaceEmployeeDirectoryAdapter
import com.performily.flowboard.features.request.infrastructure.repository.LocalRequestAttachmentStorage
import com.performily.flowboard.features.request.infrastructure.repository.RequestRepositoryImpl
import com.performily.flowboard.features.request.infrastructure.repository.RequestTypeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RequestRepositoryModule {

    @Binds
    fun bindRequestRepository(impl: RequestRepositoryImpl): RequestRepository

    @Binds
    fun bindRequestTypeRepository(impl: RequestTypeRepositoryImpl): RequestTypeRepository

    @Binds
    fun bindRequestEmployeeDirectory(impl: WorkspaceEmployeeDirectoryAdapter): RequestEmployeeDirectory

    @Binds
    fun bindVacationBalanceProvider(impl: BenefitsVacationBalanceAdapter): VacationBalanceProvider

    // TEMPORAL: reemplazar por la implementación de Firebase Storage cuando se defina.
    @Binds
    fun bindRequestAttachmentStorage(impl: LocalRequestAttachmentStorage): RequestAttachmentStorage
}
