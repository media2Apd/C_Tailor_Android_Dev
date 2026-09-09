package com.cuso.mobile.repository

import android.content.Context
import android.net.Uri
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.model.inventory.AdjustStockRequest
import com.cuso.mobile.model.inventory.CreateInventoryItemResponse
import com.cuso.mobile.model.inventory.CreateItemGroupRequest
import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemListResponse
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.ItemGroupDto
import com.cuso.mobile.model.inventory.ItemGroupListResponse
import com.cuso.mobile.model.inventory.LowStockItemDto
import com.cuso.mobile.model.inventory.PurchaseOrderData
import com.cuso.mobile.model.inventory.UpdateInventoryItemResponse
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

    /**
     * Extracts an error message from a Retrofit Response object.
     */
    private fun <T> extractErrorMessage(response: Response<T>, fallbackMessage: String): String {
        return response.errorBody()?.string()
            ?: response.message().takeIf { it.isNotBlank() }
            ?: "$fallbackMessage (Code: ${response.code()})"
    }
}