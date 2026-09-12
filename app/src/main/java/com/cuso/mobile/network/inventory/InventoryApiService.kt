package com.cuso.mobile.network.inventory

import com.cuso.mobile.model.inventory.AdjustStockQuantityRequest
import com.cuso.mobile.model.inventory.AdjustStockRequest
import com.cuso.mobile.model.inventory.AdjustmentReasonsResponse
import com.cuso.mobile.model.inventory.CreateInventoryItemResponse
import com.cuso.mobile.model.inventory.CreateItemGroupRequest
import com.cuso.mobile.model.inventory.CreateItemGroupResponse
import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.CreatePurchaseOrderResponse
import com.cuso.mobile.model.inventory.CreateSupplierRequest
import com.cuso.mobile.model.inventory.CreateWarehouseRequest
import com.cuso.mobile.model.inventory.DecreaseStockRequest
import com.cuso.mobile.model.inventory.DeleteItemGroupResponse
import com.cuso.mobile.model.inventory.IncreaseStockRequest
import com.cuso.mobile.model.inventory.InventoryItemDetailResponse
import com.cuso.mobile.model.inventory.InventoryItemListResponse
import com.cuso.mobile.model.inventory.InventoryViewOneResponse
import com.cuso.mobile.model.inventory.ItemGroupListResponse
import com.cuso.mobile.model.inventory.ItemGroupViewOneResponse
import com.cuso.mobile.model.inventory.LowStockResponse
import com.cuso.mobile.model.inventory.ReverseAdjustmentRequest
import com.cuso.mobile.model.inventory.StockAdjustmentDetailResponse
import com.cuso.mobile.model.inventory.StockAdjustmentListResponse
import com.cuso.mobile.model.inventory.StockSummaryListResponse
import com.cuso.mobile.model.inventory.SupplierActionResponse
import com.cuso.mobile.model.inventory.SupplierDropdownResponse
import com.cuso.mobile.model.inventory.SupplierLedgerResponse
import com.cuso.mobile.model.inventory.SupplierListResponse
import com.cuso.mobile.model.inventory.SupplierViewOneResponse
import com.cuso.mobile.model.inventory.TransferStockRequest
import com.cuso.mobile.model.inventory.UpdateInventoryItemResponse
import com.cuso.mobile.model.inventory.UpdateItemGroupResponse
import com.cuso.mobile.model.inventory.UpdateWarehouseRequest
import com.cuso.mobile.model.inventory.WarehouseDropdownResponse
import com.cuso.mobile.model.inventory.WarehouseListResponse
import com.cuso.mobile.model.inventory.WarehouseMessageResponse
import com.cuso.mobile.model.inventory.WarehouseResponse
import com.cuso.mobile.model.settings.BinItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryApiService {

    // =========================================================================
    // 1. INVENTORY ITEMS
    // =========================================================================

    /**
     * Get paginated inventory items list with filters.
     */
    @GET("/api/inventory/item/view-all")
    suspend fun getInventoryItems(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<InventoryItemListResponse>

    /**
     * Get recently added inventory items.
     */
    @GET("/api/inventory/item/recent")
    suspend fun getRecentInventoryItems(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("limit") limit: Int = 10
    ): Response<InventoryItemListResponse>

    /**
     * Get single inventory item detail by ID.
     */
    @GET("/api/inventory/item/view-one/{id}")
    suspend fun getInventoryItemById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InventoryItemDetailResponse>

    /**
     * Get comprehensive single inventory item view by ID.
     */
    @GET("/api/inventory/item/view-one/{id}")
    suspend fun getInventoryViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InventoryViewOneResponse>

    /**
     * Create a new inventory item with multipart form data.
     */
    @Multipart
    @POST("/api/inventory/item/create")
    suspend fun createItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: MultipartBody.Part?
    ): Response<CreateInventoryItemResponse>

    /**
     * Update a existing inventory item with multipart form data.
     */
    @Multipart
    @PUT("/api/inventory/item/update-one/{id}")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<UpdateInventoryItemResponse>

    /**
     * Adjust stock quantity for an inventory item.
     */
    @POST("/api/inventory/item/adjust-stock")
    suspend fun adjustStock(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: AdjustStockRequest
    ): Response<InventoryItemDetailResponse>

    // =========================================================================
    // 2. ITEM GROUPS
    // =========================================================================

    /**
     * Get paginated item groups list.
     */
    @GET("/api/inventory/item-group/view-all")
    suspend fun getInventoryItemGroupList(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("search") search: String? = null
    ): Response<ItemGroupListResponse>

    /**
     * Get paginated item groups view one.
     */
    @GET("/api/inventory/item-group/view-one/{id}")
    suspend fun getInventoryItemGroupViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<ItemGroupViewOneResponse>

    /**
     * Get paginated item groups create.
     */
    @POST("/api/inventory/item-group/create")
    suspend fun createItemGroup(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateItemGroupRequest
    ): Response<CreateItemGroupResponse>

    /**
     * Update an existing item group.
     */
    @PUT("/api/inventory/item-group/update-one/{id}")
    suspend fun updateItemGroup(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateItemGroupRequest
    ): Response<UpdateItemGroupResponse>

    /**
     * Get paginated item groups delete.
     */
    @DELETE("/api/inventory/item-group/delete-one/{id}")
    suspend fun deleteItemGroup(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteItemGroupResponse>

    // =========================================================================
    // 3. LOW STOCK ALERTS & PURCHASE ORDERS
    // =========================================================================

    /**
     * Get all low stock alerts with optional warehouse filter.
     */
    @GET("/api/inventory/low-stock-alert/view-all")
    suspend fun getLowStockAlerts(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null
    ): Response<LowStockResponse>

    /**
     * Get low stock details for a specific item in a warehouse.
     */
    @GET("/api/inventory/low-stock-alert/detail/{itemId}/{warehouseId}")
    suspend fun getLowStockItemDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("itemId") itemId: String,
        @Path("warehouseId") warehouseId: String
    ): Response<BinItem.LowStockDetailResponse>

    /**
     * Create a purchase order to restock low-stock items.
     */
    @POST("/api/inventory/low-stock-alert/reorder")
    suspend fun createPurchaseOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreatePurchaseOrderRequest
    ): Response<CreatePurchaseOrderResponse>


    // =========================================================================
    // 4. STOCK ADJUSTMENTS & TRANSFERS
    // =========================================================================

    // --- 4.1 Valid Reasons ---
    @GET("/api/inventory/stock-adjustment/reasons")
    suspend fun getValidAdjustmentReasons(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<AdjustmentReasonsResponse>

//    // --- 4.2 Adjust Stock (Increase / Decrease) ---
//    @POST("/api/inventory/stock-adjustment/adjust")
//    suspend fun adjustStockQuantity(
//        @Header("Authorization") token: String,
//        @Header("X-CSRF-Token") csrfToken: String,
//        @Body request: AdjustStockQuantityRequest
//    ): Response<StockAdjustmentDetailResponse>


    // --- 1. INCREASE STOCK ENDPOINT ---
    @POST("/api/inventory/stock-adjustment/increase")
    suspend fun increaseStock(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: IncreaseStockRequest
    ): Response<StockAdjustmentDetailResponse>

    // --- 2. DECREASE STOCK ENDPOINT ---
    @POST("/api/inventory/stock-adjustment/decrease")
    suspend fun decreaseStock(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: DecreaseStockRequest
    ): Response<StockAdjustmentDetailResponse>

    // --- 4.3 Transfer Stock ---
    @POST("/api/inventory/stock-adjustment/transfer")
    suspend fun transferStock(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: TransferStockRequest
    ): Response<StockAdjustmentDetailResponse>

    // --- 4.4 Reverse Adjustment ---
    @POST("/api/inventory/stock-adjustment/{adjustmentId}/reverse")
    suspend fun reverseStockAdjustment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("adjustmentId") adjustmentId: String,
        @Body request: ReverseAdjustmentRequest = ReverseAdjustmentRequest()
    ): Response<StockAdjustmentDetailResponse>

    // --- 4.5 List Adjustments History (Paginated) ---
    /**
     * Fetch list of adjustment transaction logs.
     */
    @GET("/api/inventory/stock-adjustment/view-all")
    suspend fun getStockAdjustmentList(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<StockAdjustmentListResponse> // Returns StockAdjustmentListResponse

    // --- 4.6 Fetch Current Stock Summary (For Adjustment Screen) ---
    /**
     * Fetch current available stock by item/warehouse for adjustment.
     * Note: Verify the path against your backend route (e.g. /summary, /stock-summary/view-all, etc.)
     */
    @GET("/api/inventory/bin-stock/overview") // or your backend route for the 2nd JSON snippet
    suspend fun getStockSummaryList(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<StockSummaryListResponse>

    // --- 4.6 Get Single Adjustment by ID ---
    @GET("/api/inventory/stock-adjustment/view-one/{id}")
    suspend fun getStockAdjustmentById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<StockAdjustmentDetailResponse>

    // =========================================================================
    // 5. WAREHOUSE
    // =========================================================================

    /**
     * Get all active warehouses.
     */
    @GET("/api/inventory/settings/warehouse/view-all")
    suspend fun getAllWarehouses(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<WarehouseListResponse>

    /**
     * Get warehouse options formatted for dropdowns.
     */
    @GET("/api/inventory/settings/warehouse/dropdown")
    suspend fun getWarehouseDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<WarehouseDropdownResponse>

    /**
     * Get single warehouse details by ID.
     */
    @GET("/api/inventory/settings/warehouse/view-one/{id}")
    suspend fun getWarehouseById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<WarehouseResponse>

    /**
     * Create a new warehouse.
     */
    @POST("/api/inventory/settings/warehouse/create")
    suspend fun createWarehouse(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateWarehouseRequest
    ): Response<WarehouseResponse>

    /**
     * Update an existing warehouse.
     */
    @PUT("/api/inventory/settings/warehouse/update-one/{id}")
    suspend fun updateWarehouse(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateWarehouseRequest
    ): Response<WarehouseResponse>

    /**
     * Soft-delete warehouse by ID.
     */
    @DELETE("/api/inventory/settings/warehouse/delete-one/{id}")
    suspend fun deleteWarehouse(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<WarehouseMessageResponse>

    /**
     * Restore a deleted warehouse by ID.
     */
    @PATCH("/api/inventory/settings/warehouse/restore/{id}")
    suspend fun restoreWarehouse(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<WarehouseResponse>

    // =========================================================================
    // 6. SUPPLIER
    // =========================================================================

    @GET("/api/inventory/supplier/view-all")
    suspend fun getAllSuppliers(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<SupplierListResponse>

    @GET("/api/inventory/supplier/dropdown")
    suspend fun getSupplierDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<SupplierDropdownResponse>

    @GET("/api/inventory/supplier/view-one/{id}")
    suspend fun getSupplierById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SupplierViewOneResponse>

    @POST("/api/inventory/supplier/create")
    suspend fun createSupplier(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateSupplierRequest
    ): Response<SupplierActionResponse>

    @PUT("/api/inventory/supplier/update-one/{id}")
    suspend fun updateSupplier(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateSupplierRequest
    ): Response<SupplierActionResponse>

    @DELETE("/api/inventory/supplier/delete-one/{id}")
    suspend fun deleteSupplier(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SupplierActionResponse>

    @PATCH("/api/inventory/supplier/restore/{id}")
    suspend fun restoreSupplier(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SupplierActionResponse>

    @GET("/api/inventory/supplier/{id}/ledger")
    suspend fun getSupplierLedger(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SupplierLedgerResponse>

}