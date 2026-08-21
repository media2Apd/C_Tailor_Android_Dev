@file:Suppress("unused")
package com.cuso.mobile.repository

import com.cuso.mobile.model.hr.CreateMemberRequest
import com.cuso.mobile.model.hr.CreatedMemberFullData
import com.cuso.mobile.model.hr.DeleteProfilePictureResponse
import com.cuso.mobile.model.hr.MemberDetail
import com.cuso.mobile.model.hr.MemberListResponse
import com.cuso.mobile.model.hr.RoleItem
import com.cuso.mobile.model.hr.ShiftItem
import com.cuso.mobile.model.hr.UpdateMemberRequest
import com.cuso.mobile.model.hr.UploadProfilePictureResponse
import com.cuso.mobile.network.hr.HrApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HrRepository @Inject constructor(
    private val hrApi: HrApiService,
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
    suspend fun getShifts(): Result<List<ShiftItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = hrApi.getShifts(accessToken, csrfToken)
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

    suspend fun updateMember(memberId: String, request: UpdateMemberRequest): Result<CreatedMemberFullData> {
        return try {
            val (authHeader, csrfToken) = getAuthHeaders()
            val response = hrApi.updateMember(authHeader, csrfToken, memberId, request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to update member"))
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
}