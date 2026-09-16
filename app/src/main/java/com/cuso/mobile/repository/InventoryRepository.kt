package com.cuso.mobile.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import com.cuso.mobile.database.dao.TokensDao
//import com.cuso.mobile.model.inventory.AddCommentRequest
import com.cuso.mobile.model.inventory.AdjustBulkStockRequest
import com.cuso.mobile.model.inventory.AdjustStockQuantityRequest
import com.cuso.mobile.model.inventory.AdjustStockRequest
import com.cuso.mobile.model.inventory.BarcodeItemDoc
import com.cuso.mobile.model.inventory.BillCreatedData
import com.cuso.mobile.model.inventory.BuildBulkItemRequest
import com.cuso.mobile.model.inventory.BulkItemDoc
import com.cuso.mobile.model.inventory.CreateInventoryItemResponse
import com.cuso.mobile.model.inventory.CreateItemGroupRequest
import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.CreateRequisitionRequest
import com.cuso.mobile.model.inventory.CreateSupplierRequest
import com.cuso.mobile.model.inventory.CreateWarehouseRequest
import com.cuso.mobile.model.inventory.DecreaseStockRequest
import com.cuso.mobile.model.inventory.DeleteInventoryItemResponse
import com.cuso.mobile.model.inventory.GenerateBarcodeRequest
import com.cuso.mobile.model.inventory.IncreaseStockRequest
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemListResponse
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.ItemGroupDto
import com.cuso.mobile.model.inventory.ItemGroupListResponse
import com.cuso.mobile.model.inventory.ItemGroupViewOneData
import com.cuso.mobile.model.inventory.LowStockItemDto
import com.cuso.mobile.model.inventory.POBillConvertData
import com.cuso.mobile.model.inventory.PurchaseOrder
import com.cuso.mobile.model.inventory.PurchaseOrderData
import com.cuso.mobile.model.inventory.PurchaseReceiveItem
import com.cuso.mobile.model.inventory.PurchaseRequisition
import com.cuso.mobile.model.inventory.ReceiveHistoryByPoResponse
import com.cuso.mobile.model.inventory.ReceivePurchaseOrderRequest
import com.cuso.mobile.model.inventory.RequisitionApprovalActionRequest
import com.cuso.mobile.model.inventory.RequisitionSingleResponse
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
import com.cuso.mobile.utils.createPartFromString
import com.cuso.mobile.utils.uriToMultipartPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
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
    // CONSTANTS & MESSAGES
    // =========================================================================
    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_PAGE_SIZE = 10
        private const val DEFAULT_ITEM_GROUP_PAGE_SIZE = 20
        private const val DEFAULT_MIME_TYPE = "image/jpeg"
        private const val MULTIPART_IMAGE_FIELD = "images"
        private const val TEMP_FILE_PREFIX = "item_upload"
        private const val TEMP_FILE_SUFFIX = ".tmp"

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

    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens() ?: throw Exception(ERROR_NO_TOKENS)
        val bearerToken = "Bearer ${tokens.accessToken}"
        val csrfToken = tokens.csrfToken
        return Pair(bearerToken, csrfToken)
    }

    // =========================================================================
    // INVENTORY ITEMS
    // =========================================================================

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

    suspend fun deleteInventoryItem(itemId: String): Result<DeleteInventoryItemResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.deleteInventoryItem(accessToken, csrfToken, itemId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifBlank { "Failed to delete item" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // ITEM GROUPS
    // =========================================================================

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

    suspend fun createItemGroup(
        params: Map<String, RequestBody>,
        images: List<MultipartBody.Part>?
    ): Result<ItemGroupDto> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.createItemGroup(accessToken, csrfToken, params, images)
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

    suspend fun updateItemGroup(
        id: String,
        params: Map<String, RequestBody>,
        images: List<MultipartBody.Part>?
    ): Result<ItemGroupDto> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.updateItemGroup(accessToken, csrfToken, id, params, images)
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

    suspend fun deleteItemGroup(id: String): Result<String> = withContext(Dispatchers.IO) {
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


    suspend fun prepareImagePart(
        context: Context,
        uri: Uri?,
        fieldName: String = "image"
    ): MultipartBody.Part? = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null

        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return@withContext null

            val maxDimension = 1080
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                maxDimension.toFloat() / maxOf(width, height)
            } else {
                1.0f
            }

            val resizedBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
            } else {
                originalBitmap
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val compressedBytes = byteArrayOutputStream.toByteArray()

            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()

            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val fileOutputStream = FileOutputStream(tempFile)
            fileOutputStream.write(compressedBytes)
            fileOutputStream.flush()
            fileOutputStream.close()

            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(
                fieldName,
                "img_${System.currentTimeMillis()}.jpg",
                requestBody
            )
        } catch (e: Exception) {
            android.util.Log.e("IMAGE_UPLOAD_ERR", "Failed to compress & prepare image: ${e.message}")
            null
        }
    }

    suspend fun prepareMultipleImagesPart(
        context: Context,
        uris: List<Uri>,
        fieldName: String = "image"
    ): List<MultipartBody.Part> = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext emptyList()
        uris.mapNotNull { uri -> prepareImagePart(context, uri, fieldName) }
    }
    // =========================================================================
    // STOCK ADJUSTMENTS & TRANSFERS
    // =========================================================================

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
     * Fetches paginated stock adjustments list from /inventory/stock-adjustment/view-all
     */
    suspend fun getStockAdjustments(
        page: Int = 1,
        limit: Int = 10,
        adjustmentType: String? = null,
        search: String? = null
    ): Result<StockAdjustmentListResponse> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getStockAdjustments(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                adjustmentType = adjustmentType,
                search = search
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch stock adjustments [${response.code()}]")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    // =========================================================================
    // SUPPLIERS
    // =========================================================================

    suspend fun getAllSuppliers(page: Int = 1, limit: Int = 50, search: String? = null, status: String? = null): Result<List<SupplierDto>> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun getSupplierDropdown(): Result<List<SupplierDropdownItem>> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun getSupplierById(id: String): Result<SupplierDto> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun createSupplier(request: CreateSupplierRequest): Result<SupplierDto> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun updateSupplier(id: String, request: CreateSupplierRequest): Result<SupplierDto> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun deleteSupplier(id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun restoreSupplier(id: String): Result<SupplierDto> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun getSupplierLedger(id: String): Result<SupplierLedgerContainer> = withContext(Dispatchers.IO) {
        try {
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

    private fun <T> extractErrorMessage(response: Response<T>, fallbackMessage: String): String {
        return response.errorBody()?.string()
            ?: response.message().takeIf { it.isNotBlank() }
            ?: "$fallbackMessage (Code: ${response.code()})"
    }
    // =========================================================================
    // BULK ITEMS
    // =========================================================================
    suspend fun getBulkItems(): Result<List<BulkItemDoc>> {
        return try {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.getBulkItems(token, csrf)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch bulk items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBulkItemById(id: String): Result<BulkItemDoc> {
        return try {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.getBulkItemById(token, csrf, id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch bulk item details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBulkItem(
        context: Context,
        name: String,
        sku: String,
        description: String?,
        categoryId: String?,
        brand: String?,
        unit: String,
        costPrice: Double,
        sellingPrice: Double,
        taxPercent: Double,
        salesAccountId: String?,
        purchaseAccountId: String?,
        trackInventory: Boolean,
        assemblyType: String,
        warehouseRestrictionId: String?,
        components: List<Pair<String, Int>>,
        imageUri: Uri?
    ): Result<BulkItemDoc> {
        return try {
            val (token, csrf) = getAuthHeaders()

            val params = mutableMapOf<String, RequestBody>(
                "name" to createPartFromString(name),
                "sku" to createPartFromString(sku),
                "unit" to createPartFromString(unit),
                "costPrice" to createPartFromString(costPrice.toString()),
                "sellingPrice" to createPartFromString(sellingPrice.toString()),
                "taxPercent" to createPartFromString(taxPercent.toString()),
                "trackInventory" to createPartFromString(trackInventory.toString()),
                "assemblyType" to createPartFromString(assemblyType)
            )

            description?.takeIf { it.isNotBlank() }?.let { params["description"] = createPartFromString(it) }
            categoryId?.takeIf { it.isNotBlank() }?.let { params["categoryId"] = createPartFromString(it) }
            brand?.takeIf { it.isNotBlank() }?.let { params["brand"] = createPartFromString(it) }
            salesAccountId?.takeIf { it.isNotBlank() }?.let { params["salesAccountId"] = createPartFromString(it) }
            purchaseAccountId?.takeIf { it.isNotBlank() }?.let { params["purchaseAccountId"] = createPartFromString(it) }
            warehouseRestrictionId?.takeIf { it.isNotBlank() }?.let { params["warehouseRestrictionId"] = createPartFromString(it) }

            val componentParts = components.mapIndexed { index, comp ->
                MultipartBody.Part.createFormData("components[$index][itemId]", comp.first)
            } + components.mapIndexed { index, comp ->
                MultipartBody.Part.createFormData("components[$index][qtyRequired]", comp.second.toString())
            }

            val imagePart = imageUri?.let { uriToMultipartPart(context, it, "image") }

            val response = inventoryApi.createBulkItem(token, csrf, params, componentParts, imagePart)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create bulk item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBulkItem(
        context: Context,
        id: String,
        name: String,
        sku: String,
        description: String?,
        categoryId: String?,
        brand: String?,
        unit: String,
        costPrice: Double,
        sellingPrice: Double,
        taxPercent: Double,
        salesAccountId: String?,
        purchaseAccountId: String?,
        trackInventory: Boolean,
        assemblyType: String,
        warehouseRestrictionId: String?,
        components: List<Pair<String, Int>>,
        imageUri: Uri?
    ): Result<BulkItemDoc> {
        return try {
            val (token, csrf) = getAuthHeaders()

            val params = mutableMapOf<String, RequestBody>(
                "name" to createPartFromString(name),
                "sku" to createPartFromString(sku),
                "unit" to createPartFromString(unit),
                "costPrice" to createPartFromString(costPrice.toString()),
                "sellingPrice" to createPartFromString(sellingPrice.toString()),
                "taxPercent" to createPartFromString(taxPercent.toString()),
                "trackInventory" to createPartFromString(trackInventory.toString()),
                "assemblyType" to createPartFromString(assemblyType)
            )

            description?.takeIf { it.isNotBlank() }?.let { params["description"] = createPartFromString(it) }
            categoryId?.takeIf { it.isNotBlank() }?.let { params["categoryId"] = createPartFromString(it) }
            brand?.takeIf { it.isNotBlank() }?.let { params["brand"] = createPartFromString(it) }
            salesAccountId?.takeIf { it.isNotBlank() }?.let { params["salesAccountId"] = createPartFromString(it) }
            purchaseAccountId?.takeIf { it.isNotBlank() }?.let { params["purchaseAccountId"] = createPartFromString(it) }
            warehouseRestrictionId?.takeIf { it.isNotBlank() }?.let { params["warehouseRestrictionId"] = createPartFromString(it) }

            val componentParts = components.mapIndexed { index, comp ->
                MultipartBody.Part.createFormData("components[$index][itemId]", comp.first)
            } + components.mapIndexed { index, comp ->
                MultipartBody.Part.createFormData("components[$index][qtyRequired]", comp.second.toString())
            }

            val imagePart = imageUri?.let { uriToMultipartPart(context, it, "image") }

            val response = inventoryApi.updateBulkItem(token, csrf, id, params, componentParts, imagePart)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update bulk item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recalculateCost(id: String): Result<BulkItemDoc> {
        return try {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.recalculateCost(token, csrf, id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to recalculate cost"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buildBulkItem(id: String, qty: Int, warehouseId: String?, remarks: String?): Result<BulkItemDoc> {
        return try {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.buildBulkItem(token, csrf, id,
                BuildBulkItemRequest(qty, warehouseId, remarks)
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to build bulk item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adjustStock(id: String, qty: Int, warehouseId: String?, remarks: String?): Result<BulkItemDoc> {
        return try {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.adjustBulkItemStock(token, csrf, id,
                AdjustBulkStockRequest(qty, warehouseId, remarks)
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to adjust stock"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBulkItem(id: String): Result<String> {
        return try {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.deleteBulkItem(token, csrf, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Bulk Item deleted successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete bulk item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBulkItem(id: String): Result<BulkItemDoc> {
        return try {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.restoreBulkItem(token, csrf, id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to restore bulk item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // PURCHASE ORDER FULL LIFECYCLE
    // =========================================================================

    suspend fun createPurchaseOrderDirect(request: PurchaseOrder): Result<PurchaseOrder> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.createPurchaseOrderDirect(accessToken, csrfToken, request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to create purchase order")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updatePurchaseOrder(id: String, request: PurchaseOrder): Result<PurchaseOrder> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.updatePurchaseOrder(accessToken, csrfToken, id, request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to update purchase order")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getAllPurchaseOrders(
        page: Int? = null,
        limit: Int? = null,
        search: String? = null,
        status: String? = null
    ): Result<List<PurchaseOrder>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getAllPurchaseOrdersList(accessToken, csrfToken, page, limit, search, status)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to load purchase orders")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun receivePurchaseOrder(request: ReceivePurchaseOrderRequest): Result<ReceivePurchaseOrderRequest> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.receivePurchaseOrder(accessToken, csrfToken, request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to receive purchase order")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getPOForBillConvert(poId: String): Result<POBillConvertData> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.getPOForBillConvert(accessToken, csrfToken, poId)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to fetch PO bill convert details")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =========================================================================
    // PURCHASE REQUISITIONS
    // =========================================================================


    suspend fun getAllRequisitions(
        page: Int? = null,
        limit: Int? = null,
        search: String? = null,
        status: String? = null
    ): Result<List<PurchaseRequisition>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getAllRequisitions(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                status = status
            )
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data) // CORRECTED: It should return the list from body.data
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to load requisitions")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRequisitionById(id: String): Result<PurchaseRequisition> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getRequisitionById(accessToken, csrfToken, id)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to load requisition detail")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    suspend fun addRequisitionComment(requisitionId: String, commentText: String): Response<RequisitionSingleResponse> {
//        val payload = AddCommentRequest(text = commentText)
//        val (accessToken, csrfToken) = getAuthHeaders()
//
//        return inventoryApi.addComment(accessToken, csrfToken, requisitionId, payload)
//    }

    suspend fun createRequisition(request: CreateRequisitionRequest): Result<PurchaseRequisition> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.createRequisition(accessToken, csrfToken, request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to create requisition")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun actionRequisitionApproval(
        id: String,
        request: RequisitionApprovalActionRequest
    ): Result<PurchaseRequisition> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.actionRequisitionApproval(accessToken, csrfToken, id, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to action requisition approval")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // BARCODE API REPOSITORY METHODS
    // =========================================================================

    /**
     * Generate a new barcode for an inventory item.
     */
    suspend fun generateBarcode(request: GenerateBarcodeRequest): Result<BarcodeItemDoc> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.generateBarcode(accessToken, csrfToken, request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to generate barcode")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch all generated barcodes list with optional search and status filter.
     */
    suspend fun getAllBarcodes(search: String? = null, status: String? = null): Result<List<BarcodeItemDoc>> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.getAllBarcodes(accessToken, csrfToken, search, status)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to load barcodes list")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch detailed information and print logs for a single barcode.
     */
    suspend fun getBarcodeViewOne(id: String): Result<BarcodeItemDoc> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.getBarcodeViewOne(accessToken, csrfToken, id)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to load barcode details")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Toggle status between Active and Inactive for a given barcode.
     */
    suspend fun toggleBarcodeStatus(id: String): Result<BarcodeItemDoc> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.toggleBarcodeStatus(accessToken, csrfToken, id)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to toggle barcode status")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Delete a barcode item.
     */
    suspend fun deleteBarcode(id: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val (accessToken, csrfToken) = getAuthHeaders()
                val response = inventoryApi.deleteBarcode(accessToken, csrfToken, id)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    Result.success(body.message ?: "Barcode deleted successfully")
                } else {
                    Result.failure(Exception(extractErrorMessage(response, "Failed to delete barcode")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =============================================================================
    // PURCHASE RECEIVE
    // =============================================================================

    suspend fun getAllReceives(page: Int = 1, limit: Int = 20, search: String? = null): Result<List<PurchaseReceiveItem>> {
        return runCatching {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.getAllReceives(token, csrf, page, limit, search)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to fetch purchase receives")
            }
        }
    }

    suspend fun getReceiveHistoryByPo(poId: String): Result<ReceiveHistoryByPoResponse> {
        return runCatching {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.getReceiveHistoryByPo(token, csrf, poId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to fetch PO receive history")
            }
        }
    }

    suspend fun getSingleReceive(id: String): Result<PurchaseReceiveItem> {
        return runCatching {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.getSingleReceive(token, csrf, id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to fetch receive details")
            }
        }
    }

    suspend fun convertReceiveToBill(receiveId: String): Result<BillCreatedData> {
        return runCatching {
            val (token, csrf) = getAuthHeaders()
            val response = inventoryApi.convertReceiveToBill(token, csrf, receiveId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to convert receive to bill")
            }
        }
    }
}
