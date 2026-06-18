package com.cuso.mobile.model


import com.google.gson.annotations.SerializedName


//existing user response
data class GoogleLoginSuccess(
    val success: Boolean,
    val message: String,
    val data: GoogleLoginData
)

data class GoogleLoginData(
    val user: GoogleUser,
    val tokens: GoogleTokens
)

data class GoogleUser(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePicture: String?,
    val organizationId: GoogleOrganization,
    val role: String,
    val memberId: String
)
data class GoogleOrganization(
    val subscription: GoogleSubscription,
    val settings: GoogleOrgSettings,
    val taxProfile: GoogleTaxProfile,
    val storage: GoogleOrgStorage,
    val isTemplateChoose: Boolean,
    @SerializedName("_id") val id: String,
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
    val branches: List<String>,
    val plan: String,
    val isTaxId: Boolean,
    val status: String,
    val isInternalOrganization: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    @SerializedName("__v") val version: Int,
    val defaultBranch: String,
    val ownerId: String,
    val ownerMemberId: String,
    val businessType: String,
    val taxId: String
)
data class GoogleSubscription(
    val startDate: String,
    val endDate: String,
    val status: String,
    val memberLimit: Int,
    val featuresEnabled: List<String>
)

data class GoogleOrgSettings(
    val country: String,
    val state: String,
    val timezone: String,
    val portalName: String,
    val termsAccepted: Boolean,
    val marketingEmails: Boolean,
    val workingDays: List<String>,
//    val companySize: String,
    val currency: String,
    val language: String,
    val address: String,
    val city: String,
    val pincode: String
)
data class GoogleTaxProfile(
    val taxRegistered: Boolean
)
data class GoogleOrgStorage(
    val used: Int,
    val limit: Int,
    val lastCalculatedAt: String
)
data class GoogleTokens(
    val accessToken: String,
    val refreshToken: String,
    val csrfToken: String,
    val sessionLoginToken: String
)

data class GoogleProfile(
    val email: String,
    val firstName: String,
    val lastName: String,
    val googleId: String
)

data class GoogleLoginNewUser(
    val success: Boolean,
    val requiresRegistration: Boolean,
    val message: String,
    val googleProfile: GoogleProfile
)
