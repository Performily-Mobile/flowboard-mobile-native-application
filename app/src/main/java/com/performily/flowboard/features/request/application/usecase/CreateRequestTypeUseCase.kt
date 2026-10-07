package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.NewRequestField
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.repository.RequestTypeRepository
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import javax.inject.Inject


class CreateRequestTypeUseCase @Inject constructor(private val repository: RequestTypeRepository) {

    suspend operator fun invoke(
        name: String,
        description: String,
        requiresAttachment: Boolean,
        balanceDeduction: BalanceDeduction,
        fields: List<NewRequestField>
    ): Result<RequestType> {
        val cleanName = name.trim()
        val cleanDescription = description.trim()
        return when {
            cleanName.isEmpty() -> Result.failure(IllegalArgumentException("Ingresa el nombre del tipo de solicitud."))
            cleanName.length > MAX_NAME -> Result.failure(IllegalArgumentException("El nombre admite hasta $MAX_NAME caracteres."))
            cleanDescription.length > MAX_DESCRIPTION ->
                Result.failure(IllegalArgumentException("La descripción admite hasta $MAX_DESCRIPTION caracteres."))
            fields.map { it.key }.toSet().size != fields.size ->
                Result.failure(IllegalArgumentException("Hay dos campos con el mismo nombre."))
            else -> repository.createRequestType(
                name = cleanName,
                description = cleanDescription.takeIf { it.isNotEmpty() },
                requiresAttachment = requiresAttachment,
                balanceDeduction = balanceDeduction,
                fields = fields
            )
        }
    }

    companion object {
        private const val MAX_NAME = 80
        private const val MAX_DESCRIPTION = 255
        private val KEY_PATTERN = Regex("^[a-z][a-zA-Z0-9]{1,49}$")

       
        fun keyFor(label: String, existingKeys: Collection<String>): String {
            val words = java.text.Normalizer.normalize(label, java.text.Normalizer.Form.NFD)
                .replace(Regex("\\p{M}"), "")
                .replace(Regex("[^A-Za-z0-9 ]"), " ")
                .trim()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
            var base = words.mapIndexed { index, word ->
                val lower = word.lowercase()
                if (index == 0) lower else lower.replaceFirstChar { it.uppercase() }
            }.joinToString("")
            if (base.isEmpty() || !base.first().isLetter()) base = "campo$base"
            if (base.length < 2) base = "${base}Valor"
            base = base.take(45)
            var key = base
            var counter = 2
            while (key in existingKeys) key = "$base${counter++}"
            return key
        }

        fun isValidKey(key: String): Boolean = KEY_PATTERN.matches(key)
    }
}
