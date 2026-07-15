package com.cuso.mobile.repository


import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.CreateChartOfAccountRequest
import com.cuso.mobile.model.finance.CreateChartOfAccountResponse
import com.cuso.mobile.model.finance.CreateExpenseResponse
import com.cuso.mobile.model.finance.ExpenseItem
import com.cuso.mobile.model.finance.ExpenseListResponse
import com.cuso.mobile.model.finance.InvoiceListResponse
import com.cuso.mobile.model.finance.InvoiceViewOneData
import com.cuso.mobile.model.sales.CustomerDetailV2
import com.cuso.mobile.model.sales.CustomerListResponseV2
import com.cuso.mobile.model.sales.GetFinanceCustomerViewOneResponse
import com.cuso.mobile.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
@Suppress("UNUSED_PARAMETER")
/**
 * FinanceRepository - Handles all finance and customer-related API calls
 * This is separate from SalesRepository to avoid conflicts and keep code organized
 */
@Singleton
class FinanceRepository @Inject constructor(
    private val api: ApiService,
    private val tokensDao: com.cuso.mobile.database.dao.TokensDao
) {

    private suspend fun getAuthHeaders(): Pair<String, String> {
        val tokens = tokensDao.getTokens()
            ?: throw Exception("No tokens found, please login again")
        return Pair("Bearer ${tokens.accessToken}", tokens.csrfToken)
    }

    // ═══════════════════════════════════════════════════════════
    // Customer V2 API Methods
    // ═══════════════════════════════════════════════════════════

    /**
     * Get customers for Finance > All Customers card view (V2 API)
     * GET /api/customers
     */
    suspend fun getCustomerForFinance(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        type: String? = null
    ): Result<CustomerListResponseV2> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getCustomerForFinance(
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

    /**
     * Get customer details by ID (V2 API)
     * GET /api/customers/{id}
     *
     * @param id Customer ID
     * @return Result containing CustomerDetailV2 or error
     */
    suspend fun getCustomerDetailV2(id: String): Result<CustomerDetailV2> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getCustomerDetailV2(accessToken, csrfToken, id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch customer details: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get customer detail for Finance > View One (V2 API)
     * GET /api/customers/{id}
     */
    suspend fun getFinanceCustomerViewOne(id: String): Result<GetFinanceCustomerViewOneResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getFinanceCustomerViewOne(accessToken, csrfToken, id)
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

    /**
     * Get invoices for Finance > Sales Invoice card view
     * GET /api/finance/invoices/view-all
     */
    suspend fun getInvoices(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<InvoiceListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getInvoices(
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

    // Add this function to FinanceRepository:

    /**
     * Get single invoice details by ID
     * GET /api/finance/invoices/{id}
     */
    suspend fun getInvoiceViewOne(id: String): Result<InvoiceViewOneData> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getInvoiceViewOne(
                token = accessToken,
                csrfToken = csrfToken,
                id = id
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "Failed to fetch invoice details: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Chart of Accounts ──
    suspend fun getChartOfAccounts(): Result<List<ChartOfAccountItem>> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getChartOfAccounts(accessToken, csrfToken)
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

    // ── Expenses: list ──
    suspend fun getExpenses(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<ExpenseListResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getExpenses(accessToken, csrfToken, page, limit, search, status)
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

    // ── Expenses: view one ──
    suspend fun getExpenseViewOne(id: String): Result<ExpenseItem> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.getExpenseViewOne(accessToken, csrfToken, id)
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

    // ── Expenses: create ──
    suspend fun createExpense(
        branch: String,
        expenseDate: String,
        accountId: String,
        paymentAccountId: String,
        amount: String,
        referenceNumber: String?,
        notes: String?,
        status: String?,
        fileParts: List<okhttp3.MultipartBody.Part> = emptyList()
    ): Result<CreateExpenseResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()

            fun String.asTextBody(): okhttp3.RequestBody =
                this.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.createExpense(
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

    // ── Chart of Accounts: create ──
    suspend fun createChartOfAccount(
        accountName: String,
        accountType: String
    ): Result<CreateChartOfAccountResponse> {
        return try {
            val (accessToken, csrfToken) = getAuthHeaders()
            val response = api.createChartOfAccount(
                token = accessToken,
                csrfToken = csrfToken,
                request = CreateChartOfAccountRequest(
                    accountName = accountName,
                    accountType = accountType
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


}

// ═══════════════════════════════════════════════════════════
// Result Wrapper for ViewModel
// ═══════════════════════════════════════════════════════════

sealed class FinanceApiResult<out T> {
    data class Success<T>(val data: T) : FinanceApiResult<T>()
    data class Error(val message: String) : FinanceApiResult<Nothing>()
    object Loading : FinanceApiResult<Nothing>()
}