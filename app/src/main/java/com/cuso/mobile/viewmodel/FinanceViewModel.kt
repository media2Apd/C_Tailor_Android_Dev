package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.ExpenseItem
import com.cuso.mobile.model.finance.ExpensePagination
import com.cuso.mobile.model.finance.InvoiceItem
import com.cuso.mobile.model.finance.InvoiceViewOneData
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
        accountType: String
    ) {
        viewModelScope.launch {
            _createAccountState.value = CreateAccountState.Loading

            val result = financeRepository.createChartOfAccount(accountName, accountType)

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