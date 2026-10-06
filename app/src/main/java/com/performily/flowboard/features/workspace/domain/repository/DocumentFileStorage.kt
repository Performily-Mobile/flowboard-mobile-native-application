package com.performily.flowboard.features.workspace.domain.repository

import com.performily.flowboard.features.workspace.domain.valueobject.DocumentUpload

/**
 * Puerto de almacenamiento de archivos del expediente.
 * Guarda el archivo elegido por el usuario y devuelve la URL donde quedó.
 * La implementación se elige en infrastructure/di (hoy local, luego Firebase Storage).
 */
interface DocumentFileStorage {

    suspend fun store(upload: DocumentUpload): Result<String>
}
