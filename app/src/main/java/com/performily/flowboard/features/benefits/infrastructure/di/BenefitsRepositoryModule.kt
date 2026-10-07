package com.performily.flowboard.features.benefits.infrastructure.di

import com.performily.flowboard.features.benefits.domain.repository.BenefitAssignmentRepository
import com.performily.flowboard.features.benefits.domain.repository.BenefitTypeRepository
import com.performily.flowboard.features.benefits.domain.repository.VacationBalanceRepository
import com.performily.flowboard.features.benefits.domain.repository.WorkspaceDirectory
import com.performily.flowboard.features.benefits.infrastructure.acl.WorkspaceDirectoryAdapter
import com.performily.flowboard.features.benefits.infrastructure.repository.BenefitAssignmentRepositoryImpl
import com.performily.flowboard.features.benefits.infrastructure.repository.BenefitTypeRepositoryImpl
import com.performily.flowboard.features.benefits.infrastructure.repository.VacationBalanceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BenefitsRepositoryModule {

    @Binds
    fun bindBenefitTypeRepository(impl: BenefitTypeRepositoryImpl): BenefitTypeRepository

    @Binds
    fun bindBenefitAssignmentRepository(impl: BenefitAssignmentRepositoryImpl): BenefitAssignmentRepository

    @Binds
    fun bindVacationBalanceRepository(impl: VacationBalanceRepositoryImpl): VacationBalanceRepository

    @Binds
    fun bindWorkspaceDirectory(impl: WorkspaceDirectoryAdapter): WorkspaceDirectory
}
