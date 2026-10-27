@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.legal.legal_license

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalLicenseItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCardProgressBar
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import androidx.core.net.toUri
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton

// Helper function to parse ISO 8601 date strings
private fun parseIsoDate(rawDate: String?): Date? {
    if (rawDate.isNullOrBlank()) return null
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (format in formats) {
        try {
            val parser = SimpleDateFormat(format, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(rawDate)
            if (date != null) return date
        } catch (_: Exception) {
            // Try next format
        }
    }
    return null
}

// Format date into readable string like "28 Aug 2026"
private fun formatDisplayDate(rawDate: String?): String {
    val date = parseIsoDate(rawDate) ?: return "-"
    return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
}

// Compute days remaining until expiration
private fun calculateDaysRemaining(rawDate: String?): Long {
    val target = parseIsoDate(rawDate) ?: return 0L
    val diffMillis = target.time - System.currentTimeMillis()
    return TimeUnit.MILLISECONDS.toDays(diffMillis)
}

// Compute validity progress percentage between issue date and expiry date
private fun computeValidityPercentage(issueDateStr: String?, expiryDateStr: String?): Float {
    val start = parseIsoDate(issueDateStr)?.time ?: return 1f
    val end = parseIsoDate(expiryDateStr)?.time ?: return 1f
    val now = System.currentTimeMillis()

    if (end <= start) return 1f
    val progress = (end - now).toFloat() / (end - start).toFloat()
    return progress.coerceIn(0f, 1f)
}

@SuppressLint("UseKtx")
@Composable
fun LegalLicenseDetailScreen(
    licenseId: String,
    viewModel: LegalViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditLicense: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Fetch license detail on entry
    LaunchedEffect(licenseId) {
        if (licenseId.isNotBlank()) {
            viewModel.fetchLicenseDetail(licenseId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLicenseDetail()
        }
    }

    val licenseDetail by viewModel.licenseDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLicenseDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.licenseDetailError.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            TabItem("Overview"),
            TabItem("Registration Info"),
            TabItem("Documents"),
            TabItem("Renewal"),
            TabItem("History")
        )
    }

    if (isLoading && licenseDetail == null) {
        ListSkeleton()
        return
    }

    val license = licenseDetail
    if (license == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = errorMessage ?: "License not found",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = redText
                )
                Spacer(Modifier.height(tokens.extraPadding))
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text(
                        text = "Go Back",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
        return
    }

    // Prepare header details
    val statusText = license.status?.replace("_", " ") ?: "Active"
    val daysRemaining = calculateDaysRemaining(license.expiryDate)
    val daysLabel = if (daysRemaining >= 0) "$daysRemaining Days" else "${abs(daysRemaining)} Days ago"
    val validityProgress = computeValidityPercentage(license.issueDate, license.expiryDate)
    val validityPercentText = "${(validityProgress * 100).toInt()}% Valid"

    // Check if the current date is strictly after expiryDate
    val expiryTimestamp = parseIsoDate(license.expiryDate)?.time
    val isExpired = expiryTimestamp != null && System.currentTimeMillis() > expiryTimestamp

    var showRenewDialog by remember { mutableStateOf(false) }
    val isRenewing by viewModel.isRenewingLicense.collectAsStateWithLifecycle()

    val headerData = DetailHeaderData(
        icon = Icons.Default.Description,
        badges = listOf(
            DetailBadge(
                text = if (isExpired) "Expired" else if (daysRemaining in 0..90) "Expiring Soon" else statusText,
                textColor = if (isExpired) redText else if (daysRemaining in 0..90) orangeText else greentext,
                bgColor = if (isExpired) redBg else if (daysRemaining in 0..90) orangeBg else greenBg,
                showDot = true
            )
        ),
        title = "${license.licenseName ?: "License"} – ${license.country ?: license.countryName ?: "General"}",
        metaItems = listOfNotNull(
            license.licenseCode ?: license.licenseNumber,
            license.licenseType,
            license.country ?: license.countryName
        ).filter { it.isNotBlank() },
        secondaryActions = listOf(
            DetailAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                onClick = { onEditLicense(license.id) }
            ),
            DetailAction(
                label = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    val firstDocUrl = license.licenseCertificate?.firstOrNull()?.url
                    if (!firstDocUrl.isNullOrBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, firstDocUrl.toUri()))
                    } else {
                        Toast.makeText(context, "No certificate available to download", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            DetailAction(
                label = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    val shareText = "License: ${license.licenseName}\nCode: ${license.licenseCode}\nAuthority: ${license.issuingAuthority}"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share License Details"))
                }
            )
        ),
        // Enabled ONLY if current date has passed the expiryDate
        primaryAction = DetailAction(
            label = "Renew License",
            icon = Icons.Default.Refresh,
            enabled = isExpired,
            onClick = {
                showRenewDialog = true
            }
        )
    )

    // Summary statistics displayed below header
    val summaryStats = listOf(
        DetailStat(
            label = "Status",
            value = statusText,
            valueColor = if (statusText.equals("Active", true)) greentext else orangeText,
            showDot = true
        ),
        DetailStat(
            label = "Risk Profile",
            value = "${license.riskLevel ?: "Low"} Risk",
            valueColor = if (license.riskLevel.equals("High", true)) redText else Primary,
            showDot = true
        ),
        DetailStat(
            label = "Type",
            value = license.licenseType ?: "Perpetual",
            valueColor = TextPrimary
        ),
        DetailStat(
            label = "Expiry Date",
            value = formatDisplayDate(license.expiryDate),
            valueColor = TextPrimary
        ),
        DetailStat(
            label = "Authority",
            value = license.issuingAuthority ?: "-",
            valueColor = TextPrimary
        )
    )

    DetailScreenTemplate(
        title = "Licenses & Registrations",
        onClose = onClose,
        header = headerData,
        summaryStats = summaryStats,
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it }
    ) { currentTab ->
        when (currentTab) {
            0 -> OverviewTabContent(
                license = license,
                daysLabel = daysLabel,
                validityProgress = validityProgress,
                validityPercentText = validityPercentText,
                onEditClick = { onEditLicense(license.id) }
            )
            1 -> RegistrationInfoTabContent(license = license)
            2 -> DocumentsTabContent(license = license)
            3 -> RenewalTabContent(license = license)
            4 -> HistoryTabContent(license = license)
        }
    }
    if (showRenewDialog) {
        // Expiry Date-க்கு அடுத்த நாள் (Next Day) கணக்கிடப்படுகிறது
        val nextDayEffective = calculateNextDay(license.expiryDate)

        RenewLicenseDialog(
            endedDateText = formatDisplayDate(license.expiryDate),
            initialEffectiveDate = nextDayEffective, // 👈 Expiry Date + 1 நாள் அனுப்பப்படுகிறது
            isLoading = isRenewing,
            onDismiss = { showRenewDialog = false },
            onConfirm = { effectiveDate, expiryDate, notes ->
                viewModel.renewLegalLicense(
                    licenseId = license.id,
                    effectiveDate = effectiveDate,
                    expiryDate = expiryDate,
                    notes = notes,
                    onSuccess = {
                        showRenewDialog = false
                        Toast.makeText(context, "License renewed successfully", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err ?: "Failed to renew license", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}
// ─────────────────────────────────────────────────────────────
// Renew License Dialog
// ─────────────────────────────────────────────────────────────

@Composable
fun RenewLicenseDialog(
    endedDateText: String,
    initialEffectiveDate: String = "",
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (effectiveDate: String, expiryDate: String, notes: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var effectiveDate by remember(initialEffectiveDate) { mutableStateOf(initialEffectiveDate) } // 👈 Expiry Date-க்கு அடுத்த நாள் prefill ஆகும்
    var expiryDate by remember { mutableStateOf("") }
    var renewalNotes by remember { mutableStateOf("") }
    val context = LocalContext.current

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(tokens.cardCornerRadius)),
            color = whiteBg,
            shape = RoundedCornerShape(tokens.cardCornerRadius)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
            ) {
                // Header Row: Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Renew License",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable(enabled = !isLoading) { onDismiss() }
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Informational text showing when the current term ended
                Text(
                    text = "The current term ended on $endedDateText. The renewed term has to start after this date.",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    lineHeight = tokens.caption.times(1.4f)
                )

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                // Date Fields in 2 columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Effective Date", isRequired = true)
                        DatePickerField(
                            value = effectiveDate,
                            onDateSelected = { effectiveDate = it },
                            enabled = false
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Expiry Date", isRequired = true)
                        DatePickerField(
                            value = expiryDate,
                            onDateSelected = { expiryDate = it },
                            enabled = !isLoading
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                // Renewal Notes Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "Renewal Notes", isRequired = false)
                    FormTextArea(
                        value = renewalNotes,
                        onValueChange = { renewalNotes = it },
                        placeholder = "Enter renewal notes",
                        enabled = !isLoading,
                        minLines = 3,
                        maxLines = 4
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.5f))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (effectiveDate.isBlank()) {
                                Toast.makeText(context, "Effective date is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (expiryDate.isBlank()) {
                                Toast.makeText(context, "Expiry date is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onConfirm(effectiveDate, expiryDate, renewalNotes)
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Renew License",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to calculate the day after expiry date (Expiry Date + 1 Day)
private fun calculateNextDay(rawDate: String?): String {
    val date = parseIsoDate(rawDate) ?: return ""
    val calendar = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, 1)
    }
    return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(calendar.time)
}
// ─────────────────────────────────────────────────────────────
// Tab 0: Overview
// ─────────────────────────────────────────────────────────────

@Composable
private fun OverviewTabContent(
    license: LegalLicenseItemDto,
    daysLabel: String,
    validityProgress: Float,
    validityPercentText: String,
    onEditClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Card 1: Validity Status
    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "Validity Status",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(greentext, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "License ${license.status ?: "Active"}",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Text(
                    text = validityPercentText,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = headerGrey
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress indicator bar
            DataCardProgressBar(
                progress = validityProgress,
                progressColor = Primary,
                trackColor = light_grey,
                height = 6.dp
            )

            Spacer(Modifier.height(tokens.extraPadding))

            // Box for days remaining and next renewal date
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                    .background(Primary_background)
                    .border(1.dp, title_border, RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                    .padding(tokens.extraPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Days Remaining:",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                        Text(
                            text = daysLabel,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Next Renewal Date:",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                        Text(
                            text = formatDisplayDate(license.renewalDate ?: license.expiryDate),
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            // Renewal Window Alert Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                    .background(yellowBg)
                    .border(1.dp, yellowText.copy(alpha = 0.35f), RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                    .padding(tokens.extraPadding)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = yellowText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Renewal Window Alert",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = activity_brown
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Renewal begins ${license.renewalNoticePeriod ?: 30} days before expiration. Auto-reminders will trigger for ${license.responsibleTeam?.name ?: "Responsible Team"}.",
                            fontSize = tokens.caption,
                            color = activity_brown
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(tokens.extraPadding))

    // Card 2: License Details
    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "License Details",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit License",
                    tint = headerGrey,
                    modifier = Modifier
                        .size(tokens.iconSize)
                        .clickable { onEditClick() }
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailStackedField(
                    label = "License ID",
                    value = license.licenseCode ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailStackedField(
                    label = "Type",
                    value = license.licenseType ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailStackedField(
                    label = "Certificate Name",
                    value = license.licenseName ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailStackedField(
                    label = "Expiry Date",
                    value = formatDisplayDate(license.expiryDate),
                    modifier = Modifier.weight(1f)
                )
            }

            // Registration Number with monospace container
            Column(modifier = Modifier.padding(vertical = tokens.extraPadding)) {
                Text(
                    text = "REGISTRATION NUMBER",
                    fontSize = tokens.label,
                    color = mutedText
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeGrey)
                        .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = license.licenseNumber ?: "-",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailStackedField(
                    label = "Issuing Authority",
                    value = license.issuingAuthority ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailStackedField(
                    label = "Jurisdiction",
                    value = listOfNotNull(license.region, license.jurisdiction, license.country ?: license.countryName).distinct().joinToString(", "),
                    modifier = Modifier.weight(1f)
                )
            }

            DetailStackedField(
                label = "Created By",
                value = license.responsibleTeam?.name ?: license.legalEntity ?: "Operations Team"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 1: Registration Info
// ─────────────────────────────────────────────────────────────

@Composable
private fun RegistrationInfoTabContent(license: LegalLicenseItemDto) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registration Details",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                DetailPill(
                    badge = DetailBadge(
                        text = "Official Record",
                        textColor = Primary,
                        bgColor = primary_light,
                        showDot = false
                    )
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            // Main Code / Registration identifier box with copy icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                    .background(Primary_background)
                    .border(1.dp, title_border, RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REGISTRATION CODE",
                            fontSize = tokens.label,
                            color = mutedText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = license.licenseNumber ?: license.licenseCode ?: "-",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = headerGrey,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Registration Code", license.licenseNumber ?: license.licenseCode.orEmpty())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailStackedField(
                    label = "Legal Name",
                    value = license.legalEntity ?: license.licenseName ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailStackedField(
                    label = "Registration Type",
                    value = license.licenseType ?: "Regular",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailStackedField(
                    label = "Issuing Authority",
                    value = license.issuingAuthority ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailStackedField(
                    label = "Status",
                    value = license.status ?: "Active",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailStackedField(
                    label = "Branch",
                    value = license.branchId?.name ?: "Main Branch",
                    modifier = Modifier.weight(1f)
                )
                DetailStackedField(
                    label = "Effective Date",
                    value = formatDisplayDate(license.effectiveDate),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailStackedField(
                    label = "Jurisdiction",
                    value = license.jurisdiction ?: license.region ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailStackedField(
                    label = "Region",
                    value = license.region ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }

            DetailStackedField(
                label = "Notes",
                value = license.notes ?: "No additional notes provided"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 2: Documents
// ─────────────────────────────────────────────────────────────

@Composable
private fun DocumentsTabContent(license: LegalLicenseItemDto) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val allDocuments = (license.licenseCertificate.orEmpty() + license.supportingDocuments.orEmpty()).distinctBy { it.id ?: it.url }

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "License Documents",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Button(
                onClick = {
                    Toast.makeText(context, "Upload Document", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary_light,
                    contentColor = Primary
                ),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Upload",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }

        if (allDocuments.isEmpty()) {
            DetailCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding * 1.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No documents attached to this license",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            }
        } else {
            allDocuments.forEach { doc ->
                val fileName = doc.originalFilename ?: "Document.pdf"
                val uploadedDate = formatDisplayDate(doc.uploadedAt)

                DetailDocumentCard(
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    name = fileName,
                    uploadedText = "Uploaded $uploadedDate",
                    typeLabel = if (fileName.endsWith(".pdf", ignoreCase = true)) "PDF" else "FILE",
                    tags = listOf("Certificate", "Verified"),
                    onReview = {
                        if (!doc.url.isNullOrBlank()) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, doc.url.toUri()))
                        } else {
                            Toast.makeText(context, "Document URL not available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onView = {
                        if (!doc.url.isNullOrBlank()) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, doc.url.toUri()))
                        } else {
                            Toast.makeText(context, "Document URL not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 3: Renewal
// ─────────────────────────────────────────────────────────────

@Composable
private fun RenewalTabContent(license: LegalLicenseItemDto) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        // Renewal Management Card
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Renewal Management",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(
                        label = "Compliance Status",
                        value = license.status ?: "Active",
                        modifier = Modifier.weight(1f)
                    )
                    DetailStackedField(
                        label = "Renewal Status",
                        value = license.renewalStatus?.replace("_", " ") ?: "Not Required",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)

                DetailStackedField(
                    label = "Reason",
                    value = license.description ?: "License rules do not require annual periodic renewals unless explicitly requested by authorities."
                )

                DetailStackedField(
                    label = "Renewal Officer",
                    value = license.responsibleTeam?.name ?: "Senior Compliance Lead"
                )

                Spacer(Modifier.height(tokens.extraPadding))

                // Action Buttons
                Button(
                    onClick = {
                        Toast.makeText(context, "Renewal initiated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Initiate Renewal",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Reminder scheduled", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    border = BorderStroke(1.dp, sectionBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(
                        text = "Schedule Reminder",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Compliance Insights Card
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "COMPLIANCE INSIGHTS",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(tokens.extraPadding))

                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(8.dp)
                            .background(greentext, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "No Action Required",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "This registration is currently valid. Keep verified certificates up to date in the documents tab.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(8.dp)
                            .background(Primary, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Next Audit Schedule",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Next review window scheduled for ${formatDisplayDate(license.renewalDate ?: license.expiryDate)}.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 4: History
// ─────────────────────────────────────────────────────────────

@Composable
private fun HistoryTabContent(license: LegalLicenseItemDto) {
    val tokens = LocalAppTokens.current

    val activities = license.activities.orEmpty()
    val timelineItems = if (activities.isNotEmpty()) {
        activities.map { act ->
            val dateObj = parseIsoDate(act.performedAt)
            val dateStr = if (dateObj != null) SimpleDateFormat("dd MMM yyyy", Locale.US).format(dateObj) else "-"
            val timeStr = if (dateObj != null) SimpleDateFormat("hh:mm a", Locale.US).format(dateObj) else ""

            DetailActivityItem(
                date = dateStr,
                time = timeStr,
                title = act.activityType ?: "Updated",
                subtitle = act.description ?: "License modified"
            )
        }
    } else {
        listOf(
            DetailActivityItem(
                date = formatDisplayDate(license.createdAt),
                time = "",
                title = "License Created / Issued",
                subtitle = "Initial creation by system"
            )
        )
    }

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "License History",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            DetailActivityTimeline(items = timelineItems)
        }
    }
}