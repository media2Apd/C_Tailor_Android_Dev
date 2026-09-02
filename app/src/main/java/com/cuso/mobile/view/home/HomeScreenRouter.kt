@file:Suppress("unused","unusedVariable","AssignedValueIsNeverRead")

package com.cuso.mobile.view.home

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.model.settings.SegmentItem
import com.cuso.mobile.view.home.branch.BranchSettingsScreen
import com.cuso.mobile.view.home.department.DepartmentSettingsScreen
import com.cuso.mobile.view.home.designation.DesignationScreen
import com.cuso.mobile.view.home.finance.account_payable.payment_mode.AllPaymentListScreen
import com.cuso.mobile.view.home.finance.account_payable.payment_mode.PaymentDetailScreenAR
import com.cuso.mobile.view.home.finance.account_payable.purchase_invoices.PurchaseInvoiceDetailScreen
import com.cuso.mobile.view.home.finance.account_payable.purchase_invoices.PurchaseInvoiceItem
import com.cuso.mobile.view.home.finance.account_payable.purchase_invoices.PurchaseInvoiceScreen
import com.cuso.mobile.view.home.finance.account_payable.suppliers.AllSuppliersScreen
import com.cuso.mobile.view.home.finance.account_payable.suppliers.SupplierDetailScreen
import com.cuso.mobile.view.home.finance.account_payable.suppliers.SupplierRow
import com.cuso.mobile.view.home.finance.account_receivable.customers.CustomerDetailViewScreen
import com.cuso.mobile.view.home.finance.account_receivable.customers.FinanceCustomerScreen
import com.cuso.mobile.view.home.finance.account_receivable.payment_received.AllPaymentScreen
import com.cuso.mobile.view.home.finance.account_receivable.payment_received.PaymentDetailScreenAP
import com.cuso.mobile.view.home.finance.account_receivable.sales_invoice.FinanceInvoiceScreen
import com.cuso.mobile.view.home.finance.account_receivable.sales_invoice.InvoiceDetailScreen
import com.cuso.mobile.view.home.finance.expenses.ExpensesScreen
import com.cuso.mobile.view.home.finance.finance_core.chart_of_accounts.ChartOfAccountScreen
import com.cuso.mobile.view.home.finance.finance_core.journal_entry.ManualJournalEntryScreen
import com.cuso.mobile.view.home.finance.finance_core.trial_balance.LedgerScreen
import com.cuso.mobile.view.home.finance.finance_core.trial_balance.TrialBalanceScreen
import com.cuso.mobile.view.home.hr.attendance.AttendanceDetailScreen
import com.cuso.mobile.view.home.hr.attendance.AttendanceScreen
import com.cuso.mobile.view.home.hr.employees.AllEmployeesScreen
import com.cuso.mobile.view.home.hr.employees.EmployeeOnboardingScreen
import com.cuso.mobile.view.home.hr.employees.ScreenMode
import com.cuso.mobile.view.home.inventory.items.AllItemGroupScreen
import com.cuso.mobile.view.home.inventory.items.CreateItemGroupScreen
import com.cuso.mobile.view.home.inventory.items.all_items.AdjustmentType
import com.cuso.mobile.view.home.inventory.items.all_items.CreateItemScreen
import com.cuso.mobile.view.home.inventory.items.all_items.InventoryScreen
import com.cuso.mobile.view.home.inventory.items.all_items.InventoryViewOne
import com.cuso.mobile.view.home.inventory.procurement.orders.CreatePurchaseOrderScreen
import com.cuso.mobile.view.home.inventory.procurement.orders.LowStockAlertsScreen
import com.cuso.mobile.view.home.inventory.procurement.orders.LowStockItem
import com.cuso.mobile.view.home.logistics.delivery.DeliveryDetailScreen
import com.cuso.mobile.view.home.logistics.delivery.DeliveryManagementScreen
import com.cuso.mobile.view.home.logistics.order_tracking.OrderTrackingScreen
import com.cuso.mobile.view.home.logistics.order_tracking.TrackingOverviewScreen
import com.cuso.mobile.view.home.opening_balance.OpeningBalancesScreen
import com.cuso.mobile.view.home.profile_settings.ProfileSettingsScreen
import com.cuso.mobile.view.home.profile_settings.SettingsScreen
import com.cuso.mobile.view.home.profile_settings.all_settings.ModuleSettingsScreen
import com.cuso.mobile.view.home.profile_settings.all_settings.SettingsOverviewScreen
import com.cuso.mobile.view.home.reports.finance.FinanceReportPage
import com.cuso.mobile.view.home.reports.finance.ProfitAndLossReportScreen
import com.cuso.mobile.view.home.reports.inventory.DeadStockReportScreen
import com.cuso.mobile.view.home.reports.inventory.InventoryReportPage
import com.cuso.mobile.view.home.reports.inventory.LowStockScreen
import com.cuso.mobile.view.home.reports.inventory.PurchaseReportScreen
import com.cuso.mobile.view.home.reports.inventory.StockSummaryScreen
import com.cuso.mobile.view.home.reports.inventory.WarehouseReportScreen
import com.cuso.mobile.view.home.reports.sales.SalesOrderReportsScreen
import com.cuso.mobile.view.home.role.RoleSettingsScreen
import com.cuso.mobile.view.home.sales.settings.garment.AddNewGarmentScreen
import com.cuso.mobile.view.home.sales.settings.garment.AddSegmentScreen
import com.cuso.mobile.view.home.sales.settings.garment.garment_category_detail.GarmentCategoryDetailScreen
import com.cuso.mobile.view.home.sales.settings.garment.GarmentTypeContent
import com.cuso.mobile.view.home.sales.settings.garment.SalesSettingsScreen
import com.cuso.mobile.view.home.sales.customer.*
import com.cuso.mobile.view.home.sales.lead.*
import com.cuso.mobile.view.home.sales.measurements.MeasurementsScreen
import com.cuso.mobile.view.home.sales.payment_listing.*
import com.cuso.mobile.view.home.sales.pricing.*
import com.cuso.mobile.view.home.sales.quotation.*
import com.cuso.mobile.view.home.sales.sales_order.*
import com.cuso.mobile.view.home.sales.settings.garment.AddNewGarmentCategoryScreen
import com.cuso.mobile.view.home.sales.settings.garment.garment_category_detail.AddExistingFieldScreen
import com.cuso.mobile.view.home.sales.settings.garment.garment_category_detail.ConfigurationPreviewScreen
import com.cuso.mobile.view.home.sales.settings.garment.garment_category_detail.CreateMeasurementFieldScreen
import com.cuso.mobile.view.home.sales.settings.garment.garment_category_detail.GarmentProfileConfigScreen
import com.cuso.mobile.view.home.sales.settings.pricing_setup.AddFabricPriceScreen
import com.cuso.mobile.view.home.sales.settings.pricing_setup.AddNewGarmentPricingScreen
import com.cuso.mobile.view.home.sales.settings.pricing_setup.AddWorkPricingScreen
import com.cuso.mobile.view.home.sales.settings.pricing_setup.PricingSetupScreen
import com.cuso.mobile.view.home.services.alteration_management.AlterationManagementScreen
import com.cuso.mobile.view.home.services.alteration_management.CreateAlterationManagementScreen
import com.cuso.mobile.view.home.services.feedback.CustomerFeedbackScreen
import com.cuso.mobile.view.home.services.feedback.FeedbackDetailScreen
import com.cuso.mobile.view.home.services.service_order.ServiceOrderOverviewScreen
import com.cuso.mobile.view.home.services.service_order.ServiceOrderScreen
import com.cuso.mobile.view.home.services.service_request.CreateServiceRequest
import com.cuso.mobile.view.home.services.service_request.OrderDetails
import com.cuso.mobile.view.home.services.service_request.ServiceDetails
import com.cuso.mobile.view.home.services.service_request.ServiceRequestScreen
import com.cuso.mobile.view.home.services.service_request.ServiceRequetDetailsScreen
import com.cuso.mobile.view.home.services.service_status.delay_rework.DelayReworkTrackingScreen
import com.cuso.mobile.view.home.services.service_status.service_delivery.ServiceDeliveryStatusScreen
import com.cuso.mobile.view.home.services.service_status.status.ServiceStatusDetailScreen
import com.cuso.mobile.view.home.services.service_status.status.ServiceStatusScreen
import com.cuso.mobile.view.home.warehouse.WarehouseSettingsScreen
import com.cuso.mobile.viewmodel.*

@Composable
fun HomeScreenRouter(
    screen: String,
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    token: String,
    hrViewModel: HrViewModel,
    customerViewModel: CustomerViewModel,
    settingsViewModel: SettingsViewModel,
    authViewModel: Authenticate,
    // Navigation Callbacks
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenModulesPanel: (String) -> Unit,
    onShowComingSoon: (String) -> Unit,
    onSalesSettingsModeChange: (Boolean) -> Unit,
    // Screen States
    selectedCustomer: CustomerItem?,
    onCustomerSelected: (CustomerItem?) -> Unit,
    selectedOrderId: String?,
    onOrderIdSelected: (String?) -> Unit,
    editOrderId: String?,
    onEditOrderIdChange: (String?) -> Unit,
    selectedManagementOrderId: String?,
    onManagementOrderIdSelected: (String?) -> Unit,
    selectedLedgerAccountId: String?,
    selectedLedgerAccountName: String,
    onLedgerAccountSelected: (id: String?, name: String) -> Unit,
    selectedSupplier: SupplierRow?,
    onSupplierSelected: (SupplierRow?) -> Unit,
    selectedPaymentModeId: String?,
    onPaymentModeSelected: (String?) -> Unit,
    selectedInvoiceId: String?,
    onInvoiceSelected: (String?) -> Unit,
    selectedPurchaseInvoice: PurchaseInvoiceItem?,
    onPurchaseInvoiceSelected: (PurchaseInvoiceItem?) -> Unit,
    selectedInventoryItemId: String?,
    onInventoryItemIdSelected: (String?) -> Unit,
    selectedLowStockItem: LowStockItem?,
    onLowStockItemSelected: (LowStockItem?) -> Unit,
    selectedItemGroupId: String?,
    onItemGroupIdSelected: (String?) -> Unit,
    employeeScreenMode: ScreenMode,
    onEmployeeScreenModeChange: (ScreenMode) -> Unit,
    selectedEmployeeId: String?,
    onEmployeeIdSelected: (String?) -> Unit,
    selectedAttendanceId: String?,
    onAttendanceIdSelected: (String?) -> Unit,
    selectedFeedbackId: String?,
    onFeedbackIdSelected: (String?) -> Unit,
    selectedRecentCustomerId: String?,
    onRecentCustomerIdSelected: (String?) -> Unit,
    editingPricingId: String?,
    onEditingPricingIdChange: (String?) -> Unit,
    quotationScreenMode: String,
    onQuotationScreenModeChange: (String) -> Unit,
    pendingOrderReviewData: OrderReviewData?,
    onPendingOrderReviewDataChange: (OrderReviewData?) -> Unit,
    onOrderFlowOriginChange: (String?) -> Unit,
    onOrderSavedSuccessfully: (savedOrderId: String?) -> Unit
) {
    var isGarmentActive by remember { mutableStateOf(false) }
    var selectedSegmentForEdit by remember { mutableStateOf<SegmentItem?>(null) }

    // Persistent primitive states for garment detail navigation
    var selectedSegmentIdForDetail by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGarmentIdForDetail by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGarmentTitleForDetail by rememberSaveable { mutableStateOf("Garment Categories") }

    when (screen) {
        // ─────────────────────────────────────────────────────────────
        // 1. HOME & SETTINGS MODULE
        // ─────────────────────────────────────────────────────────────
        "home" -> HomeScreenContent(
            navController = navController,
            widthSizeClass = widthSizeClass,
            onNavigate = onSafeNavigate,
            onCustomerClick = { customerId ->
                if (customerId.isNotBlank()) {
                    onRecentCustomerIdSelected(customerId)
                    onNavigate("view_customer_recent")
                } else {
                    onNavigate("sales_customers")
                }
            }
        )
        "settings", "home_organization_profile" -> SettingsScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack
        )
        "home_branch_management" -> BranchSettingsScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack
        )
        "home_department_teams" -> DepartmentSettingsScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack
        )
        "home_designation" -> DesignationScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack
        )
        "home_role_management" -> RoleSettingsScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack
        )
        "home_opening_balance" -> OpeningBalancesScreen(
            navController = navController,
            onBack = onGoBack
        )
        "home_warehouse_management" -> WarehouseSettingsScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack
        )
        "profile-settings" -> ProfileSettingsScreen(
            onClose = onGoBack,
            onOrganizationSetup = { onSafeNavigate("settings_overview") },
            onBranchManagement = { onSafeNavigate("home_branch_management") },
            onGarmentType = { onSafeNavigate("sales_garment_type") },
            onGarmentPricing = { onSafeNavigate("sales_garment_pricing_setup") },
            onMarketing = { onShowComingSoon("Marketing Settings Coming Soon") },
            onFinance = { onSafeNavigate("finance_chart_of_accounts") },
            onInventory = { onSafeNavigate("inventory_items") },
            onLogistics = { onSafeNavigate("logistics_delivery") },
            onServices = { onSafeNavigate("services_service_status") },
            onHR = { onSafeNavigate("hr_all_employees") },
            onIT = { onShowComingSoon("IT Settings Coming Soon") },
            onLegal = { onShowComingSoon("Legal Settings Coming Soon") },
            onSecurity = { onShowComingSoon("Security Settings Coming Soon") },
            onReports = { onSafeNavigate("reports_sales") },
            onDepartment = { onSafeNavigate("home_department_teams") },
            onDesignation = { onSafeNavigate("home_designation") },
            onHelpSupport = { onSafeNavigate("home_warehouse_management") },
            onLogout = {
                settingsViewModel.logout {
                    authViewModel.logout {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        )
        "settings_overview" -> SettingsOverviewScreen(
            onClose = onGoBack,
            onNavigateToOrganizationSettings = { onSafeNavigate("home_organization_profile") },
            onNavigateToModuleSettings = { onSafeNavigate("module_settings") }
        )
        "module_settings" -> ModuleSettingsScreen(
            onClose = onGoBack,
            onConfigureHome = { onSafeNavigate("home_organization_profile") },
            onConfigureSales = { onSafeNavigate("sales_garment_type") },
            onConfigureSalesPricing = { onSafeNavigate("sales_garment_pricing_setup") },
            onConfigureFinance = { onSafeNavigate("finance_chart_of_accounts") },
            onConfigureInventory = { onSafeNavigate("inventory_items") },
            onConfigureLogistics = { onSafeNavigate("logistics_delivery") },
            onConfigureServices = { onSafeNavigate("services_service_status") },
            onConfigureHR = { onSafeNavigate("hr_all_employees") },
            onConfigureIT = { onShowComingSoon("IT Settings Coming Soon") },
            onConfigureLegal = { onShowComingSoon("Legal Settings Coming Soon") },
            onConfigureSecurity = { onShowComingSoon("Security Settings Coming Soon") },
            onConfigureReports = { onSafeNavigate("reports_sales") },
            onConfigureMarketing = {onShowComingSoon("Marketing Settings Coming Soon")},
            onNavigateToModule = {}
        )

        // ─────────────────────────────────────────────────────────────
        // 2. SALES MODULE
        // ─────────────────────────────────────────────────────────────
        "sales_lead" -> LeadScreenContent(
            onCreateLead = { onNavigate("create_lead") },
            onViewLead = { onNavigate("view_lead") },
            onEditLead = { onNavigate("edit_lead") },
            onClose = {
                onSalesSettingsModeChange(false)
                onGoBack()
            },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )
        "create_lead" -> LeadFormScreen(
            mode = LeadFormMode.CREATE,
            onBack = onGoBack
        )
        "view_lead" -> LeadFormScreen(
            mode = LeadFormMode.VIEW,
            onBack = onGoBack,
            onEditRequested = { onNavigate("edit_lead") }
        )
        "edit_lead" -> LeadFormScreen(
            mode = LeadFormMode.EDIT,
            onBack = onGoBack,
            onConvertToOrder = { orderReviewData ->
                onPendingOrderReviewDataChange(orderReviewData)
                onOrderFlowOriginChange("lead")
                onNavigate("create_order")
            }
        )
        "sales_customers" -> CustomerScreen(
            navController = navController,
            onClose = onGoBack,
            onCreateCustomer = { onNavigate("create_customer") },
            onView = { customer ->
                onCustomerSelected(customer)
                onNavigate("view_customer")
            },
            onEdit = { customer ->
                onCustomerSelected(customer)
                onNavigate("edit_customer")
            },
            onDelete = { customer -> customerViewModel.deleteCustomer(customer.id) },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )
        "view_customer", "edit_customer" -> {
            if (selectedCustomer != null && selectedCustomer.id.isNotBlank()) {
                CustomerDetailScreen(
                    navController = navController,
                    customerId = selectedCustomer.id,
                    startInEditMode = screen == "edit_customer",
                    onClose = onGoBack,
                    onUpdateSuccess = {
                        customerViewModel.refresh()
                        onGoBack()
                    },
                    onRequestEdit = { onNavigate("edit_customer") }
                )
            } else {
                onGoBack()
            }
        }
        "view_customer_recent" -> {
            if (!selectedRecentCustomerId.isNullOrBlank()) {
                CustomerDetailScreen(
                    navController = navController,
                    customerId = selectedRecentCustomerId,
                    startInEditMode = false,
                    onClose = {
                        onRecentCustomerIdSelected(null)
                        onGoBack()
                    },
                    onUpdateSuccess = {
                        customerViewModel.refresh()
                        onRecentCustomerIdSelected(null)
                        onGoBack()
                    },
                    onRequestEdit = { onNavigate("edit_customer") }
                )
            } else {
                onGoBack()
            }
        }
        "sales_measurements" -> MeasurementsScreen(
            navController = navController,
            onBack = onGoBack,
            onCreateOrder = { onNavigate("create_order") },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )
        "sales_sales_orders" -> SalesOrderScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack,
            onCreateOrder = {
                onPendingOrderReviewDataChange(null)
                onNavigate("create_order")
            },
            onViewOrder = { orderId ->
                onOrderIdSelected(orderId)
                onNavigate("order_overview")
            },
            onEditOrder = { orderId -> onEditOrderIdChange(orderId) },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )
        "create_order" -> CreateOrderScreen(
            initialData = pendingOrderReviewData,
            onBack = {
                onPendingOrderReviewDataChange(null)
                onGoBack()
            },
            onCancel = {
                onPendingOrderReviewDataChange(null)
                onGoBack()
            },
            onNextStep = { orderReviewData ->
                onPendingOrderReviewDataChange(orderReviewData)
                onNavigate("measurement_entry")
            }
        )
        "measurement_entry" -> {
            pendingOrderReviewData?.let { data ->
                MeasurementEntryScreen(
                    garmentType = data.garments.firstOrNull()?.categoryName ?: "Shirt",
                    customerName = data.fullName,
                    customerPhone = data.phone,
                    onClose = onGoBack,
                    onSaveMeasurement = {
                        onNavigate("order_preview")
                    }
                )
            } ?: run { onGoBack() }
        }
        "order_preview" -> {
            pendingOrderReviewData?.let { data ->
                OrderPreviewScreen(
                    orderData = data,
                    onClose = onGoBack,
                    onConfirmOrder = {
                        onOrderSavedSuccessfully(data.orderId)
                    },
                    onCancel = {
                        onPendingOrderReviewDataChange(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }
        "create_order_review" -> {
            pendingOrderReviewData?.let { data ->
                CreateOrderNextStep(
                    orderData = data,
                    onBack = { updatedData ->
                        onPendingOrderReviewDataChange(updatedData)
                        onGoBack()
                    },
                    onClose = onGoBack,
                    onSaveOrder = { _, savedOrderId ->
                        onOrderSavedSuccessfully(savedOrderId)
                    }
                )
            } ?: run { onGoBack() }
        }
        "order_overview" -> {
            selectedOrderId?.let { id ->
                OrderOverviewScreen(
                    orderId = id,
                    onClose = onGoBack,
                    onEditOrder = { reviewData ->
                        onPendingOrderReviewDataChange(reviewData)
                        onNavigate("create_order")
                    },
                    onCreateNew = {
                        onPendingOrderReviewDataChange(null)
                        onNavigate("create_order")
                    }
                )
            } ?: run { onGoBack() }
        }
        "sales_pricing_overview" -> PricingScreen(
            onClose = {
                onSalesSettingsModeChange(false)
                onGoBack()
            },
            onAddNewPricing = {
                onEditingPricingIdChange(null)
                onNavigate("create_garment_pricing")
            },
            onCardClick = { pricingId ->
                onEditingPricingIdChange(pricingId)
                onNavigate("create_garment_pricing")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )
        "garment_pricing_list" -> GarmentPricingListScreen(
            onBack = onGoBack,
            onAddNewPricing = {
                onEditingPricingIdChange(null)
                onNavigate("create_garment_pricing")
            },
            onCardClick = { pricingId ->
                onEditingPricingIdChange(pricingId)
                onNavigate("create_garment_pricing")
            }
        )
        "create_garment_pricing" -> AddGarmentPricingScreen(
            pricingId = editingPricingId,
            onClose = {
                onEditingPricingIdChange(null)
                onGoBack()
            },
            onSave = {
                onEditingPricingIdChange(null)
                onGoBack()
            }
        )
        "sales_pricing_quotation" -> QuotationScreen(
            onClose = {
                onSalesSettingsModeChange(false)
                onGoBack()
            },
            onAddNe = {
                onEditingPricingIdChange(null)
                onQuotationScreenModeChange("create")
                onNavigate("create_quotation")
            },
            onView = { id ->
                onEditingPricingIdChange(id)
                onQuotationScreenModeChange("view")
                onNavigate("create_quotation")
            },
            onEdit = { id ->
                onEditingPricingIdChange(id)
                onQuotationScreenModeChange("edit")
                onNavigate("create_quotation")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )
        "create_quotation" -> CreateQuotationScreen(
            quotationId = editingPricingId,
            mode = quotationScreenMode,
            onClose = onGoBack,
            onSave = onGoBack,
            token = token
        )
        "sales_payment_and_billing" -> PaymentListingScreen(
            navController = navController,
            widthSizeClass = widthSizeClass,
            onBack = {
                onSalesSettingsModeChange(false)
                onGoBack()
            },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") },
            onPaymentClick = { onNavigate("payment_detail") }
        )
        "payment_detail" -> PaymentInformationScreen(onClose = onGoBack)
        "sales_settings" -> SalesSettingsScreen(
            navController = navController,
            onClose = onGoBack,
            onAddSegmentClick = {
                selectedSegmentForEdit = null
                settingsViewModel.clearSelectedSegmentDetail()
                onNavigate("sales_add_segment")
            },
            onEditSegmentClick = { segment ->
                selectedSegmentForEdit = segment
                settingsViewModel.fetchSegmentById(segment.id)
                onNavigate("sales_add_segment")
            },
            onAddGarmentClick = { onNavigate("sales_add_garment") },
            onConfigureGarmentClick = { segmentId, garmentId, garmentTitle ->
                settingsViewModel.setSelectedGarmentForDetail(segmentId, garmentId, garmentTitle)
                onNavigate("sales_category_detail")
            },
            viewModel = settingsViewModel
        )

        "sales_garment_category_detail", "sales_category_detail" -> {
            val segmentId by settingsViewModel.selectedSegmentIdForStyle.collectAsStateWithLifecycle()
            val garmentId by settingsViewModel.selectedGarmentIdForStyle.collectAsStateWithLifecycle()
            val title by settingsViewModel.selectedGarmentTitleForStyle.collectAsStateWithLifecycle()

            GarmentCategoryDetailScreen(
                categoryTitle = title,
                segmentId = segmentId,
                garmentId = garmentId,
                onClose = onGoBack,
                onAddGarmentCategoryClick = {
                    onNavigate("sales_add_garment_category")
                },
                viewModel = settingsViewModel
            )
        }
        "sales_add_segment" -> AddSegmentScreen(
            segmentToEdit = selectedSegmentForEdit,
            viewModel = settingsViewModel,
            onClose = {
                selectedSegmentForEdit = null
                settingsViewModel.clearSelectedSegmentDetail()
                onGoBack()
            },
            onSegmentSaved = {
                selectedSegmentForEdit = null
                settingsViewModel.clearSelectedSegmentDetail()
                settingsViewModel.fetchSegments()
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────────────────────
        // 2. SALES MODULE - GARMENT SETTINGS
        // ─────────────────────────────────────────────────────────────
        "sales_garment_type" -> GarmentTypeContent(
            viewModel = settingsViewModel,
            onClose = {
                onSalesSettingsModeChange(false)
                onGoBack()
            },
            onAddSegmentClick = {
                selectedSegmentForEdit = null
                settingsViewModel.clearSelectedSegmentDetail()
                onNavigate("sales_add_segment")
            },
            onEditSegmentClick = { segment ->
                selectedSegmentForEdit = segment
                settingsViewModel.fetchSegmentById(segment.id)
                onNavigate("sales_add_segment")
            },
            onAddGarmentClick = { onNavigate("sales_add_garment") },
            onConfigureGarmentClick = { segmentId, garmentId, garmentTitle ->
                settingsViewModel.setSelectedGarmentForDetail(segmentId, garmentId, garmentTitle)
                onNavigate("sales_category_detail")
            }
        )

        // 2. Garment Profile Screen
        "sales_garment_profile" -> GarmentProfileConfigScreen(
            profileTitle = "Men's Shirt Profile",
            isActive = isGarmentActive,
            onClose = onGoBack,
            onAddExistingClick = { onNavigate("sales_add_existing_field") },
            onAddFieldClick = { onNavigate("sales_create_measurement_field") },
            onPreviewClick = { onNavigate("sales_configuration_preview") },
            onToggleActiveState = { isGarmentActive = it }
        )
        "sales_configuration_preview" -> ConfigurationPreviewScreen(
            garmentTitle = "Men's Shirt",
            onClose = onGoBack,
            onBackToEdit = onGoBack,
            onActivateConfirmed = {
                isGarmentActive = true
                onGoBack()
            }
        )

        // 3. Add Existing Field Screen
        "sales_add_existing_field" -> AddExistingFieldScreen(
            onClose = onGoBack,
            onAddSelected = { selectedFields ->
                onGoBack()
            },
            viewModel = settingsViewModel
        )

        // 4. Create Measurement Field Screen
        "sales_create_measurement_field" -> CreateMeasurementFieldScreen(
            onClose = onGoBack,
            onSave = { createdField ->
                // Return back after saving the measurement field
                onGoBack()
            },
            viewModel = settingsViewModel
        )

        "sales_add_garment" -> AddNewGarmentScreen(
            viewModel = settingsViewModel,
            onClose = onGoBack,
            onGarmentCreated = onGoBack
        )

        "sales_add_garment_category" -> AddNewGarmentCategoryScreen(
            onClose = onGoBack,
            onGarmentCategoryCreated = onGoBack,
            viewModel = settingsViewModel
        )

        // ─────────────────────────────────────────────────────────────
        // SALES PRICING SETUP SCREEN
        // ─────────────────────────────────────────────────────────────
        "sales_garment_pricing_setup" -> PricingSetupScreen(
            onClose = onGoBack,
            onAddGarmentPricing = { onNavigate("sales_add_garment_pricing") },
            onAddFabricPricing = { onNavigate("sales_add_fabric_pricing") },
            onAddWorkPricing = { onNavigate("sales_add_work_pricing") },
            onEditGarmentPricing = { garmentName ->
                onNavigate("sales_add_garment_pricing")
            }
        )

        "sales_add_garment_pricing" -> AddNewGarmentPricingScreen(
            onClose = onGoBack,
            onSaveSuccess = onGoBack
        )

        "sales_add_fabric_pricing" -> AddFabricPriceScreen(
            onClose = onGoBack,
            onSaveSuccess = onGoBack
        )

        "sales_add_work_pricing" -> AddWorkPricingScreen(
            onClose = onGoBack,
            onSaveSuccess = onGoBack
        )

        // ─────────────────────────────────────────────────────────────
        // 3. FINANCE MODULE
        // ─────────────────────────────────────────────────────────────
        "finance_sales_invoices" -> FinanceInvoiceScreen(
            onClose = onGoBack,
            onInvoiceClick = { invoice ->
                onInvoiceSelected(invoice.id)
                onNavigate("finance_invoice_detail")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Finance") }
        )
        "finance_invoice_detail" -> {
            selectedInvoiceId?.let { id ->
                InvoiceDetailScreen(
                    invoiceId = id,
                    onClose = {
                        onInvoiceSelected(null)
                        onGoBack()
                    },
                    token = token
                )
            } ?: run { onGoBack() }
        }
        "finance_purchase_invoices" -> PurchaseInvoiceScreen(
            onClose = onGoBack,
            onInvoiceClick = { invoice ->
                onPurchaseInvoiceSelected(invoice)
                onNavigate("finance_purchase_invoice_detail")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Finance") }
        )
        "finance_purchase_invoice_detail" -> {
            selectedPurchaseInvoice?.let { invoice ->
                PurchaseInvoiceDetailScreen(
                    invoiceId = invoice.id,
                    onClose = onGoBack
                )
            } ?: run { onGoBack() }
        }
        "finance_customers" -> FinanceCustomerScreen(
            onClose = onGoBack,
            onCustomerEdit = { customerId ->
                onRecentCustomerIdSelected(customerId)
                onNavigate("finance_customer_detail")
            },
            onCustomerClick = { customerId ->
                onRecentCustomerIdSelected(customerId)
                onNavigate("finance_customer_detail")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Finance") }
        )
        "finance_customer_detail" -> {
            selectedRecentCustomerId?.let { id ->
                CustomerDetailViewScreen(
                    customerId = id,
                    onClose = {
                        onRecentCustomerIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }
        "finance_suppliers" -> AllSuppliersScreen(
            onClose = onGoBack,
            onBreadCrumbClick = { onOpenModulesPanel("Finance") },
            onSupplierClick = { supplier ->
                onSupplierSelected(supplier)
                onNavigate("finance_supplier_detail")
            }
        )
        "finance_supplier_detail" -> {
            selectedSupplier?.let { supplier ->
                SupplierDetailScreen(
                    supplier = supplier,
                    onClose = {
                        onSupplierSelected(null)
                        onGoBack()
                    },
                    onBreadcrumbClick = { onOpenModulesPanel("Finance") }
                )
            } ?: run { onGoBack() }
        }
        "finance_expenses" -> ExpensesScreen(
            onClose = onGoBack,
            onBreadCrumbClick = { onOpenModulesPanel("Finance") }
        )
        "finance_chart_of_accounts" -> ChartOfAccountScreen(
            onClose = onGoBack,
            onBreadcrumbClick = { onOpenModulesPanel("Finance") }
        )
        "finance_journal_screen" -> ManualJournalEntryScreen(onClose = onGoBack)
        "finance_trial_balance" -> TrialBalanceScreen(
            onClose = onGoBack,
            onAccountClick = { accountId, accountName ->
                onLedgerAccountSelected(accountId, accountName)
                onNavigate("finance_ledger")
            },
            onBreadcrumbClick = { onOpenModulesPanel("Finance") }
        )
        "finance_ledger" -> {
            selectedLedgerAccountId?.let { id ->
                LedgerScreen(
                    accountId = id,
                    accountName = selectedLedgerAccountName,
                    onClose = {
                        onLedgerAccountSelected(null, "Ledger")
                        onGoBack()
                    },
                    onBreadcrumbClick = { onOpenModulesPanel("Finance") }
                )
            } ?: run { onGoBack() }
        }
        "finance_payments_received" -> AllPaymentScreen(
            onViewPayment = { onNavigate("payment_detail_screen") },
            onBreadCrumbClick = { onOpenModulesPanel("Finance") }
        )
        "payment_detail_screen" -> PaymentDetailScreenAR(onClose = onGoBack)
        "finance_payments_mode" -> AllPaymentListScreen(
            onClose = onGoBack,
            onPaymentClick = { paymentId ->
                onPaymentModeSelected(paymentId)
                onNavigate("payment_mode_detail")
            }
        )
        "payment_mode_detail" -> PaymentDetailScreenAP(
            onClose = {
                onPaymentModeSelected(null)
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────────────────────
        // 4. INVENTORY MODULE
        // ─────────────────────────────────────────────────────────────
        "inventory_items" -> InventoryScreen(
            onClose = onGoBack,
            onAddItem = { onNavigate("inventory_create_item") },
            onViewItem = { item ->
                onInventoryItemIdSelected(item._id)
                onNavigate("inventory_item_detail")
            },
            onEditItem = { onNavigate("inventory_create_item") },
            onBreadCrumbClick = { onOpenModulesPanel("Inventory") }
        )
        "inventory_create_item" -> CreateItemScreen(
            onDismiss = onGoBack,
            onItemCreated = onGoBack
        )
        "inventory_item_detail" -> {
            selectedInventoryItemId?.let { id ->
                val itemDetailViewModel: InventoryViewModel = hiltViewModel()
                val selectedItem by itemDetailViewModel.selectedItem.collectAsStateWithLifecycle()
                val isLoadingDetail by itemDetailViewModel.isLoadingItemDetail.collectAsStateWithLifecycle()
                val detailError by itemDetailViewModel.itemDetailError.collectAsStateWithLifecycle()

                LaunchedEffect(id) {
                    itemDetailViewModel.fetchInventoryItemDetail(id)
                }

                InventoryViewOne(
                    item = selectedItem,
                    isLoading = isLoadingDetail,
                    errorMessage = detailError,
                    onDismiss = {
                        onInventoryItemIdSelected(null)
                        onGoBack()
                    },
                    onAdjustStock = { },
                    onAdjustStockSubmit = { type, quantity, reason, notes ->
                        val apiType = when (type) {
                            AdjustmentType.INCREASE -> "increase"
                            AdjustmentType.DECREASE -> "decrease"
                            AdjustmentType.SET_EXACT -> "set"
                        }
                        itemDetailViewModel.adjustStock(
                            itemId = id,
                            adjustmentType = apiType,
                            quantity = quantity,
                            reason = reason,
                            notes = notes
                        )
                    },
                    onWarehouseTransfer = { },
                    onReorderStock = { },
                    onMarkInactive = { },
                    onEdit = { },
                    onShare = { }
                )
            } ?: run { onGoBack() }
        }
        "inventory_low_stock_alerts" -> LowStockAlertsScreen(
            onClose = onGoBack,
            onReorderClick = { item ->
                onLowStockItemSelected(item)
                onNavigate("inventory_create_purchase_order")
            },
            onCreateNewItem = { onNavigate("inventory_create_purchase_order") },
            onBreadcrumbClick = { onOpenModulesPanel("Inventory") }
        )
        "inventory_create_purchase_order" -> CreatePurchaseOrderScreen(
            onClose = {
                onLowStockItemSelected(null)
                onGoBack()
            },
            onCancel = {
                onLowStockItemSelected(null)
                onGoBack()
            },
            onCreateOrder = {
                onLowStockItemSelected(null)
                onGoBack()
                onGoBack()
            }
        )
        "inventory_item_groups" -> AllItemGroupScreen(
            onDismiss = onGoBack,
            onAddItemGroup = { onNavigate("inventory_create_item_group") },
            onView = { groupId -> onItemGroupIdSelected(groupId) },
            onEdit = { groupId ->
                onItemGroupIdSelected(groupId)
                onNavigate("inventory_create_item_group")
            },
            onDelete = { },
            onBreadCrumbClick = { onOpenModulesPanel("Inventory") }
        )
        "inventory_create_item_group" -> CreateItemGroupScreen(
            onDismiss = {
                onItemGroupIdSelected(null)
                onGoBack()
            },
            onSave = {
                onItemGroupIdSelected(null)
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────────────────────
        // 5. HR MODULE
        // ─────────────────────────────────────────────────────────────
        "hr_all_employees" -> AllEmployeesScreen(
            onDismiss = onGoBack,
            onAddEmployee = {
                onEmployeeScreenModeChange(ScreenMode.CREATE)
                onEmployeeIdSelected(null)
                onNavigate("hr_employee_onboarding")
            },
            onView = { employee ->
                onEmployeeScreenModeChange(ScreenMode.VIEW)
                onEmployeeIdSelected(employee._id)
                onNavigate("hr_employee_onboarding")
            },
            onEdit = { employee ->
                onEmployeeScreenModeChange(ScreenMode.EDIT)
                onEmployeeIdSelected(employee._id)
                onNavigate("hr_employee_onboarding")
            },
            onDelete = { },
            hrViewModel = hrViewModel,
            onBreadCrumbClick = { onOpenModulesPanel("HR") }
        )
        "hr_employee_onboarding" -> EmployeeOnboardingScreen(
            mode = employeeScreenMode,
            memberIdToLoad = selectedEmployeeId,
            onDismiss = {
                onEmployeeIdSelected(null)
                onGoBack()
            },
            onCreateEmployee = {
                onEmployeeIdSelected(null)
                onGoBack()
            },
            onUpdateEmployee = {
                onEmployeeIdSelected(null)
                onGoBack()
            },
            hrViewModel = hrViewModel
        )
        "hr_attendance" -> AttendanceScreen(
            onClose = onGoBack,
            onBreadCrumbClick = { onOpenModulesPanel("HR") },
            onRecordClick = { recordId ->
                onAttendanceIdSelected(recordId)
                onNavigate("hr_attendance_detail")
            }
        )
        "hr_attendance_detail" -> AttendanceDetailScreen(
            onClose = {
                onAttendanceIdSelected(null)
                onGoBack()
            },
            onBreadCrumbClick = { onOpenModulesPanel("HR") },
            onHistoryClick = { }
        )

        // ─────────────────────────────────────────────────────────────
        // 6. LOGISTICS MODULE
        // ─────────────────────────────────────────────────────────────
        "logistics_delivery" -> DeliveryManagementScreen(
            onDismiss = onGoBack,
            onView = { onNavigate("delivery_detail") },
            onBreadCrumbClick = { onOpenModulesPanel("Logistics") }
        )
        "delivery_detail" -> DeliveryDetailScreen(onDismiss = onGoBack)
        "logistics_order_tracking" -> OrderTrackingScreen(
            onClose = onGoBack,
            onViewOrder = { order ->
                onOrderIdSelected(order.id)
                onNavigate("tracking_overview")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Logistics") }
        )
        "tracking_overview" -> TrackingOverviewScreen(onClose = onGoBack)

        // ─────────────────────────────────────────────────────────────
        // 7. SERVICES MODULE
        // ─────────────────────────────────────────────────────────────
        "services_service_status", "sales_orders" -> ServiceStatusScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack,
            onViewOrder = { orderId ->
                onManagementOrderIdSelected(orderId)
                onNavigate("service_status_detail")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Services") }
        )
        "service_status_detail", "order_management_overview" -> {
            selectedManagementOrderId?.let { id ->
                ServiceStatusDetailScreen(
                    orderId = id,
                    onClose = {
                        onManagementOrderIdSelected(null)
                        onGoBack()
                    },
                    onEditOrder = { onEditOrderIdChange(id) }
                )
            } ?: run { onGoBack() }
        }
        "services_delay_rework" -> DelayReworkTrackingScreen(
            onClose = onGoBack,
            onLogNewDelay = { },
            onRegisterAlteration = { onNavigate("create_alteration") },
            onCreateReworkOrder = { onNavigate("create_order") },
            onViewLog = { },
            onGenerateReport = { }
        )
        "services_service_delivery" -> ServiceDeliveryStatusScreen(
            onClose = onGoBack,
            onContactCustomer = { },
            onEditDetails = { },
            onReschedule = { },
            onMarkDelivered = { },
            onViewUrgentOrder = { }
        )
        "services_service_order", "services_service_orders" -> ServiceOrderScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack,
            onCreateOrder = {
                onPendingOrderReviewDataChange(null)
                onNavigate("create_order")
            },
            onViewOrder = { orderId ->
                onOrderIdSelected(orderId)
                onNavigate("service_order_overview")
            },
            onEditOrder = { orderId -> onEditOrderIdChange(orderId) },
            onBreadCrumbClick = { onOpenModulesPanel("Services") }
        )
        "service_order_overview" -> {
            selectedOrderId?.let { id ->
                ServiceOrderOverviewScreen(
                    orderId = id,
                    onClose = onGoBack,
                    onEditOrder = { reviewData ->
                        onPendingOrderReviewDataChange(reviewData)
                        onNavigate("create_order")
                    },
                    onCreateNew = {
                        onPendingOrderReviewDataChange(null)
                        onNavigate("create_order")
                    }
                )
            } ?: run { onGoBack() }
        }
        "services_service_request" -> ServiceRequestScreen(
            onClose = { },
            onBreadcrumbClick = { },
            onCreateNewRequest = { onNavigate("create_request") },
            onViewClick = { onNavigate("review_services") }
        )
        "create_request" -> CreateServiceRequest()
        "review_services" -> ServiceRequetDetailsScreen(
            service = ServiceDetails(
                serviceRef = "SR-1045",
                reviewStatus = "Pending Review",
                service = "Bespoke Alteration",
                requestDate = "Oct 24, 2025",
                priority = "High",
                serviceCategory = "Suit Fitting & Adjustments",
                preferredCompletionDate = "Nov 15, 2023",
                serviceType = "Internal Production Refit",
                customerName = "Jonathan Sterling",
                phoneNumber = "+1 (555) 123-4567",
                emailAddress = "j.sterling@executive.com",
                shippingAddress = "452 Premium Way, Floor 12\nManhattan, NY 10001"
            ),
            order = OrderDetails(
                orderId = "#ORD-8829-23",
                status = "Completed",
                garmentItem = "Custom Charcoal 3-Piece Wool Suit",
                orderDate = "Sep 12, 2023",
                deliveryDate = "Oct 15, 2023",
                issueDescription = "The sleeves are approximately 2 inches too long...",
                internalNotes = "Check fabric elasticity before cutting...",
                attachmentCount = 3
            ),
            onBack = onGoBack,
            onViewFullOrderHistory = { onNavigate("order_history") }
        )
        "services_alteration_management" -> AlterationManagementScreen(
            onClose = onGoBack,
            onCreateNewAlteration = { onNavigate("create_alteration") },
            onBreadcrumbClick = { onOpenModulesPanel("Services") },
            onViewClick = { }
        )
        "create_alteration" -> CreateAlterationManagementScreen(onClose = onGoBack)
        "services_customer_feedback" -> CustomerFeedbackScreen(
            onDismiss = onGoBack,
            onView = { feedbackId ->
                onFeedbackIdSelected(feedbackId)
                onNavigate("feedback_detail")
            },
            onEdit = { },
            onDelete = { },
            onBreadCrumbClick = { onOpenModulesPanel("Services") }
        )
        "feedback_detail" -> FeedbackDetailScreen(
            onDismiss = {
                onFeedbackIdSelected(null)
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────────────────────
        // 8. REPORTS MODULE
        // ─────────────────────────────────────────────────────────────
        "reports_sales" -> SalesOrderReportsScreen(
            onClose = onGoBack,
            onBreadCrumbClick = { onOpenModulesPanel("Reports") }
        )
        "reports_inventory" -> InventoryReportPage(
            onClose = onGoBack,
            onBreadCrumbClick = { onOpenModulesPanel("Reports") },
            onReportClick = { route ->
                val implemented = setOf(
                    "reports_inventory_stock_summary", "reports_inventory_low_stock",
                    "reports_inventory_warehouse_report", "reports_inventory_purchase_report",
                    "reports_inventory_dead_stock"
                )
                if (route in implemented) onNavigate(route) else onShowComingSoon("Coming Soon, Stay tuned !")
            }
        )
        "reports_inventory_stock_summary" -> StockSummaryScreen(onClose = onGoBack)
        "reports_inventory_low_stock" -> LowStockScreen(onClose = onGoBack)
        "reports_inventory_warehouse_report" -> WarehouseReportScreen(onClose = onGoBack)
        "reports_inventory_purchase_report" -> PurchaseReportScreen(onClose = onGoBack)
        "reports_inventory_dead_stock" -> DeadStockReportScreen(onClose = onGoBack)
        "reports_finance" -> FinanceReportPage(
            onClose = onGoBack,
            onBreadCrumbClick = { onOpenModulesPanel("Reports") },
            onReportClick = { route ->
                if (route == "reports_finance_profit_and_loss_report") onNavigate(route)
                else onShowComingSoon("Coming Soon, Stay tuned !")
            }
        )
        "reports_finance_profit_and_loss_report" -> ProfitAndLossReportScreen(onClose = onGoBack)

        else -> {}
    }
}