package com.cuso.mobile.network.hr

import com.cuso.mobile.model.hr.*
import com.cuso.mobile.model.sales.StaffResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface HrApiService {
    @GET("/api/members/dropdown-filter")
    suspend fun getMembersDropdownFilter(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<StaffResponse>

    @GET("/api/members/view-all")
    suspend fun getMembers(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<MemberListResponse>

    @GET("/api/members/view-one/{id}")
    suspend fun getMemberViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") memberId: String
    ): Response<MemberDetailResponse>

    @POST("/api/members/create")
    suspend fun createMember(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateMemberRequest
    ): Response<CreateMemberResponse>

    @PUT("/api/members/update-one/{id}")
    suspend fun updateMember(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") memberId: String,
        @Body request: UpdateMemberRequest
    ): Response<CreateMemberResponse>

    @Multipart
    @PUT("/api/members/update/profile-picture/{memberId}")
    suspend fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("memberId") memberId: String,
        @Part file: MultipartBody.Part
    ): Response<UploadProfilePictureResponse>

    @DELETE("/api/members/delete/profile-picture/{memberId}")
    suspend fun deleteProfilePicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("memberId") memberId: String
    ): Response<DeleteProfilePictureResponse>

    @GET("/api/roles/view-all")
    suspend fun getRoles(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<RoleListResponse>

    @GET("/api/shifts/view-all")
    suspend fun getShifts(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ShiftListResponse>
}