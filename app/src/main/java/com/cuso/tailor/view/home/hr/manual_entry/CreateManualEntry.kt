@file:Suppress("unused", "unusedVariable", "AssignedValueIsNeverRead", "VariableNeverRead")
package com.cuso.tailor.view.home.hr.manual_entry

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun ManualAttendanceEntryScreen(
    onClose: () -> Unit = {},
    onSubmit: () -> Unit = {},
    viewModel: HrViewModel
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Default time values so validation won't fail if user keeps default
    var dateString by remember { mutableStateOf("") }
    var checkInTime by remember { mutableStateOf("09:00 AM") }
    var checkOutTime by remember { mutableStateOf("05:00 PM") }
    var reasonText by remember { mutableStateOf("") }
    var selectedApprover by remember { mutableStateOf("") }
    var approverDropdownOpen by remember { mutableStateOf(false) }
    var selectedApproverId by remember { mutableStateOf("") }

    val isSubmitting by viewModel.isSubmittingManualAttendance.collectAsState()

    // Fetch members on screen launch
    LaunchedEffect(Unit) {
        viewModel.fetchMembers(page = 1, limit = 100)
    }

    val membersList by viewModel.members.collectAsState()
    val isLoadingMembers by viewModel.isLoadingMembers.collectAsState()

    // Holds selected employee details
    var selectedMemberId by remember { mutableStateOf("") }
    var selectedMemberName by remember { mutableStateOf("") }
    var memberDropdownOpen by remember { mutableStateOf(false) }

    // Map members to readable dropdown labels: "FirstName LastName (MemberId)"
    val memberOptions = remember(membersList) {
        membersList.map { member ->
            val fullName = listOfNotNull(member.firstName, member.lastName).joinToString(" ").trim()
            val code = member.memberId?.takeIf { it.isNotBlank() } ?: ""
            if (code.isNotBlank()) "$fullName ($code)" else fullName
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TitleBar(title = "Manual Attendance Entry", onClose = onClose)
        },
        bottomBar = {
            // Fixed Bottom Actions (Cancel + Submit Request)
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
                        enabled = !isSubmitting,
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
                            viewModel.submitManualAttendance(
                                organizationMemberId = selectedMemberId,
                                date = dateString,
                                checkIn = checkInTime,
                                checkOut = checkOutTime,
                                adminNote = reasonText,
                                onSuccess = {
                                    Toast.makeText(context, "Manual attendance created successfully", Toast.LENGTH_SHORT).show()
                                    onSubmit()
                                },
                                onError = { errorMsg ->
                                    // Shows exact reason if validation fails or API errors out
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Submit Request",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section Sub-header
                Column {
                    Text(
                        "Manual Attendance Entry",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Edit, Manual Entry for users...",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }

                // 1. Employee Dropdown
                FormDropdown(
                    label = "Employee",
                    isRequired = true,
                    value = if (isLoadingMembers && memberOptions.isEmpty()) "Loading employees..." else selectedMemberName,
                    expanded = memberDropdownOpen,
                    onExpandChange = { memberDropdownOpen = it },
                    options = memberOptions,
                    onOptionSelected = { selectedLabel ->
                        selectedMemberName = selectedLabel
                        val selectedIndex = memberOptions.indexOf(selectedLabel)
                        if (selectedIndex != -1) {
                            selectedMemberId = membersList[selectedIndex]._id
                        }
                    }
                )

                // 2. Date input with functional DatePickerField
                Column {
                    FormLabel(text = "Date", isRequired = true)
                    DatePickerField(
                        value = dateString,
                        onDateSelected = { dateString = it }
                    )
                }

                // 3. Check-in & Check-out Time row with TimePickerField
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Check-in Time", isRequired = true)
                        TimePickerField(
                            value = checkInTime,
                            onTimeSelected = { checkInTime = it }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Check-out Time", isRequired = true)
                        TimePickerField(
                            value = checkOutTime,
                            onTimeSelected = { checkOutTime = it }
                        )
                    }
                }

                // 4. Reason for Manual Entry
                Column {
                    FormLabel(text = "Reason for Manual Entry")
                    FormTextArea(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        placeholder = "Please describe why this manual entry is required...",
                        minLines = 4,
                        borderColor = grey_border
                    )
                }

                // 5. Approver Dropdown
                FormDropdown(
                    label = "Approver",
                    isRequired = true,
                    value = selectedApprover,
                    expanded = approverDropdownOpen,
                    onExpandChange = { approverDropdownOpen = it },
                    options = memberOptions,
                    onOptionSelected = { selectedLabel ->
                        selectedApprover = selectedLabel
                        val selectedIndex = memberOptions.indexOf(selectedLabel)
                        if (selectedIndex != -1) {
                            selectedApproverId = membersList[selectedIndex]._id
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}