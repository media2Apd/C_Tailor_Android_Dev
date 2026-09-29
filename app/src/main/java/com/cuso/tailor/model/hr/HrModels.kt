@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)

package com.cuso.tailor.model.hr

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

// ═══════════════════════════════════════════════════════════
// ── Roles: GET /api/roles/view-all ──
// ═══════════════════════════════════════════════════════════

data class RoleListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<RoleItem> = emptyList()
)

data class RoleItem(
    val _id: String,
    val name: String,
    val description: String? = null,
    val organizationId: String? = null,
    val isSystemRole: Boolean = false,
    val isDefault: Boolean = false,
    val status: Boolean = true,
    val createdBy: RoleCreatedBy? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class RoleCreatedBy(
    val firstName: String? = null,
    val lastName: String? = null
)

// ═══════════════════════════════════════════════════════════
// ── Members (Employees): GET /api/members/view-all ──
// ═══════════════════════════════════════════════════════════

data class MemberListResponse(
    val success: Boolean,
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1,
    val data: List<MemberItem> = emptyList()
)

data class MemberItem(
    val _id: String,
    val userId: MemberUserRef? = null,
    val organizationId: String? = null,
    val role: String? = null,
    val branchId: MemberBranchRef? = null,
    val workingBranchId: String? = null,
    val departmentId: MemberDepartmentRef? = null,
    val designationId: MemberDesignationRef? = null,
    val shiftId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val profilePictureId: String? = null,
    val hasTemporaryAddress: Boolean = false,
    val employmentType: String? = null,
    val status: String? = null,
    val joinedAt: String? = null,
    val isDeleted: Boolean = false,
    val termsAccepted: Boolean = false,
    val doj: String? = null,
    val dob: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val memberId: String? = null,
    val customRoleId: MemberCustomRoleRefList? = null
)

@JsonAdapter(MemberCustomRoleDeserializer::class)
data class MemberCustomRoleRefList(
    val _id: String? = null,
    val name: String? = null
)

class MemberCustomRoleDeserializer : JsonDeserializer<MemberCustomRoleRefList?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MemberCustomRoleRefList? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                MemberCustomRoleRefList(
                    _id = obj.get("_id")?.asString,
                    name = obj.get("name")?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> MemberCustomRoleRefList(_id = json.asString)
            else -> null
        }
    }
}

data class MemberUserRef(
    val _id: String? = null,
    val email: String? = null,
    val mobile: String? = null
)

@JsonAdapter(MemberBranchDeserializer::class)
data class MemberBranchRef(
    val _id: String? = null,
    val name: String? = null
)

class MemberBranchDeserializer : JsonDeserializer<MemberBranchRef?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MemberBranchRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                MemberBranchRef(
                    _id = obj.get("_id")?.asString,
                    name = obj.get("name")?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> MemberBranchRef(_id = json.asString)
            else -> null
        }
    }
}

@JsonAdapter(MemberDepartmentDeserializer::class)
data class MemberDepartmentRef(
    val _id: String? = null,
    val name: String? = null
)

class MemberDepartmentDeserializer : JsonDeserializer<MemberDepartmentRef?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MemberDepartmentRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                MemberDepartmentRef(
                    _id = obj.get("_id")?.asString,
                    name = obj.get("name")?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> MemberDepartmentRef(_id = json.asString)
            else -> null
        }
    }
}

@JsonAdapter(MemberDesignationDeserializer::class)
data class MemberDesignationRef(
    val _id: String? = null,
    val name: String? = null
)

class MemberDesignationDeserializer : JsonDeserializer<MemberDesignationRef?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MemberDesignationRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                MemberDesignationRef(
                    _id = obj.get("_id")?.asString,
                    name = obj.get("name")?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> MemberDesignationRef(_id = json.asString)
            else -> null
        }
    }
}

// ── Small UI helpers (used by AllEmployeesScreen) ──

fun MemberItem.displayName(): String =
    "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifBlank { "—" }

fun MemberItem.displayInitials(): String {
    val f = firstName?.firstOrNull()?.uppercaseChar()
    val l = lastName?.firstOrNull()?.uppercaseChar()
    return listOfNotNull(f, l).joinToString("").ifBlank { "?" }
}

fun MemberItem.displayRole(): String =
    role?.replaceFirstChar { it.uppercase() } ?: "—"

fun MemberItem.displayStatus(): String =
    status?.replaceFirstChar { it.uppercase() } ?: "—"

// ═══════════════════════════════════════════════════════════
// ── Shifts: GET /api/shifts/view-all ──
// ═══════════════════════════════════════════════════════════

data class ShiftListResponse(
    val success: Boolean,
    val data: List<ShiftItem> = emptyList()
)

data class ShiftItem(
    val _id: String,
    val name: String,
    val shiftId: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val organizationId: String? = null,
    val description: String? = null,
    val status: Boolean = true,
    val isDefault: Boolean = false,
    val customWorkingDays: List<String> = emptyList(),
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

fun ShiftItem.displayTimeRange(): String {
    if (startTime.isNullOrBlank() || endTime.isNullOrBlank()) return "—"
    return "$startTime - $endTime"
}

// ═══════════════════════════════════════════════════════════
// ── Create & Update Request Models (Matching Exact Payload) ──
// ═══════════════════════════════════════════════════════════

data class AddressRequest(
    val flatNo: String? = null,
    val street: String? = null,
    val areaZone: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val countryCode: String? = null,
    val countryName: String? = null,
    val subdivisionCode: String? = null,
    val subdivisionName: String? = null
)

data class EducationRequestItem(
    val instituteName: String,
    val degree: String,
    val specialization: String,
    val startDate: String? = null,
    val completionDate: String? = null,
    val cgpa: String? = null
)

data class WorkExperienceRequestItem(
    val companyName: String,
    val jobTitle: String,
    val employmentType: String? = null,
    val location: String? = null,
    val fromDate: String,
    val toDate: String? = null,
    val jobDescription: String,
    val isRelevant: Boolean
)

data class NomineeRequestItem(
    val name: String,
    val relationship: String,
    val share: String
)

data class CreateMemberRequest(
    val firstName: String,
    val lastName: String,
    val personalMail: String,
    val workMobile: String,
    val aadhaarNo: String,
    val panNo: String,
    val passportNo: String,
    val bloodGroup: String,
    val dob: String,
    val doj: String,
    val gender: String,
    val martialStatus: String,
    val emergencyContactName: String,
    val emergencyContactMobile: String,
    val uanNo: String,
    val esicNumber: String,
    val pfAccountNo: String,
    val payFrequency: String,
    val employmentType: String,
    val workingDistrict: String,
    val branchId: String?,
    val branchName: String?,
    val departmentId: String?,
    val designationId: String?,
    val customRoleId: String?,
    val shiftId: String?,
    val reportingTo: String?,
    val secondaryReportingTo: String?,
    // Bank Details
    val accountHolderName: String,
    val accountNumber: String,
    val bankName: String,
    val ifscCode: String,
    // Address Details
    val permanentAddress: AddressRequest,
    val hasTemporaryAddress: Boolean,
    val temporaryAddress: AddressRequest? = null,
    // Arrays
    val education: List<EducationRequestItem> = emptyList(),
    val workExperience: List<WorkExperienceRequestItem> = emptyList(),
    val pfGratuityNominees: List<NomineeRequestItem> = emptyList()
){
    val email: String get() = personalMail
    val personalEmail: String get() = personalMail
    val personalMobile: String get() = emergencyContactMobile
}

data class UpdateMemberRequest(
    val firstName: String,
    val lastName: String,
    val personalMail: String,
    val workMobile: String,
    val aadhaarNo: String,
    val panNo: String,
    val passportNo: String,
    val bloodGroup: String,
    val dob: String,
    val doj: String,
    val gender: String,
    val martialStatus: String,
    val emergencyContactName: String,
    val emergencyContactMobile: String,
    val uanNo: String,
    val esicNumber: String,
    val pfAccountNo: String,
    val payFrequency: String,
    val employmentType: String,
    val workingDistrict: String,
    val branchId: String?,
    val branchName: String?,
    val departmentId: String?,
    val designationId: String?,
    val customRoleId: String?,
    val shiftId: String?,
    val reportingTo: String?,
    val secondaryReportingTo: String?,
    // Bank Details
    val accountHolderName: String,
    val accountNumber: String,
    val bankName: String,
    val ifscCode: String,
    // Address Details
    val permanentAddress: AddressRequest,
    val hasTemporaryAddress: Boolean,
    val temporaryAddress: AddressRequest? = null,
    // Arrays
    val education: List<EducationRequestItem> = emptyList(),
    val workExperience: List<WorkExperienceRequestItem> = emptyList(),
    val pfGratuityNominees: List<NomineeRequestItem> = emptyList()
)

// ── Responses ──

data class CreateMemberResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CreatedMemberFullData? = null
)

data class EducationResponseItem(
    val instituteName: String? = null,
    val degree: String? = null,
    val specialization: String? = null,
    val completionDate: String? = null,
    val _id: String? = null
)

data class WorkExperienceResponseItem(
    val companyName: String? = null,
    val jobTitle: String? = null,
    val fromDate: String? = null,
    val toDate: String? = null,
    val jobDescription: String? = null,
    val isRelevant: Boolean = false,
    val _id: String? = null
)

data class CreatedMemberFullData(
    val _id: String,
    val userId: String? = null,
    val organizationId: String? = null,
    val role: String? = null,
    val customRoleId: String? = null,
    val branchId: String? = null,
    val departmentId: String? = null,
    val designationId: String? = null,
    val shiftId: String? = null,
    val workingDistrict: String? = null,
    val doj: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val workMobile: String? = null,
    val personalMobile: String? = null,
    val profilePicture: String? = null,
    val profilePictureId: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val martialStatus: String? = null,
    val permanentAddress: AddressRequest? = null,
    val temporaryAddress: AddressRequest? = null,
    val hasTemporaryAddress: Boolean = false,
    val employmentType: String? = null,
    val reportingTo: String? = null,
    val secondaryReportingTo: String? = null,
    val status: String? = null,
    val education: List<EducationResponseItem> = emptyList(),
    val workExperience: List<WorkExperienceResponseItem> = emptyList(),
    val joinedAt: String? = null,
    val isDeleted: Boolean = false,
    val createdBy: String? = null,
    val termsAccepted: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val memberId: String? = null
)

// ═══════════════════════════════════════════════════════════
// ── Member Detail: GET /api/members/{id} ──
// ═══════════════════════════════════════════════════════════

data class MemberDetailResponse(
    val success: Boolean,
    val member: MemberDetail? = null
)

data class OrgSettings(
    val portalName: String? = null
)

data class MemberOrganizationRef(
    val _id: String? = null,
    val businessId: String? = null,
    val name: String? = null,
    val organizationPicture: String? = null,
    val settings: OrgSettings? = null
)

data class MemberCustomRoleRef(
    val _id: String? = null,
    val name: String? = null
)

data class MemberBranchDetailRef(
    val _id: String? = null,
    val name: String? = null
)

data class MemberDepartmentDetailRef(
    val _id: String? = null,
    val name: String? = null
)

data class MemberAddress(
    val flatNo: String? = null,
    val street: String? = null,
    val areaZone: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val countryCode: String? = null,
    val countryName: String? = null,
    val subdivisionCode: String? = null,
    val subdivisionName: String? = null
)

data class MemberEducationDetail(
    val instituteName: String? = null,
    val degree: String? = null,
    val specialization: String? = null,
    val completionDate: String? = null,
    val _id: String? = null
)

data class MemberWorkExperienceDetail(
    val companyName: String? = null,
    val jobTitle: String? = null,
    val fromDate: String? = null,
    val toDate: String? = null,
    val jobDescription: String? = null,
    val isRelevant: Boolean = false,
    val _id: String? = null
)

data class MemberDetail(
    val _id: String,
    val userId: MemberUserRef? = null,
    val organizationId: MemberOrganizationRef? = null,
    val role: String? = null,
    val branchId: MemberBranchDetailRef? = null,
    val workingBranchId: String? = null,
    val departmentId: MemberDepartmentDetailRef? = null,
    val designationId: MemberDesignationRef? = null,
    val shiftId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val workMobile: String? = null,
    val personalMobile: String? = null,
    val profilePicture: String? = null,
    val profilePictureId: String? = null,
    val hasTemporaryAddress: Boolean = false,
    val employmentType: String? = null,
    val status: String? = null,
    val joinedAt: String? = null,
    val isDeleted: Boolean = false,
    val createdBy: String? = null,
    val termsAccepted: Boolean = false,
    val doj: String? = null,
    val dob: String? = null,
    val permanentAddress: MemberAddress? = null,
    val temporaryAddress: MemberAddress? = null,
    val martialStatus: String? = null,
    val gender: String? = null,
    val workingDistrict: String? = null,
    val reportingTo: String? = null,
    val secondaryReportingTo: String? = null,
    val education: List<MemberEducationDetail> = emptyList(),
    val workExperience: List<MemberWorkExperienceDetail> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val memberId: String? = null,
    val customRoleId: MemberCustomRoleRef? = null,
    val permissions: Map<String, Any>? = null,
    val __v: Int? = null
)

data class UploadProfilePictureResponse(
    val message: String?,
    val member: UploadedMemberInfo
)

data class DeleteProfilePictureResponse(
    val message: String?,
    val member: UploadedMemberInfo
)

data class UploadedMemberInfo(
    val _id: String?,
    val profilePicture: String?,
    val profilePictureId: String?
)