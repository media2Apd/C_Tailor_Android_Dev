@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)
package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.finance.AccountDropdownItem
import com.cuso.tailor.model.finance.*
import com.cuso.tailor.model.finance.CashFlowData
import com.cuso.tailor.model.finance.CashFlowResponse
import com.cuso.tailor.model.finance.CategoryExpenseSlice
import com.cuso.tailor.model.finance.ChartOfAccountItem
import com.cuso.tailor.model.finance.CreateCustomerApiResponse
import com.cuso.tailor.model.finance.CreateCustomerPayload
import com.cuso.tailor.model.finance.CreateInvoiceApiResponse
import com.cuso.tailor.model.finance.CreatedCustomerData
import com.cuso.tailor.model.finance.CustomerFinancialProfileData
import com.cuso.tailor.model.finance.CustomerFinancialProfileResponse
import com.cuso.tailor.model.finance.CustomerPaymentDetailResponse
import com.cuso.tailor.model.finance.DashboardAlertDto
import com.cuso.tailor.model.finance.ExpenseItem
import com.cuso.tailor.model.finance.ExpensePagination
import com.cuso.tailor.model.finance.FinanceDistributionData
import com.cuso.tailor.model.finance.FinanceDistributionResponse
import com.cuso.tailor.model.finance.FinanceKpiData
import com.cuso.tailor.model.finance.FinanceKpiResponse
import com.cuso.tailor.model.finance.GenericDeleteResponse
import com.cuso.tailor.model.finance.InvoiceItem
import com.cuso.tailor.model.finance.InvoiceViewOneData
import com.cuso.tailor.model.finance.JournalEntryDetailData
import com.cuso.tailor.model.finance.JournalEntryItem
import com.cuso.tailor.model.finance.JournalEntryLineRequest
import com.cuso.tailor.model.finance.JournalEntryPagination
import com.cuso.tailor.model.finance.LedgerItem
import com.cuso.tailor.model.finance.OperationsOverviewData
import com.cuso.tailor.model.finance.OperationsOverviewResponse
import com.cuso.tailor.model.finance.PaymentTermItem
import com.cuso.tailor.model.finance.PaymentTermsResponse
import com.cuso.tailor.model.finance.RecentTransactionDto
import com.cuso.tailor.model.finance.RecordPaymentData
import com.cuso.tailor.model.finance.SendInvoiceApiResponse
import com.cuso.tailor.model.finance.SupplierOverviewApiResponse
import com.cuso.tailor.model.finance.SupplierOverviewData
import com.cuso.tailor.model.finance.TrialBalanceItem
import com.cuso.tailor.model.finance.VoidInvoiceRequest
import com.cuso.tailor.model.finance.VoidInvoiceResponse
import com.cuso.tailor.model.sales.CustomerListResponseV2
import com.cuso.tailor.model.sales.CustomerOrderItem
import com.cuso.tailor.model.sales.CustomerOrdersResponse
import com.cuso.tailor.model.sales.FinanceCustomerViewOneData
import com.cuso.tailor.model.sales.PaginationInfo
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.repository.FinanceRepository
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

sealed class CreateAccountState {
    object Idle : CreateAccountState()
    object Loading : CreateAccountState()
    data class Success(val message: String) : CreateAccountState()
    data class Error(val message: String) : CreateAccountState()
}

sealed class UpdateAccountState {
    object Idle : UpdateAccountState()
    object Loading : UpdateAccountState()
    data class Success(val message: String) : UpdateAccountState()
    data class Error(val message: String) : UpdateAccountState()
}

sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Loading : DeleteAccountState()
    data class Success(val message: String) : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}

sealed class CreateExpenseState {
    object Idle : CreateExpenseState()
    object Loading : CreateExpenseState()
    data class Success(val message: String) : CreateExpenseState()
    data class Error(val message: String) : CreateExpenseState()
}

sealed class CreateJournalState {
    object Idle : CreateJournalState()
    object Loading : CreateJournalState()
    data class Success(val message: String) : CreateJournalState()
    data class Error(val message: String) : CreateJournalState()
}

sealed class UpdateJournalState {
    object Idle : UpdateJournalState()
    object Loading : UpdateJournalState()
    data class Success(val message: String) : UpdateJournalState()
    data class Error(val message: String) : UpdateJournalState()
}

sealed class DeleteJournalState {
    object Idle : DeleteJournalState()
    object Loading : DeleteJournalState()
    data class Success(val message: String) : DeleteJournalState()
    data class Error(val message: String) : DeleteJournalState()
}

// ─────────────────────────────────────────────────────────────
// Create Customer State
// ─────────────────────────────────────────────────────────────
sealed class CreateCustomerState {
    object Idle : CreateCustomerState()
    object Loading : CreateCustomerState()
    data class Success(val message: String, val customer: CreatedCustomerData?) : CreateCustomerState()
    data class Error(val message: String) : CreateCustomerState()
}

sealed class VoidInvoiceState {
    object Idle : VoidInvoiceState()
    object Loading : VoidInvoiceState()
    data class Success(val message: String) : VoidInvoiceState()
    data class Error(val message: String) : VoidInvoiceState()
}

// Sealed state for Create Invoice
sealed class CreateInvoiceState {
    object Idle : CreateInvoiceState()
    object Loading : CreateInvoiceState()
    data class Success(val message: String, val invoice: InvoiceViewOneData?) : CreateInvoiceState()
    data class Error(val message: String) : CreateInvoiceState()
}

// Generic UI State for handling invoice delete loading, success, and error
sealed class DeleteInvoiceState {
    object Idle : DeleteInvoiceState()
    object Loading : DeleteInvoiceState()
    data class Success(val message: String) : DeleteInvoiceState()
    data class Error(val message: String) : DeleteInvoiceState()
}

sealed class UpdateCustomerState {
    object Idle : UpdateCustomerState()
    object Loading : UpdateCustomerState()
    data class Success(val message: String) : UpdateCustomerState()
    data class Error(val message: String) : UpdateCustomerState()
}

// UI State for Recording Payment
sealed class RecordPaymentState {
    object Idle : RecordPaymentState()
    object Loading : RecordPaymentState()
    data class Success(val message: String, val data: RecordPaymentData?) : RecordPaymentState()
    data class Error(val message: String) : RecordPaymentState()
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val repo: ApiRepository
) : ViewModel() {

    private fun extractErrorMessage(throwable: Throwable?, fallback: String): String {
        val raw = throwable?.message?.trim()
        if (raw.isNullOrBlank()) return fallback
        return try {
            JSONObject(raw).optString("message").takeIf { it.isNotBlank() } ?: fallback
        } catch (e: Exception) {
            raw.takeIf { !it.startsWith("{") } ?: fallback
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ── 1. EXPENSES: Pagination & State ──
    // ─────────────────────────────────────────────────────────────
    private val _expenseList = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenseList: StateFlow<List<ExpenseItem>> = _expenseList.asStateFlow()

    private val _expensePagination = MutableStateFlow<ExpensePagination?>(null)
    val expensePagination: StateFlow<ExpensePagination?> = _expensePagination.asStateFlow()

    private val _isLoadingExpenses = MutableStateFlow(false)
    val isLoadingExpenses: StateFlow<Boolean> = _isLoadingExpenses.asStateFlow()

    private val _isLoadingMoreExpenses = MutableStateFlow(false)
    val isLoadingMoreExpenses: StateFlow<Boolean> = _isLoadingMoreExpenses.asStateFlow()

    private val _canLoadMoreExpenses = MutableStateFlow(true)
    val canLoadMoreExpenses: StateFlow<Boolean> = _canLoadMoreExpenses.asStateFlow()

    private val _currentExpensePage = MutableStateFlow(1)
    val currentExpensePage: StateFlow<Int> = _currentExpensePage.asStateFlow()

    private val _expenseError = MutableStateFlow<String?>(null)
    val expenseError: StateFlow<String?> = _expenseError.asStateFlow()

    private var activeExpenseSearch: String? = null
    private var activeExpenseStatus: String? = null
    private var fetchExpensesJob: Job? = null

    // 1. KPI State
    private val _kpiData = MutableStateFlow<FinanceKpiData?>(null)
    val kpiData: StateFlow<FinanceKpiData?> = _kpiData.asStateFlow()

    // 2. Cash Flow State
    private val _cashFlowData = MutableStateFlow<CashFlowData?>(null)
    val cashFlowData: StateFlow<CashFlowData?> = _cashFlowData.asStateFlow()

    // 3. Distribution State
    private val _distributionData = MutableStateFlow<FinanceDistributionData?>(null)
    val distributionData: StateFlow<FinanceDistributionData?> = _distributionData.asStateFlow()

    // 4. Overviews State
    private val _salesOverview = MutableStateFlow<OperationsOverviewData?>(null)
    val salesOverview: StateFlow<OperationsOverviewData?> = _salesOverview.asStateFlow()

    private val _purchaseOverview = MutableStateFlow<OperationsOverviewData?>(null)
    val purchaseOverview: StateFlow<OperationsOverviewData?> = _purchaseOverview.asStateFlow()

    private val _ledgerOverview = MutableStateFlow<OperationsOverviewData?>(null)
    val ledgerOverview: StateFlow<OperationsOverviewData?> = _ledgerOverview.asStateFlow()

    private val _expensesOverview = MutableStateFlow<OperationsOverviewData?>(null)
    val expensesOverview: StateFlow<OperationsOverviewData?> = _expensesOverview.asStateFlow()

    private val _fundsOverview = MutableStateFlow<OperationsOverviewData?>(null)
    val fundsOverview: StateFlow<OperationsOverviewData?> = _fundsOverview.asStateFlow()

    // 5. Recent Transactions State
    private val _recentTransactions = MutableStateFlow<List<RecentTransactionDto>>(emptyList())
    val recentTransactions: StateFlow<List<RecentTransactionDto>> = _recentTransactions.asStateFlow()

    // 6. Alerts & Pending Actions State
    private val _alertsList = MutableStateFlow<List<DashboardAlertDto>>(emptyList())
    val alertsList: StateFlow<List<DashboardAlertDto>> = _alertsList.asStateFlow()

    // 7. Bank Snapshot State
    private val _bankSnapshot = MutableStateFlow<BankSnapshotData?>(null)
    val bankSnapshot: StateFlow<BankSnapshotData?> = _bankSnapshot.asStateFlow()

    // 8. Expense by Category State
    private val _expenseCategories = MutableStateFlow<List<CategoryExpenseSlice>>(emptyList())
    val expenseCategories: StateFlow<List<CategoryExpenseSlice>> = _expenseCategories.asStateFlow()

    // Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAllDashboardData()
    }

    /**
     * Executes parallel requests to fetch all dashboard sections
     */
    fun fetchAllDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Fetch KPI
            launch {
                repo.request<FinanceKpiResponse> {
                    get("/api/finance/dashboard/kpi-summary")
                }.onSuccess { _kpiData.value = it.data }
            }

            // 2. Fetch Cash Flow Analytics
            launch {
                repo.request<CashFlowResponse> {
                    get("/api/finance/dashboard/cash-flow")
                }.onSuccess { _cashFlowData.value = it.data }
            }

            // 3. Fetch Distribution
            launch {
                repo.request<FinanceDistributionResponse> {
                    get("/api/finance/dashboard/distribution")
                }.onSuccess { _distributionData.value = it.data }
            }

            // 4. Fetch Operations Overviews
            launch {
                repo.request<OperationsOverviewResponse> {
                    get("/api/finance/dashboard/sales-overview")
                }.onSuccess { _salesOverview.value = it.data }
            }

            launch {
                repo.request<OperationsOverviewResponse> {
                    get("/api/finance/dashboard/purchase-overview")
                }.onSuccess { _purchaseOverview.value = it.data }
            }

            launch {
                repo.request<OperationsOverviewResponse> {
                    get("/api/finance/dashboard/ledger-overview")
                }.onSuccess { _ledgerOverview.value = it.data }
            }

            launch {
                repo.request<OperationsOverviewResponse> {
                    get("/api/finance/dashboard/expenses-overview")
                }.onSuccess { _expensesOverview.value = it.data }
            }

            launch {
                repo.request<OperationsOverviewResponse> {
                    get("/api/finance/dashboard/funds-overview")
                }.onSuccess { _fundsOverview.value = it.data }
            }

            // 5. Fetch Recent Transactions
            launch {
                repo.request<RecentTransactionsResponse> {
                    get("/api/finance/dashboard/recent-transactions")
                }.onSuccess { _recentTransactions.value = it.data?.transactions.orEmpty() }
            }

            // 6. Fetch Alerts & Pending Actions
            launch {
                repo.request<DashboardAlertsResponse> {
                    get("/api/finance/dashboard/pending-alerts")
                }.onSuccess { _alertsList.value = it.data?.alerts.orEmpty() }
            }

            // 7. Fetch Bank Snapshot
            launch {
                repo.request<BankSnapshotResponse> {
                    get("/api/finance/dashboard/bank-snapshot")
                }.onSuccess { _bankSnapshot.value = it.data }
            }

            // 8. Fetch Expense by Category
            launch {
                repo.request<ExpenseByCategoryResponse> {
                    get("/api/finance/dashboard/expense-by-category")
                }.onSuccess { _expenseCategories.value = it.data?.categories.orEmpty() }
            }

            _isLoading.value = false
        }
    }


    fun fetchExpenses(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchExpensesJob?.cancel()
        fetchExpensesJob = launchBusy {
            _isLoadingExpenses.value = true
            _expenseError.value = null
            _currentExpensePage.value = page
            activeExpenseSearch = search
            activeExpenseStatus = status

            val result = financeRepository.getExpenses(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    val newExpenses = response.data
                    val pagination = response.pagination

                    _expenseList.value = newExpenses
                    _expensePagination.value = pagination

                    val totalPages = pagination.totalPages
                    _canLoadMoreExpenses.value = page < totalPages && newExpenses.isNotEmpty()
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _expenseError.value = extractErrorMessage(e, "Failed to fetch expenses")
                    }
                }
            )
            _isLoadingExpenses.value = false
        }
    }

    fun loadMoreExpenses(limit: Int = 10) {
        if (_isLoadingMoreExpenses.value || _isLoadingExpenses.value || !_canLoadMoreExpenses.value) {
            return
        }

        launchBusy {
            _isLoadingMoreExpenses.value = true
            val nextPage = _currentExpensePage.value + 1

            val result = financeRepository.getExpenses(
                page = nextPage,
                limit = limit,
                search = activeExpenseSearch,
                status = activeExpenseStatus
            )

            result.fold(
                onSuccess = { response ->
                    val newExpenses = response.data
                    val pagination = response.pagination

                    if (newExpenses.isNotEmpty()) {
                        _expenseList.value += newExpenses
                        _currentExpensePage.value = nextPage
                        _expensePagination.value = pagination

                        val totalPages = pagination.totalPages
                        _canLoadMoreExpenses.value = nextPage < totalPages
                    } else {
                        _canLoadMoreExpenses.value = false
                    }
                },
                onFailure = {
                    // Do not permanently lock pagination on single request failure
                }
            )
            _isLoadingMoreExpenses.value = false
        }
    }

    fun refreshExpenses() {
        fetchExpenses(page = 1, search = activeExpenseSearch, status = activeExpenseStatus)
    }

    // ─────────────────────────────────────────────────────────────
    // ── 2. INVOICES: Pagination & State ──
    // ─────────────────────────────────────────────────────────────
    private val _invoiceList = MutableStateFlow<List<InvoiceItem>>(emptyList())
    val invoiceList: StateFlow<List<InvoiceItem>> = _invoiceList.asStateFlow()

    private val _invoicePagination = MutableStateFlow<PaginationInfo?>(null)
    val invoicePagination: StateFlow<PaginationInfo?> = _invoicePagination.asStateFlow()

    private val _isLoadingInvoices = MutableStateFlow(false)
    val isLoadingInvoices: StateFlow<Boolean> = _isLoadingInvoices.asStateFlow()

    private val _isLoadingMoreInvoices = MutableStateFlow(false)
    val isLoadingMoreInvoices: StateFlow<Boolean> = _isLoadingMoreInvoices.asStateFlow()

    private val _canLoadMoreInvoices = MutableStateFlow(true)
    val canLoadMoreInvoices: StateFlow<Boolean> = _canLoadMoreInvoices.asStateFlow()

    private val _currentInvoicePage = MutableStateFlow(1)
    val currentInvoicePage: StateFlow<Int> = _currentInvoicePage.asStateFlow()

    private val _invoiceError = MutableStateFlow<String?>(null)
    val invoiceError: StateFlow<String?> = _invoiceError.asStateFlow()

    private var activeInvoiceSearch: String? = null
    private var activeInvoiceStatus: String? = null
    private var fetchInvoicesJob: Job? = null

    fun fetchInvoices(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchInvoicesJob?.cancel()
        fetchInvoicesJob = launchBusy {
            _isLoadingInvoices.value = true
            _invoiceError.value = null
            _currentInvoicePage.value = page
            activeInvoiceSearch = search
            activeInvoiceStatus = status

            val result = financeRepository.getInvoices(page, limit, search, status)
            when {
                result.isSuccess -> {
                    val body = result.getOrNull()
                    val newInvoices = body?.data ?: emptyList()
                    val pagination = body?.pagination

                    _invoiceList.value = newInvoices
                    _invoicePagination.value = pagination

                    val totalPages = pagination?.totalPages ?: 1
                    _canLoadMoreInvoices.value = page < totalPages && newInvoices.isNotEmpty()
                }
                result.isFailure -> {
                    val e = result.exceptionOrNull()
                    if (e !is CancellationException) {
                        _invoiceError.value = extractErrorMessage(e, "Failed to fetch invoices")
                    }
                }
            }
            _isLoadingInvoices.value = false
        }
    }

    fun loadMoreInvoices(limit: Int = 10) {
        if (_isLoadingMoreInvoices.value || _isLoadingInvoices.value || !_canLoadMoreInvoices.value) {
            return
        }

        launchBusy {
            _isLoadingMoreInvoices.value = true
            val nextPage = _currentInvoicePage.value + 1

            val result = financeRepository.getInvoices(
                page = nextPage,
                limit = limit,
                search = activeInvoiceSearch,
                status = activeInvoiceStatus
            )

            when {
                result.isSuccess -> {
                    val body = result.getOrNull()
                    val newInvoices = body?.data ?: emptyList()
                    val pagination = body?.pagination

                    if (newInvoices.isNotEmpty()) {
                        _invoiceList.value += newInvoices
                        _currentInvoicePage.value = nextPage
                        _invoicePagination.value = pagination

                        val totalPages = pagination?.totalPages ?: nextPage
                        _canLoadMoreInvoices.value = nextPage < totalPages
                    } else {
                        _canLoadMoreInvoices.value = false
                    }
                }
                result.isFailure -> {
                    // Do not permanently lock pagination
                }
            }
            _isLoadingMoreInvoices.value = false
        }
    }

    fun refreshInvoices() {
        fetchInvoices(page = 1, search = activeInvoiceSearch, status = activeInvoiceStatus)
    }

    // ─────────────────────────────────────────────────────────────
    // ── CUSTOMER ORDERS & METRICS (API STATE) ──
    // ─────────────────────────────────────────────────────────────
    private val _customerOrders = MutableStateFlow<List<CustomerOrderItem>>(emptyList())
    val customerOrders: StateFlow<List<CustomerOrderItem>> = _customerOrders.asStateFlow()

    private val _isLoadingCustomerOrders = MutableStateFlow(false)
    val isLoadingCustomerOrders: StateFlow<Boolean> = _isLoadingCustomerOrders.asStateFlow()

    private val _customerOrdersError = MutableStateFlow<String?>(null)
    val customerOrdersError: StateFlow<String?> = _customerOrdersError.asStateFlow()

    fun fetchCustomerOrders(customerId: String) {
        if (customerId.isBlank()) return

        launchBusy {
            _isLoadingCustomerOrders.value = true
            _customerOrdersError.value = null

            val result = repo.request<CustomerOrdersResponse> {
                get("/api/finance/customers/view-transactions/$customerId")
            }

            result.fold(
                onSuccess = { res ->
                    _customerOrders.value = res.data?.orders.orEmpty()
                },
                onFailure = { error ->
                    _customerOrders.value = emptyList()
                    _customerOrdersError.value = error.message ?: "Failed to load orders"
                }
            )
            _isLoadingCustomerOrders.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ── CREATE CUSTOMER VIA API REPOSITORY ──
    // ─────────────────────────────────────────────────────────────
    private val _createCustomerState = MutableStateFlow<CreateCustomerState>(CreateCustomerState.Idle)
    val createCustomerState: StateFlow<CreateCustomerState> = _createCustomerState.asStateFlow()

    fun createCustomer(
        payload: CreateCustomerPayload,
        onSuccessCallback: () -> Unit = {}
    ) {
        launchBusy {
            _createCustomerState.value = CreateCustomerState.Loading

            val result = repo.request<CreateCustomerApiResponse> {
                post("/api/sales/customers/create", payload)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _createCustomerState.value = CreateCustomerState.Success(
                            message = response.message,
                            customer = response.data
                        )
                        // Reload first page of customers list automatically
                        fetchCustomerForFinance(page = 1)
                        onSuccessCallback()
                    } else {
                        _createCustomerState.value = CreateCustomerState.Error(
                            response.message.ifBlank { "Failed to create customer" }
                        )
                    }
                },
                onFailure = { throwable ->
                    _createCustomerState.value = CreateCustomerState.Error(
                        extractErrorMessage(throwable, "Failed to create customer")
                    )
                }
            )
        }
    }

    fun resetCreateCustomerState() {
        _createCustomerState.value = CreateCustomerState.Idle
    }

    fun clearCustomerOrders() {
        _customerOrders.value = emptyList()
        _customerOrdersError.value = null
    }
    // ─────────────────────────────────────────────────────────────
    // ── 3. FINANCE CUSTOMERS: Pagination & State ──
    // ─────────────────────────────────────────────────────────────
    // Latest page response (customers + pagination)
    private val _financeCustomerList = MutableStateFlow<CustomerListResponseV2?>(null)
    val financeCustomerList: StateFlow<CustomerListResponseV2?> = _financeCustomerList

    private val _isLoadingFinanceCustomers = MutableStateFlow(false)
    val isLoadingFinanceCustomers: StateFlow<Boolean> = _isLoadingFinanceCustomers

    private val _isLoadingMoreFinanceCustomers = MutableStateFlow(false)
    val isLoadingMoreFinanceCustomers: StateFlow<Boolean> = _isLoadingMoreFinanceCustomers

    private val _financeCustomerError = MutableStateFlow<String?>(null)
    val financeCustomerError: StateFlow<String?> = _financeCustomerError

    private val _currentFinanceCustomerPage = MutableStateFlow(1)
    val currentFinanceCustomerPage: StateFlow<Int> = _currentFinanceCustomerPage

    private val _canLoadMoreFinanceCustomers = MutableStateFlow(false)
    val canLoadMoreFinanceCustomers: StateFlow<Boolean> = _canLoadMoreFinanceCustomers

    private var fetchFinanceCustomersJob: Job? = null
    private var loadMoreFinanceCustomersJob: Job? = null
    private var activeFinanceCustomerSearch: String? = null
    private var activeFinanceCustomerType: String? = null

    // Fetch the first page (or any page) and replace the current list
    fun fetchCustomerForFinance(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        type: String? = null
    ) {
        fetchFinanceCustomersJob?.cancel()
        loadMoreFinanceCustomersJob?.cancel()

        fetchFinanceCustomersJob = viewModelScope.launch {
            _isLoadingFinanceCustomers.value = true
            _financeCustomerError.value = null
            _currentFinanceCustomerPage.value = page

            activeFinanceCustomerSearch = search?.trim()?.ifBlank { null }
            activeFinanceCustomerType = type?.trim()?.ifBlank { null }

            val result = financeRepository.getCustomerForFinance(
                page = page,
                limit = limit,
                search = activeFinanceCustomerSearch,
                type = activeFinanceCustomerType
            )

            result
                .onSuccess { response ->
                    _financeCustomerList.value = response
                    val totalPages = response.pagination?.totalPages ?: 1
                    _canLoadMoreFinanceCustomers.value =
                        page < totalPages && response.data.isNotEmpty()
                }
                .onFailure { e ->
                    if (e !is CancellationException) {
                        _financeCustomerError.value =
                            extractErrorMessage(e, "Failed to fetch customers")
                    }
                }

            _isLoadingFinanceCustomers.value = false
        }
    }

    // Fetch the next page and append it to the current list
    fun loadMoreFinanceCustomers(limit: Int = 10) {
        if (_isLoadingFinanceCustomers.value || _isLoadingMoreFinanceCustomers.value) return
        if (!_canLoadMoreFinanceCustomers.value) return

        val nextPage = _currentFinanceCustomerPage.value + 1

        loadMoreFinanceCustomersJob = viewModelScope.launch {
            _isLoadingMoreFinanceCustomers.value = true

            val result = financeRepository.getCustomerForFinance(
                page = nextPage,
                limit = limit,
                search = activeFinanceCustomerSearch,
                type = activeFinanceCustomerType
            )

            result
                .onSuccess { response ->
                    val current = _financeCustomerList.value
                    _financeCustomerList.value = response.copy(
                        data = current?.data.orEmpty() + response.data
                    )
                    _currentFinanceCustomerPage.value = nextPage

                    val totalPages = response.pagination?.totalPages ?: 1
                    _canLoadMoreFinanceCustomers.value =
                        nextPage < totalPages && response.data.isNotEmpty()
                }
                .onFailure { e ->
                    if (e !is CancellationException) {
                        _financeCustomerError.value =
                            extractErrorMessage(e, "Failed to load more customers")
                    }
                }

            _isLoadingMoreFinanceCustomers.value = false
        }
    }

    /**
     * Refresh
     */
    fun refreshCustomerForFinance() {
        fetchCustomerForFinance(page = 1, search = activeFinanceCustomerSearch)
    }
    // ─────────────────────────────────────────────────────────────
    // ── 4. JOURNAL ENTRIES: Pagination & State ──
    // ─────────────────────────────────────────────────────────────
    private val _journalEntries = MutableStateFlow<List<JournalEntryItem>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntryItem>> = _journalEntries.asStateFlow()

    private val _journalEntryPagination = MutableStateFlow<JournalEntryPagination?>(null)
    val journalEntryPagination: StateFlow<JournalEntryPagination?> = _journalEntryPagination.asStateFlow()

    private val _isLoadingJournalEntries = MutableStateFlow(false)
    val isLoadingJournalEntries: StateFlow<Boolean> = _isLoadingJournalEntries.asStateFlow()

    private val _isLoadingMoreJournalEntries = MutableStateFlow(false)
    val isLoadingMoreJournalEntries: StateFlow<Boolean> = _isLoadingMoreJournalEntries.asStateFlow()

    private val _canLoadMoreJournalEntries = MutableStateFlow(true)
    val canLoadMoreJournalEntries: StateFlow<Boolean> = _canLoadMoreJournalEntries.asStateFlow()

    private val _currentJournalEntryPage = MutableStateFlow(1)
    val currentJournalEntryPage: StateFlow<Int> = _currentJournalEntryPage.asStateFlow()

    private val _journalEntriesError = MutableStateFlow<String?>(null)
    val journalEntriesError: StateFlow<String?> = _journalEntriesError.asStateFlow()

    private var activeJournalSearch: String? = null
    private var activeJournalStatus: String? = null
    private var fetchJournalJob: Job? = null

    fun fetchJournalEntries(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchJournalJob?.cancel()
        fetchJournalJob = launchBusy {
            _isLoadingJournalEntries.value = true
            _journalEntriesError.value = null
            _currentJournalEntryPage.value = page
            activeJournalSearch = search
            activeJournalStatus = status

            val result = financeRepository.getJournalEntries(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    val newEntries = response.data
                    val pagination = response.pagination

                    _journalEntries.value = newEntries
                    _journalEntryPagination.value = pagination

                    val totalPages = pagination?.totalPages ?: 1
                    _canLoadMoreJournalEntries.value = page < totalPages && newEntries.isNotEmpty()
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _journalEntriesError.value = extractErrorMessage(e, "Failed to fetch journal entries")
                    }
                }
            )
            _isLoadingJournalEntries.value = false
        }
    }

    fun loadMoreJournalEntries(limit: Int = 10) {
        if (_isLoadingMoreJournalEntries.value || _isLoadingJournalEntries.value || !_canLoadMoreJournalEntries.value) {
            return
        }

        launchBusy {
            _isLoadingMoreJournalEntries.value = true
            val nextPage = _currentJournalEntryPage.value + 1

            val result = financeRepository.getJournalEntries(
                page = nextPage,
                limit = limit,
                search = activeJournalSearch,
                status = activeJournalStatus
            )

            result.fold(
                onSuccess = { response ->
                    val newEntries = response.data
                    val pagination = response.pagination

                    if (newEntries.isNotEmpty()) {
                        _journalEntries.value += newEntries
                        _currentJournalEntryPage.value = nextPage
                        _journalEntryPagination.value = pagination

                        val totalPages = pagination?.totalPages ?: nextPage
                        _canLoadMoreJournalEntries.value = nextPage < totalPages
                    } else {
                        _canLoadMoreJournalEntries.value = false
                    }
                },
                onFailure = {
                    // Do not permanently lock pagination
                }
            )
            _isLoadingMoreJournalEntries.value = false
        }
    }

    fun refreshJournalEntries() {
        fetchJournalEntries(page = 1, search = activeJournalSearch, status = activeJournalStatus)
    }

    // ─────────────────────────────────────────────────────────────
    // ── 5. CHART OF ACCOUNTS & DROPDOWNS ──
    // ─────────────────────────────────────────────────────────────
    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.Idle)
    val createAccountState: StateFlow<CreateAccountState> = _createAccountState.asStateFlow()

    private val _updateAccountState = MutableStateFlow<UpdateAccountState>(UpdateAccountState.Idle)
    val updateAccountState: StateFlow<UpdateAccountState> = _updateAccountState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState.asStateFlow()

    private val _chartOfAccounts = MutableStateFlow<List<ChartOfAccountItem>>(emptyList())
    val chartOfAccounts: StateFlow<List<ChartOfAccountItem>> = _chartOfAccounts.asStateFlow()

    private val _isLoadingChartOfAccounts = MutableStateFlow(false)
    val isLoadingChartOfAccounts: StateFlow<Boolean> = _isLoadingChartOfAccounts.asStateFlow()

    private val _chartOfAccountsError = MutableStateFlow<String?>(null)
    val chartOfAccountsError: StateFlow<String?> = _chartOfAccountsError.asStateFlow()

    private val _accountDropdownList = MutableStateFlow<List<AccountDropdownItem>>(emptyList())
    val accountDropdownList: StateFlow<List<AccountDropdownItem>> = _accountDropdownList.asStateFlow()

    private val _isLoadingAccountDropdown = MutableStateFlow(false)
    val isLoadingAccountDropdown: StateFlow<Boolean> = _isLoadingAccountDropdown.asStateFlow()

    fun fetchChartOfAccounts() {
        launchBusy {
            _isLoadingChartOfAccounts.value = true
            _chartOfAccountsError.value = null
            val result = financeRepository.getChartOfAccounts()
            result.fold(
                onSuccess = { _chartOfAccounts.value = it },
                onFailure = { e ->
                    _chartOfAccountsError.value = extractErrorMessage(e, "Failed to fetch chart of accounts")
                }
            )
            _isLoadingChartOfAccounts.value = false
        }
    }

    fun fetchChartOfAccountsDropdown(context: String = "parent_account") {
        launchBusy {
            _isLoadingAccountDropdown.value = true
            val result = financeRepository.getChartOfAccountsDropdown(context)
            result.fold(
                onSuccess = { _accountDropdownList.value = it },
                onFailure = { /* Fail silently or handle dropdown error */ }
            )
            _isLoadingAccountDropdown.value = false
        }
    }

    fun createChartOfAccount(
        accountName: String,
        accountType: String,
        description: String? = null,
        parentAccount: String? = null
    ) {
        launchBusy {
            _createAccountState.value = CreateAccountState.Loading
            val result = financeRepository.createChartOfAccount(
                accountName = accountName,
                accountType = accountType,
                description = description,
                parentAccount = parentAccount
            )
            result.fold(
                onSuccess = { response ->
                    _createAccountState.value = CreateAccountState.Success(
                        response.message ?: "Account added successfully"
                    )
                    fetchChartOfAccounts()
                },
                onFailure = { e ->
                    _createAccountState.value = CreateAccountState.Error(
                        extractErrorMessage(e, "Failed to create account")
                    )
                }
            )
        }
    }

    fun updateChartOfAccount(
        id: String,
        accountName: String,
        accountType: String,
        description: String? = null,
        parentAccount: String? = null
    ) {
        launchBusy {
            _updateAccountState.value = UpdateAccountState.Loading
            val result = financeRepository.updateChartOfAccount(
                id = id,
                accountName = accountName,
                accountType = accountType,
                description = description,
                parentAccount = parentAccount
            )
            result.fold(
                onSuccess = { response ->
                    _updateAccountState.value = UpdateAccountState.Success(
                        response.message ?: "Account updated successfully"
                    )
                    fetchChartOfAccounts()
                },
                onFailure = { e ->
                    _updateAccountState.value = UpdateAccountState.Error(
                        extractErrorMessage(e, "Failed to update account")
                    )
                }
            )
        }
    }

    fun deleteChartOfAccount(id: String) {
        launchBusy {
            _deleteAccountState.value = DeleteAccountState.Loading
            val result = financeRepository.deleteChartOfAccount(id)
            result.fold(
                onSuccess = { response ->
                    _deleteAccountState.value = DeleteAccountState.Success(
                        response.message ?: "Account deleted successfully"
                    )
                    fetchChartOfAccounts()
                },
                onFailure = { e ->
                    _deleteAccountState.value = DeleteAccountState.Error(
                        extractErrorMessage(e, "Failed to delete account")
                    )
                }
            )
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = DeleteAccountState.Idle
    }

    fun resetUpdateAccountState() {
        _updateAccountState.value = UpdateAccountState.Idle
    }

    fun resetCreateAccountState() {
        _createAccountState.value = CreateAccountState.Idle
    }

    // ─────────────────────────────────────────────────────────────
    // ── 6. OTHER FINANCE OPERATIONS ──
    // ─────────────────────────────────────────────────────────────

    // View One Customer
    private val _financeCustomerDetail = MutableStateFlow<FinanceCustomerViewOneData?>(null)
    val financeCustomerDetail: StateFlow<FinanceCustomerViewOneData?> = _financeCustomerDetail.asStateFlow()

    private val _isLoadingFinanceCustomerDetail = MutableStateFlow(false)
    val isLoadingFinanceCustomerDetail: StateFlow<Boolean> = _isLoadingFinanceCustomerDetail.asStateFlow()

    private val _financeCustomerDetailError = MutableStateFlow<String?>(null)
    val financeCustomerDetailError: StateFlow<String?> = _financeCustomerDetailError.asStateFlow()

    fun getFinanceCustomerViewOne(id: String) {
        launchBusy {
            _isLoadingFinanceCustomerDetail.value = true
            _financeCustomerDetailError.value = null
            val result = financeRepository.getFinanceCustomerViewOne(id)
            when {
                result.isSuccess -> _financeCustomerDetail.value = result.getOrNull()?.data
                result.isFailure -> _financeCustomerDetailError.value =
                    extractErrorMessage(result.exceptionOrNull(), "Failed to fetch customer detail")
            }
            _isLoadingFinanceCustomerDetail.value = false
        }
    }

    // Invoices View One
    private val _selectedInvoice = MutableStateFlow<InvoiceItem?>(null)
    val selectedInvoice: StateFlow<InvoiceItem?> = _selectedInvoice.asStateFlow()

    private val _invoiceDetail = MutableStateFlow<InvoiceViewOneData?>(null)
    val invoiceDetail: StateFlow<InvoiceViewOneData?> = _invoiceDetail.asStateFlow()

    private val _isLoadingInvoiceDetail = MutableStateFlow(false)
    val isLoadingInvoiceDetail: StateFlow<Boolean> = _isLoadingInvoiceDetail.asStateFlow()

    private val _invoiceDetailError = MutableStateFlow<String?>(null)
    val invoiceDetailError: StateFlow<String?> = _invoiceDetailError.asStateFlow()

    fun fetchInvoiceDetail(invoiceId: String) {
        launchBusy {
            _isLoadingInvoiceDetail.value = true
            _invoiceDetailError.value = null
            _invoiceDetail.value = null
            val result = financeRepository.getInvoiceViewOne(invoiceId)
            when {
                result.isSuccess -> _invoiceDetail.value = result.getOrNull()
                result.isFailure -> _invoiceDetailError.value =
                    extractErrorMessage(result.exceptionOrNull(), "Failed to fetch invoice details")
            }
            _isLoadingInvoiceDetail.value = false
        }
    }

    fun selectInvoice(invoice: InvoiceItem) {
        _selectedInvoice.value = invoice
    }

    fun clearSelectedInvoice() {
        _selectedInvoice.value = null
    }

    fun clearInvoiceDetail() {
        _invoiceDetail.value = null
        _invoiceDetailError.value = null
        _isLoadingInvoiceDetail.value = false
    }

    // Expenses: detail & create
    private val _expenseDetail = MutableStateFlow<ExpenseItem?>(null)
    val expenseDetail: StateFlow<ExpenseItem?> = _expenseDetail.asStateFlow()

    private val _isLoadingExpenseDetail = MutableStateFlow(false)
    val isLoadingExpenseDetail: StateFlow<Boolean> = _isLoadingExpenseDetail.asStateFlow()

    private val _createExpenseState = MutableStateFlow<CreateExpenseState>(CreateExpenseState.Idle)
    val createExpenseState: StateFlow<CreateExpenseState> = _createExpenseState.asStateFlow()

    fun fetchExpenseDetail(id: String) {
        launchBusy {
            _isLoadingExpenseDetail.value = true
            _expenseDetail.value = null
            val result = financeRepository.getExpenseViewOne(id)
            result.fold(
                onSuccess = { _expenseDetail.value = it },
                onFailure = { e ->
                    _expenseError.value = extractErrorMessage(e, "Failed to fetch expense detail")
                }
            )
            _isLoadingExpenseDetail.value = false
        }
    }

    fun createExpense(
        branch: String,
        expenseDate: String,
        accountId: String,
        paymentAccountId: String,
        amount: String,
        referenceNumber: String?,
        notes: String?,
        status: String?,
        fileParts: List<okhttp3.MultipartBody.Part> = emptyList()
    ) {
        launchBusy {
            _createExpenseState.value = CreateExpenseState.Loading
            val result = financeRepository.createExpense(
                branch, expenseDate, accountId, paymentAccountId,
                amount, referenceNumber, notes, status, fileParts
            )
            result.fold(
                onSuccess = { response ->
                    _createExpenseState.value = CreateExpenseState.Success(
                        response.message ?: "Expense added successfully"
                    )
                    refreshExpenses()
                },
                onFailure = { e ->
                    _createExpenseState.value = CreateExpenseState.Error(
                        extractErrorMessage(e, "Failed to create expense")
                    )
                }
            )
        }
    }

    fun resetCreateExpenseState() {
        _createExpenseState.value = CreateExpenseState.Idle
    }

    fun clearExpenseDetail() {
        _expenseDetail.value = null
    }

    // Trial Balance
    private val _trialBalanceList = MutableStateFlow<List<TrialBalanceItem>>(emptyList())
    val trialBalanceList: StateFlow<List<TrialBalanceItem>> = _trialBalanceList.asStateFlow()

    private val _isLoadingTrialBalance = MutableStateFlow(false)
    val isLoadingTrialBalance: StateFlow<Boolean> = _isLoadingTrialBalance.asStateFlow()

    private val _trialBalanceError = MutableStateFlow<String?>(null)
    val trialBalanceError: StateFlow<String?> = _trialBalanceError.asStateFlow()

    fun fetchTrialBalance() {
        launchBusy {
            _isLoadingTrialBalance.value = true
            _trialBalanceError.value = null
            val result = financeRepository.getTrialBalance()
            result.fold(
                onSuccess = { _trialBalanceList.value = it },
                onFailure = { e ->
                    _trialBalanceError.value = extractErrorMessage(e, "Failed to fetch trial balance")
                }
            )
            _isLoadingTrialBalance.value = false
        }
    }

    // Ledger
    private val _ledgerList = MutableStateFlow<List<LedgerItem>>(emptyList())
    val ledgerList: StateFlow<List<LedgerItem>> = _ledgerList.asStateFlow()

    private val _isLoadingLedger = MutableStateFlow(false)
    val isLoadingLedger: StateFlow<Boolean> = _isLoadingLedger.asStateFlow()

    private val _ledgerError = MutableStateFlow<String?>(null)
    val ledgerError: StateFlow<String?> = _ledgerError.asStateFlow()

    fun fetchLedger(accountId: String) {
        launchBusy {
            _isLoadingLedger.value = true
            _ledgerError.value = null
            val result = financeRepository.getLedger(accountId)
            result.fold(
                onSuccess = { _ledgerList.value = it },
                onFailure = { e ->
                    _ledgerError.value = extractErrorMessage(e, "Failed to fetch ledger")
                }
            )
            _isLoadingLedger.value = false
        }
    }

    fun clearLedger() {
        _ledgerList.value = emptyList()
        _ledgerError.value = null
    }

    // Journal Entry Detail & Mutations
    private val _createJournalState = MutableStateFlow<CreateJournalState>(CreateJournalState.Idle)
    val createJournalState: StateFlow<CreateJournalState> = _createJournalState.asStateFlow()

    private val _updateJournalState = MutableStateFlow<UpdateJournalState>(UpdateJournalState.Idle)
    val updateJournalState: StateFlow<UpdateJournalState> = _updateJournalState.asStateFlow()

    private val _deleteJournalState = MutableStateFlow<DeleteJournalState>(DeleteJournalState.Idle)
    val deleteJournalState: StateFlow<DeleteJournalState> = _deleteJournalState.asStateFlow()

    private val _journalEntryDetail = MutableStateFlow<JournalEntryDetailData?>(null)
    val journalEntryDetail: StateFlow<JournalEntryDetailData?> = _journalEntryDetail.asStateFlow()

    private val _isLoadingJournalDetail = MutableStateFlow(false)
    val isLoadingJournalDetail: StateFlow<Boolean> = _isLoadingJournalDetail.asStateFlow()

    private val _journalDetailError = MutableStateFlow<String?>(null)
    val journalDetailError: StateFlow<String?> = _journalDetailError.asStateFlow()

    fun createJournal(
        branchId: String,
        entryDate: String,
        reference: String?,
        notes: String?,
        status: String = "Posted",
        lines: List<JournalEntryLineRequest>
    ) {
        launchBusy {
            _createJournalState.value = CreateJournalState.Loading
            val result = financeRepository.createJournal(
                branchId = branchId,
                entryDate = entryDate,
                reference = reference,
                notes = notes,
                status = status,
                lines = lines
            )
            result.fold(
                onSuccess = { response ->
                    _createJournalState.value = CreateJournalState.Success(
                        response.message ?: "Journal entry posted successfully"
                    )
                    refreshJournalEntries()
                },
                onFailure = { e ->
                    _createJournalState.value = CreateJournalState.Error(
                        extractErrorMessage(e, "Failed to post journal entry")
                    )
                }
            )
        }
    }

    fun updateJournal(
        id: String,
        branchId: String,
        entryDate: String,
        reference: String?,
        notes: String?,
        status: String = "Posted",
        lines: List<JournalEntryLineRequest>
    ) {
        launchBusy {
            _updateJournalState.value = UpdateJournalState.Loading
            val result = financeRepository.updateJournal(
                id = id,
                branchId = branchId,
                entryDate = entryDate,
                reference = reference,
                notes = notes,
                status = status,
                lines = lines
            )
            result.fold(
                onSuccess = { response ->
                    _updateJournalState.value = UpdateJournalState.Success(
                        response.message ?: "Journal entry updated successfully"
                    )
                    refreshJournalEntries()
                },
                onFailure = { e ->
                    _updateJournalState.value = UpdateJournalState.Error(
                        extractErrorMessage(e, "Failed to update journal entry")
                    )
                }
            )
        }
    }

    fun deleteJournalEntry(id: String) {
        launchBusy {
            _deleteJournalState.value = DeleteJournalState.Loading
            val result = financeRepository.deleteJournalEntry(id)
            result.fold(
                onSuccess = { message ->
                    _deleteJournalState.value = DeleteJournalState.Success(message)
                    refreshJournalEntries()
                },
                onFailure = { e ->
                    _deleteJournalState.value = DeleteJournalState.Error(
                        extractErrorMessage(e, "Failed to delete journal entry")
                    )
                }
            )
        }
    }

    fun fetchJournalEntryDetail(id: String) {
        launchBusy {
            _isLoadingJournalDetail.value = true
            _journalDetailError.value = null
            _journalEntryDetail.value = null
            val result = financeRepository.getJournalEntryViewOne(id)
            result.fold(
                onSuccess = { _journalEntryDetail.value = it },
                onFailure = { e ->
                    _journalDetailError.value = extractErrorMessage(e, "Failed to fetch journal entry")
                }
            )
            _isLoadingJournalDetail.value = false
        }
    }
    // ── VOID INVOICE STATE ──
    private val _voidInvoiceState = MutableStateFlow<VoidInvoiceState>(VoidInvoiceState.Idle)
    val voidInvoiceState: StateFlow<VoidInvoiceState> = _voidInvoiceState.asStateFlow()

    fun voidInvoice(invoiceId: String, reason: String) {
        if (invoiceId.isBlank()) return
        launchBusy {
            _voidInvoiceState.value = VoidInvoiceState.Loading

            // Endpoint: POST /api/finance/invoices/{id}/void
            val result = repo.request<VoidInvoiceResponse> {
                post(
                    "/api/finance/sales-invoices/void/$invoiceId",
                    VoidInvoiceRequest(reason = reason.trim())
                )
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _voidInvoiceState.value = VoidInvoiceState.Success(res.message)
                        fetchInvoiceDetail(invoiceId)
                        refreshInvoices()
                    } else {
                        _voidInvoiceState.value = VoidInvoiceState.Error(res.message.ifBlank { "Failed to void invoice" })
                    }
                },
                onFailure = { error ->
                    _voidInvoiceState.value = VoidInvoiceState.Error(
                        extractErrorMessage(error, "Failed to void invoice")
                    )
                }
            )
        }
    }

    fun resetVoidInvoiceState() {
        _voidInvoiceState.value = VoidInvoiceState.Idle
    }
    // ─────────────────────────────────────────────────────────────
    // ── SUPPLIER OVERVIEW API VIA API REPOSITORY ──
    // ─────────────────────────────────────────────────────────────
    private val _supplierOverview = MutableStateFlow<SupplierOverviewData?>(null)
    val supplierOverview: StateFlow<SupplierOverviewData?> = _supplierOverview.asStateFlow()

    private val _isLoadingSupplierOverview = MutableStateFlow(false)
    val isLoadingSupplierOverview: StateFlow<Boolean> = _isLoadingSupplierOverview.asStateFlow()

    private val _supplierOverviewError = MutableStateFlow<String?>(null)
    val supplierOverviewError: StateFlow<String?> = _supplierOverviewError.asStateFlow()

    fun fetchSupplierOverview(supplierId: String) {
        if (supplierId.isBlank()) return
        launchBusy {
            _isLoadingSupplierOverview.value = true
            _supplierOverviewError.value = null

            // Endpoint calling with GenericApi / ApiRepository
            val result = repo.request<SupplierOverviewApiResponse> {
                get("/api/inventory/supplier/$supplierId/overview")
            }

            result.fold(
                onSuccess = { response ->
                    _supplierOverview.value = response.data
                },
                onFailure = { error ->
                    _supplierOverviewError.value = extractErrorMessage(error, "Failed to load supplier overview")
                }
            )
            _isLoadingSupplierOverview.value = false
        }
    }

    // ── PAYMENT TERMS STATE ──
    private val _paymentTermsList = MutableStateFlow<List<PaymentTermItem>>(emptyList())
    val paymentTermsList: StateFlow<List<PaymentTermItem>> = _paymentTermsList.asStateFlow()

    private val _isLoadingPaymentTerms = MutableStateFlow(false)
    val isLoadingPaymentTerms: StateFlow<Boolean> = _isLoadingPaymentTerms.asStateFlow()

    fun fetchPaymentTerms() {
        launchBusy {
            _isLoadingPaymentTerms.value = true
            // Endpoint for payment terms list
            val result = repo.request<PaymentTermsResponse> {
                get("/api/finance/settings/payment-terms/view-all")
            }
            result.fold(
                onSuccess = { response ->
                    _paymentTermsList.value = response.data
                },
                onFailure = { /* Handle error if needed */ }
            )
            _isLoadingPaymentTerms.value = false
        }
    }

    // ── CREATE INVOICE STATE ──
    private val _createInvoiceState = MutableStateFlow<CreateInvoiceState>(CreateInvoiceState.Idle)
    val createInvoiceState: StateFlow<CreateInvoiceState> = _createInvoiceState.asStateFlow()

    // ── 1. SAVE AS DRAFT ONLY ──
    fun createSalesInvoice(
        customerId: String,
        invoiceDate: String,
        paymentTermId: String,
        currency: String = "INR",
        exchangeRate: String = "1",
        priceIncludesTax: Boolean = false,
        linesJson: String,
        notes: String,
        termsAndConditions: String,
        attachmentParts: List<okhttp3.MultipartBody.Part> = emptyList()
    ) {
        launchBusy {
            _createInvoiceState.value = CreateInvoiceState.Loading

            val result = repo.request<CreateInvoiceApiResponse> {
                postMultipart(
                    url = "/api/finance/sales-invoices/create",
                    parts = mapOf(
                        "customerId" to repo.text(customerId),
                        "invoiceDate" to repo.text(invoiceDate),
                        "paymentTermId" to repo.text(paymentTermId),
                        "currency" to repo.text(currency),
                        "exchangeRate" to repo.text(exchangeRate),
                        "priceIncludesTax" to repo.text(priceIncludesTax.toString()),
                        "lines" to repo.text(linesJson),
                        "notes" to repo.text(notes),
                        "termsAndConditions" to repo.text(termsAndConditions)
                    ),
                    files = attachmentParts
                )
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _createInvoiceState.value = CreateInvoiceState.Success(
                            message = response.message ?: "Invoice saved as draft",
                            invoice = response.data
                        )
                        refreshInvoices()
                    } else {
                        _createInvoiceState.value = CreateInvoiceState.Error(
                            response.message ?: "Failed to create invoice"
                        )
                    }
                },
                onFailure = { error ->
                    _createInvoiceState.value = CreateInvoiceState.Error(
                        extractErrorMessage(error, "Failed to create invoice")
                    )
                }
            )
        }
    }

    // ── 2. CREATE AND SEND INVOICE (POSTS TO LEDGER) ──
    // ── CREATE AND SEND SALES INVOICE ──
    fun createAndSendSalesInvoice(
        customerId: String,
        invoiceDate: String,
        paymentTermId: String,
        currency: String = "INR",
        exchangeRate: String = "1",
        priceIncludesTax: Boolean = false,
        linesJson: String,
        notes: String,
        termsAndConditions: String,
        attachmentParts: List<okhttp3.MultipartBody.Part> = emptyList()
    ) {
        launchBusy {
            _createInvoiceState.value = CreateInvoiceState.Loading

            // Step 1: Create invoice (POST /api/finance/sales-invoices/create)
            val createResult = repo.request<CreateInvoiceApiResponse> {
                postMultipart(
                    url = "/api/finance/sales-invoices/create",
                    parts = mapOf(
                        "customerId" to repo.text(customerId),
                        "invoiceDate" to repo.text(invoiceDate),
                        "paymentTermId" to repo.text(paymentTermId),
                        "currency" to repo.text(currency),
                        "exchangeRate" to repo.text(exchangeRate),
                        "priceIncludesTax" to repo.text(priceIncludesTax.toString()),
                        "lines" to repo.text(linesJson),
                        "notes" to repo.text(notes),
                        "termsAndConditions" to repo.text(termsAndConditions)
                    ),
                    files = attachmentParts
                )
            }

            createResult.fold(
                onSuccess = { createResponse ->
                    if (!createResponse.success) {
                        _createInvoiceState.value = CreateInvoiceState.Error(
                            createResponse.message ?: "Failed to create invoice"
                        )
                        return@fold
                    }

                    // Extract the created invoice ID from response
                    val createdInvoiceId = createResponse.data?.id.orEmpty()
                        .ifBlank { createResponse.data?.id.orEmpty() }

                    if (createdInvoiceId.isBlank()) {
                        _createInvoiceState.value = CreateInvoiceState.Error(
                            "Invoice created, but failed to retrieve invoice ID for sending."
                        )
                        return@fold
                    }

                    // Step 2: Send invoice with ID in path URL (POST /api/finance/sales-invoices/send/{id})
                    // Uses default empty body: emptyMap<String, String>()
                    val sendResult = repo.request<SendInvoiceApiResponse> {
                        post("/api/finance/sales-invoices/send/$createdInvoiceId")
                    }

                    sendResult.fold(
                        onSuccess = { sendResponse ->
                            if (sendResponse.success) {
                                _createInvoiceState.value = CreateInvoiceState.Success(
                                    message = sendResponse.message ?: "Invoice sent and posted to ledger.",
                                    invoice = sendResponse.data
                                )
                                refreshInvoices()
                            } else {
                                _createInvoiceState.value = CreateInvoiceState.Error(
                                    sendResponse.message ?: "Invoice created, but failed to send"
                                )
                            }
                        },
                        onFailure = { sendError ->
                            _createInvoiceState.value = CreateInvoiceState.Error(
                                "Invoice created as draft, but sending failed: " +
                                        extractErrorMessage(sendError, "Failed to send invoice")
                            )
                        }
                    )
                },
                onFailure = { createError ->
                    _createInvoiceState.value = CreateInvoiceState.Error(
                        extractErrorMessage(createError, "Failed to create invoice")
                    )
                }
            )
        }
    }

    // ── SEND STANDALONE INVOICE (DRAFT TO SENT) ──
    fun sendInvoice(invoiceId: String) {
        if (invoiceId.isBlank()) return
        launchBusy {
            _createInvoiceState.value = CreateInvoiceState.Loading

            // Hits POST /api/finance/sales-invoices/send/{id}
            val result = repo.request<SendInvoiceApiResponse> {
                post("/api/finance/sales-invoices/send/$invoiceId")
            }

            result.fold(
                onSuccess = { sendResponse ->
                    if (sendResponse.success) {
                        _createInvoiceState.value = CreateInvoiceState.Success(
                            message = sendResponse.message ?: "Invoice sent and posted to ledger.",
                            invoice = sendResponse.data
                        )
                        // Refresh details and list
                        fetchInvoiceDetail(invoiceId)
                        refreshInvoices()
                    } else {
                        _createInvoiceState.value = CreateInvoiceState.Error(
                            sendResponse.message ?: "Failed to send invoice"
                        )
                    }
                },
                onFailure = { sendError ->
                    _createInvoiceState.value = CreateInvoiceState.Error(
                        extractErrorMessage(sendError, "Failed to send invoice")
                    )
                }
            )
        }
    }

    // ── DELETE INVOICE STATE ──
    private val _deleteInvoiceState = MutableStateFlow<DeleteInvoiceState>(DeleteInvoiceState.Idle)
    val deleteInvoiceState: StateFlow<DeleteInvoiceState> = _deleteInvoiceState.asStateFlow()

    /**
     * Deletes a sales invoice by ID (DELETE /api/finance/sales-invoices/{id})
     * Automatically triggers refreshInvoices() on success.
     */
    fun deleteInvoice(invoiceId: String) {
        if (invoiceId.isBlank()) return
        launchBusy {
            _deleteInvoiceState.value = DeleteInvoiceState.Loading

            // Endpoint: DELETE /api/finance/sales-invoices/{id}
            val result = repo.request<GenericDeleteResponse> {
                delete("/api/finance/sales-invoices/delete-one/$invoiceId")
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _deleteInvoiceState.value = DeleteInvoiceState.Success(
                            response.message ?: "Invoice deleted successfully"
                        )
                        // Automatically refreshes the invoice list
                        refreshInvoices()
                    } else {
                        _deleteInvoiceState.value = DeleteInvoiceState.Error(
                            response.message ?: "Failed to delete invoice"
                        )
                    }
                },
                onFailure = { error ->
                    _deleteInvoiceState.value = DeleteInvoiceState.Error(
                        extractErrorMessage(error, "Failed to delete invoice")
                    )
                }
            )
        }
    }

    // ── Inside FinanceViewModel Class ──

    // 1. Customer Financial Profile State
    private val _customerFinancialProfile = MutableStateFlow<CustomerFinancialProfileData?>(null)
    val customerFinancialProfile: StateFlow<CustomerFinancialProfileData?> = _customerFinancialProfile.asStateFlow()

    private val _isLoadingCustomerProfile = MutableStateFlow(false)
    val isLoadingCustomerProfile: StateFlow<Boolean> = _isLoadingCustomerProfile.asStateFlow()

    private val _customerProfileError = MutableStateFlow<String?>(null)
    val customerProfileError: StateFlow<String?> = _customerProfileError.asStateFlow()

    // 2. Update Customer State
    private val _updateCustomerState = MutableStateFlow<UpdateCustomerState>(UpdateCustomerState.Idle)
    val updateCustomerState: StateFlow<UpdateCustomerState> = _updateCustomerState.asStateFlow()

    /**
     * Fetch Customer Financial Profile for Prefilling in Edit Mode
     */
    fun fetchCustomerFinancialProfile(customerId: String) {
        if (customerId.isBlank()) return
        launchBusy {
            _isLoadingCustomerProfile.value = true
            _customerProfileError.value = null

            val result = repo.request<CustomerFinancialProfileResponse> {
                get("/api/finance/customers/view-one/$customerId")
            }

            result.fold(
                onSuccess = { res ->
                    _customerFinancialProfile.value = res.data
                },
                onFailure = { error ->
                    _customerProfileError.value = extractErrorMessage(error, "Failed to load customer profile")
                }
            )
            _isLoadingCustomerProfile.value = false
        }
    }

    /**
     * Update Customer API
     */
    fun updateCustomer(
        customerId: String,
        payload: CreateCustomerPayload,
        onSuccessCallback: () -> Unit = {}
    ) {
        launchBusy {
            _updateCustomerState.value = UpdateCustomerState.Loading

            val result = repo.request<GenericDeleteResponse> {
                put("/api/sales/customers/update/$customerId", payload)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateCustomerState.value = UpdateCustomerState.Success(
                            response.message ?: "Customer updated successfully"
                        )
                        fetchCustomerForFinance(page = 1)
                        onSuccessCallback()
                    } else {
                        _updateCustomerState.value = UpdateCustomerState.Error(
                            response.message ?: "Failed to update customer"
                        )
                    }
                },
                onFailure = { throwable ->
                    _updateCustomerState.value = UpdateCustomerState.Error(
                        extractErrorMessage(throwable, "Failed to update customer")
                    )
                }
            )
        }
    }

    // ── RECORD PAYMENT RECEIPT STATE ──
    private val _recordPaymentState = MutableStateFlow<RecordPaymentState>(RecordPaymentState.Idle)
    val recordPaymentState: StateFlow<RecordPaymentState> = _recordPaymentState.asStateFlow()

    /**
     * Dynamic Multipart API to Record Customer Payment Receipt
     * Endpoint: POST /api/finance/payments-received/create (or /api/finance/sales-invoices/record-payment)
     */
    fun recordCustomerPayment(
        customerId: String,
        branchId: String,
        invoiceId: String,
        amount: Double,
        paymentMode: String,
        depositToAccountId: String,
        paymentDate: String,
        referenceNumber: String,
        notes: String,
        attachmentParts: List<okhttp3.MultipartBody.Part> = emptyList(),
        onSuccess: (String) -> Unit = {}
    ) {
        launchBusy {
            _recordPaymentState.value = RecordPaymentState.Loading

            val partsMap = mutableMapOf(
                "customerId" to repo.text(customerId),
                "branchId" to repo.text(branchId),
                "invoiceId" to repo.text(invoiceId),
                "amount" to repo.text(amount.toInt().toString()),
                "paymentMode" to repo.text(paymentMode),
                "depositToAccountId" to repo.text(depositToAccountId),
                "paymentDate" to repo.text(paymentDate),
                "notes" to repo.text(notes)
            )

            if (referenceNumber.isNotBlank()) {
                partsMap["referenceNumber"] = repo.text(referenceNumber)
            }

            val result = repo.request<RecordPaymentApiResponse> {
                postMultipart(
                    url = "/api/finance/sales-payments/create",
                    parts = partsMap,
                    files = attachmentParts
                )
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _recordPaymentState.value = RecordPaymentState.Success(
                            message = response.message,
                            data = response.data
                        )
                        // Refresh invoices list and view one details
                        fetchInvoiceDetail(invoiceId)
                        refreshInvoices()
                        onSuccess(response.message)
                    } else {
                        _recordPaymentState.value = RecordPaymentState.Error(
                            response.message.ifBlank { "Failed to record payment" }
                        )
                    }
                },
                onFailure = { error ->
                    _recordPaymentState.value = RecordPaymentState.Error(
                        extractErrorMessage(error, "Failed to record payment")
                    )
                }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ── CUSTOMER RECEIPTS / PAYMENTS RECEIVED STATE ──
    // ─────────────────────────────────────────────────────────────
    private val _receiptsList = MutableStateFlow<List<CustomerPaymentItem>>(emptyList())
    val receiptsList: StateFlow<List<CustomerPaymentItem>> = _receiptsList.asStateFlow()

    private val _receiptsPagination = MutableStateFlow<PaginationInfo?>(null)
    val receiptsPagination: StateFlow<PaginationInfo?> = _receiptsPagination.asStateFlow()

    private val _isLoadingReceipts = MutableStateFlow(false)
    val isLoadingReceipts: StateFlow<Boolean> = _isLoadingReceipts.asStateFlow()

    private val _isLoadingMoreReceipts = MutableStateFlow(false)
    val isLoadingMoreReceipts: StateFlow<Boolean> = _isLoadingMoreReceipts.asStateFlow()

    private val _canLoadMoreReceipts = MutableStateFlow(true)
    val canLoadMoreReceipts: StateFlow<Boolean> = _canLoadMoreReceipts.asStateFlow()

    private val _currentReceiptPage = MutableStateFlow(1)
    val currentReceiptPage: StateFlow<Int> = _currentReceiptPage.asStateFlow()

    private val _receiptsError = MutableStateFlow<String?>(null)
    val receiptsError: StateFlow<String?> = _receiptsError.asStateFlow()

    private var activeReceiptSearch: String? = null
    private var activeReceiptStatus: String? = null
    private var fetchReceiptsJob: Job? = null

    /**
     * Fetch customer payments / receipts with pagination & search
     */
    fun fetchCustomerReceipts(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchReceiptsJob?.cancel()
        fetchReceiptsJob = launchBusy {
            _isLoadingReceipts.value = true
            _receiptsError.value = null
            _currentReceiptPage.value = page
            activeReceiptSearch = search?.trim()?.ifBlank { null }
            activeReceiptStatus = status?.trim()?.ifBlank { null }

            val queryParams = mutableMapOf<String, Any>(
                "page" to page,
                "limit" to limit
            )
            activeReceiptSearch?.let { queryParams["search"] = it }
            activeReceiptStatus?.let { queryParams["status"] = it }

            val result = repo.request<CustomerPaymentResponse> {
                get("/api/finance/sales-payments/view-all", queryParams)
            }

            result.fold(
                onSuccess = { response ->
                    _receiptsList.value = response.data
                    _receiptsPagination.value = response.pagination

                    val totalPages = response.pagination?.totalPages ?: 1
                    _canLoadMoreReceipts.value = page < totalPages && response.data.isNotEmpty()
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _receiptsError.value = extractErrorMessage(e, "Failed to load receipts")
                    }
                }
            )
            _isLoadingReceipts.value = false
        }
    }

    /**
     * Load next page for infinite scrolling
     */
    fun loadMoreCustomerReceipts(limit: Int = 10) {
        if (_isLoadingMoreReceipts.value || _isLoadingReceipts.value || !_canLoadMoreReceipts.value) return

        launchBusy {
            _isLoadingMoreReceipts.value = true
            val nextPage = _currentReceiptPage.value + 1

            val queryParams = mutableMapOf<String, Any>(
                "page" to nextPage,
                "limit" to limit
            )
            activeReceiptSearch?.let { queryParams["search"] = it }
            activeReceiptStatus?.let { queryParams["status"] = it }

            val result = repo.request<CustomerPaymentResponse> {
                get("/api/finance/sales-payments/view-all", queryParams)
            }

            result.fold(
                onSuccess = { response ->
                    val newItems = response.data
                    if (newItems.isNotEmpty()) {
                        _receiptsList.value += newItems
                        _currentReceiptPage.value = nextPage
                        _receiptsPagination.value = response.pagination

                        val totalPages = response.pagination?.totalPages ?: nextPage
                        _canLoadMoreReceipts.value = nextPage < totalPages
                    } else {
                        _canLoadMoreReceipts.value = false
                    }
                },
                onFailure = {
                    // Fail silently to allow retry on next scroll
                }
            )
            _isLoadingMoreReceipts.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ── OUTSTANDING INVOICES PER CUSTOMER ──
    // ─────────────────────────────────────────────────────────────
    private val _outstandingInvoices = MutableStateFlow<List<CustomerOutstandingInvoiceItem>>(emptyList())
    val outstandingInvoices: StateFlow<List<CustomerOutstandingInvoiceItem>> = _outstandingInvoices.asStateFlow()

    private val _isLoadingOutstandingInvoices = MutableStateFlow(false)
    val isLoadingOutstandingInvoices: StateFlow<Boolean> = _isLoadingOutstandingInvoices.asStateFlow()

    /**
     * Fetch unpaid/outstanding invoices for the selected customer
     * Endpoint: GET /api/finance/customers/:customerId/outstanding-invoices
     */
    fun fetchCustomerOutstandingInvoices(customerId: String) {
        if (customerId.isBlank()) {
            _outstandingInvoices.value = emptyList()
            return
        }
        launchBusy {
            _isLoadingOutstandingInvoices.value = true
            val result = repo.request<CustomerOutstandingInvoicesResponse> {
                get("/api/finance/sales-invoices/outstanding/$customerId")
            }
            result.fold(
                onSuccess = { response ->
                    _outstandingInvoices.value = response.data
                },
                onFailure = {
                    _outstandingInvoices.value = emptyList()
                }
            )
            _isLoadingOutstandingInvoices.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ── RECEIPT / PAYMENT DETAIL & VOID ──
    // ─────────────────────────────────────────────────────────────
    private val _receiptDetail = MutableStateFlow<CustomerPaymentDetailData?>(null)
    val receiptDetail: StateFlow<CustomerPaymentDetailData?> = _receiptDetail.asStateFlow()

    private val _isLoadingReceiptDetail = MutableStateFlow(false)
    val isLoadingReceiptDetail: StateFlow<Boolean> = _isLoadingReceiptDetail.asStateFlow()

    private val _receiptDetailError = MutableStateFlow<String?>(null)
    val receiptDetailError: StateFlow<String?> = _receiptDetailError.asStateFlow()

    /**
     * Fetch single payment receipt details
     */
    fun fetchReceiptDetail(paymentId: String) {
        if (paymentId.isBlank()) return
        launchBusy {
            _isLoadingReceiptDetail.value = true
            _receiptDetailError.value = null
            _receiptDetail.value = null

            val result = repo.request<CustomerPaymentDetailResponse> {
                get("/api/finance/sales-payments/view-one/$paymentId")
            }

            result.fold(
                onSuccess = { response ->
                    _receiptDetail.value = response.data
                },
                onFailure = { error ->
                    _receiptDetailError.value = extractErrorMessage(error, "Failed to load receipt details")
                }
            )
            _isLoadingReceiptDetail.value = false
        }
    }

    /**
     * Void a payment receipt
     */
    fun voidCustomerPayment(paymentId: String, reason: String = "Void requested", onSuccess: () -> Unit = {}) {
        if (paymentId.isBlank()) return
        launchBusy {
            val result = repo.request<GenericDeleteResponse> {
                post(
                    "/api/finance/sales-payments/void/$paymentId",
                    mapOf("reason" to reason)
                )
            }
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        fetchReceiptDetail(paymentId)
                        refreshCustomerReceipts()
                        onSuccess()
                    }
                },
                onFailure = { /* Handle error */ }
            )
        }
    }

    fun clearReceiptDetail() {
        _receiptDetail.value = null
        _receiptDetailError.value = null
    }

    fun clearOutstandingInvoices() {
        _outstandingInvoices.value = emptyList()
    }

    fun refreshCustomerReceipts() {
        fetchCustomerReceipts(page = 1, search = activeReceiptSearch, status = activeReceiptStatus)
    }

    fun resetRecordPaymentState() {
        _recordPaymentState.value = RecordPaymentState.Idle
    }

    fun clearCustomerFinancialProfile() {
        _customerFinancialProfile.value = null
        _customerProfileError.value = null
    }

    fun resetUpdateCustomerState() {
        _updateCustomerState.value = UpdateCustomerState.Idle
    }

    fun resetDeleteInvoiceState() {
        _deleteInvoiceState.value = DeleteInvoiceState.Idle
    }

    fun resetCreateInvoiceState() {
        _createInvoiceState.value = CreateInvoiceState.Idle
    }

    fun clearSupplierOverview() {
        _supplierOverview.value = null
        _supplierOverviewError.value = null
    }
    fun clearJournalEntryDetail() {
        _journalEntryDetail.value = null
        _journalDetailError.value = null
    }

    fun resetDeleteJournalState() {
        _deleteJournalState.value = DeleteJournalState.Idle
    }

    fun resetCreateJournalState() {
        _createJournalState.value = CreateJournalState.Idle
    }

    fun resetUpdateJournalState() {
        _updateJournalState.value = UpdateJournalState.Idle
    }
}