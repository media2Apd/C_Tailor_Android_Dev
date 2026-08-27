package com.cuso.mobile.view.home.sales.payment_listing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.adaptive_screen.getAdaptiveTokens
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg

// ─────────────────────────────────────────────────────────────
// Local design tokens specific to this screen
// (kept local since they are not part of the shared theme file)
// ─────────────────────────────────────────────────────────────
private val AvatarBg = Color(0xFFEDE9FE)
private val AvatarText = Color(0xFF7C6FEA)

private val PartialBg = Color(0xFFFFEDD5)
private val PartialText = Color(0xFFC2410C)

private val CompletedBg = Color(0xFFDCFCE7)
private val CompletedText = Color(0xFF16A34A)

private val PendingBg = Color(0xFFFEE2E2)
private val PendingText = Color(0xFFB91C1C)

private val AmountHighlight = Color(0xFFEA580C)

private val LabelGray = Color(0xFF9CA3AF)
private val PrimaryDark = Color(0xFF111827)
private val DividerGray = title_border
private val ChevronGray = Color(0xFFC4C4C4)

// ─────────────────────────────────────────────────────────────
// Data model
// ─────────────────────────────────────────────────────────────
enum class PaymentStatus { PARTIAL, COMPLETED, PENDING }
enum class InvoiceState { GENERATED, PENDING }

data class PaymentEntry(
    val paymentId: String,
    val orderId: String,
    val customerInitials: String,
    val customerName: String,
    val paymentMethod: String,
    val amount: String,
    val balance: String,
    val paymentType: String,
    val status: PaymentStatus,
    val invoiceState: InvoiceState
)

private fun samplePayments(): List<PaymentEntry> = listOf(
    PaymentEntry("#PAY-1023", "#ORD-5518", "PS", "Priya Sharma", "Card", "₹22,000", "₹10,000", "Advance", PaymentStatus.PARTIAL, InvoiceState.GENERATED),
    PaymentEntry("#PAY-1023", "#ORD-5518", "AV", "Amit Verma", "UPI", "₹22,000", "₹0", "Full Payment", PaymentStatus.COMPLETED, InvoiceState.GENERATED),
    PaymentEntry("#PAY-1023", "#ORD-5518", "PS", "Priya Sharma", "Card", "₹22,000", "₹10,000", "Advance", PaymentStatus.PENDING, InvoiceState.PENDING),
    PaymentEntry("#PAY-1023", "#ORD-5518", "AV", "Amit Verma", "UPI", "₹22,000", "₹0", "Full Payment", PaymentStatus.COMPLETED, InvoiceState.GENERATED),
    PaymentEntry("#PAY-1023", "#ORD-5518", "PS", "Priya Sharma", "Card", "₹22,000", "₹10,000", "Advance", PaymentStatus.PARTIAL, InvoiceState.GENERATED)
)

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun PaymentListingScreen(
    navController: NavController,
    widthSizeClass: WindowWidthSizeClass,
    onBack: () -> Unit = {},
    onBreadCrumbClick: () -> Unit = {},
    onPaymentClick: (PaymentEntry) -> Unit = {}
) {
    val tokens = getAdaptiveTokens(widthSizeClass)

    CompositionLocalProvider(LocalAppTokens provides tokens) {
        var searchQuery by remember { mutableStateOf("") }
        val payments = remember { samplePayments() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                Modifier.fillMaxWidth()
            ) {
                // ── Header ──
                TitleBar("All Orders", onClose = onBack)
            }

            // ── Breadcrumb + Search ──
            Column(modifier = Modifier.fillMaxWidth()) {
                ScreenBreadcrumb(
                    segments = listOf("Sales", "Payment Listing"),
                    onClick = { onBreadCrumbClick() }
                )
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Payment...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = LabelGray,
                    onFilterClick = { /* open filter drawer */ }
                )
            }

            HorizontalDivider(color = DividerGray)

            // ── Payment list ──
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(payments) { entry ->
                    PaymentCard(entry = entry, onClick = { onPaymentClick(entry) })
                    HorizontalDivider(color = DividerGray)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Payment Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun PaymentCard(
    entry: PaymentEntry,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val (badgeBg, badgeText, badgeLabel) = statusVisuals(entry.status)

    // Row gaps scale relative to the compact baseline (10dp)
    val gapScale = tokens.extraPadding.value / 10f
    val gapTiny = (4 * gapScale).dp
    val gapSmall = (6 * gapScale).dp
    val gapMedium = (10 * gapScale).dp
    val gapLarge = (12 * gapScale).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding + 6.dp)
    ) {
        // ── Top row: id + status badge ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.paymentId,
                    color = PrimaryDark,
                    fontSize = tokens.bodyMedium
                )
                Text(
                    text = " / ${entry.orderId}",
                    color = LabelGray,
                    fontSize = tokens.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            StatusBadge(text = badgeLabel, bgColor = badgeBg, textColor = badgeText)
            Spacer(modifier = Modifier.width(gapTiny))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ChevronGray,
                modifier = Modifier.size(tokens.iconSize)
            )
        }

        Spacer(modifier = Modifier.height(gapMedium))

        // ── Customer row ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(entry.customerInitials)
            Spacer(modifier = Modifier.width(gapMedium))
            Column {
                Text(
                    text = entry.customerName,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Medium,
                    fontSize = tokens.bodyMedium
                )
                Text(
                    text = entry.paymentMethod,
                    color = LabelGray,
                    fontSize = tokens.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(gapLarge))

        // ── Amount / Balance / Type / Invoice — single 2-column block ──
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Amount", color = LabelGray, fontSize = tokens.bodySmall)
                Spacer(modifier = Modifier.height(gapTiny))
                Text(
                    text = entry.amount,
                    color = PrimaryDark,
                    fontSize = tokens.bodyMedium
                )
                Spacer(modifier = Modifier.height(gapTiny))
                Text(text = entry.paymentType, color = title_color, fontSize = tokens.bodySmall)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Balance", color = LabelGray, fontSize = tokens.bodySmall)
                Spacer(modifier = Modifier.height(gapTiny))
                Text(
                    text = entry.balance,
                    color = when {
                        entry.balance == "₹0" -> PrimaryDark
                        entry.status == PaymentStatus.PENDING -> PendingText
                        else -> AmountHighlight
                    },
                    fontSize = tokens.bodyMedium
                )
                Spacer(modifier = Modifier.height(gapTiny))
                InvoiceIndicator(entry.invoiceState)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Small pieces
// ─────────────────────────────────────────────────────────────
@Composable
private fun InitialsAvatar(initials: String) {
    val tokens = LocalAppTokens.current
    val avatarSize = tokens.iconSize + 16.dp

    Box(
        modifier = Modifier
            .size(avatarSize)
            .clip(CircleShape)
            .background(AvatarBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = AvatarText,
            fontWeight = FontWeight.SemiBold,
            fontSize = tokens.caption
        )
    }
}

@Composable
private fun StatusBadge(text: String, bgColor: Color, textColor: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = tokens.extraPadding + 2.dp, vertical = 5.dp)
    ) {
        Text(text = text, color = textColor, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InvoiceIndicator(state: InvoiceState) {
    val tokens = LocalAppTokens.current
    val (dotColor, label) = when (state) {
        InvoiceState.GENERATED -> CompletedText to "Invoice Generated"
        InvoiceState.PENDING -> LabelGray to "Invoice Pending"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = dotColor, fontSize = tokens.bodySmall)
    }
}

// ─────────────────────────────────────────────────────────────
// Status -> visuals mapping
// ─────────────────────────────────────────────────────────────
private data class StatusVisual(val bg: Color, val text: Color, val label: String)

private fun statusVisuals(status: PaymentStatus): StatusVisual = when (status) {
    PaymentStatus.PARTIAL -> StatusVisual(PartialBg, PartialText, "Partial")
    PaymentStatus.COMPLETED -> StatusVisual(CompletedBg, CompletedText, "Completed")
    PaymentStatus.PENDING -> StatusVisual(PendingBg, PendingText, "Pending")
}