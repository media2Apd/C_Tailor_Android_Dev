@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")

package com.cuso.tailor.view.home.security.user

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.CreateUserRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.role.SCREENSHOT_PERMISSIONS_CATALOG
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.*
import java.security.SecureRandom

// Standard actions for matrix table columns
private val MATRIX_COLUMNS = listOf(
    "view" to "VIEW",
    "create" to "CREATE",
    "edit" to "EDIT",
    "delete" to "DELETE",
    "approve" to "APPROVE",
    "export" to "EXPORT",
    "print" to "PRINT",
    "share" to "SHARE"
)

private val SCOPE_LABELS = mapOf(
    "branch" to "Branch-level (Inherited)",
    "regional" to "Regional (Inherited)",
    "org-wide" to "Organization-wide (Inherited)"
)

@Composable
fun CreateUserScreen(
    securityViewModel: SecurityViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    designationViewModel: DesignationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onUserCreated: () -> Unit = onBack
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Master state observers from ViewModels
    val rolesList by hrViewModel.roles.collectAsStateWithLifecycle()
    val roleDetails by hrViewModel.roleDetails.collectAsStateWithLifecycle()
    val membersList by hrViewModel.members.collectAsStateWithLifecycle()
    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val departmentUiState by departmentViewModel.uiState.collectAsStateWithLifecycle()
    val designationUiState by designationViewModel.uiState.collectAsStateWithLifecycle()
    val isCreatingUser by securityViewModel.isCreatingUser.collectAsStateWithLifecycle()

    // ── Form State ──
    // Section 1: User Info
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var employeeCode by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    // Section 2: Organization & Assignment
    var selectedDepartmentId by remember { mutableStateOf("") }
    var selectedDepartmentName by remember { mutableStateOf("") }
    var departmentExpanded by remember { mutableStateOf(false) }

    var selectedDesignationId by remember { mutableStateOf("") }
    var selectedDesignationName by remember { mutableStateOf("") }
    var designationExpanded by remember { mutableStateOf(false) }

    var selectedBranchId by remember { mutableStateOf("") }
    var selectedBranchName by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }
    var allowedBranches by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedReportingToId by remember { mutableStateOf("") }
    var selectedReportingToName by remember { mutableStateOf("") }
    var managerExpanded by remember { mutableStateOf(false) }

    // Section 3: Role & Access
    var selectedRoleId by remember { mutableStateOf("") }
    var selectedRoleName by remember { mutableStateOf("") }
    var roleScope by remember { mutableStateOf("") }
    var roleExpanded by remember { mutableStateOf(false) }
    var roleError by remember { mutableStateOf(false) }

    // Base permissions inherited from role
    var rolePermissions by remember { mutableStateOf<Map<String, Map<String, Map<String, Boolean>>>>(emptyMap()) }
    var rolePermissionsFlat by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    // Section 4: Security
    var password by remember { mutableStateOf(generateTempPassword()) }
    var require2FA by remember { mutableStateOf(false) }
    var accountStatus by remember { mutableStateOf("active") }
    var sendSetupEmail by remember { mutableStateOf(true) }

    // Section 5: Additional Permissions (diff map: module -> submodule -> action -> Boolean)
    var customPermissions by remember { mutableStateOf<Map<String, Map<String, Map<String, Boolean>>>>(emptyMap()) }
    var expandedModules by remember { mutableStateOf(setOf<String>()) }
    var hiddenSubmodules by remember { mutableStateOf(setOf<String>()) }

    val previewScrollState = rememberScrollState()
    val overrideScrollState = rememberScrollState()

    // ── 1. Initial Data Fetching from all ViewModels ──
    LaunchedEffect(Unit) {
        hrViewModel.fetchRoles()
        hrViewModel.fetchMembers(page = 1, limit = 50)
        branchViewModel.loadBranches()
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
    }

    // Process Branches from state
    val branches = remember(branchUiState) {
        (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()
    }

    // Process Departments from state
    val departments = remember(departmentUiState) {
        (departmentUiState as? DepartmentUiState.Success)?.departments ?: emptyList()
    }

    // Process Designations from state
    val designations = remember(designationUiState) {
        (designationUiState as? DesignationUiState.Success)?.items ?: emptyList()
    }

    // Auto-select single branch if only one is available
    LaunchedEffect(branches) {
        if (selectedBranchId.isBlank() && branches.size == 1) {
            selectedBranchId = branches.first().id
            selectedBranchName = branches.first().name ?: "Main Branch"
        }
    }

    // Fetch role permissions preview when a role is selected
    LaunchedEffect(selectedRoleId) {
        if (selectedRoleId.isNotBlank()) {
            hrViewModel.fetchRoleById(selectedRoleId)
        }
    }

    // Update role permissions preview matrix when role details arrive
    LaunchedEffect(roleDetails) {
        roleDetails?.let { details ->
            roleScope = details.accessScope ?: "branch"
            val rawPerms = details.permissions ?: emptyMap()

            val structured = mutableMapOf<String, MutableMap<String, MutableMap<String, Boolean>>>()
            val flat = mutableMapOf<String, Boolean>()

            rawPerms.forEach { (modKey, subModMap) ->
                val subMap = mutableMapOf<String, MutableMap<String, Boolean>>()
                subModMap.forEach { (subKey, actMap) ->
                    if (actMap is Map<*, *>) {
                        val actionMap = mutableMapOf<String, Boolean>()
                        actMap.forEach { (actKey, allowed) ->
                            if (allowed == true) {
                                actionMap[actKey.toString()] = true
                                flat["$modKey.$subKey.$actKey"] = true
                            }
                        }
                        subMap[subKey] = actionMap
                    }
                }
                structured[modKey] = subMap
            }

            rolePermissions = structured
            rolePermissionsFlat = flat
            customPermissions = emptyMap() // Reset custom overrides when switching roles
        }
    }

    // Diff count for extra and revoked permissions
    val (extraCount, revokedCount) = remember(customPermissions, rolePermissions) {
        var extra = 0
        var revoked = 0
        customPermissions.forEach { (m, subs) ->
            subs.forEach { (s, acts) ->
                acts.forEach { (a, isGranted) ->
                    val base = rolePermissions[m]?.get(s)?.get(a) == true
                    if (isGranted && !base) extra++
                    if (!isGranted && base) revoked++
                }
            }
        }
        Pair(extra, revoked)
    }

    // Helper to toggle an action override and diff against role defaults
    fun setActionOverride(module: String, submodule: String, action: String, isChecked: Boolean) {
        val baseValue = rolePermissions[module]?.get(submodule)?.get(action) == true

        val currentCopy = customPermissions.mapValues { (_, subs) ->
            subs.mapValues { (_, acts) -> acts.toMutableMap() }.toMutableMap()
        }.toMutableMap()

        val modMap = currentCopy.getOrPut(module) { mutableMapOf() }
        val subMap = modMap.getOrPut(submodule) { mutableMapOf() }

        if (isChecked == baseValue) {
            // Reverted back to role default -> Remove entry
            subMap.remove(action)
            if (subMap.isEmpty()) modMap.remove(submodule)
            if (modMap.isEmpty()) currentCopy.remove(module)
        } else {
            // Overridden difference stored
            subMap[action] = isChecked
        }

        customPermissions = currentCopy
    }

    fun isActionEffective(module: String, submodule: String, action: String): Boolean {
        val override = customPermissions[module]?.get(submodule)?.get(action)
        if (override != null) return override
        return rolePermissions[module]?.get(submodule)?.get(action) == true
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Create New User",
                onClose = onBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            var hasError = false
                            if (firstName.isBlank()) {
                                firstNameError = true
                                hasError = true
                            }
                            if (lastName.isBlank()) {
                                lastNameError = true
                                hasError = true
                            }
                            if (emailAddress.isBlank() || !emailAddress.contains("@")) {
                                emailError = true
                                hasError = true
                            }
                            if (selectedRoleId.isBlank()) {
                                roleError = true
                                hasError = true
                            }
                            if (roleScope == "regional" && allowedBranches.isEmpty()) {
                                Toast.makeText(context, "Select at least one regional branch", Toast.LENGTH_SHORT).show()
                                hasError = true
                            } else if (roleScope != "org-wide" && roleScope != "regional" && selectedBranchId.isBlank()) {
                                Toast.makeText(context, "Branch is required", Toast.LENGTH_SHORT).show()
                                hasError = true
                            }

                            if (hasError) return@Button

                            // Format custom permissions map matching the exact screenshot structure
                            val formattedCustomPermissions: Map<String, Any> = if (customPermissions.isNotEmpty()) {
                                customPermissions.mapValues { (_, subs) ->
                                    subs.mapValues { (_, acts) -> acts }
                                }
                            } else {
                                emptyMap()
                            }

                            // Assemble typed request payload with selected IDs
                            val request = CreateUserRequest(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                employeeCode = employeeCode.trim().ifBlank { null },
                                email = emailAddress.trim(),
                                mobile = mobileNumber.filter { it.isDigit() }.ifBlank { null },
                                password = password,
                                role = "custom",
                                customRoleId = selectedRoleId,
                                branchId = if (roleScope == "regional" || roleScope == "org-wide") null else selectedBranchId.ifBlank { null },
                                allowedBranches = if (roleScope == "regional") allowedBranches else emptyList(),
                                departmentId = selectedDepartmentId.ifBlank { null },
                                designationId = selectedDesignationId.ifBlank { null },
                                reportingTo = selectedReportingToId.ifBlank { null },
                                customPermissions = formattedCustomPermissions,
                                require2FA = require2FA,
                                accountStatus = accountStatus,
                                sendSetupEmail = sendSetupEmail
                            )

                            // Submit to API
                            securityViewModel.createUser(
                                request = request,
                                onSuccess = {
                                    Toast.makeText(context, "User created successfully", Toast.LENGTH_SHORT).show()
                                    onUserCreated()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isCreatingUser,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isCreatingUser) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "Create User",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        },
        containerColor = Primary_background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.5f)
        ) {
            // ── Section 1: User Information ──
            item {
                SectionCard(title = "1. User Information") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        // Photo upload placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(90.dp)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(whiteBg)
                                .clickable { /* File upload */ }
                                .padding(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("UPLOAD PHOTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }

                        // First and Last Name
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            Column {
                                FormLabel("First Name", isRequired = true)
                                Spacer(Modifier.height(4.dp))
                                FormTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it; firstNameError = false },
                                    placeholder = "e.g. Arthur",
                                    isError = firstNameError,
                                    errorMessage = "First name is required"
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            Column {
                                FormLabel("Last Name", isRequired = true)
                                Spacer(Modifier.height(4.dp))
                                FormTextField(
                                    value = lastName,
                                    onValueChange = { lastName = it; lastNameError = false },
                                    placeholder = "e.g. Pendelton",
                                    isError = lastNameError,
                                    errorMessage = "Last name is required"
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Column {
                        FormLabel("Employee ID")
                        Spacer(Modifier.height(4.dp))
                        FormTextField(
                            value = employeeCode,
                            onValueChange = { employeeCode = it.uppercase() },
                            placeholder = "e.g. EMP-10293"
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Column {
                        FormLabel("Email Address", isRequired = true)
                        Spacer(Modifier.height(4.dp))
                        FormTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it; emailError = false },
                            placeholder = "arthur.pendelton@company.com",
                            isError = emailError,
                            errorMessage = "Valid email is required"
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Column {
                        FormLabel("Mobile Number")
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(tokens.fieldHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                                    .background(whiteBg)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+91", fontSize = tokens.bodySmall, color = TextPrimary)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextField(
                                    value = mobileNumber,
                                    onValueChange = { mobileNumber = it.filter { c -> c.isDigit() } },
                                    placeholder = "9876543210"
                                )
                            }
                        }
                    }
                }
            }

            // ── Section 2: Organization & Assignment ──
            item {
                SectionCard(title = "2. Organization & Assignment") {
                    // Department Dropdown (Populated from DepartmentViewModel)
                    Column {
                        FormLabel("Department")
                        Spacer(Modifier.height(4.dp))
                        FormDropdown(
                            value = selectedDepartmentName.ifBlank { "Select Department..." },
                            expanded = departmentExpanded,
                            onExpandChange = { departmentExpanded = it },
                            options = if (departments.isNotEmpty()) departments.map { it.name } else listOf("No Departments"),
                            onOptionSelected = { name ->
                                selectedDepartmentName = name
                                selectedDepartmentId = departments.find { it.name == name }?._id.orEmpty()
                                departmentExpanded = false
                            }
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Designation Dropdown (Populated from DesignationViewModel)
                    Column {
                        FormLabel("Designation")
                        Spacer(Modifier.height(4.dp))
                        FormDropdown(
                            value = selectedDesignationName.ifBlank { "Select Designation..." },
                            expanded = designationExpanded,
                            onExpandChange = { designationExpanded = it },
                            options = if (designations.isNotEmpty()) designations.map { it.name } else listOf("No Designations"),
                            onOptionSelected = { name ->
                                selectedDesignationName = name
                                selectedDesignationId = designations.find { it.name == name }?.id.orEmpty()
                                designationExpanded = false
                            }
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Branch Dropdown (Populated from BranchViewModel)
                    if (roleScope == "regional") {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FormLabel("Allowed Branches (Multi-Select)", isRequired = true)
                                Text(
                                    text = "${allowedBranches.size} of ${branches.size} Selected",
                                    fontSize = 11.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(6.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Primary_background,
                                border = BorderStroke(1.dp, sectionBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Select access branches:", fontSize = 11.sp, color = mutedText)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                "Select All",
                                                fontSize = 11.sp,
                                                color = Primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { allowedBranches = branches.map { it.id } }
                                            )
                                            Text("•", fontSize = 11.sp, color = mutedText)
                                            Text(
                                                "Clear",
                                                fontSize = 11.sp,
                                                color = mutedText,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { allowedBranches = emptyList() }
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = sectionBorder)

                                    branches.forEach { b ->
                                        val isChecked = allowedBranches.contains(b.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isChecked) whiteBg else Color.Transparent)
                                                .clickable {
                                                    allowedBranches = if (isChecked) allowedBranches - b.id else allowedBranches + b.id
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AppCheckbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    allowedBranches = if (checked) allowedBranches + b.id else allowedBranches - b.id
                                                }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(b.name ?: "Branch", fontSize = tokens.bodySmall, color = title_color)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column {
                            FormLabel("Primary Branch", isRequired = roleScope != "org-wide")
                            Spacer(Modifier.height(4.dp))
                            FormDropdown(
                                value = selectedBranchName.ifBlank { "Select Branch..." },
                                expanded = branchExpanded,
                                onExpandChange = { branchExpanded = it },
                                options = branches.map { it.name ?: it.branchId ?: "" },
                                onOptionSelected = { name ->
                                    selectedBranchName = name
                                    selectedBranchId = branches.find { (it.name ?: it.branchId) == name }?.id.orEmpty()
                                    branchExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Reporting Manager Dropdown (Populated from HrViewModel.fetchMembers())
                    Column {
                        FormLabel("Reporting Manager")
                        Spacer(Modifier.height(4.dp))
                        val managerOptions = remember(membersList) {
                            listOf("None") + membersList.map { "${it.firstName} ${it.lastName}".trim() }
                        }

                        FormDropdown(
                            value = selectedReportingToName.ifBlank { "Search names..." },
                            expanded = managerExpanded,
                            onExpandChange = { managerExpanded = it },
                            options = managerOptions,
                            onOptionSelected = { name ->
                                selectedReportingToName = name
                                selectedReportingToId = if (name == "None") {
                                    ""
                                } else {
                                    membersList.find { "${it.firstName} ${it.lastName}".trim() == name }?._id.orEmpty()
                                }
                                managerExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Section 3: Role & Access ──
            item {
                SectionCard(
                    title = "3. Role & Access",
                    subtitle = "Define system privileges. Granular permissions are inherited from the assigned role."
                ) {
                    Column {
                        FormLabel("Primary Role", isRequired = true)
                        Spacer(Modifier.height(4.dp))
                        FormDropdown(
                            value = selectedRoleName.ifBlank { "Select role..." },
                            expanded = roleExpanded,
                            onExpandChange = { roleExpanded = it },
                            options = rolesList.map { it.name },
                            onOptionSelected = { name ->
                                val r = rolesList.find { it.name == name }
                                if (r != null) {
                                    selectedRoleId = r._id
                                    selectedRoleName = r.name
                                }
                                roleError = false
                                roleExpanded = false
                            },
                            isError = roleError,
                            errorMessage = if (roleError) "Primary role is required" else null
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Access Scope Indicator
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Primary_background,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(whiteBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Hub, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Access Scope", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = mutedText)
                                Text(
                                    SCOPE_LABELS[roleScope] ?: "Select a role",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 1.5f))

                    Text("Permission Preview Matrix", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = title_color)
                    Spacer(Modifier.height(8.dp))

                    // Condensed Matrix Preview
                    val activeCatalogModules = remember(rolePermissionsFlat) {
                        SCREENSHOT_PERMISSIONS_CATALOG.filter { module ->
                            module.submodules.any { sm ->
                                sm.allowedActions.any { act ->
                                    rolePermissionsFlat["${module.key}.${sm.key}.$act"] == true
                                }
                            }
                        }
                    }

                    if (selectedRoleId.isBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Select a role to preview its permissions.", fontSize = 11.sp, color = mutedText)
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp)),
                            color = whiteBg
                        ) {
                            Box(modifier = Modifier.horizontalScroll(previewScrollState)) {
                                Column(modifier = Modifier.width(620.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Primary_background)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Module", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(180.dp))
                                        Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(70.dp))
                                        Text("Create", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(70.dp))
                                        Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(70.dp))
                                        Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(70.dp))
                                        Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(70.dp))
                                    }

                                    HorizontalDivider(color = sectionBorder)

                                    activeCatalogModules.forEach { mod ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(mod.label, fontSize = 11.sp, color = title_color, modifier = Modifier.width(180.dp))
                                            MatrixPreviewCell(mod.submodules.any { rolePermissionsFlat["${mod.key}.${it.key}.view"] == true }, Modifier.width(70.dp))
                                            MatrixPreviewCell(mod.submodules.any { rolePermissionsFlat["${mod.key}.${it.key}.create"] == true }, Modifier.width(70.dp))
                                            MatrixPreviewCell(mod.submodules.any { rolePermissionsFlat["${mod.key}.${it.key}.edit"] == true }, Modifier.width(70.dp))
                                            MatrixPreviewCell(mod.submodules.any { rolePermissionsFlat["${mod.key}.${it.key}.delete"] == true }, Modifier.width(70.dp))
                                            MatrixPreviewCell(mod.submodules.any { rolePermissionsFlat["${mod.key}.${it.key}.export"] == true }, Modifier.width(70.dp))
                                        }
                                        HorizontalDivider(color = sectionBorder.copy(alpha = 0.35f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 4: Account & Security ──
            item {
                SectionCard(title = "4. Account & Security") {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormLabel("Temporary Password", isRequired = true)
                            Text(
                                text = "Generate New",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                                modifier = Modifier.clickable { password = generateTempPassword() }
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        FormPasswordField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Enter temporary password"
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Must be changed on first login. Must meet enterprise complexity rules.",
                            fontSize = 10.sp,
                            color = mutedText
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Require 2FA Authentication", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                            Text("Enforce multi-factor authentication upon first login.", fontSize = tokens.caption, color = mutedText)
                        }
                        MiniSwitch(checked = require2FA, onCheckedChange = { require2FA = it })
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Column {
                        FormLabel("Account Status")
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 2)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { accountStatus = "active" }) {
                                RadioButton(selected = accountStatus == "active", onClick = { accountStatus = "active" }, colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                Spacer(Modifier.width(4.dp))
                                Text("Active", fontSize = tokens.bodySmall, color = title_color)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { accountStatus = "inactive" }) {
                                RadioButton(selected = accountStatus == "inactive", onClick = { accountStatus = "inactive" }, colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                Spacer(Modifier.width(4.dp))
                                Text("Inactive", fontSize = tokens.bodySmall, color = title_color)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sendSetupEmail = !sendSetupEmail }
                    ) {
                        AppCheckbox(checked = sendSetupEmail, onCheckedChange = { sendSetupEmail = it })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Send account setup instructions to the user's email address immediately.",
                            fontSize = 11.5.sp,
                            color = title_color
                        )
                    }
                }
            }

            // ── Section: Draft Summary ──
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("DRAFT SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = mutedText)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Primary_background),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = if (firstName.isNotBlank() || lastName.isNotBlank()) "$firstName $lastName".trim() else "New User",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Text(emailAddress.ifBlank { "—" }, fontSize = 11.sp, color = mutedText)
                            }
                        }

                        HorizontalDivider(color = sectionBorder)

                        SummaryFieldRow("Role", selectedRoleName.ifBlank { "—" })
                        SummaryFieldRow(
                            "Branch",
                            if (roleScope == "regional") "${allowedBranches.size} branch(es) selected" else selectedBranchName.ifBlank { "—" }
                        )
                        SummaryFieldRow("Extra permissions", if (customPermissions.isNotEmpty()) "${customPermissions.size} module(s)" else "None")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status", fontSize = 12.sp, color = mutedText)
                            StatusBadge(
                                text = accountStatus.replaceFirstChar { it.uppercase() },
                                bgColor = if (accountStatus == "active") greenBg else redBg,
                                textColor = if (accountStatus == "active") greentext else redText
                            )
                        }
                    }
                }
            }

            // ── Section 5: Additional Permissions Matrix ──
            item {
                SectionCard(
                    title = "5. Additional Permissions",
                    subtitle = "Optional. Give this user extra access, or revoke some, on top of the role. Only differences are saved."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BadgeIndicator("Extra ($extraCount)", greentext)
                            BadgeIndicator("Revoked ($revokedCount)", redText)
                        }

                        if (customPermissions.isNotEmpty()) {
                            Text(
                                text = "Reset to role defaults",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                                modifier = Modifier.clickable { customPermissions = emptyMap() }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    if (selectedRoleId.isBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Select a role first.", fontSize = 11.sp, color = mutedText)
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp)),
                            color = whiteBg
                        ) {
                            Box(modifier = Modifier.horizontalScroll(overrideScrollState)) {
                                Column(modifier = Modifier.width(960.dp)) {
                                    // Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Primary_background.copy(alpha = 0.5f))
                                            .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("MODULE / SUBMODULE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(360.dp))
                                        MATRIX_COLUMNS.forEach { (_, title) ->
                                            Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                                                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = sectionBorder)

                                    SCREENSHOT_PERMISSIONS_CATALOG.forEach { module ->
                                        val isExpanded = expandedModules.contains(module.key)

                                        // Module Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedModules = if (isExpanded) expandedModules - module.key else expandedModules + module.key
                                                }
                                                .padding(horizontal = tokens.screenPadding, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(360.dp)) {
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(module.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = title_color)
                                            }

                                            // Bulk module action controls
                                            MATRIX_COLUMNS.forEach { (actionKey, _) ->
                                                val validSubmodules = module.submodules.filter { it.allowedActions.contains(actionKey) }
                                                Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                                                    if (validSubmodules.isNotEmpty()) {
                                                        val allOn = validSubmodules.all { sm -> isActionEffective(module.key, sm.key, actionKey) }
                                                        AppCheckbox(
                                                            checked = allOn,
                                                            onCheckedChange = { checked ->
                                                                validSubmodules.forEach { sm ->
                                                                    setActionOverride(module.key, sm.key, actionKey, checked)
                                                                }
                                                            },
                                                            size = 14.dp
                                                        )
                                                    } else {
                                                        Text("—", fontSize = 12.sp, color = sectionBorder)
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = sectionBorder.copy(alpha = 0.35f))

                                        // Submodules
                                        if (isExpanded) {
                                            module.submodules.forEach { sm ->
                                                val submoduleKey = "${module.key}.${sm.key}"
                                                val isHidden = hiddenSubmodules.contains(submoduleKey)

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(if (isHidden) light_grey.copy(alpha = 0.2f) else whiteBg)
                                                        .padding(horizontal = tokens.screenPadding, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(360.dp).padding(start = 24.dp)) {
                                                        Icon(
                                                            painter = painterResource(id = if (isHidden) R.drawable.ic_eye_close else R.drawable.ic_eye_open),
                                                            contentDescription = null,
                                                            tint = if (isHidden) redText else TextSecondary,
                                                            modifier = Modifier.size(14.dp).clickable {
                                                                hiddenSubmodules = if (hiddenSubmodules.contains(submoduleKey)) hiddenSubmodules - submoduleKey else hiddenSubmodules + submoduleKey
                                                            }
                                                        )
                                                        Spacer(Modifier.width(10.dp))
                                                        Text(sm.label, fontSize = 11.5.sp, color = if (isHidden) mutedText else title_color)
                                                    }

                                                    MATRIX_COLUMNS.forEach { (actionKey, _) ->
                                                        Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                                                            if (sm.allowedActions.contains(actionKey)) {
                                                                val isChecked = isActionEffective(module.key, sm.key, actionKey)
                                                                val isBase = rolePermissions[module.key]?.get(sm.key)?.get(actionKey) == true
                                                                val isExtra = isChecked && !isBase
                                                                val isRevoked = !isChecked && isBase

                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(
                                                                            when {
                                                                                isExtra -> Color(0xFFDCFCE7)
                                                                                isRevoked -> Color(0xFFFEE2E2)
                                                                                else -> Color.Transparent
                                                                            }
                                                                        )
                                                                        .padding(2.dp)
                                                                ) {
                                                                    AppCheckbox(
                                                                        checked = isChecked,
                                                                        onCheckedChange = { checked ->
                                                                            setActionOverride(module.key, sm.key, actionKey, checked)
                                                                        },
                                                                        size = 14.dp
                                                                    )
                                                                }
                                                            } else {
                                                                Text("—", fontSize = 12.sp, color = sectionBorder)
                                                            }
                                                        }
                                                    }
                                                }
                                                HorizontalDivider(color = sectionBorder.copy(alpha = 0.25f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(tokens.screenPadding * 2)) }
        }
    }
}

// ── Shared Helpers ──

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding * 1.4f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
            if (subtitle != null) {
                Text(subtitle, fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun SummaryFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = mutedText)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = title_color)
    }
}

@Composable
private fun BadgeIndicator(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MatrixPreviewCell(isAllowed: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isAllowed) {
            Icon(Icons.Default.Check, contentDescription = "Allowed", tint = greentext, modifier = Modifier.size(16.dp))
        } else {
            Text("—", fontSize = 12.sp, color = sectionBorder)
        }
    }
}

private fun generateTempPassword(): String {
    val chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
    val rnd = SecureRandom()
    return (1..10).map { chars[rnd.nextInt(chars.length)] }.joinToString("")
}