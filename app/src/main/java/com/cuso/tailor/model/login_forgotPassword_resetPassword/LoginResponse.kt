// com/cuso/mobile/model/Organization.kt

package com.cuso.tailor.model.login_forgotPassword_resetPassword

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
// Every field has a default value, so Kotlin generates a no-arg constructor.
// Gson uses it, so a MISSING field in the JSON keeps its default instead of becoming null.
data class Organization(
    val subscription: Subscription = Subscription(),   // login API sends no subscription object
    val settings: Settings = Settings(),
    val isInternalOrganization: Boolean = false,
    val _id: String = "",
    val businessId: String = "",
    val name: String = "",
    val industry: String = "",                          // login API does NOT send this
    val orgType: String = "",
    val organizationPicture: String? = null,
    val organizationPictureId: String? = null,
    val domains: List<String> = emptyList(),
    val email: String = "",
    val mobile: String = "",
    val orgSetupComplete: Boolean = false,
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val segments: List<String> = emptyList(),
    val branches: List<String> = emptyList(),
    val isTaxId: Boolean = false,
    val status: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val slug: String = "",
    val __v: Int = 0,
    val defaultBranch: String = "",
    val ownerId: String = "",
    val ownerMemberId: String = "",
    val businessType: String = "",
    val taxId: String = "",
    val plan: String? = null
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

// All defaults, so the class has a no-arg constructor and can be created as Subscription()
data class Subscription(
    val startDate: String = "",
    val endDate: String = "",
    val status: String = "",
    val memberLimit: Int = 0,
    val featuresEnabled: List<String> = emptyList()
)

data class Settings(
    // Login API sends "countryName" / "subdivisionName"; other APIs may send "country" / "state".
    // "alternate" makes Gson accept both names.
    @SerializedName(value = "country", alternate = ["countryName"])
    val country: String = "",
    @SerializedName(value = "state", alternate = ["subdivisionName"])
    val state: String = "",
    val portalName: String = "",
    val termsAccepted: Boolean = false,
    val marketingEmails: Boolean = false,
    val workingDays: List<String> = emptyList(),
    val timezone: String = "",
    val currency: String = "",
    val language: String = "",
    val address: String = "",
    val city: String = "",
    val pincode: String = "",
    val companySize: String = ""
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val csrfToken: String,
    val sessionLoginToken: String,
    val orgToken: String
)

