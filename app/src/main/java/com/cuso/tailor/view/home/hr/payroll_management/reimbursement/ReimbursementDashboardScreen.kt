@file:Suppress("unused")

package com.cuso.tailor.view.home.hr.payroll_management.reimbursement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar

// -----------------------------------------------------------------------------
// Data Classes for Dashboard Screen
// -----------------------------------------------------------------------------

data class MetricCardData(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val value: String,
    val label: String,
    val subText: String,
    val badgeText: String? = null,
    val badgeBg: Color? = null,
    val badgeTextTint: Color? = null,
    val subTextColor: Color = TextSecondary
)

data class ActionRequiredItem(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val actionText: String
)

data class RecentClaimItem(
    val claimId: String,
    val employee: String,
    val typeAndPeriod: String,
    val claimedAmount: String,
    val approvedAmount: String?,
    val status: String,
    val isPendingReview: Boolean
)

data class BonusBatchItem(
    val batchName: String,
    val type: String,
    val employeeCount: Int,
    val totalPayout: String,
    val approvalStatus: String,
    val paymentStatus: String
)

data class PendingApprovalItem(
    val batchName: String,
    val createdBy: String,
    val employeeCount: Int,
    val payout: String,
    val priority: String,
    val status: String
)

data class TimelineActivityItem(
    val dateText: String,
    val description: String,
    val dotColor: Color
)

// -----------------------------------------------------------------------------
// Main Composable Screen
// -----------------------------------------------------------------------------

@Composable
fun ReimbursementBonusDashboardScreen(
    onNavigateBack: () -> Unit = {},
    onManageClaimsClick: () -> Unit = {},
    onManageBonusClick: () -> Unit = {},
    onReviewBonusApprovalsClick: () -> Unit = {},
    onViewPayrollImpactClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Top App Bar
        TitleBar(
            title = "Reimbursements & Bonus",
            onClose = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card with Period Dropdown
            item {
                DashboardHeaderSection()
            }

            // Top 6 Metrics Grid
            item {
                TopMetricsSection()
            }

            // Section 1: Modules Overview Cards
            item {
                Text(
                    text = "MODULES",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                ModuleSummaryCard(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "Reimbursement Claims",
                    subtitle = "Expense reviews & policy checks",
                    description = "Review employee expense claims, supporting documents, policy eligibility and approved reimbursement amounts.",
                    row1Label = "Pending Review",
                    row1Value = "18 Claims",
                    row2Label = "Pending Payment",
                    row2Value = "12 Claims",
                    row3Label = "Total Pending Value",
                    row3Value = "₹2,46,800",
                    actionText = "Manage Claims",
                    onActionClick = onManageClaimsClick
                )
            }

            item {
                ModuleSummaryCard(
                    icon = Icons.Default.CardGiftcard,
                    title = "Bonus & Incentives",
                    subtitle = "Batch creations & payroll prep",
                    description = "Create and calculate employee bonus and incentive batches before submitting them for approval.",
                    row1Label = "Draft Batches",
                    row1Value = "4 Batches",
                    row2Label = "Pending Approval",
                    row2Value = "8 Batches",
                    row3Label = "Planned Payout",
                    row3Value = "₹18,72,500",
                    actionText = "Manage Bonus & Incentives",
                    onActionClick = onManageBonusClick
                )
            }

            item {
                ModuleSummaryCard(
                    icon = Icons.Default.CheckCircleOutline,
                    title = "Bonus Approval",
                    subtitle = "Queue management & checks",
                    description = "Review submitted bonus batches, validate calculations and approve or return them for changes.",
                    row1Label = "Pending Approval",
                    row1Value = "5 Batches",
                    row2Label = "In Review",
                    row2Value = "3 Batches",
                    row3Label = "Approval Value",
                    row3Value = "₹12,48,000",
                    actionText = "Review Bonus Approvals",
                    onActionClick = onReviewBonusApprovalsClick
                )
            }

            // Section 2: Current Payroll Workflow
            item {
                CurrentPayrollWorkflowCard()
            }

            // Section 3: Action Required List
            item {
                ActionRequiredSection()
            }

            // Section 4: Reimbursement Summary Overview
            item {
                ReimbursementOverviewCard(onViewAllClick = onManageClaimsClick)
            }

            // Section 5: Recent Claims Data Table
            item {
                RecentClaimsTableSection(onViewAllClick = onManageClaimsClick)
            }

            // Section 6: Bonus & Incentive Batches Data Table
            item {
                BonusBatchesTableSection(onViewAllClick = onManageBonusClick)
            }

            // Section 7: Pending Bonus Approvals Data Table
            item {
                PendingBonusApprovalsTableSection(onViewAllClick = onReviewBonusApprovalsClick)
            }

            // Section 8: September Payroll Impact
            item {
                PayrollImpactCard(onViewPayrollClick = onViewPayrollImpactClick)
            }

            // Section 9: Upcoming Payroll Payouts Card
            item {
                UpcomingPayrollPayoutsCard(onViewPayrollClick = onViewPayrollImpactClick)
            }

            // Section 10: Quick Action Buttons (2x2 Grid)
            item {
                QuickActionButtonsSection()
            }

            // Section 11: Recent Activity Timeline
            item {
                RecentActivityTimelineCard()
            }

            // Bottom Export Summary Button
            item {
                Button(
                    onClick = { /* Export action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Summary",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Export Summary",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Dashboard Header Component
// -----------------------------------------------------------------------------

@Composable
private fun DashboardHeaderSection() {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Reimbursements & Bonus Dashboard",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Text(
            text = "Manage employee reimbursement claims, incentive payouts and bonus approvals from one workspace.",
            fontSize = tokens.bodySmall,
            color = TextSecondary,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(Modifier.height(4.dp))

        // Month Selection Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { /* Select month callback */ }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Month",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "September 2026",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Top 6 Metrics Grid Component
// -----------------------------------------------------------------------------

@Composable
private fun TopMetricsSection() {
    val metrics = remember {
        listOf(
            MetricCardData(
                icon = Icons.Default.AttachMoney,
                iconBg = Color(0xFFEDE9FE),
                iconTint = Color(0xFF6366F1),
                value = "18",
                label = "Pending Claims",
                subText = "₹2,46,800",
                badgeText = "Requires Review",
                badgeBg = orangeBg,
                badgeTextTint = orangeText,
                subTextColor = Primary
            ),
            MetricCardData(
                icon = Icons.Default.CheckCircleOutline,
                iconBg = greenBg,
                iconTint = greentext,
                value = "42",
                label = "Approved Claims",
                subText = "₹6,84,500",
                badgeText = "Sep 2026",
                badgeBg = modelBg,
                badgeTextTint = TextSecondary
            ),
            MetricCardData(
                icon = Icons.Default.Layers,
                iconBg = light_blue_border,
                iconTint = Primary,
                value = "8",
                label = "Pending Batches",
                subText = "264 Employees"
            ),
            MetricCardData(
                icon = Icons.Default.NotificationsNone,
                iconBg = orangeBg,
                iconTint = orangeText,
                value = "5",
                label = "Awaiting Approval",
                subText = "₹12,48,000",
                subTextColor = orangeText
            ),
            MetricCardData(
                icon = Icons.Default.AccountBalanceWallet,
                iconBg = greenBg,
                iconTint = greentext,
                value = "₹18.72L",
                label = "Approved Bonus",
                subText = "Final planned val",
                badgeText = "Sep 2026",
                badgeBg = modelBg,
                badgeTextTint = TextSecondary
            ),
            MetricCardData(
                icon = Icons.Default.Shield,
                iconBg = redBg,
                iconTint = redText,
                value = "9",
                label = "Requires Attention",
                subText = "Critical pending",
                badgeText = "Action",
                badgeBg = redBg,
                badgeTextTint = redText,
                subTextColor = redText
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.chunked(2).forEach { rowList ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowList.forEach { metric ->
                    MetricCardItem(
                        data = metric,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCardItem(
    data: MetricCardData,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp) // Tight vertical spacing between items
        ) {
            // Top Row: Icon Container and Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp), // Minimal bottom gap after top header
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon Container
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(data.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        tint = data.iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Trailing Badge Pill
                if (data.badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(data.badgeBg ?: modelBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = data.badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = data.badgeTextTint ?: TextSecondary,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            // Metric Value (e.g., "18", "42", "₹18.72L")
            Text(
                text = data.value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 16.sp
            )

            // Secondary Label (e.g., "Pending Claims", "Approved Claims")
            Text(
                text = data.label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            // Subtitle / Amount (e.g., "₹2,46,800", "264 Employees")
            Text(
                text = data.subText,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = data.subTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Module Summary Card Component
// -----------------------------------------------------------------------------

@Composable
private fun ModuleSummaryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    description: String,
    row1Label: String,
    row1Value: String,
    row2Label: String,
    row2Value: String,
    row3Label: String,
    row3Value: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(light_blue_border),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
            }

            // Description
            Text(
                text = description,
                fontSize = tokens.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal
            )

            HorizontalDivider(color = modelBorder, thickness = 1.dp)

            // 3 Rows Info
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SummaryKeyValueRow(label = row1Label, value = row1Value)
                SummaryKeyValueRow(label = row2Label, value = row2Value)
                SummaryKeyValueRow(
                    label = row3Label,
                    value = row3Value,
                    valueColor = Primary
                )
            }

            Spacer(Modifier.height(4.dp))

            // Action Navigation Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onActionClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = actionText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryKeyValueRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

// -----------------------------------------------------------------------------
// Current Payroll Workflow Component
// -----------------------------------------------------------------------------

@Composable
private fun CurrentPayrollWorkflowCard() {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Current Payroll Workflow",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            // Workflow 1: Reimbursements
            Text(
                text = "REIMBURSEMENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkflowPill(text = "Sub: 18", bg = modelBg, tint = TextPrimary)
                WorkflowChevron()
                WorkflowPill(text = "Review: 11", bg = orangeBg, tint = orangeText)
                WorkflowChevron()
                WorkflowPill(text = "Apprv: 42", bg = greenBg, tint = greentext)
                WorkflowChevron()
                WorkflowPill(text = "Pending: 12", bg = Color(0xFFF3E8FF), tint = Color(0xFF7E22CE))
            }

            Spacer(Modifier.height(4.dp))

            // Workflow 2: Bonus & Incentives
            Text(
                text = "BONUS & INCENTIVES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkflowPill(text = "Draft: 4", bg = modelBg, tint = TextPrimary)
                WorkflowChevron()
                WorkflowPill(text = "Sub: 8", bg = modelBg, tint = TextPrimary)
                WorkflowChevron()
                WorkflowPill(text = "Apprvl: 5", bg = light_blue_border, tint = Primary)
                WorkflowChevron()
                WorkflowPill(text = "Apprv: 21", bg = greenBg, tint = greentext)
                WorkflowChevron()
                WorkflowPill(text = "Paid", bg = Color(0xFFF3E8FF), tint = Color(0xFF7E22CE))
            }
        }
    }
}

@Composable
private fun WorkflowPill(text: String, bg: Color, tint: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = tint,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun WorkflowChevron() {
    Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = TextSecondary,
        modifier = Modifier.size(14.dp)
    )
}

// -----------------------------------------------------------------------------
// Action Required Section
// -----------------------------------------------------------------------------

@Composable
private fun ActionRequiredSection() {
    val tokens = LocalAppTokens.current

    val actionItems = remember {
        listOf(
            ActionRequiredItem(
                icon = Icons.Default.Description,
                iconBg = redBg,
                iconTint = redText,
                title = "Reimbursement Documents Missing",
                subtitle = "3 Claims awaiting receipt uploads",
                actionText = "Review Claims"
            ),
            ActionRequiredItem(
                icon = Icons.Default.WarningAmber,
                iconBg = orangeBg,
                iconTint = orangeText,
                title = "Policy Limit Exceeded",
                subtitle = "2 Claims cross business travel caps",
                actionText = "Review Claims"
            ),
            ActionRequiredItem(
                icon = Icons.Default.Edit,
                iconBg = light_blue_border,
                iconTint = Primary,
                title = "Bonus Manual Adjustments",
                subtitle = "2 Batches with override variances",
                actionText = "Review Bonus"
            ),
            ActionRequiredItem(
                icon = Icons.Default.NotificationsNone,
                iconBg = redBg,
                iconTint = redText,
                title = "Bonus Approval Pending",
                subtitle = "1 High Priority Batch needs approval",
                actionText = "Review Approval"
            ),
            ActionRequiredItem(
                icon = Icons.Default.Settings,
                iconBg = modelBg,
                iconTint = TextSecondary,
                title = "Payroll Mapping Issue",
                subtitle = "1 Bonus Batch lacks GL output code",
                actionText = "Review"
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Action Required",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = redBg
                ) {
                    Text(
                        text = "9 Items Need Attention",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = redText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = modelBorder)

            // Items List
            actionItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(item.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = item.actionText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary,
                        modifier = Modifier.clickable { /* Handle action */ }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Reimbursement Overview Breakdown Component
// -----------------------------------------------------------------------------

@Composable
private fun ReimbursementOverviewCard(onViewAllClick: () -> Unit) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reimbursement Overview",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewAllClick() }
                ) {
                    Text(
                        text = "View All Claims",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            HorizontalDivider(color = modelBorder)

            SummaryKeyValueRow(label = "Total Claims", value = "64 Claims")
            SummaryKeyValueRow(label = "Submitted (Review Queue)", value = "18 Claims", valueColor = orangeText)
            SummaryKeyValueRow(label = "Approved This Month", value = "42 Claims", valueColor = greentext)
            SummaryKeyValueRow(label = "Rejected / Returned", value = "6 Claims", valueColor = redText)
            SummaryKeyValueRow(label = "Pending Payment", value = "12 Claims", valueColor = Color(0xFF7E22CE))
            SummaryKeyValueRow(label = "Paid", value = "30 Claims")
        }
    }
}

// -----------------------------------------------------------------------------
// Recent Claims Horizontal Data Table
// -----------------------------------------------------------------------------

@Composable
private fun RecentClaimsTableSection(onViewAllClick: () -> Unit) {
    val tokens = LocalAppTokens.current

    val claims = remember {
        listOf(
            RecentClaimItem("RC-2026-0148", "Anita Sharma", "Travel • Sep 2026", "₹8,400", null, "Pending Review", true),
            RecentClaimItem("RC-2026-0145", "Rahul Verma", "Medical • Sep 2026", "₹12,800", "₹10,000", "Partially Approved", false),
            RecentClaimItem("RC-2026-0142", "Priya Nair", "Client Expense • Sep", "₹6,250", "₹6,250", "Approved", false),
            RecentClaimItem("RC-2026-0138", "Sneha Iyer", "Travel • Sep 2026", "₹4,800", "₹4,800", "Pending Payment", false)
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recent Claims",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Text(
                        text = "Claims submitted across teams",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewAllClick() }
                ) {
                    Text(
                        text = "View All",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Text(
                text = "← Swipe horizontally to view full table →",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = mutedText
            )

            // Horizontal Scrollable Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("CLAIM ID", 110.dp)
                        TableHeaderCell("EMPLOYEE", 120.dp)
                        TableHeaderCell("TYPE & PERIOD", 130.dp)
                        TableHeaderCell("CLAIMED", 90.dp)
                        TableHeaderCell("APPROVED", 90.dp)
                        TableHeaderCell("STATUS", 120.dp)
                        TableHeaderCell("ACTION", 80.dp, Alignment.Center)
                    }

                    HorizontalDivider(color = BorderGray)

                    // Data Rows
                    claims.forEach { claim ->
                        Row(
                            modifier = Modifier
                                .background(whiteBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(claim.claimId, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = TextSecondary, modifier = Modifier.width(110.dp))
                            Text(claim.employee, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.width(120.dp))
                            Text(claim.typeAndPeriod, fontSize = 11.sp, fontWeight = FontWeight.Normal, color = TextSecondary, modifier = Modifier.width(130.dp))
                            Text(claim.claimedAmount, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.width(90.dp))
                            Text(claim.approvedAmount ?: "—", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (claim.approvedAmount != null) greentext else TextSecondary, modifier = Modifier.width(90.dp))

                            Box(modifier = Modifier.width(120.dp)) {
                                TableStatusChip(status = claim.status)
                            }

                            Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                                if (claim.isPendingReview) {
                                    Button(
                                        onClick = { /* Review */ },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { /* View */ },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, BorderGray),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("View", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = modelBorder)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Bonus & Incentive Batches Data Table
// -----------------------------------------------------------------------------

@Composable
private fun BonusBatchesTableSection(onViewAllClick: () -> Unit) {
    val tokens = LocalAppTokens.current

    val batches = remember {
        listOf(
            BonusBatchItem("Q2 Performance", "Performance • 86 Employees", 86, "₹8,42,000", "Pending Approval", "Not Processed"),
            BonusBatchItem("August Sales", "Sales • 32 Employees", 32, "₹3,18,500", "In Review", "Not Processed"),
            BonusBatchItem("Attendance Incentive", "Attendance • 64 Employees", 64, "₹1,92,000", "Changes Requested", "Not Processed"),
            BonusBatchItem("Festival Bonus", "Festival • 248 Employees", 248, "₹24,80,000", "Draft", "Not Processed")
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bonus & Incentive Batches",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Text(
                        text = "Current cycle batch statuses",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewAllClick() }
                ) {
                    Text(
                        text = "View All Batches",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Text(
                text = "← Swipe horizontally to view full table →",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = mutedText
            )

            // Horizontal Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("BATCH & TYPE", 160.dp)
                        TableHeaderCell("EMPLOYEES", 90.dp, Alignment.Center)
                        TableHeaderCell("TOTAL PAYOUT", 110.dp)
                        TableHeaderCell("APPROVAL STATUS", 140.dp)
                        TableHeaderCell("PAYMENT STATUS", 120.dp)
                        TableHeaderCell("ACTION", 80.dp, Alignment.Center)
                    }

                    HorizontalDivider(color = BorderGray)

                    batches.forEach { batch ->
                        Row(
                            modifier = Modifier
                                .background(whiteBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(160.dp)) {
                                Text(batch.batchName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(batch.type, fontSize = 10.sp, fontWeight = FontWeight.Normal, color = TextSecondary)
                            }

                            Text(batch.employeeCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center, modifier = Modifier.width(90.dp))
                            Text(batch.totalPayout, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.width(110.dp))

                            Box(modifier = Modifier.width(140.dp)) {
                                TableStatusChip(status = batch.approvalStatus)
                            }

                            Text(batch.paymentStatus, fontSize = 11.sp, fontWeight = FontWeight.Normal, color = TextSecondary, modifier = Modifier.width(120.dp))

                            Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "View",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary,
                                    modifier = Modifier.clickable { /* View details */ }
                                )
                            }
                        }
                        HorizontalDivider(color = modelBorder)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Pending Bonus Approvals Data Table
// -----------------------------------------------------------------------------

@Composable
private fun PendingBonusApprovalsTableSection(onViewAllClick: () -> Unit) {
    val tokens = LocalAppTokens.current

    val pendingList = remember {
        listOf(
            PendingApprovalItem("Q2 Performance", "By Ananya Iyer • 86 Employees", 86, "₹8,42,000", "High", "Pending"),
            PendingApprovalItem("August Sales", "By Rajesh Kumar • 32 Employees", 32, "₹3,18,500", "Medium", "In Review"),
            PendingApprovalItem("Retention Bonus", "By Ananya Iyer • 12 Employees", 12, "₹6,00,000", "High", "Pending")
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pending Bonus Approvals",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Text(
                        text = "Queue awaiting leadership sign-off",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewAllClick() }
                ) {
                    Text(
                        text = "View Queue",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Text(
                text = "← Swipe horizontally to view full table →",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = mutedText
            )

            // Horizontal Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("BATCH / PROGRAM", 170.dp)
                        TableHeaderCell("PAYOUT", 110.dp)
                        TableHeaderCell("PRIORITY", 90.dp)
                        TableHeaderCell("STATUS", 90.dp)
                        TableHeaderCell("ACTION", 80.dp, Alignment.Center)
                    }

                    HorizontalDivider(color = BorderGray)

                    pendingList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .background(whiteBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(170.dp)) {
                                Text(item.batchName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(item.createdBy, fontSize = 10.sp, fontWeight = FontWeight.Normal, color = TextSecondary)
                            }

                            Text(item.payout, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.width(110.dp))

                            Box(modifier = Modifier.width(90.dp)) {
                                PriorityChip(priority = item.priority)
                            }

                            Box(modifier = Modifier.width(90.dp)) {
                                TableStatusChip(status = item.status)
                            }

                            Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                                Button(
                                    onClick = { /* Review */ },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                }
                            }
                        }
                        HorizontalDivider(color = modelBorder)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Payroll Impact & Upcoming Payouts Cards
// -----------------------------------------------------------------------------

@Composable
private fun PayrollImpactCard(onViewPayrollClick: () -> Unit) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "September Payroll Impact",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewPayrollClick() }
                ) {
                    Text(
                        text = "View Payroll",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            HorizontalDivider(color = modelBorder)

            SummaryKeyValueRow(label = "Approved Reimbursements", value = "₹6,84,500")
            SummaryKeyValueRow(label = "Approved Bonus Payout", value = "₹18,72,500")
            SummaryKeyValueRow(label = "Total Additional Payout", value = "₹25,57,000", valueColor = Primary)
            SummaryKeyValueRow(label = "Employees Affected", value = "286 Employees")
            SummaryKeyValueRow(label = "Pending Payroll Mapping", value = "19 Items", valueColor = orangeText)
            SummaryKeyValueRow(label = "Payroll Status", value = "Not Processed")
        }
    }
}

@Composable
private fun UpcomingPayrollPayoutsCard(onViewPayrollClick: () -> Unit) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Payroll Payouts",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewPayrollClick() }
                ) {
                    Text(
                        text = "View Payroll",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            HorizontalDivider(color = modelBorder)

            SummaryKeyValueRow(label = "Payroll Cycle", value = "September 2026 Payroll")
            SummaryKeyValueRow(label = "Reimbursements Share", value = "₹6,84,500")
            SummaryKeyValueRow(label = "Bonus & Incentives Share", value = "₹18,72,500")
            SummaryKeyValueRow(label = "Total Expected Payout", value = "₹25,57,000", valueColor = Primary)
            SummaryKeyValueRow(label = "Payment Method", value = "Bank Transfer via Payroll")
            SummaryKeyValueRow(label = "Expected Date", value = "30 Sep 2026")
        }
    }
}

// -----------------------------------------------------------------------------
// Quick Action Buttons Section
// -----------------------------------------------------------------------------

@Composable
private fun QuickActionButtonsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedActionButton(
                label = "+ Reimbursement",
                modifier = Modifier.weight(1f),
                onClick = { /* Add Reimbursement */ }
            )
            OutlinedActionButton(
                label = "+ Bonus/Incentive",
                modifier = Modifier.weight(1f),
                onClick = { /* Add Bonus */ }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedActionButton(
                label = "✓ Review Approvals",
                modifier = Modifier.weight(1f),
                onClick = { /* Review Approvals */ }
            )
            OutlinedActionButton(
                label = "Pending Items",
                modifier = Modifier.weight(1f),
                onClick = { /* Pending Items */ }
            )
        }
    }
}

@Composable
private fun OutlinedActionButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Recent Activity Timeline Component
// -----------------------------------------------------------------------------

@Composable
private fun RecentActivityTimelineCard() {
    val tokens = LocalAppTokens.current

    val activities = remember {
        listOf(
            TimelineActivityItem(
                dateText = "27 Aug • 11:24 AM",
                description = "Reimbursement claim RC-2026-0148 submitted — Anita Sharma",
                dotColor = Primary
            ),
            TimelineActivityItem(
                dateText = "27 Aug • 10:48 AM",
                description = "Q2 Performance Incentive submitted for approval — Ananya Iyer",
                dotColor = Primary
            ),
            TimelineActivityItem(
                dateText = "27 Aug • 10:15 AM",
                description = "Reimbursement RC-2026-0142 approved — Rajesh Kumar",
                dotColor = greentext
            ),
            TimelineActivityItem(
                dateText = "26 Aug • 05:20 PM",
                description = "August Sales Incentive moved to In Review — Rajesh Kumar",
                dotColor = Primary
            ),
            TimelineActivityItem(
                dateText = "26 Aug • 04:45 PM",
                description = "6 employee bonus adjustments added — Ananya Iyer",
                dotColor = Color(0xFF94A3B8)
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Card Title
            Text(
                text = "Recent Activity",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Spacer(Modifier.height(18.dp))

            // Timeline Items
            activities.forEachIndexed { index, act ->
                val isLast = index == activities.lastIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left Column: Dot + Continuous Vertical Connecting Line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(16.dp)
                    ) {
                        // Circular Dot
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(act.dotColor)
                        )

                        // Vertical Timeline Connecting Line
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .weight(1f)
                                    .background(BorderGray)
                            )
                        } else {
                            // Bottom stub line to match visual balance
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height(14.dp)
                                    .background(BorderGray)
                            )
                        }
                    }

                    // Right Column: Date + Description Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (isLast) 4.dp else 22.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = act.dateText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )

                        Text(
                            text = act.description,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Table Helper Components
// -----------------------------------------------------------------------------

@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    alignment: Alignment = Alignment.CenterStart
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun TableStatusChip(status: String) {
    val (bg, textColor) = when (status.lowercase()) {
        "approved", "apprv" -> Pair(greenBg, greentext)
        "pending review", "in review" -> Pair(orangeBg, orangeText)
        "partially approved", "pending approval", "pending" -> Pair(light_blue_border, Primary)
        "pending payment" -> Pair(Color(0xFFF3E8FF), Color(0xFF7E22CE))
        "changes requested" -> Pair(redBg, redText)
        else -> Pair(modelBg, TextSecondary)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun PriorityChip(priority: String) {
    val (bg, textColor) = when (priority.lowercase()) {
        "high" -> Pair(redBg, redText)
        "medium" -> Pair(orangeBg, orangeText)
        else -> Pair(greenBg, greentext)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = priority,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}