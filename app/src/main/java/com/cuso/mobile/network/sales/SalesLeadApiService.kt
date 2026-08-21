package com.cuso.mobile.network.sales

import com.cuso.mobile.model.sales.*
import retrofit2.Response
import retrofit2.http.*

interface SalesLeadApiService {
    @GET("/api/common/lead-statuses/view-all")
    suspend fun getSalesData(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<SalesResponse>

    @GET("/api/sales-leads/view-all?page=1&limit=10")
    suspend fun getSalesLeads(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<SalesSummaryResponse>

    @GET("/api/sales-leads/view-all")
    suspend fun getTableData(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<LeadsTableResponse>

    @GET("/api/sales-leads/view-one/{id}")
    suspend fun getViewOne(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<ViewOneLeadResponse>

    @POST("/api/sales-leads/create")
    suspend fun createLead(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateLeadFormRequest
    ): Response<CreateLeadFormResponse>

    @POST("/api/sales-leads/convert-to-order/{leadId}")
    suspend fun convertedToOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("leadId") leadId: String
    ): Response<ConvertLeadToOrderResponse>

    @PUT("/api/sales-leads/update-one/{id}")
    suspend fun updateLead(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateLeadRequest
    ): Response<UpdateLeadResponse>

    @DELETE("/api/sales-leads/delete-one/{id}")
    suspend fun deleteLead(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteLeadResponse>
}