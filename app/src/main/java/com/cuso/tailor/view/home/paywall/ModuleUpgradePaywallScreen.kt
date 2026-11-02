package com.cuso.tailor.view.home.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.permission.PaywallModuleConfig
import com.cuso.tailor.ui.theme.*

@Composable
fun ModuleUpgradePaywallScreen(
    config: PaywallModuleConfig,
    onClose: () -> Unit,
    onUpgradePlan: () -> Unit,
    onViewPricing: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Scaffold(
        containerColor = whiteBg,
        topBar = {
            // Header with Close Button and Centered Module Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
            ) {
                // Close button (X) inside circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, sectionBorder, CircleShape)
                        .clickable { onClose() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Centered Module Badge
                Surface(
                    color = primary_light,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_warehouse),
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = config.moduleTitle,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Bottom Action Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onUpgradePlan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight * 1.15f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Upgrade Plan",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = whiteBg
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onViewPricing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight * 1.15f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = title_color)
                    ) {
                        Text(
                            text = "View Pricing",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // ── 1. Hero Promo Card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 1.5f))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Primary_background,
                                primary_light.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        primary_light,
                        RoundedCornerShape(tokens.cardCornerRadius * 1.5f)
                    )
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Efficiency Pill
                    Surface(
                        color = whiteBg,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = complete_button_bg,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = config.efficiencyBadge,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                        }
                    }

                    // Circle with Rocket Icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Card Title & Subtitle
                    Text(
                        text = "Optimize Your ${config.moduleKey.replaceFirstChar { it.uppercase() }}",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )

                    Text(
                        text = "Businesses manage ${config.moduleKey} smarter with our tools.",
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── 2. Headline & Description ──
            Text(
                text = config.headline,
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = title_color,
                lineHeight = tokens.h2.times(1.25f)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = config.description,
                fontSize = tokens.bodyMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            // ── 3. Features Included List ──
            Text(
                text = "FEATURES INCLUDED IN THIS UPGRADE",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Bold,
                color = mutedText,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(10.dp))

            config.featuresIncluded.forEach { feature ->
                LockedFeatureItem(
                    title = feature,
                    tokens = tokens
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(14.dp))

            // ── 4. Current Plan Info Card ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = primary_light.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, light_blue_border)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Current Plan: ",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Text(
                                text = config.currentPlan,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }

                        Text(
                            text = "Upgrade to unlock advanced features.",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LockedFeatureItem(
    title: String,
    tokens: com.cuso.tailor.adaptive_screen.AppDesignTokens
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Primary_background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = mutedText,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}