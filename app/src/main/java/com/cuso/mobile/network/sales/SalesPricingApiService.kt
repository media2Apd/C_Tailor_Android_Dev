package com.cuso.mobile.network.sales

import com.cuso.mobile.model.sales.*
import retrofit2.Response
import retrofit2.http.*

interface SalesPricingApiService {
    @POST("/api/pricing-quotations/garment-pricing/set-price-for-garment")
    suspend fun savePricingQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: PricingQuotationSaveRequest
    ): Response<PricingQuotationSaveResponse>

    @GET("/api/pricing-quotations/garment-pricing/view-all")
    suspend fun getGarmentPricingList(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): ApiResponse<List<GarmentPricingListItemDto>>

    @GET("/api/pricing-quotations/garment-pricing/view-one/{id}")
    suspend fun getGarmentPricingDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): ApiResponse<GarmentPricingDetailDto>

    @PUT("/api/pricing-quotations/garment-pricing/update-price-for-garment/{id}")
    suspend fun updatePricingQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: PricingQuotationSaveRequest
    ): Response<PricingQuotationSaveResponse>

    @GET("/api/pricing-quotations/garment-pricing/options")
    suspend fun getGarmentPricing(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<GarmentPricingResponse>

    @GET("/api/quotations/view-all")
    suspend fun getQuotations(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<QuotationListResponse>

    @POST("/api/quotations/create")
    suspend fun createQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateQuotationRequest
    ): Response<CreateQuotationResponse>

    @DELETE("/api/quotations/delete-one/{id}")
    suspend fun deleteQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<QuotationDeleteResponse>

    @GET("/api/quotations/view-one/{id}")
    suspend fun getQuotationById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<QuotationDetailResponse>
}