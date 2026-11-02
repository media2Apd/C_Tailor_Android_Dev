package com.cuso.tailor.model.it

import com.google.gson.annotations.SerializedName

data class AssetMetricsDto(
    @SerializedName("totalAssets") val totalAssets: Int = 0,
    @SerializedName("assigned") val assigned: Int = 0,
    @SerializedName("available") val available: Int = 0,
    @SerializedName("underMaintenance") val underMaintenance: Int = 0,
    @SerializedName("disposalDue") val disposalDue: Int = 0
)

data class AssetItemDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("assetName") val assetName: String = "",
    @SerializedName("type") val type: String = "Hardware",
    @SerializedName("category") val category: String? = null,
    @SerializedName("assetId") val assetId: String = "",
    @SerializedName("serialOrLicense") val serialOrLicense: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("assignedTo") val assignedTo: String? = null,
    @SerializedName("status") val status: String = "Available",
    @SerializedName("lifecycle") val lifecycle: String? = null,
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("model") val model: String? = null
)

data class AssetPaginationDto(
    @SerializedName("currentPage") val currentPage: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("totalRecords") val totalRecords: Int = 0
)

data class AssetDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("metrics") val metrics: AssetMetricsDto? = null,
    @SerializedName("assets") val assets: List<AssetItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: AssetPaginationDto? = null,
    @SerializedName("message") val message: String? = null
)

data class AssetPurchaseInfoDto(
    @SerializedName("purchaseDate") val purchaseDate: String? = null,
    @SerializedName("cost") val cost: Double? = null,
    @SerializedName("currency") val currency: String? = "INR",
    @SerializedName("vendor") val vendor: String? = null,
    @SerializedName("warrantyUntil") val warrantyUntil: String? = null
)

data class AssetLocationDto(
    @SerializedName("branchName") val branchName: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("locationDetails") val locationDetails: String? = null,
    @SerializedName("branchId") val branchId: String? = null
)

data class AssetAssigneeDto(
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("empId") val empId: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("assignedOn") val assignedOn: String? = null
)

data class AssetDetailDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("assetId") val assetId: String = "",
    @SerializedName("assetName") val assetName: String = "",
    @SerializedName("type") val type: String = "Hardware",
    @SerializedName("category") val category: String? = null,
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("serialOrLicense") val serialOrLicense: String? = null,
    @SerializedName("condition") val condition: String? = "Good",
    @SerializedName("status") val status: String = "Available",
    @SerializedName("lifecycleText") val lifecycleText: String? = null,
    @SerializedName("linkedDeviceId") val linkedDeviceId: String? = null,
    @SerializedName("isMarkedForDisposal") val isMarkedForDisposal: Boolean = false,
    @SerializedName("disposalReason") val disposalReason: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("purchaseInfo") val purchaseInfo: AssetPurchaseInfoDto? = null,
    @SerializedName("location") val location: AssetLocationDto? = null,
    @SerializedName("currentAssignee") val currentAssignee: AssetAssigneeDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class AssetDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: AssetDetailDto? = null,
    @SerializedName("message") val message: String? = null
)

data class RegisterAssetRequest(
    @SerializedName("assetName") val assetName: String,
    @SerializedName("type") val type: String,
    @SerializedName("category") val category: String,
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("manufacturer") val manufacturer: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("condition") val condition: String,
    @SerializedName("cost") val cost: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("department") val department: String?,
    @SerializedName("locationDetails") val locationDetails: String?,
    @SerializedName("purchaseDate") val purchaseDate: String?,
    @SerializedName("branchName") val branchName: String?,
    @SerializedName("vendor") val vendor: String?,
    @SerializedName("warrantyUntil") val warrantyUntil: String?
)

data class RegisterAssetResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("asset") val asset: AssetDetailDto? = null
)

data class MarkDisposalRequest(
    @SerializedName("reason") val reason: String
)

data class MarkDisposalResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("asset") val asset: AssetDetailDto? = null
)

data class AssignAssetRequest(
    @SerializedName("assigneeName") val assigneeName: String,
    @SerializedName("empId") val empId: String?,
    @SerializedName("department") val department: String?,
    @SerializedName("branchName") val branchName: String?,
    @SerializedName("userId") val userId: String?
)

data class AssignAssetResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("asset") val asset: AssetDetailDto? = null
)

data class OrgMemberUserInfoDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("mobile") val mobile: String? = null
)

data class OrgMemberNamedEntityDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null
)

data class OrgMemberDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("userId") val userId: OrgMemberUserInfoDto? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("memberId") val memberId: String? = null,
    @SerializedName("employeeCode") val employeeCode: String? = null,
    @SerializedName("departmentId") val departmentId: OrgMemberNamedEntityDto? = null,
    @SerializedName("branchId") val branchId: OrgMemberNamedEntityDto? = null,
    @SerializedName("status") val status: String? = null
) {
    val fullName: String
        get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifBlank { memberId.orEmpty() }

    val dropdownLabel: String
        get() {
            val code = if (!memberId.isNullOrBlank()) " ($memberId)" else ""
            return "$fullName$code"
        }
}

data class OrgMembersResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("data") val data: List<OrgMemberDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)