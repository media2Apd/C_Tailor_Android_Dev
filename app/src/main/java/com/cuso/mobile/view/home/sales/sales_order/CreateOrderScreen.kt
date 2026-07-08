package com.cuso.mobile.view.home.sales.sales_order

import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.cuso.mobile.database.entities.GarmentMeasurement
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.Customer
import com.cuso.mobile.model.CustomerGarment
import com.cuso.mobile.model.CustomerOrder
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.sales.customer.CustomerOutlinedField
import com.cuso.mobile.view.home.sales.customer.LabeledField
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.viewmodel.BranchUiState
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder

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

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    initialData: OrderReviewData? = null,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onNextStep: (OrderReviewData) -> Unit = {},
    salesViewModel: SalesViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // ✅ Check if we're in edit mode
    val isEditMode = initialData?.orderId != null

    var phone by rememberSaveable {
        mutableStateOf(initialData?.phone ?: "")
    }

    var fullName by rememberSaveable {
        mutableStateOf(initialData?.fullName ?: "")
    }

    var address by rememberSaveable {
        mutableStateOf(initialData?.address ?: "")
    }

    var gender by rememberSaveable {
        mutableStateOf(initialData?.gender ?: "")
    }

    var dressFor by rememberSaveable {
        mutableStateOf(initialData?.dressFor ?: "")
    }

    var source by rememberSaveable {
        mutableStateOf(initialData?.source ?: "")
    }

    var countryCode by rememberSaveable {
        mutableStateOf(initialData?.countryCode ?: "+91")
    }

    var selectedDesignImages by rememberSaveable {
        mutableStateOf(initialData?.designImages ?: emptyList())
    }

    var selectedBranchId by rememberSaveable {
        mutableStateOf(initialData?.branchId ?: "")
    }

    var orderDate by rememberSaveable {
        mutableStateOf(initialData?.orderDate ?: "")
    }

    var trialDate by rememberSaveable {
        mutableStateOf(initialData?.trialDate ?: "")
    }

    var deliveryDate by rememberSaveable {
        mutableStateOf(initialData?.deliveryDate ?: "")
    }

    var recordedVoiceNoteUri by rememberSaveable {
        mutableStateOf(initialData?.voiceNoteUri)
    }

    // ── Customer state ──
    var showImagePickerOptions by rememberSaveable { mutableStateOf(false) }
    var capturedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    var dressForExpanded by rememberSaveable { mutableStateOf(false) }
    var sourceExpanded by rememberSaveable { mutableStateOf(false) }

    // ── Branches ──
    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()
    val isLoadingBranches = branchUiState is BranchUiState.Loading
    var genderExpanded by rememberSaveable { mutableStateOf(false) }

    // ── Permission state ──
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // ── Gallery Launcher ──
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedDesignImages = selectedDesignImages + it
        }
    }

    LaunchedEffect(initialData) {
        initialData?.garments?.let { garments ->
            garments.forEach { salesViewModel.addOrUpdateGarment(it) }
        }
    }

    // ── Camera Launcher ──
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            capturedImageUri?.let { uri ->
                selectedDesignImages = selectedDesignImages + uri
                capturedImageUri = null
            }
        }
    }

    var expandedSection by rememberSaveable { mutableStateOf("customer") }

    var branchExpanded by rememberSaveable { mutableStateOf(false) }
    val branchNameToId = rememberSaveable(branches) {
        branches.associate { it.name.orEmpty().ifBlank { "Unnamed Branch" } to it.id }
    }
    val selectedBranchName = branches.find { it.id == selectedBranchId }?.name.orEmpty()

    var stylingNotes by rememberSaveable { mutableStateOf("") }

    var isRecording by rememberSaveable { mutableStateOf(false) }
    var mediaRecorder by rememberSaveable { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by rememberSaveable { mutableStateOf<File?>(null) }

    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

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
                                "Browse",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF111827)
                            )
                        }
                    }

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
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6B7280))
                ) {
                    Text("Cancel")
                }
            },
            dismissButton = null
        )
    }

    val customerSearchResult by salesViewModel.customerSearchResult.collectAsStateWithLifecycle()
    val isSearchingCustomer by salesViewModel.isSearchingCustomer.collectAsStateWithLifecycle()
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    var selectedCustomer by rememberSaveable { mutableStateOf<Customer?>(null) }
    val activeOrgCategoryIds by salesViewModel.activeOrgCategoryIds.collectAsStateWithLifecycle()
    val commonCategories by salesViewModel.orgGarmentCategories.collectAsStateWithLifecycle()
    val isLoadingCategories by salesViewModel.isLoadingOrgGarments.collectAsStateWithLifecycle()
    val selectedGarments by salesViewModel.selectedGarments.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (initialData == null) {
            salesViewModel.clearAllSelectedGarments()
        }
        branchViewModel.loadBranches()
        salesViewModel.fetchOrgGarmentCategories()
        salesViewModel.fetchActiveOrgGarments()
        salesViewModel.loadSelectedGarments()
    }

    LaunchedEffect(phone) {
        if (phone.length >= 4 && !isEditMode) {
            salesViewModel.searchCustomerByMobile(mobile = phone, countryCode = countryCode)
        } else {
            salesViewModel.clearCustomerSearch()
        }
    }

    val quickCategories: List<Pair<String, String>> = remember(commonCategories, activeOrgCategoryIds) {
        commonCategories
            .filter { it._id in activeOrgCategoryIds }
            .map { it.categoryName to it._id }
    }

    var showGarmentDialog by rememberSaveable { mutableStateOf(false) }
    var editingGarmentId by rememberSaveable { mutableStateOf<String?>(null) }

    var tempGarment by remember {
        mutableStateOf(
            SelectedGarment(
                category = "",
                categoryName = "",
                categoryId = "",
                quantity = 1,
                price = 0.0,
                priority = "",
                trialRequired = false,
                fabricSource = "",
                fabricType = "",
                colorTone = "",
                pattern = "",
                models = emptyList()
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording() {
        val file = File.createTempFile("voice_note_", ".m4a", context.cacheDir)
        recordingFile = file
        mediaRecorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        isRecording = true
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
            // recording too short or already stopped — ignore
        }
        mediaRecorder = null
        isRecording = false
        recordingFile?.let { file ->
            recordedVoiceNoteUri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
        }
    }

    fun openGarmentDialog(categoryName: String, category: String) {
        android.util.Log.d("GARMENT_DEBUG", "categoryName param=$categoryName | category param=$category")
        tempGarment = SelectedGarment(
            category = category,
            categoryName = categoryName,
            categoryId = category,
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

    fun importGarments(garments: List<CustomerGarment>) {
        garments.forEach { cg ->
            val garment = SelectedGarment(
                category = cg.category,
                categoryName = cg.categoryName,
                categoryId = cg.id,
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
                Text(
                    if (isEditMode) "Edit Order" else "Create Order",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "close",
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            salesViewModel.clearAllSelectedGarments()
                            salesViewModel.clearCustomerSearch()
                            onBack()
                        },
                    tint = Color(0xFF111827)
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        salesViewModel.clearAllSelectedGarments()
                        salesViewModel.clearCustomerSearch()
                        onCancel()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF374151)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        val data = OrderReviewData(
                            orderId = initialData?.orderId,
                            customerId = selectedCustomer?.id ?: initialData?.customerId ?: "",
                            branchId = selectedBranchId,
                            fullName = fullName,
                            countryCode = countryCode,
                            phone = phone,
                            gender = gender,
                            dressFor = dressFor,
                            address = address,
                            garments = selectedGarments,
                            orderDate = orderDate,
                            source = source,
                            trialDate = trialDate,
                            deliveryDate = deliveryDate,
                            discount = initialData?.discount ?: 0.0,
                            paidSoFar = initialData?.paidSoFar ?: 0.0,
                            designImages = selectedDesignImages,
                            existingImageUrls = initialData?.existingImageUrls ?: emptyList(),
                            voiceNoteUri = recordedVoiceNoteUri
                        )
                        onNextStep(data)
                    },
                    modifier = Modifier.weight(2f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 3.dp
                    )
                ) {
                    Text(
                        if (isEditMode) "Update Order" else "Next Step",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    .padding(bottom = 16.dp)
            ) {

                // ══════════════════════════════════════════════
                // 1. CUSTOMER DETAILS
                // ══════════════════════════════════════════════
                SectionCard(
                    title = "Customer Details",
                    expanded = expandedSection == "customer",
                    onToggle = { expandedSection = if (expandedSection == "customer") "" else "customer" }
                ) {

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        PhoneInputField(
                            phoneValue = phone,
                            onPhoneChange = { newPhone ->
                                if (!isEditMode) {
                                    phone = newPhone
                                    if (newPhone.isEmpty()) salesViewModel.clearCustomerSearch()
                                }
                            },
                            onCountryChange = { country -> countryCode = country.code },
                            isLoading = isSearchingCustomer,
                            onRetry = {
                                if (phone.length >= 4 && !isEditMode) {
                                    salesViewModel.searchCustomerByMobile(mobile = phone, countryCode = countryCode)
                                }
                            },
                            enabled = !isEditMode
                        )
                    }

                    // Hide customer search in edit mode
                    if (!isEditMode) {
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

                                            if ((customerSearchResult?.orders?.size ?: 0) > 0) {
                                                OutlinedButton(
                                                    onClick = {
                                                        fullName = customer.name
                                                        address = customer.address?.addressLine ?: ""
                                                        showImportDialog = true
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFF3B3BF9)),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        containerColor = Color(0xFFEEF2FF)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
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
                                                OutlinedButton(
                                                    onClick = {
                                                        fullName = customer.name
                                                        address = customer.address?.addressLine ?: ""
                                                        salesViewModel.clearCustomerSearch()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFF3B3BF9)),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        containerColor = Color(0xFFEEF2FF)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
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
                    }

                    Spacer(Modifier.height(4.dp))

                    LabeledField("Full Name *") {
                        CustomerOutlinedField(
                            value = fullName,
                            onValueChange = { if (!isEditMode) fullName = it },
                            placeholder = "Enter your name",
                            enabled = !isEditMode
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    FormLabel("Address")
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = {
                            Text(
                                "Enter full billing/shipping address...",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF3B3BF9),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF111827)),
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(4.dp))

                    FormDropdown(
                        label = "Gender",
                        value = gender.ifEmpty { "Male" },
                        expanded = genderExpanded,
                        onExpandChange = { genderExpanded = it },
                        options = listOf("Male", "Female", "Other"),
                        onOptionSelected = { if (!isEditMode) gender = it },
                        isRequired = true,
                        enabled = !isEditMode
                    )

                    Spacer(Modifier.height(4.dp))

                    FormDropdown(
                        label = "Dress For",
                        value = dressFor.ifEmpty { "Select an option" },
                        expanded = dressForExpanded,
                        onExpandChange = { dressForExpanded = it },
                        options = listOf("Men", "Women", "Kids", "Unisex"),
                        onOptionSelected = { if (!isEditMode) dressFor = it },
                        isRequired = true

                    )

                    Spacer(Modifier.height(4.dp))

                    FormDropdown(
                        label = "Source",
                        value = source.ifEmpty { "Select an option" },
                        expanded = sourceExpanded,
                        onExpandChange = { sourceExpanded = it },
                        options = listOf("Walk-in", "Phone", "WhatsApp", "Referral", "Online"),
                        onOptionSelected = { if (!isEditMode) source = it },
                        isRequired = true
                    )
                }

                // ══════════════════════════════════════════════
                // 2. GARMENT DETAILS
                // ══════════════════════════════════════════════
                SectionCard(
                    title = "Garment Details",
                    expanded = expandedSection == "garment",
                    onToggle = { expandedSection = if (expandedSection == "garment") "" else "garment" },
                    action = { }
                ) {
                    Row(
                        Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "QUICK ADD CATEGORY",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "+ Add Category",
                            fontSize = 11.sp,
                            color = Primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    when {
                        isLoadingCategories -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(80.dp),
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
                                    .background(Color.White, RoundedCornerShape(8.dp))
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
                                .background(Color.White, RoundedCornerShape(8.dp))
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
                                        .background(Color.White, RoundedCornerShape(8.dp))
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
                                            if (garment.pattern.isNotBlank() && garment.pattern != "Solid") subtitleParts.add(garment.pattern)
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
                SectionCard(
                    title = "Delivery Details",
                    expanded = expandedSection == "delivery",
                    onToggle = { expandedSection = if (expandedSection == "delivery") "" else "delivery" }
                ) {
                    FormLabel("Order Date")
                    DatePickerField(
                        value = orderDate,
                        onDateSelected = { orderDate = it }
                    )
                    Spacer(Modifier.height(4.dp))
                    FormLabel("Trial Date")
                    DatePickerField(
                        value = trialDate,
                        onDateSelected = { trialDate = it }
                    )
                    Spacer(Modifier.height(4.dp))
                    FormLabel("Target Delivery Date")
                    DatePickerField(
                        value = deliveryDate,
                        onDateSelected = { deliveryDate = it }
                    )
                    Spacer(Modifier.height(4.dp))

                    FormDropdown(
                        label = "Assigned Branch",
                        value = when {
                            isLoadingBranches -> "Loading branches..."
                            selectedBranchName.isEmpty() -> "Select Branch"
                            else -> selectedBranchName
                        },
                        expanded = branchExpanded && !isLoadingBranches,
                        onExpandChange = { branchExpanded = it },
                        options = if (branches.isEmpty()) listOf("No branches found") else branchNameToId.keys.toList(),
                        onOptionSelected = { selectedName ->
                            branchNameToId[selectedName]?.let { id -> selectedBranchId = id }
                        },
                        isRequired = true
                    )
                }

                // ══════════════════════════════════════════════
                // 4. DESIGN REFERENCE
                // ══════════════════════════════════════════════
                SectionCard(
                    title = "Design Reference",
                    expanded = expandedSection == "design",
                    onToggle = { expandedSection = if (expandedSection == "design") "" else "design" }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showImagePickerOptions = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.camera),
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Camera", fontSize = 13.sp, color = Color(0xFF374151))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

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
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Design Reference",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
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
                                            contentDescription = "close",
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clickable {
                                                    salesViewModel.clearAllSelectedGarments()
                                                    salesViewModel.clearCustomerSearch()
                                                    onBack()
                                                },
                                            tint = Color(0xFF111827)
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
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
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
                SectionCard(
                    title = "Instructions",
                    expanded = expandedSection == "instructions",
                    onToggle = { expandedSection = if (expandedSection == "instructions") "" else "instructions" }
                ) {
                    FormLabel("Styling Notes")
                    OutlinedTextField(
                        value = stylingNotes,
                        onValueChange = { stylingNotes = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = {
                            Text(
                                "Special requirements, cutting instructions...",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF3B3BF9),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF111827))
                    )

                    Spacer(Modifier.height(16.dp))

                    FormLabel("Voice Instructions")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    if (isRecording) {
                                        stopRecording()
                                    } else if (micPermissionState.status.isGranted) {
                                        startRecording()
                                    } else {
                                        micPermissionState.launchPermissionRequest()
                                    }
                                },
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
                            if (recordedVoiceNoteUri != null && !isRecording) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Voice note recorded ✓",
                                        fontSize = 13.sp,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Remove",
                                        fontSize = 13.sp,
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { recordedVoiceNoteUri = null }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showGarmentDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showGarmentDialog = false },
                    sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true,
                        confirmValueChange = { true }
                    ),
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    dragHandle = {
                        BottomSheetDefaults.DragHandle(modifier = Modifier.padding(vertical = 8.dp))
                    }
                ) {
                    InlineGarmentPanel(
                        garment = tempGarment,
                        categories = quickCategories,
                        onGarmentChange = { tempGarment = it },
                        onSave = {
                            saveGarment()
                            showGarmentDialog = false
                        },
                        onCancel = { showGarmentDialog = false }
                    )
                }
            }

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
    val expandedOrders = remember { mutableStateOf(setOf<String>()) }
    val selectedGarments = remember { mutableStateOf(mapOf<String, Set<String>>()) }
    val totalSelected = selectedGarments.value.values.sumOf { it.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Previous Measurements", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text("Select garments to copy", fontSize = 13.sp, color = Color(0xFF6B7280))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF9CA3AF))
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))

                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orders.forEach { order ->
                        val isExpanded = expandedOrders.value.contains(order.id)
                        val orderSelectedGarments = selectedGarments.value[order.id] ?: emptySet()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedOrders.value =
                                                if (isExpanded) expandedOrders.value - order.id
                                                else expandedOrders.value + order.id
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
                                            val isSelected = orderSelectedGarments.contains(garment.id)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val current = selectedGarments.value.toMutableMap()
                                                        val currentSet = (current[order.id] ?: emptySet()).toMutableSet()
                                                        if (isSelected) currentSet.remove(garment.id) else currentSet.add(garment.id)
                                                        current[order.id] = currentSet
                                                        selectedGarments.value = current
                                                    }
                                                    .padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
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

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        garment.categoryName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF111827)
                                                    )
                                                    val measurementText = garment.measurementSnapshot
                                                        ?.entries
                                                        ?.take(3)
                                                        ?.joinToString(", ") { it.key }
                                                        ?: ""
                                                    if (measurementText.isNotBlank()) {
                                                        Text("$measurementText...", fontSize = 12.sp, color = Color(0xFF6B7280))
                                                    }
                                                }

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            val garmentsToImport = orders.flatMap { order ->
                                val selectedIds = selectedGarments.value[order.id] ?: emptySet()
                                order.garments.filter { it.id in selectedIds }
                            }
                            onImport(garmentsToImport)
                        },
                        modifier = Modifier.weight(2f).height(48.dp),
                        enabled = totalSelected > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B3BF9),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Import Selected ($totalSelected)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// GARMENT DETAIL PANEL (matches uploaded design reference)
// ─────────────────────────────────────────────────────────────

@Composable
private fun InlineGarmentPanel(
    garment: SelectedGarment,
    categories: List<Pair<String, String>>,
    onGarmentChange: (SelectedGarment) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val priorityOptions = listOf("Low", "Medium", "High", "Urgent")
    val fabricSourceOptions = listOf("In-House", "Client")
    val fabricTypeOptions = listOf("Cotton", "Polyester", "Silk", "Wool", "Linen", "Denim", "Satin", "Velvet", "Jersey", "Chiffon")
    val patternOptions = listOf("Solid", "Striped", "Checked", "Printed", "Plain", "Plaid", "Floral")
    val availableModels = listOf(
        GarmentModel("1", "Slim Fit"),
        GarmentModel("2", "Shirt")
    )

    var priorityExpanded by remember { mutableStateOf(false) }
    var fabricTypeExpanded by remember { mutableStateOf(false) }
    var patternExpanded by remember { mutableStateOf(false) }

    var selectedModels by remember(garment.id) { mutableStateOf(garment.models.toMutableList()) }
    var measurements by remember(garment.id) {
        mutableStateOf(
            if (garment.measurements.isNotEmpty())
                garment.measurements.map { m -> MeasurementField(id = m.id.ifBlank { m.label }, label = m.label, value = m.value, unit = m.unit) }
            else defaultMeasurementsFor(selectedModels)
        )
    }

    var subSection by remember { mutableStateOf("basic") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "ADD NEW GARMENT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ── Basic Information ──
            GarmentSubSection(
                icon = Icons.Default.Info,
                label = "Basic Information",
                expanded = subSection == "basic",
                onToggle = { subSection = if (subSection == "basic") "" else "basic" }
            ) {
                LabeledField("Garment Type *") {
                    CustomerOutlinedField(
                        value = garment.categoryName,
                        onValueChange = { newName ->
                            categories.find { it.first == newName }?.let {
                                onGarmentChange(garment.copy(categoryName = it.first, category = it.second))
                            } ?: onGarmentChange(garment.copy(categoryName = newName))
                        },
                        placeholder = "Enter garment type",
                        enabled = false
                    )
                }

                Spacer(Modifier.height(16.dp))

                LabeledField("Quantity *") {
                    CustomerOutlinedField(
                        value = garment.quantity.toString(),
                        onValueChange = { selected ->
                            selected.toIntOrNull()?.let {
                                onGarmentChange(garment.copy(quantity = it))
                            }
                        },
                        placeholder = "Enter quantity",
                        enabled = true
                    )
                }
                Spacer(Modifier.height(16.dp))

                FormDropdown(
                    label = "Priority",
                    value = garment.priority.ifEmpty { "Select Priority" },
                    expanded = priorityExpanded,
                    onExpandChange = { priorityExpanded = it },
                    options = priorityOptions,
                    onOptionSelected = { selected -> onGarmentChange(garment.copy(priority = selected)) },
                    isRequired = true
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Trial Required", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Text("Schedule fitting?", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                    Switch(
                        checked = garment.trialRequired,
                        onCheckedChange = { onGarmentChange(garment.copy(trialRequired = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF3B3BF9),
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── Fabric Details ──
            GarmentSubSection(
                icon = Icons.Default.Description,
                label = "Fabric Details",
                expanded = subSection == "fabric",
                onToggle = { subSection = if (subSection == "fabric") "" else "fabric" }
            ) {
                FormLabel("Fabric Source")
                SegmentedToggle(
                    options = fabricSourceOptions,
                    selected = garment.fabricSource.ifEmpty { "In-House" },
                    onSelect = { selected -> onGarmentChange(garment.copy(fabricSource = selected)) }
                )

                Spacer(Modifier.height(16.dp))

                FormDropdown(
                    label = "Fabric Type",
                    value = garment.fabricType.ifEmpty { "e.g Cotton" },
                    expanded = fabricTypeExpanded,
                    onExpandChange = { fabricTypeExpanded = it },
                    options = fabricTypeOptions,
                    onOptionSelected = { selected -> onGarmentChange(garment.copy(fabricType = selected)) },
                    isRequired = true
                )

                Spacer(Modifier.height(16.dp))

                FormLabel("Color / Tone")
                ColorPickerField(
                    value = garment.colorTone,
                    onColorSelected = { onGarmentChange(garment.copy(colorTone = it)) }
                )

                Spacer(Modifier.height(16.dp))

                FormDropdown(
                    label = "Pattern",
                    value = garment.pattern.ifEmpty { "Select Pattern" },
                    expanded = patternExpanded,
                    onExpandChange = { patternExpanded = it },
                    options = patternOptions,
                    onOptionSelected = { selected -> onGarmentChange(garment.copy(pattern = selected)) },
                    isRequired = true
                )
            }

            // ── Models ──
            GarmentSubSection(
                icon = Icons.Default.RadioButtonUnchecked,
                label = "Models",
                expanded = subSection == "models",
                onToggle = { subSection = if (subSection == "models") "" else "models" }
            ) {
                ModelGridSelector(
                    models = availableModels,
                    selectedModels = selectedModels,
                    onModelToggle = { modelName ->
                        val wasEmpty = selectedModels.isEmpty()
                        if (selectedModels.contains(modelName)) {
                            selectedModels.clear()
                        } else {
                            selectedModels.clear()
                            selectedModels.add(modelName)
                        }
                        if (wasEmpty && selectedModels.isNotEmpty() && measurements.isEmpty()) {
                            measurements = defaultMeasurementsFor(selectedModels)
                        }
                        if (selectedModels.isEmpty()) {
                            measurements = emptyList()
                        }
                        onGarmentChange(garment.copy(models = selectedModels.toList()))
                    }
                )

                if (selectedModels.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    MeasurementsSection(
                        measurements = measurements,
                        onMeasurementsChange = { updated ->
                            measurements = updated
                            onGarmentChange(
                                garment.copy(
                                    measurements = updated.map { m ->
                                        GarmentMeasurement(id = m.id, label = m.label, value = m.value, unit = m.unit)
                                    }
                                )
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Action Buttons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(2f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Reusable Sub-Section Component ──
@Composable
private fun GarmentSubSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by androidx.compose.animation.core.animateFloatAsState(
        if (expanded) 180f else 0f, label = "sub_chevron"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3B3BF9))
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF3B3BF9),
                modifier = Modifier.size(24.dp).rotate(chevronRotation)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)) {
                content()
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB))
    }
}

// ── Segmented toggle (In-House / Client) ──
@Composable
fun SegmentedToggle(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color(0xFF3B3BF9) else Color.Transparent)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF374151)
                )
            }
        }
    }
}

// ── Model selector chips (Slim Fit / Shirt with icon) ──
@Composable
fun ModelGridSelector(
    models: List<GarmentModel>,
    selectedModels: List<String>,
    onModelToggle: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        models.forEach { model ->
            val isSelected = selectedModels.contains(model.name)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
                        RoundedCornerShape(8.dp)
                    )
                    .background(if (isSelected) Color(0xFFEEF0FF) else Color.White)
                    .clickable { onModelToggle(model.name) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    model.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF374151)
                )
            }
        }
    }
}

// ── Measurements Section: renders one MeasurementInputField per measurement + Add Custom Field ──
@Composable
fun MeasurementsSection(
    measurements: List<MeasurementField>,
    onMeasurementsChange: (List<MeasurementField>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        measurements.forEachIndexed { index, field ->
            MeasurementInputField(
                label = "${field.label} (Number)",
                value = field.value,
                onValueChange = { newValue ->
                    val updated = measurements.toMutableList()
                    updated[index] = field.copy(value = newValue)
                    onMeasurementsChange(updated)
                },
                unit = field.unit,
                onUnitChange = { newUnit ->
                    val updated = measurements.toMutableList()
                    updated[index] = field.copy(unit = newUnit)
                    onMeasurementsChange(updated)
                }
            )
        }

        Spacer(Modifier.height(4.dp))

        AddCustomFieldLink(
            onClick = {
                val updated = measurements.toMutableList()
                updated.add(
                    MeasurementField(
                        id = "custom_${System.currentTimeMillis()}",
                        label = "Custom Field",
                        value = "",
                        unit = "inch"
                    )
                )
                onMeasurementsChange(updated)
            }
        )

        // ── Custom field rows (label + value + remove), matches "Label / Value" in image ──
        measurements.filter { it.id.startsWith("custom_") }.forEach { field ->
            val index = measurements.indexOfFirst { it.id == field.id }
            if (index >= 0) {
                Spacer(Modifier.height(8.dp))
                CustomFieldRow(
                    labelValue = field.label,
                    onLabelChange = { newLabel ->
                        val updated = measurements.toMutableList()
                        updated[index] = field.copy(label = newLabel)
                        onMeasurementsChange(updated)
                    },
                    fieldValue = field.value,
                    onFieldValueChange = { newValue ->
                        val updated = measurements.toMutableList()
                        updated[index] = field.copy(value = newValue)
                        onMeasurementsChange(updated)
                    },
                    onRemove = {
                        val updated = measurements.toMutableList()
                        updated.removeAt(index)
                        onMeasurementsChange(updated)
                    }
                )
            }
        }
    }
}

// ── Measurement text field (Chest, Sleeve Length etc with unit dropdown) ──
@Composable
fun MeasurementInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    unitOptions: List<String> = listOf("inch", "cm")
) {
    var unitExpanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF111827)),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 10.dp),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text("0.0", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                        }
                        inner()
                    }
                }
            )

            Box {
                Row(
                    modifier = Modifier
                        .width(72.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { unitExpanded = true }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(unit, fontSize = 13.sp, color = Color(0xFF374151))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    unitOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it, fontSize = 13.sp) },
                            onClick = { onUnitChange(it); unitExpanded = false }
                        )
                    }
                }
            }
        }
    }
}

// ── "+ Add Custom Field" link ──
@Composable
fun AddCustomFieldLink(onClick: () -> Unit) {
    Text(
        "+ Add Custom Field",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF3B3BF9),
        modifier = Modifier.clickable { onClick() }
    )
}

// ── Custom Label / Value row with remove ──
@Composable
fun CustomFieldRow(
    labelValue: String,
    onLabelChange: (String) -> Unit,
    fieldValue: String,
    onFieldValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Label", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = labelValue,
                onValueChange = onLabelChange,
                textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 10.dp),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        if (labelValue.isEmpty()) Text("Label Name", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                        inner()
                    }
                }
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text("Value", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = fieldValue,
                onValueChange = onFieldValueChange,
                textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 10.dp),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        if (fieldValue.isEmpty()) Text("Value", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                        inner()
                    }
                }
            )
        }

        IconButton(onClick = onRemove, modifier = Modifier.padding(top = 20.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFF9CA3AF))
        }
    }
}

// ── Default Measurements ──
private fun defaultMeasurementsFor(modelNames: List<String>): List<MeasurementField> {
    if (modelNames.isEmpty()) return emptyList()
    return listOf(
        MeasurementField(id = "chest", label = "Chest"),
        MeasurementField(id = "sleeve_length", label = "Sleeve Length")
    )
}

@Composable
private fun SectionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by androidx.compose.animation.core.animateFloatAsState(
        if (expanded) 180f else 0f, label = "section_chevron"
    )
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Row(verticalAlignment = Alignment.CenterVertically) {
                action?.invoke()
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp).rotate(chevronRotation)
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                content()
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF0F0F0))
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF6B7280),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

/**
 * Color/Tone field: shows a swatch + hex text. Tap to open a full HSV color picker.
 */
@Composable
fun ColorPickerField(
    value: String,
    onColorSelected: (String) -> Unit,
    placeholder: String = "Color name"
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentColor = remember(value) { parseHexColorOrNull(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Filled.Colorize, contentDescription = "Color picker", tint = PrimaryBorder
        )
        Text(
            text = value.ifBlank { placeholder },
            fontSize = 13.sp,
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
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Choose Color", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                HsvColorPicker(
                    modifier = Modifier.fillMaxWidth().height(260.dp).padding(10.dp),
                    controller = controller,
                    initialColor = parseHexColorOrNull(selectedHex) ?: Color(0xFF3B82F6),
                    onColorChanged = { envelope ->
                        val argb = envelope.color.toArgb()
                        val rgbHex = String.format("#%06X", 0xFFFFFF and argb)
                        selectedHex = rgbHex
                    }
                )

                BrightnessSlider(
                    modifier = Modifier.fillMaxWidth().height(35.dp),
                    controller = controller
                )

                AlphaSlider(
                    modifier = Modifier.fillMaxWidth().height(35.dp),
                    controller = controller
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(parseHexColorOrNull(selectedHex) ?: Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    )
                    Text(selectedHex.uppercase(), fontSize = 14.sp, color = Color(0xFF111827))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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