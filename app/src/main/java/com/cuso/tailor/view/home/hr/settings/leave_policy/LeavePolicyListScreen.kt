@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.hr.settings.leave_policy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.LeavePolicyItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeavePoliciesScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onCreatePolicy: () -> Unit,
    onViewPolicy: (String) -> Unit,
    onEditPolicy: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val policies by viewModel.leavePolicies.collectAsState()
    val isLoading by viewModel.isLoadingLeavePolicies.collectAsState()
    val canLoadMore by viewModel.canLoadMoreLeavePolicies.collectAsState()
    val isLoadingMore by viewModel.isLoadingMoreLeavePolicies.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var policyToDelete by remember { mutableStateOf<LeavePolicyItemDto?>(null) }
    val listState = rememberLazyListState()

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.fetchLeavePolicies(page = 1)
    }

    // Pagination detection
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= totalItems - 2 && canLoadMore && !isLoadingMore && !isLoading
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMoreLeavePolicies()
        }
    }

    // KPI Metrics calculation
    val totalCount = policies.size
    val activeCount = remember(policies) { policies.count { it.status.equals("active", ignoreCase = true) } }
    val draftCount = remember(policies) { policies.count { !it.status.equals("active", ignoreCase = true) } }
    val totalEmployeesCovered = remember(policies) { policies.sumOf { it.employeesAssigned } }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Create Policy",
            icon = Icons.Default.Add,
            onClick = onCreatePolicy,
            alignment = Alignment.BottomEnd,
            bottomPadding = 80.dp,
            endPadding = 16.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            TitleBar(
                title = "Leave Policies",
                onClose = onBack
            )

            // Search Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.onLeavePolicySearchQueryChanged(it)
                },
                placeholder = "Search leave policy name, code...",
                showFilterIcon = false
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // KPI Metric Cards Grid
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PolicyKpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Total Policies",
                                value = totalCount.toString(),
                                icon = Icons.Default.Shield,
                                iconBg = light_grey,
                                iconTint = headerGrey
                            )
                            PolicyKpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Active Policies",
                                value = activeCount.toString(),
                                icon = Icons.Default.CheckCircle,
                                iconBg = greenBg,
                                iconTint = greentext,
                                hasIndicator = true,
                                indicatorColor = Color(0xFF10B981)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PolicyKpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Draft Policies",
                                value = draftCount.toString(),
                                icon = Icons.Default.Edit,
                                iconBg = yellowBg,
                                iconTint = yellowText,
                                hasIndicator = true,
                                indicatorColor = Color(0xFFF59E0B)
                            )
                            PolicyKpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Employees Covered",
                                value = totalEmployeesCovered.toString(),
                                icon = Icons.Default.People,
                                iconBg = primary_light,
                                iconTint = Primary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // Loading State
                if (isLoading && policies.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary, strokeWidth = 2.5.dp)
                        }
                    }
                } else if (policies.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = whiteBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No leave policies found",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                                Text(
                                    text = "Click 'Create Policy' to bundle leave types and define eligibility rules.",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText
                                )
                            }
                        }
                    }
                } else {
                    // Data Cards Listing
                    items(policies, key = { it.id }) { policy ->
                        val isActive = policy.status.equals("active", ignoreCase = true)
                        val applicableText = remember(policy.eligibility) {
                            val statuses = policy.eligibility?.employmentStatuses ?: emptyList()
                            if (statuses.isNotEmpty()) {
                                statuses.joinToString(", ") { s ->
                                    s.replace("-", " ")
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                }
                            } else {
                                "All Employees"
                            }
                        }

                        val effectiveDateStr = remember(policy.effectiveFrom) {
                            formatPolicyDate(policy.effectiveFrom)
                        }

                        DataCard(
                            item = policy,
                            title = policy.name,
                            code = policy.code.ifBlank { null },
                            subtitle = policy.description?.takeIf { it.isNotBlank() } ?: "No description provided.",
                            topBadgeText = if (isActive) "Active" else "Draft",
                            topBadgeTextColor = if (isActive) greentext else yellowText,
                            topBadgeBgColor = if (isActive) greenBg else yellowBg,
                            actions = listOf(
                                MenuAction(
                                    label = "View Details",
                                    icon = Icons.Default.Visibility,
                                    onClick = { onViewPolicy(policy.id) }
                                ),
                                MenuAction(
                                    label = "Edit Policy",
                                    icon = Icons.Default.Edit,
                                    onClick = { onEditPolicy(policy.id) }
                                ),
                                MenuAction(
                                    label = "Delete Policy",
                                    icon = Icons.Default.Delete,
                                    tint = redText,
                                    textColor = redText,
                                    onClick = { policyToDelete = policy }
                                )
                            ),
                            onClick = { onViewPolicy(policy.id) },
                            content = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                // Applicable Scope & Location
                                Column {
                                    Text(
                                        text = "APPLICABLE EMPLOYEES & LOCATION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = mutedText,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = applicableText,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (policy.eligibility?.includeProbationers == true) {
                                            "All Locations • Initial Tenure Eligible"
                                        } else {
                                            "All Locations"
                                        },
                                        fontSize = tokens.caption,
                                        color = mutedText
                                    )
                                }

                                // Configured Leave Types Chips
                                if (policy.leaveTypes.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = "CONFIGURED LEAVE TYPES",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = mutedText,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val displayed = policy.leaveTypes.take(3)
                                            val extraCount = policy.leaveTypes.size - displayed.size

                                            displayed.forEach { lt ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = primary_light,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
                                                ) {
                                                    Text(
                                                        text = "Leave Type",
                                                        fontSize = 11.sp,
                                                        color = Primary,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (extraCount > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = light_grey,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, grey_border)
                                                ) {
                                                    Text(
                                                        text = "+$extraCount more",
                                                        fontSize = 10.sp,
                                                        color = headerGrey,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = grey_border, thickness = 1.dp)

                                // Footer: Assigned count & Effective Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "EMPLOYEES ASSIGNED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = mutedText
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = if (policy.employeesAssigned > 0) "${policy.employeesAssigned}" else "0 Unassigned",
                                                fontSize = tokens.bodySmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "EFFECTIVE DATE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = mutedText
                                        )
                                        Text(
                                            text = effectiveDateStr,
                                            fontSize = tokens.bodySmall,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                    // Bottom info strip
                    item {
                        Surface(
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = primary_light.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Leave Policy Lifecycle & Accrual Logic",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = title_color
                                    )
                                    Text(
                                        text = "Leave Policies define how published Leave Types (entitlements, carry-forward, encashment) are grouped and assigned to employee groups without modifying historical records.",
                                        fontSize = tokens.caption,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (policyToDelete != null) {
        val policy = policyToDelete!!
        AlertDialog(
            onDismissRequest = { policyToDelete = null },
            title = {
                Text(
                    text = "Delete Leave Policy",
                    fontWeight = FontWeight.Bold,
                    fontSize = tokens.h2,
                    color = title_color
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${policy.name}\"? Employees assigned to this policy will no longer have this bundle active. This action cannot be undone.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = policy.id
                        policyToDelete = null
                        viewModel.deleteLeavePolicy(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redText),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Policy", color = whiteBg, fontSize = tokens.bodySmall)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { policyToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = TextPrimary, fontSize = tokens.bodySmall)
                }
            },
            containerColor = whiteBg,
            shape = RoundedCornerShape(tokens.cardCornerRadius)
        )
    }
}

@Composable
private fun PolicyKpiCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    hasIndicator: Boolean = false,
    indicatorColor: Color = Color.Transparent
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier.height(tokens.cardHeight * 0.72f),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = mutedText,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (hasIndicator) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(indicatorColor)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatPolicyDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "Not Set"
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val year = parts[0]
            val monthIdx = parts[1].toIntOrNull()?.minus(1) ?: 0
            val day = parts[2]
            "$day ${months.getOrElse(monthIdx) { "Jan" }} $year"
        } else {
            raw
        }
    } catch (_: Exception) {
        raw
    }
}