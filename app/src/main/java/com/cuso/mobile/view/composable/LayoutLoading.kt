package com.cuso.mobile.view.composable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.modelBg
import com.cuso.mobile.ui.theme.modelBorder

// ── Calibrated industrial shimmer brush ──
@Composable
fun rememberShimmerBrush(
    shimmerColors: List<Color> = listOf(
        Color(0xFFE3E5EB),
        Color(0xFFF1F3F8),
        Color(0xFFE3E5EB)
    )
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )
}

// ── Reusable Shimmer Component ──
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    brush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// ── 100% Adaptive Dashboard Skeleton ──
@Composable
fun DashboardSkeleton() {
    val tokens = LocalAppTokens.current
    val shimmerBrush = rememberShimmerBrush()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(
            horizontal = tokens.screenPadding,
            vertical = tokens.screenPadding
        ),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 1.25f),
        userScrollEnabled = false
    ) {
        // 1. Adaptive Greeting Card Skeleton
        item {
            GreetingCardSkeleton(tokens = tokens, brush = shimmerBrush)
        }

        // 2. Adaptive Stats Grid Skeleton (2x2 on phone, 1x4 on tablet)
        item {
            AdaptiveStatsGridSkeleton(tokens = tokens, brush = shimmerBrush)
        }

        // 3. Adaptive Quick Modules Section Skeleton
        item {
            QuickModulesSkeleton(tokens = tokens, brush = shimmerBrush)
        }

        // 4. Adaptive Recent Activity Section Skeleton
        item {
            RecentActivitySkeleton(tokens = tokens, brush = shimmerBrush)
        }

        // 5. Adaptive Recent Customers Section Skeleton
        item {
            RecentCustomersSkeleton(tokens = tokens, brush = shimmerBrush)
        }

        item {
            Spacer(Modifier.height(tokens.screenPadding * 0.5f))
        }
    }
}

// ── 1. Greeting Card Skeleton ──
@Composable
private fun GreetingCardSkeleton(tokens: AppDesignTokens, brush: Brush) {
    val cardCorner = RoundedCornerShape(tokens.cardCornerRadius * 1.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.cardHeight)
            .clip(cardCorner)
            .background(Color(0xFF2F27CE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            // Title placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(if (tokens.isTablet) 0.35f else 0.55f)
                    .height(tokens.h1.value.dp),
                shape = RoundedCornerShape(6.dp),
                brush = rememberShimmerBrush(
                    listOf(
                        Color(0xFF433BD4),
                        Color(0xFF635CE0),
                        Color(0xFF433BD4)
                    )
                )
            )

            Spacer(Modifier.height(tokens.screenPadding * 0.375f))

            // Subtitle placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(if (tokens.isTablet) 0.45f else 0.75f)
                    .height(tokens.bodyMedium.value.dp),
                shape = RoundedCornerShape(4.dp),
                brush = rememberShimmerBrush(
                    listOf(
                        Color(0xFF433BD4),
                        Color(0xFF554EDB),
                        Color(0xFF433BD4)
                    )
                )
            )
        }
    }
}

// ── 2. Adaptive Stats Grid Skeleton (Responsive by gridColumns) ──
@Composable
private fun AdaptiveStatsGridSkeleton(tokens: AppDesignTokens, brush: Brush) {
    val totalStats = 4 // Total 4 dashboard metrics: Revenue, Orders, Customers, Pending
    val rows = (0 until totalStats).chunked(tokens.gridColumns)

    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)
            ) {
                rowItems.forEach { _ ->
                    DashboardStatCardSkeleton(
                        tokens = tokens,
                        brush = brush,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fill missing cells in last row if applicable
                if (rowItems.size < tokens.gridColumns) {
                    repeat(tokens.gridColumns - rowItems.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCardSkeleton(
    tokens: AppDesignTokens,
    brush: Brush,
    modifier: Modifier = Modifier
) {
    val cardCorner = RoundedCornerShape(tokens.cardCornerRadius * 1.6f)

    Column(
        modifier = modifier
            .background(Color(0xFFF8F9FF), cardCorner)
            .border(1.dp, Color(0xFFE8EAF4), cardCorner)
            .padding(tokens.cardPadding * 0.7f)
    ) {
        // Icon + Title Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(
                modifier = Modifier.size(tokens.iconSize),
                shape = RoundedCornerShape(4.dp),
                brush = brush
            )
            Spacer(Modifier.width(tokens.screenPadding * 0.3f))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(tokens.bodySmall.value.dp),
                shape = RoundedCornerShape(4.dp),
                brush = brush
            )
        }

        Spacer(Modifier.height(tokens.screenPadding * 0.7f))

        // Large Number
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(tokens.h2.value.dp * 1.15f),
            shape = RoundedCornerShape(5.dp),
            brush = brush
        )

        Spacer(Modifier.height(tokens.screenPadding * 0.25f))

        // Trend Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(
                modifier = Modifier.size(tokens.iconSize * 0.65f),
                shape = CircleShape,
                brush = brush
            )
            Spacer(Modifier.width(4.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(36.dp)
                    .height(tokens.caption.value.dp),
                shape = RoundedCornerShape(3.dp),
                brush = brush
            )
        }
    }
}

// ── 3. Quick Modules Section Skeleton ──
@Composable
private fun QuickModulesSkeleton(tokens: AppDesignTokens, brush: Brush) {
    val moduleCount = if (tokens.isTablet) 8 else 5

    Column {
        ShimmerBox(
            modifier = Modifier
                .width(130.dp)
                .height(tokens.h2.value.dp),
            shape = RoundedCornerShape(4.dp),
            brush = brush
        )

        Spacer(Modifier.height(tokens.screenPadding * 0.75f))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding),
            userScrollEnabled = false
        ) {
            items(moduleCount) {
                Column(
                    modifier = Modifier.width(tokens.buttonHeight * 1.45f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShimmerBox(
                        modifier = Modifier.size(tokens.buttonHeight * 1.27f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        brush = brush
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.375f))

                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(tokens.caption.value.dp),
                        shape = RoundedCornerShape(3.dp),
                        brush = brush
                    )
                }
            }
        }
    }
}

// ── 4. Recent Activity Section Skeleton ──
@Composable
private fun RecentActivitySkeleton(tokens: AppDesignTokens, brush: Brush) {
    val cardCorner = RoundedCornerShape(tokens.cardCornerRadius)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(130.dp)
                    .height(tokens.h2.value.dp),
                shape = RoundedCornerShape(4.dp),
                brush = brush
            )
            ShimmerBox(
                modifier = Modifier
                    .width(55.dp)
                    .height(tokens.bodyMedium.value.dp),
                shape = RoundedCornerShape(4.dp),
                brush = brush
            )
        }

        Spacer(Modifier.height(tokens.screenPadding * 0.875f))

        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
            repeat(2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(modelBg, cardCorner)
                        .border(1.dp, modelBorder, cardCorner)
                        .padding(tokens.cardPadding * 0.7f),
                    verticalAlignment = Alignment.Top
                ) {
                    ShimmerBox(
                        modifier = Modifier.size(tokens.buttonHeight),
                        shape = CircleShape,
                        brush = brush
                    )

                    Spacer(Modifier.width(tokens.screenPadding * 0.75f))

                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth(if (tokens.isTablet) 0.6f else 0.9f)
                                .height(tokens.bodyMedium.value.dp),
                            shape = RoundedCornerShape(4.dp),
                            brush = brush
                        )
                        Spacer(Modifier.height(tokens.screenPadding * 0.375f))
                        ShimmerBox(
                            modifier = Modifier
                                .width(70.dp)
                                .height(tokens.caption.value.dp),
                            shape = RoundedCornerShape(3.dp),
                            brush = brush
                        )
                    }

                    Spacer(Modifier.width(tokens.screenPadding * 0.5f))

                    ShimmerBox(
                        modifier = Modifier
                            .width(50.dp)
                            .height(tokens.bodyMedium.value.dp),
                        shape = RoundedCornerShape(4.dp),
                        brush = brush
                    )
                }
            }
        }
    }
}

// ── 5. Recent Customers Section Skeleton ──
@Composable
private fun RecentCustomersSkeleton(tokens: AppDesignTokens, brush: Brush) {
    val cardCorner = RoundedCornerShape(tokens.cardCornerRadius)

    Column {
        ShimmerBox(
            modifier = Modifier
                .width(150.dp)
                .height(tokens.h2.value.dp),
            shape = RoundedCornerShape(4.dp),
            brush = brush
        )

        Spacer(Modifier.height(tokens.screenPadding * 0.75f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(modelBg, cardCorner)
                .border(1.dp, modelBorder, cardCorner)
        ) {
            repeat(4) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = tokens.cardPadding * 0.7f,
                            vertical = tokens.cardPadding * 0.6f
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier.size(tokens.buttonHeight),
                        shape = CircleShape,
                        brush = brush
                    )

                    Spacer(Modifier.width(tokens.screenPadding * 0.75f))

                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth(if (tokens.isTablet) 0.35f else 0.5f)
                                .height(tokens.bodyMedium.value.dp),
                            shape = RoundedCornerShape(4.dp),
                            brush = brush
                        )
                        Spacer(Modifier.height(tokens.screenPadding * 0.25f))
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth(if (tokens.isTablet) 0.25f else 0.35f)
                                .height(tokens.caption.value.dp),
                            shape = RoundedCornerShape(3.dp),
                            brush = brush
                        )
                    }

                    ShimmerBox(
                        modifier = Modifier.size(tokens.iconSize * 0.9f),
                        shape = RoundedCornerShape(3.dp),
                        brush = brush
                    )
                }

                if (index != 3) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            start = tokens.cardPadding * 0.7f,
                            end = tokens.cardPadding * 0.7f
                        ),
                        thickness = 1.dp,
                        color = modelBorder
                    )
                }
            }
        }
    }
}

// ── Lead & DataCard Exact Mirror Skeleton ──
@Composable
fun ListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int? = null
) {
    val tokens = LocalAppTokens.current
    val shimmerBrush = rememberShimmerBrush()
    val count = itemCount ?: if (tokens.isTablet) 7 else 5

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(
            horizontal = 0.dp,
            vertical = tokens.extraPadding
        ),
        userScrollEnabled = false
    ) {
        items(count) { index ->
            DataCardItemSkeleton(
                tokens = tokens,
                brush = shimmerBrush
            )

            // Divider matching DataCard showDivider
            HorizontalDivider(
                color = BorderGray,
                thickness = 1.dp
            )
        }
    }
}

// ── Single DataCard Skeleton Item ──
@Composable
private fun DataCardItemSkeleton(
    tokens: AppDesignTokens,
    brush: Brush
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = tokens.screenPadding,
                vertical = 14.dp
            )
    ) {
        // --- 1. Top Row: Order ID (Left) & Status Badge + 3-Dots Menu (Right) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Order ID: ..." metadata placeholder
            ShimmerBox(
                modifier = Modifier
                    .width(if (tokens.isTablet) 180.dp else 130.dp)
                    .height(tokens.caption.value.dp),
                shape = RoundedCornerShape(3.dp),
                brush = brush
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Top Status Badge placeholder
                ShimmerBox(
                    modifier = Modifier
                        .width(if (tokens.isTablet) 110.dp else 90.dp)
                        .height(22.dp),
                    shape = RoundedCornerShape(20.dp),
                    brush = brush
                )

                Spacer(Modifier.width(8.dp))

                // 3-Dots Action Menu Icon placeholder
                ShimmerBox(
                    modifier = Modifier.size(if (tokens.isTablet) 24.dp else 20.dp),
                    shape = CircleShape,
                    brush = brush
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- 2. Main Row: Title (Customer Name) & Subtitle (Date • Garment • Qty) ---
        Column(modifier = Modifier.fillMaxWidth()) {
            // Customer Name / Lead Person Title
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(if (tokens.isTablet) 0.35f else 0.48f)
                    .height(tokens.bodyMedium.value.dp * 1.1f),
                shape = RoundedCornerShape(4.dp),
                brush = brush
            )

            Spacer(Modifier.height(4.dp))

            // Subtitle: "Date • Garment • Qty"
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(if (tokens.isTablet) 0.55f else 0.78f)
                    .height(tokens.caption.value.dp),
                shape = RoundedCornerShape(3.dp),
                brush = brush
            )
        }

        Spacer(Modifier.height(10.dp))

        // --- 3. Footer Row: Budget / Amount Range Field ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Currency Icon Box placeholder
                ShimmerBox(
                    modifier = Modifier.size(20.dp),
                    shape = RoundedCornerShape(4.dp),
                    brush = brush
                )
                Spacer(Modifier.width(6.dp))
                // Budget Range text placeholder: "₹1,000 - ₹50,000"
                ShimmerBox(
                    modifier = Modifier
                        .width(if (tokens.isTablet) 180.dp else 140.dp)
                        .height(tokens.bodySmall.value.dp),
                    shape = RoundedCornerShape(3.dp),
                    brush = brush
                )
            }
        }
    }
}