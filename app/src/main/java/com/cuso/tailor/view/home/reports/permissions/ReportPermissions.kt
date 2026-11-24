package com.cuso.tailor.view.home.reports.permissions

/**
 * Centralized report permission keys matching Web ReportPermissions and ReportRoutes.
 */
object ReportPermissions {

    val SALES_REPORT_KEYS = listOf(
        "leadMasterRegister",
        "leadSourcePerformance",
        "leadOverdueFollowups",
        "leadFollowupActivity",
        "oppMasterRegister",
        "oppStageFunnel",
        "oppClosingAlerts",
        "quoteMasterRegister",
        "quoteDiscountLeakage",
        "quoteExpiringAudit",
        "custMasterRegister",
        "custLtvRevenue",
        "custCreditReceivables",
        "custDormantWinback",
        "measMasterRegister",
        "measPopularity",
        "measStaleAudit",
        "orderMasterRegister",
        "orderProductStream",
        "orderDeliverySla",
        "branchWiseSales"
    )

    val MARKETING_REPORT_KEYS = listOf(
        "mktCampaignPerformance",
        "mktChannelPerformance"
    )

    val FINANCE_REPORT_KEYS = listOf(
        "revenueExpenseSummary",
        "cashFlow",
        "receivablesAging",
        "payablesAging",
        "gstSummary",
        "paymentCollections",
        "advancePayments",
        "paymentModes",
        "balancePending",
        "overduePayments"
    )

    val INVENTORY_REPORT_KEYS = listOf(
        "invStockSummary",
        "invLowStockReorder",
        "invStockMovement",
        "invMaterialConsumption",
        "invValuation"
    )

    val LOGISTICS_REPORT_KEYS = listOf(
        "logDeliveryPerformance",
        "logShipmentTracking",
        "logReturnsRto",
        "logCodReconciliation"
    )

    val SERVICE_REPORT_KEYS = listOf(
        "customerFeedbackSummary",
        "complaintsEscalation",
        "servicePerformanceSla"
    )

    val HR_REPORT_KEYS = listOf(
        "hrAttendanceRegister",
        "hrPayrollRuns",
        "hrPieceRates",
        "hrReimbursements"
    )

    val IT_REPORT_KEYS = listOf(
        "itAssetRegister",
        "itServiceDesk",
        "itIntegrationSync",
        "itSystemBackup"
    )

    val SECURITY_REPORT_KEYS = listOf(
        "secIncidentsAlerts",
        "secAccessActivity",
        "secApiIntegrations"
    )

    val LEGAL_REPORT_KEYS = listOf(
        "legalContractsExpiry",
        "legalComplianceLicense",
        "legalCasesDisputes"
    )

    /**
     * Returns all permission keys belonging to a report hub route.
     */
    fun getCategoryKeysForRoute(route: String): List<String>? = when (route) {
        "reports_sales" -> SALES_REPORT_KEYS
        "reports_marketing" -> MARKETING_REPORT_KEYS
        "reports_finance" -> FINANCE_REPORT_KEYS
        "reports_inventory" -> INVENTORY_REPORT_KEYS
        "reports_logistics" -> LOGISTICS_REPORT_KEYS
        "reports_services" -> SERVICE_REPORT_KEYS
        "reports_human_resource" -> HR_REPORT_KEYS
        "reports_it" -> IT_REPORT_KEYS
        "reports_security" -> SECURITY_REPORT_KEYS
        "reports_legal" -> LEGAL_REPORT_KEYS
        else -> null
    }
}