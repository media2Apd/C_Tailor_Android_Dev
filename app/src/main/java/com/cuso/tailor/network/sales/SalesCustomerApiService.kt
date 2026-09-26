package com.cuso.tailor.network.sales

import com.cuso.tailor.model.sales.*
import retrofit2.Response
import retrofit2.http.*

interface SalesCustomerApiService {
    @GET("/api/sales/customers/view-all")
    suspend fun getCustomers(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponse>

    @GET("/api/sales/customers")
    suspend fun getCustomersV2(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponseV2>

    @GET("/api/sales/customers/{id}")
    suspend fun getCustomerDetailV2(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetCustomerDetailResponseV2>

    @GET("/api/sales/customers/view-one/{id}")
    suspend fun getCustomerView(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetCustomerViewResponse>

    @POST("/api/sales/customers/create")
    suspend fun createCustomer(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateCustomerRequest
    ): Response<UpdateCustomerResponse>

    @PUT("/api/sales/customers/update-one/{id}")
    suspend fun updateCustomer(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateCustomerRequest
    ): Response<GetCustomerViewResponse>

    @DELETE("/api/sales/customers/delete-one/{id}")
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