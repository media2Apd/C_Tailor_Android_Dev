package com.cuso.mobile.network.sales.settings

import com.cuso.mobile.model.settings.ChangeGarmentStatusRequest
import com.cuso.mobile.model.settings.ChangeGarmentStatusResponse
import com.cuso.mobile.model.settings.ChangeSegmentStatusRequest
import com.cuso.mobile.model.settings.ChangeSegmentStatusResponse
import com.cuso.mobile.model.settings.CreateGarmentRequest
import com.cuso.mobile.model.settings.CreateGarmentResponse
import com.cuso.mobile.model.settings.CreateGarmentStyleRequest
import com.cuso.mobile.model.settings.CreateMeasurementFieldRequest
import com.cuso.mobile.model.settings.CreateSegmentRequest
import com.cuso.mobile.model.settings.CreateSegmentResponse
import com.cuso.mobile.model.settings.DeactivateMeasurementFieldResponse
import com.cuso.mobile.model.settings.DeleteGarmentStyleResponse
import com.cuso.mobile.model.settings.DeleteSegmentResponse
import com.cuso.mobile.model.settings.GarmentListResponse
import com.cuso.mobile.model.settings.GarmentStyleDetailResponse
import com.cuso.mobile.model.settings.GarmentStyleListResponse
import com.cuso.mobile.model.settings.MeasurementFieldDetailResponse
import com.cuso.mobile.model.settings.MeasurementFieldListResponse
import com.cuso.mobile.model.settings.SegmentDetailResponse
import com.cuso.mobile.model.settings.SegmentListResponse
import com.cuso.mobile.model.settings.UpdateGarmentStyleRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SalesSettingsApiService {
    // Fetch all segments
    @GET("/api/sales/segments/view-all")
    suspend fun getSegments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<SegmentListResponse>

    // View one segment by ID
    @GET("/api/sales/segments/view-one/{id}")
    suspend fun getSegmentById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SegmentDetailResponse>

    // Add PUT/PATCH endpoint for updating a segment
    @PUT("/api/sales/segments/update-one/{id}")
    suspend fun updateSegment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateSegmentRequest
    ): Response<CreateSegmentResponse>

    // Create a new segment
    @POST("/api/sales/segments/create")
    suspend fun createSegment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateSegmentRequest
    ): Response<CreateSegmentResponse>

    //Delete a segment
    @DELETE("api/sales/segments/delete-one/{id}")
    suspend fun deleteSegment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteSegmentResponse>

    // ── Garment Endpoints ──
    @GET("/api/sales/garments/view-all")
    suspend fun getGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<GarmentListResponse>

    //create new garment
    @POST("/api/sales/garments/create")
    suspend fun createGarment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateGarmentRequest
    ): Response<CreateGarmentResponse>
    //fetch garment category
    @GET("/api/sales/garment-categories/view-all")
    suspend fun getGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("segmentId") segmentId: String?,
        @Query("garmentId") garmentId: String?
    ): Response<GarmentStyleListResponse>

    // 1. Create Garment category
    @POST("/api/sales/garment-categories/create")
    suspend fun createGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateGarmentStyleRequest
    ): Response<GarmentStyleDetailResponse>

    // 2. Update Garment category
    @PUT("/api/sales/garment-categories/update-one/{id}")
    suspend fun updateGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateGarmentStyleRequest
    ): Response<GarmentStyleDetailResponse>

    // 3. Delete Garment category
    @DELETE("/api/sales/garment-categories/delete-one/{id}")
    suspend fun deleteGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteGarmentStyleResponse>

    // ── Get All Measurement Fields ──
    @GET("/api/sales/measurement-fields/view-all")
    suspend fun getMeasurementFields(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MeasurementFieldListResponse>

    // ── Create Measurement Field ──
    @POST("/api/sales/measurement-fields/create")
    suspend fun createMeasurementField(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateMeasurementFieldRequest
    ): Response<MeasurementFieldDetailResponse>

    // ── Update Single Measurement Field ──
    @PUT("/api/sales/garment-categories/update-one/{id}")
    suspend fun updateMeasurementField(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateGarmentStyleRequest
    ): Response<GarmentStyleDetailResponse>

    // ── Garment Categories View One ──
    @GET("/api/sales/garment-categories/view-one/{id}")
    suspend fun getGarmentCategoryById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GarmentStyleDetailResponse>

    // ── Deactivate measurement ──
    @DELETE("sales/settings/measurement-fields/{id}/deactivate")
    suspend fun deactivateMeasurementField(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") fieldId: String
    ): Response<DeactivateMeasurementFieldResponse>

    @PATCH("/api/sales/segments/change-status/{id}")
    suspend fun changeSegmentStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: ChangeSegmentStatusRequest
    ): Response<ChangeSegmentStatusResponse>

    @PATCH("/api/sales/garments/change-status/{id}")
    suspend fun changeGarmentStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: ChangeGarmentStatusRequest
    ): Response<ChangeGarmentStatusResponse>
}