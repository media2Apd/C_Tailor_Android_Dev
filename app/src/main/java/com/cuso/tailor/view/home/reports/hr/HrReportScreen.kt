@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.hr

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate

@Composable
fun HrPayrollReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for HR & Payroll module
    val categories = remember {
        listOf(
            // Category 1: Time & Attendance Tracking (Image 1)
            ReportCategory(
                id = "time_attendance_tracking",
                title = "Time & Attendance Tracking",
                reports = listOf(
                    ReportItem(
                        id = "hr_attendance_register",
                        title = "Attendance Register & Analytics Report",
                        description = "Chronological audit of daily employee punches, late arrivals, overtime minutes, and attendance rates",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Payroll & Compensation Governance (Image 2)
            ReportCategory(
                id = "payroll_compensation_governance",
                title = "Payroll & Compensation Governance",
                reports = listOf(
                    ReportItem(
                        id = "hr_payroll_run_audit",
                        title = "Payroll Run Audit & Analytics Report",
                        description = "Audit monthly payroll run executions, salary assignment readiness, calculation statuses, and data locks",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "hr_piece_rate_wage",
                        title = "Piece Rate Master & Wage Report",
                        description = "Comprehensive benchmark ledger of workflow operations, garment tailoring piece rates, and overtime tariffs",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "hr_reimbursement_claims",
                        title = "Reimbursement Claims & Settlement Report",
                        description = "Track employee expense claims, manager approval statuses, reimbursement disbursements, and claim categories",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer options configured for HR & Payroll reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Department / Role",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Staff", isSelected = true),
                    FilterOption("tailoring", "Tailors & Masters"),
                    FilterOption("sales", "Sales Executives"),
                    FilterOption("admin", "Admin & Ops")
                )
            ),
            FilterSection(
                title = "Favorite Status",
                icon = Icons.Filled.Star,
                type = FilterSectionType.CHIP_ROW,
                options = listOf(
                    FilterOption("all", "All", isSelected = true),
                    FilterOption("starred", "Starred Only")
                )
            )
        )
    }

    // Render using the common reusable reports template
    ReportsScreenTemplate(
        title = "HR & Payroll Reports",
        searchPlaceholder = "Search attendance, payroll runs, piece rates, or reimbursement reports...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}