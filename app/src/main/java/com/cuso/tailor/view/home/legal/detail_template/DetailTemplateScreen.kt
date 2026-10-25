@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.detail_template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.activity_brown
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.complete_button_bg
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.SettingsTabs
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.composable.TitleBar

// ─────────────────────────────────────────────────────────────
// Data models (only data changes between pages)
// ─────────────────────────────────────────────────────────────

/** Small status pill shown in the header and inside rows. */
data class DetailBadge(
    val text: String,
    val textColor: Color,
    val bgColor: Color,
    val showDot: Boolean = true
)

/** Label and value box. The dot color follows valueColor. */
data class DetailStat(
    val label: String,
    val value: String,
    val valueColor: Color = TextPrimary,
    val showDot: Boolean = false
)

/** Button or quick action. */
data class DetailAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit = {}
)

/** Everything the top header card needs. */
data class DetailHeaderData(
    val icon: ImageVector,
    val badges: List<DetailBadge>,
    val title: String,
    val metaItems: List<String>,
    val highlightStats: List<DetailStat> = emptyList(),
    val primaryAction: DetailAction? = null,
    val secondaryActions: List<DetailAction> = emptyList()
)

/** Bottom attention banner. */
data class DetailBannerData(
    val title: String,
    val message: String,
    val actionLabel: String,
    val onAction: () -> Unit = {}
)

enum class TimelineState { DONE, CURRENT, PENDING }

/** One step in a vertical progress timeline. */
data class DetailStepItem(
    val title: String,
    val subtitle: String,
    val state: TimelineState,
    val tag: String? = null
)

/** One entry in a dated activity timeline. */
data class DetailActivityItem(
    val date: String,
    val time: String,
    val title: String,
    val subtitle: String
)

// ─────────────────────────────────────────────────────────────
// Main template
// ─────────────────────────────────────────────────────────────

/**
 * Page skeleton: TitleBar, header card, summary card, SettingsTabs, tab content, banner.
 * Only [tabContent] changes per tab. Everything else is driven by data.
 */
@Composable
fun DetailScreenTemplate(
    title: String,
    onClose: () -> Unit,
    header: DetailHeaderData,
    summaryStats: List<DetailStat>,
    tabs: List<TabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    banner: DetailBannerData? = null,
    tabContent: @Composable ColumnScope.(selectedIndex: Int) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(title = title, onClose = onClose)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            DetailHeaderCard(header)

            if (summaryStats.isNotEmpty()) {
                DetailSummaryCard(summaryStats)
            }

            SettingsTabs(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = onTabSelected,
                modifier = Modifier.border(1.dp, title_border, RoundedCornerShape(12.dp))
            )

            tabContent(selectedTabIndex)

            if (banner != null) {
                DetailAttentionBanner(banner)
            }

            Spacer(Modifier.height(tokens.screenPadding))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Cards
// ─────────────────────────────────────────────────────────────

/** Base white bordered card used by every block. */
@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border),
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailHeaderCard(data: DetailHeaderData) {
    val tokens = LocalAppTokens.current
    val iconShape = RoundedCornerShape(tokens.cardCornerRadius)

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(tokens.buttonHeight)
                        .clip(iconShape)
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(modifier = Modifier.weight(1f)) {
                    if (data.badges.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            data.badges.forEach { DetailPill(it) }
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    Text(
                        text = data.title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    if (data.metaItems.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = data.metaItems.joinToString("  \u2022  "),
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                }
            }

            if (data.highlightStats.isNotEmpty()) {
                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(Modifier.height(tokens.extraPadding))

                Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    data.highlightStats.forEach {
                        DetailStatBox(
                            stat = it,
                            modifier = Modifier.weight(1f),
                            valueSize = tokens.bodyMedium
                        )
                    }
                }
            }

            data.primaryAction?.let { action ->
                Spacer(Modifier.height(tokens.extraPadding))
                Button(
                    onClick = action.onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    action.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = action.label,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }

            if (data.secondaryActions.isNotEmpty()) {
                Spacer(Modifier.height(tokens.extraPadding))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.secondaryActions.forEach { action ->
                        val shape = RoundedCornerShape(tokens.cardCornerRadius)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .border(1.dp, BorderGray, shape)
                                .background(whiteBg)
                                .clickable { action.onClick() }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            action.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = action.label,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                            Text(
                                text = action.label,
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Grid of stat boxes, two per row. A single leftover box fills the full width. */
@Composable
fun DetailSummaryCard(stats: List<DetailStat>) {
    val tokens = LocalAppTokens.current
    DetailCard {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            stats.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    pair.forEach {
                        DetailStatBox(stat = it, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Titled card for tab content. Title, subtitle and trailing slot are optional. */
@Composable
fun DetailSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleDotColor: Color? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    DetailCard(modifier = modifier) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (titleDotColor != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(titleDotColor, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }
                }
                trailing?.invoke()
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Small building blocks
// ─────────────────────────────────────────────────────────────

@Composable
fun DetailPill(badge: DetailBadge, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .clip(shape)
            .background(badge.bgColor, shape)
            .border(1.dp, badge.textColor.copy(alpha = 0.25f), shape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (badge.showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(badge.textColor, CircleShape)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = badge.text,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = badge.textColor,
            maxLines = 1
        )
    }
}

/** Small boxed label and value. */
@Composable
fun DetailStatBox(
    stat: DetailStat,
    modifier: Modifier = Modifier,
    valueSize: TextUnit = LocalAppTokens.current.bodySmall
) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius)

    Column(
        modifier = modifier
            .clip(shape)
            .background(Primary_background, shape)
            .border(1.dp, title_border, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = stat.label.uppercase(),
            fontSize = tokens.label,
            color = mutedText
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (stat.showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(stat.valueColor, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = stat.value,
                fontSize = valueSize,
                fontWeight = FontWeight.Medium,
                color = stat.valueColor
            )
        }
    }
}

/** Label on the left, value on the right, optional divider below. */
@Composable
fun DetailInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
    showDivider: Boolean = true
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = headerGrey)
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
    if (showDivider) HorizontalDivider(color = light_grey, thickness = 1.dp)
}

/** Label on the left, pill on the right, optional divider below. */
@Composable
fun DetailBadgeRow(
    label: String,
    badge: DetailBadge?,
    fallbackValue: String = "",
    showDivider: Boolean = true
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = headerGrey)
        if (badge != null) {
            DetailPill(badge)
        } else {
            Text(
                text = fallbackValue,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
    if (showDivider) HorizontalDivider(color = light_grey, thickness = 1.dp)
}

/** Small uppercase label above the value. Use inside a Row with weight for two columns. */
@Composable
fun DetailStackedField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Spacer(Modifier.height(tokens.extraPadding))
        Text(text = label.uppercase(), fontSize = tokens.label, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = tokens.bodySmall, color = TextPrimary)
        Spacer(Modifier.height(tokens.extraPadding))
        if (showDivider) HorizontalDivider(color = light_grey, thickness = 1.dp)
    }
}

/** Tappable row with leading icon, title, subtitle and chevron. */
@Composable
fun DetailLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, BorderGray, shape)
            .clickable { onClick() }
            .padding(tokens.extraPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(tokens.buttonHeight)
                .clip(shape)
                .background(light_grey),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = headerGrey,
                modifier = Modifier.size(tokens.iconSize)
            )
        }

        Spacer(Modifier.width(tokens.extraPadding))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(text = subtitle, fontSize = tokens.caption, color = headerGrey)
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = mutedText,
            modifier = Modifier.size(tokens.iconSize)
        )
    }
}

/** File card with type pill, tag chips, a review button and a view icon button. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailDocumentCard(
    icon: ImageVector,
    name: String,
    uploadedText: String,
    typeLabel: String,
    tags: List<String>,
    onReview: () -> Unit = {},
    onView: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius)

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(tokens.buttonHeight)
                        .clip(shape)
                        .background(orangeBg)
                        .border(1.dp, orangeText.copy(alpha = 0.25f), shape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = orangeText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(text = uploadedText, fontSize = tokens.caption, color = headerGrey)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeGrey)
                        .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = typeLabel, fontSize = tokens.label, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeGrey)
                            .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = tag, fontSize = tokens.caption, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReview,
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    shape = shape,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text(
                        text = "Review",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(tokens.buttonHeight)
                        .clip(shape)
                        .border(1.dp, BorderGray, shape)
                        .clickable { onView() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View",
                        tint = TextSecondary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Timelines
// ─────────────────────────────────────────────────────────────

/** Vertical progress timeline with done, current and pending steps. */
@Composable
fun DetailStepTimeline(steps: List<DetailStepItem>) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            val isLast = index == steps.lastIndex
            val titleColor = when (step.state) {
                TimelineState.DONE -> TextPrimary
                TimelineState.CURRENT -> greentext
                TimelineState.PENDING -> TextSecondary
            }

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StepIndicator(step.state)
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(
                                    if (step.state == TimelineState.DONE) complete_button_bg
                                    else sectionBorder
                                )
                        )
                    }
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else tokens.extraPadding)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = step.title,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = titleColor
                        )
                        if (step.tag != null) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(greenBg)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = step.tag,
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Medium,
                                    color = greentext
                                )
                            }
                        }
                    }
                    Text(text = step.subtitle, fontSize = tokens.caption, color = headerGrey)
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(state: TimelineState) {
    when (state) {
        TimelineState.DONE -> Box(
            modifier = Modifier
                .size(20.dp)
                .background(complete_button_bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = whiteBg,
                modifier = Modifier.size(12.dp)
            )
        }

        TimelineState.CURRENT -> Box(
            modifier = Modifier
                .size(20.dp)
                .background(greenBg, CircleShape)
                .border(1.dp, complete_button_bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(greentext, CircleShape)
            )
        }

        TimelineState.PENDING -> Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(iconMuted, CircleShape)
            )
        }
    }
}

/** Dated activity timeline with date and time on the left. */
@Composable
fun DetailActivityTimeline(items: List<DetailActivityItem>) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(
                    modifier = Modifier.width(64.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.date,
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = item.time,
                        fontSize = tokens.caption,
                        color = mutedText,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Primary, CircleShape)
                    )
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(sectionBorder)
                        )
                    }
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else tokens.screenPadding)) {
                    Text(
                        text = item.title,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(text = item.subtitle, fontSize = tokens.caption, color = headerGrey)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Banner
// ─────────────────────────────────────────────────────────────

@Composable
fun DetailAttentionBanner(data: DetailBannerData) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(yellowBg, shape)
            .border(1.dp, yellowText.copy(alpha = 0.3f), shape)
            .padding(tokens.screenPadding)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = yellowText,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = data.title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = activity_brown
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = data.message,
            fontSize = tokens.caption,
            color = activity_brown
        )

        Spacer(Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier.clickable { data.onAction() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.actionLabel,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
        }
    }
}