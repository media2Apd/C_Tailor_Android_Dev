package com.cuso.mobile.view.home.reusablecomposables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.modelBorder

// ── Shimmer brush — reusable animated gradient ──
@Composable
 fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val shimmerColors = listOf(
        Color(0xFFC3C3C7),
        Color(0xFFE2E2E3),
        Color(0xFFE8EAF4)
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

@Composable
 fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val brush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// ── Full-page dashboard skeleton — mirrors HomeScreenContentBody layout ──
@Composable
 fun DashboardSkeleton() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7FB)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        userScrollEnabled = false
    ) {
        // Greeting card skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE8EAF4))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    ShimmerBox(modifier = Modifier.width(160.dp).height(20.dp))
                    Spacer(Modifier.height(10.dp))
                    ShimmerBox(modifier = Modifier.width(200.dp).height(14.dp))
                }
            }
        }

        // Stats grid skeleton — 2x2
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFf8f9ff), RoundedCornerShape(20.dp))
                                    .border(1.dp, Color(0xFFe8eaf4), RoundedCornerShape(20.dp))
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ShimmerBox(modifier = Modifier.size(18.dp), shape = RoundedCornerShape(4.dp))
                                    Spacer(Modifier.width(6.dp))
                                    ShimmerBox(modifier = Modifier.width(50.dp).height(12.dp))
                                }
                                Spacer(Modifier.height(14.dp))
                                ShimmerBox(modifier = Modifier.width(60.dp).height(18.dp))
                                Spacer(Modifier.height(8.dp))
                                ShimmerBox(modifier = Modifier.width(40.dp).height(11.dp))
                            }
                        }
                    }
                }
            }
        }

        // Quick Modules skeleton
        item {
            Column {
                ShimmerBox(modifier = Modifier.width(130.dp).height(18.dp))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(5) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ShimmerBox(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(16.dp))
                            Spacer(Modifier.height(6.dp))
                            ShimmerBox(modifier = Modifier.width(40.dp).height(10.dp))
                        }
                    }
                }
            }
        }

        // Recent Activity skeleton
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(modifier = Modifier.width(130.dp).height(16.dp))
                    ShimmerBox(modifier = Modifier.width(50.dp).height(13.dp))
                }
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerBox(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(12.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(13.dp))
                                Spacer(Modifier.height(6.dp))
                                ShimmerBox(modifier = Modifier.width(60.dp).height(11.dp))
                            }
                        }
                    }
                }
            }
        }

        // Recent Customers skeleton
        item {
            Column {
                ShimmerBox(modifier = Modifier.width(150.dp).height(16.dp))
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerBox(modifier = Modifier.size(40.dp), shape = CircleShape)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ShimmerBox(modifier = Modifier.width(100.dp).height(13.dp))
                                Spacer(Modifier.height(6.dp))
                                ShimmerBox(modifier = Modifier.width(70.dp).height(11.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Lead list skeleton — mirrors each lead card's layout ──
@Composable
 fun ListSkeleton(itemCount: Int = 4) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        userScrollEnabled = false
    ) {
        items(itemCount) { index ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // date row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ShimmerBox(modifier = Modifier.size(14.dp), shape = RoundedCornerShape(3.dp))
                            Spacer(Modifier.width(6.dp))
                            ShimmerBox(modifier = Modifier.width(80.dp).height(12.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        // name
                        ShimmerBox(modifier = Modifier.width(90.dp).height(16.dp))
                        Spacer(Modifier.height(8.dp))
                        // subtitle (type • variant • qty)
                        ShimmerBox(modifier = Modifier.width(160.dp).height(12.dp))
                        Spacer(Modifier.height(10.dp))
                        // price range
                        ShimmerBox(modifier = Modifier.width(130.dp).height(14.dp))
                    }

                    Spacer(Modifier.width(12.dp))

                    // right side: badge + menu dots
                    Column(horizontalAlignment = Alignment.End) {
                        ShimmerBox(
                            modifier = Modifier.width(70.dp).height(24.dp),
                            shape = RoundedCornerShape(50)
                        )
                        Spacer(Modifier.height(28.dp))
                        ShimmerBox(modifier = Modifier.width(4.dp).height(16.dp), shape = RoundedCornerShape(2.dp))
                    }
                }

                if (index != itemCount - 1) {
                    HorizontalDivider(thickness = 1.dp, color = modelBorder)
                }
            }
        }
    }
}