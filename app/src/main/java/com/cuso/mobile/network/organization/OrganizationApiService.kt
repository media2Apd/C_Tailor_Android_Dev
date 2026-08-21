package com.cuso.mobile.network.organization

import com.cuso.mobile.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface OrganizationApiService {
    @Multipart
    @POST("api/organizations/upload-picture")
    suspend fun uploadOrganizationPicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part picture: MultipartBody.Part
    ): Response<UploadOrganizationPictureResponse>

    @PUT("/api/organizations/update-one")
    suspend fun updateOrganization(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: UpdateOrganizationRequest
    ): Response<UpdateOrganizationResponse>

    @GET("/api/branches/view-all")
    suspend fun getBranches(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): BranchListResponse

    @POST("/api/branches/create")
    suspend fun createBranch(
        @Header("Authorization") authorization: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Body request: CreateBranchRequest
    ): CreateBranchResponse

    @PUT("/api/branches/update-one/{id}")
    suspend fun updateBranch(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateBranchRequest
    ): Response<UpdateBranchResponse>

    @GET("/api/departments/view-all")
    suspend fun getDepartments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): DepartmentResponse

    @POST("/api/departments/create")
    suspend fun createDepartment(
        @Header("Authorization") authorization: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Body request: DepartmentCreateRequest
    ): DepartmentCreateResponse

    @PUT("/api/departments/update-one/{id}")
    suspend fun updateDepartment(
        @Header("Authorization") authorization: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Path("id") id: String,
        @Body request: DepartmentUpdateRequest
    ): DepartmentUpdateResponse

    @GET("/api/designations/view-all")
    suspend fun getDesignations(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<DesignationListResponse>

    @POST("/api/designations/create")
    suspend fun createDesignation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: DesignationCreateRequest
    ): Response<DesignationCreateResponse>

    @PUT("/api/designations/update-one/{id}")
    suspend fun updateDesignation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: DesignationUpdateRequest
    ): Response<DesignationUpdateResponse>

    @DELETE("/api/designations/delete-one/{id}")
    suspend fun deleteDesignation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DesignationDeleteResponse>

    @GET("/api/org-garments/view-all")
    suspend fun getOrgGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<com.cuso.mobile.model.sales.GarmentCategoriesResponse>

    @GET("/api/org-garments/view-all")
    suspend fun getActiveOrgGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ActiveOrgGarmentResponse>

    @GET("/api/common/categories/view-all")
    suspend fun getOrgGarmentCommonCategories(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<OrgGarmentResponse>

    @POST("/api/org-garments/add")
    suspend fun addOrgGarmentCategory(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: AddGarmentRequest
    ): Response<AddOrgGarmentResponse>

    @DELETE("/api/org-garments/remove/{categoryId}")
    suspend fun removeOrgGarmentCategory(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("categoryId") categoryId: String
    ): Response<RemoveOrgGarmentResponse>
}