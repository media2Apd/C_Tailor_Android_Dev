package com.cuso.mobile.view.home.logistics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar

// ── Colors (sizing now comes from AppDesignTokens) ──────────────────────────
private val ActiveHalo    = Color(0xFFECEBFF)
private val GreenActive   = Color(0xFF22C55E)
private val GrayLight     = Color(0xFFE5E7EB)
private val GrayBorder    = Color(0xFFE5E5E5)
private val GrayText      = Color(0xFF9CA3AF)
private val DarkText      = Color(0xFF111827)
private val InTransitBg   = Color(0xFFEEEEFF)
private val InTransitText = Color(0xFF4F46E5)

// ── Data ─────────────────────────────────────────────────────────────────────
private data class TimelineStep(
    val title: String,
    val subtitle: String,
    val description: String?,
    val time: String?,
    val icon: ImageVector,
    val defaultActive: Boolean,
    val activeColor: Color
)

private val timelineSteps = listOf(
    TimelineStep(
        title = "Shipped",
        subtitle = "Warehouse A",
        description = "Parcel picked up from primary hub.",
        time = "Oct 24 • 10:00 AM",
        icon = Icons.Default.Warehouse,
        defaultActive = true,
        activeColor = Primary
    ),
    TimelineStep(
        title = "In Transit",
        subtitle = "Sorting Center",
        description = "Processing through regional distribution network.",
        time = "Oct 25 • 02:00 PM",
        icon = Icons.Default.LocalShipping,
        defaultActive = false,
        activeColor = Primary
    ),
    TimelineStep(
        title = "Out for Delivery",
        subtitle = "Local Hub",
        description = "Estimated: Tomorrow",
        time = null,
        icon = Icons.Default.Layers,
        defaultActive = false,
        activeColor = Primary
    ),
    TimelineStep(
        title = "Delivered",
        subtitle = "Customer Address",
        description = null,
        time = null,
        icon = Icons.Default.Home,
        defaultActive = false,
        activeColor = Primary
    ),
)

// ── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingOverviewScreen(onClose: () -> Unit = {}) {
    val tokens = LocalAppTokens.current

    // Each step tracks whether it has been completed/ticked
    val tickedStates = remember { mutableStateListOf(*timelineSteps.map { it.defaultActive }.toTypedArray()) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TitleBar("Tracking Overview", onClose = onClose)

                    }
                    HorizontalDivider(color = GrayBorder)
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(tokens.screenPadding * 0.8f))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {

                // ── Order ID + Badge ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ORD-88294-LX",
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(InTransitBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "IN TRANSIT",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = InTransitText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.7f))

                // ── Info row ──────────────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoCell("Tracking No", "TRK-99210045IX", Modifier.weight(1f))
                    VerticalDividerLine()
                    InfoCell("Courier", "Velocity Express", Modifier.weight(1f))
                    VerticalDividerLine()
                    InfoCell("Est. Delivery", "Oct 27, 2023", Modifier.weight(1f))
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.8f))

                // ── Buttons ───────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
                    ) { Text("View Shipment", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold) }

                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("Update Status", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold) }
                }
            }

            Spacer(Modifier.height(tokens.screenPadding * 1.2f))
            HorizontalDivider(color = GrayBorder)

            // ── Tracking Timeline ─────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.cardPadding * 0.35f)
            ) {
                Text(
                    "Tracking Timeline",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
            Spacer(Modifier.height(tokens.screenPadding * 0.8f))

            // Find active step index (first unticked step)
            val activeIndex = tickedStates.indexOfFirst { !it }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {

                timelineSteps.forEachIndexed { index, step ->
                    val isTicked = tickedStates[index]
                    val isActive = index == activeIndex

                    AnimatedTimelineItem(
                        step = step,
                        isTicked = isTicked,
                        isActive = isActive,
                        isLast = index == timelineSteps.lastIndex,
                        onToggle = { tickedStates[index] = !tickedStates[index] }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = GrayBorder)

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding * 0.5f, vertical = tokens.cardPadding * 0.35f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(R.drawable.ic_info), contentDescription = "shipment information")
                Spacer(Modifier.width(8.dp))
                Text("Shipment Information", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = DarkText)
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {

                Spacer(Modifier.height(tokens.screenPadding * 0.6f))
                ShipmentInfoRow("Origin", "Warehouse A, New Delhi")
                HorizontalDivider(color = GrayBorder)
                ShipmentInfoRow("Destination", "742 Evergreen Terrace, Springfield")
                HorizontalDivider(color = GrayBorder)
                ShipmentInfoRow("Package Weight", "2.5 KG")
            }

            Spacer(Modifier.height(tokens.screenPadding))
            HorizontalDivider(color = GrayBorder)

            // ── Courier Details ───────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding * 0.5f, vertical = tokens.cardPadding * 0.35f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(tokens.iconSize * 1.55f),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize * 0.9f)) }
                Spacer(Modifier.width(8.dp))
                Text("Courier Details", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = DarkText)
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {

                Spacer(Modifier.height(tokens.screenPadding * 0.6f))
                CourierDetailRow("Courier", "Velocity Express")
                HorizontalDivider(color = GrayBorder)
                CourierDetailRow("Service Type", "Next Day Air")
                Spacer(Modifier.height(tokens.screenPadding * 1.6f))
            }
        }
    }
}

// ── Animated Timeline Item ────────────────────────────────────────────────────
@Composable
private fun AnimatedTimelineItem(
    step: TimelineStep,
    isTicked: Boolean,
    isActive: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Bounce scale when ticked/active
    val scale by animateFloatAsState(
        targetValue = if (isTicked || isActive) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )

    // Animated fill fraction for the connector bar (0f -> 1f)
    val lineFill by animateFloatAsState(
        targetValue = if (isTicked) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "lineFill"
    )

    val circleOuterSize = tokens.iconSize * 2.4f
    val circleInnerSize = tokens.iconSize * 2.0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // ── Icon column + halo + connector ─────────────────────────────
        Box(
            modifier = Modifier
                .width(circleOuterSize)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // 1. Connector bar drawn BEHIND circle (starts from center of circle down to bottom of row)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = circleOuterSize / 2)
                        .width(2.5.dp)
                        .fillMaxHeight()
                        .background(GrayBorder)
                ) {
                    // Filled green portion animates from top down
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .fillMaxHeight(lineFill)
                            .align(Alignment.TopCenter)
                            .background(GreenActive)
                    )
                }
            }

            // 2. Circle container drawn ON TOP of the connector line
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(circleOuterSize),
                contentAlignment = Alignment.Center
            ) {
                // Soft purple halo behind active step (e.g. "In Transit")
                if (isActive && !isTicked) {
                    Box(
                        modifier = Modifier
                            .size(circleOuterSize)
                            .background(ActiveHalo, CircleShape)
                    )
                }

                // Inner Step Icon Circle
                Box(
                    modifier = Modifier
                        .size(circleInnerSize)
                        .clip(CircleShape)
                        .background(
                            when {
                                isTicked -> GreenActive
                                isActive -> step.activeColor
                                else -> GrayLight
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(
                        targetState = isTicked,
                        animationSpec = tween(300),
                        label = "iconCrossfade"
                    ) { ticked ->
                        if (ticked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = whiteBg,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        } else {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = if (isActive) whiteBg else Color(0xFF6B7280),
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        // ── Text Labels ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(top = 4.dp, bottom = if (isLast) 0.dp else 24.dp)
        ) {
            Text(
                text = step.title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Text(step.subtitle, fontSize = tokens.bodySmall, color = GrayText)
            step.description?.let { Text(it, fontSize = tokens.bodySmall, color = GrayText) }
            step.time?.let { Text(it, fontSize = tokens.caption, color = GrayText) }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────
@Composable
private fun InfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(label, fontSize = tokens.caption, color = GrayText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = DarkText)
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(modifier = Modifier.width(1.dp).height(36.dp).background(GrayBorder))
}

@Composable
private fun ShipmentInfoRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = tokens.caption, color = GrayText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = tokens.bodyMedium, color = DarkText)
    }
}

@Composable
private fun CourierDetailRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = tokens.bodySmall, color = GrayText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = DarkText)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
fun TrackingOverviewPreview() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalAppTokens provides com.cuso.mobile.adaptive_screen.getAdaptiveTokens(
                androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact
            )
        ) {
            TrackingOverviewScreen()
        }
    }
}