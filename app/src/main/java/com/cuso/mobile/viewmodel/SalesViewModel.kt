package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.database.entities.LeadEntity
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.database.entities.SalesSummaryEntity
import com.cuso.mobile.model.sales.CategoryItem
import com.cuso.mobile.model.sales.CreateLeadFormRequest
import com.cuso.mobile.model.sales.CustomerSearchResponse
import com.cuso.mobile.model.sales.LeadData
import com.cuso.mobile.model.sales.LeadTableItem
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.model.sales.ViewOneLeadData
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import com.cuso.mobile.database.dao.SelectedGarmentDao
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.AddOrgGarmentResponse
import com.cuso.mobile.model.OrgGarmentCategory
import com.cuso.mobile.model.RemoveOrgGarmentResponse
import com.cuso.mobile.model.sales.ConvertToOrderData
import com.cuso.mobile.model.sales.GarmentCategory
import com.cuso.mobile.model.sales.OrderItem
import com.cuso.mobile.model.sales.StatusData
import com.cuso.mobile.utils.launchBusy
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "SalesViewModel"

@HiltViewModel
@Suppress("unused")
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository,
    private val selectedGarmentDao: SelectedGarmentDao
) : ViewModel() {

    // ── Common Categories ─────────────────────────────────────────
    private val _orgGarmentCategories = MutableStateFlow<List<OrgGarmentCategory>>(emptyList())
    val orgGarmentCategories: StateFlow<List<OrgGarmentCategory>> = _orgGarmentCategories.asStateFlow()

    private val _isLoadingOrgGarments = MutableStateFlow(false)
    val isLoadingOrgGarments: StateFlow<Boolean> = _isLoadingOrgGarments.asStateFlow()

    private val _orgGarmentError = MutableStateFlow<String?>(null)
    val orgGarmentError: StateFlow<String?> = _orgGarmentError.asStateFlow()

    // ── Active Org Garment IDs ────────────────────────────────────
    private val _activeOrgCategoryIds = MutableStateFlow<List<String>>(emptyList())
    val activeOrgCategoryIds: StateFlow<List<String>> = _activeOrgCategoryIds.asStateFlow()

    // ── Add Garment State ─────────────────────────────────────────
    private val _addGarmentState = MutableStateFlow<SaleState<AddOrgGarmentResponse>>(SaleState.Idle)
    val addGarmentState: StateFlow<SaleState<AddOrgGarmentResponse>> = _addGarmentState.asStateFlow()

    private val _isAddingGarment = MutableStateFlow(false)
    val isAddingGarment: StateFlow<Boolean> = _isAddingGarment.asStateFlow()

    // ── Remove Garment State ──────────────────────────────────────
    private val _removeGarmentState = MutableStateFlow<SaleState<RemoveOrgGarmentResponse>>(SaleState.Idle)
    val removeGarmentState: StateFlow<SaleState<RemoveOrgGarmentResponse>> = _removeGarmentState.asStateFlow()

    private val _isRemovingGarment = MutableStateFlow(false)
    val isRemovingGarment: StateFlow<Boolean> = _isRemovingGarment.asStateFlow()

    // ── Other States ──────────────────────────────────────────────
    private val _fetchState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val fetchState: StateFlow<SaleState<Unit>> = _fetchState.asStateFlow()

    private val _leadState = MutableStateFlow<SaleState<LeadData>>(SaleState.Idle)
    val leadState: StateFlow<SaleState<LeadData>> = _leadState.asStateFlow()

    private val _updateState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val updateState: StateFlow<SaleState<Unit>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<SaleState<Unit>>(SaleState.Idle)
    val deleteState: StateFlow<SaleState<Unit>> = _deleteState.asStateFlow()

    val salesStatuses: StateFlow<List<SalesStatusEntity>> =
        repository.getSalesStatuses()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesSummary: StateFlow<SalesSummaryEntity?> =
        repository.getSalesSummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _staffList = MutableStateFlow<List<StaffDto>>(emptyList())
    val staffList: StateFlow<List<StaffDto>> = _staffList.asStateFlow()

    private val _selectedStaffId = MutableStateFlow("")
    val selectedStaffId: StateFlow<String> = _selectedStaffId.asStateFlow()

    private val _isLoadingStaff = MutableStateFlow(false)
    val isLoadingStaff: StateFlow<Boolean> = _isLoadingStaff.asStateFlow()

    private val _staffError = MutableStateFlow<String?>(null)
    val staffError: StateFlow<String?> = _staffError.asStateFlow()

    private val _selectedLead = MutableStateFlow<LeadEntity?>(null)
    val selectedLead: StateFlow<LeadEntity?> = _selectedLead.asStateFlow()

    private val _isLoadingLeadDetails = MutableStateFlow(false)
    val isLoadingLeadDetails: StateFlow<Boolean> = _isLoadingLeadDetails.asStateFlow()

    private val _leadDetailsError = MutableStateFlow<String?>(null)
    val leadDetailsError: StateFlow<String?> = _leadDetailsError.asStateFlow()

    val leads: StateFlow<List<LeadEntity>> =
        repository.getLeads()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _tableLeads = MutableStateFlow<List<LeadTableItem>>(emptyList())
    val tableLeads: StateFlow<List<LeadTableItem>> = _tableLeads.asStateFlow()

    private val _isLoadingTableLeads = MutableStateFlow(false)
    val isLoadingTableLeads: StateFlow<Boolean> = _isLoadingTableLeads.asStateFlow()

    private val _tableError = MutableStateFlow<String?>(null)
    val tableError: StateFlow<String?> = _tableError.asStateFlow()

    private val _garmentCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val garmentCategories: StateFlow<List<CategoryItem>> = _garmentCategories.asStateFlow()

    private var isFetchingLeadDetails = false

    // ── Dropdown Options ─────────────────────────────────────────
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

    // ── Selected Garments (Room DB) ──────────────────────────────
    private val _selectedGarments = MutableStateFlow<List<SelectedGarment>>(emptyList())
    val selectedGarments: StateFlow<List<SelectedGarment>> = _selectedGarments.asStateFlow()

    private var currentGarmentSessionId = "draft_order"

    fun initGarmentSession(userId: String) {
        currentGarmentSessionId = "draft_order_$userId"
    }

    // ── Customer Search ───────────────────────────────────────────
    private val _customerSearchResult = MutableStateFlow<CustomerSearchResponse?>(null)
    val customerSearchResult: StateFlow<CustomerSearchResponse?> = _customerSearchResult.asStateFlow()

    private val _isSearchingCustomer = MutableStateFlow(false)
    val isSearchingCustomer: StateFlow<Boolean> = _isSearchingCustomer.asStateFlow()

    private var searchJob: Job? = null

    // ── Update Order (Edit flow) ──
    private val _updateOrderState = MutableStateFlow<SaleState<OrderItem>>(SaleState.Idle)
    val updateOrderState: StateFlow<SaleState<OrderItem>> = _updateOrderState.asStateFlow()

    //convert to order
    private val _convertOrderState = MutableStateFlow<ConvertOrderState>(ConvertOrderState.Idle)
    val convertOrderState: StateFlow<ConvertOrderState> = _convertOrderState.asStateFlow()

    // ── Infinite scroll state ─────────────────────────────
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()


    fun searchCustomerByMobile(mobile: String, countryCode: String) {
        searchJob?.cancel()
        if (mobile.length < 4) {
            _customerSearchResult.value = null
            _isSearchingCustomer.value = false   //   clear stale spinner
            return
        }
        searchJob =  viewModelScope.launch {
            delay(400.milliseconds)
            _isSearchingCustomer.value = true

            val callStart = System.currentTimeMillis()

            val fullNumber = countryCode
                .replace("+", "")
                .plus(mobile.trim())

            Log.d(TAG, "🔍 Searching customer: $fullNumber")   //   — debug log

            val result = repository.searchCustomerByMobile(fullNumber)

            result
                .onSuccess {
                    Log.d(TAG, "  Customer search success: ${it.customer?.name}")
                    _customerSearchResult.value = it
                }
                .onFailure {
                    Log.e(TAG, "Customer search failed: ${it.message}")
                    _customerSearchResult.value = null
                }

            //   ensures spinner is visible for at least 400ms — forces a real
            // suspension point so Compose can't collapse true→false into one frame
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

    // ── Staff ─────────────────────────────────────────────────────
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

    fun selectStaff(staffId: String) { _selectedStaffId.value = staffId }
    fun getSelectedStaffId(): String = _selectedStaffId.value

    // ── Sales Data ────────────────────────────────────────────────
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
                Log.d(TAG, "  All dropdown options fetched successfully")
            } catch (e: Exception) {
                Log.e(TAG, " Error fetching dropdown options: ${e.message}")
            } finally {
                _isLoadingSources.value = false
            }
        }
    }
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _pageSize = MutableStateFlow(10)
    val pageSize: StateFlow<Int> = _pageSize.asStateFlow()

    fun setPageSize(size: Int) {
        _pageSize.value = size
        _currentPage.value = 1
        fetchTableLeads()
    }

    fun goToPage(page: Int) {
        _currentPage.value = page
        fetchTableLeads()
    }
    //    — pagination state


    private val _totalLeads = MutableStateFlow(0)
    val totalLeads: StateFlow<Int> = _totalLeads.asStateFlow()

    //    — pagination actions
    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
        fetchTableLeads()
    }

    fun onItemsPerPageChange(newSize: Int) {
        _pageSize.value = newSize
        _currentPage.value = 1
        fetchTableLeads()
    }


    fun fetchTableLeads(reset: Boolean = true) {
        if (_isLoadingTableLeads.value || _isLoadingMore.value) return   // duplicate call guard

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

    // ── Lead Details ──────────────────────────────────────────────
    fun selectLead(lead: LeadEntity) { _selectedLead.value = lead }
    fun clearSelectedLead() { _selectedLead.value = null }

    fun fetchLeadDetails(leadId: String, onComplete: (Boolean) -> Unit) {
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

    private fun convertToLeadEntity(data: ViewOneLeadData): LeadEntity {
        fun extractGarmentIds(garments: List<GarmentCategory>?) =
            garments?.mapNotNull { it._id }?.joinToString(",") ?: ""
        fun extractStatusName(status: StatusData) = status.name

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
            address = data.contact?.address ?: "",
            area = data.contact?.area ?: "",
            city = data.contact?.city ?: "",
            preferredContactMethod = data.contact?.preferredContactMethod ?: "",
            enquiryType = data.enquiryType,
            estimatedQuantity = data.estimatedQuantity ?: 0,
            budgetMin = data.budgetRange?.min ?: 0,
            budgetMax = data.budgetRange?.max ?: 0,
            occasion = data.occasion ?: "",
            garments = extractGarmentIds(data.garmentCategory),
            enquiryDate = data.enquiryDate,
            requiredDate = data.requiredDate ?: "",
            source = data.source,
            leadOwner = data.leadOwner?._id ?: "",
            appointmentRequired = data.appointment?.isRequired ?: false,
            appointmentDate = data.appointment?.date ?: "",
            appointmentTime = data.appointment?.time ?: "",
            assignedStaff = data.appointment?.assignedStaff?._id,
            priority = data.appointment?.priority ?: "",
            followUpDate = data.appointment?.followUpDate ?: "",
            internalNotes = data.notes?.find { it.type == "internal" }?.message ?: "",
            customerNotes = data.notes?.find { it.type == "customer" }?.message ?: ""
        )
    }
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


    // ── Lead CRUD ─────────────────────────────────────────────────
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

    // ── Org Garment Categories ────────────────────────────────────
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
                .onFailure { Log.e(TAG, " fetchActiveOrgGarments error: ${it.message}") }
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

    fun resetAddGarmentState() { _addGarmentState.value = SaleState.Idle; _isAddingGarment.value = false }
    fun resetRemoveGarmentState() { _removeGarmentState.value = SaleState.Idle; _isRemovingGarment.value = false }

    fun fetchGarmentCategories() {
        viewModelScope.launch {
            repository.fetchGarmentCategories()
                .onSuccess { _garmentCategories.value = it }
                .onFailure { Log.e(TAG, "Error: ${it.message}") }
        }
    }

    // ── Selected Garments (Room DB) ──────────────────────────────
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
                Log.e(TAG, "❌ Failed to save garment: ${e.message}")
            }
        }
    }

    fun deleteSelectedGarment(garmentId: String) {
        launchBusy {
            try { selectedGarmentDao.deleteGarmentById(garmentId) }
            catch (e: Exception) { Log.e(TAG, "❌ Failed to delete: ${e.message}") }
        }
    }

    fun clearAllSelectedGarments() {
        launchBusy {
            try { selectedGarmentDao.clearSession(currentGarmentSessionId) }
            catch (e: Exception) { Log.e(TAG, "❌ Failed to clear: ${e.message}") }
        }
    }

    // ── Reset States ──────────────────────────────────────────────
    fun resetLeadState() { _leadState.value = SaleState.Idle }
    fun resetDeleteState() { _deleteState.value = SaleState.Idle }
    fun resetUpdateState() { _updateState.value = SaleState.Idle }
    fun resetLeadDetailsState() {
        _isLoadingLeadDetails.value = false
        _leadDetailsError.value = null
        isFetchingLeadDetails = false
    }


    fun resetConvertOrderState() {
        _convertOrderState.value = ConvertOrderState.Idle
    }



}

sealed class SaleState<out T> {
    object Idle : SaleState<Nothing>()
    object Loading : SaleState<Nothing>()
    data class Success<T>(val data: T) : SaleState<T>()
    data class Error(val message: String) : SaleState<Nothing>()
}

sealed class ConvertOrderState {
    object Idle : ConvertOrderState()
    object Loading : ConvertOrderState()
    data class Success(val data: ConvertToOrderData) : ConvertOrderState()
    data class Error(val message: String) : ConvertOrderState()
}