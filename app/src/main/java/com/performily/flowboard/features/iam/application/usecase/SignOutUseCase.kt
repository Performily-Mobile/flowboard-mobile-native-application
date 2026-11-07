package com.performily.flowboard.features.iam.application.usecase

import com.performily.flowboard.features.iam.domain.repository.IamRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val repository: IamRepository) {
    suspend operator fun invoke() = repository.signOut()
}
