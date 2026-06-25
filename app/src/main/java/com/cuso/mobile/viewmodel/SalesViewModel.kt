// com/cuso/mobile/viewmodel/SalesViewModel.kt

package com.cuso.mobile.viewmodel

import AddOrgGarmentResponse
import OrgGarmentCategory
import RemoveOrgGarmentResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.database.entities.LeadEntity
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.database.entities.SalesSummaryEntity
import com.cuso.mobile.model.CategoryItem
import com.cuso.mobile.model.CreateLeadFormRequest
import com.cuso.mobile.model.LeadData
import com.cuso.mobile.model.LeadTableItem
import com.cuso.mobile.model.StaffDto
import com.cuso.mobile.model.ViewOneLeadData
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import com.cuso.mobile.model.StatusData
import javax.inject.Inject

private const val TAG = "SalesViewModel"

@HiltViewModel
@Suppress("unused")
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    // ── Org Garment Categories ──
    private val _orgGarmentCategories = MutableStateFlow<List<OrgGarmentCategory>>(emptyList())
    val orgGarmentCategories: StateFlow<List<OrgGarmentCategory>> = _orgGarmentCategories.asStateFlow()

    private val _isLoadingOrgGarments = MutableStateFlow(false)
    val isLoadingOrgGarments: StateFlow<Boolean> = _isLoadingOrgGarments.asStateFlow()

    private val _orgGarmentError = MutableStateFlow<String?>(null)
    val orgGarmentError: StateFlow<String?> = _orgGarmentError.asStateFlow()

    // ── Add Garment State ──
    private val _addGarmentState = MutableStateFlow<SaleState<AddOrgGarmentResponse>>(SaleState.Idle)
    val addGarmentState: StateFlow<SaleState<AddOrgGarmentResponse>> = _addGarmentState.asStateFlow()

    private val _isAddingGarment = MutableStateFlow(false)
    val isAddingGarment: StateFlow<Boolean> = _isAddingGarment.asStateFlow()

    // ── Remove Garment State ──
    private val _removeGarmentState = MutableStateFlow<SaleState<RemoveOrgGarmentResponse>>(SaleState.Idle)
    val removeGarmentState: StateFlow<SaleState<RemoveOrgGarmentResponse>> = _removeGarmentState.asStateFlow()

    private val _isRemovingGarment = MutableStateFlow(false)
    val isRemovingGarment: StateFlow<Boolean> = _isRemovingGarment.asStateFlow()

    // ── Fetch state ──
    private val _fetchState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val fetchState: StateFlow<SaleState<Unit>> = _fetchState

    // ── Lead creation state ──
    private val _leadState = MutableStateFlow<SaleState<LeadData>>(SaleState.Idle)
    val leadState: StateFlow<SaleState<LeadData>> = _leadState

    // ── Lead update state ──
    private val _updateState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val updateState: StateFlow<SaleState<Unit>> = _updateState.asStateFlow()

    // ── Lead delete state ──
    private val _deleteState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val deleteState: StateFlow<SaleState<Unit>> = _deleteState.asStateFlow()

    // ── Sales Statuses from Room ──
    val salesStatuses: StateFlow<List<SalesStatusEntity>> =
        repository.getSalesStatuses()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Sales Summary from Room ──
    val salesSummary: StateFlow<SalesSummaryEntity?> =
        repository.getSalesSummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Staff list ──
    private val _staffList = MutableStateFlow<List<StaffDto>>(emptyList())
    val staffList: StateFlow<List<StaffDto>> = _staffList.asStateFlow()

    private val _selectedStaffId = MutableStateFlow("")
    val selectedStaffId: StateFlow<String> = _selectedStaffId.asStateFlow()

    private val _isLoadingStaff = MutableStateFlow(false)
    val isLoadingStaff: StateFlow<Boolean> = _isLoadingStaff.asStateFlow()

    private val _staffError = MutableStateFlow<String?>(null)
    val staffError: StateFlow<String?> = _staffError.asStateFlow()

    // ── Selected Lead for View/Edit ──
    private val _selectedLead = MutableStateFlow<LeadEntity?>(null)
    val selectedLead: StateFlow<LeadEntity?> = _selectedLead.asStateFlow()

    // ── Lead details loading states ──
    private val _isLoadingLeadDetails = MutableStateFlow(false)
    val isLoadingLeadDetails: StateFlow<Boolean> = _isLoadingLeadDetails.asStateFlow()

    private val _leadDetailsError = MutableStateFlow<String?>(null)
    val leadDetailsError: StateFlow<String?> = _leadDetailsError.asStateFlow()

    // ── Leads from Room ──
    val leads: StateFlow<List<LeadEntity>> =
        repository.getLeads()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // ── Table leads from API (no Room save) ──
    private val _tableLeads = MutableStateFlow<List<LeadTableItem>>(emptyList())
    val tableLeads: StateFlow<List<LeadTableItem>> = _tableLeads.asStateFlow()

    private val _isLoadingTableLeads = MutableStateFlow(false)
    val isLoadingTableLeads: StateFlow<Boolean> = _isLoadingTableLeads.asStateFlow()

    private val _tableError = MutableStateFlow<String?>(null)
    val tableError: StateFlow<String?> = _tableError.asStateFlow()

    // ── Garment Categories ──
    private val _garmentCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val garmentCategories: StateFlow<List<CategoryItem>> = _garmentCategories.asStateFlow()

    // ── Flag to prevent duplicate API calls ──
    private var isFetchingLeadDetails = false

    // ─────────────────────────────────────────────────────────────
    // Staff Functions
    // ─────────────────────────────────────────────────────────────

    fun fetchStaff() {
        viewModelScope.launch {
            _isLoadingStaff.value = true
            _staffError.value = null

            repository.getStaff()
                .onSuccess { staff ->
                    _staffList.value = staff
                    if (staff.isNotEmpty()) {
                        _selectedStaffId.value = staff.first().id
                    }
                }
                .onFailure { exception ->
                    _staffError.value = exception.message
                }

            _isLoadingStaff.value = false
        }
    }

    fun selectStaff(staffId: String) {
        _selectedStaffId.value = staffId
    }

    fun getSelectedStaffId(): String {
        return _selectedStaffId.value
    }

    // ─────────────────────────────────────────────────────────────
    // Sales Data Functions
    // ─────────────────────────────────────────────────────────────

    fun fetchSalesData() {
        viewModelScope.launch {
            _fetchState.value = SaleState.Loading
            try {
                repository.fetchAndSaveSalesStatuses()
                repository.fetchAndSaveSummary()
                _fetchState.value = SaleState.Success(Unit)
            } catch (e: Exception) {
                _fetchState.value = SaleState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Table Leads Functions (API only)
    // ─────────────────────────────────────────────────────────────

    fun fetchTableLeads() {
        viewModelScope.launch {
            _isLoadingTableLeads.value = true
            _tableError.value = null
            try {
                val result = repository.fetchTableData()
                result.onSuccess { leads ->
                    _tableLeads.value = leads
                }.onFailure { error ->
                    _tableError.value = error.message
                }
            } catch (e: Exception) {
                _tableError.value = e.message
            } finally {
                _isLoadingTableLeads.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Lead Details Functions (View/Edit)
    // ─────────────────────────────────────────────────────────────

    fun selectLead(lead: LeadEntity) {
        _selectedLead.value = lead
    }

    fun clearSelectedLead() {
        _selectedLead.value = null
    }

    fun fetchLeadDetails(leadId: String, onComplete: (Boolean) -> Unit) {
        if (isFetchingLeadDetails) {
            Log.d(TAG, "⏳ Already fetching lead details, skipping duplicate call for ID: $leadId")
            return
        }

        viewModelScope.launch {
            isFetchingLeadDetails = true
            _isLoadingLeadDetails.value = true
            _leadDetailsError.value = null

            try {
                Log.d(TAG, "🔄 Fetching lead details for ID: $leadId")
                val result = repository.fetchFullLeadDetails(leadId)
                result.onSuccess { leadData ->
                    Log.d(TAG, "✅ Lead details fetched successfully for ID: $leadId")
                    val leadEntity = convertToLeadEntity(leadData)
                    _selectedLead.value = leadEntity
                    onComplete(true)
                }.onFailure { error ->
                    Log.e(TAG, "❌ Failed to fetch lead details: ${error.message}")
                    _leadDetailsError.value = error.message
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}")
                _leadDetailsError.value = e.message
                onComplete(false)
            } finally {
                _isLoadingLeadDetails.value = false
                isFetchingLeadDetails = false
            }
        }
    }

    private fun convertToLeadEntity(data: ViewOneLeadData): LeadEntity {
        fun extractGarmentIds(garments: List<String>?): String {
            return garments?.joinToString(",") ?: ""
        }

        fun extractStatusName(status: StatusData): String {
            return status.name
        }

        return LeadEntity(
            id = data._id,
            customerType = data.customerType,
            status = extractStatusName(data.status),
            createdAt = data.createdAt,
            fullName = data.person?.name ?: "",
            phone = data.person?.phone ?: "",
            email = data.person?.email ?: "",
            gender = data.person?.gender ?: "",
            dob = data.person?.dob ?: "",
            address = "",
            area = "",
            city = "",
            preferredContactMethod = "",
            enquiryType = data.enquiryType,
            estimatedQuantity = data.estimatedQuantity ?: 0,
            budgetMin = data.budgetRange?.min ?: 0,
            budgetMax = data.budgetRange?.max ?: 0,
            occasion = data.occasion ?: "",
            garments = extractGarmentIds(data.garmentCategory),
            enquiryDate = data.enquiryDate,
            requiredDate = data.requiredDate ?: "",
            source = data.source,
            appointmentRequired = data.appointment?.isRequired ?: false,
            appointmentDate = data.appointment?.date ?: "",
            appointmentTime = data.appointment?.time ?: "",
            assignedStaff = data.appointment?.assignedStaff?._id ?: "",
            priority = data.appointment?.priority ?: "",
            followUpDate = data.appointment?.followUpDate ?: "",
            internalNotes = data.notes?.find { it.type == "internal" }?.content ?: "",
            customerNotes = data.notes?.find { it.type == "customer" }?.content ?: ""
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Lead CRUD Operations (API only - no Room)
    // ─────────────────────────────────────────────────────────────

    fun createLead(request: CreateLeadFormRequest) {
        viewModelScope.launch {
            _leadState.value = SaleState.Loading
            repository.createLead(request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _leadState.value = SaleState.Success(response.data)
                    } else {
                        _leadState.value = SaleState.Error("Failed to create lead")
                    }
                },
                onFailure = {
                    _leadState.value = SaleState.Error(it.message ?: "Unknown error")
                }
            )
        }
    }

    fun updateLeadById(leadId: String, request: CreateLeadFormRequest) {
        viewModelScope.launch {
            _updateState.value = SaleState.Loading
            try {
                Log.d(TAG, "🔄 Updating lead with ID: $leadId")
                val response = repository.updateLead(leadId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "✅ Lead updated successfully for ID: $leadId")
                    _updateState.value = SaleState.Success(Unit)
                    fetchTableLeads()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    Log.e(TAG, "❌ Update failed [$code]: $errorBody")
                    _updateState.value = SaleState.Error("Update failed [$code]: $errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during update: ${e.message}")
                _updateState.value = SaleState.Error("Exception: ${e.message}")
            }
        }
    }

    fun deleteLead(id: String) {
        viewModelScope.launch {
            _deleteState.value = SaleState.Loading
            try {
                val result = repository.deleteLead(id)
                result.onSuccess {
                    _deleteState.value = SaleState.Success(Unit)
                    fetchTableLeads()
                }.onFailure { error ->
                    _deleteState.value = SaleState.Error(error.message ?: "Delete failed")
                }
            } catch (e: Exception) {
                _deleteState.value = SaleState.Error(e.message ?: "Delete failed")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Org Garment Categories Functions
    // ─────────────────────────────────────────────────────────────

    fun fetchOrgGarmentCategories() {
        viewModelScope.launch {
            _isLoadingOrgGarments.value = true
            _orgGarmentError.value = null

            val result = repository.fetchOrgGarmentCategories()
            result.onSuccess { categories ->
                _orgGarmentCategories.value = categories
            }.onFailure { error ->
                _orgGarmentError.value = error.message
                Log.e(TAG, "Error fetching org garment categories: ${error.message}")
            }

            _isLoadingOrgGarments.value = false
        }
    }

    fun addOrgGarmentCategory(categoryId: String) {
        viewModelScope.launch {
            _isAddingGarment.value = true
            _addGarmentState.value = SaleState.Loading

            val result = repository.addOrgGarmentCategory(categoryId)
            result.onSuccess { response ->
                if (response.success) {
                    _addGarmentState.value = SaleState.Success(response)
                    fetchOrgGarmentCategories()
                } else {
                    _addGarmentState.value = SaleState.Error("Failed to add category")
                }
            }.onFailure { error ->
                _addGarmentState.value = SaleState.Error(error.message ?: "Unknown error")
                Log.e(TAG, "Error adding garment category: ${error.message}")
            }

            _isAddingGarment.value = false
        }
    }

    fun removeOrgGarmentCategory(categoryId: String) {
        viewModelScope.launch {
            _isRemovingGarment.value = true
            _removeGarmentState.value = SaleState.Loading

            val result = repository.removeOrgGarmentCategory(categoryId)
            result.onSuccess { response ->
                if (response.success) {
                    _removeGarmentState.value = SaleState.Success(response)
                    fetchOrgGarmentCategories()
                } else {
                    _removeGarmentState.value = SaleState.Error("Failed to remove category")
                }
            }.onFailure { error ->
                _removeGarmentState.value = SaleState.Error(error.message ?: "Unknown error")
                Log.e(TAG, "Error removing garment category: ${error.message}")
            }

            _isRemovingGarment.value = false
        }
    }

    fun resetAddGarmentState() {
        _addGarmentState.value = SaleState.Idle
    }

    fun resetRemoveGarmentState() {
        _removeGarmentState.value = SaleState.Idle
    }

    // ─────────────────────────────────────────────────────────────
    // Garment Categories (API only - no Room)
    // ─────────────────────────────────────────────────────────────

    fun fetchGarmentCategories() {
        viewModelScope.launch {
            val result = repository.fetchGarmentCategories()
            result.onSuccess { categories ->
                _garmentCategories.value = categories
            }.onFailure { error ->
                Log.e(TAG, "Error fetching garment categories: ${error.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Reset States
    // ─────────────────────────────────────────────────────────────

    fun resetLeadState() {
        _leadState.value = SaleState.Idle
    }

    fun resetDeleteState() {
        _deleteState.value = SaleState.Idle
    }

    fun resetUpdateState() {
        _updateState.value = SaleState.Idle
    }

    fun resetLeadDetailsState() {
        _isLoadingLeadDetails.value = false
        _leadDetailsError.value = null
        isFetchingLeadDetails = false
    }
}

sealed class SaleState<out T> {
    object Idle : SaleState<Nothing>()
    object Loading : SaleState<Nothing>()
    data class Success<T>(val data: T) : SaleState<T>()
    data class Error(val message: String) : SaleState<Nothing>()
}