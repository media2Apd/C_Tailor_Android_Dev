package com.cuso.tailor.model.permission

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for GET /api/plans/limits
 */
data class PlanLimitsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: PlanLimitsData? = null,
    @SerializedName("message")
    val message: String? = null
)

data class PlanLimitsData(
    @SerializedName("activePlanCode")
    val activePlanCode: String? = null,
    @SerializedName("hasActiveSubscription")
    val hasActiveSubscription: Boolean = false,
    @SerializedName("isTrialExpired")
    val isTrialExpired: Boolean = false,
    @SerializedName("hasUsedTrial")
    val hasUsedTrial: Boolean = false,
    @SerializedName("lockReason")
    val lockReason: String? = null,
    @SerializedName("subscription")
    val subscription: PlanSubscriptionDto? = null,
    @SerializedName("limits")
    val limits: PlanLimitsDto? = null,
    @SerializedName("unlockedModules")
    val unlockedModules: List<String> = emptyList(),
    @SerializedName("usage")
    val usage: PlanUsageDto? = null
)

data class PlanSubscriptionDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("planName")
    val planName: String? = null,
    @SerializedName("planCode")
    val planCode: String? = null,
    @SerializedName("planPriceId")
    val planPriceId: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("isTrial")
    val isTrial: Boolean = false,
    @SerializedName("trialEndDate")
    val trialEndDate: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("currentPeriodEnd")
    val currentPeriodEnd: String? = null,
    @SerializedName("billingPeriod")
    val billingPeriod: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("gatewayUsed")
    val gatewayUsed: String? = null,
    @SerializedName("trial")
    val trial: PlanTrialDto? = null
)

/**
 * Trial progress details returned inside subscription.trial from GET /api/plans/limits
 */
data class PlanTrialDto(
    @SerializedName("hasPlan")
    val hasPlan: Boolean = false,
    @SerializedName("isTrial")
    val isTrial: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("daysRemaining")
    val daysRemaining: Int = 0,
    @SerializedName("headerText")
    val headerText: String? = null,
    @SerializedName("progressText")
    val progressText: String? = null,
    @SerializedName("trialEndsFormatted")
    val trialEndsFormatted: String? = null,
    @SerializedName("progressPercentage")
    val progressPercentage: Float = 0f,
    @SerializedName("isExpired")
    val isExpired: Boolean = false
)

data class PlanLimitsDto(
    @SerializedName("storageLimitBytes")
    val storageLimitBytes: Long = 0L,
    @SerializedName("branchLimit")
    val branchLimit: Int = 0,
    @SerializedName("employeeLimit")
    val employeeLimit: Int = 0,
    @SerializedName("userLimit")
    val userLimit: Int = 0,
    @SerializedName("orderLimit")
    val orderLimit: Int = 0
)

data class PlanUsageDto(
    @SerializedName("storageBytes")
    val storageBytes: Long = 0L,
    @SerializedName("trackedStorageAssets")
    val trackedStorageAssets: Int = 0,
    @SerializedName("storageTrackingStatus")
    val storageTrackingStatus: String? = null,
    @SerializedName("branches")
    val branches: Int = 0,
    @SerializedName("employees")
    val employees: Int = 0,
    @SerializedName("users")
    val users: Int = 0,
    @SerializedName("monthlyOrders")
    val monthlyOrders: Int = 0
)

/**
 * UI model representing a module section and its enabled sub-module features
 * extracted dynamically from /api/common/plan-setups/customer/my-plan-modules.
 */
data class PlanFeatureModuleGroup(
    val moduleKey: String,
    val moduleTitle: String,
    val features: List<String>
)