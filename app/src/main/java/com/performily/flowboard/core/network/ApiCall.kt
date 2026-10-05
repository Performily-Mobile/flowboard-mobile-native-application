package com.performily.flowboard.core.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ApiException(val code: String?, override val message: String) : Exception(message)

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
            message = json?.get("message")?.asString ?: "Error ${response.code()}"
        )
    } catch (exception: Exception) {
        ApiException(null, "Error ${response.code()}")
    }
}
