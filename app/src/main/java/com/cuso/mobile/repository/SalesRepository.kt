// com/cuso/mobile/repository/SalesRepository.kt

package com.cuso.mobile.repository

import AddGarmentRequest
import AddOrgGarmentResponse
import OrgGarmentCategory
import RemoveOrgGarmentResponse
import com.cuso.mobile.database.dao.LeadDao
import com.cuso.mobile.database.dao.SalesStatusDao
import com.cuso.mobile.database.dao.SalesSummaryDao
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.database.entities.LeadEntity
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.database.entities.SalesSummaryEntity
import com.cuso.mobile.database.entities.toEntity
import com.cuso.mobile.model.CategoryItem
import com.cuso.mobile.model.CreateLeadFormRequest
import com.cuso.mobile.model.CreateLeadFormResponse
import com.cuso.mobile.model.LeadTableItem
import com.cuso.mobile.model.StaffDto
import com.cuso.mobile.model.UpdateLeadResponse
import com.cuso.mobile.model.ViewOneLeadData
import com.cuso.mobile.model.toEntity
import com.cuso.mobile.network.ApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
@Suppress("unused")
class SalesRepository @Inject constructor(
    private val api: ApiService,
    private val salesStatusDao: SalesStatusDao,
    private val salesSummaryDao: SalesSummaryDao,
    private val tokensDao: TokensDao,
    private val leadDao: LeadDao
) {

    // helper to get tokens or throw early
    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // Sales statuses
    fun getSalesStatuses(): Flow<List<SalesStatusEntity>> =
        salesStatusDao.getAll()

    suspend fun fetchAndSaveSalesStatuses() {
        val (accessToken, csrfToken) = getAuthHeaders()
        val response = api.getSalesData(accessToken, csrfToken)
        if (response.isSuccessful && response.body()?.success == true) {
            val entities = response.body()!!.data.map { it.toEntity() }
            salesStatusDao.clearAll()
            salesStatusDao.upsertAll(entities)
        } else {
            throw Exception("Failed: ${response.code()}")
        }
    }

    // Sales summary
    fun getSalesSummary(): Flow<SalesSummaryEntity?> =
        salesSummaryDao.getSummary()

    suspend fun fetchAndSaveSummary() {
        val (accessToken, csrfToken) = getAuthHeaders()
        val response = api.getSalesLeads(accessToken, csrfToken)
        if (response.isSuccessful && response.body()?.success == true) {
            val data = response.body()!!.data
            salesSummaryDao.clear()
            salesSummaryDao.upsert(
                SalesSummaryEntity(
                    totalAssigned = data.totalAssigned,
                    active = data.active,
                    inactive = data.inactive,
                    availableSlots = data.availableSlots
                )
            )
        } else {
            throw Exception("Failed: ${response.code()}")
        }
    }

    suspend fun fetchGarmentCategories(): Result<List<CategoryItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getOrgGarments(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data.categories)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Fetch table data directly from API - no Room save
    suspend fun fetchTableData(): Result<List<LeadTableItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getTableData(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch leads: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Fetch full lead details using getViewOne API
    suspend fun fetchFullLeadDetails(leadId: String): Result<ViewOneLeadData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getViewOne(accessToken, csrfToken, leadId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch lead details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Get Lead ID from API
    suspend fun getLeadId(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getViewOne(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data._id)
            } else {
                Result.failure(Exception("Failed to get lead ID: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLead(request: CreateLeadFormRequest): Result<CreateLeadFormResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.createLead(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    leadDao.upsert(body.toEntity(request))
                }
                Result.success(body)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create lead"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Updated deleteLead with API call
    suspend fun deleteLead(id: String): Result<Unit> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.deleteLead(accessToken, csrfToken, id)

            // Check if response is successful and body indicates success
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    // Also delete from local Room database
                    leadDao.deleteById(id)
                    Result.success(Unit)
                } else {
                    val errorMsg = body?.message ?: "Delete operation failed"
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Failed to delete lead: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLead(id: String, request: CreateLeadFormRequest): Response<UpdateLeadResponse> {
        val (accessToken, csrfToken) = getAuthHeaders()

        fun String.asRequestBody(): RequestBody =
            this.toRequestBody("text/plain".toMediaTypeOrNull())

        // ✅ Filter out empty garment IDs
        val validGarments = request.garments.filter { it.isNotBlank() }

        // ✅ Get notes
        val internalNote = request.notes.find { it.type == "internal" }
        val customerNote = request.notes.find { it.type == "customer" }

        return api.updateLead(
            accessToken = accessToken,
            csrfToken = csrfToken,
            id = id,
            customerType = request.customerType.asRequestBody(),
            enquiryType = request.enquiryType.asRequestBody(),
            estimatedQuantity = request.estimatedQuantity.toString().asRequestBody(),
            budgetMin = request.budgetRange.min.toString().asRequestBody(),
            budgetMax = request.budgetRange.max.toString().asRequestBody(),
            enquiryDate = request.enquiryDate.asRequestBody(),
            requiredDate = request.requiredDate.asRequestBody(),
            status = request.status.asRequestBody(),
            source = request.source.asRequestBody(),
            personName = request.person.name.asRequestBody(),
            personPhone = request.person.phone.asRequestBody(),
            personEmail = request.person.email.asRequestBody(),
            appointmentIsRequired = request.appointment.isRequired.toString().asRequestBody(),
            personGender = request.person.gender.asRequestBody(),
            personDob = request.person.dob.asRequestBody(),
            contactAddress = request.contact.address.asRequestBody(),
            contactArea = request.contact.area.asRequestBody(),
            contactCity = request.contact.city.asRequestBody(),
            contactPreferredContactMethod = request.contact.preferredContactMethod.asRequestBody(),
            // ✅ Pass null for garment categories that don't exist
            garmentCategory0 = validGarments.getOrNull(0)?.asRequestBody(),
            garmentCategory1 = validGarments.getOrNull(1)?.asRequestBody(),
            garmentCategory2 = validGarments.getOrNull(2)?.asRequestBody(),
            garmentCategory3 = validGarments.getOrNull(3)?.asRequestBody(),
            garmentCategory4 = validGarments.getOrNull(4)?.asRequestBody(),
            noteMessage = (internalNote?.message ?: "-").asRequestBody(),
            noteType = (internalNote?.type ?: "internal").asRequestBody(),
            noteMessage1 = customerNote?.message?.asRequestBody(),
            noteType1 = customerNote?.type?.asRequestBody()
        )
    }

    suspend fun getStaff(): Result<List<StaffDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getMembersDropdownFilter(accessToken, csrfToken)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                Result.success(data)
            } else {
                Result.failure(Exception("Failed to fetch staff: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // In SalesRepository.kt



    // In SalesRepository.kt

    // com/cuso/mobile/repository/SalesRepository.kt

// ──── ORG GARMENT CATEGORIES ────

    // com/cuso/mobile/repository/SalesRepository.kt

// ──── ORG GARMENT CATEGORIES ────

    suspend fun fetchOrgGarmentCategories(): Result<List<OrgGarmentCategory>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getOrgGarmentCommonCategories(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                val categories = response.body()?.data ?: emptyList()
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrgGarmentCategory(categoryId: String): Result<AddOrgGarmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = AddGarmentRequest(categoryId)
            val response = api.addOrgGarmentCategory(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeOrgGarmentCategory(categoryId: String): Result<RemoveOrgGarmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.removeOrgGarmentCategory(accessToken, csrfToken, categoryId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to remove category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLeads(): Flow<List<LeadEntity>> =
        leadDao.getAll()
}