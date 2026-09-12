package com.cuso.mobile.network.inventory.settings

import com.cuso.mobile.model.inventory.ProductCategoriesResponse
import com.cuso.mobile.model.settings.BaseInventoryResponse
import com.cuso.mobile.model.settings.BinItem
import com.cuso.mobile.model.settings.CreateBinRequest
import com.cuso.mobile.model.settings.CreateFloorRequest
import com.cuso.mobile.model.settings.CreateRackRequest
import com.cuso.mobile.model.settings.CreateSectionRequest
import com.cuso.mobile.model.settings.FloorItem
import com.cuso.mobile.model.settings.GetBinsResponse
import com.cuso.mobile.model.settings.GetFloorsResponse
import com.cuso.mobile.model.settings.GetRacksResponse
import com.cuso.mobile.model.settings.GetSectionsResponse
import com.cuso.mobile.model.settings.RackItem
import com.cuso.mobile.model.settings.SectionItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InventorySettingsApiService {

    // ===========================================================
    // 1. PRODUCT CATEGORY ENDPOINTS
    // ===========================================================

    @GET("/api/inventory/category/dropdown")
    suspend fun getProductCategoriesDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ProductCategoriesResponse>

    // ===========================================================
    // 2. FLOOR ENDPOINTS
    // ===========================================================

    @GET("/api/inventory/settings/floor/view-all")
    suspend fun getFloors(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<GetFloorsResponse>

    @POST("/api/inventory/settings/floor/create")
    suspend fun createFloor(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateFloorRequest
    ): Response<BaseInventoryResponse<FloorItem>>

    @PUT("/api/inventory/settings/floor/update-one/{id}")
    suspend fun updateFloor(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateFloorRequest
    ): Response<BaseInventoryResponse<FloorItem>>

    @DELETE("/api/inventory/settings/floor/delete-one/{id}")
    suspend fun deleteFloor(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BaseInventoryResponse<Unit>>

    // ===========================================================
    // 3. SECTION ENDPOINTS
    // ===========================================================

    @GET("/api/inventory/settings/section/view-all")
    suspend fun getSections(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null,
        @Query("floorId") floorId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GetSectionsResponse>

    @POST("/api/inventory/settings/section/create")
    suspend fun createSection(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateSectionRequest
    ): Response<BaseInventoryResponse<SectionItem>>

    @PUT("/api/inventory/settings/section/update-one/{id}")
    suspend fun updateSection(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateSectionRequest
    ): Response<BaseInventoryResponse<SectionItem>>

    @DELETE("/api/inventory/settings/section/delete-one/{id}")
    suspend fun deleteSection(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BaseInventoryResponse<Unit>>

    // ===========================================================
    // 4. RACK ENDPOINTS
    // ===========================================================

    @GET("/api/inventory/settings/rack/view-all")
    suspend fun getRacks(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null,
        @Query("floorId") floorId: String? = null,
        @Query("sectionId") sectionId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GetRacksResponse>

    @POST("/api/inventory/settings/rack/create")
    suspend fun createRack(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateRackRequest
    ): Response<BaseInventoryResponse<RackItem>>

    @PUT("/api/inventory/settings/rack/update-one/{id}")
    suspend fun updateRack(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateRackRequest
    ): Response<BaseInventoryResponse<RackItem>>

    @DELETE("/api/inventory/settings/rack/delete-one/{id}")
    suspend fun deleteRack(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BaseInventoryResponse<Unit>>

    // ===========================================================
    // 5. BIN ENDPOINTS
    // ===========================================================

    @GET("/api/inventory/settings/bin/view-all")
    suspend fun getBins(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<GetBinsResponse>

    @POST("/api/inventory/settings/bin/create")
    suspend fun createBin(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateBinRequest
    ): Response<BaseInventoryResponse<BinItem>>

    @PUT("/api/inventory/settings/bin/update-one/{id}")
    suspend fun updateBin(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateBinRequest
    ): Response<BaseInventoryResponse<BinItem>>

    @DELETE("/api/inventory/settings/bin/delete-one/{id}")
    suspend fun deleteBin(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<BaseInventoryResponse<Unit>>
}