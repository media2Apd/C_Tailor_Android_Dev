// com/cuso/mobile/model/Organization.kt

package com.cuso.mobile.model.login_forgotPassword_resetPassword

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LoginData(
    val user: User,
    val tokens: Tokens
)

data class User(
    val id: String = "",
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val profilePicture: String = "",
    val organizationId: Organization,  //   Login API version
    val role: String,
    val memberId: String = ""
)

//   This is the Login API version of Organization (branches = List<String>)
data class Organization(
    val subscription: Subscription,
    val settings: Settings,
    val isInternalOrganization: Boolean,
    val _id: String,
    val businessId: String,
    val name: String,
    val industry: String,
    val orgType: String,
    val organizationPicture: String?,
    val organizationPictureId: String?,
    val domains: List<String>,
    val email: String,
    val mobile: String,
    val orgSetupComplete: Boolean,
    val totalMembers: Int,
    val activeMembers: Int,
    val segments: List<String>,
    val branches: List<String>,  //   Login API returns List<String>
    val isTaxId: Boolean,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    val __v: Int,
    val defaultBranch: String="",
    val ownerId: String,
    val ownerMemberId: String,
    val businessType: String,
    val taxId: String,
    val plan: String? = null  // Login API returns plan as String ID
)

//   This is the Organization API version (branches = List<Branch>)
data class OrganizationDetails(
    val subscription: Subscription,
    val settings: Settings,
    val isInternalOrganization: Boolean,
    val _id: String,
    val businessId: String,
    val name: String,
    val industry: String,
    val orgType: String,
    val organizationPicture: String?,
    val organizationPictureId: String?,
    val domains: List<String>,
    val email: String,
    val mobile: String,
    val orgSetupComplete: Boolean,
    val totalMembers: Int,
    val activeMembers: Int,
    val segments: List<String>,
    val branches: List<Branch>,  //   Organization API returns List<Branch>
    val isTaxId: Boolean,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    val __v: Int,
    val defaultBranch: DefaultBranch?=null,
    val ownerId: String,
    val ownerMemberId: String,
    val businessType: String,
    val taxId: String,
    val plan: Plan? = null  // Organization API returns Plan object
)

data class DefaultBranch(
    val _id: String? = null,
    val name: String? = null,
    val branchId: String? = null,
    val isMainBranch: Boolean? = null,
    val status: String? = null,
    val slug: String? = null
)

//   Branch data class (for Organization API)
data class Branch(
    val address: BranchAddress?,
    val _id: String,
    val organizationId: String,
    val branchId: String,
    val name: String,
    val contactEmail: String,
    val contactMobile: String,
    val isMainBranch: Boolean,
    val status: String,
    val isDeleted: Boolean,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    val __v: Int
)

//   BranchAddress data class
data class BranchAddress(
    val country: String,
    val state: String
)

//   Plan data class (for Organization API)
data class Plan(
    val _id: String,
    val name: String,
    val price: Int,
    val categoryLimit: Int,
    val branchLimit: Int,
    val departmentLimit: Int,
    val orderLimit: Int,
    val employeeLimit: Int,
    @SerializedName("features") val features: List<JsonElement>? = emptyList(),
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

// ... rest of your data classes (Subscription, Settings, Tokens) remain the same

data class Subscription(
    val startDate: String = "",
    val endDate: String = "",
    val status: String = "",
    val memberLimit: Int,
    val featuresEnabled: List<String>
)

data class Settings(
    val country: String,
    val state: String,
    val portalName: String,
    val termsAccepted: Boolean,
    val marketingEmails: Boolean,
    val workingDays: List<String>,
    val timezone: String,
    val currency: String,
    val language: String,
    val address: String,
    val city: String,
    val pincode: String,
    val companySize: String = "")

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val csrfToken: String,
    val sessionLoginToken: String,
    val orgToken: String
)

