package com.cuso.mobile.network.sales

import com.cuso.mobile.model.sales.*
import retrofit2.Response
import retrofit2.http.*

interface SalesCustomerApiService {
    @GET("/api/customers/view-all")
    suspend fun getCustomers(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponse>

    @GET("/api/customers")
    suspend fun getCustomersV2(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponseV2>

    @GET("/api/customers/{id}")
    suspend fun getCustomerDetailV2(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetCustomerDetailResponseV2>

    @GET("/api/customers/view-one/{id}")
    suspend fun getCustomerView(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetCustomerViewResponse>

    @PUT("/api/customers/update-one/{id}")
    suspend fun updateCustomer(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateCustomerRequest
    ): Response<UpdateCustomerResponse>

    @DELETE("/api/customers/delete-one/{id}")
    suspend fun deleteCustomer(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteCustomerResponse>

    @GET("/api/sales-orders/search-by-mobile")
    suspend fun searchCustomerByMobile(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("mobile") mobile: String
    ): Response<CustomerSearchResponse>
}