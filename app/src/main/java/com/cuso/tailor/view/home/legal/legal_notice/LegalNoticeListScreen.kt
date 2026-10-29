@file:Suppress("unused", "AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.legal.legal_notice

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
import com.cuso.tailor.model.legal.LegalNoticeItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun LegalNoticeListScreen(
    viewModel: LegalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val notices by viewModel.legalNotices.collectAsState()
    val isLoading by viewModel.isLoadingNotices.collectAsState()
    val errorMessage by viewModel.noticesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<LegalNoticeItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchLegalNotices()
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Notice / Dispute",
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
                title = "Legal Notice & Dispute",
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
                    modifier = Modifier.fillMaxSize().padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Failed to fetch legal notices",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = redText
                    )
                }
            } else {
                val filteredList = notices.filter {
                    it.caseTitle.contains(searchQuery, ignoreCase = true) ||
                            it.caseNumber.contains(searchQuery, ignoreCase = true) ||
                            it.partyType.contains(searchQuery, ignoreCase = true)
                }

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(tokens.screenPadding), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No records matching query" else "No dispute or notice records available",
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
                            val isPending = item.status.equals("Pending", ignoreCase = true) || item.status.equals("Under_Review", ignoreCase = true)
                            val badgeBg = if (isPending) orangeBg else greenBg
                            val badgeText = if (isPending) orangeText else greentext

                            val formattedDeadline = formatNoticeIsoDate(item.nextDeadline)
                            val (remainingText, isExpired) = calculateNoticeRemaining(item.nextDeadline)

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
                                title = item.caseTitle.ifBlank { "Untitled Dispute" },
                                titleFontWeight = FontWeight.Medium,
                                titleColor = TextPrimary,
                                topBadgeText = item.status.ifBlank { "Active" },
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
                                                text = item.caseNumber.ifBlank { "N/A" },
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        // Party Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Party",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = item.partyType.ifBlank { "N/A" },
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        // 4-column Stat Container: Category, Country, Type, Risk
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Primary_background, RoundedCornerShape(tokens.cardCornerRadius))
                                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                                                .padding(tokens.extraPadding),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Category", fontSize = tokens.caption, color = mutedText)
                                                Text(item.caseType.ifBlank { "Commercial" }, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Country", fontSize = tokens.caption, color = mutedText)
                                                Text(item.displayCountry.ifBlank { "India" }, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Type", fontSize = tokens.caption, color = mutedText)
                                                Text(item.caseType.ifBlank { "Notice" }, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Risk", fontSize = tokens.caption, color = mutedText)
                                                Text(item.riskLevel.ifBlank { "Low" }, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            }
                                        }

                                        // Next Action Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Next Action : $formattedDeadline",
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
            title = "Delete Legal Dispute Record",
            message = "Are you sure you want to delete case '${item.caseTitle}'? This action cannot be reversed.",
            checkboxText = "I understand that this legal case will be removed permanently",
            cancelText = "Cancel",
            confirmText = "Delete Record",
            onDismiss = { itemToDelete = null },
            onDelete = {
                viewModel.deleteLegalNotice(
                    noticeId = item.id,
                    onSuccess = { itemToDelete = null },
                    onError = { itemToDelete = null }
                )
            }
        )
    }
}

private fun formatNoticeIsoDate(isoDate: String): String {
    if (isoDate.isBlank()) return "N/A"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoDate) ?: return isoDate.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun calculateNoticeRemaining(dateIso: String): Pair<String, Boolean> {
    if (dateIso.isBlank()) return Pair("N/A", false)
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val target = parser.parse(dateIso) ?: return Pair("N/A", false)
        val diffMs = target.time - System.currentTimeMillis()
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