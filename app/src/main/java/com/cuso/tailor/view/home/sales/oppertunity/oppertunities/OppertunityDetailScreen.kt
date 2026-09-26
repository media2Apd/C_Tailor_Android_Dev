package com.cuso.tailor.view.home.sales.oppertunity.oppertunities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.OpportunityDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.OpportunityDetailUiState
import com.cuso.tailor.viewmodel.OpportunityViewModel

@Composable
fun OpportunityDetailScreen(
    opportunityId: String,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onConvertToOrder: () -> Unit,
    viewModel: OpportunityViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var newNote by remember { mutableStateOf("") }

    // Fetch opportunity detail and activities on launch
    LaunchedEffect(opportunityId) {
        if (opportunityId.isNotBlank()) {
            viewModel.loadOpportunityDetail(opportunityId)
        }
    }

    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val apiActivities by viewModel.activities.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar(title = "Opportunities Details", onClose = onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        when (val state = detailState) {
            is OpportunityDetailUiState.Loading -> {
                ListSkeleton()
            }

            is OpportunityDetailUiState.Error -> {
                AppErrorState(
                    title = "Failed to load details",
                    message = state.message,
                    onRetry = { viewModel.loadOpportunityDetail(opportunityId) }
                )
            }

            is OpportunityDetailUiState.Success -> {
                val item: OpportunityDto = state.item

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Spacer(Modifier.padding(top = tokens.extraPadding))

                    // Section 1: Overview & Actions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.opportunityCode ?: "—",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                                    .background(yellowBg)
                                    .padding(horizontal = tokens.screenPadding * 0.75f, vertical = tokens.extraPadding * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.stage?.name ?: "New",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = yellowText
                                )
                            }
                        }

                        // Title & Customer
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.35f)) {
                            Text(
                                text = item.name ?: "Untitled Deal",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = headerGrey,
                                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                                Text(
                                    text = item.customer?.fullName ?: "—",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = headerGrey
                                )
                            }
                        }

                        HorizontalDivider(color = grey_border)

                        // 3 Summary Metric Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            DetailMetricBox(
                                label = "VALUE",
                                value = "₹${item.estimatedValue?.toLong() ?: 0}",
                                valueColor = Primary,
                                modifier = Modifier.weight(1f)
                            )
                            DetailMetricBox(
                                label = "CLOSING DATE",
                                value = item.expectedClosingDate?.take(10) ?: "—",
                                modifier = Modifier.weight(1f)
                            )
                            DetailMetricBox(
                                label = "SALESPERSON",
                                value = item.salesperson?.firstName ?: "—",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            OutlinedButton(
                                onClick = onEdit,
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                border = BorderStroke(1.dp, Primary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                                Text(
                                    text = "Edit",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = onConvertToOrder,
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                                Text(
                                    text = "Convert to Order",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }

                    // Section 2: Deal Information
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Deal Information",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )

                            Text(
                                text = "View CRM Logs",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = Primary,
                                modifier = Modifier.clickable {}
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))

                        DealInfoRow(label = "Customer", value = item.customer?.fullName ?: "—")
                        DealInfoRow(label = "Mobile", value = item.customer?.mobileNumber ?: "—")
                        DealInfoRow(label = "Store", value = item.branch?.name ?: "Main Branch")
                        DealInfoRow(label = "Stage", value = item.stage?.name ?: "—", leadingDotColor = Primary, valueColor = Primary)
                        DealInfoRow(label = "Value", value = "₹${item.estimatedValue ?: 0.0}")
                        DealInfoRow(label = "Closing Date", value = item.expectedClosingDate?.take(10) ?: "—")

                        // Probability Progress Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = tokens.extraPadding * 0.4f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                        ) {
                            val percentage = item.stage?.percentage ?: 30
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Probability",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = headerGrey
                                )
                                Text(
                                    text = "$percentage%",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }

                            DataCardProgressBar(
                                progress = percentage / 100f,
                                progressColor = Primary,
                                trackColor = light_grey,
                                height = 4.dp
                            )
                        }
                    }

                    // Section 3: Product Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                    ) {
                        Text(
                            text = "Product Breakdown",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        item.garmentSpecifications?.forEach { spec ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = spec.garment?.displayName ?: spec.garment?.name ?: "Custom Garment",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                            .background(light_grey)
                                            .padding(horizontal = tokens.screenPadding * 0.5f, vertical = tokens.extraPadding * 0.2f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = spec.garmentCategory?.displayName ?: spec.garmentCategory?.name ?: "Bespoke",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Normal,
                                            color = TextSecondary
                                        )
                                    }

                                    Text(
                                        text = "Qty: ${spec.quantity.toString().padStart(2, '0')}",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Normal,
                                        color = headerGrey
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = grey_border, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "₹${item.estimatedValue ?: 0.0}",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }

                    // Section 4: Deal Notes
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = "Deal Notes",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = tokens.extraPadding * 0.4f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.fieldHeight)
                                    .clip(CircleShape)
                                    .background(whiteBg)
                                    .border(1.dp, light_blue_border, CircleShape)
                                    .padding(horizontal = tokens.screenPadding * 0.9f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newNote.isEmpty()) {
                                    Text(
                                        text = "Write a comment...",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Normal,
                                        color = mutedText
                                    )
                                }
                                BasicTextField(
                                    value = newNote,
                                    onValueChange = { newNote = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = tokens.bodySmall, color = TextPrimary)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(tokens.fieldHeight)
                                    .clip(CircleShape)
                                    .background(disabled)
                                    .clickable {
                                        if (newNote.isNotBlank()) {
                                            viewModel.addActivityNote(item.id, newNote.trim())
                                            newNote = ""
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                                )
                            }
                        }

                        // Activities list
                        apiActivities.filter { it.notes?.isNotBlank() == true }.forEach { act ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(tokens.fieldHeight * 0.9f)
                                        .clip(CircleShape)
                                        .background(light_grey),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "A",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color
                                    )
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = tokens.cardCornerRadius * 0.8f,
                                        bottomEnd = 0.dp,
                                        bottomStart = tokens.cardCornerRadius * 0.8f
                                    ),
                                    color = whiteBg,
                                    border = BorderStroke(1.dp, light_blue_border)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(
                                            horizontal = tokens.screenPadding * 0.8f,
                                            vertical = tokens.extraPadding * 0.8f
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = act.activityType ?: "Note",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = act.createdAt?.take(10) ?: "",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Normal,
                                                color = headerGrey
                                            )
                                        }
                                        Spacer(Modifier.height(tokens.extraPadding * 0.35f))
                                        Text(
                                            text = act.notes.orEmpty(),
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Normal,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 5: Activity Timeline
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = "Activity Timeline",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        apiActivities.forEachIndexed { index, act ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier.height(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (index == 0) activity_purple else orangeText)
                                        )
                                    }

                                    if (index < apiActivities.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .fillMaxHeight()
                                                .background(grey_border)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(tokens.extraPadding))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = if (index < apiActivities.size - 1) tokens.extraPadding * 1.5f else 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = act.activityType ?: "Activity",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = act.createdAt?.take(10) ?: "",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Normal,
                                            color = headerGrey
                                        )
                                    }

                                    Spacer(Modifier.height(tokens.extraPadding * 0.25f))

                                    Text(
                                        text = act.notes ?: "Opportunity status updated",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Normal,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.padding(top = 100.dp))
                }
            }
        }
    }
}

@Composable
fun DealInfoRow(
    label: String,
    value: String,
    leadingDotColor: Color? = null,
    hasAvatar: Boolean = false,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = tokens.extraPadding * 0.6f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingDotColor != null) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(leadingDotColor)
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                }
                if (hasAvatar) {
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clip(CircleShape)
                            .background(primary_light)
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                }
                Text(
                    text = value,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = valueColor
                )
            }
        }
        HorizontalDivider(color = grey_border, thickness = 0.8.dp)
    }
}

@Composable
fun DetailMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = badgeGrey,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding * 0.65f)) {
            Text(
                text = label,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = headerGrey
            )
            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}