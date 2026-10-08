package com.performily.flowboard.features.payroll.domain.repository

import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile

/**
 * Port for storing the PDF of a payslip.
 *
 * Saves the picked file and returns the URL where it was left; the backend only registers its metadata.
 */
interface PayslipFileStorage {

    /**
     * Saves the file.
     *
     * @param file the validated payslip file
     * @return the URL where the file was stored
     */
    suspend fun store(file: PayrollSystemFile): Result<String>

    /**
     * Deletes a stored file that was never registered in the backend. It does not fail if the file does not exist.
     *
     * @param storageUrl URL returned by [store]
     */
    suspend fun delete(storageUrl: String)
}
