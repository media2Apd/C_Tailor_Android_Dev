@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_documents

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
import com.cuso.tailor.model.legal.LegalDocumentItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

private fun formatExpiryDate(rawDate: String?): String {
    val date = parseDateString(rawDate) ?: return "-"
    return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
}

private fun calculateExpiryDays(rawDate: String?): String {
    val target = parseDateString(rawDate) ?: return ""
    val diffMillis = target.time - System.currentTimeMillis()
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        diffDays > 0 -> "(${diffDays}d remaining )"
        diffDays == 0L -> "(Expires today)"
        else -> "(${kotlin.math.abs(diffDays)}d expired )"
    }
}

private fun getLegalDocumentFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Status",
        options = listOf(
            FilterOption("Draft", "Draft"),
            FilterOption("Pending", "Pending"),
            FilterOption("Active", "Active"),
            FilterOption("Expired", "Expired")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Category",
        options = listOf(
            FilterOption("Contract", "Contract"),
            FilterOption("Employment", "Employment"),
            FilterOption("NDA", "NDA"),
            FilterOption("Compliance", "Compliance")
        ),
        isMultiSelect = true
    )
)

@Composable
fun LegalDocumentsScreen(
    viewModel: LegalViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddDocument: () -> Unit = {},
    onDocumentClick: (LegalDocumentItemDto) -> Unit = {},
    onEditDocument: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        viewModel.fetchLegalDocuments()
    }

    val documents by viewModel.legalDocuments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLegalDocuments.collectAsStateWithLifecycle()
    val errorMessage by viewModel.legalDocumentsError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember { mutableStateOf(getLegalDocumentFilterSections()) }

    var itemToDelete by remember { mutableStateOf<LegalDocumentItemDto?>(null) }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { it.options.count { opt -> opt.isSelected } }
    }

    val filteredDocuments = remember(documents, searchQuery, filterSections) {
        val selectedStatuses = filterSections.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()
        val selectedCategories = filterSections.find { it.title == "Category" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()

        documents.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.documentName?.contains(searchQuery, ignoreCase = true) == true ||
                    item.documentNumber?.contains(searchQuery, ignoreCase = true) == true ||
                    item.category?.contains(searchQuery, ignoreCase = true) == true

            val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesCategory = selectedCategories.isEmpty() || selectedCategories.any { it.equals(item.category, ignoreCase = true) }

            matchesSearch && matchesStatus && matchesCategory
        }
    }

    val fabConfig = remember(onAddDocument) {
        FabConfig(
            label = "Add Document",
            icon = Icons.Default.Add,
            onClick = onAddDocument
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
                TitleBar(
                    title = "All Documents",
                    onClose = onBack
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading && documents.isEmpty()) {
                    ListSkeleton()
                } else if (!errorMessage.isNullOrBlank() && documents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Error loading legal documents",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = redText
                            )
                            Spacer(Modifier.height(tokens.extraPadding))
                            Button(
                                onClick = { viewModel.fetchLegalDocuments() },
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
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = {
                                    searchQuery = it
                                    viewModel.fetchLegalDocuments(search = it)
                                },
                                placeholder = "Search SO number, customer...",
                                filterCount = activeFilterCount,
                                onFilterClick = { filterDrawerState.open() },
                                accentColor = Primary,
                                borderColor = sectionBorder,
                                textSecondaryColor = mutedText
                            )
                        }

                        items(filteredDocuments, key = { it.id }) { item ->
                            val statusText = item.status?.replace("_", " ") ?: ""
                            val codeText = item.documentNumber ?: ""
                            val categoryText = item.category ?: ""

                            // Select only the version where isCurrentVersion == true; fallback to item.version or the last version in the list
                            val currentVersionObj = item.versions?.find { it.isCurrentVersion == true }
                                ?: item.versions?.lastOrNull()
                            val versionText = (currentVersionObj?.version ?: item.version ?: "v1").uppercase()

                            val ownerText = item.legalEntity ?: ""
                            val accessText = item.accessLevel ?: ""
                            val riskText = item.riskLevel ?: ""
                            val expiryDateFormatted = formatExpiryDate(item.expiryDate)
                            val remainingFormatted = calculateExpiryDays(item.expiryDate)

                            DataCard(
                                item = item,
                                title = item.documentName ?: "",
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
                                        // Passes the top-level document _id
                                        onClick = { onEditDocument(item.id) }
                                    ),
                                    MenuAction(
                                        label = "View Details",
                                        // Passes the top-level document object containing _id
                                        onClick = { onDocumentClick(item) }
                                    ),
                                    MenuAction(
                                        label = "Delete",
                                        textColor = redText,
                                        onClick = { itemToDelete = item }
                                    )
                                ),
                                showActionsInHeader = false,
                                showDivider = false,
                                // Clicking anywhere on the card passes the top-level document object
                                onClick = { onDocumentClick(item) },
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Row 1: Documents ID
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Documents ID",
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

                                        Spacer(Modifier.height(6.dp))

                                        // Row 3: Version
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Version",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Normal,
                                                color = mutedText
                                            )
                                            Text(
                                                text = versionText,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // Stat Grid: Owner, Access, Risk
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
                                                        text = "Access",
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Normal,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = accessText,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = orangeText,
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

                                        // Expiry Date Row with Remaining Days
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
            title = "Filter Documents",
            sections = filterSections,
            onApply = { updatedSections -> filterSections = updatedSections },
            onClearAll = { filterSections = getLegalDocumentFilterSections() }
        )

        itemToDelete?.let { targetDoc ->
            DeleteModel(
                title = "Delete Document",
                message = "You're going to delete \"${targetDoc.documentName ?: targetDoc.documentNumber}\". Are you sure?",
                checkboxText = "I confirm this deletion",
                cancelText = "Cancel",
                confirmText = "Delete",
                onDismiss = { itemToDelete = null },
                onDelete = {
                    viewModel.deleteLegalDocument(
                        documentId = targetDoc.id,
                        onSuccess = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            itemToDelete = null
                        },
                        onError = { err ->
                            Toast.makeText(context, err ?: "Failed to delete document", Toast.LENGTH_SHORT).show()
                            itemToDelete = null
                        }
                    )
                }
            )
        }
    }
}