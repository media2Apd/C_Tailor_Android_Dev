@file:Suppress("unused", "unusedVariable", "AssignedValueIsNeverRead", "SpellCheckingInspection", "VariableIsNeverRead",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.cuso.tailor.model.inventory.LowStockItemDto
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.StockLocationItemDto
import com.cuso.tailor.model.sales.CustomerItem
import com.cuso.tailor.model.sales.MeasurementItem
import com.cuso.tailor.model.sales.OrderOverviewData
import com.cuso.tailor.model.sales.OrderReviewData
import com.cuso.tailor.model.settings.ProductionTemplateDto
import com.cuso.tailor.model.settings.SegmentItem
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.home.hr.attendance_management.attendance.AttendanceListScreen
import com.cuso.tailor.view.home.branch.BranchSettingsScreen
import com.cuso.tailor.view.home.department.DepartmentSettingsScreen
import com.cuso.tailor.view.home.designation.DesignationScreen
import com.cuso.tailor.view.home.finance.account_payable.payment_mode.AllPaymentListScreen
import com.cuso.tailor.view.home.finance.account_payable.payment_mode.PaymentDetailScreenAR
import com.cuso.tailor.view.home.finance.account_payable.purchase_invoices.PurchaseInvoiceDetailScreen
import com.cuso.tailor.view.home.finance.account_payable.purchase_invoices.PurchaseInvoiceItem
import com.cuso.tailor.view.home.finance.account_payable.purchase_invoices.PurchaseInvoiceScreen
import com.cuso.tailor.view.home.finance.account_payable.suppliers.FinanceSupplierDetailScreen
import com.cuso.tailor.view.home.finance.account_payable.suppliers.SupplierRow
import com.cuso.tailor.view.home.finance.account_receivable.customers.CreateCustomerScreen
import com.cuso.tailor.view.home.finance.account_receivable.customers.CustomerDetailViewScreen
import com.cuso.tailor.view.home.finance.account_receivable.customers.FinanceCustomerScreen
import com.cuso.tailor.view.home.finance.account_receivable.payment_received.AllPaymentScreen
import com.cuso.tailor.view.home.finance.account_receivable.payment_received.PaymentDetailScreenAP
import com.cuso.tailor.view.home.finance.account_receivable.sales_invoice.CreateSalesInvoiceScreen
import com.cuso.tailor.view.home.finance.account_receivable.sales_invoice.FinanceInvoiceScreen
import com.cuso.tailor.view.home.finance.account_receivable.sales_invoice.InvoiceDetailScreen
import com.cuso.tailor.view.home.finance.receipts.CustomerReceiptsListScreen
import com.cuso.tailor.view.home.finance.expenses.ExpensesScreen
import com.cuso.tailor.view.home.finance.finance_core.chart_of_accounts.ChartOfAccountScreen
import com.cuso.tailor.view.home.finance.finance_core.journal_entry.ManualJournalEntryScreen
import com.cuso.tailor.view.home.finance.finance_core.trial_balance.LedgerScreen
import com.cuso.tailor.view.home.finance.finance_core.trial_balance.TrialBalanceScreen
import com.cuso.tailor.view.home.finance.settings.AddGstSettingsScreen
import com.cuso.tailor.view.home.finance.settings.AddTaxGroupScreen
import com.cuso.tailor.view.home.finance.settings.GstSettingsOverviewScreen
import com.cuso.tailor.view.home.finance.settings.TaxRatesScreen
import com.cuso.tailor.view.home.hr.attendance_management.attendance.AttendanceDetailScreen
import com.cuso.tailor.view.home.hr.attendance_management.employees.AllEmployeesScreen
import com.cuso.tailor.view.home.hr.attendance_management.employees.EmployeeOnboardingScreen
import com.cuso.tailor.view.home.hr.attendance_management.employees.ScreenMode
import com.cuso.tailor.view.home.hr.attendance_management.manual_entry.ManualAttendanceEntryScreen
import com.cuso.tailor.view.home.hr.attendance_management.monthly_calendar.AttendanceCalendarScreen
import com.cuso.tailor.view.home.hr.attendance_management.shift.CreateNewListScreen
import com.cuso.tailor.view.home.hr.attendance_management.shift.ShiftListOverviewScreen
import com.cuso.tailor.view.home.hr.document_management.document_category.AddDocumentCategoryScreen
import com.cuso.tailor.view.home.hr.document_management.document_category.DocumentCategoryListScreen
import com.cuso.tailor.view.home.hr.document_management.employee_document.EmployeeDocumentListScreen
import com.cuso.tailor.view.home.hr.document_management.employee_document.EmployeeDocumentUploadScreen
import com.cuso.tailor.view.home.hr.leave_management.leave_approval.LeaveApprovalScreen
import com.cuso.tailor.view.home.hr.leave_management.apply_leave.ApplyLeaveScreen
import com.cuso.tailor.view.home.hr.payroll_management.salary_components.SalaryComponentFormScreen
import com.cuso.tailor.view.home.hr.payroll_management.salary_components.SalaryComponentListScreen
import com.cuso.tailor.view.home.hr.payroll_management.salary_template.SalaryTemplateFormScreen
import com.cuso.tailor.view.home.hr.payroll_management.salary_template.SalaryTemplateListScreen
import com.cuso.tailor.view.home.hr.training_management.assign_training.AssignTrainingScreen
import com.cuso.tailor.view.home.hr.training_management.assign_training.TrainingAssignmentListScreen
import com.cuso.tailor.view.home.hr.training_management.training_program.AddEditProgramScreen
import com.cuso.tailor.view.home.hr.training_management.training_program.TrainingProgramListScreen
import com.cuso.tailor.view.home.inventory.approval.ApprovalDetailScreen
import com.cuso.tailor.view.home.inventory.approval.ApprovalsListScreen
import com.cuso.tailor.view.home.inventory.auto_reorder.AutoReorderScreen
import com.cuso.tailor.view.home.inventory.auto_reorder.CreateAutoReorderRuleScreen
import com.cuso.tailor.view.home.inventory.billing.AllBillsScreen
import com.cuso.tailor.view.home.inventory.billing.BillPdfPreviewScreen
import com.cuso.tailor.view.home.inventory.billing.NewVendorInvoiceScreen
import com.cuso.tailor.view.home.inventory.bulk_items.AddBulkItemScreen
import com.cuso.tailor.view.home.inventory.bulk_items.BulkDetailsScreen
import com.cuso.tailor.view.home.inventory.bulk_items.BulkListScreen
import com.cuso.tailor.view.home.inventory.items.adjustment.AdjustmentType
import com.cuso.tailor.view.home.inventory.items.adjustment.AllOrdersStockListScreen
import com.cuso.tailor.view.home.inventory.items.all_items.CreateItemScreen
import com.cuso.tailor.view.home.inventory.items.all_items.InventoryScreen
import com.cuso.tailor.view.home.inventory.items.all_items.InventoryViewOne
import com.cuso.tailor.view.home.inventory.items.item_groups.AllItemGroupScreen
import com.cuso.tailor.view.home.inventory.items.item_groups.CreateItemGroupScreen
import com.cuso.tailor.view.home.inventory.items.item_groups.ItemGroupDetailScreen
import com.cuso.tailor.view.home.inventory.items.transferorder.TransferOrdersStockListScreen
import com.cuso.tailor.view.home.inventory.multi_channel_management.allocation_list.AllocationListScreen
import com.cuso.tailor.view.home.inventory.multi_channel_management.basic_info.BasicInfoScreen
import com.cuso.tailor.view.home.inventory.multi_channel_management.category_listing.CategoryManagementScreen
import com.cuso.tailor.view.home.inventory.multi_channel_management.category_listing.CreateCategoryScreen
import com.cuso.tailor.view.home.inventory.payments_made.AllInventoryPaymentScreen
import com.cuso.tailor.view.home.inventory.payments_made.PaymentOverviewDetailScreen
import com.cuso.tailor.view.home.inventory.pricing_list.AllPricingScreen
import com.cuso.tailor.view.home.inventory.procurement.barcode.AllBarcodesScreen
import com.cuso.tailor.view.home.inventory.procurement.barcode.BarcodeGeneratorScreen
import com.cuso.tailor.view.home.inventory.procurement.billslist.AllBillListScreen
import com.cuso.tailor.view.home.inventory.procurement.billslist.PurchaseDetailScreen as PayablePurchaseDetailScreen
import com.cuso.tailor.view.home.inventory.procurement.billslist.RecordBillPaymentScreen
import com.cuso.tailor.view.home.inventory.procurement.credits.AllCreditsScreen
import com.cuso.tailor.view.home.inventory.procurement.location_management.AllStockLocationScreen
import com.cuso.tailor.view.home.inventory.procurement.location_management.StockLocationDetailsScreen
import com.cuso.tailor.view.home.inventory.procurement.location_management.StockLocationFormScreen
import com.cuso.tailor.view.home.inventory.procurement.orders.CreatePurchaseOrderScreen
import com.cuso.tailor.view.home.inventory.procurement.orders.LowStockAlertsScreen
import com.cuso.tailor.view.home.inventory.procurement.purchaseReceive.AllOrdersScreen
import com.cuso.tailor.view.home.inventory.procurement.purchaseReceive.NewBillScreen
import com.cuso.tailor.view.home.inventory.procurement.purchaseReceive.PreviewPdfScreen
import com.cuso.tailor.view.home.inventory.procurement.purchaseReceive.PurchaseDetailScreen as ReceivePurchaseDetailScreen
import com.cuso.tailor.view.home.inventory.procurement.purchase_order.POCreateScreen
import com.cuso.tailor.view.home.inventory.procurement.purchase_order.PODetailScreen
import com.cuso.tailor.view.home.inventory.procurement.purchase_order.POListScreen
import com.cuso.tailor.view.home.inventory.procurement.requisitions.AllRequisitionsScreen
import com.cuso.tailor.view.home.inventory.procurement.requisitions.CreateRequisitionScreen
import com.cuso.tailor.view.home.inventory.procurement.requisitions.RequisitionDetailScreen
import com.cuso.tailor.view.home.inventory.procurement.returns.AllReturnsScreen
import com.cuso.tailor.view.home.inventory.procurement.suppliers.InventorySupplierDetailScreen
import com.cuso.tailor.view.home.inventory.procurement.suppliers.InventorySuppliersScreen
import com.cuso.tailor.view.home.inventory.safety_stock.SafetyStockScreen
import com.cuso.tailor.view.home.inventory.settings.*
import com.cuso.tailor.view.home.logistics.delivery.DeliveryDetailScreen
import com.cuso.tailor.view.home.logistics.delivery.DeliveryManagementScreen
import com.cuso.tailor.view.home.logistics.order_tracking.OrderTrackingScreen
import com.cuso.tailor.view.home.logistics.order_tracking.TrackingOverviewScreen
import com.cuso.tailor.view.home.opening_balance.OpeningBalancesScreen
import com.cuso.tailor.view.home.profile_settings.ProfileSettingsScreen
import com.cuso.tailor.view.home.profile_settings.all_settings.ModuleSettingsScreen
import com.cuso.tailor.view.home.profile_settings.all_settings.OrganizationSettingsScreen
import com.cuso.tailor.view.home.profile_settings.all_settings.SettingsOverviewScreen
import com.cuso.tailor.view.home.profile_settings.setup_pages.BusinessSetupScreen
import com.cuso.tailor.view.home.profile_settings.setup_pages.SettingsScreen
import com.cuso.tailor.view.home.reports.finance.FinanceReportPage
import com.cuso.tailor.view.home.reports.finance.ProfitAndLossReportScreen
import com.cuso.tailor.view.home.reports.inventory.*
import com.cuso.tailor.view.home.reports.sales.SalesOrderReportsScreen
import com.cuso.tailor.view.home.role.RoleSettingsScreen
import com.cuso.tailor.view.home.sales.customer.*
import com.cuso.tailor.view.home.sales.dashboard.SalesDashboardScreen
import com.cuso.tailor.view.home.finance.receipts.CustomerReceiptDetailScreen
import com.cuso.tailor.view.home.sales.lead.*
import com.cuso.tailor.view.home.sales.measurements.MeasurementFieldsConfigScreen
import com.cuso.tailor.view.home.sales.measurements.MeasurementManagementScreen
import com.cuso.tailor.view.home.sales.measurements.MeasurementsAvailableDialog
import com.cuso.tailor.view.home.sales.measurements.MeasurementsScreen
import com.cuso.tailor.view.home.sales.oppertunity.oppertunities.CreateOpportunityScreen
import com.cuso.tailor.view.home.sales.oppertunity.oppertunities.OpportunitiesListScreen
import com.cuso.tailor.view.home.sales.oppertunity.oppertunities.OpportunityDetailScreen
import com.cuso.tailor.view.home.sales.oppertunity.oppertunity_pipeline.OpportunityPipelineScreen
import com.cuso.tailor.view.home.sales.payment_listing.*
import com.cuso.tailor.view.home.sales.pricing.*
import com.cuso.tailor.view.home.sales.quotation.*
import com.cuso.tailor.view.home.sales.sales_order.*
import com.cuso.tailor.view.home.sales.settings.garment.*
import com.cuso.tailor.view.home.sales.settings.garment.garment_category_detail.*
import com.cuso.tailor.view.home.sales.settings.measurement_list.MeasurementListScreen
import com.cuso.tailor.view.home.sales.settings.pricing_setup.*
import com.cuso.tailor.view.home.services.alteration_management.AlterationManagementScreen
import com.cuso.tailor.view.home.services.feedback.FeedbackDetailScreen
import com.cuso.tailor.view.home.services.service_order.production_orders.ServiceOrderOverviewScreen
import com.cuso.tailor.view.home.services.service_order.production_orders.ServiceOrderScreen
import com.cuso.tailor.view.home.services.service_request.CreateServiceRequest
import com.cuso.tailor.view.home.services.service_request.ServiceRequestDetailsScreen
import com.cuso.tailor.view.home.services.service_request.ServiceRequestScreen
import com.cuso.tailor.view.home.services.service_status.delay_rework.DelayReworkTrackingScreen
import com.cuso.tailor.view.home.services.service_status.service_delivery.ServiceDeliveryStatusScreen
import com.cuso.tailor.view.home.services.service_status.status.ServiceStatusDetailScreen
import com.cuso.tailor.view.home.services.service_status.status.ServiceStatusScreen
import com.cuso.tailor.view.home.services.settings.CreateServiceTemplateWizardScreen
import com.cuso.tailor.view.home.services.settings.ServiceTemplateDetailViewScreen
import com.cuso.tailor.view.home.services.settings.ServiceTemplateListScreen
import com.cuso.tailor.view.home.subscriptions.SubscriptionFlowContainer
import com.cuso.tailor.view.home.warehouse.WarehouseSettingsScreen
import com.cuso.tailor.model.finance.InvoiceViewOneData
import com.cuso.tailor.model.hr.ExpenseClaimDto
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.home.finance.account_receivable.sales_invoice.RecordPaymentReceiptScreen
import com.cuso.tailor.view.home.finance.credit.CreateCreditNoteScreen
import com.cuso.tailor.view.home.finance.credit.CreditNoteDetailScreen
import com.cuso.tailor.view.home.finance.dashboard.FinanceOverviewScreen
import com.cuso.tailor.view.home.finance.funds.bankAccounts.BankAccountDetailScreen
import com.cuso.tailor.view.home.finance.funds.bankAccounts.CreateBankAccountScreen
import com.cuso.tailor.view.home.finance.funds.bankAccounts.BankAccountsListScreen
import com.cuso.tailor.view.home.finance.funds.bankstatement.BankStatementDetailScreen
import com.cuso.tailor.view.home.finance.funds.bankstatement.BankStatementListScreen
import com.cuso.tailor.view.home.finance.funds.bankstatement.ImportBankStatementScreen
import com.cuso.tailor.view.home.finance.funds.cashbooks.CashBookDetailScreen
import com.cuso.tailor.view.home.finance.funds.cashbooks.CashBookListScreen
import com.cuso.tailor.view.home.finance.purchase.CreatePurchaseReturnScreen
import com.cuso.tailor.view.home.finance.purchase.FinanceReturnScreen
import com.cuso.tailor.view.home.finance.purchase.PurchaseReturnDetailScreen
import com.cuso.tailor.view.home.finance.purchase.VendorCreditListScreen
import com.cuso.tailor.view.home.finance.receipts.CreatePaymentReceiptScreen
import com.cuso.tailor.view.home.hr.dashboard.TeamOperationsDashboardScreen
import com.cuso.tailor.view.home.hr.organization_structure.OrganizationStructureScreen
import com.cuso.tailor.view.home.inventory.dashboard.InventoryDashboardScreen
import com.cuso.tailor.view.home.inventory.expense_management.approval.ExpenseClaimDetailScreen
import com.cuso.tailor.view.home.inventory.expense_management.approval.ExpenseManagementScreen
import com.cuso.tailor.view.home.inventory.expense_management.expense_type.AddExpenseTypeScreen
import com.cuso.tailor.view.home.inventory.expense_management.expense_type.ExpenseTypeScreen
import com.cuso.tailor.view.home.inventory.expense_management.new_claims.NewClaimsScreen
import com.cuso.tailor.view.home.inventory.expense_management.new_claims.SubmitExpenseClaimScreen
import com.cuso.tailor.view.home.inventory.inventory.packages.CreatePackageScreen
import com.cuso.tailor.view.home.inventory.inventory.packages.PackageDetailScreen
import com.cuso.tailor.view.home.inventory.inventory.packages.PackagesScreen
import com.cuso.tailor.view.home.inventory.inventory.picklist.CreatePicklistScreen
import com.cuso.tailor.view.home.inventory.inventory.picklist.PicklistDetailScreen
import com.cuso.tailor.view.home.inventory.inventory.picklist.PicklistListScreen
import com.cuso.tailor.view.home.inventory.pricing_list.NewPriceListScreen
import com.cuso.tailor.view.home.inventory.expense_management.finance_reimbursement.FinanceReimbursementScreen
import com.cuso.tailor.view.home.inventory.expense_management.finance_reimbursement.ProcessPaymentScreen
import com.cuso.tailor.view.home.inventory.expense_management.new_claims.EditExpenseClaimScreen
import com.cuso.tailor.view.home.legal.dashboard.LegalDashboardScreen
import com.cuso.tailor.view.home.legal.legal_compliance.CreateLegalComplianceScreen
import com.cuso.tailor.view.home.legal.legal_compliance.LegalComplianceDetailScreen
import com.cuso.tailor.view.home.legal.legal_compliance.LegalComplianceScreen
import com.cuso.tailor.view.home.legal.legal_contract.ContractOverviewScreen
import com.cuso.tailor.view.home.legal.legal_contract.CreateLegalContractScreen
import com.cuso.tailor.view.home.legal.legal_contract.LegalContractsScreen
import com.cuso.tailor.view.home.legal.legal_customer.CreateLegalCustomerScreen
import com.cuso.tailor.view.home.legal.legal_customer.LegalCustomerDetailScreen
import com.cuso.tailor.view.home.legal.legal_customer.LegalCustomerListScreen
import com.cuso.tailor.view.home.legal.legal_documents.CreateUpdateLegalDocumentScreen
import com.cuso.tailor.view.home.legal.legal_documents.LegalDocumentDetailScreen
import com.cuso.tailor.view.home.legal.legal_documents.LegalDocumentsScreen
import com.cuso.tailor.view.home.legal.legal_employee.LegalEmployeeCreateEditScreen
import com.cuso.tailor.view.home.legal.legal_employee.LegalEmployeeDetailScreen
import com.cuso.tailor.view.home.legal.legal_employee.LegalEmployeeListScreen
import com.cuso.tailor.view.home.legal.legal_license.CreateLegalLicenseScreen
import com.cuso.tailor.view.home.legal.legal_license.LegalLicenseDetailScreen
import com.cuso.tailor.view.home.legal.legal_license.LegalLicenseScreen
import com.cuso.tailor.view.home.services.alteration_management.ViewAlterationManagementScreen
import com.cuso.tailor.view.home.services.customer_complaint.ComplaintDetailViewScreen
import com.cuso.tailor.view.home.services.customer_complaint.ComplaintListScreen
import com.cuso.tailor.view.home.services.feedback.CustomerFeedbackScreen
import com.cuso.tailor.view.home.services.dashboard.ServicesDashboardScreen
import com.cuso.tailor.view.home.services.service_order.production_work.OrderProductionPipelineScreen
import com.cuso.tailor.view.home.services.service_order.production_work.ProductionWorksScreen
import com.cuso.tailor.view.home.services.service_order.production_work.QualityDeliveryDispatchScreen
import com.cuso.tailor.view.home.services.service_request.ReviewServiceRequestScreen
import com.cuso.tailor.viewmodel.*

private const val TEMP_PAYMENT_TERM_ID = "6ab206c595aeff058133df3d"
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun HomeScreenRouter(
    screen: String,
    selectedLegalEmployeeId: String? = null,
    onLegalEmployeeIdSelected: (String?) -> Unit = {},
    selectedLegalDocumentId: String? = null,
    onLegalDocumentIdSelected: (String?) -> Unit = {},
    selectedLegalLicenseId: String? = null,
    onLegalLicenseIdSelected: (String?) -> Unit = {},
    selectedLegalContractId: String? = null,
    onLegalContractIdSelected: (String?) -> Unit = {},
    selectedLegalComplianceId: String? = null,
    onLegalComplianceIdSelected: (String?) -> Unit = {},
    selectedComplaintId: String? = null,
    onComplaintIdSelected: (String?) -> Unit = {},
    selectedAlterationId: String? = null,
    onAlterationIdSelected: (String?) -> Unit = {},
    selectedClaimForPayment: ExpenseClaimDto? = null,
    onClaimForPaymentSelected: (ExpenseClaimDto?) -> Unit = {},
    selectedExpenseTypeId: String? = null,
    onExpenseTypeIdSelected: (String?) -> Unit = {},
    selectedExpenseClaimId: String? = null,
    onExpenseClaimIdSelected: (String?) -> Unit = {},
    selectedPriceListId: String? = null,
    onPriceListIdSelected: (String?) -> Unit = {},
    selectedPurchaseReturnId: String? = null,
    onPurchaseReturnIdSelected: (String?) -> Unit = {},
    selectedBankAccountId: String? = null,
    onBankAccountIdSelected: (String?) -> Unit = {},
    selectedCashShiftId: String? = null,
    selectedStatementId: String? = null,
    onStatementIdSelected: (String?) -> Unit = {},
    onCashShiftSelected: (String?) -> Unit = {},
    selectedCreditNoteId: String? = null,
    onCreditNoteSelected: (String?) -> Unit = {},
    selectedPackageIdForDetail: String? = null,
    onPackageIdForDetailSelected: (String?) -> Unit = {},
    selectedPackageIdForEdit: String? = null,
    onPackageIdForEditSelected: (String?) -> Unit = {},
    selectedPicklistId: String? = null,
    onPicklistIdSelected: (String?) -> Unit = {},
    selectedReceiptId: String? = null,
    onReceiptSelected: (String?) -> Unit = {},
    isOrderFromFinance: Boolean = false,
    invoiceForPayment: InvoiceViewOneData? = null,
    onInvoiceForPaymentChange: (InvoiceViewOneData?) -> Unit = {},
    onOrderFromFinanceChange: (Boolean) -> Unit = {},
    prefillOrderForInvoice: OrderOverviewData? = null,
    onPrefillOrderForInvoiceChange: (OrderOverviewData?) -> Unit = {},
    selectedDocumentCategoryId: String? = null,
    onDocumentCategoryIdSelected: (String?) -> Unit = {},
    selectedDocumentId: String? = null,
    onDocumentIdSelected: (String?) -> Unit = {},
    selectedCategoryIdForEdit: String? = null,
    selectedSalaryComponentId: String? = null,
    selectedTrainingProgramId: String? = null,
    onTrainingProgramIdSelected: (String?) -> Unit = {},
    selectedTrainingAssignmentId: String? = null,
    onTrainingAssignmentIdSelected: (String?) -> Unit = {},
    selectedSalaryTemplateId: String? = null,
    onSalaryTemplateIdSelected: (String?) -> Unit = {},
    onSalaryComponentIdSelected: (String?) -> Unit = {},
    onCategoryIdForEditSelected: (String?) -> Unit = {},
    selectedApprovalId: String? = null,
    onApprovalIdSelected: (String?) -> Unit = {},
    selectedShiftId: String? = null,
    onShiftIdSelected: (String?) -> Unit = {},
    selectedServiceRequestId: String? = null,
    onServiceRequestIdSelected: (String?) -> Unit = {},
    selectedAssignmentJobCardId: String? = null,
    onAssignmentJobCardIdSelected: (String?) -> Unit = {},
    selectedOpportunityId: String? = null,
    onOpportunityIdSelected: (String?) -> Unit = {},
    selectedMeasurementItem: MeasurementItem? = null,
    selectedServiceTemplate: ProductionTemplateDto? = null,
    onServiceTemplateSelected: (ProductionTemplateDto?) -> Unit = {},
    onMeasurementItemSelected: (MeasurementItem?) -> Unit = {},
    selectedBarcodeIdForDetail: String? = null,
    onBarcodeIdForDetailSelected: (String?) -> Unit = {},
    selectedSupplier: SupplierRow?,
    selectedReceivePoId: String? = null,
    onReceivePoIdSelected: (String?) -> Unit = {},
    onFinanceSupplierSelected: (SupplierRow?) -> Unit,
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    selectedPurchaseOrderForDetail: PurchaseOrder?,
    onPurchaseOrderForDetailSelected: (PurchaseOrder?) -> Unit,
    selectedRequisitionIdForDetail: String?,
    onRequisitionIdForDetailSelected: (String?) -> Unit,
    token: String,
    selectedBulkItemId: String?,
    onBulkItemIdSelected: (String?) -> Unit,
    hrViewModel: HrViewModel,
    servicesViewModel: ServicesViewModel,
    customerViewModel: CustomerViewModel,
    settingsViewModel: SettingsViewModel,
    salesViewModel: SalesViewModel,
    branchViewModel: BranchViewModel,
    authViewModel: Authenticate,
    inventoryViewModel: InventoryViewModel,
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    // Pops the stack back to a specific screen in one step
    onPopToScreen: (String) -> Unit = {},
    onOpenDrawer: () -> Unit,
    onOpenModulesPanel: (String) -> Unit,
    onShowComingSoon: (String) -> Unit,
    onSalesSettingsModeChange: (Boolean) -> Unit,
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
    selectedSupplierId: String?,
    onSupplierSelected: (String?) -> Unit,
    selectedPaymentModeId: String?,
    onPaymentModeSelected: (String?) -> Unit,
    selectedInvoiceId: String?,
    onInvoiceSelected: (String?) -> Unit,
    selectedPurchaseInvoice: PurchaseInvoiceItem?,
    onPurchaseInvoiceSelected: (PurchaseInvoiceItem?) -> Unit,
    selectedInventoryItemId: String?,
    onInventoryItemIdSelected: (String?) -> Unit,
    selectedLowStockItem: LowStockItemDto?,
    onLowStockItemSelected: (LowStockItemDto?) -> Unit,
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
    selectedLegalCustomerId: String?,
    onLegalCustomerIdSelected: (String?) -> Unit,
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
    when (screen) {
        "home", "settings", "home_organization_profile", "home_branch_management",
        "home_department_teams", "home_designation", "home_role_management",
        "home_opening_balance", "home_warehouse_management", "profile-settings",
        "settings_overview", "manage_subscription", "module_settings",
        "organization_settings", "home_business_setup" -> {
            HomeAndSettingsRouter(
                screen = screen,
                navController = navController,
                widthSizeClass = widthSizeClass,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel,
                onNavigate = onNavigate,
                onSafeNavigate = onSafeNavigate,
                onGoBack = onGoBack,
                onOpenDrawer = onOpenDrawer,
                onRecentCustomerIdSelected = onRecentCustomerIdSelected,
                onShowComingSoon = onShowComingSoon
            )
        }

        "inventory_allocation_rules", "inventory_create_allocation", "inventory_pdf_templates",
        "inventory_location_structure", "inventory_floor_overview", "inventory_add_floor",
        "inventory_section_overview", "inventory_add_section", "inventory_rack_overview",
        "inventory_add_rack", "inventory_bin_overview", "inventory_add_bin",
        "inventory_auto_reorder_settings"  -> {
            InventoryStructureRouter(
                screen = screen,
                settingsViewModel = settingsViewModel,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onShowComingSoon = onShowComingSoon
            )
        }

        // In HomeScreenRouter: Add opportunity routes to SalesRouter block
        "sales_dashboard", "sales_lead", "create_lead", "view_lead", "edit_lead", "convert_lead_to_opportunity", "sales_customers",
        "create_customer", "view_customer", "edit_customer", "view_customer_recent",
        "sales_measurements", "measurements_available_view", "measurement_management_view",
        "measurement_fields_config", "sales_sales_orders", "create_order",
        "measurement_entry", "order_preview", "create_order_review", "order_overview",
        "sales_pricing_overview", "garment_pricing_list", "create_garment_pricing",
        "sales_pricing_quotation", "create_quotation","view_quotation", "sales_payment_and_billing",
        "payment_detail", "sales_settings", "sales_category_detail", "sales_add_segment",
        "sales_garment_type", "sales_garment_profile", "sales_configuration_preview",
        "sales_add_existing_field", "sales_create_measurement_field", "sales_add_garment",
        "sales_add_garment_category", "sales_garment_pricing_setup", "sales_add_garment_pricing",
        "sales_add_fabric_pricing", "sales_add_work_pricing", "sales_measurement_list",
        "sales_opportunity_pipeline", "sales_opportunities", "create_opportunity", "opportunity_detail" -> {
            SalesRouter(
                screen = screen,
                isOrderFromFinance = isOrderFromFinance,
                onOrderFromFinanceChange = onOrderFromFinanceChange,
                prefillOrderForInvoice = prefillOrderForInvoice,
                onPrefillOrderForInvoiceChange = onPrefillOrderForInvoiceChange,
                selectedOpportunityId = selectedOpportunityId,
                onOpportunityIdSelected = onOpportunityIdSelected,
                navController = navController,
                widthSizeClass = widthSizeClass,
                selectedMeasurementItem = selectedMeasurementItem,
                onMeasurementItemSelected = onMeasurementItemSelected,
                token = token,
                customerViewModel = customerViewModel,
                settingsViewModel = settingsViewModel,
                selectedCustomer = selectedCustomer,
                onCustomerSelected = onCustomerSelected,
                selectedOrderId = selectedOrderId,
                onOrderIdSelected = onOrderIdSelected,
                editOrderId = editOrderId,
                onEditOrderIdChange = onEditOrderIdChange,
                selectedRecentCustomerId = selectedRecentCustomerId,
                onRecentCustomerIdSelected = onRecentCustomerIdSelected,
                editingPricingId = editingPricingId,
                onEditingPricingIdChange = onEditingPricingIdChange,
                quotationScreenMode = quotationScreenMode,
                onQuotationScreenModeChange = onQuotationScreenModeChange,
                pendingOrderReviewData = pendingOrderReviewData,
                onPendingOrderReviewDataChange = onPendingOrderReviewDataChange,
                onOrderFlowOriginChange = onOrderFlowOriginChange,
                onOrderSavedSuccessfully = onOrderSavedSuccessfully,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onOpenDrawer = onOpenDrawer,
                onOpenModulesPanel = onOpenModulesPanel,
                onSalesSettingsModeChange = onSalesSettingsModeChange
            )
        }

        // ── Add "finance_credits", "finance_create_credit", "finance_credit_detail" into this list ──
        "finance_dashboard", "finance_receipts", "finance_create_payment_receipt", "finance_credits",
        "finance_vendor_credits",
        "finance_create_credit", "finance_credit_detail", "finance_sales_invoices", "finance_invoice_detail",
        "finance_record_payment", "finance_purchase_invoices", "finance_purchase_invoice_detail",
        "finance_customers", "finance_customer_detail", "finance_suppliers", "finance_supplier_detail",
        "finance_expenses", "finance_chart_of_accounts", "finance_journal_screen", "finance_trial_balance",
        "finance_ledger", "finance_payments_received", "payment_detail_screen", "finance_payments_mode",
        "payment_mode_detail", "finance_gst_settings", "finance_create_customer", "finance_add_gst",
        "finance_tax_rates", "finance_add_tax_group", "finance_create_sales_invoice", "finance_purchase_orders",
        "finance_purchase_receive", "finance_receipt_detail", "finance_bills_list", "finance_payments_made",
        "finance_purchase_returns", "finance_purchase_return_detail", "finance_create_purchase_return",
        "finance_sales_orders", "finance_cash_book", "finance_cash_book_detail",
        "finance_bank_accounts", "finance_create_bank_account", "finance_bank_account_detail",
        "finance_bank_statements", "finance_bank_statement_detail", "finance_import_bank_statement" -> {
            FinanceRouter(
                screen = screen,
                selectedPurchaseReturnId = selectedPurchaseReturnId,
                onPurchaseReturnIdSelected = onPurchaseReturnIdSelected,
                selectedStatementId = selectedStatementId,
                onStatementIdSelected = onStatementIdSelected,
                selectedBankAccountId = selectedBankAccountId,
                onBankAccountIdSelected = onBankAccountIdSelected,
                selectedCashShiftId = selectedCashShiftId,
                onCashShiftSelected = onCashShiftSelected,
                selectedCreditNoteId = selectedCreditNoteId,
                onCreditNoteSelected = onCreditNoteSelected,
                isOrderFromFinance = isOrderFromFinance,
                onOrderFromFinanceChange = onOrderFromFinanceChange,
                prefillOrderForInvoice = prefillOrderForInvoice,
                onPrefillOrderForInvoiceChange = onPrefillOrderForInvoiceChange,
                token = token,
                settingsViewModel = settingsViewModel,
                salesViewModel = salesViewModel,
                branchViewModel = branchViewModel,
                invoiceForPayment = invoiceForPayment,
                onInvoiceForPaymentChange = onInvoiceForPaymentChange,
                selectedSupplier = selectedSupplier,
                inventoryViewModel = inventoryViewModel,
                customerViewModel = customerViewModel,
                selectedLedgerAccountId = selectedLedgerAccountId,
                selectedLedgerAccountName = selectedLedgerAccountName,
                onLedgerAccountSelected = onLedgerAccountSelected,
                selectedSupplierId = selectedSupplierId,
                onSupplierSelected = onSupplierSelected,
                selectedPaymentModeId = selectedPaymentModeId,
                onPaymentModeSelected = onPaymentModeSelected,
                selectedInvoiceId = selectedInvoiceId,
                onInvoiceSelected = onInvoiceSelected,
                selectedReceiptId = selectedReceiptId,
                onReceiptSelected = onReceiptSelected,
                selectedPurchaseInvoice = selectedPurchaseInvoice,
                onPurchaseInvoiceSelected = onPurchaseInvoiceSelected,
                selectedRecentCustomerId = selectedRecentCustomerId,
                onRecentCustomerIdSelected = onRecentCustomerIdSelected,
                selectedReceivePoId = selectedReceivePoId,
                onReceivePoIdSelected = onReceivePoIdSelected,
                selectedPurchaseOrderForDetail = selectedPurchaseOrderForDetail,
                onPurchaseOrderForDetailSelected = onPurchaseOrderForDetailSelected,
                navController = navController,
                onOpenDrawer = onOpenDrawer,
                onPendingOrderReviewDataChange = onPendingOrderReviewDataChange,
                onOrderIdSelected = onOrderIdSelected,
                onEditOrderIdChange = onEditOrderIdChange,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onOpenModulesPanel = onOpenModulesPanel
                )
        }

        "inventory_dashboard", "inventory_items", "inventory_all_items", "inventory_create_item",
        "inventory_item_detail", "inventory_item_groups", "inventory_item_group_detail",
        "inventory_create_item_group", "inventory_bulk", "inventory_all_bulk",
        "inventory_bulk_detail", "inventory_add_bulk", "inventory_adjustment",
        "inventory_adjustments", "inventory_transfer_order", "inventory_transfer_stock",
        "inventory_items_transfer_stock", "inventory_stock_location",
        "inventory_location_management", "inventory_procurement_location_management",
        "inventory_stock_location_details", "inventory_stock_location_form",
        "inventory_safety_stock", "inventory_auto_reorder","inventory_auto_reorder_detail",
        "inventory_multichannel_basic_info",
        "inventory_multichannel_allocation_list",
        "inventory_multichannel_category_listing", "inventory_category_listing",
        "inventory_approvals", "inventory_pricing_list", "inventory_new_pricing", "inventory_approval_detail", "inventory_create_category" -> {
            InventoryMainRouter(
                screen = screen,
                selectedPriceListId = selectedPriceListId,
                onPriceListIdSelected = onPriceListIdSelected,
                selectedApprovalId = selectedApprovalId,
                onApprovalIdSelected = onApprovalIdSelected,
                selectedInventoryItemId = selectedInventoryItemId,
                onInventoryItemIdSelected = onInventoryItemIdSelected,
                selectedItemGroupId = selectedItemGroupId,
                onItemGroupIdSelected = onItemGroupIdSelected,
                selectedBulkItemId = selectedBulkItemId,
                onBulkItemIdSelected = onBulkItemIdSelected,
                // Pass category edit parameters here
                selectedCategoryIdForEdit = selectedCategoryIdForEdit,
                onCategoryIdForEditSelected = onCategoryIdForEditSelected,
                inventoryViewModel = inventoryViewModel,
                settingsViewModel = settingsViewModel,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onOpenModulesPanel = onOpenModulesPanel,
                onShowComingSoon = onShowComingSoon
            )
        }

        "inventory_suppliers", "inventory_supplier_detail", "inventory_requisitions",
        "inventory_procurement_purchase_request", "inventory_procurement_requisitions",
        "inventory_requisition_detail", "inventory_create_requisition",
        "inventory_purchase_orders", "inventory_procurement_purchase_order",
        "inventory_procurement_purchase_orders", "inventory_procurement_orders",
        "inventory_purchase_order_detail_flow", "inventory_create_purchase_order_flow",
        "inventory_purchase_receive", "inventory_procurement_purchase_receive",
        "inventory_purchase_receive_detail", "inventory_purchase_new_bill",
        "inventory_purchase_receive_preview_pdf", "inventory_all_bills",
        "inventory_procurement_bill_list", "inventory_procurement_bills_list",
        "inventory_bills_list", "inventory_bill_list", "inventory_payable_purchase_detail",
        "inventory_record_bill_payment", "inventory_barcode", "inventory_procurement_barcode",
        "inventory_procurement_barcode_list", "inventory_all_barcodes",
        "inventory_create_barcode", "inventory_low_stock_alerts", "inventory_low_stock_alert",
        "inventory_create_purchase_order", "inventory_payables_payments",
        "inventory_payments_made", "inventory_payment_overview_detail",
        "inventory_credits", "inventory_procurement_credits", "inventory_payables_credits",
        "inventory_returns", "inventory_procurement_returns", "inventory_billing",
        "inventory_bill_preview", "inventory_create_bill",
        "inventory_picklist", "inventory_picklist_detail", "inventory_create_package", "inventory_package_detail", "inventory_create_picklist", "inventory_packages" -> {
            InventoryProcurementRouter(
                screen = screen,
                selectedPackageIdForDetail = selectedPackageIdForDetail,
                onPackageIdForDetailSelected = onPackageIdForDetailSelected,
                selectedPackageIdForEdit = selectedPackageIdForEdit,
                onPackageIdForEditSelected = onPackageIdForEditSelected,
                selectedPicklistId = selectedPicklistId,
                onPicklistIdSelected = onPicklistIdSelected,
                selectedBarcodeIdForDetail = selectedBarcodeIdForDetail,
                onBarcodeIdForDetailSelected = onBarcodeIdForDetailSelected,
                selectedReceivePoId = selectedReceivePoId,
                onReceivePoIdSelected = onReceivePoIdSelected,
                selectedPurchaseOrderForDetail = selectedPurchaseOrderForDetail,
                onPurchaseOrderForDetailSelected = onPurchaseOrderForDetailSelected,
                selectedRequisitionIdForDetail = selectedRequisitionIdForDetail,
                onRequisitionIdForDetailSelected = onRequisitionIdForDetailSelected,
                selectedSupplierId = selectedSupplierId,
                onSupplierSelected = onSupplierSelected,
                selectedInvoiceId = selectedInvoiceId,
                onInvoiceSelected = onInvoiceSelected,
                selectedPaymentModeId = selectedPaymentModeId,
                onPaymentModeSelected = onPaymentModeSelected,
                selectedLowStockItem = selectedLowStockItem,
                onLowStockItemSelected = onLowStockItemSelected,
                inventoryViewModel = inventoryViewModel,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onOpenModulesPanel = onOpenModulesPanel
            )
        }

        "hr_dashboard","hr_all_employees", "hr_employee_onboarding", "hr_attendance",
        "hr_attendance_detail", "hr_monthly_attendance", "hr_daily_attendance",
        "hr_attendance_manual_entry", "hr_attendance_shift_management","hr_payroll_salary_structure",
        "hr_payroll_salary_components",
        "hr_shift_list",
        "hr_create_shift","hr_payroll_salary_components_create",
        "hr_leave_approval",
        "hr_apply_leave",
        "hr_payroll_salary_templates",
        "hr_payroll_salary_templates_form",
        "hr_training_management", "hr_add_edit_program",
        "hr_training_assignment_list", "hr_assign_training_form",
        "hr_document_categories",
        "hr_add_document_category",
        "hr_employee_document_upload",
        "hr_new_document_upload", "hr_organization_structure",
        "hr_expense_claims","hr_expense_new_claim", "hr_expense_type", "hr_submit_expense_claim", "hr_expense_claim_detail","hr_expense_claim_detail_readonly",
        "hr_expense_finance_reimbursement","hr_edit_expense_claim","hr_add_expense_type","hr_expense_process_payment"   -> {
            HrRouter(
                screen = screen,
                selectedClaimForPayment = selectedClaimForPayment,
                onClaimForPaymentSelected = onClaimForPaymentSelected,
                selectedExpenseTypeId = selectedExpenseTypeId,
                onExpenseTypeIdSelected = onExpenseTypeIdSelected,
                selectedExpenseClaimId = selectedExpenseClaimId,
                onExpenseClaimIdSelected = onExpenseClaimIdSelected,
                selectedDocumentCategoryId = selectedDocumentCategoryId,
                onDocumentCategoryIdSelected = onDocumentCategoryIdSelected,
                selectedDocumentId = selectedDocumentId,
                onDocumentIdSelected = onDocumentIdSelected,
                hrViewModel = hrViewModel,
                employeeScreenMode = employeeScreenMode,
                onEmployeeScreenModeChange = onEmployeeScreenModeChange,
                selectedEmployeeId = selectedEmployeeId,
                selectedTrainingAssignmentId = selectedTrainingAssignmentId,
                onTrainingAssignmentIdSelected = onTrainingAssignmentIdSelected,
                selectedSalaryTemplateId = selectedSalaryTemplateId,
                onSalaryTemplateIdSelected = onSalaryTemplateIdSelected,
                selectedTrainingProgramId = selectedTrainingProgramId,
                onTrainingProgramIdSelected = onTrainingProgramIdSelected,
                onEmployeeIdSelected = onEmployeeIdSelected,
                selectedSalaryComponentId = selectedSalaryComponentId,
                onSalaryComponentIdSelected = onSalaryComponentIdSelected,
                selectedAttendanceId = selectedAttendanceId,
                onAttendanceIdSelected = onAttendanceIdSelected,
                selectedShiftId = selectedShiftId,
                onShiftIdSelected = onShiftIdSelected,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onOpenModulesPanel = onOpenModulesPanel
            )
        }

        "logistics_delivery", "delivery_detail", "logistics_order_tracking",
        "tracking_overview" -> {
            LogisticsRouter(
                screen = screen,
                onOrderIdSelected = onOrderIdSelected,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onOpenModulesPanel = onOpenModulesPanel
            )
        }

        // ── Legal Module Router ──
        "legal_dashboard", "legal_contracts", "legal_contract_detail", "legal_create_contract", "legal_compliance",
        "legal_compliance_detail", "legal_create_compliance", "legal_documents", "legal_document_detail", "legal_create_document",
        "legal_licenses", "legal_license_detail", "legal_create_license",
        "legal_customer", "legal_customer_detail", "legal_create_customer_record",
        "legal_employee", "legal_employee_detail", "legal_create_employee" -> {
            LegalRouter(
                screen = screen,
                selectedContractId = selectedLegalContractId,
                onContractIdSelected = onLegalContractIdSelected,
                selectedComplianceId = selectedLegalComplianceId,
                onComplianceIdSelected = onLegalComplianceIdSelected,
                selectedLegalDocumentId = selectedLegalDocumentId,
                onLegalDocumentIdSelected = onLegalDocumentIdSelected,
                selectedLegalLicenseId = selectedLegalLicenseId,
                onLegalLicenseIdSelected = onLegalLicenseIdSelected,
                selectedLegalCustomerId = selectedLegalCustomerId,
                onLegalCustomerIdSelected = onLegalCustomerIdSelected,
                selectedLegalEmployeeId = selectedLegalEmployeeId,
                onLegalEmployeeIdSelected = onLegalEmployeeIdSelected,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                hrViewModel = hrViewModel,
                branchViewModel = branchViewModel
            )
        }

        // Added "services_production_orders" and "services_service_orders" to the screen list
        "services_dashboard", "services_service_status", "sales_orders", "service_status_detail",
        "order_management_overview", "services_delay_rework", "services_service_delivery",
        "services_production_orders", "services_service_orders", "services_service_order",
        "service_order_overview", "services_service_request",
        "create_request", "review_services", "services_alteration_management",
        "create_alteration", "view_alteration", "services_customer_complaints",
        "services_customer_feedback", "feedback_detail",
        "services_service_templates", "services_create_service_template",
        "services_production_works",
        "services_view_service_template", "review_service_request", "complaint_detail",
        "services_production_pipeline", "services_qc_delivery", "services_assignment_mode" -> {
            ServicesRouter(
                screen = screen,
                servicesViewModel = servicesViewModel,
                selectedAssignmentJobCardId = selectedAssignmentJobCardId,
                onAssignmentJobCardIdSelected = onAssignmentJobCardIdSelected,
                selectedComplaintId = selectedComplaintId,
                onComplaintIdSelected = onComplaintIdSelected,
                selectedAlterationId = selectedAlterationId,
                onAlterationIdSelected = onAlterationIdSelected,
                selectedServiceRequestId = selectedServiceRequestId,
                onServiceRequestIdSelected = onServiceRequestIdSelected,
                navController = navController,
                selectedManagementOrderId = selectedManagementOrderId,
                onManagementOrderIdSelected = onManagementOrderIdSelected,
                selectedOrderId = selectedOrderId,
                onOrderIdSelected = onOrderIdSelected,
                onEditOrderIdChange = onEditOrderIdChange,
                selectedFeedbackId = selectedFeedbackId,
                onFeedbackIdSelected = onFeedbackIdSelected,
                selectedServiceTemplate = selectedServiceTemplate,
                onServiceTemplateSelected = onServiceTemplateSelected,
                pendingOrderReviewData = pendingOrderReviewData,
                onPendingOrderReviewDataChange = onPendingOrderReviewDataChange,
                // inside the services block
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onPopToScreen = onPopToScreen,
                onOpenDrawer = onOpenDrawer,
                onOpenModulesPanel = onOpenModulesPanel,
                hrViewModel = hrViewModel
            )
        }

        "reports_sales", "reports_inventory", "reports_inventory_stock_summary",
        "reports_inventory_low_stock", "reports_inventory_warehouse_report",
        "reports_inventory_purchase_report", "reports_inventory_dead_stock",
        "reports_finance", "reports_finance_profit_and_loss_report" -> {
            ReportsRouter(
                screen = screen,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
                onOpenModulesPanel = onOpenModulesPanel,
                onShowComingSoon = onShowComingSoon
            )
        }

        else -> {}
    }
}

@Composable
private fun HomeAndSettingsRouter(
    screen: String,
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    settingsViewModel: SettingsViewModel,
    authViewModel: Authenticate,
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onRecentCustomerIdSelected: (String?) -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    when (screen) {
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
            onNavigateToOrganizationSettings = { onSafeNavigate("organization_settings") },
            onNavigateToModuleSettings = { onSafeNavigate("module_settings") },
            onNavigateToSubscriptionSettings = { onSafeNavigate("manage_subscription") }
        )

        "manage_subscription" -> SubscriptionFlowContainer(
            onClose = onGoBack,
            onStartUpgrade = {}
        )

        "module_settings" -> ModuleSettingsScreen(
            onClose = onGoBack,
            onNavigateToModule = { route -> onSafeNavigate(route) },
            onConfigureHome = { onSafeNavigate("home_organization_profile") },
            onConfigureSales = { onSafeNavigate("sales_garment_type") },
            onConfigureSalesPricing = { onSafeNavigate("sales_garment_pricing_setup") },
            onConfigureMarketing = { onShowComingSoon("Marketing Settings Coming Soon") },
            onConfigureFinance = { onSafeNavigate("finance_chart_of_accounts") },
            onConfigureInventory = { onSafeNavigate("inventory_location_structure") },
            onNavigateWarehouse = { onSafeNavigate("inventory_location_structure") },
            onNavigateAllocationRules = { onSafeNavigate("inventory_allocation_rules") },
            onNavigatePdfTemplates = { onSafeNavigate("inventory_pdf_templates") },
            onNavigateLocationStructure = { onSafeNavigate("inventory_location_structure") },
            onNavigateFloorOverview = { onSafeNavigate("inventory_floor_overview") },
            onNavigateSectionOverview = { onSafeNavigate("inventory_section_overview") },
            onNavigateRackOverview = { onSafeNavigate("inventory_rack_overview") },
            onNavigateBinOverview = { onSafeNavigate("inventory_bin_overview") },
            onNavigateAutoReorder = { onSafeNavigate("inventory_auto_reorder_settings") },
            onConfigureLogistics = { onSafeNavigate("logistics_delivery") },
            onConfigureServices = { onSafeNavigate("services_service_status") },
            onNavigateServiceTemplates = { onSafeNavigate("services_service_templates") },
            onConfigureHR = { onSafeNavigate("hr_all_employees") },
            onConfigureIT = { onShowComingSoon("IT Settings Coming Soon") },
            onConfigureLegal = { onShowComingSoon("Legal Settings Coming Soon") },
            onConfigureSecurity = { onShowComingSoon("Security Settings Coming Soon") },
            onConfigureReports = { onSafeNavigate("reports_sales") }
        )

        "organization_settings" -> OrganizationSettingsScreen(
            onClose = onGoBack,
            onNavigate = { routeKey ->
                when (routeKey) {
                    "organization_profile" -> onNavigate("home_organization_profile")
                    "business_setup" -> onNavigate("home_business_setup")
                    "tax_rates" -> onNavigate("finance_tax_rates")
                    "gst_settings" -> onNavigate("finance_gst_settings")
                    "branch_management" -> onNavigate("home_branch_management")
                    "department_teams" -> onNavigate("home_department_teams")
                    "designation" -> onNavigate("home_designation")
                    "role" -> onNavigate("home_role_management")
                    "opening_balance" -> onNavigate("home_opening_balance")
                    "warehouse" -> onNavigate("home_warehouse_management")
                    "manage_subcription", "overview", "current_plan" -> onNavigate("manage_subscription")
                    else -> onNavigate(routeKey)
                }
            }
        )

        "home_business_setup" -> BusinessSetupScreen(
            onClose = onGoBack,
            onSaveAndContinue = { onGoBack() }
        )
    }
}

@Composable
private fun InventoryStructureRouter(
    screen: String,
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    when (screen) {
        "inventory_allocation_rules" -> AllocationRulesScreen(
            onClose = onGoBack,
            onAddNewClick = { onNavigate("inventory_create_allocation") }
        )

        "inventory_create_allocation" -> CreateAllocationScreen(
            onClose = onGoBack,
            onSaveRule = onGoBack
        )

        "inventory_pdf_templates" -> PdfTemplatesScreen(
            onClose = onGoBack,
            onCreateNewTemplate = { onShowComingSoon("Create Template Coming Soon") }
        )

        "inventory_auto_reorder_settings" -> AutoReorderSettingsScreen(
            onClose = onGoBack
        )

        "inventory_location_structure" -> LocationStructureScreen(
            onClose = onGoBack,
            onAddFloor = { onNavigate("inventory_add_floor") },
            onFloorClick = { onNavigate("inventory_floor_overview") }
        )

        // Floor Overview -> FAB navigates to Add Section Screen
        "inventory_floor_overview" -> FloorOverviewScreen(
            viewModel = settingsViewModel,
            onClose = onGoBack,
            onAddSection = {
                settingsViewModel.clearSelectedSectionForEdit()
                onNavigate("inventory_add_section")
            },
            onEditFloor = { floor ->
                settingsViewModel.setSelectedFloorForEdit(floor)
                onNavigate("inventory_add_floor")
            }
        )

        "inventory_add_floor" -> {
            val selectedFloor by settingsViewModel.selectedFloorForEdit.collectAsStateWithLifecycle()
            AddFloorScreen(
                floorItem = selectedFloor,
                viewModel = settingsViewModel,
                onClose = {
                    settingsViewModel.clearSelectedFloorForEdit()
                    onGoBack()
                },
                onSave = {
                    settingsViewModel.clearSelectedFloorForEdit()
                    onGoBack()
                }
            )
        }

        // Section Overview -> FAB navigates to Add Rack Screen
        "inventory_section_overview" -> SectionOverviewScreen(
            viewModel = settingsViewModel,
            onClose = onGoBack,
            onAddRack = {
                settingsViewModel.clearSelectedRackForEdit()
                onNavigate("inventory_add_rack")
            },
            onEditSection = { section ->
                settingsViewModel.setSelectedSectionForEdit(section)
                onNavigate("inventory_add_section")
            }
        )

        "inventory_add_section" -> {
            val selectedSection by settingsViewModel.selectedSectionForEdit.collectAsStateWithLifecycle()
            AddSectionScreen(
                sectionItem = selectedSection,
                viewModel = settingsViewModel,
                onClose = {
                    settingsViewModel.clearSelectedSectionForEdit()
                    onGoBack()
                },
                onSave = {
                    settingsViewModel.clearSelectedSectionForEdit()
                    onGoBack()
                }
            )
        }

        // Rack Overview -> FAB navigates to Add Bin Screen
        "inventory_rack_overview" -> RackOverviewScreen(
            viewModel = settingsViewModel,
            onClose = onGoBack,
            onAddBin = {
                settingsViewModel.clearSelectedBinForEdit()
                onNavigate("inventory_add_bin")
            },
            onEditRack = { rack ->
                settingsViewModel.setSelectedRackForEdit(rack)
                onNavigate("inventory_add_rack")
            }
        )

        "inventory_add_rack" -> {
            val selectedRack by settingsViewModel.selectedRackForEdit.collectAsStateWithLifecycle()
            AddRackScreen(
                rackItem = selectedRack,
                viewModel = settingsViewModel,
                onClose = {
                    settingsViewModel.clearSelectedRackForEdit()
                    onGoBack()
                },
                onSave = {
                    settingsViewModel.clearSelectedRackForEdit()
                    onGoBack()
                }
            )
        }

        // Bin Overview -> Displays bins only, no Add button
        "inventory_bin_overview" -> BinOverviewScreen(
            viewModel = settingsViewModel,
            onClose = onGoBack,
            onEditBin = { bin ->
                settingsViewModel.setSelectedBinForEdit(bin)
                onNavigate("inventory_add_bin")
            }
        )

        "inventory_add_bin" -> {
            val selectedBin by settingsViewModel.selectedBinForEdit.collectAsStateWithLifecycle()
            AddBinScreen(
                binItem = selectedBin,
                viewModel = settingsViewModel,
                onClose = {
                    settingsViewModel.clearSelectedBinForEdit()
                    onGoBack()
                },
                onSave = {
                    settingsViewModel.clearSelectedBinForEdit()
                    onGoBack()
                }
            )
        }
    }
}
@Composable
private fun SalesRouter(
    screen: String,
    isOrderFromFinance: Boolean,
    onOrderFromFinanceChange: (Boolean) -> Unit,
    prefillOrderForInvoice: OrderOverviewData?,
    onPrefillOrderForInvoiceChange: (OrderOverviewData?) -> Unit,
    selectedOpportunityId: String?,
    onOpportunityIdSelected: (String?) -> Unit,
    selectedMeasurementItem: MeasurementItem?,
    onMeasurementItemSelected: (MeasurementItem?) -> Unit,
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    token: String,
    customerViewModel: CustomerViewModel,
    settingsViewModel: SettingsViewModel,
    selectedCustomer: CustomerItem?,
    onCustomerSelected: (CustomerItem?) -> Unit,
    selectedOrderId: String?,
    onOrderIdSelected: (String?) -> Unit,
    editOrderId: String?,
    onEditOrderIdChange: (String?) -> Unit,
    selectedRecentCustomerId: String?,
    onRecentCustomerIdSelected: (String?) -> Unit,
    editingPricingId: String?,
    onEditingPricingIdChange: (String?) -> Unit,
    quotationScreenMode: String,
    onQuotationScreenModeChange: (String) -> Unit,
    pendingOrderReviewData: OrderReviewData?,
    onPendingOrderReviewDataChange: (OrderReviewData?) -> Unit,
    onOrderFlowOriginChange: (String?) -> Unit,
    onOrderSavedSuccessfully: (savedOrderId: String?) -> Unit,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenModulesPanel: (String) -> Unit,
    onSalesSettingsModeChange: (Boolean) -> Unit
) {
    var isGarmentActive by remember { mutableStateOf(false) }
    var selectedSegmentForEdit by remember { mutableStateOf<SegmentItem?>(null) }
    var measurementIndex by remember { mutableIntStateOf(0) }

    // order_preview-ku thevai
    val context = LocalContext.current
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()
    val orderActionState by salesOrderViewModel.actionState.collectAsState()
    val updateOrderState by salesOrderViewModel.updateOrderState.collectAsState()

    LaunchedEffect(orderActionState) {
        val state = orderActionState
        if (state is OrderActionState.Error) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            salesOrderViewModel.resetActionState()
        }
    }

// Update success / error handling
    LaunchedEffect(updateOrderState) {
        when (val state = updateOrderState) {
            is UpdateOrderState.Success -> {
                Toast.makeText(context, "Order updated successfully!", Toast.LENGTH_SHORT).show()
                OrderFlowStore.clear()
                measurementIndex = 0
                onPendingOrderReviewDataChange(null)
                onOrderSavedSuccessfully(state.order.id)   // adjust to your UpdateOrderState.Success field
                onNavigate("sales_sales_orders")
                salesOrderViewModel.resetUpdateOrderState()
            }
            is UpdateOrderState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                salesOrderViewModel.resetUpdateOrderState()
            }
            else -> Unit
        }
    }

    when (screen) {
        // Sales Dashboard Screen Route
        "sales_dashboard" -> SalesDashboardScreen(
            onClose = onGoBack,
            onNavigateToOrders = { onNavigate("sales_sales_orders") },
            onNavigateToQuotations = { onNavigate("sales_pricing_quotation") },
            onNavigateToCustomers = { onNavigate("sales_customers") },
            onNavigateToLeads = { onNavigate("sales_lead") },
            onNavigateToPipeline = { onNavigate("sales_opportunity_pipeline") }
        )

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
            onEditRequested = { onNavigate("edit_lead") },
            onConvertToOpportunity = { onNavigate("convert_lead_to_opportunity") }
        )

//        "convert_lead_to_opportunity" -> ConvertLeadToOpportunityScreen(
//            onClose = onGoBack,
//            onConversionSuccess = onGoBack
//        )

        "edit_lead" -> LeadFormScreen(
            mode = LeadFormMode.EDIT,
            onBack = onGoBack,
            onConvertToOrder = { orderReviewData ->
                onPendingOrderReviewDataChange(orderReviewData)
                onOrderFlowOriginChange("lead")
                onNavigate("create_order")
            }
        )

        // 1. Opportunity Pipeline Screen
        "sales_opportunity_pipeline" -> OpportunityPipelineScreen(
            onClose = onGoBack,
            onCreateLead = { onNavigate("create_lead") },
            onAddDeal = {
                onOpportunityIdSelected(null)
                onNavigate("create_opportunity")
            },
            onClearAll = { }
        )

        // 2. Convert Lead to Opportunity Screen (Standalone Page)
        "convert_lead_to_opportunity" -> ConvertLeadToOpportunityScreen(
            onClose = onGoBack,
            onConversionSuccess = {
                onGoBack()
            }
        )

        // 3. Opportunities List Screen
        "sales_opportunities" -> OpportunitiesListScreen(
            onClose = onGoBack,
            onAddDeal = {
                onOpportunityIdSelected(null)
                onNavigate("create_opportunity")
            },
            onViewOpportunity = { item ->
                onOpportunityIdSelected(item.id)
                onNavigate("opportunity_detail")
            },
            onEditOpportunity = { item ->
                onOpportunityIdSelected(item.id)
                onNavigate("create_opportunity")
            }
        )

        // 4. Create Opportunity Screen
        "create_opportunity" -> CreateOpportunityScreen(
            opportunityId = selectedOpportunityId,
            onClose = {
                onOpportunityIdSelected(null)
                onGoBack()
            },
            onSubmit = {
                onOpportunityIdSelected(null)
                onGoBack()
            }
        )

        // 5. Opportunity Detail Screen
        "opportunity_detail" -> OpportunityDetailScreen(
            opportunityId = selectedOpportunityId.orEmpty(),
            onClose = {
                onOpportunityIdSelected(null)
                onGoBack()
            },
            onEdit = {
                onNavigate("create_opportunity")
            },
            onConvertToOrder = { onNavigate("create_order") }
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

        "create_customer" -> {
            CustomerDetailScreen(
                navController = navController,
                customerId = "",
                isCreateMode = true,
                startInEditMode = true,
                onClose = onGoBack,
                onUpdateSuccess = {
                    customerViewModel.refresh()
                    onGoBack()
                }
            )
        }

        "view_customer", "edit_customer" -> {
            if (selectedCustomer != null && selectedCustomer.id.isNotBlank()) {
                CustomerDetailScreen(
                    navController = navController,
                    customerId = selectedCustomer.id,
                    isCreateMode = false,
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
            onCreateMeasurement = {
                onNavigate("measurement_entry")
            },
            onViewMeasurement = { item ->
                //  Check if customerId exists in clicked item
                android.util.Log.d("MEASUREMENT_DEBUG", "STEP 1: Clicked Measurement Card -> id='${item.id}', customerId='${item.customerId}', customerName='${item.customerName}'")
                onMeasurementItemSelected(item)
                onNavigate("measurements_available_view")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )
        "measurement_entry" -> {
            val draft = OrderFlowStore.draft
            val garmentItems = draft?.items?.filterIsInstance<DynamicGarmentItem>().orEmpty()

            if (draft != null && pendingOrderReviewData != null && garmentItems.isNotEmpty()) {
                val index = measurementIndex.coerceIn(0, garmentItems.lastIndex)
                val garment = garmentItems[index]
                val cfg = garment.config

                key(garment.id) {
                    MeasurementEntryScreen(
                        garmentType = cfg.garmentType,
                        garmentCategory = cfg.garmentCategory.takeIf { it != "Select Category" }.orEmpty(),
                        garmentCategoryId = cfg.garmentCategoryId,
                        orderItemId = garment.id,
                        customerName = draft.customerName,
                        customerPhone = draft.phone,
                        initialMeasurements = emptyMap(),
                        onClose = {
                            if (index > 0) measurementIndex = index - 1
                            else onGoBack()
                        },
                        onSaveMeasurement = {
                            if (index < garmentItems.lastIndex) measurementIndex = index + 1
                            else onNavigate("order_preview")
                        }
                    )
                }
            } else {
                MeasurementEntryScreen(
                    garmentType = "Shirt",
                    onClose = onGoBack,
                    onSaveMeasurement = { onGoBack() }
                )
            }
        }

        "measurements_available_view" -> {
            //  Check if customerId is being passed into the dialog
            android.util.Log.d("MEASUREMENT_DEBUG", "STEP 2: Router navigating to measurements_available_view -> customerId='${selectedMeasurementItem?.customerId}'")
            MeasurementsAvailableDialog(
                customerId = selectedMeasurementItem?.customerId,
                onGarmentSelected = { preset ->
                    onNavigate("measurement_management_view")
                },
                onAddNew = {
                    onNavigate("measurement_management_view")
                },
                onClose = onGoBack
            )
        }

        "measurement_management_view" -> {
            MeasurementManagementScreen(
                customerName = selectedMeasurementItem?.customerName ?: "Hameed Rahman",
                phone = selectedMeasurementItem?.contact ?: "+1 (555) 123-4567",
                lastOrderDate = selectedMeasurementItem?.measuredDate ?: "Dec 20, 2025",
                onAddField = { onNavigate("measurement_fields_config") },
                onClose = onGoBack
            )
        }

        "measurement_fields_config" -> {
            MeasurementFieldsConfigScreen(
                onAddNewCategory = { onNavigate("sales_add_garment_category") },
                onClose = onGoBack
            )
        }

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
                measurementIndex = 0
                onNavigate("measurement_entry")
            }
        )
        "order_preview" -> {
            pendingOrderReviewData?.let { data ->
                OrderPreviewScreen(
                    orderData = data,
                    onClose = {
                        onGoBack()
                    },
                    onConfirmOrder = { confirm ->
                        val draft = OrderFlowStore.draft
                        if (draft == null) {
                            Toast.makeText(context, "Order data missing, please start again", Toast.LENGTH_LONG).show()
                        } else if (orderActionState is OrderActionState.Loading ||
                            updateOrderState is UpdateOrderState.Loading
                        ) {
                            // double tap block
                        } else {
                            val built = buildCreateOrderFields(
                                draft = draft,
                                measurements = OrderFlowStore.measurements,
                                salespersonId = draft.salespersonId,
                                paymentTermId = TEMP_PAYMENT_TERM_ID,
                                paymentMode = confirm.paymentMode,
                                advanceAmountPaid = confirm.paymentAmountReceived,
                                isFullPayment = confirm.isFullPayment,
                                orderNotes = confirm.orderNotes
                            )

                            if (built.errors.isNotEmpty()) {
                                Toast.makeText(context, built.errors.first(), Toast.LENGTH_LONG).show()
                            } else {
                                val binaryImageParts = data.designImages.mapNotNull { uri ->
                                    uriToMultipartPart(context, uri, "attachments")
                                }

                                // If orderId is the 24-character MongoDB hex ID, use it:
                                val editId = when {
                                    !data.orderId.isNullOrBlank() && !data.orderId.startsWith("ORD") -> data.orderId
                                    !data.editOrderId.isNullOrBlank() && !data.editOrderId.startsWith("ORD") -> data.editOrderId
                                    else -> data.orderId ?: data.editOrderId
                                }

                                if (!editId.isNullOrBlank()) {
                                    // EDIT MODE: same payload, update API
                                    salesOrderViewModel.updateOrder(
                                        orderId = editId,
                                        fields = built.fields,
                                        existingImages = data.existingImageUrls,
                                        imageParts = binaryImageParts,
                                        voiceNotePart = null
                                    )
                                } else {
                                    // CREATE MODE
                                    salesOrderViewModel.createSalesOrder(
                                        fields = built.fields,
                                        imageParts = binaryImageParts,
                                        voiceNotePart = null
                                    ) { savedOrder ->
                                        Toast.makeText(context, "Order created successfully!", Toast.LENGTH_SHORT).show()
                                        OrderFlowStore.clear()
                                        measurementIndex = 0
                                        onPendingOrderReviewDataChange(null)
                                        onOrderSavedSuccessfully(savedOrder.id)
                                        onNavigate("sales_sales_orders")
                                    }
                                }
                            }
                        }
                    },
                    onCancel = {
                        OrderFlowStore.clear()
                        measurementIndex = 0
//                        onPendingOrderReviewDataChange(null)
                        onNavigate("sales_sales_orders")
                    }
                )
            } ?: run { onGoBack() }
        }
//        "create_order_review" -> {
//            pendingOrderReviewData?.let { data ->
//                CreateOrderNextStep(
//                    orderData = data,
//                    onBack = { updatedData ->
//                        onPendingOrderReviewDataChange(updatedData)
//                        onGoBack()
//                    },
//                    onClose = onGoBack,
//                    onSaveOrder = { _, savedOrderId ->
//                        onOrderSavedSuccessfully(savedOrderId)
//                    }
//                )
//            } ?: run { onGoBack() }
//        }

        "order_overview" -> {
            selectedOrderId?.let { id ->
                OrderOverviewScreen(
                    orderId = id,
                    showConvertToInvoice = isOrderFromFinance, // Visible only when coming from Finance module
                    onConvertToInvoice = { orderData ->
                        onPrefillOrderForInvoiceChange(orderData) // Store order data for invoice form
                        onNavigate("finance_create_sales_invoice") // Navigate to Create Invoice screen
                    },
                    onClose = {
                        onOrderFromFinanceChange(false)
                        onGoBack()
                    },
                    onEditOrder = { reviewData ->
                        onEditOrderIdChange(id)
                        onPendingOrderReviewDataChange(reviewData)
                        onNavigate("create_order")
                    },
                    onCreateNew = {
                        onEditOrderIdChange(null)
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
                onNavigate("view_quotation") // <-- DIRECT TO VIEW SCREEN
            },
            onEdit = { id ->
                onEditingPricingIdChange(id)
                onQuotationScreenModeChange("edit")
                onNavigate("create_quotation")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Sales") }
        )

        // ADD THIS ROUTE:
        "view_quotation" -> QuotationViewScreen(
            quotationId = editingPricingId.orEmpty(),
            onClose = {
                onEditingPricingIdChange(null)
                onGoBack()
            },
            onEdit = {
                onQuotationScreenModeChange("edit")
                onNavigate("create_quotation")
            }
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
            onPaymentClick = { orderId ->
                onOrderIdSelected(orderId)
                onNavigate("payment_detail")
            }
        )

        "payment_detail" -> PaymentInformationScreen(
            orderId = selectedOrderId.orEmpty(),
            onClose = onGoBack
        )

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

        "sales_category_detail" -> {
            val segmentId by settingsViewModel.selectedSegmentIdForStyle.collectAsStateWithLifecycle()
            val garmentId by settingsViewModel.selectedGarmentIdForStyle.collectAsStateWithLifecycle()
            val title by settingsViewModel.selectedGarmentTitleForStyle.collectAsStateWithLifecycle()

            GarmentCategoryDetailScreen(
                categoryTitle = title,
                segmentId = segmentId,
                garmentId = garmentId,
                onClose = onGoBack,
                onAddGarmentCategoryClick = { onNavigate("sales_add_garment_category") },
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

        "sales_add_existing_field" -> AddExistingFieldScreen(
            onClose = onGoBack,
            onAddSelected = { onGoBack() },
            viewModel = settingsViewModel
        )

        "sales_create_measurement_field" -> CreateMeasurementFieldScreen(
            onClose = onGoBack,
            onSave = { onGoBack() },
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

        "sales_garment_pricing_setup" -> PricingSetupScreen(
            onClose = onGoBack,
            onAddGarmentPricing = {
                onEditingPricingIdChange(null)
                onNavigate("sales_add_garment_pricing")
            },
            onAddFabricPricing = { onNavigate("sales_add_fabric_pricing") },
            onAddWorkPricing = {
                onEditingPricingIdChange(null)
                onNavigate("sales_add_work_pricing")
            },
            onEditGarmentPricing = { id ->
                onEditingPricingIdChange(id)
                onNavigate("sales_add_garment_pricing")
            },
            onEditWorkPricing = { id ->
                onEditingPricingIdChange(id)
                onNavigate("sales_add_work_pricing")
            }
        )

        "sales_add_garment_pricing" -> AddNewGarmentPricingScreen(
            garmentId = editingPricingId,
            onClose = {
                onEditingPricingIdChange(null)
                onGoBack()
            },
            onSaveSuccess = {
                onEditingPricingIdChange(null)
                onGoBack()
            }
        )

        "sales_add_fabric_pricing" -> AddFabricPriceScreen(
            onClose = onGoBack,
            onSaveSuccess = onGoBack
        )

        "sales_add_work_pricing" -> AddWorkPricingScreen(
            workId = editingPricingId,
            onClose = {
                onEditingPricingIdChange(null)
                onGoBack()
            },
            onSaveSuccess = {
                onEditingPricingIdChange(null)
                onGoBack()
            }
        )

        "sales_measurement_list" -> {
            MeasurementListScreen(
                onClose = onGoBack,
                onAddMeasurement = { onNavigate("sales_create_measurement_field") },
                viewModel = settingsViewModel
            )
        }
    }
}

// ── FINANCE ROUTER ──
@Composable
private fun FinanceRouter(
    screen: String,
    token: String,
    selectedPurchaseReturnId: String? = null,
    onPurchaseReturnIdSelected: (String?) -> Unit = {},
    selectedBankAccountId: String? = null,
    selectedStatementId: String? = null,
    onStatementIdSelected: (String?) -> Unit = {},
    onBankAccountIdSelected: (String?) -> Unit = {},
    selectedCashShiftId: String? = null,
    onCashShiftSelected: (String?) -> Unit = {},
    onCreditNoteSelected: (String?) -> Unit = {},
    selectedCreditNoteId: String? = null, // <-- Receive as parameter
    isOrderFromFinance: Boolean,
    invoiceForPayment: InvoiceViewOneData?,
    selectedReceiptId: String?,
    onReceiptSelected: (String?) -> Unit,
    onInvoiceForPaymentChange: (InvoiceViewOneData?) -> Unit,
    onOrderFromFinanceChange: (Boolean) -> Unit,
    prefillOrderForInvoice: OrderOverviewData?,
    onPrefillOrderForInvoiceChange: (OrderOverviewData?) -> Unit,
    selectedSupplier: SupplierRow?,
    selectedLedgerAccountId: String?,
    selectedLedgerAccountName: String,
    onLedgerAccountSelected: (id: String?, name: String) -> Unit,
    selectedSupplierId: String?,
    onSupplierSelected: (String?) -> Unit,
    selectedPaymentModeId: String?,
    onPaymentModeSelected: (String?) -> Unit,
    selectedInvoiceId: String?,
    onInvoiceSelected: (String?) -> Unit,
    selectedPurchaseInvoice: PurchaseInvoiceItem?,
    onPurchaseInvoiceSelected: (PurchaseInvoiceItem?) -> Unit,
    selectedRecentCustomerId: String?,
    onRecentCustomerIdSelected: (String?) -> Unit,
    inventoryViewModel: InventoryViewModel,
    settingsViewModel: SettingsViewModel,
    branchViewModel: BranchViewModel,
    salesViewModel: SalesViewModel,
    customerViewModel: CustomerViewModel,
    selectedReceivePoId: String? = null,
    onReceivePoIdSelected: (String?) -> Unit = {},
    selectedPurchaseOrderForDetail: PurchaseOrder? = null,
    onPurchaseOrderForDetailSelected: (PurchaseOrder?) -> Unit = {},
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    onPendingOrderReviewDataChange: (OrderReviewData?) -> Unit,
    onOrderIdSelected: (String?) -> Unit,
    onEditOrderIdChange: (String?) -> Unit,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    val financeViewModel: FinanceViewModel = hiltViewModel()
    var selectedPurchaseOrderForEdit by remember { mutableStateOf<PurchaseOrder?>(null) }

    when (screen) {
        // Finance Dashboard overview screen
        "finance_dashboard" -> FinanceOverviewScreen(
            onClose = onGoBack,
            onViewDistribution = { onNavigate("finance_chart_of_accounts") },
            onViewFunds = { onNavigate("finance_chart_of_accounts") },
            onSummaryView = { title ->
                when (title) {
                    "Sales" -> onNavigate("finance_sales_invoices")
                    "Purchase" -> onNavigate("finance_purchase_orders")
                    "General Ledger" -> onNavigate("finance_chart_of_accounts")
                    "Expenses" -> onNavigate("finance_expenses")
                    "Funds" -> onNavigate("finance_chart_of_accounts")
                }
            },
            onGoTo = { title ->
                when (title) {
                    "Sales" -> onNavigate("finance_sales_invoices")
                    "Purchase" -> onNavigate("finance_purchase_orders")
                    "General Ledger" -> onNavigate("finance_trial_balance")
                    "Expenses" -> onNavigate("finance_expenses")
                    "Funds" -> onNavigate("finance_chart_of_accounts")
                }
            },
            onAlertClick = { title ->
                when (title) {
                    "Supplier Payments Due" -> onNavigate("finance_payments_made")
                    "Expense Claims" -> onNavigate("finance_expenses")
                    "Bank Reconciliation" -> onNavigate("finance_trial_balance")
                }
            }
        )

        // Finance Sales Orders: sets finance origin flag to true
        "finance_sales_orders" -> {
            SalesOrderScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack,
                onCreateOrder = {
                    onPendingOrderReviewDataChange(null)
                    onNavigate("create_order")
                },
                onViewOrder = { orderId ->
                    onOrderIdSelected(orderId)
                    onOrderFromFinanceChange(true) // Set flag for finance context
                    onNavigate("order_overview")
                },
                onEditOrder = { orderId -> onEditOrderIdChange(orderId) },
                onBreadCrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        // Single Create Sales Invoice route receiving prefilled order data
        "finance_create_sales_invoice" -> CreateSalesInvoiceScreen(
            prefillOrder = prefillOrderForInvoice, // Pass prefilled order object
            onClose = {
                onPrefillOrderForInvoiceChange(null)
                onGoBack()
            },
            onInvoiceCreatedSuccessfully = {
                onPrefillOrderForInvoiceChange(null)
                onGoBack()
            },
            customerViewModel = customerViewModel,
            inventoryViewModel = inventoryViewModel
        )

        "finance_sales_invoices" -> FinanceInvoiceScreen(
            onClose = onGoBack,
            onInvoiceClick = { invoice ->
                onInvoiceSelected(invoice.id)
                onNavigate("finance_invoice_detail")
            },
            onCreateInvoice = {
                onPrefillOrderForInvoiceChange(null)
                onNavigate("finance_create_sales_invoice")
            },
            onRecordPayment = { invoiceData ->
                onInvoiceForPaymentChange(invoiceData)
                onNavigate("finance_record_payment")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Finance") }
        )

        "finance_receipts" -> CustomerReceiptsListScreen(
            viewModel = financeViewModel,
            onClose = onGoBack,
            onCreateReceiptClick = {
                onNavigate("finance_create_payment_receipt")
            },
            onReceiptClick = { receipt ->
                onReceiptSelected(receipt.id)
                onNavigate("finance_receipt_detail")
            }
        )

        "finance_receipt_detail" -> {
            selectedReceiptId?.let { id ->
                CustomerReceiptDetailScreen(
                    paymentId = id,
                    viewModel = financeViewModel,
                    onClose = {
                        onReceiptSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }
        // New route to open Record Payment / Create Receipt Screen
        "finance_create_payment_receipt" -> CreatePaymentReceiptScreen(
            financeViewModel = financeViewModel,
            customerViewModel = customerViewModel,
            onClose = onGoBack,
            onPaymentRecorded = {
                financeViewModel.fetchCustomerReceipts(page = 1)
            }
        )

        // 1. Credit Notes List Screen
        "finance_credits" -> {
            com.cuso.tailor.view.home.finance.credit.CreditNotesListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onNavigateToCreate = {
                    onNavigate("finance_create_credit")
                },
                onNavigateToDetail = { id ->
                    onCreditNoteSelected(id) // Sets ID into HomeScreen level state
                    onNavigate("finance_credit_detail")
                }
            )
        }

        // 2. Create Credit Note Screen
        "finance_create_credit" -> {
            CreateCreditNoteScreen(
                viewModel = financeViewModel,
                inventoryViewModel = inventoryViewModel,
                customerViewModel = customerViewModel,
                branchViewModel = branchViewModel,
                onBack = onGoBack
            )
        }

        // 3. Credit Note Detail Screen (View One)
        "finance_credit_detail" -> {
            selectedCreditNoteId?.let { id ->
                CreditNoteDetailScreen(
                    creditNoteId = id,
                    viewModel = financeViewModel,
                    onBack = {
                        onCreditNoteSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "finance_invoice_detail" -> {
            selectedInvoiceId?.let { id ->
                InvoiceDetailScreen(
                    invoiceId = id,
                    onClose = {
                        onInvoiceSelected(null)
                        onGoBack()
                    },
                    onRecordPayment = { invoiceData ->
                        // Store the full invoice object globally in HomeScreen state
                        onInvoiceForPaymentChange(invoiceData)
                        // Trigger actual screenStack push in HomeScreen
                        onNavigate("finance_record_payment")
                    },
                    token = token
                )
            } ?: run { onGoBack() }
        }

        "finance_record_payment" -> {
            invoiceForPayment?.let { invoice ->
                val recordState by financeViewModel.recordPaymentState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                RecordPaymentReceiptScreen(
                    invoice = invoice,
                    isLoading = recordState is RecordPaymentState.Loading,
                    onClose = {
                        onInvoiceForPaymentChange(null)
                        financeViewModel.resetRecordPaymentState()
                        onGoBack()
                    },
                    onSavePayment = { customerId, branchId, invoiceId, amount, mode, depositAccountId, isoDate, refNo, notes, uris, sendNotification ->
                        val binaryParts = uris.mapNotNull { uri ->
                            uriToMultipartPart(context, uri, "attachments")
                        }

                        financeViewModel.recordCustomerPayment(
                            customerId = customerId,
                            branchId = branchId,
                            invoiceId = invoiceId,
                            amount = amount,
                            paymentMode = mode,
                            depositToAccountId = depositAccountId,
                            paymentDate = isoDate,
                            referenceNumber = refNo,
                            notes = notes,
                            attachmentParts = binaryParts,
                            onSuccess = { successMsg ->
                                DynamicIslandManager.showSuccess(successMsg)
                                onInvoiceForPaymentChange(null)
                                financeViewModel.resetRecordPaymentState()
                                onGoBack()
                            }
                        )
                    }
                )
            } ?: run {
                // If invoice object is null, safely go back
                onGoBack()
            }
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
        "finance_purchase_orders" -> {
            POListScreen(
                viewModel = inventoryViewModel,
                onNavigateToCreate = {
                    selectedPurchaseOrderForEdit = null
                    onNavigate("inventory_create_purchase_order_flow")
                },
                onNavigateToDetail = { po ->
                    onPurchaseOrderForDetailSelected(po)
                    onNavigate("inventory_purchase_order_detail_flow")
                },
                onClose = onGoBack
            )
        }

        // 2. Purchase Receive -> Inventory Purchase Receive Screen
        "finance_purchase_receive" -> {
            AllOrdersScreen(
                onClose = onGoBack,
                onCreateOrderClick = { onNavigate("inventory_create_purchase_order") },
                onOrderClick = { poId, _ ->
                    onReceivePoIdSelected(poId)
                    onNavigate("inventory_purchase_receive_detail")
                }
            )
        }

        // 3. Purchase Bills -> Inventory Bills List Screen
        "finance_bills_list" -> {
            AllBillListScreen(
                onClose = onGoBack,
                onCreateOrder = { onNavigate("inventory_create_purchase_order") },
                onInvoiceClick = { billItem ->
                    if (billItem.id.isNotBlank()) {
                        onInvoiceSelected(billItem.id)
                        onNavigate("inventory_payable_purchase_detail")
                    }
                },
                onOptionsClick = { }
            )
        }

        // Purchase Returns List Screen
        // Purchase Returns List Screen
        "finance_purchase_returns" -> {
            FinanceReturnScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onCreateReturnClick = {
                    onNavigate("finance_create_purchase_return") // 👈 Navigates to Create Screen
                },
                onReturnClick = { returnId ->
                    if (returnId.isNotBlank()) {
                        onPurchaseReturnIdSelected(returnId)
                        onNavigate("finance_purchase_return_detail")
                    }
                }
            )
        }

        //  Create Purchase Return Screen
        "finance_create_purchase_return" -> {
            CreatePurchaseReturnScreen(
                onBack = onGoBack,
                onCreateReturn = {
                    onGoBack()
                }
            )
        }

        // Purchase Return Detail View Screen
        "finance_purchase_return_detail" -> {
            selectedPurchaseReturnId?.takeIf { it.isNotBlank() }?.let { id ->
                PurchaseReturnDetailScreen(
                    returnId = id,
                    viewModel = financeViewModel,
                    onBack = {
                        onPurchaseReturnIdSelected(null)
                        onGoBack()
                    },
                    onSettleRefund = {
                        // Handle settle refund action
                    },
                    onViewPo = { poNumber ->
                        // Navigate to PO details if needed
                    }
                )
            } ?: run {
                // If ID is missing or null, safely navigate back to avoid blank screen
                onGoBack()
            }
        }

        "finance_vendor_credits" -> {
            VendorCreditListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onCreditClick = { creditId ->
                }
            )
        }

        // 4. Payments Made -> Inventory Payments Made Screen
        "finance_payments_made" -> {
            AllInventoryPaymentScreen(
                onClose = onGoBack,
                onPaymentSelect = { paymentId ->
                    if (paymentId.isNotBlank()) {
                        onPaymentModeSelected(paymentId)
                        onNavigate("inventory_payment_overview_detail")
                    }
                },
                onFilterClick = { }
            )
        }
        "finance_customers" -> FinanceCustomerScreen(
            onClose = onGoBack,
            onCustomerClick = { customerId ->
                onRecentCustomerIdSelected(customerId)
                onNavigate("finance_customer_detail")
            },
            onCustomerEdit = { customerId ->
                onRecentCustomerIdSelected(customerId)
                onNavigate("finance_create_customer")
            },
            onAddCustomer = {
                onRecentCustomerIdSelected(null)
                onNavigate("finance_create_customer")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Finance") }
        )


        "finance_create_customer" -> CreateCustomerScreen(
            customerId = selectedRecentCustomerId,
            onClose = {
                onRecentCustomerIdSelected(null)
                onGoBack()
            },
            onCustomerCreated = {
                onRecentCustomerIdSelected(null)
                onGoBack()
            }
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

        // ── FINANCE SUPPLIERS LIST (Uses InventorySuppliersScreen UI, but navigates to Finance Detail) ──
        // ── FINANCE SUPPLIERS LIST ──
        "finance_suppliers" -> {
            InventorySuppliersScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Finance") },
                onSupplierClick = { supplierDto ->
                    onSupplierSelected(supplierDto.id)
                    onNavigate("finance_supplier_detail")
                }
            )
        }

        // ── FINANCE SUPPLIER DETAIL ──
        "finance_supplier_detail" -> {
            FinanceSupplierDetailScreen(
                supplier = selectedSupplier ?: SupplierRow(id = selectedSupplierId.orEmpty()),
                supplierId = selectedSupplierId.orEmpty(),
                onNewOrder = {
                    // Navigate to Create Purchase Order screen
                    onNavigate("inventory_create_purchase_order_flow")
                },
                onClose = {
                    onSupplierSelected(null)
                    onGoBack()
                },
                onBreadcrumbClick = { onOpenModulesPanel("Finance") }
            )
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
        "finance_gst_settings" -> GstSettingsOverviewScreen(
            viewModel = settingsViewModel,
            onClose = onGoBack,
            onAddGst = {
                settingsViewModel.clearSelectedTaxRegistration()
                onNavigate("finance_add_gst")
            },
            onEditGst = { selectedItem ->
                settingsViewModel.fetchTaxRegistrationById(selectedItem.id)
                onNavigate("finance_add_gst")
            }
        )

        "finance_add_gst" -> AddGstSettingsScreen(
            settingsViewModel = settingsViewModel,     // <-- Added this parameter
            branchViewModel = branchViewModel,
            inventoryViewModel = inventoryViewModel,
            salesViewModel = salesViewModel,
            onClose = {
                settingsViewModel.clearSelectedTaxRegistration()
                onGoBack()
            },
            onSave = {
                settingsViewModel.clearSelectedTaxRegistration()
                onGoBack()
            }
        )

        "finance_tax_rates" -> TaxRatesScreen(
            taxRates = emptyList(),
            stats = emptyList(),
            onClose = onGoBack,
            onAddTaxRate = { onNavigate("finance_add_gst") },
            onAddTaxGroup = { onNavigate("finance_add_tax_group") },
            onEditRate = { },
            onDeleteRate = { }
        )

        "finance_add_tax_group" -> AddTaxGroupScreen(
            onClose = onGoBack,
            onCreateGroup = { onGoBack() }
        )

        // ── Cash Books List Screen ──
        "finance_cash_book" -> {
            CashBookListScreen(
                viewModel = financeViewModel,
                branchViewModel = branchViewModel,
                onBack = onGoBack,
                onNavigateToDetail = { shiftId ->
                    onCashShiftSelected(shiftId)
                    onNavigate("finance_cash_book_detail")
                }
            )
        }

        // ── Cash Book Detail Screen ──
        "finance_cash_book_detail" -> {
            selectedCashShiftId?.let { shiftId ->
                CashBookDetailScreen(
                    shiftId = shiftId,
                    viewModel = financeViewModel,
                    onBack = {
                        onCashShiftSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        // ── BANK ACCOUNTS LIST ──
        "finance_bank_accounts" -> {
            BankAccountsListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onNavigateToCreate = {
                    onNavigate("finance_create_bank_account")
                },
                onNavigateToDetail = { accountId ->
                    if (accountId.isNotBlank()) {
                        onBankAccountIdSelected(accountId) // Sets non-null ID
                        onNavigate("finance_bank_account_detail")
                    }
                }
            )
        }

        // ── CREATE BANK ACCOUNT ──
        "finance_create_bank_account" -> {
            CreateBankAccountScreen(
                viewModel = financeViewModel,
                branchViewModel = branchViewModel,
                onBack = onGoBack
            )
        }

        // ── BANK ACCOUNT DETAIL ──
        "finance_bank_account_detail" -> {
            selectedBankAccountId?.takeIf { it.isNotBlank() }?.let { validId ->
                BankAccountDetailScreen(
                    accountId = validId,
                    viewModel = financeViewModel,
                    onBack = {
                        onBankAccountIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run {
                // If ID is missing or null, safely pop back to prevent blank screen
                onGoBack()
            }
        }

        // ── BANK STATEMENTS LIST SCREEN ──
        "finance_bank_statements" -> {
            BankStatementListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onNavigateToImport = {
                    onNavigate("finance_import_bank_statement")
                },
                onNavigateToDetail = { statementId ->
                    if (statementId.isNotBlank()) {
                        onStatementIdSelected(statementId)
                        onNavigate("finance_bank_statement_detail")
                    }
                }
            )
        }

        // ── BANK STATEMENT DETAIL SCREEN ──
        "finance_bank_statement_detail" -> {
            selectedStatementId?.takeIf { it.isNotBlank() }?.let { id ->
                BankStatementDetailScreen(
                    statementId = id,
                    viewModel = financeViewModel,
                    onBack = {
                        onStatementIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        // ── IMPORT BANK STATEMENT FULL PAGE SCREEN ──
        "finance_import_bank_statement" -> {
            ImportBankStatementScreen(
                viewModel = financeViewModel,
                onBack = onGoBack
            )
        }
    }
}

@Composable
private fun InventoryMainRouter(
    screen: String,
    selectedPriceListId: String?,
    onPriceListIdSelected: (String?) -> Unit,
    selectedApprovalId: String?,
    onApprovalIdSelected: (String?) -> Unit,
    selectedInventoryItemId: String?,
    onInventoryItemIdSelected: (String?) -> Unit,
    selectedItemGroupId: String?,
    onItemGroupIdSelected: (String?) -> Unit,
    selectedBulkItemId: String?,
    onBulkItemIdSelected: (String?) -> Unit,
    // Add category edit parameters here
    selectedCategoryIdForEdit: String?,
    onCategoryIdForEditSelected: (String?) -> Unit,
    inventoryViewModel: InventoryViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    var selectedAdjustmentType by remember { mutableStateOf(AdjustmentType.TransferStock) }
    var selectedStockLocationItem by remember { mutableStateOf<StockLocationItemDto?>(null) }

    when (screen) {
        "inventory_dashboard" -> {
            InventoryDashboardScreen(
                viewModel = inventoryViewModel
            )
        }

        "inventory_items", "inventory_all_items" -> InventoryScreen(
            onClose = onGoBack,
            onAddItem = { onNavigate("inventory_create_item") },
            onViewItem = { item ->
                onInventoryItemIdSelected(item._id)
                onNavigate("inventory_item_detail")
            },
            onEditItem = { onNavigate("inventory_create_item") },
            onBreadCrumbClick = { onOpenModulesPanel("Inventory") }
        )

        "inventory_create_item" -> {
            android.util.Log.d("InventoryNav", ">> Entering inventory_create_item with ID: '$selectedInventoryItemId'")
            CreateItemScreen(
                itemId = selectedInventoryItemId,
                onDismiss = {
                    onInventoryItemIdSelected(null)
                    onGoBack()
                },
                onItemCreated = {
                    onInventoryItemIdSelected(null)
                    onGoBack()
                }
            )
        }

        "inventory_item_detail" -> {
            selectedInventoryItemId?.let { id ->
                val selectedItem by inventoryViewModel.selectedItem.collectAsStateWithLifecycle()
                val isLoadingDetail by inventoryViewModel.isLoadingItemDetail.collectAsStateWithLifecycle()
                val detailError by inventoryViewModel.itemDetailError.collectAsStateWithLifecycle()

                LaunchedEffect(id) {
                    inventoryViewModel.fetchInventoryItemDetail(id)
                }

                InventoryViewOne(
                    item = selectedItem,
                    isLoading = isLoadingDetail,
                    errorMessage = detailError,
                    onDismiss = {
                        onInventoryItemIdSelected(null)
                        onGoBack()
                    },
                    onAdjustStock = { item ->
                        selectedAdjustmentType = AdjustmentType.Increase
                        onInventoryItemIdSelected(item._id)
                        onNavigate("inventory_adjustment")
                    },
                    onWarehouseTransfer = { item ->
                        selectedAdjustmentType = AdjustmentType.TransferStock
                        onInventoryItemIdSelected(item._id)
                        onNavigate("inventory_transfer_stock")
                    },
                    onReorderStock = {
                        onNavigate("inventory_create_purchase_order")
                    },
                    onMarkInactive = { },
                    onEdit = {
                        onNavigate("inventory_create_item")
                    },
                    onShare = { }
                )
            } ?: run { onGoBack() }
        }

        "inventory_item_groups" -> AllItemGroupScreen(
            onDismiss = onGoBack,
            onAddItemGroup = { onNavigate("inventory_create_item_group") },
            onView = { groupId ->
                onItemGroupIdSelected(groupId)
                onNavigate("inventory_item_group_detail")
            },
            onEdit = { groupId ->
                onItemGroupIdSelected(groupId)
                onNavigate("inventory_create_item_group")
            },
            onBreadCrumbClick = { onOpenModulesPanel("Inventory") }
        )

        "inventory_item_group_detail" -> {
            selectedItemGroupId?.let { id ->
                ItemGroupDetailScreen(
                    itemGroupId = id,
                    onClose = {
                        onItemGroupIdSelected(null)
                        onGoBack()
                    },
                    onEdit = {
                        onNavigate("inventory_create_item_group")
                    },
                    onAdjustStock = { onNavigate("inventory_adjustment") },
                    onExportPdf = { }
                )
            } ?: run { onGoBack() }
        }

        "inventory_create_item_group" -> CreateItemGroupScreen(
            onDismiss = {
                onItemGroupIdSelected(null)
                onGoBack()
            },
            onSaveSuccess = {
                onItemGroupIdSelected(null)
                onGoBack()
            }
        )

        "inventory_bulk", "inventory_all_bulk" -> BulkListScreen(
            onClose = onGoBack,
            onItemClick = { itemId ->
                if (itemId.isNotBlank()) {
                    onBulkItemIdSelected(itemId)
                    onNavigate("inventory_bulk_detail")
                }
            },
            onEditClick = { itemId ->
                if (itemId.isNotBlank()) {
                    onBulkItemIdSelected(itemId)
                    onNavigate("inventory_add_bulk")
                }
            },
            onAddBulkClick = {
                onBulkItemIdSelected(null)
                onNavigate("inventory_add_bulk")
            }
        )

        "inventory_bulk_detail" -> {
            selectedBulkItemId?.takeIf { it.isNotBlank() }?.let { itemId ->
                BulkDetailsScreen(
                    itemId = itemId,
                    onClose = onGoBack,
                    onEdit = { editId ->
                        onBulkItemIdSelected(editId)
                        onNavigate("inventory_add_bulk")
                    },
                    onAdjustStock = { onNavigate("inventory_adjustment") }
                )
            } ?: run { onGoBack() }
        }

        "inventory_add_bulk" -> {
            AddBulkItemScreen(
                editItemId = selectedBulkItemId,
                onClose = {
                    onBulkItemIdSelected(null)
                    onGoBack()
                },
                onSaved = {
                    onBulkItemIdSelected(null)
                    onGoBack()
                }
            )
        }

        "inventory_adjustment", "inventory_adjustments" -> AllOrdersStockListScreen(
            preselectedItemId = selectedInventoryItemId,
            initialAdjustmentType = selectedAdjustmentType,
            onClose = onGoBack
        )

        "inventory_transfer_order", "inventory_transfer_stock", "inventory_items_transfer_stock" -> TransferOrdersStockListScreen(
            preselectedItemId = selectedInventoryItemId,
            onClose = {
                onInventoryItemIdSelected(null)
                onGoBack()
            }
        )

        "inventory_stock_location", "inventory_location_management", "inventory_procurement_location_management" -> {
            AllStockLocationScreen(
                inventoryViewModel = inventoryViewModel,
                onClose = onGoBack,
                onItemClick = { item ->
                    selectedStockLocationItem = item
                    inventoryViewModel.setSelectedStockLocationItem(item)
                    onNavigate("inventory_stock_location_details")
                },
                onLocationClick = { item ->
                    selectedStockLocationItem = item
                    inventoryViewModel.setSelectedStockLocationItem(item)
                    onNavigate("inventory_stock_location_form")
                },
                onOptionsClick = { _ -> }
            )
        }

        "inventory_stock_location_details" -> {
            StockLocationDetailsScreen(
                itemId = selectedStockLocationItem?.id,
                inventoryViewModel = inventoryViewModel,
                onClose = onGoBack
            )
        }

        "inventory_stock_location_form" -> {
            StockLocationFormScreen(
                item = selectedStockLocationItem,
                onClose = {
                    selectedStockLocationItem = null
                    onGoBack()
                },
                onSave = {
                    inventoryViewModel.fetchStockLocationItems()
                    selectedStockLocationItem = null
                    onGoBack()
                }
            )
        }

        "inventory_safety_stock" -> SafetyStockScreen(
            onClose = onGoBack,
//            onItemClick = { selectedItem ->
//                android.util.Log.d("InventoryNav", ">> SafetyStock Card Clicked. ItemId: '${selectedItem.itemId}'")
//                if (selectedItem.itemId.isNotBlank()) {
//                    onInventoryItemIdSelected(selectedItem.itemId)
//                    onNavigate("inventory_item_detail")
//                } else {
//                    android.util.Log.e("InventoryNav", ">> SafetyStock Card Click: ItemId is NULL or BLANK!")
//                }
//            },
            onEditClick = { itemId ->
                android.util.Log.d("InventoryNav", ">> SafetyStock 3-Dot Edit Clicked. ItemId: '$itemId'")
                if (itemId.isNotBlank()) {
                    onInventoryItemIdSelected(itemId)
                    onNavigate("inventory_create_item")
                } else {
                    android.util.Log.e("InventoryNav", ">> SafetyStock Edit Click: ItemId is NULL or BLANK!")
                }
            },
            onFilterClick = { }
        )

        // 1. Auto Reorder List Screen
        "inventory_auto_reorder" -> AutoReorderScreen(
            onBack = onGoBack,
            onCreateNewItem = {
                println("DEBUG_NAV: Create New Item Clicked!")
                onNavigate("inventory_auto_reorder_detail")
            },
            onEditItem = { item ->
                // Navigate to edit rule
            },
            onFilterClick = { }
        )

        // 2. Create Rule Screen
        "inventory_auto_reorder_detail" -> CreateAutoReorderRuleScreen(
            onClose = onGoBack,
            onCreateRuleSuccess = {
                onGoBack()
            }
        )

        "inventory_multichannel_category_listing", "inventory_category_listing" -> {
            CategoryManagementScreen(
                viewModel = inventoryViewModel,          // <-- Share instance
                settingsViewModel = settingsViewModel,
                onClose = onGoBack,
                onAddCategory = {
                    onCategoryIdForEditSelected(null)
                    onNavigate("inventory_create_category")
                },
                onEditCategory = { category ->
                    onCategoryIdForEditSelected(category.id)
                    onNavigate("inventory_create_category")
                }
            )
        }

        "inventory_create_category" -> {
            CreateCategoryScreen(
                categoryId = selectedCategoryIdForEdit,
                inventoryViewModel = inventoryViewModel,   // <-- Share instance
                settingsViewModel = settingsViewModel,
                onClose = {
                    onCategoryIdForEditSelected(null)
                    onGoBack()
                },
                onCancel = {
                    onCategoryIdForEditSelected(null)
                    onGoBack()
                },
                onSavedSuccess = {
                    onCategoryIdForEditSelected(null)
                    onGoBack()
                }
            )
        }

        // ── Approvals List Screen ──
        "inventory_approvals" -> ApprovalsListScreen(
            viewModel = inventoryViewModel,
            onBack = onGoBack,
            onItemClick = { approvalId ->
                onApprovalIdSelected(approvalId)
                onNavigate("inventory_approval_detail")
            },
            onFilterClick = { }
        )

        // ── Approval Detail Screen ──
        "inventory_approval_detail" -> {
            selectedApprovalId?.let { id ->
                ApprovalDetailScreen(
                    approvalId = id,
                    viewModel = inventoryViewModel,
                    onBack = {
                        onApprovalIdSelected(null)
                        onGoBack()
                    },
                    onDownloadPdf = { }
                )
            } ?: run {
                // If id is null, safely navigate back to avoid blank screen
                onGoBack()
            }
        }

        "inventory_pricing_list" -> AllPricingScreen(
            viewModel = inventoryViewModel,
            onClose = onGoBack,
            onAddNew = {
                onPriceListIdSelected(null)
                inventoryViewModel.clearSelectedPriceListDetail()
                onNavigate("inventory_new_pricing")
            },
            onItemClick = { item ->
                onPriceListIdSelected(item.id)
                onNavigate("inventory_new_pricing")
            },
            onEditItem = { item ->
                onPriceListIdSelected(item.id)
                onNavigate("inventory_new_pricing")
            }
        )
        "inventory_new_pricing" -> NewPriceListScreen(
            editPriceListId = selectedPriceListId,
            inventoryViewModel = inventoryViewModel,
            onClose = {
                onPriceListIdSelected(null)
                inventoryViewModel.clearSelectedPriceListDetail()
                onGoBack()
            }
        )

        // Multi-Channel Basic Info Screen
        "inventory_multichannel_basic_info" -> {
            BasicInfoScreen(
                onClose = onGoBack
            )
        }

        // Multi-Channel Allocation List Screen
            "inventory_multichannel_allocation_list" -> {
            AllocationListScreen(
                onClose = onGoBack,
                onAddNew = { },
                onViewItem = { item -> },
                onEditItem = { item -> },
                onDeleteItem = { item -> }
            )
        }
    }
}

@Composable
private fun InventoryProcurementRouter(
    screen: String,
    selectedPackageIdForDetail: String? = null,
    onPackageIdForDetailSelected: (String?) -> Unit = {},
    selectedPackageIdForEdit: String? = null,
    onPackageIdForEditSelected: (String?) -> Unit = {},
    selectedPicklistId: String? = null,
    onPicklistIdSelected: (String?) -> Unit = {},
    selectedBarcodeIdForDetail: String?,
    onBarcodeIdForDetailSelected: (String?) -> Unit,
    selectedReceivePoId: String?,
    onReceivePoIdSelected: (String?) -> Unit,
    selectedPurchaseOrderForDetail: PurchaseOrder?,
    onPurchaseOrderForDetailSelected: (PurchaseOrder?) -> Unit,
    selectedRequisitionIdForDetail: String?,
    onRequisitionIdForDetailSelected: (String?) -> Unit,
    selectedSupplierId: String?,
    onSupplierSelected: (String?) -> Unit,
    selectedInvoiceId: String?,
    onInvoiceSelected: (String?) -> Unit,
    selectedPaymentModeId: String?,
    onPaymentModeSelected: (String?) -> Unit,
    selectedLowStockItem: LowStockItemDto?,
    onLowStockItemSelected: (LowStockItemDto?) -> Unit,
    inventoryViewModel: InventoryViewModel,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    var selectedPurchaseOrderForEdit by remember { mutableStateOf<PurchaseOrder?>(null) }

    when (screen) {
        // ── INVENTORY SUPPLIERS (Only Inventory navigation) ──
        "inventory_suppliers" -> {
            InventorySuppliersScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Inventory") },
                onSupplierClick = { supplier ->
                    onSupplierSelected(supplier.id)
                    onNavigate("inventory_supplier_detail") // <-- Goes to Inventory Supplier Detail!
                }
            )
        }

        // ── INVENTORY SUPPLIER DETAIL ──
        "inventory_supplier_detail" -> {
            selectedSupplierId?.let { id ->
                InventorySupplierDetailScreen(
                    supplierId = id,
                    onClose = {
                        onSupplierSelected(null)
                        onGoBack()
                    },
                    onBreadcrumbClick = { onOpenModulesPanel("Inventory") }
                )
            } ?: run { onGoBack() }
        }

        "inventory_requisitions", "inventory_procurement_purchase_request", "inventory_procurement_requisitions" -> AllRequisitionsScreen(
            onClose = onGoBack,
            onCreateRequisition = {
                onRequisitionIdForDetailSelected(null)
                onNavigate("inventory_create_requisition")
            },
            onEditRequisition = { requisition ->
                onRequisitionIdForDetailSelected(requisition.id)
                onNavigate("inventory_create_requisition")
            },
            onRequisitionClick = { requisition ->
                val id = requisition.id ?: return@AllRequisitionsScreen
                onRequisitionIdForDetailSelected(id)
                onNavigate("inventory_requisition_detail")
            }
        )

        "inventory_requisition_detail" -> {
            RequisitionDetailScreen(
                requisitionId = selectedRequisitionIdForDetail,
                onClose = {
                    onRequisitionIdForDetailSelected(null)
                    onGoBack()
                },
                onEdit = { onNavigate("inventory_create_requisition") },
                onPdfDownload = { },
                onConvertToPO = { onNavigate("inventory_create_purchase_order") }
            )
        }

        "inventory_create_requisition" -> CreateRequisitionScreen(
            onClose = {
                onRequisitionIdForDetailSelected(null)
                onGoBack()
            },
            editRequisitionId = selectedRequisitionIdForDetail,
            onSuccessSubmitted = {
                onRequisitionIdForDetailSelected(null)
                onGoBack()
            }
        )

        "inventory_purchase_orders", "inventory_procurement_purchase_order", "inventory_procurement_purchase_orders", "inventory_procurement_orders" -> {
            POListScreen(
                viewModel = inventoryViewModel,
                onNavigateToCreate = {
                    selectedPurchaseOrderForEdit = null
                    onNavigate("inventory_create_purchase_order_flow")
                },
                onNavigateToDetail = { po ->
                    onPurchaseOrderForDetailSelected(po)
                    onNavigate("inventory_purchase_order_detail_flow")
                },
                onClose = onGoBack
            )
        }

        "inventory_purchase_order_detail_flow" -> {
            selectedPurchaseOrderForDetail?.let { po ->
                PODetailScreen(
                    order = po,
                    viewModel = inventoryViewModel,
                    onEdit = {
                        selectedPurchaseOrderForEdit = po
                        onNavigate("inventory_create_purchase_order_flow")
                    },
                    onClose = {
                        onPurchaseOrderForDetailSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "inventory_create_purchase_order_flow" -> {
            POCreateScreen(
                viewModel = inventoryViewModel,
                existingPo = selectedPurchaseOrderForEdit,
                onDismiss = {
                    selectedPurchaseOrderForEdit = null
                    onGoBack()
                }
            )
        }

        "inventory_purchase_receive", "inventory_procurement_purchase_receive" -> AllOrdersScreen(
            onClose = onGoBack,
            onCreateOrderClick = { onNavigate("inventory_create_purchase_order") },
            onOrderClick = { poId, _ ->
                onReceivePoIdSelected(poId)
                onNavigate("inventory_purchase_receive_detail")
            }
        )

        "inventory_purchase_receive_detail" -> ReceivePurchaseDetailScreen(
            poId = selectedReceivePoId.orEmpty(),
            onClose = {
                onReceivePoIdSelected(null)
                onGoBack()
            },
            onEditClick = { },
            onPreviewPdfClick = { onNavigate("inventory_purchase_receive_preview_pdf") },
            onConvertToBillSuccess = { onNavigate("inventory_purchase_new_bill") }
        )

        "inventory_purchase_new_bill" -> NewBillScreen(
            onClose = onGoBack,
            onCancel = onGoBack,
            onSave = onGoBack
        )

        "inventory_purchase_receive_preview_pdf" -> PreviewPdfScreen(
            onClose = onGoBack,
            onEditClick = { onNavigate("inventory_purchase_receive_detail") },
            onConvertToBillClick = { onNavigate("inventory_purchase_new_bill") }
        )

        "inventory_all_bills", "inventory_procurement_bill_list", "inventory_procurement_bills_list", "inventory_bills_list", "inventory_bill_list" -> AllBillListScreen(
            onClose = onGoBack,
            onCreateOrder = { onNavigate("inventory_create_purchase_order") },
            onInvoiceClick = { billItem ->
                if (billItem.id.isNotBlank()) {
                    onInvoiceSelected(billItem.id)
                    onNavigate("inventory_payable_purchase_detail")
                }
            },
            onOptionsClick = { }
        )

        "inventory_payable_purchase_detail" -> {
            selectedInvoiceId?.let { id ->
                PayablePurchaseDetailScreen(
                    billId = id,
                    onClose = {
                        onInvoiceSelected(null)
                        onGoBack()
                    },
                    onDownloadPdf = { },
                    onRecordPayment = { onNavigate("inventory_record_bill_payment") }
                )
            } ?: run { onGoBack() }
        }

        "inventory_record_bill_payment" -> {
            selectedInvoiceId?.let { id ->
                RecordBillPaymentScreen(
                    billId = id,
                    onClose = onGoBack,
                    onPaymentSuccess = onGoBack
                )
            } ?: run { onGoBack() }
        }

        "inventory_barcode", "inventory_barcode_list", "inventory_procurement_barcode", "inventory_procurement_barcode_list", "inventory_all_barcodes" -> AllBarcodesScreen(
            onClose = onGoBack,
            onCreateBarcode = {
                onBarcodeIdForDetailSelected(null)
                onNavigate("inventory_create_barcode")
            },
            onBarcodeClick = { item ->
                onBarcodeIdForDetailSelected(item.id)
                onNavigate("inventory_create_barcode")
            }
        )

        "inventory_create_barcode" -> BarcodeGeneratorScreen(
            barcodeId = selectedBarcodeIdForDetail,
            onClose = {
                onBarcodeIdForDetailSelected(null)
                onGoBack()
            },
            onBarcodeGeneratedSuccessfully = {
                onBarcodeIdForDetailSelected(null)
                onGoBack()
            }
        )

        "inventory_low_stock_alerts", "inventory_low_stock_alert" -> {
            LowStockAlertsScreen(
                onClose = onGoBack,
                onReorderClick = { item ->
                    onLowStockItemSelected(item)
                    if (item.warehouseId != null) {
                        inventoryViewModel.fetchLowStockItemDetail(item.itemId, item.warehouseId)
                    } else {
                        inventoryViewModel.setReorderItemDirectly(item)
                    }
                    onNavigate("inventory_create_purchase_order")
                },
                onCreateNewItem = { onNavigate("inventory_create_purchase_order") },
                onBreadcrumbClick = { onOpenModulesPanel("Inventory") }
            )
        }

        "inventory_create_purchase_order" -> {
            CreatePurchaseOrderScreen(
                initialItem = selectedLowStockItem,
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
                }
            )
        }

        "inventory_payables_payments" -> AllPaymentListScreen(
            onClose = onGoBack,
            onPaymentClick = { paymentId ->
                onPaymentModeSelected(paymentId)
                onNavigate("payment_mode_detail")
            }
        )

        "inventory_payments_made" -> AllInventoryPaymentScreen(
            onClose = onGoBack,
            onPaymentSelect = { paymentId ->
                if (paymentId.isNotBlank()) {
                    onPaymentModeSelected(paymentId)
                    onNavigate("inventory_payment_overview_detail")
                }
            },
            onFilterClick = { }
        )

        "inventory_payment_overview_detail" -> {
            PaymentOverviewDetailScreen(
                paymentId = selectedPaymentModeId.orEmpty(),
                viewModel = inventoryViewModel,
                onClose = {
                    onPaymentModeSelected(null)
                    onGoBack()
                },
                onEditClick = { },
                onPreviewPdfClick = { },
                onSendEmailClick = { }
            )
        }

        "inventory_credits", "inventory_procurement_credits", "inventory_payables_credits" -> AllCreditsScreen(
            onClose = onGoBack,
            onCreditClick = { },
            onOptionsClick = { }
        )

        "inventory_returns", "inventory_procurement_returns", "inventory_procurement_return", "inventory_return" -> AllReturnsScreen(
            onClose = onGoBack,
            onCreateReturnClick = {
                // If you have a create return screen, navigate here (e.g., onNavigate("create_return"))
            },
            onReturnItemClick = { returnItem ->
                // Handle click if needed
            },
            onEditRma = { },
            onPdfExport = { }
        )

        // ── Inventory Picklist Screen ──
        "inventory_picklist" -> {
            PicklistListScreen(
                viewModel = inventoryViewModel,
                onClose = onGoBack,
                onCreatePicklistClick = {
                    onPicklistIdSelected(null)
                    onNavigate("inventory_create_picklist")
                },
                onEditPicklistClick = { editId ->
                    onPicklistIdSelected(editId)
                    onNavigate("inventory_create_picklist")
                },
                onPicklistItemClick = { item ->
                    onPicklistIdSelected(item.id)
                    onNavigate("inventory_picklist_detail")
                }
            )
        }

        // ── Inventory Picklist Detail Screen ──
        "inventory_picklist_detail" -> {
            selectedPicklistId?.let { id ->
                PicklistDetailScreen(
                    picklistId = id,
                    viewModel = inventoryViewModel,
                    onClose = {
                        onPicklistIdSelected(null)
                        onGoBack()
                    },
                    onEditClick = { editId ->
                        onPicklistIdSelected(editId)
                        onNavigate("inventory_create_picklist")
                    }
                )
            } ?: run { onGoBack() }
        }


        // ── Create / Edit Picklist Screen ──
        "inventory_create_picklist" -> {
            CreatePicklistScreen(
                picklistId = selectedPicklistId,
                viewModel = inventoryViewModel,
                onClose = {
                    onGoBack()
                },
                onCancel = {
                    onGoBack()
                },
                onSuccessSaved = {
                    onGoBack()
                }
            )
        }

        // ── Packages Screen Route ──
        // ── Packages Screen Route ──
        "inventory_packages" -> {
            PackagesScreen(
                viewModel = inventoryViewModel,
                onNavigateBack = onGoBack,
                onCreatePackageClick = {
                    onPackageIdForEditSelected(null)
                    onPackageIdForDetailSelected(null)
                    onNavigate("inventory_create_package")
                },
                onPackageItemClick = { pkg ->
                    // Set ID and navigate to Detail Screen
                    onPackageIdForDetailSelected(pkg.id)
                    onNavigate("inventory_package_detail")
                },
                onEditPackageClick = { pkg ->
                    // Set ID for Edit mode and navigate to Create/Edit Screen
                    onPackageIdForDetailSelected(null)
                    onPackageIdForEditSelected(pkg.id)
                    onNavigate("inventory_create_package")
                }
            )
        }
//        "inventory_create_package" -> {
//            CreatePackageScreen(
//                viewModel = inventoryViewModel,
//                onClose = onGoBack,
//                onPackageCreated = onGoBack
//            )
//        }



        // ── 2. Create / Edit Package Screen Route ──
        "inventory_create_package" -> {
            CreatePackageScreen(
                packageId = selectedPackageIdForEdit,
                viewModel = inventoryViewModel,
                onClose = {
                    onPackageIdForEditSelected(null)
                    onGoBack()
                },
                onPackageSaved = {
                    onPackageIdForEditSelected(null)
                    onGoBack()
                }
            )
        }

        // ── 3. Package Detail Screen Route ──
        "inventory_package_detail" -> {
            selectedPackageIdForDetail?.let { id ->
                PackageDetailScreen(
                    packageId = id,
                    viewModel = inventoryViewModel,
                    onClose = {
                        onPackageIdForDetailSelected(null)
                        onGoBack()
                    },
                    onEditClick = { editId ->
                        onPackageIdForDetailSelected(null)
                        onPackageIdForEditSelected(editId)
                        onNavigate("inventory_create_package")
                    }
                )
            } ?: run { onGoBack() }
        }
        "inventory_billing" -> AllBillsScreen(
            onClose = onGoBack,
            onCreateBillClick = { onNavigate("inventory_create_bill") },
            onBillDetailsClick = { _ -> onNavigate("inventory_bill_preview") },
            onFilterClick = { }
        )

        "inventory_bill_preview" -> BillPdfPreviewScreen(
            onClose = onGoBack,
            onEditClick = { onNavigate("inventory_create_bill") },
            onConvertToBillClick = { onNavigate("inventory_create_bill") }
        )

        "inventory_create_bill" -> NewVendorInvoiceScreen(
            onBack = onGoBack,
            onCancel = onGoBack,
            onSaveDraft = onGoBack,
            onSaveAndSend = onGoBack
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HrRouter(
    screen: String,
    selectedClaimForPayment: ExpenseClaimDto? = null,
    onClaimForPaymentSelected: (ExpenseClaimDto?) -> Unit = {},
    selectedExpenseTypeId: String? = null,
    onExpenseTypeIdSelected: (String?) -> Unit = {},
    hrViewModel: HrViewModel,
    selectedExpenseClaimId: String? = null,
    onExpenseClaimIdSelected: (String?) -> Unit = {},
    selectedDocumentCategoryId: String? = null,
    onDocumentCategoryIdSelected: (String?) -> Unit = {},
    selectedDocumentId: String? = null,
    onDocumentIdSelected: (String?) -> Unit = {},
    selectedSalaryTemplateId: String? = null,
    onSalaryTemplateIdSelected: (String?) -> Unit = {},
    employeeScreenMode: ScreenMode,
    onEmployeeScreenModeChange: (ScreenMode) -> Unit,
    selectedEmployeeId: String?,
    selectedTrainingAssignmentId: String? = null,
    onTrainingAssignmentIdSelected: (String?) -> Unit = {},
    selectedTrainingProgramId: String? = null,
    onTrainingProgramIdSelected: (String?) -> Unit = {},
    onEmployeeIdSelected: (String?) -> Unit,
    selectedSalaryComponentId: String? = null,
    onSalaryComponentIdSelected: (String?) -> Unit = {},
    selectedAttendanceId: String?,
    onAttendanceIdSelected: (String?) -> Unit,
    selectedShiftId: String?,
    onShiftIdSelected: (String?) -> Unit,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    when (screen) {
        // HR Dashboard screen
        "hr_dashboard" -> TeamOperationsDashboardScreen()
        "hr_all_employees", "hr_employee_management" -> AllEmployeesScreen(
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
            onNavigateToEdit = { editMemberId ->
                onEmployeeScreenModeChange(ScreenMode.EDIT)
                onEmployeeIdSelected(editMemberId)
            },
            hrViewModel = hrViewModel
        )
        // Organization Structure screen (opened from "All Employees" sub-item)
        "hr_organization_structure" -> OrganizationStructureScreen(
            hrViewModel = hrViewModel,
            onClose = onGoBack
        )

        "hr_daily_attendance", "hr_attendance" -> AttendanceListScreen(
            onClose = onGoBack,
            onManualEntryClick = { onNavigate("hr_attendance_manual_entry") },
            viewModel = hrViewModel
        )

        "hr_attendance_manual_entry" -> ManualAttendanceEntryScreen(
            onClose = onGoBack,
            onSubmit = { onGoBack() },
            viewModel = hrViewModel
        )

        "hr_attendance_shift_management", "hr_shift_list" -> ShiftListOverviewScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onAddShiftClick = {
                onShiftIdSelected(null)
                onNavigate("hr_create_shift")
            },
            onEditShift = { shift ->
                onShiftIdSelected(shift.id)
                onNavigate("hr_create_shift")
            }
        )

        "hr_create_shift" -> CreateNewListScreen(
            viewModel = hrViewModel,
            shiftIdToEdit = selectedShiftId,
            onClose = {
                onShiftIdSelected(null)
                onGoBack()
            },
            onSubmitSuccess = {
                onShiftIdSelected(null)
                onGoBack()
            }
        )

        "hr_monthly_attendance" -> AttendanceCalendarScreen(
            onClose = onGoBack
        )

        "hr_attendance_detail" -> AttendanceDetailScreen(
            onClose = {
                onAttendanceIdSelected(null)
                onGoBack()
            },
            onBreadCrumbClick = { onOpenModulesPanel("HR") },
            onHistoryClick = { }
        )

        "hr_leave_approval" -> LeaveApprovalScreen(
            onClose = onGoBack,
            viewModel = hrViewModel
        )

        "hr_apply_leave" -> ApplyLeaveScreen(
            onClose = onGoBack,
            viewModel = hrViewModel
        )

        "hr_payroll_salary_structure", "hr_payroll_salary_components" -> SalaryComponentListScreen(
            onClose = onGoBack,
            viewModel = hrViewModel,
            onCreateComponentClick = {
                // Clear ID for Create Mode and navigate
                onSalaryComponentIdSelected(null)
                onNavigate("hr_payroll_salary_components_create")
            },
            onEditComponentClick = { componentId ->
                // Set ID for Edit Mode (triggers view-one API and prefill)
                onSalaryComponentIdSelected(componentId)
                onNavigate("hr_payroll_salary_components_create")
            },
            onViewComponentClick = { componentId ->
                // Set ID for View Mode
                onSalaryComponentIdSelected(componentId)
                onNavigate("hr_payroll_salary_components_create")
            }
        )

        "hr_payroll_salary_components_create" -> SalaryComponentFormScreen(
            componentId = selectedSalaryComponentId,
            onClose = {
                onSalaryComponentIdSelected(null)
                onGoBack()
            },
            viewModel = hrViewModel
        )

        // ─────────────────────────────────────────────
        // SALARY TEMPLATES LIST SCREEN
        // ─────────────────────────────────────────────
        "hr_payroll_salary_templates" -> SalaryTemplateListScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onCreateTemplateClick = {
                // Clear ID for create mode and navigate
                onSalaryTemplateIdSelected(null)
                onNavigate("hr_payroll_salary_templates_form")
            },
            onEditTemplateClick = { templateId ->
                // Set ID for edit mode (triggers view-one API) and navigate
                onSalaryTemplateIdSelected(templateId)
                onNavigate("hr_payroll_salary_templates_form")
            },
            onViewTemplateClick = { templateId ->
                // Set ID for view mode and navigate
                onSalaryTemplateIdSelected(templateId)
                onNavigate("hr_payroll_salary_templates_form")
            }
        )

        // ─────────────────────────────────────────────
        // SALARY TEMPLATE FORM SCREEN (CREATE / EDIT)
        // ─────────────────────────────────────────────
        "hr_payroll_salary_templates_form" -> SalaryTemplateFormScreen(
            templateId = selectedSalaryTemplateId,
            viewModel = hrViewModel,
            onClose = {
                onSalaryTemplateIdSelected(null)
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────
        // TRAINING PROGRAM LIST SCREEN (VIEW ALL & DELETE)
        // ─────────────────────────────────────────────
        "hr_training_management" -> TrainingProgramListScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onAddProgramClick = {
                // Clear selected program ID for create mode and navigate
                onTrainingProgramIdSelected(null)
                onNavigate("hr_add_edit_program")
            },
            onEditProgramClick = { programId ->
                // Set selected program ID for edit mode (triggers view-one API) and navigate
                onTrainingProgramIdSelected(programId)
                onNavigate("hr_add_edit_program")
            }
        )

        // ─────────────────────────────────────────────
        // ADD / EDIT PROGRAM SCREEN (VIEW ONE / CREATE / UPDATE)
        // ─────────────────────────────────────────────
        "hr_add_edit_program" -> AddEditProgramScreen(
            programId = selectedTrainingProgramId,
            viewModel = hrViewModel,
            onClose = {
                onTrainingProgramIdSelected(null)
                onGoBack()
            },
            onSaveSuccess = {
                onTrainingProgramIdSelected(null)
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────
        // TRAINING ASSIGNMENT LIST SCREEN (VIEW ALL & DELETE)
        // ─────────────────────────────────────────────
        "hr_training_assignment_list" -> TrainingAssignmentListScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onAssignTrainingClick = {
                onTrainingAssignmentIdSelected(null)
                onNavigate("hr_assign_training_form")
            },
            onEditAssignmentClick = { id ->
                onTrainingAssignmentIdSelected(id)
                onNavigate("hr_assign_training_form")
            }
        )

        // ── Document Management Routes ──
        "hr_employee_document_upload" -> EmployeeDocumentListScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onNavigateToUpload = {
                // Clear ID for Create Mode
                onDocumentIdSelected(null)
                onNavigate("hr_new_document_upload")
            },
            onEditDocument = { docId ->
                // Set ID for Edit Mode (triggers view-one and prefill)
                onDocumentIdSelected(docId)
                onNavigate("hr_new_document_upload")
            }
        )

        "hr_new_document_upload" -> EmployeeDocumentUploadScreen(
            documentId = selectedDocumentId,
            viewModel = hrViewModel,
            onClose = {
                onDocumentIdSelected(null)
                onGoBack()
            }
        )

        // ── Document Category Routes ──
        "hr_document_categories" -> DocumentCategoryListScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onNavigateToAddCategory = {
                onDocumentCategoryIdSelected(null)
                onNavigate("hr_add_document_category")
            },
            onEditCategory = { categoryId ->
                onDocumentCategoryIdSelected(categoryId)
                onNavigate("hr_add_document_category")
            }
        )

        "hr_add_document_category" -> AddDocumentCategoryScreen(
            categoryId = selectedDocumentCategoryId,
            viewModel = hrViewModel,
            onClose = {
                onDocumentCategoryIdSelected(null)
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────
        // ASSIGN TRAINING SCREEN (VIEW ONE / CREATE / UPDATE)
        // ─────────────────────────────────────────────
        "hr_assign_training_form" -> AssignTrainingScreen(
            assignmentId = selectedTrainingAssignmentId,
            viewModel = hrViewModel,
            onClose = {
                onTrainingAssignmentIdSelected(null)
                onGoBack()
            },
            onAssignSuccess = {
                onTrainingAssignmentIdSelected(null)
                onGoBack()
            }
        )

        // ─────────────────────────────────────────────
        // EXPENSE CLAIMS & APPROVALS SCREEN
        // ─────────────────────────────────────────────
        "hr_expense_claims" -> ExpenseManagementScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onViewClaimDetail = { claimId ->
                onExpenseClaimIdSelected(claimId)
                onNavigate("hr_expense_claim_detail")
            }
        )

        // Read-only Details without Manager Approval actions
        "hr_expense_claim_detail_readonly" -> {
            selectedExpenseClaimId?.takeIf { it.isNotBlank() }?.let { id ->
                ExpenseClaimDetailScreen(
                    claimId = id,
                    title = "Expense Details",      // Set header to Expense Details
                    showApprovalActions = false,    // Hide approval actions
                    viewModel = hrViewModel,
                    onClose = {
                        onExpenseClaimIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        // Full Details with Manager Approval actions
        "hr_expense_claim_detail" -> {
            selectedExpenseClaimId?.takeIf { it.isNotBlank() }?.let { id ->
                ExpenseClaimDetailScreen(
                    claimId = id,
                    title = "Expense Details",
                    showApprovalActions = true,
                    viewModel = hrViewModel,
                    onClose = {
                        onExpenseClaimIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        // ─────────────────────────────────────────────
        // EXPENSE TYPE LIST
        // ─────────────────────────────────────────────
        "hr_expense_type" -> ExpenseTypeScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onExpenseTypeClick = { type ->
                onExpenseClaimIdSelected(type.id)
                onNavigate("hr_expense_claim_detail_readonly")
            },
            onAddNewExpenseType = {
                onExpenseTypeIdSelected(null)
                onNavigate("hr_add_expense_type")
            },
            onEditExpenseType = { id ->
                onExpenseTypeIdSelected(id)
                onNavigate("hr_add_expense_type")
            }
        )

        // ─────────────────────────────────────────────
        // ADD / EDIT EXPENSE TYPE SCREEN
        // ─────────────────────────────────────────────
        "hr_add_expense_type" -> AddExpenseTypeScreen(
            expenseTypeId = selectedExpenseTypeId,
            viewModel = hrViewModel,
            onClose = {
                onExpenseTypeIdSelected(null)
                onGoBack()
            },
            onSavedSuccess = {
                onExpenseTypeIdSelected(null)
                onGoBack()
            }
        )
        // ─────────────────────────────────────────────
        // FINANCE REIMBURSEMENT SCREEN
        // ─────────────────────────────────────────────
        "hr_expense_finance_reimbursement" -> {
            FinanceReimbursementScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onViewDetail = { claim ->
                    onExpenseClaimIdSelected(claim.id)
                    onNavigate("hr_expense_claim_detail_readonly")
                },
                onProcessPayClick = { claim ->
                    onClaimForPaymentSelected(claim)
                    onNavigate("hr_expense_process_payment")
                }
            )
        }

        // ─────────────────────────────────────────────
        // PROCESS PAYMENT STANDALONE SCREEN
        // ─────────────────────────────────────────────
        "hr_expense_process_payment" -> {
            selectedClaimForPayment?.let { claim ->
                ProcessPaymentScreen(
                    claim = claim,
                    viewModel = hrViewModel,
                    onClose = {
                        onClaimForPaymentSelected(null)
                        onGoBack()
                    },
                    onPaymentSuccess = {
                        onClaimForPaymentSelected(null)
                        hrViewModel.fetchReimbursementClaims()
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        // ─────────────────────────────────────────────
        // NEW CLAIMS LIST (View goes to details without approvals)
        // ─────────────────────────────────────────────
        "hr_expense_new_claim" -> NewClaimsScreen(
            viewModel = hrViewModel,
            onClose = onGoBack,
            onClaimClick = { claim ->
                onExpenseClaimIdSelected(claim.id)
                onNavigate("hr_expense_claim_detail_readonly")
            },
            onEditClaimClick = { claim ->
                onExpenseClaimIdSelected(claim.id)
                onNavigate("hr_edit_expense_claim") // Navigates to Edit Expense Claim Screen
            },
            onNavigateToSubmitClaim = {
                onExpenseClaimIdSelected(null)
                onNavigate("hr_submit_expense_claim") // Navigates to Submit Expense Claim Screen
            }
        )

        "hr_submit_expense_claim" -> SubmitExpenseClaimScreen(
            hrViewModel = hrViewModel,
            onClose = onGoBack,
            onSubmitSuccess = {
                hrViewModel.fetchExpenseClaims(page = 1, limit = 10, status = null, search = null)
                onGoBack()
            }
        )

        "hr_edit_expense_claim" -> {
            selectedExpenseClaimId?.let { id ->
                EditExpenseClaimScreen(
                    claimId = id,
                    hrViewModel = hrViewModel,
                    onClose = {
                        onExpenseClaimIdSelected(null)
                        onGoBack()
                    },
                    onSubmitSuccess = {
                        onExpenseClaimIdSelected(null)
                        hrViewModel.fetchExpenseClaims(page = 1, limit = 10, status = null, search = null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }
    }
}



@Composable
private fun LogisticsRouter(
    screen: String,
    onOrderIdSelected: (String?) -> Unit,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    when (screen) {
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
    }
}

@Composable
private fun LegalRouter(
    screen: String,
    hrViewModel: HrViewModel,
    branchViewModel: BranchViewModel,
    selectedContractId: String?,
    onContractIdSelected: (String?) -> Unit,
    selectedComplianceId: String? = null,
    onComplianceIdSelected: (String?) -> Unit = {},
    selectedLegalDocumentId: String? = null,
    onLegalDocumentIdSelected: (String?) -> Unit = {},
    selectedLegalLicenseId: String? = null,
    onLegalLicenseIdSelected: (String?) -> Unit = {},
    selectedLegalCustomerId: String? = null,
    onLegalCustomerIdSelected: (String?) -> Unit = {},
    selectedLegalEmployeeId: String? = null,
    onLegalEmployeeIdSelected: (String?) -> Unit = {},
    legalViewModel: LegalViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit
) {
    when (screen) {
        "legal_dashboard" -> LegalDashboardScreen()

        "legal_contracts" -> LegalContractsScreen(
            onBack = onGoBack,
            onCreateContract = {
                onContractIdSelected(null)
                onNavigate("legal_create_contract")
            },
            onContractClick = { contractId ->
                if (contractId.isNotBlank()) {
                    onContractIdSelected(contractId)
                    onNavigate("legal_contract_detail")
                }
            },
            onEditContract = { contractId ->
                if (contractId.isNotBlank()) {
                    onContractIdSelected(contractId)
                    onNavigate("legal_create_contract")
                }
            }
        )

        "legal_contract_detail" -> {
            selectedContractId?.takeIf { it.isNotBlank() }?.let { validId ->
                ContractOverviewScreen(
                    contractId = validId,
                    onClose = {
                        onContractIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run {
                onGoBack()
            }
        }

        "legal_create_contract" -> {
            CreateLegalContractScreen(
                contractIdToEdit = selectedContractId,
                onClose = {
                    onContractIdSelected(null)
                    onGoBack()
                }
            )
        }

        "legal_compliance" -> {
            LegalComplianceScreen(
                onBack = onGoBack,
                onAddCompliance = {
                    onComplianceIdSelected(null)
                    onNavigate("legal_create_compliance")
                },
                onComplianceClick = { compliance ->
                    if (compliance.id.isNotBlank()) {
                        onComplianceIdSelected(compliance.id)
                        onNavigate("legal_compliance_detail")
                    }
                },
                onEditCompliance = { complianceId ->
                    if (complianceId.isNotBlank()) {
                        onComplianceIdSelected(complianceId)
                        onNavigate("legal_create_compliance")
                    }
                }
            )
        }

        "legal_compliance_detail" -> {
            selectedComplianceId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalComplianceDetailScreen(
                    complianceId = validId,
                    onClose = {
                        onComplianceIdSelected(null)
                        onGoBack()
                    },
                    onEdit = { id ->
                        if (id.isNotBlank()) {
                            onComplianceIdSelected(id)
                            onNavigate("legal_create_compliance")
                        }
                    }
                )
            } ?: run {
                onGoBack()
            }
        }

        "legal_create_compliance" -> {
            CreateLegalComplianceScreen(
                complianceIdToEdit = selectedComplianceId,
                onClose = {
//                    onComplianceIdSelected(null)
                    onGoBack()
                }
            )
        }

        "legal_documents" -> {
            LegalDocumentsScreen(
                onBack = onGoBack,
                onAddDocument = {
                    onLegalDocumentIdSelected(null)
                    onNavigate("legal_create_document")
                },
                onDocumentClick = { doc ->
                    if (doc.id.isNotBlank()) {
                        onLegalDocumentIdSelected(doc.id)
                        onNavigate("legal_document_detail")
                    }
                },
                onEditDocument = { docId ->
                    if (docId.isNotBlank()) {
                        onLegalDocumentIdSelected(docId)
                        onNavigate("legal_create_document")
                    }
                }
            )
        }

        "legal_document_detail" -> {
            selectedLegalDocumentId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalDocumentDetailScreen(
                    documentId = validId,
                    onClose = {
                        onLegalDocumentIdSelected(null)
                        onGoBack()
                    },
                    onEdit = { id ->
                        if (id.isNotBlank()) {
                            onLegalDocumentIdSelected(id)
                            onNavigate("legal_create_document")
                        }
                    }
                )
            } ?: run {
                onGoBack()
            }
        }

        "legal_create_document" -> {
            CreateUpdateLegalDocumentScreen(
                documentIdToEdit = selectedLegalDocumentId,
                onClose = {
                    onGoBack()
                }
            )
        }

        // ── Legal License List ──
        "legal_licenses" -> {
            LegalLicenseScreen(
                onBack = onGoBack,
                onAddLicense = {
                    onLegalLicenseIdSelected(null)
                    onNavigate("legal_create_license")
                },
                onLicenseClick = { license ->
                    if (license.id.isNotBlank()) {
                        onLegalLicenseIdSelected(license.id)
                        onNavigate("legal_license_detail")
                    }
                },
                onEditLicense = { licenseId ->
                    if (licenseId.isNotBlank()) {
                        onLegalLicenseIdSelected(licenseId)
                        onNavigate("legal_create_license")
                    }
                }
            )
        }

        // ── Legal License Detail (Protected against null ID) ──
        "legal_license_detail" -> {
            selectedLegalLicenseId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalLicenseDetailScreen(
                    licenseId = validId,
                    onClose = {
                        onLegalLicenseIdSelected(null)
                        onGoBack()
                    },
                    onEditLicense = { id ->
                        if (id.isNotBlank()) {
                            onLegalLicenseIdSelected(id)
                            onNavigate("legal_create_license")
                        }
                    }
                )
            } ?: run {
                onGoBack()
            }
        }

        // ── Legal License Create / Edit ──
        "legal_create_license" -> {
            CreateLegalLicenseScreen(
                licenseIdToEdit = selectedLegalLicenseId,
                onClose = {
                    onLegalLicenseIdSelected(null)
                    onGoBack()
                }
            )
        }

        "legal_customer" -> {
            LegalCustomerListScreen(
                onBack = onGoBack,
                onAddCustomerRecord = {
                    onLegalCustomerIdSelected(null)
                    onNavigate("legal_create_customer_record")
                },
                onRecordClick = { item ->
                    if (item.id.isNotBlank()) {
                        onLegalCustomerIdSelected(item.id)
                        onNavigate("legal_customer_detail")
                    }
                },
                onEditRecord = { id ->
                    if (id.isNotBlank()) {
                        onLegalCustomerIdSelected(id)
                        onNavigate("legal_create_customer_record")
                    }
                }
            )
        }

        "legal_customer_detail" -> {
            selectedLegalCustomerId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalCustomerDetailScreen(
                    recordId = validId,
                    onClose = {
                        onLegalCustomerIdSelected(null)
                        onGoBack()
                    },
                    onEditRecord = { id ->
                        if (id.isNotBlank()) {
                            onLegalCustomerIdSelected(id)
                            onNavigate("legal_create_customer_record")
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_customer_record" -> {
            CreateLegalCustomerScreen(
                recordIdToEdit = selectedLegalCustomerId,
                onClose = {
//                    onLegalCustomerIdSelected(null)
                    onGoBack()
                }
            )
        }

        // ── Employee & Labour Legal List ──
        "legal_employee" -> {
            LegalEmployeeListScreen(
                viewModel = legalViewModel,
                onNavigateBack = onGoBack,
                onNavigateToCreate = {
                    onLegalEmployeeIdSelected(null)
                    onNavigate("legal_create_employee")
                },
                onNavigateToEdit = { employeeId ->
                    if (employeeId.isNotBlank()) {
                        onLegalEmployeeIdSelected(employeeId)
                        onNavigate("legal_create_employee")
                    }
                },
                onNavigateToDetail = { employeeId ->
                    if (employeeId.isNotBlank()) {
                        onLegalEmployeeIdSelected(employeeId)
                        onNavigate("legal_employee_detail")
                    }
                }
            )
        }

        // ── Employee & Labour Legal Detail (Null-safe guard) ──
        "legal_employee_detail" -> {
            selectedLegalEmployeeId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalEmployeeDetailScreen(
                    recordId = validId,
                    viewModel = legalViewModel,
                    hrViewModel = hrViewModel, // Pass hrViewModel to resolve member names
                    onNavigateBack = {
                        onLegalEmployeeIdSelected(null)
                        onGoBack()
                    },
                    onNavigateToEdit = { id ->
                        if (id.isNotBlank()) {
                            onLegalEmployeeIdSelected(id)
                            onNavigate("legal_create_employee")
                        }
                    }
                )
            } ?: run {
                onGoBack()
            }
        }

        // ── Employee & Labour Legal Create / Edit ──
        "legal_create_employee" -> {
            val departmentViewModel: DepartmentViewModel = hiltViewModel()
            LegalEmployeeCreateEditScreen(
                idToEdit = selectedLegalEmployeeId,
                viewModel = legalViewModel,
                hrViewModel = hrViewModel,
                branchViewModel = branchViewModel,
                departmentViewModel = departmentViewModel,
                onNavigateBack = {
                    onLegalEmployeeIdSelected(null)
                    onGoBack()
                }
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ServicesRouter(
    screen: String,
    hrViewModel: HrViewModel,
    servicesViewModel: ServicesViewModel,
    selectedAssignmentJobCardId: String? = null,
    onAssignmentJobCardIdSelected: (String?) -> Unit = {},
    selectedComplaintId: String? = null,
    onComplaintIdSelected: (String?) -> Unit = {},
    selectedAlterationId: String? = null,
    onAlterationIdSelected: (String?) -> Unit = {},
    selectedServiceRequestId: String?,
    onServiceRequestIdSelected: (String?) -> Unit,
    navController: NavHostController,
    selectedManagementOrderId: String?,
    onManagementOrderIdSelected: (String?) -> Unit,
    selectedOrderId: String?,
    onOrderIdSelected: (String?) -> Unit,
    onEditOrderIdChange: (String?) -> Unit,
    selectedFeedbackId: String?,
    onFeedbackIdSelected: (String?) -> Unit,
    selectedServiceTemplate: ProductionTemplateDto?,
    onServiceTemplateSelected: (ProductionTemplateDto?) -> Unit,
    pendingOrderReviewData: OrderReviewData?,
    onPendingOrderReviewDataChange: (OrderReviewData?) -> Unit,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onPopToScreen: (String) -> Unit = {},
    onOpenDrawer: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    when (screen) {
        "services_dashboard" -> ServicesDashboardScreen(
            onNavigateToRequests = { onNavigate("services_service_request") },
            onNavigateToComplaints = { onNavigate("services_customer_feedback") }
        )
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

        // Match "services_service_order", "services_service_orders", and "services_production_orders"
        "services_production_orders",
        "services_service_orders",
        "services_service_order" -> ServiceOrderScreen(
            navController = navController,
            onMenuClick = onOpenDrawer,
            onBack = onGoBack,
            onCreateOrder = {
                onPendingOrderReviewDataChange(null)
                onNavigate("create_order")
            },
            onViewOrder = { orderId ->
                onOrderIdSelected(orderId)
                onNavigate("service_order_overview") // Navigates to ServiceOrderOverviewScreen
            },
            onEditOrder = { orderId -> onEditOrderIdChange(orderId) },
            onBreadCrumbClick = { onOpenModulesPanel("Services") }
        )
        // ── Production Works Screen ──
        "services_production_works" -> ProductionWorksScreen(
            onClose = onGoBack,
            onOrderClick = { orderId ->
                // Ensure orderId is never null or blank before navigating
                if (orderId.isNotBlank()) {
                    onOrderIdSelected(orderId)
                    onNavigate("services_production_pipeline")
                }
            },
            viewModel = servicesViewModel
        )

        // ── Production Pipeline Screen ──
        "services_production_pipeline" -> {
            val orderIdToLoad = selectedOrderId?.takeIf { it.isNotBlank() }
            if (orderIdToLoad != null) {
                OrderProductionPipelineScreen(
                    orderId = orderIdToLoad,
                    viewModel = servicesViewModel,
                    onBack = {
                        onOrderIdSelected(null)
                        onGoBack()
                    },
                    onVerifyClick = { id ->
                        onOrderIdSelected(id)
                        onNavigate("services_qc_delivery")
                    },
                    onGoToAssignmentClick = { id, jobCardId ->
                        // Keep the order id and remember which garment to open for assignment
                        onOrderIdSelected(id)
                        onAssignmentJobCardIdSelected(jobCardId)
                        onNavigate("services_assignment_mode")
                    }
                )
            } else {
                // If ID is missing, safely pop back to prevent blank screen
                onGoBack()
            }
        }

        // ── Assignment Mode: opens Assign Garment Production for one job card ──
        "services_assignment_mode" -> {
            val orderIdToLoad = selectedOrderId?.takeIf { it.isNotBlank() }
            val jobCardId = selectedAssignmentJobCardId?.takeIf { it.isNotBlank() }
            if (orderIdToLoad != null && jobCardId != null) {
                ServiceOrderOverviewScreen(
                    orderId = orderIdToLoad,
                    initialAssignJobCardId = jobCardId,
                    // Back arrow / back gesture returns to Production Details
                    onClose = onGoBack,
                    // "Assign & Route to Workshop" success returns to Production Details
                    onAssignmentRouted = onGoBack,
                    onCreateNew = {
                        onPendingOrderReviewDataChange(null)
                        onNavigate("create_order")
                    }
                )
            } else {
                // Missing ids: safely pop back to avoid a blank screen
                onGoBack()
            }
        }

        // ── QC & Delivery Dispatch Screen ──
        "services_qc_delivery" -> {
            val orderIdToLoad = selectedOrderId?.takeIf { it.isNotBlank() }
            if (orderIdToLoad != null) {
                QualityDeliveryDispatchScreen(
                    orderId = orderIdToLoad,
                    viewModel = servicesViewModel,
                    onBack = onGoBack,
                    onBackToOrders = {
                        // Pop QC and pipeline screens in one step and land on Production Works.
                        // selectedOrderId is intentionally NOT cleared here: clearing it while
                        // the QC/pipeline routes are still composing triggers their
                        // "missing id" fallback (onGoBack) and causes the blank screen.
                        // Production Works sets it again on the next order click.
                        onPopToScreen("services_production_works")
                    }
                )
            } else {
                onGoBack()
            }
        }

        "service_order_overview" -> {
            selectedOrderId?.let { id ->
                ServiceOrderOverviewScreen(
                    orderId = id,
                    onClose = onGoBack,
                    onCreateNew = {
                        onPendingOrderReviewDataChange(null)
                        onNavigate("create_order")
                    }
                )
            } ?: run { onGoBack() }
        }

        "services_service_request" -> ServiceRequestScreen(
            onClose = onGoBack,
            onCreateNewRequest = { onNavigate("create_request") },
            onViewClick = { requestId ->
                onServiceRequestIdSelected(requestId)
                onNavigate("review_services")
            }
        )

// Create Service Request Route with Review Navigation Callback
        "create_request" -> CreateServiceRequest(
            onClose = onGoBack,
            onCancel = onGoBack,
            onNavigateToReview = { onNavigate("review_service_request") }
        )

// In-Progress Creation Review Screen (Preserves Form Data)
        "review_service_request" -> ReviewServiceRequestScreen(
            onBackToEdit = onGoBack,
            onSuccessSubmitted = {
                onGoBack()
                onGoBack()
            }
        )

// View Existing Service Request Details
        "review_services" -> {
            if (!selectedServiceRequestId.isNullOrBlank()) {
                ServiceRequestDetailsScreen(
                    requestId = selectedServiceRequestId,
                    onBack = {
                        onServiceRequestIdSelected(null)
                        onGoBack()
                    }
                )
            } else {
                onGoBack()
            }
        }

        "services_alteration_management" -> {
            val servicesViewModel: ServicesViewModel = hiltViewModel()

            AlterationManagementScreen(
                viewModel = servicesViewModel,
                onClose = onGoBack,
                onCreateNewAlteration = {
                    onAlterationIdSelected(null)
                    onNavigate("create_alteration")
                },
                onBreadcrumbClick = { onOpenModulesPanel("Services") },
                onItemClick = { item ->
                    if (item.id.isNotBlank()) {
                        onAlterationIdSelected(item.id)
                        onNavigate("view_alteration")
                    }
                }
            )
        }

        // Handles both create and view cleanly without 'CreateAlterationManagementScreen'
        "view_alteration", "create_alteration" -> {
            selectedAlterationId?.takeIf { it.isNotBlank() }?.let { validId ->
                val servicesViewModel: ServicesViewModel = hiltViewModel()
                ViewAlterationManagementScreen(
                    alterationId = validId,
                    viewModel = servicesViewModel,
                    onClose = {
                        onAlterationIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run {
                onGoBack()
            }
        }

//        "services_customer_feedback" -> CustomerFeedbackScreen(
//            onDismiss = onGoBack,
//            onView = { feedbackId ->
//                onFeedbackIdSelected(feedbackId)
//                onNavigate("feedback_detail")
//            },
//            onEdit = { },
//            onDelete = { },
//            onBreadCrumbClick = { onOpenModulesPanel("Services") }
//        )



        "services_service_templates" -> ServiceTemplateListScreen(
            onClose = onGoBack,
            onAddNewTemplate = {
                android.util.Log.d("TEMPLATE_NAV_DEBUG", ">> Add New Clicked: clearing selected template")
                onServiceTemplateSelected(null)
                onNavigate("services_create_service_template")
            },
            onViewTemplate = { templateItem ->
                android.util.Log.d("TEMPLATE_NAV_DEBUG", ">> View Template Clicked: id='${templateItem.id}', name='${templateItem.name}'")
                onServiceTemplateSelected(templateItem)
                onNavigate("services_view_service_template")
            },
            onEditTemplate = { templateItem ->
                android.util.Log.d("TEMPLATE_NAV_DEBUG", ">> Edit Template Clicked: id='${templateItem.id}', name='${templateItem.name}'")
                onServiceTemplateSelected(templateItem)
                onNavigate("services_create_service_template")
            }
        )

        "services_view_service_template" -> {
            selectedServiceTemplate?.id?.let { id ->
                ServiceTemplateDetailViewScreen(
                    templateId = id,
                    onClose = {
                        onServiceTemplateSelected(null)
                        onGoBack()
                    },
                    onDeactivate = {
                        onServiceTemplateSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "services_create_service_template" -> {
            android.util.Log.d("TEMPLATE_NAV_DEBUG", ">> Navigating to Create/Edit Screen with templateId: '${selectedServiceTemplate?.id}'")
            CreateServiceTemplateWizardScreen(
                templateIdToEdit = selectedServiceTemplate?.id,
                onClose = {
                    onServiceTemplateSelected(null)
                    onGoBack()
                },
                onTemplateCreated = {
                    onServiceTemplateSelected(null)
                    onGoBack()
                }
            )
        }

        // ── Customer Complaints Screen ──
        "services_customer_complaints" -> {
            ComplaintListScreen(
                viewModel = servicesViewModel,
                onBack = onGoBack,
                onComplaintClick = { complaintId ->
                    onComplaintIdSelected(complaintId)
                    onNavigate("complaint_detail")
                }
            )
        }

        // In HomeScreenRouter.kt inside ServicesRouter:
        "complaint_detail" -> {
            selectedComplaintId?.let { id ->
                ComplaintDetailViewScreen(
                    complaintId = id,
                    viewModel = servicesViewModel,
                    hrViewModel = hrViewModel, // 👈 Pass hrViewModel here
                    onBack = {
                        onComplaintIdSelected(null)
                        onGoBack()
                    },
                    onEditClick = { }
                )
            } ?: run { onGoBack() }
        }
        // ── Customer Feedback Screen ──
        "services_customer_feedback" -> {
            CustomerFeedbackScreen(
                viewModel = servicesViewModel,
                onBack = onGoBack,
                onFeedbackClick = { feedbackId ->
                    onFeedbackIdSelected(feedbackId)
                    onNavigate("feedback_detail")
                }
            )
        }

        // ── Customer Feedback Detail Screen ──
        "feedback_detail" -> {
            selectedFeedbackId?.let { id ->
                FeedbackDetailScreen(
                    feedbackId = id,
                    viewModel = servicesViewModel,
                    onDismiss = {
                        onFeedbackIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run {
                onGoBack()
            }
        }
    }
}

@Composable
private fun ReportsRouter(
    screen: String,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    when (screen) {
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
    }
}
