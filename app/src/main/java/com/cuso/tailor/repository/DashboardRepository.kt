package com.cuso.tailor.repository

import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.model.sales.DashboardData
import com.cuso.tailor.network.user.UserApiService
import javax.inject.Inject
import javax.inject.Singleton

interface DashboardRepository {
    suspend fun getAdvancedDashboard(): Result<DashboardData>
}

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
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

            val response = userApi.getDashboardDetails(
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