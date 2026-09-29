@file:Suppress("unused")

package com.cuso.tailor.repository

import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.model.settings.CreateStageRequest
import com.cuso.tailor.model.settings.CreateTemplateRequest
import com.cuso.tailor.model.settings.ProductionStageDto
import com.cuso.tailor.model.settings.ProductionTemplateDto
import com.cuso.tailor.model.settings.StageListResponse
import com.cuso.tailor.model.settings.TemplateListResponse
import com.cuso.tailor.network.services.settings.ServiceSettingsApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServicesRepository @Inject constructor(
    private val templateApi: ServiceSettingsApiInterface,
    private val tokensDao: TokensDao
) {
    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens() ?: throw Exception("Session expired. Please log in again.")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // ─────────────────────────────────────────────────────────────
    // Template Methods
    // ─────────────────────────────────────────────────────────────

    suspend fun getTemplates(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<TemplateListResponse> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.getProductionTemplates(token, csrf, page, limit, search, status)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractError(response, "Failed to fetch production templates")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTemplateById(id: String): Result<ProductionTemplateDto> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.getProductionTemplateById(token, csrf, id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractError(response, "Failed to load template details")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTemplate(request: CreateTemplateRequest): Result<ProductionTemplateDto> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.createProductionTemplate(token, csrf, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractError(response, "Failed to create template")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTemplate(id: String, request: CreateTemplateRequest): Result<ProductionTemplateDto> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.updateProductionTemplate(token, csrf, id, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractError(response, "Failed to update template")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTemplate(id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.deleteProductionTemplate(token, csrf, id)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Template deleted successfully")
            } else {
                Result.failure(Exception(extractError(response, "Failed to delete template")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Production Stage Methods
    // ─────────────────────────────────────────────────────────────

    suspend fun getProductionStages(
        page: Int = 1,
        limit: Int = 50,
        search: String? = null,
        status: String? = null
    ): Result<StageListResponse> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.getProductionStages(token, csrf, page, limit, search, status)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractError(response, "Failed to fetch production stages")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductionStageById(id: String): Result<ProductionStageDto> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.getProductionStageById(token, csrf, id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractError(response, "Failed to load stage details")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProductionStage(request: CreateStageRequest): Result<ProductionStageDto> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.createProductionStage(token, csrf, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractError(response, "Failed to create stage")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProductionStage(id: String, request: CreateStageRequest): Result<ProductionStageDto> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.updateProductionStage(token, csrf, id, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractError(response, "Failed to update stage")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProductionStage(id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (token, csrf) = getAuthHeaders()
            val response = templateApi.deleteProductionStage(token, csrf, id)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Production stage deleted successfully")
            } else {
                Result.failure(Exception(extractError(response, "Failed to delete production stage")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> extractError(response: Response<T>, fallback: String): String {
        return response.errorBody()?.string() ?: response.message().takeIf { it.isNotBlank() } ?: fallback
    }
}