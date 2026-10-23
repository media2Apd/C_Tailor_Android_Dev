
@file:Suppress("unused")
package com.cuso.tailor.view.home.services.customer_complaint

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ComplaintItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.viewmodel.ServicesViewModel

@Composable
fun ComplaintListScreen(
    viewModel: ServicesViewModel,
    onBack: () -> Unit,
    onComplaintClick: (String) -> Unit
) {
    val tokens: AppDesignTokens = LocalAppTokens.current
    val complaints by viewModel.complaintsList.collectAsState()
    val searchQuery by viewModel.complaintSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingComplaints.collectAsState()
    val errorMessage by viewModel.complaintsError.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchComplaints()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.7f)
                ) {
                    // Header Bar with Arrow Back and Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(tokens.buttonHeight * 0.8f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = title_color,
                                modifier = Modifier.size(tokens.iconSize * 1.1f)
                            )
                        }
                        Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                        Text(
                            text = "Complaint List",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Search & Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onComplaintSearch(it) },
                            placeholder = {
                                Text(
                                    text = "Search SO number, customer...",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = mutedText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BorderGray,
                                focusedBorderColor = Primary,
                                unfocusedContainerColor = lightGray,
                                focusedContainerColor = whiteBg
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.fieldHeight)
                        )

                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                        // Filter Button
                        Surface(
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            border = BorderStroke(1.dp, BorderGray),
                            color = whiteBg,
                            modifier = Modifier
                                .size(tokens.fieldHeight)
                                .clickable { /* Open filter sheet if needed */ }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = "Filter",
                                    tint = title_color,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalDivider(
                color = dividerColor,
                thickness = 1.dp
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (!errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Error loading complaints",
                        color = redText,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (complaints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No complaints found",
                        color = mutedText,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = tokens.extraPadding * 0.5f)
                ) {
                    items(complaints) { item ->
                        ComplaintDataCardItem(
                            item = item,
                            onClick = { onComplaintClick(item.id) }
                        )
                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reuses the DataCard component configured for the Complaint item layout
 */
@Composable
private fun ComplaintDataCardItem(
    item: ComplaintItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Format status label and color strictly with Colors.kt
    val rawStatus = item.status?.lowercase() ?: "in progress"
    val (statusLabel, statusBg, statusText) = when {
        rawStatus.contains("resolve") || rawStatus.contains("close") -> {
            Triple("Resolved", greenBg, greentext)
        }
        rawStatus.contains("reject") || rawStatus.contains("cancel") -> {
            Triple("Cancelled", redBg, redText)
        }
        else -> {
            Triple("In Progress", yellowBg, yellowText)
        }
    }

    DataCard(
        item = item,
        onClick = { onClick() },
        showDivider = false,
        // Header title: Customer Name
        title = item.customerDisplayName,
        titleColor = title_color,
        titleFontWeight = FontWeight.Medium,
        // Subtitle: Complaint Code
        subtitle = item.displayCode,
        // Status Badge & Action Menu on top row
        topBadgeText = statusLabel,
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusText,
        topBadgeShowDot = false,
        topBadgeInline = true,
        actions = listOf(
            MenuAction(
                label = "View Details",
                tint = headerGrey,
                textColor = TextPrimary,
                onClick = onClick
            )
        ),
        // 3-Column Footer Card (Contact | Category | Complaint)
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                    .background(light_blue)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contact
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Contact",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.contactPhone,
                            fontSize = tokens.bodySmall,
                            color = title_color,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Category
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "Category",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.categoryDisplay,
                            fontSize = tokens.bodySmall,
                            color = title_color,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Complaint
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Complaint",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.complaintDisplay,
                            fontSize = tokens.bodySmall,
                            color = title_color,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    )
}