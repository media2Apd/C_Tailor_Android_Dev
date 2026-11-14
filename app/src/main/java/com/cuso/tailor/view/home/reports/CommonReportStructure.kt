package com.cuso.tailor.view.home.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.PagingUiState
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import kotlinx.coroutines.flow.distinctUntilChanged

data class ReportMetricSpec(
    val label: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val valueColor: Color = TextPrimary,
    val subtitleColor: Color = mutedText,
    val iconTint: Color = Primary,
    val iconBgColor: Color = primary_light
)

data class ReportGridCell(
    val label: String,
    val value: String,
    val valueColor: Color = TextPrimary
)

@Composable
fun ReportActionPillButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val borderThickness = tokens.extraPadding * 0.1f

    Surface(
        modifier = modifier
            .height(tokens.fieldHeight * 0.9f)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        color = whiteBg,
        border = BorderStroke(borderThickness, sectionBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = tokens.extraPadding * 1.2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextSecondary,
                modifier = Modifier.size(tokens.iconSize * 0.85f)
            )
            Spacer(Modifier.width(tokens.extraPadding * 0.6f))
            Text(
                text = label,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ReportMetricGrid(
    metrics: List<ReportMetricSpec>,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val rows = remember(metrics) { metrics.chunked(2) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        rows.forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                rowMetrics.forEach { metric ->
                    ReportMetricCard(
                        spec = metric,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ReportMetricCard(
    spec: ReportMetricSpec,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val borderThickness = tokens.extraPadding * 0.1f
    val cornerRadius = tokens.cardCornerRadius * 0.8f

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = whiteBg,
        border = BorderStroke(borderThickness, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.extraPadding * 1.2f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spec.label,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 1.4f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .background(spec.iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = null,
                        tint = spec.iconTint,
                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                    )
                }
            }

            Text(
                text = spec.value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = spec.valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = spec.subtitle,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = spec.subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ReportDetailGridBox(
    rows: List<List<ReportGridCell>>,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val cornerRadius = tokens.cardCornerRadius * 0.65f
    val borderThickness = tokens.extraPadding * 0.1f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(Primary_background)
            .border(borderThickness, grey_border, RoundedCornerShape(cornerRadius))
            .padding(tokens.extraPadding * 1.2f),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.1f)
    ) {
        rows.forEach { rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
            ) {
                rowCells.forEach { cell ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                    ) {
                        Text(
                            text = cell.label,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cell.value,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = cell.valueColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportInlineMetricRow(
    label: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
        trailingContent()
    }
}

@Composable
fun ReportCardFooterCaption(
    text: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = dividerColor)
        Spacer(Modifier.height(tokens.extraPadding * 0.8f))
        Text(
            text = text,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = mutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun <T> ReportInfinitelyPaginatedScaffold(
    title: String,
    subtitle: String? = null,
    sectionTitle: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchPlaceholder: String,
    metrics: List<ReportMetricSpec>,
    pagingState: PagingUiState<T>,
    filteredItems: List<T>,
    itemKey: (T) -> String,
    filterSections: List<FilterSection>,
    activeFilterCount: Int,
    onApplyFilters: (List<FilterSection>) -> Unit,
    onClearAllFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onShareClick: () -> Unit,
    onExportClick: () -> Unit,
    onClose: () -> Unit,
    emptyMessage: String = "No records found.",
    itemContent: @Composable (T) -> Unit
) {
    val tokens = LocalAppTokens.current
    val listState = rememberLazyListState()
    val filterDrawerState = rememberFilterDrawerState()
    var blurRadius by remember { mutableStateOf(tokens.extraPadding * 0f) }

    val shouldTriggerLoadMore by remember(
        pagingState.items.size,
        pagingState.canLoadMore,
        pagingState.isLoadingMore,
        pagingState.isInitialLoading
    ) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            pagingState.canLoadMore &&
                    !pagingState.isLoadingMore &&
                    !pagingState.isInitialLoading &&
                    totalItems > 0 &&
                    lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { shouldTriggerLoadMore }
            .distinctUntilChanged()
            .collect { trigger ->
                if (trigger) onLoadMore()
            }
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
        ) {
            TitleBar(
                title = title,
                onClose = onClose
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(
                        start = tokens.screenPadding,
                        end = tokens.screenPadding,
                        bottom = tokens.extraPadding
                    ),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                ReportActionPillButton(
                    label = "Share",
                    icon = Icons.Default.Share,
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f)
                )
                ReportActionPillButton(
                    label = "Export",
                    icon = Icons.Default.Download,
                    onClick = onExportClick,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = dividerColor)

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = tokens.extraPadding * 1.2f)
            ) {
                item(key = "search_filter_bar") {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        placeholder = searchPlaceholder,
                        filterCount = activeFilterCount,
                        onFilterClick = { filterDrawerState.open() },
                        accentColor = Primary,
                        height = tokens.fieldHeight
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                }
                item(key = "metrics_grid") {
                    ReportMetricGrid(metrics = metrics)
                    Spacer(Modifier.height(tokens.extraPadding * 1.5f))
                }
                item(key = "section_header") {
                    Text(
                        text = sectionTitle,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color,
                        modifier = Modifier.padding(
                            horizontal = tokens.screenPadding,
                            vertical = tokens.extraPadding * 0.4f
                        )
                    )
                }

                when {
                    pagingState.isInitialLoading && filteredItems.isEmpty() -> {
                        item(key = "initial_loading") {
                            ListSkeleton()
                        }
                    }

                    filteredItems.isEmpty() -> {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = tokens.screenPadding * 2f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emptyMessage,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = mutedText
                                )
                            }
                        }
                    }

                    else -> {
                        items(
                            items = filteredItems,
                            key = { itemKey(it) }
                        ) { record ->
                            itemContent(record)
                        }

                        if (pagingState.isLoadingMore) {
                            item(key = "loading_more_footer") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = tokens.extraPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Primary,
                                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filters",
            sections = filterSections,
            onApply = onApplyFilters,
            onClearAll = onClearAllFilters,
            onBackgroundBlurChange = { blurRadius = it }
        )
    }
}