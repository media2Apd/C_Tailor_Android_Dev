@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "unused_variable",
    "unused_parameter"
)

package com.cuso.tailor.view.home.hr.payroll_management.salary_template

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.SalaryTemplateDto
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.UiState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

/**
 * Screen displaying the list of all salary templates with search and action options.
 */
@Composable
fun SalaryTemplateListScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onCreateTemplateClick: () -> Unit,
    onEditTemplateClick: (String) -> Unit,
    onViewTemplateClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    var searchQuery by remember { mutableStateOf("") }
    val templates by viewModel.salaryTemplates.collectAsStateWithLifecycle()
    val listState by viewModel.templateListState.collectAsStateWithLifecycle()
    val isLoading = listState is UiState.Loading

    // Initial load from the backend
    LaunchedEffect(Unit) {
        viewModel.fetchSalaryTemplates()
    }

    // Filter templates locally based on search query
    val filteredTemplates = remember(templates, searchQuery) {
        if (searchQuery.isBlank()) {
            templates
        } else {
            templates.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.description?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    // Reusable FAB Scaffold
    FabScaffold(
        fab = FabConfig(
            label = "Create Template",
            icon = Icons.Default.Add,
            onClick = onCreateTemplateClick
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Header Bar
                TitleBar(
                    title = "Salary Templates",
                    onClose = onClose
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.fetchSalaryTemplates(search = it.ifBlank { null })
                    },
                    placeholder = "Search templates..."
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Content Section
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (isLoading && templates.isEmpty()) {
                        ListSkeleton()
                    } else if (filteredTemplates.isEmpty()) {
                        Text(
                            text = "No templates found",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredTemplates,
                                key = { it.id }
                            ) { template ->
                                SalaryTemplateCardItem(
                                    template = template,
                                    onViewClick = { onViewTemplateClick(template.id) },
                                    onEditClick = { onEditTemplateClick(template.id) },
                                    onDeleteClick = {
                                        viewModel.deleteSalaryTemplate(
                                            templateId = template.id,
                                            onSuccess = {
                                                DynamicIslandManager.showSuccess("Template deleted successfully")
                                            },
                                            onError = {
                                                DynamicIslandManager.showError(it)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Salary Template item reusing the generic DataCard component.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun SalaryTemplateCardItem(
    template: SalaryTemplateDto,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Actions for the three-dot dropdown menu
    val menuActions = remember {
        listOf(
            MenuAction(
                label = "View",
                icon = Icons.Outlined.RemoveRedEye,
                textColor = TextPrimary,
                tint = mutedText,
                onClick = onViewClick
            ),
            MenuAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                textColor = TextPrimary,
                tint = mutedText,
                onClick = onEditClick
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                textColor = redText,
                tint = redText,
                onClick = onDeleteClick
            )
        )
    }

    // Left footer field showing component count
    val footerFields = listOf(
        DataCardField(
            painter = painterResource( R.drawable.ic_person),
            text = "${template.components.size} Components",
            textColor = TextPrimary,
            iconTint = TextPrimary,
            valueFontWeight = FontWeight.Medium
        )
    )

    // Formatted Annual CTC on the right
    val formattedCtc = "CTC: $ ${String.format("%,.0f", template.annualCTC ?: 0.0)}"

    // Reusing standard DataCard
    DataCard(
        item = template,
        title = template.name,
        subtitle = template.description?.ifBlank { null },
        titleColor = title_color,
        titleFontWeight = FontWeight.Medium,
        topBadgeText = if (template.isActive) "Active" else "Inactive",
        topBadgeTextColor = if (template.isActive) greentext else redText,
        topBadgeBgColor = if (template.isActive) greenBg else redBg,
        topBadgeDotColor = if (template.isActive) greentext else redText,
        topBadgeInline = true,
        actions = menuActions,
        onClick = { onViewClick() },
        footerFields = footerFields,
        trailingText = formattedCtc
    )
}