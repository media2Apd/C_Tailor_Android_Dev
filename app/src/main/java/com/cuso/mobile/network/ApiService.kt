package com.cuso.mobile.network

import AddGarmentRequest
import AddOrgGarmentResponse
import GarmentCategoriesResponse
import OrgGarmentResponse
import RemoveOrgGarmentResponse
import com.cuso.mobile.model.CreateLeadFormRequest
import com.cuso.mobile.model.CreateLeadFormResponse
import com.cuso.mobile.model.DeleteLeadResponse
import com.cuso.mobile.model.EmailResponse
import com.cuso.mobile.model.EmailVerify
import com.cuso.mobile.model.GoogleLoginRequest
import com.cuso.mobile.model.LeadsTableResponse
//import com.cuso.mobile.model.GoogleLoginRequest
//import com.cuso.mobile.model.GoogleLoginResponse

import com.cuso.mobile.model.PasswordResponse
import com.cuso.mobile.model.PasswordVerify
import com.cuso.mobile.model.RegisterVerifyOtp
import com.cuso.mobile.model.RegisterVerifyOtpResponse
import com.cuso.mobile.model.SalesResponse
import com.cuso.mobile.model.SalesSummaryResponse
import com.cuso.mobile.model.SignupRequest
import com.cuso.mobile.model.SignupResponse
import com.cuso.mobile.model.StaffResponse
import com.cuso.mobile.model.UpdateLeadResponse
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
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface ApiService {
//  Sign up

    @POST("/api/auth/register/send-otp")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    @POST("/api/auth/register/verify-otp")
    suspend fun signupVerifyOtp(
        @Body request: RegisterVerifyOtp
    ): Response<RegisterVerifyOtpResponse>


    //  Check email id
    @POST("/api/auth/check-email")
    suspend fun verifyEmail(
        @Body request: EmailVerify
    ): Response<EmailResponse>

    //  Password verification
    @POST("/api/auth/login")
    suspend fun verifyPassword(
        @Body request: PasswordVerify
    ): Response<PasswordResponse>

    //  Otp sending
    @POST("/api/auth/login/send-otp")
    suspend fun otpSend(
        @Body request: otpSendRequest
    ): Response<otpSendResponse>


    //  Otp verification
    @POST("api/auth/login/verify-otp")
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


    @GET("/api/members/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<meResponse>


    @GET("/api/organizations/my-organization")
    suspend fun getMyOrganization(
        @Header("Authorization") token: String
    ): Response<myOrganizationResponse>

    @GET("/api/dashboard-preference/my-layout")
    suspend fun getMyLayout(
        @Header("Authorization") token: String
    ): Response<myLayoutResponse>

    @GET("/api/common/lead-statuses/view-all")   // your actual endpoint
    suspend fun getSalesData(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<SalesResponse>

    // Rename the function to reflect what it actually does


    @GET("/api/sales-leads/view-all?page=1&limit=10")  // Use the correct path
    suspend fun getSalesLeads(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<SalesSummaryResponse>

    @POST("/api/sales-leads/create")
    suspend fun createLead(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateLeadFormRequest
    ): Response<CreateLeadFormResponse>

    @GET("/api/sales-leads/view-one/{id}")
    suspend fun getViewOne(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token")  csrfToken: String,
        @Path("id") id: String
    ): Response<ViewOneLeadResponse>


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
        // ✅ All garment categories are nullable - skip if not present
        @Part("garmentCategory[0]") garmentCategory0: RequestBody? = null,
        @Part("garmentCategory[1]") garmentCategory1: RequestBody? = null,
        @Part("garmentCategory[2]") garmentCategory2: RequestBody? = null,
        @Part("garmentCategory[3]") garmentCategory3: RequestBody? = null,
        @Part("garmentCategory[4]") garmentCategory4: RequestBody? = null,
        @Part("notes[1][message]") noteMessage1: RequestBody? = null,
        @Part("notes[1][type]") noteType1: RequestBody? = null,
    ): Response<UpdateLeadResponse>

    // In ApiService.kt

    @DELETE("/api/sales-leads/delete-one/{id}")
    suspend fun deleteLead(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<DeleteLeadResponse>


    @GET("/api/members/dropdown-filter")    // your actual endpoint
    suspend fun getMembersDropdownFilter(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<StaffResponse>

    @GET("/api/sales-leads/view-all?page=1&limit=10")   // your actual endpoint
    suspend fun getTableData(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<LeadsTableResponse>

    // In your API service interface
    @GET("/api/org-garments/view-all")
    suspend fun getOrgGarments(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<GarmentCategoriesResponse>  // ← Correct response type

    @GET("/api/common/categories/view-all") // Update with your actual endpoint
    suspend fun getOrgGarmentCommonCategories(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<OrgGarmentResponse>

    @POST("/api/org-garments/add") // Update with your actual endpoint
    suspend fun addOrgGarmentCategory(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: AddGarmentRequest
    ): Response<AddOrgGarmentResponse>

    @DELETE("/api/org-garments/remove/{categoryId}") // Update with your actual endpoint
    suspend fun removeOrgGarmentCategory(
        @Header("Authorization") accessToken: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("categoryId") categoryId: String
    ): Response<RemoveOrgGarmentResponse>

}