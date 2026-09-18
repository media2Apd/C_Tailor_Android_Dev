package com.cuso.tailor.network.user

import com.cuso.tailor.model.sales.DashboardResponse
import com.cuso.tailor.model.sales.meResponse
import com.cuso.tailor.model.sales.myLayoutResponse
import com.cuso.tailor.model.sales.myOrganizationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApiService {
    @GET("/api/members/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<meResponse>

    @GET("/api/organizations/my-organization")
    suspend fun getMyOrganization(@Header("Authorization") token: String): Response<myOrganizationResponse>

    @GET("/api/dashboard-preference/my-layout")
    suspend fun getMyLayout(@Header("Authorization") token: String): Response<myLayoutResponse>

    @GET("/api/dashboard/advanced-dashboard")
    suspend fun getDashboardDetails(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<DashboardResponse>
}