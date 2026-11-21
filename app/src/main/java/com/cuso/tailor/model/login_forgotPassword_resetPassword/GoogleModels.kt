package com.cuso.tailor.model.login_forgotPassword_resetPassword

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String
)

sealed class GoogleLoginResult {
    data class ExistingUser(val response: GoogleLoginResponse) : GoogleLoginResult()
    data class NewUser(val response: GoogleLoginResponse) : GoogleLoginResult()
    data class Failure(val message: String) : GoogleLoginResult()
}

data class GoogleProfile(
    @SerializedName("email")
    val email: String = "",
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = "",
    @SerializedName("googleId")
    val googleId: String = ""
)

data class GoogleLoginResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("requiresRegistration")
    val requiresRegistration: Boolean = false,
    @SerializedName("googleProfile")
    val googleProfile: GoogleProfile? = null,
    @SerializedName("data")
    val data: GoogleLoginData? = null
)

data class GoogleLoginData(
    @SerializedName("user")
    val user: GoogleAuthUser,
    @SerializedName("tokens")
    val tokens: GoogleAuthTokens
)

data class GoogleAuthUser(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("userId")
    val userId: String = "",
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("profilePicture")
    val profilePicture: String? = null,
    @SerializedName("organizationId")
    val organizationId: GoogleOrganization? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("memberId")
    val memberId: String? = null
)

data class GoogleOrganization(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("industry")
    val industry: String? = null,
    @SerializedName("orgType")
    val orgType: String? = null,
    @SerializedName("organizationPicture")
    val organizationPicture: String? = null,
    @SerializedName("organizationPictureId")
    val organizationPictureId: String? = null,
    @SerializedName("domains")
    val domains: List<String> = emptyList(),
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("mobile")
    val mobile: String? = null,
    @SerializedName("orgSetupComplete")
    val orgSetupComplete: Boolean = false,
    @SerializedName("totalMembers")
    val totalMembers: Int = 0,
    @SerializedName("activeMembers")
    val activeMembers: Int = 0,
    @SerializedName("ownerId")
    val ownerId: String? = null,
    @SerializedName("ownerMemberId")
    val ownerMemberId: String? = null,
    @SerializedName("businessType")
    val businessType: String? = null,
    @SerializedName("segments")
    val segments: List<String> = emptyList(),
    @SerializedName("defaultBranch")
    val defaultBranch: String? = null,
    @SerializedName("branches")
    val branches: List<String> = emptyList(),
    @SerializedName("defaultWarehouse")
    val defaultWarehouse: String? = null,
    @SerializedName("plan")
    val plan: String? = null,
    @SerializedName("activeSubscription")
    val activeSubscription: String? = null,
    @SerializedName("accountManager")
    val accountManager: String? = null,
    @SerializedName("isTaxId")
    val isTaxId: Boolean = false,
    @SerializedName("taxId")
    val taxId: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("isInternalOrganization")
    val isInternalOrganization: Boolean = false,
    @SerializedName("isSeededData")
    val isSeededData: Boolean = false,
    @SerializedName("clientType")
    val clientType: String? = null,
    @SerializedName("billingMode")
    val billingMode: String? = null,
    @SerializedName("settings")
    val settings: OrgSettings? = null,
    @SerializedName("securityPolicy")
    val securityPolicy: OrgSecurityPolicy? = null,
    @SerializedName("subscription")
    val subscription: GoogleSubscription? = null,
    @SerializedName("promotionReservations")
    val promotionReservations: List<PromotionReservation> = emptyList(),
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("__v")
    val __v: Int = 0
)

data class GoogleSubscription(
    @SerializedName("startDate")
    val startDate: String = "",
    @SerializedName("endDate")
    val endDate: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("memberLimit")
    val memberLimit: Int = 0,
    @SerializedName("featuresEnabled")
    val featuresEnabled: List<String> = emptyList()
)

data class OrgSettings(
    @SerializedName("countryName")
    val countryName: String? = null,
    @SerializedName("subdivisionName")
    val subdivisionName: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("state")
    val state: String? = null,
    @SerializedName("portalName")
    val portalName: String? = null,
    @SerializedName("termsAccepted")
    val termsAccepted: Boolean = false,
    @SerializedName("isAiPolicy")
    val isAiPolicy: Boolean = false,
    @SerializedName("companySize")
    val companySize: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("pincode")
    val pincode: String? = null,
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("timezone")
    val timezone: String? = null,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("openingBalanceDate")
    val openingBalanceDate: String? = null,
    @SerializedName("fiscalYearStartMonth")
    val fiscalYearStartMonth: Int = 4,
    @SerializedName("marketingEmails")
    val marketingEmails: Boolean = false,
    @SerializedName("workingDays")
    val workingDays: List<String> = emptyList(),
    @SerializedName("countryCode")
    val countryCode: String? = null,
    @SerializedName("subdivisionCode")
    val subdivisionCode: String? = null
)

data class OrgSecurityPolicy(
    @SerializedName("passwordMaxAgeDays")
    val passwordMaxAgeDays: Int = 90,
    @SerializedName("maxSessionTimeoutMinutes")
    val maxSessionTimeoutMinutes: Int = 60,
    @SerializedName("dataEncryptionEnabled")
    val dataEncryptionEnabled: Boolean = false,
    @SerializedName("maxDesktopSessions")
    val maxDesktopSessions: Int = 1,
    @SerializedName("maxMobileSessions")
    val maxMobileSessions: Int = 1
)

data class PromotionReservation(
    @SerializedName("_id")
    val _id: String? = null,
    @SerializedName("campaignKey")
    val campaignKey: String? = null,
    @SerializedName("planPrice")
    val planPrice: String? = null,
    @SerializedName("razorpayPlanId")
    val razorpayPlanId: String? = null,
    @SerializedName("regularRazorpayPlanId")
    val regularRazorpayPlanId: String? = null,
    @SerializedName("promoAmount")
    val promoAmount: Long = 0L,
    @SerializedName("promoLabel")
    val promoLabel: String? = null,
    @SerializedName("durationCycles")
    val durationCycles: Int = 1,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("expiresAt")
    val expiresAt: String? = null,
    @SerializedName("razorpaySubscriptionId")
    val razorpaySubscriptionId: String? = null,
    @SerializedName("redeemedAt")
    val redeemedAt: String? = null,
    @SerializedName("releasedAt")
    val releasedAt: String? = null
)

data class GoogleAuthTokens(
    @SerializedName("accessToken")
    val accessToken: String = "",
    @SerializedName("refreshToken")
    val refreshToken: String = "",
    @SerializedName("csrfToken")
    val csrfToken: String = "",
    @SerializedName("sessionLoginToken")
    val sessionLoginToken: String = ""
)