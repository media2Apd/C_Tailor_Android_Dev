@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.payments_made

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class AppliedBill(
    val billNumber: String,
    val date: String,
    val amount: Double,
    val paidAmount: Double
)

data class PaymentDetails(
    val paymentId: String,
    val status: String,
    val navigationPath: String,
    val vendorName: String,
    val receiptNumber: String,
    val receiptDate: String,
    val vendorEmail: String,
    val vendorPhone: String,
    val vendorAddress: String,
    val totalAmountPaid: Double,
    val recipientName: String,
    val referenceNumber: String,
    val paymentMethod: String,
    val amountText: String,
    val billBreakdown: List<AppliedBill>
)

@Composable
fun AllInventoryPaymentScreen(
    onClose: () -> Unit = {},
    onPaymentSelect: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    val paymentsList by viewModel.paymentsMadeList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingPaymentsMade.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMorePaymentsMade.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMorePaymentsMade.collectAsStateWithLifecycle()
    val error by viewModel.paymentsMadeError.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchAllPaymentsMade(page = 1)
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 3 && canLoadMore && !isLoading && !isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMorePaymentsMade()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                TitleBar(
                    title = "All Payment",
                    onClose = onClose
                )
            }
        },
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.onPaymentsMadeSearchQueryChanged(it)
                },
                placeholder = "Search Customers...",
                onFilterClick = onFilterClick
            )

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    error != null && paymentsList.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load payments",
                            message = error ?: "-",
                            onRetry = { viewModel.fetchAllPaymentsMade(page = 1) }
                        )
                    }

                    paymentsList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No payments found",
                                fontSize = tokens.bodyMedium,
                                color = close_color
                            )
                        }
                    }

                    else -> {
                        // Spaced by 10.dp between cards
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = paymentsList,
                                key = { it.id }
                            ) { payment ->
                                val (statusBg, statusTextColor) = resolvePaymentStatusColors(payment.status)
                                val refCode = payment.referenceNumber?.takeIf { it.isNotBlank() } ?: payment.paymentNumber
//                                val isSelected = selectedIds.contains(payment.id)

                                // Reusing DataCard with custom leading checkbox and content body
                                DataCard(
                                    item = payment,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(whiteBg),
                                    showDateIcon = false,
                                    dateText = "${formatPaymentDate(payment.paymentDate)}   ",
                                    code = "#$refCode#",
                                    topBadgeText = payment.status.ifBlank { "-" },
                                    topBadgeBgColor = statusBg,
                                    topBadgeTextColor = statusTextColor,
                                    topBadgeDotColor = statusTextColor,
                                    topBadgeShowDot = true,
                                    showActionsInHeader = true,
                                    actions = listOf(
                                        MenuAction(
                                            label = "View Details",
                                            icon = Icons.Outlined.RemoveRedEye,
                                            onClick = { onPaymentSelect(payment.id) }
                                        )
                                    ),
                                    showHeaderDivider = true,
                                    showDivider = false,
                                    onClick = { onPaymentSelect(payment.id) },
                                    content = {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Middle Row: SUPPLIER and AMOUNT PAID
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "SUPPLIER",
                                                        fontSize = tokens.label,
                                                        fontWeight = FontWeight.Medium,
                                                        color = mutedText,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = payment.supplierId?.name?.ifBlank { null } ?: "-",
                                                        fontSize = tokens.bodyMedium,
                                                        color = TextPrimary
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "AMOUNT PAID",
                                                        fontSize = tokens.label,
                                                        fontWeight = FontWeight.Medium,
                                                        color = mutedText,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "₹${formatIndianNumber(payment.amount)}",
                                                        fontSize = tokens.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Primary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // Bottom Row: PO Number, Mode, Unused
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.Start
                                                ) {
                                                    Text(
                                                        text = "PO Number",
                                                        fontSize = tokens.caption,
                                                        color = close_color
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = payment.billId?.billNumber?.ifBlank { null } ?: "-",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Mode",
                                                        fontSize = tokens.caption,
                                                        color = close_color
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = payment.paymentMode?.ifBlank { null } ?: "-",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text(
                                                        text = "Unused",
                                                        fontSize = tokens.caption,
                                                        color = close_color
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    val unusedVal = payment.billId?.balanceDue ?: 0.0
                                                    Text(
                                                        text = "${unusedVal.toInt()}",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            if (isLoadingMore) {
                                item {
                                    ThreeDotLoading()
                                }
                            }
                            item{
                                Spacer(Modifier.height(50.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPaymentDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val parsed = inputFormat.parse(isoDate)
        parsed?.let { outputFormat.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun resolvePaymentStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "completed", "paid", "success" -> greenBg to greentext
        "void", "cancelled" -> redBg to redText
        "pending" -> background_light_purple to Primary
        else -> light_grey to TextSecondary
    }
}

@Composable
fun PaymentOverviewDetailScreen(
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onPreviewPdfClick: () -> Unit = {},
    onSendEmailClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val paymentInfo = remember {
        PaymentDetails(
            paymentId = "Payment-88995",
            status = "PAID",
            navigationPath = "Purchase Orders / Bill-88995",
            vendorName = "Tailoring Business Vendor",
            receiptNumber = "PM-0004",
            receiptDate = "Oct 24, 2023",
            vendorEmail = "biling@tailor.com",
            vendorPhone = "+91 98765 43210",
            vendorAddress = "Industrial Area, Phase II, New Delhi",
            totalAmountPaid = 24.50,
            recipientName = "Tailoring Business Vendor Services",
            referenceNumber = "REF-992834-X",
            paymentMethod = "Digital Wallet / UPI",
            amountText = "Twenty Four Rupees and Fifty Paisa Only",
            billBreakdown = listOf(
                AppliedBill("INV-2023-088", "Oct 12, 2023", 15.00, 15.00),
                AppliedBill("INV-2023-094", "Oct 20, 2023", 9.50, 9.50)
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth()
            ) {
                TitleBar(
                    title = "Payments Made Overview",
                    onClose = onClose
                )
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                Modifier.fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = paymentInfo.paymentId,
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(greenBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = paymentInfo.status,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = greentext
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Text(
                    text = paymentInfo.navigationPath,
                    fontSize = tokens.caption,
                    color = close_color,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = onPreviewPdfClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(tokens.fieldHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = null,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Preview PDF",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onSendEmailClick,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(tokens.fieldHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Send Email",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Column(
                Modifier.fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                        .padding(tokens.screenPadding * 0.9f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(background_light_purple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_shirts),
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 1.3f)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = paymentInfo.vendorName,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(background_light_purple)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = paymentInfo.status,
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "PAYMENT\nRECEIPT",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                textAlign = TextAlign.End,
                                lineHeight = 20.sp,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Receipt #: ",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = paymentInfo.receiptNumber,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Date: ",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = paymentInfo.receiptDate,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerBg, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                            .padding(tokens.extraPadding)
                    ) {
                        Text(
                            text = paymentInfo.vendorEmail,
                            fontSize = tokens.bodySmall,
                            color = title_color
                        )
                        Text(
                            text = paymentInfo.vendorPhone,
                            fontSize = tokens.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = paymentInfo.vendorAddress,
                            fontSize = tokens.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                            .background(light_blue)
                            .border(
                                1.dp,
                                light_blue_border,
                                RoundedCornerShape(tokens.cardCornerRadius * 0.8f)
                            )
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL AMOUNT PAID",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${"%.2f".format(paymentInfo.totalAmountPaid)}",
                                fontSize = tokens.h1,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "PAID TO",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = paymentInfo.recipientName,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "REFERENCE",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = paymentInfo.referenceNumber,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MODE OF PAYMENT",
                        fontSize = tokens.label,
                        color = mutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = paymentInfo.paymentMethod,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AMOUNT IN WORDS",
                        fontSize = tokens.label,
                        color = mutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = paymentInfo.amountText,
                        fontSize = tokens.caption,
                        fontStyle = FontStyle.Italic,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "APPLIED BILLS",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = textSubdued,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, dividerColor, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerBg)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "BILL DETAILS",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = headerGrey,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = "AMOUNT",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = headerGrey,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "PAID",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = headerGrey,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        paymentInfo.billBreakdown.forEach { bill ->
                            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = bill.billNumber,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = bill.date,
                                        fontSize = tokens.caption,
                                        color = mutedText
                                    )
                                }
                                Text(
                                    text = "₹${"%.2f".format(bill.amount)}",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "₹${"%.2f".format(bill.paidAmount)}",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerBg)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Applied Amount",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(2.5f)
                            )
                            Text(
                                text = "₹${"%.2f".format(paymentInfo.totalAmountPaid)}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDivider(color = dividerColor, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "PREPARED BY",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = close_color
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDivider(color = dividerColor, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "AUTHORIZED SIGNATURE",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = close_color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "This is a computer-generated receipt and does not require a physical signature.",
                        fontSize = tokens.caption,
                        color = close_color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Created with cuso invoice",
                            fontSize = tokens.label,
                            color = title_color
                        )
                    }
                }
            }
        }
    }
}