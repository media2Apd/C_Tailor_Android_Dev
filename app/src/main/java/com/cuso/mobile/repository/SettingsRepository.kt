package com.cuso.mobile.repository

import com.android.volley.Response
import com.cuso.mobile.database.dao.SelectedGarmentDao
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.database.entities.GarmentMeasurement
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.settings.CreateGarmentRequest
import com.cuso.mobile.model.settings.CreateGarmentResponse
import com.cuso.mobile.model.settings.CreateGarmentStyleRequest
import com.cuso.mobile.model.settings.CreateMeasurementFieldRequest
import com.cuso.mobile.model.settings.CreateSegmentRequest
import com.cuso.mobile.model.settings.CreateSegmentResponse
import com.cuso.mobile.model.settings.DeactivateMeasurementFieldResponse
import com.cuso.mobile.model.settings.DeleteSegmentResponse
import com.cuso.mobile.model.settings.GarmentItem
import com.cuso.mobile.model.settings.GarmentStyleItem
import com.cuso.mobile.model.settings.MeasurementFieldItem
import com.cuso.mobile.model.settings.SegmentItem
import com.cuso.mobile.model.settings.UpdateGarmentStyleRequest
import com.cuso.mobile.network.sales.settings.SalesSettingsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val salesSettingsApi: SalesSettingsApiService,
    private val tokensDao: TokensDao,
    val selectedGarmentDao: SelectedGarmentDao // 👈 1. Injected SelectedGarmentDao
) {
    // Helper method to fetch auth tokens from local database
    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // Fetch all segments list
    suspend fun getSegments(page: Int = 1, limit: Int = 50): Result<List<SegmentItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getSegments(accessToken, csrfToken, page, limit)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to fetch segments"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch single segment by ID (View One)
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

    // Call API to create a new garment segment
    suspend fun createSegment(request: CreateSegmentRequest): Result<CreateSegmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createSegment(accessToken, csrfToken, request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to create segment"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Call API to update segment
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

    // ── Garment Operations ──
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

    suspend fun createGarment(request: CreateGarmentRequest): Result<CreateGarmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createGarment(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create garment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Get Garment Style ──
    fun getGarmentStyles(
        segmentId: String?,
        garmentId: String?
    ): Flow<Result<List<GarmentStyleItem>>> = flow {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            android.util.Log.d("API_QUERY", "Calling view-all with segmentId=$segmentId, garmentId=$garmentId")
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

    // ── Create Garment Style ──
    suspend fun createGarmentStyle(request: CreateGarmentStyleRequest): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createGarmentStyle(accessToken, csrfToken, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to create garment category"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update Garment Style ──
    suspend fun updateGarmentStyle(id: String, request: CreateGarmentStyleRequest): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.updateGarmentStyle(accessToken, csrfToken, id, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to update garment category"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Delete Garment Style ──
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

    // ── Fetch Measurement Fields ──
    suspend fun getMeasurementFields(page: Int = 1, limit: Int = 50): Result<List<MeasurementFieldItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getMeasurementFields(accessToken, csrfToken, page, limit)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to fetch measurement fields"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Create Measurement Field ──
    suspend fun createMeasurementField(request: CreateMeasurementFieldRequest): Result<MeasurementFieldItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.createMeasurementField(accessToken, csrfToken, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to create measurement field"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update Measurement Field ──
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
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to update measurement field"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ── Fetch Single Garment Category by ID (View One) ──
    suspend fun getGarmentCategoryById(id: String): Result<GarmentStyleItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.getGarmentCategoryById(accessToken, csrfToken, id)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to fetch garment category details"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Deactivate Measurement Field ──
    suspend fun deactivateMeasurementField(fieldId: String): Result<DeactivateMeasurementFieldResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesSettingsApi.deactivateMeasurementField(accessToken, csrfToken, fieldId)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to deactivate measurement field"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 2. Room DB Local Measurement Operations ──
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
}