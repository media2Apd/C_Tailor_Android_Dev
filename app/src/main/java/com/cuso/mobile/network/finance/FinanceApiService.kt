package com.cuso.mobile.network.finance

import com.cuso.mobile.model.finance.*
import com.cuso.mobile.model.sales.CustomerListResponseV2
import com.cuso.mobile.model.sales.GetFinanceCustomerViewOneResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface FinanceApiService {
    @GET("/api/finance/customers/view-all")
    suspend fun getCustomerForFinance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<CustomerListResponseV2>

    @GET("/api/finance/customers/view-overview/{id}")
    suspend fun getFinanceCustomerViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<GetFinanceCustomerViewOneResponse>

    @GET("/api/finance/sales-invoices/view-all")
    suspend fun getInvoices(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<InvoiceListResponse>

    @GET("/api/finance/sales-invoices/view-one/{id}")
    suspend fun getInvoiceViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<InvoiceViewOneResponse>

    @GET("/api/finance/chart-of-accounts/view-all")
    suspend fun getChartOfAccounts(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<ChartOfAccountsResponse>
    @GET("/api/finance/chart-of-accounts/dropdown")
    suspend fun getChartOfAccountsDropdown(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("context") context: String = "parent_account"
    ): Response<AccountDropdownResponse>

    @POST("/api/finance/chart-of-accounts/create")
    suspend fun createChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateChartOfAccountRequest
    ): Response<CreateChartOfAccountResponse>

    @PUT("/api/finance/chart-of-accounts/update-one/{id}")
    suspend fun updateChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: CreateChartOfAccountRequest
    ): Response<CreateChartOfAccountResponse>

    @DELETE("/api/finance/chart-of-accounts/delete-one/{id}")
    suspend fun deleteChartOfAccount(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<CreateChartOfAccountResponse>

    @GET("api/reports/trial-balance")
    suspend fun getTrialBalance(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String
    ): Response<TrialBalanceResponse>

    @GET("/api/finance/ledger/account/{accountId}")
    suspend fun getLedger(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("accountId") accountId: String
    ): Response<LedgerResponse>

    @GET("/api/finance/journal-entries/view-all")
    suspend fun getJournalEntries(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<JournalEntryListResponse>

    @POST("/api/finance/journal-entries/create")
    suspend fun createJournalEntry(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Body request: CreateJournalEntryRequest
    ): Response<CreateJournalEntryResponse>

    @GET("api/finance/journal-entries/view-one/{id}")
    suspend fun getJournalEntryDetail(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<JournalEntryDetailResponse>

    @PUT("/api/finance/journal-entries/update-one/{id}")
    suspend fun updateJournalEntry(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String,
        @Body request: UpdateJournalEntryRequest
    ): Response<UpdateJournalEntryResponse>

    @DELETE("api/finance/journal-entries/delete-one/{id}")
    suspend fun deleteJournalEntry(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<CreateJournalEntryResponse>

    @GET("/api/finance/expenses/view-all")
    suspend fun getExpenses(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<ExpenseListResponse>

    @GET("api/finance/expenses/{id}")
    suspend fun getExpenseViewOne(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: String
    ): Response<ExpenseViewOneResponse>

    @Multipart
    @POST("/api/finance/expenses/create")
    suspend fun createExpense(
        @Header("Authorization") token: String,
        @Header("X-CSRF-Token") csrfToken: String,
        @Part("branch") branch: RequestBody,
        @Part("expenseDate") expenseDate: RequestBody,
        @Part("accountId") accountId: RequestBody,
        @Part("paymentAccountId") paymentAccountId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("referenceNumber") referenceNumber: RequestBody?,
        @Part("notes") notes: RequestBody?,
        @Part("status") status: RequestBody?,
        @Part files: List<MultipartBody.Part> = emptyList()
    ): Response<CreateExpenseResponse>
}