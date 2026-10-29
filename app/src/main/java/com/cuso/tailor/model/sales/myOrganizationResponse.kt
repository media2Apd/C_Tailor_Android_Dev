package com.cuso.tailor.model.sales

import com.cuso.tailor.model.login_forgotPassword_resetPassword.Branch
import com.cuso.tailor.model.login_forgotPassword_resetPassword.BranchAddress
import com.cuso.tailor.model.login_forgotPassword_resetPassword.DefaultBranch
import com.cuso.tailor.model.login_forgotPassword_resetPassword.Organization
import com.cuso.tailor.model.login_forgotPassword_resetPassword.OrganizationDetails
import com.cuso.tailor.model.login_forgotPassword_resetPassword.Plan
import com.cuso.tailor.model.login_forgotPassword_resetPassword.Settings
import com.cuso.tailor.model.login_forgotPassword_resetPassword.Subscription
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

// =====================================================================
// RAW (lenient) models: used ONLY by Gson to parse /my-organization.
// Every field is nullable with a default, so missing/extra/null fields never crash.
// =====================================================================

data class myOrganizationResponse(
    val success: Boolean? = null,
    val data: OrgApiData? = null
)

data class OrgApiData(
    val organization: OrgApiDetails? = null,
    val stats: OrgApiStats? = null
)

data class OrgApiStats(
    val totalBranches: Int? = null,
    val totalDepartments: Int? = null,
    val totalEmployees: Int? = null
)

data class OrgApiDetails(
    val subscription: OrgApiSubscription? = null,
    val settings: OrgApiSettings? = null,
    val isInternalOrganization: Boolean? = null,
    val _id: String? = null,
    val businessId: String? = null,
    val name: String? = null,
    val industry: String? = null,
    val orgType: String? = null,
    val organizationPicture: String? = null,
    val organizationPictureId: String? = null,
    val domains: List<String>? = null,
    val email: String? = null,
    val mobile: String? = null,
    val orgSetupComplete: Boolean? = null,
    val totalMembers: Int? = null,
    val activeMembers: Int? = null,
    val segments: List<String>? = null,
    // JsonElement: can be an id string, a full object, null, or missing
    val branches: List<JsonElement>? = null,
    val defaultBranch: JsonElement? = null,
    val plan: JsonElement? = null,
    val isTaxId: Boolean? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val slug: String? = null,
    val __v: Int? = null,
    val ownerId: String? = null,
    val ownerMemberId: String? = null,
    val businessType: String? = null,
    val taxId: String? = null
)

data class OrgApiSubscription(
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String? = null,
    val memberLimit: Int? = null,
    val featuresEnabled: List<String>? = null
)

// add: import com.google.gson.annotations.SerializedName
data class OrgApiSettings(
    @SerializedName(value = "country", alternate = ["countryName"])
    val country: String? = null,
    @SerializedName(value = "state", alternate = ["subdivisionName"])
    val state: String? = null,
    val portalName: String? = null,
    val termsAccepted: Boolean? = null,
    val marketingEmails: Boolean? = null,
    val workingDays: List<String>? = null,
    val timezone: String? = null,
    val currency: String? = null,
    val language: String? = null,
    val address: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val companySize: String? = null
)

// =====================================================================
// CLEAN (non-null) models: used by the UI. Same shape as the ORIGINAL
// classes, so Branch/Department/CreateItem/OrganizationSetUp compile as before.
// =====================================================================

data class OrganizationDataWrapper(
    val organization: OrganizationDetails,
    val stats: OrgStats?
)

data class OrgStats(
    val totalBranches: Int,
    val totalDepartments: Int,
    val totalEmployees: Int
)

// =====================================================================
// Safe JSON helpers (never throw)
// =====================================================================

private fun JsonObject.str(key: String): String =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString ?: ""

private fun JsonObject.int(key: String, default: Int = 0): Int = try {
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asInt ?: default
} catch (_: Exception) { default }

private fun JsonObject.bool(key: String, default: Boolean = false): Boolean = try {
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asBoolean ?: default
} catch (_: Exception) { default }

// Extracts an id whether the server sends "abc", {"_id":"abc"}, null, or nothing.
fun JsonElement?.toIdOrEmpty(): String = when {
    this == null || this.isJsonNull -> ""
    this.isJsonPrimitive -> this.asString
    this.isJsonObject -> this.asJsonObject.str("_id")
    else -> ""
}

// Plan: only an object can be converted. A plain id string (not populated) -> null.
private fun JsonElement?.toPlanOrNull(): Plan? {
    if (this == null || !this.isJsonObject) return null
    val o = this.asJsonObject
    // If a limit is missing we use Int.MAX_VALUE (= no limit), NOT 0.
    // 0 would wrongly show "Plan Limit Reached" in the Branch/Department screens.
    val noLimit = Int.MAX_VALUE
    return Plan(
        _id = o.str("_id"),
        name = o.str("name"),
        price = o.int("price"),
        categoryLimit = o.int("categoryLimit", noLimit),
        branchLimit = o.int("branchLimit", noLimit),
        departmentLimit = o.int("departmentLimit", noLimit),
        orderLimit = o.int("orderLimit", noLimit),
        employeeLimit = o.int("employeeLimit", noLimit),
        features = o.get("features")?.takeIf { it.isJsonArray }?.asJsonArray?.toList() ?: emptyList(),
        isActive = o.bool("isActive", true),
        createdAt = o.str("createdAt"),
        updatedAt = o.str("updatedAt"),
        __v = o.int("__v")
    )
}

private fun JsonElement?.toDefaultBranch(): DefaultBranch? = when {
    this == null || this.isJsonNull -> null
    this.isJsonPrimitive -> DefaultBranch(_id = this.asString)
    this.isJsonObject -> {
        val o = this.asJsonObject
        DefaultBranch(
            _id = o.str("_id").ifEmpty { null },
            name = o.str("name").ifEmpty { null },
            branchId = o.str("branchId").ifEmpty { null },
            isMainBranch = o.bool("isMainBranch"),
            status = o.str("status").ifEmpty { null },
            slug = o.str("slug").ifEmpty { null }
        )
    }
    else -> null
}

// Branch can come as an id string OR a full object. Both are handled.
private fun JsonElement.toBranchOrNull(): Branch? {
    if (this.isJsonNull) return null
    if (this.isJsonPrimitive) {
        return Branch(
            address = null, _id = this.asString, organizationId = "", branchId = "",
            name = "", contactEmail = "", contactMobile = "", isMainBranch = false,
            status = "", isDeleted = false, createdBy = "", createdAt = "",
            updatedAt = "", slug = "", __v = 0
        )
    }
    if (!this.isJsonObject) return null
    val o = this.asJsonObject
    val addr = o.get("address")?.takeIf { it.isJsonObject }?.asJsonObject
    return Branch(
        address = addr?.let { BranchAddress(country = it.str("country"), state = it.str("state")) },
        _id = o.str("_id"),
        organizationId = o.str("organizationId"),
        branchId = o.str("branchId"),
        name = o.str("name"),
        contactEmail = o.str("contactEmail"),
        contactMobile = o.str("contactMobile"),
        isMainBranch = o.bool("isMainBranch"),
        status = o.str("status"),
        isDeleted = o.bool("isDeleted"),
        createdBy = o.str("createdBy"),
        createdAt = o.str("createdAt"),
        updatedAt = o.str("updatedAt"),
        slug = o.str("slug"),
        __v = o.int("__v")
    )
}

// =====================================================================
// Mappers: raw -> clean
// =====================================================================

private fun OrgApiDetails.toSubscription() = Subscription(
    startDate = subscription?.startDate ?: "",
    endDate = subscription?.endDate ?: "",
    status = subscription?.status ?: "",
    memberLimit = subscription?.memberLimit ?: 0,
    featuresEnabled = subscription?.featuresEnabled ?: emptyList()
)

private fun OrgApiDetails.toSettings() = Settings(
    country = settings?.country ?: "",
    state = settings?.state ?: "",
    portalName = settings?.portalName ?: "",
    termsAccepted = settings?.termsAccepted ?: false,
    marketingEmails = settings?.marketingEmails ?: false,
    workingDays = settings?.workingDays ?: emptyList(),
    timezone = settings?.timezone ?: "",
    currency = settings?.currency ?: "",
    language = settings?.language ?: "",
    address = settings?.address ?: "",
    city = settings?.city ?: "",
    pincode = settings?.pincode ?: "",
    companySize = settings?.companySize ?: ""
)

// For the UI (ProfileViewModel): full object version
fun OrgApiDetails.toOrganizationDetails(): OrganizationDetails = OrganizationDetails(
    subscription = toSubscription(),
    settings = toSettings(),
    isInternalOrganization = isInternalOrganization ?: false,
    _id = _id ?: "",
    businessId = businessId ?: "",
    name = name ?: "",
    industry = industry ?: "",
    orgType = orgType ?: "",
    organizationPicture = organizationPicture,
    organizationPictureId = organizationPictureId,
    domains = domains ?: emptyList(),
    email = email ?: "",
    mobile = mobile ?: "",
    orgSetupComplete = orgSetupComplete ?: false,
    totalMembers = totalMembers ?: 0,
    activeMembers = activeMembers ?: 0,
    segments = segments ?: emptyList(),
    branches = branches.orEmpty().mapNotNull { it.toBranchOrNull() },
    isTaxId = isTaxId ?: false,
    status = status ?: "",
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: "",
    slug = slug ?: "",
    __v = __v ?: 0,
    defaultBranch = defaultBranch.toDefaultBranch(),
    ownerId = ownerId ?: "",
    ownerMemberId = ownerMemberId ?: "",
    businessType = businessType ?: "",
    taxId = taxId ?: "",
    plan = plan.toPlanOrNull()
)

// For login flow (Authenticate.saveOrganizationData): id-only version
fun OrgApiDetails.toOrganization(): Organization = Organization(
    subscription = toSubscription(),
    settings = toSettings(),
    isInternalOrganization = isInternalOrganization ?: false,
    _id = _id ?: "",
    businessId = businessId ?: "",
    name = name ?: "",
    industry = industry ?: "",
    orgType = orgType ?: "",
    organizationPicture = organizationPicture,
    organizationPictureId = organizationPictureId,
    domains = domains ?: emptyList(),
    email = email ?: "",
    mobile = mobile ?: "",
    orgSetupComplete = orgSetupComplete ?: false,
    totalMembers = totalMembers ?: 0,
    activeMembers = activeMembers ?: 0,
    segments = segments ?: emptyList(),
    branches = branches.orEmpty().map { it.toIdOrEmpty() }.filter { it.isNotEmpty() },
    isTaxId = isTaxId ?: false,
    status = status ?: "",
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: "",
    slug = slug ?: "",
    __v = __v ?: 0,
    defaultBranch = defaultBranch.toIdOrEmpty(),
    ownerId = ownerId ?: "",
    ownerMemberId = ownerMemberId ?: "",
    businessType = businessType ?: "",
    taxId = taxId ?: "",
    plan = plan.toIdOrEmpty().ifEmpty { null }
)

// Whole response -> clean UI model. Never returns null:
// even if "data" or "organization" is empty, the UI gets an empty-but-valid object.
fun myOrganizationResponse.toUiData(): OrganizationDataWrapper {
    val raw = data?.organization ?: OrgApiDetails()
    val rawStats = data?.stats
    return OrganizationDataWrapper(
        organization = raw.toOrganizationDetails(),
        stats = rawStats?.let {
            OrgStats(
                totalBranches = it.totalBranches ?: 0,
                totalDepartments = it.totalDepartments ?: 0,
                totalEmployees = it.totalEmployees ?: 0
            )
        }
    )
}