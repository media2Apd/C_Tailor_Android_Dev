package com.cuso.tailor.view.home.paywall

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cuso.tailor.R
import com.cuso.tailor.model.permission.PaywallModuleConfig

// ─────────────────────────────────────────────────────────────────────────────
// Design colors (taken 1:1 from the approved design)
// ─────────────────────────────────────────────────────────────────────────────
private val PaywallBg = Color(0xFFF5F5FD)
private val PaywallWhite = Color(0xFFFFFFFF)
private val PaywallAccent = Color(0xFF3B2FB8)          // Badge text, icons, plan name
private val PaywallButton = Color(0xFF4535C8)          // Upgrade Plan button
private val PaywallBadgeBg = Color(0xFFE6E5FA)         // Module badge background
private val PaywallHeroBorder = Color(0xFFD9D6F7)
private val PaywallHeroGradientStart = Color(0xFFF1F0FD)
private val PaywallHeroGradientEnd = Color(0xFFE5E3FA)
private val PaywallHeroCircle = Color(0xFFDAD8F6)
private val PaywallGreen = Color(0xFF1B7A4B)           // "+45%" part of the pill
private val PaywallPillText = Color(0xFF4A5470)
private val PaywallTitle = Color(0xFF141432)
private val PaywallBody = Color(0xFF55607A)
private val PaywallLabel = Color(0xFF5F6B85)           // Section label + icon tint
private val PaywallRowText = Color(0xFF1E2440)
private val PaywallLockBox = Color(0xFFEEF0F7)
private val PaywallPlanBg = Color(0xFFEDEEFB)
private val PaywallPlanBorder = Color(0xFFDCDDF5)
private val PaywallOutline = Color(0xFFD5D8E6)

@Composable
fun ModuleUpgradePaywallScreen(
    config: PaywallModuleConfig,
    onClose: () -> Unit,
    onUpgradePlan: () -> Unit,
    onViewPricing: () -> Unit
) {
    // This screen is shown full screen (HomeScreen hides TopBar / BottomBar for it),
    // so it handles the status bar and navigation bar insets by itself.
    Scaffold(
        containerColor = PaywallBg,
        topBar = {
            PaywallHeader(
                moduleTitle = config.moduleTitle,
                onClose = onClose
            )
        },
        bottomBar = {
            PaywallBottomActions(
                onUpgradePlan = onUpgradePlan,
                onViewPricing = onViewPricing
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(22.dp))

            // 1. Hero promo card
            PaywallHeroCard(config = config)

            // 2. Headline and description
            Spacer(Modifier.height(36.dp))
            Text(
                text = config.headline,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.3).sp,
                fontWeight = FontWeight.Bold,
                color = PaywallTitle
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = config.description,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = PaywallBody
            )

            // 3. Locked features list
            Spacer(Modifier.height(26.dp))
            Text(
                text = "FEATURES INCLUDED IN THIS UPGRADE",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = PaywallLabel
            )
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                config.featuresIncluded.forEach { feature ->
                    LockedFeatureItem(title = feature)
                }
            }

            // 4. Current plan info card
            Spacer(Modifier.height(30.dp))
            CurrentPlanCard(planName = config.currentPlan)

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header: close button on the left, module badge in the center
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PaywallHeader(
    moduleTitle: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaywallBg)
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 18.dp)
            .height(44.dp)
    ) {
        // Close button (white circle with a soft shadow)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .shadow(elevation = 2.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(PaywallWhite)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paywall_close),
                contentDescription = "Close",
                tint = PaywallLabel,
                modifier = Modifier.size(18.dp)
            )
        }

        // Centered module badge
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(20.dp))
                .background(PaywallBadgeBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paywall_cube),
                contentDescription = null,
                tint = PaywallAccent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = moduleTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PaywallAccent
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero card: efficiency pill, rocket circle, title and subtitle
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PaywallHeroCard(config: PaywallModuleConfig) {
    val cardShape = RoundedCornerShape(28.dp)
    val moduleName = config.moduleKey.replaceFirstChar { it.uppercase() }

    // Split the badge text, e.g. "+18% Revenue" -> "+18%" (green) and "Revenue" (gray)
    val badgeParts = config.efficiencyBadge.trim().split(" ", limit = 2)
    val badgeValue = badgeParts.firstOrNull().orEmpty()
    val badgeLabel = badgeParts.getOrNull(1).orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(241.dp)
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(PaywallHeroGradientStart, PaywallHeroGradientEnd)
                )
            )
            .border(1.5.dp, PaywallHeroBorder, cardShape)
            .padding(start = 28.dp, end = 28.dp, top = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Efficiency pill
        Row(
            modifier = Modifier
                .zIndex(1f)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp), clip = false)
                .clip(RoundedCornerShape(16.dp))
                .background(PaywallWhite)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paywall_trending_up),
                contentDescription = null,
                tint = PaywallGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = PaywallGreen, fontWeight = FontWeight.SemiBold)) {
                        append(badgeValue)
                    }
                    if (badgeLabel.isNotEmpty()) {
                        append(" ")
                        withStyle(SpanStyle(color = PaywallPillText, fontWeight = FontWeight.Medium)) {
                            append(badgeLabel)
                        }
                    }
                },
                fontSize = 13.sp
            )
        }

        // Rocket circle
        Box(
            modifier = Modifier
                .offset(y = (-4).dp)
                .size(78.dp)
                .clip(CircleShape)
                .background(PaywallHeroCircle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paywall_rocket),
                contentDescription = null,
                tint = PaywallAccent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        // Uses rightTitle if present, otherwise falls back to "Optimize Your [Module]"
        Text(
            text = config.heroTitle.ifBlank { "Optimize Your $moduleName" },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PaywallTitle,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Uses rightDescription if present, otherwise falls back to "Businesses manage ..."
        Text(
            text = config.heroSubtitle.ifBlank { "Businesses manage ${config.moduleKey} smarter with our tools." },
            fontSize = 14.5.sp,
            lineHeight = 21.sp,
            color = PaywallBody,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 290.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Locked feature row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LockedFeatureItem(title: String) {
    val rowShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(rowShape)
            .background(PaywallWhite)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PaywallLockBox),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paywall_lock),
                contentDescription = "Locked",
                tint = PaywallLabel,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = PaywallRowText
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Current plan info card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CurrentPlanCard(planName: String) {
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(PaywallPlanBg)
            .border(BorderStroke(1.dp, PaywallPlanBorder), cardShape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PaywallWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paywall_info),
                contentDescription = null,
                tint = PaywallAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = PaywallTitle, fontWeight = FontWeight.SemiBold)) {
                        append("Current Plan: ")
                    }
                    withStyle(SpanStyle(color = PaywallAccent, fontWeight = FontWeight.SemiBold)) {
                        append(planName)
                    }
                },
                fontSize = 14.5.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Upgrade to unlock advanced features.",
                fontSize = 14.sp,
                color = PaywallBody
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom actions: pinned Upgrade Plan and View Pricing buttons
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PaywallBottomActions(
    onUpgradePlan: () -> Unit,
    onViewPricing: () -> Unit
) {
    val buttonShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaywallBg)
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary button
        Button(
            onClick = onUpgradePlan,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = buttonShape,
                    clip = false,
                    spotColor = PaywallButton.copy(alpha = 0.4f)
                ),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(containerColor = PaywallButton),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Upgrade Plan",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PaywallWhite
            )
            Spacer(Modifier.size(10.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_paywall_rocket),
                contentDescription = null,
                tint = PaywallWhite,
                modifier = Modifier.size(18.dp)
            )
        }

        // Secondary outlined button
        OutlinedButton(
            onClick = onViewPricing,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = buttonShape,
            border = BorderStroke(1.dp, PaywallOutline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = PaywallWhite,
                contentColor = PaywallRowText
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "View Pricing",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PaywallRowText
            )
        }
    }
}