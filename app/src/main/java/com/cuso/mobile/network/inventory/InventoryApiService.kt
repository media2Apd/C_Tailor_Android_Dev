package com.cuso.mobile.network.inventory

import com.cuso.mobile.model.inventory.*
import com.cuso.mobile.model.settings.BinItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface InventoryApiService {
    @GET("/api/inventory/item/view-all")
    suspend fun getInventoryItems(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<InventoryItemListResponse>

    @GET("/api/inventory/item/view-one/{id}")
    suspend fun getInventoryItemById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InventoryItemDetailResponse>

    @GET("/api/inventory/item/recent")
    suspend fun getRecentInventoryItems(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("limit") limit: Int = 10
    ): Response<InventoryItemListResponse>

    @POST("/api/inventory/item/adjust-stock")
    suspend fun adjustStock(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: AdjustStockRequest
    ): Response<InventoryItemDetailResponse>

    @Multipart
    @POST("/api/inventory/item/create")
    suspend fun createInventoryItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<InventoryItemDetailResponse>

    @GET("/api/inventory/item/view-one/{id}")
    suspend fun getInventoryViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InventoryViewOneResponse>

    @GET("/api/inventory/low-stock-alert/view-all")
    suspend fun getLowStockAlerts(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null
    ): Response<LowStockResponse>

    @POST("/api/inventory/low-stock-alert/reorder")
    suspend fun createPurchaseOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreatePurchaseOrderRequest
    ): Response<CreatePurchaseOrderResponse>

    @GET("/api/inventory/low-stock-alert/detail/{itemId}/{warehouseId}")
    suspend fun getLowStockItemDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("itemId") itemId: String,
        @Path("warehouseId") warehouseId: String
    ): Response<BinItem.LowStockDetailResponse>
}