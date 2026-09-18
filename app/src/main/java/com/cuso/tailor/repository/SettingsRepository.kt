package com.cuso.tailor.repository

import android.content.Context
import android.net.Uri
import com.cuso.tailor.database.dao.SelectedGarmentDao
import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.database.entities.GarmentMeasurement
import com.cuso.tailor.database.entities.SelectedGarment
import com.cuso.tailor.model.inventory.ProductCategoryItem
import com.cuso.tailor.model.settings.*
import com.cuso.tailor.network.inventory.settings.InventorySettingsApiService
import com.cuso.tailor.network.sales.settings.SalesSettingsApiService
import com.cuso.tailor.utils.createPartFromString
import com.cuso.tailor.utils.uriToMultipartPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val salesSettingsApi: SalesSettingsApiService,
    private val inventoryApi: InventorySettingsApiService,
    private val tokensDao: TokensDao,
    val selectedGarmentDao: SelectedGarmentDao
) {
    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // ===========================================================
    // 1. PRODUCT CATEGORIES (INVENTORY)
    // ===========================================================

    suspend fun getProductCategories(): Result<List<ProductCategoryItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = inventoryApi.getProductCategoriesDropdown(accessToken, csrfToken)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data ?: emptyList())
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch categories"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 2. SEGMENTS (SALES)
    // ===========================================================

    suspend fun getSegments(page: Int = 1, limit: Int = 50): Result<List<SegmentItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getSegments(accessToken, csrfToken, page, limit)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch segments"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSegmentById(id: String): Result<SegmentItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getSegmentById(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch segment details"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSegment(request: CreateSegmentRequest): Result<CreateSegmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createSegment(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create segment"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSegment(id: String, request: CreateSegmentRequest): Result<CreateSegmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.updateSegment(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to update segment"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSegment(id: String): Result<DeleteSegmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.deleteSegment(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete segment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeSegmentStatus(
        id: String,
        request: ChangeSegmentStatusRequest
    ): Result<ChangeSegmentStatusResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.changeSegmentStatus(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to change segment status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 3. GARMENTS (SALES)
    // ===========================================================

    suspend fun getGarments(): Result<List<GarmentItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getGarments(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch garments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGarment(
        context: Context,
        name: String,
        code: String,
        description: String?,
        applicableSegmentIds: List<String>,
        imageUri: Uri?
    ): Result<CreateGarmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val namePart = createPartFromString(name)
            val displayNamePart = createPartFromString(name)
            val codePart = createPartFromString(code)
            val descPart = description?.let { createPartFromString(it) }
            val stitchablePart = createPartFromString("true")

            val segmentParts = applicableSegmentIds.map { id ->
                MultipartBody.Part.createFormData("applicableSegments[]", id)
            }

            val imagePart = imageUri?.let { uriToMultipartPart(context, it, "image") }

            val response = salesSettingsApi.createGarment(
                token = accessToken,
                csrfToken = csrfToken,
                name = namePart,
                displayName = displayNamePart,
                code = codePart,
                description = descPart,
                isCustomStitchable = stitchablePart,
                applicableSegments = segmentParts,
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create garment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGarment(
        context: Context,
        id: String,
        name: String,
        description: String?,
        applicableSegmentIds: List<String>,
        imageUri: Uri?
    ): Result<CreateGarmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val namePart = createPartFromString(name)
            val displayNamePart = createPartFromString(name)
            val descPart = description?.takeIf { it.isNotBlank() }?.let { createPartFromString(it) }
            val stitchablePart = createPartFromString("true")

            val segmentParts = applicableSegmentIds.map { segmentId ->
                MultipartBody.Part.createFormData("applicableSegments[]", segmentId)
            }

            val imagePart = imageUri?.let { uriToMultipartPart(context, it, "image") }

            val response = salesSettingsApi.updateGarment(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                name = namePart,
                displayName = displayNamePart,
                description = descPart,
                isCustomStitchable = stitchablePart,
                applicableSegments = segmentParts,
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update garment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeGarmentStatus(
        id: String,
        request: ChangeGarmentStatusRequest
    ): Result<ChangeGarmentStatusResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.changeGarmentStatus(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to change garment status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGarment(id: String): Result<DeleteGarmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.deleteGarment(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete garment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGarmentDetail(id: String): Result<GarmentDetail> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getGarmentDetail(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch garment details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGarmentBasicPrice(
        id: String,
        request: UpdateGarmentBasicPriceRequest
    ): Result<UpdateGarmentBasicPriceResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.updateGarmentBasicPrice(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update pricing: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 4. GARMENT STYLES / CATEGORIES (SALES)
    // ===========================================================

    fun getGarmentStyles(
        segmentId: String?,
        garmentId: String?
    ): Flow<Result<List<GarmentStyleItem>>> = flow {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getGarmentStyle(
                token = accessToken,
                csrfToken = csrfToken,
                segmentId = segmentId,
                garmentId = garmentId
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.data))
            } else {
                emit(Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch styles")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun createGarmentStyle(request: CreateGarmentStyleRequest): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createGarmentStyle(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create garment category"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGarmentStyle(id: String, request: CreateGarmentStyleRequest): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.updateGarmentStyle(accessToken, csrfToken, id, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to update garment category"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGarmentStyle(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.deleteGarmentStyle(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Garment category permanently deleted.")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to delete garment category"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGarmentCategoryById(id: String): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getGarmentCategoryById(accessToken, csrfToken, id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch garment category details"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeGarmentCategoryStatus(
        categoryId: String,
        newStatus: String
    ): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.changeGarmentCategoryStatus(
                token = accessToken,
                csrfToken = csrfToken,
                categoryId = categoryId,
                request = mapOf("status" to newStatus)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 5. MEASUREMENT FIELDS (SALES)
    // ===========================================================

    suspend fun getMeasurementFields(page: Int = 1, limit: Int = 50): Result<List<MeasurementFieldItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getMeasurementFields(accessToken, csrfToken, page, limit)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch measurement fields"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMeasurementField(request: CreateMeasurementFieldRequest): Result<MeasurementFieldItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createMeasurementField(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create measurement field"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMeasurementField(
        id: String,
        request: UpdateGarmentStyleRequest
    ): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.updateMeasurementField(accessToken, csrfToken, id, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to update measurement field"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeMeasurementFieldStatus(
        fieldId: String,
        status: String
    ): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = ChangeMeasurementFieldStatusRequest(status = status)
            val response = salesSettingsApi.changeMeasurementFieldStatus(
                token = accessToken,
                csrfToken = csrfToken,
                id = fieldId,
                request = request
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "Measurement field status successfully updated to $status.")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to update measurement field status"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateMeasurementField(fieldId: String): Result<DeactivateMeasurementFieldResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.deactivateMeasurementField(accessToken, csrfToken, fieldId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to deactivate measurement field"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 6. WORK PRICING (SALES)
    // ===========================================================

    suspend fun fetchWorkPricing(segmentId: String?, status: String?): Result<List<WorkPricingItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getWorkPricing(token = accessToken, csrfToken = csrfToken, segmentId = segmentId, status = status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error fetching work pricing"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkPricingViewOne(id: String): Result<WorkPricingDetail> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getWorkPricingViewOne(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch work pricing details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createWorkPricing(request: WorkPricingRequest): Result<WorkPricingResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createWorkPricing(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateWorkPricing(id: String, request: WorkPricingRequest): Result<WorkPricingResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.updateWorkPricing(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun changeWorkPricingStatus(id: String, status: String): Result<WorkPricingItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.changeWorkPricingStatus(accessToken, csrfToken, id, mapOf("status" to status))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Update Failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 7. LOCAL ROOM DB (MEASUREMENTS)
    // ===========================================================

    fun getLocalMeasurements(categoryId: String): Flow<SelectedGarment?> {
        return selectedGarmentDao.getGarmentByCategoryId(categoryId)
    }

    suspend fun saveSelectedFieldsToRoom(
        categoryId: String,
        categoryName: String,
        measurements: List<GarmentMeasurement>
    ) {
        val entity = SelectedGarment(
            id = categoryId,
            categoryId = categoryId,
            categoryName = categoryName,
            category = categoryName,
            orderSessionId = "garment_profile_$categoryId",
            measurements = measurements
        )
        selectedGarmentDao.insertGarment(entity)
    }

    suspend fun deleteLocalMeasurementField(categoryId: String, fieldId: String, currentMeasurements: List<GarmentMeasurement>) {
        val updated = currentMeasurements.filter { it.id != fieldId }
        val entity = SelectedGarment(
            id = categoryId,
            categoryId = categoryId,
            categoryName = "Garment Profile",
            orderSessionId = "garment_profile_$categoryId",
            measurements = updated
        )
        selectedGarmentDao.insertGarment(entity)
    }

    // ===========================================================
    // 8. FLOORS (INVENTORY)
    // ===========================================================

    suspend fun getFloors(
        warehouseId: String? = null,
        page: Int = 1,
        limit: Int = 50
    ): Result<List<FloorItemSettings>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<GetFloorsResponse> = inventoryApi.getFloors(
                token = accessToken,
                csrfToken = csrfToken,
                warehouseId = warehouseId,
                page = page,
                limit = limit
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.floors ?: emptyList())
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to fetch floors"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFloor(request: CreateFloorRequest): Result<FloorItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<FloorItem>> = inventoryApi.createFloor(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create floor"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFloor(
        id: String,
        request: CreateFloorRequest
    ): Result<FloorItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<FloorItem>> = inventoryApi.updateFloor(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = request
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to update floor"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFloor(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<Unit>> = inventoryApi.deleteFloor(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "Floor deleted successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to delete floor"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 9. SECTIONS (INVENTORY)
    // ===========================================================

    suspend fun getSections(
        warehouseId: String? = null,
        floorId: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<List<SectionItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<GetSectionsResponse> = inventoryApi.getSections(
                token = accessToken,
                csrfToken = csrfToken,
                warehouseId = warehouseId,
                floorId = floorId,
                page = page,
                limit = limit
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val list = body.sections ?: body.data ?: emptyList()
                Result.success(list)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch sections"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSection(request: CreateSectionRequest): Result<SectionItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<SectionItem>> = inventoryApi.createSection(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create section"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSection(
        id: String,
        request: CreateSectionRequest
    ): Result<SectionItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<SectionItem>> = inventoryApi.updateSection(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = request
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to update section"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSection(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<Unit>> = inventoryApi.deleteSection(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "Section deleted successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: body?.message ?: "Failed to delete section"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 10. RACKS (INVENTORY)
    // ===========================================================

    suspend fun getRacks(
        warehouseId: String? = null,
        floorId: String? = null,
        sectionId: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<List<RackItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<GetRacksResponse> = inventoryApi.getRacks(
                token = accessToken,
                csrfToken = csrfToken,
                warehouseId = warehouseId?.takeIf { it.isNotBlank() },
                floorId = floorId?.takeIf { it.isNotBlank() },
                sectionId = sectionId?.takeIf { it.isNotBlank() },
                page = page,
                limit = limit
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val list = body.racks ?: body.data ?: emptyList()
                Result.success(list)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch racks"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRack(request: CreateRackRequest): Result<RackItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<RackItem>> = inventoryApi.createRack(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create rack"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRack(
        id: String,
        request: CreateRackRequest
    ): Result<RackItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<RackItem>> = inventoryApi.updateRack(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = request
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to update rack"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRack(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<Unit>> = inventoryApi.deleteRack(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "Rack deleted successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to delete rack"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 11. BINS (INVENTORY)
    // ===========================================================

    // ===========================================================
    // 11. BINS (INVENTORY)
    // ===========================================================

    suspend fun getBins(
        page: Int = 1,
        limit: Int = 50
    ): Result<List<BinItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<GetBinsResponse> = inventoryApi.getBins(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val list = body.bins ?: body.data ?: emptyList()
                Result.success(list)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch bins"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBin(request: CreateBinRequest): Result<BinItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<BinItem>> = inventoryApi.createBin(accessToken, csrfToken, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to create bin"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBin(
        id: String,
        request: CreateBinRequest
    ): Result<BinItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<BinItem>> = inventoryApi.updateBin(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = request
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to update bin"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBin(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response: Response<BaseInventoryResponse<Unit>> = inventoryApi.deleteBin(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "Bin deleted successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: body?.message ?: "Failed to delete bin"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================================================
    // 12. DESIGNS (SALES)
    // ===========================================================

    suspend fun getDesigns(
        page: Int = 1,
        limit: Int = 20,
        designType: String? = null,
        status: String? = null,
        search: String? = null
    ): Result<List<DesignItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getDesigns(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                designType = designType,
                status = status,
                search = search
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch designs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDesignById(id: String): Result<DesignItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getDesignById(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch design detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDesign(
        context: Context,
        name: String,
        designType: String,
        code: String,
        description: String?,
        status: String = "Active",
        segmentIds: List<String>,
        imageUri: Uri?
    ): Result<DesignItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val params = mutableMapOf<String, RequestBody>(
                "name" to createPartFromString(name),
                "designType" to createPartFromString(designType),
                "code" to createPartFromString(code),
                "status" to createPartFromString(status)
            )
            description?.takeIf { it.isNotBlank() }?.let {
                params["description"] = createPartFromString(it)
            }

            val garmentParts = segmentIds.map { id ->
                MultipartBody.Part.createFormData("applicableGarments[][segmentId]", id)
            }
            val imagePart = imageUri?.let { uriToMultipartPart(context, it, "image") }

            val response = salesSettingsApi.createDesign(
                token = accessToken,
                csrfToken = csrfToken,
                params = params,
                applicableGarments = garmentParts,
                image = imagePart
            )

            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create design"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDesign(
        context: Context,
        id: String,
        name: String,
        designType: String,
        code: String,
        description: String?,
        status: String,
        segmentIds: List<String>,
        imageUri: Uri?
    ): Result<DesignItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val params = mutableMapOf<String, RequestBody>(
                "name" to createPartFromString(name),
                "designType" to createPartFromString(designType),
                "code" to createPartFromString(code),
                "status" to createPartFromString(status)
            )
            description?.takeIf { it.isNotBlank() }?.let {
                params["description"] = createPartFromString(it)
            }

            val garmentParts = segmentIds.map { segmentId ->
                MultipartBody.Part.createFormData("applicableGarments[][segmentId]", segmentId)
            }
            val imagePart = imageUri?.let { uriToMultipartPart(context, it, "image") }

            val response = salesSettingsApi.updateDesign(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                params = params,
                applicableGarments = garmentParts,
                image = imagePart
            )

            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update design"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeDesignStatus(id: String, nextStatus: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = ChangeDesignStatusRequest(status = nextStatus)
            val response = salesSettingsApi.changeDesignStatus(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = request
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.message ?: "Status updated successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update design status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDesign(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.deleteDesign(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.message ?: "Design deleted successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete design"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}