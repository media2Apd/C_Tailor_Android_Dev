package com.cuso.tailor.model.it

import com.google.gson.annotations.SerializedName

// --- List & Dashboard DTOs ---

data class SlaAnalyticsDto(
    @SerializedName("onTrackPercent") val onTrackPercent: String = "0%",
    @SerializedName("atRiskPercent") val atRiskPercent: String = "0%",
    @SerializedName("breachedPercent") val breachedPercent: String = "0%"
)

data class TicketMetricsDto(
    @SerializedName("totalTickets") val totalTickets: Int = 0,
    @SerializedName("open") val open: Int = 0,
    @SerializedName("inProgress") val inProgress: Int = 0,
    @SerializedName("resolved") val resolved: Int = 0,
    @SerializedName("closed") val closed: Int = 0,
    @SerializedName("slaBreached") val slaBreached: Int = 0,
    @SerializedName("slaAtRisk") val slaAtRisk: Int = 0,
    @SerializedName("slaOnTrack") val slaOnTrack: Int = 0,
    @SerializedName("slaAnalytics") val slaAnalytics: SlaAnalyticsDto? = null
)

data class TicketItemDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("ticketId") val ticketId: String = "",
    @SerializedName("subject") val subject: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("requester") val requester: String = "",
    @SerializedName("branch") val branch: String = "",
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("assignee") val assignee: String = "Unassigned",
    @SerializedName("status") val status: String = "Open",
    @SerializedName("slaStatus") val slaStatus: String? = null,
    @SerializedName("slaUpdateText") val slaUpdateText: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class TicketPaginationDto(
    @SerializedName("currentPage") val currentPage: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("totalRecords") val totalRecords: Int = 0
)

data class TicketDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("metrics") val metrics: TicketMetricsDto? = null,
    @SerializedName("tickets") val tickets: List<TicketItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: TicketPaginationDto? = null,
    @SerializedName("message") val message: String? = null
)

// --- Ticket Detail DTOs ---

data class TicketRequesterDto(
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("department") val department: String? = null
)

data class TicketBranchDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("branchId") val branchId: String? = null
)

data class TicketAssigneeDto(
    @SerializedName("team") val team: String? = null,
    @SerializedName("assignedUserId") val assignedUserId: String? = null,
    @SerializedName("assignedUserName") val assignedUserName: String? = null
)

data class TicketRelatedEntityDto(
    @SerializedName("deviceCode") val deviceCode: String? = null,
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("assetCode") val assetCode: String? = null,
    @SerializedName("assetId") val assetId: String? = null,
    @SerializedName("applicationName") val applicationName: String? = null
)

data class TicketSlaDto(
    @SerializedName("targetResolutionTime") val targetResolutionTime: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("badgeText") val badgeText: String? = null,
    @SerializedName("firstResponseTime") val firstResponseTime: String? = null,
    @SerializedName("actualResolutionTime") val actualResolutionTime: String? = null
)

data class TicketSlaLiveDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("badgeText") val badgeText: String? = null,
    @SerializedName("createdTime") val createdTime: String? = null,
    @SerializedName("firstResponseTime") val firstResponseTime: String? = null,
    @SerializedName("currentTime") val currentTime: String? = null,
    @SerializedName("targetTime") val targetTime: String? = null
)

data class TicketConversationMessageDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("senderId") val senderId: String? = null,
    @SerializedName("senderName") val senderName: String? = null,
    @SerializedName("senderRole") val senderRole: String? = null,
    @SerializedName("message") val message: String = "",
    @SerializedName("isInternalNote") val isInternalNote: Boolean = false,
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null
)

data class TicketHistoryLogDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("action") val action: String = "",
    @SerializedName("performedBy") val performedBy: String = "",
    @SerializedName("details") val details: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class TicketDetailDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("ticketId") val ticketId: String = "",
    @SerializedName("subject") val subject: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("status") val status: String = "Open",
    @SerializedName("description") val description: String = "",
    @SerializedName("requester") val requester: TicketRequesterDto? = null,
    @SerializedName("branch") val branch: TicketBranchDto? = null,
    @SerializedName("assignee") val assignee: TicketAssigneeDto? = null,
    @SerializedName("relatedEntity") val relatedEntity: TicketRelatedEntityDto? = null,
    @SerializedName("sla") val sla: TicketSlaDto? = null,
    @SerializedName("slaLive") val slaLive: TicketSlaLiveDto? = null,
    @SerializedName("conversations") val conversations: List<TicketConversationMessageDto> = emptyList(),
    @SerializedName("historyLogs") val historyLogs: List<TicketHistoryLogDto> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class TicketDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: TicketDetailDto? = null,
    @SerializedName("ticket") val ticket: TicketDetailDto? = null,
    @SerializedName("message") val message: String? = null
)

// --- Request Payloads ---

data class CreateTicketRequest(
    @SerializedName("requesterName") val requesterName: String,
    @SerializedName("requesterDepartment") val requesterDepartment: String?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("subject") val subject: String,
    @SerializedName("category") val category: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("branchName") val branchName: String?,
    @SerializedName("relatedDeviceCode") val relatedDeviceCode: String?,
    @SerializedName("relatedAssetCode") val relatedAssetCode: String?,
    @SerializedName("applicationName") val applicationName: String?,
    @SerializedName("description") val description: String
)

data class UpdateTicketStateRequest(
    @SerializedName("assigneeTeam") val assigneeTeam: String? = null,
    @SerializedName("assignedUserName") val assignedUserName: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("priority") val priority: String? = null
)

data class PostTicketMessageRequest(
    @SerializedName("message") val message: String,
    @SerializedName("isInternalNote") val isInternalNote: Boolean
)