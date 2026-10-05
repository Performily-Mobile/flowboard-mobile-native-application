package com.performily.flowboard.core.domain

data class FileReference(
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long,
    val storageUrl: String
) {
    init {
        require(fileName.isNotBlank()) { "El archivo debe tener nombre." }
        require(sizeInBytes > 0) { "El archivo está vacío." }
    }
}
