package com.performily.flowboard.features.iam.domain.entity

data class Account(val id: Long, val employeeId: Long, val username: String, val role: String, val status: String)
