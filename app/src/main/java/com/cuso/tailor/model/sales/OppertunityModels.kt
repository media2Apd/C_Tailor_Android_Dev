@file:Suppress("unused")
package com.cuso.tailor.model.sales

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName


data class OpportunityPipelineItem(
    val id: String,
    val stage: String,
    val customerName: String,
    val description: String,
    val amount: String,
    val salesperson: String,
    val timeAgo: String,
    val badgeLabel: String = "-"
)

data class OpportunityListItem(
    val id: String,
    val code: String,
    val title: String,
    val customerName: String,
    val category: String,
    val closingDate: String,
    val estimatedValue: String,
    val status: String = "-"
)

data class ProductBreakdownItem(
    val name: String,
    val tag: String,
    val quantity: Int,
    val unitPrice: String,
    val totalAmount: String
)

data class DealNoteItem(
    val author: String,
    val timeAgo: String,
    val message: String
)

data class ActivityTimelineItem(
    val title: String,
    val timeAgo: String,
    val description: String,
    val dotColor: Color
)

data class OpportunityStagesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<OpportunityStageItemDto> = emptyList()
)

data class OpportunityStageItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("percentage") val percentage: Int = 0,
    @SerializedName("isSystemDefined") val isSystemDefined: Boolean = false,
    @SerializedName("displayOrder") val displayOrder: Int = 0
)


// ── 1. VIEW ALL MODEL ──
data class OpportunityListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<OpportunityDto> = emptyList(),
    @SerializedName("pagination") val pagination: OpportunityPaginationDto? = null
)

data class OpportunityPaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

// ── 2. VIEW ONE MODEL ──
data class OpportunityDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: OpportunityDto? = null
)

// ── 3. CREATE OPPORTUNITY MODELS ──
data class CreateOpportunityRequest(
    @SerializedName("name") val name: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("leadId") val leadId: String? = null,
    @SerializedName("stageId") val stageId: String,
    @SerializedName("salespersonId") val salespersonId: String,
    @SerializedName("estimatedValue") val estimatedValue: Double,
    @SerializedName("expectedClosingDate") val expectedClosingDate: String,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("garmentSpecifications") val garmentSpecifications: List<CreateGarmentSpecPayload>
)

data class CreateGarmentSpecPayload(
    @SerializedName("garmentId") val garmentId: String,
    @SerializedName("garmentCategoryId") val garmentCategoryId: String,
    @SerializedName("quantity") val quantity: Int
)

data class CreateOpportunityResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OpportunityDto? = null
)

// ── 4. DELETE ONE MODEL ──
data class DeleteOpportunityResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Any? = null
)

// ── 5. ADD LOG ACTIVITY MODELS ──
data class AddOpportunityActivityRequest(
    @SerializedName("opportunityId") val opportunityId: String,
    @SerializedName("activityType") val activityType: String = "Message",
    @SerializedName("duration") val duration: Int = 15,
    @SerializedName("notes") val notes: String
)

data class AddOpportunityActivityResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OpportunityActivityDto? = null
)

// ── 6. LOG HISTORY MODEL ──
data class OpportunityActivityHistoryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<OpportunityActivityDto> = emptyList()
)

data class OpportunityActivityDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("opportunityId") val opportunityId: String? = null,
    @SerializedName("activityType") val activityType: String? = null,
    @SerializedName("duration") val duration: Int? = 0,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("createdBy") val createdBy: Any? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("stageTransition") val stageTransition: StageTransitionDto? = null
)

data class StageTransitionDto(
    @SerializedName("fromStageId") val fromStageId: StageMiniDto? = null,
    @SerializedName("toStageId") val toStageId: StageMiniDto? = null
)

// ── SHARED OPPORTUNITY OBJECT ──
data class OpportunityDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("opportunityCode") val opportunityCode: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("leadId") val lead: Any? = null,
    @SerializedName("customerId") val customer: CustomerMiniDto? = null,
    @SerializedName("branchId") val branch: BranchMiniDto? = null,
    @SerializedName("garmentSpecifications") val garmentSpecifications: List<GarmentSpecDto>? = null,
    @SerializedName("estimatedValue") val estimatedValue: Double? = null,
    @SerializedName("currency") val currency: String? = "INR",
    @SerializedName("stageId") val stage: StageMiniDto? = null,
    @SerializedName("salespersonId") val salesperson: SalespersonMiniDto? = null,
    @SerializedName("expectedClosingDate") val expectedClosingDate: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("status") val status: String? = "Active",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class BranchMiniDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null
)

data class CustomerMiniDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("email") val email: String? = null
)

data class GarmentSpecDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("garmentId") val garment: GarmentMiniDto? = null,
    @SerializedName("garmentCategoryId") val garmentCategory: GarmentCategoryMiniDto? = null,
    @SerializedName("quantity") val quantity: Int = 1
)

data class GarmentMiniDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("code") val code: String? = null
)

data class GarmentCategoryMiniDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("sku") val sku: String? = null
)

data class StageMiniDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("percentage") val percentage: Int? = null,
    @SerializedName("displayOrder") val displayOrder: Int? = null
)

data class SalespersonMiniDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)