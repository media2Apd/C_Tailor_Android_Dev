@file:Suppress(
    "UNUSED_PARAMETER",
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused",
    "RedundantSuppression"
)

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.sales.CreateCustomerRequest
import com.cuso.tailor.model.sales.CustomerBillingAddressRequest
import com.cuso.tailor.model.sales.CustomerItem
import com.cuso.tailor.model.sales.CustomerViewData
import com.cuso.tailor.model.sales.UpdateCustomerRequest
import com.cuso.tailor.model.sales.customers
import com.cuso.tailor.repository.SalesRepository
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                    val customerData = response.data
                    // Fix: Unnecessary safe call removed
                    val customerList = customerData.customers
                    val pagination = response.pagination

                    totalPages = pagination?.totalPages ?: 1

                    val currentList = if (isRefresh) {
                        emptyList()
                    } else {
                        (uiState.value as? CustomerUiState.Success)?.customers ?: emptyList()
                    }

                    val updatedList = currentList + customerList

                    _uiState.update {
                        CustomerUiState.Success(
                            customers = updatedList,
                            total = pagination?.total ?: updatedList.size,
                            totalPages = totalPages
                        )
                    }

                    currentPage++
                    _canLoadMore.value = pagination?.hasNextPage ?: (currentPage <= totalPages)
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

    private var originalCustomer: CustomerViewData? = null

    /**
     * Fetches single customer details.
     */
    fun loadCustomerDetail(id: String) {
        launchBusy {
            _detailState.update { CustomerDetailUiState.Loading }
            _updateState.update { CustomerUpdateState.Idle }

            val result = repository.getCustomerView(id)

            result.fold(
                onSuccess = { data ->
                    originalCustomer = data
                    val addr = data.effectiveAddress
                    val line = addr?.addressLine?.takeIf { it.isNotBlank() }
                        ?: listOfNotNull(addr?.flatNo, addr?.street).filter { it.isNotBlank() }.joinToString(", ")

                    _formState.update {
                        CustomerFormState(
                            type = data.type.ifBlank { "individual" },
                            name = data.name,
                            mobile = data.mobile,
                            email = data.email.orEmpty(),
                            gender = data.gender.orEmpty(),
                            dob = data.dob,
                            status = data.status ?: "Active",
                            contactMethod = data.preferences?.contactMethod.orEmpty().ifBlank { "WhatsApp" },
                            addressLine = line,
                            city = addr?.city.orEmpty(),
                            area = addr?.areaZone ?: addr?.area.orEmpty(),
                            pincode = addr?.pincode.orEmpty()
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

    /**
     * Resets the form state for creating a new customer.
     */
    fun resetFormForNewCustomer() {
        originalCustomer = null
        _formState.update { CustomerFormState() }
        _detailState.update { CustomerDetailUiState.Success(CustomerViewData()) }
    }

    // ── Form field updates ──
    fun onTypeChange(value: String) = _formState.update { it.copy(type = value) }
    fun onNameChange(value: String) = _formState.update { it.copy(name = value) }
    fun onEmailChange(value: String) = _formState.update { it.copy(email = value) }
    fun onGenderChange(value: String) = _formState.update { it.copy(gender = value) }
    fun onDobChange(value: String) = _formState.update { it.copy(dob = value) }
    fun onAreaChange(value: String) = _formState.update { it.copy(area = value) }
    fun onMobileChange(value: String) = _formState.update { it.copy(mobile = value) }
    fun onStatusChange(value: String) = _formState.update { it.copy(status = value) }
    fun onContactMethodChange(value: String) = _formState.update { it.copy(contactMethod = value) }
    fun onAddressLineChange(value: String) = _formState.update { it.copy(addressLine = value) }
    fun onCityChange(value: String) = _formState.update { it.copy(city = value) }
    fun onPincodeChange(value: String) = _formState.update { it.copy(pincode = value) }

    /**
     * Creates a new customer profile using user-provided input.
     */
    fun createCustomer() {
        viewModelScope.launch {
            _createState.value = CustomerCreateState.Loading

            // Format customerType with first letter capitalized (e.g., "individual" -> "Individual")
            val formattedCustomerType = _formState.value.type.trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            // Build billing address only if at least one address field is provided
            val address = CustomerBillingAddressRequest(
                flatNo = null,
                street = _formState.value.addressLine.trim().takeIf { it.isNotBlank() },
                areaZone = _formState.value.area.trim().takeIf { it.isNotBlank() },
                city = _formState.value.city.trim().takeIf { it.isNotBlank() },
                subdivisionName = null,
                countryName = null,
                pincode = _formState.value.pincode.trim().takeIf { it.isNotBlank() }
            )

            val request = CreateCustomerRequest(
                fullName = _formState.value.name.trim(),
                mobileNumber = _formState.value.mobile.trim(),
                email = _formState.value.email.trim().takeIf { it.isNotBlank() },
                customerType = formattedCustomerType,
                gender = _formState.value.gender.trim().takeIf { it.isNotBlank() },
                dateOfBirth = _formState.value.dob.trim().takeIf { it.isNotBlank() },
                preferredLanguage = null, // Set only if collected from user form
                preferredContactMethod = _formState.value.contactMethod.trim().takeIf { it.isNotBlank() },
                customerLevel = null, // Set only if collected from user form
                taxId = null,
                taxIdType = null,
                billingAddress = address,
                sameAsBillingAddress = true,
                status = _formState.value.status.trim().takeIf { it.isNotBlank() }
            )

            val result = repository.createCustomer(request)
            result.fold(
                onSuccess = { createdCustomer ->
                    _createState.value = CustomerCreateState.Success(createdCustomer)
                },
                onFailure = { throwable ->
                    _createState.value = CustomerCreateState.Error(throwable.message ?: "Failed to create customer")
                }
            )
        }
    }

    /**
     * Updates an existing customer profile using user-provided input.
     */
    fun updateCustomer(customerId: String) {
        viewModelScope.launch {
            _updateState.value = CustomerUpdateState.Loading

            val formattedCustomerType = _formState.value.type.trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            val address = CustomerBillingAddressRequest(
                flatNo = null,
                street = _formState.value.addressLine.trim().takeIf { it.isNotBlank() },
                areaZone = _formState.value.area.trim().takeIf { it.isNotBlank() },
                city = _formState.value.city.trim().takeIf { it.isNotBlank() },
                subdivisionName = null,
                countryName = null,
                pincode = _formState.value.pincode.trim().takeIf { it.isNotBlank() }
            )

            val request = UpdateCustomerRequest(
                fullName = _formState.value.name.trim(),
                mobileNumber = _formState.value.mobile.trim(),
                email = _formState.value.email.trim().takeIf { it.isNotBlank() },
                customerType = formattedCustomerType,
                gender = _formState.value.gender.trim().takeIf { it.isNotBlank() },
                dateOfBirth = _formState.value.dob.trim().takeIf { it.isNotBlank() },
                preferredLanguage = null,
                preferredContactMethod = _formState.value.contactMethod.trim().takeIf { it.isNotBlank() },
                customerLevel = null,
                taxId = null,
                taxIdType = null,
                billingAddress = address,
                sameAsBillingAddress = true,
                status = _formState.value.status.trim().takeIf { it.isNotBlank() }
            )

            val result = repository.updateCustomer(customerId, request)
            result.fold(
                onSuccess = { updatedCustomer ->
                    _updateState.value = CustomerUpdateState.Success(updatedCustomer)
                },
                onFailure = { throwable ->
                    _updateState.value = CustomerUpdateState.Error(throwable.message ?: "Failed to update customer")
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
                    refresh()
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
    val contactMethod: String = "WhatsApp",
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