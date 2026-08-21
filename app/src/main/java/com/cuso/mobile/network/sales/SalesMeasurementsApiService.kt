package com.cuso.mobile.network.sales

import com.cuso.mobile.model.sales.MeasurementsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SalesMeasurementsApiService {
    @GET("/api/measurements/customers-last-orders")
    suspend fun getMeasurements(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<MeasurementsResponse>
}