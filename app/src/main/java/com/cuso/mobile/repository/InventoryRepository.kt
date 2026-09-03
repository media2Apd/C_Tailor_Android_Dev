package com.cuso.mobile.repository

import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemListResponse
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.LowStockItemDto
import com.cuso.mobile.model.inventory.PurchaseOrderData
import javax.inject.Inject
import javax.inject.Singleton
import com.cuso.mobile.network.inventory.InventoryApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryApi: InventoryApiService,
    private val tokensDao: com.cuso.mobile.database.dao.TokensDao
) {

    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // ── Inventory Items: list ──
    suspend fun getInventoryItems(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<InventoryItemListResponse> {
        return try {
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
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch inventory items: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Inventory Items: get single item by id (for the "View" details popup) ──
    suspend fun getInventoryItemById(id: String): Result<InventoryItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getInventoryItemById(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch item details: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Inventory Items: recent ──
    suspend fun getRecentInventoryItems(limit: Int = 10): Result<InventoryItemListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getRecentInventoryItems(
                token = accessToken,
                csrfToken = csrfToken,
                limit = limit
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch recent items: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Inventory Items: adjust stock ──
    suspend fun adjustStock(
        itemId: String,
        adjustmentType: String,
        quantity: Double,
        reason: String,
        notes: String
    ): Result<InventoryItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.adjustStock(
                token = accessToken,
                csrfToken = csrfToken,
                request = com.cuso.mobile.model.inventory.AdjustStockRequest(
                    itemId = itemId,
                    adjustmentType = adjustmentType,
                    quantity = quantity,
                    reason = reason,
                    notes = notes
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to adjust stock: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ── Inventory Items: create (multipart) ──
    suspend fun createInventoryItem(
        context: android.content.Context,
        form: com.cuso.mobile.model.inventory.CreateItemFormState
    ): Result<InventoryItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            fun part(value: String): okhttp3.RequestBody =
                value.toRequestBody("text/plain".toMediaTypeOrNull())

            val fields = mapOf(
                "itemType" to part(form.itemType),
                "name" to part(form.name),
                "sku" to part(form.sku),
                "category" to part(form.category),
                "status" to part(form.status),
                "unit" to part(form.unit),
                "autoGenerateSku" to part(form.autoGenerateSku.toString()),
                "returnable" to part(form.returnable.toString()),
                "hsnCode" to part(form.hsnCode),
                "taxPercentage" to part(form.taxPercentage),
                "taxInclusive" to part(form.taxInclusive.toString()),
                "length" to part(form.length),
                "width" to part(form.width),
                "height" to part(form.height),
                "weight" to part(form.weight),
                "manufacturer" to part(form.manufacturer),
                "brand" to part(form.brand),
                "barcode" to part(form.barcode),
                "sellingPrice" to part(form.sellingPrice),
                "salesAccount" to part(form.salesAccount),
                "salesDescription" to part(form.salesDescription),
                "costPrice" to part(form.costPrice),
                "purchaseAccount" to part(form.purchaseAccount),
                "preferredVendor" to part(form.preferredVendor),
                "purchaseDescription" to part(form.purchaseDescription),
                "trackInventory" to part(form.trackInventory.toString()),
                "isSerialTracked" to part(form.isSerialTracked.toString()),
                "inventoryAccount" to part(form.inventoryAccount),
                "openingStock" to part(form.openingStock)
            )

            val imagePart: okhttp3.MultipartBody.Part? = form.imageUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                bytes?.let {
                    val requestBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                    okhttp3.MultipartBody.Part.createFormData("images", "item_image.jpg", requestBody)  //   CHANGED: "image" → "images"
                }
            }

            val response = inventoryApi.createInventoryItem(
                token = accessToken,
                csrfToken = csrfToken,
                fields = fields,
                image = imagePart
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to create item: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ── Inventory Item: View One (details popup) ──
    suspend fun getInventoryViewOne(id: String): Result<InventoryItemviewone> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getInventoryViewOne(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch item details: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //low stock alert
    suspend fun getLowStockAlerts(warehouseId: String? = null): Result<List<LowStockItemDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getLowStockAlerts(accessToken, csrfToken, warehouseId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data ?: emptyList())
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch low stock alerts"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPurchaseOrder(request: CreatePurchaseOrderRequest): Result<PurchaseOrderData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.createPurchaseOrder(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create purchase order"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLowStockItemDetail(
        itemId: String,
        warehouseId: String
    ): Result<LowStockItemDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getLowStockItemDetail(accessToken, csrfToken, itemId, warehouseId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch item details"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}