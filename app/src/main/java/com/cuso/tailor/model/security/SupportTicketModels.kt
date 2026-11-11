package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

// --- Generic Responses ---
data class SupportTicketListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SupportTicketListData? = null,
    @SerializedName("message") val message: String? = null
)

data class SupportTicketSingleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SupportTicketItem? = null,
    @SerializedName("message") val message: String? = null
)

data class SupportTicketListData(
    @SerializedName("tickets") val tickets: List<SupportTicketItem> = emptyList(),
    @SerializedName("meta") val meta: SupportTicketMeta? = null
)

data class SupportTicketMeta(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

// --- Ticket Item Model ---
data class SupportTicketItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("ticketNumber") val ticketNumber: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("type") val type: String = "external",
    @SerializedName("issueType") val issueType: String = "",
    @SerializedName("subject") val subject: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("affectedDevice") val affectedDevice: String? = null,
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("affectedArea") val affectedArea: String = "",
    @SerializedName("affectedUserId") val affectedUserId: Any? = null, // Can be ID string or User object
    @SerializedName("branchId") val branchId: Any? = null,             // Can be ID string or Branch object
    @SerializedName("contactPreference") val contactPreference: String = "Email",
    @SerializedName("contactEmail") val contactEmail: String? = null,
    @SerializedName("status") val status: String = "Open",
    @SerializedName("assignedTeam") val assignedTeam: String? = null,
    @SerializedName("assignedTo") val assignedTo: Any? = null,         // Can be ID string or Member object
    @SerializedName("assignmentNote") val assignmentNote: String? = null,
    @SerializedName("sourceArticleId") val sourceArticleId: String? = null,
    @SerializedName("attachments") val attachments: List<TicketAttachment> = emptyList(),
    @SerializedName("sla") val sla: TicketSlaInfo? = null,
    @SerializedName("resolution") val resolution: TicketResolutionInfo? = null,
    @SerializedName("conversation") val conversation: List<TicketConversationMessage> = emptyList(),
    @SerializedName("activityHistory") val activityHistory: List<TicketActivityLog> = emptyList(),
    @SerializedName("createdBy") val createdBy: Any? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class TicketAttachment(
    @SerializedName("url") val url: String = "",
    @SerializedName("publicId") val publicId: String? = null,
    @SerializedName("filename") val filename: String = "",
    @SerializedName("size") val size: Long = 0L,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class TicketSlaInfo(
    @SerializedName("firstResponseTarget") val firstResponseTarget: String? = null,
    @SerializedName("firstResponseActual") val firstResponseActual: String? = null,
    @SerializedName("resolutionTarget") val resolutionTarget: String? = null,
    @SerializedName("isEscalated") val isEscalated: Boolean = false,
    @SerializedName("status") val status: String? = "Ok",
    @SerializedName("timeRemaining") val timeRemaining: String? = null
)

data class TicketResolutionInfo(
    @SerializedName("status") val status: String = "Pending",
    @SerializedName("resolvedAt") val resolvedAt: String? = null,
    @SerializedName("resolvedBy") val resolvedBy: Any? = null,
    @SerializedName("resolutionNote") val resolutionNote: String? = null
)

data class TicketConversationMessage(
    @SerializedName("_id") val id: String = "",
    @SerializedName("senderId") val senderId: Any? = null,
    @SerializedName("senderType") val senderType: String = "requester", // "requester" or "support"
    @SerializedName("senderName") val senderName: String? = null,
    @SerializedName("message") val message: String = "",
    @SerializedName("attachments") val attachments: List<TicketAttachment> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null
)

data class TicketActivityLog(
    @SerializedName("_id") val id: String = "",
    @SerializedName("action") val action: String = "",
    @SerializedName("performedBy") val performedBy: Any? = null,
    @SerializedName("performedByName") val performedByName: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)