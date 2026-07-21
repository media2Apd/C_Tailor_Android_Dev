package com.cuso.mobile.model.hr

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
    val role: String? = null,                    // "owner" | "admin" | ...
    val branchId: MemberBranchRef? = null,
    val workingBranchId: String? = null,
    val departmentId: MemberDepartmentRef? = null,
    val designationId: String? = null,           // ✅ FIXED — API returns a plain id string, not {_id, name}
    val shiftId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val profilePictureId: String? = null,
    val hasTemporaryAddress: Boolean = false,
    val employmentType: String? = null,           // "full-time" | ...
    val status: String? = null,                   // "active" | "inactive"
    val joinedAt: String? = null,
    val isDeleted: Boolean = false,
    val termsAccepted: Boolean = false,
    val doj: String? = null,
    val dob: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val memberId: String? = null,                 // e.g. "CUS-001"
    val customRoleId: MemberCustomRoleRefList? = null
)

// ✅ NEW — matches the {_id, name} shape the list API actually returns for customRoleId
data class MemberCustomRoleRefList(
    val _id: String? = null,
    val name: String? = null
)

data class MemberUserRef(
    val _id: String? = null,
    val email: String? = null,
    val mobile: String? = null
)

data class MemberBranchRef(
    val _id: String? = null,
    val name: String? = null
)

data class MemberDepartmentRef(
    val _id: String? = null,
    val name: String? = null
)

data class MemberDesignationRef(
    val _id: String? = null,
    val name: String? = null
)

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
    val shiftId: String? = null,          // e.g. "SHIFT-001"
    val startTime: String? = null,         // "09:00"
    val endTime: String? = null,           // "18:00"
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

// ── Small UI helper ──
fun ShiftItem.displayTimeRange(): String {
    if (startTime.isNullOrBlank() || endTime.isNullOrBlank()) return "—"
    return "$startTime - $endTime"
}



// ═══════════════════════════════════════════════════════════
// ── Create Member: POST /api/members/create ──
// ═══════════════════════════════════════════════════════════

data class TemporaryAddressRequest(
    val country: String,
    val state: String,
    val city: String,
    val street: String,
    val postalCode: String
)

data class EducationRequest(
    val instituteName: String,
    val degree: String,
    val specialization: String,
    val completionDate: String   // ISO format e.g. "2026-07-22T00:00:00.000Z"
)

data class WorkExperienceRequest(
    val companyName: String,
    val jobTitle: String,
    val fromDate: String,
    val toDate: String,
    val jobDescription: String,
    val isCurrentRole: Boolean
)




data class CreatedMemberData(
    val _id: String,
    val memberId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val doj: String? = null,
    val status: String? = null,
    val createdAt: String? = null
)

// ═══════════════════════════════════════════════════════════
// ── Create Member: POST /api/members/create ──
// ═══════════════════════════════════════════════════════════

data class AddressRequest(
    val country: String,
    val state: String,
    val city: String,
    val street: String,
    val postalCode: String
)

data class EducationRequestItem(
    val instituteName: String,
    val degree: String,
    val specialization: String,
    val completionDate: String   // "yyyy-MM-dd"
)

data class WorkExperienceRequestItem(
    val companyName: String,
    val jobTitle: String,
    val fromDate: String,        // "yyyy-MM-dd"
    val jobDescription: String,
    val isRelevant: Boolean      // backend field name — not "isCurrentRole"
    // NOTE: backend does not accept "toDate" for work experience
)

data class CreateMemberRequest(
    val firstName: String,
    val lastName: String,
    val email: String,                    // work email
    val personalEmail: String,
    val personalMobile: String,
    val workMobile: String,
    val dob: String,                      // "yyyy-MM-dd"
    val gender: String,                   // "male" | "female" | "other"
    val martialStatus: String,            // backend spelling (typo preserved intentionally)
    val doj: String,                      // "yyyy-MM-dd"
    val branchId: String?,
    val departmentId: String?,
    val designationId: String?,
    val customRoleId: String?,
    val shiftId: String?,
    val workingDistrict: String?,
    val employmentType: String,           // "full-time" | "part-time" | "contract"
    val reportingTo: String?,
    val secondaryReportingTo: String?,
    val permanentAddress: AddressRequest,
    val hasTemporaryAddress: Boolean,
    val temporaryAddress: AddressRequest? = null,
    val education: List<EducationRequestItem> = emptyList(),
    val workExperience: List<WorkExperienceRequestItem> = emptyList()
)
// ── Response ──

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



//--------------------------------------------------------------------------------------------
//---------VIEW EMPLOYEE----------------------------------------------------------------------
//--------------------------------------------------------------------------------------------
// ═══════════════════════════════════════════════════════════
// ── Member Detail: GET /api/members/{id}  (VIEW/EDIT prefill) ──
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
    val country: String? = null,
    val state: String? = null,
    val city: String? = null,
    val street: String? = null,
    val postalCode: String? = null
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
    val jobDescription: String? = null,
    val isRelevant: Boolean = false,
    val _id: String? = null
)

data class MemberDetail(
    val _id: String,
    val userId: String? = null,
    val organizationId: MemberOrganizationRef? = null,
    val role: String? = null,
    val customRoleId: MemberCustomRoleRef? = null,
    val branchId: MemberBranchDetailRef? = null,
    val departmentId: MemberDepartmentDetailRef? = null,
    val designationId: String? = null,      // NOTE: plain id string, not an object, per this API
    val shiftId: String? = null,            // NOTE: plain id string, not an object, per this API
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
    val permanentAddress: MemberAddress? = null,
    val temporaryAddress: MemberAddress? = null,
    val hasTemporaryAddress: Boolean = false,
    val employmentType: String? = null,
    val reportingTo: String? = null,
    val secondaryReportingTo: String? = null,
    val status: String? = null,
    val education: List<MemberEducationDetail> = emptyList(),
    val workExperience: List<MemberWorkExperienceDetail> = emptyList(),
    val joinedAt: String? = null,
    val isDeleted: Boolean = false,
    val createdBy: String? = null,
    val termsAccepted: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val memberId: String? = null
)