package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.model.sales.CustomerViewAddress
import com.cuso.mobile.model.sales.CustomerViewData
import com.cuso.mobile.model.sales.UpdateCustomerRequest
import com.cuso.mobile.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    // ─────────────────────────────────────────────
    // LIST STATE (Customer list screen)
    // ─────────────────────────────────────────────

    private val _uiState = MutableStateFlow<CustomerUiState>(CustomerUiState.Loading)
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    // ✅ AFTER
    private var currentPage = 1
    private var currentLimit = 10
    private var currentSearch: String? = null
    private var currentType: String? = null

    // ✅ NEW — UI-ku expose pannanum, PaginationFooter-ku venum
    private val _currentPageFlow = MutableStateFlow(1)
    val currentPageFlow: StateFlow<Int> = _currentPageFlow.asStateFlow()

    private val _pageSizeFlow = MutableStateFlow(10)
    val pageSizeFlow: StateFlow<Int> = _pageSizeFlow.asStateFlow()
    private val _createState = MutableStateFlow<CustomerCreateState>(CustomerCreateState.Idle)
    val createState: StateFlow<CustomerCreateState> = _createState.asStateFlow()
    private val _deleteState = MutableStateFlow<CustomerDeleteState>(CustomerDeleteState.Idle)
    val deleteState: StateFlow<CustomerDeleteState> = _deleteState.asStateFlow()
    init {
        loadCustomers()
    }


    fun loadCustomers(
        page: Int = currentPage,
        limit: Int = currentLimit,
        search: String? = currentSearch,
        type: String? = currentType
    ) {
        currentPage = page
        currentLimit = limit
        _currentPageFlow.value = page      // ✅ NEW
        _pageSizeFlow.value = limit        // ✅ NEW

        viewModelScope.launch {
            _uiState.update { CustomerUiState.Loading }

            val result = repository.getCustomers(
                page = page,
                limit = limit,
                search = search,
                type = type
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        CustomerUiState.Success(
                            customers = response.data,
                            total = response.total,
                            totalPages = response.totalPages
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        CustomerUiState.Error(error.message ?: "Failed to load customers")
                    }
                }
            )
        }
    }
    fun onSearch(query: String) {
        currentSearch = query.takeIf { it.isNotBlank() }
        currentPage = 1
        loadCustomers(page = 1, search = currentSearch)
    }

    fun onTypeFilterChange(type: String) {
        currentType = type.takeIf { it != "all" }
        currentPage = 1
        loadCustomers(page = 1, type = currentType)
    }

    fun onPageChange(page: Int) {
        loadCustomers(page = page)
    }

    fun onItemsPerPageChange(limit: Int) {
        currentLimit = limit
        currentPage = 1
        loadCustomers(page = 1, limit = limit)
    }

    fun refresh() {
        loadCustomers(page = 1)
    }

    // ─────────────────────────────────────────────
    // DETAIL / VIEW / EDIT STATE (Customer detail screen)
    // ─────────────────────────────────────────────

    private val _detailState = MutableStateFlow<CustomerDetailUiState>(CustomerDetailUiState.Loading)
    val detailState: StateFlow<CustomerDetailUiState> = _detailState.asStateFlow()

    private val _formState = MutableStateFlow(CustomerFormState())
    val formState: StateFlow<CustomerFormState> = _formState.asStateFlow()

    private val _updateState = MutableStateFlow<CustomerUpdateState>(CustomerUpdateState.Idle)
    val updateState: StateFlow<CustomerUpdateState> = _updateState.asStateFlow()

    // Keep the original response so we can re-send fields the form doesn't edit
    // (organizationId, createdAt, referralCount, totalSpend, pendingPayment, __v, etc.)
    private var originalCustomer: CustomerViewData? = null

    /**
     * Called when View or Edit is tapped on the list screen.
     * Fetches the "view-one" style response and populates the editable form
     * (Personal Information step) with it. Other wizard steps stay static.
     */
    fun loadCustomerDetail(id: String) {
        viewModelScope.launch {
            _detailState.update { CustomerDetailUiState.Loading }
            _updateState.update { CustomerUpdateState.Idle }

            val result = repository.getCustomerView(id)

            result.fold(
                onSuccess = { data ->
                    originalCustomer = data
                    _formState.update {
                        CustomerFormState(
                            type = data.type,
                            name = data.name,
                            mobile = data.mobile,
                            email = data.email ?: "",              // ✅ ADD
                            gender = data.gender ?: "",            // ✅ ADD
                            dob = data.dob ?: "",                  // ✅ ADD
                            status = data.status,
                            addressLine = data.address?.addressLine ?: "",
                            city = data.address?.city ?: "",
                            area = data.address?.area ?: "",       // ✅ ADD
                            pincode = data.address?.pincode ?: ""
                        )
                    }
                    _detailState.update { CustomerDetailUiState.Success(data) }
                },
                onFailure = { error ->
                    _detailState.update {
                        CustomerDetailUiState.Error(error.message ?: "Failed to load customer")
                    }
                }
            )
        }
    }

    // ── Form field updates (bind these to your TextFields in Step 1) ──

    fun onTypeChange(value: String) = _formState.update { it.copy(type = value) }
    fun onNameChange(value: String) = _formState.update { it.copy(name = value) }
    fun onEmailChange(value: String) = _formState.update { it.copy(email = value) }
    fun onGenderChange(value: String) = _formState.update { it.copy(gender = value) }
    fun onDobChange(value: String) = _formState.update { it.copy(dob = value) }
    fun onAreaChange(value: String) = _formState.update { it.copy(area = value) }
    fun onMobileChange(value: String) = _formState.update { it.copy(mobile = value) }
    fun onStatusChange(value: String) = _formState.update { it.copy(status = value) }
    fun onAddressLineChange(value: String) = _formState.update { it.copy(addressLine = value) }
    fun onCityChange(value: String) = _formState.update { it.copy(city = value) }
    fun onPincodeChange(value: String) = _formState.update { it.copy(pincode = value) }

    /**
     * Called when Update button (Step 5) is tapped.
     * Builds the payload from the current form state + original untouched
     * fields, sends it to the update API, and emits success/error.
     */
    fun updateCustomer(id: String) {
        val original = originalCustomer
        if (original == null) {
            _updateState.update { CustomerUpdateState.Error("Customer not loaded") }
            return
        }

        val form = _formState.value

        val request = UpdateCustomerRequest(
            type = form.type,
            name = form.name,
            mobile = form.mobile,
            email = form.email.takeIf { it.isNotBlank() },      // ✅ ADD
            gender = form.gender.takeIf { it.isNotBlank() },     // ✅ ADD
            dob = form.dob.takeIf { it.isNotBlank() },           // ✅ ADD
            status = form.status,
            address = CustomerViewAddress(
                addressLine = form.addressLine,
                city = form.city,
                area = form.area,                                 // ✅ ADD
                pincode = form.pincode
            ),
            preferences = original.preferences,                   // ✅ ADD — re-send untouched preferences
            referralCount = original.referralCount ?: 0,
            totalSpend = original.totalSpend ?: 0,
            pendingPayment = original.pendingPayment ?: 0,
            id = original.id,
            organizationId = original.organizationId,
            createdAt = original.createdAt,
            updatedAt = original.updatedAt,
            v = original.v
        )

        viewModelScope.launch {
            _updateState.update { CustomerUpdateState.Loading }

            val result = repository.updateCustomer(id, request)

            result.fold(
                onSuccess = { updatedData ->
                    originalCustomer = updatedData
                    _detailState.update { CustomerDetailUiState.Success(updatedData) }
                    _updateState.update { CustomerUpdateState.Success(updatedData) }
                },
                onFailure = { error ->
                    _updateState.update {
                        CustomerUpdateState.Error(error.message ?: "Failed to update customer")
                    }
                }
            )
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch {
            _deleteState.update { CustomerDeleteState.Loading }

            val result = repository.deleteCustomer(id)

            result.fold(
                onSuccess = {
                    _deleteState.update { CustomerDeleteState.Success }
                    refresh()   // reload the list so the deleted row disappears
                },
                onFailure = { error ->
                    _deleteState.update {
                        CustomerDeleteState.Error(error.message ?: "Failed to delete customer")
                    }
                }
            )
        }
    }

    fun resetUpdateState() {
        _updateState.update { CustomerUpdateState.Idle }
    }
    fun resetDeleteState() {
        _deleteState.update { CustomerDeleteState.Idle }
    }
    fun resetCreateState() {
        _createState.update { CustomerCreateState.Idle }
    }
}

// ─────────────────────────────────────────────
// UI STATE CONTRACTS
// ─────────────────────────────────────────────

sealed class CustomerUiState {
    data object Loading : CustomerUiState()
    data class Success(
        val customers: List<CustomerItem>,
        val total: Int,
        val totalPages: Int
    ) : CustomerUiState()
    data class Error(val message: String) : CustomerUiState()
}


sealed class CustomerDetailUiState {
    data object Loading : CustomerDetailUiState()
    data class Success(val customer: CustomerViewData) : CustomerDetailUiState()
    data class Error(val message: String) : CustomerDetailUiState()
}

sealed class CustomerUpdateState {
    data object Idle : CustomerUpdateState()
    data object Loading : CustomerUpdateState()
    data class Success(val customer: CustomerViewData) : CustomerUpdateState()
    data class Error(val message: String) : CustomerUpdateState()
}

sealed class CustomerCreateState {
    data object Idle : CustomerCreateState()
    data object Loading : CustomerCreateState()
    data class Success(val customer: CustomerViewData) : CustomerCreateState()
    data class Error(val message: String) : CustomerCreateState()
}

data class CustomerFormState(
    val type: String = "individual",
    val name: String = "",
    val mobile: String = "",
    val email: String = "",       // ✅ ADD
    val gender: String = "",      // ✅ ADD
    val dob: String = "",         // ✅ ADD
    val status: String = "Active",
    val addressLine: String = "",
    val city: String = "",
    val area: String = "",        // ✅ ADD
    val pincode: String = ""
)

sealed class CustomerDeleteState {
    data object Idle : CustomerDeleteState()
    data object Loading : CustomerDeleteState()
    data object Success : CustomerDeleteState()
    data class Error(val message: String) : CustomerDeleteState()
}