@file:Suppress("unused")
package com.cuso.tailor.repository

import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.model.hr.ApplyLeaveRequest
import com.cuso.tailor.model.hr.AttendanceApproveRequest
import com.cuso.tailor.model.hr.AttendanceRecord
import com.cuso.tailor.model.hr.CreateLeaveRequest
import com.cuso.tailor.model.hr.CreateManualAttendanceRequest
import com.cuso.tailor.model.hr.CreateMemberRequest
import com.cuso.tailor.model.hr.CreateShiftRequest
import com.cuso.tailor.model.hr.CreatedMemberFullData
import com.cuso.tailor.model.hr.DeleteProfilePictureResponse
import com.cuso.tailor.model.hr.LeaveRequestItemDto
import com.cuso.tailor.model.hr.LeaveTypeItemDto
import com.cuso.tailor.model.hr.MemberDetail
import com.cuso.tailor.model.hr.MemberListResponse
import com.cuso.tailor.model.hr.MonthlyAttendanceItem
import com.cuso.tailor.model.hr.RoleItem
import com.cuso.tailor.model.hr.SalaryComponentItem
import com.cuso.tailor.model.hr.SalaryComponentListData
import com.cuso.tailor.model.hr.SalaryComponentRequest
import com.cuso.tailor.model.hr.ShiftDetailData
import com.cuso.tailor.model.hr.ShiftDto
import com.cuso.tailor.model.hr.ShiftItem
import com.cuso.tailor.model.hr.ToggleSalaryComponentStatusRequest
import com.cuso.tailor.model.hr.UpdateLeaveStatusRequest
import com.cuso.tailor.model.hr.UpdateMemberRequest
import com.cuso.tailor.model.hr.UpdateShiftRequest
import com.cuso.tailor.model.hr.UploadProfilePictureResponse
import com.cuso.tailor.network.hr.HrApiService
import com.cuso.tailor.utils.format24HrTo12Hr
import com.cuso.tailor.utils.formatIsoToTime
import com.cuso.tailor.utils.formatMinutesToHours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HrRepository @Inject constructor(
    private val hrApi: HrApiService,
    private val tokensDao: TokensDao
) {

    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // ── Roles ──
    suspend fun getRoles(): Result<List<RoleItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getRoles(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch roles: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Members (Employees) ──
    suspend fun getMembers(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<MemberListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getMembers(
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
                    Exception(response.errorBody()?.string() ?: "Failed to fetch employees: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Shifts ──
    suspend fun getShifts(): Result<List<ShiftItem>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getShifts(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()?.data ?: emptyList()
                Result.success(dtoList.map { it.toShiftItem() })
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch shifts: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildMemberFormFields(request: CreateMemberRequest): Map<String, RequestBody> {
        val gson = com.google.gson.Gson()
        val textType = "text/plain".toMediaTypeOrNull()

        fun String.asTextBody(): RequestBody = this.toRequestBody(textType)

        val fields = mutableMapOf<String, RequestBody>()

        // Simple fields
        fields["firstName"] = request.firstName.asTextBody()
        fields["lastName"] = request.lastName.asTextBody()
        fields["email"] = request.email.asTextBody()
        fields["personalEmail"] = request.personalEmail.asTextBody()
        fields["personalMobile"] = request.personalMobile.asTextBody()
        fields["workMobile"] = request.workMobile.asTextBody()
        fields["dob"] = request.dob.asTextBody()  // Must be yyyy-MM-dd
        fields["gender"] = request.gender.asTextBody()
        fields["martialStatus"] = request.martialStatus.asTextBody()
        fields["doj"] = request.doj.asTextBody()  // Must be yyyy-MM-dd
        fields["employmentType"] = request.employmentType.asTextBody()
        fields["hasTemporaryAddress"] = request.hasTemporaryAddress.toString().asTextBody()

        // Optional fields
        request.branchId?.let { fields["branchId"] = it.asTextBody() }
        request.departmentId?.let { fields["departmentId"] = it.asTextBody() }
        request.designationId?.let { fields["designationId"] = it.asTextBody() }
        request.customRoleId?.let { fields["customRoleId"] = it.asTextBody() }
        request.shiftId?.let { fields["shiftId"] = it.asTextBody() }
        request.workingDistrict.let { fields["workingDistrict"] = it.asTextBody() }
        request.reportingTo?.let { fields["reportingTo"] = it.asTextBody() }
        request.secondaryReportingTo?.let { fields["secondaryReportingTo"] = it.asTextBody() }

        // Nested objects - serialize to JSON (like SalesRepository does)
        fields["permanentAddress"] = gson.toJson(request.permanentAddress).asTextBody()
        request.temporaryAddress?.let {
            fields["temporaryAddress"] = gson.toJson(it).asTextBody()
        }
        fields["education"] = gson.toJson(request.education).asTextBody()
        fields["workExperience"] = gson.toJson(request.workExperience).asTextBody()

        return fields
    }

    suspend fun createMember(request: CreateMemberRequest): Result<CreatedMemberFullData> {
        return try {
            val (authHeader, csrfToken) = getAuthHeaders()
            val response = hrApi.createMember(authHeader, csrfToken, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to create member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMember(memberId: String, request: UpdateMemberRequest): Result<String> {
        return try {
            val (authHeader, csrfToken) = getAuthHeaders()
            val response = hrApi.updateMember(authHeader, csrfToken, memberId, request)

            if (response.isSuccessful) {
                Result.success("Member updated successfully")
            } else {
                val errorString = response.errorBody()?.string()
                Result.failure(Exception(errorString ?: "Failed to update member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMemberDetail(memberId: String): Result<MemberDetail> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getMemberViewOne(accessToken, csrfToken, memberId)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()
                val detail = body?.data ?: body?.member
                if (detail != null) {
                    Result.success(detail)
                } else {
                    Result.failure(Exception("Empty member detail"))
                }
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch employee detail: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(memberId: String, file: File): Result<UploadProfilePictureResponse> {
        return try {
            val (authHeader, csrfToken) = getAuthHeaders()

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "profilePicture",
                file.name,
                requestFile
            )

            val response = hrApi.uploadProfilePicture(
                token = authHeader,
                csrfToken = csrfToken,
                memberId = memberId,
                file = filePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Upload failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfilePicture(memberId: String): Result<DeleteProfilePictureResponse> {
        return try {
            val (authHeader, csrfToken) = getAuthHeaders()

            val response = hrApi.deleteProfilePicture(
                token = authHeader,
                csrfToken = csrfToken,
                memberId = memberId
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //MONTHLY ATTENDANCE
    suspend fun getMonthlyAttendance(
        organizationMemberId: String,
        month: Int,
        year: Int
    ): Result<List<MonthlyAttendanceItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getMonthlyAttendance(
                token = accessToken,
                csrfToken = csrfToken,
                organizationMemberId = organizationMemberId,
                month = month,
                year = year
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch attendance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Daily Attendance with Pagination ──
    suspend fun getDailyAttendanceList(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null,
        date: String? = null
    ): Result<List<AttendanceRecord>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val effectiveStatus = if (status.equals("All", ignoreCase = true)) null else status?.lowercase()
            val effectiveSearch = search?.trim()?.ifBlank { null }

            val response = hrApi.getAttendanceList(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = effectiveSearch,
                status = effectiveStatus,
                date = date
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()?.data ?: emptyList()
                val mappedList = dtoList.map { dto ->
                    val fullName = listOfNotNull(
                        dto.organizationMember?.firstName,
                        dto.organizationMember?.lastName
                    ).joinToString(" ").ifBlank { "Unknown Member" }

                    val empCode = dto.organizationMember?.memberId
                        ?: dto.organizationMember?.memberId
                        ?: "N/A"

                    val deptName = dto.organizationMember?.department?.name ?: "General"
                    val shiftName = dto.shift?.name ?: "General Shift"

                    val inTime = formatIsoToTime(dto.firstIn)
                    val outTime = formatIsoToTime(dto.lastOut)
                    val totalHrs = formatMinutesToHours(dto.totalWorkingMinutes)
                    val displayDate = formatIsoToDisplayDate(dto.date)

                    AttendanceRecord(
                        id = dto.id,
                        name = fullName,
                        empCode = empCode,
                        department = deptName,
                        shift = shiftName,
                        inTime = inTime,
                        totalHours = totalHrs,
                        outTime = outTime,
                        status = dto.status?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Absent",
                        date = displayDate,
                        approvalStatus = dto.approvalStatus?.lowercase() ?: ""
                    )
                }
                Result.success(mappedList)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch attendance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun formatIsoToDisplayDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = isoFormat.parse(isoString)
            val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            date?.let { displayFormat.format(it) } ?: ""
        } catch (_: Exception) {
            ""
        }
    }
    //approval
    suspend fun approveAttendance(attendanceId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val request = AttendanceApproveRequest(
                attendanceId = attendanceId,
                isApproved = true,
                approvalStatus = "approved",
                status = "present"
            )
            val response = hrApi.approveAttendance(
                token = accessToken,
                csrfToken = csrfToken,
                attendanceId = attendanceId,
                request = request
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Attendance approved successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to approve attendance"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //create manual entry
    suspend fun createManualAttendance(request: CreateManualAttendanceRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.createManualAttendance(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Manual attendance created successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to create manual attendance"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //shifts

    suspend fun getShiftsViewAll(): Result<List<ShiftItem>> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getShiftsViewAll(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()?.data ?: emptyList()
                Result.success(dtoList.map { it.toShiftItem() })
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch shifts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ShiftDto.toShiftItem(): ShiftItem {
        val breakMins = breakDuration?.let { "$it mins" } ?: "0 mins"
        val type = shiftType ?: if (isDefault) "Fixed" else "Rotation"
        val deptOrCode = shiftId ?: description ?: "Shift"

        return ShiftItem(
            id = id,
            title = name ?: "Unnamed Shift",
            department = deptOrCode,
            startTime = format24HrTo12Hr(startTime),
            endTime = format24HrTo12Hr(endTime),
            breakDuration = breakMins,
            shiftType = type,
            isActive = status
        )
    }

    suspend fun createShift(request: CreateShiftRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.createShift(
                token = accessToken,
                csrfToken = csrfToken,
                request = request
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Shift created successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to create shift"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShiftDetail(shiftId: String): Result<ShiftDetailData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getShiftDetail(accessToken, csrfToken, shiftId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to load shift detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShift(shiftId: String, request: UpdateShiftRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.updateShift(accessToken, csrfToken, shiftId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Shift updated successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update shift"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteShift(shiftId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.deleteShift(accessToken, csrfToken, shiftId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Shift deleted successfully")
            } else {
                Result.failure(Exception("Failed to delete shift"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //LEAVE MANAGEMENT

    // Fetch Leave Requests
    suspend fun getLeaveRequests(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null
    ): Result<List<LeaveRequestItemDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getLeaveRequests(accessToken, csrfToken, page, limit, status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch leave requests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update Leave Request Status
    suspend fun updateLeaveStatus(
        id: String,
        status: String,
        approverNote: String? = null
    ): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val request = UpdateLeaveStatusRequest(
                status = status,
                approverNote = approverNote
            )
            val response = hrApi.updateLeaveStatus(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update leave status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get Leave Types
    suspend fun getLeaveTypes(): Result<List<LeaveTypeItemDto>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getLeaveTypes(accessToken, csrfToken)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch leave types"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create Leave Request
    suspend fun createLeaveRequest(
        organizationMemberId: String,
        leaveTypeId: String,
        startDate: String,
        endDate: String,
        totalDays: Int,
        isHalfDay: Boolean,
        reason: String,
        attachmentParts: List<MultipartBody.Part>
    ): Result<String> {
        return try {
            val textMediaType = "text/plain".toMediaTypeOrNull()
            val (accessToken, csrfToken) = getAuthHeaders()

            val partMap = mapOf(
                "organizationMemberId" to organizationMemberId.toRequestBody(textMediaType),
                "leaveTypeId" to leaveTypeId.toRequestBody(textMediaType),
                "startDate" to startDate.toRequestBody(textMediaType),
                "endDate" to endDate.toRequestBody(textMediaType),
                "totalDays" to totalDays.toString().toRequestBody(textMediaType),
                "isHalfDay" to isHalfDay.toString().toRequestBody(textMediaType),
                "reason" to reason.toRequestBody(textMediaType)
            )

            val response = hrApi.createLeaveRequest(
                token = accessToken,
                csrfToken = csrfToken,
                partMap = partMap,
                attachments = attachmentParts.ifEmpty { null }
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create leave request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════
    // ── Salary Components ──
    // ═══════════════════════════════════════════════

    suspend fun getSalaryComponents(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        type: String? = null,
        status: String? = null
    ): Result<SalaryComponentListData> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getSalaryComponents(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                type = type,
                status = status
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch salary components"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalaryComponentDetail(id: String): Result<SalaryComponentItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getSalaryComponentDetail(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch component detail"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSalaryComponent(request: SalaryComponentRequest): Result<SalaryComponentItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.createSalaryComponent(accessToken, csrfToken, request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to create salary component"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSalaryComponent(id: String, request: SalaryComponentRequest): Result<SalaryComponentItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.updateSalaryComponent(accessToken, csrfToken, id, request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update salary component"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleSalaryComponentStatus(id: String, isActive: Boolean): Result<SalaryComponentItem> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.toggleSalaryComponentStatus(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = ToggleSalaryComponentStatusRequest(isActive = isActive)
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update component status"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSalaryComponent(id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.deleteSalaryComponent(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Salary component deleted successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to delete salary component"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}