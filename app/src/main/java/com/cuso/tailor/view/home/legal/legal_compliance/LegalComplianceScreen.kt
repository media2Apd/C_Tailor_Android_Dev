@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_compliance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.cuso.tailor.model.legal.LegalComplianceDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ── Date and Countdown Helpers ──

private fun parseDateString(rawDate: String?): Date? {
    if (rawDate.isNullOrBlank()) return null
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (pattern in formats) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(rawDate)
            if (date != null) return date
        } catch (_: Exception) {}
    }
    return null
}

private fun formatDueDate(rawDate: String?): String {
    val date = parseDateString(rawDate) ?: return "-"
    return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
}

private fun calculateDaysRemaining(rawDate: String?): String {
    val target = parseDateString(rawDate) ?: return ""
    val diffMillis = target.time - System.currentTimeMillis()
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        diffDays > 0 -> "(${diffDays}d remaining )"
        diffDays == 0L -> "(Due today)"
        else -> "(${kotlin.math.abs(diffDays)}d overdue )"
    }
}

// ── Filter Options ──

private fun getLegalComplianceFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Status",
        options = listOf(
            FilterOption("Pending", "Pending"),
            FilterOption("Active", "Active"),
            FilterOption("Completed", "Completed"),
            FilterOption("Under_Review", "Under Review")
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
    ),
    FilterSection(
        title = "Category",
        options = listOf(
            FilterOption("Tax", "Tax"),
            FilterOption("Labor", "Labor"),
            FilterOption("Corporate", "Corporate"),
            FilterOption("Safety", "Safety")
        ),
        isMultiSelect = true
    )
)

// ── Main Screen ──

@Composable
fun LegalComplianceScreen(
    viewModel: LegalViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddCompliance: () -> Unit = {},
    onComplianceClick: (LegalComplianceDto) -> Unit = {},
    onEditCompliance: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        viewModel.fetchCompliances()
    }

    val compliances by viewModel.compliances.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember { mutableStateOf(getLegalComplianceFilterSections()) }

    // State for delete dialog
    var itemToDelete by remember { mutableStateOf<LegalComplianceDto?>(null) }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { it.options.count { opt -> opt.isSelected } }
    }

    // Filter and search logic
    val filteredCompliances = remember(compliances, searchQuery, filterSections) {
        val selectedStatuses = filterSections.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()
        val selectedRisks = filterSections.find { it.title == "Risk Level" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()
        val selectedCategories = filterSections.find { it.title == "Category" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()

        compliances.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.complianceName?.contains(searchQuery, ignoreCase = true) == true ||
                    item.complianceCode?.contains(searchQuery, ignoreCase = true) == true ||
                    item.complianceCategory?.contains(searchQuery, ignoreCase = true) == true

            val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesRisk = selectedRisks.isEmpty() || selectedRisks.any { it.equals(item.riskLevel, ignoreCase = true) }
            val matchesCategory = selectedCategories.isEmpty() || selectedCategories.any { it.equals(item.complianceCategory, ignoreCase = true) }

            matchesSearch && matchesStatus && matchesRisk && matchesCategory
        }
    }

    // FAB Configuration matching bottom-end floating button
    val fabConfig = remember(onAddCompliance) {
        FabConfig(
            label = "Add Complaince",
            icon = Icons.Default.Add,
            onClick = onAddCompliance
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        fab = fabConfig,
        fabVisible = true
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Reused TitleBar component
                TitleBar(
                    title = "All Compliance",
                    onClose = onBack
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading && compliances.isEmpty()) {
                    ListSkeleton()
                } else if (!errorMessage.isNullOrBlank() && compliances.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Error loading compliance records",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = redText
                            )
                            Spacer(Modifier.height(tokens.extraPadding))
                            Button(
                                onClick = { viewModel.fetchCompliances() },
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
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // Reused SearchFilterBar component
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = {
                                    searchQuery = it
                                    viewModel.fetchCompliances(search = it)
                                },
                                placeholder = "Search SO number, customer...",
                                filterCount = activeFilterCount,
                                onFilterClick = { filterDrawerState.open() },
                                accentColor = Primary,
                                borderColor = sectionBorder,
                                textSecondaryColor = mutedText
                            )
                        }

                        items(filteredCompliances, key = { it.id }) { item ->
                            val statusText = item.status?.replace("_", " ") ?: ""
                            val codeText = item.complianceCode ?: ""
                            val categoryText = item.complianceCategory ?: ""
                            val jurisdictionText = item.jurisdiction ?: ""
                            val ownerText = item.responsibleTeam ?: item.complianceOwner?.fullName ?: ""
                            val riskText = item.riskLevel ?: ""
                            val dueDateFormatted = formatDueDate(item.dueDate)
                            val remainingFormatted = calculateDaysRemaining(item.dueDate)

                            // Reusing DataCard Component with Edit, View Details, and Delete options
                            DataCard(
                                item = item,
                                title = item.complianceName ?: "",
                                titleFontWeight = FontWeight.Medium,
                                titleColor = TextPrimary,
                                topBadgeText = statusText,
                                topBadgeTextColor = orangeText,
                                topBadgeBgColor = orangeBg,
                                topBadgeShowDot = false,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction(
                                        label = "Edit",
                                        onClick = { onEditCompliance(item.id) }
                                    ),
                                    MenuAction(
                                        label = "View Details",
                                        onClick = { onComplianceClick(item) }
                                    ),
                                    MenuAction(
                                        label = "Delete",
                                        textColor = redText,
                                        onClick = { itemToDelete = item }
                                    )
                                ),
                                showActionsInHeader = false,
                                showDivider = false,
                                onClick = { onComplianceClick(item) },
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Row 1: ID
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ID",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Normal,
                                                color = mutedText
                                            )
                                            Text(
                                                text = codeText,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextPrimary
                                            )
                                        }

                                        Spacer(Modifier.height(6.dp))

                                        // Row 2: Category
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Category",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Normal,
                                                color = mutedText
                                            )
                                            Text(
                                                text = categoryText,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // Inner Stat Grid: Jurisdiction, Owner, Risk
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .background(Primary_background)
                                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .padding(vertical = 10.dp, horizontal = 12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Jurisdiction",
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Normal,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = jurisdictionText,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Owner",
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Normal,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = ownerText,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Risk",
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Normal,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = riskText,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        // Due Date Row with Remaining Days
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.Normal)) {
                                                    append("Due Date :")
                                                }
                                                withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Medium)) {
                                                    append(dueDateFormatted)
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

        // Sliding Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Compliances",
            sections = filterSections,
            onApply = { updatedSections ->
                filterSections = updatedSections
            },
            onClearAll = {
                filterSections = getLegalComplianceFilterSections()
            }
        )

        // Delete Confirmation Modal
        itemToDelete?.let { targetCompliance ->
            DeleteModel(
                title = "Delete Compliance",
                message = "You're going to delete \"${targetCompliance.complianceName ?: targetCompliance.complianceCode}\". Are you sure?",
                checkboxText = "I confirm this deletion",
                cancelText = "Cancel",
                confirmText = "Delete",
                onDismiss = { itemToDelete = null },
                onDelete = {
                    viewModel.deleteCompliance(
                        complianceId = targetCompliance.id,
                        onSuccess = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            itemToDelete = null
                        },
                        onError = { err ->
                            Toast.makeText(context, err ?: "Failed to delete compliance", Toast.LENGTH_SHORT).show()
                            itemToDelete = null
                        }
                    )
                }
            )
        }
    }
}