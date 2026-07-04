package com.cuso.mobile.repository

import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.model.DashboardData
import com.cuso.mobile.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

interface DashboardRepository {
    suspend fun getAdvancedDashboard(): Result<DashboardData>
}

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokensDao: TokensDao
) : DashboardRepository {

    // Same pattern as SalesRepository.getAuthHeaders()
    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    override suspend fun getAdvancedDashboard(): Result<DashboardData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val response = apiService.getDashboardDetails(
                token = accessToken,
                csrfToken = csrfToken
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: response.errorBody()?.string()
                            ?: "Failed to load dashboard: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}