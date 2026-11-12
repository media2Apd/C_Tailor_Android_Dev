@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.database.dao.PermissionDao
import com.cuso.tailor.database.entities.PlanModuleEntity
import com.cuso.tailor.database.entities.UserPermissionEntity
import com.cuso.tailor.model.permission.ActionPermission
import com.cuso.tailor.model.permission.ModulePermissionsData
import com.cuso.tailor.model.permission.ModulePermissionsResponse
import com.cuso.tailor.model.permission.MyPermissionsData
import com.cuso.tailor.model.permission.MyPermissionsResponse
import com.cuso.tailor.model.permission.PaywallModuleConfig
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.view.home.paywall.ModuleUpgradeConfig
import com.cuso.tailor.view.home.paywall.PermissionAction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val permissionDao: PermissionDao,
    private val gson: Gson
) : ViewModel() {

    private val _modulePermissions = MutableStateFlow<ModulePermissionsData?>(null)
    val modulePermissions: StateFlow<ModulePermissionsData?> = _modulePermissions.asStateFlow()

    private val _myPermissions = MutableStateFlow<MyPermissionsData?>(null)
    val myPermissions: StateFlow<MyPermissionsData?> = _myPermissions.asStateFlow()

    private val _isLoadingPermissions = MutableStateFlow(false)
    val isLoadingPermissions: StateFlow<Boolean> = _isLoadingPermissions.asStateFlow()

    private val _isWorkspaceLocked = MutableStateFlow(false)
    val isWorkspaceLocked: StateFlow<Boolean> = _isWorkspaceLocked.asStateFlow()

    init {
        loadFromCache()
        fetchPermissions(forceRefresh = false)
    }

    private fun loadFromCache() {
        viewModelScope.launch {
            val cachedUserPerms = permissionDao.getUserPermissions()
            val cachedPlanModules = permissionDao.getPlanModules()

            if (cachedUserPerms != null) {
                val listType = object : TypeToken<List<String>>() {}.type
                val mapType = object : TypeToken<Map<String, Map<String, ActionPermission>>>() {}.type

                val visibleList: List<String> = try {
                    gson.fromJson(cachedUserPerms.visiblePermissionsJson, listType) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                val permissionsMap: Map<String, Map<String, ActionPermission>> = try {
                    gson.fromJson(cachedUserPerms.rawPermissionsJson, mapType) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                _myPermissions.value = MyPermissionsData(
                    isOwner = cachedUserPerms.isOwner,
                    permissions = permissionsMap,
                    visible = visibleList
                )
            }

            if (cachedPlanModules != null) {
                try {
                    _modulePermissions.value = gson.fromJson(
                        cachedPlanModules.modulesJson,
                        ModulePermissionsData::class.java
                    )
                } catch (e: Exception) {
                    _modulePermissions.value = null
                }
            }

            evaluateWorkspaceLock()
        }
    }

    fun fetchPermissions(forceRefresh: Boolean = false, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoadingPermissions.value = true
            try {
                if (forceRefresh) {
                    permissionDao.clearAllPermissions()
                }

                coroutineScope {
                    val moduleDeferred = async {
                        apiRepository.request<ModulePermissionsResponse> {
                            get("/api/common/plan-setups/customer/my-plan-modules")
                        }
                    }

                    val meDeferred = async {
                        apiRepository.request<MyPermissionsResponse> {
                            get("/api/permissions/me")
                        }
                    }

                    val moduleResult = moduleDeferred.await()
                    val meResult = meDeferred.await()

                    moduleResult.fold(
                        onSuccess = { res ->
                            _modulePermissions.value = res.data
                            res.data?.let { data ->
                                permissionDao.insertPlanModules(
                                    PlanModuleEntity(
                                        id = 1,
                                        modulesJson = gson.toJson(data)
                                    )
                                )
                            }
                        },
                        onFailure = {
                            if (forceRefresh) _modulePermissions.value = null
                        }
                    )

                    meResult.fold(
                        onSuccess = { res ->
                            _myPermissions.value = res.data
                            res.data?.let { data ->
                                permissionDao.insertUserPermissions(
                                    UserPermissionEntity(
                                        id = 1,
                                        isOwner = data.isOwner,
                                        visiblePermissionsJson = gson.toJson(data.visible),
                                        rawPermissionsJson = gson.toJson(data.permissions)
                                    )
                                )
                            }
                        },
                        onFailure = {
                            if (forceRefresh) _myPermissions.value = null
                        }
                    )

                    evaluateWorkspaceLock()
                }
            } catch (e: Exception) {
                evaluateWorkspaceLock()
            } finally {
                _isLoadingPermissions.value = false
                onComplete?.invoke()
            }
        }
    }

    private fun evaluateWorkspaceLock() {
        val moduleData = _modulePermissions.value
        val myPermData = _myPermissions.value

        val isPlanEmpty = moduleData == null || (
                moduleData.home.isEmpty() &&
                        moduleData.sales.isEmpty() &&
                        moduleData.marketing.isEmpty() &&
                        moduleData.finance.isEmpty() &&
                        moduleData.inventory.isEmpty() &&
                        moduleData.logistics.isEmpty() &&
                        moduleData.services.isEmpty() &&
                        moduleData.hr.isEmpty() &&
                        moduleData.itModule.isEmpty() &&
                        moduleData.legal.isEmpty() &&
                        moduleData.security.isEmpty() &&
                        moduleData.reports.isEmpty()
                )

        val isUserRestricted = myPermData != null && !myPermData.isOwner && myPermData.visible.isEmpty()
        _isWorkspaceLocked.value = isPlanEmpty || isUserRestricted
    }

    /**
     * Checks if plan includes the feature
     */
    fun isPlanFeatureSubscribed(module: String, feature: String): Boolean {
        val moduleData = _modulePermissions.value ?: return true
        return moduleData.isFeatureAllowed(module, feature)
    }

    /**
     * Checks if user has access to a specific module feature (combining plan subscription and view permissions).
     */
    fun hasFeatureAccess(module: String, feature: String): Boolean {
        if (!isPlanFeatureSubscribed(module, feature)) return false
        val myData = _myPermissions.value ?: return true
        if (myData.isOwner) return true
        return myData.canView(module, feature)
    }

    /**
     * View Permission check: Returns true if user can view the route.
     */
    fun canViewRoute(route: String): Boolean {
        val mapping = getRouteFeatureMapping(route) ?: return true
        val (module, feature) = mapping

        // 1. Plan check
        if (!isPlanFeatureSubscribed(module, feature)) return false

        // 2. User action view check
        val myData = _myPermissions.value ?: return true
        if (myData.isOwner) return true
        return myData.canView(module, feature)
    }

    /**
     * Create Action check for current route or module.feature
     */
    fun canCreate(routeOrFeature: String): Boolean {
        val mapping = getRouteFeatureMapping(routeOrFeature)
        val myData = _myPermissions.value ?: return true
        if (myData.isOwner) return true

        return if (mapping != null) {
            myData.canCreate(mapping.first, mapping.second)
        } else {
            true
        }
    }

    /**
     * Edit Action check for current route or module.feature
     */
    fun canEdit(routeOrFeature: String): Boolean {
        val mapping = getRouteFeatureMapping(routeOrFeature)
        val myData = _myPermissions.value ?: return true
        if (myData.isOwner) return true

        return if (mapping != null) {
            myData.canEdit(mapping.first, mapping.second)
        } else {
            true
        }
    }

    /**
     * Delete Action check for current route or module.feature
     */
    fun canDelete(routeOrFeature: String): Boolean {
        val mapping = getRouteFeatureMapping(routeOrFeature)
        val myData = _myPermissions.value ?: return true
        if (myData.isOwner) return true

        return if (mapping != null) {
            myData.canDelete(mapping.first, mapping.second)
        } else {
            true
        }
    }

    // Inside PermissionViewModel.kt

    fun canAction(module: String, feature: String, action: PermissionAction): Boolean {
        val myData = _myPermissions.value ?: return true
        if (myData.isOwner) return true
        if (!isPlanFeatureSubscribed(module, feature)) return false

        return when (action) {
            PermissionAction.VIEW -> myData.canView(module, feature)
            PermissionAction.CREATE -> myData.canCreate(module, feature)
            PermissionAction.EDIT -> myData.canEdit(module, feature)
            PermissionAction.DELETE -> myData.canDelete(module, feature)
            PermissionAction.EXPORT -> myData.canExport(module, feature)
            PermissionAction.PRINT -> myData.canPrint(module, feature)
            PermissionAction.SHARE -> myData.canShare(module, feature)
            PermissionAction.APPROVE -> myData.canApprove(module, feature)
        }
    }

    fun canActionForRoute(route: String, action: PermissionAction): Boolean {
        val mapping = getRouteFeatureMapping(route) ?: return true
        return canAction(mapping.first, mapping.second, action)
    }

    fun canExport(routeOrFeature: String): Boolean =
        canActionForRoute(routeOrFeature, PermissionAction.EXPORT)

    fun canPrint(routeOrFeature: String): Boolean =
        canActionForRoute(routeOrFeature, PermissionAction.PRINT)

    fun canApprove(routeOrFeature: String): Boolean =
        canActionForRoute(routeOrFeature, PermissionAction.APPROVE)

    fun hasRouteAccess(route: String): Boolean = canViewRoute(route)

    fun getPaywallConfigForModule(moduleOrFeatureKey: String): PaywallModuleConfig {
        val currentPlan = _myPermissions.value?.let { "Professional Plan" } ?: "Standard Plan"
        return ModuleUpgradeConfig.getConfig(
            key = moduleOrFeatureKey,
            planName = currentPlan
        )
    }

    fun getRouteFeatureMapping(route: String): Pair<String, String>? {
        return when (route) {
            // ── 1. HOME & SETTINGS ──
            "home_organization_profile"-> "home" to "organizationProfile"
            "home_branch_management" -> "home" to "branches"
            "home_department_teams" -> "home" to "departments"
            "home_designation" -> "home" to "designations"
            "home_warehouse_management" -> "home" to "warehouse"
            "home_approval_rules", "home_create_approval_rule" -> "home" to "approvalRules"

            // ── 2. SALES MODULE ──
            "sales_dashboard" -> "sales" to "dashboard"
            "sales_lead", "create_lead", "view_lead", "edit_lead", "convert_lead_to_opportunity" -> "sales" to "leadManagement"
            "sales_opportunity_pipeline", "sales_opportunities", "create_opportunity", "opportunity_detail" -> "sales" to "opportunities"
            "sales_sales_orders", "create_order", "order_preview", "order_overview", "sales_orders", "service_order_overview", "order_management_overview" -> "sales" to "orders"
            "sales_customers", "create_customer", "view_customer", "edit_customer", "view_customer_recent" -> "sales" to "customers"
            "sales_measurements", "measurement_entry", "measurements_available_view", "measurement_management_view", "measurement_fields_config" -> "sales" to "measurements"
            "sales_pricing_overview", "garment_pricing_list", "create_garment_pricing", "sales_pricing_quotation", "create_quotation", "view_quotation" -> "sales" to "pricingQuotes"
            "sales_payment_and_billing", "payment_detail" -> "sales" to "billingAndPayments"
            "sales_garment_type", "sales_add_segment", "sales_category_detail", "sales_garment_profile", "sales_configuration_preview", "sales_add_existing_field", "sales_create_measurement_field", "sales_add_garment", "sales_add_garment_category" -> "sales" to "garmentMeasurement"
            "sales_settings" -> "sales" to "designSetup"
            "sales_garment_pricing_setup", "sales_add_garment_pricing" -> "sales" to "pricingGarment"
            "sales_add_fabric_pricing" -> "sales" to "pricingFabric"
            "sales_add_work_pricing" -> "sales" to "pricingWork"
            "sales_measurement_list" -> "sales" to "measurementList"

            // ── 3. FINANCE MODULE ──
            "finance_dashboard" -> "finance" to "dashboard"
            "finance_customers", "finance_customer_detail", "finance_create_customer" -> "finance" to "finCustomers"
            "finance_sales_orders" -> "finance" to "finOrders"
            "finance_sales_invoices", "finance_invoice_detail", "finance_create_sales_invoice" -> "finance" to "finInvoices"
            "finance_receipts", "finance_receipt_detail", "finance_create_payment_receipt", "finance_payments_received", "payment_detail_screen" -> "finance" to "finReceipts"
            "finance_credits", "finance_create_credit", "finance_credit_detail" -> "finance" to "finCustomerCredits"
            "finance_suppliers", "finance_supplier_detail", "finance_create_supplier" -> "finance" to "finSuppliers"
            "finance_purchase_orders" -> "finance" to "finPurchaseOrders"
            "finance_purchase_receive" -> "finance" to "finPurchaseReceives"
            "finance_bills_list", "finance_purchase_invoices", "finance_purchase_invoice_detail" -> "finance" to "finPurchaseBills"
            "finance_payments_made", "finance_record_payment" -> "finance" to "finPaymentsMade"
            "finance_purchase_returns", "finance_purchase_return_detail", "finance_create_purchase_return" -> "finance" to "finPurchaseReturns"
            "finance_vendor_credits" -> "finance" to "finSupplierCredits"
            "finance_chart_of_accounts", "finance_trial_balance", "finance_ledger", "home_opening_balance" -> "finance" to "finChartOfAccounts"
            "finance_journal_screen" -> "finance" to "finJournalEntries"
            "finance_expenses" -> "finance" to "expenses"
            "finance_bank_accounts", "finance_create_bank_account", "finance_bank_account_detail" -> "finance" to "finBankAccounts"
            "finance_cash_book", "finance_cash_book_detail", "finance_payments_mode", "payment_mode_detail" -> "finance" to "finCashBooks"
            "finance_bank_statements", "finance_bank_statement_detail", "finance_import_bank_statement" -> "finance" to "finBankStatements"
            "finance_add_gst", "finance_gst_settings" -> "finance" to "finTaxRegistration"
            "finance_tax_rates", "finance_add_tax_group" -> "finance" to "finTaxRatesRules"

            // ── 4. INVENTORY MODULE ──
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
            "inventory_approvals", "inventory_approval_detail" -> "inventory" to "approvals"
            "inventory_location_structure", "inventory_floor_overview", "inventory_add_floor", "inventory_section_overview", "inventory_add_section", "inventory_rack_overview", "inventory_add_rack", "inventory_bin_overview", "inventory_add_bin" -> "inventory" to "warehouseStructure"
            "inventory_auto_reorder_settings" -> "inventory" to "autoReorderSettings"
            "inventory_pdf_templates" -> "inventory" to "pdftemp"

            // ── 5. HR MODULE ──
            "hr_dashboard" -> "hr" to "hrDashboard"
            "hr_all_employees", "hr_employee_onboarding", "hr_employee_management" -> "hr" to "employeeManagement"
            "hr_organization_structure" -> "hr" to "allEmployees"
            "hr_daily_attendance", "hr_attendance", "hr_attendance_detail" -> "hr" to "dailyAttendance"
            "hr_monthly_attendance" -> "hr" to "monthlyAttendance"
            "hr_attendance_manual_entry" -> "hr" to "manualEntry"
            "hr_attendance_shift_management", "hr_shift_list", "hr_create_shift" -> "hr" to "shiftManagement"
            "hr_leave_approval" -> "hr" to "leaveApproval"
            "hr_apply_leave" -> "hr" to "leaveDashboard"
            "hr_payroll_salary_structure", "hr_payroll_salary_templates", "hr_payroll_salary_templates_form" -> "hr" to "salaryStructureMaster"
            "hr_payroll_salary_components", "hr_payroll_salary_components_create" -> "hr" to "employeeSalaryAssignment"
            "hr_expense_claims", "hr_expense_claim_detail", "hr_expense_claim_detail_readonly" -> "hr" to "approvals"
            "hr_expense_new_claim", "hr_submit_expense_claim", "hr_edit_expense_claim" -> "hr" to "newClaims"
            "hr_expense_type", "hr_add_expense_type" -> "hr" to "expenseType"
            "hr_expense_finance_reimbursement", "hr_expense_process_payment" -> "hr" to "financeReimbursement"
            "hr_training_management", "hr_add_edit_program" -> "hr" to "trainingProgram"
            "hr_training_assignment_list", "hr_assign_training_form" -> "hr" to "assignmentManagement"
            "hr_employee_document_upload", "hr_new_document_upload" -> "hr" to "employeeDocuments"
            "hr_document_categories", "hr_add_document_category" -> "hr" to "documentCategories"

            // ── 6. MARKETING MODULE ──
            "marketing_dashboard" -> "marketing" to "dashboard"
            "marketing_campaigns_overview", "marketing_all_campaigns", "marketing_campaign_detail" -> "marketing" to "campaigns"
            "marketing_advertising_overview", "marketing_advertising" -> "marketing" to "advertising"
            "marketing_social_overview", "marketing_social_media" -> "marketing" to "socialMedia"
            "marketing_promotions_overview", "marketing_all_promotions" -> "marketing" to "promotions"
            "marketing_customers_overview", "marketing_all_customers" -> "marketing" to "leadsAudience"
            "marketing_website_overview", "marketing_traffic_engagement", "marketing_pages", "marketing_sources" -> "marketing" to "website"
            "marketing_channels" -> "marketing" to "marketingChannels"
            "marketing_calendar" -> "marketing" to "marketingCalendar"
            "marketing_insights" -> "marketing" to "marketingInsights"

            // ── 7. LOGISTICS MODULE ──
            "logistics_dashboard" -> "logistics" to "dashboard"
            "logistics_orders", "logistics_order_detail" -> "logistics" to "orderList"
            "logistics_delivery", "delivery_detail" -> "logistics" to "delivery"
            "logistics_dispatched" -> "logistics" to "dispatched"
            "logistics_shipments", "shipment_detail", "create_shipment" -> "logistics" to "shipments"
            "logistics_courier_management", "courier_overview" -> "logistics" to "courierManagement"
            "logistics_courier_partner_list", "courier_partner_overview", "add_courier_partner" -> "logistics" to "courierPartners"
            "logistics_tracking_events", "logistics_order_tracking", "tracking_overview" -> "logistics" to "trackingEvents"
            "logistics_returns", "return_detail", "create_return" -> "logistics" to "returns"
            "logistics_cod_screen", "logistics_cod_detail" -> "logistics" to "codOrders"

            // ── 8. SERVICES MODULE ──
            "services_dashboard" -> "services" to "dashboard"
            "services_production_orders", "services_service_orders", "services_service_order" -> "services" to "productionOrders"
            "services_production_works", "services_production_pipeline", "services_qc_delivery", "services_assignment_mode" -> "services" to "productionWorks"
            "services_service_request", "create_request", "review_services", "review_service_request" -> "services" to "serviceRequests"
            "services_alteration_management", "create_alteration", "view_alteration" -> "services" to "alterations"
            "services_customer_complaints", "complaint_detail" -> "services" to "customerComplaints"
            "services_customer_feedback", "feedback_detail" -> "services" to "customerFeedback"
            "services_service_templates", "services_create_service_template", "services_view_service_template" -> "services" to "serviceTemplates"
            "services_service_status", "service_status_detail", "services_service_delivery", "services_delay_rework" -> "services" to "workflowStages"

            // ── 9. IT MODULE ──
            "it_dashboard" -> "it" to "dashboard"
            "it_device_management", "it_device_detail" -> "it" to "deviceManagement"
            "it_pos_billing", "it_pos_billing_detail" -> "it" to "posAndBillingTechnology"
            "it_integrations" -> "it" to "integrations"
            "it_backup_recovery", "it_backup_recovery_detail", "it_backup_restore" -> "it" to "backupAndRecovery"
            "it_asset_management", "it_asset_detail", "it_asset_register" -> "it" to "itAssetManagement"
            "it_service_desk", "it_service_desk_create", "it_service_desk_detail" -> "it" to "itServiceDesk"

            // ── 10. LEGAL MODULE ──
            "legal_dashboard" -> "legal" to "dashboard"
            "legal_contracts", "legal_contract_detail", "legal_create_contract" -> "legal" to "contracts"
            "legal_compliance", "legal_compliance_detail", "legal_create_compliance" -> "legal" to "compliance"
            "legal_documents", "legal_document_detail", "legal_create_document" -> "legal" to "documents"
            "legal_licenses", "legal_license_detail", "legal_create_license" -> "legal" to "licenses"
            "legal_customer", "legal_customer_detail", "legal_create_customer_record" -> "legal" to "customerLegal"
            "legal_employee", "legal_employee_detail", "legal_create_employee" -> "legal" to "employeeLegal"
            "legal_vendor", "legal_vendor_detail", "legal_create_vendor" -> "legal" to "vendorLegal"
            "legal_notice", "legal_notice_detail", "legal_create_notice" -> "legal" to "notices"
            "legal_calendar" -> "legal" to "auditCalendar"

            // ── 11. SECURITY MODULE ──
            "security_dashboard" -> "security" to "dashboard"
            "security_roles", "home_role_management", "home_create_role" -> "security" to "roles"
            "security_data_backup" -> "security" to "dataBackup"
            "security_recovery" -> "security" to "recovery"
            "security_api_integration" -> "security" to "apiSecurity"
            "security_api_logs" -> "security" to "apiLogs"
            "security_mobile_security" -> "security" to "mobileSecurity"
            "security_help_center" -> "security" to "helpCenter"
            "security_support_ticket" -> "security" to "supportTicket"
            "security_audit_logs" -> "security" to "auditLogs"
            "security_users", "security_create_user" -> "security" to "users"

            // ── 12. REPORTS MODULE ──
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

            else -> null
        }
    }

    fun getRouteAction(route: String): PermissionAction {
        return when {
            route.contains("create") || route.contains("add") -> PermissionAction.CREATE
            route.contains("edit") || route.contains("update") -> PermissionAction.EDIT
            route.contains("approval") || route.contains("approve") -> PermissionAction.APPROVE
            else -> PermissionAction.VIEW
        }
    }
}