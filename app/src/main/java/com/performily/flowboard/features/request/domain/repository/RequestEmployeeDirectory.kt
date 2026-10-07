package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.features.request.domain.entity.RequestPerson


interface RequestEmployeeDirectory {
    suspend fun getPerson(employeeId: Long): Result<RequestPerson>
    suspend fun getPeople(): Result<List<RequestPerson>>
    suspend fun getTeam(managerId: Long): Result<List<RequestPerson>>
}
