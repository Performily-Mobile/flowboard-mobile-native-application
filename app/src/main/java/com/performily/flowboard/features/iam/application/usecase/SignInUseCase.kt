package com.performily.flowboard.features.iam.application.usecase

import com.performily.flowboard.features.iam.domain.repository.IamRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val repository: IamRepository) {
    suspend operator fun invoke(username: String, password: String) = repository.signIn(username.trim(), password)
}
