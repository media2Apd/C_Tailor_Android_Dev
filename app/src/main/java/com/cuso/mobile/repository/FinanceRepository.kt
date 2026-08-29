package com.cuso.mobile.repository

import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.model.finance.AccountDropdownItem
import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.CreateChartOfAccountRequest
import com.cuso.mobile.model.finance.CreateChartOfAccountResponse
import com.cuso.mobile.model.finance.CreateExpenseResponse
import com.cuso.mobile.model.finance.CreateJournalEntryRequest
import com.cuso.mobile.model.finance.CreateJournalEntryResponse
import com.cuso.mobile.model.finance.ExpenseItem
import com.cuso.mobile.model.finance.ExpenseListResponse
import com.cuso.mobile.model.finance.InvoiceListResponse
import com.cuso.mobile.model.finance.InvoiceViewOneData
import com.cuso.mobile.model.finance.JournalEntryDetailData
import com.cuso.mobile.model.finance.JournalEntryLineRequest
import com.cuso.mobile.model.finance.JournalEntryListResponse
import com.cuso.mobile.model.finance.LedgerItem
import com.cuso.mobile.model.finance.TrialBalanceItem
import com.cuso.mobile.model.finance.UpdateJournalEntryRequest
import com.cuso.mobile.model.finance.UpdateJournalEntryResponse
import com.cuso.mobile.model.sales.CustomerListResponseV2
import com.cuso.mobile.model.sales.GetFinanceCustomerViewOneResponse
import com.cuso.mobile.network.finance.FinanceApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val financeApi: FinanceApiService,
    private val tokensDao: TokensDao
) {

    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // ═══════════════════════════════════════════════════════════
    // 1. Customer V2 API Methods
    // ═══════════════════════════════════════════════════════════

    suspend fun getCustomerForFinance(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        type: String? = null
    ): Result<CustomerListResponseV2> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getCustomerForFinance(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                type = type
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch customers: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFinanceCustomerViewOne(id: String): Result<GetFinanceCustomerViewOneResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getFinanceCustomerViewOne(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch customer detail: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 2. Invoices API Methods
    // ═══════════════════════════════════════════════════════════

    suspend fun getInvoices(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<InvoiceListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getInvoices(
                token = accessToken,
                csrfToken = csrfToken,
                page = page,
                limit = limit,
                search = search,
                status = status
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch invoices: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvoiceViewOne(id: String): Result<InvoiceViewOneData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getInvoiceViewOne(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch invoice details: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 3. Chart of Accounts API Methods
    // ═══════════════════════════════════════════════════════════

    suspend fun getChartOfAccounts(): Result<List<ChartOfAccountItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getChartOfAccounts(accessToken, csrfToken)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch chart of accounts: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChartOfAccountsDropdown(context: String = "parent_account"): Result<List<AccountDropdownItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getChartOfAccountsDropdown(accessToken, csrfToken, context)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch dropdown accounts: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createChartOfAccount(
        accountName: String,
        accountType: String,
        description: String? = null,
        parentAccount: String? = null
    ): Result<CreateChartOfAccountResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.createChartOfAccount(
                token = accessToken,
                csrfToken = csrfToken,
                request = CreateChartOfAccountRequest(
                    accountName = accountName,
                    accountType = accountType,
                    description = description,
                    parentAccount = parentAccount
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to create account: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChartOfAccount(
        id: String,
        accountName: String,
        accountType: String,
        description: String? = null,
        parentAccount: String? = null
    ): Result<CreateChartOfAccountResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.updateChartOfAccount(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = CreateChartOfAccountRequest(
                    accountName = accountName,
                    accountType = accountType,
                    description = description,
                    parentAccount = parentAccount
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update account: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChartOfAccount(id: String): Result<CreateChartOfAccountResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.deleteChartOfAccount(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to delete account: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 4. Expenses API Methods
    // ═══════════════════════════════════════════════════════════

    suspend fun getExpenses(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<ExpenseListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getExpenses(accessToken, csrfToken, page, limit, search, status)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch expenses: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpenseViewOne(id: String): Result<ExpenseItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getExpenseViewOne(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch expense: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createExpense(
        branch: String,
        expenseDate: String,
        accountId: String,
        paymentAccountId: String,
        amount: String,
        referenceNumber: String?,
        notes: String?,
        status: String?,
        fileParts: List<MultipartBody.Part> = emptyList()
    ): Result<CreateExpenseResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            fun String.asTextBody(): RequestBody =
                this.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = financeApi.createExpense(
                token = accessToken,
                csrfToken = csrfToken,
                branch = branch.asTextBody(),
                expenseDate = expenseDate.asTextBody(),
                accountId = accountId.asTextBody(),
                paymentAccountId = paymentAccountId.asTextBody(),
                amount = amount.asTextBody(),
                referenceNumber = referenceNumber?.asTextBody(),
                notes = notes?.asTextBody(),
                status = status?.asTextBody(),
                files = fileParts
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to create expense: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 5. Journal Entries API Methods
    // ═══════════════════════════════════════════════════════════

    suspend fun getJournalEntries(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<JournalEntryListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getJournalEntries(accessToken, csrfToken, page, limit, search, status)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch journal entries: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJournalEntryViewOne(id: String): Result<JournalEntryDetailData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getJournalEntryDetail(accessToken, csrfToken, id)
            val data = response.body()?.data
            if (response.isSuccessful && response.body()?.success == true && data != null) {
                Result.success(data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch journal entry: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createJournal(
        branchId: String,
        entryDate: String,
        reference: String?,
        notes: String?,
        status: String,
        lines: List<JournalEntryLineRequest>
    ): Result<CreateJournalEntryResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.createJournalEntry(
                token = accessToken,
                csrfToken = csrfToken,
                request = CreateJournalEntryRequest(
                    branchId = branchId,
                    entryDate = entryDate,
                    reference = reference,
                    notes = notes,
                    status = status,
                    lines = lines
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to create journal entry: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJournal(
        id: String,
        branchId: String,
        entryDate: String,
        reference: String?,
        notes: String?,
        status: String,
        lines: List<JournalEntryLineRequest>
    ): Result<UpdateJournalEntryResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.updateJournalEntry(
                token = accessToken,
                csrfToken = csrfToken,
                id = id,
                request = UpdateJournalEntryRequest(
                    branchId = branchId,
                    entryDate = entryDate,
                    reference = reference,
                    notes = notes,
                    status = status,
                    lines = lines
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to update journal entry: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJournalEntry(id: String): Result<String> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.deleteJournalEntry(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Journal entry deleted successfully")
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to delete journal entry: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 6. Trial Balance & Ledger API Methods
    // ═══════════════════════════════════════════════════════════

    suspend fun getTrialBalance(): Result<List<TrialBalanceItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getTrialBalance(
                token = accessToken,
                csrfToken = csrfToken
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.list ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch trial balance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLedger(accountId: String): Result<List<LedgerItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = financeApi.getLedger(accessToken, csrfToken, accountId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch ledger: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}