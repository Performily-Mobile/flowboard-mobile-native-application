package com.performily.flowboard.core.navigation

import androidx.lifecycle.ViewModel
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.core.session.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppShellViewModel @Inject constructor(
    currentEmployeeProvider: CurrentEmployeeProvider
) : ViewModel() {

    val role: UserRole = currentEmployeeProvider.currentRole()
}
