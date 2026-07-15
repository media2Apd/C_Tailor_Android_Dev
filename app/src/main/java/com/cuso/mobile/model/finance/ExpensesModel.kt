package com.cuso.mobile.model.finance

import com.google.gson.JsonElement

data class ChartOfAccountsResponse(
    val success: Boolean,
    val data: List<ChartOfAccountItem>
)

data class ChartOfAccountItem(
    val _id: String,
    val organizationId: String,
    val accountName: String,
    val accountCode: String,
    val accountType: String,
    val category: String? = null,
    val parentAccount: ParentAccountRef? = null,   // nested — one level deep, safe
    val level: Int,
    val isGroup: Boolean,
    val isSystemAccount: Boolean,
    val openingBalance: Double,
    val normalBalance: String,
    val currency: String,
    val allowManualEntry: Boolean,
    val reconciliation: Boolean,
    val description: String? = null,
    val status: String,
    val createdBy: String,
    val isEditable: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

// ⚠️ Inside a ParentAccountRef, the backend sometimes sends ITS OWN
// parentAccount as a plain String id (e.g. "Furniture".parentAccount.parentAccount)
// instead of a full object. JsonElement absorbs either shape without crashing.
data class ParentAccountRef(
    val _id: String,
    val organizationId: String? = null,
    val accountName: String,
    val accountCode: String,
    val accountType: String,
    val category: String? = null,
    val parentAccount: JsonElement? = null,
    val level: Int,
    val isGroup: Boolean,
    val isSystemAccount: Boolean,
    val openingBalance: Double,
    val normalBalance: String,
    val currency: String,
    val allowManualEntry: Boolean,
    val reconciliation: Boolean,
    val description: String? = null,
    val status: String,
    val createdBy: String,
    val isEditable: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

// ═══════════════════════════════════════════════════
// Expenses
// ═══════════════════════════════════════════════════

data class ExpenseListResponse(
    val success: Boolean,
    val pagination: ExpensePagination,
    val data: List<ExpenseItem>
)

data class ExpensePagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class ExpenseAccountRef(
    val _id: String,
    val accountName: String,
    val accountCode: String
)

data class ExpenseFile(
    val url: String,
    val publicId: String,
    val uploadedAt: String,
    val _id: String
)

data class ExpenseItem(
    val _id: String,
    val organizationId: String,
    val branch: String,
    val expenseNumber: String,
    val expenseDate: String,
    val accountId: ExpenseAccountRef,
    val amount: Double,
    val paymentAccountId: ExpenseAccountRef,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val status: String,
    val files: List<ExpenseFile> = emptyList(),
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

// Single-expense detail response (view-one) — reuses ExpenseItem
data class ExpenseViewOneResponse(
    val success: Boolean,
    val data: ExpenseItem
)

// AFTER
// Create expense response — accountId / paymentAccountId come back as plain
// string IDs here (NOT populated objects like in list / view-one responses)
data class CreateExpenseData(
    val _id: String,
    val organizationId: String,
    val branch: String,
    val expenseNumber: String,
    val expenseDate: String,
    val accountId: String,
    val amount: Double,
    val paymentAccountId: String,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val status: String,
    val files: List<ExpenseFile> = emptyList(),
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class CreateExpenseResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CreateExpenseData? = null
)

data class CreateChartOfAccountRequest(
    val accountName: String,
    val accountType: String,       // "Expense", "Asset", etc.
    val isGroup: Boolean = false,
    val allowManualEntry: Boolean = true
)

data class CreateChartOfAccountResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ChartOfAccountItem? = null
)