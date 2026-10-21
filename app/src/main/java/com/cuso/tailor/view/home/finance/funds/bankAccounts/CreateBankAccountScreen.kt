package com.cuso.tailor.view.home.finance.funds.bankAccounts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CreateBankAccountRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.FinanceViewModel

@Composable
fun CreateBankAccountScreen(
    viewModel: FinanceViewModel,
    branchViewModel: BranchViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
    }

    val branchUiState by branchViewModel.uiState.collectAsState()
    val branchesList = remember(branchUiState) {
        (branchUiState as? BranchUiState.Success)?.branches.orEmpty()
    }
    val branchOptions = remember(branchesList) {
        val names = branchesList.mapNotNull { it.name }
        if (names.isEmpty()) listOf("Select Branch") else names
    }

    // Form States
    var accountDisplayName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Checking") }
    var isAccountTypeOpen by remember { mutableStateOf(false) }
    val accountTypeOptions = listOf("Checking", "Savings", "Money Market", "Credit Card")

    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var bankBranchName by remember { mutableStateOf("") }

    var selectedBranchName by remember { mutableStateOf("Select Branch") }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var isBranchDropdownOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Add Bank Account", onClose = onBack)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // =============================================================
            // Section 1: Basic Info
            // =============================================================
            SectionHeader(title = "Basic Info", icon = Icons.Outlined.Info)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column {
                        FormLabel(text = "Account Name", isRequired = true)
                        FormTextField(
                            value = accountDisplayName,
                            onValueChange = { accountDisplayName = it },
                            placeholder = "e.g. HDFC Operations - Mumbai",
                            containerColor = badgeGrey,
                            textColor = TitleColor
                        )
                    }

                    FormDropdown(
                        label = "Account Type",
                        isRequired = true,
                        value = accountType,
                        expanded = isAccountTypeOpen,
                        onExpandChange = { isAccountTypeOpen = it },
                        options = accountTypeOptions,
                        onOptionSelected = { accountType = it }
                    )
                }
            }

            // =============================================================
            // Section 2: Bank Details
            // =============================================================
            SectionHeader(title = "Bank Details", icon = Icons.Outlined.AccountBalance)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column {
                        FormLabel(text = "Banking Name", isRequired = true)
                        FormTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            placeholder = "e.g. HDFC Bank",
                            containerColor = badgeGrey,
                            textColor = TitleColor
                        )
                    }

                    Column {
                        FormLabel(text = "Account Number", isRequired = true)
                        FormTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it },
                            placeholder = "Enter account number",
                            keyboardType = KeyboardType.Number,
                            containerColor = badgeGrey,
                            textColor = TitleColor
                        )
                    }

                    Column {
                        FormLabel(text = "IFSC Code", isRequired = true)
                        FormTextField(
                            value = ifscCode,
                            onValueChange = { ifscCode = it.uppercase() },
                            placeholder = "e.g. HDFC0000123",
                            containerColor = badgeGrey,
                            textColor = TitleColor
                        )
                    }

                    Column {
                        FormLabel(text = "Bank Branch", isRequired = true)
                        FormTextField(
                            value = bankBranchName,
                            onValueChange = { bankBranchName = it },
                            placeholder = "e.g. Bandra East Branch, Mumbai",
                            containerColor = badgeGrey,
                            textColor = TitleColor
                        )
                    }
                }
            }

            // =============================================================
            // Section 3: Branch Jurisdiction
            // =============================================================
            SectionHeader(title = "Jurisdiction", icon = Icons.Outlined.Payments)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    FormDropdown(
                        label = "Branch Jurisdiction",
                        isRequired = false,
                        value = selectedBranchName,
                        expanded = isBranchDropdownOpen,
                        onExpandChange = { isBranchDropdownOpen = it },
                        options = branchOptions,
                        onOptionSelected = { chosenBranch ->
                            val chosenObj = branchesList.find { it.name == chosenBranch }
                            selectedBranchName = chosenBranch
                            selectedBranchId = chosenObj?.id
                        }
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            // Action Buttons
            FormActionButtons(
                cancelText = "Cancel",
                primaryText = "Save Account",
                onCancel = onBack,
                onPrimaryClick = {
                    if (accountDisplayName.isBlank()) {
                        Toast.makeText(context, "Please enter an account name", Toast.LENGTH_SHORT).show()
                        return@FormActionButtons
                    }
                    if (bankName.isBlank()) {
                        Toast.makeText(context, "Please enter bank name", Toast.LENGTH_SHORT).show()
                        return@FormActionButtons
                    }
                    if (accountNumber.isBlank()) {
                        Toast.makeText(context, "Please enter account number", Toast.LENGTH_SHORT).show()
                        return@FormActionButtons
                    }
                    if (ifscCode.isBlank()) {
                        Toast.makeText(context, "Please enter IFSC code", Toast.LENGTH_SHORT).show()
                        return@FormActionButtons
                    }

                    val request = CreateBankAccountRequest(
                        accountDisplayName = accountDisplayName.trim(),
                        accountNumber = accountNumber.trim(),
                        accountType = accountType,
                        bankBranchName = bankBranchName.trim(),
                        bankName = bankName.trim(),
                        branchId = selectedBranchId,
                        currency = "INR",
                        routingIdentifierType = "IFSC",
                        routingIdentifierValue = ifscCode.trim(),
                        status = "Active"
                    )

                    viewModel.createBankAccount(request) { successMsg ->
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                isLoading = isSubmitting,
                buttonHeight = tokens.buttonHeight,
                cornerRadius = tokens.cardCornerRadius * 0.6f,
                primaryColor = Primary
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(primary_light, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize * 0.85f)
            )
        }
        Text(
            text = title,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TitleColor
        )
    }
}