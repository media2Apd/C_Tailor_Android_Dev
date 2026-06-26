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
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.BranchListResponse
import com.cuso.mobile.model.CategoryItem
import com.cuso.mobile.model.CreateLeadFormRequest
import com.cuso.mobile.model.CreateLeadFormResponse
import com.cuso.mobile.model.DepartmentResponse
import com.cuso.mobile.model.LeadTableItem
import com.cuso.mobile.model.StaffDto
import com.cuso.mobile.model.UpdateBranchRequest
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

// ✅ Import designation models
import com.cuso.mobile.model.DesignationItem
import com.cuso.mobile.model.DesignationCreateRequest
import com.cuso.mobile.model.DesignationCreateResponse
import com.cuso.mobile.model.DesignationUpdateRequest
import com.cuso.mobile.model.DesignationUpdateResponse
import com.cuso.mobile.model.DesignationDeleteResponse

@Singleton
@Suppress("unused")
class SalesRepository @Inject constructor(
    private val api: ApiService,
    private val salesStatusDao: SalesStatusDao,
    private val salesSummaryDao: SalesSummaryDao,
    private val tokensDao: TokensDao,
    private val leadDao: LeadDao
) {

    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // ── Sales Statuses ────────────────────────────────────────────

    fun getSalesStatuses(): Flow<List<SalesStatusEntity>> = salesStatusDao.getAll()

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

    // ── Sales Summary ─────────────────────────────────────────────

    fun getSalesSummary(): Flow<SalesSummaryEntity?> = salesSummaryDao.getSummary()

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

    // ── Garment Categories (old - used elsewhere) ─────────────────

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

    // ── Table Leads ───────────────────────────────────────────────

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

    // ── Lead Details ──────────────────────────────────────────────

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

    // ── Lead CRUD ─────────────────────────────────────────────────

    suspend fun createLead(request: CreateLeadFormRequest): Result<CreateLeadFormResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.createLead(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) leadDao.upsert(body.toEntity(request))
                Result.success(body)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create lead"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLead(id: String): Result<Unit> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.deleteLead(accessToken, csrfToken, id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    leadDao.deleteById(id)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(body?.message ?: "Delete operation failed"))
                }
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to delete: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLead(id: String, request: CreateLeadFormRequest): Response<UpdateLeadResponse> {
        val (accessToken, csrfToken) = getAuthHeaders()
        fun String.asRequestBody(): RequestBody =
            this.toRequestBody("text/plain".toMediaTypeOrNull())

        val validGarments = request.garments.filter { it.isNotBlank() }
        val internalNote  = request.notes.find { it.type == "internal" }
        val customerNote  = request.notes.find { it.type == "customer" }

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
            garmentCategory0 = validGarments.getOrNull(0)?.asRequestBody(),
            garmentCategory1 = validGarments.getOrNull(1)?.asRequestBody(),
            garmentCategory2 = validGarments.getOrNull(2)?.asRequestBody(),
            garmentCategory3 = validGarments.getOrNull(3)?.asRequestBody(),
            garmentCategory4 = validGarments.getOrNull(4)?.asRequestBody(),
            noteMessage  = (internalNote?.message ?: "-").asRequestBody(),
            noteType     = (internalNote?.type ?: "internal").asRequestBody(),
            noteMessage1 = customerNote?.message?.asRequestBody(),
            noteType1    = customerNote?.type?.asRequestBody()
        )
    }

    // ── Staff ─────────────────────────────────────────────────────

    suspend fun getStaff(): Result<List<StaffDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getMembersDropdownFilter(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch staff: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Org Garment Categories ────────────────────────────────────

    /**
     * GET /api/common/categories/view-all
     * React: SummaryApi.getCommonCategories
     * Returns all common categories → shown in display grid
     */
    suspend fun fetchOrgGarmentCategories(): Result<List<OrgGarmentCategory>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getOrgGarmentCommonCategories(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * GET /api/org-garments/view-all  (OrgGarmentResponse return type)
     * React: SummaryApi.getAllCategories
     *   → res.data.data.categories.filter(c => c.isActive).map(c => c.categoryId._id)
     *
     * Each OrgGarmentCategory has:
     *   isActive   : Boolean
     *   categoryId : OrgCategoryDetail?  ← nested object
     *     └── _id  : String              ← this matches common category _id
     *
     * We return only the active ones' categoryId._id
     * These IDs are used to highlight matching tiles in the grid
     */
    suspend fun fetchActiveOrgGarmentIds(): Result<List<String>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            // ✅ getActiveOrgGarments → OrgGarmentResponse (data: List<OrgGarmentCategory>)
            val response = api.getActiveOrgGarments(accessToken, csrfToken)

            if (response.isSuccessful && response.body()?.success == true) {
                val activeIds = response.body()!!
                    .data.categories          // ← data.categories எடுக்கிறோம்
                    .filter { it.isActive }
                    .mapNotNull { it.categoryId?._id }

                Result.success(activeIds)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Branches ──────────────────────────────────────────────────

    suspend fun getBranches(): Result<BranchListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getBranches(accessToken, csrfToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * PUT /api/branches/update-one/{id}
     * id must be the branch's real Mongo _id (BranchItem._id), not the display branchId string.
     */
    suspend fun updateBranch(id: String, request: UpdateBranchRequest): Result<BranchItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.updateBranch(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update branch: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * POST /api/org-garments/add
     * React: SummaryApi.addCategories → data: { categoryIds: added }
     */
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

    /**
     * DELETE /api/org-garments/remove/{categoryId}
     * React: SummaryApi.removeOneCategory → removed.map(id => api({ url: url(id) }))
     */
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

    suspend fun getDepartments(): Result<DepartmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getDepartments(accessToken, csrfToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Designations ──────────────────────────────────────────────

    /**
     * GET /api/designations/view-all
     * Fetch all designations
     */
    suspend fun getDesignations(): Result<List<DesignationItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getDesignations(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to load designations: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * POST /api/designations/create
     * Create a new designation
     */
    suspend fun createDesignation(request: DesignationCreateRequest): Result<DesignationCreateResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.createDesignation(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create designation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ ADD THIS - Update Designation
    /**
     * PUT /api/designations/update-one/{id}
     * Update an existing designation
     */
    suspend fun updateDesignation(
        id: String,
        request: DesignationUpdateRequest
    ): Result<DesignationUpdateResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.updateDesignation(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update designation: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ ADD THIS - Delete Designation
    /**
     * DELETE /api/designations/delete-one/{id}
     * Soft delete a designation
     */
    suspend fun deleteDesignation(id: String): Result<DesignationDeleteResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.deleteDesignation(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to delete designation: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Leads (Room) ──────────────────────────────────────────────

    fun getLeads(): Flow<List<LeadEntity>> = leadDao.getAll()
}