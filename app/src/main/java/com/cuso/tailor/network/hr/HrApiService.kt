package com.cuso.tailor.network.hr

import com.cuso.tailor.model.hr.ApplyLeaveRequest
import com.cuso.tailor.model.hr.ApplyLeaveResponse
import com.cuso.tailor.model.hr.AttendanceApproveRequest
import com.cuso.tailor.model.hr.AttendanceApproveResponse
import com.cuso.tailor.model.hr.AttendanceResponse
import com.cuso.tailor.model.hr.CreateLeaveRequest
import com.cuso.tailor.model.hr.CreateLeaveResponse
import com.cuso.tailor.model.hr.CreateManualAttendanceRequest
import com.cuso.tailor.model.hr.CreateManualAttendanceResponse
import com.cuso.tailor.model.hr.CreateMemberRequest
import com.cuso.tailor.model.hr.CreateMemberResponse
import com.cuso.tailor.model.hr.CreateShiftRequest
import com.cuso.tailor.model.hr.CreateShiftResponse
import com.cuso.tailor.model.hr.DeleteProfilePictureResponse
import com.cuso.tailor.model.hr.GenericApiResponse
import com.cuso.tailor.model.hr.LeaveActionResponse
import com.cuso.tailor.model.hr.LeaveRequestsResponse
import com.cuso.tailor.model.hr.LeaveTypeResponse
import com.cuso.tailor.model.hr.MemberDetailResponse
import com.cuso.tailor.model.hr.MemberListResponse
import com.cuso.tailor.model.hr.MonthlyAttendanceResponse
import com.cuso.tailor.model.hr.RoleListResponse
import com.cuso.tailor.model.hr.SalaryComponentListResponse
import com.cuso.tailor.model.hr.SalaryComponentRequest
import com.cuso.tailor.model.hr.SalaryComponentSingleResponse
import com.cuso.tailor.model.hr.ShiftDetailResponse
import com.cuso.tailor.model.hr.ShiftListResponse
import com.cuso.tailor.model.hr.ToggleSalaryComponentStatusRequest
import com.cuso.tailor.model.hr.UpdateLeaveStatusRequest
import com.cuso.tailor.model.hr.UpdateLeaveStatusResponse
import com.cuso.tailor.model.sales.StaffResponse
import com.cuso.tailor.model.hr.UpdateMemberRequest
import com.cuso.tailor.model.hr.UpdateShiftRequest
import com.cuso.tailor.model.hr.UpdateShiftResponse
import com.cuso.tailor.model.hr.UploadProfilePictureResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface HrApiService {
    @GET("/api/members/dropdown-filter")
    suspend fun getMembersDropdownFilter(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<StaffResponse>

    @GET("/api/members/view-all")
    suspend fun getMembers(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<MemberListResponse>

    @GET("/api/members/view-one/{id}")
    suspend fun getMemberViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") memberId: String
    ): Response<MemberDetailResponse>

    @POST("/api/members/create")
    suspend fun createMember(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateMemberRequest
    ): Response<CreateMemberResponse>

    @PUT("/api/members/update-one/{id}")
    suspend fun updateMember(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") memberId: String,
        @Body request: UpdateMemberRequest
    ): Response<CreateMemberResponse>

    @Multipart
    @PUT("/api/members/update/profile-picture/{memberId}")
    suspend fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("memberId") memberId: String,
        @Part file: MultipartBody.Part
    ): Response<UploadProfilePictureResponse>

    @DELETE("/api/members/delete/profile-picture/{memberId}")
    suspend fun deleteProfilePicture(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("memberId") memberId: String
    ): Response<DeleteProfilePictureResponse>

    @GET("/api/roles/view-all")
    suspend fun getRoles(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<RoleListResponse>

    @GET("/api/shifts/view-all")
    suspend fun getShifts(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ShiftListResponse>

    // Monthly Attendance
    @GET("/api/hr/attendance/monthly-view")
    suspend fun getMonthlyAttendance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("organizationMemberId") organizationMemberId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<MonthlyAttendanceResponse>

    // Daily Attendance (With Pagination & Filters)
    @GET("/api/hr/attendance/view-all")
    suspend fun getAttendanceList(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("date") date: String? = null
    ): Response<AttendanceResponse>

    //approve
    @PATCH("/api/hr/attendance-manual/approve/{id}")
    suspend fun approveAttendance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") attendanceId: String,
        @Body request: AttendanceApproveRequest
    ): Response<AttendanceApproveResponse>

    //create manual entry

    @POST("/api/hr/attendance-manual/create")
    suspend fun createManualAttendance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateManualAttendanceRequest
    ): Response<CreateManualAttendanceResponse>

    @GET("/api/hr/shift/view-all")
    suspend fun getShiftsViewAll(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ShiftListResponse>

    @POST("/api/hr/shift/create")
    suspend fun createShift(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateShiftRequest
    ): Response<CreateShiftResponse>

    @GET("/api/hr/shift/view-one/{id}")
    suspend fun getShiftDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<ShiftDetailResponse>

    @PUT("/api/hr/shift/update-one/{id}")
    suspend fun updateShift(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateShiftRequest
    ): Response<UpdateShiftResponse>

    @DELETE("/api/hr/shift/delete-one/{id}")
    suspend fun deleteShift(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GenericApiResponse>

    //LEAVE MANAGEMENT

    // Fetch all leave requests
    @GET("/api/hr/leave-request/by-status/pending")
    suspend fun getLeaveRequests(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null
    ): Response<LeaveRequestsResponse>

    // Update leave request status (Approve / Reject)
    @PATCH("/api/hr/leave-request/status/{id}")
    suspend fun updateLeaveStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateLeaveStatusRequest
    ): Response<UpdateLeaveStatusResponse>

    // Fetch all configured leave types
    @GET("/api/hr/leave-type/view-all")
    suspend fun getLeaveTypes(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<LeaveTypeResponse>

    // Create a new leave request
    @Multipart
    @POST("/api/hr/leave-request/create")
    suspend fun createLeaveRequest(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part attachments: List<MultipartBody.Part>?
    ): Response<CreateLeaveResponse>

    //SALARY COMPONENTS

    // ═══════════════════════════════════════════════
    // ── SALARY COMPONENTS ──
    // ═══════════════════════════════════════════════

    @GET("/api/hr/salary-component/view-all")
    suspend fun getSalaryComponents(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null
    ): Response<SalaryComponentListResponse>

    @GET("/api/hr/salary-component/view-one/{id}")
    suspend fun getSalaryComponentDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SalaryComponentSingleResponse>

    @POST("/api/hr/salary-component/create")
    suspend fun createSalaryComponent(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: SalaryComponentRequest
    ): Response<SalaryComponentSingleResponse>

    @PUT("/api/hr/salary-component/update/{id}")
    suspend fun updateSalaryComponent(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: SalaryComponentRequest
    ): Response<SalaryComponentSingleResponse>

    @PATCH("/api/hr/salary-component/delete/{id}")
    suspend fun toggleSalaryComponentStatus(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: ToggleSalaryComponentStatusRequest
    ): Response<SalaryComponentSingleResponse>

    @DELETE("/api/hr/salary-component/delete/{id}")
    suspend fun deleteSalaryComponent(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<SalaryComponentSingleResponse>

}