package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.ExpenseItem
import com.cuso.mobile.model.finance.ExpensePagination
import com.cuso.mobile.model.finance.InvoiceItem
import com.cuso.mobile.model.finance.InvoiceViewOneData
import com.cuso.mobile.model.finance.JournalEntryItem
import com.cuso.mobile.model.finance.JournalEntryPagination
import com.cuso.mobile.model.finance.TrialBalanceItem
import com.cuso.mobile.model.sales.CustomerListResponseV2
import com.cuso.mobile.model.sales.FinanceCustomerViewOneData
import com.cuso.mobile.model.sales.PaginationInfo
import com.cuso.mobile.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@Suppress("UNUSED_PARAMETER")
/**
 * FinanceViewModel - Handles all finance and customer-related operations
 * Uses FinanceRepository for V2 API calls
 */

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

    // ── Chart of Accounts: create ──
    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.Idle)
    val createAccountState: StateFlow<CreateAccountState> = _createAccountState.asStateFlow()

    private val _financeCustomerList = MutableStateFlow<CustomerListResponseV2?>(null)
    val financeCustomerList: StateFlow<CustomerListResponseV2?> = _financeCustomerList.asStateFlow()

    private val _isLoadingFinanceCustomers = MutableStateFlow(false)
    val isLoadingFinanceCustomers: StateFlow<Boolean> = _isLoadingFinanceCustomers.asStateFlow()

    private val _financeCustomerError = MutableStateFlow<String?>(null)
    val financeCustomerError: StateFlow<String?> = _financeCustomerError.asStateFlow()

    // ── View One (Customer Detail) ──
    private val _financeCustomerDetail = MutableStateFlow<FinanceCustomerViewOneData?>(null)
    val financeCustomerDetail: StateFlow<FinanceCustomerViewOneData?> = _financeCustomerDetail.asStateFlow()

    private val _isLoadingFinanceCustomerDetail = MutableStateFlow(false)
    val isLoadingFinanceCustomerDetail: StateFlow<Boolean> = _isLoadingFinanceCustomerDetail.asStateFlow()

    private val _financeCustomerDetailError = MutableStateFlow<String?>(null)
    val financeCustomerDetailError: StateFlow<String?> = _financeCustomerDetailError.asStateFlow()

    // ── Invoices (View All) ──
    private val _invoiceList = MutableStateFlow<List<InvoiceItem>>(emptyList())
    val invoiceList: StateFlow<List<InvoiceItem>> = _invoiceList.asStateFlow()

    private val _invoicePagination = MutableStateFlow<PaginationInfo?>(null)
    val invoicePagination: StateFlow<PaginationInfo?> = _invoicePagination.asStateFlow()

    private val _isLoadingInvoices = MutableStateFlow(false)
    val isLoadingInvoices: StateFlow<Boolean> = _isLoadingInvoices.asStateFlow()

    private val _invoiceError = MutableStateFlow<String?>(null)
    val invoiceError: StateFlow<String?> = _invoiceError.asStateFlow()

    // ── Selected invoice (View One) — no separate API, reuses list item ──
    private val _selectedInvoice = MutableStateFlow<InvoiceItem?>(null)
    val selectedInvoice: StateFlow<InvoiceItem?> = _selectedInvoice.asStateFlow()

    // Add these states and functions to FinanceViewModel:

    // ── Invoice View One (Detail) ──
    private val _invoiceDetail = MutableStateFlow<InvoiceViewOneData?>(null)
    val invoiceDetail: StateFlow<InvoiceViewOneData?> = _invoiceDetail.asStateFlow()

    private val _isLoadingInvoiceDetail = MutableStateFlow(false)
    val isLoadingInvoiceDetail: StateFlow<Boolean> = _isLoadingInvoiceDetail.asStateFlow()

    private val _invoiceDetailError = MutableStateFlow<String?>(null)
    val invoiceDetailError: StateFlow<String?> = _invoiceDetailError.asStateFlow()

    // ── Chart of Accounts ──
    private val _chartOfAccounts = MutableStateFlow<List<ChartOfAccountItem>>(emptyList())
    val chartOfAccounts: StateFlow<List<ChartOfAccountItem>> = _chartOfAccounts.asStateFlow()

    private val _isLoadingChartOfAccounts = MutableStateFlow(false)
    val isLoadingChartOfAccounts: StateFlow<Boolean> = _isLoadingChartOfAccounts.asStateFlow()

    private val _chartOfAccountsError = MutableStateFlow<String?>(null)
    val chartOfAccountsError: StateFlow<String?> = _chartOfAccountsError.asStateFlow()

    // ── Expenses: list ──
    private val _expenseList = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenseList: StateFlow<List<ExpenseItem>> = _expenseList.asStateFlow()

    private val _expensePagination = MutableStateFlow<ExpensePagination?>(null)
    val expensePagination: StateFlow<ExpensePagination?> = _expensePagination.asStateFlow()

    private val _isLoadingExpenses = MutableStateFlow(false)
    val isLoadingExpenses: StateFlow<Boolean> = _isLoadingExpenses.asStateFlow()

    private val _expenseError = MutableStateFlow<String?>(null)
    val expenseError: StateFlow<String?> = _expenseError.asStateFlow()

    // ── Expenses: view one ──
    private val _expenseDetail = MutableStateFlow<ExpenseItem?>(null)
    val expenseDetail: StateFlow<ExpenseItem?> = _expenseDetail.asStateFlow()

    private val _isLoadingExpenseDetail = MutableStateFlow(false)
    val isLoadingExpenseDetail: StateFlow<Boolean> = _isLoadingExpenseDetail.asStateFlow()

    // ── Expenses: create ──
    private val _createExpenseState = MutableStateFlow<CreateExpenseState>(CreateExpenseState.Idle)
    val createExpenseState: StateFlow<CreateExpenseState> = _createExpenseState.asStateFlow()

    // ── Chart of Accounts: update ──
    private val _updateAccountState = MutableStateFlow<UpdateAccountState>(UpdateAccountState.Idle)
    val updateAccountState: StateFlow<UpdateAccountState> = _updateAccountState.asStateFlow()

    // ── Chart of Accounts: delete ──
    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState.asStateFlow()

    // ── Trial Balance ──
    private val _trialBalanceList = MutableStateFlow<List<TrialBalanceItem>>(emptyList())
    val trialBalanceList: StateFlow<List<TrialBalanceItem>> = _trialBalanceList.asStateFlow()

    private val _isLoadingTrialBalance = MutableStateFlow(false)
    val isLoadingTrialBalance: StateFlow<Boolean> = _isLoadingTrialBalance.asStateFlow()

    private val _trialBalanceError = MutableStateFlow<String?>(null)
    val trialBalanceError: StateFlow<String?> = _trialBalanceError.asStateFlow()

    // ── Journal Entries ──
    private val _journalEntries = MutableStateFlow<List<JournalEntryItem>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntryItem>> = _journalEntries.asStateFlow()

    private val _journalEntryPagination = MutableStateFlow<JournalEntryPagination?>(null)
    val journalEntryPagination: StateFlow<JournalEntryPagination?> = _journalEntryPagination.asStateFlow()

    private val _isLoadingJournalEntries = MutableStateFlow(false)
    val isLoadingJournalEntries: StateFlow<Boolean> = _isLoadingJournalEntries.asStateFlow()

    private val _journalEntriesError = MutableStateFlow<String?>(null)
    val journalEntriesError: StateFlow<String?> = _journalEntriesError.asStateFlow()

    // ── Journal Entries: create ──
    private val _createJournalState = MutableStateFlow<CreateJournalState>(CreateJournalState.Idle)
    val createJournalState: StateFlow<CreateJournalState> = _createJournalState.asStateFlow()

    fun createJournal(
        branchId: String,
        entryDate: String,
        reference: String?,
        notes: String?,
        status: String = "Posted",
        lines: List<com.cuso.mobile.model.finance.JournalEntryLineRequest>
    ) {
        viewModelScope.launch {
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
                    fetchJournalEntries()   // refresh list after posting
                },
                onFailure = { e ->
                    _createJournalState.value = CreateJournalState.Error(
                        e.message ?: "Failed to post journal entry"
                    )
                }
            )
        }
    }

    fun resetCreateJournalState() {
        _createJournalState.value = CreateJournalState.Idle
    }

    fun getFinanceCustomerViewOne(id: String) {
        viewModelScope.launch {
            _isLoadingFinanceCustomerDetail.value = true
            _financeCustomerDetailError.value = null

            val result = financeRepository.getFinanceCustomerViewOne(id)

            when {
                result.isSuccess -> {
                    _financeCustomerDetail.value = result.getOrNull()?.data
                }
                result.isFailure -> {
                    _financeCustomerDetailError.value = result.exceptionOrNull()?.message ?: "Failed to fetch customer detail"
                }
            }
            _isLoadingFinanceCustomerDetail.value = false
        }
    }

    fun fetchCustomerForFinance(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingFinanceCustomers.value = true
            _financeCustomerError.value = null

            val result = financeRepository.getCustomerForFinance(page, limit, search)

            when {
                result.isSuccess -> {
                    _financeCustomerList.value = result.getOrNull()
                }
                result.isFailure -> {
                    _financeCustomerError.value = result.exceptionOrNull()?.message ?: "Failed to fetch customers"
                }
            }
            _isLoadingFinanceCustomers.value = false
        }
    }



    fun fetchInvoices(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingInvoices.value = true
            _invoiceError.value = null

            val result = financeRepository.getInvoices(page, limit, search, status)

            when {
                result.isSuccess -> {
                    val body = result.getOrNull()
                    _invoiceList.value = body?.data?.data ?: emptyList()
                    _invoicePagination.value = body?.data?.pagination
                }
                result.isFailure -> {
                    _invoiceError.value = result.exceptionOrNull()?.message ?: "Failed to fetch invoices"
                }
            }
            _isLoadingInvoices.value = false
        }
    }



    /**
     * Fetch single invoice details by ID
     */
    fun fetchInvoiceDetail(invoiceId: String) {
        viewModelScope.launch {
            _isLoadingInvoiceDetail.value = true
            _invoiceDetailError.value = null
            _invoiceDetail.value = null

            val result = financeRepository.getInvoiceViewOne(invoiceId)

            when {
                result.isSuccess -> {
                    _invoiceDetail.value = result.getOrNull()
                }
                result.isFailure -> {
                    _invoiceDetailError.value = result.exceptionOrNull()?.message ?: "Failed to fetch invoice details"
                }
            }
            _isLoadingInvoiceDetail.value = false
        }
    }

    fun fetchChartOfAccounts() {
        viewModelScope.launch {
            _isLoadingChartOfAccounts.value = true
            _chartOfAccountsError.value = null

            val result = financeRepository.getChartOfAccounts()
            result.fold(
                onSuccess = { _chartOfAccounts.value = it },
                onFailure = { _chartOfAccountsError.value = it.message ?: "Failed to fetch chart of accounts" }
            )
            _isLoadingChartOfAccounts.value = false
        }
    }

    fun fetchExpenses(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingExpenses.value = true
            _expenseError.value = null

            val result = financeRepository.getExpenses(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    _expenseList.value = response.data
                    _expensePagination.value = response.pagination
                },
                onFailure = { _expenseError.value = it.message ?: "Failed to fetch expenses" }
            )
            _isLoadingExpenses.value = false
        }
    }

    fun fetchExpenseDetail(id: String) {
        viewModelScope.launch {
            _isLoadingExpenseDetail.value = true
            _expenseDetail.value = null

            val result = financeRepository.getExpenseViewOne(id)
            result.fold(
                onSuccess = { _expenseDetail.value = it },
                onFailure = { _expenseError.value = it.message ?: "Failed to fetch expense detail" }
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
        viewModelScope.launch {
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
                    fetchExpenses()   // refresh list after creating
                },
                onFailure = { e ->
                    _createExpenseState.value = CreateExpenseState.Error(
                        e.message ?: "Failed to create expense"
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
        viewModelScope.launch {
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
                    fetchChartOfAccounts()   // refresh dropdown list after adding
                },
                onFailure = { e ->
                    _createAccountState.value = CreateAccountState.Error(
                        e.message ?: "Failed to create account"
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
        viewModelScope.launch {
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
                    fetchChartOfAccounts()   // refresh list after update
                },
                onFailure = { e ->
                    _updateAccountState.value = UpdateAccountState.Error(
                        e.message ?: "Failed to update account"
                    )
                }
            )
        }
    }

    fun deleteChartOfAccount(id: String) {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.Loading

            val result = financeRepository.deleteChartOfAccount(id)

            result.fold(
                onSuccess = { response ->
                    _deleteAccountState.value = DeleteAccountState.Success(
                        response.message ?: "Account deleted successfully"
                    )
                    fetchChartOfAccounts()   // refresh list after delete
                },
                onFailure = { e ->
                    _deleteAccountState.value = DeleteAccountState.Error(
                        e.message ?: "Failed to delete account"
                    )
                }
            )
        }
    }

    fun fetchTrialBalance() {
        viewModelScope.launch {
            _isLoadingTrialBalance.value = true
            _trialBalanceError.value = null

            val result = financeRepository.getTrialBalance()
            result.fold(
                onSuccess = { _trialBalanceList.value = it },
                onFailure = { _trialBalanceError.value = it.message ?: "Failed to fetch trial balance" }
            )
            _isLoadingTrialBalance.value = false
        }
    }

    fun fetchJournalEntries(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingJournalEntries.value = true
            _journalEntriesError.value = null

            val result = financeRepository.getJournalEntries(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    _journalEntries.value = response.data
                    _journalEntryPagination.value = response.pagination
                },
                onFailure = { _journalEntriesError.value = it.message ?: "Failed to fetch journal entries" }
            )
            _isLoadingJournalEntries.value = false
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

    /**
     * Clear invoice detail state
     */
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