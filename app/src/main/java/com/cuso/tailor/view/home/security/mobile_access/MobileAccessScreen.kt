@file:Suppress("unused", "AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.security.mobile_access

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.MobileDeviceItem
import com.cuso.tailor.model.security.MobileSecurityAuditItem
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.security.mobile_access.components.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun MobileAccessScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var selectedTopTabIndex by remember { mutableIntStateOf(0) }

    val mainTabs = remember {
        listOf(
            TabItem(label = "Device management", icon = Icons.Default.Smartphone),
            TabItem(label = "Authentication", icon = Icons.Default.VpnKey),
            TabItem(label = "Data Security", icon = Icons.Default.Shield),
            TabItem(label = "Security Audit", icon = Icons.AutoMirrored.Filled.ListAlt)
        )
    }

    var selectedDeviceForPage by remember { mutableStateOf<MobileDeviceItem?>(null) }
    var selectedAuditForPage by remember { mutableStateOf<MobileSecurityAuditItem?>(null) }

    LaunchedEffect(selectedTopTabIndex) {
        when (selectedTopTabIndex) {
            0 -> viewModel.fetchMobileDevices()
            1 -> viewModel.fetchMobileAuthPolicy()
            2 -> viewModel.fetchMobileDataSecurityPolicy()
            3 -> viewModel.fetchMobileSecurityAudit()
        }
    }

    // ── FULL PAGE: Device Details ──
    if (selectedDeviceForPage != null) {
        DeviceDetailScreen(
            device = selectedDeviceForPage!!,
            viewModel = viewModel,
            onBack = { selectedDeviceForPage = null }
        )
        return
    }

    // ── FULL PAGE: Audit Details ──
    if (selectedAuditForPage != null) {
        AuditDetailScreen(
            audit = selectedAuditForPage!!,
            viewModel = viewModel,
            onBack = { selectedAuditForPage = null }
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0) ,
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Mobile Authentication",
                onClose = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // SettingsTabs is placed outside TopBar directly under TitleBar
            SettingsTabs(
                tabs = mainTabs,
                selectedIndex = selectedTopTabIndex,
                onTabSelected = { selectedTopTabIndex = it },
                modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTopTabIndex) {
                    0 -> DeviceManagementTabContent(
                        viewModel = viewModel,
                        onDeviceClick = { selectedDeviceForPage = it }
                    )

                    1 -> AuthenticationTabContent(viewModel = viewModel)
                    2 -> DataSecurityTabContent(viewModel = viewModel)
                    3 -> SecurityAuditTabContent(
                        viewModel = viewModel,
                        onAuditClick = { selectedAuditForPage = it }
                    )
                }
            }
        }
    }
}