package com.cuso.mobile.repository

import android.content.Context
import android.net.Uri
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.model.inventory.AdjustStockQuantityRequest
import com.cuso.mobile.model.inventory.AdjustStockRequest
import com.cuso.mobile.model.inventory.CreateInventoryItemResponse
import com.cuso.mobile.model.inventory.CreateItemGroupRequest
import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.CreateSupplierRequest
import com.cuso.mobile.model.inventory.CreateWarehouseRequest
import com.cuso.mobile.model.inventory.DecreaseStockRequest
import com.cuso.mobile.model.inventory.IncreaseStockRequest
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemListResponse
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.ItemGroupDto
import com.cuso.mobile.model.inventory.ItemGroupListResponse
import com.cuso.mobile.model.inventory.ItemGroupViewOneData
import com.cuso.mobile.model.inventory.LowStockItemDto
import com.cuso.mobile.model.inventory.PurchaseOrderData
import com.cuso.mobile.model.inventory.ReverseAdjustmentRequest
import com.cuso.mobile.model.inventory.StockAdjustmentData
import com.cuso.mobile.model.inventory.StockAdjustmentListResponse
import com.cuso.mobile.model.inventory.StockSummaryListResponse
import com.cuso.mobile.model.inventory.SupplierDropdownItem
import com.cuso.mobile.model.inventory.SupplierDto
import com.cuso.mobile.model.inventory.SupplierLedgerContainer
import com.cuso.mobile.model.inventory.TransferStockRequest
import com.cuso.mobile.model.inventory.UpdateInventoryItemResponse
import com.cuso.mobile.model.inventory.UpdateWarehouseRequest
import com.cuso.mobile.model.inventory.WarehouseDropdownItem
import com.cuso.mobile.model.inventory.WarehouseItem
import com.cuso.mobile.network.inventory.InventoryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryApi: InventoryApiService,
    private val tokensDao: TokensDao
) {

    // =========================================================================
    // CONSTANTS
    // =========================================================================
    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_PAGE_SIZE = 10
        private const val DEFAULT_ITEM_GROUP_PAGE_SIZE = 20
        private const val DEFAULT_MIME_TYPE = "image/jpeg"
        private const val MULTIPART_IMAGE_FIELD = "images"
        private const val TEMP_FILE_PREFIX = "item_upload"
        private const val TEMP_FILE_SUFFIX = ".tmp"

        // Error Messages
        private const val ERROR_NO_TOKENS = "No tokens found, please login again"
        private const val ERROR_FETCH_ITEMS = "Failed to fetch inventory items"
        private const val ERROR_FETCH_DETAILS = "Failed to fetch item details"
        private const val ERROR_FETCH_RECENT = "Failed to fetch recent items"
        private const val ERROR_ADJUST_STOCK = "Failed to adjust stock"
        private const val ERROR_CREATE_ITEM = "Failed to create inventory item"
        private const val ERROR_FETCH_ITEM_GROUPS = "Failed to fetch item groups"
        private const val ERROR_LOW_STOCK_ALERTS = "Failed to fetch low stock alerts"
        private const val ERROR_CREATE_PURCHASE_ORDER = "Failed to create purchase order"
        private const val ERROR_FETCH_WAREHOUSES = "Failed to fetch warehouses"
        private const val ERROR_FETCH_DROPDOWN = "Failed to fetch warehouse dropdown list"
        private const val ERROR_CREATE_WAREHOUSE = "Failed to create warehouse"
        private const val ERROR_UPDATE_WAREHOUSE = "Failed to update warehouse"
        private const val ERROR_DELETE_WAREHOUSE = "Failed to delete warehouse"
        private const val ERROR_RESTORE_WAREHOUSE = "Failed to restore warehouse"
    }

    // =========================================================================
    // AUTHENTICATION HEADERS
    // =========================================================================

    /**
     * Retrieves the stored authorization tokens to attach to API requests.
     * @return Pair containing (Bearer AccessToken, CsrfToken)
     */
    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens() ?: throw Exception(ERROR_NO_TOKENS)
        val bearerToken = "Bearer ${tokens.accessToken}"
        val csrfToken = tokens.csrfToken
        return Pair(bearerToken, csrfToken)
    }

    // =========================================================================
    // INVENTORY ITEMS
    // =========================================================================

    /**
     * Fetch paginated list of inventory items with optional search and status filters.
     */
    suspend fun getInventoryItems(
        page: Int = DEFAULT_PAGE,
        limit: Int = DEFAULT_PAGE_SIZE,
        search: String? = null,
        status: String? = null
    ): Result<InventoryItemListResponse> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getInventoryItems(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                status = status
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_ITEMS)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch single inventory item details by ID.
     */
    suspend fun getInventoryItemById(id: String): Result<InventoryItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getInventoryItemById(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_DETAILS)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch recent inventory items.
     */
    suspend fun getRecentInventoryItems(limit: Int = DEFAULT_PAGE_SIZE): Result<InventoryItemListResponse> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.getRecentInventoryItems(
                    token = accessToken,
                    csrfToken = csrfToken,
                    limit = limit
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_RECENT)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch complete single item details view.
     */
    suspend fun getInventoryViewOne(id: String): Result<InventoryItemviewone> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getInventoryViewOne(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_DETAILS)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // STOCK OPERATIONS & CREATION
    // =========================================================================

    /**
     * Adjust stock quantity and log reason for changes.
     */
    suspend fun adjustStock(
        itemId: String,
        adjustmentType: String,
        quantity: Double,
        reason: String,
        notes: String
    ): Result<InventoryItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = AdjustStockRequest(
                itemId = itemId,
                adjustmentType = adjustmentType,
                quantity = quantity,
                reason = reason,
                notes = notes
            )
            val response = inventoryApi.adjustStock(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_ADJUST_STOCK)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new inventory item with multipart form-data.
     */
    suspend fun createItem(
        params: Map<String, RequestBody>,
        imagePart: MultipartBody.Part?
    ): Result<CreateInventoryItemResponse> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.createItem(
                token = accessToken,
                csrfToken = csrfToken,
                params = params,
                images = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_CREATE_ITEM)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing inventory item with multipart form data.
     */
    suspend fun updateItem(
        id: String,
        params: Map<String, RequestBody>,
        imagePart: MultipartBody.Part?
    ): Result<UpdateInventoryItemResponse> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.updateItem(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                params = params,
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to update item"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // ITEM GROUPS
    // =========================================================================

    /**
     * Fetch paginated item groups list.
     */
    suspend fun getInventoryItemGroup(
        page: Int = DEFAULT_PAGE,
        pageSize: Int = DEFAULT_ITEM_GROUP_PAGE_SIZE,
        search: String? = null
    ): Result<ItemGroupListResponse> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getInventoryItemGroupList(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                search = search
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_ITEM_GROUPS)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new item group.
     */
    suspend fun createItemGroup(request: CreateItemGroupRequest): Result<ItemGroupDto> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.createItemGroup(accessToken, csrfToken, request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    Result.success(body.data)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create item group"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Update an existing item group by ID.
     */
    suspend fun updateItemGroup(
        id: String,
        request: CreateItemGroupRequest
    ): Result<ItemGroupDto> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.updateItemGroup(accessToken, csrfToken, id, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to update item group"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Fetch single item group details with its variants.
     */
    suspend fun getInventoryItemGroupViewOne(id: String): Result<ItemGroupViewOneData> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.getInventoryItemGroupViewOne(accessToken, csrfToken, id)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch item group details"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Update an existing item group.
     */
//    suspend fun updateItemGroup(
//        id: String,
//        request: CreateItemGroupRequest
//    ): Result<ItemGroupDto> = withContext(Dispatchers.IO) {
//        try {
//            val (accessToken, csrfToken) = getAuthHeaders()
//            val response = inventoryApi.updateItemGroup(accessToken, csrfToken, id, request)
//            val body = response.body()
//
//            if (response.isSuccessful && body?.success == true) {
//                Result.success(body.data)
//            } else {
//                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to update item group"
//                Result.failure(Exception(errorMsg))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    /**
     * Delete an item group by ID with categoryId header.
     */
    suspend fun deleteItemGroup(
        id: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.deleteItemGroup(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "Item group deleted successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to delete item group"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // LOW STOCK & PURCHASE ORDERS
    // =========================================================================

    /**
     * Fetch list of items with low stock alerts.
     */
    suspend fun getLowStockAlerts(warehouseId: String? = null): Result<List<LowStockItemDto>> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.getLowStockAlerts(
                    token = accessToken,
                    csrfToken = csrfToken,
                    warehouseId = warehouseId
                )
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    Result.success(body.data ?: emptyList())
                } else {
                    Result.failure(Exception(extractErrorMessage(response, ERROR_LOW_STOCK_ALERTS)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch single low-stock item details for a specific warehouse.
     */
    suspend fun getLowStockItemDetail(
        itemId: String,
        warehouseId: String
    ): Result<LowStockItemDto> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getLowStockItemDetail(
                token = accessToken,
                csrfToken = csrfToken,
                itemId = itemId,
                warehouseId = warehouseId
            )
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_DETAILS)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a purchase order for restocking.
     */
    suspend fun createPurchaseOrder(
        request: CreatePurchaseOrderRequest
    ): Result<PurchaseOrderData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.createPurchaseOrder(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_CREATE_PURCHASE_ORDER)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // HELPER FUNCTIONS
    // =========================================================================

    /**
     * Converts a Uri into a MultipartBody.Part for file upload.
     */
    suspend fun prepareImagePart(context: Context, uri: Uri?): MultipartBody.Part? = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null

        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: DEFAULT_MIME_TYPE
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null

            val tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(MULTIPART_IMAGE_FIELD, tempFile.name, requestBody)
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // STOCK ADJUSTMENTS & TRANSFERS
    // =========================================================================

    /**
     * Fetch the list of valid stock adjustment reasons from backend.
     */
    suspend fun getValidAdjustmentReasons(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getValidAdjustmentReasons(accessToken, csrfToken)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to load adjustment reasons")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    /**
//     * Adjust stock quantity (Increase or Decrease).
//     */
//    suspend fun adjustStockQuantity(
//        request: AdjustStockQuantityRequest
//    ): Result<StockAdjustmentData> = withContext(Dispatchers.IO) {
//        try {
//            val (accessToken, csrfToken) = getAuthHeaders()
//            val response = inventoryApi.adjustStockQuantity(accessToken, csrfToken, request)
//            val body = response.body()
//
//            if (response.isSuccessful && body?.success == true && body.data != null) {
//                Result.success(body.data)
//            } else {
//                Result.failure(Exception(extractErrorMessage(response, "Failed to adjust stock")))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
    /**
     * 1. Increase Stock API Call
     */
    suspend fun increaseStock(
        request: IncreaseStockRequest
    ): Result<StockAdjustmentData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.increaseStock(accessToken, csrfToken, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to increase stock")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 2. Decrease Stock API Call
     */
    suspend fun decreaseStock(
        request: DecreaseStockRequest
    ): Result<StockAdjustmentData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.decreaseStock(accessToken, csrfToken, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to decrease stock")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Transfer stock between warehouses / bins.
     */
    suspend fun transferStock(
        request: TransferStockRequest
    ): Result<StockAdjustmentData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.transferStock(accessToken, csrfToken, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to transfer stock")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reverse a previous stock adjustment.
     */
    suspend fun reverseStockAdjustment(
        adjustmentId: String,
        reason: String? = "Other",
        notes: String? = null
    ): Result<StockAdjustmentData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = ReverseAdjustmentRequest(reason = reason, notes = notes)
            val response = inventoryApi.reverseStockAdjustment(accessToken, csrfToken, adjustmentId, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to reverse adjustment")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch stock summary list with error-safe handling.
     */
    suspend fun getStockSummaryList(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null
    ): Result<StockSummaryListResponse> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getStockSummaryList(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to load stock summary")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get single adjustment detail by ID.
     */
    suspend fun getStockAdjustmentById(id: String): Result<StockAdjustmentData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getStockAdjustmentById(accessToken, csrfToken, id)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to fetch adjustment details")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // WAREHOUSE OPERATIONS
    // =========================================================================

    /**
     * Fetch all active warehouses.
     */
    suspend fun getAllWarehouses(): Result<List<WarehouseItem>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getAllWarehouses(accessToken, csrfToken)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_WAREHOUSES)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch dropdown lookup list of warehouses.
     */
    suspend fun getWarehouseDropdown(): Result<List<WarehouseDropdownItem>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getWarehouseDropdown(accessToken, csrfToken)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_DROPDOWN)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch single warehouse detail by ID.
     */
    suspend fun getWarehouseById(id: String): Result<WarehouseItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getWarehouseById(accessToken, csrfToken, id)

            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_FETCH_DETAILS)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new warehouse.
     */
    suspend fun createWarehouse(request: CreateWarehouseRequest): Result<WarehouseItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.createWarehouse(accessToken, csrfToken, request)

            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_CREATE_WAREHOUSE)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing warehouse.
     */
    suspend fun updateWarehouse(id: String, request: UpdateWarehouseRequest): Result<WarehouseItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.updateWarehouse(accessToken, csrfToken, id, request)

            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_UPDATE_WAREHOUSE)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete warehouse by ID.
     */
    suspend fun deleteWarehouse(id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.deleteWarehouse(accessToken, csrfToken, id)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_DELETE_WAREHOUSE)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore a deleted warehouse by ID.
     */
    suspend fun restoreWarehouse(id: String): Result<WarehouseItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.restoreWarehouse(accessToken, csrfToken, id)

            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(extractErrorMessage(response, ERROR_RESTORE_WAREHOUSE)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extracts an error message from a Retrofit Response object.
     */
    private fun <T> extractErrorMessage(response: Response<T>, fallbackMessage: String): String {
        return response.errorBody()?.string()
            ?: response.message().takeIf { it.isNotBlank() }
            ?: "$fallbackMessage (Code: ${response.code()})"
    }

    // =========================================================================
    // SUPPLIER
    // =========================================================================
    suspend fun getAllSuppliers(page: Int = 1, limit: Int = 50, search: String? = null, status: String? = null): Result<List<SupplierDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getAllSuppliers(accessToken, csrfToken, page, limit, search, status)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch suppliers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSupplierDropdown(): Result<List<SupplierDropdownItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getSupplierDropdown(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch supplier dropdown"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSupplierById(id: String): Result<SupplierDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getSupplierById(accessToken, csrfToken, id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch supplier details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSupplier(request: CreateSupplierRequest): Result<SupplierDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.createSupplier(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: body?.message ?: "Failed to create supplier"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSupplier(id: String, request: CreateSupplierRequest): Result<SupplierDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.updateSupplier(accessToken, csrfToken, id, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: body?.message ?: "Failed to update supplier"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSupplier(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.deleteSupplier(accessToken, csrfToken, id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "Supplier deleted successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: body?.message ?: "Failed to delete supplier"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreSupplier(id: String): Result<SupplierDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.restoreSupplier(accessToken, csrfToken, id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: body?.message ?: "Failed to restore supplier"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSupplierLedger(id: String): Result<SupplierLedgerContainer> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getSupplierLedger(accessToken, csrfToken, id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load supplier ledger"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}