package com.cuso.tailor.network.sales

import com.cuso.tailor.model.sales.AddOpportunityActivityRequest
import com.cuso.tailor.model.sales.AddOpportunityActivityResponse
import com.cuso.tailor.model.sales.CreateOpportunityRequest
import com.cuso.tailor.model.sales.CreateOpportunityResponse
import com.cuso.tailor.model.sales.DeleteOpportunityResponse
import com.cuso.tailor.model.sales.OpportunityActivityHistoryResponse
import com.cuso.tailor.model.sales.OpportunityDetailResponse
import com.cuso.tailor.model.sales.OpportunityListResponse
import com.cuso.tailor.model.sales.OpportunityStagesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SalesOppertunitiesApiService {
    @GET("/api/sales/settings/opportunity/stages/dropdown")
    suspend fun getOpportunityStages(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<OpportunityStagesResponse>


    // =========================================================
    // 1. VIEW ALL
    // =========================================================
    @GET("/api/sales/opportunities/view-all")
    suspend fun getOpportunities(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<OpportunityListResponse>

    // =========================================================
    // 2. VIEW ONE
    // =========================================================
    @GET("/api/sales/opportunities/view-one/{id}")
    suspend fun getOpportunityById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<OpportunityDetailResponse>

    // =========================================================
    // 3. CREATE OPPORTUNITY
    // =========================================================
    @POST("/api/sales/opportunities/create")
    suspend fun createOpportunity(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateOpportunityRequest
    ): Response<CreateOpportunityResponse>

    // =========================================================
    // 4. DELETE ONE
    // =========================================================
    @DELETE("/api/sales/opportunities/delete-one/{id}")
    suspend fun deleteOpportunity(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteOpportunityResponse>

    // =========================================================
    // 5. ADD LOG ACTIVITY
    // =========================================================
    @POST("/api/sales/opportunities/create/log-activity")
    suspend fun addOpportunityActivity(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: AddOpportunityActivityRequest
    ): Response<AddOpportunityActivityResponse>

    // =========================================================
    // 6. LOG HISTORY
    // =========================================================
    @GET("/api/sales/opportunities/history/{id}")
    suspend fun getOpportunityActivityHistory(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") opportunityId: String
    ): Response<OpportunityActivityHistoryResponse>
}