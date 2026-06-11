package com.cuso.mobile.model


data class LoginData(
    val user: User,
    val tokens: Tokens
)
data class User(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePicture: String?,
    val organizationId: Organization,
    val role: String,
    val memberId: String
)
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
    val branches: List<String>,
    val isTaxId: Boolean,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    val __v: Int,
    val defaultBranch: String,
    val ownerId: String,
    val ownerMemberId: String,
    val businessType: String,
    val taxId: String
)
data class Subscription(
    val startDate: String,
    val endDate: String,
    val status: String,
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
    val companySize: String,
    val timezone: String,
    val currency: String,
    val language: String,
    val address: String,
    val city: String,
    val pincode: String
)
data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val csrfToken: String,
    val sessionLoginToken: String,
    val orgToken: String
)