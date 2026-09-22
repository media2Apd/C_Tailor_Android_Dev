package com.cuso.tailor.network.inventory

import com.cuso.tailor.model.inventory.AddRequisitionCommentRequest
import com.cuso.tailor.model.inventory.AdjustBulkStockRequest
import com.cuso.tailor.model.inventory.AdjustStockRequest
import com.cuso.tailor.model.inventory.AdjustmentReasonsResponse
import com.cuso.tailor.model.inventory.AllReceivesResponse
import com.cuso.tailor.model.inventory.AssignStockLocationRequest
import com.cuso.tailor.model.inventory.AssignStockLocationResponse
import com.cuso.tailor.model.inventory.BarcodeDetailResponse
import com.cuso.tailor.model.inventory.BarcodeListResponse
import com.cuso.tailor.model.inventory.BaseBulkResponse
import com.cuso.tailor.model.inventory.BinDropdownResponse
import com.cuso.tailor.model.inventory.BuildBulkItemRequest
import com.cuso.tailor.model.inventory.BulkItemDetailResponse
import com.cuso.tailor.model.inventory.BulkItemListResponse
import com.cuso.tailor.model.inventory.ConvertToBillResponse
import com.cuso.tailor.model.inventory.CreateBillRequest
import com.cuso.tailor.model.inventory.CreateBillResponse
import com.cuso.tailor.model.inventory.CreateInventoryItemResponse
import com.cuso.tailor.model.inventory.CreateItemGroupResponse
import com.cuso.tailor.model.inventory.CreatePurchaseOrderRequest
import com.cuso.tailor.model.inventory.CreatePurchaseOrderResponse
import com.cuso.tailor.model.inventory.CreateRequisitionRequest
import com.cuso.tailor.model.inventory.CreateSupplierRequest
import com.cuso.tailor.model.inventory.CreateWarehouseRequest
import com.cuso.tailor.model.inventory.DecreaseStockRequest
import com.cuso.tailor.model.inventory.DeleteBarcodeResponse
import com.cuso.tailor.model.inventory.DeleteInventoryItemResponse
import com.cuso.tailor.model.inventory.DeleteItemGroupResponse
import com.cuso.tailor.model.inventory.DeleteRequisitionResponse
import com.cuso.tailor.model.inventory.FloorDropdownResponse
import com.cuso.tailor.model.inventory.GenerateBarcodeRequest
import com.cuso.tailor.model.inventory.IncreaseStockRequest
import com.cuso.tailor.model.inventory.InventoryItemDetailResponse
import com.cuso.tailor.model.inventory.InventoryItemListResponse
import com.cuso.tailor.model.inventory.InventoryViewOneResponse
import com.cuso.tailor.model.inventory.ItemGroupListResponse
import com.cuso.tailor.model.inventory.ItemGroupViewOneResponse
import com.cuso.tailor.model.inventory.LowStockResponse
import com.cuso.tailor.model.inventory.POBillConvertResponse
import com.cuso.tailor.model.inventory.PaymentTermsResponse
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.PurchaseOrderDetailResponse
import com.cuso.tailor.model.inventory.PurchaseOrderListResponse
import com.cuso.tailor.model.inventory.PurchaseOrderSingleResponse
import com.cuso.tailor.model.inventory.PurchaseOrderSummaryResponse
import com.cuso.tailor.model.inventory.PurchaseReceiveResponse
import com.cuso.tailor.model.inventory.RackDropdownResponse
import com.cuso.tailor.model.inventory.ReceiveHistoryByPoResponse
import com.cuso.tailor.model.inventory.ReceivePurchaseOrderRequest
import com.cuso.tailor.model.inventory.RequisitionApprovalActionRequest
import com.cuso.tailor.model.inventory.RequisitionListResponse
import com.cuso.tailor.model.inventory.RequisitionSingleResponse
import com.cuso.tailor.model.inventory.ReverseAdjustmentRequest
import com.cuso.tailor.model.inventory.SafetyStockResponse
import com.cuso.tailor.model.inventory.SectionDropdownResponse
import com.cuso.tailor.model.inventory.SingleReceiveResponse
import com.cuso.tailor.model.inventory.StockAdjustmentDetailResponse
import com.cuso.tailor.model.inventory.StockAdjustmentListResponse
import com.cuso.tailor.model.inventory.StockLocationItemListResponse
import com.cuso.tailor.model.inventory.StockLocationViewOneResponse
import com.cuso.tailor.model.inventory.StockSummaryListResponse
import com.cuso.tailor.model.inventory.SubmitForApprovalRequest
import com.cuso.tailor.model.inventory.SubmitForApprovalResponse
import com.cuso.tailor.model.inventory.SupplierActionResponse
import com.cuso.tailor.model.inventory.SupplierDropdownResponse
import com.cuso.tailor.model.inventory.SupplierLedgerResponse
import com.cuso.tailor.model.inventory.SupplierListResponse
import com.cuso.tailor.model.inventory.SupplierViewOneResponse
import com.cuso.tailor.model.inventory.TaxGroupsResponse
import com.cuso.tailor.model.inventory.TransferStockRequest
import com.cuso.tailor.model.inventory.UpdateInventoryItemResponse
import com.cuso.tailor.model.inventory.UpdateItemGroupResponse
import com.cuso.tailor.model.inventory.UpdateWarehouseRequest
import com.cuso.tailor.model.inventory.ViewMultipleReceivesRequest
import com.cuso.tailor.model.inventory.ViewMultipleReceivesResponse
import com.cuso.tailor.model.inventory.WarehouseDropdownResponse
import com.cuso.tailor.model.inventory.WarehouseListResponse
import com.cuso.tailor.model.inventory.WarehouseMessageResponse
import com.cuso.tailor.model.inventory.WarehouseResponse
import com.cuso.tailor.model.settings.BinItem
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
     * Update an existing inventory item with multipart form data.
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

    @DELETE("/api/inventory/item/delete-one/{id}")
    suspend fun deleteInventoryItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") itemId: String
    ): Response<DeleteInventoryItemResponse>

    // =========================================================================
    // 2. ITEM GROUPS (WITH MULTIPART IMAGES)
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
     * Get single item group by ID.
     */
    @GET("/api/inventory/item-group/view-one/{id}")
    suspend fun getInventoryItemGroupViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<ItemGroupViewOneResponse>

    /**
     * Create a new item group with multipart images and form fields.
     */
    @Multipart
    @POST("/api/inventory/item-group/create")
    suspend fun createItemGroup(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>?
    ): Response<CreateItemGroupResponse>

    /**
     * Update an existing item group with multipart images and form fields.
     */
    @Multipart
    @PUT("/api/inventory/item-group/update-one/{id}")
    suspend fun updateItemGroup(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>?
    ): Response<UpdateItemGroupResponse>

    /**
     * Delete an item group.
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
    suspend fun getStockAdjustments(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("adjustmentType") adjustmentType: String? = null,
        @Query("search") search: String? = null
    ): Response<StockAdjustmentListResponse>

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
        @Query("limit") limit: Int = 10,
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

    // =========================================================================
    // 7. BULK ITEM
    // =========================================================================
    @GET("/api/inventory/bulk-item/view-all")
    suspend fun getBulkItems(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null
    ): Response<BulkItemListResponse>

    @GET("/api/inventory/bulk-item/view-one/{id}")
    suspend fun getBulkItemById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BulkItemDetailResponse>

    @Multipart
    @POST("/api/inventory/bulk-item/create")
    suspend fun createBulkItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part components: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part? = null
    ): Response<BulkItemDetailResponse>

    @Multipart
    @PUT("/api/inventory/bulk-item/update-one/{id}")
    suspend fun updateBulkItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part components: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part? = null
    ): Response<BulkItemDetailResponse>

    @POST("/api/inventory/bulk-item/{id}/recalculate-cost")
    suspend fun recalculateCost(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BulkItemDetailResponse>

    @POST("/api/inventory/bulk-item/{id}/build")
    suspend fun buildBulkItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: BuildBulkItemRequest
    ): Response<BulkItemDetailResponse>

    @POST("/api/inventory/bulk-item/{id}/adjust-stock")
    suspend fun adjustBulkItemStock(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: AdjustBulkStockRequest
    ): Response<BulkItemDetailResponse>

    @DELETE("/api/inventory/bulk-item/delete-one/{id}")
    suspend fun deleteBulkItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BaseBulkResponse>

    @POST("/api/inventory/bulk-item/restore/{id}")
    suspend fun restoreBulkItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BulkItemDetailResponse>

    // -------------------------------------------------------------------------
    // PURCHASE ORDERS MANAGEMENT
    // -------------------------------------------------------------------------

    @POST("/api/inventory/purchase-order/create")
    suspend fun createPurchaseOrderDirect(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: PurchaseOrder
    ): Response<PurchaseOrderSingleResponse>

    @PUT("/api/inventory/purchase-order/update-one/{id}")
    suspend fun updatePurchaseOrder(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String,
        @Body request: PurchaseOrder
    ): Response<PurchaseOrderSingleResponse>

    @GET("/api/inventory/purchase-order/view-all")
    suspend fun getAllPurchaseOrdersList(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<PurchaseOrderListResponse>

    @GET("/api/inventory/purchase-order/view-one/{id}")
    suspend fun getPurchaseOrderById(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<PurchaseOrderDetailResponse>

    @POST("/api/inventory/purchase-order/receive/")
    suspend fun receivePurchaseOrder(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: ReceivePurchaseOrderRequest
    ): Response<PurchaseReceiveResponse>

    @GET("/api/inventory/purchase-order/get-for-bill-convert/{id}")
    suspend fun getPOForBillConvert(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") poId: String
    ): Response<POBillConvertResponse>

    // =========================================================================
    // 8. PURCHASE REQUISITIONS
    // =========================================================================

    /**
     * Fetch all requisitions with optional filters.
     */
    @GET("/api/inventory/requisition/view-all")
    suspend fun getAllRequisitions(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<RequisitionListResponse>

    /**
     * Fetch single requisition details by ID.
     */
    @GET("/api/inventory/requisition/view-one/{id}")
    suspend fun getRequisitionById(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<RequisitionSingleResponse>

    /**
     * Add comment to a purchase requisition.
     */
    @POST("/api/inventory/requisition/{id}/comment")
    suspend fun addRequisitionComment(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") requisitionId: String,
        @Body request: AddRequisitionCommentRequest
    ): Response<RequisitionSingleResponse>

    /**
     * Create a new purchase requisition.
     */
    @POST("/api/inventory/requisition/create")
    suspend fun createRequisition(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: CreateRequisitionRequest
    ): Response<RequisitionSingleResponse>

    @PUT("/api/inventory/requisition/update-one/{id}")
    suspend fun updateRequisition(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateRequisitionRequest
    ): Response<RequisitionSingleResponse>

    /**
     * Action requisition approval (Approve / Reject).
     */
    @POST("/api/inventory/requisition/{id}/submit-for-approval")
    suspend fun submitForApproval(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: SubmitForApprovalRequest? = null
    ): Response<SubmitForApprovalResponse>

    @DELETE("/api/inventory/requisition/delete-one/{id}")
    suspend fun deleteRequisition(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteRequisitionResponse>



    // =============================================================================
    // BARCODE ENDPOINTS
    // =============================================================================

    @POST("/api/inventory/barcode/generate")
    suspend fun generateBarcode(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: GenerateBarcodeRequest
    ): Response<BarcodeDetailResponse>

    @GET("/api/inventory/barcode/view-all")
    suspend fun getAllBarcodes(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<BarcodeListResponse>

    @GET("/api/inventory/barcode/view-one/{id}")
    suspend fun getBarcodeViewOne(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<BarcodeDetailResponse>

    @POST("/api/inventory/barcode/{id}/toggle-status")
    suspend fun toggleBarcodeStatus(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<BarcodeDetailResponse>

    @DELETE("/api/inventory/barcode/delete-one/{id}")
    suspend fun deleteBarcode(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteBarcodeResponse>

    // =============================================================================
    // PURCHASE RECEIVE
    // =============================================================================

    @GET("/api/inventory/purchase-receive/view-all")
    suspend fun getAllReceives(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null
    ): Response<AllReceivesResponse>

    @GET("/api/inventory/purchase-receive/view-one/{id}")
    suspend fun getSingleReceive(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<SingleReceiveResponse>

    @POST("/api/inventory/purchase-receives/convert-to-bill")
    suspend fun convertReceiveToBill(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") receiveId: String
    ): Response<ConvertToBillResponse>

    /**
     * Fetches detailed receive history and item progress for a specific Purchase Order.
     */
    @GET("/api/inventory/purchase-receive/history/{poId}")
    suspend fun getReceiveHistoryByPo(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("poId") poId: String
    ): Response<ReceiveHistoryByPoResponse>

    @GET("/api/finance/settings/payment-terms/view-all")
    suspend fun getPaymentTerms(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
    ): Response<PaymentTermsResponse>

    @GET("/api/finance/settings/tax-groups/view-all")
    suspend fun getTaxGroups(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
    ): Response<TaxGroupsResponse>

    @POST("/api/finance/purchase-bills/create")
    suspend fun createBill(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateBillRequest
    ): Response<CreateBillResponse>

    @POST("/api/inventory/purchase-receive/view-multiple")
    suspend fun viewMultipleReceives(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: ViewMultipleReceivesRequest
    ): Response<ViewMultipleReceivesResponse>

    /**
     * Fetches paginated summary of purchase orders and their aggregate receive status.
     */
    @GET("inventory/purchase-receive/po-summary")
    suspend fun getPurchaseOrderSummary(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null
    ): Response<PurchaseOrderSummaryResponse>

    // =============================================================================
    // LOCATION MANAGEMENT
    // =============================================================================

    @GET("/api/inventory/bin-stock/stock-location")
    suspend fun getStockLocationItems(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<StockLocationItemListResponse>

    @GET("/api/inventory/bin-stock/stock-location/{id}")
    suspend fun getStockLocationViewOne(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<StockLocationViewOneResponse>

    @GET("/api/inventory/settings/floor/warehouse/{warehouseId}")
    suspend fun getFloorDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("warehouseId") warehouseId: String
    ): Response<FloorDropdownResponse>

    @GET("/api/inventory/settings/section/floor/{floorId}")
    suspend fun getSectionDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("floorId") floorId: String
    ): Response<SectionDropdownResponse>

    @GET("/api/inventory/settings/rack/section/{sectionId}")
    suspend fun getRackDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("sectionId") sectionId: String
    ): Response<RackDropdownResponse>

    @GET("/api/inventory/settings/bin/rack/{rackId}")
    suspend fun getBinDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("rackId") rackId: String
    ): Response<BinDropdownResponse>

    /**
     * Assign inventory item stock to a specific bin location.
     */
    @POST("/api/inventory/bin-stock/create")
    suspend fun assignStockLocation(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: AssignStockLocationRequest
    ): Response<AssignStockLocationResponse>

    // =============================================================================
    // SAFETY STOCK
    // =============================================================================
    @GET("/api/inventory/safety-stock/view-all")
    suspend fun getSafetyStock(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("warehouseId") warehouseId: String? = null
    ): Response<SafetyStockResponse>
}