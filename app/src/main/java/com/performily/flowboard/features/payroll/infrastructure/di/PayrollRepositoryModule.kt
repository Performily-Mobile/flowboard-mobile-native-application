package com.performily.flowboard.features.payroll.infrastructure.di

import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.domain.repository.PayrollPeriodRepository
import com.performily.flowboard.features.payroll.domain.repository.PayslipFileStorage
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.infrastructure.repository.LocalPayslipFileStorage
import com.performily.flowboard.features.payroll.infrastructure.repository.PayrollPeriodRepositoryImpl
import com.performily.flowboard.features.payroll.infrastructure.repository.PayslipRepositoryImpl
import com.performily.flowboard.features.payroll.infrastructure.repository.WorkspacePayrollEmployeeDirectory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface PayrollRepositoryModule {

    @Binds
    fun bindPayslipRepository(impl: PayslipRepositoryImpl): PayslipRepository

    @Binds
    fun bindPayrollPeriodRepository(impl: PayrollPeriodRepositoryImpl): PayrollPeriodRepository

    @Binds
    fun bindPayrollEmployeeDirectory(impl: WorkspacePayrollEmployeeDirectory): PayrollEmployeeDirectory

    // TEMPORAL: reemplazar por el almacenamiento real cuando se defina (igual que en Workspace).
    @Binds
    fun bindPayslipFileStorage(impl: LocalPayslipFileStorage): PayslipFileStorage
}
