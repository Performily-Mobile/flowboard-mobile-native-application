package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestFieldValue
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import javax.inject.Inject


class SubmitRequestUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(
        type: RequestType,
        period: RequestPeriod?,
        fieldValues: List<RequestFieldValue>,
        attachments: List<FileReference>
    ): Result<Request> {
        validate(type, period, fieldValues, attachments)?.let { return Result.failure(IllegalArgumentException(it)) }
        return repository.submit(
            requesterId = currentEmployee.currentEmployeeId().value,
            requestTypeId = type.id,
            period = period,
            fieldValues = fieldValues.filter { it.value.isNotBlank() }.map { it.copy(value = it.value.trim()) },
            attachments = attachments
        )
    }

    companion object {

        fun validate(
            type: RequestType,
            period: RequestPeriod?,
            fieldValues: List<RequestFieldValue>,
            attachments: List<FileReference>
        ): String? {
            if (type.deductsBalance && (period == null || period.hasHours)) {
                return "Este tipo de solicitud necesita un periodo de días completos."
            }
            type.fields.forEach { field ->
                val value = fieldValues.firstOrNull { it.key == field.key }?.value?.trim().orEmpty()
                if (field.required && value.isEmpty()) return "Completa el campo \"${field.label}\"."
                if (value.isNotEmpty() && !field.dataType.isValid(value)) return "Revisa el formato de \"${field.label}\"."
                if (value.length > MAX_VALUE) return "\"${field.label}\" admite hasta $MAX_VALUE caracteres."
            }
            if (type.requiresAttachment && attachments.isEmpty()) return "Adjunta el documento de sustento."
            return null
        }

        private const val MAX_VALUE = 500
    }
}
