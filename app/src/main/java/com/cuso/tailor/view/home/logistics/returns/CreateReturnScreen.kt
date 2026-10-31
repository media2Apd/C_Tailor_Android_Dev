package com.cuso.tailor.view.home.logistics.returns

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CreateReturnCustomerPayload
import com.cuso.tailor.model.logistics.CreateReturnGarmentSummaryPayload
import com.cuso.tailor.model.logistics.DeliveredOrderDto
import com.cuso.tailor.model.logistics.ReturnAttachmentDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LogisticsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

val RETURN_TYPE_OPTIONS = listOf(
    "Alteration",
    "Damage",
    "Exchange",
    "Internal Production Refit"
)

val PRIORITY_OPTIONS = listOf(
    "Low",
    "Medium",
    "High",
    "High (Expedited)"
)

@Composable
fun CreateReturnRequestScreen(
    returnIdToEdit: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: LogisticsViewModel
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !returnIdToEdit.isNullOrBlank()

    val deliveredOrders by viewModel.deliveredOrders.collectAsState()
    val isCreating by viewModel.isCreatingReturn.collectAsState()
    val isUpdating by viewModel.isUpdatingReturn.collectAsState()
    val isSubmitting = isCreating || isUpdating

    val createError by viewModel.createReturnError.collectAsState()
    val existingDetail by viewModel.currentReturnDetail.collectAsState()
    val isLoadingDetail by viewModel.isLoadingReturnDetail.collectAsState()

    // 1. Sales Order Reference State
    var selectedOrder by remember { mutableStateOf<DeliveredOrderDto?>(null) }
    var selectedOrderId by remember { mutableStateOf("") }
    var orderCodeText by remember { mutableStateOf("") }
    var deliveredDateDisplay by remember { mutableStateOf("") }
    var orderedDateYyyyMmDd by remember { mutableStateOf("") }
    var deliveredDateYyyyMmDd by remember { mutableStateOf("") }
    var selectedGarmentName by remember { mutableStateOf("") }
    var currentReturnCode by remember { mutableStateOf("") }

    // 2. Customer Snapshot State
    var customerNameText by remember { mutableStateOf("") }
    var customerPhoneText by remember { mutableStateOf("") }
    var customerEmailText by remember { mutableStateOf("") }
    var customerAddressText by remember { mutableStateOf("") }

    // 3. Return & Service Details State
    var returnType by remember { mutableStateOf(RETURN_TYPE_OPTIONS[0]) }
    var priority by remember { mutableStateOf(PRIORITY_OPTIONS[1]) }
    var preferredCompletionDateDisplay by remember { mutableStateOf("") }
    var serviceCategoryText by remember { mutableStateOf("Suit Fitting & Adjustments") }

    // 4. Issue Description & Resolution State
    var issueDescriptionText by remember { mutableStateOf("") }
    var preferredResolutionNotesText by remember { mutableStateOf("") }
    var internalNotesText by remember { mutableStateOf("") }

    // 5. Existing Cloudinary Attachments (Edit Mode) & Newly Picked Local URIs
    var existingAttachments by remember { mutableStateOf<List<ReturnAttachmentDto>>(emptyList()) }
    var selectedNewImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Dropdown expanded states
    var isOrderDropdownExpanded by remember { mutableStateOf(false) }
    var isGarmentDropdownExpanded by remember { mutableStateOf(false) }
    var isReturnTypeDropdownExpanded by remember { mutableStateOf(false) }
    var isPriorityDropdownExpanded by remember { mutableStateOf(false) }

    // Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val totalAllowed = 5 - existingAttachments.size
            selectedNewImages = (selectedNewImages + uris).distinct().take(totalAllowed.coerceAtLeast(0))
        }
    }

    // Fetch delivered orders and detail when in edit mode
    LaunchedEffect(returnIdToEdit) {
        viewModel.fetchDeliveredOrders()
        if (isEditMode) {
            viewModel.fetchReturnDetail(returnIdToEdit)
        }
    }

    // Prefill form when existingDetail is fetched in edit mode
    LaunchedEffect(existingDetail) {
        if (isEditMode && existingDetail != null) {
            val detail = existingDetail!!
            currentReturnCode = detail.returnId
            selectedOrderId = detail.orderIdString
            orderCodeText = detail.orderCode

            // Customer
            customerNameText = detail.customer?.name ?: ""
            customerPhoneText = detail.customer?.phone ?: ""
            customerEmailText = detail.customer?.email ?: ""
            customerAddressText = detail.customer?.address ?: ""

            // Service details
            returnType = detail.returnType.ifBlank { RETURN_TYPE_OPTIONS[0] }
            priority = detail.priority.ifBlank { PRIORITY_OPTIONS[1] }
            serviceCategoryText = detail.serviceCategory ?: ""
            detail.preferredCompletionDate?.let {
                preferredCompletionDateDisplay = formatToDisplayDate(it)
            }

            // Descriptions
            issueDescriptionText = detail.issueDescription ?: ""
            preferredResolutionNotesText = detail.preferredResolutionNotes ?: ""
            internalNotesText = detail.internalNotes ?: ""

            // Garment summary
            val garment = detail.garmentSummary
            selectedGarmentName = garment?.itemName ?: ""
            orderedDateYyyyMmDd = garment?.orderedDate ?: ""
            deliveredDateYyyyMmDd = garment?.deliveredDate ?: ""
            deliveredDateDisplay = formatToDisplayDate(garment?.deliveredDate)

            // Attachments
            existingAttachments = detail.attachments.orEmpty()
        }
    }

    // Display backend errors
    LaunchedEffect(createError) {
        createError?.let { DynamicIslandManager.showError(it) }
    }

    fun onOrderSelected(order: DeliveredOrderDto) {
        selectedOrder = order
        selectedOrderId = order.id
        orderCodeText = order.orderCode

        val cust = order.customerSnapshot ?: order.customerId
        customerNameText = cust?.fullName ?: cust?.name ?: ""
        customerPhoneText = cust?.mobileNumber ?: cust?.phone ?: ""
        customerEmailText = cust?.email ?: ""
        customerAddressText = cust?.shippingAddress?.toDisplayText()
            ?: cust?.billingAddress?.toDisplayText() ?: ""

        deliveredDateDisplay = formatToDisplayDate(order.deliveredAt ?: order.dueDate)
        deliveredDateYyyyMmDd = formatToPayloadDate(order.deliveredAt ?: order.dueDate)
        orderedDateYyyyMmDd = formatToPayloadDate(order.orderDate)

        val garments = order.garments.ifEmpty { order.items }
        val firstGarmentName = garments.firstOrNull()?.getDisplayName() ?: "Custom Garment"
        selectedGarmentName = firstGarmentName
        serviceCategoryText = "$firstGarmentName Adjustments"
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Return Request" else "Create Return Request",
                onClose = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, BorderGray),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextPrimary,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedOrderId.isBlank()) {
                                DynamicIslandManager.showError("Please select a delivered order")
                                return@Button
                            }
                            if (customerNameText.isBlank() || customerPhoneText.isBlank()) {
                                DynamicIslandManager.showError("Customer details cannot be empty")
                                return@Button
                            }
                            if (issueDescriptionText.isBlank()) {
                                DynamicIslandManager.showError("Please describe the issue")
                                return@Button
                            }

                            // Convert selected image URIs to multipart binary parts
                            val binaryParts = selectedNewImages.mapNotNull { uri ->
                                uriToMultipartPart(context, uri, "attachments")
                            }

                            val payloadCompletionDate = convertPickerDateToPayloadDate(preferredCompletionDateDisplay)

                            if (isEditMode) {
                                // Submit Update API
                                viewModel.submitUpdateReturn(
                                    id = returnIdToEdit,
                                    returnId = currentReturnCode,
                                    orderId = selectedOrderId,
                                    orderCode = orderCodeText,
                                    customer = CreateReturnCustomerPayload(
                                        name = customerNameText.trim(),
                                        phone = customerPhoneText.trim(),
                                        email = customerEmailText.trim(),
                                        address = customerAddressText.trim()
                                    ),
                                    returnType = returnType,
                                    serviceCategory = serviceCategoryText.trim(),
                                    priority = priority,
                                    preferredCompletionDate = payloadCompletionDate,
                                    issueDescription = issueDescriptionText.trim(),
                                    preferredResolutionNotes = preferredResolutionNotesText.trim(),
                                    internalNotes = internalNotesText.trim(),
                                    garmentSummary = CreateReturnGarmentSummaryPayload(
                                        itemName = selectedGarmentName,
                                        orderedDate = orderedDateYyyyMmDd,
                                        deliveredDate = deliveredDateYyyyMmDd
                                    ),
                                    existingAttachments = existingAttachments,
                                    newAttachmentParts = binaryParts,
                                    onSuccess = { msg ->
                                        DynamicIslandManager.showSuccess(msg)
                                        onNavigateBack()
                                    }
                                )
                            } else {
                                // Submit Create API
                                viewModel.submitCreateReturn(
                                    orderId = selectedOrderId,
                                    orderCode = orderCodeText,
                                    customer = CreateReturnCustomerPayload(
                                        name = customerNameText.trim(),
                                        phone = customerPhoneText.trim(),
                                        email = customerEmailText.trim(),
                                        address = customerAddressText.trim()
                                    ),
                                    returnType = returnType,
                                    serviceCategory = serviceCategoryText.trim(),
                                    priority = priority,
                                    preferredCompletionDate = payloadCompletionDate,
                                    issueDescription = issueDescriptionText.trim(),
                                    preferredResolutionNotes = preferredResolutionNotesText.trim(),
                                    internalNotes = internalNotesText.trim(),
                                    garmentSummary = CreateReturnGarmentSummaryPayload(
                                        itemName = selectedGarmentName,
                                        orderedDate = orderedDateYyyyMmDd,
                                        deliveredDate = deliveredDateYyyyMmDd
                                    ),
                                    attachmentParts = binaryParts,
                                    onSuccess = { msg ->
                                        DynamicIslandManager.showSuccess(msg)
                                        onNavigateBack()
                                    }
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(tokens.fieldHeight)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isEditMode) "Update Return Request" else "Create Return Request",
                                color = whiteBg,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isEditMode && isLoadingDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ─── 1. SALES ORDER REFERENCE CARD ──────────────────────
                FormCard(
                    icon = Icons.Outlined.Description,
                    title = "Sales Order Reference"
                ) {
                    val orderOptions = deliveredOrders.map { order ->
                        val custName = order.customerSnapshot?.name
                            ?: order.customerId?.fullName ?: "Customer"
                        "${order.orderCode} — $custName"
                    }

                    val currentOrderLabel = if (orderCodeText.isNotBlank()) {
                        "$orderCodeText — $customerNameText"
                    } else ""

                    FormDropdown(
                        label = "Select Delivered Order",
                        isRequired = true,
                        value = currentOrderLabel,
                        expanded = isOrderDropdownExpanded,
                        onExpandChange = { isOrderDropdownExpanded = it },
                        options = orderOptions,
                        onOptionSelected = { selectedLabel ->
                            val matchingOrder = deliveredOrders.find { order ->
                                val custName = order.customerSnapshot?.name
                                    ?: order.customerId?.fullName ?: "Customer"
                                "${order.orderCode} — $custName" == selectedLabel
                            }
                            matchingOrder?.let { onOrderSelected(it) }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                FormLabel(text = "Order Code")
                                FormTextField(
                                    value = orderCodeText,
                                    onValueChange = {},
                                    placeholder = "Auto-filled",
                                    enabled = false
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                FormLabel(text = "Delivered Date")
                                FormTextField(
                                    value = deliveredDateDisplay,
                                    onValueChange = {},
                                    placeholder = "Auto-filled",
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    val garmentList = selectedOrder?.let { it.garments.ifEmpty { it.items } } ?: emptyList()
                    val garmentOptions = (garmentList.map { it.getDisplayName() } + selectedGarmentName)
                        .filter { it.isNotBlank() }
                        .distinct()

                    FormDropdown(
                        label = "Select Primary Garment for Return",
                        value = selectedGarmentName,
                        expanded = isGarmentDropdownExpanded,
                        onExpandChange = { isGarmentDropdownExpanded = it },
                        options = garmentOptions,
                        enabled = garmentOptions.isNotEmpty(),
                        onOptionSelected = { chosenGarment ->
                            selectedGarmentName = chosenGarment
                            serviceCategoryText = "$chosenGarment Adjustments"
                        }
                    )
                }

                // ─── 2. CUSTOMER SNAPSHOT CARD ───────────────────────────
                FormCard(
                    icon = Icons.Outlined.Person,
                    title = "Customer Snapshot"
                ) {
                    Column {
                        FormLabel(text = "Customer Name", isRequired = true)
                        FormTextField(
                            value = customerNameText,
                            onValueChange = { customerNameText = it },
                            placeholder = "Customer Full Name"
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                FormLabel(text = "Phone Number", isRequired = true)
                                FormTextField(
                                    value = customerPhoneText,
                                    onValueChange = { customerPhoneText = it },
                                    placeholder = "Mobile number"
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                FormLabel(text = "Email Address")
                                FormTextField(
                                    value = customerEmailText,
                                    onValueChange = { customerEmailText = it },
                                    placeholder = "customer@example.com"
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Column {
                        FormLabel(text = "Customer Address")
                        FormTextArea(
                            value = customerAddressText,
                            onValueChange = { customerAddressText = it },
                            placeholder = "Full delivery or communication address",
                            minLines = 2,
                            maxLines = 3
                        )
                    }
                }

                // ─── 3. RETURN & SERVICE DETAILS CARD ────────────────────
                FormCard(
                    icon = Icons.Outlined.Checklist,
                    title = "Return & Service Details"
                ) {
                    FormDropdown(
                        label = "Return Type",
                        isRequired = true,
                        value = returnType,
                        expanded = isReturnTypeDropdownExpanded,
                        onExpandChange = { isReturnTypeDropdownExpanded = it },
                        options = RETURN_TYPE_OPTIONS,
                        onOptionSelected = { returnType = it }
                    )

                    Spacer(Modifier.height(8.dp))

                    FormDropdown(
                        label = "Priority Level",
                        value = priority,
                        expanded = isPriorityDropdownExpanded,
                        onExpandChange = { isPriorityDropdownExpanded = it },
                        options = PRIORITY_OPTIONS,
                        onOptionSelected = { priority = it }
                    )

                    Spacer(Modifier.height(8.dp))

                    Column {
                        FormLabel(text = "Preferred Completion Date")
                        DatePickerField(
                            value = preferredCompletionDateDisplay,
                            onDateSelected = { preferredCompletionDateDisplay = it }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Column {
                        FormLabel(text = "Service Category")
                        FormTextField(
                            value = serviceCategoryText,
                            onValueChange = { serviceCategoryText = it },
                            placeholder = "e.g. Suit Fitting & Adjustments"
                        )
                    }
                }

                // ─── 4. ISSUE DESCRIPTION & RESOLUTION CARD ──────────────
                FormCard(
                    icon = Icons.Outlined.ContentPaste,
                    title = "Issue Description & Resolution"
                ) {
                    Column {
                        FormLabel(text = "Issue Description", isRequired = true)
                        FormTextArea(
                            value = issueDescriptionText,
                            onValueChange = { issueDescriptionText = it },
                            placeholder = "Describe the issue...",
                            minLines = 3,
                            maxLines = 5
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Column {
                        FormLabel(text = "Preferred Resolution Notes")
                        FormTextArea(
                            value = preferredResolutionNotesText,
                            onValueChange = { preferredResolutionNotesText = it },
                            placeholder = "Desired outcome...",
                            minLines = 3,
                            maxLines = 4
                        )
                    }
                }

                // ─── 5. PHOTOGRAPHIC EVIDENCE CARD ───────────────────────
                FormCard(
                    icon = Icons.Outlined.CloudUpload,
                    title = "Photographic Evidence"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(cardBgLight)
                            .clickable {
                                val totalCurrent = existingAttachments.size + selectedNewImages.size
                                if (totalCurrent < 5) imagePickerLauncher.launch("image/*")
                                else DynamicIslandManager.showError("Maximum 5 files allowed")
                            }
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.CloudUpload,
                                contentDescription = "Upload",
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Upload evidence or fitting photos",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Supports JPG, PNG • Max 5 files",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }
                    }

                    val totalPhotos = existingAttachments.size + selectedNewImages.size
                    if (totalPhotos > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Existing Cloudinary Images from API
                            existingAttachments.forEachIndexed { index, item ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(light_grey)
                                ) {
                                    AsyncImage(
                                        model = item.url,
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable {
                                                existingAttachments = existingAttachments.toMutableList().also {
                                                    it.removeAt(index)
                                                }
                                            }
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = whiteBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // 2. Newly picked local device images
                            selectedNewImages.forEachIndexed { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(light_grey)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable {
                                                selectedNewImages = selectedNewImages.toMutableList().also {
                                                    it.removeAt(index)
                                                }
                                            }
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = whiteBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ─── 6. INTERNAL NOTES CARD ──────────────────────────────
                FormCard(
                    icon = Icons.Outlined.Lock,
                    title = "Internal Notes"
                ) {
                    Column {
                        FormLabel(text = "Staff Processing Notes")
                        FormTextArea(
                            value = internalNotesText,
                            onValueChange = { internalNotesText = it },
                            placeholder = "Internal notes...",
                            minLines = 3,
                            maxLines = 4
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FormCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            content()
        }
    }
}

private fun formatToDisplayDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoString)
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: ""
    } catch (_: Exception) {
        isoString
    }
}

private fun formatToPayloadDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoString)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        date?.let { formatter.format(it) } ?: ""
    } catch (_: Exception) {
        isoString
    }
}

private fun convertPickerDateToPayloadDate(pickerDate: String): String {
    if (pickerDate.isBlank()) return ""
    return try {
        val parser = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = parser.parse(pickerDate)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        date?.let { formatter.format(it) } ?: pickerDate
    } catch (_: Exception) {
        pickerDate
    }
}