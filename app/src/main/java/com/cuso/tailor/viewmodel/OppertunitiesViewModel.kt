package com.cuso.tailor.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.sales.*
import com.cuso.tailor.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private const val TAG = "OpportunityViewModel"

// ── UI States ──

sealed class OpportunitiesUiState {
    data object Loading : OpportunitiesUiState()
    data class Success(val items: List<OpportunityListItem>, val totalCount: Int) : OpportunitiesUiState()
    data class Error(val message: String) : OpportunitiesUiState()
}

sealed class OpportunityDetailUiState {
    data object Loading : OpportunityDetailUiState()
    data class Success(val item: OpportunityDto) : OpportunityDetailUiState()
    data class Error(val message: String) : OpportunityDetailUiState()
}

sealed interface OpportunityActionState {
    data object Idle : OpportunityActionState
    data object Loading : OpportunityActionState
    data class Success(val message: String) : OpportunityActionState
    data class Error(val message: String) : OpportunityActionState
}

@HiltViewModel
class OpportunityViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    // 1. View All State
    private val _uiState = MutableStateFlow<OpportunitiesUiState>(OpportunitiesUiState.Loading)
    val uiState: StateFlow<OpportunitiesUiState> = _uiState.asStateFlow()

    // 2. View One Detail State
    private val _detailState = MutableStateFlow<OpportunityDetailUiState>(OpportunityDetailUiState.Loading)
    val detailState: StateFlow<OpportunityDetailUiState> = _detailState.asStateFlow()

    // 3. Create State
    private val _createState = MutableStateFlow<OpportunityActionState>(OpportunityActionState.Idle)
    val createState: StateFlow<OpportunityActionState> = _createState.asStateFlow()

    // 4. Delete State
    private val _deleteState = MutableStateFlow<OpportunityActionState>(OpportunityActionState.Idle)
    val deleteState: StateFlow<OpportunityActionState> = _deleteState.asStateFlow()

    // 5. Convert Lead to Opportunity State
    private val _convertLeadState = MutableStateFlow<OpportunityActionState>(OpportunityActionState.Idle)
    val convertLeadState: StateFlow<OpportunityActionState> = _convertLeadState.asStateFlow()

    // 6. Log Activities State
    private val _activities = MutableStateFlow<List<OpportunityActivityDto>>(emptyList())
    val activities: StateFlow<List<OpportunityActivityDto>> = _activities.asStateFlow()

    // Opportunity Stages Dropdown State
    private val _opportunityStages = MutableStateFlow<List<OpportunityStageItemDto>>(emptyList())
    val opportunityStages: StateFlow<List<OpportunityStageItemDto>> = _opportunityStages.asStateFlow()

    init {
        loadOpportunities()
        fetchOpportunityStages()
    }

    // =========================================================
    // 1. VIEW ALL
    // =========================================================
    fun loadOpportunities(search: String? = null) {
        viewModelScope.launch {
            _uiState.update { OpportunitiesUiState.Loading }
            repository.getOpportunities(search = search?.takeIf { it.isNotBlank() })
                .onSuccess { response ->
                    val uiItems = response.data.map { it.toUiItem() }
                    val total = response.pagination?.total ?: uiItems.size
                    _uiState.update { OpportunitiesUiState.Success(uiItems, total) }
                }
                .onFailure { error ->
                    _uiState.update {
                        OpportunitiesUiState.Error(error.message ?: "Failed to load opportunities")
                    }
                }
        }
    }

    fun onSearch(query: String) {
        loadOpportunities(search = query)
    }

    // =========================================================
    // 2. VIEW ONE
    // =========================================================
    fun loadOpportunityDetail(opportunityId: String) {
        viewModelScope.launch {
            _detailState.update { OpportunityDetailUiState.Loading }
            repository.getOpportunityById(opportunityId)
                .onSuccess { dto ->
                    _detailState.update { OpportunityDetailUiState.Success(dto) }
                    loadActivities(opportunityId)
                }
                .onFailure { err ->
                    _detailState.update {
                        OpportunityDetailUiState.Error(err.message ?: "Failed to load opportunity details")
                    }
                }
        }
    }

    // =========================================================
    // 3. CREATE OPPORTUNITY
    // =========================================================
    fun createOpportunity(request: CreateOpportunityRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _createState.value = OpportunityActionState.Loading
            repository.createOpportunity(request)
                .onSuccess {
                    _createState.value = OpportunityActionState.Success("Opportunity created successfully")
                    loadOpportunities()
                    onSuccess()
                }
                .onFailure {
                    _createState.value = OpportunityActionState.Error(it.message ?: "Failed to create opportunity")
                }
        }
    }

    // =========================================================
    // 4. CONVERT LEAD TO OPPORTUNITY
    // =========================================================
    fun convertLeadToOpportunity(request: ConvertLeadToOpportunityRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _convertLeadState.value = OpportunityActionState.Loading
            repository.convertLeadToOpportunity(request)
                .onSuccess { response ->
                    _convertLeadState.value = OpportunityActionState.Success(response.message)
                    loadOpportunities()
                    onSuccess()
                }
                .onFailure { error ->
                    _convertLeadState.value = OpportunityActionState.Error(
                        error.message ?: "Failed to convert lead to opportunity"
                    )
                }
        }
    }

    // =========================================================
    // 5. DELETE ONE
    // =========================================================
    fun deleteOpportunity(opportunityId: String) {
        viewModelScope.launch {
            _deleteState.value = OpportunityActionState.Loading
            repository.deleteOpportunity(opportunityId)
                .onSuccess { message ->
                    _deleteState.value = OpportunityActionState.Success(message)
                    loadOpportunities()
                }
                .onFailure {
                    _deleteState.value = OpportunityActionState.Error(it.message ?: "Failed to delete opportunity")
                }
        }
    }

    // =========================================================
    // 6. ADD LOG ACTIVITY
    // =========================================================
    fun addActivityNote(
        opportunityId: String,
        note: String,
        activityType: String = "Message",
        duration: Int = 15
    ) {
        if (note.isBlank()) return
        viewModelScope.launch {
            val request = AddOpportunityActivityRequest(
                opportunityId = opportunityId,
                activityType = activityType,
                duration = duration,
                notes = note
            )
            repository.addOpportunityActivity(request)
                .onSuccess {
                    loadActivities(opportunityId)
                }
                .onFailure {
                    Log.e(TAG, "Failed to log activity: ${it.message}")
                }
        }
    }

    // =========================================================
    // 7. LOG HISTORY
    // =========================================================
    fun loadActivities(opportunityId: String) {
        viewModelScope.launch {
            repository.getOpportunityActivityHistory(opportunityId)
                .onSuccess { historyList ->
                    _activities.value = historyList
                }
                .onFailure {
                    Log.e(TAG, "Failed to load activities: ${it.message}")
                }
        }
    }

    // =========================================================
    // Opportunity Stages API
    // =========================================================
    fun fetchOpportunityStages() {
        viewModelScope.launch {
            repository.getOpportunityStages()
                .onSuccess { response ->
                    _opportunityStages.value = response.data.sortedBy { it.displayOrder }
                }
                .onFailure {
                    Log.e(TAG, "Failed to fetch stages: ${it.message}")
                }
        }
    }

    fun resetActionStates() {
        _createState.value = OpportunityActionState.Idle
        _deleteState.value = OpportunityActionState.Idle
        _convertLeadState.value = OpportunityActionState.Idle
    }

    // ── Mapping Helper ──
    private fun OpportunityDto.toUiItem(): OpportunityListItem {
        val categoryName = garmentSpecifications?.firstOrNull()?.garmentCategory?.displayName
            ?: garmentSpecifications?.firstOrNull()?.garmentCategory?.name
            ?: garmentSpecifications?.firstOrNull()?.garment?.displayName
            ?: garmentSpecifications?.firstOrNull()?.garment?.name
            ?: "Silk Sarees"

        val formattedDate = formatIsoDate(expectedClosingDate)
        val valueString = estimatedValue?.let { "₹${it.toLong()}" } ?: "₹0"

        return OpportunityListItem(
            id = id,
            code = opportunityCode ?: "—",
            title = name ?: "Untitled Deal",
            customerName = customer?.fullName ?: "Unknown Customer",
            category = categoryName,
            closingDate = formattedDate,
            estimatedValue = valueString,
            status = stage?.name ?: "New Opportunity"
        )
    }

    private fun formatIsoDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
            )
            var parsedDate: Date? = null
            for (pattern in inputFormats) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    parsedDate = sdf.parse(isoDate)
                    if (parsedDate != null) break
                } catch (_: Exception) {}
            }
            parsedDate?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(it)
            } ?: isoDate
        } catch (_: Exception) {
            isoDate
        }
    }
}