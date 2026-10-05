package com.performily.flowboard.features.workspace.domain.valueobject

import java.time.LocalDate

data class EmploymentPeriod(
    val hireDate: LocalDate,
    val contractEndDate: LocalDate? = null
) {
    init {
        require(contractEndDate == null || !contractEndDate.isBefore(hireDate)) {
            "La fecha de fin de contrato no puede ser anterior a la fecha de ingreso."
        }
    }

    val isOpenEnded: Boolean get() = contractEndDate == null

    companion object {
        fun of(contractType: ContractType, hireDate: LocalDate, contractEndDate: LocalDate?): EmploymentPeriod {
            require(contractType != ContractType.FIXED_TERM || contractEndDate != null) {
                "Un contrato a plazo fijo requiere fecha de fin."
            }
            return EmploymentPeriod(hireDate, contractEndDate)
        }
    }
}
