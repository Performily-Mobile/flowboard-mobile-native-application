package com.performily.flowboard.features.request.domain.valueobject


data class AttachmentUpload(
    val sourceUri: String,
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long
) {
    init {
        require(sourceUri.isNotBlank()) { "Selecciona un archivo." }
        require(fileName.isNotBlank()) { "El archivo debe tener nombre." }
        require(contentType in ALLOWED_CONTENT_TYPES) { "Solo se aceptan archivos PDF, JPG o PNG." }
        require(sizeInBytes > 0) { "El archivo está vacío." }
        require(sizeInBytes <= MAX_SIZE_IN_BYTES) { "El archivo supera los 5 MB." }
    }

    companion object {
        const val MAX_SIZE_IN_BYTES: Long = 5L * 1024 * 1024
        val ALLOWED_CONTENT_TYPES = setOf("application/pdf", "image/jpeg", "image/png")
    }
}
