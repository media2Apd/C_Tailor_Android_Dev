@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)
package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.ExpenseItem
import com.cuso.mobile.model.finance.ExpensePagination
import com.cuso.mobile.model.finance.InvoiceItem
import com.cuso.mobile.model.finance.InvoiceViewOneData
import com.cuso.mobile.model.finance.JournalEntryDetailData
import com.cuso.mobile.model.finance.JournalEntryItem
import com.cuso.mobile.model.finance.JournalEntryPagination
import com.cuso.mobile.model.finance.LedgerItem
import com.cuso.mobile.model.finance.TrialBalanceItem
import com.cuso.mobile.model.sales.CustomerListResponseV2
import com.cuso.mobile.model.sales.FinanceCustomerViewOneData
import com.cuso.mobile.model.sales.PaginationInfo
import com.cuso.mobile.repository.FinanceRepository
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject

sealed class CreateAccountState {
    object Idle : CreateAccountState()
    object Loading : CreateAccountState()
    data class Success(val message: String) : CreateAccountState()
    data class Error(val message: String) : CreateAccountState()
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeRepository: FinanceRepository
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
                        _expenseList.value = _expenseList.value + newExpenses
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
                        _invoiceList.value = _invoiceList.value + newInvoices
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
    // ── 3. FINANCE CUSTOMERS: Pagination & State ──
    // ─────────────────────────────────────────────────────────────
    private val _financeCustomerList = MutableStateFlow<CustomerListResponseV2?>(null)
    val financeCustomerList: StateFlow<CustomerListResponseV2?> = _financeCustomerList.asStateFlow()

    private val _isLoadingFinanceCustomers = MutableStateFlow(false)
    val isLoadingFinanceCustomers: StateFlow<Boolean> = _isLoadingFinanceCustomers.asStateFlow()

    private val _isLoadingMoreFinanceCustomers = MutableStateFlow(false)
    val isLoadingMoreFinanceCustomers: StateFlow<Boolean> = _isLoadingMoreFinanceCustomers.asStateFlow()

    private val _canLoadMoreFinanceCustomers = MutableStateFlow(true)
    val canLoadMoreFinanceCustomers: StateFlow<Boolean> = _canLoadMoreFinanceCustomers.asStateFlow()

    private val _currentFinanceCustomerPage = MutableStateFlow(1)
    val currentFinanceCustomerPage: StateFlow<Int> = _currentFinanceCustomerPage.asStateFlow()

    private val _financeCustomerError = MutableStateFlow<String?>(null)
    val financeCustomerError: StateFlow<String?> = _financeCustomerError.asStateFlow()

    private var activeFinanceCustomerSearch: String? = null
    private var fetchFinanceCustomersJob: Job? = null

    fun fetchCustomerForFinance(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null
    ) {
        fetchFinanceCustomersJob?.cancel()
        fetchFinanceCustomersJob = launchBusy {
            _isLoadingFinanceCustomers.value = true
            _financeCustomerError.value = null
            _currentFinanceCustomerPage.value = page
            activeFinanceCustomerSearch = search

            val result = financeRepository.getCustomerForFinance(page, limit, search)
            when {
                result.isSuccess -> {
                    val response = result.getOrNull()
                    val customers = response?.data ?: emptyList()
                    val pagination = response?.pagination

                    _financeCustomerList.value = response

                    val totalPages = pagination?.totalPages ?: 1
                    _canLoadMoreFinanceCustomers.value = page < totalPages && customers.isNotEmpty()
                }
                result.isFailure -> {
                    val e = result.exceptionOrNull()
                    if (e !is CancellationException) {
                        _financeCustomerError.value = extractErrorMessage(e, "Failed to fetch customers")
                    }
                }
            }
            _isLoadingFinanceCustomers.value = false
        }
    }

    fun loadMoreCustomerForFinance(limit: Int = 10) {
        if (_isLoadingMoreFinanceCustomers.value || _isLoadingFinanceCustomers.value || !_canLoadMoreFinanceCustomers.value) {
            return
        }

        launchBusy {
            _isLoadingMoreFinanceCustomers.value = true
            val nextPage = _currentFinanceCustomerPage.value + 1

            val result = financeRepository.getCustomerForFinance(
                page = nextPage,
                limit = limit,
                search = activeFinanceCustomerSearch
            )

            when {
                result.isSuccess -> {
                    val newResponse = result.getOrNull()
                    val newCustomers = newResponse?.data ?: emptyList()
                    val pagination = newResponse?.pagination

                    if (newCustomers.isNotEmpty()) {
                        val currentCustomers = _financeCustomerList.value?.data ?: emptyList()
                        _financeCustomerList.value = _financeCustomerList.value?.copy(
                            data = currentCustomers + newCustomers,
                            pagination = pagination
                        ) ?: newResponse

                        _currentFinanceCustomerPage.value = nextPage
                        val totalPages = pagination?.totalPages ?: nextPage
                        _canLoadMoreFinanceCustomers.value = nextPage < totalPages
                    } else {
                        _canLoadMoreFinanceCustomers.value = false
                    }
                }
                result.isFailure -> {
                    // Do not permanently lock pagination
                }
            }
            _isLoadingMoreFinanceCustomers.value = false
        }
    }

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
                        _journalEntries.value = _journalEntries.value + newEntries
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
    // ── 5. OTHER FINANCE OPERATIONS ──
    // ─────────────────────────────────────────────────────────────

    // Chart of Accounts
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

    // View One Customer
    private val _financeCustomerDetail = MutableStateFlow<FinanceCustomerViewOneData?>(null)
    val financeCustomerDetail: StateFlow<FinanceCustomerViewOneData?> = _financeCustomerDetail.asStateFlow()

    private val _isLoadingFinanceCustomerDetail = MutableStateFlow(false)
    val isLoadingFinanceCustomerDetail: StateFlow<Boolean> = _isLoadingFinanceCustomerDetail.asStateFlow()

    private val _financeCustomerDetailError = MutableStateFlow<String?>(null)
    val financeCustomerDetailError: StateFlow<String?> = _financeCustomerDetailError.asStateFlow()

    // Invoices View One
    private val _selectedInvoice = MutableStateFlow<InvoiceItem?>(null)
    val selectedInvoice: StateFlow<InvoiceItem?> = _selectedInvoice.asStateFlow()

    private val _invoiceDetail = MutableStateFlow<InvoiceViewOneData?>(null)
    val invoiceDetail: StateFlow<InvoiceViewOneData?> = _invoiceDetail.asStateFlow()

    private val _isLoadingInvoiceDetail = MutableStateFlow(false)
    val isLoadingInvoiceDetail: StateFlow<Boolean> = _isLoadingInvoiceDetail.asStateFlow()

    private val _invoiceDetailError = MutableStateFlow<String?>(null)
    val invoiceDetailError: StateFlow<String?> = _invoiceDetailError.asStateFlow()

    // Expenses: detail & create
    private val _expenseDetail = MutableStateFlow<ExpenseItem?>(null)
    val expenseDetail: StateFlow<ExpenseItem?> = _expenseDetail.asStateFlow()

    private val _isLoadingExpenseDetail = MutableStateFlow(false)
    val isLoadingExpenseDetail: StateFlow<Boolean> = _isLoadingExpenseDetail.asStateFlow()

    private val _createExpenseState = MutableStateFlow<CreateExpenseState>(CreateExpenseState.Idle)
    val createExpenseState: StateFlow<CreateExpenseState> = _createExpenseState.asStateFlow()

    // Trial Balance
    private val _trialBalanceList = MutableStateFlow<List<TrialBalanceItem>>(emptyList())
    val trialBalanceList: StateFlow<List<TrialBalanceItem>> = _trialBalanceList.asStateFlow()

    private val _isLoadingTrialBalance = MutableStateFlow(false)
    val isLoadingTrialBalance: StateFlow<Boolean> = _isLoadingTrialBalance.asStateFlow()

    private val _trialBalanceError = MutableStateFlow<String?>(null)
    val trialBalanceError: StateFlow<String?> = _trialBalanceError.asStateFlow()

    // Ledger
    private val _ledgerList = MutableStateFlow<List<LedgerItem>>(emptyList())
    val ledgerList: StateFlow<List<LedgerItem>> = _ledgerList.asStateFlow()

    private val _isLoadingLedger = MutableStateFlow(false)
    val isLoadingLedger: StateFlow<Boolean> = _isLoadingLedger.asStateFlow()

    private val _ledgerError = MutableStateFlow<String?>(null)
    val ledgerError: StateFlow<String?> = _ledgerError.asStateFlow()

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
        lines: List<com.cuso.mobile.model.finance.JournalEntryLineRequest>
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
        lines: List<com.cuso.mobile.model.finance.JournalEntryLineRequest>
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

    fun clearJournalEntryDetail() {
        _journalEntryDetail.value = null
        _journalDetailError.value = null
    }

    fun resetDeleteJournalState() {
        _deleteJournalState.value = DeleteJournalState.Idle
    }

    fun clearLedger() {
        _ledgerList.value = emptyList()
        _ledgerError.value = null
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

    fun clearInvoiceDetail() {
        _invoiceDetail.value = null
        _invoiceDetailError.value = null
        _isLoadingInvoiceDetail.value = false
    }

    fun selectInvoice(invoice: InvoiceItem) {
        _selectedInvoice.value = invoice
    }

    fun clearSelectedInvoice() {
        _selectedInvoice.value = null
    }

    fun resetCreateExpenseState() {
        _createExpenseState.value = CreateExpenseState.Idle
    }

    fun clearExpenseDetail() {
        _expenseDetail.value = null
    }

    fun resetCreateJournalState() {
        _createJournalState.value = CreateJournalState.Idle
    }

    fun resetUpdateJournalState() {
        _updateJournalState.value = UpdateJournalState.Idle
    }
}

sealed class CreateExpenseState {
    object Idle : CreateExpenseState()
    object Loading : CreateExpenseState()
    data class Success(val message: String) : CreateExpenseState()
    data class Error(val message: String) : CreateExpenseState()
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