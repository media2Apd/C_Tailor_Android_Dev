package com.cuso.mobile.repository

import com.cuso.mobile.model.hr.CreateMemberRequest
import com.cuso.mobile.model.hr.CreatedMemberFullData
import com.cuso.mobile.model.hr.MemberDetail
import com.cuso.mobile.model.hr.MemberListResponse
import com.cuso.mobile.model.hr.RoleItem
import com.cuso.mobile.model.hr.ShiftItem
import com.cuso.mobile.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HrRepository @Inject constructor(
    private val api: ApiService,
    private val tokensDao: com.cuso.mobile.database.dao.TokensDao
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
            val response = api.getRoles(accessToken, csrfToken)
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
            val response = api.getMembers(
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
    suspend fun getShifts(): Result<List<ShiftItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getShifts(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
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
        request.workingDistrict?.let { fields["workingDistrict"] = it.asTextBody() }
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

    suspend fun createMember(
        request: CreateMemberRequest,
        imageFile: java.io.File?
    ): Result<CreatedMemberFullData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            val fields = buildMemberFormFields(request)
            val imagePart: MultipartBody.Part? = imageFile?.let { file ->
                val reqFile = file.asRequestBody("image/png".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("userImage", file.name, reqFile)
            }

            val response = api.createMember(accessToken, csrfToken, fields, imagePart)

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response data"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Failed to create employee: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMember(
        memberId: String,
        request: CreateMemberRequest,
        imageFile: java.io.File?
    ): Result<CreatedMemberFullData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val fields = buildMemberFormFields(request)
            val imagePart = imageFile?.let { file ->
                val reqFile = file.asRequestBody("image/png".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("userImage", file.name, reqFile)
            }
            val response = api.updateMember(accessToken, csrfToken, memberId, fields, imagePart)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response data"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Failed to update employee: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMemberDetail(memberId: String): Result<MemberDetail> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getMemberViewOne(accessToken, csrfToken, memberId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.member?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty member detail"))
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch employee detail: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}