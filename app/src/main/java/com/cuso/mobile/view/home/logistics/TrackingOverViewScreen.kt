package com.example.tracking

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.Primary_background

// ── Colors ───────────────────────────────────────────────────────────────────
private val PrimaryBlue   = Color(0xFF1A1AE6)
private val LightBlue     = Color(0xFFEEEEFF)
private val GreenActive   = Color(0xFF22C55E)
private val GrayLight     = Color(0xFFF5F5F5)
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
    val defaultActive: Boolean,
    val activeColor: Color
)

private val timelineSteps = listOf(
    TimelineStep("Shipped",          "Warehouse A",      "Parcel picked up from primary hub.",             "Oct 24 • 10:00 AM", true,  GreenActive),
    TimelineStep("In Transit",       "Sorting Center",   "Processing through regional distribution network.", "Oct 25 • 02:00 PM", true,  PrimaryBlue),
    TimelineStep("Out for Delivery", "Local Hub",        "Estimated: Tomorrow",                            null,                false, GreenActive),
    TimelineStep("Delivered",        "Customer Address", null,                                             null,                false, GreenActive),
)

// ── Screen ───────────────────────────────────────────────────────────────────
@Composable
fun TrackingOverviewScreen(onClose: () -> Unit = {}) {
    // Each step tracks whether it has been ticked
    val tickedStates = remember { mutableStateListOf(*timelineSteps.map { it.defaultActive }.toTypedArray()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tracking Overview",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onClose() }
            )
        }

        HorizontalDivider(color = GrayBorder)
        Spacer(Modifier.height(16.dp))

        // ── Order ID + Badge ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ORD-88294-LX", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(InTransitBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("IN TRANSIT", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = InTransitText, letterSpacing = 0.5.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Info row ──────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell("Tracking No",  "TRK-99210045IX",  Modifier.weight(1f))
            VerticalDividerLine()
            InfoCell("Courier",      "Velocity Express", Modifier.weight(1f))
            VerticalDividerLine()
            InfoCell("Est. Delivery","Oct 27, 2023",     Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // ── Buttons ───────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue)
            ) { Text("View Shipment", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }

            Button(
                onClick = {},
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) { Text("Update Status", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = GrayBorder)
        Spacer(Modifier.height(20.dp))

        // ── Tracking Timeline ─────────────────────────────────────────────
        Text("Tracking Timeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(Modifier.height(16.dp))

        timelineSteps.forEachIndexed { index, step ->
            AnimatedTimelineItem(
                step        = step,
                isTicked    = tickedStates[index],
                isLast      = index == timelineSteps.lastIndex,
                nextIsTicked = if (index < timelineSteps.lastIndex) tickedStates[index + 1] else false,
                onToggle    = { tickedStates[index] = !tickedStates[index] }
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = GrayBorder)
        Spacer(Modifier.height(20.dp))

        // ── Shipment Information ──────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .border(1.5.dp, PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("i", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue) }
            Spacer(Modifier.width(8.dp))
            Text("Shipment Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
        }

        Spacer(Modifier.height(12.dp))
        ShipmentInfoRow("Origin",         "Warehouse A, New Delhi")
        HorizontalDivider(color = GrayBorder)
        ShipmentInfoRow("Destination",    "742 Evergreen Terrace, Springfield")
        HorizontalDivider(color = GrayBorder)
        ShipmentInfoRow("Package Weight", "2.5 KG")

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = GrayBorder)
        Spacer(Modifier.height(20.dp))

        // ── Courier Details ───────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(LightBlue),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.width(8.dp))
            Text("Courier Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
        }

        Spacer(Modifier.height(12.dp))
        CourierDetailRow("Courier",      "Velocity Express")
        HorizontalDivider(color = GrayBorder)
        CourierDetailRow("Service Type", "Next Day Air")
        Spacer(Modifier.height(32.dp))
    }
}

// ── Animated Timeline Item ────────────────────────────────────────────────────
@Composable
private fun AnimatedTimelineItem(
    step: TimelineStep,
    isTicked: Boolean,
    isLast: Boolean,
    nextIsTicked: Boolean,
    onToggle: () -> Unit
) {
    // Bounce scale when ticked
    val scale by animateFloatAsState(
        targetValue = if (isTicked) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )

    // Animated fill fraction for the connector bar (0f → 1f)
    val lineFill by animateFloatAsState(
        targetValue = if (nextIsTicked) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "lineFill"
    )

    Row(modifier = Modifier.fillMaxWidth()) {

        // ── Icon column + animated connector ─────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Clickable icon circle
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isTicked && step.activeColor == GreenActive -> GreenActive
                            isTicked -> step.activeColor
                            else -> GrayLight
                        }
                    )
                    .then(
                        if (!isTicked) Modifier.border(1.dp, GrayBorder, CircleShape)
                        else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                // Crossfade between original icon and tick
                Crossfade(
                    targetState = isTicked,
                    animationSpec = tween(300),
                    label = "iconCrossfade"
                ) { ticked ->
                    if (ticked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Animated connector bar ────────────────────────────────────
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(GrayBorder)
                ) {
                    // Filled portion animates from top down
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight(lineFill)
                            .align(Alignment.TopCenter)
                            .background(
                                color = if (isTicked) step.activeColor.copy(alpha = 0.7f)
                                else GreenActive.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        // ── Text ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(top = 6.dp)) {
            Text(
                text = step.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isTicked) DarkText else GrayText
            )
            Text(step.subtitle, fontSize = 12.sp, color = GrayText)
            step.description?.let { Text(it, fontSize = 12.sp, color = GrayText) }
            step.time?.let { Text(it, fontSize = 11.sp, color = GrayText) }
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
