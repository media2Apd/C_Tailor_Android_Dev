@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.legal.legal_contract

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalContractDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun LegalContractsScreen(
    onBack: () -> Unit,
    onCreateContract: () -> Unit = {},
    onContractClick: (String) -> Unit = {},
    onEditContract: (String) -> Unit = {},
    viewModel: LegalViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val contracts by viewModel.contracts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingContracts.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    // State for managing contract deletion dialog
    var contractToDelete by remember { mutableStateOf<LegalContractDto?>(null) }

    // Re-fetch trigger counter to force reload on navigation or deletion
    var refreshTrigger by remember { mutableIntStateOf(0) }
    // Re-fetch contracts whenever this screen is navigated to or when refreshTrigger increments
    LaunchedEffect(refreshTrigger) {
        viewModel.fetchContracts(search = searchQuery.ifBlank { null })
    }

    // Delete Confirmation Dialog
    contractToDelete?.let { contract ->
        DeleteModel(
            title = "Delete Contract",
            message = "You're going to delete the contract \"${contract.contractName ?: contract.contractNumber}\".\nAre you sure?",
            checkboxText = "I confirm this deletion",
            confirmText = "Yes, Delete!",
            cancelText = "No, Keep It.",
            onDismiss = { contractToDelete = null },
            onDelete = {
                val targetId = contract.id
                contractToDelete = null
                viewModel.deleteContract(
                    contractId = targetId,
                    onSuccess = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        refreshTrigger++ // Forces LaunchedEffect to immediately re-run fetchContracts()
                    },
                    onError = { err ->
                        Toast.makeText(context, err ?: "Failed to delete contract", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }



    // Reusable FabScaffold wrapping the entire screen with a draggable Create button
    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Create Contract",
            icon = Icons.Default.Add,
            onClick = onCreateContract
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "All Contracts",
                    onClose = onBack
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Reusable SearchFilterBar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.fetchContracts(search = it.ifBlank { null })
                    },
                    placeholder = "Search SO number, customer...",
                    onFilterClick = { /* Handle Filter drawer */ }
                )

                // List Section
                if (isLoading && contracts.isEmpty()) {
                    ListSkeleton()
                } else if (contracts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No legal contracts found",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(contracts, key = { it.id }) { contract ->
                            LegalContractDataCardItem(
                                contract = contract,
                                onContractClick = onContractClick,
                                onEditContract = onEditContract,
                                onDeleteContract = { contractToDelete = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Reusable DataCard Integration
// =============================================================================
@Composable
private fun LegalContractDataCardItem(
    contract: LegalContractDto,
    onContractClick: (String) -> Unit,
    onEditContract: (String) -> Unit,
    onDeleteContract: (LegalContractDto) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Format clean status and badge styling
    val cleanStatus = (contract.renewalStatus ?: "Pending").replace("_", " ")
    val (statusBg, statusFg) = when (cleanStatus.lowercase()) {
        "active", "approved" -> Pair(greenBg, greentext)
        "pending", "renewal notice" -> Pair(yellowBg, orangeText)
        else -> Pair(cardBgLight, TextSecondary)
    }

    // Expiry calculation
    val (expiryFormatted, remainingDays) = calculateExpiryAndRemainingDays(contract.endDate)

    // Reusable Action Dropdown Items
    val actions = remember(contract) {
        listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Outlined.Visibility,
                onClick = {
                    if (contract.id.isNotBlank()) {
                        onContractClick(contract.id)
                    }
                }
            ),
            MenuAction(
                label = "Edit",
                icon = Icons.Outlined.Edit,
                onClick = {
                    if (contract.id.isNotBlank()) {
                        onEditContract(contract.id) // <-- CALL onEditContract INSTEAD OF onContractClick
                    }
                }
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                tint = redText,
                textColor = redText,
                onClick = { onDeleteContract(contract) }
            )
        )
    }

    DataCard(
        item = contract,
        onClick = {
            if (contract.id.isNotBlank()) {
                onContractClick(contract.id)
            }
        },
        title = contract.contractName?.ifBlank { "Untitled Contract" } ?: "Untitled Contract",
        titleFontWeight = FontWeight.Medium,
        titleColor = TextPrimary,
        topBadgeText = cleanStatus,
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusFg,
        topBadgeShowDot = false,
        topBadgeInline = true,
        actions = actions,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Subtitle Row: Contract ID Label & Code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contract ID",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Text(
                        text = contract.contractNumber ?: "—",
                        fontSize = tokens.caption,
                        color = TextPrimary
                    )
                }

                // Inner Box: Type, Country/Branch, Risk
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
                    color = Primary_background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Type", fontSize = tokens.label, color = mutedText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = contract.contractType ?: "—",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        // Country / Branch
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Country/Branch", fontSize = tokens.label, color = mutedText)
                            Spacer(modifier = Modifier.height(2.dp))
                            val branchLocation = listOfNotNull(contract.countryName, contract.region)
                                .filter { it.isNotBlank() }
                                .joinToString("/")
                            Text(
                                text = branchLocation.ifBlank { "—" },
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        // Risk
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Risk", fontSize = tokens.label, color = mutedText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = contract.riskLevel ?: "Low",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Footer Row: Expiry Date and Days Remaining
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Expiry :",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Text(
                        text = "$expiryFormatted ",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                    if (remainingDays >= 0) {
                        Text(
                            text = "(${remainingDays}d remaining)",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = redText
                        )
                    }
                }
            }
        }
    )
}

// =============================================================================
// Date and Expiry Calculations
// =============================================================================
private fun calculateExpiryAndRemainingDays(endDateIso: String?): Pair<String, Long> {
    if (endDateIso.isNullOrBlank()) return Pair("—", -1L)
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(endDateIso) ?: return Pair(endDateIso.take(10), -1L)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val diffMillis = date.time - Date().time
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis).coerceAtLeast(0)
        Pair(formatter.format(date), days)
    } catch (_: Exception) {
        Pair(endDateIso.take(10), -1L)
    }
}