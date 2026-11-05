@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)
package com.cuso.tailor.view.home.role

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun RoleSettingsScreen(
    navController: NavController,
    hrViewModel: HrViewModel = hiltViewModel(),
    onAddRoleClick: () -> Unit = {},
    onEditRoleClick: (String) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Observe API states from HrViewModel
    val roles by hrViewModel.roles.collectAsStateWithLifecycle()
    val isLoading by hrViewModel.isLoadingRoles.collectAsStateWithLifecycle()
    val rolesError by hrViewModel.rolesError.collectAsStateWithLifecycle()

    // Trigger API call when screen opens
    LaunchedEffect(Unit) {
        hrViewModel.fetchRoles()
    }

    var searchQuery by remember { mutableStateOf("") }

    // Filter roles based on search query
    val filteredRoles = remember(roles, searchQuery) {
        roles.filter { r ->
            searchQuery.isBlank() ||
                    r.name.contains(searchQuery, ignoreCase = true) ||
                    r.description?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                    TitleBar(
                        title = "Role Management",
                        onClose = onBack
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent
        ) { paddingValues ->

            FabScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                fab = FabConfig(
                    label = "Add Role",
                    icon = Icons.Default.Add,
                    onClick = onAddRoleClick, // Navigates to full CreateRoleScreen
                    endPadding = tokens.screenPadding,
                    bottomPadding = tokens.screenPadding,
                    draggable = true
                ),
                fabVisible = true
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Role...",
                        onFilterClick = { }
                    )
                    HorizontalDivider(color = title_border)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when {
                            isLoading && roles.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Primary)
                                }
                            }

                            !rolesError.isNullOrBlank() && roles.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = rolesError ?: "Error loading roles",
                                            color = redText,
                                            fontSize = tokens.bodyMedium
                                        )
                                        Spacer(Modifier.height(tokens.extraPadding))
                                        Button(
                                            onClick = { hrViewModel.fetchRoles() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        ) {
                                            Text("Retry", fontSize = tokens.bodySmall, color = whiteBg)
                                        }
                                    }
                                }
                            }

                            filteredRoles.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = mutedText,
                                            modifier = Modifier.size(tokens.imageDp)
                                        )
                                        Spacer(Modifier.height(tokens.extraPadding))
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "No matching roles found" else "No roles found",
                                            color = TextSecondary,
                                            fontSize = tokens.bodyMedium
                                        )
                                    }
                                }
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(filteredRoles, key = { it._id }) { role ->
                                        val (badgeText, badgeTextColor, badgeBgColor) = if (role.status) {
                                            Triple("Active", greentext, greenBg)
                                        } else {
                                            Triple("Inactive", mutedText, light_grey)
                                        }

                                        val createdByName = role.createdBy?.let {
                                            "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim()
                                        }.takeIf { !it.isNullOrBlank() } ?: "System"

                                        DataCard(
                                            item = role,
                                            title = role.name,
                                            titleColor = title_color,
                                            subtitle = role.description,
                                            topBadgeText = badgeText,
                                            topBadgeTextColor = badgeTextColor,
                                            topBadgeBgColor = badgeBgColor,
                                            topBadgeInline = true,
                                            footerFields = listOf(
                                                DataCardField(
                                                    text = if (role.isSystemRole) "System Role" else "Custom Role",
                                                    textColor = title_color,
                                                    label = "Role Type",
                                                    asRow = true,
                                                    labelColor = mutedText
                                                ),
                                                DataCardField(
                                                    text = createdByName,
                                                    textColor = title_color,
                                                    label = "Created By",
                                                    asRow = true,
                                                    labelColor = mutedText
                                                ),
                                                DataCardField(
                                                    text = if (role.isDefault) "Yes" else "No",
                                                    textColor = title_color,
                                                    label = "Default",
                                                    asRow = true,
                                                    labelColor = mutedText
                                                )
                                            ),
                                            actions = listOf(
                                                MenuAction(
                                                    label = "Edit",
                                                    icon = Icons.Default.Edit,
                                                    onClick = {
                                                        // Navigates to full CreateRoleScreen with role ID
                                                        onEditRoleClick(role._id)
                                                    }
                                                )
                                            )
                                        )
                                    }
                                    item { Spacer(Modifier.height(tokens.extraPadding)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}