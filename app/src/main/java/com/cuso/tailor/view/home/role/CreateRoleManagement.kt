@file:Suppress("UNUSED_PARAMETER", "unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.role

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.HrViewModel

// ── Models & State Holders ──

data class ScopeOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val description: String
)

data class FieldPermissionGroup(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val fields: List<FieldItem>
)

data class FieldItem(
    val key: String,
    val label: String
)

data class MatrixSubmodule(
    val key: String,
    val label: String,
    val section: String? = null,
    val allowedActions: Set<String>
)

data class MatrixModule(
    val key: String,
    val label: String,
    val submodules: List<MatrixSubmodule>
)

// Standard Action Sets matching the UI
private val VIEW_ONLY = setOf("view")
private val VIEW_EDIT = setOf("view", "edit")
private val VIEW_EDIT_EXPORT = setOf("view", "edit", "export")
private val CRUD = setOf("view", "create", "edit", "delete")
private val CRUDX = setOf("view", "create", "edit", "delete", "export")
private val VIEW_EXPORT = setOf("view", "export")
private val VIEW_PRINT_EXPORT = setOf("view", "export", "print")
private val VIEW_APPROVE = setOf("view", "approve")
private val REPORT_ACTIONS = setOf("view", "export", "print", "share")
private val LEGAL_ACTIONS = setOf("view", "create", "edit", "delete", "export", "print", "share")

// ── Exact Screenshot Matrix Structure (Total 942 Perms) with matching API keys ──
val SCREENSHOT_PERMISSIONS_CATALOG: List<MatrixModule> = listOf(
    MatrixModule(
        key = "home",
        label = "Home",
        submodules = listOf(
            MatrixSubmodule("organizationProfile", "Organization Profile", allowedActions = VIEW_EDIT),
            MatrixSubmodule("branches", "Branch Management", allowedActions = CRUD),
            MatrixSubmodule("departments", "Department & Teams", allowedActions = CRUD),
            MatrixSubmodule("designations", "Designation", allowedActions = CRUD),
            MatrixSubmodule("pricing", "Pricing", allowedActions = VIEW_EDIT),
            MatrixSubmodule("billing", "Billing", allowedActions = VIEW_EDIT_EXPORT),
            MatrixSubmodule("warehouse", "Warehouse", allowedActions = CRUD),
            MatrixSubmodule("customization", "Customization", allowedActions = VIEW_EDIT),
            MatrixSubmodule("widgetLibrary", "Widget Library", allowedActions = VIEW_EDIT),
            MatrixSubmodule("approvalRules", "Approval Rules", allowedActions = CRUD),
            MatrixSubmodule("approvalInbox", "Approval Inbox", allowedActions = CRUD)
        )
    ),
    MatrixModule(
        key = "sales",
        label = "Sales",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("leadManagement", "Lead Management", allowedActions = CRUDX),
            MatrixSubmodule("opportunities", "Opportunities", allowedActions = CRUDX),
            MatrixSubmodule("orders", "Order Management", allowedActions = CRUDX),
            MatrixSubmodule("customers", "Customers", allowedActions = CRUDX),
            MatrixSubmodule("measurements", "Measurements", allowedActions = CRUDX),
            MatrixSubmodule("pricingQuotes", "Pricing & Quotes", allowedActions = CRUDX),
            MatrixSubmodule("billingAndPayments", "Billing & Payments", allowedActions = CRUDX),
            MatrixSubmodule("garmentMeasurement", "Garment Measurement", allowedActions = CRUDX),
            MatrixSubmodule("pricingGarment", "Garment Pricing", section = "Pricing Setup", allowedActions = CRUD),
            MatrixSubmodule("pricingFabric", "Fabric Pricing", section = "Pricing Setup", allowedActions = CRUD),
            MatrixSubmodule("pricingWork", "Work Pricing", section = "Pricing Setup", allowedActions = CRUD),
            MatrixSubmodule("designSetup", "Design Setup", section = "Settings", allowedActions = CRUD),
            MatrixSubmodule("measurementList", "Measurement List", section = "Settings", allowedActions = CRUD)
        )
    ),
    MatrixModule(
        key = "marketing",
        label = "Marketing",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("website", "Website", allowedActions = CRUD),
            MatrixSubmodule("campaigns", "Campaigns", allowedActions = CRUDX),
            MatrixSubmodule("leadsAudience", "Leads & Audience", allowedActions = CRUDX),
            MatrixSubmodule("engagement", "Engagement", allowedActions = CRUD),
            MatrixSubmodule("growth", "Growth", allowedActions = CRUD),
            MatrixSubmodule("pages", "Pages", allowedActions = CRUD),
            MatrixSubmodule("budget", "Budget", allowedActions = CRUDX),
            MatrixSubmodule("team", "Team", allowedActions = CRUD)
        )
    ),
    MatrixModule(
        key = "finance",
        label = "Finance",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("finCustomers", "Customers Directory", section = "Accounts Receivable (Sales)", allowedActions = VIEW_EXPORT),
            MatrixSubmodule("finOrders", "Sales Orders", section = "Accounts Receivable (Sales)", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("finInvoices", "Invoices", section = "Accounts Receivable (Sales)", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("finReceipts", "Payment Receipts", section = "Accounts Receivable (Sales)", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("finCustomerCredits", "Customer Credits & Refunds", section = "Accounts Receivable (Sales)", allowedActions = CRUDX),
            MatrixSubmodule("finSuppliers", "Suppliers Directory", section = "Accounts Payable (Purchase)", allowedActions = VIEW_EXPORT),
            MatrixSubmodule("finPurchaseOrders", "Purchase Orders", section = "Accounts Payable (Purchase)", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("finPurchaseReceives", "Purchase Receives", section = "Accounts Payable (Purchase)", allowedActions = VIEW_EXPORT),
            MatrixSubmodule("finPurchaseBills", "Purchase Bills", section = "Accounts Payable (Purchase)", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("finPaymentsMade", "Payments Made", section = "Accounts Payable (Purchase)", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("finPurchaseReturns", "Purchase Returns", section = "Accounts Payable (Purchase)", allowedActions = CRUDX),
            MatrixSubmodule("finSupplierCredits", "Supplier Credits & Debit Notes", section = "Accounts Payable (Purchase)", allowedActions = CRUDX),
            MatrixSubmodule("finChartOfAccounts", "Chart of Accounts", section = "General Ledger", allowedActions = CRUD),
            MatrixSubmodule("finJournalEntries", "Journal Entries", section = "General Ledger", allowedActions = CRUDX),
            MatrixSubmodule("expenses", "Expense Management", allowedActions = CRUDX),
            MatrixSubmodule("finBankAccounts", "Bank Accounts", section = "Funds & Banking", allowedActions = CRUD),
            MatrixSubmodule("finCashBooks", "Cash Books", section = "Funds & Banking", allowedActions = CRUD),
            MatrixSubmodule("finBankStatements", "Bank Statements & Reconcile", section = "Funds & Banking", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("finTaxRegistration", "Tax Registration Master", section = "Taxes & Compliance", allowedActions = CRUD),
            MatrixSubmodule("finTaxRatesRules", "Tax Rates & Calculation Rules", section = "Taxes & Compliance", allowedActions = CRUD),
            MatrixSubmodule("payrollAccountMapping", "Payroll Account Mapping", allowedActions = CRUD),
            MatrixSubmodule("payrollAccrual", "Payroll Accrual", allowedActions = CRUD),
            MatrixSubmodule("payrollDisbursement", "Salary Disbursement", allowedActions = CRUD)
        )
    ),
    MatrixModule(
        key = "inventory",
        label = "Inventory",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("items", "All Items", allowedActions = CRUDX),
            MatrixSubmodule("itemGroups", "Item Groups", allowedActions = CRUDX),
            MatrixSubmodule("bulk", "Bulk Operations", allowedActions = CRUDX),
            MatrixSubmodule("pricing", "Pricing & Price Lists", allowedActions = CRUD),
            MatrixSubmodule("adjustment", "Adjustment", allowedActions = CRUD),
            MatrixSubmodule("transferOrders", "Transfer Orders", allowedActions = CRUD),
            MatrixSubmodule("procSuppliers", "Suppliers", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procRequisitions", "Purchase Request", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procPurchaseOrders", "Purchase Order", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procPurchaseReceives", "Purchase Receive", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procBills", "Bill List", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procPaymentsMade", "Payments Made", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procPurchaseReturn", "Purchase Return", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procVendorCredits", "Vendor Credits", section = "Procurement", allowedActions = CRUDX),
            MatrixSubmodule("procBarcodes", "Barcode List", section = "Procurement", allowedActions = setOf("view", "create", "edit", "delete", "export", "print")),
            MatrixSubmodule("locations", "Location Management", allowedActions = CRUD),
            MatrixSubmodule("custom", "Customers", section = "Sales", allowedActions = VIEW_EXPORT),
            MatrixSubmodule("ord", "Orders", section = "Sales", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("inv", "Invoice", section = "Sales", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("rec", "Receipts", section = "Sales", allowedActions = VIEW_PRINT_EXPORT),
            MatrixSubmodule("lowStockAlert", "Low Stock Alert", allowedActions = VIEW_EXPORT),
            MatrixSubmodule("autoReorder", "Auto Reorder", allowedActions = CRUD),
            MatrixSubmodule("safetyStock", "Safety Stock", allowedActions = CRUD),
            MatrixSubmodule("picklist", "Picklist", section = "Inventory Out", allowedActions = CRUDX),
            MatrixSubmodule("packages", "Packages", section = "Inventory Out", allowedActions = CRUDX),
            MatrixSubmodule("category", "Category Listing", section = "Multichannel", allowedActions = CRUDX),
            MatrixSubmodule("approvals", "Approvals", allowedActions = VIEW_APPROVE),
            MatrixSubmodule("warehouseStructure", "Warehouse Structure", allowedActions = CRUD),
            MatrixSubmodule("autoReorderSettings", "Auto Reorder Settings", allowedActions = VIEW_EDIT),
            MatrixSubmodule("pdftemp", "PDF Templates", section = "Customization", allowedActions = VIEW_EDIT)
        )
    ),
    MatrixModule(
        key = "logistics",
        label = "Logistics",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("orderList", "Order List", allowedActions = CRUDX),
            MatrixSubmodule("courierPartners", "Courier Partner List", allowedActions = CRUDX),
            MatrixSubmodule("shipments", "Shipments", allowedActions = CRUDX),
            MatrixSubmodule("courierManagement", "Courier Management", allowedActions = CRUDX),
            MatrixSubmodule("trackingEvents", "Tracking Events", allowedActions = CRUDX),
            MatrixSubmodule("codOrders", "COD Orders Dashboard", allowedActions = CRUDX),
            MatrixSubmodule("delivery", "Delivery Management", allowedActions = CRUDX),
            MatrixSubmodule("returns", "Returns Management", allowedActions = CRUDX)
        )
    ),
    MatrixModule(
        key = "services",
        label = "Services",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("productionOrders", "Production Orders", section = "Service & Orders", allowedActions = CRUDX),
            MatrixSubmodule("productionWorks", "Production Works", section = "Service & Orders", allowedActions = CRUDX),
            MatrixSubmodule("serviceRequests", "Service Request", allowedActions = CRUD),
            MatrixSubmodule("alterations", "Alteration Management", allowedActions = CRUD),
            MatrixSubmodule("customerFeedback", "Customer Feedback", allowedActions = CRUDX),
            MatrixSubmodule("customerComplaints", "Customer Complaints", allowedActions = CRUDX),
            MatrixSubmodule("serviceTemplates", "Service Templates", allowedActions = CRUD),
            MatrixSubmodule("workflowStages", "Workflow Stages", allowedActions = CRUD),
            MatrixSubmodule("returns", "Return", allowedActions = CRUD),
            MatrixSubmodule("damagedGoods", "Damaged Goods", allowedActions = CRUD)
        )
    ),
    MatrixModule(
        key = "hr",
        label = "HR",
        submodules = listOf(
            MatrixSubmodule("hrDashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("employeeManagement", "Employee Management", allowedActions = setOf("view", "create")),
            MatrixSubmodule("allEmployees", "All Employees", section = "Organization Structure", allowedActions = VIEW_ONLY),
            MatrixSubmodule("productionTeam", "Production Team", section = "Organization Structure", allowedActions = CRUDX),
            MatrixSubmodule("dailyAttendance", "Daily Attendance", section = "Attendance Management", allowedActions = setOf("view", "create", "approve")),
            MatrixSubmodule("monthlyAttendance", "Monthly Attendance", section = "Attendance Management", allowedActions = VIEW_EXPORT),
            MatrixSubmodule("manualEntry", "Manual Entry", section = "Attendance Management", allowedActions = setOf("view", "create")),
            MatrixSubmodule("shiftManagement", "Shift Management", section = "Attendance Management", allowedActions = CRUD),
            MatrixSubmodule("leaveDashboard", "Leave Dashboard", section = "Leave Management", allowedActions = VIEW_ONLY),
            MatrixSubmodule("leaveBalance", "Leave Balance", section = "Leave Management", allowedActions = setOf("view", "delete")),
            MatrixSubmodule("leaveApproval", "Leave Approval", section = "Leave Management", allowedActions = setOf("view", "approve", "reject")),
            MatrixSubmodule("leave", "Leave Management", section = "Leave Management", allowedActions = CRUDX + "approve"),
            MatrixSubmodule("salaryStructureMaster", "Salary Structure Master", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("employeeSalaryAssignment", "Employee Salary Assignment", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("payrollProcessing", "Payroll Processing", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("payslipManagement", "Payslip Management", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("reimbursementAndBonus", "Reimbursements & Bonus", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("statutoryCompliance", "Statutory Compliance", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("loansAdvances", "Loans & Advances", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("payroll", "Payroll Management Core", section = "Payroll Management", allowedActions = CRUDX),
            MatrixSubmodule("approvals", "Approvals", section = "Expense Management", allowedActions = VIEW_APPROVE),
            MatrixSubmodule("newClaims", "New Claims", section = "Expense Management", allowedActions = CRUD),
            MatrixSubmodule("expenseType", "Expense Type", section = "Expense Management", allowedActions = CRUD),
            MatrixSubmodule("financeReimbursement", "Finance Reimbursement", section = "Expense Management", allowedActions = setOf("view", "create")),
            MatrixSubmodule("expenses", "Expense Management Core", section = "Expense Management", allowedActions = CRUDX),
            MatrixSubmodule("trainingProgram", "Training Programs", section = "Training Management", allowedActions = CRUD),
            MatrixSubmodule("assignmentManagement", "Assign Management", section = "Training Management", allowedActions = CRUD),
            MatrixSubmodule("training", "Training Management Core", section = "Training Management", allowedActions = CRUD),
            MatrixSubmodule("employeeDocuments", "Employee Documents", section = "Document Management", allowedActions = CRUD),
            MatrixSubmodule("documentCategories", "Document Categories", section = "Document Management", allowedActions = CRUD),
            MatrixSubmodule("documents", "Document Management Core", section = "Document Management", allowedActions = CRUDX),
            MatrixSubmodule("allRequests", "All Requests", section = "Exit Management", allowedActions = CRUD + "approve"),
            MatrixSubmodule("exit", "Exit Management Core", section = "Exit Management", allowedActions = CRUD + "approve"),
            MatrixSubmodule("leaveTypes", "Leave Types", section = "HR Settings", allowedActions = CRUD),
            MatrixSubmodule("leavePolicies", "Leave Policies", section = "HR Settings", allowedActions = CRUDX),
            MatrixSubmodule("pieceRate", "Piece Rate Setup", section = "HR Settings", allowedActions = CRUDX)
        )
    ),
    MatrixModule(
        key = "it",
        label = "IT",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("deviceManagement", "Device Management", allowedActions = CRUDX),
            MatrixSubmodule("posAndBillingTechnology", "POS & Billing Technology", allowedActions = CRUDX),
            MatrixSubmodule("integrations", "Integrations", allowedActions = CRUDX),
            MatrixSubmodule("backupAndRecovery", "Backup & Recovery", allowedActions = CRUDX),
            MatrixSubmodule("itAssetManagement", "IT Asset Management", allowedActions = CRUDX),
            MatrixSubmodule("itServiceDesk", "IT Service Desk", allowedActions = CRUDX)
        )
    ),
    MatrixModule(
        key = "legal",
        label = "Legal",
        submodules = listOf(
            MatrixSubmodule("contracts", "Contracts & Agreements", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("compliance", "Compliance Management", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("documents", "Legal Documents", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("licenses", "License & Registrations", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("customerLegal", "Customer Legal", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("employeeLegal", "Employee & Labour Legal", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("vendorLegal", "Vendor & Supplier Legal", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("notices", "Legal Notices & Dispute", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("auditCalendar", "Audit & Legal Calendar", allowedActions = LEGAL_ACTIONS),
            MatrixSubmodule("approvals", "Approvals", allowedActions = setOf("view", "approve", "export")),
            MatrixSubmodule("dashboard", "Dashboard", allowedActions = VIEW_ONLY)
        )
    ),
    MatrixModule(
        key = "security",
        label = "Security",
        submodules = listOf(
            MatrixSubmodule("dashboard", "Security Dashboard", allowedActions = VIEW_ONLY),
            MatrixSubmodule("users", "User Management", section = "User & Role", allowedActions = CRUDX),
            MatrixSubmodule("roles", "Role Management", section = "User & Role", allowedActions = CRUD),
            MatrixSubmodule("dataBackup", "Data Backup", section = "Backup & Recovery", allowedActions = CRUDX),
            MatrixSubmodule("recovery", "Recovery", section = "Backup & Recovery", allowedActions = CRUDX),
            MatrixSubmodule("apiSecurity", "API Integration", section = "API Integration", allowedActions = CRUD),
            MatrixSubmodule("apiLogs", "API Logs", section = "API Integration", allowedActions = VIEW_EXPORT),
            MatrixSubmodule("mobileSecurity", "Mobile Device", allowedActions = CRUD),
            MatrixSubmodule("helpCenter", "Help Center", section = "Support", allowedActions = CRUD),
            MatrixSubmodule("supportTicket", "Support Ticket", section = "Support", allowedActions = CRUD),
            MatrixSubmodule("auditLogs", "Audit Logs", allowedActions = CRUD)
        )
    ),
    MatrixModule(
        key = "reports",
        label = "Reports",
        submodules = listOf(
            MatrixSubmodule("leadMasterRegister", "Lead · Master Register", section = "Lead Management", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("leadSourcePerformance", "Lead · Source Performance & ROI", section = "Lead Management", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("leadOverdueFollowups", "Lead · Overdue Follow-ups Audit", section = "Lead Management", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("leadFollowupActivity", "Lead · Follow-Up Activity Register", section = "Lead Management", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("oppMasterRegister", "Opportunity · Master Register", section = "Opportunity Management", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("oppStageFunnel", "Opportunity · Stage Funnel & Forecast", section = "Opportunity Management", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("oppClosingAlerts", "Opportunity · Closing Soon & Overdue", section = "Opportunity Management", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("custMasterRegister", "Customer · Master Directory", section = "Customer & CRM Intelligence", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("custLtvRevenue", "Customer · Lifetime Value", section = "Customer & CRM Intelligence", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("custCreditReceivables", "Customer · Credit & Outstanding", section = "Customer & CRM Intelligence", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("custDormantWinback", "Customer · Dormant Win-Back", section = "Customer & CRM Intelligence", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("measMasterRegister", "Measurement · Master Register", section = "Garment Measurements & Sizing", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("measPopularity", "Measurement · Garment Popularity", section = "Garment Measurements & Sizing", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("measStaleAudit", "Measurement · Stale Audit", section = "Garment Measurements & Sizing", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("orderMasterRegister", "Order · Master Register", section = "Sales Orders & Fulfillment", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("orderProductStream", "Order · Sales by Product Stream", section = "Sales Orders & Fulfillment", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("orderDeliverySla", "Order · On-Time Delivery SLA", section = "Sales Orders & Fulfillment", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("branchWiseSales", "Order · Branch-wise Sales Comparison", section = "Sales Orders & Fulfillment", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("quoteMasterRegister", "Quotation · Master Register", section = "Quotation & Estimates", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("quoteDiscountLeakage", "Quotation · Discount Leakage", section = "Quotation & Estimates", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("quoteExpiringAudit", "Quotation · Expiring Quotations", section = "Quotation & Estimates", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("revenueExpenseSummary", "Revenue & Expense Summary", section = "Profit & Cash Flow", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("cashFlow", "Cash Flow Statement", section = "Profit & Cash Flow", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("receivablesAging", "Receivables Aging", section = "Aging", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("payablesAging", "Payables Aging", section = "Aging", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("gstSummary", "GST / Tax Summary", section = "Tax", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("paymentCollections", "Payment Collections", section = "Payments & Collections", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("advancePayments", "Advance Payments", section = "Payments & Collections", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("paymentModes", "Payment Modes", section = "Payments & Collections", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("balancePending", "Balance Pending", section = "Payments & Collections", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("overduePayments", "Overdue Payments", section = "Payments & Collections", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("customerFeedbackSummary", "Customer Feedback Summary", section = "Service Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("complaintsEscalation", "Complaints & Escalation", section = "Service Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("servicePerformanceSla", "Service Performance & SLA", section = "Service Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("legalContractsExpiry", "Contracts Expiry Register", section = "Legal Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("legalComplianceLicense", "Compliance & License Register", section = "Legal Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("legalCasesDisputes", "Legal Cases & Disputes", section = "Legal Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("secIncidentsAlerts", "Security Incidents & Alerts", section = "Security Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("secAccessActivity", "Access & Permission Activity", section = "Security Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("secApiIntegrations", "API Integration Security", section = "Security Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("invStockSummary", "Stock Summary", section = "Inventory Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("invLowStockReorder", "Low Stock & Reorder", section = "Inventory Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("invStockMovement", "Stock Movement", section = "Inventory Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("invMaterialConsumption", "Material Consumption", section = "Inventory Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("invValuation", "Inventory Valuation", section = "Inventory Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("logDeliveryPerformance", "Delivery Performance", section = "Logistics Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("logShipmentTracking", "Shipment Tracking", section = "Logistics Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("logReturnsRto", "Returns & RTO", section = "Logistics Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("logCodReconciliation", "COD Reconciliation", section = "Logistics Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("itAssetRegister", "IT Asset Register", section = "IT Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("itServiceDesk", "IT Service Desk", section = "IT Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("itIntegrationSync", "Integration Sync Health", section = "IT Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("itSystemBackup", "System Backup Health", section = "IT Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("mktCampaignPerformance", "Campaign Performance & ROI", section = "Marketing Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("mktChannelPerformance", "Marketing Channel Performance", section = "Marketing Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("hrAttendanceRegister", "Attendance Register & Analytics", section = "HR & Payroll Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("hrPayrollRuns", "Payroll Runs Audit", section = "HR & Payroll Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("hrPieceRates", "Piece Rate Master", section = "HR & Payroll Reports", allowedActions = REPORT_ACTIONS),
            MatrixSubmodule("hrReimbursements", "Reimbursement & Settlement", section = "HR & Payroll Reports", allowedActions = REPORT_ACTIONS)
        )
    )
)

private val MATRIX_COLUMNS = listOf(
    "view" to "VIEW",
    "create" to "CREATE",
    "edit" to "EDIT",
    "delete" to "DELETE",
    "approve" to "APPROVE",
    "export" to "EXPORT",
    "print" to "PRINT",
    "share" to "SHARE"
)

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CreateRoleScreen(
    roleIdToEdit: String? = null,
    viewModel: HrViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onRoleSaved: () -> Unit = onBack
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEdit = !roleIdToEdit.isNullOrBlank()

    // Observe loading and state from ViewModel
    val isRoleLoading by viewModel.isRoleLoading.collectAsState()
    val roleErrorMessage by viewModel.roleErrorMessage.collectAsState()
    val roleDetails by viewModel.roleDetails.collectAsState()

    // ── Form State ──
    var roleName by remember { mutableStateOf("") }
    var roleCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isActiveStatus by remember { mutableStateOf(true) }
    var isDefaultRole by remember { mutableStateOf(false) }
    var selectedScope by remember { mutableStateOf("branch") }
    var sessionTimeoutMinutes by remember { mutableIntStateOf(30) }
    var timeoutDropdownExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    val scopeOptions = remember {
        listOf(
            ScopeOption("branch", "Branch-level", Icons.Default.Apartment, "Access restricted to assigned specific branch data."),
            ScopeOption("regional", "Regional", Icons.Default.Map, "Access encompasses all branches within assigned regions."),
            ScopeOption("org-wide", "Org-wide", Icons.Default.Public, "Global access to all data across the organization.")
        )
    }

    val timeoutOptions = remember {
        listOf(15, 30, 60, 120, 240, 480).map { m ->
            if (m >= 60) "${m / 60} hour${if (m > 60) "s" else ""}" else "$m minutes"
        }
    }

    // ── Field Permissions State ──
    val fieldGroups = remember {
        listOf(
            FieldPermissionGroup(
                key = "sales",
                label = "Sales Fields",
                icon = Icons.Default.Storefront,
                fields = listOf(
                    FieldItem("customerName", "Customer Name"),
                    FieldItem("phone", "Phone"),
                    FieldItem("orderValue", "Order Value")
                )
            ),
            FieldPermissionGroup(
                key = "inventory",
                label = "Inventory Fields",
                icon = Icons.Default.Inventory2,
                fields = listOf(
                    FieldItem("stockQty", "Stock Quantity"),
                    FieldItem("costPrice", "Cost Price")
                )
            ),
            FieldPermissionGroup(
                key = "hr",
                label = "HR Fields",
                icon = Icons.Default.Badge,
                fields = listOf(
                    FieldItem("personalDetails", "Personal Details"),
                    FieldItem("salary", "Salary")
                )
            )
        )
    }

    var fieldPermissions by remember {
        mutableStateOf(
            mutableMapOf(
                "sales_customerName_view" to true,
                "sales_customerName_edit" to true,
                "sales_phone_view" to true,
                "sales_phone_edit" to true,
                "sales_orderValue_view" to true,
                "sales_orderValue_edit" to true,
                "inventory_stockQty_view" to true,
                "inventory_stockQty_edit" to true,
                "inventory_costPrice_view" to true,
                "inventory_costPrice_edit" to true,
                "hr_personalDetails_view" to true,
                "hr_personalDetails_edit" to true,
                "hr_salary_view" to true,
                "hr_salary_edit" to true
            )
        )
    }

    var expandedFieldGroup by remember { mutableStateOf<String?>("sales") }

    // ── Matrix Permissions State: "$moduleKey.$submoduleKey.$action" -> Boolean ──
    var matrixPermissions by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    var hiddenSubmodules by remember { mutableStateOf(setOf<String>()) }
    var expandedModules by remember {
        mutableStateOf(SCREENSHOT_PERMISSIONS_CATALOG.map { it.key }.toSet())
    }

    // Horizontal scroll state for permissions table
    val matrixScrollState = rememberScrollState()

    // 1. Initial Fetch if in Edit Mode
    LaunchedEffect(roleIdToEdit) {
        if (!roleIdToEdit.isNullOrBlank()) {
            viewModel.fetchRoleById(roleIdToEdit)
        }
    }

    // 2. Populate Edit Mode Data when role details arrive
    LaunchedEffect(roleDetails) {
        roleDetails?.let { role ->
            roleName = role.name
            roleCode = role.roleCode
            description = role.description.orEmpty()
            isActiveStatus = role.status
            isDefaultRole = role.isDefault
            selectedScope = role.accessScope ?: "branch"
            sessionTimeoutMinutes = role.sessionTimeoutMinutes ?: 30

            // Populate Field Permissions
            val newFieldMap = mutableMapOf<String, Boolean>()
            role.fieldPermissions?.forEach { (groupKey, fieldsMap) ->
                fieldsMap.forEach { (fieldKey, actionsMap) ->
                    actionsMap.forEach { (actionKey, isAllowed) ->
                        newFieldMap["${groupKey}_${fieldKey}_${actionKey}"] = isAllowed
                    }
                }
            }
            if (newFieldMap.isNotEmpty()) {
                fieldPermissions = newFieldMap
            }

            // Populate Matrix Permissions and extract "hidden": true states
            val newMatrixMap = mutableMapOf<String, Boolean>()
            val newHiddenSet = mutableSetOf<String>()

            role.permissions?.forEach { (moduleKey, submodulesMap) ->
                submodulesMap.forEach { (submoduleKey, actionsMap) ->
                    // 1. Check if the module level itself is hidden
                    if (submoduleKey == "hidden" && actionsMap is Boolean && actionsMap) {
                        newHiddenSet.add("${moduleKey}.module_root")
                    } else if (actionsMap is Map<*, *>) {
                        // 2. Check submodule actions and submodule hidden state
                        @Suppress("UNCHECKED_CAST")
                        val subActions = actionsMap as Map<String, Any>
                        subActions.forEach { (actionKey, isAllowed) ->
                            if (actionKey == "hidden" && isAllowed == true) {
                                newHiddenSet.add("$moduleKey.$submoduleKey")
                            } else if (isAllowed == true) {
                                newMatrixMap["$moduleKey.$submoduleKey.$actionKey"] = true
                            }
                        }
                    }
                }
            }

            if (newMatrixMap.isNotEmpty()) {
                matrixPermissions = newMatrixMap
            }
            hiddenSubmodules = newHiddenSet
        }
    }

    // 3. Error Handling Toast
    LaunchedEffect(roleErrorMessage) {
        roleErrorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearRoleError()
        }
    }

    // Counters
    val totalPerms = remember {
        SCREENSHOT_PERMISSIONS_CATALOG.sumOf { m -> m.submodules.sumOf { it.allowedActions.size } }
    }
    val selectedPerms = remember(matrixPermissions) {
        matrixPermissions.values.count { it }
    }
    val allMatrixSelected = selectedPerms > 0 && selectedPerms == totalPerms

    Scaffold(
        topBar = {
            TitleBar(
                title = if (isEdit) "Edit Role" else "Create New Role",
                onClose = onBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        enabled = !isRoleLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            var hasError = false
                            if (roleName.isBlank()) {
                                nameError = true
                                hasError = true
                            }
                            if (roleCode.isBlank()) {
                                codeError = true
                                hasError = true
                            }
                            if (!hasError) {
                                viewModel.saveRole(
                                    roleIdToEdit = roleIdToEdit,
                                    name = roleName,
                                    roleCode = roleCode,
                                    description = description,
                                    status = isActiveStatus,
                                    isDefault = isDefaultRole,
                                    accessScope = selectedScope,
                                    sessionTimeoutMinutes = sessionTimeoutMinutes,
                                    matrixPermissions = matrixPermissions,
                                    hiddenSubmodules = hiddenSubmodules,
                                    fieldPermissions = fieldPermissions,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            if (isEdit) "Role updated successfully" else "Role created successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onRoleSaved()
                                    }
                                )
                            }
                        },
                        enabled = !isRoleLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        if (isRoleLoading) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = if (isEdit) "Save Changes" else "Create Role",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        },
        containerColor = Primary_background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.5f)
        ) {
            // ── Section 1: Role Information ──
            item {
                SectionContainerCard(title = "Role Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column {
                            FormLabel("Role Name", isRequired = true)
                            Spacer(Modifier.height(4.dp))
                            FormTextField(
                                value = roleName,
                                onValueChange = {
                                    roleName = it
                                    nameError = false
                                    if (!isEdit && roleCode.isBlank()) {
                                        roleCode = it.trim().uppercase().replace("[^A-Z0-9]+".toRegex(), "_")
                                    }
                                },
                                placeholder = "e.g. Master Tailor",
                                isError = nameError,
                                errorMessage = "Role name is required"
                            )
                        }

                        Column {
                            FormLabel("Role Code", isRequired = true)
                            Spacer(Modifier.height(4.dp))
                            FormTextField(
                                value = roleCode,
                                onValueChange = {
                                    roleCode = it.uppercase()
                                    codeError = false
                                },
                                placeholder = "MASTER_TAILOR",
                                isError = codeError,
                                errorMessage = "Role code is required"
                            )
                        }

                        Column {
                            FormLabel("Description")
                            Spacer(Modifier.height(4.dp))
                            FormTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = "Provides access to tailoring operations and branch-level activities"
                            )
                        }

                        Column {
                            FormLabel("Status")
                            Spacer(Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 2),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { isActiveStatus = true }
                                ) {
                                    RadioButton(
                                        selected = isActiveStatus,
                                        onClick = { isActiveStatus = true },
                                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Active", fontSize = tokens.bodySmall, color = title_color)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { isActiveStatus = false }
                                ) {
                                    RadioButton(
                                        selected = !isActiveStatus,
                                        onClick = { isActiveStatus = false },
                                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Inactive", fontSize = tokens.bodySmall, color = title_color)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Default role", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                                Text("Replaces the current default role for new staff.", fontSize = tokens.caption, color = mutedText)
                            }
                            MiniSwitch(
                                checked = isDefaultRole,
                                onCheckedChange = { isDefaultRole = it }
                            )
                        }
                    }
                }
            }

            // ── Section 2: Access Scope ──
            item {
                SectionContainerCard(
                    title = "Access Scope",
                    subtitle = "Determine the organizational boundary for this role."
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        scopeOptions.forEach { opt ->
                            val isSelected = selectedScope == opt.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                    .clickable { selectedScope = opt.id },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                color = if (isSelected) primary_light else whiteBg,
                                border = BorderStroke(1.5.dp, if (isSelected) Primary else sectionBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(tokens.extraPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.buttonHeight * 0.85f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) whiteBg else light_grey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = opt.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Primary else mutedText,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    }

                                    Spacer(Modifier.width(tokens.extraPadding))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = opt.label,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Primary else title_color
                                        )
                                        Text(
                                            text = opt.description,
                                            fontSize = tokens.caption,
                                            color = TextSecondary
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedScope = opt.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Column {
                            FormLabel("Idle Session Timeout")
                            Spacer(Modifier.height(4.dp))
                            FormDropdown(
                                value = if (sessionTimeoutMinutes >= 60) "${sessionTimeoutMinutes / 60} hours" else "$sessionTimeoutMinutes minutes",
                                expanded = timeoutDropdownExpanded,
                                onExpandChange = { timeoutDropdownExpanded = it },
                                options = timeoutOptions,
                                onOptionSelected = { selected ->
                                    sessionTimeoutMinutes = if (selected.contains("hour")) {
                                        selected.filter { it.isDigit() }.toInt() * 60
                                    } else {
                                        selected.filter { it.isDigit() }.toInt()
                                    }
                                    timeoutDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Section 3: Field Permissions ──
            item {
                SectionContainerCard(
                    title = "Field Permissions",
                    trailingBadge = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary_light)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Active Rules", fontSize = tokens.label, color = Primary, fontWeight = FontWeight.Medium)
                        }
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        fieldGroups.forEach { group ->
                            val isOpen = expandedFieldGroup == group.key

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                border = BorderStroke(1.dp, sectionBorder),
                                color = whiteBg
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedFieldGroup = if (isOpen) null else group.key
                                            }
                                            .padding(horizontal = tokens.extraPadding, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(group.icon, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                                            Spacer(Modifier.width(8.dp))
                                            Text(group.label, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                                        }
                                        Icon(
                                            imageVector = if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = mutedText,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    }

                                    if (isOpen) {
                                        HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                                        Column(
                                            modifier = Modifier.padding(tokens.extraPadding),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            group.fields.forEach { field ->
                                                val viewKey = "${group.key}_${field.key}_view"
                                                val editKey = "${group.key}_${field.key}_edit"

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(field.label, fontSize = tokens.caption, color = title_color)

                                                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            AppCheckbox(
                                                                checked = fieldPermissions[viewKey] == true,
                                                                onCheckedChange = { checked ->
                                                                    fieldPermissions = fieldPermissions.toMutableMap().apply {
                                                                        put(viewKey, checked)
                                                                        if (!checked) put(editKey, false)
                                                                    }
                                                                }
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("View", fontSize = tokens.label, color = TextSecondary)
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            AppCheckbox(
                                                                checked = fieldPermissions[editKey] == true,
                                                                onCheckedChange = { checked ->
                                                                    fieldPermissions = fieldPermissions.toMutableMap().apply {
                                                                        put(editKey, checked)
                                                                        if (checked) put(viewKey, true)
                                                                    }
                                                                }
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("Edit", fontSize = tokens.label, color = TextSecondary)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 4: Module & Submodule Permissions Header ──
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = tokens.cardCornerRadius, topEnd = tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(vertical = tokens.extraPadding * 1.2f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Module & Submodule Permissions",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "$selectedPerms of $totalPerms permissions selected",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (allMatrixSelected) "Clear All Matrix" else "Select All Matrix",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                                modifier = Modifier.clickable {
                                    val target = !allMatrixSelected
                                    matrixPermissions = matrixPermissions.toMutableMap().apply {
                                        SCREENSHOT_PERMISSIONS_CATALOG.forEach { mod ->
                                            mod.submodules.forEach { sm ->
                                                sm.allowedActions.forEach { act ->
                                                    put("${mod.key}.${sm.key}.$act", target)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                            val allCollapsed = expandedModules.isEmpty()
                            Text(
                                text = if (allCollapsed) "Expand all" else "Collapse all",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                                modifier = Modifier.clickable {
                                    expandedModules = if (allCollapsed) {
                                        SCREENSHOT_PERMISSIONS_CATALOG.map { it.key }.toSet()
                                    } else {
                                        emptySet()
                                    }
                                }
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Column headers inside horizontal scrollable container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(matrixScrollState)
                        ) {
                            Row(
                                modifier = Modifier
                                    .width(960.dp)
                                    .background(Primary_background.copy(alpha = 0.5f))
                                    .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MODULE / SUBMODULE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.width(360.dp)
                                )

                                MATRIX_COLUMNS.forEach { (_, title) ->
                                    Box(
                                        modifier = Modifier.width(72.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 5: Granular Lazy Loaded Module & Submodule Rows (Lag-free) ──
            SCREENSHOT_PERMISSIONS_CATALOG.forEach { module ->
                val isExpanded = expandedModules.contains(module.key)

                // Module Header Item
                item(key = "module_${module.key}") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .horizontalScroll(matrixScrollState)
                    ) {
                        Column(modifier = Modifier.width(960.dp)) {
                            val modSelectedCount = module.submodules.sumOf { sm ->
                                sm.allowedActions.count { matrixPermissions["${module.key}.${sm.key}.$it"] == true }
                            }
                            val modTotalCount = module.submodules.sumOf { it.allowedActions.size }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedModules = if (expandedModules.contains(module.key)) {
                                            expandedModules - module.key
                                        } else {
                                            expandedModules + module.key
                                        }
                                    }
                                    .padding(horizontal = tokens.screenPadding, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val moduleKey = "${module.key}.module_root"
                                val isModuleHidden = hiddenSubmodules.contains(moduleKey)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(360.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))

                                    // Module level Eye Icon: changes to ic_eye_close if module is hidden
                                    Icon(
                                        painter = painterResource(
                                            id = if (isModuleHidden) R.drawable.ic_eye_close else R.drawable.ic_eye_open
                                        ),
                                        contentDescription = "Toggle Module Visibility",
                                        tint = if (isModuleHidden) redText else TextSecondary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                hiddenSubmodules = if (hiddenSubmodules.contains(moduleKey)) {
                                                    hiddenSubmodules - moduleKey
                                                } else {
                                                    hiddenSubmodules + moduleKey
                                                }
                                            }
                                    )
                                    Spacer(Modifier.width(6.dp))

                                    Text(
                                        text = module.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isModuleHidden) mutedText else title_color
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "$modSelectedCount/$modTotalCount",
                                        fontSize = 10.sp,
                                        color = mutedText
                                    )
                                }

                                MATRIX_COLUMNS.forEach { (actionKey, _) ->
                                    val validSubmodules = module.submodules.filter { it.allowedActions.contains(actionKey) }
                                    Box(
                                        modifier = Modifier.width(72.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (validSubmodules.isNotEmpty()) {
                                            val allChecked = validSubmodules.all { sm ->
                                                matrixPermissions["${module.key}.${sm.key}.$actionKey"] == true
                                            }
                                            AppCheckbox(
                                                checked = allChecked,
                                                onCheckedChange = { checked ->
                                                    matrixPermissions = matrixPermissions.toMutableMap().apply {
                                                        validSubmodules.forEach { sm ->
                                                            put("${module.key}.${sm.key}.$actionKey", checked)
                                                        }
                                                    }
                                                },
                                                size = 14.dp
                                            )
                                        } else {
                                            Text("—", fontSize = 12.sp, color = sectionBorder)
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = sectionBorder.copy(alpha = 0.35f), thickness = 0.5.dp)
                        }
                    }
                }

                // Submodules - Only composed when expanded
                if (isExpanded) {
                    val grouped = module.submodules.groupBy { it.section }

                    grouped.forEach { (sectionName, submodulesList) ->
                        if (!sectionName.isNullOrBlank()) {
                            item(key = "section_${module.key}_$sectionName") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Primary_background.copy(alpha = 0.25f))
                                        .horizontalScroll(matrixScrollState)
                                ) {
                                    val sectionSelected = submodulesList.sumOf { s ->
                                        s.allowedActions.count { matrixPermissions["${module.key}.${s.key}.$it"] == true }
                                    }
                                    val sectionTotal = submodulesList.sumOf { it.allowedActions.size }

                                    Row(
                                        modifier = Modifier
                                            .width(960.dp)
                                            .padding(start = tokens.screenPadding + 16.dp, top = 8.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = mutedText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = sectionName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "$sectionSelected/$sectionTotal",
                                            fontSize = 10.sp,
                                            color = mutedText
                                        )
                                    }
                                }
                            }
                        }

                        items(submodulesList, key = { "sub_${module.key}_${it.key}" }) { submodule ->
                            val submoduleKey = "${module.key}.${submodule.key}"
                            val isHidden = hiddenSubmodules.contains(submoduleKey)
                            val indentPadding = if (submodule.section != null) 36.dp else 24.dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isHidden) light_grey.copy(alpha = 0.2f) else Color.White)
                                    .horizontalScroll(matrixScrollState)
                            ) {
                                Column(modifier = Modifier.width(960.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = tokens.screenPadding, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .width(360.dp)
                                                .padding(start = indentPadding)
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    id = if (isHidden) R.drawable.ic_eye_close else R.drawable.ic_eye_open
                                                ),
                                                contentDescription = if (isHidden) "Hidden from Sidebar" else "Visible in Sidebar",
                                                tint = if (isHidden) redText else TextSecondary,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        hiddenSubmodules = if (hiddenSubmodules.contains(submoduleKey)) {
                                                            hiddenSubmodules - submoduleKey
                                                        } else {
                                                            hiddenSubmodules + submoduleKey
                                                        }
                                                    }
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = submodule.label,
                                                fontSize = 11.5.sp,
                                                color = if (isHidden) mutedText else title_color
                                            )
                                        }

                                        MATRIX_COLUMNS.forEach { (actionKey, _) ->
                                            Box(
                                                modifier = Modifier.width(72.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (submodule.allowedActions.contains(actionKey)) {
                                                    val isChecked = matrixPermissions["${module.key}.${submodule.key}.$actionKey"] == true
                                                    AppCheckbox(
                                                        checked = isChecked,
                                                        onCheckedChange = { checked ->
                                                            matrixPermissions = matrixPermissions.toMutableMap().apply {
                                                                put("${module.key}.${submodule.key}.$actionKey", checked)
                                                            }
                                                        },
                                                        size = 14.dp
                                                    )
                                                } else {
                                                    Text("—", fontSize = 12.sp, color = sectionBorder)
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = sectionBorder.copy(alpha = 0.25f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(tokens.screenPadding * 2)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionContainerCard(
    title: String,
    subtitle: String? = null,
    trailingBadge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.4f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
                    if (subtitle != null) {
                        Text(subtitle, fontSize = tokens.caption, color = mutedText)
                    }
                }
                trailingBadge?.invoke()
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            content()
        }
    }
}