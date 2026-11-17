@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_license

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalLicenseItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// Helper function to parse ISO 8601 date string to Date object
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
            // Continue trying other formats
        }
    }
    return null
}

// Format date for visual representation
private fun formatDisplayDate(rawDate: String?): String {
    val parsed = parseIsoDate(rawDate) ?: return "-"
    return SimpleDateFormat("dd MMM yyyy", Locale.US).format(parsed)
}

// Compute days remaining until expiration
private fun calculateRemainingDays(rawDate: String?): String {
    val target = parseIsoDate(rawDate) ?: return ""
    val diffMillis = target.time - System.currentTimeMillis()
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        diffDays > 0 -> "(${diffDays}d remaining )"
        diffDays == 0L -> "(Expires today)"
        else -> "(${abs(diffDays)}d expired )"
    }
}

// Filter section definitions for License Screen
private fun getLegalLicenseFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Status",
        options = listOf(
            FilterOption("Active", "Active"),
            FilterOption("Pending", "Pending"),
            FilterOption("Expired", "Expired"),
            FilterOption("Suspended", "Suspended")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Risk Level",
        options = listOf(
            FilterOption("Low", "Low"),
            FilterOption("Medium", "Medium"),
            FilterOption("High", "High")
        ),
        isMultiSelect = true
    )
)

@Composable
fun LegalLicenseScreen(
    viewModel: LegalViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddLicense: () -> Unit = {},
    onLicenseClick: (LegalLicenseItemDto) -> Unit = {},
    onEditLicense: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        viewModel.fetchLegalLicenses()
    }

    val licensesPagingState by viewModel.legalLicensesPagingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.licensesError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember { mutableStateOf(getLegalLicenseFilterSections()) }

    var licenseToDelete by remember { mutableStateOf<LegalLicenseItemDto?>(null) }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { it.options.count { opt -> opt.isSelected } }
    }

    val filteredLicenses = remember(licensesPagingState.items, searchQuery, filterSections) {
        val selectedStatuses = filterSections.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()
        val selectedRisks = filterSections.find { it.title == "Risk Level" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()

        licensesPagingState.items.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.licenseName?.contains(searchQuery, ignoreCase = true) == true ||
                    item.licenseCode?.contains(searchQuery, ignoreCase = true) == true ||
                    item.licenseNumber?.contains(searchQuery, ignoreCase = true) == true ||
                    item.issuingAuthority?.contains(searchQuery, ignoreCase = true) == true

            val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesRisk = selectedRisks.isEmpty() || selectedRisks.any { it.equals(item.riskLevel, ignoreCase = true) }

            matchesSearch && matchesStatus && matchesRisk
        }
    }

    val displayPagingState = remember(licensesPagingState, filteredLicenses) {
        licensesPagingState.copy(items = filteredLicenses)
    }

    val fabConfig = remember(onAddLicense) {
        FabConfig(
            label = "Add License",
            icon = Icons.Default.Add,
            onClick = onAddLicense
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        fab = fabConfig,
        fabVisible = !displayPagingState.isInitialLoading
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "All License",
                    onClose = onBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.fetchLegalLicenses(search = it)
                    },
                    placeholder = "Search SO number, customer...",
                    filterCount = activeFilterCount,
                    onFilterClick = { filterDrawerState.open() },
                    accentColor = Primary,
                    borderColor = sectionBorder,
                    textSecondaryColor = mutedText
                )

                when {
                    !errorMessage.isNullOrBlank() && displayPagingState.items.isEmpty() && !displayPagingState.isInitialLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = errorMessage ?: "Failed to load legal licenses",
                                    fontSize = tokens.bodySmall,
                                    color = redText
                                )
                                Spacer(Modifier.height(tokens.extraPadding))
                                Button(
                                    onClick = { viewModel.fetchLegalLicenses() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
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

                    !displayPagingState.isInitialLoading && displayPagingState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No legal licenses found",
                                fontSize = tokens.bodyMedium,
                                color = mutedText
                            )
                        }
                    }

                    else -> {
                        PaginatedDataList(
                            state = displayPagingState,
                            onLoadMore = { viewModel.loadMoreLegalLicenses() },
                            modifier = Modifier.fillMaxSize()
                        ) { licenseItem ->
                            val statusText = licenseItem.status?.replace("_", " ") ?: "-"
                            val codeText = licenseItem.licenseCode ?: licenseItem.licenseNumber ?: "-"
                            val typeText = licenseItem.licenseType ?: "-"
                            val authorityText = licenseItem.issuingAuthority ?: "-"
                            val countryRegionText = listOfNotNull(
                                licenseItem.country ?: licenseItem.countryName,
                                licenseItem.region
                            ).filter { it.isNotBlank() }.joinToString("/")
                            val riskText = licenseItem.riskLevel ?: "-"
                            val expiryDateFormatted = formatDisplayDate(licenseItem.expiryDate)
                            val remainingFormatted = calculateRemainingDays(licenseItem.expiryDate)

                            DataCard(
                                item = licenseItem,
                                title = licenseItem.licenseName ?: "-",
                                titleFontWeight = FontWeight.Medium,
                                titleColor = TextPrimary,
                                topBadgeText = statusText,
                                topBadgeTextColor = orangeText,
                                topBadgeBgColor = orangeBg,
                                topBadgeShowDot = false,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction(label = "Edit", onClick = { onEditLicense(licenseItem.id) }),
                                    MenuAction(label = "View Details", onClick = { onLicenseClick(licenseItem) }),
                                    MenuAction(label = "Delete", textColor = redText, onClick = { licenseToDelete = licenseItem })
                                ),
                                showActionsInHeader = false,
                                showDivider = false,
                                onClick = { onLicenseClick(licenseItem) },
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("License ID", fontSize = tokens.bodySmall, color = mutedText)
                                            Text(
                                                text = codeText,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextPrimary
                                            )
                                        }

                                        Spacer(Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Type", fontSize = tokens.bodySmall, color = mutedText)
                                            Text(typeText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .background(Primary_background)
                                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .padding(vertical = 10.dp, horizontal = 12.dp)
                                        ) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Authority", fontSize = tokens.caption, color = mutedText)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(authorityText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Country", fontSize = tokens.caption, color = mutedText)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(countryRegionText.ifBlank { "-" }, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Risk", fontSize = tokens.caption, color = mutedText)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(riskText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.Normal)) {
                                                    append("Expiry :")
                                                }
                                                withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Medium)) {
                                                    append(expiryDateFormatted)
                                                }
                                                if (remainingFormatted.isNotBlank()) {
                                                    append(" ")
                                                    withStyle(SpanStyle(color = redText, fontWeight = FontWeight.Medium)) {
                                                        append(remainingFormatted)
                                                    }
                                                }
                                            },
                                            fontSize = tokens.bodySmall
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Licenses",
            sections = filterSections,
            onApply = { updatedSections -> filterSections = updatedSections },
            onClearAll = { filterSections = getLegalLicenseFilterSections() }
        )

        licenseToDelete?.let { targetLicense ->
            DeleteModel(
                title = "Delete License",
                message = "You're going to delete \"${targetLicense.licenseName ?: targetLicense.licenseCode}\". Are you sure?",
                checkboxText = "I confirm this deletion",
                cancelText = "Cancel",
                confirmText = "Delete",
                onDismiss = { licenseToDelete = null },
                onDelete = {
                    viewModel.deleteLegalLicense(
                        licenseId = targetLicense.id,
                        onSuccess = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            licenseToDelete = null
                        },
                        onError = { err ->
                            Toast.makeText(context, err ?: "Failed to delete legal license", Toast.LENGTH_SHORT).show()
                            licenseToDelete = null
                        }
                    )
                }
            )
        }
    }
}