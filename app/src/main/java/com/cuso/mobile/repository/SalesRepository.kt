package com.cuso.mobile.repository

import AddGarmentRequest
import AddOrgGarmentResponse
import CreateBranchRequest
import CreateBranchResponse
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
import com.cuso.mobile.model.AssignStageResponse
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.BranchListResponse
import com.cuso.mobile.model.CategoryItem
import com.cuso.mobile.model.CreateLeadFormRequest
import com.cuso.mobile.model.CreateLeadFormResponse
import com.cuso.mobile.model.CreateOrderRequest
import com.cuso.mobile.model.CreateQuotationRequest
import com.cuso.mobile.model.CreateQuotationResponse
import com.cuso.mobile.model.CustomerListResponse
import com.cuso.mobile.model.CustomerSearchResponse
import com.cuso.mobile.model.CustomerViewData
import com.cuso.mobile.model.DepartmentCreateRequest
import com.cuso.mobile.model.DepartmentCreateResponse
import com.cuso.mobile.model.DepartmentResponse
import com.cuso.mobile.model.DepartmentUpdateRequest
import com.cuso.mobile.model.DepartmentUpdateResponse
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

import com.cuso.mobile.model.DesignationItem
import com.cuso.mobile.model.DesignationCreateRequest
import com.cuso.mobile.model.DesignationCreateResponse
import com.cuso.mobile.model.DesignationUpdateRequest
import com.cuso.mobile.model.DesignationUpdateResponse
import com.cuso.mobile.model.DesignationDeleteResponse
import com.cuso.mobile.model.GarmentPricingDetailDto
import com.cuso.mobile.model.GarmentPricingItem
import com.cuso.mobile.model.GarmentPricingListItemDto
import com.cuso.mobile.model.GarmentStageDoc
import com.cuso.mobile.model.MeasurementsResponse
import com.cuso.mobile.model.OrderItem
import com.cuso.mobile.model.OrderManagementResponse
import com.cuso.mobile.model.OrderOverviewData
import com.cuso.mobile.model.OrderResponse
import com.cuso.mobile.model.OrderViewData
import com.cuso.mobile.model.PricingQuotationSaveRequest
import com.cuso.mobile.model.PricingQuotationSaveResponse
import com.cuso.mobile.model.QuotationListResponse
import com.cuso.mobile.model.StageAssignRequest
import com.cuso.mobile.model.UpdateCustomerRequest
import com.cuso.mobile.model.UpdateOrganizationRequest
import com.cuso.mobile.model.UpdateOrganizationResponse
import com.cuso.mobile.model.UpdateStageRequest
import com.cuso.mobile.model.toOrderItem

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

    suspend fun createOrder(
        request: CreateOrderRequest,
        imageParts: List<okhttp3.MultipartBody.Part> = emptyList(),
        voiceNotePart: okhttp3.MultipartBody.Part? = null
    ): Result<OrderItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val gson = com.google.gson.Gson()

            fun String.asTextBody(): RequestBody =
                this.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.createOrder(
                token = accessToken,
                csrfToken = csrfToken,
                customer = gson.toJson(request.customer).asTextBody(),
                branch = request.branch.asTextBody(),
                wearerType = request.wearerType?.asTextBody(),
                source = request.source?.asTextBody(),
                orderType = request.orderType?.asTextBody(),
                garments = gson.toJson(request.garments).asTextBody(),
                paymentDetails = gson.toJson(request.paymentDetails).asTextBody(),
                orderDate = request.orderDate.asTextBody(),
                trialDate = request.trialDate?.asTextBody(),
                deliveryDate = request.deliveryDate?.asTextBody(),
                totalAmount = request.totalAmount.toString().asTextBody(),
                status = request.status?.asTextBody(),
                designImages = imageParts,
                voiceNote = voiceNotePart
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val apiResponse = response.body()?.data
                    ?: return Result.failure(Exception("Order data is null"))
                val orderItem = apiResponse.toOrderItem()
                Result.success(orderItem)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to create order"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateOrder(
        orderId: String,
        request: CreateOrderRequest,
        existingImages: List<String> = emptyList(),
        imageParts: List<okhttp3.MultipartBody.Part> = emptyList(),
        voiceNotePart: okhttp3.MultipartBody.Part? = null
    ): Result<OrderItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val gson = com.google.gson.Gson()

            fun String.asTextBody(): RequestBody =
                this.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.updateOrder(
                token = accessToken,
                csrfToken = csrfToken,
                orderId = orderId,
                customer = gson.toJson(request.customer).asTextBody(),
                branch = request.branch.asTextBody(),
                wearerType = request.wearerType?.asTextBody(),
                source = request.source?.asTextBody(),
                orderType = request.orderType?.asTextBody(),
                garments = gson.toJson(request.garments).asTextBody(),
                paymentDetails = gson.toJson(request.paymentDetails).asTextBody(),
                orderDate = request.orderDate.asTextBody(),
                trialDate = request.trialDate?.asTextBody(),
                deliveryDate = request.deliveryDate?.asTextBody(),
                totalAmount = request.totalAmount.toString().asTextBody(),
                existingImages = gson.toJson(existingImages).asTextBody(),
                designImages = imageParts,
                voiceNote = voiceNotePart
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val apiResponse = response.body()?.data
                    ?: return Result.failure(Exception("Order data is null"))
                Result.success(apiResponse.toOrderItem())
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.message()
                    ?: "Failed to update order"
                Result.failure(Exception(errorMsg))
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

    suspend fun searchCustomerByMobile(mobile: String): Result<CustomerSearchResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.searchCustomerByMobile(accessToken, csrfToken, mobile)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search customer: ${response.code()}"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Org Garment Categories ────────────────────────────────────

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

    suspend fun fetchActiveOrgGarmentIds(): Result<List<String>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getActiveOrgGarments(accessToken, csrfToken)

            if (response.isSuccessful && response.body()?.success == true) {
                val activeIds = response.body()!!
                    .data.categories
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

    suspend fun createBranch(request: CreateBranchRequest): Result<CreateBranchResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.createBranch(accessToken, csrfToken, request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create branch"))
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

    // ── Departments ──────────────────────────────────────────────

    suspend fun getDepartments(): Result<DepartmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getDepartments(accessToken, csrfToken)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception("Failed to load departments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDepartment(request: DepartmentCreateRequest): Result<DepartmentCreateResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.createDepartment(accessToken, csrfToken, request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create department"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDepartment(
        id: String,
        request: DepartmentUpdateRequest
    ): Result<DepartmentUpdateResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.updateDepartment(accessToken, csrfToken, id, request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update department"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Designations ──────────────────────────────────────────────

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

    // ── Organization ──────────────────────────────────────────────────

    /**
     * PUT /api/organizations/update-one
     * Update organization details (organizationPicture, if present in request, is sent as a Base64 string)
     */
    suspend fun updateOrganization(
        token: String,
        request: UpdateOrganizationRequest
    ): Result<UpdateOrganizationResponse> {
        return try {
            val (_, csrfToken) = getAuthHeaders()
            val response = api.updateOrganization(
                token = token,
                csrfToken = csrfToken,
                request = request
            )
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update organization"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sales/Orders API Methods ─────────────────────────────────

    suspend fun getOrders(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<OrderResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getOrders(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                status = status
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getOrderManagement(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<OrderManagementResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getOrderManagement(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                status = status
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch order management data: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getOrdersView(orderId: String): Result<OrderViewData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getOrdersView(accessToken, csrfToken, orderId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Order data is null"))
                Result.success(data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch order view: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getOrderById(orderId: String): Result<OrderItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getOrderById(
                token = accessToken,
                csrfToken = csrfToken,
                orderId = orderId
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val apiResponse = response.body()?.data
                    ?: return Result.failure(Exception("Order not found"))
                Result.success(apiResponse.toOrderItem())
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: String
    ): Result<OrderItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.updateOrderStatus(
                token = accessToken,
                csrfToken = csrfToken,
                orderId = orderId,
                status = status
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val apiResponse = response.body()?.data
                    ?: return Result.failure(Exception("Failed to update status"))
                Result.success(apiResponse.toOrderItem())
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Customer API Methods ──────────────────────────────────────────

    suspend fun getCustomers(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        type: String? = null
    ): Result<CustomerListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getCustomers(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                type = type
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch customers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Measurements API ──

    suspend fun getMeasurements(): Result<MeasurementsResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getMeasurements(
                token = accessToken,
                csrfToken = csrfToken
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch measurements: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Customer View / Update ──────────────────────────────

    suspend fun getCustomerView(id: String): Result<CustomerViewData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getCustomerView(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch customer: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomer(id: String, request: UpdateCustomerRequest): Result<CustomerViewData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.updateCustomer(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update customer: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteCustomer(id: String): Result<Boolean> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.deleteCustomer(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to delete customer: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sales Order Overview ──────────────────────────────────────

    suspend fun getSalesOverview(orderId: String): Result<OrderOverviewData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getSalesOverview(accessToken, csrfToken, orderId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch order overview: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignCutting(
        orderId: String,
        garmentItemId: String,
        staffId: String,
        quantity: Int
    ): Result<AssignStageResponse> = safeAssignCall {
        val (token, csrfToken) = getAuthHeaders()
        api.assignCutting(token, csrfToken, orderId, garmentItemId, StageAssignRequest(listOf(staffId), quantity))
    }

    suspend fun assignStitching(
        orderId: String,
        garmentItemId: String,
        staffId: String,
        quantity: Int
    ): Result<AssignStageResponse> = safeAssignCall {
        val (token, csrfToken) = getAuthHeaders()
        api.assignStitching(token, csrfToken, orderId, garmentItemId, StageAssignRequest(listOf(staffId), quantity))
    }

    suspend fun assignQc(
        orderId: String,
        garmentItemId: String,
        staffId: String,
        quantity: Int
    ): Result<AssignStageResponse> = safeAssignCall {
        val (token, csrfToken) = getAuthHeaders()
        api.assignQc(token, csrfToken, orderId, garmentItemId, StageAssignRequest(listOf(staffId), quantity))
    }

    private suspend fun safeAssignCall(call: suspend () -> Response<AssignStageResponse>): Result<AssignStageResponse> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Assign failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStage(
        orderId: String,
        garmentItemId: String,
        stageName: String,
        status: String
    ): Result<GarmentStageDoc> {
        return try {
            val (token, csrfToken) = getAuthHeaders()
            val response = api.updateStage(
                token = token,
                csrfToken = csrfToken,
                orderId = orderId,
                garmentItemId = garmentItemId,
                stageName = stageName.trim().lowercase(),
                request = UpdateStageRequest(status = status)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Stage data is null"))
                Result.success(data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update stage: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePricingQuotation(
        request: PricingQuotationSaveRequest
    ): Result<PricingQuotationSaveResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.savePricingQuotation(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to save pricing: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════
// FILE: repository/SalesRepository.kt (ADD these 2 functions to existing repository)
// ══════════════════════════════════════════

    // ── Dashboard: fetch all garment pricing cards ──
    // ── Dashboard: fetch all garment pricing cards ──
    suspend fun getGarmentPricingList(): Result<List<GarmentPricingListItemDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getGarmentPricingList(accessToken, csrfToken)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch pricing list"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Edit screen: fetch single record detail to prefill fields ──
    suspend fun getGarmentPricingDetail(id: String): Result<GarmentPricingDetailDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getGarmentPricingDetail(accessToken, csrfToken, id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch pricing detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePricingQuotation(
        id: String,
        request: PricingQuotationSaveRequest
    ): Result<PricingQuotationSaveResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.updatePricingQuotation(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = request
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to update pricing: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Quotation List (Sales → Pricing & Quotations screen) ──

    suspend fun getQuotations(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<QuotationListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getQuotations(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                status = status
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch quotations: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Fetch all garment pricing details
     * GET /api/garment-pricing/view-all
     */
    suspend fun getGarmentPricing(): Result<List<GarmentPricingItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getGarmentPricing(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch garment pricing: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createQuotation(
        request: CreateQuotationRequest
    ): Result<CreateQuotationResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.createQuotation(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to save quotation: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getLeads(): Flow<List<LeadEntity>> = leadDao.getAll()
}