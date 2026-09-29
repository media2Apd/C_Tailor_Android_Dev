@file:Suppress("UNUSED_VALUE", "unused", "unusedVariable")
package com.cuso.tailor.view.home.services.service_request

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.AlterationDetail
import com.cuso.tailor.model.service.CreateServiceRequestItemPayload
import com.cuso.tailor.model.service.CreateServiceRequestPayload
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.CustomerUiState
import com.cuso.tailor.viewmodel.CustomerViewModel
import com.cuso.tailor.viewmodel.ServiceActionUiState
import com.cuso.tailor.viewmodel.ServicesViewModel

@Composable
fun CreateServiceRequest(
    onClose: () -> Unit = {},
    onCancel: () -> Unit = {},
    serviceViewModel: ServicesViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    val actionState by serviceViewModel.actionState.collectAsStateWithLifecycle()
    val customerState by customerViewModel.uiState.collectAsStateWithLifecycle()

    // ── Active Accordion Section States ──
    var customerInfoExpanded by remember { mutableStateOf(true) }
    var salesOrderRefExpanded by remember { mutableStateOf(true) }
    var serviceDetailsExpanded by remember { mutableStateOf(true) }
    var uploadEvidenceExpanded by remember { mutableStateOf(true) }
    var preferredResolutionExpanded by remember { mutableStateOf(true) }
    var internalNotesExpanded by remember { mutableStateOf(true) }
    var chargesExpanded by remember { mutableStateOf(true) }

    // ── 1. Customer Information State ──
    var customerName by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }

    // ── 2. Sales Order Reference State ──
    var orderId by remember { mutableStateOf("") }
    var orderIdExpanded by remember { mutableStateOf(false) }
    var garmentName by remember { mutableStateOf("") }
    var garmentType by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var trialCompleted by remember { mutableStateOf(false) }

    // ── 3. Service Details State ──
    var serviceType by remember { mutableStateOf("") }
    var serviceTypeExpanded by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf("") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var issueDescription by remember { mutableStateOf("") }

    // ── 4. Upload Evidence State ──
    var selectedAttachments by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedAttachments = (selectedAttachments + uris).distinct()
        }
    }

    // ── 5. Preferred Resolution State ──
    var preferredServiceDate by remember { mutableStateOf("") }
    var resolutionNotes by remember { mutableStateOf("") }

    // ── 6. Internal Notes State ──
    var staffOnlyComments by remember { mutableStateOf("") }

    // ── 7. Charges State ──
    var serviceCharge by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
    }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ServiceActionUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                serviceViewModel.resetActionState()
                onClose()
            }
            is ServiceActionUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                serviceViewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                TitleBar(title ="Create Service Request", onClose = onClose)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2.2f)
            ) {
                Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                // ─────────────────────────────────────────────
                // 1. CUSTOMER INFORMATION
                // ─────────────────────────────────────────────
                FormAccordionSection(
                    title = "Customer Information",
                    iconPainter = painterResource(R.drawable.ic_person),
                    isExpanded = customerInfoExpanded,
                    onToggle = { customerInfoExpanded = !customerInfoExpanded }
                ) {
                    FormLabel(text = "Customer Name")
                    FormTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        placeholder = "Enter Customer Name"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Customer ID")
                    FormTextField(
                        value = customerId,
                        onValueChange = { customerId = it },
                        placeholder = "Enter Customer ID"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Phone Number")
                    FormTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = "Enter Mobile Number",
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Email Address")
                    FormTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        placeholder = "Enter Email Address",
                        keyboardType = KeyboardType.Email
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // ─────────────────────────────────────────────
                // 2. SALES ORDER REFERENCE
                // ─────────────────────────────────────────────
                FormAccordionSection(
                    title = "Sales Order Reference",
                    iconPainter = painterResource(R.drawable.ic_clippad_lines),
                    isExpanded = salesOrderRefExpanded,
                    onToggle = { salesOrderRefExpanded = !salesOrderRefExpanded }
                ) {
                    FormDropdown(
                        label = "Order ID",
                        value = orderId.ifBlank { "Select Order ID" },
                        expanded = orderIdExpanded,
                        onExpandChange = { orderIdExpanded = it },
                        options = listOf("SO-2024-0492", "SO-2024-0510", "SO-2024-0588"),
                        onOptionSelected = { orderId = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Garment Name")
                    FormTextField(
                        value = garmentName,
                        onValueChange = { garmentName = it },
                        placeholder = "Enter Garment Name"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Garment Type")
                    FormTextField(
                        value = garmentType,
                        onValueChange = { garmentType = it },
                        placeholder = "Enter Garment Type"
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Delivery Date")
                    DatePickerField(
                        value = deliveryDate,
                        onDateSelected = { deliveryDate = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Trial Completed Switch & Delivered Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Trial Completed",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            MiniSwitch(
                                checked = trialCompleted,
                                onCheckedChange = { trialCompleted = it }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(greenBg)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Delivered",
                                color = greentext,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // ─────────────────────────────────────────────
                // 3. SERVICE DETAILS
                // ─────────────────────────────────────────────
                FormAccordionSection(
                    title = "Service Details",
                    iconPainter = painterResource(R.drawable.ic_clippad_lines),
                    isExpanded = serviceDetailsExpanded,
                    onToggle = { serviceDetailsExpanded = !serviceDetailsExpanded }
                ) {
                    FormDropdown(
                        label = "Service Type",
                        value = serviceType.ifBlank { "Select Service Type" },
                        expanded = serviceTypeExpanded,
                        onExpandChange = { serviceTypeExpanded = it },
                        options = listOf("Alteration", "Repair", "Restyling", "Replacement"),
                        onOptionSelected = { serviceType = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormDropdown(
                        label = "Priority",
                        value = priority.ifBlank { "Select Priority" },
                        expanded = priorityExpanded,
                        onExpandChange = { priorityExpanded = it },
                        options = listOf("Low", "Medium", "High", "Urgent"),
                        onOptionSelected = { priority = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Issue Description")
                    FormTextArea(
                        value = issueDescription,
                        onValueChange = { issueDescription = it },
                        placeholder = "Detail the specific issue with the garment..."
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // ─────────────────────────────────────────────
                // 4. UPLOAD EVIDENCE
                // ─────────────────────────────────────────────
                FormAccordionSection(
                    title = "Upload Evidence",
                    iconPainter = painterResource(R.drawable.ic_upload_cloud),
                    isExpanded = uploadEvidenceExpanded,
                    onToggle = { uploadEvidenceExpanded = !uploadEvidenceExpanded }
                ) {
                    ImageUploadSection(
                        isImage = false,
                        selectedImages = selectedAttachments,
                        onBrowseClick = { filePickerLauncher.launch("image/*") },
                        onCameraClick = null,
                        onRemoveImage = { uri ->
                            selectedAttachments = selectedAttachments - uri
                        },
                        browseText = "Browse Files",
                        previewHeaderTitle = "ATTACHED FILES"
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // ─────────────────────────────────────────────
                // 5. PREFERRED RESOLUTION
                // ─────────────────────────────────────────────
                FormAccordionSection(
                    title = "Preferred Resolution",
                    iconPainter = painterResource(R.drawable.ic_calendar),
                    isExpanded = preferredResolutionExpanded,
                    onToggle = { preferredResolutionExpanded = !preferredResolutionExpanded }
                ) {
                    FormLabel(text = "Preferred Service Date")
                    DatePickerField(
                        value = preferredServiceDate,
                        onDateSelected = { preferredServiceDate = it }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel(text = "Resolution Notes")
                    FormTextArea(
                        value = resolutionNotes,
                        onValueChange = { resolutionNotes = it },
                        placeholder = "Additional details about desired resolution..."
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // ─────────────────────────────────────────────
                // 6. INTERNAL NOTES
                // ─────────────────────────────────────────────
                FormAccordionSection(
                    title = "Internal Notes",
                    iconPainter = painterResource(R.drawable.ic_lock_2),
                    isExpanded = internalNotesExpanded,
                    onToggle = { internalNotesExpanded = !internalNotesExpanded }
                ) {
                    FormLabel(text = "Staff-only comments")
                    FormTextArea(
                        value = staffOnlyComments,
                        onValueChange = { staffOnlyComments = it },
                        placeholder = "Document any internal observations or private instructions here..."
                    )

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "These notes are never visible to the customer.",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // ─────────────────────────────────────────────
                // 7. CHARGES
                // ─────────────────────────────────────────────
                FormAccordionSection(
                    title = "Charges",
                    iconPainter = null,
                    isExpanded = chargesExpanded,
                    onToggle = { chargesExpanded = !chargesExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Service",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )

                        BasicTextField(
                            value = serviceCharge,
                            onValueChange = { serviceCharge = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                textAlign = TextAlign.End
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier
                                .width(90.dp)
                                .height(tokens.fieldHeight * 0.85f)
                                .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(whiteBg)
                                .padding(horizontal = 8.dp),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (serviceCharge.isEmpty()) {
                                        Text("0.00", fontSize = tokens.bodySmall, color = mutedText)
                                    }
                                    inner()
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    Row(
                        modifier = Modifier.clickable { /* Add charge row */ },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Field",
                            color = Primary,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                        )
                    }
                }
            }

            // ── Floating Action Bar: Cancel & Initialize Service Request ──
            StepNavigationFab(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                showBack = true,
                onBack = onCancel,
                backLabel = "Cancel",
                trailingAction = TrailingFabAction.Update(
                    label = "Initialize Service",
                    isLoading = actionState is ServiceActionUiState.Loading,
                    onClick = {
                        val resolvedCustomerId = (customerState as? CustomerUiState.Success)
                            ?.customers?.firstOrNull()?.id ?: ""

                        val parsedCharge = serviceCharge.toDoubleOrNull() ?: 0.0

                        val payload = CreateServiceRequestPayload(
                            customerId = resolvedCustomerId,
                            primaryCategory = serviceType.orDash(),
                            originalSalesOrderId = orderId.orDash(),
                            priority = priority.orDash(),
                            deliveryDate = (if (preferredServiceDate.isNotBlank()) preferredServiceDate else deliveryDate).orDash(),
                            advanceAmountPaid = 0.0,
                            attachments = selectedAttachments.map { it.toString() },
                            items = listOf(
                                CreateServiceRequestItemPayload(
                                    category = serviceType.orDash(),
                                    garmentDescription = garmentName.orDash(),
                                    serviceCharge = parsedCharge,
                                    issueSummary = issueDescription.orDash(),
                                    internalNotes = staffOnlyComments.orDash(),
                                    alterations = listOf(
                                        AlterationDetail(
                                            targetArea = "Sleeve_Length",
                                            action = "Shorten",
                                            value = 1.0,
                                            unit = "inch",
                                            notes = issueDescription.orDash()
                                        )
                                    )
                                )
                            )
                        )

                        serviceViewModel.createServiceRequest(payload) {
                            onClose()
                        }
                    }
                )
            )
        }
    }
}

@Composable
private fun FormAccordionSection(
    title: String,
    iconPainter: Painter?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (iconPainter != null) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                    }
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = tokens.extraPadding),
                    content = content
                )
            }
        }
    }
}

private fun String?.orDash(): String =
    if (this.isNullOrBlank()) "-" else this