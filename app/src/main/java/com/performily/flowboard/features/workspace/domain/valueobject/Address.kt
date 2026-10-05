package com.performily.flowboard.features.workspace.domain.valueobject

data class Address(
    val street: String? = null,
    val district: String? = null,
    val province: String? = null,
    val department: String? = null
) {
    val isEmpty: Boolean
        get() = listOf(street, district, province, department).all { it.isNullOrBlank() }

    val formatted: String
        get() = listOf(street, district, province, department)
            .filterNot { it.isNullOrBlank() }
            .joinToString(", ")
}
