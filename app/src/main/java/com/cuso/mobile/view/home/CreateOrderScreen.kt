package com.cuso.mobile.view.sales

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberImagePainter
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.CustomerGarment
import com.cuso.mobile.model.CustomerOrder
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.home.DatePickerField
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.example.tailorapp.ui.screens.OrderReviewData
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

// ─────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────

data class GarmentModel(
    val id: String,
    val name: String,
    val isSelected: Boolean = false
)

data class MeasurementField(
    val id: String,
    val label: String,
    val value: String = "",
    val unit: String = "inch"
)

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreateOrderScreen(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onNextStep: (OrderReviewData) -> Unit = {},
    salesViewModel: SalesViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // ── Customer state ──
    var phone by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var dressFor by remember { mutableStateOf("Men") }
    var source by remember { mutableStateOf("Walk-in") }
    var showDressDropdown by remember { mutableStateOf(false) }
    var showSourceDropdown by remember { mutableStateOf(false) }
    var countryCode by remember { mutableStateOf("+91") }

    // ── Design Reference - Photo Upload States ──
    var selectedDesignImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showImagePickerOptions by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    //--Branch---from api
    // ── Branches ──
    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = (branchUiState as? com.cuso.mobile.viewmodel.BranchUiState.Success)?.branches ?: emptyList()
    val isLoadingBranches = branchUiState is com.cuso.mobile.viewmodel.BranchUiState.Loading
    var selectedBranchId by remember { mutableStateOf("") }
    // ── Permission state ──
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // ── Gallery Launcher ──
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedDesignImages = selectedDesignImages + it
            // Upload logic here
            // salesViewModel.uploadDesignImage(it)
        }
    }

    // ── Camera Launcher ──
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            capturedImageUri?.let { uri ->
                selectedDesignImages = selectedDesignImages + uri
                // Upload logic here
                // salesViewModel.uploadDesignImage(uri)
                capturedImageUri = null
            }
        }
    }

    // ── Function to handle camera capture ──
    fun captureDesignImage() {
        if (cameraPermissionState.status.isGranted) {
            val tempFile = File.createTempFile("design_image_", ".jpg", context.cacheDir)
            capturedImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            capturedImageUri?.let { cameraLauncher.launch(it) }
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // ── Image picker options dialog ──
    if (showImagePickerOptions) {
        AlertDialog(
            onDismissRequest = { showImagePickerOptions = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Add Design Reference",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gallery option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                            .clickable {
                                showImagePickerOptions = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            null,
                            tint = Color(0xFF3B3BF9),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Browser",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF111827)
                            )

                        }
                    }

                    // Camera option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                            .clickable {
                                showImagePickerOptions = false
                                captureDesignImage()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            tint = Color(0xFF3B3BF9),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Take Photo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF111827)
                            )
                            Text(
                                "Capture with camera",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    // Remove all photos option
                    if (selectedDesignImages.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                .clickable {
                                    showImagePickerOptions = false
                                    selectedDesignImages = emptyList()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Remove All Photos",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFEF4444)
                                )
                                Text(
                                    "Remove all selected images",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showImagePickerOptions = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text("Cancel")
                }
            },
            dismissButton = null
        )
    }

    // ── Customer search states ──
    val customerSearchResult by salesViewModel.customerSearchResult.collectAsStateWithLifecycle()
    val isSearchingCustomer by salesViewModel.isSearchingCustomer.collectAsStateWithLifecycle()
    var showImportDialog by remember { mutableStateOf(false) }

    // ── Garment categories ──
    val activeOrgCategoryIds by salesViewModel.activeOrgCategoryIds.collectAsStateWithLifecycle()
    val commonCategories by salesViewModel.orgGarmentCategories.collectAsStateWithLifecycle()
    val isLoadingCategories by salesViewModel.isLoadingOrgGarments.collectAsStateWithLifecycle()

    // ── Selected garments ──
    val selectedGarments by salesViewModel.selectedGarments.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        salesViewModel.fetchOrgGarmentCategories()
        salesViewModel.fetchActiveOrgGarments()
        salesViewModel.loadSelectedGarments()
    }

    // ── Trigger search on phone change ──
    LaunchedEffect(phone) {
        if (phone.length >= 4) {
            salesViewModel.searchCustomerByMobile(
                mobile = phone,
                countryCode = countryCode
            )
        } else {
            salesViewModel.clearCustomerSearch()
        }
    }

    val quickCategories: List<Pair<String, String>> = remember(commonCategories, activeOrgCategoryIds) {
        commonCategories
            .filter { it._id in activeOrgCategoryIds }
            .map { it.categoryName to it._id }
    }

    // ── Dialog state ──
    var showGarmentDialog by remember { mutableStateOf(false) }
    var editingGarmentId by remember { mutableStateOf<String?>(null) }

    var tempGarment by remember {
        mutableStateOf(
            SelectedGarment(
                category = "",
                categoryName = "",
                quantity = 1,
                price = 0.0,
                priority = "Low",
                trialRequired = false,
                fabricSource = "In-House",
                fabricType = "",
                colorTone = "",
                pattern = "Solid",
                models = emptyList()
            )
        )
    }

    // ── Delivery state ──
    var orderDate by remember { mutableStateOf("") }
    var trialDate by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var showBranchDropdown by remember { mutableStateOf(false) }

    // ── Notes ──
    var stylingNotes by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }

    fun openGarmentDialog(categoryName: String, category: String) {
        tempGarment = SelectedGarment(
            category = category,
            categoryName = categoryName,
            quantity = 1,
            price = 0.0,
            priority = "Low",
            trialRequired = false,
            fabricSource = "In-House",
            fabricType = "",
            colorTone = "",
            pattern = "Solid",
            models = emptyList()
        )
        editingGarmentId = null
        showGarmentDialog = true
    }

    fun editGarmentDialog(garment: SelectedGarment) {
        tempGarment = garment
        editingGarmentId = garment.id
        showGarmentDialog = true
    }

    fun saveGarment() {
        salesViewModel.addOrUpdateGarment(tempGarment)
        showGarmentDialog = false
    }

    fun deleteGarment(garmentId: String) {
        salesViewModel.deleteSelectedGarment(garmentId)
    }

    // ── Import garments from previous order ──
    fun importGarments(garments: List<CustomerGarment>) {
        garments.forEach { cg ->
            val garment = SelectedGarment(
                category = cg.category,
                categoryName = cg.categoryName,
                quantity = cg.quantity,
                priority = cg.priority,
                trialRequired = cg.trialRequired,
                fabricSource = cg.fabricDetails?.fabricSource ?: "In-House",
                fabricType = cg.fabricDetails?.fabricType ?: "",
                colorTone = cg.fabricDetails?.color ?: "",
                pattern = cg.fabricDetails?.pattern ?: "Solid",
                models = cg.models
            )
            salesViewModel.addOrUpdateGarment(garment)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onBack() },
                    tint = Color(0xFF111827)
                )
                Text(
                    "Create Order",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        salesViewModel.clearAllSelectedGarments()
                        salesViewModel.clearCustomerSearch()
                        onCancel()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = {
                        val data = OrderReviewData(
                            fullName = fullName,
                            countryCode = countryCode,
                            phone = phone,
                            gender = gender,
                            dressFor = dressFor,
                            address = address,
                            garments = selectedGarments,
                            trialDate = trialDate,
                            deliveryDate = deliveryDate
                        )
                        onNextStep(data)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Next Step", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF3F4F6)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ══════════════════════════════════════════════
                // 1. CUSTOMER DETAILS
                // ══════════════════════════════════════════════
                SectionCard(title = "CUSTOMER DETAILS") {

                    FormLabel("Phone")

                    // ── Phone field ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhoneInputField(
                                phoneValue = phone,
                                onPhoneChange = { newPhone ->
                                    phone = newPhone
                                    if (newPhone.isEmpty()) salesViewModel.clearCustomerSearch()
                                },
                                onCountryChange = { country ->
                                    countryCode = country.code
                                }
                            )
                        }
                        if (isSearchingCustomer) {
                            Spacer(Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF3B3BF9)
                            )
                        }
                    }

                    // ── Found Customer Card ──
                    AnimatedVisibility(
                        visible = customerSearchResult?.customer != null,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        customerSearchResult?.customer?.let { customer ->
                            Spacer(Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(3.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF0FDF4))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "FOUND CUSTOMER",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF16A34A),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { salesViewModel.clearCustomerSearch() }
                                        )
                                    }

                                    // Customer info + Import button
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                customer.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )
                                            val orderCount = customerSearchResult?.orders?.size ?: 0
                                            Text(
                                                "Found $orderCount previous order${if (orderCount != 1) "s" else ""}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }

                                        // Import Data button
                                        if ((customerSearchResult?.orders?.size ?: 0) > 0) {
                                            OutlinedButton(
                                                onClick = {
                                                    fullName = customer.name
                                                    address = customer.address?.addressLine ?: ""
                                                    showImportDialog = true
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    Color(0xFF3B3BF9)
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = Color(0xFFEEF2FF)
                                                ),
                                                contentPadding = PaddingValues(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Default.Download,
                                                    null,
                                                    tint = Color(0xFF3B3BF9),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "Import Data",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF3B3BF9),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        } else {
                                            // Just auto-fill name
                                            OutlinedButton(
                                                onClick = {
                                                    fullName = customer.name
                                                    address = customer.address?.addressLine ?: ""
                                                    salesViewModel.clearCustomerSearch()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    Color(0xFF3B3BF9)
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = Color(0xFFEEF2FF)
                                                ),
                                                contentPadding = PaddingValues(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                )
                                            ) {
                                                Text(
                                                    "Use Details",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF3B3BF9),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    FormLabel("Full Name")
                    FormTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Enter Customer Name"
                    )

                    Spacer(Modifier.height(4.dp))

                    FormLabel("Address")
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text(
                                "Enter full billing/shipping address...",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF3B3BF9),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    Spacer(Modifier.height(4.dp))

                    FormLabel("Gender")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { gender = option }
                            ) {
                                RadioButton(
                                    selected = gender == option,
                                    onClick = { gender = option },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B3BF9))
                                )
                                Text(option, fontSize = 14.sp, color = Color(0xFF374151))
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    FormLabel("Dress For")
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .clickable { showDressDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dressFor, fontSize = 14.sp, color = Color(0xFF111827))
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280))
                        }
                        DropdownMenu(
                            expanded = showDressDropdown,
                            onDismissRequest = { showDressDropdown = false },
                            containerColor = Color.White
                        ) {
                            listOf("Men", "Women", "Kids", "Unisex").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { dressFor = opt; showDressDropdown = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    FormLabel("Source")
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .clickable { showSourceDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(source, fontSize = 14.sp, color = Color(0xFF111827))
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280))
                        }
                        DropdownMenu(
                            expanded = showSourceDropdown,
                            onDismissRequest = { showSourceDropdown = false },
                            containerColor = Color.White
                        ) {
                            listOf("Walk-in", "Phone", "WhatsApp", "Referral", "Online").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { source = opt; showSourceDropdown = false }
                                )
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 2. GARMENT DETAILS
                // ══════════════════════════════════════════════
                SectionCard(
                    title = "GARMENT DETAILS",
                    action = {
                        Row(
                            modifier = Modifier.clickable { openGarmentDialog("", "") },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = Color(0xFF3B3BF9),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Add Category",
                                fontSize = 13.sp,
                                color = Color(0xFF3B3BF9),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                ) {
                    Text(
                        "QUICK ADD CATEGORY",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    when {
                        isLoadingCategories -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp))
                            }
                        }

                        quickCategories.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No garment categories configured. Add them in Settings.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        else -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(quickCategories) { (name, categoryId) ->
                                    Column(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                            .clickable { openGarmentDialog(name, categoryId) }
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color(0xFFEEF2FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Checkroom,
                                                contentDescription = name,
                                                tint = Color(0xFF3B3BF9),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Text(
                                            name,
                                            fontSize = 12.sp,
                                            color = Color(0xFF374151),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "SELECTED GARMENTS",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    if (selectedGarments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No garments added yet.",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedGarments.forEach { garment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                        .clickable { editGarmentDialog(garment) }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFEEF2FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Checkroom,
                                                null,
                                                tint = Color(0xFF3B3BF9),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                garment.categoryName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )
                                            val subtitleParts = mutableListOf<String>()
                                            subtitleParts.add("Qty: ${garment.quantity}")
                                            if (garment.fabricType.isNotBlank()) subtitleParts.add(garment.fabricType)
                                            if (garment.colorTone.isNotBlank()) subtitleParts.add(garment.colorTone)
                                            if (garment.pattern.isNotBlank() && garment.pattern != "Solid") subtitleParts.add(
                                                garment.pattern
                                            )
                                            Text(
                                                subtitleParts.joinToString(" | "),
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { editGarmentDialog(garment) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                null,
                                                tint = Color(0xFF3B3BF9),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { deleteGarment(garment.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            null,
                                            tint = Color(0xFF9CA3AF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 3. DELIVERY DETAILS
                // ══════════════════════════════════════════════
                SectionCard(title = "DELIVERY DETAILS") {
                    FormLabel("Order Date")
                    DatePickerField(
                        value = orderDate.ifEmpty { "Select Date" },
                        onDateSelected = { orderDate = it }
                    )
                    Spacer(Modifier.height(4.dp))
                    FormLabel("Trial Date")
                    DatePickerField(
                        value = trialDate.ifEmpty { "Select Date" },
                        onDateSelected = { trialDate = it }
                    )
                    Spacer(Modifier.height(4.dp))
                    FormLabel("Target Delivery Date")
                    DatePickerField(
                        value = deliveryDate.ifEmpty { "Select Date" },
                        onDateSelected = { deliveryDate = it }
                    )
                    Spacer(Modifier.height(4.dp))
                    // ── Replace the "Assigned Branch" Box{...} block inside DELIVERY DETAILS SectionCard with this ──
// Add near the other collectAsStateWithLifecycle() calls, ABOVE the Composable's UI code:
//
// val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
// val branches = (branchUiState as? com.cuso.mobile.viewmodel.BranchUiState.Success)?.branches ?: emptyList()
// val isLoadingBranches = branchUiState is com.cuso.mobile.viewmodel.BranchUiState.Loading
//
// Replace `var branch by remember { mutableStateOf("") }` with:
// var selectedBranchId by remember { mutableStateOf("") }

                    FormLabel("Assigned Branch")
                    Box {
                        // NOTE: adjust `b.name` below if BranchItem's display field is called
                        // something else (e.g. b.branchName). `b.id` is assumed to be the
                        // unique identifier field on BranchItem.
                        val selectedBranchName = branches.find { it.id == selectedBranchId }?.name.orEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .clickable { showBranchDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    isLoadingBranches -> "Loading branches..."
                                    selectedBranchName.isEmpty() -> "Select Branch"
                                    else -> selectedBranchName
                                },
                                fontSize = 14.sp,
                                color = if (selectedBranchName.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF111827)
                            )
                            if (isLoadingBranches) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280))
                            }
                        }
                        DropdownMenu(
                            expanded = showBranchDropdown && !isLoadingBranches,
                            onDismissRequest = { showBranchDropdown = false },
                            containerColor = Color.White
                        ) {
                            if (branches.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No branches found", color = Color(0xFF9CA3AF)) },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                branches.forEach { b ->
                                    val branchName = b.name.orEmpty()
                                    val branchId = b.id
                                    if (branchId.isNotBlank()) {
                                        DropdownMenuItem(
                                            text = { Text(branchName.ifBlank { "Unnamed Branch" }) },
                                            onClick = {
                                                selectedBranchId = branchId
                                                showBranchDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 4. DESIGN REFERENCE (with Photo Upload)
                // ══════════════════════════════════════════════
                SectionCard(title = "DESIGN REFERENCE") {
                    // ── Upload buttons ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Browse Files button
                        OutlinedButton(
                            onClick = { showImagePickerOptions = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFD1D5DB)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                null,
                                tint = Color(0xFF3B3BF9),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Browse Files", fontSize = 13.sp, color = Color(0xFF374151))
                        }

                        // Camera button
                        OutlinedButton(
                            onClick = {
                                if (cameraPermissionState.status.isGranted) {
                                    captureDesignImage()
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFD1D5DB)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                tint = Color(0xFF3B3BF9),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Camera", fontSize = 13.sp, color = Color(0xFF374151))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Selected Images Preview ──
                    if (selectedDesignImages.isNotEmpty()) {
                        Text(
                            "SELECTED IMAGES (${selectedDesignImages.size})",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedDesignImages) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3F4F6))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = rememberImagePainter(uri),
                                        contentDescription = "Design Reference",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Remove individual image button
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(20.dp)
                                            .background(Color(0xFFEF4444), CircleShape)
                                            .clickable {
                                                selectedDesignImages = selectedDesignImages.filter { it != uri }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Image,
                                    null,
                                    tint = Color(0xFFD1D5DB),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "No images added",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    "Tap Browse or Camera to add design references",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBDBDBD)
                                )
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 5. INSTRUCTIONS
                // ══════════════════════════════════════════════
                SectionCard(title = "INSTRUCTIONS") {
                    FormLabel("Styling Notes")
                    OutlinedTextField(
                        value = stylingNotes,
                        onValueChange = { stylingNotes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text(
                                "Special requirements, cutting instructions...",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF3B3BF9),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                }

                // ══════════════════════════════════════════════
                // 6. VOICE INSTRUCTIONS
                // ══════════════════════════════════════════════
                SectionCard(title = "VOICE INSTRUCTIONS") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { isRecording = !isRecording },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFFEEF2FF)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                null,
                                tint = if (isRecording) Color.White else Color(0xFF3B3BF9),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isRecording) "Stop Recording" else "Start Recording",
                                color = if (isRecording) Color.White else Color(0xFF3B3BF9),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Garment Detail Dialog ──
            if (showGarmentDialog) {
                GarmentDetailDialog(
                    garment = tempGarment,
                    categories = quickCategories,
                    onGarmentChange = { tempGarment = it },
                    onSave = { saveGarment() },
                    onDismiss = { showGarmentDialog = false }
                )
            }

            // ── Previous Measurements Import Dialog ──
            if (showImportDialog) {
                customerSearchResult?.let { result ->
                    PreviousMeasurementsDialog(
                        orders = result.orders,
                        onImport = { selectedGarmentsList ->
                            importGarments(selectedGarmentsList)
                            showImportDialog = false
                            salesViewModel.clearCustomerSearch()
                        },
                        onDismiss = { showImportDialog = false }
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────
// Previous Measurements Import Dialog
// ─────────────────────────────────────────────────────────────

@Composable
fun PreviousMeasurementsDialog(
    orders: List<CustomerOrder>,
    onImport: (List<CustomerGarment>) -> Unit,
    onDismiss: () -> Unit
) {
    // Track which orders are expanded
    val expandedOrders = remember { mutableStateOf(setOf<String>()) }
    // Track selected garments: orderId -> set of garment ids
    val selectedGarments = remember { mutableStateOf(mapOf<String, Set<String>>()) }

    val totalSelected = selectedGarments.value.values.sumOf { it.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Header ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Previous Measurements",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            "Select garments to copy",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF9CA3AF))
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── Orders List ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orders.forEach { order ->
                        val isExpanded = expandedOrders.value.contains(order.id)
                        val orderSelectedGarments = selectedGarments.value[order.id] ?: emptySet()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFE5E7EB)
                            )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // ── Order Header ──
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedOrders.value =
                                                if (isExpanded)
                                                    expandedOrders.value - order.id
                                                else
                                                    expandedOrders.value + order.id
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "Order #${order.orderNumber}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )
                                            // Status chip
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        when (order.status.lowercase()) {
                                                            "confirmed" -> Color(0xFFDCFCE7)
                                                            "completed" -> Color(0xFFDCFCE7)
                                                            "pending" -> Color(0xFFFEF3C7)
                                                            "cancelled" -> Color(0xFFFFEBEE)
                                                            else -> Color(0xFFE0E7FF)
                                                        },
                                                        RoundedCornerShape(20.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    order.status,
                                                    fontSize = 11.sp,
                                                    color = when (order.status.lowercase()) {
                                                        "confirmed" -> Color(0xFF16A34A)
                                                        "completed" -> Color(0xFF16A34A)
                                                        "pending" -> Color(0xFFD97706)
                                                        "cancelled" -> Color(0xFFDC2626)
                                                        else -> Color(0xFF4338CA)
                                                    },
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Text(
                                            "${order.orderDate.take(10)} • ${order.garments.size} Garment${if (order.garments.size != 1) "s" else ""}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    Icon(
                                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // ── Garments List (expanded) ──
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        order.garments.forEach { garment ->
                                            val isSelected =
                                                orderSelectedGarments.contains(garment.id)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val current =
                                                            selectedGarments.value.toMutableMap()
                                                        val currentSet =
                                                            (current[order.id] ?: emptySet()).toMutableSet()
                                                        if (isSelected) currentSet.remove(garment.id)
                                                        else currentSet.add(garment.id)
                                                        current[order.id] = currentSet
                                                        selectedGarments.value = current
                                                    }
                                                    .padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                // Checkbox
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .background(
                                                            if (isSelected) Color(0xFF3B3BF9) else Color.White,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) Color(0xFF3B3BF9) else Color(0xFFD1D5DB),
                                                            RoundedCornerShape(4.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }

                                                // Garment info
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        garment.categoryName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF111827)
                                                    )
                                                    // Measurements preview
                                                    val measurementText =
                                                        garment.measurementSnapshot
                                                            ?.entries
                                                            ?.take(3)
                                                            ?.joinToString(", ") { it.key }
                                                            ?: ""
                                                    if (measurementText.isNotBlank()) {
                                                        Text(
                                                            "$measurementText...",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF6B7280)
                                                        )
                                                    }
                                                }

                                                // Select text
                                                Text(
                                                    if (isSelected) "Selected" else "Select",
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── Action Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFFE5E7EB)
                        )
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            // Collect all selected garments from all orders
                            val garmentsToImport = orders.flatMap { order ->
                                val selectedIds = selectedGarments.value[order.id] ?: emptySet()
                                order.garments.filter { it.id in selectedIds }
                            }
                            onImport(garmentsToImport)
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                        enabled = totalSelected > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B3BF9),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Import Selected ($totalSelected)",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// GARMENT DETAIL DIALOG
// ─────────────────────────────────────────────────────────────

@Composable
fun GarmentDetailDialog(
    garment: SelectedGarment,
    categories: List<Pair<String, String>>,
    onGarmentChange: (SelectedGarment) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val priorityOptions = listOf("Low", "Medium", "High", "Urgent")
    val fabricSourceOptions = listOf("In-House", "Client")
    val patternOptions = listOf("Solid", "Striped", "Checked", "Printed", "Plain")

    val availableModels = listOf(
        GarmentModel("1", "Ankle Fit"),
        GarmentModel("2", "Mom Fit")
    )

    var selectedModels by remember(garment.id) {
        mutableStateOf(garment.models.toMutableList())
    }

    // ── Measurements state (driven by selected models) ──
    var measurements by remember(garment.id) {
        mutableStateOf(defaultMeasurementsFor(selectedModels))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Add New Garment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            "CONFIGURE DETAILS",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF9CA3AF))
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))
                Text(
                    "BASIC INFORMATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp
                )

                FormLabel("Garment Type")
                GarmentTypeSelector(
                    selectedCategoryId = garment.categoryName,
                    categories = categories,
                    onGarmentTypeChange = { newName, newId ->
                        onGarmentChange(garment.copy(categoryName = newName, category = newId))
                    }
                )

                FormLabel("Quantity")
                QuantitySelector(
                    quantity = garment.quantity,
                    onQuantityChange = { newQty -> onGarmentChange(garment.copy(quantity = newQty)) }
                )

                FormLabel("Priority")
                PrioritySelector(
                    selectedPriority = garment.priority,
                    options = priorityOptions,
                    onPriorityChange = { newPriority ->
                        onGarmentChange(garment.copy(priority = newPriority))
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Switch(
                        checked = garment.trialRequired,
                        onCheckedChange = { isChecked ->
                            onGarmentChange(garment.copy(trialRequired = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF3B3BF9),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                    Column {
                        Text(
                            "Trial Required",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Text("Schedule fitting?", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))
                Text(
                    "FABRIC DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp
                )

                FormLabel("Fabric Source")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fabricSourceOptions.forEach { option ->
                        FilterChip(
                            selected = garment.fabricSource == option,
                            onClick = { onGarmentChange(garment.copy(fabricSource = option)) },
                            label = { Text(option, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEEF2FF),
                                selectedLabelColor = Color(0xFF3B3BF9)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = garment.fabricSource == option,
                                borderColor = Color(0xFFE5E7EB),
                                selectedBorderColor = Color(0xFF3B3BF9)
                            )
                        )
                    }
                }

                FormLabel("Fabric Type")
                FormTextField(
                    value = garment.fabricType,
                    onValueChange = { newValue -> onGarmentChange(garment.copy(fabricType = newValue)) },
                    placeholder = "e.g. Cotton"
                )

                FormLabel("Color / Tone")
                ColorPickerField(
                    value = garment.colorTone,
                    onColorSelected = { hex -> onGarmentChange(garment.copy(colorTone = hex)) }
                )

                FormLabel("Pattern")
                PatternSelector(
                    selectedPattern = garment.pattern,
                    options = patternOptions,
                    onPatternChange = { newPattern -> onGarmentChange(garment.copy(pattern = newPattern)) }
                )

                HorizontalDivider(color = Color(0xFFE5E7EB))
                Text(
                    "MODELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp
                )

                ModelGridSelector(
                    models = availableModels,
                    selectedModels = selectedModels,
                    onModelToggle = { modelName ->
                        val wasEmpty = selectedModels.isEmpty()
                        if (selectedModels.contains(modelName)) {
                            selectedModels.remove(modelName)
                        } else {
                            selectedModels.add(modelName)
                        }
                        // Auto-populate measurement fields the first time a model is picked
                        if (wasEmpty && selectedModels.isNotEmpty() && measurements.isEmpty()) {
                            measurements = defaultMeasurementsFor(selectedModels)
                        }
                        // Clear measurements if no model is selected anymore
                        if (selectedModels.isEmpty()) {
                            measurements = emptyList()
                        }
                        onGarmentChange(garment.copy(models = selectedModels.toList()))
                    }
                )

                // ── MEASUREMENTS SECTION (shows only after a model is selected) ──
                if (selectedModels.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    MeasurementsSection(
                        measurements = measurements,
                        onMeasurementsChange = { updated ->
                            measurements = updated
                            // NOTE: persist `updated` against the garment here once
                            // SelectedGarment exposes a `measurements` field, e.g.:
                            // onGarmentChange(garment.copy(measurements = updated))
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Default measurement fields per selected model(s) ──
// Customize this if different models need different measurement sets.
private fun defaultMeasurementsFor(modelNames: List<String>): List<MeasurementField> {
    if (modelNames.isEmpty()) return emptyList()
    return listOf(
        MeasurementField(id = "chest", label = "Chest"),
        MeasurementField(id = "sleeve_length", label = "Sleeve Length")
    )
}

// ─────────────────────────────────────────────────────────────
// Reusable Components
// ─────────────────────────────────────────────────────────────

@Composable
private fun ModelGridSelector(
    models: List<GarmentModel>,
    selectedModels: List<String>,
    onModelToggle: (String) -> Unit
) {
    val rows = models.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowModels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowModels.forEach { model ->
                    val isSelected = selectedModels.contains(model.name)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) Color(0xFFEEF2FF) else Color.White,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onModelToggle(model.name) }
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Checkroom,
                            contentDescription = model.name,
                            tint = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF374151),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            model.name,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF374151),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // pad odd-count last row so card width stays consistent
                if (rowModels.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MeasurementsSection(
    measurements: List<MeasurementField>,
    onMeasurementsChange: (List<MeasurementField>) -> Unit
) {
    val unitOptions = listOf("inch", "cm")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "MEASUREMENTS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3B3BF9),
            letterSpacing = 0.5.sp
        )

        measurements.forEachIndexed { index, field ->
            MeasurementRow(
                field = field,
                unitOptions = unitOptions,
                onValueChange = { newValue ->
                    val updated = measurements.toMutableList()
                    updated[index] = field.copy(value = newValue)
                    onMeasurementsChange(updated)
                },
                onLabelChange = { newLabel ->
                    val updated = measurements.toMutableList()
                    updated[index] = field.copy(label = newLabel)
                    onMeasurementsChange(updated)
                },
                onUnitChange = { newUnit ->
                    val updated = measurements.toMutableList()
                    updated[index] = field.copy(unit = newUnit)
                    onMeasurementsChange(updated)
                },
                onRemove = {
                    val updated = measurements.toMutableList()
                    updated.removeAt(index)
                    onMeasurementsChange(updated)
                },
                removable = index >= 2 // keep first two (Chest, Sleeve Length) non-removable
            )

            if (index != measurements.lastIndex) {
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }
        }

        Text(
            "+ Add Custom Field",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF3B3BF9),
            modifier = Modifier.clickable {
                val updated = measurements + MeasurementField(
                    id = "custom_${System.currentTimeMillis()}",
                    label = "New Field"
                )
                onMeasurementsChange(updated)
            }
        )
    }
}

@Composable
private fun MeasurementRow(
    field: MeasurementField,
    unitOptions: List<String>,
    onValueChange: (String) -> Unit,
    onLabelChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onRemove: () -> Unit,
    removable: Boolean
) {
    var showUnitDropdown by remember { mutableStateOf(false) }
    var isEditingLabel by remember { mutableStateOf(false) }
    var labelDraft by remember(field.id) { mutableStateOf(field.label) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditingLabel) {
                OutlinedTextField(
                    value = labelDraft,
                    onValueChange = { labelDraft = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF3B3BF9)
                    )
                )
                Icon(
                    Icons.Default.Check,
                    null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            onLabelChange(labelDraft)
                            isEditingLabel = false
                        }
                )
            } else {
                Text(
                    field.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    modifier = Modifier.clickable { isEditingLabel = true }
                )
                if (removable) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemove() }
                    )
                }
            }
        }

        Text("Number", fontSize = 12.sp, color = Color(0xFF9CA3AF))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = field.value,
                onValueChange = { newVal ->
                    if (newVal.isEmpty() || newVal.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onValueChange(newVal)
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("0.0", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF3B3BF9),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
            )

            Box {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .clickable { showUnitDropdown = true }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(field.unit, fontSize = 14.sp, color = Color(0xFF111827))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = showUnitDropdown,
                    onDismissRequest = { showUnitDropdown = false },
                    containerColor = Color.White
                ) {
                    unitOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { onUnitChange(opt); showUnitDropdown = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternSelector(
    selectedPattern: String,
    options: List<String>,
    onPatternChange: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(options) { option ->
            val isSelected = selectedPattern == option
            FilterChip(
                selected = isSelected,
                onClick = { onPatternChange(option) },
                label = { Text(option, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEEF2FF),
                    selectedLabelColor = Color(0xFF3B3BF9)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
                    selectedBorderColor = Color(0xFF3B3BF9)
                )
            )
        }
    }
}
@Composable
private fun SectionCard(
    title: String,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                letterSpacing = 0.8.sp
            )
            action?.invoke()
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF9CA3AF), fontSize = 14.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedBorderColor = Color(0xFF3B3BF9),
            unfocusedContainerColor = Color(0xFFF9FAFB),
            focusedContainerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}

@Composable
private fun DateField(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (value.isEmpty()) placeholder else value,
            fontSize = 14.sp,
            color = if (value.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF111827)
        )
        Icon(
            Icons.Default.CalendarMonth,
            null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(18.dp)
        )
    }
}


/**
 * Color/Tone field: shows a swatch + hex text. Tap to open a full HSV color picker.
 * value -> hex string like "#FF5733" (or "" if not picked)
 */
@Composable
fun ColorPickerField(
    value: String,
    onColorSelected: (String) -> Unit,
    placeholder: String = "Pick a color"
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentColor = remember(value) { parseHexColorOrNull(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(currentColor ?: Color(0xFFE5E7EB), CircleShape)
                .border(1.dp, Color(0xFFD1D5DB), CircleShape)
        )
        Text(
            text = value.ifBlank { placeholder },
            fontSize = 14.sp,
            color = if (value.isBlank()) Color(0xFF9CA3AF) else Color(0xFF111827)
        )
    }

    if (showDialog) {
        ColorPickerDialog(
            initialHex = value,
            onDismiss = { showDialog = false },
            onConfirm = { hex ->
                onColorSelected(hex)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val controller = rememberColorPickerController()
    var selectedHex by remember { mutableStateOf(initialHex.ifBlank { "#3B82F6" }) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Choose Color", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                // ── Full HSV color wheel ──
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(10.dp),
                    controller = controller,
                    initialColor = parseHexColorOrNull(selectedHex) ?: Color(0xFF3B82F6),
                    onColorChanged = { envelope ->
                        val argb = envelope.color.toArgb()
                        val rgbHex = String.format("#%06X", 0xFFFFFF and argb)
                        selectedHex = rgbHex
                    }
                )

                // ── Brightness slider ──
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller
                )

                // ── Alpha slider (optional, remove if not needed) ──
                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller
                )

                // ── Preview + hex display ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                parseHexColorOrNull(selectedHex) ?: Color(0xFFE5E7EB),
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    )
                    Text(selectedHex.uppercase(), fontSize = 14.sp, color = Color(0xFF111827))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF374151))
                    }
                    Button(
                        onClick = { onConfirm(selectedHex.uppercase()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Select", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun parseHexColorOrNull(hex: String): Color? {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        if (cleaned.length != 6 && cleaned.length != 8) return null
        val colorLong = cleaned.toLong(16)
        if (cleaned.length == 6) Color(0xFF000000 or colorLong) else Color(colorLong)
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun GarmentTypeSelector(
    selectedCategoryId: String,
    categories: List<Pair<String, String>>,
    onGarmentTypeChange: (name: String, id: String) -> Unit
) {
    if (categories.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Text(
                "No garment categories configured. Add them in Settings.",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { (name, id) ->
            val isSelected = selectedCategoryId == id
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) Color(0xFFEEF2FF) else Color.White,
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onGarmentTypeChange(name, id) }
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isSelected) Color(0xFF3B3BF9) else Color(0xFFEEF2FF),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Checkroom,
                        contentDescription = name,
                        tint = if (isSelected) Color.White else Color(0xFF3B3BF9),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF374151),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Remove, null, tint = Color(0xFF6B7280))
        }
        Text(
            quantity.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color(0xFF3B3BF9))
        }
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: String,
    options: List<String>,
    onPriorityChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selectedPriority == option
            val color = when (option.lowercase()) {
                "urgent" -> Color(0xFFEF4444)
                "high" -> Color(0xFFFF9800)
                "medium" -> Color(0xFFFFC107)
                else -> Color(0xFF9CA3AF)
            }
            FilterChip(
                selected = isSelected,
                onClick = { onPriorityChange(option) },
                label = { Text(option, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color,
                    disabledContainerColor = Color(0xFFF3F4F6),
                    disabledLabelColor = Color(0xFF9CA3AF)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) color else Color(0xFFE5E7EB),
                    selectedBorderColor = color
                )
            )
        }
    }
}