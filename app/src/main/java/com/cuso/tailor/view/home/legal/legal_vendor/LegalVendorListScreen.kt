@file:Suppress("unused", "AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.legal.legal_vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalVendorItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun LegalVendorListScreen(
    viewModel: LegalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val vendors by viewModel.legalVendors.collectAsState()
    val isLoading by viewModel.isLoadingVendors.collectAsState()
    val errorMessage by viewModel.vendorsError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<LegalVendorItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchLegalVendors()
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Vendor Record",
            icon = Icons.Default.Add,
            onClick = onNavigateToCreate
        ),
        fabVisible = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TitleBar(
                title = "Legal",
                onClose = onNavigateBack
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search SO number, customer...",
                filterCount = 0,
                onFilterClick = null
            )

            if (isLoading) {
               ListSkeleton()
            } else if (!errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Failed to fetch vendors",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = redText
                    )
                }
            } else {
                val filteredList = vendors.filter {
                    it.vendor.contains(searchQuery, ignoreCase = true) ||
                            it.legalVendorNumber.contains(searchQuery, ignoreCase = true) ||
                            it.supplierType.contains(searchQuery, ignoreCase = true)
                }

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No vendors matching query" else "No vendor records available",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            val isPending = item.status.equals("Pending", ignoreCase = true)
                            val badgeBg = if (isPending) orangeBg else greenBg
                            val badgeText = if (isPending) orangeText else greentext

                            // Calculate dynamic compliance percentage from API fields
                            val complianceScore = calculateComplianceScore(item)
                            val progressFloat = complianceScore / 100f
                            val progressColor = if (complianceScore >= 80) complete_button_bg else if (complianceScore >= 50) orangeText else redText

                            // Parse dynamic expiry date and remaining days
                            val formattedExpiryDate = formatIsoDate(item.endDate)
                            val (remainingText, isExpired) = calculateDaysRemaining(item.endDate)

                            val actions = listOf(
                                MenuAction(
                                    label = "Edit",
                                    icon = Icons.Default.Edit,
                                    textColor = TextPrimary,
                                    tint = headerGrey,
                                    onClick = { onNavigateToEdit(item.id) }
                                ),
                                MenuAction(
                                    label = "View Details",
                                    icon = Icons.Default.Visibility,
                                    textColor = TextPrimary,
                                    tint = headerGrey,
                                    onClick = { onNavigateToDetail(item.id) }
                                ),
                                MenuAction(
                                    label = "Delete",
                                    icon = Icons.Default.Delete,
                                    textColor = redText,
                                    tint = redText,
                                    onClick = { itemToDelete = item }
                                )
                            )

                            DataCard(
                                item = item,
                                title = item.vendor.ifBlank { "" },
                                titleFontWeight = FontWeight.Medium,
                                titleColor = TextPrimary,
                                topBadgeText = item.status.ifBlank { "" },
                                topBadgeTextColor = badgeText,
                                topBadgeBgColor = badgeBg,
                                actions = actions,
                                onClick = { onNavigateToDetail(item.id) },
                                content = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Record ID Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Record ID",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = item.legalVendorNumber.ifBlank { "N/A" },
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        // Compliance Progress Row (Calculated from API)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Compliance",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextSecondary
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(modifier = Modifier.width(90.dp)) {
                                                    DataCardProgressBar(
                                                        progress = progressFloat,
                                                        progressColor = progressColor,
                                                        trackColor = light_grey,
                                                        height = 6.dp
                                                    )
                                                }
                                                Text(
                                                    text = "$complianceScore%",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }

                                        // Highlight Stat Container (Directly from API)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = Primary_background,
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = sectionBorder,
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                                                )
                                                .padding(tokens.extraPadding),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "Category",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = mutedText
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = item.supplierType.ifBlank { "N/A" },
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "Country",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = mutedText
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = item.displayCountry.ifBlank { "N/A" },
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "Risk",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = mutedText
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = item.riskLevel.ifBlank { "N/A" },
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }

                                        // Dynamic Expiry Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Expiry : $formattedExpiryDate",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = mutedText
                                            )
                                            Text(
                                                text = remainingText,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isExpired) redText else TextSecondary
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        DeleteModel(
            title = "Delete Legal Vendor Record",
            message = "Are you sure you want to delete vendor '${item.vendor}'? This action cannot be reversed.",
            checkboxText = "I understand that this legal record will be removed permanently",
            cancelText = "Cancel",
            confirmText = "Delete Record",
            onDismiss = { itemToDelete = null },
            onDelete = {
                viewModel.deleteLegalVendor(
                    vendorId = item.id,
                    onSuccess = { itemToDelete = null },
                    onError = { itemToDelete = null }
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Dynamic Helper Utilities
// ─────────────────────────────────────────────────────────────

fun calculateComplianceScore(vendor: LegalVendorItem): Int {
    var points = 0
    val totalWeight = 5

    if (vendor.vendorAgreement.isNotEmpty()) points++
    if (vendor.slaDocuments.isNotEmpty()) points++
    if (vendor.supportingDocuments.isNotEmpty()) points++
    if (vendor.status.equals("Active", ignoreCase = true)) points++
    if (vendor.procurementOwner != null || vendor.legalOwner != null) points++

    return (points * 100) / totalWeight
}

fun formatIsoDate(isoDate: String): String {
    if (isoDate.isBlank()) return "N/A"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoDate) ?: return isoDate.take(10)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        formatter.format(date)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

fun calculateDaysRemaining(endDateIso: String): Pair<String, Boolean> {
    if (endDateIso.isBlank()) return Pair("N/A", false)
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val endDate = parser.parse(endDateIso) ?: return Pair("N/A", false)
        val diffMs = endDate.time - System.currentTimeMillis()
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

        if (diffDays >= 0) {
            Pair("(${diffDays}d remaining)", false)
        } else {
            Pair("(Expired ${-diffDays}d ago)", true)
        }
    } catch (_: Exception) {
        Pair("N/A", false)
    }
}