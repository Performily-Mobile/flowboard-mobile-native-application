package com.performily.flowboard.features.benefits.infrastructure.local

import android.content.Context
import com.google.gson.Gson
import com.performily.flowboard.features.benefits.infrastructure.remote.VacationBalanceDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda en el teléfono la última respuesta del saldo de vacaciones de cada
 * colaborador, para mostrarla cuando no hay conexión (MA-57).
 */
@Singleton
class VacationBalanceCache @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun save(employeeId: Long, balance: VacationBalanceDto, syncedAt: LocalDateTime) {
        preferences.edit()
            .putString(balanceKey(employeeId), gson.toJson(balance))
            .putString(syncedAtKey(employeeId), syncedAt.toString())
            .apply()
    }

    fun load(employeeId: Long): CachedVacationBalance? {
        val json = preferences.getString(balanceKey(employeeId), null) ?: return null
        val syncedAt = preferences.getString(syncedAtKey(employeeId), null) ?: return null
        return runCatching {
            CachedVacationBalance(
                balance = gson.fromJson(json, VacationBalanceDto::class.java),
                syncedAt = LocalDateTime.parse(syncedAt)
            )
        }.getOrNull()
    }

    private fun balanceKey(employeeId: Long) = "vacation_balance_$employeeId"
    private fun syncedAtKey(employeeId: Long) = "vacation_balance_synced_at_$employeeId"

    private companion object {
        const val PREFERENCES_NAME = "benefits_cache"
    }
}

data class CachedVacationBalance(
    val balance: VacationBalanceDto,
    val syncedAt: LocalDateTime
)
