package com.cuso.mobile.network.inventory.settings

import retrofit2.Response
import com.cuso.mobile.model.settings.BaseInventoryResponse
import com.cuso.mobile.model.settings.BinItem
import com.cuso.mobile.model.settings.CreateBinRequest
import com.cuso.mobile.model.settings.CreateFloorRequest
import com.cuso.mobile.model.settings.CreateRackRequest
import com.cuso.mobile.model.settings.CreateSectionRequest
import com.cuso.mobile.model.settings.FloorItem
import com.cuso.mobile.model.settings.GetBinsResponse
import com.cuso.mobile.model.settings.RackItem
import com.cuso.mobile.model.settings.SectionItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface InventorySettingsApiService {
    @POST("/api/inventory/floor/create")
    suspend fun createFloor(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: CreateFloorRequest
    ): Response<BaseInventoryResponse<FloorItem>>

    @GET("inventory/floors")
    suspend fun getFloors(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null
    ): Response<BaseInventoryResponse<List<FloorItem>>>

    @POST("/api/inventory/section/create")
    suspend fun createSection(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: CreateSectionRequest
    ): Response<BaseInventoryResponse<SectionItem>>

    @GET("inventory/sections")
    suspend fun getSections(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null,
        @Query("floorId") floorId: String? = null
    ): Response<BaseInventoryResponse<List<SectionItem>>>

    @POST("/api/inventory/rack/create")
    suspend fun createRack(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: CreateRackRequest
    ): Response<BaseInventoryResponse<RackItem>>

    @GET("inventory/racks")
    suspend fun getRacks(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null,
        @Query("floorId") floorId: String? = null,
        @Query("sectionId") sectionId: String? = null
    ): Response<BaseInventoryResponse<List<RackItem>>>

    @POST("/api/inventory/bin/create")
    suspend fun createBin(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Body request: CreateBinRequest
    ): Response<BaseInventoryResponse<BinItem>>

    @GET("/api/inventory/bin/view-all")
    suspend fun getBins(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Query("warehouseId") warehouseId: String? = null,
        @Query("rackId") rackId: String? = null
    ): Response<GetBinsResponse>
}