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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import com.cuso.tailor.view.home.paywall.LocalScreenPermissions
import com.cuso.tailor.view.home.paywall.ScreenActionPermissions
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
import com.cuso.tailor.model.settings.TaxRateItemDto
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
import com.cuso.tailor.view.home.logistics.delivery_management.delivery.DeliveryDetailScreen
import com.cuso.tailor.view.home.logistics.order_tracking.TrackingOverviewScreen
import com.cuso.tailor.view.home.opening_balance.OpeningBalancesScreen
import com.cuso.tailor.view.home.profile_settings.ProfileSettingsScreen
import com.cuso.tailor.view.home.profile_settings.all_settings.ModuleSettingsScreen
import com.cuso.tailor.view.home.profile_settings.all_settings.OrganizationSettingsScreen
import com.cuso.tailor.view.home.profile_settings.all_settings.SettingsOverviewScreen
import com.cuso.tailor.view.home.profile_settings.setup_pages.BusinessSetupScreen
import com.cuso.tailor.view.home.profile_settings.setup_pages.SettingsScreen
import com.cuso.tailor.view.home.logistics.dashboard.LogisticsDashboardScreen
import com.cuso.tailor.view.home.reports.sales.SalesOrderReportsScreen
import com.cuso.tailor.view.home.reports.sales.reports.BranchWiseSalesReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.CustomerCreditReceivablesReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.CustomerDormantWinbackReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.CustomerLtvRevenueReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.CustomerMasterDirectoryReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.DeliverySlaReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.LeadFollowupActivityReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.LeadMasterRegisterReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.LeadOverdueFollowupsReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.LeadSourcePerformanceReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.MeasurementMasterRegisterReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.MeasurementStaleAuditReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.OpportunityClosingAlertsReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.OpportunityMasterRegisterReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.OpportunityStageFunnelReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.OrderMasterRegisterReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.OrderProductStreamReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.QuotationDiscountLeakageReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.QuotationExpiringAuditReportScreen
import com.cuso.tailor.view.home.reports.sales.reports.QuotationMasterRegisterReportScreen
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
import com.cuso.tailor.view.home.services.settings.template.CreateServiceTemplateWizardScreen
import com.cuso.tailor.view.home.services.settings.template.ServiceTemplateDetailViewScreen
import com.cuso.tailor.view.home.services.settings.template.ServiceTemplateListScreen
import com.cuso.tailor.view.home.subscriptions.CurrentPlanScreen
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
import com.cuso.tailor.view.home.it.all_devices.AllDeviceScreen
import com.cuso.tailor.view.home.it.all_devices.DeviceDetailScreen
import com.cuso.tailor.view.home.it.backup.BackupRecoveryDetailScreen
import com.cuso.tailor.view.home.it.backup.BackupRecoveryListScreen
import com.cuso.tailor.view.home.it.backup.RestoreBackupScreen
import com.cuso.tailor.view.home.it.dashboard.ItDashboardScreen
import com.cuso.tailor.view.home.it.pos_billing.PosDetailScreen
import com.cuso.tailor.view.home.it.pos_billing.TerminalFleetStatusScreen
import com.cuso.tailor.view.home.legal.dashboard.LegalDashboardScreen
import com.cuso.tailor.view.home.legal.legal_calendar.AuditLegalCalendarScreen
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
import com.cuso.tailor.view.home.legal.legal_notice.CreateEditLegalNoticeScreen
import com.cuso.tailor.view.home.legal.legal_notice.LegalNoticeDetailScreen
import com.cuso.tailor.view.home.legal.legal_notice.LegalNoticeListScreen
import com.cuso.tailor.view.home.reports.marketing.MarketingReportsScreen
import com.cuso.tailor.view.home.reports.finance.FinanceReportsScreen
import com.cuso.tailor.view.home.reports.inventory.InventoryReportsScreen
import com.cuso.tailor.view.home.reports.logistics.LogisticsReportsScreen
import com.cuso.tailor.view.home.reports.service.ServiceReportsScreen
import com.cuso.tailor.view.home.reports.hr.HrPayrollReportsScreen
import com.cuso.tailor.view.home.reports.it.ItReportsScreen
import com.cuso.tailor.view.home.reports.security.SecurityReportsScreen
import com.cuso.tailor.view.home.reports.legal.LegalReportsScreen
import com.cuso.tailor.view.home.reports.hr.reports.AttendanceReportScreen
import com.cuso.tailor.view.home.reports.hr.reports.PayrollRunReportScreen
import com.cuso.tailor.view.home.reports.hr.reports.PieceRateReportScreen
import com.cuso.tailor.view.home.reports.hr.reports.ReimbursementReportScreen
import com.cuso.tailor.viewmodel.ReportViewModel
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
import com.cuso.tailor.view.home.legal.legal_vendor.CreateEditLegalVendorScreen
import com.cuso.tailor.view.home.legal.legal_vendor.LegalVendorDetailScreen
import com.cuso.tailor.view.home.legal.legal_vendor.LegalVendorListScreen
import com.cuso.tailor.view.home.logistics.cod.AllCodScreen
import com.cuso.tailor.view.home.logistics.cod.CodOrderOverviewScreen
import com.cuso.tailor.view.home.logistics.courier_management.AllCourierScreen
import com.cuso.tailor.view.home.logistics.courier_management.CourierOverviewScreen
import com.cuso.tailor.view.home.logistics.courier_partners.AddEditCourierPartnerScreen
import com.cuso.tailor.view.home.logistics.courier_partners.CourierPartnerListScreen // Added Import
import com.cuso.tailor.view.home.logistics.courier_partners.CourierPartnerOverviewScreen
import com.cuso.tailor.view.home.logistics.delivery_management.delivery.DeliveryListScreen
import com.cuso.tailor.view.home.logistics.delivery_management.dispatch.DispatchListScreen
import com.cuso.tailor.view.home.logistics.order_list.OrderListScreen
import com.cuso.tailor.view.home.logistics.order_list.LogisticsOrderDetailScreen
import com.cuso.tailor.view.home.logistics.order_tracking.TrackingEventsListScreen
import com.cuso.tailor.view.home.logistics.returns.CreateReturnRequestScreen
import com.cuso.tailor.view.home.logistics.returns.LogisticsReturnScreen
import com.cuso.tailor.view.home.logistics.returns.ReturnRequestDetailsScreen
import com.cuso.tailor.view.home.logistics.shipment.CreateShipmentScreen
import com.cuso.tailor.view.home.logistics.shipment.ShipmentDetailScreen
import com.cuso.tailor.view.logistics.ShipmentListScreen
import com.cuso.tailor.view.home.it.asset_management.ItAssetDetailScreen
import com.cuso.tailor.view.home.it.asset_management.ItAssetListScreen
import com.cuso.tailor.view.home.it.asset_management.ItRegisterAssetScreen
import com.cuso.tailor.view.home.it.service_desk.ItCreateTicketScreen
import com.cuso.tailor.view.home.it.service_desk.ItServiceDeskListScreen
import com.cuso.tailor.view.home.it.service_desk.ItTicketDetailScreen
import com.cuso.tailor.view.home.marketing.advertisement.AdvertisingOverviewScreen
import com.cuso.tailor.view.home.marketing.compaign.all_compaign.AllCampaignsListScreen
import com.cuso.tailor.view.home.marketing.compaign.all_compaign.CampaignDetailOverviewScreen
import com.cuso.tailor.view.home.marketing.dashboard.MarketingDashboardScreen
import com.cuso.tailor.view.home.paywall.ModuleUpgradePaywallScreen
import com.cuso.tailor.view.home.settings.ApprovalRulesScreen
import com.cuso.tailor.view.home.settings.CreateEditApprovalRuleScreen
import com.cuso.tailor.viewmodel.MarketingViewModel
import com.cuso.tailor.model.permission.PaywallModuleConfig
import com.cuso.tailor.view.home.paywall.FeatureGuard
import com.cuso.tailor.view.home.paywall.PermissionAction
import com.cuso.tailor.model.settings.ServicesProductionStageDto
import com.cuso.tailor.view.home.finance.settings.AddTaxRateScreen
import com.cuso.tailor.view.home.finance.settings.TaxRateItem
import com.cuso.tailor.view.home.hr.dashboard.CreateFeedScreen
import com.cuso.tailor.view.home.hr.dashboard.HrDashboardScreen
import com.cuso.tailor.view.home.hr.exit_clearance.ExitClearanceDetailScreen
import com.cuso.tailor.view.home.hr.exit_clearance.ExitClearanceListScreen
import com.cuso.tailor.view.home.hr.settings.leave_type.CreateLeaveTypeScreen
import com.cuso.tailor.view.home.hr.settings.leave_type.LeaveTypesListScreen
import com.cuso.tailor.view.home.hr.settings.leave_type.LeaveTypeViewScreen
import com.cuso.tailor.view.home.it.integerations.IntegrationManagementScreen
import com.cuso.tailor.view.home.role.CreateRoleScreen
import com.cuso.tailor.view.home.security.api_integration.AddApiIntegrationScreen
import com.cuso.tailor.view.home.security.api_integration.ApiIntegrationDetailScreen
import com.cuso.tailor.view.home.security.api_integration.ApiIntegrationListScreen
import com.cuso.tailor.view.home.security.api_logs.ApiLogDetailScreen
import com.cuso.tailor.view.home.security.api_logs.ApiLogsListScreen
import com.cuso.tailor.view.home.security.audit_logs.AuditLogDetailScreen
import com.cuso.tailor.view.home.security.audit_logs.AuditLogsListScreen
import com.cuso.tailor.view.home.security.backup.CreateBackupScreen
import com.cuso.tailor.view.home.security.backup.DataBackupDetailScreen
import com.cuso.tailor.view.home.security.backup.DataBackupListScreen
import com.cuso.tailor.view.home.security.dashboard.SecurityDashboardScreen
import com.cuso.tailor.view.home.security.mobile_access.MobileAccessScreen
import com.cuso.tailor.view.home.security.recovery.DataRecoveryDetailScreen
import com.cuso.tailor.view.home.security.recovery.DataRecoveryListScreen
import com.cuso.tailor.view.home.security.support.help_center.HelpArticleDetailScreen
import com.cuso.tailor.view.home.security.support.help_center.HelpCenterScreen
import com.cuso.tailor.view.home.security.support.support_ticket.CreateSupportTicketScreen
import com.cuso.tailor.view.home.security.support.support_ticket.SupportTicketDetailScreen
import com.cuso.tailor.view.home.security.support.support_ticket.SupportTicketsListScreen
import com.cuso.tailor.view.home.security.user.CreateUserScreen
import com.cuso.tailor.view.home.security.user.UserManagementScreen
import com.cuso.tailor.view.home.services.settings.production_stages.ProductionStagesListScreen
import com.cuso.tailor.view.home.services.settings.workflow_stages.AddEditProductionStageScreen
import com.cuso.tailor.viewmodel.PermissionViewModel
import com.cuso.tailor.view.home.hr.settings.leave_policy.LeavePoliciesScreen
import com.cuso.tailor.view.home.hr.settings.leave_policy.CreateLeavePolicyScreen
import com.cuso.tailor.view.home.hr.settings.leave_policy.ViewLeavePolicyScreen
import com.cuso.tailor.view.home.hr.settings.piece_rate_setup.AddEditPieceRateScreen
import com.cuso.tailor.view.home.hr.settings.piece_rate_setup.PieceRateSetupListScreen
import com.cuso.tailor.view.home.hr.exit_request.ExitManagementScreen
import com.cuso.tailor.view.home.hr.exit_request.ExitRequestFormScreen
import com.cuso.tailor.view.home.hr.exit_request.ManagerApprovalScreen
import com.cuso.tailor.view.home.hr.organization_structure.CreateProductionTeamScreen
import com.cuso.tailor.view.home.hr.organization_structure.ProductionTeamDetailScreen
import com.cuso.tailor.view.home.hr.organization_structure.ProductionTeamsScreen
import com.cuso.tailor.view.home.hr.settlement.ExitSettlementDetailScreen
import com.cuso.tailor.view.home.hr.settlement.ExitSettlementListScreen
import com.cuso.tailor.view.home.reports.it.reports.ItAssetReportScreen
import com.cuso.tailor.view.home.reports.it.reports.ItServiceDeskReportScreen
import com.cuso.tailor.view.home.reports.security.reports.AccessActivityReportScreen
import com.cuso.tailor.view.home.reports.security.reports.ApiIntegrationSecurityReportScreen
import com.cuso.tailor.view.home.reports.security.reports.SecurityIncidentsReportScreen

private const val TEMP_PAYMENT_TERM_ID = "6ab206c595aeff058133df3d"

data class HomeScreenViewModels(
    val hrViewModel: HrViewModel,
    val servicesViewModel: ServicesViewModel,
    val customerViewModel: CustomerViewModel,
    val settingsViewModel: SettingsViewModel,
    val salesViewModel: SalesViewModel,
    val branchViewModel: BranchViewModel,
    val authViewModel: Authenticate,
    val inventoryViewModel: InventoryViewModel,
    val permissionViewModel: PermissionViewModel
)

data class HomeScreenNavigation(
    val navController: NavHostController,
    val widthSizeClass: WindowWidthSizeClass,
    val token: String,
    val onNavigate: (String) -> Unit,
    val onSafeNavigate: (String) -> Unit,
    val onAccessRestricted: (String) -> Unit = {},
    val onSafeNavigateWithAction: (String, PermissionAction?) -> Unit = { route, _ -> onSafeNavigate(route) },
    val onPaywallRequired: (PaywallModuleConfig) -> Unit = {},
    val onGoBack: () -> Unit,
    val onPopToScreen: (String) -> Unit = {},
    val onOpenDrawer: () -> Unit,
    val onOpenModulesPanel: (String) -> Unit,
    val onShowComingSoon: (String) -> Unit,
    val onSalesSettingsModeChange: (Boolean) -> Unit
)

data class SalesStateHolder(
    val selectedCustomer: CustomerItem? = null,
    val onCustomerSelected: (CustomerItem?) -> Unit = {},
    val selectedOrderId: String? = null,
    val onOrderIdSelected: (String?) -> Unit = {},
    val editOrderId: String? = null,
    val onEditOrderIdChange: (String?) -> Unit = {},
    val selectedManagementOrderId: String? = null,
    val onManagementOrderIdSelected: (String?) -> Unit = {},
    val selectedOpportunityId: String? = null,
    val onOpportunityIdSelected: (String?) -> Unit = {},
    val selectedMeasurementItem: MeasurementItem? = null,
    val onMeasurementItemSelected: (MeasurementItem?) -> Unit = {},
    val editingPricingId: String? = null,
    val onEditingPricingIdChange: (String?) -> Unit = {},
    val quotationScreenMode: String = "create",
    val onQuotationScreenModeChange: (String) -> Unit = {},
    val pendingOrderReviewData: OrderReviewData? = null,
    val onPendingOrderReviewDataChange: (OrderReviewData?) -> Unit = {},
    val onOrderFlowOriginChange: (String?) -> Unit = {},
    val onOrderSavedSuccessfully: (savedOrderId: String?) -> Unit = {}
)

data class FinanceStateHolder(
    val selectedLedgerAccountId: String? = null,
    val selectedLedgerAccountName: String = "Ledger",
    val onLedgerAccountSelected: (id: String?, name: String) -> Unit = { _, _ -> },
    val selectedSupplier: SupplierRow? = null,
    val onFinanceSupplierSelected: (SupplierRow?) -> Unit = {},
    val selectedSupplierId: String? = null,
    val onSupplierSelected: (String?) -> Unit = {},
    val selectedPaymentModeId: String? = null,
    val onPaymentModeSelected: (String?) -> Unit = {},
    val selectedInvoiceId: String? = null,
    val onInvoiceSelected: (String?) -> Unit = {},
    val selectedReceiptId: String? = null,
    val onReceiptSelected: (String?) -> Unit = {},
    val selectedPurchaseInvoice: PurchaseInvoiceItem? = null,
    val onPurchaseInvoiceSelected: (PurchaseInvoiceItem?) -> Unit = {},
    val selectedPurchaseReturnId: String? = null,
    val onPurchaseReturnIdSelected: (String?) -> Unit = {},
    val selectedBankAccountId: String? = null,
    val onBankAccountIdSelected: (String?) -> Unit = {},
    val selectedCashShiftId: String? = null,
    val onCashShiftSelected: (String?) -> Unit = {},
    val selectedStatementId: String? = null,
    val onStatementIdSelected: (String?) -> Unit = {},
    val selectedCreditNoteId: String? = null,
    val onCreditNoteSelected: (String?) -> Unit = {},
    val invoiceForPayment: InvoiceViewOneData? = null,
    val onInvoiceForPaymentChange: (InvoiceViewOneData?) -> Unit = {},
    val isOrderFromFinance: Boolean = false,
    val onOrderFromFinanceChange: (Boolean) -> Unit = {},
    val isPurchaseFromFinance: Boolean = false,
    val onPurchaseFromFinanceChange: (Boolean) -> Unit = {},
    val prefillOrderForInvoice: OrderOverviewData? = null,
    val onPrefillOrderForInvoiceChange: (OrderOverviewData?) -> Unit = {}
)

data class InventoryStateHolder(
    val selectedInventoryItemId: String? = null,
    val onInventoryItemIdSelected: (String?) -> Unit = {},
    val selectedLowStockItem: LowStockItemDto? = null,
    val onLowStockItemSelected: (LowStockItemDto?) -> Unit = {},
    val selectedItemGroupId: String? = null,
    val onItemGroupIdSelected: (String?) -> Unit = {},
    val selectedBulkItemId: String? = null,
    val onBulkItemIdSelected: (String?) -> Unit = {},
    val selectedRequisitionIdForDetail: String? = null,
    val onRequisitionIdForDetailSelected: (String?) -> Unit = {},
    val selectedPurchaseOrderForDetail: PurchaseOrder? = null,
    val onPurchaseOrderForDetailSelected: (PurchaseOrder?) -> Unit = {},
    val selectedReceivePoId: String? = null,
    val onReceivePoIdSelected: (String?) -> Unit = {},
    val selectedBarcodeIdForDetail: String? = null,
    val onBarcodeIdForDetailSelected: (String?) -> Unit = {},
    val selectedCategoryIdForEdit: String? = null,
    val onCategoryIdForEditSelected: (String?) -> Unit = {},
    val selectedPicklistId: String? = null,
    val onPicklistIdSelected: (String?) -> Unit = {},
    val selectedPackageIdForDetail: String? = null,
    val onPackageIdForDetailSelected: (String?) -> Unit = {},
    val selectedPackageIdForEdit: String? = null,
    val onPackageIdForEditSelected: (String?) -> Unit = {},
    val selectedPriceListId: String? = null,
    val onPriceListIdSelected: (String?) -> Unit = {},
    val selectedApprovalId: String? = null,
    val onApprovalIdSelected: (String?) -> Unit = {}
)

data class HrStateHolder(
    val employeeScreenMode: ScreenMode = ScreenMode.CREATE,
    val onEmployeeScreenModeChange: (ScreenMode) -> Unit = {},
    val selectedEmployeeId: String? = null,
    val onEmployeeIdSelected: (String?) -> Unit = {},
    val selectedAttendanceId: String? = null,
    val onAttendanceIdSelected: (String?) -> Unit = {},
    val selectedShiftId: String? = null,
    val onShiftIdSelected: (String?) -> Unit = {},
    val selectedDocumentId: String? = null,
    val onDocumentIdSelected: (String?) -> Unit = {},
    val selectedDocumentCategoryId: String? = null,
    val onDocumentCategoryIdSelected: (String?) -> Unit = {},
    val selectedSalaryComponentId: String? = null,
    val onSalaryComponentIdSelected: (String?) -> Unit = {},
    val selectedSalaryTemplateId: String? = null,
    val onSalaryTemplateIdSelected: (String?) -> Unit = {},
    val selectedTrainingProgramId: String? = null,
    val onTrainingProgramIdSelected: (String?) -> Unit = {},
    val selectedTrainingAssignmentId: String? = null,
    val onTrainingAssignmentIdSelected: (String?) -> Unit = {},
    val selectedExpenseClaimId: String? = null,
    val onExpenseClaimIdSelected: (String?) -> Unit = {},
    val selectedExpenseTypeId: String? = null,
    val onExpenseTypeIdSelected: (String?) -> Unit = {},
    val selectedClaimForPayment: ExpenseClaimDto? = null,
    val onClaimForPaymentSelected: (ExpenseClaimDto?) -> Unit = {},
    val selectedLeaveTypeIdForEdit: String? = null,
    val onLeaveTypeIdForEditSelected: (String?) -> Unit = {},
    val selectedLeaveTypeIdForView: String? = null,
    val onLeaveTypeIdForViewSelected: (String?) -> Unit = {},
    // Added Leave Policy navigation state holders
    val selectedLeavePolicyIdForEdit: String? = null,
    val onLeavePolicyIdForEditSelected: (String?) -> Unit = {},
    val selectedLeavePolicyIdForView: String? = null,
    val onLeavePolicyIdForViewSelected: (String?) -> Unit = {},
    // Piece rate navigation states
    val selectedPieceRateIdForEdit: String? = null,
    val onPieceRateIdForEditSelected: (String?) -> Unit = {},
    // Exit Request navigation states
    val selectedExitRequestId: String? = null,
    val onExitRequestIdSelected: (String?) -> Unit = {},
    // Production Team navigation states
    val selectedProductionTeamId: String? = null,
    val onProductionTeamIdSelected: (String?) -> Unit = {}
)

data class LogisticsStateHolder(
    val selectedShipmentId: String? = null,
    val onShipmentIdSelected: (String?) -> Unit = {},
    val selectedReturnId: String? = null,
    val onReturnIdSelected: (String?) -> Unit = {},
    val selectedLogisticsPackageId: String? = null,
    val onLogisticsPackageIdSelected: (String?) -> Unit = {}
)

data class SecurityStateHolder(
    val selectedBackupId: String? = null,
    val onBackupIdSelected: (String?) -> Unit = {},
    val selectedRecoveryId: String? = null,
    val onRecoveryIdSelected: (String?) -> Unit = {},
    val selectedIntegrationId: String? = null,
    val onIntegrationIdSelected: (String?) -> Unit = {},
    val selectedApiLogId: String? = null,
    val onApiLogIdSelected: (String?) -> Unit = {},
    val selectedAuditLogId: String? = null,
    val onAuditLogIdSelected: (String?) -> Unit = {},
    val selectedRoleIdForEdit: String? = null,
    val onRoleIdForEditSelected: (String?) -> Unit = {},
    val selectedApprovalRuleIdForEdit: String? = null,
    val onApprovalRuleIdForEditSelected: (String?) -> Unit = {},
    val selectedSupportTicketId: String? = null,
    val onSupportTicketIdSelected: (String?) -> Unit = {}
)

data class ServicesStateHolder(
    val selectedFeedbackId: String? = null,
    val onFeedbackIdSelected: (String?) -> Unit = {},
    val selectedComplaintId: String? = null,
    val onComplaintIdSelected: (String?) -> Unit = {},
    val selectedServiceTemplate: ProductionTemplateDto? = null,
    val onServiceTemplateSelected: (ProductionTemplateDto?) -> Unit = {},
    val selectedProductionStage: ServicesProductionStageDto? = null,
    val onProductionStageSelected: (ServicesProductionStageDto?) -> Unit = {},
    val selectedServiceRequestId: String? = null,
    val onServiceRequestIdSelected: (String?) -> Unit = {},
    val selectedAlterationId: String? = null,
    val onAlterationIdSelected: (String?) -> Unit = {},
    val selectedAssignmentJobCardId: String? = null,
    val onAssignmentJobCardIdSelected: (String?) -> Unit = {}
)

data class ItStateHolder(
    val selectedItDeviceId: String? = null,
    val onItDeviceIdSelected: (String?) -> Unit = {},
    val selectedPosTerminalId: String? = null,
    val onPosTerminalIdSelected: (String?) -> Unit = {},
    val selectedBackupSnapshotId: String? = null,
    val onBackupSnapshotIdSelected: (String?) -> Unit = {},
    val selectedAssetId: String? = null,
    val onAssetIdSelected: (String?) -> Unit = {},
    val selectedTicketId: String? = null,
    val onTicketIdSelected: (String?) -> Unit = {}
)

data class LegalStateHolder(
    val selectedLegalContractId: String? = null,
    val onLegalContractIdSelected: (String?) -> Unit = {},
    val selectedLegalComplianceId: String? = null,
    val onLegalComplianceIdSelected: (String?) -> Unit = {},
    val selectedLegalDocumentId: String? = null,
    val onLegalDocumentIdSelected: (String?) -> Unit = {},
    val selectedLegalLicenseId: String? = null,
    val onLegalLicenseIdSelected: (String?) -> Unit = {},
    val selectedLegalCustomerId: String? = null,
    val onLegalCustomerIdSelected: (String?) -> Unit = {},
    val selectedLegalEmployeeId: String? = null,
    val onLegalEmployeeIdSelected: (String?) -> Unit = {},
    val selectedLegalVendorId: String? = null,
    val onLegalVendorIdSelected: (String?) -> Unit = {},
    val selectedLegalNoticeId: String? = null,
    val onLegalNoticeIdSelected: (String?) -> Unit = {}
)

data class MarketingStateHolder(
    val selectedCampaignCode: String? = null,
    val onCampaignCodeSelected: (String?) -> Unit = {}
)

data class CommonStateHolder(
    val selectedRecentCustomerId: String? = null,
    val onRecentCustomerIdSelected: (String?) -> Unit = {},
    val paywallModuleConfig: PaywallModuleConfig? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun HomeScreenRouter(
    screen: String,
    selectedApiLogId: String? = null,
    onApiLogIdSelected: (String?) -> Unit = {},
    viewModels: HomeScreenViewModels,
    hrReportViewModel: ReportViewModel,
    navigation: HomeScreenNavigation,
    salesState: SalesStateHolder = SalesStateHolder(),
    financeState: FinanceStateHolder = FinanceStateHolder(),
    inventoryState: InventoryStateHolder = InventoryStateHolder(),
    hrState: HrStateHolder = HrStateHolder(),
    logisticsState: LogisticsStateHolder = LogisticsStateHolder(),
    securityState: SecurityStateHolder = SecurityStateHolder(),
    servicesState: ServicesStateHolder = ServicesStateHolder(),
    itState: ItStateHolder = ItStateHolder(),
    legalState: LegalStateHolder = LegalStateHolder(),
    marketingState: MarketingStateHolder = MarketingStateHolder(),
    commonState: CommonStateHolder = CommonStateHolder()
) {
    val myPermissions by viewModels.permissionViewModel.myPermissions.collectAsStateWithLifecycle()
    val modulePermissions by viewModels.permissionViewModel.modulePermissions.collectAsStateWithLifecycle()

    // Provide route-level action permissions to child Composables (FabScaffold, DataCard, etc.)
    val routePermissions = remember(screen, myPermissions, modulePermissions) {
        val mapping = viewModels.permissionViewModel.getRouteFeatureMapping(screen)
        if (mapping != null) {
            val (mod, feat) = mapping
            ScreenActionPermissions(
                module = mod,
                feature = feat,
                canView = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.VIEW),
                canCreate = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.CREATE),
                canEdit = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.EDIT),
                canDelete = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.DELETE),
                canExport = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.EXPORT),
                canPrint = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.PRINT),
                canShare = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.SHARE),
                canApprove = viewModels.permissionViewModel.canAction(mod, feat, PermissionAction.APPROVE)
            )
        } else {
            ScreenActionPermissions()
        }
    }

    CompositionLocalProvider(LocalScreenPermissions provides routePermissions) {
        when (screen) {

        // In HomeScreenRouter when(screen):
        "module_upgrade_paywall" -> {
            val config = commonState.paywallModuleConfig ?: viewModels.permissionViewModel.getPaywallConfigForModule("inventory")
            ModuleUpgradePaywallScreen(
                config = config,
                onClose = navigation.onGoBack,
                onUpgradePlan = {
                    navigation.onNavigate("manage_subscription")
                },
                onViewPricing = {
                    navigation.onNavigate("manage_subscription")
                }
            )
        }
        // Also add "home_create_approval_rule" here so it routes to HomeAndSettingsRouter
        // Added "home_create_role" to route into HomeAndSettingsRouter
            "home", "settings", "home_organization_profile", "home_branch_management",
            "home_department_teams", "home_approval_rules", "home_create_approval_rule", "home_designation", "home_role_management", "home_create_role",
            "home_opening_balance", "home_warehouse_management", "profile-settings",
            "settings_overview", "manage_subscription", "module_settings",
            "organization_settings", "home_business_setup" -> {
                HomeAndSettingsRouter(
                    screen = screen,
                    permissionViewModel = viewModels.permissionViewModel,
                    selectedApprovalRuleIdForEdit = securityState.selectedApprovalRuleIdForEdit,
                    onApprovalRuleIdForEditSelected = securityState.onApprovalRuleIdForEditSelected,
                    selectedRoleIdForEdit = securityState.selectedRoleIdForEdit,
                    onRoleIdForEditSelected = securityState.onRoleIdForEditSelected,
                    navController = navigation.navController,
                    widthSizeClass = navigation.widthSizeClass,
                    settingsViewModel = viewModels.settingsViewModel,
                    authViewModel = viewModels.authViewModel,
                    onNavigate = navigation.onNavigate,
                    onSafeNavigate = navigation.onSafeNavigate,
                    onSafeNavigateWithAction = navigation.onSafeNavigateWithAction,
                    onPaywallRequired = navigation.onPaywallRequired,
                    onGoBack = navigation.onGoBack,
                    onOpenDrawer = navigation.onOpenDrawer,
                    onRecentCustomerIdSelected = commonState.onRecentCustomerIdSelected,
                    onShowComingSoon = navigation.onShowComingSoon
                )
            }

        "inventory_allocation_rules", "inventory_create_allocation", "inventory_pdf_templates",
        "inventory_location_structure", "inventory_floor_overview", "inventory_add_floor",
        "inventory_section_overview", "inventory_add_section", "inventory_rack_overview",
        "inventory_add_rack", "inventory_bin_overview", "inventory_add_bin",
        "inventory_auto_reorder_settings"  -> {
            InventoryStructureRouter(
                screen = screen,
                permissionViewModel = viewModels.permissionViewModel,
                settingsViewModel = viewModels.settingsViewModel,
                onNavigate = navigation.onNavigate,
                onSafeNavigate = navigation.onSafeNavigateWithAction,
                onPaywallRequired = navigation.onPaywallRequired,
                onGoBack = navigation.onGoBack,
                onShowComingSoon = navigation.onShowComingSoon
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
                permissionViewModel = viewModels.permissionViewModel,
                isOrderFromFinance = financeState.isOrderFromFinance,
                onOrderFromFinanceChange = financeState.onOrderFromFinanceChange,
                prefillOrderForInvoice = financeState.prefillOrderForInvoice,
                onPrefillOrderForInvoiceChange = financeState.onPrefillOrderForInvoiceChange,
                selectedOpportunityId = salesState.selectedOpportunityId,
                onOpportunityIdSelected = salesState.onOpportunityIdSelected,
                navController = navigation.navController,
                widthSizeClass = navigation.widthSizeClass,
                selectedMeasurementItem = salesState.selectedMeasurementItem,
                onMeasurementItemSelected = salesState.onMeasurementItemSelected,
                token = navigation.token,
                customerViewModel = viewModels.customerViewModel,
                settingsViewModel = viewModels.settingsViewModel,
                selectedCustomer = salesState.selectedCustomer,
                onCustomerSelected = salesState.onCustomerSelected,
                selectedOrderId = salesState.selectedOrderId,
                onOrderIdSelected = salesState.onOrderIdSelected,
                editOrderId = salesState.editOrderId,
                onEditOrderIdChange = salesState.onEditOrderIdChange,
                selectedRecentCustomerId = commonState.selectedRecentCustomerId,
                onRecentCustomerIdSelected = commonState.onRecentCustomerIdSelected,
                editingPricingId = salesState.editingPricingId,
                onEditingPricingIdChange = salesState.onEditingPricingIdChange,
                quotationScreenMode = salesState.quotationScreenMode,
                onQuotationScreenModeChange = salesState.onQuotationScreenModeChange,
                pendingOrderReviewData = salesState.pendingOrderReviewData,
                onPendingOrderReviewDataChange = salesState.onPendingOrderReviewDataChange,
                onOrderFlowOriginChange = salesState.onOrderFlowOriginChange,
                onOrderSavedSuccessfully = salesState.onOrderSavedSuccessfully,
                onNavigate = navigation.onNavigate,
                onSafeNavigate = navigation.onSafeNavigateWithAction,
                onPaywallRequired = navigation.onPaywallRequired,
                onGoBack = navigation.onGoBack,
                onOpenDrawer = navigation.onOpenDrawer,
                onOpenModulesPanel = navigation.onOpenModulesPanel,
                onSalesSettingsModeChange = navigation.onSalesSettingsModeChange
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
        "finance_bank_statements", "finance_bank_statement_detail", "finance_import_bank_statement","finance_add_tax_rate" -> {
            FinanceRouter(
                screen = screen,
                permissionViewModel = viewModels.permissionViewModel,
                onPurchaseFromFinanceChange = financeState.onPurchaseFromFinanceChange,
                selectedPurchaseReturnId = financeState.selectedPurchaseReturnId,
                onPurchaseReturnIdSelected = financeState.onPurchaseReturnIdSelected,
                selectedStatementId = financeState.selectedStatementId,
                onStatementIdSelected = financeState.onStatementIdSelected,
                selectedBankAccountId = financeState.selectedBankAccountId,
                onBankAccountIdSelected = financeState.onBankAccountIdSelected,
                selectedCashShiftId = financeState.selectedCashShiftId,
                onCashShiftSelected = financeState.onCashShiftSelected,
                selectedCreditNoteId = financeState.selectedCreditNoteId,
                onCreditNoteSelected = financeState.onCreditNoteSelected,
                isOrderFromFinance = financeState.isOrderFromFinance,
                onOrderFromFinanceChange = financeState.onOrderFromFinanceChange,
                prefillOrderForInvoice = financeState.prefillOrderForInvoice,
                onPrefillOrderForInvoiceChange = financeState.onPrefillOrderForInvoiceChange,
                token = navigation.token,
                settingsViewModel = viewModels.settingsViewModel,
                salesViewModel = viewModels.salesViewModel,
                branchViewModel = viewModels.branchViewModel,
                invoiceForPayment = financeState.invoiceForPayment,
                onInvoiceForPaymentChange = financeState.onInvoiceForPaymentChange,
                selectedSupplier = financeState.selectedSupplier,
                inventoryViewModel = viewModels.inventoryViewModel,
                customerViewModel = viewModels.customerViewModel,
                selectedLedgerAccountId = financeState.selectedLedgerAccountId,
                selectedLedgerAccountName = financeState.selectedLedgerAccountName,
                onLedgerAccountSelected = financeState.onLedgerAccountSelected,
                selectedSupplierId = financeState.selectedSupplierId,
                onSupplierSelected = financeState.onSupplierSelected,
                selectedPaymentModeId = financeState.selectedPaymentModeId,
                onPaymentModeSelected = financeState.onPaymentModeSelected,
                selectedInvoiceId = financeState.selectedInvoiceId,
                onInvoiceSelected = financeState.onInvoiceSelected,
                selectedReceiptId = financeState.selectedReceiptId,
                onReceiptSelected = financeState.onReceiptSelected,
                selectedPurchaseInvoice = financeState.selectedPurchaseInvoice,
                onPurchaseInvoiceSelected = financeState.onPurchaseInvoiceSelected,
                selectedRecentCustomerId = commonState.selectedRecentCustomerId,
                onRecentCustomerIdSelected = commonState.onRecentCustomerIdSelected,
                selectedReceivePoId = inventoryState.selectedReceivePoId,
                onReceivePoIdSelected = inventoryState.onReceivePoIdSelected,
                selectedPurchaseOrderForDetail = inventoryState.selectedPurchaseOrderForDetail,
                onPurchaseOrderForDetailSelected = inventoryState.onPurchaseOrderForDetailSelected,
                navController = navigation.navController,
                onOpenDrawer = navigation.onOpenDrawer,
                onPendingOrderReviewDataChange = salesState.onPendingOrderReviewDataChange,
                onOrderIdSelected = salesState.onOrderIdSelected,
                onEditOrderIdChange = salesState.onEditOrderIdChange,
                onNavigate = navigation.onNavigate,
                onSafeNavigate = navigation.onSafeNavigateWithAction,
                onAccessRestricted = navigation.onAccessRestricted,
                onPaywallRequired = navigation.onPaywallRequired,
                onGoBack = navigation.onGoBack,
                onOpenModulesPanel = navigation.onOpenModulesPanel
            )
        }

        // ── Marketing Module Router Dispatch ──
        "marketing_dashboard", "marketing_campaigns_overview",
        "marketing_all_campaigns", "marketing_campaign_detail",
        "marketing_advertising_overview", "marketing_advertising",
        "marketing_social_overview", "marketing_social_media",
        "marketing_promotions_overview", "marketing_all_promotions",
        "marketing_customers_overview", "marketing_all_customers",
        "marketing_website_overview", "marketing_traffic_engagement",
        "marketing_pages", "marketing_sources", "marketing_channels",
        "marketing_calendar", "marketing_insights" -> {
            MarketingRouter(
                screen = screen,
                permissionViewModel = viewModels.permissionViewModel,
                selectedCampaignCode = marketingState.selectedCampaignCode,
                onCampaignCodeSelected = marketingState.onCampaignCodeSelected,
                onNavigate = navigation.onNavigate,
                onSafeNavigate = navigation.onSafeNavigateWithAction,
                onPaywallRequired = navigation.onPaywallRequired,
                onGoBack = navigation.onGoBack,
                onShowComingSoon = navigation.onShowComingSoon
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
                permissionViewModel = viewModels.permissionViewModel,
                selectedPriceListId = inventoryState.selectedPriceListId,
                onPriceListIdSelected = inventoryState.onPriceListIdSelected,
                selectedApprovalId = inventoryState.selectedApprovalId,
                onApprovalIdSelected = inventoryState.onApprovalIdSelected,
                selectedInventoryItemId = inventoryState.selectedInventoryItemId,
                onInventoryItemIdSelected = inventoryState.onInventoryItemIdSelected,
                selectedItemGroupId = inventoryState.selectedItemGroupId,
                onItemGroupIdSelected = inventoryState.onItemGroupIdSelected,
                selectedBulkItemId = inventoryState.selectedBulkItemId,
                onBulkItemIdSelected = inventoryState.onBulkItemIdSelected,
                selectedCategoryIdForEdit = inventoryState.selectedCategoryIdForEdit,
                onCategoryIdForEditSelected = inventoryState.onCategoryIdForEditSelected,
                inventoryViewModel = viewModels.inventoryViewModel,
                settingsViewModel = viewModels.settingsViewModel,
                onNavigate = navigation.onNavigate,
                onSafeNavigate = navigation.onSafeNavigateWithAction,
                onPaywallRequired = navigation.onPaywallRequired,
                onGoBack = navigation.onGoBack,
                onOpenModulesPanel = navigation.onOpenModulesPanel,
                onShowComingSoon = navigation.onShowComingSoon
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
                permissionViewModel = viewModels.permissionViewModel,
                isPurchaseFromFinance = financeState.isPurchaseFromFinance,
                onPurchaseFromFinanceChange = financeState.onPurchaseFromFinanceChange,
                selectedPackageIdForDetail = inventoryState.selectedPackageIdForDetail,
                onPackageIdForDetailSelected = inventoryState.onPackageIdForDetailSelected,
                selectedPackageIdForEdit = inventoryState.selectedPackageIdForEdit,
                onPackageIdForEditSelected = inventoryState.onPackageIdForEditSelected,
                selectedPicklistId = inventoryState.selectedPicklistId,
                onPicklistIdSelected = inventoryState.onPicklistIdSelected,
                selectedBarcodeIdForDetail = inventoryState.selectedBarcodeIdForDetail,
                onBarcodeIdForDetailSelected = inventoryState.onBarcodeIdForDetailSelected,
                selectedReceivePoId = inventoryState.selectedReceivePoId,
                onReceivePoIdSelected = inventoryState.onReceivePoIdSelected,
                selectedPurchaseOrderForDetail = inventoryState.selectedPurchaseOrderForDetail,
                onPurchaseOrderForDetailSelected = inventoryState.onPurchaseOrderForDetailSelected,
                selectedRequisitionIdForDetail = inventoryState.selectedRequisitionIdForDetail,
                onRequisitionIdForDetailSelected = inventoryState.onRequisitionIdForDetailSelected,
                selectedSupplierId = financeState.selectedSupplierId,
                onSupplierSelected = financeState.onSupplierSelected,
                selectedInvoiceId = financeState.selectedInvoiceId,
                onInvoiceSelected = financeState.onInvoiceSelected,
                selectedPaymentModeId = financeState.selectedPaymentModeId,
                onPaymentModeSelected = financeState.onPaymentModeSelected,
                selectedLowStockItem = inventoryState.selectedLowStockItem,
                onLowStockItemSelected = inventoryState.onLowStockItemSelected,
                inventoryViewModel = viewModels.inventoryViewModel,
                onNavigate = navigation.onNavigate,
                onSafeNavigate = navigation.onSafeNavigateWithAction,
                onPaywallRequired = navigation.onPaywallRequired,
                onGoBack = navigation.onGoBack,
                onOpenModulesPanel = navigation.onOpenModulesPanel
            )
        }

        "hr_dashboard","hr_all_employees", "hr_create_feed", "hr_employee_onboarding", "hr_attendance",
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
        "hr_create_leave_type",
        "hr_view_leave_type",
        "hr_employee_document_upload","hr_leave_types",
        "hr_leave_policies", "hr_create_leave_policy", "hr_view_leave_policy",
        "hr_piece_rate_setup", "hr_add_edit_piece_rate",
        "hr_exit_management", "hr_exit_request_form", "hr_exit_manager_approval",
        "hr_exit_clearance", "hr_exit_clearance_detail",
        "hr_exit_settlements", "hr_exit_settlement_detail",
        "hr_new_document_upload", "hr_organization_structure",
        "hr_production_team", "hr_create_production_team", "hr_production_team_detail",
        "hr_expense_claims","hr_expense_new_claim", "hr_expense_type", "hr_submit_expense_claim", "hr_expense_claim_detail","hr_expense_claim_detail_readonly",
        "hr_expense_finance_reimbursement","hr_edit_expense_claim","hr_add_expense_type","hr_expense_process_payment"   -> {
            HrRouter(
                screen = screen,
                permissionViewModel = viewModels.permissionViewModel,
                settingsViewModel = viewModels.settingsViewModel,
                selectedPieceRateIdForEdit = hrState.selectedPieceRateIdForEdit,
                onPieceRateIdForEditSelected = hrState.onPieceRateIdForEditSelected,
                selectedExitRequestId = hrState.selectedExitRequestId,
                onExitRequestIdSelected = hrState.onExitRequestIdSelected,
                selectedProductionTeamId = hrState.selectedProductionTeamId,
                onProductionTeamIdSelected = hrState.onProductionTeamIdSelected,
                selectedLeavePolicyIdForEdit = hrState.selectedLeavePolicyIdForEdit,
                onLeavePolicyIdForEditSelected = hrState.onLeavePolicyIdForEditSelected,
                selectedLeavePolicyIdForView = hrState.selectedLeavePolicyIdForView,
                onLeavePolicyIdForViewSelected = hrState.onLeavePolicyIdForViewSelected,
                selectedClaimForPayment = hrState.selectedClaimForPayment,
                selectedLeaveTypeIdForView = hrState.selectedLeaveTypeIdForView,
                onLeaveTypeIdForViewSelected = hrState.onLeaveTypeIdForViewSelected,
                selectedLeaveTypeIdForEdit = hrState.selectedLeaveTypeIdForEdit,
                onLeaveTypeIdForEditSelected = hrState.onLeaveTypeIdForEditSelected,
                onClaimForPaymentSelected = hrState.onClaimForPaymentSelected,
                selectedExpenseTypeId = hrState.selectedExpenseTypeId,
                onExpenseTypeIdSelected = hrState.onExpenseTypeIdSelected,
                selectedExpenseClaimId = hrState.selectedExpenseClaimId,
                onExpenseClaimIdSelected = hrState.onExpenseClaimIdSelected,
                selectedDocumentCategoryId = hrState.selectedDocumentCategoryId,
                onDocumentCategoryIdSelected = hrState.onDocumentCategoryIdSelected,
                selectedDocumentId = hrState.selectedDocumentId,
                onDocumentIdSelected = hrState.onDocumentIdSelected,
                hrViewModel = viewModels.hrViewModel,
                employeeScreenMode = hrState.employeeScreenMode,
                onEmployeeScreenModeChange = hrState.onEmployeeScreenModeChange,
                selectedEmployeeId = hrState.selectedEmployeeId,
                selectedTrainingAssignmentId = hrState.selectedTrainingAssignmentId,
                onTrainingAssignmentIdSelected = hrState.onTrainingAssignmentIdSelected,
                selectedSalaryTemplateId = hrState.selectedSalaryTemplateId,
                onSalaryTemplateIdSelected = hrState.onSalaryTemplateIdSelected,
                selectedTrainingProgramId = hrState.selectedTrainingProgramId,
                onTrainingProgramIdSelected = hrState.onTrainingProgramIdSelected,
                onEmployeeIdSelected = hrState.onEmployeeIdSelected,
                selectedSalaryComponentId = hrState.selectedSalaryComponentId,
                onSalaryComponentIdSelected = hrState.onSalaryComponentIdSelected,
                selectedAttendanceId = hrState.selectedAttendanceId,
                onAttendanceIdSelected = hrState.onAttendanceIdSelected,
                selectedShiftId = hrState.selectedShiftId,
                onShiftIdSelected = hrState.onShiftIdSelected,
                onNavigate = navigation.onNavigate,
                onSafeNavigate = navigation.onSafeNavigateWithAction,
                onPaywallRequired = navigation.onPaywallRequired,
                onGoBack = navigation.onGoBack,
                onOpenModulesPanel = navigation.onOpenModulesPanel
            )
        }

            "logistics_dashboard", "logistics_orders", "logistics_order_detail", "logistics_delivery",
            "delivery_detail", "logistics_order_tracking", "tracking_overview", "logistics_dispatched",
            "logistics_shipments", "shipment_detail", "create_shipment", "logistics_courier_management",
            "courier_overview", "logistics_courier_partner_list", "courier_partner_overview", "add_courier_partner",
            "logistics_tracking_events", "logistics_returns", "return_detail", "create_return", "logistics_cod_screen", "logistics_cod_detail" -> {
                LogisticsRouter(
                    screen = screen,
                    permissionViewModel = viewModels.permissionViewModel,
                    selectedReturnId = logisticsState.selectedReturnId,
                    onReturnIdSelected = logisticsState.onReturnIdSelected,
                    selectedLogisticsPackageId = logisticsState.selectedLogisticsPackageId,
                    onLogisticsPackageIdSelected = logisticsState.onLogisticsPackageIdSelected,
                    selectedShipmentId = logisticsState.selectedShipmentId,
                    onShipmentIdSelected = logisticsState.onShipmentIdSelected,
                    onOrderIdSelected = salesState.onOrderIdSelected,
                    onNavigate = navigation.onNavigate,
                    onSafeNavigate = navigation.onSafeNavigateWithAction,
                    onPaywallRequired = navigation.onPaywallRequired,
                    onGoBack = navigation.onGoBack,
                    onOpenModulesPanel = navigation.onOpenModulesPanel
                )
            }

            // Legal Module Router
            "legal_dashboard", "legal_calendar", "legal_contracts", "legal_contract_detail", "legal_create_contract", "legal_compliance",
            "legal_compliance_detail", "legal_create_compliance", "legal_documents", "legal_document_detail", "legal_create_document",
            "legal_licenses", "legal_license_detail", "legal_create_license",
            "legal_customer", "legal_customer_detail", "legal_create_customer_record",
            "legal_employee", "legal_employee_detail", "legal_create_employee",
            "legal_vendor", "legal_vendor_detail", "legal_create_vendor",
            "legal_notice", "legal_notice_detail", "legal_create_notice" -> {
                LegalRouter(
                    screen = screen,
                    permissionViewModel = viewModels.permissionViewModel,
                    selectedContractId = legalState.selectedLegalContractId,
                    onContractIdSelected = legalState.onLegalContractIdSelected,
                    selectedComplianceId = legalState.selectedLegalComplianceId,
                    onComplianceIdSelected = legalState.onLegalComplianceIdSelected,
                    selectedLegalDocumentId = legalState.selectedLegalDocumentId,
                    onLegalDocumentIdSelected = legalState.onLegalDocumentIdSelected,
                    selectedLegalLicenseId = legalState.selectedLegalLicenseId,
                    onLegalLicenseIdSelected = legalState.onLegalLicenseIdSelected,
                    selectedLegalCustomerId = legalState.selectedLegalCustomerId,
                    onLegalCustomerIdSelected = legalState.onLegalCustomerIdSelected,
                    selectedLegalEmployeeId = legalState.selectedLegalEmployeeId,
                    onLegalEmployeeIdSelected = legalState.onLegalEmployeeIdSelected,
                    selectedLegalVendorId = legalState.selectedLegalVendorId,
                    onLegalVendorIdSelected = legalState.onLegalVendorIdSelected,
                    selectedLegalNoticeId = legalState.selectedLegalNoticeId,
                    onLegalNoticeIdSelected = legalState.onLegalNoticeIdSelected,
                    onNavigate = navigation.onNavigate,
                    onSafeNavigate = navigation.onSafeNavigateWithAction,
                    onPaywallRequired = navigation.onPaywallRequired,
                    onGoBack = navigation.onGoBack,
                    hrViewModel = viewModels.hrViewModel,
                    branchViewModel = viewModels.branchViewModel,
                    inventoryViewModel = viewModels.inventoryViewModel
                )
            }

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
            "services_production_pipeline", "services_qc_delivery", "services_assignment_mode",
            "services_production_stages", "services_create_production_stage", "services_workflow_stages" -> {
                ServicesRouter(
                    screen = screen,
                    permissionViewModel = viewModels.permissionViewModel,
                    servicesViewModel = viewModels.servicesViewModel,
                    settingsViewModel = viewModels.settingsViewModel,
                    selectedAssignmentJobCardId = servicesState.selectedAssignmentJobCardId,
                    onAssignmentJobCardIdSelected = servicesState.onAssignmentJobCardIdSelected,
                    selectedComplaintId = servicesState.selectedComplaintId,
                    onComplaintIdSelected = servicesState.onComplaintIdSelected,
                    selectedAlterationId = servicesState.selectedAlterationId,
                    onAlterationIdSelected = servicesState.onAlterationIdSelected,
                    selectedServiceRequestId = servicesState.selectedServiceRequestId,
                    onServiceRequestIdSelected = servicesState.onServiceRequestIdSelected,
                    navController = navigation.navController,
                    selectedManagementOrderId = salesState.selectedManagementOrderId,
                    onManagementOrderIdSelected = salesState.onManagementOrderIdSelected,
                    selectedOrderId = salesState.selectedOrderId,
                    onOrderIdSelected = salesState.onOrderIdSelected,
                    onEditOrderIdChange = salesState.onEditOrderIdChange,
                    selectedFeedbackId = servicesState.selectedFeedbackId,
                    onFeedbackIdSelected = servicesState.onFeedbackIdSelected,
                    selectedServiceTemplate = servicesState.selectedServiceTemplate,
                    onServiceTemplateSelected = servicesState.onServiceTemplateSelected,
                    selectedProductionStage = servicesState.selectedProductionStage,
                    onProductionStageSelected = servicesState.onProductionStageSelected,
                    pendingOrderReviewData = salesState.pendingOrderReviewData,
                    onPendingOrderReviewDataChange = salesState.onPendingOrderReviewDataChange,
                    onNavigate = navigation.onNavigate,
                    onSafeNavigate = navigation.onSafeNavigateWithAction,
                    onPaywallRequired = navigation.onPaywallRequired,
                    onGoBack = navigation.onGoBack,
                    onPopToScreen = navigation.onPopToScreen,
                    onOpenDrawer = navigation.onOpenDrawer,
                    onOpenModulesPanel = navigation.onOpenModulesPanel,
                    hrViewModel = viewModels.hrViewModel
                )
            }

            "reports_sales",
            "reports_sales_lead_master", "reports_sales_lead_source",
            "reports_sales_lead_overdue", "reports_sales_lead_activity",
            "reports_sales_opp_master", "reports_sales_opp_funnel", "reports_sales_opp_closing",
            "reports_sales_cust_master", "reports_sales_cust_ltv",
            "reports_sales_cust_credit", "reports_sales_cust_dormant",
            "reports_sales_meas_master", "reports_sales_meas_stale",
            "reports_sales_order_master", "reports_sales_order_stream",
            "reports_sales_order_sla", "reports_sales_branch_wise",
            "reports_sales_quote_master", "reports_sales_quote_discount", "reports_sales_quote_expiring",
            "reports_marketing", "reports_finance", "reports_inventory",
            "reports_logistics", "reports_services", "reports_human_resource",
            "reports_hr_attendance_register", "reports_hr_payroll_run_audit",
            "reports_hr_piece_rate_wage", "reports_hr_reimbursement_claims",
            "reports_sec_incidents_alerts", "reports_sec_access_activity",
            "reports_sec_api_integrations",
            "reports_it_assets", "reports_it_service_desk",
            "reports_it", "reports_security", "reports_legal" -> {
                ReportsRouter(
                    screen = screen,
                    permissionViewModel = viewModels.permissionViewModel,
                    hrReportViewModel = hrReportViewModel,
                    onNavigate = navigation.onNavigate,
                    onSafeNavigate = navigation.onSafeNavigateWithAction,
                    onPaywallRequired = navigation.onPaywallRequired,
                    onGoBack = navigation.onGoBack,
                    onOpenModulesPanel = navigation.onOpenModulesPanel,
                    onShowComingSoon = navigation.onShowComingSoon
                )
            }

            "it_dashboard", "it_device_management", "it_device_detail",
            "it_pos_billing", "it_pos_billing_detail",
            "it_integrations", "it_backup_recovery", "it_backup_recovery_detail", "it_backup_restore",
            "it_asset_management", "it_asset_detail", "it_asset_register",
            "it_service_desk", "it_service_desk_create", "it_service_desk_detail" -> {
                ItRouter(
                    screen = screen,
                    permissionViewModel = viewModels.permissionViewModel,
                    selectedTicketId = itState.selectedTicketId,
                    onTicketIdSelected = itState.onTicketIdSelected,
                    selectedAssetId = itState.selectedAssetId,
                    onAssetIdSelected = itState.onAssetIdSelected,
                    selectedBackupSnapshotId = itState.selectedBackupSnapshotId,
                    onBackupSnapshotIdSelected = itState.onBackupSnapshotIdSelected,
                    selectedItDeviceId = itState.selectedItDeviceId,
                    onItDeviceIdSelected = itState.onItDeviceIdSelected,
                    selectedPosTerminalId = itState.selectedPosTerminalId,
                    onPosTerminalIdSelected = itState.onPosTerminalIdSelected,
                    onNavigate = navigation.onNavigate,
                    onSafeNavigate = navigation.onSafeNavigateWithAction,
                    onPaywallRequired = navigation.onPaywallRequired,
                    onGoBack = navigation.onGoBack,
                    onShowComingSoon = navigation.onShowComingSoon
                )
            }

            // ── Security Module Router Dispatch ──
            "security_dashboard", "security_users", "security_roles",
            "security_data_backup", "security_create_backup", "security_backup_detail",
            "security_recovery", "security_recovery_detail",
            "security_api_integration", "security_create_api_integration", "security_api_integration_detail",
            "security_api_logs", "security_api_log_detail",
            "security_audit_logs", "security_audit_log_detail",
            "security_mobile_security", "security_help_center", "security_support_ticket",
            "security_create_support_ticket", "security_support_ticket_detail", "security_create_user" -> {
                SecurityRouter(
                    screen = screen,
                    selectedIntegrationId = securityState.selectedIntegrationId,
                    onIntegrationIdSelected = securityState.onIntegrationIdSelected,
                    selectedBackupId = securityState.selectedBackupId,
                    onBackupIdSelected = securityState.onBackupIdSelected,
                    selectedRecoveryId = securityState.selectedRecoveryId,
                    onRecoveryIdSelected = securityState.onRecoveryIdSelected,
                    selectedApiLogId = securityState.selectedApiLogId,
                    onApiLogIdSelected = securityState.onApiLogIdSelected,
                    selectedAuditLogId = securityState.selectedAuditLogId,
                    onAuditLogIdSelected = securityState.onAuditLogIdSelected,
                    selectedSupportTicketId = securityState.selectedSupportTicketId,
                    onSupportTicketIdSelected = securityState.onSupportTicketIdSelected,
                    onNavigate = navigation.onNavigate,
                    onSafeNavigate = navigation.onSafeNavigateWithAction,
                    onPaywallRequired = navigation.onPaywallRequired,
                    onGoBack = navigation.onGoBack,
                    branchViewModel = viewModels.branchViewModel,
                    hrViewModel = viewModels.hrViewModel,
                    permissionViewModel = viewModels.permissionViewModel,
                    onShowComingSoon = navigation.onShowComingSoon
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun HomeAndSettingsRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    selectedApprovalRuleIdForEdit: String? = null,
    onApprovalRuleIdForEditSelected: (String?) -> Unit = {},
    selectedRoleIdForEdit: String? = null,
    onRoleIdForEditSelected: (String?) -> Unit = {},
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    settingsViewModel: SettingsViewModel,
    authViewModel: Authenticate,
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String) -> Unit,
    onSafeNavigateWithAction: (String, PermissionAction?) -> Unit = { route, _ -> onSafeNavigate(route) },
    onPaywallRequired: (PaywallModuleConfig) -> Unit = {},
    onGoBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onRecentCustomerIdSelected: (String?) -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    @Composable
    fun HomeGuard(
        module: String = "home",
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = module,
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = module,
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "home" -> HomeScreenContent(
            navController = navController,
            widthSizeClass = widthSizeClass,
            onNavigate = onSafeNavigate,
            onCustomerClick = { customerId ->
                if (customerId.isNotBlank()) {
                    onRecentCustomerIdSelected(customerId)
                    onSafeNavigate("view_customer_recent")
                } else {
                    onSafeNavigate("sales_customers")
                }
            }
        )

        "settings", "home_organization_profile" -> HomeGuard("home", "organizationProfile", PermissionAction.VIEW) {
            SettingsScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack
            )
        }

        "home_branch_management" -> HomeGuard("home", "branches", PermissionAction.VIEW) {
            BranchSettingsScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack
            )
        }

        "home_department_teams" -> HomeGuard("home", "departments", PermissionAction.VIEW) {
            DepartmentSettingsScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack
            )
        }

        "home_designation" -> HomeGuard("home", "designations", PermissionAction.VIEW) {
            DesignationScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack
            )
        }

        "home_role_management" -> HomeGuard("security", "roles", PermissionAction.VIEW) {
            RoleSettingsScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack,
                onAddRoleClick = {
                    onRoleIdForEditSelected(null)
                    onSafeNavigateWithAction("home_create_role", PermissionAction.CREATE)
                },
                onEditRoleClick = { roleId ->
                    onRoleIdForEditSelected(roleId)
                    onSafeNavigateWithAction("home_create_role", PermissionAction.EDIT)
                }
            )
        }

        "home_create_role" -> HomeGuard(
            module = "security",
            feature = "roles",
            action = if (selectedRoleIdForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateRoleScreen(
                roleIdToEdit = selectedRoleIdForEdit,
                onBack = {
                    onRoleIdForEditSelected(null)
                    onGoBack()
                },
                onRoleSaved = {
                    onRoleIdForEditSelected(null)
                    onGoBack()
                }
            )
        }

        "home_opening_balance" -> HomeGuard("finance", "finChartOfAccounts", PermissionAction.VIEW) {
            OpeningBalancesScreen(
                navController = navController,
                onBack = onGoBack
            )
        }

        "home_warehouse_management" -> HomeGuard("home", "warehouse", PermissionAction.VIEW) {
            WarehouseSettingsScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack
            )
        }

        "home_approval_rules" -> HomeGuard("home", "approvalRules", PermissionAction.VIEW) {
            ApprovalRulesScreen(
                navController = navController,
                onBack = onGoBack,
                onCreateRule = {
                    onApprovalRuleIdForEditSelected(null)
                    onSafeNavigateWithAction("home_create_approval_rule", PermissionAction.CREATE)
                },
                onEditRule = { ruleId ->
                    onApprovalRuleIdForEditSelected(ruleId)
                    onSafeNavigateWithAction("home_create_approval_rule", PermissionAction.EDIT)
                }
            )
        }

        "home_create_approval_rule" -> HomeGuard(
            module = "home",
            feature = "approvalRules",
            action = if (selectedApprovalRuleIdForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateEditApprovalRuleScreen(
                ruleId = selectedApprovalRuleIdForEdit,
                onClose = {
                    onApprovalRuleIdForEditSelected(null)
                    onGoBack()
                }
            )
        }

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
            // Open organization settings menu directly without blocking
            onNavigateToOrganizationSettings = { onNavigate("organization_settings") },
            onNavigateToModuleSettings = { onNavigate("module_settings") },
            onNavigateToSubscriptionSettings = { onNavigate("manage_subscription") }
        )

//        "manage_subscription" -> SubscriptionFlowContainer(
//            onClose = onGoBack,
//            onStartUpgrade = {}
//        )

        "manage_subscription" -> CurrentPlanScreen(
            onClose = onGoBack
        )

        "module_settings" -> ModuleSettingsScreen(
            onClose = onGoBack,
            onNavigateToModule = { route -> onSafeNavigate(route) },
            onPaywallRequired = onPaywallRequired,
            onConfigureHome = { onSafeNavigate("home_organization_profile") },
            onConfigureSales = { onSafeNavigate("sales_garment_type") },
            onNavigateLeaveTypes = { onSafeNavigate("hr_leave_types") },
            onNavigateLeavePolicies = { onSafeNavigate("hr_leave_policies") },
            onNavigatePieceRateSetup = { onSafeNavigate("hr_piece_rate_setup") },
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
            onNavigateProductionStages = { onSafeNavigate("services_production_stages") },
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
                    // Check permission only when user clicks the specific item
                    "organization_profile" -> onSafeNavigate("home_organization_profile")
                    "business_setup" -> onSafeNavigate("home_business_setup")
                    "approval_rules" -> onSafeNavigate("home_approval_rules")
                    "tax_rates" -> onSafeNavigate("finance_tax_rates")
                    "gst_settings" -> onSafeNavigate("finance_gst_settings")
                    "branch_management" -> onSafeNavigate("home_branch_management")
                    "department_teams" -> onSafeNavigate("home_department_teams")
                    "designation" -> onSafeNavigate("home_designation")
                    "role" -> onSafeNavigate("home_role_management")
                    "opening_balance" -> onSafeNavigate("home_opening_balance")
                    "warehouse" -> onSafeNavigate("home_warehouse_management")
                    "manage_subcription", "overview", "current_plan" -> onNavigate("manage_subscription")
                    else -> onSafeNavigate(routeKey)
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
    permissionViewModel: PermissionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    @Composable
    fun StructureGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "inventory",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "inventory",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "inventory_allocation_rules" -> StructureGuard("warehouseStructure", PermissionAction.VIEW) {
            AllocationRulesScreen(
                onClose = onGoBack,
                onAddNewClick = { onSafeNavigate("inventory_create_allocation", PermissionAction.CREATE) }
            )
        }

        "inventory_create_allocation" -> StructureGuard("warehouseStructure", PermissionAction.CREATE) {
            CreateAllocationScreen(
                onClose = onGoBack,
                onSaveRule = onGoBack
            )
        }

        "inventory_pdf_templates" -> StructureGuard("pdftemp", PermissionAction.VIEW) {
            PdfTemplatesScreen(
                onClose = onGoBack,
                onCreateNewTemplate = { onShowComingSoon("Create Template Coming Soon") }
            )
        }

        "inventory_auto_reorder_settings" -> StructureGuard("autoReorderSettings", PermissionAction.VIEW) {
            AutoReorderSettingsScreen(
                onClose = onGoBack
            )
        }

        "inventory_location_structure" -> StructureGuard("warehouseStructure", PermissionAction.VIEW) {
            LocationStructureScreen(
                onClose = onGoBack,
                onAddFloor = { onSafeNavigate("inventory_add_floor", PermissionAction.CREATE) },
                onFloorClick = { onSafeNavigate("inventory_floor_overview", PermissionAction.VIEW) }
            )
        }

        "inventory_floor_overview" -> StructureGuard("warehouseStructure", PermissionAction.VIEW) {
            FloorOverviewScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onAddSection = {
                    settingsViewModel.clearSelectedSectionForEdit()
                    onSafeNavigate("inventory_add_section", PermissionAction.CREATE)
                },
                onEditFloor = { floor ->
                    settingsViewModel.setSelectedFloorForEdit(floor)
                    onSafeNavigate("inventory_add_floor", PermissionAction.EDIT)
                }
            )
        }

        "inventory_add_floor" -> {
            val selectedFloor by settingsViewModel.selectedFloorForEdit.collectAsStateWithLifecycle()
            StructureGuard(
                feature = "warehouseStructure",
                action = if (selectedFloor != null) PermissionAction.EDIT else PermissionAction.CREATE
            ) {
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
        }

        "inventory_section_overview" -> StructureGuard("warehouseStructure", PermissionAction.VIEW) {
            SectionOverviewScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onAddRack = {
                    settingsViewModel.clearSelectedRackForEdit()
                    onSafeNavigate("inventory_add_rack", PermissionAction.CREATE)
                },
                onEditSection = { section ->
                    settingsViewModel.setSelectedSectionForEdit(section)
                    onSafeNavigate("inventory_add_section", PermissionAction.EDIT)
                }
            )
        }

        "inventory_add_section" -> {
            val selectedSection by settingsViewModel.selectedSectionForEdit.collectAsStateWithLifecycle()
            StructureGuard(
                feature = "warehouseStructure",
                action = if (selectedSection != null) PermissionAction.EDIT else PermissionAction.CREATE
            ) {
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
        }

        "inventory_rack_overview" -> StructureGuard("warehouseStructure", PermissionAction.VIEW) {
            RackOverviewScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onAddBin = {
                    settingsViewModel.clearSelectedBinForEdit()
                    onSafeNavigate("inventory_add_bin", PermissionAction.CREATE)
                },
                onEditRack = { rack ->
                    settingsViewModel.setSelectedRackForEdit(rack)
                    onSafeNavigate("inventory_add_rack", PermissionAction.EDIT)
                }
            )
        }

        "inventory_add_rack" -> {
            val selectedRack by settingsViewModel.selectedRackForEdit.collectAsStateWithLifecycle()
            StructureGuard(
                feature = "warehouseStructure",
                action = if (selectedRack != null) PermissionAction.EDIT else PermissionAction.CREATE
            ) {
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
        }

        "inventory_bin_overview" -> StructureGuard("warehouseStructure", PermissionAction.VIEW) {
            BinOverviewScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onEditBin = { bin ->
                    settingsViewModel.setSelectedBinForEdit(bin)
                    onSafeNavigate("inventory_add_bin", PermissionAction.EDIT)
                }
            )
        }

        "inventory_add_bin" -> {
            val selectedBin by settingsViewModel.selectedBinForEdit.collectAsStateWithLifecycle()
            StructureGuard(
                feature = "warehouseStructure",
                action = if (selectedBin != null) PermissionAction.EDIT else PermissionAction.CREATE
            ) {
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
}
@Composable
private fun SalesRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
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
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
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

    @Composable
    fun SalesGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "sales",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "sales",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        // 1. Dashboard (catalog key: "dashboard") - Action: VIEW
        "sales_dashboard" -> SalesGuard("dashboard", PermissionAction.VIEW) {
            SalesDashboardScreen(
                onClose = onGoBack,
                onNavigateToOrders = { onSafeNavigate("sales_sales_orders", PermissionAction.VIEW) },
                onNavigateToQuotations = { onSafeNavigate("sales_pricing_quotation", PermissionAction.VIEW) },
                onNavigateToCustomers = { onSafeNavigate("sales_customers", PermissionAction.VIEW) },
                onNavigateToLeads = { onSafeNavigate("sales_lead", PermissionAction.VIEW) },
                onNavigateToPipeline = { onSafeNavigate("sales_opportunity_pipeline", PermissionAction.VIEW) }
            )
        }

        // 2. Lead Management (catalog key: "leadManagement") - Actions: VIEW, CREATE, EDIT
        "sales_lead" -> SalesGuard("leadManagement", PermissionAction.VIEW) {
            LeadScreenContent(
                onCreateLead = { onSafeNavigate("create_lead", PermissionAction.CREATE) },
                onViewLead = { onSafeNavigate("view_lead", PermissionAction.VIEW) },
                onEditLead = { onSafeNavigate("edit_lead", PermissionAction.EDIT) },
                onClose = {
                    onSalesSettingsModeChange(false)
                    onGoBack()
                },
                onBreadCrumbClick = { onOpenModulesPanel("Sales") }
            )
        }

        "create_lead" -> SalesGuard("leadManagement", PermissionAction.CREATE) {
            LeadFormScreen(
                mode = LeadFormMode.CREATE,
                onBack = onGoBack
            )
        }

        "view_lead" -> SalesGuard("leadManagement", PermissionAction.VIEW) {
            LeadFormScreen(
                mode = LeadFormMode.VIEW,
                onBack = onGoBack,
                onEditRequested = { onSafeNavigate("edit_lead", PermissionAction.EDIT) },
                onConvertToOpportunity = { onSafeNavigate("convert_lead_to_opportunity", PermissionAction.CREATE) }
            )
        }

        "edit_lead" -> SalesGuard("leadManagement", PermissionAction.EDIT) {
            LeadFormScreen(
                mode = LeadFormMode.EDIT,
                onBack = onGoBack,
                onConvertToOrder = { orderReviewData ->
                    // 1. Check plan subscription first
                    if (!permissionViewModel.isPlanFeatureSubscribed("sales", "orders")) {
                        onPaywallRequired(permissionViewModel.getPaywallConfigForModule("sales"))
                    } else if (permissionViewModel.canUserAction("sales", "orders", PermissionAction.CREATE)) {
                        // 2. Plan is valid and user has permission
                        onPendingOrderReviewDataChange(orderReviewData)
                        onOrderFlowOriginChange("lead")
                        onNavigate("create_order")
                    } else {
                        // 3. Plan is valid, trigger access restricted flow
                        onSafeNavigate("create_order", PermissionAction.CREATE)
                    }
                }
            )
        }

        // 3. Opportunities Management (catalog key: "opportunities") - Actions: VIEW, CREATE, EDIT
        "sales_opportunity_pipeline" -> SalesGuard("opportunities", PermissionAction.VIEW) {
            OpportunityPipelineScreen(
                onClose = onGoBack,
                onCreateLead = { onSafeNavigate("create_lead", PermissionAction.CREATE) },
                onAddDeal = {
                    onOpportunityIdSelected(null)
                    onSafeNavigate("create_opportunity", PermissionAction.CREATE)
                },
                onClearAll = { }
            )
        }

        "convert_lead_to_opportunity" -> SalesGuard("leadManagement", PermissionAction.EDIT) {
            ConvertLeadToOpportunityScreen(
                onClose = onGoBack,
                onConversionSuccess = {
                    onGoBack()
                }
            )
        }

        "sales_opportunities" -> SalesGuard("opportunities", PermissionAction.VIEW) {
            OpportunitiesListScreen(
                onClose = onGoBack,
                onAddDeal = {
                    onOpportunityIdSelected(null)
                    onSafeNavigate("create_opportunity", PermissionAction.CREATE)
                },
                onViewOpportunity = { item ->
                    onOpportunityIdSelected(item.id)
                    onSafeNavigate("opportunity_detail", PermissionAction.VIEW)
                },
                onEditOpportunity = { item ->
                    onOpportunityIdSelected(item.id)
                    onSafeNavigate("create_opportunity", PermissionAction.EDIT)
                }
            )
        }

        "create_opportunity" -> SalesGuard(
            feature = "opportunities",
            action = if (selectedOpportunityId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateOpportunityScreen(
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
        }

        "opportunity_detail" -> SalesGuard("opportunities", PermissionAction.VIEW) {
            OpportunityDetailScreen(
                opportunityId = selectedOpportunityId.orEmpty(),
                onClose = {
                    onOpportunityIdSelected(null)
                    onGoBack()
                },
                onEdit = {
                    onSafeNavigate("create_opportunity", PermissionAction.EDIT)
                },
                onConvertToOrder = { onSafeNavigate("create_order", PermissionAction.CREATE) }
            )
        }

        // 4. Customer Management (catalog key: "customers") - Actions: VIEW, CREATE, EDIT, DELETE
        "sales_customers" -> SalesGuard("customers", PermissionAction.VIEW) {
            CustomerScreen(
                navController = navController,
                onClose = onGoBack,
                onCreateCustomer = { onSafeNavigate("create_customer", PermissionAction.CREATE) },
                onView = { customer ->
                    onCustomerSelected(customer)
                    onSafeNavigate("view_customer", PermissionAction.VIEW)
                },
                onEdit = { customer ->
                    onCustomerSelected(customer)
                    onSafeNavigate("edit_customer", PermissionAction.EDIT)
                },
                onDelete = { customer ->
                    // 1. Check plan subscription first
                    if (!permissionViewModel.isPlanFeatureSubscribed("sales", "customers")) {
                        onPaywallRequired(permissionViewModel.getPaywallConfigForModule("sales"))
                    } else if (permissionViewModel.canUserAction("sales", "customers", PermissionAction.DELETE)) {
                        // 2. Plan is valid and user has delete permission
                        customerViewModel.deleteCustomer(customer.id)
                    } else {
                        // 3. Plan is valid, trigger permission flow
                        onSafeNavigate("sales_customers", PermissionAction.DELETE)
                    }
                },
                onBreadCrumbClick = { onOpenModulesPanel("Sales") }
            )
        }

        "create_customer" -> SalesGuard("customers", PermissionAction.CREATE) {
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

        "view_customer", "edit_customer" -> SalesGuard(
            feature = "customers",
            action = if (screen == "edit_customer") PermissionAction.EDIT else PermissionAction.VIEW
        ) {
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
                    onRequestEdit = { onSafeNavigate("edit_customer", PermissionAction.EDIT) }
                )
            } else {
                onGoBack()
            }
        }

        "view_customer_recent" -> SalesGuard("customers", PermissionAction.VIEW) {
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
                    onRequestEdit = { onSafeNavigate("edit_customer", PermissionAction.EDIT) }
                )
            } else {
                onGoBack()
            }
        }

        // 5. Measurements (catalog key: "measurements") - Action: VIEW
        "sales_measurements" -> SalesGuard("measurements", PermissionAction.VIEW) {
            MeasurementsScreen(
                navController = navController,
                onBack = onGoBack,
                onCreateMeasurement = {
                    onSafeNavigate("measurement_entry", PermissionAction.CREATE)
                },
                onViewMeasurement = { item ->
                    onMeasurementItemSelected(item)
                    onSafeNavigate("measurements_available_view", PermissionAction.VIEW)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Sales") }
            )
        }

        "measurement_entry" -> SalesGuard(
            feature = if (pendingOrderReviewData != null) "orders" else "measurements",
            action = if (pendingOrderReviewData != null) PermissionAction.CREATE else PermissionAction.VIEW
        ) {
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

        "measurements_available_view" -> SalesGuard("measurements", PermissionAction.VIEW) {
            MeasurementsAvailableDialog(
                customerId = selectedMeasurementItem?.customerId,
                onGarmentSelected = {
                    onSafeNavigate("measurement_management_view", PermissionAction.VIEW)
                },
                onAddNew = {
                    onSafeNavigate("measurement_management_view", PermissionAction.VIEW)
                },
                onClose = onGoBack
            )
        }

        "measurement_management_view" -> SalesGuard("measurements", PermissionAction.VIEW) {
            MeasurementManagementScreen(
                customerName = selectedMeasurementItem?.customerName ?: "Hameed Rahman",
                phone = selectedMeasurementItem?.contact ?: "+1 (555) 123-4567",
                lastOrderDate = selectedMeasurementItem?.measuredDate ?: "Dec 20, 2025",
                onAddField = { onSafeNavigate("measurement_fields_config", PermissionAction.EDIT) },
                onClose = onGoBack
            )
        }

        "measurement_fields_config" -> SalesGuard("measurements", PermissionAction.VIEW) {
            MeasurementFieldsConfigScreen(
                onAddNewCategory = { onSafeNavigate("sales_add_garment_category", PermissionAction.CREATE) },
                onClose = onGoBack
            )
        }

        // 6. Sales Orders (catalog key: "orders") - Actions: VIEW, CREATE, EDIT
        "sales_sales_orders" -> SalesGuard("orders", PermissionAction.VIEW) {
            SalesOrderScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack,
                onCreateOrder = {
                    onPendingOrderReviewDataChange(null)
                    onSafeNavigate("create_order", PermissionAction.CREATE)
                },
                onViewOrder = { orderId ->
                    onOrderIdSelected(orderId)
                    onSafeNavigate("order_overview", PermissionAction.VIEW)
                },
                onEditOrder = { orderId ->
                    // 1. Check plan subscription first
                    if (!permissionViewModel.isPlanFeatureSubscribed("sales", "orders")) {
                        onPaywallRequired(permissionViewModel.getPaywallConfigForModule("sales"))
                    } else if (permissionViewModel.canUserAction("sales", "orders", PermissionAction.EDIT)) {
                        // 2. Plan is valid and user has edit permission
                        onEditOrderIdChange(orderId)
                    } else {
                        // 3. Plan is valid, trigger permission flow
                        onSafeNavigate("create_order", PermissionAction.EDIT)
                    }
                },
                onBreadCrumbClick = { onOpenModulesPanel("Sales") }
            )
        }

        "create_order" -> SalesGuard(
            feature = "orders",
            action = if (pendingOrderReviewData?.orderId != null || pendingOrderReviewData?.editOrderId != null) {
                PermissionAction.EDIT
            } else {
                PermissionAction.CREATE
            }
        ) {
            CreateOrderScreen(
                initialData = pendingOrderReviewData,
                onBack = {
                    onGoBack()
                },
                onCancel = {
                    onGoBack()
                },
                onNextStep = { orderReviewData ->
                    onPendingOrderReviewDataChange(orderReviewData)
                    measurementIndex = 0
                    onNavigate("measurement_entry")
                }
            )
        }

        "order_preview" -> SalesGuard("orders", PermissionAction.CREATE) {
            pendingOrderReviewData?.let { data ->
                // True while either create or update order request is running
                val isFinalizing = orderActionState is OrderActionState.Loading ||
                        updateOrderState is UpdateOrderState.Loading

                OrderPreviewScreen(
                    orderData = data,
                    isFinalizing = isFinalizing,
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
                            // Block duplicate submission while loading
                        } else {
                            // Pass selected payment term code (fallback to paymentTermId if code is empty)
                            val selectedPaymentTerm = draft.paymentTermId

                            val built = buildCreateOrderFields(
                                draft = draft,
                                measurements = OrderFlowStore.measurements,
                                salespersonId = draft.salespersonId,
                                paymentTermId = selectedPaymentTerm,
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

                                // Resolve MongoDB ID when editing an existing order
                                val editId = when {
                                    !data.orderId.isNullOrBlank() && !data.orderId.startsWith("ORD") -> data.orderId
                                    !data.editOrderId.isNullOrBlank() && !data.editOrderId.startsWith("ORD") -> data.editOrderId
                                    else -> data.orderId ?: data.editOrderId
                                }

                                if (!editId.isNullOrBlank()) {
                                    salesOrderViewModel.updateOrder(
                                        orderId = editId,
                                        fields = built.fields,
                                        existingImages = data.existingImageUrls,
                                        imageParts = binaryImageParts,
                                        voiceNotePart = null
                                    )
                                } else {
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
                        onNavigate("sales_sales_orders")
                    }
                )
            } ?: run { onGoBack() }
        }

        "order_overview" -> SalesGuard("orders", PermissionAction.VIEW) {
            selectedOrderId?.let { id ->
                OrderOverviewScreen(
                    orderId = id,
                    showConvertToInvoice = isOrderFromFinance,
                    onConvertToInvoice = { orderData ->
                        onPrefillOrderForInvoiceChange(orderData)
                        onSafeNavigate("finance_create_sales_invoice", PermissionAction.CREATE)
                    },
                    onClose = {
                        onOrderFromFinanceChange(false)
                        onGoBack()
                    },
                    onEditOrder = { reviewData ->
                        // 1. Check plan subscription first
                        if (!permissionViewModel.isPlanFeatureSubscribed("sales", "orders")) {
                            onPaywallRequired(permissionViewModel.getPaywallConfigForModule("sales"))
                        } else if (permissionViewModel.canUserAction("sales", "orders", PermissionAction.EDIT)) {
                            // 2. Plan is valid and user has permission
                            onEditOrderIdChange(id)
                            onPendingOrderReviewDataChange(reviewData)
                            onNavigate("create_order")
                        } else {
                            // 3. Plan is valid, trigger access restricted flow
                            onSafeNavigate("create_order", PermissionAction.EDIT)
                        }
                    },
                    onCreateNew = {
                        onEditOrderIdChange(null)
                        onPendingOrderReviewDataChange(null)
                        onSafeNavigate("create_order", PermissionAction.CREATE)
                    }
                )
            } ?: run { onGoBack() }
        }

        // 7. Pricing & Quotations (catalog key: "pricingQuotes") - Actions: VIEW, CREATE, EDIT
        "sales_pricing_overview" -> SalesGuard("pricingQuotes", PermissionAction.VIEW) {
            PricingScreen(
                onClose = {
                    onSalesSettingsModeChange(false)
                    onGoBack()
                },
                onAddNewPricing = {
                    onEditingPricingIdChange(null)
                    onSafeNavigate("create_garment_pricing", PermissionAction.CREATE)
                },
                onCardClick = { pricingId ->
                    onEditingPricingIdChange(pricingId)
                    onSafeNavigate("create_garment_pricing", PermissionAction.EDIT)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Sales") }
            )
        }

        "garment_pricing_list" -> SalesGuard("pricingQuotes", PermissionAction.VIEW) {
            GarmentPricingListScreen(
                onBack = onGoBack,
                onAddNewPricing = {
                    onEditingPricingIdChange(null)
                    onSafeNavigate("create_garment_pricing", PermissionAction.CREATE)
                }
            )
        }

        "create_garment_pricing" -> SalesGuard(
            feature = "pricingQuotes",
            action = if (editingPricingId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddGarmentPricingScreen(
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
        }

        "sales_pricing_quotation" -> SalesGuard("pricingQuotes", PermissionAction.VIEW) {
            QuotationScreen(
                onClose = {
                    onSalesSettingsModeChange(false)
                    onGoBack()
                },
                onAddNe = {
                    onEditingPricingIdChange(null)
                    onQuotationScreenModeChange("create")
                    onSafeNavigate("create_quotation", PermissionAction.CREATE)
                },
                onView = { id ->
                    onEditingPricingIdChange(id)
                    onSafeNavigate("view_quotation", PermissionAction.VIEW)
                },
                onEdit = { id ->
                    onEditingPricingIdChange(id)
                    onQuotationScreenModeChange("edit")
                    onSafeNavigate("create_quotation", PermissionAction.EDIT)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Sales") }
            )
        }

        "view_quotation" -> SalesGuard("pricingQuotes", PermissionAction.VIEW) {
            QuotationViewScreen(
                quotationId = editingPricingId.orEmpty(),
                onClose = {
                    onEditingPricingIdChange(null)
                    onGoBack()
                },
                onEdit = {
                    onQuotationScreenModeChange("edit")
                    onSafeNavigate("create_quotation", PermissionAction.EDIT)
                }
            )
        }

        "create_quotation" -> SalesGuard(
            feature = "pricingQuotes",
            action = if (quotationScreenMode == "edit") PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateQuotationScreen(
                quotationId = editingPricingId,
                mode = quotationScreenMode,
                onClose = onGoBack,
                onSave = onGoBack,
                token = token
            )
        }

        // 8. Billing & Payments (catalog key: "billingAndPayments") - Action: VIEW
        "sales_payment_and_billing" -> SalesGuard("billingAndPayments", PermissionAction.VIEW) {
            PaymentListingScreen(
                navController = navController,
                widthSizeClass = widthSizeClass,
                onBack = {
                    onSalesSettingsModeChange(false)
                    onGoBack()
                },
                onBreadCrumbClick = { onOpenModulesPanel("Sales") },
                onPaymentClick = { orderId ->
                    onOrderIdSelected(orderId)
                    onSafeNavigate("payment_detail", PermissionAction.VIEW)
                }
            )
        }

        "payment_detail" -> SalesGuard("billingAndPayments", PermissionAction.VIEW) {
            PaymentInformationScreen(
                orderId = selectedOrderId.orEmpty(),
                onClose = onGoBack
            )
        }

        // 9. Sales Settings Submodules
        "sales_settings" -> SalesGuard("garmentMeasurement", PermissionAction.VIEW) {
            SalesSettingsScreen(
                navController = navController,
                onClose = onGoBack,
                onAddSegmentClick = {
                    selectedSegmentForEdit = null
                    settingsViewModel.clearSelectedSegmentDetail()
                    onSafeNavigate("sales_add_segment", PermissionAction.CREATE)
                },
                onEditSegmentClick = { segment ->
                    selectedSegmentForEdit = segment
                    settingsViewModel.fetchSegmentById(segment.id)
                    onSafeNavigate("sales_add_segment", PermissionAction.EDIT)
                },
                onAddGarmentClick = { onSafeNavigate("sales_add_garment", PermissionAction.CREATE) },
                onConfigureGarmentClick = { segmentId, garmentId, garmentTitle ->
                    settingsViewModel.setSelectedGarmentForDetail(segmentId, garmentId, garmentTitle)
                    onSafeNavigate("sales_category_detail", PermissionAction.EDIT)
                },
                viewModel = settingsViewModel
            )
        }

        "sales_category_detail" -> SalesGuard("garmentMeasurement", PermissionAction.EDIT) {
            val segmentId by settingsViewModel.selectedSegmentIdForStyle.collectAsStateWithLifecycle()
            val garmentId by settingsViewModel.selectedGarmentIdForStyle.collectAsStateWithLifecycle()
            val title by settingsViewModel.selectedGarmentTitleForStyle.collectAsStateWithLifecycle()

            GarmentCategoryDetailScreen(
                categoryTitle = title,
                segmentId = segmentId,
                garmentId = garmentId,
                onClose = onGoBack,
                onAddGarmentCategoryClick = { onSafeNavigate("sales_add_garment_category", PermissionAction.CREATE) },
                viewModel = settingsViewModel
            )
        }

        "sales_add_segment" -> SalesGuard(
            feature = "garmentMeasurement",
            action = if (selectedSegmentForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddSegmentScreen(
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
        }

        "sales_garment_type" -> SalesGuard("garmentMeasurement", PermissionAction.VIEW) {
            GarmentTypeContent(
                viewModel = settingsViewModel,
                onClose = {
                    onSalesSettingsModeChange(false)
                    onGoBack()
                },
                onAddSegmentClick = {
                    selectedSegmentForEdit = null
                    settingsViewModel.clearSelectedSegmentDetail()
                    onSafeNavigate("sales_add_segment", PermissionAction.CREATE)
                },
                onEditSegmentClick = { segment ->
                    selectedSegmentForEdit = segment
                    settingsViewModel.fetchSegmentById(segment.id)
                    onSafeNavigate("sales_add_segment", PermissionAction.EDIT)
                },
                onAddGarmentClick = { onSafeNavigate("sales_add_garment", PermissionAction.CREATE) },
                onConfigureGarmentClick = { segmentId, garmentId, garmentTitle ->
                    settingsViewModel.setSelectedGarmentForDetail(segmentId, garmentId, garmentTitle)
                    onSafeNavigate("sales_category_detail", PermissionAction.EDIT)
                }
            )
        }

        "sales_garment_profile" -> SalesGuard("garmentMeasurement", PermissionAction.VIEW) {
            GarmentProfileConfigScreen(
                profileTitle = "Men's Shirt Profile",
                isActive = isGarmentActive,
                onClose = onGoBack,
                onAddExistingClick = { onSafeNavigate("sales_add_existing_field", PermissionAction.CREATE) },
                onAddFieldClick = { onSafeNavigate("sales_create_measurement_field", PermissionAction.CREATE) },
                onPreviewClick = { onSafeNavigate("sales_configuration_preview", PermissionAction.VIEW) },
                onToggleActiveState = { isGarmentActive = it }
            )
        }

        "sales_configuration_preview" -> SalesGuard("garmentMeasurement", PermissionAction.VIEW) {
            ConfigurationPreviewScreen(
                garmentTitle = "Men's Shirt",
                onClose = onGoBack,
                onBackToEdit = onGoBack,
                onActivateConfirmed = {
                    isGarmentActive = true
                    onGoBack()
                }
            )
        }

        "sales_add_existing_field" -> SalesGuard("garmentMeasurement", PermissionAction.CREATE) {
            AddExistingFieldScreen(
                onClose = onGoBack,
                onAddSelected = { onGoBack() },
                viewModel = settingsViewModel
            )
        }

        "sales_create_measurement_field" -> SalesGuard("garmentMeasurement", PermissionAction.CREATE) {
            CreateMeasurementFieldScreen(
                onClose = onGoBack,
                onSave = { onGoBack() },
                viewModel = settingsViewModel
            )
        }

        "sales_add_garment" -> SalesGuard("garmentMeasurement", PermissionAction.CREATE) {
            AddNewGarmentScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onGarmentCreated = onGoBack
            )
        }

        "sales_add_garment_category" -> SalesGuard("garmentMeasurement", PermissionAction.CREATE) {
            AddNewGarmentCategoryScreen(
                onClose = onGoBack,
                onGarmentCategoryCreated = onGoBack,
                viewModel = settingsViewModel
            )
        }

        "sales_garment_pricing_setup" -> SalesGuard("pricingGarment", PermissionAction.VIEW) {
            PricingSetupScreen(
                onClose = onGoBack,
                onAddGarmentPricing = {
                    onEditingPricingIdChange(null)
                    onSafeNavigate("sales_add_garment_pricing", PermissionAction.CREATE)
                },
                onAddFabricPricing = { onSafeNavigate("sales_add_fabric_pricing", PermissionAction.CREATE) },
                onAddWorkPricing = {
                    onEditingPricingIdChange(null)
                    onSafeNavigate("sales_add_work_pricing", PermissionAction.CREATE)
                },
                onEditGarmentPricing = { id ->
                    onEditingPricingIdChange(id)
                    onSafeNavigate("sales_add_garment_pricing", PermissionAction.EDIT)
                },
                onEditWorkPricing = { id ->
                    onEditingPricingIdChange(id)
                    onSafeNavigate("sales_add_work_pricing", PermissionAction.EDIT)
                }
            )
        }

        "sales_add_garment_pricing" -> SalesGuard(
            feature = "pricingGarment",
            action = if (editingPricingId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddNewGarmentPricingScreen(
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
        }

        "sales_add_fabric_pricing" -> SalesGuard("pricingFabric", PermissionAction.CREATE) {
            AddFabricPriceScreen(
                onClose = onGoBack,
                onSaveSuccess = onGoBack
            )
        }

        "sales_add_work_pricing" -> SalesGuard(
            feature = "pricingWork",
            action = if (editingPricingId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddWorkPricingScreen(
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
        }

        "sales_measurement_list" -> SalesGuard("measurementList", PermissionAction.VIEW) {
            MeasurementListScreen(
                onClose = onGoBack,
                onAddMeasurement = { onSafeNavigate("sales_create_measurement_field", PermissionAction.CREATE) },
                viewModel = settingsViewModel
            )
        }
    }
}

// ── FINANCE ROUTER ──
@Composable
private fun FinanceRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    onPurchaseFromFinanceChange: (Boolean) -> Unit = {},
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
    selectedCreditNoteId: String? = null,
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
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onAccessRestricted: (String) -> Unit = {},
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    val financeViewModel: FinanceViewModel = hiltViewModel()
    var selectedPurchaseOrderForEdit by remember { mutableStateOf<PurchaseOrder?>(null) }
    var selectedTaxRateForEdit by remember { mutableStateOf<TaxRateItemDto?>(null) }
    var isEditingGst by remember { mutableStateOf(false) }

    @Composable
    fun FinanceGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "finance",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "finance",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    // Helper for shared inventory/finance purchase screens: check plan subscription first, then permissions
    fun navigateWithFinancePermission(
        feature: String,
        action: PermissionAction,
        targetRoute: String
    ) {
        // 1. Highest priority: check plan subscription first
        if (!permissionViewModel.isPlanFeatureSubscribed("finance", feature)) {
            onPaywallRequired(permissionViewModel.getPaywallConfigForModule("finance"))
            return
        }
        // 2. Plan is subscribed: now check role permission
        if (!permissionViewModel.canUserAction("finance", feature, action)) {
            if (action == PermissionAction.VIEW) {
                onAccessRestricted("Finance - $feature")
            }
            return
        }
        onPurchaseFromFinanceChange(true)
        onNavigate(targetRoute)
    }

    when (screen) {
        // 0. Finance Dashboard (catalog key: "dashboard") - Action: VIEW
        "finance_dashboard" -> FinanceGuard("dashboard", PermissionAction.VIEW) {
            FinanceOverviewScreen(
                onClose = onGoBack,
                onViewDistribution = { onSafeNavigate("finance_chart_of_accounts", PermissionAction.VIEW) },
                onViewFunds = { onSafeNavigate("finance_chart_of_accounts", PermissionAction.VIEW) },
                onSummaryView = { title ->
                    when (title) {
                        "Sales" -> onSafeNavigate("finance_sales_invoices", PermissionAction.VIEW)
                        "Purchase" -> onSafeNavigate("finance_purchase_orders", PermissionAction.VIEW)
                        "General Ledger" -> onSafeNavigate("finance_chart_of_accounts", PermissionAction.VIEW)
                        "Expenses" -> onSafeNavigate("finance_expenses", PermissionAction.VIEW)
                        "Funds" -> onSafeNavigate("finance_chart_of_accounts", PermissionAction.VIEW)
                    }
                },
                onGoTo = { title ->
                    when (title) {
                        "Sales" -> onSafeNavigate("finance_sales_invoices", PermissionAction.VIEW)
                        "Purchase" -> onSafeNavigate("finance_purchase_orders", PermissionAction.VIEW)
                        "General Ledger" -> onSafeNavigate("finance_trial_balance", PermissionAction.VIEW)
                        "Expenses" -> onSafeNavigate("finance_expenses", PermissionAction.VIEW)
                        "Funds" -> onSafeNavigate("finance_chart_of_accounts", PermissionAction.VIEW)
                    }
                },
                onAlertClick = { title ->
                    when (title) {
                        "Supplier Payments Due" -> onSafeNavigate("finance_payments_made", PermissionAction.VIEW)
                        "Expense Claims" -> onSafeNavigate("finance_expenses", PermissionAction.VIEW)
                        "Bank Reconciliation" -> onSafeNavigate("finance_trial_balance", PermissionAction.VIEW)
                    }
                }
            )
        }

        // 1. Finance Sales Orders (catalog key: "finOrders") - Actions: VIEW, CREATE, EDIT
        "finance_sales_orders" -> FinanceGuard("finOrders", PermissionAction.VIEW) {
            SalesOrderScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack,
                onCreateOrder = {
                    // 1. Check plan subscription first
                    if (!permissionViewModel.isPlanFeatureSubscribed("finance", "finOrders")) {
                        onPaywallRequired(permissionViewModel.getPaywallConfigForModule("finance"))
                    } else if (permissionViewModel.canUserAction("finance", "finOrders", PermissionAction.CREATE)) {
                        // 2. Plan is valid and user has permission
                        onPendingOrderReviewDataChange(null)
                        onNavigate("create_order")
                    } else {
                        // 3. Plan is valid, trigger access restricted flow
                        onSafeNavigate("finance_sales_orders", PermissionAction.CREATE)
                    }
                },
                onViewOrder = { orderId ->
                    // 1. Check plan subscription first
                    if (!permissionViewModel.isPlanFeatureSubscribed("finance", "finOrders")) {
                        onPaywallRequired(permissionViewModel.getPaywallConfigForModule("finance"))
                    } else {
                        onOrderIdSelected(orderId)
                        onOrderFromFinanceChange(true)
                        onNavigate("order_overview")
                    }
                },
                onEditOrder = { orderId ->
                    // 1. Check plan subscription first
                    if (!permissionViewModel.isPlanFeatureSubscribed("finance", "finOrders")) {
                        onPaywallRequired(permissionViewModel.getPaywallConfigForModule("finance"))
                    } else if (permissionViewModel.canUserAction("finance", "finOrders", PermissionAction.EDIT)) {
                        // 2. Plan is valid and user has permission
                        onEditOrderIdChange(orderId)
                    } else {
                        // 3. Plan is valid, trigger access restricted flow
                        onSafeNavigate("finance_sales_orders", PermissionAction.EDIT)
                    }
                },
                onBreadCrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        // 2. Sales Invoices (catalog key: "finInvoices") - Actions: VIEW, CREATE
        "finance_create_sales_invoice" -> FinanceGuard("finInvoices", PermissionAction.CREATE) {
            CreateSalesInvoiceScreen(
                prefillOrder = prefillOrderForInvoice,
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
        }

        "finance_sales_invoices" -> FinanceGuard("finInvoices", PermissionAction.VIEW) {
            FinanceInvoiceScreen(
                onClose = onGoBack,
                onInvoiceClick = { invoice ->
                    onInvoiceSelected(invoice.id)
                    onSafeNavigate("finance_invoice_detail", PermissionAction.VIEW)
                },
                onCreateInvoice = {
                    onPrefillOrderForInvoiceChange(null)
                    onSafeNavigate("finance_create_sales_invoice", PermissionAction.CREATE)
                },
                onRecordPayment = { invoiceData ->
                    onInvoiceForPaymentChange(invoiceData)
                    onSafeNavigate("finance_record_payment", PermissionAction.CREATE)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        "finance_invoice_detail" -> FinanceGuard("finInvoices", PermissionAction.VIEW) {
            selectedInvoiceId?.let { id ->
                InvoiceDetailScreen(
                    invoiceId = id,
                    onClose = {
                        onInvoiceSelected(null)
                        onGoBack()
                    },
                    onRecordPayment = { invoiceData ->
                        onInvoiceForPaymentChange(invoiceData)
                        onSafeNavigate("finance_record_payment", PermissionAction.CREATE)
                    },
                    token = token
                )
            } ?: run { onGoBack() }
        }

        // 3. Customer Receipts (catalog key: "finReceipts") - Actions: VIEW, CREATE
        "finance_receipts" -> FinanceGuard("finReceipts", PermissionAction.VIEW) {
            CustomerReceiptsListScreen(
                viewModel = financeViewModel,
                onClose = onGoBack,
                onCreateReceiptClick = {
                    onSafeNavigate("finance_create_payment_receipt", PermissionAction.CREATE)
                },
                onReceiptClick = { receipt ->
                    onReceiptSelected(receipt.id)
                    onSafeNavigate("finance_receipt_detail", PermissionAction.VIEW)
                }
            )
        }

        "finance_receipt_detail" -> FinanceGuard("finReceipts", PermissionAction.VIEW) {
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

        "finance_create_payment_receipt" -> FinanceGuard("finReceipts", PermissionAction.CREATE) {
            CreatePaymentReceiptScreen(
                financeViewModel = financeViewModel,
                customerViewModel = customerViewModel,
                onClose = onGoBack,
                onPaymentRecorded = {
                    financeViewModel.fetchCustomerReceipts(page = 1)
                }
            )
        }

        "finance_record_payment" -> FinanceGuard("finReceipts", PermissionAction.CREATE) {
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
                    onSavePayment = { customerId, branchId, invoiceId, amount, mode, depositAccountId, isoDate, refNo, notes, uris, _ ->
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
                onGoBack()
            }
        }

        // 4. Customer Credit Notes (catalog key: "finCustomerCredits") - Actions: VIEW, CREATE
        "finance_credits" -> FinanceGuard("finCustomerCredits", PermissionAction.VIEW) {
            com.cuso.tailor.view.home.finance.credit.CreditNotesListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onNavigateToCreate = {
                    onSafeNavigate("finance_create_credit", PermissionAction.CREATE)
                },
                onNavigateToDetail = { id ->
                    onCreditNoteSelected(id)
                    onSafeNavigate("finance_credit_detail", PermissionAction.VIEW)
                }
            )
        }

        "finance_create_credit" -> FinanceGuard("finCustomerCredits", PermissionAction.CREATE) {
            CreateCreditNoteScreen(
                viewModel = financeViewModel,
                inventoryViewModel = inventoryViewModel,
                customerViewModel = customerViewModel,
                branchViewModel = branchViewModel,
                onBack = onGoBack
            )
        }

        "finance_credit_detail" -> FinanceGuard("finCustomerCredits", PermissionAction.VIEW) {
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

        // 5. Purchase Bills (catalog key: "finPurchaseBills") - Action: VIEW
        "finance_purchase_invoices" -> FinanceGuard("finPurchaseBills", PermissionAction.VIEW) {
            PurchaseInvoiceScreen(
                onClose = onGoBack,
                onInvoiceClick = { invoice ->
                    onPurchaseInvoiceSelected(invoice)
                    onSafeNavigate("finance_purchase_invoice_detail", PermissionAction.VIEW)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        "finance_purchase_invoice_detail" -> FinanceGuard("finPurchaseBills", PermissionAction.VIEW) {
            selectedPurchaseInvoice?.let { invoice ->
                PurchaseInvoiceDetailScreen(
                    invoiceId = invoice.id,
                    onClose = onGoBack
                )
            } ?: run { onGoBack() }
        }

        // 6. Purchase Orders (catalog key: "finPurchaseOrders") - Actions: VIEW, CREATE
        "finance_purchase_orders" -> FinanceGuard("finPurchaseOrders", PermissionAction.VIEW) {
            POListScreen(
                viewModel = inventoryViewModel,
                onNavigateToCreate = {
                    selectedPurchaseOrderForEdit = null
                    navigateWithFinancePermission(
                        feature = "finPurchaseOrders",
                        action = PermissionAction.CREATE,
                        targetRoute = "inventory_create_purchase_order_flow"
                    )
                },
                onNavigateToDetail = { po ->
                    onPurchaseOrderForDetailSelected(po)
                    navigateWithFinancePermission(
                        feature = "finPurchaseOrders",
                        action = PermissionAction.VIEW,
                        targetRoute = "inventory_purchase_order_detail_flow"
                    )
                },
                onClose = onGoBack
            )
        }

        // 7. Purchase Receives (catalog key: "finPurchaseReceives") - Actions: VIEW, CREATE
        "finance_purchase_receive" -> FinanceGuard("finPurchaseReceives", PermissionAction.VIEW) {
            AllOrdersScreen(
                onClose = onGoBack,
                onCreateOrderClick = {
                    navigateWithFinancePermission(
                        feature = "finPurchaseOrders",
                        action = PermissionAction.CREATE,
                        targetRoute = "inventory_create_purchase_order"
                    )
                },
                onOrderClick = { poId, _ ->
                    onReceivePoIdSelected(poId)
                    navigateWithFinancePermission(
                        feature = "finPurchaseReceives",
                        action = PermissionAction.VIEW,
                        targetRoute = "inventory_purchase_receive_detail"
                    )
                }
            )
        }

        // 8. Purchase Bills List (catalog key: "finPurchaseBills") - Actions: VIEW, CREATE
        "finance_bills_list" -> FinanceGuard("finPurchaseBills", PermissionAction.VIEW) {
            AllBillListScreen(
                onClose = onGoBack,
                onCreateOrder = {
                    navigateWithFinancePermission(
                        feature = "finPurchaseOrders",
                        action = PermissionAction.CREATE,
                        targetRoute = "inventory_create_purchase_order"
                    )
                },
                onInvoiceClick = { billItem ->
                    if (billItem.id.isNotBlank()) {
                        onInvoiceSelected(billItem.id)
                        navigateWithFinancePermission(
                            feature = "finPurchaseBills",
                            action = PermissionAction.VIEW,
                            targetRoute = "inventory_payable_purchase_detail"
                        )
                    }
                },
                onOptionsClick = { }
            )
        }

        // 9. Purchase Returns (catalog key: "finPurchaseReturns") - Actions: VIEW, CREATE
        "finance_purchase_returns" -> FinanceGuard("finPurchaseReturns", PermissionAction.VIEW) {
            FinanceReturnScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onCreateReturnClick = {
                    onSafeNavigate("finance_create_purchase_return", PermissionAction.CREATE)
                },
                onReturnClick = { returnId ->
                    if (returnId.isNotBlank()) {
                        onPurchaseReturnIdSelected(returnId)
                        onSafeNavigate("finance_purchase_return_detail", PermissionAction.VIEW)
                    }
                }
            )
        }

        "finance_create_purchase_return" -> FinanceGuard("finPurchaseReturns", PermissionAction.CREATE) {
            CreatePurchaseReturnScreen(
                onBack = onGoBack,
                onCreateReturn = {
                    onGoBack()
                }
            )
        }

        "finance_purchase_return_detail" -> FinanceGuard("finPurchaseReturns", PermissionAction.VIEW) {
            selectedPurchaseReturnId?.takeIf { it.isNotBlank() }?.let { id ->
                PurchaseReturnDetailScreen(
                    returnId = id,
                    viewModel = financeViewModel,
                    onBack = {
                        onPurchaseReturnIdSelected(null)
                        onGoBack()
                    },
                    onSettleRefund = { },
                    onViewPo = { _ -> }
                )
            } ?: run {
                onGoBack()
            }
        }

        // 10. Supplier Credits (catalog key: "finSupplierCredits") - Action: VIEW
        "finance_vendor_credits" -> FinanceGuard("finSupplierCredits", PermissionAction.VIEW) {
            VendorCreditListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onCreditClick = { _ -> }
            )
        }

        // 11. Payments Made (catalog key: "finPaymentsMade") - Action: VIEW
        "finance_payments_made" -> FinanceGuard("finPaymentsMade", PermissionAction.VIEW) {
            AllInventoryPaymentScreen(
                onClose = onGoBack,
                onPaymentSelect = { paymentId ->
                    if (paymentId.isNotBlank()) {
                        onPaymentModeSelected(paymentId)
                        navigateWithFinancePermission(
                            feature = "finPaymentsMade",
                            action = PermissionAction.VIEW,
                            targetRoute = "inventory_payment_overview_detail"
                        )
                    }
                },
                onFilterClick = { }
            )
        }

        // 12. Finance Customers (catalog key: "finCustomers") - Actions: VIEW, CREATE, EDIT
        "finance_customers" -> FinanceGuard("finCustomers", PermissionAction.VIEW) {
            FinanceCustomerScreen(
                onClose = onGoBack,
                onCustomerClick = { customerId ->
                    onRecentCustomerIdSelected(customerId)
                    onSafeNavigate("finance_customer_detail", PermissionAction.VIEW)
                },
                onCustomerEdit = { customerId ->
                    onRecentCustomerIdSelected(customerId)
                    onSafeNavigate("finance_create_customer", PermissionAction.EDIT)
                },
                onAddCustomer = {
                    onRecentCustomerIdSelected(null)
                    onSafeNavigate("finance_create_customer", PermissionAction.CREATE)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        "finance_create_customer" -> FinanceGuard(
            feature = "finCustomers",
            action = if (selectedRecentCustomerId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateCustomerScreen(
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
        }

        "finance_customer_detail" -> FinanceGuard("finCustomers", PermissionAction.VIEW) {
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

        // 13. Finance Suppliers (catalog key: "finSuppliers") - Action: VIEW
        "finance_suppliers" -> FinanceGuard("finSuppliers", PermissionAction.VIEW) {
            InventorySuppliersScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Finance") },
                onSupplierClick = { supplierDto ->
                    onSupplierSelected(supplierDto.id)
                    onSafeNavigate("finance_supplier_detail", PermissionAction.VIEW)
                }
            )
        }

        "finance_supplier_detail" -> FinanceGuard("finSuppliers", PermissionAction.VIEW) {
            FinanceSupplierDetailScreen(
                supplier = selectedSupplier ?: SupplierRow(id = selectedSupplierId.orEmpty()),
                supplierId = selectedSupplierId.orEmpty(),
                onNewOrder = {
                    navigateWithFinancePermission(
                        feature = "finPurchaseOrders",
                        action = PermissionAction.CREATE,
                        targetRoute = "inventory_create_purchase_order_flow"
                    )
                },
                onClose = {
                    onSupplierSelected(null)
                    onGoBack()
                },
                onBreadcrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        // 14. Expense Management (catalog key: "expenses") - Action: VIEW
        "finance_expenses" -> FinanceGuard("expenses", PermissionAction.VIEW) {
            ExpensesScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        // 15. General Ledger (catalog keys: "finChartOfAccounts", "finJournalEntries") - Action: VIEW
        "finance_chart_of_accounts" -> FinanceGuard("finChartOfAccounts", PermissionAction.VIEW) {
            ChartOfAccountScreen(
                onClose = onGoBack,
                onBreadcrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        "finance_journal_screen" -> FinanceGuard("finJournalEntries", PermissionAction.VIEW) {
            ManualJournalEntryScreen(onClose = onGoBack)
        }

        "finance_trial_balance" -> FinanceGuard("finChartOfAccounts", PermissionAction.VIEW) {
            TrialBalanceScreen(
                onClose = onGoBack,
                onAccountClick = { accountId, accountName ->
                    onLedgerAccountSelected(accountId, accountName)
                    onSafeNavigate("finance_ledger", PermissionAction.VIEW)
                },
                onBreadcrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        "finance_ledger" -> FinanceGuard("finChartOfAccounts", PermissionAction.VIEW) {
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

        "finance_payments_received" -> FinanceGuard("finReceipts", PermissionAction.VIEW) {
            AllPaymentScreen(
                onViewPayment = { onSafeNavigate("payment_detail_screen", PermissionAction.VIEW) },
                onBreadCrumbClick = { onOpenModulesPanel("Finance") }
            )
        }

        "payment_detail_screen" -> FinanceGuard("finReceipts", PermissionAction.VIEW) {
            PaymentDetailScreenAR(onClose = onGoBack)
        }

        "finance_payments_mode" -> FinanceGuard("finCashBooks", PermissionAction.VIEW) {
            AllPaymentListScreen(
                onClose = onGoBack,
                onPaymentClick = { paymentId ->
                    onPaymentModeSelected(paymentId)
                    onSafeNavigate("payment_mode_detail", PermissionAction.VIEW)
                }
            )
        }

        "payment_mode_detail" -> FinanceGuard("finCashBooks", PermissionAction.VIEW) {
            PaymentDetailScreenAP(
                onClose = {
                    onPaymentModeSelected(null)
                    onGoBack()
                }
            )
        }

        // 16. Finance Settings / Taxes (catalog keys: "finTaxRegistration", "finTaxRatesRules")
        "finance_gst_settings" -> FinanceGuard("finTaxRegistration", PermissionAction.VIEW) {
            GstSettingsOverviewScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onAddGst = {
                    isEditingGst = false
                    settingsViewModel.clearSelectedTaxRegistration()
                    onSafeNavigate("finance_add_gst", PermissionAction.CREATE)
                },
                onEditGst = { selectedItem ->
                    isEditingGst = true
                    settingsViewModel.fetchTaxRegistrationById(selectedItem.id)
                    onSafeNavigate("finance_add_gst", PermissionAction.EDIT)
                }
            )
        }

        "finance_add_gst" -> FinanceGuard(
            feature = "finTaxRegistration",
            action = if (isEditingGst) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddGstSettingsScreen(
                settingsViewModel = settingsViewModel,
                branchViewModel = branchViewModel,
                inventoryViewModel = inventoryViewModel,
                salesViewModel = salesViewModel,
                onClose = {
                    isEditingGst = false
                    settingsViewModel.clearSelectedTaxRegistration()
                    onGoBack()
                },
                onSave = {
                    isEditingGst = false
                    settingsViewModel.clearSelectedTaxRegistration()
                    onGoBack()
                }
            )
        }

        "finance_tax_rates" -> FinanceGuard("finTaxRatesRules", PermissionAction.VIEW) {
            TaxRatesScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onAddTaxRate = {
                    selectedTaxRateForEdit = null
                    onSafeNavigate("finance_add_tax_rate", PermissionAction.CREATE)
                },
                onAddTaxGroup = {
                    onSafeNavigate("finance_add_tax_group", PermissionAction.CREATE)
                },
                onEditRate = { rateItem ->
                    selectedTaxRateForEdit = rateItem
                    onSafeNavigate("finance_add_tax_rate", PermissionAction.EDIT)
                },
                onDeleteRate = { _ -> }
            )
        }

        "finance_add_tax_rate" -> FinanceGuard(
            feature = "finTaxRatesRules",
            action = if (selectedTaxRateForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddTaxRateScreen(
                onClose = {
                    selectedTaxRateForEdit = null
                    onGoBack()
                },
                onSaveTaxRate = { _ ->
                    selectedTaxRateForEdit = null
                    onGoBack()
                }
            )
        }

        "finance_add_tax_group" -> FinanceGuard("finTaxRatesRules", PermissionAction.CREATE) {
            LaunchedEffect(Unit) {
                settingsViewModel.fetchTaxRates(page = 1, limit = 100)
            }

            val taxRatesList by settingsViewModel.taxRates.collectAsStateWithLifecycle()
            val isSaving by settingsViewModel.isSubmittingTaxGroup.collectAsStateWithLifecycle()

            val allTaxes = remember(taxRatesList) {
                taxRatesList.map { dto ->
                    TaxRateItem(
                        id = dto.id,
                        name = dto.name,
                        rate = dto.rate
                    )
                }
            }

            AddTaxGroupScreen(
                onClose = onGoBack,
                allTaxes = allTaxes,
                isLoading = isSaving,
                onCreateGroup = { name, taxRateIds ->
                    settingsViewModel.createTaxGroup(
                        name = name,
                        taxRateIds = taxRateIds,
                        onSuccess = { onGoBack() }
                    )
                }
            )
        }

        // 17. Funds & Banking (catalog keys: "finCashBooks", "finBankAccounts", "finBankStatements")
        "finance_cash_book" -> FinanceGuard("finCashBooks", PermissionAction.VIEW) {
            CashBookListScreen(
                viewModel = financeViewModel,
                branchViewModel = branchViewModel,
                onBack = onGoBack,
                onNavigateToDetail = { shiftId ->
                    onCashShiftSelected(shiftId)
                    onSafeNavigate("finance_cash_book_detail", PermissionAction.VIEW)
                }
            )
        }

        "finance_cash_book_detail" -> FinanceGuard("finCashBooks", PermissionAction.VIEW) {
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

        "finance_bank_accounts" -> FinanceGuard("finBankAccounts", PermissionAction.VIEW) {
            BankAccountsListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onNavigateToCreate = {
                    onSafeNavigate("finance_create_bank_account", PermissionAction.CREATE)
                },
                onNavigateToDetail = { accountId ->
                    if (accountId.isNotBlank()) {
                        onBankAccountIdSelected(accountId)
                        onSafeNavigate("finance_bank_account_detail", PermissionAction.VIEW)
                    }
                }
            )
        }

        "finance_create_bank_account" -> FinanceGuard("finBankAccounts", PermissionAction.CREATE) {
            CreateBankAccountScreen(
                viewModel = financeViewModel,
                branchViewModel = branchViewModel,
                onBack = onGoBack
            )
        }

        "finance_bank_account_detail" -> FinanceGuard("finBankAccounts", PermissionAction.VIEW) {
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
                onGoBack()
            }
        }

        "finance_bank_statements" -> FinanceGuard("finBankStatements", PermissionAction.VIEW) {
            BankStatementListScreen(
                viewModel = financeViewModel,
                onBack = onGoBack,
                onNavigateToImport = {
                    onSafeNavigate("finance_import_bank_statement", PermissionAction.VIEW)
                },
                onNavigateToDetail = { statementId ->
                    if (statementId.isNotBlank()) {
                        onStatementIdSelected(statementId)
                        onSafeNavigate("finance_bank_statement_detail", PermissionAction.VIEW)
                    }
                }
            )
        }

        "finance_bank_statement_detail" -> FinanceGuard("finBankStatements", PermissionAction.VIEW) {
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

        "finance_import_bank_statement" -> FinanceGuard("finBankStatements", PermissionAction.VIEW) {
            ImportBankStatementScreen(
                viewModel = financeViewModel,
                onBack = onGoBack
            )
        }
    }
}

/**
 * Router dedicated to Marketing Module screens.
 */
/**
 * Router dedicated to Marketing Module screens.
 */
@Composable
private fun MarketingRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    selectedCampaignCode: String? = null,
    onCampaignCodeSelected: (String?) -> Unit = {},
    marketingViewModel: MarketingViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    @Composable
    fun MarketingGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "marketing",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "marketing",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "marketing_dashboard" -> MarketingGuard("dashboard", PermissionAction.VIEW) {
            MarketingDashboardScreen(
                viewModel = marketingViewModel,
                onNavigateBack = onGoBack,
                onNavigateToFunnelDetails = { onSafeNavigate("marketing_website_overview", PermissionAction.VIEW) },
                onNavigateToChannels = { onSafeNavigate("marketing_channels", PermissionAction.VIEW) },
                onNavigateToCampaigns = { onSafeNavigate("marketing_all_campaigns", PermissionAction.VIEW) }
            )
        }

        "marketing_advertising_overview", "marketing_advertising" -> MarketingGuard("advertising", PermissionAction.VIEW) {
            AdvertisingOverviewScreen(
                viewModel = marketingViewModel,
                onNavigateBack = onGoBack
            )
        }

        "marketing_all_campaigns", "marketing_campaigns_overview", "marketing_campaigns" -> MarketingGuard("campaigns", PermissionAction.VIEW) {
            AllCampaignsListScreen(
                viewModel = marketingViewModel,
                onNavigateBack = onGoBack,
                onNavigateToDetail = { campaignCode ->
                    onCampaignCodeSelected(campaignCode)
                    onSafeNavigate("marketing_campaign_detail", PermissionAction.VIEW)
                }
            )
        }

        "marketing_campaign_detail" -> MarketingGuard("campaigns", PermissionAction.VIEW) {
            selectedCampaignCode?.takeIf { it.isNotBlank() }?.let { code ->
                CampaignDetailOverviewScreen(
                    campaignCode = code,
                    viewModel = marketingViewModel,
                    onNavigateBack = {
                        onCampaignCodeSelected(null)
                        onGoBack()
                    }
                )
            } ?: run {
                onGoBack()
            }
        }

        else -> {
            onShowComingSoon("Coming Soon, Stay tuned !")
            onGoBack()
        }
    }
}

@Composable
private fun InventoryMainRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
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
    selectedCategoryIdForEdit: String?,
    onCategoryIdForEditSelected: (String?) -> Unit,
    inventoryViewModel: InventoryViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    var selectedAdjustmentType by remember { mutableStateOf(AdjustmentType.TransferStock) }
    var selectedStockLocationItem by remember { mutableStateOf<StockLocationItemDto?>(null) }

    @Composable
    fun InventoryGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "inventory",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "inventory",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        // 1. Dashboard (catalog key: "dashboard") - Action: VIEW
        "inventory_dashboard" -> InventoryGuard("dashboard", PermissionAction.VIEW) {
            InventoryDashboardScreen(
                viewModel = inventoryViewModel
            )
        }

        // 2. All Items (catalog key: "items") - Actions: VIEW, CREATE, EDIT
        "inventory_items", "inventory_all_items" -> InventoryGuard("items", PermissionAction.VIEW) {
            InventoryScreen(
                onClose = onGoBack,
                onAddItem = {
                    onInventoryItemIdSelected(null)
                    onSafeNavigate("inventory_create_item", PermissionAction.CREATE)
                },
                onViewItem = { item ->
                    onInventoryItemIdSelected(item._id)
                    onSafeNavigate("inventory_item_detail", PermissionAction.VIEW)
                },
                onEditItem = { onSafeNavigate("inventory_create_item", PermissionAction.EDIT) },
                onBreadCrumbClick = { onOpenModulesPanel("Inventory") }
            )
        }

        "inventory_create_item" -> InventoryGuard(
            feature = "items",
            action = if (selectedInventoryItemId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
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

        "inventory_item_detail" -> InventoryGuard("items", PermissionAction.VIEW) {
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
                        onSafeNavigate("inventory_adjustment", PermissionAction.VIEW)
                    },
                    onWarehouseTransfer = { item ->
                        selectedAdjustmentType = AdjustmentType.TransferStock
                        onInventoryItemIdSelected(item._id)
                        onSafeNavigate("inventory_transfer_stock", PermissionAction.VIEW)
                    },
                    onReorderStock = {
                        onSafeNavigate("inventory_create_purchase_order", PermissionAction.CREATE)
                    },
                    onMarkInactive = { },
                    onEdit = {
                        onSafeNavigate("inventory_create_item", PermissionAction.EDIT)
                    },
                    onShare = { }
                )
            } ?: run { onGoBack() }
        }

        // 3. Item Groups (catalog key: "itemGroups") - Actions: VIEW, CREATE, EDIT
        "inventory_item_groups" -> InventoryGuard("itemGroups", PermissionAction.VIEW) {
            AllItemGroupScreen(
                onDismiss = onGoBack,
                onAddItemGroup = {
                    onItemGroupIdSelected(null)
                    onSafeNavigate("inventory_create_item_group", PermissionAction.CREATE)
                },
                onView = { groupId ->
                    onItemGroupIdSelected(groupId)
                    onSafeNavigate("inventory_item_group_detail", PermissionAction.VIEW)
                },
                onEdit = { groupId ->
                    onItemGroupIdSelected(groupId)
                    onSafeNavigate("inventory_create_item_group", PermissionAction.EDIT)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Inventory") }
            )
        }

        "inventory_item_group_detail" -> InventoryGuard("itemGroups", PermissionAction.VIEW) {
            selectedItemGroupId?.let { id ->
                ItemGroupDetailScreen(
                    itemGroupId = id,
                    onClose = {
                        onItemGroupIdSelected(null)
                        onGoBack()
                    },
                    onEdit = {
                        onSafeNavigate("inventory_create_item_group", PermissionAction.EDIT)
                    },
                    onAdjustStock = { onSafeNavigate("inventory_adjustment", PermissionAction.VIEW) },
                    onExportPdf = { }
                )
            } ?: run { onGoBack() }
        }

        "inventory_create_item_group" -> InventoryGuard(
            feature = "itemGroups",
            action = if (selectedItemGroupId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateItemGroupScreen(
                onDismiss = {
                    onItemGroupIdSelected(null)
                    onGoBack()
                },
                onSaveSuccess = {
                    onItemGroupIdSelected(null)
                    onGoBack()
                }
            )
        }

        // 4. Bulk Operations (catalog key: "bulk") - Actions: VIEW, CREATE, EDIT
        "inventory_bulk", "inventory_all_bulk" -> InventoryGuard("bulk", PermissionAction.VIEW) {
            BulkListScreen(
                onClose = onGoBack,
                onItemClick = { itemId ->
                    if (itemId.isNotBlank()) {
                        onBulkItemIdSelected(itemId)
                        onSafeNavigate("inventory_bulk_detail", PermissionAction.VIEW)
                    }
                },
                onEditClick = { itemId ->
                    if (itemId.isNotBlank()) {
                        onBulkItemIdSelected(itemId)
                        onSafeNavigate("inventory_add_bulk", PermissionAction.EDIT)
                    }
                },
                onAddBulkClick = {
                    onBulkItemIdSelected(null)
                    onSafeNavigate("inventory_add_bulk", PermissionAction.CREATE)
                }
            )
        }

        "inventory_bulk_detail" -> InventoryGuard("bulk", PermissionAction.VIEW) {
            selectedBulkItemId?.takeIf { it.isNotBlank() }?.let { itemId ->
                BulkDetailsScreen(
                    itemId = itemId,
                    onClose = onGoBack,
                    onEdit = { editId ->
                        onBulkItemIdSelected(editId)
                        onSafeNavigate("inventory_add_bulk", PermissionAction.EDIT)
                    },
                    onAdjustStock = { onSafeNavigate("inventory_adjustment", PermissionAction.VIEW) }
                )
            } ?: run { onGoBack() }
        }

        "inventory_add_bulk" -> InventoryGuard(
            feature = "bulk",
            action = if (selectedBulkItemId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
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

        // 5. Adjustments & Transfer (catalog keys: "adjustment", "transferOrders")
        "inventory_adjustment", "inventory_adjustments" -> InventoryGuard("adjustment", PermissionAction.VIEW) {
            AllOrdersStockListScreen(
                preselectedItemId = selectedInventoryItemId,
                initialAdjustmentType = selectedAdjustmentType,
                onClose = onGoBack
            )
        }

        "inventory_transfer_order", "inventory_transfer_stock", "inventory_items_transfer_stock" -> InventoryGuard("transferOrders", PermissionAction.VIEW) {
            TransferOrdersStockListScreen(
                preselectedItemId = selectedInventoryItemId,
                onClose = {
                    onInventoryItemIdSelected(null)
                    onGoBack()
                }
            )
        }

        // 6. Location Management (catalog key: "locations") - Actions: VIEW, EDIT
        "inventory_stock_location", "inventory_location_management", "inventory_procurement_location_management" -> InventoryGuard("locations", PermissionAction.VIEW) {
            AllStockLocationScreen(
                inventoryViewModel = inventoryViewModel,
                onClose = onGoBack,
                onItemClick = { item ->
                    selectedStockLocationItem = item
                    inventoryViewModel.setSelectedStockLocationItem(item)
                    onSafeNavigate("inventory_stock_location_details", PermissionAction.VIEW)
                },
                onLocationClick = { item ->
                    selectedStockLocationItem = item
                    inventoryViewModel.setSelectedStockLocationItem(item)
                    onSafeNavigate("inventory_stock_location_form", PermissionAction.EDIT)
                },
                onOptionsClick = { _ -> }
            )
        }

        "inventory_stock_location_details" -> InventoryGuard("locations", PermissionAction.VIEW) {
            StockLocationDetailsScreen(
                itemId = selectedStockLocationItem?.id,
                inventoryViewModel = inventoryViewModel,
                onClose = onGoBack
            )
        }

        "inventory_stock_location_form" -> InventoryGuard("locations", PermissionAction.EDIT) {
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

        // 7. Inventory Rules & Approvals
        "inventory_safety_stock" -> InventoryGuard("safetyStock", PermissionAction.VIEW) {
            SafetyStockScreen(
                onClose = onGoBack,
                onEditClick = { itemId ->
                    if (itemId.isNotBlank()) {
                        onInventoryItemIdSelected(itemId)
                        onSafeNavigate("inventory_create_item", PermissionAction.EDIT)
                    }
                },
                onFilterClick = { }
            )
        }

        "inventory_auto_reorder" -> InventoryGuard("autoReorder", PermissionAction.VIEW) {
            AutoReorderScreen(
                onBack = onGoBack,
                onCreateNewItem = {
                    onSafeNavigate("inventory_auto_reorder_detail", PermissionAction.CREATE)
                },
                onEditItem = { _ -> },
                onFilterClick = { }
            )
        }

        "inventory_auto_reorder_detail" -> InventoryGuard("autoReorder", PermissionAction.CREATE) {
            CreateAutoReorderRuleScreen(
                onClose = onGoBack,
                onCreateRuleSuccess = { onGoBack() }
            )
        }

        // 8. Multi-Channel Category (catalog key: "category") - Actions: VIEW, CREATE, EDIT
        "inventory_multichannel_category_listing", "inventory_category_listing" -> InventoryGuard("category", PermissionAction.VIEW) {
            CategoryManagementScreen(
                viewModel = inventoryViewModel,
                settingsViewModel = settingsViewModel,
                onClose = onGoBack,
                onAddCategory = {
                    onCategoryIdForEditSelected(null)
                    onSafeNavigate("inventory_create_category", PermissionAction.CREATE)
                },
                onEditCategory = { category ->
                    onCategoryIdForEditSelected(category.id)
                    onSafeNavigate("inventory_create_category", PermissionAction.EDIT)
                }
            )
        }

        "inventory_create_category" -> InventoryGuard(
            feature = "category",
            action = if (selectedCategoryIdForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateCategoryScreen(
                categoryId = selectedCategoryIdForEdit,
                inventoryViewModel = inventoryViewModel,
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

        // 9. Approvals (catalog key: "approvals") - Actions: VIEW, APPROVE
        "inventory_approvals" -> InventoryGuard("approvals", PermissionAction.VIEW) {
            ApprovalsListScreen(
                viewModel = inventoryViewModel,
                onBack = onGoBack,
                onItemClick = { approvalId ->
                    onApprovalIdSelected(approvalId)
                    onSafeNavigate("inventory_approval_detail", PermissionAction.APPROVE)
                },
                onFilterClick = { }
            )
        }

        "inventory_approval_detail" -> InventoryGuard("approvals", PermissionAction.APPROVE) {
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
                onGoBack()
            }
        }

        // 10. Pricing & Price Lists (catalog key: "pricing") - Actions: VIEW, CREATE, EDIT
        "inventory_pricing_list" -> InventoryGuard("pricing", PermissionAction.VIEW) {
            AllPricingScreen(
                viewModel = inventoryViewModel,
                onClose = onGoBack,
                onAddNew = {
                    onPriceListIdSelected(null)
                    inventoryViewModel.clearSelectedPriceListDetail()
                    onSafeNavigate("inventory_new_pricing", PermissionAction.CREATE)
                },
                onItemClick = { item ->
                    onPriceListIdSelected(item.id)
                    onSafeNavigate("inventory_new_pricing", PermissionAction.EDIT)
                },
                onEditItem = { item ->
                    onPriceListIdSelected(item.id)
                    onSafeNavigate("inventory_new_pricing", PermissionAction.EDIT)
                }
            )
        }

        "inventory_new_pricing" -> InventoryGuard(
            feature = "pricing",
            action = if (selectedPriceListId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            NewPriceListScreen(
                editPriceListId = selectedPriceListId,
                inventoryViewModel = inventoryViewModel,
                onClose = {
                    onPriceListIdSelected(null)
                    inventoryViewModel.clearSelectedPriceListDetail()
                    onGoBack()
                }
            )
        }

        "inventory_multichannel_basic_info" -> InventoryGuard("category", PermissionAction.VIEW) {
            BasicInfoScreen(onClose = onGoBack)
        }

        "inventory_multichannel_allocation_list" -> InventoryGuard("category", PermissionAction.VIEW) {
            AllocationListScreen(
                onClose = onGoBack,
                onAddNew = { },
                onViewItem = { _ -> },
                onEditItem = { _ -> },
                onDeleteItem = { _ -> }
            )
        }
    }
}

@Composable
private fun InventoryProcurementRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    isPurchaseFromFinance: Boolean = false,
    onPurchaseFromFinanceChange: (Boolean) -> Unit = {},
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
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    var selectedPurchaseOrderForEdit by remember { mutableStateOf<PurchaseOrder?>(null) }

    @Composable
    fun ProcGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        financeFeature: String? = null,
        content: @Composable () -> Unit
    ) {
        val activeModule = if (isPurchaseFromFinance && financeFeature != null) "finance" else "inventory"
        val activeFeature = if (isPurchaseFromFinance && financeFeature != null) financeFeature else feature
        FeatureGuard(
            module = activeModule,
            feature = activeFeature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = activeModule,
            onUpgradeRequired = onPaywallRequired,
            onGoBack = {
                onPurchaseFromFinanceChange(false)
                onGoBack()
            },
            content = content
        )
    }

    when (screen) {
        // 1. Suppliers (catalog key: "procSuppliers") - Action: VIEW
        "inventory_suppliers" -> ProcGuard("procSuppliers", PermissionAction.VIEW) {
            InventorySuppliersScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Inventory") },
                onSupplierClick = { supplier ->
                    onSupplierSelected(supplier.id)
                    onSafeNavigate("inventory_supplier_detail", PermissionAction.VIEW)
                }
            )
        }

        "inventory_supplier_detail" -> ProcGuard("procSuppliers", PermissionAction.VIEW) {
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

        // 2. Requisitions (catalog key: "procRequisitions") - Actions: VIEW, CREATE, EDIT
        "inventory_requisitions", "inventory_procurement_purchase_request", "inventory_procurement_requisitions" -> ProcGuard("procRequisitions", PermissionAction.VIEW) {
            AllRequisitionsScreen(
                onClose = onGoBack,
                onCreateRequisition = {
                    onRequisitionIdForDetailSelected(null)
                    onSafeNavigate("inventory_create_requisition", PermissionAction.CREATE)
                },
                onEditRequisition = { requisition ->
                    onRequisitionIdForDetailSelected(requisition.id)
                    onSafeNavigate("inventory_create_requisition", PermissionAction.EDIT)
                },
                onRequisitionClick = { requisition ->
                    val id = requisition.id ?: return@AllRequisitionsScreen
                    onRequisitionIdForDetailSelected(id)
                    onSafeNavigate("inventory_requisition_detail", PermissionAction.VIEW)
                }
            )
        }

        "inventory_requisition_detail" -> ProcGuard("procRequisitions", PermissionAction.VIEW) {
            RequisitionDetailScreen(
                requisitionId = selectedRequisitionIdForDetail,
                onClose = {
                    onRequisitionIdForDetailSelected(null)
                    onGoBack()
                },
                onEdit = { onSafeNavigate("inventory_create_requisition", PermissionAction.EDIT) },
                onPdfDownload = { },
                onConvertToPO = { onSafeNavigate("inventory_create_purchase_order", PermissionAction.CREATE) }
            )
        }

        "inventory_create_requisition" -> ProcGuard(
            feature = "procRequisitions",
            action = if (selectedRequisitionIdForDetail != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateRequisitionScreen(
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
        }

        // 3. Purchase Orders (catalog key: "procPurchaseOrders") - Actions: VIEW, CREATE, EDIT
        "inventory_purchase_orders", "inventory_procurement_purchase_order", "inventory_procurement_purchase_orders", "inventory_procurement_orders" -> ProcGuard("procPurchaseOrders", PermissionAction.VIEW) {
            POListScreen(
                viewModel = inventoryViewModel,
                onNavigateToCreate = {
                    onPurchaseFromFinanceChange(false)
                    selectedPurchaseOrderForEdit = null
                    onSafeNavigate("inventory_create_purchase_order_flow", PermissionAction.CREATE)
                },
                onNavigateToDetail = { po ->
                    onPurchaseFromFinanceChange(false)
                    onPurchaseOrderForDetailSelected(po)
                    onSafeNavigate("inventory_purchase_order_detail_flow", PermissionAction.VIEW)
                },
                onClose = onGoBack
            )
        }

        "inventory_purchase_order_detail_flow" -> ProcGuard(
            feature = "procPurchaseOrders",
            action = PermissionAction.VIEW,
            financeFeature = "finPurchaseOrders"
        ) {
            selectedPurchaseOrderForDetail?.let { po ->
                PODetailScreen(
                    order = po,
                    viewModel = inventoryViewModel,
                    onEdit = {
                        val activeModule = if (isPurchaseFromFinance) "finance" else "inventory"
                        val activeFeature = if (isPurchaseFromFinance) "finPurchaseOrders" else "procPurchaseOrders"

                        // 1. Check plan subscription first
                        if (!permissionViewModel.isPlanFeatureSubscribed(activeModule, activeFeature)) {
                            onPaywallRequired(permissionViewModel.getPaywallConfigForModule(activeModule))
                        } else if (permissionViewModel.canUserAction(activeModule, activeFeature, PermissionAction.EDIT)) {
                            // 2. Plan is valid and user has permission
                            selectedPurchaseOrderForEdit = po
                            onNavigate("inventory_create_purchase_order_flow")
                        } else {
                            // 3. Plan is valid, trigger access restricted flow
                            onSafeNavigate("inventory_create_purchase_order_flow", PermissionAction.EDIT)
                        }
                    },
                    onClose = {
                        onPurchaseFromFinanceChange(false)
                        onPurchaseOrderForDetailSelected(null)
                        onGoBack()
                    }
                )
            } ?: run {
                onPurchaseFromFinanceChange(false)
                onGoBack()
            }
        }

        "inventory_create_purchase_order_flow" -> ProcGuard(
            feature = "procPurchaseOrders",
            action = if (selectedPurchaseOrderForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE,
            financeFeature = "finPurchaseOrders"
        ) {
            POCreateScreen(
                viewModel = inventoryViewModel,
                existingPo = selectedPurchaseOrderForEdit,
                onDismiss = {
                    onPurchaseFromFinanceChange(false)
                    selectedPurchaseOrderForEdit = null
                    onGoBack()
                }
            )
        }

        // 4. Purchase Receives (catalog key: "procPurchaseReceives") - Actions: VIEW, CREATE
        "inventory_purchase_receive", "inventory_procurement_purchase_receive" -> ProcGuard("procPurchaseReceives", PermissionAction.VIEW) {
            AllOrdersScreen(
                onClose = onGoBack,
                onCreateOrderClick = {
                    onPurchaseFromFinanceChange(false)
                    onSafeNavigate("inventory_create_purchase_order", PermissionAction.CREATE)
                },
                onOrderClick = { poId, _ ->
                    onPurchaseFromFinanceChange(false)
                    onReceivePoIdSelected(poId)
                    onSafeNavigate("inventory_purchase_receive_detail", PermissionAction.VIEW)
                }
            )
        }

        "inventory_purchase_receive_detail" -> ProcGuard(
            feature = "procPurchaseReceives",
            action = PermissionAction.VIEW,
            financeFeature = "finPurchaseReceives"
        ) {
            ReceivePurchaseDetailScreen(
                poId = selectedReceivePoId.orEmpty(),
                onClose = {
                    onPurchaseFromFinanceChange(false)
                    onReceivePoIdSelected(null)
                    onGoBack()
                },
                onEditClick = { },
                onPreviewPdfClick = { onSafeNavigate("inventory_purchase_receive_preview_pdf", PermissionAction.VIEW) },
                onConvertToBillSuccess = { onSafeNavigate("inventory_purchase_new_bill", PermissionAction.CREATE) }
            )
        }

        "inventory_purchase_new_bill" -> ProcGuard("procPurchaseReceives", PermissionAction.CREATE) {
            NewBillScreen(
                onClose = onGoBack,
                onCancel = onGoBack,
                onSave = onGoBack
            )
        }

        "inventory_purchase_receive_preview_pdf" -> ProcGuard("procPurchaseReceives", PermissionAction.VIEW) {
            PreviewPdfScreen(
                onClose = onGoBack,
                onEditClick = { onSafeNavigate("inventory_purchase_receive_detail", PermissionAction.VIEW) },
            ) { onSafeNavigate("inventory_purchase_new_bill", PermissionAction.CREATE) }
        }

        // 5. Purchase Bills (catalog key: "procBills") - Actions: VIEW, CREATE
        "inventory_all_bills", "inventory_procurement_bill_list", "inventory_procurement_bills_list", "inventory_bills_list", "inventory_bill_list" -> ProcGuard("procBills", PermissionAction.VIEW) {
            AllBillListScreen(
                onClose = onGoBack,
                onCreateOrder = {
                    onPurchaseFromFinanceChange(false)
                    onSafeNavigate("inventory_create_purchase_order", PermissionAction.CREATE)
                },
                onInvoiceClick = { billItem ->
                    if (billItem.id.isNotBlank()) {
                        onPurchaseFromFinanceChange(false)
                        onInvoiceSelected(billItem.id)
                        onSafeNavigate("inventory_payable_purchase_detail", PermissionAction.VIEW)
                    }
                },
                onOptionsClick = { }
            )
        }

        "inventory_payable_purchase_detail" -> ProcGuard(
            feature = "procBills",
            action = PermissionAction.VIEW,
            financeFeature = "finPurchaseBills"
        ) {
            selectedInvoiceId?.let { id ->
                PayablePurchaseDetailScreen(
                    billId = id,
                    onClose = {
                        onPurchaseFromFinanceChange(false)
                        onInvoiceSelected(null)
                        onGoBack()
                    },
                    onDownloadPdf = { },
                    onRecordPayment = { onSafeNavigate("inventory_record_bill_payment", PermissionAction.CREATE) }
                )
            } ?: run {
                onPurchaseFromFinanceChange(false)
                onGoBack()
            }
        }

        "inventory_record_bill_payment" -> ProcGuard(
            feature = "procBills",
            action = PermissionAction.CREATE,
            financeFeature = "finPurchaseBills"
        ) {
            selectedInvoiceId?.let { id ->
                RecordBillPaymentScreen(
                    billId = id,
                    onClose = onGoBack,
                    onPaymentSuccess = onGoBack
                )
            } ?: run { onGoBack() }
        }

        // 6. Barcodes (catalog key: "procBarcodes") - Actions: VIEW, CREATE
        "inventory_barcode", "inventory_barcode_list", "inventory_procurement_barcode", "inventory_procurement_barcode_list", "inventory_all_barcodes" -> ProcGuard("procBarcodes", PermissionAction.VIEW) {
            AllBarcodesScreen(
                onClose = onGoBack,
                onCreateBarcode = {
                    onBarcodeIdForDetailSelected(null)
                    onSafeNavigate("inventory_create_barcode", PermissionAction.CREATE)
                },
                onBarcodeClick = { item ->
                    onBarcodeIdForDetailSelected(item.id)
                    onSafeNavigate("inventory_create_barcode", PermissionAction.VIEW)
                }
            )
        }

        "inventory_create_barcode" -> ProcGuard(
            feature = "procBarcodes",
            action = if (selectedBarcodeIdForDetail != null) PermissionAction.VIEW else PermissionAction.CREATE
        ) {
            BarcodeGeneratorScreen(
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
        }

        // 7. Low Stock Alerts (catalog key: "lowStockAlert") - Action: VIEW
        "inventory_low_stock_alerts", "inventory_low_stock_alert" -> ProcGuard("lowStockAlert", PermissionAction.VIEW) {
            LowStockAlertsScreen(
                onClose = onGoBack,
                onReorderClick = { item ->
                    onPurchaseFromFinanceChange(false)
                    onLowStockItemSelected(item)
                    if (item.warehouseId != null) {
                        inventoryViewModel.fetchLowStockItemDetail(item.itemId, item.warehouseId)
                    } else {
                        inventoryViewModel.setReorderItemDirectly(item)
                    }
                    onSafeNavigate("inventory_create_purchase_order", PermissionAction.CREATE)
                },
                onCreateNewItem = {
                    onPurchaseFromFinanceChange(false)
                    onSafeNavigate("inventory_create_purchase_order", PermissionAction.CREATE)
                },
                onBreadcrumbClick = { onOpenModulesPanel("Inventory") }
            )
        }

        "inventory_create_purchase_order" -> ProcGuard(
            feature = "procPurchaseOrders",
            action = PermissionAction.CREATE,
            financeFeature = "finPurchaseOrders"
        ) {
            CreatePurchaseOrderScreen(
                initialItem = selectedLowStockItem,
                onClose = {
                    onPurchaseFromFinanceChange(false)
                    onLowStockItemSelected(null)
                    onGoBack()
                },
                onCancel = {
                    onPurchaseFromFinanceChange(false)
                    onLowStockItemSelected(null)
                    onGoBack()
                },
                onCreateOrder = {
                    onPurchaseFromFinanceChange(false)
                    onLowStockItemSelected(null)
                    onGoBack()
                }
            )
        }

        // 8. Payments Made (catalog key: "procPaymentsMade") - Action: VIEW
        "inventory_payables_payments" -> ProcGuard("procPaymentsMade", PermissionAction.VIEW) {
            AllPaymentListScreen(
                onClose = onGoBack,
                onPaymentClick = { paymentId ->
                    onPaymentModeSelected(paymentId)
                    onSafeNavigate("payment_mode_detail", PermissionAction.VIEW)
                }
            )
        }

        "inventory_payments_made" -> ProcGuard("procPaymentsMade", PermissionAction.VIEW) {
            AllInventoryPaymentScreen(
                onClose = onGoBack,
                onPaymentSelect = { paymentId ->
                    if (paymentId.isNotBlank()) {
                        onPurchaseFromFinanceChange(false)
                        onPaymentModeSelected(paymentId)
                        onSafeNavigate("inventory_payment_overview_detail", PermissionAction.VIEW)
                    }
                },
                onFilterClick = { }
            )
        }

        "inventory_payment_overview_detail" -> ProcGuard(
            feature = "procPaymentsMade",
            action = PermissionAction.VIEW,
            financeFeature = "finPaymentsMade"
        ) {
            PaymentOverviewDetailScreen(
                paymentId = selectedPaymentModeId.orEmpty(),
                viewModel = inventoryViewModel,
                onClose = {
                    onPurchaseFromFinanceChange(false)
                    onPaymentModeSelected(null)
                    onGoBack()
                },
                onEditClick = { },
                onPreviewPdfClick = { },
                onSendEmailClick = { }
            )
        }

        // 9. Vendor Credits & Purchase Returns (catalog keys: "procVendorCredits", "procPurchaseReturn")
        "inventory_credits", "inventory_procurement_credits", "inventory_payables_credits" -> ProcGuard("procVendorCredits", PermissionAction.VIEW) {
            AllCreditsScreen(
                onClose = onGoBack,
                onCreditClick = { },
                onOptionsClick = { }
            )
        }

        "inventory_returns", "inventory_procurement_returns", "inventory_procurement_return", "inventory_return" -> ProcGuard("procPurchaseReturn", PermissionAction.VIEW) {
            AllReturnsScreen(
                onClose = onGoBack,
                onCreateReturnClick = { },
                onReturnItemClick = { _ -> },
                onEditRma = { },
                onPdfExport = { }
            )
        }

        // 10. Picklist & Packages (catalog keys: "picklist", "packages") - Actions: VIEW, CREATE, EDIT
        "inventory_picklist" -> ProcGuard("picklist", PermissionAction.VIEW) {
            PicklistListScreen(
                viewModel = inventoryViewModel,
                onClose = onGoBack,
                onCreatePicklistClick = {
                    onPicklistIdSelected(null)
                    onSafeNavigate("inventory_create_picklist", PermissionAction.CREATE)
                },
                onEditPicklistClick = { editId ->
                    onPicklistIdSelected(editId)
                    onSafeNavigate("inventory_create_picklist", PermissionAction.EDIT)
                },
                onPicklistItemClick = { item ->
                    onPicklistIdSelected(item.id)
                    onSafeNavigate("inventory_picklist_detail", PermissionAction.VIEW)
                }
            )
        }

        "inventory_picklist_detail" -> ProcGuard("picklist", PermissionAction.VIEW) {
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
                        onSafeNavigate("inventory_create_picklist", PermissionAction.EDIT)
                    }
                )
            } ?: run { onGoBack() }
        }

        "inventory_create_picklist" -> ProcGuard(
            feature = "picklist",
            action = if (selectedPicklistId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreatePicklistScreen(
                picklistId = selectedPicklistId,
                viewModel = inventoryViewModel,
                onClose = { onGoBack() },
                onCancel = { onGoBack() },
                onSuccessSaved = { onGoBack() }
            )
        }

        "inventory_packages" -> ProcGuard("packages", PermissionAction.VIEW) {
            PackagesScreen(
                viewModel = inventoryViewModel,
                onNavigateBack = onGoBack,
                onCreatePackageClick = {
                    onPackageIdForEditSelected(null)
                    onPackageIdForDetailSelected(null)
                    onSafeNavigate("inventory_create_package", PermissionAction.CREATE)
                },
                onPackageItemClick = { pkg ->
                    onPackageIdForDetailSelected(pkg.id)
                    onSafeNavigate("inventory_package_detail", PermissionAction.VIEW)
                },
                onEditPackageClick = { pkg ->
                    onPackageIdForDetailSelected(null)
                    onPackageIdForEditSelected(pkg.id)
                    onSafeNavigate("inventory_create_package", PermissionAction.EDIT)
                }
            )
        }

        "inventory_create_package" -> ProcGuard(
            feature = "packages",
            action = if (selectedPackageIdForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
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

        "inventory_package_detail" -> ProcGuard("packages", PermissionAction.VIEW) {
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
                        onSafeNavigate("inventory_create_package", PermissionAction.EDIT)
                    }
                )
            } ?: run { onGoBack() }
        }

        "inventory_billing" -> ProcGuard("procBills", PermissionAction.VIEW) {
            AllBillsScreen(
                onClose = onGoBack,
                onCreateBillClick = { onSafeNavigate("inventory_create_bill", PermissionAction.CREATE) },
                onBillDetailsClick = { _ -> onSafeNavigate("inventory_bill_preview", PermissionAction.VIEW) },
                onFilterClick = { }
            )
        }

        "inventory_bill_preview" -> ProcGuard("procBills", PermissionAction.VIEW) {
            BillPdfPreviewScreen(
                onClose = onGoBack,
                onEditClick = { onSafeNavigate("inventory_create_bill", PermissionAction.EDIT) },
                onConvertToBillClick = { onSafeNavigate("inventory_create_bill", PermissionAction.CREATE) }
            )
        }

        "inventory_create_bill" -> ProcGuard("procBills", PermissionAction.CREATE) {
            NewVendorInvoiceScreen(
                onBack = onGoBack,
                onCancel = onGoBack,
                onSaveDraft = onGoBack,
                onSaveAndSend = onGoBack
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HrRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    settingsViewModel: SettingsViewModel,
    selectedPieceRateIdForEdit: String? = null,
    onPieceRateIdForEditSelected: (String?) -> Unit = {},
    selectedExitRequestId: String? = null,
    onExitRequestIdSelected: (String?) -> Unit = {},
    selectedProductionTeamId: String? = null,
    onProductionTeamIdSelected: (String?) -> Unit = {},
    selectedLeavePolicyIdForEdit: String? = null,
    onLeavePolicyIdForEditSelected: (String?) -> Unit = {},
    selectedLeavePolicyIdForView: String? = null,
    onLeavePolicyIdForViewSelected: (String?) -> Unit = {},
    selectedClaimForPayment: ExpenseClaimDto? = null,
    selectedLeaveTypeIdForEdit: String? = null,
    selectedLeaveTypeIdForView: String? = null,
    onLeaveTypeIdForViewSelected: (String?) -> Unit = {},
    onLeaveTypeIdForEditSelected: (String?) -> Unit = {},
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
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    @Composable
    fun HrGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "hr",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "hr",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        // 1. HR Dashboard
        "hr_dashboard" -> HrGuard("hrDashboard", PermissionAction.VIEW) {
            HrDashboardScreen(
                onClose = onGoBack,
                onNavigateToLeave = { onSafeNavigate("hr_apply_leave", PermissionAction.VIEW) },
                onNavigateToAttendance = { onSafeNavigate("hr_daily_attendance", PermissionAction.VIEW) },
                onNavigateToShift = { onSafeNavigate("hr_attendance_shift_management", PermissionAction.VIEW) },
                onNavigateToCreateFeed = { onSafeNavigate("hr_create_feed", PermissionAction.VIEW) }
            )
        }

        "hr_create_feed" -> HrGuard("hrDashboard", PermissionAction.VIEW) {
            CreateFeedScreen(
                onClose = onGoBack,
                onSubmit = { _, _, _, _, _, _, _, _, _, _, _ -> onGoBack() }
            )
        }

        // 2. Employee Management
        "hr_all_employees", "hr_employee_management" -> HrGuard("employeeManagement", PermissionAction.VIEW) {
            AllEmployeesScreen(
                onDismiss = onGoBack,
                onAddEmployee = {
                    onEmployeeScreenModeChange(ScreenMode.CREATE)
                    onEmployeeIdSelected(null)
                    onSafeNavigate("hr_employee_onboarding", PermissionAction.CREATE)
                },
                onView = { employee ->
                    onEmployeeScreenModeChange(ScreenMode.VIEW)
                    onEmployeeIdSelected(employee._id)
                    onSafeNavigate("hr_employee_onboarding", PermissionAction.VIEW)
                },
                onEdit = { employee ->
                    onEmployeeScreenModeChange(ScreenMode.EDIT)
                    onEmployeeIdSelected(employee._id)
                    onSafeNavigate("hr_employee_onboarding", PermissionAction.EDIT)
                },
                onDelete = { },
                hrViewModel = hrViewModel,
                onBreadCrumbClick = { onOpenModulesPanel("HR") }
            )
        }

        "hr_employee_onboarding" -> HrGuard(
            feature = "employeeManagement",
            action = when (employeeScreenMode) {
                ScreenMode.CREATE -> PermissionAction.CREATE
                ScreenMode.EDIT -> PermissionAction.EDIT
                ScreenMode.VIEW -> PermissionAction.VIEW
            }
        ) {
            EmployeeOnboardingScreen(
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
        }

        // 3. Organization Structure & Production Teams
        "hr_organization_structure" -> HrGuard("allEmployees", PermissionAction.VIEW) {
            OrganizationStructureScreen(
                hrViewModel = hrViewModel,
                onClose = onGoBack
            )
        }

        "hr_production_team" -> HrGuard("productionTeam", PermissionAction.VIEW) {
            ProductionTeamsScreen(
                hrViewModel = hrViewModel,
                onClose = onGoBack,
                onCreateTeam = {
                    onProductionTeamIdSelected(null)
                    onSafeNavigate("hr_create_production_team", PermissionAction.CREATE)
                },
                onViewTeam = { teamId ->
                    onProductionTeamIdSelected(teamId)
                    onSafeNavigate("hr_production_team_detail", PermissionAction.VIEW)
                },
                onEditTeam = { teamId ->
                    onProductionTeamIdSelected(teamId)
                    onSafeNavigate("hr_create_production_team", PermissionAction.EDIT)
                }
            )
        }

        "hr_create_production_team" -> HrGuard(
            feature = "productionTeam",
            action = if (selectedProductionTeamId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateProductionTeamScreen(
                teamIdToEdit = selectedProductionTeamId,
                hrViewModel = hrViewModel,
                onClose = {
                    onProductionTeamIdSelected(null)
                    onGoBack()
                },
                onSavedSuccess = {
                    onProductionTeamIdSelected(null)
                    onGoBack()
                }
            )
        }

        "hr_production_team_detail" -> HrGuard("productionTeam", PermissionAction.VIEW) {
            selectedProductionTeamId?.takeIf { it.isNotBlank() }?.let { validId ->
                ProductionTeamDetailScreen(
                    teamId = validId,
                    hrViewModel = hrViewModel,
                    onClose = {
                        onProductionTeamIdSelected(null)
                        onGoBack()
                    },
                    onEditTeam = { teamId ->
                        onProductionTeamIdSelected(teamId)
                        onSafeNavigate("hr_create_production_team", PermissionAction.EDIT)
                    }
                )
            } ?: run { onGoBack() }
        }

        // 4. Attendance Management
        "hr_daily_attendance", "hr_attendance" -> HrGuard("dailyAttendance", PermissionAction.VIEW) {
            AttendanceListScreen(
                onClose = onGoBack,
                onManualEntryClick = { onSafeNavigate("hr_attendance_manual_entry", PermissionAction.CREATE) },
                viewModel = hrViewModel
            )
        }

        "hr_attendance_manual_entry" -> HrGuard("manualEntry", PermissionAction.CREATE) {
            ManualAttendanceEntryScreen(
                onClose = onGoBack,
                onSubmit = { onGoBack() },
                viewModel = hrViewModel
            )
        }

        "hr_attendance_shift_management", "hr_shift_list" -> HrGuard("shiftManagement", PermissionAction.VIEW) {
            ShiftListOverviewScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onAddShiftClick = {
                    onShiftIdSelected(null)
                    onSafeNavigate("hr_create_shift", PermissionAction.CREATE)
                },
                onEditShift = { shift ->
                    onShiftIdSelected(shift.id)
                    onSafeNavigate("hr_create_shift", PermissionAction.EDIT)
                }
            )
        }

        "hr_create_shift" -> HrGuard(
            feature = "shiftManagement",
            action = if (selectedShiftId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateNewListScreen(
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
        }

        "hr_monthly_attendance" -> HrGuard("monthlyAttendance", PermissionAction.VIEW) {
            AttendanceCalendarScreen(onClose = onGoBack)
        }

        "hr_attendance_detail" -> HrGuard("dailyAttendance", PermissionAction.VIEW) {
            AttendanceDetailScreen(
                onClose = {
                    onAttendanceIdSelected(null)
                    onGoBack()
                },
                onBreadCrumbClick = { onOpenModulesPanel("HR") },
                onHistoryClick = { }
            )
        }

        // 5. Leave Management
        "hr_leave_approval" -> HrGuard("leaveApproval", PermissionAction.VIEW) {
            LeaveApprovalScreen(onClose = onGoBack, viewModel = hrViewModel)
        }

        "hr_apply_leave" -> HrGuard("leaveDashboard", PermissionAction.VIEW) {
            ApplyLeaveScreen(onClose = onGoBack, viewModel = hrViewModel)
        }

        // 6. Payroll Management
        "hr_payroll_salary_structure", "hr_payroll_salary_components" -> HrGuard("salaryStructureMaster", PermissionAction.VIEW) {
            SalaryComponentListScreen(
                onClose = onGoBack,
                viewModel = hrViewModel,
                onCreateComponentClick = {
                    onSalaryComponentIdSelected(null)
                    onSafeNavigate("hr_payroll_salary_components_create", PermissionAction.CREATE)
                },
                onEditComponentClick = { componentId ->
                    onSalaryComponentIdSelected(componentId)
                    onSafeNavigate("hr_payroll_salary_components_create", PermissionAction.EDIT)
                },
                onViewComponentClick = { componentId ->
                    onSalaryComponentIdSelected(componentId)
                    onSafeNavigate("hr_payroll_salary_components_create", PermissionAction.VIEW)
                }
            )
        }

        "hr_payroll_salary_components_create" -> HrGuard(
            feature = "salaryStructureMaster",
            action = if (selectedSalaryComponentId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            SalaryComponentFormScreen(
                componentId = selectedSalaryComponentId,
                onClose = {
                    onSalaryComponentIdSelected(null)
                    onGoBack()
                },
                viewModel = hrViewModel
            )
        }

        "hr_payroll_salary_templates" -> HrGuard("salaryStructureMaster", PermissionAction.VIEW) {
            SalaryTemplateListScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onCreateTemplateClick = {
                    onSalaryTemplateIdSelected(null)
                    onSafeNavigate("hr_payroll_salary_templates_form", PermissionAction.CREATE)
                },
                onEditTemplateClick = { templateId ->
                    onSalaryTemplateIdSelected(templateId)
                    onSafeNavigate("hr_payroll_salary_templates_form", PermissionAction.EDIT)
                },
                onViewTemplateClick = { templateId ->
                    onSalaryTemplateIdSelected(templateId)
                    onSafeNavigate("hr_payroll_salary_templates_form", PermissionAction.VIEW)
                }
            )
        }

        "hr_payroll_salary_templates_form" -> HrGuard(
            feature = "salaryStructureMaster",
            action = if (selectedSalaryTemplateId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            SalaryTemplateFormScreen(
                templateId = selectedSalaryTemplateId,
                viewModel = hrViewModel,
                onClose = {
                    onSalaryTemplateIdSelected(null)
                    onGoBack()
                }
            )
        }

        // 7. Training Management
        "hr_training_management" -> HrGuard("trainingProgram", PermissionAction.VIEW) {
            TrainingProgramListScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onAddProgramClick = {
                    onTrainingProgramIdSelected(null)
                    onSafeNavigate("hr_add_edit_program", PermissionAction.CREATE)
                },
                onEditProgramClick = { programId ->
                    onTrainingProgramIdSelected(programId)
                    onSafeNavigate("hr_add_edit_program", PermissionAction.EDIT)
                }
            )
        }

        "hr_add_edit_program" -> HrGuard(
            feature = "trainingProgram",
            action = if (selectedTrainingProgramId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddEditProgramScreen(
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
        }

        "hr_training_assignment_list" -> HrGuard("assignmentManagement", PermissionAction.VIEW) {
            TrainingAssignmentListScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onAssignTrainingClick = {
                    onTrainingAssignmentIdSelected(null)
                    onSafeNavigate("hr_assign_training_form", PermissionAction.CREATE)
                },
                onEditAssignmentClick = { id ->
                    onTrainingAssignmentIdSelected(id)
                    onSafeNavigate("hr_assign_training_form", PermissionAction.EDIT)
                }
            )
        }

        "hr_assign_training_form" -> HrGuard(
            feature = "assignmentManagement",
            action = if (selectedTrainingAssignmentId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AssignTrainingScreen(
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
        }

        // 8. Document Management
        "hr_employee_document_upload" -> HrGuard("employeeDocuments", PermissionAction.VIEW) {
            EmployeeDocumentListScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onNavigateToUpload = {
                    onDocumentIdSelected(null)
                    onSafeNavigate("hr_new_document_upload", PermissionAction.CREATE)
                },
                onEditDocument = { docId ->
                    onDocumentIdSelected(docId)
                    onSafeNavigate("hr_new_document_upload", PermissionAction.EDIT)
                }
            )
        }

        "hr_new_document_upload" -> HrGuard(
            feature = "employeeDocuments",
            action = if (selectedDocumentId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            EmployeeDocumentUploadScreen(
                documentId = selectedDocumentId,
                viewModel = hrViewModel,
                onClose = {
                    onDocumentIdSelected(null)
                    onGoBack()
                }
            )
        }

        "hr_document_categories" -> HrGuard("documentCategories", PermissionAction.VIEW) {
            DocumentCategoryListScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onNavigateToAddCategory = {
                    onDocumentCategoryIdSelected(null)
                    onSafeNavigate("hr_add_document_category", PermissionAction.CREATE)
                },
                onEditCategory = { categoryId ->
                    onDocumentCategoryIdSelected(categoryId)
                    onSafeNavigate("hr_add_document_category", PermissionAction.EDIT)
                }
            )
        }

        "hr_add_document_category" -> HrGuard(
            feature = "documentCategories",
            action = if (selectedDocumentCategoryId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddDocumentCategoryScreen(
                categoryId = selectedDocumentCategoryId,
                viewModel = hrViewModel,
                onClose = {
                    onDocumentCategoryIdSelected(null)
                    onGoBack()
                }
            )
        }

        // 9. Expense Management
        "hr_expense_claims" -> HrGuard("approvals", PermissionAction.VIEW) {
            ExpenseManagementScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onViewClaimDetail = { claimId ->
                    onExpenseClaimIdSelected(claimId)
                    onSafeNavigate("hr_expense_claim_detail", PermissionAction.VIEW)
                }
            )
        }

        "hr_expense_claim_detail_readonly" -> HrGuard("newClaims", PermissionAction.VIEW) {
            selectedExpenseClaimId?.takeIf { it.isNotBlank() }?.let { id ->
                ExpenseClaimDetailScreen(
                    claimId = id,
                    title = "Expense Details",
                    showApprovalActions = false,
                    viewModel = hrViewModel,
                    onClose = {
                        onExpenseClaimIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "hr_expense_claim_detail" -> HrGuard("approvals", PermissionAction.VIEW) {
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

        "hr_expense_type" -> HrGuard("expenseType", PermissionAction.VIEW) {
            ExpenseTypeScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onExpenseTypeClick = { type ->
                    onExpenseClaimIdSelected(type.id)
                    onSafeNavigate("hr_expense_claim_detail_readonly", PermissionAction.VIEW)
                },
                onAddNewExpenseType = {
                    onExpenseTypeIdSelected(null)
                    onSafeNavigate("hr_add_expense_type", PermissionAction.CREATE)
                },
                onEditExpenseType = { id ->
                    onExpenseTypeIdSelected(id)
                    onSafeNavigate("hr_add_expense_type", PermissionAction.EDIT)
                }
            )
        }

        "hr_add_expense_type" -> HrGuard(
            feature = "expenseType",
            action = if (selectedExpenseTypeId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddExpenseTypeScreen(
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
        }

        "hr_expense_finance_reimbursement" -> HrGuard("financeReimbursement", PermissionAction.VIEW) {
            FinanceReimbursementScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onViewDetail = { claim ->
                    onExpenseClaimIdSelected(claim.id)
                    onSafeNavigate("hr_expense_claim_detail_readonly", PermissionAction.VIEW)
                },
                onProcessPayClick = { claim ->
                    onClaimForPaymentSelected(claim)
                    onSafeNavigate("hr_expense_process_payment", PermissionAction.CREATE)
                }
            )
        }

        "hr_expense_process_payment" -> HrGuard("financeReimbursement", PermissionAction.CREATE) {
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

        "hr_expense_new_claim" -> HrGuard("newClaims", PermissionAction.VIEW) {
            NewClaimsScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onClaimClick = { claim ->
                    onExpenseClaimIdSelected(claim.id)
                    onSafeNavigate("hr_expense_claim_detail_readonly", PermissionAction.VIEW)
                },
                onEditClaimClick = { claim ->
                    onExpenseClaimIdSelected(claim.id)
                    onSafeNavigate("hr_edit_expense_claim", PermissionAction.EDIT)
                },
                onNavigateToSubmitClaim = {
                    onExpenseClaimIdSelected(null)
                    onSafeNavigate("hr_submit_expense_claim", PermissionAction.CREATE)
                }
            )
        }

        "hr_submit_expense_claim" -> HrGuard("newClaims", PermissionAction.CREATE) {
            SubmitExpenseClaimScreen(
                hrViewModel = hrViewModel,
                onClose = onGoBack,
                onSubmitSuccess = {
                    hrViewModel.fetchExpenseClaims(page = 1, limit = 10, status = null, search = null)
                    onGoBack()
                }
            )
        }

        "hr_edit_expense_claim" -> HrGuard("newClaims", PermissionAction.EDIT) {
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

        // 10. HR Settings (Leave Types, Leave Policies, Piece Rate)
        "hr_leave_types" -> HrGuard("leaveTypes", PermissionAction.VIEW) {
            LeaveTypesListScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onCreateNew = {
                    onLeaveTypeIdForEditSelected(null)
                    onSafeNavigate("hr_create_leave_type", PermissionAction.CREATE)
                },
                onViewLeaveType = { leaveTypeId ->
                    onLeaveTypeIdForViewSelected(leaveTypeId)
                    onSafeNavigate("hr_view_leave_type", PermissionAction.VIEW)
                },
                onEditLeaveType = { leaveTypeId ->
                    onLeaveTypeIdForEditSelected(leaveTypeId)
                    onSafeNavigate("hr_create_leave_type", PermissionAction.EDIT)
                }
            )
        }

        "hr_view_leave_type" -> HrGuard("leaveTypes", PermissionAction.VIEW) {
            LeaveTypeViewScreen(
                leaveTypeId = selectedLeaveTypeIdForView.orEmpty(),
                viewModel = settingsViewModel,
                onClose = {
                    onLeaveTypeIdForViewSelected(null)
                    onGoBack()
                },
                onEdit = { leaveTypeId ->
                    onLeaveTypeIdForViewSelected(null)
                    onLeaveTypeIdForEditSelected(leaveTypeId)
                    onSafeNavigate("hr_create_leave_type", PermissionAction.EDIT)
                }
            )
        }

        "hr_create_leave_type" -> HrGuard(
            feature = "leaveTypes",
            action = if (selectedLeaveTypeIdForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateLeaveTypeScreen(
                leaveTypeIdToEdit = selectedLeaveTypeIdForEdit,
                viewModel = settingsViewModel,
                onClose = {
                    onLeaveTypeIdForEditSelected(null)
                    onGoBack()
                },
                onSavedSuccess = {
                    onLeaveTypeIdForEditSelected(null)
                    settingsViewModel.fetchLeaveTypes(page = 1)
                    onGoBack()
                }
            )
        }

        "hr_leave_policies" -> HrGuard("leavePolicies", PermissionAction.VIEW) {
            LeavePoliciesScreen(
                viewModel = settingsViewModel,
                onBack = onGoBack,
                onCreatePolicy = {
                    onLeavePolicyIdForEditSelected(null)
                    onSafeNavigate("hr_create_leave_policy", PermissionAction.CREATE)
                },
                onViewPolicy = { policyId ->
                    onLeavePolicyIdForViewSelected(policyId)
                    onSafeNavigate("hr_view_leave_policy", PermissionAction.VIEW)
                },
                onEditPolicy = { policyId ->
                    onLeavePolicyIdForEditSelected(policyId)
                    onSafeNavigate("hr_create_leave_policy", PermissionAction.EDIT)
                }
            )
        }

        "hr_create_leave_policy" -> HrGuard(
            feature = "leavePolicies",
            action = if (selectedLeavePolicyIdForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateLeavePolicyScreen(
                viewModel = settingsViewModel,
                editPolicyId = selectedLeavePolicyIdForEdit,
                onBack = {
                    onLeavePolicyIdForEditSelected(null)
                    onGoBack()
                },
                onSavedSuccess = {
                    onLeavePolicyIdForEditSelected(null)
                    settingsViewModel.fetchLeavePolicies(page = 1)
                    onGoBack()
                }
            )
        }

        "hr_view_leave_policy" -> HrGuard("leavePolicies", PermissionAction.VIEW) {
            selectedLeavePolicyIdForView?.takeIf { it.isNotBlank() }?.let { validId ->
                ViewLeavePolicyScreen(
                    policyId = validId,
                    viewModel = settingsViewModel,
                    onBack = {
                        onLeavePolicyIdForViewSelected(null)
                        onGoBack()
                    },
                    onEditPolicy = { policyId ->
                        onLeavePolicyIdForViewSelected(null)
                        onLeavePolicyIdForEditSelected(policyId)
                        onSafeNavigate("hr_create_leave_policy", PermissionAction.EDIT)
                    }
                )
            } ?: run { onGoBack() }
        }

        "hr_piece_rate_setup" -> HrGuard("pieceRate", PermissionAction.VIEW) {
            PieceRateSetupListScreen(
                viewModel = settingsViewModel,
                onBack = onGoBack,
                onAddRate = {
                    onPieceRateIdForEditSelected(null)
                    onSafeNavigate("hr_add_edit_piece_rate", PermissionAction.CREATE)
                },
                onEditRate = { rateId ->
                    onPieceRateIdForEditSelected(rateId)
                    onSafeNavigate("hr_add_edit_piece_rate", PermissionAction.EDIT)
                }
            )
        }

        "hr_add_edit_piece_rate" -> HrGuard(
            feature = "pieceRate",
            action = if (selectedPieceRateIdForEdit != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddEditPieceRateScreen(
                rateId = selectedPieceRateIdForEdit,
                viewModel = settingsViewModel,
                onClose = {
                    onPieceRateIdForEditSelected(null)
                    onGoBack()
                },
                onSavedSuccess = {
                    onPieceRateIdForEditSelected(null)
                    settingsViewModel.fetchPieceRates(page = 1)
                    onGoBack()
                }
            )
        }

        // 11. Exit Management
        "hr_exit_management" -> HrGuard("allRequests", PermissionAction.VIEW) {
            ExitManagementScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onCreateNewRequest = { onSafeNavigate("hr_exit_request_form", PermissionAction.CREATE) },
                onOpenApproval = { exitRequestId ->
                    onExitRequestIdSelected(exitRequestId)
                    onSafeNavigate("hr_exit_manager_approval", PermissionAction.VIEW)
                }
            )
        }

        "hr_exit_request_form" -> HrGuard("allRequests", PermissionAction.CREATE) {
            ExitRequestFormScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onSubmitSuccess = {
                    hrViewModel.fetchExitRequests()
                    onGoBack()
                }
            )
        }

        "hr_exit_manager_approval" -> HrGuard("allRequests", PermissionAction.VIEW) {
            selectedExitRequestId?.takeIf { it.isNotBlank() }?.let { validId ->
                ManagerApprovalScreen(
                    exitRequestId = validId,
                    viewModel = hrViewModel,
                    onClose = {
                        onExitRequestIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "hr_exit_clearance" -> HrGuard("clearance", PermissionAction.VIEW) {
            ExitClearanceListScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onOpenClearanceDetail = { exitRequestId ->
                    onExitRequestIdSelected(exitRequestId)
                    onSafeNavigate("hr_exit_clearance_detail", PermissionAction.VIEW)
                },
                onOpenManagerApproval = { exitRequestId ->
                    onExitRequestIdSelected(exitRequestId)
                    onSafeNavigate("hr_exit_manager_approval", PermissionAction.VIEW)
                }
            )
        }

        "hr_exit_clearance_detail" -> HrGuard("clearance", PermissionAction.VIEW) {
            selectedExitRequestId?.takeIf { it.isNotBlank() }?.let { validId ->
                ExitClearanceDetailScreen(
                    exitRequestId = validId,
                    viewModel = hrViewModel,
                    onClose = {
                        onExitRequestIdSelected(null)
                        onGoBack()
                    },
                    onOpenManagerApproval = { exitRequestId ->
                        onExitRequestIdSelected(exitRequestId)
                        onSafeNavigate("hr_exit_manager_approval", PermissionAction.VIEW)
                    }
                )
            } ?: run { onGoBack() }
        }

        "hr_exit_settlements" -> HrGuard("settlements", PermissionAction.VIEW) {
            ExitSettlementListScreen(
                viewModel = hrViewModel,
                onClose = onGoBack,
                onProcessSettlement = { exitRequestId ->
                    onExitRequestIdSelected(exitRequestId)
                    onSafeNavigate("hr_exit_settlement_detail", PermissionAction.VIEW)
                }
            )
        }

        "hr_exit_settlement_detail" -> HrGuard("settlements", PermissionAction.VIEW) {
            selectedExitRequestId?.takeIf { it.isNotBlank() }?.let { validId ->
                ExitSettlementDetailScreen(
                    exitRequestId = validId,
                    viewModel = hrViewModel,
                    onClose = {
                        onExitRequestIdSelected(null)
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
    permissionViewModel: PermissionViewModel,
    selectedReturnId: String? = null,
    onReturnIdSelected: (String?) -> Unit = {},
    selectedLogisticsPackageId: String? = null,
    onLogisticsPackageIdSelected: (String?) -> Unit = {},
    selectedShipmentId: String? = null,
    onShipmentIdSelected: (String?) -> Unit = {},
    onOrderIdSelected: (String?) -> Unit,
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    val logisticsViewModel: LogisticsViewModel = hiltViewModel()

    @Composable
    fun LogisticsGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "logistics",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "logistics",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "logistics_dashboard" -> LogisticsGuard("dashboard", PermissionAction.VIEW) {
            LogisticsDashboardScreen(
                onNavigateBack = onGoBack,
                onViewAllShipments = { onSafeNavigate("logistics_order_tracking", PermissionAction.VIEW) },
                onViewAllCodOrders = { onSafeNavigate("logistics_cod_screen", PermissionAction.VIEW) }
            )
        }

        "logistics_orders" -> LogisticsGuard("orderList", PermissionAction.VIEW) {
            OrderListScreen(
                viewModel = logisticsViewModel,
                onClose = onGoBack,
                onViewOrderDetails = { packageItem ->
                    val targetId = packageItem._id.ifBlank {
                        packageItem.packageId ?: packageItem.orderId ?: ""
                    }
                    if (targetId.isNotBlank()) {
                        onLogisticsPackageIdSelected(targetId)
                        onSafeNavigate("logistics_order_detail", PermissionAction.VIEW)
                    }
                }
            )
        }

        "logistics_order_detail" -> LogisticsGuard("orderList", PermissionAction.VIEW) {
            selectedLogisticsPackageId?.takeIf { it.isNotBlank() }?.let { id ->
                LogisticsOrderDetailScreen(
                    orderId = id,
                    viewModel = logisticsViewModel,
                    onClose = {
                        onLogisticsPackageIdSelected(null)
                        onGoBack()
                    },
                    onCancelOrderClick = {
                        onLogisticsPackageIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "logistics_dispatched" -> LogisticsGuard("dispatched", PermissionAction.VIEW) {
            DispatchListScreen(viewModel = logisticsViewModel, onClose = onGoBack)
        }

        "logistics_delivery" -> LogisticsGuard("delivery", PermissionAction.VIEW) {
            DeliveryListScreen(
                viewModel = logisticsViewModel,
                onNavigateBack = onGoBack,
                onViewDeliveryDetail = { deliveryId ->
                    if (deliveryId.isNotBlank()) {
                        onLogisticsPackageIdSelected(deliveryId)
                        onSafeNavigate("delivery_detail", PermissionAction.VIEW)
                    }
                }
            )
        }

        "delivery_detail" -> LogisticsGuard("delivery", PermissionAction.VIEW) {
            selectedLogisticsPackageId?.takeIf { it.isNotBlank() }?.let { id ->
                DeliveryDetailScreen(
                    deliveryId = id,
                    viewModel = logisticsViewModel,
                    onBack = {
                        onLogisticsPackageIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "logistics_shipments" -> LogisticsGuard("shipments", PermissionAction.VIEW) {
            ShipmentListScreen(
                viewModel = logisticsViewModel,
                onNavigateBack = onGoBack,
                onNavigateToDetails = { shipmentId ->
                    val cleanId = shipmentId.trim()
                    if (cleanId.isNotBlank()) {
                        onShipmentIdSelected(cleanId)
                        onSafeNavigate("shipment_detail", PermissionAction.VIEW)
                    }
                },
                onCreateShipmentClick = {
                    onSafeNavigate("create_shipment", PermissionAction.CREATE)
                }
            )
        }

        "create_shipment" -> LogisticsGuard("shipments", PermissionAction.CREATE) {
            CreateShipmentScreen(
                logisticsViewModel = logisticsViewModel,
                onClose = onGoBack,
                onSuccessCreated = onGoBack
            )
        }

        "shipment_detail" -> LogisticsGuard("shipments", PermissionAction.VIEW) {
            selectedShipmentId?.takeIf { it.isNotBlank() }?.let { validId ->
                ShipmentDetailScreen(
                    shipmentId = validId,
                    viewModel = logisticsViewModel,
                    onBack = {
                        onShipmentIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "logistics_courier_management" -> LogisticsGuard("courierManagement", PermissionAction.VIEW) {
            AllCourierScreen(
                viewModel = logisticsViewModel,
                onBack = onGoBack,
                onViewDeliveryDetail = { deliveryId ->
                    val cleanId = deliveryId.trim()
                    if (cleanId.isNotBlank()) {
                        onLogisticsPackageIdSelected(cleanId)
                        onSafeNavigate("courier_overview", PermissionAction.VIEW)
                    }
                }
            )
        }

        "courier_overview" -> LogisticsGuard("courierManagement", PermissionAction.VIEW) {
            selectedLogisticsPackageId?.takeIf { it.isNotBlank() }?.let { validId ->
                CourierOverviewScreen(
                    deliveryId = validId,
                    viewModel = logisticsViewModel,
                    onBack = {
                        onLogisticsPackageIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "logistics_courier_partner_list" -> LogisticsGuard("courierPartners", PermissionAction.VIEW) {
            CourierPartnerListScreen(
                viewModel = logisticsViewModel,
                onBack = onGoBack,
                onNewShipmentClick = {
                    onLogisticsPackageIdSelected(null)
                    onSafeNavigate("add_courier_partner", PermissionAction.CREATE)
                },
                onViewCourierDetail = { courierId ->
                    val cleanId = courierId.trim()
                    if (cleanId.isNotBlank()) {
                        onLogisticsPackageIdSelected(cleanId)
                        onSafeNavigate("courier_partner_overview", PermissionAction.VIEW)
                    }
                }
            )
        }

        "courier_partner_overview" -> LogisticsGuard("courierPartners", PermissionAction.VIEW) {
            selectedLogisticsPackageId?.takeIf { it.isNotBlank() }?.let { validId ->
                CourierPartnerOverviewScreen(
                    courierId = validId,
                    viewModel = logisticsViewModel,
                    onBack = {
                        onLogisticsPackageIdSelected(null)
                        onGoBack()
                    },
                    onEditCourierClick = {
                        onSafeNavigate("add_courier_partner", PermissionAction.EDIT)
                    }
                )
            } ?: run { onGoBack() }
        }

        "add_courier_partner" -> LogisticsGuard(
            feature = "courierPartners",
            action = if (selectedLogisticsPackageId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddEditCourierPartnerScreen(
                courierIdToEdit = selectedLogisticsPackageId,
                viewModel = logisticsViewModel,
                onBack = { onGoBack() }
            )
        }

        "logistics_tracking_events", "logistics_order_tracking" -> LogisticsGuard("trackingEvents", PermissionAction.VIEW) {
            TrackingEventsListScreen(
                viewModel = logisticsViewModel,
                onNavigateBack = onGoBack,
                onViewEventDetail = { shipmentId ->
                    val cleanId = shipmentId.trim()
                    if (cleanId.isNotBlank()) {
                        onShipmentIdSelected(cleanId)
                        onSafeNavigate("tracking_overview", PermissionAction.VIEW)
                    }
                }
            )
        }

        "tracking_overview" -> LogisticsGuard("trackingEvents", PermissionAction.VIEW) {
            selectedShipmentId?.takeIf { it.isNotBlank() }?.let { trackingId ->
                TrackingOverviewScreen(
                    viewModel = logisticsViewModel,
                    trackingIdentifier = trackingId,
                    onViewShipment = { shipmentId ->
                        onShipmentIdSelected(shipmentId)
                        onSafeNavigate("shipment_detail", PermissionAction.VIEW)
                    },
                    onClose = {
                        onShipmentIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "logistics_returns" -> LogisticsGuard("returns", PermissionAction.VIEW) {
            LogisticsReturnScreen(
                onNavigateBack = onGoBack,
                onCreateReturnClick = {
                    onReturnIdSelected(null)
                    onSafeNavigate("create_return", PermissionAction.CREATE)
                },
                onEditReturnClick = { returnId ->
                    onReturnIdSelected(returnId)
                    onSafeNavigate("create_return", PermissionAction.EDIT)
                },
                onReturnClick = { returnItem ->
                    val targetId = returnItem.id.ifBlank { returnItem.returnId }
                    if (targetId.isNotBlank()) {
                        onReturnIdSelected(targetId)
                        onSafeNavigate("return_detail", PermissionAction.VIEW)
                    }
                },
                viewModel = logisticsViewModel
            )
        }

        "create_return" -> LogisticsGuard(
            feature = "returns",
            action = if (selectedReturnId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateReturnRequestScreen(
                returnIdToEdit = selectedReturnId,
                onNavigateBack = {
                    onReturnIdSelected(null)
                    onGoBack()
                },
                viewModel = logisticsViewModel
            )
        }

        "return_detail" -> LogisticsGuard("returns", PermissionAction.VIEW) {
            selectedReturnId?.takeIf { it.isNotBlank() }?.let { validReturnId ->
                ReturnRequestDetailsScreen(
                    returnId = validReturnId,
                    onNavigateBack = {
                        onReturnIdSelected(null)
                        onGoBack()
                    },
                    viewModel = logisticsViewModel
                )
            } ?: run { onGoBack() }
        }

        "logistics_cod_screen" -> LogisticsGuard("codOrders", PermissionAction.VIEW) {
            AllCodScreen(
                viewModel = logisticsViewModel,
                onBack = onGoBack,
                onCreateOrderClick = {
                    onSafeNavigate("create_order", PermissionAction.CREATE)
                },
                onViewDeliveryDetail = { deliveryId ->
                    val cleanId = deliveryId.trim()
                    if (cleanId.isNotBlank()) {
                        onLogisticsPackageIdSelected(cleanId)
                        onSafeNavigate("logistics_cod_detail", PermissionAction.VIEW)
                    }
                }
            )
        }

        "logistics_cod_detail" -> LogisticsGuard("codOrders", PermissionAction.VIEW) {
            selectedLogisticsPackageId?.takeIf { it.isNotBlank() }?.let { validId ->
                CodOrderOverviewScreen(
                    codOrderId = validId,
                    viewModel = logisticsViewModel,
                    onBack = {
                        onLogisticsPackageIdSelected(null)
                        onGoBack()
                    },
                    onViewTrackingHistory = { trackingId ->
                        onShipmentIdSelected(trackingId)
                        onSafeNavigate("tracking_overview", PermissionAction.VIEW)
                    }
                )
            } ?: run { onGoBack() }
        }
    }
}

@Composable
private fun LegalRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    hrViewModel: HrViewModel,
    inventoryViewModel: InventoryViewModel,
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
    selectedLegalVendorId: String? = null,
    onLegalVendorIdSelected: (String?) -> Unit = {},
    selectedLegalNoticeId: String? = null,
    onLegalNoticeIdSelected: (String?) -> Unit = {},
    legalViewModel: LegalViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit
) {
    @Composable
    fun LegalGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "legal",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "legal",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "legal_dashboard" -> LegalGuard("dashboard", PermissionAction.VIEW) {
            LegalDashboardScreen()
        }

        "legal_calendar" -> LegalGuard("auditCalendar", PermissionAction.VIEW) {
            AuditLegalCalendarScreen(
                onNavigateBack = onGoBack,
                onScheduleClick = {
                    onSafeNavigate("legal_create_compliance", PermissionAction.CREATE)
                },
                onEventClick = { event ->
                    when (event.module?.lowercase()) {
                        "compliance" -> event.recordId?.let { id ->
                            onComplianceIdSelected(id)
                            onSafeNavigate("legal_compliance_detail", PermissionAction.VIEW)
                        }
                        "contract" -> event.recordId?.let { id ->
                            onContractIdSelected(id)
                            onSafeNavigate("legal_contract_detail", PermissionAction.VIEW)
                        }
                        "license" -> event.recordId?.let { id ->
                            onLegalLicenseIdSelected(id)
                            onSafeNavigate("legal_license_detail", PermissionAction.VIEW)
                        }
                        "employee" -> event.recordId?.let { id ->
                            onLegalEmployeeIdSelected(id)
                            onSafeNavigate("legal_employee_detail", PermissionAction.VIEW)
                        }
                        "vendor" -> event.recordId?.let { id ->
                            onLegalVendorIdSelected(id)
                            onSafeNavigate("legal_vendor_detail", PermissionAction.VIEW)
                        }
                        "customer" -> event.recordId?.let { id ->
                            onLegalCustomerIdSelected(id)
                            onSafeNavigate("legal_customer_detail", PermissionAction.VIEW)
                        }
                        "notice" -> event.recordId?.let { id ->
                            onLegalNoticeIdSelected(id)
                            onSafeNavigate("legal_notice_detail", PermissionAction.VIEW)
                        }
                        "document" -> event.recordId?.let { id ->
                            onLegalDocumentIdSelected(id)
                            onSafeNavigate("legal_document_detail", PermissionAction.VIEW)
                        }
                    }
                },
                viewModel = legalViewModel
            )
        }

        "legal_contracts" -> LegalGuard("contracts", PermissionAction.VIEW) {
            LegalContractsScreen(
                onBack = onGoBack,
                onCreateContract = {
                    onContractIdSelected(null)
                    onSafeNavigate("legal_create_contract", PermissionAction.CREATE)
                },
                onContractClick = { contractId ->
                    if (contractId.isNotBlank()) {
                        onContractIdSelected(contractId)
                        onSafeNavigate("legal_contract_detail", PermissionAction.VIEW)
                    }
                },
                onEditContract = { contractId ->
                    if (contractId.isNotBlank()) {
                        onContractIdSelected(contractId)
                        onSafeNavigate("legal_create_contract", PermissionAction.EDIT)
                    }
                }
            )
        }

        "legal_contract_detail" -> LegalGuard("contracts", PermissionAction.VIEW) {
            selectedContractId?.takeIf { it.isNotBlank() }?.let { validId ->
                ContractOverviewScreen(
                    contractId = validId,
                    onClose = {
                        onContractIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_contract" -> LegalGuard(
            feature = "contracts",
            action = if (selectedContractId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateLegalContractScreen(
                contractIdToEdit = selectedContractId,
                onClose = {
                    onContractIdSelected(null)
                    onGoBack()
                }
            )
        }

        "legal_compliance" -> LegalGuard("compliance", PermissionAction.VIEW) {
            LegalComplianceScreen(
                onBack = onGoBack,
                onAddCompliance = {
                    onComplianceIdSelected(null)
                    onSafeNavigate("legal_create_compliance", PermissionAction.CREATE)
                },
                onComplianceClick = { compliance ->
                    if (compliance.id.isNotBlank()) {
                        onComplianceIdSelected(compliance.id)
                        onSafeNavigate("legal_compliance_detail", PermissionAction.VIEW)
                    }
                },
                onEditCompliance = { complianceId ->
                    if (complianceId.isNotBlank()) {
                        onComplianceIdSelected(complianceId)
                        onSafeNavigate("legal_create_compliance", PermissionAction.EDIT)
                    }
                }
            )
        }

        "legal_compliance_detail" -> LegalGuard("compliance", PermissionAction.VIEW) {
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
                            onSafeNavigate("legal_create_compliance", PermissionAction.EDIT)
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_compliance" -> LegalGuard(
            feature = "compliance",
            action = if (selectedComplianceId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateLegalComplianceScreen(
                complianceIdToEdit = selectedComplianceId,
                onClose = { onGoBack() }
            )
        }

        "legal_documents" -> LegalGuard("documents", PermissionAction.VIEW) {
            LegalDocumentsScreen(
                onBack = onGoBack,
                onAddDocument = {
                    onLegalDocumentIdSelected(null)
                    onSafeNavigate("legal_create_document", PermissionAction.CREATE)
                },
                onDocumentClick = { doc ->
                    if (doc.id.isNotBlank()) {
                        onLegalDocumentIdSelected(doc.id)
                        onSafeNavigate("legal_document_detail", PermissionAction.VIEW)
                    }
                },
                onEditDocument = { docId ->
                    if (docId.isNotBlank()) {
                        onLegalDocumentIdSelected(docId)
                        onSafeNavigate("legal_create_document", PermissionAction.EDIT)
                    }
                }
            )
        }

        "legal_document_detail" -> LegalGuard("documents", PermissionAction.VIEW) {
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
                            onSafeNavigate("legal_create_document", PermissionAction.EDIT)
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_document" -> LegalGuard(
            feature = "documents",
            action = if (selectedLegalDocumentId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateUpdateLegalDocumentScreen(
                documentIdToEdit = selectedLegalDocumentId,
                onClose = { onGoBack() }
            )
        }

        "legal_licenses" -> LegalGuard("licenses", PermissionAction.VIEW) {
            LegalLicenseScreen(
                onBack = onGoBack,
                onAddLicense = {
                    onLegalLicenseIdSelected(null)
                    onSafeNavigate("legal_create_license", PermissionAction.CREATE)
                },
                onLicenseClick = { license ->
                    if (license.id.isNotBlank()) {
                        onLegalLicenseIdSelected(license.id)
                        onSafeNavigate("legal_license_detail", PermissionAction.VIEW)
                    }
                },
                onEditLicense = { licenseId ->
                    if (licenseId.isNotBlank()) {
                        onLegalLicenseIdSelected(licenseId)
                        onSafeNavigate("legal_create_license", PermissionAction.EDIT)
                    }
                }
            )
        }

        "legal_license_detail" -> LegalGuard("licenses", PermissionAction.VIEW) {
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
                            onSafeNavigate("legal_create_license", PermissionAction.EDIT)
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_license" -> LegalGuard(
            feature = "licenses",
            action = if (selectedLegalLicenseId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateLegalLicenseScreen(
                licenseIdToEdit = selectedLegalLicenseId,
                onClose = {
                    onLegalLicenseIdSelected(null)
                    onGoBack()
                }
            )
        }

        "legal_customer" -> LegalGuard("customerLegal", PermissionAction.VIEW) {
            LegalCustomerListScreen(
                onBack = onGoBack,
                onAddCustomerRecord = {
                    onLegalCustomerIdSelected(null)
                    onSafeNavigate("legal_create_customer_record", PermissionAction.CREATE)
                },
                onRecordClick = { item ->
                    if (item.id.isNotBlank()) {
                        onLegalCustomerIdSelected(item.id)
                        onSafeNavigate("legal_customer_detail", PermissionAction.VIEW)
                    }
                },
                onEditRecord = { id ->
                    if (id.isNotBlank()) {
                        onLegalCustomerIdSelected(id)
                        onSafeNavigate("legal_create_customer_record", PermissionAction.EDIT)
                    }
                }
            )
        }

        "legal_customer_detail" -> LegalGuard("customerLegal", PermissionAction.VIEW) {
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
                            onSafeNavigate("legal_create_customer_record", PermissionAction.EDIT)
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_customer_record" -> LegalGuard(
            feature = "customerLegal",
            action = if (selectedLegalCustomerId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateLegalCustomerScreen(
                recordIdToEdit = selectedLegalCustomerId,
                onClose = { onGoBack() }
            )
        }

        "legal_employee" -> LegalGuard("employeeLegal", PermissionAction.VIEW) {
            LegalEmployeeListScreen(
                viewModel = legalViewModel,
                onNavigateBack = onGoBack,
                onNavigateToCreate = {
                    onLegalEmployeeIdSelected(null)
                    onSafeNavigate("legal_create_employee", PermissionAction.CREATE)
                },
                onNavigateToEdit = { employeeId ->
                    if (employeeId.isNotBlank()) {
                        onLegalEmployeeIdSelected(employeeId)
                        onSafeNavigate("legal_create_employee", PermissionAction.EDIT)
                    }
                },
                onNavigateToDetail = { employeeId ->
                    if (employeeId.isNotBlank()) {
                        onLegalEmployeeIdSelected(employeeId)
                        onSafeNavigate("legal_employee_detail", PermissionAction.VIEW)
                    }
                }
            )
        }

        "legal_employee_detail" -> LegalGuard("employeeLegal", PermissionAction.VIEW) {
            selectedLegalEmployeeId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalEmployeeDetailScreen(
                    recordId = validId,
                    viewModel = legalViewModel,
                    hrViewModel = hrViewModel,
                    onNavigateBack = {
                        onLegalEmployeeIdSelected(null)
                        onGoBack()
                    },
                    onNavigateToEdit = { id ->
                        if (id.isNotBlank()) {
                            onLegalEmployeeIdSelected(id)
                            onSafeNavigate("legal_create_employee", PermissionAction.EDIT)
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_employee" -> LegalGuard(
            feature = "employeeLegal",
            action = if (selectedLegalEmployeeId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
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

        "legal_vendor" -> LegalGuard("vendorLegal", PermissionAction.VIEW) {
            LegalVendorListScreen(
                viewModel = legalViewModel,
                onNavigateBack = onGoBack,
                onNavigateToCreate = {
                    onLegalVendorIdSelected(null)
                    onSafeNavigate("legal_create_vendor", PermissionAction.CREATE)
                },
                onNavigateToDetail = { vendorId ->
                    if (vendorId.isNotBlank()) {
                        onLegalVendorIdSelected(vendorId)
                        onSafeNavigate("legal_vendor_detail", PermissionAction.VIEW)
                    }
                },
                onNavigateToEdit = { vendorId ->
                    if (vendorId.isNotBlank()) {
                        onLegalVendorIdSelected(vendorId)
                        onSafeNavigate("legal_create_vendor", PermissionAction.EDIT)
                    }
                }
            )
        }

        "legal_vendor_detail" -> LegalGuard("vendorLegal", PermissionAction.VIEW) {
            selectedLegalVendorId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalVendorDetailScreen(
                    vendorId = validId,
                    viewModel = legalViewModel,
                    onNavigateBack = {
                        onLegalVendorIdSelected(null)
                        onGoBack()
                    },
                    onNavigateToEdit = { id ->
                        if (id.isNotBlank()) {
                            onLegalVendorIdSelected(id)
                            onSafeNavigate("legal_create_vendor", PermissionAction.EDIT)
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_vendor" -> LegalGuard(
            feature = "vendorLegal",
            action = if (selectedLegalVendorId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateEditLegalVendorScreen(
                idToEdit = selectedLegalVendorId,
                viewModel = legalViewModel,
                hrViewModel = hrViewModel,
                branchViewModel = branchViewModel,
                inventoryViewModel = inventoryViewModel,
                onNavigateBack = {
                    onLegalVendorIdSelected(null)
                    onGoBack()
                }
            )
        }

        "legal_notice" -> LegalGuard("notices", PermissionAction.VIEW) {
            LegalNoticeListScreen(
                viewModel = legalViewModel,
                onNavigateBack = onGoBack,
                onNavigateToCreate = {
                    onLegalNoticeIdSelected(null)
                    onSafeNavigate("legal_create_notice", PermissionAction.CREATE)
                },
                onNavigateToDetail = { noticeId ->
                    if (noticeId.isNotBlank()) {
                        onLegalNoticeIdSelected(noticeId)
                        onSafeNavigate("legal_notice_detail", PermissionAction.VIEW)
                    }
                },
                onNavigateToEdit = { noticeId ->
                    if (noticeId.isNotBlank()) {
                        onLegalNoticeIdSelected(noticeId)
                        onSafeNavigate("legal_create_notice", PermissionAction.EDIT)
                    }
                }
            )
        }

        "legal_notice_detail" -> LegalGuard("notices", PermissionAction.VIEW) {
            selectedLegalNoticeId?.takeIf { it.isNotBlank() }?.let { validId ->
                LegalNoticeDetailScreen(
                    noticeId = validId,
                    viewModel = legalViewModel,
                    onNavigateBack = {
                        onLegalNoticeIdSelected(null)
                        onGoBack()
                    },
                    onNavigateToEdit = { id ->
                        if (id.isNotBlank()) {
                            onLegalNoticeIdSelected(id)
                            onSafeNavigate("legal_create_notice", PermissionAction.EDIT)
                        }
                    }
                )
            } ?: run { onGoBack() }
        }

        "legal_create_notice" -> LegalGuard(
            feature = "notices",
            action = if (selectedLegalNoticeId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            CreateEditLegalNoticeScreen(
                idToEdit = selectedLegalNoticeId,
                viewModel = legalViewModel,
                hrViewModel = hrViewModel,
                branchViewModel = branchViewModel,
                onNavigateBack = {
                    onLegalNoticeIdSelected(null)
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
    permissionViewModel: PermissionViewModel,
    hrViewModel: HrViewModel,
    servicesViewModel: ServicesViewModel,
    settingsViewModel: SettingsViewModel,
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
    selectedProductionStage: ServicesProductionStageDto?,
    onProductionStageSelected: (ServicesProductionStageDto?) -> Unit,
    pendingOrderReviewData: OrderReviewData?,
    onPendingOrderReviewDataChange: (OrderReviewData?) -> Unit,
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onPopToScreen: (String) -> Unit = {},
    onOpenDrawer: () -> Unit,
    onOpenModulesPanel: (String) -> Unit
) {
    var selectedStageForEdit by remember { mutableStateOf<ServicesProductionStageDto?>(null) }
    val productionStagesViewModel: SettingsViewModel = hiltViewModel()

    @Composable
    fun ServicesGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "services",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "services",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "services_dashboard" -> ServicesGuard("dashboard", PermissionAction.VIEW) {
            ServicesDashboardScreen(
                onNavigateToRequests = { onSafeNavigate("services_service_request", PermissionAction.VIEW) },
                onNavigateToComplaints = { onSafeNavigate("services_customer_feedback", PermissionAction.VIEW) }
            )
        }

        "services_service_status", "sales_orders" -> ServicesGuard("workflowStages", PermissionAction.VIEW) {
            ServiceStatusScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack,
                onViewOrder = { orderId ->
                    onManagementOrderIdSelected(orderId)
                    onSafeNavigate("service_status_detail", PermissionAction.VIEW)
                },
                onBreadCrumbClick = { onOpenModulesPanel("Services") }
            )
        }

        "service_status_detail", "order_management_overview" -> ServicesGuard("workflowStages", PermissionAction.VIEW) {
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

        "services_delay_rework" -> ServicesGuard("workflowStages", PermissionAction.VIEW) {
            DelayReworkTrackingScreen(
                onClose = onGoBack,
                onLogNewDelay = { },
                onRegisterAlteration = { onSafeNavigate("create_alteration", PermissionAction.CREATE) },
                onCreateReworkOrder = { onSafeNavigate("create_order", PermissionAction.CREATE) },
                onViewLog = { },
                onGenerateReport = { }
            )
        }

        "services_service_delivery" -> ServicesGuard("workflowStages", PermissionAction.VIEW) {
            ServiceDeliveryStatusScreen(
                onClose = onGoBack,
                onContactCustomer = { },
                onEditDetails = { },
                onReschedule = { },
                onMarkDelivered = { },
                onViewUrgentOrder = { }
            )
        }

        "services_production_orders",
        "services_service_orders",
        "services_service_order" -> ServicesGuard("productionOrders", PermissionAction.VIEW) {
            ServiceOrderScreen(
                navController = navController,
                onMenuClick = onOpenDrawer,
                onBack = onGoBack,
                onCreateOrder = {
                    onPendingOrderReviewDataChange(null)
                    onSafeNavigate("create_order", PermissionAction.CREATE)
                },
                onViewOrder = { orderId ->
                    onOrderIdSelected(orderId)
                    onSafeNavigate("service_order_overview", PermissionAction.VIEW)
                },
                onEditOrder = { orderId -> onEditOrderIdChange(orderId) },
                onBreadCrumbClick = { onOpenModulesPanel("Services") }
            )
        }

        "services_production_works" -> ServicesGuard("productionWorks", PermissionAction.VIEW) {
            ProductionWorksScreen(
                onClose = onGoBack,
                onOrderClick = { orderId ->
                    if (orderId.isNotBlank()) {
                        onOrderIdSelected(orderId)
                        onSafeNavigate("services_production_pipeline", PermissionAction.VIEW)
                    }
                },
                viewModel = servicesViewModel
            )
        }

        "services_production_pipeline" -> ServicesGuard("productionWorks", PermissionAction.VIEW) {
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
                        onSafeNavigate("services_qc_delivery", PermissionAction.VIEW)
                    },
                    onGoToAssignmentClick = { id, jobCardId ->
                        onOrderIdSelected(id)
                        onAssignmentJobCardIdSelected(jobCardId)
                        onSafeNavigate("services_assignment_mode", PermissionAction.VIEW)
                    }
                )
            } else {
                onGoBack()
            }
        }

        "services_assignment_mode" -> ServicesGuard("productionWorks", PermissionAction.VIEW) {
            val orderIdToLoad = selectedOrderId?.takeIf { it.isNotBlank() }
            val jobCardId = selectedAssignmentJobCardId?.takeIf { it.isNotBlank() }
            if (orderIdToLoad != null && jobCardId != null) {
                ServiceOrderOverviewScreen(
                    orderId = orderIdToLoad,
                    initialAssignJobCardId = jobCardId,
                    onClose = onGoBack,
                    onAssignmentRouted = onGoBack,
                    onCreateNew = {
                        onPendingOrderReviewDataChange(null)
                        onSafeNavigate("create_order", PermissionAction.CREATE)
                    }
                )
            } else {
                onGoBack()
            }
        }

        "services_qc_delivery" -> ServicesGuard("productionWorks", PermissionAction.VIEW) {
            val orderIdToLoad = selectedOrderId?.takeIf { it.isNotBlank() }
            if (orderIdToLoad != null) {
                QualityDeliveryDispatchScreen(
                    orderId = orderIdToLoad,
                    viewModel = servicesViewModel,
                    onBack = onGoBack,
                    onBackToOrders = { onPopToScreen("services_production_works") }
                )
            } else {
                onGoBack()
            }
        }

        "services_production_stages", "services_workflow_stages" -> ServicesGuard("workflowStages", PermissionAction.VIEW) {
            ProductionStagesListScreen(
                viewModel = settingsViewModel,
                onClose = onGoBack,
                onAddNewStage = {
                    onProductionStageSelected(null)
                    onSafeNavigate("services_create_production_stage", PermissionAction.CREATE)
                },
                onEditStage = { stage ->
                    onProductionStageSelected(stage)
                    onSafeNavigate("services_create_production_stage", PermissionAction.EDIT)
                }
            )
        }

        "services_create_production_stage" -> ServicesGuard(
            feature = "workflowStages",
            action = if (selectedProductionStage != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddEditProductionStageScreen(
                stageToEdit = selectedProductionStage,
                viewModel = settingsViewModel,
                onClose = {
                    onProductionStageSelected(null)
                    onGoBack()
                }
            )
        }

        "service_order_overview" -> ServicesGuard("productionOrders", PermissionAction.VIEW) {
            selectedOrderId?.let { id ->
                ServiceOrderOverviewScreen(
                    orderId = id,
                    onClose = onGoBack,
                    onCreateNew = {
                        onPendingOrderReviewDataChange(null)
                        onSafeNavigate("create_order", PermissionAction.CREATE)
                    }
                )
            } ?: run { onGoBack() }
        }

        "services_service_request" -> ServicesGuard("serviceRequests", PermissionAction.VIEW) {
            ServiceRequestScreen(
                onClose = onGoBack,
                onCreateNewRequest = { onSafeNavigate("create_request", PermissionAction.CREATE) },
                onViewClick = { requestId ->
                    onServiceRequestIdSelected(requestId)
                    onSafeNavigate("review_services", PermissionAction.VIEW)
                }
            )
        }

        "create_request" -> ServicesGuard("serviceRequests", PermissionAction.CREATE) {
            CreateServiceRequest(
                onClose = onGoBack,
                onCancel = onGoBack,
                onNavigateToReview = { onSafeNavigate("review_service_request", PermissionAction.CREATE) }
            )
        }

        "review_service_request" -> ServicesGuard("serviceRequests", PermissionAction.CREATE) {
            ReviewServiceRequestScreen(
                onBackToEdit = onGoBack,
                onSuccessSubmitted = {
                    onGoBack()
                    onGoBack()
                }
            )
        }

        "review_services" -> ServicesGuard("serviceRequests", PermissionAction.VIEW) {
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

        "services_alteration_management" -> ServicesGuard("alterations", PermissionAction.VIEW) {
            AlterationManagementScreen(
                viewModel = servicesViewModel,
                onClose = onGoBack,
                onCreateNewAlteration = {
                    onAlterationIdSelected(null)
                    onSafeNavigate("create_alteration", PermissionAction.CREATE)
                },
                onBreadcrumbClick = { onOpenModulesPanel("Services") },
                onItemClick = { item ->
                    if (item.id.isNotBlank()) {
                        onAlterationIdSelected(item.id)
                        onSafeNavigate("view_alteration", PermissionAction.VIEW)
                    }
                }
            )
        }

        "view_alteration", "create_alteration" -> ServicesGuard("alterations", PermissionAction.VIEW) {
            selectedAlterationId?.takeIf { it.isNotBlank() }?.let { validId ->
                ViewAlterationManagementScreen(
                    alterationId = validId,
                    viewModel = servicesViewModel,
                    onClose = {
                        onAlterationIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "services_service_templates" -> ServicesGuard("serviceTemplates", PermissionAction.VIEW) {
            ServiceTemplateListScreen(
                onClose = onGoBack,
                onAddNewTemplate = {
                    onServiceTemplateSelected(null)
                    onSafeNavigate("services_create_service_template", PermissionAction.CREATE)
                },
                onViewTemplate = { templateItem ->
                    onServiceTemplateSelected(templateItem)
                    onSafeNavigate("services_view_service_template", PermissionAction.VIEW)
                },
                onEditTemplate = { templateItem ->
                    onServiceTemplateSelected(templateItem)
                    onSafeNavigate("services_create_service_template", PermissionAction.EDIT)
                }
            )
        }

        "services_view_service_template" -> ServicesGuard("serviceTemplates", PermissionAction.VIEW) {
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

        "services_create_service_template" -> ServicesGuard(
            feature = "serviceTemplates",
            action = if (selectedServiceTemplate != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
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

        "services_customer_complaints" -> ServicesGuard("customerComplaints", PermissionAction.VIEW) {
            ComplaintListScreen(
                viewModel = servicesViewModel,
                onBack = onGoBack,
                onComplaintClick = { complaintId ->
                    onComplaintIdSelected(complaintId)
                    onSafeNavigate("complaint_detail", PermissionAction.VIEW)
                }
            )
        }

        "complaint_detail" -> ServicesGuard("customerComplaints", PermissionAction.VIEW) {
            selectedComplaintId?.let { id ->
                ComplaintDetailViewScreen(
                    complaintId = id,
                    viewModel = servicesViewModel,
                    hrViewModel = hrViewModel,
                    onBack = {
                        onComplaintIdSelected(null)
                        onGoBack()
                    },
                    onEditClick = { }
                )
            } ?: run { onGoBack() }
        }

        "services_customer_feedback" -> ServicesGuard("customerFeedback", PermissionAction.VIEW) {
            CustomerFeedbackScreen(
                viewModel = servicesViewModel,
                onBack = onGoBack,
                onFeedbackClick = { feedbackId ->
                    onFeedbackIdSelected(feedbackId)
                    onSafeNavigate("feedback_detail", PermissionAction.VIEW)
                }
            )
        }

        "feedback_detail" -> ServicesGuard("customerFeedback", PermissionAction.VIEW) {
            selectedFeedbackId?.let { id ->
                FeedbackDetailScreen(
                    feedbackId = id,
                    viewModel = servicesViewModel,
                    onDismiss = {
                        onFeedbackIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }
    }
}

/**
 * Router dedicated to IT Module screens.
 */
/**
 * Router dedicated to IT Module screens.
 */
@Composable
private fun ItRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    selectedTicketId: String? = null,
    onTicketIdSelected: (String?) -> Unit = {},
    selectedAssetId: String? = null,
    onAssetIdSelected: (String?) -> Unit = {},
    selectedBackupSnapshotId: String? = null,
    onBackupSnapshotIdSelected: (String?) -> Unit = {},
    selectedItDeviceId: String? = null,
    onItDeviceIdSelected: (String?) -> Unit = {},
    selectedPosTerminalId: String? = null,
    onPosTerminalIdSelected: (String?) -> Unit = {},
    itViewModel: ItViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    @Composable
    fun ItGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "it",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "it",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "it_dashboard" -> ItGuard("dashboard", PermissionAction.VIEW) {
            ItDashboardScreen(
                onNavigateToRoute = { route -> onSafeNavigate(route, null) },
                viewModel = itViewModel
            )
        }

        "it_device_management" -> ItGuard("deviceManagement", PermissionAction.VIEW) {
            AllDeviceScreen(
                viewModel = itViewModel,
                onBack = onGoBack,
                onDeviceClick = { deviceId ->
                    onItDeviceIdSelected(deviceId)
                    onSafeNavigate("it_device_detail", PermissionAction.VIEW)
                }
            )
        }

        "it_device_detail" -> ItGuard("deviceManagement", PermissionAction.VIEW) {
            selectedItDeviceId?.takeIf { it.isNotBlank() }?.let { id ->
                DeviceDetailScreen(
                    deviceId = id,
                    viewModel = itViewModel,
                    onBack = {
                        onItDeviceIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "it_pos_billing" -> ItGuard("posAndBillingTechnology", PermissionAction.VIEW) {
            TerminalFleetStatusScreen(
                viewModel = itViewModel,
                onBack = onGoBack,
                onTerminalClick = { id ->
                    onPosTerminalIdSelected(id)
                    onSafeNavigate("it_pos_billing_detail", PermissionAction.VIEW)
                }
            )
        }

        "it_pos_billing_detail" -> ItGuard("posAndBillingTechnology", PermissionAction.VIEW) {
            selectedPosTerminalId?.takeIf { it.isNotBlank() }?.let { id ->
                PosDetailScreen(
                    posTerminalId = id,
                    viewModel = itViewModel,
                    onBack = {
                        onPosTerminalIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "it_integrations" -> ItGuard("integrations", PermissionAction.VIEW) {
            val marketingViewModel: MarketingViewModel = hiltViewModel()
            IntegrationManagementScreen(
                viewModel = marketingViewModel,
                onClose = onGoBack
            )
        }

        "it_backup_recovery" -> ItGuard("backupAndRecovery", PermissionAction.VIEW) {
            BackupRecoveryListScreen(
                viewModel = itViewModel,
                onBack = onGoBack,
                onBackupClick = { snapshotId ->
                    onBackupSnapshotIdSelected(snapshotId)
                    onSafeNavigate("it_backup_recovery_detail", PermissionAction.VIEW)
                }
            )
        }

        "it_backup_recovery_detail" -> ItGuard("backupAndRecovery", PermissionAction.VIEW) {
            selectedBackupSnapshotId?.takeIf { it.isNotBlank() }?.let { validSnapshotId ->
                BackupRecoveryDetailScreen(
                    snapshotId = validSnapshotId,
                    viewModel = itViewModel,
                    onBack = {
                        onBackupSnapshotIdSelected(null)
                        onGoBack()
                    },
                    onRestoreClick = { targetBackupId ->
                        onBackupSnapshotIdSelected(targetBackupId)
                        onSafeNavigate("it_backup_restore", PermissionAction.EDIT)
                    }
                )
            } ?: run { onGoBack() }
        }

        "it_backup_restore" -> ItGuard("backupAndRecovery", PermissionAction.EDIT) {
            selectedBackupSnapshotId?.takeIf { it.isNotBlank() }?.let { validSnapshotId ->
                RestoreBackupScreen(
                    snapshotId = validSnapshotId,
                    viewModel = itViewModel,
                    onBack = onGoBack,
                    onRestoreTriggered = { onGoBack() }
                )
            } ?: run { onGoBack() }
        }

        "it_asset_management" -> ItGuard("itAssetManagement", PermissionAction.VIEW) {
            ItAssetListScreen(
                viewModel = itViewModel,
                onNavigateBack = onGoBack,
                onNavigateDetail = { assetId ->
                    onAssetIdSelected(assetId)
                    onSafeNavigate("it_asset_detail", PermissionAction.VIEW)
                },
                onNavigateRegister = {
                    onSafeNavigate("it_asset_register", PermissionAction.CREATE)
                }
            )
        }

        "it_asset_detail" -> ItGuard("itAssetManagement", PermissionAction.VIEW) {
            selectedAssetId?.takeIf { it.isNotBlank() }?.let { validAssetId ->
                ItAssetDetailScreen(
                    assetId = validAssetId,
                    viewModel = itViewModel,
                    onNavigateBack = {
                        onAssetIdSelected(null)
                        onGoBack()
                    },
                    onNavigateDevice = { deviceId ->
                        onItDeviceIdSelected(deviceId)
                        onSafeNavigate("it_device_detail", PermissionAction.VIEW)
                    }
                )
            } ?: run { onGoBack() }
        }

        "it_asset_register" -> ItGuard("itAssetManagement", PermissionAction.CREATE) {
            ItRegisterAssetScreen(
                viewModel = itViewModel,
                onNavigateBack = onGoBack
            )
        }

        "it_service_desk" -> ItGuard("itServiceDesk", PermissionAction.VIEW) {
            ItServiceDeskListScreen(
                viewModel = itViewModel,
                onNavigateBack = onGoBack,
                onNavigateDetail = { ticketId ->
                    onTicketIdSelected(ticketId)
                    onSafeNavigate("it_service_desk_detail", PermissionAction.VIEW)
                },
                onNavigateCreate = {
                    onSafeNavigate("it_service_desk_create", PermissionAction.CREATE)
                }
            )
        }

        "it_service_desk_create" -> ItGuard("itServiceDesk", PermissionAction.CREATE) {
            ItCreateTicketScreen(
                viewModel = itViewModel,
                onNavigateBack = onGoBack
            )
        }

        "it_service_desk_detail" -> ItGuard("itServiceDesk", PermissionAction.VIEW) {
            selectedTicketId?.takeIf { it.isNotBlank() }?.let { validTicketId ->
                ItTicketDetailScreen(
                    ticketId = validTicketId,
                    viewModel = itViewModel,
                    onNavigateBack = {
                        onTicketIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        else -> onGoBack()
    }
}



/**
 * Router dedicated to Security Module screens.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SecurityRouter(
    screen: String,
    selectedIntegrationId: String? = null,
    onIntegrationIdSelected: (String?) -> Unit = {},
    selectedBackupId: String? = null,
    onBackupIdSelected: (String?) -> Unit = {},
    selectedRecoveryId: String? = null,
    onRecoveryIdSelected: (String?) -> Unit = {},
    selectedApiLogId: String? = null,
    onApiLogIdSelected: (String?) -> Unit = {},
    selectedAuditLogId: String? = null,
    onAuditLogIdSelected: (String?) -> Unit = {},
    selectedSupportTicketId: String? = null,
    onSupportTicketIdSelected: (String?) -> Unit = {},
    securityViewModel: SecurityViewModel = hiltViewModel(),
    hrViewModel: HrViewModel,
    branchViewModel: BranchViewModel,
    permissionViewModel: PermissionViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    var selectedHelpArticleSlug by remember { mutableStateOf<String?>(null) }

    @Composable
    fun SecurityGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "security",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "security",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "security_dashboard" -> SecurityGuard("dashboard", PermissionAction.VIEW) {
            SecurityDashboardScreen(
                viewModel = securityViewModel,
                onClose = onGoBack
            )
        }

        "security_mobile_security" -> SecurityGuard("mobileSecurity", PermissionAction.VIEW) {
            MobileAccessScreen(
                viewModel = securityViewModel,
                onBack = onGoBack
            )
        }

        "security_support_ticket" -> SecurityGuard("supportTicket", PermissionAction.VIEW) {
            SupportTicketsListScreen(
                onClose = onGoBack,
                onCreateTicket = {
                    onSupportTicketIdSelected(null)
                    onSafeNavigate("security_create_support_ticket", PermissionAction.CREATE)
                },
                onViewTicket = { ticketId: String ->
                    onSupportTicketIdSelected(ticketId)
                    onSafeNavigate("security_support_ticket_detail", PermissionAction.VIEW)
                },
                viewModel = securityViewModel
            )
        }

        "security_create_support_ticket" -> SecurityGuard("supportTicket", PermissionAction.CREATE) {
            CreateSupportTicketScreen(
                viewModel = securityViewModel,
                hrViewModel = hrViewModel,
                branchViewModel = branchViewModel,
                onBack = {
                    onSupportTicketIdSelected(null)
                    onGoBack()
                }
            )
        }

        "security_support_ticket_detail" -> SecurityGuard("supportTicket", PermissionAction.VIEW) {
            selectedSupportTicketId?.takeIf { it.isNotBlank() }?.let { validTicketId: String ->
                SupportTicketDetailScreen(
                    ticketId = validTicketId,
                    viewModel = securityViewModel,
                    hrViewModel = hrViewModel,
                    onBack = {
                        onSupportTicketIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "security_help_center" -> SecurityGuard("helpCenter", PermissionAction.VIEW) {
            if (selectedHelpArticleSlug != null) {
                HelpArticleDetailScreen(
                    articleSlug = selectedHelpArticleSlug!!,
                    viewModel = securityViewModel,
                    onBack = { selectedHelpArticleSlug = null },
                    onCreateTicketClick = { onSafeNavigate("security_support_ticket", PermissionAction.VIEW) }
                )
            } else {
                HelpCenterScreen(
                    viewModel = securityViewModel,
                    onBack = onGoBack,
                    onArticleClick = { article -> selectedHelpArticleSlug = article.slug },
                    onCreateTicketClick = { onSafeNavigate("security_support_ticket", PermissionAction.VIEW) }
                )
            }
        }

        "security_users" -> SecurityGuard("users", PermissionAction.VIEW) {
            UserManagementScreen(
                securityViewModel = securityViewModel,
                permissionViewModel = permissionViewModel,
                onBack = onGoBack,
                onCreateUserClick = {
                    onSafeNavigate("security_create_user", PermissionAction.CREATE)
                },
                onViewUserClick = { },
                onEditUserClick = { }
            )
        }

        "security_create_user" -> SecurityGuard("users", PermissionAction.CREATE) {
            val departmentViewModel: DepartmentViewModel = hiltViewModel()
            val designationViewModel: DesignationViewModel = hiltViewModel()

            CreateUserScreen(
                securityViewModel = securityViewModel,
                hrViewModel = hrViewModel,
                branchViewModel = branchViewModel,
                departmentViewModel = departmentViewModel,
                designationViewModel = designationViewModel,
                onBack = onGoBack,
                onUserCreated = {
                    securityViewModel.fetchUsers()
                    onGoBack()
                }
            )
        }

        "security_roles" -> SecurityGuard("roles", PermissionAction.VIEW) {
            LaunchedEffect(Unit) {
                onNavigate("home_role_management")
            }
        }

        "security_data_backup" -> SecurityGuard("dataBackup", PermissionAction.VIEW) {
            DataBackupListScreen(
                viewModel = securityViewModel,
                onNavigateBack = onGoBack,
                onNavigateToDetail = { backupId ->
                    onBackupIdSelected(backupId)
                    onSafeNavigate("security_backup_detail", PermissionAction.VIEW)
                },
                onNavigateToCreate = {
                    onSafeNavigate("security_create_backup", PermissionAction.CREATE)
                }
            )
        }

        "security_create_backup" -> SecurityGuard("dataBackup", PermissionAction.CREATE) {
            CreateBackupScreen(
                viewModel = securityViewModel,
                onNavigateBack = onGoBack,
                onSuccessCreated = {
                    securityViewModel.fetchBackups()
                    onGoBack()
                }
            )
        }

        "security_backup_detail" -> SecurityGuard("dataBackup", PermissionAction.VIEW) {
            selectedBackupId?.takeIf { it.isNotBlank() }?.let { validId ->
                DataBackupDetailScreen(
                    backupId = validId,
                    viewModel = securityViewModel,
                    onNavigateBack = {
                        onBackupIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "security_recovery" -> SecurityGuard("recovery", PermissionAction.VIEW) {
            DataRecoveryListScreen(
                viewModel = securityViewModel,
                onNavigateBack = onGoBack,
                onNavigateToRecoveryDetail = { recoveryId ->
                    onRecoveryIdSelected(recoveryId)
                    onSafeNavigate("security_recovery_detail", PermissionAction.VIEW)
                }
            )
        }

        "security_recovery_detail" -> SecurityGuard("recovery", PermissionAction.VIEW) {
            selectedRecoveryId?.takeIf { it.isNotBlank() }?.let { validRecoveryId ->
                DataRecoveryDetailScreen(
                    recoveryId = validRecoveryId,
                    viewModel = securityViewModel,
                    onNavigateBack = {
                        onRecoveryIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "security_api_integration" -> SecurityGuard("apiSecurity", PermissionAction.VIEW) {
            ApiIntegrationListScreen(
                viewModel = securityViewModel,
                onNavigateBack = onGoBack,
                onNavigateToDetail = { integrationId ->
                    onIntegrationIdSelected(integrationId)
                    onSafeNavigate("security_api_integration_detail", PermissionAction.VIEW)
                },
                onNavigateToAdd = {
                    onIntegrationIdSelected(null)
                    onSafeNavigate("security_create_api_integration", PermissionAction.CREATE)
                },
                onNavigateToEdit = { integrationId ->
                    onIntegrationIdSelected(integrationId)
                    onSafeNavigate("security_create_api_integration", PermissionAction.EDIT)
                }
            )
        }

        "security_create_api_integration" -> SecurityGuard(
            feature = "apiSecurity",
            action = if (selectedIntegrationId != null) PermissionAction.EDIT else PermissionAction.CREATE
        ) {
            AddApiIntegrationScreen(
                integrationIdToEdit = selectedIntegrationId,
                viewModel = securityViewModel,
                onNavigateBack = {
                    onIntegrationIdSelected(null)
                    onGoBack()
                },
                onSuccessSaved = {
                    onIntegrationIdSelected(null)
                    securityViewModel.fetchApiIntegrations()
                    onGoBack()
                }
            )
        }

        "security_api_integration_detail" -> SecurityGuard("apiSecurity", PermissionAction.VIEW) {
            selectedIntegrationId?.takeIf { it.isNotBlank() }?.let { validId ->
                ApiIntegrationDetailScreen(
                    integrationId = validId,
                    viewModel = securityViewModel,
                    onNavigateBack = {
                        onIntegrationIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "security_api_logs" -> SecurityGuard("apiLogs", PermissionAction.VIEW) {
            ApiLogsListScreen(
                viewModel = securityViewModel,
                onNavigateBack = onGoBack,
                onNavigateToDetail = { logId ->
                    onApiLogIdSelected(logId)
                    onSafeNavigate("security_api_log_detail", PermissionAction.VIEW)
                }
            )
        }

        "security_api_log_detail" -> SecurityGuard("apiLogs", PermissionAction.VIEW) {
            selectedApiLogId?.takeIf { it.isNotBlank() }?.let { validId ->
                ApiLogDetailScreen(
                    logId = validId,
                    viewModel = securityViewModel,
                    onNavigateBack = {
                        onApiLogIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        "security_audit_logs" -> SecurityGuard("auditLogs", PermissionAction.VIEW) {
            AuditLogsListScreen(
                viewModel = securityViewModel,
                onNavigateBack = onGoBack,
                onNavigateToDetail = { logId ->
                    onAuditLogIdSelected(logId)
                    onSafeNavigate("security_audit_log_detail", PermissionAction.VIEW)
                }
            )
        }

        "security_audit_log_detail" -> SecurityGuard("auditLogs", PermissionAction.VIEW) {
            selectedAuditLogId?.takeIf { it.isNotBlank() }?.let { validId ->
                AuditLogDetailScreen(
                    logId = validId,
                    viewModel = securityViewModel,
                    onNavigateBack = {
                        onAuditLogIdSelected(null)
                        onGoBack()
                    }
                )
            } ?: run { onGoBack() }
        }

        else -> {
            onShowComingSoon("Coming Soon, Stay tuned !")
            onGoBack()
        }
    }
}
@Composable
private fun ReportsRouter(
    screen: String,
    permissionViewModel: PermissionViewModel,
    hrReportViewModel: ReportViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onSafeNavigate: (String, PermissionAction?) -> Unit,
    onPaywallRequired: (PaywallModuleConfig) -> Unit,
    onGoBack: () -> Unit,
    onOpenModulesPanel: (String) -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    @Composable
    fun ReportsGuard(
        feature: String,
        action: PermissionAction = PermissionAction.VIEW,
        content: @Composable () -> Unit
    ) {
        FeatureGuard(
            module = "reports",
            feature = feature,
            action = action,
            permissionViewModel = permissionViewModel,
            upgradeKey = "reports",
            onUpgradeRequired = onPaywallRequired,
            onGoBack = onGoBack,
            content = content
        )
    }

    when (screen) {
        "reports_sales" -> ReportsGuard("orderMasterRegister", PermissionAction.VIEW) {
            SalesOrderReportsScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Reports") },
                onReportClick = { reportItem ->
                    val id = reportItem.id.lowercase()
                    val title = reportItem.title.lowercase()
                    when {
                        id == "leadmasterregister" || id.contains("lead_master") || title.contains("lead master") -> onSafeNavigate("reports_sales_lead_master", PermissionAction.VIEW)
                        id == "leadsourceperformance" || id.contains("lead_source") || title.contains("lead source") || title.contains("marketing roi") -> onSafeNavigate("reports_sales_lead_source", PermissionAction.VIEW)
                        id == "leadoverduefollowups" || id.contains("overdue_followup") || title.contains("overdue follow") -> onSafeNavigate("reports_sales_lead_overdue", PermissionAction.VIEW)
                        id == "leadfollowupactivity" || id.contains("followup_activity") || id.contains("follow_up") ||
                                title.contains("follow-up") || title.contains("follow up") || title.contains("followup") -> onSafeNavigate("reports_sales_lead_activity", PermissionAction.VIEW)
                        id == "oppmasterregister" || id.contains("opp_master") || title.contains("opportunity pipeline") || title.contains("opportunity master") -> onSafeNavigate("reports_sales_opp_master", PermissionAction.VIEW)
                        id == "oppstagefunnel" || id.contains("stage_funnel") || title.contains("stage funnel") || title.contains("funnel") -> onSafeNavigate("reports_sales_opp_funnel", PermissionAction.VIEW)
                        id == "oppclosingalerts" || id.contains("closing") || title.contains("closing soon") || title.contains("overdue deal") -> onSafeNavigate("reports_sales_opp_closing", PermissionAction.VIEW)
                        id == "custmasterregister" || id.contains("cust_master") || id.contains("customer_master") || title.contains("customer master") -> onSafeNavigate("reports_sales_cust_master", PermissionAction.VIEW)
                        id == "custltvrevenue" || id.contains("ltv") || title.contains("lifetime value") || title.contains("ltv") -> onSafeNavigate("reports_sales_cust_ltv", PermissionAction.VIEW)
                        id == "custcreditreceivables" || id.contains("credit") || id.contains("dues") || id.contains("receivable") ||
                                title.contains("credit") || title.contains("dues") || title.contains("receivable") -> onSafeNavigate("reports_sales_cust_credit", PermissionAction.VIEW)
                        id == "custdormantwinback" || id.contains("dormant") || id.contains("winback") ||
                                title.contains("dormant") || title.contains("win-back") || title.contains("win back") -> onSafeNavigate("reports_sales_cust_dormant", PermissionAction.VIEW)
                        id == "measmasterregister" || id.contains("meas_master") || title.contains("measurement master") -> onSafeNavigate("reports_sales_meas_master", PermissionAction.VIEW)
                        id == "measstaleaudit" || id.contains("stale") || title.contains("stale") || title.contains("outdated measurement") -> onSafeNavigate("reports_sales_meas_stale", PermissionAction.VIEW)
                        id == "ordermasterregister" || id.contains("order_master") || title.contains("order master") -> onSafeNavigate("reports_sales_order_master", PermissionAction.VIEW)
                        id == "orderproductstream" || id.contains("product_stream") || title.contains("product stream") -> onSafeNavigate("reports_sales_order_stream", PermissionAction.VIEW)
                        id == "orderdeliverysla" || id.contains("delivery_sla") || title.contains("delivery sla") || title.contains("on-time") -> onSafeNavigate("reports_sales_order_sla", PermissionAction.VIEW)
                        id == "branchwisesales" || id.contains("branch") || title.contains("branch-wise") || title.contains("branch wise") -> onSafeNavigate("reports_sales_branch_wise", PermissionAction.VIEW)
                        id == "quotemasterregister" || id.contains("quote_master") || title.contains("quotation master") -> onSafeNavigate("reports_sales_quote_master", PermissionAction.VIEW)
                        id == "quotediscountleakage" || id.contains("discount") || id.contains("leakage") ||
                                title.contains("discount") || title.contains("leakage") || title.contains("margin audit") -> onSafeNavigate("reports_sales_quote_discount", PermissionAction.VIEW)
                        id == "quoteexpiringaudit" || id.contains("expiring") || title.contains("expiring") -> onSafeNavigate("reports_sales_quote_expiring", PermissionAction.VIEW)
                        else -> onShowComingSoon("Coming Soon, Stay tuned!")
                    }
                }
            )
        }

        "reports_sales_lead_master" -> ReportsGuard("leadMasterRegister", PermissionAction.VIEW) { LeadMasterRegisterReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_lead_source" -> ReportsGuard("leadSourcePerformance", PermissionAction.VIEW) { LeadSourcePerformanceReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_lead_overdue" -> ReportsGuard("leadOverdueFollowups", PermissionAction.VIEW) { LeadOverdueFollowupsReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_lead_activity" -> ReportsGuard("leadFollowupActivity", PermissionAction.VIEW) { LeadFollowupActivityReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_sales_opp_master" -> ReportsGuard("oppMasterRegister", PermissionAction.VIEW) { OpportunityMasterRegisterReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_opp_funnel" -> ReportsGuard("oppStageFunnel", PermissionAction.VIEW) { OpportunityStageFunnelReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_opp_closing" -> ReportsGuard("oppClosingAlerts", PermissionAction.VIEW) { OpportunityClosingAlertsReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_sales_cust_master" -> ReportsGuard("custMasterRegister", PermissionAction.VIEW) { CustomerMasterDirectoryReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_cust_ltv" -> ReportsGuard("custLtvRevenue", PermissionAction.VIEW) { CustomerLtvRevenueReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_cust_credit" -> ReportsGuard("custCreditReceivables", PermissionAction.VIEW) { CustomerCreditReceivablesReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_cust_dormant" -> ReportsGuard("custDormantWinback", PermissionAction.VIEW) { CustomerDormantWinbackReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_sales_meas_master" -> ReportsGuard("measMasterRegister", PermissionAction.VIEW) { MeasurementMasterRegisterReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_meas_stale" -> ReportsGuard("measStaleAudit", PermissionAction.VIEW) { MeasurementStaleAuditReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_sales_order_master" -> ReportsGuard("orderMasterRegister", PermissionAction.VIEW) { OrderMasterRegisterReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_order_stream" -> ReportsGuard("orderProductStream", PermissionAction.VIEW) { OrderProductStreamReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_order_sla" -> ReportsGuard("orderDeliverySla", PermissionAction.VIEW) { DeliverySlaReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_branch_wise" -> ReportsGuard("branchWiseSales", PermissionAction.VIEW) { BranchWiseSalesReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_sales_quote_master" -> ReportsGuard("quoteMasterRegister", PermissionAction.VIEW) { QuotationMasterRegisterReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_quote_discount" -> ReportsGuard("quoteDiscountLeakage", PermissionAction.VIEW) { QuotationDiscountLeakageReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sales_quote_expiring" -> ReportsGuard("quoteExpiringAudit", PermissionAction.VIEW) { QuotationExpiringAuditReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_marketing" -> ReportsGuard("mktCampaignPerformance", PermissionAction.VIEW) {
            MarketingReportsScreen(onClose = onGoBack, onBreadCrumbClick = { onOpenModulesPanel("Reports") })
        }

        "reports_finance" -> ReportsGuard("revenueExpenseSummary", PermissionAction.VIEW) {
            FinanceReportsScreen(onClose = onGoBack, onBreadCrumbClick = { onOpenModulesPanel("Reports") })
        }

        "reports_inventory" -> ReportsGuard("invStockSummary", PermissionAction.VIEW) {
            InventoryReportsScreen(onClose = onGoBack, onBreadCrumbClick = { onOpenModulesPanel("Reports") })
        }

        "reports_logistics" -> ReportsGuard("logDeliveryPerformance", PermissionAction.VIEW) {
            LogisticsReportsScreen(onClose = onGoBack, onBreadCrumbClick = { onOpenModulesPanel("Reports") })
        }

        "reports_services" -> ReportsGuard("servicePerformanceSla", PermissionAction.VIEW) {
            ServiceReportsScreen(onClose = onGoBack, onBreadCrumbClick = { onOpenModulesPanel("Reports") })
        }

        "reports_human_resource" -> ReportsGuard("hrAttendanceRegister", PermissionAction.VIEW) {
            HrPayrollReportsScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Reports") },
                onReportClick = { reportItem ->
                    when (reportItem.id) {
                        "hr_attendance_register" -> onSafeNavigate("reports_hr_attendance_register", PermissionAction.VIEW)
                        "hr_payroll_run_audit" -> onSafeNavigate("reports_hr_payroll_run_audit", PermissionAction.VIEW)
                        "hr_piece_rate_wage" -> onSafeNavigate("reports_hr_piece_rate_wage", PermissionAction.VIEW)
                        "hr_reimbursement_claims" -> onSafeNavigate("reports_hr_reimbursement_claims", PermissionAction.VIEW)
                        else -> onShowComingSoon("Coming Soon, Stay tuned!")
                    }
                }
            )
        }

        "reports_hr_attendance_register" -> ReportsGuard("hrAttendanceRegister", PermissionAction.VIEW) { AttendanceReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_hr_payroll_run_audit" -> ReportsGuard("hrPayrollRuns", PermissionAction.VIEW) { PayrollRunReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_hr_piece_rate_wage" -> ReportsGuard("hrPieceRates", PermissionAction.VIEW) { PieceRateReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_hr_reimbursement_claims" -> ReportsGuard("hrReimbursements", PermissionAction.VIEW) { ReimbursementReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_it" -> ReportsGuard("itAssetRegister", PermissionAction.VIEW) {
            ItReportsScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Reports") },
                onReportClick = { reportItem ->
                    val id = reportItem.id.lowercase()
                    val title = reportItem.title.lowercase()
                    when {
                        id in setOf("it_asset_register", "it_assets", "it_asset_report") ||
                                title.contains("asset") || title.contains("hardware") -> onSafeNavigate("reports_it_assets", PermissionAction.VIEW)
                        id in setOf("it_service_desk", "it_service_desk_report", "it_sla_report") ||
                                title.contains("service desk") || title.contains("ticket") || title.contains("sla") -> onSafeNavigate("reports_it_service_desk", PermissionAction.VIEW)
                        else -> onShowComingSoon("Coming Soon, Stay tuned!")
                    }
                }
            )
        }

        "reports_it_assets" -> ReportsGuard("itAssetRegister", PermissionAction.VIEW) { ItAssetReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_it_service_desk" -> ReportsGuard("itServiceDesk", PermissionAction.VIEW) {
            ItServiceDeskReportScreen(
                onClose = onGoBack,
                onViewTicketAudit = { _ -> onSafeNavigate("it_service_desk", PermissionAction.VIEW) },
                viewModel = hrReportViewModel
            )
        }

        "reports_security" -> ReportsGuard("secIncidentsAlerts", PermissionAction.VIEW) {
            SecurityReportsScreen(
                onClose = onGoBack,
                onBreadCrumbClick = { onOpenModulesPanel("Reports") },
                onReportClick = { reportItem ->
                    val id = reportItem.id.lowercase()
                    val title = reportItem.title.lowercase()
                    when {
                        id in setOf("sec_incidents_alerts", "security_incidents_alerts") ||
                                title.contains("incident") || title.contains("alert") -> onSafeNavigate("reports_sec_incidents_alerts", PermissionAction.VIEW)
                        id in setOf(
                            "sec_access_activity",
                            "security_access_activity",
                            "sec_access_permission",
                            "sec_access_permissions",
                            "security_access_permission"
                        ) || title.contains("access") || title.contains("permission") -> onSafeNavigate("reports_sec_access_activity", PermissionAction.VIEW)
                        id in setOf(
                            "sec_api_integrations",
                            "security_api_integrations",
                            "sec_api_integration",
                            "sec_api_security",
                            "security_api_security"
                        ) || title.contains("api") || title.contains("integration") -> onSafeNavigate("reports_sec_api_integrations", PermissionAction.VIEW)
                        else -> onShowComingSoon("Coming Soon, Stay tuned!")
                    }
                }
            )
        }

        "reports_sec_incidents_alerts" -> ReportsGuard("secIncidentsAlerts", PermissionAction.VIEW) { SecurityIncidentsReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }
        "reports_sec_access_activity" -> ReportsGuard("secAccessActivity", PermissionAction.VIEW) {
            AccessActivityReportScreen(
                onClose = onGoBack,
                onAuditLogClick = { _ -> onSafeNavigate("security_audit_logs", PermissionAction.VIEW) },
                viewModel = hrReportViewModel
            )
        }
        "reports_sec_api_integrations" -> ReportsGuard("secApiIntegrations", PermissionAction.VIEW) { ApiIntegrationSecurityReportScreen(onClose = onGoBack, viewModel = hrReportViewModel) }

        "reports_legal" -> ReportsGuard("legalContractsExpiry", PermissionAction.VIEW) {
            LegalReportsScreen(onClose = onGoBack, onBreadCrumbClick = { onOpenModulesPanel("Reports") })
        }
    }
}
