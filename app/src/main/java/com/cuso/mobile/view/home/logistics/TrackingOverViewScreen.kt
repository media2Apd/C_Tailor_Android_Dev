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
import androidx.compose.material.icons.filled.Close
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
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar

// ── Colors ───────────────────────────────────────────────────────────────────
private val ActiveHalo    = Color(0xFFECEBFF)
private val GreenActive   = Color(0xFF22C55E)
private val GrayLight     = Color(0xFFE5E7EB)
private val GrayBorder    = Color(0xFFE5E5E5)
private val GrayText      = Color(0xFF9CA3AF)
private val DarkText      = Color(0xFF111827)
private val InTransitBg   = Color(0xFFEEEEFF)
private val InTransitText = Color(0xFF4F46E5)

private val paddingValue = 20.dp

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
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingValue)
            ) {

                // ── Order ID + Badge ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ORD-88294-LX",
                        fontSize = 16.sp,
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InTransitText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Info row ──────────────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoCell("Tracking No", "TRK-99210045IX", Modifier.weight(1f))
                    VerticalDividerLine()
                    InfoCell("Courier", "Velocity Express", Modifier.weight(1f))
                    VerticalDividerLine()
                    InfoCell("Est. Delivery", "Oct 27, 2023", Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                // ── Buttons ───────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
                    ) { Text("View Shipment", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }

                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("Update Status", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = GrayBorder)

            // ── Tracking Timeline ─────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Tracking Timeline",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
            Spacer(Modifier.height(16.dp))

            // Find active step index (first unticked step)
            val activeIndex = tickedStates.indexOfFirst { !it }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingValue)
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
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(R.drawable.ic_info), contentDescription = "shipment information")
                Spacer(Modifier.width(8.dp))
                Text("Shipment Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingValue)
            ) {

                Spacer(Modifier.height(12.dp))
                ShipmentInfoRow("Origin", "Warehouse A, New Delhi")
                HorizontalDivider(color = GrayBorder)
                ShipmentInfoRow("Destination", "742 Evergreen Terrace, Springfield")
                HorizontalDivider(color = GrayBorder)
                ShipmentInfoRow("Package Weight", "2.5 KG")
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = GrayBorder)

            // ── Courier Details ───────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp)) }
                Spacer(Modifier.width(8.dp))
                Text("Courier Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingValue)
            ) {

                Spacer(Modifier.height(12.dp))
                CourierDetailRow("Courier", "Velocity Express")
                HorizontalDivider(color = GrayBorder)
                CourierDetailRow("Service Type", "Next Day Air")
                Spacer(Modifier.height(32.dp))
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // ── Icon column + halo + connector ─────────────────────────────
        Box(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // 1. Connector bar drawn BEHIND circle (starts from center of circle at 22.dp down to bottom of row)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 22.dp)
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
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft purple halo behind active step (e.g. "In Transit")
                if (isActive && !isTicked) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ActiveHalo, CircleShape)
                    )
                }

                // Inner Step Icon Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = if (isActive) whiteBg else Color(0xFF6B7280),
                                modifier = Modifier.size(18.dp)
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
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Text(step.subtitle, fontSize = 13.sp, color = GrayText)
            step.description?.let { Text(it, fontSize = 13.sp, color = GrayText) }
            step.time?.let { Text(it, fontSize = 12.sp, color = GrayText) }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────
@Composable
private fun InfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(label, fontSize = 11.sp, color = GrayText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DarkText)
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(modifier = Modifier.width(1.dp).height(36.dp).background(GrayBorder))
}

@Composable
private fun ShipmentInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = 12.sp, color = GrayText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, color = DarkText)
    }
}

@Composable
private fun CourierDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = GrayText)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkText)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
fun TrackingOverviewPreview() {
    MaterialTheme { TrackingOverviewScreen() }
}