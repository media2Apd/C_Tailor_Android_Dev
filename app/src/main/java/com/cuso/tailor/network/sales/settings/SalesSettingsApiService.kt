package com.cuso.tailor.network.sales.settings

import com.cuso.tailor.model.settings.ChangeDesignStatusRequest
import com.cuso.tailor.model.settings.ChangeDesignStatusResponse
import com.cuso.tailor.model.settings.ChangeGarmentCategoryStatusResponse
import com.cuso.tailor.model.settings.ChangeGarmentStatusRequest
import com.cuso.tailor.model.settings.ChangeGarmentStatusResponse
import com.cuso.tailor.model.settings.ChangeMeasurementFieldStatusRequest
import com.cuso.tailor.model.settings.ChangeMeasurementFieldStatusResponse
import com.cuso.tailor.model.settings.ChangeSegmentStatusRequest
import com.cuso.tailor.model.settings.ChangeSegmentStatusResponse
import com.cuso.tailor.model.settings.CreateGarmentResponse
import com.cuso.tailor.model.settings.CreateGarmentStyleRequest
import com.cuso.tailor.model.settings.CreateMeasurementFieldRequest
import com.cuso.tailor.model.settings.CreateSegmentRequest
import com.cuso.tailor.model.settings.CreateSegmentResponse
import com.cuso.tailor.model.settings.DeactivateMeasurementFieldResponse
import com.cuso.tailor.model.settings.DeleteDesignResponse
import com.cuso.tailor.model.settings.DeleteGarmentResponse
import com.cuso.tailor.model.settings.DeleteGarmentStyleResponse
import com.cuso.tailor.model.settings.DeleteSegmentResponse
import com.cuso.tailor.model.settings.DesignDetailResponse
import com.cuso.tailor.model.settings.DesignListResponse
import com.cuso.tailor.model.settings.GarmentDetailResponse
import com.cuso.tailor.model.settings.GarmentListResponse
import com.cuso.tailor.model.settings.GarmentStyleDetailResponse
import com.cuso.tailor.model.settings.GarmentStyleListResponse
import com.cuso.tailor.model.settings.MeasurementFieldDetailResponse
import com.cuso.tailor.model.settings.MeasurementFieldListResponse
import com.cuso.tailor.model.settings.SegmentDetailResponse
import com.cuso.tailor.model.settings.SegmentListResponse
import com.cuso.tailor.model.settings.UpdateGarmentBasicPriceRequest
import com.cuso.tailor.model.settings.UpdateGarmentBasicPriceResponse
import com.cuso.tailor.model.settings.UpdateGarmentStyleRequest
import com.cuso.tailor.model.settings.WorkPricingDetailResponse
import com.cuso.tailor.model.settings.WorkPricingListResponse
import com.cuso.tailor.model.settings.WorkPricingRequest
import com.cuso.tailor.model.settings.WorkPricingResponse
import com.cuso.tailor.model.settings.WorkPricingResponseForChangeStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface SalesSettingsApiService {

    // ═══════════════════════════════════════════════════════════════
    // 1. SEGMENTS ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    // Fetch all segments
    @GET("/api/sales/settings/segments/view-all")
    suspend fun getSegments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<SegmentListResponse>

    // View one segment by ID
    @GET("/api/sales/settings/segments/view-one/{id}")
    suspend fun getSegmentById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SegmentDetailResponse>

    // Create a new segment
    @POST("/api/sales/settings/segments/create")
    suspend fun createSegment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateSegmentRequest
    ): Response<CreateSegmentResponse>

    // Update an existing segment
    @PUT("/api/sales/settings/segments/update-one/{id}")
    suspend fun updateSegment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateSegmentRequest
    ): Response<CreateSegmentResponse>

    // Delete a segment
    @DELETE("/api/sales/settings/segments/delete-one/{id}")
    suspend fun deleteSegment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteSegmentResponse>

    // Change segment active/inactive status
    @PATCH("/api/sales/settings/segments/change-status/{id}")
    suspend fun changeSegmentStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: ChangeSegmentStatusRequest
    ): Response<ChangeSegmentStatusResponse>


    // ═══════════════════════════════════════════════════════════════
    // 2. GARMENTS ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    // Fetch all garments
    @GET("/api/sales/settings/garments/view-all")
    suspend fun getGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<GarmentListResponse>

    // View one garment details by ID
    @GET("/api/sales/settings/garments/view-one/{id}")
    suspend fun getGarmentDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") garmentId: String
    ): Response<GarmentDetailResponse>

    // Create new garment with Single Image (Multipart Form-Data)
    @Multipart
    @POST("/api/sales/settings/garments/create")
    suspend fun createGarment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part("name") name: RequestBody,
        @Part("displayName") displayName: RequestBody,
        @Part("code") code: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("isCustomStitchable") isCustomStitchable: RequestBody,
        @Part applicableSegments: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part? // <-- Single image part
    ): Response<CreateGarmentResponse>

    // Update existing garment with Single Image (Multipart Form-Data)
    @Multipart
    @PUT("/api/sales/settings/garments/update-one/{id}")
    suspend fun updateGarment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Part("name") name: RequestBody,
        @Part("displayName") displayName: RequestBody,
//        @Part("code") code: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("isCustomStitchable") isCustomStitchable: RequestBody,
        @Part applicableSegments: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part? // <-- Single image part (optional)
    ): Response<CreateGarmentResponse>

    // Update garment basic pricing and status
    @PUT("/api/sales/settings/garments/update-one/{id}")
    suspend fun updateGarmentBasicPrice(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateGarmentBasicPriceRequest
    ): Response<UpdateGarmentBasicPriceResponse>

    // Change garment active/inactive status
    @PATCH("/api/sales/settings/garments/change-status/{id}")
    suspend fun changeGarmentStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: ChangeGarmentStatusRequest
    ): Response<ChangeGarmentStatusResponse>

    // Delete a garment by ID
    @DELETE("/api/sales/settings/garments/delete-one/{id}")
    suspend fun deleteGarment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteGarmentResponse>


    // ═══════════════════════════════════════════════════════════════
    // 3. GARMENT CATEGORIES / STYLES ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    // Fetch all garment styles / categories
    @GET("/api/sales/settings/garment-categories/view-all")
    suspend fun getGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("segmentId") segmentId: String?,
        @Query("garmentId") garmentId: String?
    ): Response<GarmentStyleListResponse>

    // View one garment category by ID
    @GET("/api/sales/settings/garment-categories/view-one/{id}")
    suspend fun getGarmentCategoryById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GarmentStyleDetailResponse>

    // Create garment category / style
    @POST("/api/sales/settings/garment-categories/create")
    suspend fun createGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateGarmentStyleRequest
    ): Response<GarmentStyleDetailResponse>

    // Update garment category / style
    @PUT("/api/sales/settings/garment-categories/update-one/{id}")
    suspend fun updateGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateGarmentStyleRequest
    ): Response<GarmentStyleDetailResponse>

    // Update measurement fields inside garment category
    @PUT("/api/sales/settings/garment-categories/update-one/{id}")
    suspend fun updateMeasurementField(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateGarmentStyleRequest
    ): Response<GarmentStyleDetailResponse>

    // Delete garment category / style
    @DELETE("/api/sales/settings/garment-categories/delete-one/{id}")
    suspend fun deleteGarmentStyle(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteGarmentStyleResponse>

    // Change garment category status
    @PATCH("/api/sales/settings/garment-categories/change-status/{id}")
    suspend fun changeGarmentCategoryStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") categoryId: String,
        @Body request: Map<String, String>
    ): Response<ChangeGarmentCategoryStatusResponse>


    // ═══════════════════════════════════════════════════════════════
    // 4. MEASUREMENT FIELDS ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    // Get all measurement fields
    @GET("/api/sales/settings/measurement-fields/view-all")
    suspend fun getMeasurementFields(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MeasurementFieldListResponse>

    // Create a new measurement field
    @POST("/api/sales/settings/measurement-fields/create")
    suspend fun createMeasurementField(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateMeasurementFieldRequest
    ): Response<MeasurementFieldDetailResponse>

    // Change measurement field status
    @PATCH("/api/sales/settings/measurement-fields/change-status/{id}")
    suspend fun changeMeasurementFieldStatus(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String,
        @Body request: ChangeMeasurementFieldStatusRequest
    ): Response<ChangeMeasurementFieldStatusResponse>

    // Deactivate a measurement field
    @DELETE("/api/sales/settings/measurement-fields/{id}/deactivate")
    suspend fun deactivateMeasurementField(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") fieldId: String
    ): Response<DeactivateMeasurementFieldResponse>


    // ═══════════════════════════════════════════════════════════════
    // 5. WORK PRICING ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    // Fetch all work pricing list
    @GET("/api/sales/settings/work-pricing/view-all")
    suspend fun getWorkPricing(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("status") status: String? = null,
        @Query("segmentId") segmentId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<WorkPricingListResponse>

    // View one work pricing item by ID
    @GET("/api/sales/settings/work-pricing/view-one/{id}")
    suspend fun getWorkPricingViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<WorkPricingDetailResponse>

    // Create new work pricing
    @POST("/api/sales/settings/work-pricing/create")
    suspend fun createWorkPricing(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: WorkPricingRequest
    ): Response<WorkPricingResponse>

    // Update existing work pricing
    @PUT("/api/sales/settings/work-pricing/update-one/{id}")
    suspend fun updateWorkPricing(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: WorkPricingRequest
    ): Response<WorkPricingResponse>

    // Change work pricing status
    @PATCH("/api/sales/settings/work-pricing/change-status/{id}")
    suspend fun changeWorkPricingStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<WorkPricingResponseForChangeStatus>

    // =========================================================================
    // 6. DESIGN LIST & VIEW OPERATIONS
    // =========================================================================

    /**
     * Get paginated design templates list with optional filters.
     */
    @GET("/api/sales/settings/designs/view-all")
    suspend fun getDesigns(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("designType") designType: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): Response<DesignListResponse>

    /**
     * Get single design details by ID.
     */
    @GET("/api/sales/settings/designs/view-one/{id}")
    suspend fun getDesignById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DesignDetailResponse>

    // =========================================================================
    // 7. DESIGN MUTATION OPERATIONS
    // =========================================================================

    /**
     * Create a new design with multipart form-data.
     */
    @Multipart
    @POST("/api/sales/settings/designs/create")
    suspend fun createDesign(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part applicableGarments: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part?
    ): Response<DesignDetailResponse>

    /**
     * Update an existing design by ID with multipart form-data.
     */
    @Multipart
    @PUT("/api/sales/settings/designs/update-one/{id}")
    suspend fun updateDesign(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part applicableGarments: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part?
    ): Response<DesignDetailResponse>

    /**
     * Change active/inactive status of a design template.
     */
    @PATCH("/api/sales/settings/designs/change-status/{id}")
    suspend fun changeDesignStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: ChangeDesignStatusRequest
    ): Response<ChangeDesignStatusResponse>

    /**
     * Delete a design template by ID.
     */
    @DELETE("/api/sales/settings/designs/delete-one/{id}")
    suspend fun deleteDesign(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteDesignResponse>
}