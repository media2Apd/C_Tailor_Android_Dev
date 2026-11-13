@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.hr.settings.piece_rate_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.PieceRateItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.currentCurrency
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel

@Composable
fun PieceRateSetupListScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onAddRate: () -> Unit,
    onEditRate: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val currency = currentCurrency()

    val pieceRates by viewModel.pieceRates.collectAsState()
    val isLoading by viewModel.isLoadingPieceRates.collectAsState()
    val canLoadMore by viewModel.canLoadMorePieceRates.collectAsState()
    val isLoadingMore by viewModel.isLoadingMorePieceRates.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<PieceRateItemDto?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchPieceRates(page = 1)
    }

    // Pagination trigger
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= totalItems - 2 && canLoadMore && !isLoadingMore && !isLoading
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMorePieceRates()
        }
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Add Rate +",
            icon = Icons.Default.Add,
            onClick = onAddRate,
            alignment = Alignment.BottomEnd,
            bottomPadding = 80.dp,
            endPadding = 16.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Piece Rate Setup",
                onClose = onBack
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.onPieceRateSearchQueryChanged(it)
                },
                placeholder = "Search piece rates...",
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
                if (isLoading && pieceRates.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary, strokeWidth = 2.5.dp)
                        }
                    }
                } else if (pieceRates.isEmpty()) {
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
                                    text = "No piece rates configured",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                                Text(
                                    text = "Click 'Add Rate +' to create regular and overtime stitching charges.",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText
                                )
                            }
                        }
                    }
                } else {
                    items(pieceRates, key = { it.id }) { rateItem ->
                        val workflowName = rateItem.workflowId?.displayName
                            ?: rateItem.workflowId?.name
                            ?: "Tailoring"
                        val garmentName = rateItem.garmentId?.displayName
                            ?: rateItem.garmentId?.name
                            ?: "Standard Garment"

                        val isScheduled = rateItem.status.equals("Scheduled", ignoreCase = true)
                        val badgeBg = if (isScheduled) Color(0xFFE0F2FE) else greenBg
                        val badgeText = if (isScheduled) Color(0xFF0284C7) else greentext

                        val effectiveDateStr = remember(rateItem.effectiveFrom) {
                            formatDateShort(rateItem.effectiveFrom)
                        }

                        DataCard(
                            item = rateItem,
                            title = workflowName,
                            subtitle = garmentName,
                            topBadgeText = rateItem.status,
                            topBadgeBgColor = badgeBg,
                            topBadgeTextColor = badgeText,
                            actions = listOf(
                                MenuAction(
                                    label = "Edit Rate",
                                    icon = Icons.Default.Edit,
                                    onClick = { onEditRate(rateItem.id) }
                                ),
                                MenuAction(
                                    label = "Delete Rate",
                                    icon = Icons.Default.Delete,
                                    tint = redText,
                                    textColor = redText,
                                    onClick = { itemToDelete = rateItem }
                                )
                            ),
                            onClick = { onEditRate(rateItem.id) },
                            content = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = "REGULAR RATE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = mutedText
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "$currency${rateItem.regularRatePerPiece.toInt()}",
                                                fontSize = tokens.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = title_color
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "OT RATE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = mutedText
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "$currency${rateItem.otRatePerPiece.toInt()}",
                                                fontSize = tokens.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = title_color
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "EFFECTIVE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = mutedText
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = effectiveDateStr,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Tailor Piece Rate",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = mutedText
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text(
                                                text = "Edit",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary,
                                                modifier = Modifier.clickable { onEditRate(rateItem.id) }
                                            )
                                            Text(
                                                text = "History",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = headerGrey,
                                                modifier = Modifier.clickable { onEditRate(rateItem.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation modal
    itemToDelete?.let { target ->
        val workflowName = target.workflowId?.displayName
            ?: target.workflowId?.name
            ?: "Tailoring"
        val garmentName = target.garmentId?.displayName
            ?: target.garmentId?.name
            ?: "Standard Garment"

        DeleteModel(
            title = "Delete Piece Rate",
            message = "You're going to delete \"$workflowName - $garmentName\" piece rate.\nHistorical payroll records will remain preserved.",
            checkboxText = "I confirm this deletion",
            cancelText = "No, Keep It.",
            confirmText = "Yes, Delete!",
            onDismiss = { itemToDelete = null },
            onDelete = {
                val id = target.id
                itemToDelete = null
                // Trigger delete API call for the selected piece rate
                viewModel.deletePieceRate(id)
            }
        )
    }
}

private fun formatDateShort(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val year = parts[0]
            val monthIdx = parts[1].toIntOrNull()?.minus(1) ?: 0
            val day = parts[2]
            "$day ${months.getOrElse(monthIdx) { "Sep" }} $year"
        } else raw
    } catch (_: Exception) {
        raw
    }
}