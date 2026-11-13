package com.cuso.tailor.model.topBarModels

// =========================================================================
// NOTIFICATION API MODELS (Matches GET /api/notifications)
// =========================================================================

data class NotificationListResponse(
    val success: Boolean = false,
    val data: List<NotificationItemDto> = emptyList(),
    val unreadCount: Int = 0,
    val message: String? = null
)

data class NotificationActionResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: NotificationItemDto? = null
)

data class NotificationItemDto(
    val _id: String = "",
    val organizationId: String = "",
    val memberId: String = "",
    val actorId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val icon: String = "",
    val severity: String = "info",
    val entityId: String? = null,
    val entityType: String? = null,
    val link: String? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val isDeleted: Boolean = false,
    val __v: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)

// =========================================================================
// BUSINESS AGENDA API MODELS
// =========================================================================

data class BusinessAgendaResponse(
    val success: Boolean = false,
    val data: BusinessAgendaData? = null,
    val message: String? = null
)

data class BusinessAgendaData(
    val todayAgenda: List<AgendaItemDto> = emptyList(),
    val upcomingEvents: List<AgendaItemDto> = emptyList(),
    val holidays: List<HolidayItemDto> = emptyList()
)

// Matches items in both `todayAgenda` (birthdays, leaves, due bills) and `upcomingEvents` (upcoming bills)
data class AgendaItemDto(
    val id: String = "",
    val module: String = "",
    val title: String = "",
    val desc: String = "",
    val time: String = "",
    val type: String = "", // "celebration" | "info" | "urgent"
    val date: String = "",
    val path: String = ""
)

// Matches items in `holidays` array from getBusinessAgendaService
data class HolidayItemDto(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val daysLeft: String = ""
)