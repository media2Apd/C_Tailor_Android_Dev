package com.cuso.mobile.network.sales

import com.cuso.mobile.model.sales.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface SalesOrderApiService {
    @GET("/api/sales-orders/view-all")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): Response<OrderResponse>

    @GET("/api/sales-leads/{orderId}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String
    ): Response<OrderDetailResponse>

    @PATCH("/api/sales-leads/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Query("status") status: String
    ): Response<OrderDetailResponse>

    @Multipart
    @POST("/api/sales-orders/create-direct")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part("leadId") leadId: RequestBody? = null,
        @Part("customer") customer: RequestBody,
        @Part("branch") branch: RequestBody,
        @Part("wearerType") wearerType: RequestBody? = null,
        @Part("source") source: RequestBody? = null,
        @Part("orderType") orderType: RequestBody? = null,
        @Part("garments") garments: RequestBody,
        @Part("paymentDetails") paymentDetails: RequestBody,
        @Part("orderDate") orderDate: RequestBody,
        @Part("trialDate") trialDate: RequestBody? = null,
        @Part("deliveryDate") deliveryDate: RequestBody? = null,
        @Part("totalAmount") totalAmount: RequestBody,
        @Part("status") status: RequestBody? = null,
        @Part designImages: List<MultipartBody.Part>,
        @Part voiceNote: MultipartBody.Part?
    ): Response<CreateOrderResponse>

    @Multipart
    @PUT("/api/sales-orders/update-one/{orderId}")
    suspend fun updateOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Part("customer") customer: RequestBody,
        @Part("branch") branch: RequestBody,
        @Part("wearerType") wearerType: RequestBody? = null,
        @Part("source") source: RequestBody? = null,
        @Part("orderType") orderType: RequestBody? = null,
        @Part("garments") garments: RequestBody,
        @Part("paymentDetails") paymentDetails: RequestBody,
        @Part("orderDate") orderDate: RequestBody,
        @Part("trialDate") trialDate: RequestBody? = null,
        @Part("deliveryDate") deliveryDate: RequestBody? = null,
        @Part("totalAmount") totalAmount: RequestBody,
        @Part("existingImages") existingImages: RequestBody,
        @Part designImages: List<MultipartBody.Part>,
        @Part voiceNote: MultipartBody.Part?
    ): Response<CreateOrderResponse>

    @GET("/api/sales-orders/view-one/{orderId}")
    suspend fun getSalesOverview(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String
    ): Response<OrderOverviewApiResponse>

    @GET("/api/sales-orders/view-one/{orderId}")
    suspend fun getOrdersView(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String
    ): Response<OrderViewResponse>

    @PUT("/api/sales-orders/receive-payment/{orderId}")
    suspend fun receivePayment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Body request: ReceivePaymentRequest
    ): Response<ReceivePaymentResponse>

    @POST("/api/sales-orders/convert-to-invoice/{orderId}")
    suspend fun convertToInvoice(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Body request: ConvertToInvoiceRequest
    ): Response<ConvertToInvoiceResponse>

    @PUT("/api/sales-orders/assign-worker-to-stage/{orderId}/{garmentItemId}/cutting")
    suspend fun assignCutting(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Body request: StageAssignRequest
    ): Response<AssignStageResponse>

    @PUT("/api/sales-orders/assign-worker-to-stage/{orderId}/{garmentItemId}/stitching")
    suspend fun assignStitching(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Body request: StageAssignRequest
    ): Response<AssignStageResponse>

    @PUT("/api/sales-orders/assign-worker-to-stage/{orderId}/{garmentItemId}/qc")
    suspend fun assignQc(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Body request: StageAssignRequest
    ): Response<AssignStageResponse>

    @PUT("/api/sales-orders/update-stage-status/{orderId}/{garmentItemId}/{stageName}")
    suspend fun updateStage(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Path("stageName") stageName: String,
        @Body request: UpdateStageRequest
    ): Response<UpdateStageResponse>

    @GET("/api/sales-orders/confirmed-orders")
    suspend fun getOrderManagement(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<OrderManagementResponse>
}