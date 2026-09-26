package com.cuso.tailor.network.sales

import com.cuso.tailor.model.sales.CustomerMeasurementResponse
import com.cuso.tailor.model.sales.MeasurementsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface SalesMeasurementsApiService {
    @GET("/api/sales/customers/measurements/latest/view-all")
    suspend fun getMeasurements(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<MeasurementsResponse>

    /**
     * Retrieves all customer measurements or filtered by customerId.
     */

    @GET("/api/sales/customers/measurements/view-all/{customerId}")
    suspend fun getAvailableMeasurement(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("customerId") customerId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<CustomerMeasurementResponse>
}