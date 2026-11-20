package com.cuso.tailor.view.home.hr.leave_management.leave_dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.LeaveUtilizationUiItem
import com.cuso.tailor.model.hr.MonthlyUsageTrendUiItem
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.activity_purple_bg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.purpleText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.DataCardProgressBar
import com.cuso.tailor.viewmodel.HrViewModel
import java.util.Locale

@Composable
fun LeaveDashboardScreen(
    hrViewModel: HrViewModel,
    onBack: () -> Unit,
    onApplyLeave: () -> Unit,
    onOpenApprovals: () -> Unit,
    onOpenLeaveBalances: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isLoading by hrViewModel.isLoadingLeaveDashboard.collectAsStateWithLifecycle()
    val error by hrViewModel.leaveDashboardError.collectAsStateWithLifecycle()
    val metrics by hrViewModel.leaveDashboardMetrics.collectAsStateWithLifecycle()
    val utilizationList by hrViewModel.leaveUtilizationList.collectAsStateWithLifecycle()
    val monthlyTrends by hrViewModel.monthlyLeaveTrends.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        hrViewModel.fetchLeaveDashboardData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
            .verticalScroll(rememberScrollState())
            .padding(tokens.screenPadding)
    ) {
        // Header Row: Title + Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Leave Dashboard",
                fontSize = tokens.h2,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(tokens.iconSize * 1.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize * 1.2f)
                )
            }
        }

        // Breadcrumb Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
        ) {
            Text(
                text = "HR",
                fontSize = tokens.bodySmall,
                color = mutedText
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = mutedText,
                modifier = Modifier.size(tokens.iconSize * 0.85f)
            )
            Text(
                text = "Leave Management",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        // Action Buttons Row (+ Apply Leave & Approvals)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Button(
                onClick = onApplyLeave,
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = whiteBg
                ),
                contentPadding = PaddingValues(horizontal = tokens.extraPadding)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
                Spacer(modifier = Modifier.width(tokens.extraPadding * 0.45f))
                Text(
                    text = "Apply Leave",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onOpenApprovals,
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                border = BorderStroke(1.dp, light_grey),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = whiteBg,
                    contentColor = TextPrimary
                ),
                contentPadding = PaddingValues(horizontal = tokens.extraPadding)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
                Spacer(modifier = Modifier.width(tokens.extraPadding * 0.45f))
                Text(
                    text = "Approvals",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = tokens.screenPadding * 2f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            if (!error.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = tokens.extraPadding),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                    color = whiteBg,
                    border = BorderStroke(1.dp, light_blue_border)
                ) {
                    Text(
                        text = error.orEmpty(),
                        color = redText,
                        fontSize = tokens.bodySmall,
                        modifier = Modifier.padding(tokens.cardPadding)
                    )
                }
            }

            // 2x2 KPI Grid
            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    DashboardKpiCard(
                        title = "Total Leaves",
                        value = formatKpiCount(metrics.totalLeaves),
                        valueColor = Primary,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardKpiCard(
                        title = "Approved",
                        value = formatKpiCount(metrics.approvedCount),
                        valueColor = greentext,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    DashboardKpiCard(
                        title = "Pending",
                        value = formatKpiCount(metrics.pendingCount),
                        // Replace with your Color.kt amber/orange token if one is defined
                        valueColor = purpleText,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardKpiCard(
                        title = "Rejected",
                        value = formatKpiCount(metrics.rejectedCount),
                        valueColor = redText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            // Leave Balance Overview Card
            LeaveBalanceOverviewCard(
                items = utilizationList,
                onViewAllClick = onOpenLeaveBalances
            )

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            // Monthly Usage Trend Card
            MonthlyUsageTrendCard(
                trends = monthlyTrends
            )
        }

        Spacer(modifier = Modifier.height(tokens.buttonHeight * 1.6f))
    }
}

@Composable
private fun DashboardKpiCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, light_grey),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = tokens.h2 * 1.25f,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun LeaveBalanceOverviewCard(
    items: List<LeaveUtilizationUiItem>,
    onViewAllClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewAllClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, light_grey),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leave Balance Overview",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "View All",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }

            if (items.isEmpty()) {
                Text(
                    text = "No leave balance allocations found.",
                    fontSize = tokens.bodySmall,
                    color = mutedText,
                    modifier = Modifier.padding(vertical = tokens.extraPadding * 0.5f)
                )
            } else {
                items.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.45f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                fontSize = tokens.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${item.used}/${item.total} days",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        DataCardProgressBar(
                            progress = item.progress,
                            progressColor = Primary,
                            trackColor = light_grey
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyUsageTrendCard(
    trends: List<MonthlyUsageTrendUiItem>
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, light_grey),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.cardPadding)
        ) {
            Text(
                text = "Monthly Usage Trend",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            if (trends.isEmpty()) {
                Text(
                    text = "No monthly trend data available.",
                    fontSize = tokens.bodySmall,
                    color = mutedText
                )
            } else {
                val chartHeight = tokens.buttonHeight * 2.4f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight + tokens.buttonHeight * 0.65f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    trends.forEach { item ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(chartHeight),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (item.heightFraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .width(tokens.iconSize * 1.15f)
                                            .fillMaxHeight(item.heightFraction)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = tokens.cardCornerRadius * 0.35f,
                                                    topEnd = tokens.cardCornerRadius * 0.35f
                                                )
                                            )
                                            .background(
                                                if (item.isCurrentMonth) Primary else activity_purple_bg
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (item.isCurrentMonth) Primary else light_blue_border,
                                                shape = RoundedCornerShape(
                                                    topStart = tokens.cardCornerRadius * 0.35f,
                                                    topEnd = tokens.cardCornerRadius * 0.35f
                                                )
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.55f))

                            Text(
                                text = item.monthLabel,
                                fontSize = tokens.caption,
                                fontWeight = if (item.isCurrentMonth) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (item.isCurrentMonth) Primary else mutedText,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatKpiCount(count: Int): String {
    return if (count in 1..9) {
        String.format(Locale.US, "%02d", count)
    } else {
        count.toString()
    }
}