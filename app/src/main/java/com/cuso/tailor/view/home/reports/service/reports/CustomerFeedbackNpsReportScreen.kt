@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.service.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.CustomerFeedbackReportItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.*
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun CustomerFeedbackNpsReportScreen(
    onClose: () -> Unit,
    onViewDetail: (CustomerFeedbackReportItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.feedbackNpsSummary.collectAsState()
    val pagingState by viewModel.feedbackNpsPaging.collectAsState()
    val filteredList by viewModel.filteredFeedbackNpsList.collectAsState()
    val searchQuery by viewModel.feedbackNpsSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "NPS Category",
                    options = listOf(
                        FilterOption("Promoter", "Promoter"),
                        FilterOption("Passive", "Passive"),
                        FilterOption("Detractor", "Detractor")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) { viewModel.loadFeedbackNpsReport() }

    val metrics = remember(summary) {
        val nps = summary?.npsSummary
        listOf(
            ReportMetricSpec(
                label = "TOTAL REVIEWS",
                value = (summary?.totalReviews ?: 0).toString(),
                subtitle = "Avg fitting ${summary?.averageFittingRating ?: 0.0}",
                icon = Icons.Default.RateReview,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "AVG OVERALL RATING",
                value = "${summary?.averageOverallRating ?: 0.0} / 5",
                subtitle = "Stitching ${summary?.averageStitchingRating ?: 0.0}  |  Delivery ${summary?.averageDeliveryRating ?: 0.0}",
                icon = Icons.Default.Star,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "NET PROMOTER SCORE",
                value = (nps?.netPromoterScore ?: 0.0).toString(),
                subtitle = "${nps?.promoters ?: 0} promoters  |  ${nps?.passives ?: 0} passives  |  ${nps?.detractors ?: 0} detractors",
                icon = Icons.Default.Campaign,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "NEGATIVE REVIEWS",
                value = (summary?.grievances?.totalNegativeReviews ?: 0).toString(),
                subtitle = "${summary?.grievances?.pendingResolution ?: 0} pending resolution",
                subtitleColor = redText,
                icon = Icons.Default.ReportProblem,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Customer Feedback & NPS Report",
        subtitle = null,
        sectionTitle = "Customer Review Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onFeedbackNpsSearchChange,
        searchPlaceholder = "Search by review, order or customer...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val cats = updated.find { it.title == "NPS Category" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyFeedbackNpsFilters(cats)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearFeedbackNpsFilters()
        },
        onLoadMore = { viewModel.loadFeedbackNpsReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Feedback & NPS report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Feedback & NPS report export started") },
        onClose = onClose,
        emptyMessage = "No customer feedback reviews found."
    ) { item ->
        val badgeText = when {
            item.npsCategory.equals("Promoter", true) -> greentext
            item.npsCategory.equals("Detractor", true) -> redText
            else -> yellowText
        }
        val badgeBg = when {
            item.npsCategory.equals("Promoter", true) -> greenBg
            item.npsCategory.equals("Detractor", true) -> redBg
            else -> yellowBg
        }

        DataCard(
            item = item,
            title = item.customerName.ifBlank { "--" },
            subtitle = "${item.reviewCode.ifBlank { "--" }}  •  ${item.orderCode.ifBlank { "--" }}",
            topBadgeText = item.npsCategory.ifBlank { "Passive" },
            topBadgeTextColor = badgeText,
            topBadgeBgColor = badgeBg,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(label = "View Details", icon = Icons.Default.Visibility, onClick = { onViewDetail(item) })
            ),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Overall Rating") {
                        Text(
                            text = "${item.overallRating} / 5",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = yellowText
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Fitting", item.fittingRating.toString()),
                                ReportGridCell("Stitching", item.stitchingRating.toString()),
                                ReportGridCell("Delivery", item.deliveryRating.toString())
                            ),
                            listOf(
                                ReportGridCell("Staff", item.staffRating.toString()),
                                ReportGridCell("NPS Score", item.npsScore.toString()),
                                ReportGridCell("Status", item.status.ifBlank { "--" })
                            ),
                            listOf(ReportGridCell("Comment", item.comment.ifBlank { "--" }))
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Submitted on ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}