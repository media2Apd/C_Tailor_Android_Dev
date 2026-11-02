@file:Suppress("unused")
package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.permission.ModulePermissionsData
import com.cuso.tailor.model.permission.ModulePermissionsResponse
import com.cuso.tailor.model.permission.PaywallModuleConfig
import com.cuso.tailor.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    private val _permissions = MutableStateFlow<ModulePermissionsData?>(null)
    val permissions: StateFlow<ModulePermissionsData?> = _permissions.asStateFlow()

    private val _isLoadingPermissions = MutableStateFlow(false)
    val isLoadingPermissions: StateFlow<Boolean> = _isLoadingPermissions.asStateFlow()

    init {
        fetchPermissions()
    }
    private val _isWorkspaceLocked = MutableStateFlow(false)
    val isWorkspaceLocked: StateFlow<Boolean> = _isWorkspaceLocked.asStateFlow()
    /**
     * Fetches current subscription features from backend endpoint
     */
    fun fetchPermissions() {
        viewModelScope.launch {
            _isLoadingPermissions.value = true
            apiRepository.request<ModulePermissionsResponse> {
                get("/api/common/plan-setups/customer/my-plan-modules")
            }.fold(
                onSuccess = { res ->
                    val data = res.data
                    _permissions.value = data

                    // Check if all module arrays are completely empty
                    val isEmptyWorkspace = data == null || (
                            data.home.isEmpty() &&
                                    data.sales.isEmpty() &&
                                    data.marketing.isEmpty() &&
                                    data.finance.isEmpty() &&
                                    data.inventory.isEmpty() &&
                                    data.logistics.isEmpty() &&
                                    data.services.isEmpty() &&
                                    data.hr.isEmpty() &&
                                    data.itModule.isEmpty() &&
                                    data.legal.isEmpty() &&
                                    data.security.isEmpty() &&
                                    data.reports.isEmpty()
                            )

                    _isWorkspaceLocked.value = isEmptyWorkspace
                },
                onFailure = {
                    // If API fails or no subscription found, keep workspace locked
                    _isWorkspaceLocked.value = true
                }
            )
            _isLoadingPermissions.value = false
        }
    }

    /**
     * Maps any app route to its (module, featureKey) pair according to the subscription API response
     */
    fun getRouteFeatureMapping(route: String): Pair<String, String>? {
        return when (route) {
            // ── HOME & SETTINGS ──
            "home_organization_profile", "organization_settings" -> "home" to "organizationProfile"
            "home_branch_management" -> "home" to "branches"
            "home_department_teams" -> "home" to "departments"
            "home_designation" -> "home" to "designations"
            "home_warehouse_management" -> "home" to "warehouse"
            "home_approval_rules", "home_create_approval_rule" -> "home" to "approvalRules"

            // ── SALES MODULE ──
            "sales_dashboard" -> "sales" to "dashboard"
            "sales_lead", "create_lead", "view_lead", "edit_lead", "convert_lead_to_opportunity" -> "sales" to "leadManagement"
            "sales_opportunity_pipeline", "sales_opportunities", "create_opportunity", "opportunity_detail" -> "sales" to "opportunities"
            "sales_sales_orders", "create_order", "order_preview", "order_overview", "sales_orders", "service_order_overview", "order_management_overview" -> "sales" to "orders"
            "sales_customers", "create_customer", "view_customer", "edit_customer", "view_customer_recent" -> "sales" to "customers"
            "sales_measurements", "measurement_entry", "measurements_available_view", "measurement_management_view", "measurement_fields_config" -> "sales" to "measurements"
            "sales_pricing_overview", "garment_pricing_list", "create_garment_pricing", "sales_pricing_quotation", "create_quotation", "view_quotation" -> "sales" to "pricingQuotes"
            "sales_payment_and_billing", "payment_detail" -> "sales" to "billingAndPayments"
            "sales_settings", "sales_garment_type", "sales_add_segment", "sales_category_detail", "sales_garment_profile", "sales_configuration_preview", "sales_add_existing_field", "sales_create_measurement_field", "sales_add_garment", "sales_add_garment_category" -> "sales" to "designSetup"
            "sales_garment_pricing_setup", "sales_add_garment_pricing", "sales_add_fabric_pricing", "sales_add_work_pricing" -> "sales" to "pricingGarment"
            "sales_measurement_list" -> "sales" to "measurementList"

            // ── MARKETING MODULE ──
            "marketing_dashboard" -> "marketing" to "dashboard"
            "marketing_campaigns_overview", "marketing_all_campaigns", "marketing_campaign_detail" -> "marketing" to "campaigns"
            "marketing_advertising_overview", "marketing_advertising" -> "marketing" to "growth"
            "marketing_social_overview", "marketing_social_media" -> "marketing" to "engagement"
            "marketing_promotions_overview", "marketing_all_promotions" -> "marketing" to "budget"
            "marketing_customers_overview", "marketing_all_customers" -> "marketing" to "leadsAudience"
            "marketing_website_overview", "marketing_traffic_engagement", "marketing_pages", "marketing_sources", "marketing_channels" -> "marketing" to "website"
            "marketing_calendar", "marketing_insights" -> "marketing" to "team"

            // ── FINANCE MODULE ──
            "finance_dashboard" -> "finance" to "dashboard"
            "finance_sales_orders" -> "finance" to "finOrders"
            "finance_sales_invoices", "finance_invoice_detail", "finance_create_sales_invoice" -> "finance" to "finInvoices"
            "finance_receipts", "finance_receipt_detail", "finance_create_payment_receipt", "finance_payments_received", "payment_detail_screen" -> "finance" to "finReceipts"
            "finance_purchase_invoices", "finance_purchase_invoice_detail", "finance_bills_list" -> "finance" to "finPurchaseBills"
            "finance_customers", "finance_customer_detail", "finance_create_customer" -> "finance" to "finCustomers"
            "finance_suppliers", "finance_supplier_detail", "finance_create_supplier" -> "finance" to "finSuppliers"
            "finance_purchase_orders" -> "finance" to "finPurchaseOrders"
            "finance_purchase_receive" -> "finance" to "finPurchaseReceives"
            "finance_payments_made", "finance_record_payment" -> "finance" to "finPaymentsMade"
            "finance_purchase_returns", "finance_purchase_return_detail", "finance_create_purchase_return" -> "finance" to "finPurchaseReturns"
            "finance_credits", "finance_create_credit", "finance_credit_detail" -> "finance" to "finCustomerCredits"
            "finance_vendor_credits" -> "finance" to "finSupplierCredits"
            "finance_chart_of_accounts", "finance_trial_balance", "finance_ledger", "home_opening_balance" -> "finance" to "finChartOfAccounts"
            "finance_journal_screen" -> "finance" to "finJournalEntries"
            "finance_expenses" -> "finance" to "expenses"
            "finance_bank_accounts", "finance_create_bank_account", "finance_bank_account_detail" -> "finance" to "finBankAccounts"
            "finance_cash_book", "finance_cash_book_detail", "finance_payments_mode", "payment_mode_detail" -> "finance" to "finCashBooks"
            "finance_bank_statements", "finance_bank_statement_detail", "finance_import_bank_statement" -> "finance" to "finBankStatements"
            "finance_add_gst", "finance_gst_settings" -> "finance" to "finTaxRegistration"
            "finance_tax_rates", "finance_add_tax_group" -> "finance" to "finTaxRatesRules"

            // ── INVENTORY MODULE ──
            "inventory_dashboard" -> "inventory" to "dashboard"
            "inventory_items", "inventory_all_items", "inventory_create_item", "inventory_item_detail" -> "inventory" to "items"
            "inventory_item_groups", "inventory_create_item_group", "inventory_item_group_detail" -> "inventory" to "itemGroups"
            "inventory_bulk", "inventory_all_bulk", "inventory_add_bulk", "inventory_bulk_detail" -> "inventory" to "bulk"
            "inventory_pricing_list", "inventory_new_pricing" -> "inventory" to "pricing"
            "inventory_adjustment", "inventory_adjustments" -> "inventory" to "adjustment"
            "inventory_transfer_order", "inventory_transfer_stock", "inventory_items_transfer_stock" -> "inventory" to "transferOrders"
            "inventory_suppliers", "inventory_supplier_detail", "inventory_procurement_suppliers" -> "inventory" to "procSuppliers"
            "inventory_requisitions", "inventory_create_requisition", "inventory_requisition_detail", "inventory_procurement_purchase_request", "inventory_procurement_requisitions" -> "inventory" to "procRequisitions"
            "inventory_purchase_orders", "inventory_create_purchase_order_flow", "inventory_purchase_order_detail_flow", "inventory_procurement_purchase_order", "inventory_procurement_purchase_orders", "inventory_procurement_orders" -> "inventory" to "procPurchaseOrders"
            "inventory_purchase_receive", "inventory_purchase_receive_detail", "inventory_purchase_new_bill", "inventory_purchase_receive_preview_pdf", "inventory_procurement_purchase_receive" -> "inventory" to "procPurchaseReceives"
            "inventory_all_bills", "inventory_billing", "inventory_bill_preview", "inventory_create_bill", "inventory_payable_purchase_detail", "inventory_procurement_bill_list", "inventory_procurement_bills_list", "inventory_bills_list", "inventory_bill_list" -> "inventory" to "procBills"
            "inventory_payments_made", "inventory_payment_overview_detail", "inventory_record_bill_payment", "inventory_payables_payments" -> "inventory" to "procPaymentsMade"
            "inventory_returns", "inventory_procurement_returns", "inventory_procurement_return", "inventory_return" -> "inventory" to "procPurchaseReturn"
            "inventory_credits", "inventory_procurement_credits", "inventory_payables_credits" -> "inventory" to "procVendorCredits"
            "inventory_barcode", "inventory_create_barcode", "inventory_barcode_list", "inventory_procurement_barcode", "inventory_procurement_barcode_list", "inventory_all_barcodes" -> "inventory" to "procBarcodes"
            "inventory_stock_location", "inventory_location_management", "inventory_procurement_location_management", "inventory_stock_location_details", "inventory_stock_location_form" -> "inventory" to "locations"
            "inventory_low_stock_alerts", "inventory_low_stock_alert", "inventory_create_purchase_order" -> "inventory" to "lowStockAlert"
            "inventory_auto_reorder", "inventory_auto_reorder_detail" -> "inventory" to "autoReorder"
            "inventory_safety_stock" -> "inventory" to "safetyStock"
            "inventory_picklist", "inventory_picklist_detail", "inventory_create_picklist" -> "inventory" to "picklist"
            "inventory_packages", "inventory_create_package", "inventory_package_detail" -> "inventory" to "packages"
            "inventory_category_listing", "inventory_create_category", "inventory_multichannel_category_listing" -> "inventory" to "category"
            "inventory_location_structure", "inventory_floor_overview", "inventory_add_floor", "inventory_section_overview", "inventory_add_section", "inventory_rack_overview", "inventory_add_rack", "inventory_bin_overview", "inventory_add_bin" -> "inventory" to "warehouseStructure"
            "inventory_auto_reorder_settings" -> "inventory" to "autoReorderSettings"
            "inventory_pdf_templates" -> "inventory" to "pdftemp"
            "inventory_approvals", "inventory_approval_detail" -> "inventory" to "approvals"
            "inventory_allocation_rules", "inventory_create_allocation", "inventory_multichannel_allocation_list" -> "inventory" to "custom"

            // ── LOGISTICS MODULE ──
            "logistics_dashboard" -> "logistics" to "dashboard"
            "logistics_orders", "logistics_order_detail" -> "logistics" to "orderList"
            "logistics_delivery", "delivery_detail", "logistics_dispatched" -> "logistics" to "delivery"
            "logistics_order_tracking", "tracking_overview", "logistics_tracking_events" -> "logistics" to "trackingEvents"
            "logistics_courier_management", "courier_overview" -> "logistics" to "courierManagement"
            "logistics_courier_partner_list", "courier_partner_overview", "add_courier_partner" -> "logistics" to "courierPartners"
            "logistics_shipments", "shipment_detail", "create_shipment" -> "logistics" to "shipments"
            "logistics_returns", "return_detail", "create_return" -> "logistics" to "returns"
            "logistics_cod_screen", "logistics_cod_detail" -> "logistics" to "codOrders"

            // ── SERVICES MODULE ──
            "services_dashboard" -> "services" to "dashboard"
            "services_production_orders", "services_service_orders", "services_service_order" -> "services" to "productionOrders"
            "services_production_works", "services_production_pipeline", "services_qc_delivery", "services_assignment_mode" -> "services" to "productionWorks"
            "services_service_request", "create_request", "review_services", "review_service_request" -> "services" to "serviceRequests"
            "services_alteration_management", "create_alteration", "view_alteration" -> "services" to "alterations"
            "services_customer_feedback", "feedback_detail" -> "services" to "customerFeedback"
            "services_customer_complaints", "complaint_detail" -> "services" to "customerComplaints"
            "services_service_templates", "services_create_service_template", "services_view_service_template" -> "services" to "serviceTemplates"
            "services_service_status", "service_status_detail", "services_service_delivery", "services_delay_rework" -> "services" to "workflowStages"

            // ── HR MODULE ──
            "hr_dashboard" -> "hr" to "hrDashboard"
            "hr_all_employees", "hr_employee_onboarding", "hr_employee_management", "hr_organization_structure" -> "hr" to "allEmployees"
            "hr_daily_attendance", "hr_attendance", "hr_attendance_detail" -> "hr" to "dailyAttendance"
            "hr_attendance_manual_entry" -> "hr" to "manualEntry"
            "hr_attendance_shift_management", "hr_shift_list", "hr_create_shift" -> "hr" to "shiftManagement"
            "hr_monthly_attendance" -> "hr" to "monthlyAttendance"
            "hr_leave_approval", "hr_apply_leave" -> "hr" to "leaveApproval"
            "hr_payroll_salary_structure", "hr_payroll_salary_components", "hr_payroll_salary_components_create", "hr_payroll_salary_templates", "hr_payroll_salary_templates_form" -> "hr" to "salaryStructureMaster"
            "hr_training_management", "hr_add_edit_program" -> "hr" to "trainingProgram"
            "hr_training_assignment_list", "hr_assign_training_form" -> "hr" to "assignmentManagement"
            "hr_employee_document_upload", "hr_new_document_upload" -> "hr" to "employeeDocuments"
            "hr_document_categories", "hr_add_document_category" -> "hr" to "documentCategories"
            "hr_expense_claims", "hr_expense_new_claim", "hr_expense_claim_detail", "hr_expense_claim_detail_readonly", "hr_submit_expense_claim", "hr_edit_expense_claim" -> "hr" to "expenses"
            "hr_expense_type", "hr_add_expense_type" -> "hr" to "expenseType"
            "hr_expense_finance_reimbursement", "hr_expense_process_payment" -> "hr" to "financeReimbursement"

            // ── IT MODULE ──
            "it_dashboard" -> "it" to "dashboard"
            "it_device_management", "it_device_detail" -> "it" to "deviceManagement"
            "it_pos_billing", "it_pos_billing_detail" -> "it" to "posAndBillingTechnology"
            "it_integrations" -> "it" to "integrations"
            "it_backup_recovery", "it_backup_recovery_detail", "it_backup_restore" -> "it" to "backupAndRecovery"
            "it_asset_management", "it_asset_detail", "it_asset_register" -> "it" to "itAssetManagement"
            "it_service_desk", "it_service_desk_create", "it_service_desk_detail" -> "it" to "itServiceDesk"

            // ── LEGAL MODULE ──
            "legal_dashboard" -> "legal" to "dashboard"
            "legal_calendar" -> "legal" to "auditCalendar"
            "legal_contracts", "legal_contract_detail", "legal_create_contract" -> "legal" to "contracts"
            "legal_compliance", "legal_compliance_detail", "legal_create_compliance" -> "legal" to "compliance"
            "legal_documents", "legal_document_detail", "legal_create_document" -> "legal" to "documents"
            "legal_licenses", "legal_license_detail", "legal_create_license" -> "legal" to "licenses"
            "legal_customer", "legal_customer_detail", "legal_create_customer_record" -> "legal" to "customerLegal"
            "legal_employee", "legal_employee_detail", "legal_create_employee" -> "legal" to "employeeLegal"
            "legal_vendor", "legal_vendor_detail", "legal_create_vendor" -> "legal" to "vendorLegal"
            "legal_notice", "legal_notice_detail", "legal_create_notice" -> "legal" to "notices"

            // ── REPORTS MODULE ──
            "reports_sales" -> "reports" to "orderMasterRegister"
            "reports_marketing" -> "reports" to "mktCampaignPerformance"
            "reports_finance" -> "reports" to "revenueExpenseSummary"
            "reports_inventory" -> "reports" to "invStockSummary"
            "reports_logistics" -> "reports" to "logDeliveryPerformance"
            "reports_services" -> "reports" to "servicePerformanceSla"
            "reports_human_resource" -> "reports" to "hrAttendanceRegister"
            "reports_it" -> "reports" to "itAssetRegister"
            "reports_security" -> "reports" to "secIncidentsAlerts"
            "reports_legal" -> "reports" to "legalContractsExpiry"

            // ── SECURITY & ROLES ──
            "home_role_management" -> "security" to "roles"

            else -> null
        }
    }

    /**
     * Checks if the given screen route is authorized under current subscription
     */
    fun hasRouteAccess(route: String): Boolean {
        val mapping = getRouteFeatureMapping(route) ?: return true
        val currentPermissions = _permissions.value ?: return true
        return currentPermissions.isFeatureAllowed(mapping.first, mapping.second)
    }

    /**
     * Returns module-specific upgrade paywall UI content
     */
    fun getPaywallConfigForModule(module: String): PaywallModuleConfig {
        return when (module.lowercase()) {
            "inventory" -> PaywallModuleConfig(
                moduleKey = "inventory",
                moduleTitle = "Inventory Module",
                headline = "Upgrade to Unlock Inventory Tools",
                description = "Track stock, suppliers, and warehouse operations easily.",
                efficiencyBadge = "+30% Efficiency",
                featuresIncluded = listOf(
                    "Stock Tracking",
                    "Warehouse Management",
                    "Supplier Management",
                    "Low Stock Alerts",
                    "Inventory Reports"
                )
            )
            "sales" -> PaywallModuleConfig(
                moduleKey = "sales",
                moduleTitle = "Sales Module",
                headline = "Upgrade to Unlock Sales Tools",
                description = "Manage pipeline, custom garment pricing, and client orders seamlessly.",
                efficiencyBadge = "+45% Conversions",
                featuresIncluded = listOf(
                    "Lead Pipeline Tracking",
                    "Custom Measurements Engine",
                    "Quotation Generator",
                    "Sales Billing & Payments",
                    "Sales Performance Reports"
                )
            )
            "finance" -> PaywallModuleConfig(
                moduleKey = "finance",
                moduleTitle = "Finance Module",
                headline = "Upgrade to Unlock Finance Tools",
                description = "Full accounting suite with general ledger, invoices, and bank reconciliation.",
                efficiencyBadge = "+50% Automation",
                featuresIncluded = listOf(
                    "General Ledger & Journal Entries",
                    "Bank Account Reconciliation",
                    "Tax & GST Automation",
                    "Vendor & Customer Credits",
                    "Financial Balance Reports"
                )
            )
            "marketing" -> PaywallModuleConfig(
                moduleKey = "marketing",
                moduleTitle = "Marketing Module",
                headline = "Upgrade to Unlock Marketing Tools",
                description = "Drive campaign growth, track social channels, and audience engagement.",
                efficiencyBadge = "+35% Growth",
                featuresIncluded = listOf(
                    "Ad Campaign Tracking",
                    "Audience Segmentation",
                    "Social Media Sync",
                    "Promotions & Budget Engine",
                    "Traffic Analytics"
                )
            )
            "logistics" -> PaywallModuleConfig(
                moduleKey = "logistics",
                moduleTitle = "Logistics Module",
                headline = "Upgrade to Unlock Logistics Tools",
                description = "Automate courier management, dispatch tracking, and return operations.",
                efficiencyBadge = "+40% Delivery Speed",
                featuresIncluded = listOf(
                    "Courier Partner Integration",
                    "Live Shipment Tracking",
                    "Cash on Delivery Management",
                    "Return Order Workflows",
                    "Dispatch Dispatch Monitoring"
                )
            )
            "services" -> PaywallModuleConfig(
                moduleKey = "services",
                moduleTitle = "Services Module",
                headline = "Upgrade to Unlock Services Tools",
                description = "Manage alterations, workshop job cards, quality control, and client feedback.",
                efficiencyBadge = "+50% Faster Turnaround",
                featuresIncluded = listOf(
                    "Production Pipeline Tracking",
                    "Workshop Job Assignment",
                    "Alteration Management",
                    "Quality Control Checks",
                    "Client Feedback Management"
                )
            )
            "hr" -> PaywallModuleConfig(
                moduleKey = "hr",
                moduleTitle = "HR & Payroll Module",
                headline = "Upgrade to Unlock HR Tools",
                description = "Automate employee onboarding, daily attendance, and expense reimbursements.",
                efficiencyBadge = "+40% Productivity",
                featuresIncluded = listOf(
                    "Attendance & Shift Tracking",
                    "Employee Leave Approvals",
                    "Payroll Salary Components",
                    "Staff Training Management",
                    "Expense Claim Approvals"
                )
            )
            "it" -> PaywallModuleConfig(
                moduleKey = "it",
                moduleTitle = "IT & Systems Module",
                headline = "Upgrade to Unlock IT Tools",
                description = "Manage terminal fleets, POS devices, asset lifecycles, and data backups.",
                efficiencyBadge = "+99.9% Uptime",
                featuresIncluded = listOf(
                    "POS & Terminal Fleet Monitor",
                    "Device & Hardware Inventory",
                    "Cloud Backup & Snapshot Restore",
                    "IT Helpdesk & Ticket Triage",
                    "System Asset Register"
                )
            )
            "legal" -> PaywallModuleConfig(
                moduleKey = "legal",
                moduleTitle = "Legal & Compliance Module",
                headline = "Upgrade to Unlock Legal Tools",
                description = "Maintain statutory licenses, contracts, audit calendars, and legal notices.",
                efficiencyBadge = "100% Compliance",
                featuresIncluded = listOf(
                    "Contract Lifecycle Management",
                    "Statutory License Renewals",
                    "Legal Notice & Dispute Tracking",
                    "Vendor & Employee Legal Files",
                    "Regulatory Audit Calendar"
                )
            )
            "reports" -> PaywallModuleConfig(
                moduleKey = "reports",
                moduleTitle = "Analytics & Reports Module",
                headline = "Upgrade to Unlock Advanced Reports",
                description = "Access executive dashboards, revenue insights, and multi-branch data.",
                efficiencyBadge = "+60% Decision Clarity",
                featuresIncluded = listOf(
                    "Multi-Branch Sales Reports",
                    "Inventory Valuation Registers",
                    "Profit & Loss Ledgers",
                    "Logistics SLA Performance",
                    "Audit & Security Logs"
                )
            )
            else -> PaywallModuleConfig(
                moduleKey = module,
                moduleTitle = "${module.replaceFirstChar { it.uppercase() }} Module",
                headline = "Upgrade to Unlock Advanced Tools",
                description = "Gain full enterprise access to scale your operations effortlessly.",
                featuresIncluded = listOf(
                    "Advanced Automation",
                    "Multi-level Approvals",
                    "Detailed Reports & Insights",
                    "Priority Cloud Sync"
                )
            )
        }
    }
}