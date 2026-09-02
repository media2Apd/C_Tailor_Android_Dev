package com.cuso.mobile.repository

import com.cuso.mobile.database.dao.LeadDao
import com.cuso.mobile.database.dao.OrganizationDao
import com.cuso.mobile.database.dao.SalesStatusDao
import com.cuso.mobile.database.dao.SalesSummaryDao
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.database.entities.LeadEntity
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.database.entities.SalesSummaryEntity
import com.cuso.mobile.database.entities.toEntity
import com.cuso.mobile.model.login_forgotPassword_resetPassword.AddGarmentRequest
import com.cuso.mobile.model.login_forgotPassword_resetPassword.AddOrgGarmentResponse
import com.cuso.mobile.model.settings.BranchItem
import com.cuso.mobile.model.settings.BranchListResponse
import com.cuso.mobile.model.settings.CreateBranchRequest
import com.cuso.mobile.model.settings.CreateBranchResponse
import com.cuso.mobile.model.settings.DepartmentCreateRequest
import com.cuso.mobile.model.settings.DepartmentCreateResponse
import com.cuso.mobile.model.settings.DepartmentResponse
import com.cuso.mobile.model.settings.DepartmentUpdateRequest
import com.cuso.mobile.model.settings.DepartmentUpdateResponse
import com.cuso.mobile.model.settings.DesignationCreateRequest
import com.cuso.mobile.model.settings.DesignationCreateResponse
import com.cuso.mobile.model.settings.DesignationDeleteResponse
import com.cuso.mobile.model.settings.DesignationItem
import com.cuso.mobile.model.settings.DesignationUpdateRequest
import com.cuso.mobile.model.settings.DesignationUpdateResponse
import com.cuso.mobile.model.login_forgotPassword_resetPassword.OrgGarmentCategory
import com.cuso.mobile.model.login_forgotPassword_resetPassword.RemoveOrgGarmentResponse
import com.cuso.mobile.model.settings.UpdateBranchRequest
import com.cuso.mobile.model.settings.UpdateOrganizationRequest
import com.cuso.mobile.model.settings.UpdateOrganizationResponse
import com.cuso.mobile.model.settings.UploadOrganizationPictureResponse
import com.cuso.mobile.model.sales.AppointmentRequest
import com.cuso.mobile.model.sales.AssignStageResponse
import com.cuso.mobile.model.sales.BudgetRangeRequest
import com.cuso.mobile.model.sales.CategoryItem
import com.cuso.mobile.model.sales.ContactRequest
import com.cuso.mobile.model.sales.ConvertToInvoiceData
import com.cuso.mobile.model.sales.ConvertToInvoiceRequest
import com.cuso.mobile.model.sales.ConvertToOrderData
import com.cuso.mobile.model.sales.CreateLeadFormRequest
import com.cuso.mobile.model.sales.CreateLeadFormResponse
import com.cuso.mobile.model.sales.CreateOrderRequest
import com.cuso.mobile.model.sales.CreateQuotationRequest
import com.cuso.mobile.model.sales.CreateQuotationResponse
import com.cuso.mobile.model.sales.CustomerDetailV2
import com.cuso.mobile.model.sales.CustomerListResponse
import com.cuso.mobile.model.sales.CustomerListResponseV2
import com.cuso.mobile.model.sales.CustomerSearchResponse
import com.cuso.mobile.model.sales.CustomerViewData
import com.cuso.mobile.model.sales.GarmentPricingDetailDto
import com.cuso.mobile.model.sales.GarmentPricingItem
import com.cuso.mobile.model.sales.GarmentPricingListItemDto
import com.cuso.mobile.model.sales.GarmentStageDoc
import com.cuso.mobile.model.sales.LeadTableItem
import com.cuso.mobile.model.sales.MeasurementsResponse
import com.cuso.mobile.model.sales.NoteRequest
import com.cuso.mobile.model.sales.OrderItem
import com.cuso.mobile.model.sales.OrderManagementResponse
import com.cuso.mobile.model.sales.OrderOverviewData
import com.cuso.mobile.model.sales.OrderResponse
import com.cuso.mobile.model.sales.OrderViewData
import com.cuso.mobile.model.sales.PersonRequest
import com.cuso.mobile.model.sales.PricingQuotationSaveRequest
import com.cuso.mobile.model.sales.PricingQuotationSaveResponse
import com.cuso.mobile.model.sales.QuotationItemDto
import com.cuso.mobile.model.sales.QuotationListResponse
import com.cuso.mobile.model.sales.ReceivePaymentData
import com.cuso.mobile.model.sales.ReceivePaymentRequest
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.model.sales.StageAssignRequest
import com.cuso.mobile.model.sales.UpdateCustomerRequest
import com.cuso.mobile.model.sales.UpdateLeadRequest
import com.cuso.mobile.model.sales.UpdateLeadResponse
import com.cuso.mobile.model.sales.UpdateStageRequest
import com.cuso.mobile.model.sales.ViewOneLeadData
import com.cuso.mobile.model.sales.toEntity
import com.cuso.mobile.model.sales.toOrderItem
import com.cuso.mobile.network.hr.HrApiService
import com.cuso.mobile.network.organization.OrganizationApiService
import com.cuso.mobile.network.sales.SalesCustomerApiService
import com.cuso.mobile.network.sales.SalesLeadApiService
import com.cuso.mobile.network.sales.SalesMeasurementsApiService
import com.cuso.mobile.network.sales.SalesOrderApiService
import com.cuso.mobile.network.sales.SalesPricingApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("unused")
class SalesRepository @Inject constructor(
    private val salesLeadApi: SalesLeadApiService,
    private val salesOrderApi: SalesOrderApiService,
    private val salesCustomerApi: SalesCustomerApiService,
    private val salesPricingApi: SalesPricingApiService,
    private val salesMeasurementsApi: SalesMeasurementsApiService,
    private val organizationApi: OrganizationApiService,
    private val hrApi: HrApiService,
    private val salesStatusDao: SalesStatusDao,
    private val salesSummaryDao: SalesSummaryDao,
    private val tokensDao: TokensDao,
    private val leadDao: LeadDao,
    private val organizationDao: OrganizationDao
) {

    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // -------------------------------------------------------------
    // Sales Statuses (Local & Remote)
    // -------------------------------------------------------------

    fun getSalesStatuses(): Flow<List<SalesStatusEntity>> = salesStatusDao.getAll()

    suspend fun fetchAndSaveSalesStatuses() {
        val (accessToken, csrfToken) = getAuthHeaders()
        val response = salesLeadApi.getSalesData(accessToken, csrfToken)
        if (response.isSuccessful && response.body()?.success == true) {
            val entities = response.body()!!.data.map { it.toEntity() }
            salesStatusDao.clearAll()
            salesStatusDao.upsertAll(entities)
        } else {
            throw Exception("Failed: ${response.code()}")
        }
    }

    // -------------------------------------------------------------
    // Sales Summary (Local & Remote)
    // -------------------------------------------------------------

    fun getSalesSummary(): Flow<SalesSummaryEntity?> = salesSummaryDao.getSummary()

    suspend fun fetchAndSaveSummary() {
        val (accessToken, csrfToken) = getAuthHeaders()
        val response = salesLeadApi.getSalesLeads(accessToken, csrfToken)
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

    // -------------------------------------------------------------
    // Garment Categories
    // -------------------------------------------------------------

    suspend fun fetchGarmentCategories(): Result<List<CategoryItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = organizationApi.getOrgGarments(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data.categories)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Table Leads
    // -------------------------------------------------------------

    data class TableLeadsResult(val leads: List<LeadTableItem>, val total: Int)

    suspend fun fetchTableData(page: Int = 1, limit: Int = 10): Result<TableLeadsResult> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesLeadApi.getTableData(accessToken, csrfToken, page, limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Result.success(TableLeadsResult(leads = body.data, total = body.total))
            } else {
                Result.failure(Exception("Failed to fetch leads: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Lead Details & CRUD Operations
    // -------------------------------------------------------------

    suspend fun fetchFullLeadDetails(leadId: String): Result<ViewOneLeadData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesLeadApi.getViewOne(accessToken, csrfToken, leadId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch lead details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLead(request: CreateLeadFormRequest): Result<CreateLeadFormResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesLeadApi.createLead(accessToken, csrfToken, request)
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

    suspend fun updateLead(id: String, request: CreateLeadFormRequest): Response<UpdateLeadResponse> {
        val (accessToken, csrfToken) = getAuthHeaders()

        val validGarments: List<String> = request.garments
            ?.mapNotNull { garment ->
                when (garment) {
                    is String -> garment.takeIf { it.isNotBlank() }
                    is Map<*, *> -> (garment["_id"] as? String)?.takeIf { it.isNotBlank() }
                    else -> null
                }
            }
            ?: emptyList()

        val updateRequest = UpdateLeadRequest(
            customerType = request.customerType,
            enquiryType = request.enquiryType,
            estimatedQuantity = request.estimatedQuantity,
            budgetRange = BudgetRangeRequest(
                min = request.budgetRange.min,
                max = request.budgetRange.max
            ),
            enquiryDate = request.enquiryDate,
            requiredDate = request.requiredDate,
            status = request.status,
            source = request.source,
            person = PersonRequest(
                name = request.person.name,
                phone = request.person.phone,
                email = request.person.email,
                gender = request.person.gender,
                dob = request.person.dob
            ),
            appointment = AppointmentRequest(
                isRequired = request.appointment.isRequired,
                date = request.appointment.date,
                time = request.appointment.time,
                assignedStaff = request.appointment.assignedStaff,
                priority = request.appointment.priority,
                followUpDate = request.appointment.followUpDate
            ),
            notes = request.notes.map { NoteRequest(message = it.message, type = it.type) },
            contact = ContactRequest(
                address = request.contact.address,
                area = request.contact.area,
                city = request.contact.city,
                preferredContactMethod = request.contact.preferredContactMethod
            ),
            garmentCategory = validGarments
        )

        return salesLeadApi.updateLead(
            accessToken = accessToken,
            csrfToken = csrfToken,
            id = id,
            request = updateRequest
        )
    }

    suspend fun deleteLead(id: String): Result<Unit> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesLeadApi.deleteLead(accessToken, csrfToken, id)
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

    suspend fun convertLeadToOrder(leadId: String): Result<ConvertToOrderData> {
        return try {
            val (authHeader, csrfToken) = getAuthHeaders()
            val response = salesLeadApi.convertedToOrder(authHeader, csrfToken, leadId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.data?.message ?: "Conversion failed"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLeads(): Flow<List<LeadEntity>> = leadDao.getAll()

    // -------------------------------------------------------------
    // Sales Orders Operations
    // -------------------------------------------------------------

    suspend fun getOrders(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<OrderResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesOrderApi.getOrders(
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

    suspend fun getOrderById(orderId: String): Result<OrderItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesOrderApi.getOrderById(
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
            val response = salesOrderApi.updateOrderStatus(
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

    suspend fun createOrder(
        request: CreateOrderRequest,
        imageParts: List<okhttp3.MultipartBody.Part> = emptyList(),
        voiceNotePart: okhttp3.MultipartBody.Part? = null
    ): Result<OrderItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val gson = Gson()

            fun String.asTextBody(): RequestBody =
                this.toRequestBody("text/plain".toMediaTypeOrNull())
            val leadIdBody = request.leadId?.asTextBody()

            val response = salesOrderApi.createOrder(
                token = accessToken,
                csrfToken = csrfToken,
                leadId = leadIdBody,
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
            val gson = Gson()

            fun String.asTextBody(): RequestBody =
                this.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = salesOrderApi.updateOrder(
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

    suspend fun getSalesOverview(orderId: String): Result<OrderOverviewData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesOrderApi.getSalesOverview(accessToken, csrfToken, orderId)
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

    suspend fun getOrdersView(orderId: String): Result<OrderViewData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesOrderApi.getOrdersView(accessToken, csrfToken, orderId)
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

    suspend fun getOrderManagement(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<OrderManagementResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesOrderApi.getOrderManagement(
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

    suspend fun receivePayment(
        orderId: String,
        amount: Double,
        method: String,
        transactionId: String = "",
        notes: String = "",
        paymentDate: String? = null,
        paymentType: String = "full"
    ): Result<ReceivePaymentData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val response = salesOrderApi.receivePayment(
                token = accessToken,
                csrfToken = csrfToken,
                orderId = orderId,
                request = ReceivePaymentRequest(
                    amount = amount,
                    method = method,
                    transactionId = transactionId,
                    notes = notes,
                    paymentDate = paymentDate,
                    paymentType = paymentType
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Payment failed"))
                }
            } else {
                Result.failure(Exception("Payment failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun convertToInvoice(salesOrderId: String, dueDate: String? = null): Result<ConvertToInvoiceData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = ConvertToInvoiceRequest(salesOrderId = salesOrderId, dueDate = dueDate)
            val response = salesOrderApi.convertToInvoice(accessToken, csrfToken, salesOrderId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Invoice data is null"))
                Result.success(data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to convert to invoice: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Production Stage Assignments & Tracking
    // -------------------------------------------------------------

    suspend fun assignCutting(
        orderId: String,
        garmentItemId: String,
        staffId: String,
        quantity: Int
    ): Result<AssignStageResponse> = safeAssignCall {
        val (token, csrfToken) = getAuthHeaders()
        salesOrderApi.assignCutting(token, csrfToken, orderId, garmentItemId, StageAssignRequest(listOf(staffId), quantity))
    }

    suspend fun assignStitching(
        orderId: String,
        garmentItemId: String,
        staffId: String,
        quantity: Int
    ): Result<AssignStageResponse> = safeAssignCall {
        val (token, csrfToken) = getAuthHeaders()
        salesOrderApi.assignStitching(token, csrfToken, orderId, garmentItemId, StageAssignRequest(listOf(staffId), quantity))
    }

    suspend fun assignQc(
        orderId: String,
        garmentItemId: String,
        staffId: String,
        quantity: Int
    ): Result<AssignStageResponse> = safeAssignCall {
        val (token, csrfToken) = getAuthHeaders()
        salesOrderApi.assignQc(token, csrfToken, orderId, garmentItemId, StageAssignRequest(listOf(staffId), quantity))
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
            val response = salesOrderApi.updateStage(
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

    // -------------------------------------------------------------
    // Customer API Operations
    // -------------------------------------------------------------

    suspend fun getCustomers(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        type: String? = null
    ): Result<CustomerListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesCustomerApi.getCustomers(
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

    suspend fun getCustomersV2(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        type: String? = null
    ): Result<CustomerListResponseV2> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesCustomerApi.getCustomersV2(
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
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch customers: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomerDetailV2(id: String): Result<CustomerDetailV2> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesCustomerApi.getCustomerDetailV2(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch customer details: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomerView(id: String): Result<CustomerViewData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesCustomerApi.getCustomerView(accessToken, csrfToken, id)
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
            val response = salesCustomerApi.updateCustomer(accessToken, csrfToken, id, request)
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

    suspend fun deleteCustomer(id: String): Result<String?> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesCustomerApi.deleteCustomer(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to delete customer: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCustomerByMobile(mobile: String): Result<CustomerSearchResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesCustomerApi.searchCustomerByMobile(accessToken, csrfToken, mobile)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search customer: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Measurements Operations
    // -------------------------------------------------------------

    suspend fun getMeasurements(
        page: Int = 1,
        limit: Int = 10
    ): Result<MeasurementsResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesMeasurementsApi.getMeasurements(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit
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

    // -------------------------------------------------------------
    // Garment Pricing & Quotations Operations
    // -------------------------------------------------------------

    suspend fun savePricingQuotation(
        request: PricingQuotationSaveRequest
    ): Result<PricingQuotationSaveResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesPricingApi.savePricingQuotation(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to save pricing: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGarmentPricingList(): Result<List<GarmentPricingListItemDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesPricingApi.getGarmentPricingList(accessToken, csrfToken)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch pricing list"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGarmentPricingDetail(id: String): Result<GarmentPricingDetailDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesPricingApi.getGarmentPricingDetail(accessToken, csrfToken, id)
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
            val response = salesPricingApi.updatePricingQuotation(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = request
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update pricing: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuotations(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<QuotationListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesPricingApi.getQuotations(
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

    suspend fun getGarmentPricing(): Result<List<GarmentPricingItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesPricingApi.getGarmentPricing(accessToken, csrfToken)
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
            val response = salesPricingApi.createQuotation(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to save quotation: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteQuotation(id: String): Result<Boolean> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesPricingApi.deleteQuotation(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to delete quotation: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuotationById(id: String): Result<QuotationItemDto> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = salesPricingApi.getQuotationById(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Quotation data is null"))
                Result.success(data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch quotation: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Staff & HR Operations
    // -------------------------------------------------------------

    suspend fun getStaff(): Result<List<StaffDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getMembersDropdownFilter(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch staff: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Organization & Branch Operations
    // -------------------------------------------------------------

    suspend fun fetchOrgGarmentCategories(): Result<List<OrgGarmentCategory>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = organizationApi.getOrgGarmentCommonCategories(accessToken, csrfToken)
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
            val response = organizationApi.getActiveOrgGarments(accessToken, csrfToken)
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

    suspend fun addOrgGarmentCategory(categoryId: String): Result<AddOrgGarmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = AddGarmentRequest(categoryId)
            val response = organizationApi.addOrgGarmentCategory(accessToken, csrfToken, request)
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
            val response = organizationApi.removeOrgGarmentCategory(accessToken, csrfToken, categoryId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to remove category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBranches(): Result<BranchListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = organizationApi.getBranches(accessToken, csrfToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBranch(id: String, request: UpdateBranchRequest): Result<Pair<BranchItem, String?>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = organizationApi.updateBranch(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Result.success(body.data to null)
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
            val response = organizationApi.createBranch(accessToken, csrfToken, request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create branch"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDepartments(): Result<DepartmentResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = organizationApi.getDepartments(accessToken, csrfToken)
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
            val response = organizationApi.createDepartment(accessToken, csrfToken, request)
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
            val response = organizationApi.updateDepartment(accessToken, csrfToken, id, request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update department"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDesignations(): Result<List<DesignationItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = organizationApi.getDesignations(accessToken, csrfToken)
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
            val response = organizationApi.createDesignation(accessToken, csrfToken, request)
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
            val response = organizationApi.updateDesignation(accessToken, csrfToken, id, request)
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
            val response = organizationApi.deleteDesignation(accessToken, csrfToken, id)
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

    suspend fun updateOrganization(
        token: String,
        request: UpdateOrganizationRequest
    ): Result<UpdateOrganizationResponse> {
        return try {
            val (_, csrfToken) = getAuthHeaders()
            val response = organizationApi.updateOrganization(
                token = token,
                csrfToken = csrfToken,
                request = request
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update organization: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadOrganizationPicture(
        token: String,
        pictureFile: java.io.File
    ): Result<UploadOrganizationPictureResponse> {
        return try {
            val (_, csrfToken) = getAuthHeaders()

            val requestBody = pictureFile.asRequestBody("image/*".toMediaTypeOrNull())
            val picturePart = okhttp3.MultipartBody.Part.createFormData(
                "picture", pictureFile.name, requestBody
            )

            val response = organizationApi.uploadOrganizationPicture(token, csrfToken, picturePart)

            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                body.data?.let { org ->
                    organizationDao.updateOrganizationPicture(
                        orgId = org._id,
                        pictureUrl = org.organizationPicture,
                        pictureId = org.organizationPictureId
                    )
                }
                Result.success(body)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to upload picture: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}