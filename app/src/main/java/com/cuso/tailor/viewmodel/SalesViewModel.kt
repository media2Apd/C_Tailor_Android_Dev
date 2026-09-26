package com.cuso.tailor.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.database.dao.SelectedGarmentDao
import com.cuso.tailor.database.entities.LeadEntity
import com.cuso.tailor.database.entities.SalesStatusEntity
import com.cuso.tailor.database.entities.SalesSummaryEntity
import com.cuso.tailor.database.entities.SelectedGarment
import com.cuso.tailor.model.login_forgotPassword_resetPassword.AddOrgGarmentResponse
import com.cuso.tailor.model.login_forgotPassword_resetPassword.OrgGarmentCategory
import com.cuso.tailor.model.login_forgotPassword_resetPassword.RemoveOrgGarmentResponse
import com.cuso.tailor.model.sales.CategoryItem
import com.cuso.tailor.model.sales.ConvertToOrderData
import com.cuso.tailor.model.sales.CreateLeadFormRequest
import com.cuso.tailor.model.sales.CustomerSearchResponse
import com.cuso.tailor.model.sales.GarmentCategoryDto
import com.cuso.tailor.model.sales.LeadData
import com.cuso.tailor.model.sales.LeadTableItem
import com.cuso.tailor.model.sales.OrderItem
import com.cuso.tailor.model.sales.StaffDto
import com.cuso.tailor.model.sales.ViewOneLeadData
import com.cuso.tailor.repository.SalesRepository
import com.cuso.tailor.repository.SettingsRepository
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "SalesViewModel"

/**
 * ViewModel managing Sales, Leads pipeline, Customer searches,
 * Production/Garment categories, and Session-based garment configurations.
 */
@HiltViewModel
@Suppress("unused")
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository,
    private val settingsRepository: SettingsRepository,
    private val selectedGarmentDao: SelectedGarmentDao
) : ViewModel() {

    // =============================================================
    // 1. Organization Garment Categories State
    // =============================================================

    private val _orgGarmentCategories = MutableStateFlow<List<OrgGarmentCategory>>(emptyList())
    val orgGarmentCategories: StateFlow<List<OrgGarmentCategory>> = _orgGarmentCategories.asStateFlow()

    private val _isLoadingOrgGarments = MutableStateFlow(false)
    val isLoadingOrgGarments: StateFlow<Boolean> = _isLoadingOrgGarments.asStateFlow()

    private val _orgGarmentError = MutableStateFlow<String?>(null)
    val orgGarmentError: StateFlow<String?> = _orgGarmentError.asStateFlow()

    private val _activeOrgCategoryIds = MutableStateFlow<List<String>>(emptyList())
    val activeOrgCategoryIds: StateFlow<List<String>> = _activeOrgCategoryIds.asStateFlow()

    // Add / Remove Category States
    private val _addGarmentState = MutableStateFlow<SaleState<AddOrgGarmentResponse>>(SaleState.Idle)
    val addGarmentState: StateFlow<SaleState<AddOrgGarmentResponse>> = _addGarmentState.asStateFlow()

    private val _isAddingGarment = MutableStateFlow(false)
    val isAddingGarment: StateFlow<Boolean> = _isAddingGarment.asStateFlow()

    private val _removeGarmentState = MutableStateFlow<SaleState<RemoveOrgGarmentResponse>>(SaleState.Idle)
    val removeGarmentState: StateFlow<SaleState<RemoveOrgGarmentResponse>> = _removeGarmentState.asStateFlow()

    private val _isRemovingGarment = MutableStateFlow(false)
    val isRemovingGarment: StateFlow<Boolean> = _isRemovingGarment.asStateFlow()

    private val _garmentCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val garmentCategories: StateFlow<List<CategoryItem>> = _garmentCategories.asStateFlow()

    // =============================================================
    // 2. Sales Summary & Status Pipeline
    // =============================================================

    private val _fetchState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val fetchState: StateFlow<SaleState<Unit>> = _fetchState.asStateFlow()

    val salesStatuses: StateFlow<List<SalesStatusEntity>> =
        repository.getSalesStatuses()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesSummary: StateFlow<SalesSummaryEntity?> =
        repository.getSalesSummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // =============================================================
    // 3. Staff & Assignments State
    // =============================================================

    private val _staffList = MutableStateFlow<List<StaffDto>>(emptyList())
    val staffList: StateFlow<List<StaffDto>> = _staffList.asStateFlow()

    private val _selectedStaffId = MutableStateFlow("")
    val selectedStaffId: StateFlow<String> = _selectedStaffId.asStateFlow()

    private val _isLoadingStaff = MutableStateFlow(false)
    val isLoadingStaff: StateFlow<Boolean> = _isLoadingStaff.asStateFlow()

    private val _staffError = MutableStateFlow<String?>(null)
    val staffError: StateFlow<String?> = _staffError.asStateFlow()

    // =============================================================
    // 4. Lead Details & Selection State
    // =============================================================

    private val _selectedLead = MutableStateFlow<LeadEntity?>(null)
    val selectedLead: StateFlow<LeadEntity?> = _selectedLead.asStateFlow()

    private val _isLoadingLeadDetails = MutableStateFlow(false)
    val isLoadingLeadDetails: StateFlow<Boolean> = _isLoadingLeadDetails.asStateFlow()

    private val _leadDetailsError = MutableStateFlow<String?>(null)
    val leadDetailsError: StateFlow<String?> = _leadDetailsError.asStateFlow()

    private var isFetchingLeadDetails = false

    val leads: StateFlow<List<LeadEntity>> =
        repository.getLeads()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lead CRUD Action States
    private val _leadState = MutableStateFlow<SaleState<LeadData>>(SaleState.Idle)
    val leadState: StateFlow<SaleState<LeadData>> = _leadState.asStateFlow()

    private val _updateState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val updateState: StateFlow<SaleState<Unit>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val deleteState: StateFlow<SaleState<Unit>> = _deleteState.asStateFlow()

    // =============================================================
    // 5. Table Leads & Pagination State
    // =============================================================

    private val _tableLeads = MutableStateFlow<List<LeadTableItem>>(emptyList())
    val tableLeads: StateFlow<List<LeadTableItem>> = _tableLeads.asStateFlow()

    private val _isLoadingTableLeads = MutableStateFlow(false)
    val isLoadingTableLeads: StateFlow<Boolean> = _isLoadingTableLeads.asStateFlow()

    private val _tableError = MutableStateFlow<String?>(null)
    val tableError: StateFlow<String?> = _tableError.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _pageSize = MutableStateFlow(10)
    val pageSize: StateFlow<Int> = _pageSize.asStateFlow()

    private val _totalLeads = MutableStateFlow(0)
    val totalLeads: StateFlow<Int> = _totalLeads.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    // =============================================================
    // 6. Dropdown Options State
    // =============================================================

    private val _leadSources = MutableStateFlow<List<String>>(emptyList())
    val leadSources: StateFlow<List<String>> = _leadSources.asStateFlow()

    private val _isLoadingSources = MutableStateFlow(false)
    val isLoadingSources: StateFlow<Boolean> = _isLoadingSources.asStateFlow()

    private val _genderOptions = MutableStateFlow<List<String>>(emptyList())
    val genderOptions: StateFlow<List<String>> = _genderOptions.asStateFlow()

    private val _preferredContactOptions = MutableStateFlow<List<String>>(emptyList())
    val preferredContactOptions: StateFlow<List<String>> = _preferredContactOptions.asStateFlow()

    private val _enquiryTypeOptions = MutableStateFlow<List<String>>(emptyList())
    val enquiryTypeOptions: StateFlow<List<String>> = _enquiryTypeOptions.asStateFlow()

    private val _priorityOptions = MutableStateFlow<List<String>>(emptyList())
    val priorityOptions: StateFlow<List<String>> = _priorityOptions.asStateFlow()

    // =============================================================
    // 7. Session-based Garment Drafts (Room DB)
    // =============================================================

    private val _selectedGarments = MutableStateFlow<List<SelectedGarment>>(emptyList())
    val selectedGarments: StateFlow<List<SelectedGarment>> = _selectedGarments.asStateFlow()

    private var currentGarmentSessionId = "draft_order"

    fun initGarmentSession(userId: String) {
        currentGarmentSessionId = "draft_order_$userId"
    }

    // =============================================================
    // 8. Customer Search & Order Conversion State
    // =============================================================

    private val _customerSearchResult = MutableStateFlow<CustomerSearchResponse?>(null)
    val customerSearchResult: StateFlow<CustomerSearchResponse?> = _customerSearchResult.asStateFlow()

    private val _isSearchingCustomer = MutableStateFlow(false)
    val isSearchingCustomer: StateFlow<Boolean> = _isSearchingCustomer.asStateFlow()

    private var searchJob: Job? = null

    private val _updateOrderState = MutableStateFlow<SaleState<OrderItem>>(SaleState.Idle)
    val updateOrderState: StateFlow<SaleState<OrderItem>> = _updateOrderState.asStateFlow()

    private val _convertOrderState = MutableStateFlow<ConvertOrderState>(ConvertOrderState.Idle)
    val convertOrderState: StateFlow<ConvertOrderState> = _convertOrderState.asStateFlow()

    // =============================================================
    // Customer Search Implementation
    // =============================================================

    /**
     * Debounces and searches for customer details using their phone number.
     */
    fun searchCustomerByMobile(mobile: String, countryCode: String) {
        searchJob?.cancel()
        if (mobile.length < 4) {
            _customerSearchResult.value = null
            _isSearchingCustomer.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(400.milliseconds)
            _isSearchingCustomer.value = true

            val callStart = System.currentTimeMillis()
            val fullNumber = countryCode.replace("+", "").plus(mobile.trim())

            Log.d(TAG, "Searching customer: $fullNumber")

            val result = repository.searchCustomerByMobile(fullNumber)
            result
                .onSuccess {
                    Log.d(TAG, "Customer search success: ${it.customer?.name}")
                    _customerSearchResult.value = it
                }
                .onFailure {
                    Log.e(TAG, "Customer search failed: ${it.message}")
                    _customerSearchResult.value = null
                }

            // Ensures spinner is visible for smooth UI transition
            val elapsed = System.currentTimeMillis() - callStart
            if (elapsed < 400) delay(400 - elapsed)

            _isSearchingCustomer.value = false
        }
    }

    fun clearCustomerSearch() {
        searchJob?.cancel()
        _customerSearchResult.value = null
        _isSearchingCustomer.value = false
    }

    // =============================================================
    // Staff Functions
    // =============================================================

    /**
     * Fetches staff list from HR endpoint and auto-selects the first staff if empty.
     */
    fun fetchStaff() {
        viewModelScope.launch {
            _isLoadingStaff.value = true
            _staffError.value = null
            repository.getStaff()
                .onSuccess { staff ->
                    _staffList.value = staff
                    if (staff.isNotEmpty()) _selectedStaffId.value = staff.first().id
                }
                .onFailure { _staffError.value = it.message }
            _isLoadingStaff.value = false
        }
    }

    fun selectStaff(staffId: String) {
        _selectedStaffId.value = staffId
    }

    fun getSelectedStaffId(): String = _selectedStaffId.value

    // =============================================================
    // Sales Data & Dropdown Options
    // =============================================================

    /**
     * Refreshes summary data, sales status counts, and dropdown options.
     */
    fun fetchSalesData() {
        viewModelScope.launch {
            _fetchState.value = SaleState.Loading
            try {
                repository.fetchAndSaveSalesStatuses()
                repository.fetchAndSaveSummary()
                fetchDropdownOptions()
                _fetchState.value = SaleState.Success(Unit)
            } catch (e: Exception) {
                _fetchState.value = SaleState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun fetchDropdownOptions() {
        viewModelScope.launch {
            _isLoadingSources.value = true
            try {
                Log.d(TAG, "Dropdown options initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing dropdown options: ${e.message}")
            } finally {
                _isLoadingSources.value = false
            }
        }
    }

    // =============================================================
    // Pagination Controls
    // =============================================================

    fun setPageSize(size: Int) {
        _pageSize.value = size
        _currentPage.value = 1
        fetchTableLeads()
    }

    fun goToPage(page: Int) {
        _currentPage.value = page
        fetchTableLeads()
    }

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
        fetchTableLeads()
    }

    fun onItemsPerPageChange(newSize: Int) {
        _pageSize.value = newSize
        _currentPage.value = 1
        fetchTableLeads()
    }

    /**
     * Fetches lead list with support for first-page reset or infinite scroll appending.
     */
    fun fetchTableLeads(reset: Boolean = true) {
        if (_isLoadingTableLeads.value || _isLoadingMore.value) return

        viewModelScope.launch {
            if (reset) {
                _currentPage.value = 1
                _canLoadMore.value = true
                _isLoadingTableLeads.value = true
            } else {
                if (!_canLoadMore.value) return@launch
                _isLoadingMore.value = true
            }
            _tableError.value = null

            try {
                repository.fetchTableData(page = _currentPage.value, limit = _pageSize.value)
                    .onSuccess {
                        _tableLeads.value = if (reset) it.leads else _tableLeads.value + it.leads
                        _totalLeads.value = it.total

                        val loadedCount = _tableLeads.value.size
                        _canLoadMore.value = loadedCount < it.total && it.leads.isNotEmpty()

                        if (_canLoadMore.value) _currentPage.value += 1
                    }
                    .onFailure { _tableError.value = it.message }
            } catch (e: Exception) {
                _tableError.value = e.message
            } finally {
                _isLoadingTableLeads.value = false
                _isLoadingMore.value = false
            }
        }
    }

    fun loadMoreLeads() {
        fetchTableLeads(reset = false)
    }

    // =============================================================
    // Lead Details & Conversion Operations
    // =============================================================

    fun selectLead(lead: LeadEntity) {
        _selectedLead.value = lead
    }

    fun clearSelectedLead() {
        _selectedLead.value = null
    }

    /**
     * Loads complete details for a single lead.
     */
    fun fetchLeadDetails(leadId: String, onComplete: (Boolean) -> Unit = {}) {
        if (isFetchingLeadDetails) return
        viewModelScope.launch {
            isFetchingLeadDetails = true
            _isLoadingLeadDetails.value = true
            _leadDetailsError.value = null
            try {
                repository.fetchFullLeadDetails(leadId)
                    .onSuccess {
                        _selectedLead.value = convertToLeadEntity(it)
                        onComplete(true)
                    }
                    .onFailure {
                        _leadDetailsError.value = it.message
                        onComplete(false)
                    }
            } catch (e: Exception) {
                _leadDetailsError.value = e.message
                onComplete(false)
            } finally {
                _isLoadingLeadDetails.value = false
                isFetchingLeadDetails = false
            }
        }
    }

    /**
     * Converts a Remote ViewOneLeadData DTO into a local Room LeadEntity.
     */
    private fun convertToLeadEntity(data: ViewOneLeadData): LeadEntity {
        val internalNotes = data.internalNotesText
            ?: data.notesList?.find { it.type == "internal" }?.message
            ?: ""

        val customerNotes = data.customerNotesText
            ?: data.notesList?.find { it.type == "customer" }?.message
            ?: ""

        return LeadEntity(
            id = data._id,
            customerType = data.customerType,
            status = data.effectiveStatus,
            createdAt = data.createdAt,
            fullName = data.effectiveName,
            phone = data.effectivePhone,
            email = data.effectiveEmail,
            gender = data.gender.orEmpty(),
            dob = data.dateOfBirth.orEmpty(),
            address = data.streetAddress,
            area = data.areaName,
            city = data.cityName,
            preferredContactMethod = data.preferredContactMethod.orEmpty(),
            enquiryType = data.enquiryType.orEmpty(),
            estimatedQuantity = data.totalQuantity,
            budgetMin = data.minBudgetVal,
            budgetMax = data.maxBudgetVal,
            occasion = data.occasion.orEmpty(),
            garments = data.effectiveGarmentIds,
            enquiryDate = data.enquiryDate,
            requiredDate = data.requiredDate.orEmpty(),
            source = data.effectiveSource,
            leadOwner = data.leadOwner?._id.orEmpty(),
            appointmentRequired = data.isAppointmentRequired ?: false,
            appointmentDate = data.appointmentDate.orEmpty(),
            appointmentTime = data.appointmentTime.orEmpty(),
            assignedStaff = data.assignedStaffId?._id.orEmpty(),
            priority = data.priorityLevel.orEmpty(),
            followUpDate = data.followUpDate.orEmpty(),
            internalNotes = internalNotes,
            customerNotes = customerNotes
        )
    }

    /**
     * Refreshes lead details silently in the background without modifying UI loading spinners.
     */
    fun silentRefreshLead(leadId: String) {
        viewModelScope.launch {
            try {
                repository.fetchFullLeadDetails(leadId)
                    .onSuccess { _selectedLead.value = convertToLeadEntity(it) }
                    .onFailure { Log.e(TAG, "Silent lead refresh failed: ${it.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Silent lead refresh exception: ${e.message}")
            }
        }
    }

    /**
     * Converts a qualified lead directly into an active Order.
     */
    fun convertLeadToOrder(leadId: String) {
        launchBusy {
            _convertOrderState.value = ConvertOrderState.Loading
            val result = repository.convertLeadToOrder(leadId)
            result.fold(
                onSuccess = { data ->
                    _convertOrderState.value = ConvertOrderState.Success(data)
                    fetchLeadDetails(leadId) { success ->
                        if (!success) {
                            Log.e(TAG, "Failed to refresh lead after conversion")
                        }
                    }
                },
                onFailure = { e ->
                    _convertOrderState.value = ConvertOrderState.Error(e.message ?: "Something went wrong")
                }
            )
        }
    }

    // =============================================================
    // Lead CRUD Operations
    // =============================================================

    fun createLead(request: CreateLeadFormRequest) {
        launchBusy {
            _leadState.value = SaleState.Loading
            repository.createLead(request).fold(
                onSuccess = { response ->
                    _leadState.value = if (response.success && response.data != null)
                        SaleState.Success(response.data)
                    else SaleState.Error("Failed to create lead")
                },
                onFailure = { _leadState.value = SaleState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun updateLeadById(leadId: String, request: CreateLeadFormRequest) {
        launchBusy {
            _updateState.value = SaleState.Loading
            try {
                val response = repository.updateLead(leadId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _updateState.value = SaleState.Success(Unit)
                    fetchTableLeads()
                } else {
                    _updateState.value = SaleState.Error("Update failed [${response.code()}]")
                }
            } catch (e: Exception) {
                _updateState.value = SaleState.Error("Exception: ${e.message}")
            }
        }
    }

    fun deleteLead(id: String) {
        launchBusy {
            _deleteState.value = SaleState.Loading
            try {
                repository.deleteLead(id)
                    .onSuccess {
                        _deleteState.value = SaleState.Success(Unit)
                        fetchTableLeads()
                    }
                    .onFailure { _deleteState.value = SaleState.Error(it.message ?: "Delete failed") }
            } catch (e: Exception) {
                _deleteState.value = SaleState.Error(e.message ?: "Delete failed")
            }
        }
    }

    // =============================================================
    // Organization Garment Categories Operations
    // =============================================================

    fun fetchOrgGarmentCategories() {
        viewModelScope.launch {
            _isLoadingOrgGarments.value = true
            _orgGarmentError.value = null
            repository.fetchOrgGarmentCategories()
                .onSuccess { _orgGarmentCategories.value = it }
                .onFailure {
                    _orgGarmentError.value = it.message
                    Log.e(TAG, "Error fetching common categories: ${it.message}")
                }
            _isLoadingOrgGarments.value = false
        }
    }

    fun fetchActiveOrgGarments() {
        viewModelScope.launch {
            repository.fetchActiveOrgGarmentIds()
                .onSuccess { _activeOrgCategoryIds.value = it }
                .onFailure { Log.e(TAG, "fetchActiveOrgGarments error: ${it.message}") }
        }
    }

    fun addOrgGarmentCategory(categoryId: String) {
        launchBusy {
            try {
                _isAddingGarment.value = true
                _addGarmentState.value = SaleState.Loading
                repository.addOrgGarmentCategory(categoryId)
                    .onSuccess { _addGarmentState.value = SaleState.Success(it) }
                    .onFailure { _addGarmentState.value = SaleState.Error(it.message ?: "Failed") }
            } catch (e: Exception) {
                _addGarmentState.value = SaleState.Error("Network error: ${e.message}")
            } finally {
                _isAddingGarment.value = false
            }
        }
    }

    fun removeOrgGarmentCategory(categoryId: String) {
        launchBusy {
            try {
                _removeGarmentState.value = SaleState.Loading
                _isRemovingGarment.value = true
                repository.removeOrgGarmentCategory(categoryId)
                    .onSuccess { response ->
                        if (response.success) _removeGarmentState.value = SaleState.Success(response)
                        else _removeGarmentState.value = SaleState.Error("Failed to remove")
                    }
                    .onFailure { _removeGarmentState.value = SaleState.Error(it.message ?: "Failed") }
            } catch (e: Exception) {
                _removeGarmentState.value = SaleState.Error("Network error: ${e.message}")
            } finally {
                _isRemovingGarment.value = false
            }
        }
    }

    fun resetAddGarmentState() {
        _addGarmentState.value = SaleState.Idle
        _isAddingGarment.value = false
    }

    fun resetRemoveGarmentState() {
        _removeGarmentState.value = SaleState.Idle
        _isRemovingGarment.value = false
    }

    fun fetchGarmentCategories() {
        viewModelScope.launch {
            repository.fetchGarmentCategories()
                .onSuccess { _garmentCategories.value = it }
                .onFailure { Log.e(TAG, "Error: ${it.message}") }
        }
    }

    /**
     * Fetches garment categories filtered by a specific garment template ID.
     */
    fun fetchGarmentCategoriesByGarmentId(
        garmentId: String,
        onResult: (List<GarmentCategoryDto>) -> Unit = {}
    ) {
        if (garmentId.isBlank()) {
            onResult(emptyList())
            return
        }
        viewModelScope.launch {
            settingsRepository.getGarmentCategoriesByGarmentId(garmentId)
                .onSuccess { categories ->
                    onResult(categories)
                }
                .onFailure { error ->
                    Log.e(TAG, "Error fetching garment categories for $garmentId: ${error.message}")
                    onResult(emptyList())
                }
        }
    }

    // =============================================================
    // Selected Garments Session Operations (Room DB)
    // =============================================================

    fun loadSelectedGarments() {
        viewModelScope.launch {
            selectedGarmentDao.getGarmentsForSession(currentGarmentSessionId)
                .collect { _selectedGarments.value = it }
        }
    }

    fun addOrUpdateGarment(garment: SelectedGarment) {
        launchBusy {
            try {
                selectedGarmentDao.insertGarment(garment.copy(orderSessionId = currentGarmentSessionId))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save garment: ${e.message}")
            }
        }
    }

    fun deleteSelectedGarment(garmentId: String) {
        launchBusy {
            try {
                selectedGarmentDao.deleteGarmentById(garmentId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete garment: ${e.message}")
            }
        }
    }

    fun clearAllSelectedGarments() {
        launchBusy {
            try {
                selectedGarmentDao.clearSession(currentGarmentSessionId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear garments session: ${e.message}")
            }
        }
    }

    // =============================================================
    // State Reset Functions
    // =============================================================

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

    fun resetConvertOrderState() {
        _convertOrderState.value = ConvertOrderState.Idle
    }
}

// =============================================================
// UI State Sealed Classes
// =============================================================

sealed class SaleState<out T> {
    data object Idle : SaleState<Nothing>()
    data object Loading : SaleState<Nothing>()
    data class Success<T>(val data: T) : SaleState<T>()
    data class Error(val message: String) : SaleState<Nothing>()
}

sealed class ConvertOrderState {
    data object Idle : ConvertOrderState()
    data object Loading : ConvertOrderState()
    data class Success(val data: ConvertToOrderData) : ConvertOrderState()
    data class Error(val message: String) : ConvertOrderState()
}