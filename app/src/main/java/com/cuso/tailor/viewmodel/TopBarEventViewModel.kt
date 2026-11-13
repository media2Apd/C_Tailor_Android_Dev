@file:Suppress("unused")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.topBarModels.BusinessAgendaData
import com.cuso.tailor.model.topBarModels.BusinessAgendaResponse
import com.cuso.tailor.model.topBarModels.NotificationActionResponse
import com.cuso.tailor.model.topBarModels.NotificationItemDto
import com.cuso.tailor.model.topBarModels.NotificationListResponse
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.TopbarSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// --- TopBar Event Endpoints ---
private const val NOTIFICATIONS_LIST_URL = "/api/notifications"
private const val NOTIFICATION_MARK_READ_URL = "/api/notifications"
private const val NOTIFICATION_MARK_ALL_READ_URL = "/api/notifications/read-all"
private const val BUSINESS_AGENDA_URL = "/api/documents/business-agenda"
private const val REGISTER_FCM_TOKEN_URL = "/api/notifications/fcm-token"


@HiltViewModel
class TopbarEventViewModel @Inject constructor(
    private val repo: ApiRepository,
    private val socketManager: TopbarSocketManager
) : ViewModel() {

    init {
        observeRealtimeEvents()
    }

    /**
     * Collects live Socket events and updates UI state immediately without extra REST calls
     */
    private fun observeRealtimeEvents() {
        viewModelScope.launch {
            socketManager.incomingNotifications.collect { newNotification ->
                val current = _notifications.value
                // Avoid duplicate entries if REST and Socket overlap
                if (current.none { it._id == newNotification._id }) {
                    _notifications.value = listOf(newNotification) + current
                    if (!newNotification.isRead) {
                        _unreadCount.value = _unreadCount.value + 1
                    }
                    DynamicIslandManager.showSuccess(newNotification.title)
                }
            }
        }

        viewModelScope.launch {
            socketManager.agendaRefreshSignal.collect {
                // Refresh Business Agenda when backend emits an update signal
                fetchBusinessAgenda()
            }
        }
    }

    fun syncFcmTokenToServer(fcmToken: String) {
        if (fcmToken.isBlank()) return
        viewModelScope.launch {
            repo.request<NotificationActionResponse> {
                post(
                    REGISTER_FCM_TOKEN_URL,
                    mapOf(
                        "fcmToken" to fcmToken,
                        "platform" to "android"
                    )
                )
            }
        }
    }

    /**
     * Connects persistent socket once user token and organizationId are available
     */
    fun startRealtimeSync(
        baseUrl: String,
        accessToken: String,
        memberId: String,
        organizationId: String
    ) {
        socketManager.connect(
            rawBaseUrl = baseUrl,
            accessToken = accessToken,
            organizationId = organizationId,
            userId = memberId
        )
    }

    // =========================================================================
    // 1. NOTIFICATIONS STATES
    // =========================================================================

    private val _notifications = MutableStateFlow<List<NotificationItemDto>>(emptyList())
    val notifications: StateFlow<List<NotificationItemDto>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoadingNotifications = MutableStateFlow(false)
    val isLoadingNotifications: StateFlow<Boolean> = _isLoadingNotifications.asStateFlow()

    private val _notificationsError = MutableStateFlow<String?>(null)
    val notificationsError: StateFlow<String?> = _notificationsError.asStateFlow()

    val unreadNotifications: StateFlow<List<NotificationItemDto>> = _notifications
        .map { list -> list.filter { !it.isRead && !it.isDeleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // 2. BUSINESS AGENDA STATES
    // =========================================================================

    private val _agendaData = MutableStateFlow(BusinessAgendaData())
    val agendaData: StateFlow<BusinessAgendaData> = _agendaData.asStateFlow()

    private val _isLoadingAgenda = MutableStateFlow(false)
    val isLoadingAgenda: StateFlow<Boolean> = _isLoadingAgenda.asStateFlow()

    private val _agendaError = MutableStateFlow<String?>(null)
    val agendaError: StateFlow<String?> = _agendaError.asStateFlow()

    val todayAgendaCount: StateFlow<Int> = _agendaData
        .map { it.todayAgenda.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val upcomingAgendaCount: StateFlow<Int> = _agendaData
        .map { it.upcomingEvents.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // =========================================================================
    // 3. NOTIFICATION API OPERATIONS
    // =========================================================================

    /**
     * Fetches notifications from GET /api/notifications
     */
    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoadingNotifications.value = true
            _notificationsError.value = null

            val result = repo.request<NotificationListResponse> {
                get(NOTIFICATIONS_LIST_URL)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        val activeList = res.data.filter { !it.isDeleted }
                        _notifications.value = activeList
                        _unreadCount.value = res.unreadCount
                    } else {
                        _notificationsError.value = res.message ?: "Failed to load notifications"
                    }
                },
                onFailure = { err ->
                    _notificationsError.value = err.message ?: "Network error loading notifications"
                }
            )
            _isLoadingNotifications.value = false
        }
    }

    /**
     * Marks a single notification as read with optimistic UI update
     */
    fun markNotificationAsRead(notificationId: String) {
        if (notificationId.isBlank()) return

        val target = _notifications.value.find { it._id == notificationId }
        if (target != null && !target.isRead) {
            _notifications.value = _notifications.value.map {
                if (it._id == notificationId) it.copy(isRead = true) else it
            }
            _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
        }

        viewModelScope.launch {
            repo.request<NotificationActionResponse> {
                patch("$NOTIFICATION_MARK_READ_URL/$notificationId/read", emptyMap<String, Any>())
            }
        }
    }

    /**
     * Marks all notifications as read with optimistic UI update
     */
    fun markAllNotificationsAsRead() {
        if (_unreadCount.value == 0) return

        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0

        viewModelScope.launch {
            val result = repo.request<NotificationActionResponse> {
                patch(NOTIFICATION_MARK_ALL_READ_URL, emptyMap<String, Any>())
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        DynamicIslandManager.showSuccess("All notifications marked as read")
                    }
                },
                onFailure = {
                    // Reload from server if request failed
                    fetchNotifications()
                }
            )
        }
    }

    // =========================================================================
    // 4. BUSINESS AGENDA API OPERATIONS
    // =========================================================================

    /**
     * Fetches Business Agenda items from GET /api/documents/business-agenda
     */
    fun fetchBusinessAgenda() {
        viewModelScope.launch {
            _isLoadingAgenda.value = true
            _agendaError.value = null

            val result = repo.request<BusinessAgendaResponse> {
                get(BUSINESS_AGENDA_URL)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _agendaData.value = res.data
                    } else {
                        _agendaError.value = res.message
                    }
                },
                onFailure = { err ->
                    _agendaError.value = err.message
                }
            )
            _isLoadingAgenda.value = false
        }
    }

    // =========================================================================
    // 5. DATE FORMATTING UTILITIES
    // =========================================================================

    /**
     * Converts UTC ISO string to local "dd/MM/yyyy, HH:mm" format
     */
    fun formatNotificationDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString) ?: return "--"
            SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            "--"
        }
    }

    /**
     * Formats today's date as "Fri, 13 Nov, 2026" for the Business Agenda header
     */
    fun getFormattedTodayHeader(): String {
        return SimpleDateFormat("EEE, dd MMM, yyyy", Locale.getDefault()).format(Date())
    }
}