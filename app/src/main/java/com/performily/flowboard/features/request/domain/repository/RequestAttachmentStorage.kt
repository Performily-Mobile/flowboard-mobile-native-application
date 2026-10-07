package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.features.request.domain.valueobject.AttachmentUpload

/**
 * Puerto de almacenamiento de los sustentos de una solicitud.
 * Guarda el archivo elegido por el usuario y devuelve la URL donde quedó.
 * La implementación se elige en infrastructure/di (hoy local, luego Firebase Storage).
 */
interface RequestAttachmentStorage {
    suspend fun store(upload: AttachmentUpload): Result<String>
}
