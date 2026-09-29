package com.cuso.tailor.network.services.settings

import com.cuso.tailor.model.settings.BaseApiResponse
import com.cuso.tailor.model.settings.CreateStageRequest
import com.cuso.tailor.model.settings.CreateTemplateRequest
import com.cuso.tailor.model.settings.DeleteStageResponse
import com.cuso.tailor.model.settings.DeleteTemplateResponse
import com.cuso.tailor.model.settings.ProductionStageDto
import com.cuso.tailor.model.settings.ProductionTemplateDto
import com.cuso.tailor.model.settings.StageListResponse
import com.cuso.tailor.model.settings.TemplateListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ServiceSettingsApiInterface {

    // ─────────────────────────────────────────────────────────────
    // Production Templates Endpoints
    // ─────────────────────────────────────────────────────────────

    @GET("/api/services/settings/production-templates/view-all")
    suspend fun getProductionTemplates(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<TemplateListResponse>

    @GET("/api/services/settings/production-templates/view-one/{id}")
    suspend fun getProductionTemplateById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BaseApiResponse<ProductionTemplateDto>>

    @POST("/api/services/settings/production-templates/create")
    suspend fun createProductionTemplate(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateTemplateRequest
    ): Response<BaseApiResponse<ProductionTemplateDto>>

    @PUT("/api/services/settings/production-templates/update-one/{id}")
    suspend fun updateProductionTemplate(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateTemplateRequest
    ): Response<BaseApiResponse<ProductionTemplateDto>>

    @DELETE("/api/services/settings/production-templates/delete-one/{id}")
    suspend fun deleteProductionTemplate(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteTemplateResponse>

    // ─────────────────────────────────────────────────────────────
    // Production Stages Endpoints
    // ─────────────────────────────────────────────────────────────

    @GET("/api/services/settings/production-stages/view-all")
    suspend fun getProductionStages(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<StageListResponse>

    @GET("/api/services/settings/production-stages/view-one/{id}")
    suspend fun getProductionStageById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BaseApiResponse<ProductionStageDto>>

    @POST("/api/services/settings/production-stages/create")
    suspend fun createProductionStage(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateStageRequest
    ): Response<BaseApiResponse<ProductionStageDto>>

    @PUT("/api/services/settings/production-stages/update-one/{id}")
    suspend fun updateProductionStage(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateStageRequest
    ): Response<BaseApiResponse<ProductionStageDto>>

    @DELETE("/api/services/settings/production-stages/delete-one/{id}")
    suspend fun deleteProductionStage(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteStageResponse>
}