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
import com.cuso.mobile.model.AssignStageResponse
import com.cuso.mobile.model.BranchListResponse
import com.cuso.mobile.model.CreateLeadFormRequest
import com.cuso.mobile.model.CreateLeadFormResponse
import com.cuso.mobile.model.CreateOrderResponse
import com.cuso.mobile.model.CustomerSearchResponse
import com.cuso.mobile.model.DeleteLeadResponse
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
import com.cuso.mobile.model.LeadsTableResponse
import com.cuso.mobile.model.OrderDetailResponse
import com.cuso.mobile.model.OrderResponse
import com.cuso.mobile.model.PasswordResponse
import com.cuso.mobile.model.PasswordVerify
import com.cuso.mobile.model.RegisterVerifyOtp
import com.cuso.mobile.model.RegisterVerifyOtpResponse
import com.cuso.mobile.model.SalesResponse
import com.cuso.mobile.model.SalesSummaryResponse
import com.cuso.mobile.model.SignupRequest
import com.cuso.mobile.model.SignupResponse
import com.cuso.mobile.model.StaffResponse
import com.cuso.mobile.model.UpdateBranchRequest
import com.cuso.mobile.model.UpdateBranchResponse
import com.cuso.mobile.model.UpdateLeadResponse
import com.cuso.mobile.model.UpdateOrganizationRequest
import com.cuso.mobile.model.UpdateOrganizationResponse
import com.cuso.mobile.model.ViewOneLeadResponse
import com.cuso.mobile.model.forgotPasswordRequest
import com.cuso.mobile.model.forgotPasswordResponse
import com.cuso.mobile.model.otpSendRequest
import com.cuso.mobile.model.otpSendResponse
import com.cuso.mobile.model.otpVerifyRequest
import com.cuso.mobile.model.otpVerifyResponse
import com.cuso.mobile.model.forgotPasswordVerifyRequest
import com.cuso.mobile.model.forgotPasswordVerifyResponse
import com.cuso.mobile.model.meResponse
import com.cuso.mobile.model.myLayoutResponse
import com.cuso.mobile.model.myOrganizationResponse
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
import com.cuso.mobile.model.CustomerItem
import com.cuso.mobile.model.CustomerListResponse
import com.cuso.mobile.model.DashboardResponse
import com.cuso.mobile.model.DeleteCustomerResponse
import com.cuso.mobile.model.GetCustomerViewResponse
import com.cuso.mobile.model.MeasurementsResponse
import com.cuso.mobile.model.OrderManagementResponse
import com.cuso.mobile.model.OrderOverviewApiResponse
import com.cuso.mobile.model.OrderViewResponse
import com.cuso.mobile.model.PricingQuotationSaveRequest
import com.cuso.mobile.model.PricingQuotationSaveResponse
import com.cuso.mobile.model.StageAssignRequest
import com.cuso.mobile.model.UpdateCustomerRequest
import com.cuso.mobile.model.UpdateCustomerResponse
import com.cuso.mobile.model.UpdateStageRequest
import com.cuso.mobile.model.UpdateStageResponse

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

    // In ApiService.kt
    @PUT("/api/organizations/update-one")
    suspend fun updateOrganization(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: UpdateOrganizationRequest
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

}