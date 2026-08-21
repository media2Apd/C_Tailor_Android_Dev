package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.model.sales.CustomerViewAddress
import com.cuso.mobile.model.sales.CustomerViewData
import com.cuso.mobile.model.sales.UpdateCustomerRequest
import com.cuso.mobile.repository.SalesRepository
import com.cuso.mobile.utils.launchBusy
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
    // Infinite Scroll States
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()


    //
    private var currentPage = 1
    private var currentLimit = 10
    private var currentSearch: String? = null
    private var currentType: String? = null
    private var totalPages = 1

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
        isRefresh: Boolean = false,
        search: String? = currentSearch,
        type: String? = currentType
    ) {
        if (isRefresh) {
            currentPage = 1
            _canLoadMore.value = true
            _uiState.update { CustomerUiState.Loading }
        } else {
            if (_isLoadingMore.value || !_canLoadMore.value) return
            _isLoadingMore.value = true
        }

        currentSearch = search
        currentType = type

        launchBusy {
            val result = repository.getCustomers(
                page = currentPage,
                limit = currentLimit,
                search = currentSearch,
                type = currentType
            )

            result.fold(
                onSuccess = { response ->
                    totalPages = response.totalPages

                    val currentList = if (isRefresh) {
                        emptyList()
                    } else {
                        (uiState.value as? CustomerUiState.Success)?.customers ?: emptyList()
                    }

                    val updatedList = currentList + response.data

                    _uiState.update {
                        CustomerUiState.Success(
                            customers = updatedList,
                            total = response.total,
                            totalPages = response.totalPages
                        )
                    }

                    currentPage++
                    _canLoadMore.value = currentPage <= totalPages
                    _isLoadingMore.value = false
                    _currentPageFlow.value = currentPage
                },
                onFailure = { error ->
                    if (isRefresh) {
                        _uiState.update { CustomerUiState.Error(error.message ?: "Failed to load") }
                    }
                    _isLoadingMore.value = false
                }
            )
        }
    }
    fun loadMoreCustomers() {
        loadCustomers(isRefresh = false)
    }

    fun onSearch(query: String) {
        currentSearch = query.takeIf { it.isNotBlank() }
        loadCustomers(isRefresh = true, search = currentSearch)
    }

    fun onTypeFilterChange(type: String) {
        currentType = type.takeIf { it != "all" }
        loadCustomers(isRefresh = true, type = currentType)
    }

    fun refresh() {
        loadCustomers(isRefresh = true)
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
        launchBusy {
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
                            email = data.email ?: "",
                            gender = data.gender ?: "",
                            dob = data.dob ?: "",
                            status = data.status,
                            addressLine = data.address?.addressLine ?: "",
                            city = data.address?.city ?: "",
                            area = data.address?.area ?: "",
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
            email = form.email.takeIf { it.isNotBlank() },
            gender = form.gender.takeIf { it.isNotBlank() },
            dob = form.dob.takeIf { it.isNotBlank() },
            status = form.status,
            address = CustomerViewAddress(
                addressLine = form.addressLine,
                city = form.city,
                area = form.area,
                pincode = form.pincode
            ),
            preferences = original.preferences,                   //    — re-send untouched preferences
            referralCount = original.referralCount ?: 0,
            totalSpend = original.totalSpend ?: 0,
            pendingPayment = original.pendingPayment ?: 0,
            id = original.id,
            organizationId = original.organizationId,
            createdAt = original.createdAt,
            updatedAt = original.updatedAt,
            v = original.v
        )

        launchBusy {
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
        launchBusy {
            _deleteState.update { CustomerDeleteState.Loading }

            val result = repository.deleteCustomer(id)

            result.fold(
                onSuccess = { message ->
                    _deleteState.update { CustomerDeleteState.Success(message = message) }
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
    val email: String = "",
    val gender: String = "",
    val dob: String = "",
    val status: String = "Active",
    val addressLine: String = "",
    val city: String = "",
    val area: String = "",
    val pincode: String = ""
)

sealed class CustomerDeleteState {
    data object Idle : CustomerDeleteState()
    data object Loading : CustomerDeleteState()
    data class Success(val message: String? = null) : CustomerDeleteState()
    data class Error(val message: String) : CustomerDeleteState()
}