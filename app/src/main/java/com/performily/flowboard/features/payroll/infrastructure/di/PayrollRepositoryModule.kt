package com.performily.flowboard.features.payroll.infrastructure.di

import com.performily.flowboard.features.payroll.domain.repository.MyPayslipRepository
import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.domain.repository.PayrollPeriodRepository
import com.performily.flowboard.features.payroll.domain.repository.PayslipFileStorage
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.infrastructure.repository.LocalPayslipFileStorage
import com.performily.flowboard.features.payroll.infrastructure.repository.MyPayslipRepositoryImpl
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
    fun bindMyPayslipRepository(impl: MyPayslipRepositoryImpl): MyPayslipRepository

    @Binds
    fun bindPayrollPeriodRepository(impl: PayrollPeriodRepositoryImpl): PayrollPeriodRepository

    @Binds
    fun bindPayrollEmployeeDirectory(impl: WorkspacePayrollEmployeeDirectory): PayrollEmployeeDirectory

    /** Temporary storage: replace it with the real one once it is defined, as in Workspace. */
    @Binds
    fun bindPayslipFileStorage(impl: LocalPayslipFileStorage): PayslipFileStorage
}
