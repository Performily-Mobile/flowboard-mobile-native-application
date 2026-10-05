package com.performily.flowboard.features.workspace.domain.valueobject

enum class DocumentType(val category: DocumentCategory) {
    IDENTITY_DOCUMENT(DocumentCategory.PERSONAL),
    EMPLOYMENT_CONTRACT(DocumentCategory.CONTRACT),
    CONTRACT_ADDENDUM(DocumentCategory.CONTRACT),
    PENSION_AFFILIATION(DocumentCategory.PERSONAL),
    HEALTH_INSURANCE(DocumentCategory.PERSONAL),
    EDUCATION_CERTIFICATE(DocumentCategory.CERTIFICATE),
    CRIMINAL_RECORD_CERTIFICATE(DocumentCategory.CERTIFICATE),
    WARNING_LETTER(DocumentCategory.CONTRACT),
    COMMENDATION_LETTER(DocumentCategory.CERTIFICATE),
    CERTIFICATE_OF_EMPLOYMENT(DocumentCategory.CERTIFICATE)
}

enum class DocumentCategory {
    CONTRACT,
    CERTIFICATE,
    PERSONAL
}
