package com.cuso.tailor.view.home.subscriptions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.cuso.tailor.view.home.DatabaseEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.permission.PlanFeatureModuleGroup
import com.cuso.tailor.model.permission.PlanLimitsData
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.darkPurple
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.toTitleCase
import com.cuso.tailor.viewmodel.PermissionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun CurrentPlanScreen(
    permissionViewModel: PermissionViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onManagePayment: () -> Unit = {},
    onCancelSubscription: () -> Unit = {},
    onUpgradePlan: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    var portalName by remember { mutableStateOf("") }

    val planLimits by permissionViewModel.planLimits.collectAsStateWithLifecycle()
    val includedModules by permissionViewModel.includedPlanModules.collectAsStateWithLifecycle()
    val isLoading by permissionViewModel.isLoadingPlanLimits.collectAsStateWithLifecycle()
    val errorMessage by permissionViewModel.planLimitsError.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        permissionViewModel.fetchPlanLimitsAndModules(forceRefreshModules = true)

        // Load portalName dynamically from Room settings table
        withContext(Dispatchers.IO) {
            try {
                val db = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    DatabaseEntryPoint::class.java
                ).appDatabase()
                val cursor = db.openHelper.readableDatabase.query("SELECT portalName FROM settings LIMIT 1")
                cursor.use {
                    if (it.moveToFirst()) {
                        val colIndex = it.getColumnIndex("portalName")
                        if (colIndex != -1) {
                            portalName = it.getString(colIndex).orEmpty()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val targetPortal = portalName.trim()
    val pricingUrl = if (targetPortal.isNotEmpty()) {
        "http://tailor.cuso.in/$targetPortal/home/settings/pricing"
    } else {
        "http://tailor.cuso.in/home/settings/pricing"
    }
    val billingUrl = if (targetPortal.isNotEmpty()) {
        "http://tailor.cuso.in/$targetPortal/home/settings/billing"
    } else {
        "http://tailor.cuso.in/home/settings/billing"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = "Current Plan",
            onClose = onClose
        )

        HorizontalDivider(color = grey_border, thickness = 1.dp)

        when {
            isLoading && planLimits == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            planLimits == null && errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = redText,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                permissionViewModel.fetchPlanLimitsAndModules(forceRefreshModules = true)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                            modifier = Modifier.height(tokens.buttonHeight)
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }

            planLimits != null -> {
                CurrentPlanContent(
                    data = planLimits!!,
                    includedModules = includedModules,
                    onManagePayment = {
                        openExternalPortalUrl(context, pricingUrl)
                        onManagePayment()
                    },
                    onCancelSubscription = {
                        openExternalPortalUrl(context, billingUrl)
                        onCancelSubscription()
                    },
                    onUpgradePlan = {
                        openExternalPortalUrl(context, pricingUrl)
                        onUpgradePlan()
                    }
                )
            }
        }
    }
}

@Composable
private fun CurrentPlanContent(
    data: PlanLimitsData,
    includedModules: List<PlanFeatureModuleGroup>,
    onManagePayment: () -> Unit,
    onCancelSubscription: () -> Unit,
    onUpgradePlan: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val subscription = data.subscription
    val limits = data.limits
    val usage = data.usage

    val isTrialing = subscription?.status.equals("trialing", ignoreCase = true)
    val isActiveStatus = subscription?.status.equals("active", ignoreCase = true) || isTrialing
    val planCodeText = subscription?.planCode ?: data.activePlanCode ?: "-"
    val planTitleText = (subscription?.planName ?: planCodeText).uppercase()
    val billingPeriodText = subscription?.billingPeriod?.toTitleCase().orEmpty().ifBlank { "-" }
    val startDateText = formatPlanDate(subscription?.startDate)
    val endDateText = formatPlanDate(subscription?.endDate)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        // 1. Hero Subscription Card + 2x2 Subscription Metadata Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                CurrentPlanHeroCard(
                    planTitle = planTitleText,
                    status = subscription?.status.orEmpty(),
                    isTrialing = isTrialing,
                    isActiveStatus = isActiveStatus,
                    billingPeriod = billingPeriodText,
                    currency = subscription?.currency.orEmpty(),
                    gateway = subscription?.gatewayUsed?.toTitleCase().orEmpty(),
                    endDateText = endDateText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    PlanInfoTile(
                        label = "PLAN CODE",
                        value = planCodeText,
                        modifier = Modifier.weight(1f)
                    )
                    PlanInfoTile(
                        label = "BILLING CYCLE",
                        value = billingPeriodText,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    PlanInfoTile(
                        label = "START DATE",
                        value = startDateText,
                        modifier = Modifier.weight(1f)
                    )
                    PlanInfoTile(
                        label = if (isTrialing) "TRIAL ENDS" else "RENEWAL DATE",
                        value = endDateText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Plan Limits Section (from /api/plans/limits)
        if (limits != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding)
                    ) {
                        Text(
                            text = "Plan Limits",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        val limitRows = listOf(
                            "Users" to formatUsageLimit(usage?.users, limits.userLimit),
                            "Branches" to formatUsageLimit(usage?.branches, limits.branchLimit),
                            "Employees" to formatUsageLimit(usage?.employees, limits.employeeLimit),
                            "Storage" to formatStorageLimit(usage?.storageBytes, limits.storageLimitBytes),
                            "Orders / month" to formatUsageLimit(usage?.monthlyOrders, limits.orderLimit)
                        )

                        limitRows.forEachIndexed { index, (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = tokens.extraPadding * 0.9f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = value,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            if (index != limitRows.lastIndex) {
                                HorizontalDivider(color = grey_border, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Included Features Section (from /api/common/plan-setups/customer/my-plan-modules)
        if (includedModules.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = "Included Features",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        includedModules.forEach { group ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                            ) {
                                Text(
                                    text = group.moduleTitle,
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.SemiBold,
                                    color = mutedText
                                )

                                group.features.forEach { featureLabel ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = greentext,
                                            modifier = Modifier.size(tokens.iconSize * 0.85f)
                                        )
                                        Text(
                                            text = featureLabel,
                                            fontSize = tokens.bodySmall,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Subscription Summary & Actions Section (all values from /api/plans/limits)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Text(
                        text = "Subscription Details",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )

                    SummaryRow(
                        label = "Status",
                        value = subscription?.status?.toTitleCase().orEmpty().ifBlank { "-" }
                    )
                    SummaryRow(
                        label = "Currency",
                        value = subscription?.currency?.uppercase().orEmpty().ifBlank { "-" }
                    )
                    SummaryRow(
                        label = "Payment Gateway",
                        value = subscription?.gatewayUsed?.toTitleCase().orEmpty().ifBlank { "-" }
                    )
                    SummaryRow(
                        label = "Tracked Storage Assets",
                        value = formatNumber(usage?.trackedStorageAssets ?: 0)
                    )

                    HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Unlocked Modules",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${data.unlockedModules.size}",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        PlanOutlineButton(
                            text = "Manage Payment",
                            textColor = Primary,
                            borderColor = Primary.copy(alpha = 0.45f),
                            onClick = onManagePayment,
                            modifier = Modifier.weight(1f)
                        )
                        PlanOutlineButton(
                            text = "Cancel Subscription",
                            textColor = redText,
                            borderColor = redText.copy(alpha = 0.55f),
                            onClick = onCancelSubscription,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = onUpgradePlan,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight * 1.15f)
                    ) {
                        Text(
                            text = "Upgrade Plan",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentPlanHeroCard(
    planTitle: String,
    status: String,
    isTrialing: Boolean,
    isActiveStatus: Boolean,
    billingPeriod: String,
    currency: String,
    gateway: String,
    endDateText: String
) {
    val tokens = LocalAppTokens.current
    val heroGradient = Brush.linearGradient(
        colors = listOf(BluePrimary, Primary, darkPurple)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(heroGradient)
            .padding(tokens.cardPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT PLAN",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = whiteBg.copy(alpha = 0.85f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTrialing) {
                        PlanStatusPill(
                            text = "FREE TRIAL",
                            bgColor = whiteBg.copy(alpha = 0.2f),
                            textColor = whiteBg
                        )
                    }

                    val statusBadgeText = if (isActiveStatus) "ACTIVE" else status.uppercase().ifBlank { "-" }
                    PlanStatusPill(
                        text = statusBadgeText,
                        bgColor = if (isActiveStatus) greenBg else yellowBg,
                        textColor = if (isActiveStatus) greentext else yellowText
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.3f)
                ) {
                    Text(
                        text = planTitle,
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (gateway.isNotBlank()) {
                        Text(
                            text = "Managed via $gateway",
                            fontSize = tokens.caption,
                            color = whiteBg.copy(alpha = 0.85f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (currency.isNotBlank()) {
                        Text(
                            text = currency.uppercase(),
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = whiteBg
                        )
                    }
                    Text(
                        text = billingPeriod,
                        fontSize = tokens.caption,
                        color = whiteBg.copy(alpha = 0.85f)
                    )
                }
            }

            HorizontalDivider(
                color = whiteBg.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            val footerText = if (isTrialing) {
                "Billed ${billingPeriod.lowercase()} after trial ends ($endDateText)"
            } else {
                "Billed ${billingPeriod.lowercase()} • Next renewal $endDateText"
            }

            Text(
                text = footerText,
                fontSize = tokens.caption,
                color = whiteBg.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun PlanStatusPill(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(bgColor)
            .padding(
                horizontal = tokens.extraPadding * 0.8f,
                vertical = tokens.extraPadding * 0.25f
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun PlanInfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.extraPadding * 1.2f, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.35f)
        ) {
            Text(
                text = label,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = title_color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
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
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun PlanOutlineButton(
    text: String,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = tokens.extraPadding * 0.8f),
        modifier = modifier.height(tokens.buttonHeight)
    ) {
        Text(
            text = text,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatPlanDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "-"
    return try {
        val cleanDate = isoString.substringBefore(".").substringBefore("Z")
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(cleanDate) ?: return isoString.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        isoString.take(10)
    }
}

private fun formatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}

private fun formatUsageLimit(used: Int?, limit: Int): String {
    val formattedLimit = formatNumber(limit)
    return if (used != null) {
        "${formatNumber(used)} / $formattedLimit"
    } else {
        formattedLimit
    }
}

private fun formatStorageLimit(usedBytes: Long?, limitBytes: Long): String {
    val limitLabel = formatBytes(limitBytes)
    return if (usedBytes != null) {
        "${formatBytes(usedBytes)} / $limitLabel"
    } else {
        limitLabel
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    val rounded = if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
    return "$rounded ${units[unitIndex]}"
}

/**
 * Opens the external web portal pricing or billing URL in the default browser.
 */
private fun openExternalPortalUrl(context: Context, url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(browserIntent)
}