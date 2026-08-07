
package com.cuso.mobile.network


import com.cuso.mobile.model.ActiveOrgGarmentResponse
import com.cuso.mobile.model.AddGarmentRequest
import com.cuso.mobile.model.AddOrgGarmentResponse
import com.cuso.mobile.model.sales.ApiResponse
import com.cuso.mobile.model.sales.AssignStageResponse
import com.cuso.mobile.model.BranchListResponse
import com.cuso.mobile.model.CreateBranchRequest
import com.cuso.mobile.model.CreateBranchResponse
import com.cuso.mobile.model.sales.CreateLeadFormRequest
import com.cuso.mobile.model.sales.CreateLeadFormResponse
import com.cuso.mobile.model.sales.CreateOrderResponse
import com.cuso.mobile.model.sales.CreateQuotationRequest
import com.cuso.mobile.model.sales.CreateQuotationResponse
import com.cuso.mobile.model.sales.CustomerSearchResponse
import com.cuso.mobile.model.sales.DeleteLeadResponse
import com.cuso.mobile.model.DepartmentCreateRequest
import com.cuso.mobile.model.DepartmentCreateResponse
import com.cuso.mobile.model.DepartmentResponse
import com.cuso.mobile.model.DepartmentUpdateRequest
import com.cuso.mobile.model.DepartmentUpdateResponse
import com.cuso.mobile.model.DesignationCreateRequest
import com.cuso.mobile.model.DesignationCreateResponse
import com.cuso.mobile.model.DesignationDeleteResponse
import com.cuso.mobile.model.DesignationListResponse
import com.cuso.mobile.model.DesignationUpdateRequest
import com.cuso.mobile.model.DesignationUpdateResponse
import com.cuso.mobile.model.EmailResponse
import com.cuso.mobile.model.EmailVerify
import com.cuso.mobile.model.GoogleLoginRequest
import com.cuso.mobile.model.sales.LeadsTableResponse
import com.cuso.mobile.model.sales.OrderDetailResponse
import com.cuso.mobile.model.sales.OrderResponse
import com.cuso.mobile.model.PasswordResponse
import com.cuso.mobile.model.PasswordVerify
import com.cuso.mobile.model.RegisterVerifyOtp
import com.cuso.mobile.model.RegisterVerifyOtpResponse
import com.cuso.mobile.model.sales.SalesResponse
import com.cuso.mobile.model.sales.SalesSummaryResponse
import com.cuso.mobile.model.SignupRequest
import com.cuso.mobile.model.SignupResponse
import com.cuso.mobile.model.sales.StaffResponse
import com.cuso.mobile.model.UpdateBranchRequest
import com.cuso.mobile.model.UpdateBranchResponse
import com.cuso.mobile.model.sales.UpdateLeadResponse
import com.cuso.mobile.model.UpdateOrganizationResponse
import com.cuso.mobile.model.sales.ViewOneLeadResponse
import com.cuso.mobile.model.forgotPasswordRequest
import com.cuso.mobile.model.forgotPasswordResponse
import com.cuso.mobile.model.otpSendRequest
import com.cuso.mobile.model.otpSendResponse
import com.cuso.mobile.model.otpVerifyRequest
import com.cuso.mobile.model.otpVerifyResponse
import com.cuso.mobile.model.forgotPasswordVerifyRequest
import com.cuso.mobile.model.forgotPasswordVerifyResponse
import com.cuso.mobile.model.sales.meResponse
import com.cuso.mobile.model.sales.myLayoutResponse
import com.cuso.mobile.model.sales.myOrganizationResponse
import com.cuso.mobile.model.organizationSetUpRequest
import com.cuso.mobile.model.organizationSetUpResponse
import com.cuso.mobile.model.resetNewPasswordRequest
import com.cuso.mobile.model.resetNewPasswordResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import com.cuso.mobile.model.sales.CustomerListResponse
import com.cuso.mobile.model.sales.CustomerListResponseV2
import com.cuso.mobile.model.DashboardResponse
import com.cuso.mobile.model.OrgGarmentResponse
import com.cuso.mobile.model.RemoveOrgGarmentResponse
import com.cuso.mobile.model.UpdateOrganizationRequest
import com.cuso.mobile.model.UploadOrganizationPictureResponse
import com.cuso.mobile.model.finance.ChartOfAccountsResponse
import com.cuso.mobile.model.finance.CreateChartOfAccountRequest
import com.cuso.mobile.model.finance.CreateChartOfAccountResponse
import com.cuso.mobile.model.finance.CreateExpenseResponse
import com.cuso.mobile.model.finance.CreateJournalEntryRequest
import com.cuso.mobile.model.finance.CreateJournalEntryResponse
import com.cuso.mobile.model.finance.ExpenseListResponse
import com.cuso.mobile.model.finance.ExpenseViewOneResponse
import com.cuso.mobile.model.finance.InvoiceListResponse
import com.cuso.mobile.model.finance.InvoiceViewOneResponse
import com.cuso.mobile.model.finance.JournalEntryDetailResponse
import com.cuso.mobile.model.finance.JournalEntryListResponse
import com.cuso.mobile.model.finance.LedgerResponse
import com.cuso.mobile.model.finance.TrialBalanceResponse
import com.cuso.mobile.model.finance.UpdateJournalEntryRequest
import com.cuso.mobile.model.finance.UpdateJournalEntryResponse
import com.cuso.mobile.model.hr.CreateMemberRequest
import com.cuso.mobile.model.hr.CreateMemberResponse
import com.cuso.mobile.model.hr.DeleteProfilePictureResponse

import com.cuso.mobile.model.hr.MemberDetailResponse
import com.cuso.mobile.model.hr.MemberListResponse
import com.cuso.mobile.model.hr.RoleListResponse
import com.cuso.mobile.model.hr.ShiftListResponse
import com.cuso.mobile.model.hr.UpdateMemberRequest
import com.cuso.mobile.model.hr.UploadProfilePictureResponse
import com.cuso.mobile.model.inventory.AdjustStockRequest
import com.cuso.mobile.model.inventory.InventoryItemDetailResponse
import com.cuso.mobile.model.inventory.InventoryItemListResponse
import com.cuso.mobile.model.inventory.InventoryViewOneResponse
import com.cuso.mobile.model.sales.ConvertLeadToOrderResponse
import com.cuso.mobile.model.sales.ConvertToInvoiceRequest
import com.cuso.mobile.model.sales.ConvertToInvoiceResponse
import com.cuso.mobile.model.sales.DeleteCustomerResponse
import com.cuso.mobile.model.sales.GarmentCategoriesResponse
import com.cuso.mobile.model.sales.GarmentPricingDetailDto
import com.cuso.mobile.model.sales.GarmentPricingListItemDto
import com.cuso.mobile.model.sales.GarmentPricingResponse
import com.cuso.mobile.model.sales.GetCustomerDetailResponseV2
import com.cuso.mobile.model.sales.GetCustomerViewResponse
import com.cuso.mobile.model.sales.GetFinanceCustomerViewOneResponse
import com.cuso.mobile.model.sales.MeasurementsResponse
import com.cuso.mobile.model.sales.OrderManagementResponse
import com.cuso.mobile.model.sales.OrderOverviewApiResponse
import com.cuso.mobile.model.sales.OrderViewResponse
import com.cuso.mobile.model.sales.PricingQuotationSaveRequest
import com.cuso.mobile.model.sales.PricingQuotationSaveResponse
import com.cuso.mobile.model.sales.QuotationDeleteResponse
import com.cuso.mobile.model.sales.QuotationDetailResponse
import com.cuso.mobile.model.sales.QuotationListResponse
import com.cuso.mobile.model.sales.ReceivePaymentRequest
import com.cuso.mobile.model.sales.ReceivePaymentResponse
import com.cuso.mobile.model.sales.StageAssignRequest
import com.cuso.mobile.model.sales.UpdateCustomerRequest
import com.cuso.mobile.model.sales.UpdateCustomerResponse
import com.cuso.mobile.model.sales.UpdateLeadRequest
import com.cuso.mobile.model.sales.UpdateStageRequest
import com.cuso.mobile.model.sales.UpdateStageResponse
import retrofit2.http.PartMap

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────

    @POST("/api/auth/register/send-otp")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    @POST("/api/auth/register/verify-otp")
    suspend fun signupVerifyOtp(
        @Body request: RegisterVerifyOtp
    ): Response<RegisterVerifyOtpResponse>

    @POST("/api/auth/check-email")
    suspend fun verifyEmail(
        @Body request: EmailVerify
    ): Response<EmailResponse>

    @POST("/api/auth/login")
    suspend fun verifyPassword(
        @Body request: PasswordVerify
    ): Response<PasswordResponse>

    @POST("/api/auth/login/send-otp")
    suspend fun otpSend(
        @Body request: otpSendRequest
    ): Response<otpSendResponse>

    @POST("/api/auth/login/verify-otp")
    suspend fun verifyOtp(
        @Body request: otpVerifyRequest
    ): Response<otpVerifyResponse>

    @POST("/api/forgot-password/send-otp")
    suspend fun forgotPassword(
        @Body request: forgotPasswordRequest
    ): Response<forgotPasswordResponse>

    @POST("/api/forgot-password/verify-otp")
    suspend fun forgotPasswordVerify(
        @Body request: forgotPasswordVerifyRequest
    ): Response<forgotPasswordVerifyResponse>

    @POST("api/forgot-password/reset-password")
    suspend fun resetNewPassword(
        @Body request: resetNewPasswordRequest
    ): Response<resetNewPasswordResponse>

    @POST("/api/auth/google/login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<JsonObject>

    @POST("/api/auth/complete-registration")
    suspend fun organizationSetUp(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: organizationSetUpRequest
    ): Response<organizationSetUpResponse>

    // ── Members ───────────────────────────────────────────────────

    @GET("/api/members/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<meResponse>

    @GET("/api/members/dropdown-filter")
    suspend fun getMembersDropdownFilter(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<StaffResponse>

    // ── Organization ──────────────────────────────────────────────

    @GET("/api/organizations/my-organization")
    suspend fun getMyOrganization(
        @Header("Authorization") token: String
    ): Response<myOrganizationResponse>

    @GET("/api/dashboard-preference/my-layout")
    suspend fun getMyLayout(
        @Header("Authorization") token: String
    ): Response<myLayoutResponse>

    // ── Sales ─────────────────────────────────────────────────────

    @GET("/api/common/lead-statuses/view-all")
    suspend fun getSalesData(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<SalesResponse>

    @GET("/api/sales-leads/view-all?page=1&limit=10")
    suspend fun getSalesLeads(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<SalesSummaryResponse>

    @GET("/api/sales-leads/view-all")
    suspend fun getTableData(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<LeadsTableResponse>

    @GET("/api/sales-leads/view-one/{id}")
    suspend fun getViewOne(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<ViewOneLeadResponse>

    @POST("/api/sales-leads/create")
    suspend fun createLead(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateLeadFormRequest
    ): Response<CreateLeadFormResponse>

    @POST("/api/sales-leads/convert-to-order/{leadId}")
    suspend fun convertedToOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path ("leadId") leadId: String
    ): Response<ConvertLeadToOrderResponse>

    @PUT("/api/sales-leads/update-one/{id}")
    suspend fun updateLead(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateLeadRequest
    ): Response<UpdateLeadResponse>



    @DELETE("/api/sales-leads/delete-one/{id}")
    suspend fun deleteLead(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteLeadResponse>

    // ── Garment Categories ────────────────────────────────────────

    @GET("/api/org-garments/view-all")
    suspend fun getOrgGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<GarmentCategoriesResponse>


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

    // ── Branches ──────────────────────────────────────────────────

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

    // ── Departments ───────────────────────────────────────────────


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


    // ── Designations ──────────────────────────────────────────────

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

    // ── Sales/Order Endpoints ──

    @GET("/api/sales-orders/view-all")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): Response<OrderResponse>


    // ── Add these to your ApiService.kt ──

    @GET("/api/sales-leads/{orderId}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String
    ): Response<OrderDetailResponse>


    @PATCH("/api/sales-leads/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Query("status") status: String
    ): Response<OrderDetailResponse>

    @GET("/api/sales-orders/search-by-mobile")
    suspend fun searchCustomerByMobile(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("mobile") mobile: String
    ): Response<CustomerSearchResponse>


    //create order when save order clicks
    @Multipart
    @POST("/api/sales-orders/create-direct")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part("customer") customer: RequestBody,
        @Part("branch") branch: RequestBody,
        @Part("wearerType") wearerType: RequestBody? = null,
        @Part("source") source: RequestBody? = null,
        @Part("orderType") orderType: RequestBody? = null,
        @Part("garments") garments: RequestBody,
        @Part("paymentDetails") paymentDetails: RequestBody,
        @Part("orderDate") orderDate: RequestBody,
        @Part("trialDate") trialDate: RequestBody? = null,
        @Part("deliveryDate") deliveryDate: RequestBody? = null,
        @Part("totalAmount") totalAmount: RequestBody,
        @Part("status") status: RequestBody? = null,
        @Part designImages: List<MultipartBody.Part>,
        @Part voiceNote: MultipartBody.Part?
    ): Response<CreateOrderResponse>

    // ── Update Order (Edit flow) ──
    @Multipart
    @PUT("/api/sales-orders/update-one/{orderId}")
    suspend fun updateOrder(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Part("customer") customer: RequestBody,
        @Part("branch") branch: RequestBody,
        @Part("wearerType") wearerType: RequestBody? = null,
        @Part("source") source: RequestBody? = null,
        @Part("orderType") orderType: RequestBody? = null,
        @Part("garments") garments: RequestBody,
        @Part("paymentDetails") paymentDetails: RequestBody,
        @Part("orderDate") orderDate: RequestBody,
        @Part("trialDate") trialDate: RequestBody? = null,
        @Part("deliveryDate") deliveryDate: RequestBody? = null,
        @Part("totalAmount") totalAmount: RequestBody,
        @Part("existingImages") existingImages: RequestBody,
        @Part designImages: List<MultipartBody.Part>,
        @Part voiceNote: MultipartBody.Part?
    ): Response<CreateOrderResponse>

    @GET("/api/customers/view-all")
    suspend fun getCustomers(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponse>

    @GET("/api/measurements/customers-last-orders")
    suspend fun getMeasurements(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<MeasurementsResponse>

    @GET("/api/dashboard/advanced-dashboard")
    suspend fun getDashboardDetails(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<DashboardResponse>

    @GET("/api/customers/view-one/{id}")
    suspend fun getCustomerView(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetCustomerViewResponse>

    @PUT("/api/customers/update-one/{id}")
    suspend fun updateCustomer(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateCustomerRequest
    ): Response<UpdateCustomerResponse>

    @DELETE("/api/customers/delete-one/{id}")
    suspend fun deleteCustomer(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteCustomerResponse>

    @GET("/api/sales-orders/view-one/{orderId}")
    suspend fun getSalesOverview(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String
    ): Response<OrderOverviewApiResponse>

    @PUT("/api/sales-orders/assign-worker-to-stage/{orderId}/{garmentItemId}/cutting")
    suspend fun assignCutting(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Body request: StageAssignRequest
    ): Response<AssignStageResponse>

    @PUT("/api/sales-orders/assign-worker-to-stage/{orderId}/{garmentItemId}/stitching")
    suspend fun assignStitching(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Body request: StageAssignRequest
    ): Response<AssignStageResponse>

    @PUT("/api/sales-orders/assign-worker-to-stage/{orderId}/{garmentItemId}/qc")
    suspend fun assignQc(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Body request: StageAssignRequest
    ): Response<AssignStageResponse>

    @PUT("/api/sales-orders/update-stage-status/{orderId}/{garmentItemId}/{stageName}")
       suspend fun updateStage(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Path("garmentItemId") garmentItemId: String,
        @Path("stageName") stageName: String,
        @Body request: UpdateStageRequest
    ): Response<UpdateStageResponse>


    @GET("/api/sales-orders/confirmed-orders")
    suspend fun getOrderManagement(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<OrderManagementResponse>

    @GET("/api/sales-orders/view-one/{orderId}")
    suspend fun getOrdersView(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String
    ): Response<OrderViewResponse>

    @POST("/api/pricing-quotations/garment-pricing/set-price-for-garment")
    suspend fun savePricingQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: PricingQuotationSaveRequest
    ): Response<PricingQuotationSaveResponse>

    @GET("/api/pricing-quotations/garment-pricing/view-all")
    suspend fun getGarmentPricingList(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,

    ): ApiResponse<List<GarmentPricingListItemDto>>

    @GET("/api/pricing-quotations/garment-pricing/view-one/{id}")
    suspend fun getGarmentPricingDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): ApiResponse<GarmentPricingDetailDto>


    @PUT("/api/pricing-quotations/garment-pricing/update-price-for-garment/{id}")
    suspend fun updatePricingQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: PricingQuotationSaveRequest
    ): Response<PricingQuotationSaveResponse>

    @GET("/api/quotations/view-all")
    suspend fun getQuotations(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<QuotationListResponse>

    @GET("/api/pricing-quotations/garment-pricing/options")
    suspend fun getGarmentPricing(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<GarmentPricingResponse>

    @POST("/api/quotations/create")
    suspend fun createQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateQuotationRequest
    ): Response<CreateQuotationResponse>

    @DELETE("/api/quotations/delete-one/{id}")
    suspend fun deleteQuotation(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<QuotationDeleteResponse>

    @GET("/api/quotations/view-one/{id}")
    suspend fun getQuotationById(
        @Header("Authorization") token: String,
        @Header("x-csrf-token") csrfToken: String,
        @Path("id") id: String
    ): Response<QuotationDetailResponse>

    @GET("api/customers")
    suspend fun getCustomersV2(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponseV2>

    @GET("api/customers/{id}")
    suspend fun getCustomerDetailV2(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetCustomerDetailResponseV2>

    @GET("/api/finance/customers/view-all")
    suspend fun getCustomerForFinance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponseV2>

    @GET("/api/finance/customers/view-overview/{id}")
    suspend fun getFinanceCustomerViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetFinanceCustomerViewOneResponse>

    @GET("/api/finance/sales-invoices/view-all")
    suspend fun getInvoices(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<InvoiceListResponse>


    @GET("/api/finance/sales-invoices/view-one/{id}")
    suspend fun getInvoiceViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InvoiceViewOneResponse>

    // ── Chart of Accounts ──
    @GET("/api/finance/chart-of-accounts/view-all")
    suspend fun getChartOfAccounts(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ChartOfAccountsResponse>

    // ── Expenses: list ──
    @GET("/api/finance/expenses/view-all")   // ⚠️ confirm exact path
    suspend fun getExpenses(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<ExpenseListResponse>

    // ── Expenses: view one ──
    @GET("api/finance/expenses/{id}")   // ⚠️ confirm exact path
    suspend fun getExpenseViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<ExpenseViewOneResponse>

    // ── Expenses: create (multipart — supports file upload like createOrder) ──
    @Multipart
    @POST("/api/finance/expenses/create")   // ⚠️ confirm exact path
    suspend fun createExpense(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part("branch") branch: RequestBody,
        @Part("expenseDate") expenseDate: RequestBody,
        @Part("accountId") accountId: RequestBody,
        @Part("paymentAccountId") paymentAccountId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("referenceNumber") referenceNumber: RequestBody?,
        @Part("notes") notes: RequestBody?,
        @Part("status") status: RequestBody?,
        @Part files: List<MultipartBody.Part> = emptyList()
    ): Response<CreateExpenseResponse>

    @POST("/api/finance/chart-of-accounts/create")
    suspend fun createChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateChartOfAccountRequest
    ): Response<CreateChartOfAccountResponse>

    @PUT("/api/finance/chart-of-accounts/update-one/{id}")   //  CONFIRM this exact path + method (PUT vs PATCH) with your backend
    suspend fun updateChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateChartOfAccountRequest   // same body shape as create
    ): Response<CreateChartOfAccountResponse>          // same response shape as create

    @DELETE("/api/finance/chart-of-accounts/delete-one/{id}")   //️ confirm exact path with backend
    suspend fun deleteChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<CreateChartOfAccountResponse>   // reusing same response shape (success + message)

    // ── Trial Balance ──
    @GET("api/finance/trial-balance")
    suspend fun getTrialBalance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<TrialBalanceResponse>

    // ── Journal Entries ──
    @GET("/api/finance/journal-entry/view-all")
    suspend fun getJournalEntries(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<JournalEntryListResponse>

    @POST("/api/finance/journal-entry/create")
    suspend fun createJournalEntry(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,   // header name-ah existing finance endpoints la irukra pattern-oda match pannikonga
        @Body request: CreateJournalEntryRequest
    ): Response<CreateJournalEntryResponse>

    @GET("/api/finance/ledger/account/{accountId}")
    suspend fun getLedger(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("accountId") accountId: String
    ): Response<LedgerResponse>

    @DELETE("api/finance/journal-entry/delete-one/{id}")
    suspend fun deleteJournalEntry(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<CreateJournalEntryResponse>

    @GET("api/finance/journal-entry/view-one/{id}")
    suspend fun getJournalEntryDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<JournalEntryDetailResponse>


    @PUT("/api/finance/journal-entry/update-one/{id}")
    suspend fun updateJournalEntry(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateJournalEntryRequest
    ): Response<UpdateJournalEntryResponse>

    // ── Inventory: list items ──
    @GET("/api/inventory/item/view-all")
    suspend fun getInventoryItems(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<InventoryItemListResponse>

    // ── Inventory Items: get single item by id ──
    // Matches: repository.getInventoryItemById(id)
    // Used by the "View" button -> Item Details popup
    @GET("/api/inventory/item/view-one/{id}")
    suspend fun getInventoryItemById(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InventoryItemDetailResponse>

    // ── Inventory Items: recent ──
    // Same response shape as the list endpoint (count + data: [...]),
    // just a different route -> reuses InventoryItemListResponse.
    @GET("/api/inventory/item/recent")
    suspend fun getRecentInventoryItems(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("limit") limit: Int = 10
    ): Response<InventoryItemListResponse>


    @POST("/api/inventory/item/adjust-stock")
    suspend fun adjustStock(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: AdjustStockRequest
    ): Response<InventoryItemDetailResponse>



    // ── Inventory Items: create (multipart — text fields + optional image) ──
    @Multipart
    @POST("/api/inventory/item/create")
    suspend fun createInventoryItem(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<InventoryItemDetailResponse>

    // ── Inventory: View One (single item details) ──
    @GET("/api/inventory/item/view-one/{id}")   // ASSUMPTION: confirm exact path with backend team
    suspend fun getInventoryViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InventoryViewOneResponse>

    // ── HR: Roles ──
    @GET("/api/roles/view-all")   // ⚠️ confirm exact path with backend
    suspend fun getRoles(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<RoleListResponse>

    // ── HR: Members (Employees) ──
    @GET("/api/members/view-all")   // ⚠️ confirm exact path with backend
    suspend fun getMembers(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<MemberListResponse>

    // ── HR: Shifts ──
    @GET("/api/shifts/view-all")   // ⚠️ confirm exact path with backend
    suspend fun getShifts(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ShiftListResponse>

    @POST("/api/members/create")
    suspend fun createMember(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateMemberRequest
    ): Response<CreateMemberResponse>

    @Multipart
    @PUT("/api/members/update/profile-picture/{memberId}")
    suspend fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("memberId") memberId: String,
        @Part file: MultipartBody.Part
    ): Response<UploadProfilePictureResponse>

    @DELETE("/api/members/delete/profile-picture/{memberId}")   // ⚠️ confirm exact path with backend
    suspend fun deleteProfilePicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("memberId") memberId: String
    ): Response<DeleteProfilePictureResponse>

    @PUT("/api/members/update-one/{id}")
    suspend fun updateMember(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") memberId: String,
        @Body request: UpdateMemberRequest,   // reuse the same request shape if fields match, or make an UpdateMemberRequest
    ): Response<CreateMemberResponse>

    @GET("/api/members/view-one/{id}")
    suspend fun getMemberViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") memberId: String
    ): Response<MemberDetailResponse>

    // ── Convert Sales Order to Invoice ──
// ⚠️ CONFIRM exact path with backend team — this is the assumed convention based on existing finance endpoints
    @POST("/api/sales-orders/convert-to-invoice/{orderId}")
    suspend fun convertToInvoice(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Body request: ConvertToInvoiceRequest
    ): Response<ConvertToInvoiceResponse>
    @PUT("/api/sales-orders/receive-payment/{orderId}")
    suspend fun receivePayment(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String,
        @Body request: ReceivePaymentRequest
    ): Response<ReceivePaymentResponse>

}