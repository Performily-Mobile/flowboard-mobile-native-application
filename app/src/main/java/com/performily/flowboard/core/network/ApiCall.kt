package com.performily.flowboard.core.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Error returned by the backend.
 *
 * @property code backend error code, or null when the failure is not an API error.
 * @property message generic text describing the type of error.
 * @property details concrete reason (for example the business rule that was violated), if any.
 */
class ApiException(
    val code: String?,
    override val message: String,
    val details: String? = null
) : Exception(message)

suspend fun <T : Any> apiCall(request: suspend () -> Response<T>): Result<T> {
    return try {
        val response = request()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            Result.success(body)
        } else {
            Result.failure(parseError(response))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        Result.failure(ApiException(null, "No se pudo conectar con el servidor."))
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}

private fun parseError(response: Response<*>): ApiException {
    val raw = response.errorBody()?.string()
    return try {
        val json = Gson().fromJson(raw, JsonObject::class.java)
        ApiException(
            code = json?.get("code")?.asString,
            message = json?.get("message")?.asString ?: "Error ${response.code()}",
            details = json?.get("details")?.takeIf { !it.isJsonNull }?.asString
        )
    } catch (exception: Exception) {
        ApiException(null, "Error ${response.code()}")
    }
}
