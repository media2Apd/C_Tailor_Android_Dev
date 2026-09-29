package com.cuso.tailor.network.services

import com.cuso.tailor.model.service.ServiceRequestListResponse
import com.cuso.tailor.model.service.CreateServiceRequestPayload
import com.cuso.tailor.model.service.ServiceRequestDetailResponse
import com.cuso.tailor.model.service.UpdateServiceStatusPayload
import retrofit2.Response
import retrofit2.http.*

interface ServicesApiService {

    @GET("/api/services/service-requests/view-all")
    suspend fun getServiceRequests(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<ServiceRequestListResponse>

    @GET("/api/services/service-requests/view-one/{id}")
    suspend fun getServiceRequestById(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<ServiceRequestDetailResponse>

    @POST("/api/services/service-requests/create")
    suspend fun createServiceRequest(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: CreateServiceRequestPayload
    ): Response<ServiceRequestDetailResponse>

    @PUT("/api/services/service-requests/update-one/{id}")
    suspend fun updateServiceRequest(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateServiceRequestPayload
    ): Response<ServiceRequestDetailResponse>

    @PATCH("/api/services/service-requests/change-status/{id}")
    suspend fun updateServiceRequestStatus(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateServiceStatusPayload
    ): Response<ServiceRequestDetailResponse>
}