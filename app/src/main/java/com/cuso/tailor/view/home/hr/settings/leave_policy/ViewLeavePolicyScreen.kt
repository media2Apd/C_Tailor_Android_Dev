package com.cuso.tailor.view.home.hr.settings.leave_policy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ViewLeavePolicyScreen(
    policyId: String,
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onEditPolicy: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val policy by viewModel.selectedLeavePolicy.collectAsState()
    val isLoading by viewModel.isLoadingLeavePolicyDetail.collectAsState()

    LaunchedEffect(policyId) {
        viewModel.fetchLeavePolicyById(policyId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSelectedLeavePolicy() }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = policy?.name ?: "Leave Policy",
                onClose = onBack
            )
        },
        containerColor = Primary_background
    ) { padding ->
        if (isLoading || policy == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.5.dp)
            }
        } else {
            val item = policy!!
            val isActive = item.status.equals("active", ignoreCase = true)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Actions & Status Pill
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActive) greenBg else yellowBg
                        ) {
                            Text(
                                text = if (isActive) "Active Policy" else "Draft Policy",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) greentext else yellowText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = { onEditPolicy(item.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = whiteBg, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Policy", color = whiteBg, fontSize = tokens.bodySmall)
                        }
                    }
                }

                // Quick Metric Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = whiteBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("EFFECTIVE FROM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.effectiveFrom?.take(10) ?: "Not Set",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = whiteBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("EFFECTIVE UNTIL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mutedText)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.effectiveUntil?.take(10) ?: "Indefinite / Open",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                            }
                        }
                    }
                }

                // ── 01. Policy Information ──
                item {
                    DetailSectionCard(stepNumber = "01", title = "Policy Information") {
                        DetailRow(label = "Policy Name", value = item.name)
                        DetailRow(label = "Policy Code", value = item.code.ifBlank { "—" })
                        DetailRow(label = "Description", value = item.description?.takeIf { it.isNotBlank() } ?: "No description provided.")
                        DetailRow(label = "Publication Status", value = item.status.replaceFirstChar { it.uppercase() })
                    }
                }

                // ── 02. Employee Eligibility ──
                item {
                    DetailSectionCard(stepNumber = "02", title = "Employee Eligibility Criteria") {
                        val statuses = item.eligibility?.employmentStatuses ?: emptyList()
                        Column {
                            Text("EMPLOYMENT STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mutedText)
                            Spacer(Modifier.height(4.dp))
                            if (statuses.isEmpty()) {
                                Text("All Employees (No restriction)", fontSize = tokens.bodySmall, color = TextPrimary)
                            } else {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    statuses.forEach { st ->
                                        Surface(shape = RoundedCornerShape(12.dp), color = light_grey) {
                                            Text(
                                                text = st.replace("-", " ").replaceFirstChar { it.uppercase() },
                                                fontSize = 11.sp,
                                                color = TextPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        DetailRow(
                            label = "Include Probationers",
                            value = if (item.eligibility?.includeProbationers == true) "Yes" else "No"
                        )
                        DetailRow(
                            label = "Auto-evaluate upon clearance",
                            value = if (item.eligibility?.autoEvaluateOnProbationClearance == true) "Yes" else "No"
                        )
                    }
                }

                // ── 03. Included Leave Bundle ──
                item {
                    DetailSectionCard(
                        stepNumber = "03",
                        title = "Included Leave Bundle",
                        subtitle = "${item.leaveTypes.size} Configured Types"
                    ) {
                        if (item.leaveTypes.isEmpty()) {
                            Text("No leave types configured for this policy.", fontSize = tokens.caption, color = mutedText)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.leaveTypes.forEachIndexed { idx, lt ->
                                    val isOverridden = lt.isOverridden
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isOverridden) yellowBg else whiteBg,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isOverridden) Color(0xFFFDE68A) else sectionBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Leave Type #${idx + 1}",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = title_color
                                                    )
                                                    if (isOverridden) {
                                                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF3C7)) {
                                                            Text("Override", fontSize = 10.sp, color = yellowText, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "Quota: ${lt.entitlement?.quota ?: 0.0} ${lt.entitlement?.unit ?: "days"}",
                                                    fontSize = tokens.caption,
                                                    color = TextSecondary
                                                )
                                            }

                                            Text(
                                                text = lt.entitlement?.accrualFrequency?.replaceFirstChar { it.uppercase() } ?: "Yearly",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = headerGrey
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 04. Assignment Rules ──
                item {
                    DetailSectionCard(stepNumber = "04", title = "Assignment Rules & Governance") {
                        DetailRow(
                            label = "Assignment Mode",
                            value = if (item.assignment?.mode == "automatic") "Automatic by Eligibility" else "Manual Assignment"
                        )
                        DetailRow(
                            label = "Policy Priority Tier",
                            value = if (item.assignment?.priorityTier == 1) "Tier 1 (High Priority)" else "Tier 2 (Normal)"
                        )
                        DetailRow(
                            label = "Conflict Resolution",
                            value = if (item.assignment?.conflictResolution == "require_hr_review") "Require HR Review on Overlap" else "Higher Priority Wins"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    stepNumber: String,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stepNumber, color = whiteBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                    if (subtitle != null) {
                        Text(text = subtitle, fontSize = tokens.caption, color = mutedText)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = grey_border, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = tokens.bodySmall, color = TextPrimary, lineHeight = 16.sp)
    }
}