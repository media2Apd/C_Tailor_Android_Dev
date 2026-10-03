package com.cuso.tailor.network.generic

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface GenericApi {

    // GET with optional query params
    @GET
    suspend fun get(
        @Url url: String,
        @QueryMap query: Map<String, @JvmSuppressWildcards Any> = emptyMap()
    ): Response<JsonElement>

    // POST with JSON body (pass emptyMap if the API needs no body)
    @POST
    suspend fun post(
        @Url url: String,
        @Body body: Any = emptyMap<String, String>()
    ): Response<JsonElement>

    // PUT with JSON body
    @PUT
    suspend fun put(
        @Url url: String,
        @Body body: Any = emptyMap<String, String>()
    ): Response<JsonElement>

    // PATCH with JSON body
    @PATCH
    suspend fun patch(
        @Url url: String,
        @Body body: Any = emptyMap<String, String>()
    ): Response<JsonElement>

    // DELETE without body
    @DELETE
    suspend fun delete(@Url url: String): Response<JsonElement>

    // DELETE with body (only if the backend really needs it)
    @HTTP(method = "DELETE", hasBody = true)
    suspend fun deleteWithBody(
        @Url url: String,
        @Body body: Any
    ): Response<JsonElement>

    // Multipart POST: text fields (PartMap) + file parts
    @Multipart
    @POST
    suspend fun postMultipart(
        @Url url: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody> = emptyMap(),
        @Part files: List<MultipartBody.Part>? = null
    ): Response<JsonElement>

    // Multipart PUT: text fields (PartMap) + file parts
    @Multipart
    @PUT
    suspend fun putMultipart(
        @Url url: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody> = emptyMap(),
        @Part files: List<MultipartBody.Part>? = null
    ): Response<JsonElement>
}