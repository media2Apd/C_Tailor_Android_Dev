package com.cuso.tailor.repository

import com.cuso.tailor.network.generic.GenericApi
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val api: GenericApi,
    private val gson: Gson // same Gson from NetworkModule (custom deserializers included)
) {

    // MAIN FUNCTION: call this from any ViewModel.
    // T = the data class you want back (e.g. ShiftListResponse, List<RoleItem>, JsonObject)
    suspend inline fun <reified T> request(
        noinline call: suspend GenericApi.() -> Response<JsonElement>
    ): Result<T> = execute(object : TypeToken<T>() {}.type, call)

    // Runs the call, checks success, converts JSON -> data class
    suspend fun <T> execute(
        type: Type,
        call: suspend GenericApi.() -> Response<JsonElement>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = api.call()
            if (response.isSuccessful) {
                val json: JsonElement = response.body() ?: JsonNull.INSTANCE
                Result.success(gson.fromJson<T>(json, type))
            } else {
                Result.failure(
                    Exception(parseError(response.errorBody()?.string(), response.code()))
                )
            }
        } catch (e: CancellationException) {
            // Never swallow coroutine cancellation
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reads "message" from the error JSON, otherwise falls back to raw text / status code
    private fun parseError(raw: String?, code: Int): String {
        return try {
            gson.fromJson(raw, JsonObject::class.java)
                ?.get("message")?.asString
                ?: "Error $code"
        } catch (e: Exception) {
            if (raw.isNullOrBlank()) "Error $code" else raw
        }
    }

    // ---------- Helpers ----------

    // Builds a query map and drops null values
    // Usage: query("page" to 1, "search" to null) -> {page=1}
    fun query(vararg pairs: Pair<String, Any?>): Map<String, Any> =
        pairs.filter { it.second != null }.associate { it.first to it.second!! }

    // String -> multipart text field
    fun text(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())

    // File -> multipart file part (fieldName = the key the backend expects)
    fun filePart(fieldName: String, file: File, mime: String = "image/*"): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            fieldName,
            file.name,
            file.asRequestBody(mime.toMediaTypeOrNull())
        )
}