package com.cuso.tailor.view.home.hr.shift

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.convert24HrTo12Hr
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun CreateNewListScreen(
    viewModel: HrViewModel,
    shiftIdToEdit: String? = null,
    onClose: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !shiftIdToEdit.isNullOrBlank()

    var shiftName by remember { mutableStateOf("") }
    var hrCode by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("05:00 PM") }
    var breakDuration by remember { mutableStateOf("60") }
    var hrType by remember { mutableStateOf("Fixed") }

    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")) }

    val shiftDetail by viewModel.shiftDetail.collectAsState()
    val isLoadingDetail by viewModel.isLoadingShiftDetail.collectAsState()
    val isCreating by viewModel.isCreatingShift.collectAsState()
    val isUpdating by viewModel.isUpdatingShift.collectAsState()
    val isBusy = isCreating || isUpdating

    // Fetch details if in edit mode
    LaunchedEffect(shiftIdToEdit) {
        if (isEditMode) {
            viewModel.fetchShiftDetail(shiftIdToEdit)
        } else {
            viewModel.clearShiftDetail()
        }
    }

    // Pre-fill form when detail arrives from API
    LaunchedEffect(shiftDetail) {
        shiftDetail?.let { detail ->
            shiftName = detail.name.orEmpty()
            hrCode = detail.shiftId.orEmpty()
            startTime = convert24HrTo12Hr(detail.startTime)
            endTime = convert24HrTo12Hr(detail.endTime)
            breakDuration = (detail.breakDuration ?: 0).toString()
            hrType = detail.shiftType?.takeIf { it.isNotBlank() } ?: "Fixed"
            if (detail.customWorkingDays.isNotEmpty()) {
                selectedDays = detail.customWorkingDays
            }
        }
    }

    Scaffold(
        containerColor = whiteBg,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Shift" else "Create New List",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        enabled = !isBusy,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, grey_border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = whiteBg,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (isEditMode) {
                                viewModel.updateShift(
                                    shiftId = shiftIdToEdit,
                                    name = shiftName,
                                    shiftCode = hrCode,
                                    startTime12 = startTime,
                                    endTime12 = endTime,
                                    breakDurationStr = breakDuration,
                                    shiftType = hrType,
                                    customWorkingDays = selectedDays,
                                    onSuccess = {
                                        Toast.makeText(context, "Shift updated successfully", Toast.LENGTH_SHORT).show()
                                        onSubmitSuccess()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                viewModel.createShift(
                                    name = shiftName,
                                    shiftId = hrCode,
                                    startTime12 = startTime,
                                    endTime12 = endTime,
                                    breakDurationStr = breakDuration,
                                    shiftType = hrType,
                                    customWorkingDays = selectedDays,
                                    onSuccess = {
                                        Toast.makeText(context, "Shift created successfully", Toast.LENGTH_SHORT).show()
                                        onSubmitSuccess()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        enabled = !isBusy && !isLoadingDetail,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isEditMode) "Update Shift" else "Submit Request",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoadingDetail) {
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
                    .background(whiteBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Shift Name
                    Column {
                        FormLabel(text = "Shift Name", isRequired = true)
                        FormTextField(
                            value = shiftName,
                            onValueChange = { shiftName = it },
                            placeholder = "e.g. Morning Production A"
                        )
                    }

                    // 2. HR Code
                    Column {
                        FormLabel(text = "HR Code", isRequired = true)
                        FormTextField(
                            value = hrCode,
                            onValueChange = { hrCode = it },
                            placeholder = "e.g. SH-MPA"
                        )
                    }

                    // 3. Start Time & End Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Start Time", isRequired = true)
                            TimePickerField(
                                value = startTime,
                                onTimeSelected = { startTime = it }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "End Time", isRequired = true)
                            TimePickerField(
                                value = endTime,
                                onTimeSelected = { endTime = it }
                            )
                        }
                    }

                    // 4. Break Duration Estimate
                    Column {
                        FormLabel(text = "Break Duration Estimate (mins)")
                        FormTextField(
                            value = breakDuration,
                            onValueChange = { breakDuration = it },
                            placeholder = "e.g. 60"
                        )
                    }

                    // 5. HR Type Switcher (Fixed / Rotational)
                    Column {
                        FormLabel(text = "HR Type")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (hrType == "Fixed") whiteBg else Color.Transparent)
                                        .then(if (hrType == "Fixed") Modifier.border(1.dp, grey_border, RoundedCornerShape(6.dp)) else Modifier)
                                        .clickable { hrType = "Fixed" }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Fixed",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = if (hrType == "Fixed") FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (hrType == "Fixed") Primary else headerGrey
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (hrType == "Rotational") whiteBg else Color.Transparent)
                                        .then(if (hrType == "Rotational") Modifier.border(1.dp, grey_border, RoundedCornerShape(6.dp)) else Modifier)
                                        .clickable { hrType = "Rotational" }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Rotational",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = if (hrType == "Rotational") FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (hrType == "Rotational") Primary else headerGrey
                                    )
                                }
                            }
                        }
                    }

                    // 6. Custom Working Days Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Custom Working Days",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Leave empty to use the organization's default working days.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allDays.forEach { day ->
                                val isSelected = selectedDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Primary else whiteBg)
                                        .border(
                                            1.dp,
                                            if (isSelected) Primary else Color(0xFFD1D5DB),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedDays = if (isSelected) {
                                                selectedDays - day
                                            } else {
                                                selectedDays + day
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}