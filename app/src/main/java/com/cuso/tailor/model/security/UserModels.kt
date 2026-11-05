package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName
//
///**
// * Root response for GET /api/users/view-all
// */
//data class UserListResponse(
//    @SerializedName("success") val success: Boolean = false,
//    @SerializedName("total") val total: Int = 0,
//    @SerializedName("page") val page: Int = 1,
//    @SerializedName("limit") val limit: Int = 10,
//    @SerializedName("totalPages") val totalPages: Int = 1,
//    @SerializedName("data") val data: List<UserManagementItemDto> = emptyList(),
//    @SerializedName("message") val message: String? = null
//)
//
///**
// * User Member item schema from /api/users/view-all
// */
//data class UserManagementItemDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("userId") val userAccount: UserAccountDetailsDto? = null,
//    @SerializedName("organizationId") val organizationId: String? = null,
//    @SerializedName("role") val role: String = "",
//    @SerializedName("branchId") val branch: UserBranchRefDto? = null,
//    @SerializedName("departmentId") val department: UserDepartmentRefDto? = null,
//    @SerializedName("designationId") val designationId: String? = null,
//    @SerializedName("shiftId") val shiftId: String? = null,
//    @SerializedName("firstName") val firstName: String = "",
//    @SerializedName("lastName") val lastName: String = "",
//    @SerializedName("profilePicture") val profilePicture: String? = null,
//    @SerializedName("employmentType") val employmentType: String? = null,
//    @SerializedName("status") val status: String = "active",
//    @SerializedName("loginEnabled") val loginEnabled: Boolean = false,
//    @SerializedName("memberId") val memberId: String? = null,
//    @SerializedName("joinedAt") val joinedAt: String? = null,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("permissions") val permissions: Map<String, Any>? = null,
//    @SerializedName("customPermissions") val customPermissions: Map<String, Any>? = null
//) {
//    val fullName: String
//        get() = "$firstName $lastName".trim().ifBlank { userAccount?.email ?: "Unknown User" }
//
//    val email: String
//        get() = userAccount?.email.orEmpty()
//
//    val mobile: String
//        get() = userAccount?.mobile.orEmpty()
//
//    val riskLevel: String
//        get() = userAccount?.riskLevel?.replaceFirstChar { it.uppercase() } ?: "Low"
//
//    val branchName: String
//        get() = branch?.name ?: "No Branch"
//
//    val roleDisplayName: String
//        get() = role.replace("-", " ")
//            .split(" ")
//            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
//}
//
//data class UserAccountDetailsDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("email") val email: String = "",
//    @SerializedName("mobile") val mobile: String? = null,
//    @SerializedName("lastLoginAt") val lastLoginAt: String? = null,
//    @SerializedName("riskLevel") val riskLevel: String? = "low",
//    @SerializedName("twoFA") val twoFA: UserTwoFADto? = null
//)
//
//data class UserTwoFADto(
//    @SerializedName("enabled") val enabled: Boolean = false
//)
//
//data class UserBranchRefDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("name") val name: String = ""
//)
//
//data class UserDepartmentRefDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("name") val name: String = ""
//)
//
///**
// * Request payload structure matching the screenshot for POST /api/users/create
// */
//data class CreateUserRequest(
//    @SerializedName("firstName") val firstName: String,
//    @SerializedName("lastName") val lastName: String,
//    @SerializedName("email") val email: String,
//    @SerializedName("mobile") val mobile: String? = null,
//    @SerializedName("password") val password: String,
//    @SerializedName("role") val role: String = "custom",
//    @SerializedName("customRoleId") val customRoleId: String,
//    @SerializedName("branchId") val branchId: String? = null,
//    @SerializedName("allowedBranches") val allowedBranches: List<String> = emptyList(),
//    @SerializedName("departmentId") val departmentId: String? = null,
//    @SerializedName("designationId") val designationId: String? = null,
//    @SerializedName("reportingTo") val reportingTo: String? = null,
//    @SerializedName("employeeCode") val employeeCode: String? = null,
//    @SerializedName("customPermissions") val customPermissions: Map<String, Any> = emptyMap(),
//    @SerializedName("require2FA") val require2FA: Boolean = false,
//    @SerializedName("accountStatus") val accountStatus: String = "active",
//    @SerializedName("sendSetupEmail") val sendSetupEmail: Boolean = true
//)
//
///**
// * Response schema for POST /api/users/create
// */
//data class CreateUserApiResponse(
//    @SerializedName("success") val success: Boolean = false,
//    @SerializedName("data") val data: CreateUserData? = null,
//    @SerializedName("message") val message: String? = null
//)
//
//data class CreateUserData(
//    @SerializedName("user") val user: CreatedUserAccountDto? = null,
//    @SerializedName("membership") val membership: CreatedMembershipDto? = null
//)
//
//data class CreatedUserAccountDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("email") val email: String = "",
//    @SerializedName("mobile") val mobile: String? = null,
//    @SerializedName("isActive") val isActive: Boolean = true,
//    @SerializedName("riskLevel") val riskLevel: String? = "low",
//    @SerializedName("currentOrganizationId") val currentOrganizationId: String? = null
//)
//
//data class CreatedMembershipDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("memberId") val memberId: String = "",
//    @SerializedName("role") val role: String = "",
//    @SerializedName("employeeCode") val employeeCode: String? = null
//)











/**
 * Root response for GET /api/users/view-all
 */
data class UserListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("data") val data: List<UserManagementItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

/**
 * User Member item schema from /api/users/view-all
 */
data class UserManagementItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("userId") val userAccount: UserAccountDetailsDto? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("role") val role: String = "",
    @SerializedName("customRoleId") val customRole: UserCustomRoleRefDto? = null,
    @SerializedName("branchId") val branch: UserBranchRefDto? = null,
    @SerializedName("departmentId") val department: UserDepartmentRefDto? = null,
    // FIX: designationId is a nested Object or null in backend, NOT a String!
    @SerializedName("designationId") val designation: UserDesignationRefDto? = null,
    @SerializedName("shiftId") val shiftId: String? = null,
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("profilePicture") val profilePicture: String? = null,
    @SerializedName("employmentType") val employmentType: String? = null,
    @SerializedName("status") val status: String = "active",
    @SerializedName("loginEnabled") val loginEnabled: Boolean = false,
    @SerializedName("memberId") val memberId: String? = null,
    @SerializedName("joinedAt") val joinedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("permissions") val permissions: Map<String, Any>? = null,
    @SerializedName("customPermissions") val customPermissions: Map<String, Any>? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim().ifBlank { userAccount?.email ?: "Unknown User" }

    val email: String
        get() = userAccount?.email.orEmpty()

    val mobile: String
        get() = userAccount?.mobile.orEmpty()

    val riskLevel: String
        get() = userAccount?.riskLevel?.replaceFirstChar { it.uppercase() } ?: "Low"

    val branchName: String
        get() = branch?.name ?: "No Branch"

    val roleDisplayName: String
        get() = customRole?.name ?: role.replace("-", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            .ifBlank { "--" }
}

data class UserAccountDetailsDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("lastLoginAt") val lastLoginAt: String? = null,
    @SerializedName("riskLevel") val riskLevel: String? = "low",
    @SerializedName("twoFA") val twoFA: UserTwoFADto? = null
)

data class UserTwoFADto(
    @SerializedName("enabled") val enabled: Boolean = false
)

data class UserBranchRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class UserDepartmentRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

// Added to handle populated designation object
data class UserDesignationRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

// Added to handle custom role object
data class UserCustomRoleRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

/**
 * Request payload structure matching the screenshot for POST /api/users/create
 */
data class CreateUserRequest(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "custom",
    @SerializedName("customRoleId") val customRoleId: String,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("allowedBranches") val allowedBranches: List<String> = emptyList(),
    @SerializedName("departmentId") val departmentId: String? = null,
    @SerializedName("designationId") val designationId: String? = null,
    @SerializedName("reportingTo") val reportingTo: String? = null,
    @SerializedName("employeeCode") val employeeCode: String? = null,
    @SerializedName("customPermissions") val customPermissions: Map<String, Any> = emptyMap(),
    @SerializedName("require2FA") val require2FA: Boolean = false,
    @SerializedName("accountStatus") val accountStatus: String = "active",
    @SerializedName("sendSetupEmail") val sendSetupEmail: Boolean = true
)

/**
 * Response schema for POST /api/users/create
 */
data class CreateUserApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: CreateUserData? = null,
    @SerializedName("message") val message: String? = null
)

data class CreateUserData(
    @SerializedName("user") val user: CreatedUserAccountDto? = null,
    @SerializedName("membership") val membership: CreatedMembershipDto? = null
)

data class CreatedUserAccountDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("riskLevel") val riskLevel: String? = "low",
    @SerializedName("currentOrganizationId") val currentOrganizationId: String? = null
)

data class CreatedMembershipDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("memberId") val memberId: String = "",
    @SerializedName("role") val role: String = "",
    @SerializedName("employeeCode") val employeeCode: String? = null
)