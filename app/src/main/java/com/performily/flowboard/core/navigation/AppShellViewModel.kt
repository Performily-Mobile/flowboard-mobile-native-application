package com.performily.flowboard.core.navigation

import androidx.lifecycle.ViewModel
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppShellViewModel @Inject constructor(repository: IamRepository): ViewModel() {
    val session = repository.session
    val role: UserRole get() = if (session.value?.isHr == true) UserRole.HUMAN_RESOURCES else UserRole.EMPLOYEE
}
