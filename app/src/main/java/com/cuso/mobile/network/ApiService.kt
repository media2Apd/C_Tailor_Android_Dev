// com/cuso/mobile/network/ApiService.kt

package com.cuso.mobile.network

import ActiveOrgGarmentResponse
import AddGarmentRequest
import AddOrgGarmentResponse
import CreateBranchRequest
import CreateBranchResponse
import GarmentCategoriesResponse
import OrgGarmentResponse
import RemoveOrgGarmentResponse
import com.cuso.mobile.model.sales.ApiResponse
import com.cuso.mobile.model.sales.AssignStageResponse
import com.cuso.mobile.model.BranchListResponse
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
import com.cuso.mobile.model.UpdateOrganizationRequest
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
import com.cuso.mobile.model.sales.DeleteCustomerResponse
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
import com.cuso.mobile.model.sales.StageAssignRequest
import com.cuso.mobile.model.sales.UpdateCustomerRequest
import com.cuso.mobile.model.sales.UpdateCustomerResponse
import com.cuso.mobile.model.sales.UpdateStageRequest
import com.cuso.mobile.model.sales.UpdateStageResponse

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

    @GET("/api/sales-leads/view-all?page=1&limit=10")
    suspend fun getTableData(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
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

    @Multipart
    @PUT("/api/sales-leads/update-one/{id}")
    suspend fun updateLead(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Part("customerType") customerType: RequestBody,
        @Part("enquiryType") enquiryType: RequestBody,
        @Part("estimatedQuantity") estimatedQuantity: RequestBody,
        @Part("budgetRange[min]") budgetMin: RequestBody,
        @Part("budgetRange[max]") budgetMax: RequestBody,
        @Part("enquiryDate") enquiryDate: RequestBody,
        @Part("requiredDate") requiredDate: RequestBody,
        @Part("status") status: RequestBody,
        @Part("source") source: RequestBody,
        @Part("person[name]") personName: RequestBody,
        @Part("person[phone]") personPhone: RequestBody,
        @Part("person[email]") personEmail: RequestBody,
        @Part("appointment[isRequired]") appointmentIsRequired: RequestBody,
        @Part("notes[0][message]") noteMessage: RequestBody,
        @Part("notes[0][type]") noteType: RequestBody,
        @Part("person[gender]") personGender: RequestBody,
        @Part("person[dob]") personDob: RequestBody,
        @Part("contact[address]") contactAddress: RequestBody,
        @Part("contact[area]") contactArea: RequestBody,
        @Part("contact[city]") contactCity: RequestBody,
        @Part("contact[preferredContactMethod]") contactPreferredContactMethod: RequestBody,
        @Part("garmentCategory[0]") garmentCategory0: RequestBody? = null,
        @Part("garmentCategory[1]") garmentCategory1: RequestBody? = null,
        @Part("garmentCategory[2]") garmentCategory2: RequestBody? = null,
        @Part("garmentCategory[3]") garmentCategory3: RequestBody? = null,
        @Part("garmentCategory[4]") garmentCategory4: RequestBody? = null,
        @Part("notes[1][message]") noteMessage1: RequestBody? = null,
        @Part("notes[1][type]") noteType1: RequestBody? = null,
    ): Response<UpdateLeadResponse>



    @DELETE("/api/sales-leads/delete-one/{id}")
    suspend fun deleteLead(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteLeadResponse>

    // ── Garment Categories ────────────────────────────────────────

    // Existing - fetchGarmentCategories() uses this (GarmentCategoriesResponse)
    @GET("/api/org-garments/view-all")
    suspend fun getOrgGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<GarmentCategoriesResponse>

    // ✅ NEW - Same URL, OrgGarmentResponse return type
    // fetchActiveOrgGarmentIds() uses this to find which common-categories are active
    // React equivalent: SummaryApi.getAllCategories → res.data.data.categories.filter(c=>c.isActive).map(c=>c.categoryId._id)
    @GET("/api/org-garments/view-all")
    suspend fun getActiveOrgGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ActiveOrgGarmentResponse>

    // Common categories display grid
    // React equivalent: SummaryApi.getCommonCategories
    @GET("/api/common/categories/view-all")
    suspend fun getOrgGarmentCommonCategories(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<OrgGarmentResponse>

    // Add org garment
    // React equivalent: SummaryApi.addCategories
    @POST("/api/org-garments/add")
    suspend fun addOrgGarmentCategory(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: AddGarmentRequest
    ): Response<AddOrgGarmentResponse>

    // Remove org garment
    // React equivalent: SummaryApi.removeOneCategory
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


    // Option 1: All unwrapped (recommended)
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
    ): DepartmentCreateResponse  // ← Remove Response wrapper

    @PUT("/api/departments/update-one/{id}")
    suspend fun updateDepartment(
        @Header("Authorization") authorization: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Path("id") id: String,
        @Body request: DepartmentUpdateRequest
    ): DepartmentUpdateResponse  // ← Remove Response wrapper


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

    // ✅ ADD THIS - Update Designation
    @PUT("/api/designations/update-one/{id}")
    suspend fun updateDesignation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: DesignationUpdateRequest
    ): Response<DesignationUpdateResponse>

    // ✅ ADD THIS - Delete Designation (Soft Delete)
    @DELETE("/api/designations/delete-one/{id}")
    suspend fun deleteDesignation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DesignationDeleteResponse>

    @Multipart
    @POST("api/organizations/upload-picture")   // ⚠️ replace with actual endpoint
    suspend fun uploadOrganizationPicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part picture: MultipartBody.Part
    ): Response<UploadOrganizationPictureResponse>

    // In ApiService.kt
    // WITH this:
    @Multipart
    @PUT("/api/organizations/update-one")
    suspend fun updateOrganization(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part("name") name: RequestBody?,
        @Part("orgType") orgType: RequestBody?,
        @Part("businessType") businessType: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part("settings") settings: RequestBody?,
        @Part organizationPicture: MultipartBody.Part?
    ): UpdateOrganizationResponse

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

    // ── Add these to your ApiService.kt after the getOrders() method ──

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
        @Part designImages: List<MultipartBody.Part>,           // pass emptyList() if none
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

    @GET("/api/customers/view-one/{id}")   // 🔁 replace with your real path
    suspend fun getCustomerView(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetCustomerViewResponse>

    @PUT("/api/customers/update-one/{id}") // 🔁 replace with your real path
    suspend fun updateCustomer(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateCustomerRequest
    ): Response<UpdateCustomerResponse>

    @DELETE("/api/customers/delete-one/{id}")   // 🔁 replace with your real path
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

    @GET("/api/sales-orders/view-one/{orderId}")   // ✅ உங்க backend-oda actual route path-ஐ இங்க கொடுங்க
    suspend fun getOrdersView(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("orderId") orderId: String
    ): Response<OrderViewResponse>

    // Add this endpoint to your ApiService interface
    @POST("/api/pricing-quotations/garment-pricing/set-price-for-garment")  // Update with your actual endpoint
    suspend fun savePricingQuotation(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: PricingQuotationSaveRequest
    ): Response<PricingQuotationSaveResponse>

    // ── Dashboard: get all garment pricing cards ──
    @GET("/api/pricing-quotations/garment-pricing/view-all")
    suspend fun getGarmentPricingList(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,

    ): ApiResponse<List<GarmentPricingListItemDto>>

    // ── Get single record detail (for edit-screen prefill) ──
    @GET("/api/pricing-quotations/garment-pricing/view-one/{id}")
    suspend fun getGarmentPricingDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): ApiResponse<GarmentPricingDetailDto>

    // ApiService.kt — add this alongside savePricingQuotation

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

    // Add this endpoint to ApiService interface:

    @GET("/api/finance/sales-invoices/view-one/{id}")
    suspend fun getInvoiceViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InvoiceViewOneResponse>

    // ── Chart of Accounts ──
    @GET("/api/finance/chart-of-accounts/view-all")   // ⚠️ confirm exact path with backend
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

    @PUT("/api/finance/chart-of-accounts/update-one/{id}")   // ⚠️ CONFIRM this exact path + method (PUT vs PATCH) with your backend
    suspend fun updateChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateChartOfAccountRequest   // same body shape as create
    ): Response<CreateChartOfAccountResponse>          // same response shape as create

    @DELETE("/api/finance/chart-of-accounts/delete-one/{id}")   // ⚠️ confirm exact path with backend
    suspend fun deleteChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<CreateChartOfAccountResponse>   // reusing same response shape (success + message)

    // ── Trial Balance ──
    @GET("api/finance/trial-balance")   // ✅ ungaloda real endpoint path-a check pannunga
    suspend fun getTrialBalance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<TrialBalanceResponse>

    // ── Journal Entries ──
    @GET("/api/finance/journal-entry/view-all")   // ✅ ungaloda real endpoint path check pannunga
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


}