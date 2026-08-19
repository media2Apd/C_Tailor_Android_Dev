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
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
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

    /**
     * Every failure path below ultimately gets its message from Throwable.message,
     * which — depending on how the repository/network layer surfaces HTTP errors —
     * can be the RAW response body string, e.g.
     *   {"success":false,"message":"E11000 duplicate key error collection: ..."}
     * instead of a clean message. This helper extracts ONLY the "message" field
     * from that JSON when present; if the body isn't JSON, or parsing fails, or
     * the field is blank, it falls back to the given hardcoded [fallback] string.
     */
    private fun extractErrorMessage(throwable: Throwable?, fallback: String): String {
        val raw = throwable?.message?.trim()
        if (raw.isNullOrBlank()) return fallback
        return try {
            JSONObject(raw).optString("message").takeIf { it.isNotBlank() } ?: fallback
        } catch (e: Exception) {
            // Not JSON — e.g. a plain exception message like "Unable to resolve host".
            // Only use it as-is if it doesn't look like a stray JSON fragment.
            raw.takeIf { !it.startsWith("{") } ?: fallback
        }
    }

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

    //   NEW — Journal Entries: update
    private val _updateJournalState = MutableStateFlow<UpdateJournalState>(UpdateJournalState.Idle)
    val updateJournalState: StateFlow<UpdateJournalState> = _updateJournalState.asStateFlow()

    // ── Ledger ──
    private val _ledgerList = MutableStateFlow<List<LedgerItem>>(emptyList())
    val ledgerList: StateFlow<List<LedgerItem>> = _ledgerList.asStateFlow()

    private val _isLoadingLedger = MutableStateFlow(false)
    val isLoadingLedger: StateFlow<Boolean> = _isLoadingLedger.asStateFlow()

    private val _ledgerError = MutableStateFlow<String?>(null)
    val ledgerError: StateFlow<String?> = _ledgerError.asStateFlow()

    private val _deleteJournalState = MutableStateFlow<DeleteJournalState>(DeleteJournalState.Idle)
    val deleteJournalState: StateFlow<DeleteJournalState> = _deleteJournalState.asStateFlow()

    // ── Journal Entry: view one (detail, for View/Edit prefill) ──
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
                        extractErrorMessage(e, "Failed to post journal entry")
                    )
                }
            )
        }
    }

    fun resetCreateJournalState() {
        _createJournalState.value = CreateJournalState.Idle
    }

    //   NEW — updates an existing journal entry by id, then refreshes the list
    fun updateJournal(
        id: String,
        branchId: String,
        entryDate: String,
        reference: String?,
        notes: String?,
        status: String = "Posted",
        lines: List<com.cuso.mobile.model.finance.JournalEntryLineRequest>
    ) {
        viewModelScope.launch {
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
                    fetchJournalEntries()   // refresh list after updating
                },
                onFailure = { e ->
                    _updateJournalState.value = UpdateJournalState.Error(
                        extractErrorMessage(e, "Failed to update journal entry")
                    )
                }
            )
        }
    }

    fun resetUpdateJournalState() {
        _updateJournalState.value = UpdateJournalState.Idle
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
                    _financeCustomerDetailError.value =
                        extractErrorMessage(result.exceptionOrNull(), "Failed to fetch customer detail")
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
                    _financeCustomerError.value =
                        extractErrorMessage(result.exceptionOrNull(), "Failed to fetch customers")
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
                    _invoiceError.value =
                        extractErrorMessage(result.exceptionOrNull(), "Failed to fetch invoices")
                }
            }
            _isLoadingInvoices.value = false
        }
    }



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
                    _invoiceDetailError.value =
                        extractErrorMessage(result.exceptionOrNull(), "Failed to fetch invoice details")
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
                onFailure = { e ->
                    _chartOfAccountsError.value = extractErrorMessage(e, "Failed to fetch chart of accounts")
                }
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
                onFailure = { e ->
                    _expenseError.value = extractErrorMessage(e, "Failed to fetch expenses")
                }
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
                        extractErrorMessage(e, "Failed to update account")
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
                        extractErrorMessage(e, "Failed to delete account")
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
                onFailure = { e ->
                    _trialBalanceError.value = extractErrorMessage(e, "Failed to fetch trial balance")
                }
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
                onFailure = { e ->
                    _journalEntriesError.value = extractErrorMessage(e, "Failed to fetch journal entries")
                }
            )
            _isLoadingJournalEntries.value = false
        }
    }

    fun fetchLedger(accountId: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            _deleteJournalState.value = DeleteJournalState.Loading
            val result = financeRepository.deleteJournalEntry(id)
            result.fold(
                onSuccess = { message ->
                    _deleteJournalState.value = DeleteJournalState.Success(message)
                    fetchJournalEntries()   // refresh list after delete
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
        viewModelScope.launch {
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

//   NEW
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