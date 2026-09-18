@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.subscriptions

import com.cuso.tailor.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextLog
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.activity_green
import com.cuso.tailor.ui.theme.activity_green_bg
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.darkPurple
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction

// ─────────────────────────────────────────────────────────────
// ENUMS & STEP DEFINITIONS
// ─────────────────────────────────────────────────────────────

private enum class SubscriptionScreen {
    Overview, UsageDetails, UpgradeFlow
}

val upgradeStepLabels = listOf("Plan", "Cycle", "Review", "Payment", "Status")

// ─────────────────────────────────────────────────────────────
// ROOT NAVIGATION CONTAINER
// ─────────────────────────────────────────────────────────────

@Composable
fun SubscriptionFlowContainer(
    onClose: () -> Unit,
    onStartUpgrade: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(SubscriptionScreen.Overview) }
    var upgradeCurrentStep by remember { mutableIntStateOf(0) }
    var selectedPlan by remember { mutableStateOf("PROFESSIONAL") }
    var isAnnualBilling by remember { mutableStateOf(true) }
    var couponCode by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("visa") }
    var isPaymentFailed by remember { mutableStateOf(false) }

    when (currentScreen) {
        SubscriptionScreen.Overview -> {
            ManageSubscriptionDashboard(
                onClose = onClose,
                onStartUpgrade = { currentScreen = SubscriptionScreen.UpgradeFlow },
                onViewUsageDetails = { currentScreen = SubscriptionScreen.UsageDetails }
            )
        }
        SubscriptionScreen.UsageDetails -> {
            UsageDetailsDashboard(
                onClose = { currentScreen = SubscriptionScreen.Overview },
                onStartUpgrade = { currentScreen = SubscriptionScreen.UpgradeFlow }
            )
        }
        SubscriptionScreen.UpgradeFlow -> {
            UpgradeSubscriptionFlow(
                currentStep = upgradeCurrentStep,
                selectedPlan = selectedPlan,
                isAnnualBilling = isAnnualBilling,
                couponCode = couponCode,
                selectedPaymentMethod = selectedPaymentMethod,
                isPaymentFailed = isPaymentFailed,
                onClose = {
                    upgradeCurrentStep = 0
                    currentScreen = SubscriptionScreen.Overview
                },
                onPlanSelected = { selectedPlan = it },
                onCycleSelected = { isAnnualBilling = it },
                onCouponChanged = { couponCode = it },
                onPaymentMethodSelected = { selectedPaymentMethod = it },
                onContinueToStep = { upgradeCurrentStep = it },
                onSimulateFailure = {
                    isPaymentFailed = true
                    upgradeCurrentStep = 4
                },
                onRetryPayment = {
                    isPaymentFailed = false
                    upgradeCurrentStep = 3
                },
                onFinishFlow = {
                    upgradeCurrentStep = 0
                    isPaymentFailed = false
                    currentScreen = SubscriptionScreen.Overview
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SCREEN 1: OVERVIEW DASHBOARD
// ─────────────────────────────────────────────────────────────

@Composable
fun ManageSubscriptionDashboard(
    onClose: () -> Unit,
    onStartUpgrade: () -> Unit,
    onViewUsageDetails: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(title = "Manage Subscription", onClose = onClose)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
        ) {
            ActionButtonsRow(tokens = tokens)

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {},
                shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                Text(
                    text = "Download Statement",
                    color = whiteBg,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = tokens.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            FreeTrialBanner(tokens = tokens, onChoosePlan = onStartUpgrade)

            Spacer(Modifier.height(16.dp))

            CurrentPlanCard(tokens = tokens, onCompareAll = onStartUpgrade)

            Spacer(Modifier.height(16.dp))

            TrialProgressCard(tokens = tokens)

            Spacer(Modifier.height(16.dp))

            PlanRecommendationCard(tokens = tokens, onCompareToProfessional = onStartUpgrade)

            Spacer(Modifier.height(24.dp))

            SectionLabel(text = "SUBSCRIPTION SUMMARY", tokens = tokens)
            Spacer(Modifier.height(12.dp))
            SubscriptionSummaryGrid(tokens = tokens)

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(text = "USAGE SNAPSHOT", tokens = tokens)
                Text(
                    text = "View detailed Usage →",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.clickable { onViewUsageDetails() }
                )
            }

            Spacer(Modifier.height(14.dp))
            WhiteCard(tokens = tokens) {
                UsageProgressBar(tokens, "Users", 0.80f, "80%", orangeText)
                UsageProgressBar(tokens, "AI Credits", 0.15f, "15%", darkGreenBg)
                UsageProgressBar(tokens, "Branches", 0.60f, "60%", orangeText)
                UsageProgressBar(tokens, "WhatsApp", 0.92f, "92%", redText)
                UsageProgressBar(tokens, "Storage", 0.72f, "72%", orangeText)
                UsageProgressBar(tokens, "SMS Messages", 0.76f, "76%", orangeText)
                UsageProgressBar(tokens, "Orders Completed", 0.84f, "84%", redText)
                UsageProgressBar(tokens, "API Calls", 0.14f, "14%", darkGreenBg)
            }
            Spacer(Modifier.height(24.dp))

            SectionLabel(text = "QUICK ACTIONS", tokens = tokens)
            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionCard(tokens, Modifier.weight(1f), "View Invoices", Icons.Default.Description)
                QuickActionCard(tokens, Modifier.weight(1f), "Payment Methods", Icons.Default.CreditCard)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionCard(tokens, Modifier.weight(1f), "Add-ons Available", Icons.Default.Widgets)
                QuickActionCard(tokens, Modifier.weight(1f), "Subscription History", Icons.Default.History)
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SCREEN 2: USAGE DETAILS
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsageDetailsDashboard(
    onClose: () -> Unit,
    onStartUpgrade: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(title = "Usage & Limits", onClose = onClose)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
        ) {
            Text(
                text = "Usage & Limits",
                fontSize = tokens.h1,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Manage your CUSO Tailor subscription, usage, billing and plan.",
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            SectionLabel(text = "BILLING PERIOD", tokens = tokens)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Aug 2026 — Sep 2026",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            SectionLabel(text = "RESOURCE STATUS", tokens = tokens)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusPill("0 At Limit", redBg, redText, tokens)
                StatusPill("2 Warning", orangeBg, orangeText, tokens)
                StatusPill("4 Attention", yellowBg, yellowText, tokens)
                StatusPill("2 Normal", greenBg, darkGreenBg, tokens)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onStartUpgrade,
                shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                Text(
                    text = "Upgrade Plan",
                    color = whiteBg,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = tokens.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            SectionLabel(text = "THRESHOLD GUIDE", tokens = tokens)
            Spacer(Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ThresholdGuideItem(greentext, "0–70% Normal", tokens)
                ThresholdGuideItem(yellowText, "70–85% Attention", tokens)
                ThresholdGuideItem(orangeText, "85–95% Warning", tokens)
                ThresholdGuideItem(redText, "95–100% Critical", tokens)
            }

            Spacer(Modifier.height(28.dp))

            SectionLabel(text = "RESOURCES & USAGE", tokens = tokens)
            Spacer(Modifier.height(14.dp))

            ResourceCard(
                tokens = tokens,
                title = "Users",
                badgeText = "ATTENTION",
                badgeBg = yellowBg,
                badgeTextClr = yellowText,
                currentVal = "12",
                maxVal = "15 users",
                percentage = "80%",
                progress = 0.80f,
                progressColor = orangeText,
                warningMessage = "You're approaching your plan limit.",
                primaryButtonText = "Add Users",
                secondaryButtonText = "Buy Add-on"
            )

            Spacer(Modifier.height(16.dp))

            ResourceCard(
                tokens = tokens,
                title = "Branches",
                badgeText = "NORMAL",
                badgeBg = greenBg,
                badgeTextClr = darkGreenBg,
                currentVal = "3",
                maxVal = "5 branches",
                percentage = "60%",
                progress = 0.60f,
                progressColor = BluePrimary,
                primaryButtonText = "Upgrade Plan",
                onPrimaryClick = onStartUpgrade
            )

            Spacer(Modifier.height(16.dp))

            ResourceCard(
                tokens = tokens,
                title = "Storage",
                badgeText = "ATTENTION",
                badgeBg = yellowBg,
                badgeTextClr = yellowText,
                currentVal = "18",
                maxVal = "25 GB",
                percentage = "72%",
                progress = 0.72f,
                progressColor = orangeText,
                warningMessage = "You're approaching your plan limit.",
                breakdownList = listOf(
                    "Customer Images" to "6.2 GB",
                    "Invoice PDFs" to "4.8 GB",
                    "Documents" to "3.9 GB",
                    "Design Files" to "2.9 GB",
                    "Reports" to "0.6 GB"
                ),
                primaryButtonText = "Buy Storage",
                secondaryButtonText = "Upgrade",
                onSecondaryClick = onStartUpgrade
            )

            Spacer(Modifier.height(16.dp))

            ResourceCard(
                tokens = tokens,
                title = "Orders",
                badgeText = "ATTENTION",
                badgeBg = yellowBg,
                badgeTextClr = yellowText,
                currentVal = "4,208",
                maxVal = "5,000 orders",
                percentage = "84%",
                progress = 0.84f,
                progressColor = orangeText,
                warningMessage = "You're approaching your plan limit.",
                primaryButtonText = "Upgrade Plan",
                secondaryButtonText = "Buy Add-on",
                onPrimaryClick = onStartUpgrade
            )

            Spacer(Modifier.height(16.dp))

            ResourceCard(
                tokens = tokens,
                title = "AI Credits",
                badgeText = "ATTENTION",
                badgeBg = yellowBg,
                badgeTextClr = yellowText,
                currentVal = "769",
                maxVal = "1,000 credits",
                percentage = "78%",
                progress = 0.78f,
                progressColor = orangeText,
                warningMessage = "You're approaching your plan limit.",
                primaryButtonText = "Buy Credits",
                secondaryButtonText = "Upgrade",
                onSecondaryClick = onStartUpgrade
            )

            Spacer(Modifier.height(16.dp))

            ResourceCard(
                tokens = tokens,
                title = "WhatsApp",
                badgeText = "ATTENTION",
                badgeBg = yellowBg,
                badgeTextClr = yellowText,
                currentVal = "8,200",
                maxVal = "10,000 messages",
                percentage = "82%",
                progress = 0.82f,
                progressColor = orangeText,
                warningMessage = "You're approaching your plan limit.",
                primaryButtonText = "Buy Messages",
                secondaryButtonText = "Upgrade",
                onSecondaryClick = onStartUpgrade
            )

            Spacer(Modifier.height(16.dp))

            ResourceCard(
                tokens = tokens,
                title = "SMS",
                badgeText = "ATTENTION",
                badgeBg = yellowBg,
                badgeTextClr = yellowText,
                currentVal = "3,800",
                maxVal = "5,000 messages",
                percentage = "76%",
                progress = 0.76f,
                progressColor = orangeText,
                warningMessage = "You're approaching your plan limit.",
                primaryButtonText = "Buy SMS"
            )

            Spacer(Modifier.height(16.dp))

            ResourceCard(
                tokens = tokens,
                title = "API Calls",
                badgeText = "NORMAL",
                badgeBg = greenBg,
                badgeTextClr = darkGreenBg,
                currentVal = "20,000",
                maxVal = "50,000 calls",
                percentage = "56%",
                progress = 0.56f,
                progressColor = BluePrimary,
                primaryButtonText = "Upgrade Plan",
                onPrimaryClick = onStartUpgrade
            )

            Spacer(Modifier.height(30.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SCREEN 3: UPGRADE STEPPER FLOW
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
    val tokens = LocalAppTokens.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(title = "Manage Subscription", onClose = onClose)
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
                        .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                ) {
                    Text(
                        text = "Upgrade / Downgrade",
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Manage your CUSO Tailor subscription, usage, billing and plan.",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(14.dp))

                    SubscriptionStepper(
                        tokens = tokens,
                        stepLabels = upgradeStepLabels,
                        currentStep = currentStep
                    )
                }

                HorizontalDivider(color = grey_border)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                        .padding(bottom = 75.dp)
                ) {
                    when (currentStep) {
                        0 -> Step1ChoosePlan(
                            tokens = tokens,
                            selectedPlan = selectedPlan,
                            onPlanSelected = onPlanSelected
                        )
                        1 -> Step2SelectCycle(
                            tokens = tokens,
                            isAnnualBilling = isAnnualBilling,
                            onCycleSelected = onCycleSelected
                        )
                        2 -> Step3ReviewOrder(
                            tokens = tokens,
                            couponCode = couponCode,
                            onCouponChanged = onCouponChanged
                        )
                        3 -> Step4Payment(
                            tokens = tokens,
                            selectedPaymentMethod = selectedPaymentMethod,
                            onPaymentMethodSelected = onPaymentMethodSelected,
                            onSimulateFailure = onSimulateFailure
                        )
                        4 -> {
                            if (isPaymentFailed) {
                                Step5FailureScreen(tokens = tokens)
                            } else {
                                Step5SuccessScreen(tokens = tokens)
                            }
                        }
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = currentStep > 0,
            backLabel = when (currentStep) {
                4 -> if (isPaymentFailed) "Update Payment" else "View Subscription"
                else -> "Back"
            },
            showBackArrow = currentStep != 4,
            showTrailingArrow = currentStep != 3,
            backWidthFraction = if (currentStep > 0) 0.46f else null,
            trailingWidthFraction = if (currentStep == 0) 1.0f else 0.48f,
            onBack = {
                if (currentStep == 4) {
                    if (isPaymentFailed) onContinueToStep(3) else onFinishFlow()
                } else {
                    onContinueToStep(currentStep - 1)
                }
            },
            trailingAction = when (currentStep) {
                0 -> TrailingFabAction.Next(label = "Continue") { onContinueToStep(1) }
                1 -> TrailingFabAction.Next(label = "Review Order") { onContinueToStep(2) }
                2 -> TrailingFabAction.Next(label = "Proceed to Payment") { onContinueToStep(3) }
                3 -> TrailingFabAction.Update(label = "Pay ₹21,239") { onContinueToStep(4) }
                4 -> {
                    if (isPaymentFailed) {
                        TrailingFabAction.Next(label = "Retry Payment") { onRetryPayment() }
                    } else {
                        TrailingFabAction.Next(label = "Go to Overview") { onFinishFlow() }
                    }
                }
                else -> null
            },
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 1: CHOOSE PLAN (WITH ANIMATED ACCORDION)
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step1ChoosePlan(
    tokens: AppDesignTokens,
    selectedPlan: String,
    onPlanSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WhiteCard(tokens = tokens, backgroundColor = primary_light, borderColor = disabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("From: STANDARD", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextLog)
                    Text("₹9,999/mo", fontSize = tokens.caption, color = TextSecondary)
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text("To:", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Bold)
                    Text(selectedPlan, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
                    Text(
                        when (selectedPlan) {
                            "LITE" -> "₹3,999/mo"
                            "PROFESSIONAL" -> "₹19,999/mo"
                            else -> "Custom/mo"
                        },
                        fontSize = tokens.caption,
                        color = Primary
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Plan 1: LITE
        PlanCard(
            tokens = tokens,
            title = "LITE",
            subtitle = "For small tailor businesses",
            price = "₹3,999/mo",
            badgeText = "Downgrade",
            badgeBg = yellowBg,
            badgeColor = yellowText,
            isSelected = selectedPlan == "LITE",
            features = listOf(
                "Up to 5 Users",
                "1 Branch Only",
                "10 GB Cloud Storage",
                "Basic Measurements & Orders",
                "Standard Sales Reports"
            ),
            onClick = { onPlanSelected("LITE") }
        )

        Spacer(Modifier.height(14.dp))

        // Plan 2: PROFESSIONAL
        PlanCard(
            tokens = tokens,
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
                "Custom Reports & Analytics",
                "50 Active Users",
                "20 Branches",
                "100 GB Cloud Storage"
            ),
            onClick = { onPlanSelected("PROFESSIONAL") }
        )

        Spacer(Modifier.height(14.dp))

        // Plan 3: ENTERPRISE
        PlanCard(
            tokens = tokens,
            title = "ENTERPRISE",
            subtitle = "Custom enterprise solutions",
            price = "Custom/mo",
            isSelected = selectedPlan == "ENTERPRISE",
            features = listOf(
                "Unlimited Users & Branches",
                "Dedicated Account Manager",
                "Full AI Automation Suite",
                "Custom ERP Integration & API Access",
                "Unlimited Storage & 24/7 Priority Support"
            ),
            onClick = { onPlanSelected("ENTERPRISE") }
        )
    }
}

@Composable
private fun PlanCard(
    tokens: AppDesignTokens,
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
    WhiteCard(
        tokens = tokens,
        borderColor = if (isSelected) Primary else Color.Transparent,
        modifier = Modifier
            .animateContentSize(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = if (isSelected) Primary else TextPrimary)
            if (badgeText != null) {
                Badge(tokens = tokens, text = badgeText, bgColor = badgeBg, textColor = badgeColor)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = tokens.bodySmall, color = TextSecondary)
        Spacer(Modifier.height(10.dp))
        Text(price, fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)

        // Smooth Animated Accordion for Features
        AnimatedVisibility(
            visible = isSelected && features.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
        ) {
            Column {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_tick_2),
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(feature, fontSize = tokens.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 2: SELECT CYCLE
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step2SelectCycle(
    tokens: AppDesignTokens,
    isAnnualBilling: Boolean,
    onCycleSelected: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WhiteCard(
            tokens = tokens,
            borderColor = if (!isAnnualBilling) Primary else BorderGray,
            modifier = Modifier.clickable { onCycleSelected(false) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Monthly Billing", fontWeight = FontWeight.Bold, fontSize = tokens.bodyLarge, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Pay month-to-month. Cancel anytime.", fontSize = tokens.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("₹19,999", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(" /month", fontSize = tokens.caption, color = TextSecondary)
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

        WhiteCard(
            tokens = tokens,
            borderColor = if (isAnnualBilling) Primary else BorderGray,
            modifier = Modifier.clickable { onCycleSelected(true) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Annual Billing", fontWeight = FontWeight.Bold, fontSize = tokens.bodyLarge, color = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Badge(tokens = tokens, text = "Save 16%", bgColor = greenBg, textColor = darkGreenBg)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Best value for growing businesses. Billed once a year.",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹16,999", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
                        Text(" /month  ", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text(
                            "₹19,999",
                            fontSize = tokens.caption,
                            color = mutedText,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("₹2,03,988 billed annually", fontSize = tokens.caption, color = greentext, fontWeight = FontWeight.Medium)
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
    tokens: AppDesignTokens,
    couponCode: String,
    onCouponChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "ORDER SUMMARY", tokens = tokens)
        Spacer(Modifier.height(8.dp))

        WhiteCard(tokens = tokens) {
            SummaryPriceRow(tokens, label = "PROFESSIONAL Plan (monthly)", value = "₹19,999")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Unused STANDARD credit", fontSize = tokens.bodySmall, color = TextLog)
                    Text("Prorated credit", fontSize = tokens.label, color = mutedText)
                }
                Text("-₹2,000", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = greentext)
            }
            Spacer(Modifier.height(8.dp))
            SummaryPriceRow(tokens, label = "GST (18%)", value = "₹3,240")
            Spacer(Modifier.height(8.dp))
            SummaryPriceRow(tokens, label = "Coupon discount", value = "—", valueColor = mutedText)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Due Today", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("₹21,239", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionLabel(text = "APPLY COUPON", tokens = tokens)
        Spacer(Modifier.height(8.dp))

        WhiteCard(tokens = tokens, padding = 6.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = couponCode,
                    onValueChange = onCouponChanged,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = tokens.bodySmall, color = TextPrimary),
                    cursorBrush = SolidColor(Primary),
                    decorationBox = { innerTextField ->
                        if (couponCode.isEmpty()) {
                            Text("Enter coupon code", color = mutedText, fontSize = tokens.bodySmall)
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
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Apply", color = Primary, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel(text = "WHAT YOU'RE GETTING", tokens = tokens)
        Spacer(Modifier.height(8.dp))

        WhiteCard(tokens = tokens) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCheckItem(tokens, "+50 Users (+35)")
                FeatureCheckItem(tokens, "+20 Branches (+15)")
                FeatureCheckItem(tokens, "+100 GB Storage")
                FeatureCheckItem(tokens, "+Corporate Orders")
                FeatureCheckItem(tokens, "+Advanced Reports")
                FeatureCheckItem(tokens, "+AI Predictions")
                FeatureCheckItem(tokens, "+AI Automation")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 4: PAYMENT
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step4Payment(
    tokens: AppDesignTokens,
    selectedPaymentMethod: String,
    onPaymentMethodSelected: (String) -> Unit,
    onSimulateFailure: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "SELECT PAYMENT METHOD", tokens = tokens)
        Spacer(Modifier.height(10.dp))

        WhiteCard(
            tokens = tokens,
            borderColor = if (selectedPaymentMethod == "visa") Primary else BorderGray,
            modifier = Modifier.clickable { onPaymentMethodSelected("visa") }
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
                        .background(darkPurple, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("VISA", color = whiteBg, fontWeight = FontWeight.Bold, fontSize = tokens.label)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Visa •••• 4242", fontWeight = FontWeight.SemiBold, fontSize = tokens.bodySmall, color = TextPrimary)
                    Text("Expires 12/28", fontSize = tokens.caption, color = mutedText)
                }
                Badge(tokens = tokens, text = "DEFAULT", bgColor = primary_light, textColor = Primary)
            }
        }

        Spacer(Modifier.height(10.dp))

        WhiteCard(
            tokens = tokens,
            borderColor = if (selectedPaymentMethod == "mastercard") Primary else BorderGray,
            modifier = Modifier.clickable { onPaymentMethodSelected("mastercard") }
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
                        .background(redText, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("MC", color = whiteBg, fontWeight = FontWeight.Bold, fontSize = tokens.label)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mastercard •••• 8291", fontWeight = FontWeight.SemiBold, fontSize = tokens.bodySmall, color = TextPrimary)
                    Text("Expires 09/27", fontSize = tokens.caption, color = mutedText)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
            Spacer(Modifier.width(4.dp))
            Text("Add payment method", fontSize = tokens.bodySmall, color = Primary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(18.dp))

        SectionLabel(text = "ORDER SUMMARY", tokens = tokens)
        Spacer(Modifier.height(8.dp))

        WhiteCard(tokens = tokens) {
            SummaryPriceRow(tokens, label = "PROFESSIONAL Plan (monthly)", value = "₹19,999")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Unused STANDARD credit", fontSize = tokens.bodySmall, color = TextLog)
                    Text("Prorated credit", fontSize = tokens.label, color = mutedText)
                }
                Text("-₹2,000", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = greentext)
            }
            Spacer(Modifier.height(8.dp))
            SummaryPriceRow(tokens, label = "GST (18%)", value = "₹3,240")

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Due Today", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("₹21,239", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        Spacer(Modifier.height(14.dp))

        WhiteCard(tokens = tokens, backgroundColor = activity_green_bg, borderColor = greenBg) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = darkGreenBg,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Secure Payment", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = activity_green)
                    Text("Your payment is encrypted and secured with 256-bit SSL.", fontSize = tokens.label, color = darkGreenBg)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Simulate failure",
            color = redText,
            fontSize = tokens.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSimulateFailure() }
                .padding(vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STEP 5: SUCCESS & FAILURE SCREENS
// ─────────────────────────────────────────────────────────────

@Composable
private fun Step5SuccessScreen(tokens: AppDesignTokens) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(greenBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = darkGreenBg,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text("Plan Activated!", fontSize = tokens.h1, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your plan is now active. All features and limits have been updated.",
            fontSize = tokens.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))

        WhiteCard(tokens = tokens, backgroundColor = activity_green_bg, borderColor = greenBg) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "ACTIVATION CHECKLIST",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Bold,
                    color = darkGreenBg
                )
                ChecklistItem(tokens, "Subscription plan updated")
                ChecklistItem(tokens, "Usage limits increased")
                ChecklistItem(tokens, "Invoice generated")
                ChecklistItem(tokens, "Confirmation email sent")
                ChecklistItem(tokens, "History updated")
            }
        }
    }
}

@Composable
private fun Step5FailureScreen(tokens: AppDesignTokens) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(redBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = redText,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text("Payment Failed", fontSize = tokens.h1, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your payment could not be processed. Please update your payment method and try again.",
            fontSize = tokens.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))

        WhiteCard(tokens = tokens, backgroundColor = redBg.copy(alpha = 0.4f), borderColor = redBg) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ACTIVATION ERROR",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Bold,
                    color = redText
                )
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = redText, modifier = Modifier.size(tokens.iconSize))
                    Spacer(Modifier.width(8.dp))
                    Text("Error: Card declined — Insufficient funds.", fontSize = tokens.bodySmall, color = TextLog)
                }
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = redText, modifier = Modifier.size(tokens.iconSize))
                    Spacer(Modifier.width(8.dp))
                    Text("Reference: TXN 2026-86234", fontSize = tokens.bodySmall, color = TextLog)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
fun SubscriptionStepper(
    tokens: AppDesignTokens,
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
                            done -> greentext
                            active -> Primary
                            else -> light_grey
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
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            active -> Text(
                                text = "${index + 1}",
                                color = whiteBg,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            else -> Text(
                                text = "${index + 1}",
                                color = mutedText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (index < stepLabels.lastIndex) {
                    val lineColor by animateColorAsState(
                        targetValue = if (index < currentStep) greentext else grey_border,
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
                            fontSize = tokens.label,
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

@Composable
private fun ResourceCard(
    tokens: AppDesignTokens,
    title: String,
    badgeText: String,
    badgeBg: Color,
    badgeTextClr: Color,
    currentVal: String,
    maxVal: String,
    percentage: String,
    progress: Float,
    progressColor: Color,
    warningMessage: String? = null,
    breakdownList: List<Pair<String, String>>? = null,
    primaryButtonText: String,
    secondaryButtonText: String? = null,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
) {
    WhiteCard(tokens = tokens) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = tokens.h2,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Badge(tokens = tokens, text = badgeText, bgColor = badgeBg, textColor = badgeTextClr)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentVal,
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "/ $maxVal",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = percentage,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = grey_border
        )

        if (warningMessage != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = orangeText,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = warningMessage,
                    fontSize = tokens.caption,
                    color = orangeText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (!breakdownList.isNullOrEmpty()) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(10.dp))

            SectionLabel(text = "USAGE BREAKDOWN", tokens = tokens)
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                breakdownList.forEach { (itemTitle, itemSize) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = itemTitle,
                            fontSize = tokens.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = itemSize,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (secondaryButtonText != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onPrimaryClick,
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(
                        text = primaryButtonText,
                        color = whiteBg,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = tokens.bodySmall
                    )
                }
                OutlinedButton(
                    onClick = onSecondaryClick,
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                    border = BorderStroke(1.dp, Primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(
                        text = secondaryButtonText,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = tokens.bodySmall
                    )
                }
            }
        } else {
            Button(
                onClick = onPrimaryClick,
                shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Text(
                    text = primaryButtonText,
                    color = whiteBg,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = tokens.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(tokens: AppDesignTokens) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
            border = BorderStroke(1.dp, Primary),
            modifier = Modifier
                .weight(1f)
                .background(whiteBg)
                .height(tokens.buttonHeight)
        ) {
            Text("Need Help", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = tokens.bodyMedium)
        }
        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
            border = BorderStroke(1.dp, Primary),
            modifier = Modifier
                .weight(1f)
                .background(whiteBg)
                .height(tokens.buttonHeight)
        ) {
            Text("Contact Billing", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = tokens.bodyMedium)
        }
    }
}

@Composable
private fun FreeTrialBanner(
    tokens: AppDesignTokens,
    onChoosePlan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(listOf(Primary, BluePrimary)),
                shape = RoundedCornerShape(tokens.cardCornerRadius)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(whiteBg.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Free Trial Active — 12 Days Remaining",
                    color = whiteBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = tokens.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Trial ends 06 Sep 2025. Choose a plan to continue uninterrupted.",
                    color = whiteBg.copy(alpha = 0.9f),
                    fontSize = tokens.bodySmall,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Choose a Plan",
                    color = whiteBg,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onChoosePlan() }
                )
            }
        }
    }
}

@Composable
private fun CurrentPlanCard(
    tokens: AppDesignTokens,
    onCompareAll: () -> Unit
) {
    WhiteCard(tokens = tokens) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CUSO TAILOR", color = mutedText, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Badge(tokens, text = "FREE TRIAL", bgColor = yellowBg, textColor = yellowText)
                Badge(tokens, text = "Active", bgColor = greenBg, textColor = darkGreenBg)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("STANDARD", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Price post trial: ₹9,999/mo", fontSize = tokens.bodySmall, color = TextSecondary, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = grey_border)
        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Trial Started", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(2.dp))
                Text("25 Aug 2025", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Trial Ends", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(2.dp))
                Text("06 Sep 2025", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Compare All Plans",
            color = Primary,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCompareAll() }
        )
    }
}

@Composable
private fun TrialProgressCard(tokens: AppDesignTokens) {
    WhiteCard(tokens = tokens) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Trial Progress", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("12", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(Modifier.width(4.dp))
                Text("days left", fontSize = tokens.bodySmall, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { 0.14f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Primary,
            trackColor = grey_border
        )

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Day 1", fontSize = tokens.caption, color = mutedText)
            Text("Today (Day 2)", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
            Text("Day 14", fontSize = tokens.caption, color = mutedText)
        }

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(primary_light, RoundedCornerShape(6.dp))
                        .padding(horizontal = 35.dp, vertical = 6.dp)
                ) {
                    Text("Day 1", color = Primary, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Text("Day 5", color = mutedText, fontSize = tokens.bodySmall)
                Text("Day 10", color = mutedText, fontSize = tokens.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Day 15", color = mutedText, fontSize = tokens.bodySmall)
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Upgrade before trial ends to retain all data and access.",
            fontSize = tokens.caption,
            color = mutedText
        )
    }
}

@Composable
private fun PlanRecommendationCard(
    tokens: AppDesignTokens,
    onCompareToProfessional: () -> Unit
) {
    WhiteCard(tokens = tokens) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text("Plan Recommendation", fontWeight = FontWeight.SemiBold, fontSize = tokens.bodyMedium, color = TextPrimary)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Order usage has reached 84%. Professional may be a better fit for your growing business.",
            fontSize = tokens.bodySmall,
            color = Primary,
            lineHeight = 17.sp
        )

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent, RoundedCornerShape(8.dp))
                .border(1.dp, yellowText.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = orangeText,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "You have 12 of 15 users active. Adding more users will require an upgrade.",
                    fontSize = tokens.caption,
                    color = orangeText,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCompareToProfessional,
            shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
            border = BorderStroke(1.dp, Primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Compare to Professional", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = tokens.bodySmall)
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
        }
    }
}

@Composable
private fun SubscriptionSummaryGrid(tokens: AppDesignTokens) {
    WhiteCard(tokens = tokens) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryItem(tokens, Modifier.weight(1f), "Current Plan", "STANDARD", "Trial active", darkGreenBg)
                SummaryItem(tokens, Modifier.weight(1f), "Monthly Price", "₹9,999", "Post-Trial", mutedText)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryItem(tokens, Modifier.weight(1f), "Active Users", "12 / 15", "80% used", orangeText)
                SummaryItem(tokens, Modifier.weight(1f), "Branches", "3 / 5", "60% used", orangeText)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryItem(tokens, Modifier.weight(1f), "Storage Space", "18 / 25 GB", "72% used", orangeText)
                SummaryItem(tokens, Modifier.weight(1f), "Next Billing", "06 Sep 2025", "Post-Trial date", mutedText)
            }
        }
    }
}

@Composable
private fun SummaryItem(
    tokens: AppDesignTokens,
    modifier: Modifier,
    title: String,
    value: String,
    subtext: String,
    subColor: Color
) {
    Box(
        modifier = modifier
            .background(badgeGrey, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(title, fontSize = tokens.caption, color = close_color)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtext, fontSize = tokens.label, color = subColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun UsageProgressBar(
    tokens: AppDesignTokens,
    label: String,
    progress: Float,
    percentage: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(percentage, fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = accentColor)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = accentColor,
            trackColor = grey_border
        )
    }
}

@Composable
private fun QuickActionCard(
    tokens: AppDesignTokens,
    modifier: Modifier,
    title: String,
    icon: ImageVector
) {
    WhiteCard(tokens = tokens, modifier = modifier.clickable { }) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(primary_light, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
fun WhiteCard(
    tokens: AppDesignTokens,
    modifier: Modifier = Modifier,
    backgroundColor: Color = whiteBg,
    borderColor: Color = Color.Transparent,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(tokens.cardCornerRadius))
            .border(2.dp, borderColor, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(padding)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SectionLabel(text: String, tokens: AppDesignTokens) {
    Text(
        text = text,
        fontSize = tokens.bodyMedium,
        color = close_color
    )
}

@Composable
private fun Badge(
    tokens: AppDesignTokens,
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = tokens.label, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun StatusPill(
    text: String,
    bgColor: Color,
    textColor: Color,
    tokens: AppDesignTokens
) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ThresholdGuideItem(
    dotColor: Color,
    label: String,
    tokens: AppDesignTokens
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = tokens.caption,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SummaryPriceRow(
    tokens: AppDesignTokens,
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = TextLog)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun FeatureCheckItem(tokens: AppDesignTokens, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = darkGreenBg, modifier = Modifier.size(tokens.iconSize))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = tokens.bodySmall, color = TextLog)
    }
}

@Composable
private fun ChecklistItem(tokens: AppDesignTokens, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = darkGreenBg, modifier = Modifier.size(tokens.iconSize))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = tokens.bodySmall, color = TextLog)
    }
}