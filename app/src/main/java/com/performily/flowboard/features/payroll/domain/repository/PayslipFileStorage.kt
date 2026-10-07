package com.performily.flowboard.features.payroll.domain.repository

import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile

/**
 * Puerto de almacenamiento del PDF de la boleta.
 * Guarda el archivo elegido y devuelve la URL donde quedó; el backend solo registra sus metadatos.
 */
interface PayslipFileStorage {

    suspend fun store(file: PayrollSystemFile): Result<String>
}
