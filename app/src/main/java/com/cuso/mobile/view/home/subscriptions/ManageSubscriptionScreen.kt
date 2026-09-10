@file:Suppress("unused", "AssignedValueIsNeverRead")
package com.cuso.mobile.view.home.subscriptions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextPrimary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.orangeText
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction

private val upgradeStepLabels = listOf(
    "Choose Plan",
    "Select Cycle",
    "Review",
    "Payment",
    "Review"
)

/**
 * Root Controller Composable for Subscription Management and Upgrade/Downgrade Flow.
 */
@Composable
fun ManageSubscriptionFlowScreen(
    onClose: () -> Unit = {}
) {
    var isUpgrading by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedPlan by remember { mutableStateOf("PROFESSIONAL") }
    var isAnnualBilling by remember { mutableStateOf(true) }
    var couponCode by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("visa") }
    var isPaymentFailed by remember { mutableStateOf(false) }

    if (!isUpgrading) {
        ManageSubscriptionDashboard(
            onClose = onClose,
            onStartUpgrade = {
                currentStep = 0
                isPaymentFailed = false
                isUpgrading = true
            }
        )
    } else {
        UpgradeSubscriptionFlow(
            currentStep = currentStep,
            selectedPlan = selectedPlan,
            isAnnualBilling = isAnnualBilling,
            couponCode = couponCode,
            selectedPaymentMethod = selectedPaymentMethod,
            isPaymentFailed = isPaymentFailed,
            onClose = { isUpgrading = false },
            onPlanSelected = { selectedPlan = it },
            onCycleSelected = { isAnnualBilling = it },
            onCouponChanged = { couponCode = it },
            onPaymentMethodSelected = { selectedPaymentMethod = it },
            onContinueToStep = { nextStep -> currentStep = nextStep },
            onSimulateFailure = {
                isPaymentFailed = true
                currentStep = 4
            },
            onRetryPayment = {
                isPaymentFailed = false
                currentStep = 4
            },
            onFinishFlow = {
                isUpgrading = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// SCREEN 1: MANAGE SUBSCRIPTION DASHBOARD
// ─────────────────────────────────────────────────────────────

@Composable
fun ManageSubscriptionDashboard(
    onClose: () -> Unit,
    onStartUpgrade: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SubscriptionTopBar(
                title = "Manage Subscription",
                onClose = onClose
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Primary),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("Need Help", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Primary),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("Contact Billing", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Download Statement", color = whiteBg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Free Trial Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Primary,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Free Trial Active — 12 Days Remaining",
                            color = whiteBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Trial ends 06 Sep 2025. Choose a plan to continue uninterrupted.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Choose a Plan",
                            color = whiteBg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { onStartUpgrade() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Current Plan Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, grey_border, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CUSO TAILOR", color = Color(0xFF6B7280), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge(text = "FREE TRIAL", bgColor = yellowBg, textColor = Color(0xFFB45309))
                            Badge(text = "Active", bgColor = Color(0xFFDCFCE7), textColor = Color(0xFF15803D))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("STANDARD", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Price post trial: ₹9,999/mo", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Trial Started", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                            Spacer(Modifier.height(2.dp))
                            Text("25 Aug 2025", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Trial Ends", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                            Spacer(Modifier.height(2.dp))
                            Text("06 Sep 2025", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = grey_border)
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Compare All Plans",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartUpgrade() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Trial Progress Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, grey_border, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trial Progress", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("12", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                            Spacer(Modifier.width(4.dp))
                            Text("days left", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { 0.14f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Primary,
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Day 1", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text("Today (Day 2)", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Medium)
                        Text("Day 14", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(primary_light, RoundedCornerShape(6.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Day 1", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Day 5", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                        Text("Day 10", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                        Text("Day 15", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Upgrade before trial ends to retain all data and access.",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Plan Recommendation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, grey_border, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Plan Recommendation", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Order usage has reached 84%. Professional may be a better fit for your growing business.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFBEB), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "You have 12 of 15 users active. Adding more users will require an upgrade.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onStartUpgrade,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Primary),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Compare to Professional →", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // SUBSCRIPTION SUMMARY
            Text("SUBSCRIPTION SUMMARY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), letterSpacing = 0.5.sp)
            Spacer(Modifier.height(12.dp))

            SummaryGrid()

            Spacer(Modifier.height(24.dp))

            // USAGE SNAPSHOT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("USAGE SNAPSHOT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), letterSpacing = 0.5.sp)
                Text(
                    "View detailed Usage →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(Modifier.height(12.dp))

            UsageProgressBar("Users", 0.80f, "80%", orangeText, Color(0xFFF59E0B))
            UsageProgressBar("AI Credits", 0.15f, "15%", Color(0xFF10B981), Color(0xFF10B981))
            UsageProgressBar("Branches", 0.60f, "60%", orangeText, Color(0xFFF59E0B))
            UsageProgressBar("WhatsApp", 0.92f, "92%", Color(0xFFEF4444), Color(0xFFEF4444))
            UsageProgressBar("Storage", 0.72f, "72%", orangeText, Color(0xFFF59E0B))
            UsageProgressBar("SMS Messages", 0.76f, "76%", orangeText, Color(0xFFF59E0B))
            UsageProgressBar("Orders Completed", 0.84f, "84%", Color(0xFFEF4444), Color(0xFFEF4444))
            UsageProgressBar("API Calls", 0.14f, "14%", Color(0xFF10B981), Color(0xFF10B981))

            Spacer(Modifier.height(24.dp))

            // QUICK ACTIONS
            Text("QUICK ACTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), letterSpacing = 0.5.sp)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(modifier = Modifier.weight(1f), title = "View Invoices", icon = Icons.Default.Description)
                QuickActionCard(modifier = Modifier.weight(1f), title = "Payment Methods", icon = Icons.Default.CreditCard)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(modifier = Modifier.weight(1f), title = "Add-ons Available", icon = Icons.Default.Widgets)
                QuickActionCard(modifier = Modifier.weight(1f), title = "Subscription History", icon = Icons.Default.History)
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SCREEN 2: UPGRADE / DOWNGRADE STEPPER FLOW
// ─────────────────────────────────────────────────────────────

@Composable
fun UpgradeSubscriptionFlow(
    currentStep: Int,
    selectedPlan: String,
    isAnnualBilling: Boolean,
    couponCode: String,
    selectedPaymentMethod: String,
    isPaymentFailed: Boolean,
    onClose: () -> Unit,
    onPlanSelected: (String) -> Unit,
    onCycleSelected: (Boolean) -> Unit,
    onCouponChanged: (String) -> Unit,
    onPaymentMethodSelected: (String) -> Unit,
    onContinueToStep: (Int) -> Unit,
    onSimulateFailure: () -> Unit,
    onRetryPayment: () -> Unit,
    onFinishFlow: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                SubscriptionTopBar(
                    title = "Manage Subscription",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Upgrade / Downgrade",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Manage your CUSO Tailor subscription, usage, billing and plan.",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(Modifier.height(14.dp))

                    SubscriptionStepper(
                        stepLabels = upgradeStepLabels,
                        currentStep = currentStep
                    )
                }

                HorizontalDivider(color = grey_border)

                // Step Body Content with bottom padding for Floating Actions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp)
                ) {
                    when (currentStep) {
                        0 -> Step1ChoosePlan(
                            selectedPlan = selectedPlan,
                            onPlanSelected = onPlanSelected
                        )
                        1 -> Step2SelectCycle(
                            isAnnualBilling = isAnnualBilling,
                            onCycleSelected = onCycleSelected
                        )
                        2 -> Step3ReviewOrder(
                            couponCode = couponCode,
                            onCouponChanged = onCouponChanged
                        )
                        3 -> Step4Payment(
                            selectedPaymentMethod = selectedPaymentMethod,
                            onPaymentMethodSelected = onPaymentMethodSelected,
                            onSimulateFailure = onSimulateFailure
                        )
                        4 -> {
                            if (isPaymentFailed) {
                                Step5FailureScreen()
                            } else {
                                Step5SuccessScreen()
                            }
                        }
                    }
                }
            }

            // Floating Bottom Step Navigation Action
            StepNavigationFab(
                showBack = currentStep > 0,
                backLabel = when (currentStep) {
                    4 -> if (isPaymentFailed) "Update Payment" else "View Subscription"
                    else -> "Cancel"
                },
                showBackArrow = false,
                onBack = {
                    if (currentStep == 4) {
                        if (isPaymentFailed) onContinueToStep(3) else onFinishFlow()
                    } else {
                        onClose()
                    }
                },
                trailingAction = when (currentStep) {
                    0 -> TrailingFabAction.Next(label = "Continue") {
                        onContinueToStep(1)
                    }
                    1 -> TrailingFabAction.Next(label = "Review Order") {
                        onContinueToStep(2)
                    }
                    2 -> TrailingFabAction.Next(label = "Proceed to Payment") {
                        onContinueToStep(3)
                    }
                    3 -> TrailingFabAction.Update(label = "Pay ₹21,239") {
                        onContinueToStep(4)
                    }
                    4 -> {
                        if (isPaymentFailed) {
                            TrailingFabAction.Next(label = "Retry Payment") {
                                onRetryPayment()
                            }
                        } else {
                            TrailingFabAction.Next(label = "Go to Overview") {
                                onFinishFlow()
                            }
                        }
                    }
                    else -> null
                },
                showTrailingArrow = currentStep != 3
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 1: CHOOSE PLAN
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step1ChoosePlan(
    selectedPlan: String,
    onPlanSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Comparison Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primary_light, RoundedCornerShape(12.dp))
                .border(
                    BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("From:  STANDARD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                    Text("₹9,999/mo", fontSize = 11.sp, color = Color(0xFF6B7280))
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text("To:", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                    Text("PROFESSIONAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Text("₹19,999/mo", fontSize = 11.sp, color = Primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Plan 1: LITE
        PlanCard(
            title = "LITE",
            subtitle = "For small tailor businesses",
            price = "₹3,999/mo",
            badgeText = "Downgrade",
            badgeBg = yellowBg,
            badgeColor = Color(0xFFB45309),
            isSelected = selectedPlan == "LITE",
            onClick = { onPlanSelected("LITE") }
        )

        Spacer(Modifier.height(14.dp))

        // Plan 2: PROFESSIONAL
        PlanCard(
            title = "PROFESSIONAL",
            subtitle = "For multi-branch operations",
            price = "₹19,999/mo",
            badgeText = "Recommended",
            badgeBg = primary_light,
            badgeColor = Primary,
            isSelected = selectedPlan == "PROFESSIONAL",
            features = listOf(
                "Unlimited Corporate Orders",
                "Advanced AI Features",
                "Custom Reports",
                "50 Users",
                "20 Branches"
            ),
            onClick = { onPlanSelected("PROFESSIONAL") }
        )

        Spacer(Modifier.height(14.dp))

        // Plan 3: ENTERPRISE
        PlanCard(
            title = "ENTERPRISE",
            subtitle = "Custom pricing",
            price = "Custom/mo",
            isSelected = selectedPlan == "ENTERPRISE",
            onClick = { onPlanSelected("ENTERPRISE") }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 2: SELECT CYCLE
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step2SelectCycle(
    isAnnualBilling: Boolean,
    onCycleSelected: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Option 1: Monthly Billing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (!isAnnualBilling) 2.dp else 1.dp,
                    color = if (!isAnnualBilling) Primary else grey_border,
                    shape = RoundedCornerShape(14.dp)
                )
                .clip(RoundedCornerShape(14.dp))
                .clickable { onCycleSelected(false) }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Monthly Billing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Pay month-to-month. Cancel anytime.", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("₹19,999", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(" /month", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                RadioButton(
                    selected = !isAnnualBilling,
                    onClick = { onCycleSelected(false) },
                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Option 2: Annual Billing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isAnnualBilling) 2.dp else 1.dp,
                    color = if (isAnnualBilling) Primary else grey_border,
                    shape = RoundedCornerShape(14.dp)
                )
                .clip(RoundedCornerShape(14.dp))
                .clickable { onCycleSelected(true) }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Annual Billing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Badge(text = "Save 16%", bgColor = Color(0xFFDCFCE7), textColor = Color(0xFF15803D))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Best value for growing businesses. Billed once a year.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹16,999", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                        Text(" /month  ", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            "₹19,999",
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("₹2,03,988 billed annually", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                }
                RadioButton(
                    selected = isAnnualBilling,
                    onClick = { onCycleSelected(true) },
                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 3: REVIEW ORDER
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step3ReviewOrder(
    couponCode: String,
    onCouponChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("ORDER SUMMARY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151), letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, grey_border, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                SummaryPriceRow(label = "PROFESSIONAL Plan (monthly)", value = "₹19,999")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Unused STANDARD credit", fontSize = 12.sp, color = Color(0xFF374151))
                        Text("Prorated credit", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    }
                    Text("-₹2,000", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A))
                }
                Spacer(Modifier.height(8.dp))
                SummaryPriceRow(label = "GST (18%)", value = "₹3,240")
                Spacer(Modifier.height(8.dp))
                SummaryPriceRow(label = "Coupon discount", value = "—", valueColor = Color(0xFF9CA3AF))

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Due Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("₹21,239", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("APPLY COUPON", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151), letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, grey_border, RoundedCornerShape(10.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = couponCode,
                onValueChange = onCouponChanged,
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                cursorBrush = SolidColor(Primary),
                decorationBox = { innerTextField ->
                    if (couponCode.isEmpty()) {
                        Text("Enter coupon code", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            )

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = primary_light),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text("Apply", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("WHAT YOU'RE GETTING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151), letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, grey_border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCheckItem("+50 Users (+35)")
                FeatureCheckItem("+20 Branches (+15)")
                FeatureCheckItem("+100 GB Storage")
                FeatureCheckItem("+Corporate Orders")
                FeatureCheckItem("+Advanced Reports")
                FeatureCheckItem("+AI Predictions")
                FeatureCheckItem("+AI Automation")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 4: PAYMENT
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step4Payment(
    selectedPaymentMethod: String,
    onPaymentMethodSelected: (String) -> Unit,
    onSimulateFailure: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("SELECT PAYMENT METHOD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151), letterSpacing = 0.5.sp)
        Spacer(Modifier.height(10.dp))

        // Visa Card Option
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selectedPaymentMethod == "visa") 2.dp else 1.dp,
                    color = if (selectedPaymentMethod == "visa") Primary else grey_border,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { onPaymentMethodSelected("visa") }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedPaymentMethod == "visa",
                    onClick = { onPaymentMethodSelected("visa") },
                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E1B4B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("VISA", color = whiteBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Visa •••• 4242", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                    Text("Expires 12/28", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }
                Badge(text = "DEFAULT", bgColor = primary_light, textColor = Primary)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Mastercard Option
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selectedPaymentMethod == "mastercard") 2.dp else 1.dp,
                    color = if (selectedPaymentMethod == "mastercard") Primary else grey_border,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { onPaymentMethodSelected("mastercard") }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedPaymentMethod == "mastercard",
                    onClick = { onPaymentMethodSelected("mastercard") },
                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEA1D2C), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("MC", color = whiteBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mastercard •••• 8291", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                    Text("Expires 09/27", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add payment method", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(18.dp))

        Text("ORDER SUMMARY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151), letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, grey_border, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                SummaryPriceRow(label = "PROFESSIONAL Plan (monthly)", value = "₹19,999")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Unused STANDARD credit", fontSize = 12.sp, color = Color(0xFF374151))
                        Text("Prorated credit", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    }
                    Text("-₹2,000", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A))
                }
                Spacer(Modifier.height(8.dp))
                SummaryPriceRow(label = "GST (18%)", value = "₹3,240")

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Due Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("₹21,239", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Secure Payment Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDF4), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Secure Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    Text("Your payment is encrypted and secured with 256-bit SSL.", fontSize = 10.sp, color = Color(0xFF15803D))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Debug simulator link
        Text(
            text = "Simulate failure",
            color = Color(0xFFEF4444),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSimulateFailure() }
                .padding(vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 5: SUCCESS SCREEN
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step5SuccessScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFDCFCE7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text("Plan Activated!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your PROFESSIONAL plan is now active. All features and limits have been updated.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDF4), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "ACTIVATION CHECKLIST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A),
                    letterSpacing = 0.5.sp
                )
                ChecklistItem("Subscription updated to PROFESSIONAL")
                ChecklistItem("Usage limits increased")
                ChecklistItem("Invoice generated")
                ChecklistItem("Confirmation email sent")
                ChecklistItem("History updated")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 5: FAILURE SCREEN
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step5FailureScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFFEE2E2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text("Payment Failed", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your payment could not be processed. Please update your payment method and try again.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF2F2), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ACTIVATION ERROR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Error: Card declined — Insufficient funds.", fontSize = 12.sp, color = Color(0xFF374151))
                }
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reference: TXN 2026-86234", fontSize = 12.sp, color = Color(0xFF374151))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEPPER COMPONENT
// ─────────────────────────────────────────────────────────────

@Composable
fun SubscriptionStepper(
    stepLabels: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepLabels.forEachIndexed { index, _ ->
                val done = index < currentStep
                val active = index == currentStep

                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val circleColor by animateColorAsState(
                        targetValue = when {
                            done -> Color(0xFF10B981)
                            active -> Primary
                            else -> Color(0xFFF3F4F6)
                        },
                        animationSpec = tween(durationMillis = 250),
                        label = "circleColor"
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(circleColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            done -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(14.dp)
                            )
                            active -> Text(
                                text = "${index + 1}",
                                color = whiteBg,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            else -> Text(
                                text = "${index + 1}",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (index < stepLabels.lastIndex) {
                    val lineColor by animateColorAsState(
                        targetValue = if (index < currentStep) Color(0xFF10B981) else Color(0xFFE5E7EB),
                        animationSpec = tween(durationMillis = 250),
                        label = "lineColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(lineColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            stepLabels.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier.size(width = 34.dp, height = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (index == currentStep) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                }

                if (index < stepLabels.lastIndex) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE HELPER UI COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun SubscriptionTopBar(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TitleBar( title, onClose = onClose)
    }
}

@Composable
private fun SummaryGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryItem(modifier = Modifier.weight(1f), title = "Current Plan", value = "STANDARD", subtext = "Trial active", subColor = Color(0xFF10B981))
            SummaryItem(modifier = Modifier.weight(1f), title = "Monthly Price", value = "₹9,999", subtext = "Post-Trial", subColor = Color(0xFF9CA3AF))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryItem(modifier = Modifier.weight(1f), title = "Active Users", value = "12 / 15", subtext = "80% used", subColor = Color(0xFFF59E0B))
            SummaryItem(modifier = Modifier.weight(1f), title = "Branches", value = "3 / 5", subtext = "60% used", subColor = Color(0xFFF59E0B))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryItem(modifier = Modifier.weight(1f), title = "Storage Space", value = "18 / 25 GB", subtext = "72% used", subColor = Color(0xFFF59E0B))
            SummaryItem(modifier = Modifier.weight(1f), title = "Next Billing", value = "06 Sep 2025", subtext = "Post-Trial date", subColor = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun SummaryItem(
    modifier: Modifier,
    title: String,
    value: String,
    subtext: String,
    subColor: Color
) {
    Box(
        modifier = modifier
            .background(BorderGray, RoundedCornerShape(10.dp))
            .border(1.dp, grey_border, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Column {
            Text(title, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtext, fontSize = 10.sp, color = subColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun UsageProgressBar(
    label: String,
    progress: Float,
    percentage: String,
    percentColor: Color,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(percentage, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = percentColor)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = barColor,
            trackColor = Color(0xFFF3F4F6)
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier,
    title: String,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .border(1.dp, grey_border, RoundedCornerShape(12.dp))
            .clickable { }
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(primary_light, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    price: String,
    badgeText: String? = null,
    badgeBg: Color = Color.Transparent,
    badgeColor: Color = Color.Transparent,
    isSelected: Boolean,
    features: List<String> = emptyList(),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Primary else grey_border,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Primary else TextPrimary)
                if (badgeText != null) {
                    Badge(text = badgeText, bgColor = badgeBg, textColor = badgeColor)
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
            Text(price, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

            if (features.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(feature, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPriceRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF374151))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun FeatureCheckItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF374151))
    }
}

@Composable
private fun ChecklistItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF374151))
    }
}

@Composable
private fun Badge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}