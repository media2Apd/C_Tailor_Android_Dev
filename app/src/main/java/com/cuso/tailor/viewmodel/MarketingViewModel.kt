@file:Suppress("unused")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.marketing.*
import com.cuso.tailor.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// --- Marketing Module Endpoints ---
private const val MARKETING_DASHBOARD_SUMMARY_URL = "/api/marketing/dashboard/summary"
private const val MARKETING_CHANNELS_OVERVIEW_URL = "/api/marketing/channels/overview"
private const val MARKETING_SOCIAL_OVERVIEW_URL = "/api/marketing/social/overview"
private const val MARKETING_WEB_ANALYTICS_OVERVIEW_URL = "/api/marketing/web-analytics/overview"
private const val MARKETING_WEB_ANALYTICS_TRAFFIC_URL = "/api/marketing/web-analytics/traffic-engagement"
private const val MARKETING_CUSTOMERS_OVERVIEW_URL = "/api/marketing/customers/overview"
private const val MARKETING_CAMPAIGNS_URL = "/api/marketing/campaigns"
private const val MARKETING_CALENDAR_VIEW_URL = "/api/marketing/calendar/view"

private const val CAMPAIGNS_DASHBOARD_URL = "/api/marketing/campaigns"

// Endpoints
private const val OAUTH_STATUS_URL = "/api/it/oauth/status"
private const val ADVERTISING_OVERVIEW_URL = "/api/marketing/advertising/overview"
private const val ADVERTISING_CREATIVES_URL = "/api/marketing/advertising/creatives"

private val AD_PLATFORM_SYNC_URL = "/api/marketing/sync/trigger"
private val AD_ACCOUNTS_URL = "/api/it/oauth/GOOGLE_ADS/accounts"


@HiltViewModel
class MarketingViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    // =========================================================================
    // 1. GLOBAL & DASHBOARD SUMMARY STATES
    // =========================================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("this_week")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _selectedTrendRange = MutableStateFlow("6M")
    val selectedTrendRange: StateFlow<String> = _selectedTrendRange.asStateFlow()

    private val _dashboardSummary = MutableStateFlow<MarketingDashboardSummaryData?>(null)
    val dashboardSummary: StateFlow<MarketingDashboardSummaryData?> = _dashboardSummary.asStateFlow()

    // =========================================================================
    // 2. MARKETING CHANNELS STATES
    // =========================================================================

    private val _channelData = MutableStateFlow<ChannelOverviewData?>(null)
    val channelData: StateFlow<ChannelOverviewData?> = _channelData.asStateFlow()

    private val _channelPerformanceList = MutableStateFlow<List<ChannelMatrixItemDto>>(emptyList())
    val channelPerformanceList: StateFlow<List<ChannelMatrixItemDto>> = _channelPerformanceList.asStateFlow()

    private val _channelSearchQuery = MutableStateFlow("")
    val channelSearchQuery: StateFlow<String> = _channelSearchQuery.asStateFlow()

    private val _selectedChannelType = MutableStateFlow<String?>(null)
    val selectedChannelType: StateFlow<String?> = _selectedChannelType.asStateFlow()

    private val _isLoadingChannels = MutableStateFlow(false)
    val isLoadingChannels: StateFlow<Boolean> = _isLoadingChannels.asStateFlow()

    private val _channelsError = MutableStateFlow<String?>(null)
    val channelsError: StateFlow<String?> = _channelsError.asStateFlow()

    /**
     * Filtered channels by search query and channel type (e.g., Paid, Organic, Owned, Direct).
     */
    val filteredChannels: StateFlow<List<ChannelMatrixItemDto>> = combine(
        _channelPerformanceList,
        _channelSearchQuery,
        _selectedChannelType
    ) { list, query, type ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.channel.lowercase().contains(cleanQuery) ||
                    item.type.lowercase().contains(cleanQuery)

            val matchesType = type.isNullOrBlank() || item.type.equals(type, ignoreCase = true)
            matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // 3. CAMPAIGNS STATES
    // =========================================================================

    private val _campaignsList = MutableStateFlow<List<CampaignItemDto>>(emptyList())
    val campaignsList: StateFlow<List<CampaignItemDto>> = _campaignsList.asStateFlow()

    private val _campaignSearchQuery = MutableStateFlow("")
    val campaignSearchQuery: StateFlow<String> = _campaignSearchQuery.asStateFlow()

    private val _selectedCampaignStatus = MutableStateFlow<String?>(null)
    val selectedCampaignStatus: StateFlow<String?> = _selectedCampaignStatus.asStateFlow()

    private val _selectedCampaignPlatform = MutableStateFlow<String?>(null)
    val selectedCampaignPlatform: StateFlow<String?> = _selectedCampaignPlatform.asStateFlow()

    private val _isLoadingCampaigns = MutableStateFlow(false)
    val isLoadingCampaigns: StateFlow<Boolean> = _isLoadingCampaigns.asStateFlow()

    private val _campaignsError = MutableStateFlow<String?>(null)
    val campaignsError: StateFlow<String?> = _campaignsError.asStateFlow()

    /**
     * Filtered campaigns by search query, status (Active, Paused, etc.), and platform (Meta, Google, etc.).
     */
    val filteredCampaigns: StateFlow<List<CampaignItemDto>> = combine(
        _campaignsList,
        _campaignSearchQuery,
        _selectedCampaignStatus,
        _selectedCampaignPlatform
    ) { list, query, status, platform ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.campaignName.lowercase().contains(cleanQuery) ||
                    item.campaignCode.lowercase().contains(cleanQuery) ||
                    item.platform.lowercase().contains(cleanQuery)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)
            val matchesPlatform = platform.isNullOrBlank() || item.platform.equals(platform, ignoreCase = true)

            matchesQuery && matchesStatus && matchesPlatform
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // 4. SOCIAL MEDIA OVERVIEW STATES
    // =========================================================================

    private val _socialOverview = MutableStateFlow<SocialOverviewData?>(null)
    val socialOverview: StateFlow<SocialOverviewData?> = _socialOverview.asStateFlow()

    private val _socialPostsList = MutableStateFlow<List<SocialPostDto>>(emptyList())
    val socialPostsList: StateFlow<List<SocialPostDto>> = _socialPostsList.asStateFlow()

    private val _socialPostSearchQuery = MutableStateFlow("")
    val socialPostSearchQuery: StateFlow<String> = _socialPostSearchQuery.asStateFlow()

    private val _selectedSocialStatus = MutableStateFlow<String?>(null)
    val selectedSocialStatus: StateFlow<String?> = _selectedSocialStatus.asStateFlow()

    private val _isLoadingSocial = MutableStateFlow(false)
    val isLoadingSocial: StateFlow<Boolean> = _isLoadingSocial.asStateFlow()

    private val _socialError = MutableStateFlow<String?>(null)
    val socialError: StateFlow<String?> = _socialError.asStateFlow()

    val filteredSocialPosts: StateFlow<List<SocialPostDto>> = combine(
        _socialPostsList,
        _socialPostSearchQuery,
        _selectedSocialStatus
    ) { list, query, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.name.lowercase().contains(cleanQuery) ||
                    item.headline.lowercase().contains(cleanQuery) ||
                    item.creativeCode.lowercase().contains(cleanQuery) ||
                    item.campaignName.lowercase().contains(cleanQuery)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)
            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // 5. WEB ANALYTICS & TRAFFIC STATES
    // =========================================================================

    private val _webAnalyticsData = MutableStateFlow<WebAnalyticsOverviewData?>(null)
    val webAnalyticsData: StateFlow<WebAnalyticsOverviewData?> = _webAnalyticsData.asStateFlow()

    private val _trafficEngagementData = MutableStateFlow<TrafficEngagementData?>(null)
    val trafficEngagementData: StateFlow<TrafficEngagementData?> = _trafficEngagementData.asStateFlow()

    private val _isLoadingWeb = MutableStateFlow(false)
    val isLoadingWeb: StateFlow<Boolean> = _isLoadingWeb.asStateFlow()

    private val _webError = MutableStateFlow<String?>(null)
    val webError: StateFlow<String?> = _webError.asStateFlow()

    // =========================================================================
    // 6. CUSTOMER & RETENTION STATES
    // =========================================================================

    private val _customerOverview = MutableStateFlow<CustomerOverviewData?>(null)
    val customerOverview: StateFlow<CustomerOverviewData?> = _customerOverview.asStateFlow()

    private val _isLoadingCustomers = MutableStateFlow(false)
    val isLoadingCustomers: StateFlow<Boolean> = _isLoadingCustomers.asStateFlow()

    private val _customersError = MutableStateFlow<String?>(null)
    val customersError: StateFlow<String?> = _customersError.asStateFlow()

    // =========================================================================
    // 7. MARKETING CALENDAR STATES
    // =========================================================================

    private val _currentCalendarYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentCalendarYear: StateFlow<Int> = _currentCalendarYear.asStateFlow()

    private val _currentCalendarMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val currentCalendarMonth: StateFlow<Int> = _currentCalendarMonth.asStateFlow()

    private val _calendarData = MutableStateFlow<MarketingCalendarData?>(null)
    val calendarData: StateFlow<MarketingCalendarData?> = _calendarData.asStateFlow()

    private val _calendarActivitiesList = MutableStateFlow<List<CalendarActivityDto>>(emptyList())
    val calendarActivitiesList: StateFlow<List<CalendarActivityDto>> = _calendarActivitiesList.asStateFlow()

    private val _selectedCalendarFilterTag = MutableStateFlow("All")
    val selectedCalendarFilterTag: StateFlow<String> = _selectedCalendarFilterTag.asStateFlow()

    private val _isLoadingCalendar = MutableStateFlow(false)
    val isLoadingCalendar: StateFlow<Boolean> = _isLoadingCalendar.asStateFlow()

    private val _calendarError = MutableStateFlow<String?>(null)
    val calendarError: StateFlow<String?> = _calendarError.asStateFlow()

    val filteredCalendarActivities: StateFlow<List<CalendarActivityDto>> = combine(
        _calendarActivitiesList,
        _selectedCalendarFilterTag
    ) { list, tag ->
        if (tag.equals("All", ignoreCase = true)) {
            list
        } else {
            list.filter { it.activityType.equals(tag, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // INITIALIZATION
    // =========================================================================

    init {
        fetchAllDashboardData()
    }

    /**
     * Fetches all marketing data concurrently for the default timeframe and range.
     */
    fun fetchAllDashboardData() {
        fetchDashboardSummary()
        fetchChannelsOverview()
        fetchSocialOverview()
        fetchWebAnalyticsOverview()
        fetchTrafficEngagement()
        fetchCustomersOverview()
        fetchCampaigns()
        fetchCalendarView()
    }

    // =========================================================================
    // 1. DASHBOARD SUMMARY API CALLS
    // =========================================================================

    fun onTimeframeSelected(timeframe: String) {
        _selectedTimeframe.value = timeframe
        fetchDashboardSummary()
    }

    fun onTrendRangeSelected(range: String) {
        _selectedTrendRange.value = range
        fetchDashboardSummary()
    }

    fun fetchDashboardSummary(
        timeframe: String = _selectedTimeframe.value,
        trendRange: String = _selectedTrendRange.value
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.request<MarketingDashboardSummaryResponse> {
                get(
                    MARKETING_DASHBOARD_SUMMARY_URL,
                    repo.query("timeframe" to timeframe, "trendRange" to trendRange)
                )
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _dashboardSummary.value = res.data
                    } else {
                        _errorMessage.value = res.message ?: "Failed to load marketing dashboard summary"
                    }
                },
                onFailure = { err ->
                    _errorMessage.value = err.message ?: "Network error fetching marketing summary"
                }
            )
            _isLoading.value = false
        }
    }

    // =========================================================================
    // 2. CHANNELS OVERVIEW API CALLS
    // =========================================================================

    fun onChannelSearchQueryChange(query: String) {
        _channelSearchQuery.value = query
    }

    fun onChannelTypeFilterChange(type: String?) {
        _selectedChannelType.value = type
    }

    fun clearChannelFilters() {
        _channelSearchQuery.value = ""
        _selectedChannelType.value = null
    }

    fun fetchChannelsOverview(days: Int = 30) {
        viewModelScope.launch {
            _isLoadingChannels.value = true
            _channelsError.value = null

            val result = repo.request<ChannelOverviewResponse> {
                get(MARKETING_CHANNELS_OVERVIEW_URL, repo.query("days" to days))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _channelData.value = res.data
                        _channelPerformanceList.value = res.data.channelPerformanceMatrix
                    } else {
                        _channelsError.value = res.message ?: "Failed to load channel performance"
                    }
                },
                onFailure = { err ->
                    _channelsError.value = err.message ?: "Network error fetching channel performance"
                }
            )
            _isLoadingChannels.value = false
        }
    }

    // =========================================================================
    // 3. CAMPAIGNS API CALLS
    // =========================================================================

    fun onCampaignSearchQueryChange(query: String) {
        _campaignSearchQuery.value = query
    }

    fun onCampaignStatusFilterChange(status: String?) {
        _selectedCampaignStatus.value = status
    }

    fun onCampaignPlatformFilterChange(platform: String?) {
        _selectedCampaignPlatform.value = platform
    }

    fun clearCampaignFilters() {
        _campaignSearchQuery.value = ""
        _selectedCampaignStatus.value = null
        _selectedCampaignPlatform.value = null
    }

    fun fetchCampaigns() {
        viewModelScope.launch {
            _isLoadingCampaigns.value = true
            _campaignsError.value = null

            val result = repo.request<CampaignsListResponse> {
                get(MARKETING_CAMPAIGNS_URL)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _campaignsList.value = res.data
                    } else {
                        _campaignsError.value = res.message ?: "Failed to load campaigns ledger"
                    }
                },
                onFailure = { err ->
                    _campaignsError.value = err.message ?: "Network error fetching campaigns"
                }
            )
            _isLoadingCampaigns.value = false
        }
    }

    // =========================================================================
    // 4. SOCIAL OVERVIEW API CALLS
    // =========================================================================

    fun onSocialSearchQueryChange(query: String) {
        _socialPostSearchQuery.value = query
    }

    fun onSocialStatusFilterChange(status: String?) {
        _selectedSocialStatus.value = status
    }

    fun clearSocialFilters() {
        _socialPostSearchQuery.value = ""
        _selectedSocialStatus.value = null
    }

    fun fetchSocialOverview(days: Int = 30) {
        viewModelScope.launch {
            _isLoadingSocial.value = true
            _socialError.value = null

            val result = repo.request<SocialOverviewResponse> {
                get(MARKETING_SOCIAL_OVERVIEW_URL, repo.query("days" to days))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _socialOverview.value = res.data
                        _socialPostsList.value = res.data.posts
                    } else {
                        _socialError.value = res.message ?: "Failed to load social metrics"
                    }
                },
                onFailure = { err ->
                    _socialError.value = err.message ?: "Network error fetching social metrics"
                }
            )
            _isLoadingSocial.value = false
        }
    }

    // =========================================================================
    // 5. WEB ANALYTICS & TRAFFIC API CALLS
    // =========================================================================

    fun fetchWebAnalyticsOverview(timeframe: String = "30D") {
        viewModelScope.launch {
            _isLoadingWeb.value = true
            _webError.value = null

            val result = repo.request<WebAnalyticsOverviewResponse> {
                get(MARKETING_WEB_ANALYTICS_OVERVIEW_URL, repo.query("timeframe" to timeframe))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _webAnalyticsData.value = res.data
                    } else {
                        _webError.value = res.message ?: "Failed to load web analytics"
                    }
                },
                onFailure = { err ->
                    _webError.value = err.message ?: "Network error fetching web analytics"
                }
            )
            _isLoadingWeb.value = false
        }
    }

    fun fetchTrafficEngagement(timeframe: String = "30D") {
        viewModelScope.launch {
            _isLoadingWeb.value = true
            val result = repo.request<TrafficEngagementResponse> {
                get(MARKETING_WEB_ANALYTICS_TRAFFIC_URL, repo.query("timeframe" to timeframe))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _trafficEngagementData.value = res.data
                    }
                },
                onFailure = { err ->
                    _webError.value = err.message ?: "Network error fetching traffic engagement"
                }
            )
            _isLoadingWeb.value = false
        }
    }

    // =========================================================================
    // 6. CUSTOMER & RETENTION API CALLS
    // =========================================================================

    fun fetchCustomersOverview(days: Int = 30) {
        viewModelScope.launch {
            _isLoadingCustomers.value = true
            _customersError.value = null

            val result = repo.request<CustomerOverviewResponse> {
                get(MARKETING_CUSTOMERS_OVERVIEW_URL, repo.query("days" to days))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _customerOverview.value = res.data
                    } else {
                        _customersError.value = res.message ?: "Failed to load customer metrics"
                    }
                },
                onFailure = { err ->
                    _customersError.value = err.message ?: "Network error fetching customer overview"
                }
            )
            _isLoadingCustomers.value = false
        }
    }

    // =========================================================================
    // 7. CALENDAR VIEW API CALLS & ACTIONS
    // =========================================================================

    fun onCalendarFilterTagSelected(tag: String) {
        _selectedCalendarFilterTag.value = tag
    }

    fun onPreviousMonth() {
        var year = _currentCalendarYear.value
        var month = _currentCalendarMonth.value - 1
        if (month < 1) {
            month = 12
            year -= 1
        }
        _currentCalendarYear.value = year
        _currentCalendarMonth.value = month
        fetchCalendarView(year, month)
    }

    fun onNextMonth() {
        var year = _currentCalendarYear.value
        var month = _currentCalendarMonth.value + 1
        if (month > 12) {
            month = 1
            year += 1
        }
        _currentCalendarYear.value = year
        _currentCalendarMonth.value = month
        fetchCalendarView(year, month)
    }

    fun fetchCalendarView(
        year: Int = _currentCalendarYear.value,
        month: Int = _currentCalendarMonth.value
    ) {
        viewModelScope.launch {
            _isLoadingCalendar.value = true
            _calendarError.value = null

            val result = repo.request<MarketingCalendarResponse> {
                get(
                    MARKETING_CALENDAR_VIEW_URL,
                    repo.query("year" to year, "month" to month)
                )
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _calendarData.value = res.data

                        // Flatten days and de-duplicate activities by ID
                        val activityMap = LinkedHashMap<String, CalendarActivityDto>()
                        res.data.calendarMatrix.days.forEach { day ->
                            day.activities.forEach { activity ->
                                if (!activityMap.containsKey(activity.id)) {
                                    activityMap[activity.id] = activity
                                }
                            }
                        }
                        _calendarActivitiesList.value = activityMap.values.toList()
                    } else {
                        _calendarError.value = res.message ?: "Failed to load marketing calendar"
                    }
                },
                onFailure = { err ->
                    _calendarError.value = err.message ?: "Network error fetching calendar"
                }
            )
            _isLoadingCalendar.value = false
        }
    }

    private val _selectedRange = MutableStateFlow("30D")
    val selectedRange: StateFlow<String> = _selectedRange.asStateFlow()

    private val _activeTrendMetric = MutableStateFlow("Spend")
    val activeTrendMetric: StateFlow<String> = _activeTrendMetric.asStateFlow()

    private val _dailyTrend = MutableStateFlow<List<CampaignDailyTrendDto>>(emptyList())
    val dailyTrend: StateFlow<List<CampaignDailyTrendDto>> = _dailyTrend.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun onDateRangeSelected(rangeId: String) {
        _selectedRange.value = rangeId
        fetchDashboardData()
    }

    fun onTrendMetricSelected(metric: String) {
        _activeTrendMetric.value = metric
    }

    fun fetchDashboardData() {
        val daysParam = when (_selectedRange.value) {
            "7D" -> 7
            "90D" -> 90
            else -> 30
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.request<CampaignsDashboardResponse> {
                get(CAMPAIGNS_DASHBOARD_URL, repo.query("days" to daysParam))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _campaignsList.value = res.data.campaigns
                        _dailyTrend.value = res.data.dailyTrend
                    } else {
                        _errorMessage.value = res.message ?: "Failed to load campaign performance"
                    }
                },
                onFailure = { err ->
                    _errorMessage.value = err.message ?: "Network error fetching campaigns"
                }
            )
            _isLoading.value = false
        }
    }

    // Derived Status Distribution counts
    val statusBreakdown: StateFlow<List<CampaignStatusBreakdown>> = _campaignsList.combine(_campaignsList) { list, _ ->
        val total = list.size.coerceAtLeast(1)
        val active = list.count { it.status.equals("Active", ignoreCase = true) }
        val scheduled = list.count { it.status.equals("Scheduled", ignoreCase = true) }
        val completed = list.count { it.status.equals("Completed", ignoreCase = true) }
        val pausedOrStopped = list.count {
            it.status.equals("Paused", ignoreCase = true) || it.status.equals("Stopped", ignoreCase = true)
        }

        listOf(
            CampaignStatusBreakdown("Active", active, (active * 100) / total),
            CampaignStatusBreakdown("Scheduled", scheduled, (scheduled * 100) / total),
            CampaignStatusBreakdown("Completed", completed, (completed * 100) / total),
            CampaignStatusBreakdown("Paused / Stopped", pausedOrStopped, (pausedOrStopped * 100) / total)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived Platform Metrics Summary
    val platformSummaries: StateFlow<List<PlatformPerformanceSummary>> = _campaignsList.combine(_campaignsList) { list, _ ->
        list.groupBy { it.platform.ifBlank { "Other" } }
            .map { (platform, items) ->
                val spend = items.sumOf { it.spendValue }
                val revenue = items.sumOf { it.revenueValue }
                val conversions = items.sumOf { it.conversions }
                val customerOrders = items.sumOf { it.customerOrders }
                val roi = if (spend > 0) revenue / spend else 0.0

                PlatformPerformanceSummary(
                    platform = platform,
                    spend = spend,
                    conversions = conversions,
                    roi = roi,
                    leads = customerOrders
                )
            }.sortedByDescending { it.spend }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // 8. STRING & DATE FORMATTING UTILITIES
    // =========================================================================

    /**
     * Formats an ISO-8601 UTC timestamp to time-elapsed string ("Just now", "X min ago", etc.).
     */
    fun formatTimeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Just now"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString) ?: return "Just now"
            val diffMins = (Date().time - date.time) / (1000 * 60)
            when {
                diffMins < 1 -> "Just now"
                diffMins < 60 -> "$diffMins min ago"
                diffMins < 1440 -> "${diffMins / 60} hr ago"
                else -> "${diffMins / 1440} days ago"
            }
        } catch (_: Exception) {
            "Just now"
        }
    }

    // --- Single Campaign Details States ---
    private val _selectedCampaignCode = MutableStateFlow<String?>(null)
    val selectedCampaignCode: StateFlow<String?> = _selectedCampaignCode.asStateFlow()

    private val _campaignOverview = MutableStateFlow<CampaignOverviewDetailsDto?>(null)
    val campaignOverview: StateFlow<CampaignOverviewDetailsDto?> = _campaignOverview.asStateFlow()

    private val _campaignPerformance = MutableStateFlow<CampaignPerformanceData?>(null)
    val campaignPerformance: StateFlow<CampaignPerformanceData?> = _campaignPerformance.asStateFlow()

    private val _campaignAudience = MutableStateFlow<List<CampaignAudienceQuarterDto>>(emptyList())
    val campaignAudience: StateFlow<List<CampaignAudienceQuarterDto>> = _campaignAudience.asStateFlow()

    private val _campaignCreatives = MutableStateFlow<List<CampaignCreativeItemDto>>(emptyList())
    val campaignCreatives: StateFlow<List<CampaignCreativeItemDto>> = _campaignCreatives.asStateFlow()

    private val _campaignTimeline = MutableStateFlow<List<CampaignTimelineEventDto>>(emptyList())
    val campaignTimeline: StateFlow<List<CampaignTimelineEventDto>> = _campaignTimeline.asStateFlow()

    private val _isLoadingCampaignDetail = MutableStateFlow(false)
    val isLoadingCampaignDetail: StateFlow<Boolean> = _isLoadingCampaignDetail.asStateFlow()

    /**
     * Loads all 5 tabs data for a campaign concurrently.
     */
    fun loadCampaignDetailData(campaignCode: String) {
        if (campaignCode.isBlank()) return
        _selectedCampaignCode.value = campaignCode

        viewModelScope.launch {
            _isLoadingCampaignDetail.value = true
            fetchCampaignOverview(campaignCode)
            fetchCampaignPerformance(campaignCode)
            fetchCampaignAudience(campaignCode)
            fetchCampaignCreatives(campaignCode)
            fetchCampaignTimeline(campaignCode)
            _isLoadingCampaignDetail.value = false
        }
    }

    fun fetchCampaignOverview(campaignCode: String) {
        viewModelScope.launch {
            val result = repo.request<CampaignOverviewResponse> {
                get("$CAMPAIGNS_DASHBOARD_URL/$campaignCode/overview")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _campaignOverview.value = res.data?.overview
                    }
                },
                onFailure = { /* Log Error */ }
            )
        }
    }

    fun fetchCampaignPerformance(campaignCode: String) {
        viewModelScope.launch {
            val result = repo.request<CampaignPerformanceResponse> {
                get("$CAMPAIGNS_DASHBOARD_URL/$campaignCode/performance")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _campaignPerformance.value = res.data
                    }
                },
                onFailure = { /* Log Error */ }
            )
        }
    }

    fun fetchCampaignAudience(campaignCode: String) {
        viewModelScope.launch {
            val result = repo.request<CampaignAudienceResponse> {
                get("$CAMPAIGNS_DASHBOARD_URL/$campaignCode/audience")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _campaignAudience.value = res.data
                    }
                },
                onFailure = { /* Log Error */ }
            )
        }
    }

    fun fetchCampaignCreatives(campaignCode: String) {
        viewModelScope.launch {
            val result = repo.request<CampaignCreativeResponse> {
                get("$CAMPAIGNS_DASHBOARD_URL/$campaignCode/creatives")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _campaignCreatives.value = res.data
                    }
                },
                onFailure = { /* Log Error */ }
            )
        }
    }

    fun fetchCampaignTimeline(campaignCode: String) {
        viewModelScope.launch {
            val result = repo.request<CampaignTimelineResponse> {
                get("$CAMPAIGNS_DASHBOARD_URL/$campaignCode/timeline")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _campaignTimeline.value = res.data
                    }
                },
                onFailure = { /* Log Error */ }
            )
        }
    }

    // Advertising StateFlows
    private val _oauthStatusList = MutableStateFlow<List<OAuthPlatformStatusDto>>(emptyList())
    val oauthStatusList: StateFlow<List<OAuthPlatformStatusDto>> = _oauthStatusList.asStateFlow()

    private val _overviewData = MutableStateFlow<AdvertisingOverviewData?>(null)
    val overviewData: StateFlow<AdvertisingOverviewData?> = _overviewData.asStateFlow()

    private val _creativesList = MutableStateFlow<List<AdvertisingCreativeDto>>(emptyList())
    val creativesList: StateFlow<List<AdvertisingCreativeDto>> = _creativesList.asStateFlow()

    private val _metaAccounts = MutableStateFlow<List<AdAccountItemDto>>(emptyList())
    val metaAccounts: StateFlow<List<AdAccountItemDto>> = _metaAccounts.asStateFlow()

    private val _googleAccounts = MutableStateFlow<List<AdAccountItemDto>>(emptyList())
    val googleAccounts: StateFlow<List<AdAccountItemDto>> = _googleAccounts.asStateFlow()

    private val _selectedMetaAccount = MutableStateFlow<String>("")
    val selectedMetaAccount: StateFlow<String> = _selectedMetaAccount.asStateFlow()

    private val _selectedGoogleAccount = MutableStateFlow<String>("")
    val selectedGoogleAccount: StateFlow<String> = _selectedGoogleAccount.asStateFlow()

    private val _isSyncingPlatforms = MutableStateFlow(false)
    val isSyncingPlatforms: StateFlow<Boolean> = _isSyncingPlatforms.asStateFlow()

    private val _advertisingSelectedDays = MutableStateFlow(7)
    val advertisingSelectedDays: StateFlow<Int> = _advertisingSelectedDays.asStateFlow()

    private val _advertisingPlatformFilter = MutableStateFlow("All")
    val advertisingPlatformFilter: StateFlow<String> = _advertisingPlatformFilter.asStateFlow()

    private val _advertisingStatusFilter = MutableStateFlow("All")
    val advertisingStatusFilter: StateFlow<String> = _advertisingStatusFilter.asStateFlow()

    private val _advertisingCampaignQuery = MutableStateFlow("")
    val advertisingCampaignQuery: StateFlow<String> = _advertisingCampaignQuery.asStateFlow()

    private val _advertisingSortOption = MutableStateFlow("Spend")
    val advertisingSortOption: StateFlow<String> = _advertisingSortOption.asStateFlow()

    fun loadAllAdvertisingApiData() {
        fetchOAuthStatus()
        fetchAdvertisingOverview(_advertisingSelectedDays.value)
        fetchCreatives(10)
        fetchCampaigns()
        fetchAccountsForPlatform("META_ADS")
        fetchAccountsForPlatform("GOOGLE_ADS")
    }

    fun onAdvertisingTimeframeSelected(days: Int) {
        _advertisingSelectedDays.value = days
        fetchAdvertisingOverview(days)
    }

    fun onAdvertisingPlatformFilterChange(platform: String) {
        _advertisingPlatformFilter.value = platform
    }

    fun onAdvertisingStatusFilterChange(status: String) {
        _advertisingStatusFilter.value = status
    }

    fun onAdvertisingCampaignQueryChange(query: String) {
        _advertisingCampaignQuery.value = query
    }

    fun onAdvertisingSortChange(sort: String) {
        _advertisingSortOption.value = sort
    }

    fun onMetaAccountSelected(accountId: String) {
        _selectedMetaAccount.value = accountId
    }

    fun onGoogleAccountSelected(accountId: String) {
        _selectedGoogleAccount.value = accountId
    }

    fun fetchOAuthStatus() {
        viewModelScope.launch {
            val result = repo.request<OAuthStatusResponse> { get(OAUTH_STATUS_URL) }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _oauthStatusList.value = res.data
                        res.data.find { it.platform == "META_ADS" }?.adAccountId?.let {
                            if (it.isNotBlank()) _selectedMetaAccount.value = it
                        }
                        res.data.find { it.platform == "GOOGLE_ADS" }?.adAccountId?.let {
                            if (it.isNotBlank()) _selectedGoogleAccount.value = it
                        }
                    }
                },
                onFailure = { /* Fail silently */ }
            )
        }
    }

    fun fetchAccountsForPlatform(platform: String) {
        viewModelScope.launch {
            val result = repo.request<AvailableAdAccountsResponse> {
                get("$AD_ACCOUNTS_URL/$platform")
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        if (platform == "META_ADS") _metaAccounts.value = res.data
                        else if (platform == "GOOGLE_ADS") _googleAccounts.value = res.data
                    }
                },
                onFailure = { /* Fail silently */ }
            )
        }
    }

    fun saveSelectedAccount(platform: String, accountId: String) {
        viewModelScope.launch {
            val result = repo.request<SyncPlatformResponse> {
                put("$AD_ACCOUNTS_URL/$platform", mapOf("accountId" to accountId))
            }
            result.fold(
                onSuccess = {
                    fetchOAuthStatus()
                },
                onFailure = { /* Fail silently */ }
            )
        }
    }

    fun triggerPlatformSync() {
        viewModelScope.launch {
            _isSyncingPlatforms.value = true
            val result = repo.request<SyncPlatformResponse> {
                post(AD_PLATFORM_SYNC_URL, emptyMap<String, String>())
            }
            result.fold(
                onSuccess = {
                    fetchOAuthStatus()
                    fetchAdvertisingOverview(_advertisingSelectedDays.value)
                    fetchCampaigns()
                    fetchCreatives(10)
                },
                onFailure = { /* Fail silently */ }
            )
            _isSyncingPlatforms.value = false
        }
    }

    fun fetchAdvertisingOverview(days: Int = 7) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repo.request<AdvertisingOverviewResponse> {
                get(ADVERTISING_OVERVIEW_URL, repo.query("days" to days))
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _overviewData.value = res.data
                    }
                },
                onFailure = { /* Fail silently */ }
            )
            _isLoading.value = false
        }
    }

    fun fetchCreatives(limit: Int = 10) {
        viewModelScope.launch {
            val result = repo.request<AdvertisingCreativesResponse> {
                get(ADVERTISING_CREATIVES_URL, repo.query("limit" to limit))
            }
            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _creativesList.value = res.data
                    }
                },
                onFailure = { /* Fail silently */ }
            )
        }
    }

    private fun formatIsoSyncDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(iso) ?: return "--"
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            "--"
        }
    }

    /**
     * Formats an ISO date string (YYYY-MM-DD) to a readable format (e.g., "14 Oct 2026").
     */
    fun formatMarketingDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateString)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: dateString
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * Formats month name and year label for calendar header (e.g. "October 2026").
     */
    fun formatCalendarHeaderLabel(): String {
        val monthName = _calendarData.value?.calendarMatrix?.monthName
        val year = _calendarData.value?.calendarMatrix?.year ?: _currentCalendarYear.value
        return if (!monthName.isNullOrBlank()) {
            "$monthName $year"
        } else {
            val cal = Calendar.getInstance().apply {
                set(Calendar.MONTH, _currentCalendarMonth.value - 1)
                set(Calendar.YEAR, _currentCalendarYear.value)
            }
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            formatter.format(cal.time)
        }
    }
}