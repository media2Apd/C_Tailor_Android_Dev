package com.cuso.mobile.view.home

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
fun HomeBackHandler(
    screenStackSize: Int,
    currentScreen: String,
    isDrawerOpen: Boolean,
    showModulesPanel: Boolean,
    showQuickAccessPanel: Boolean,
    onCloseDrawer: () -> Unit,
    onCloseModulesPanel: () -> Unit,
    onCloseQuickAccessPanel: () -> Unit,
    onSetSalesSettingsMode: (Boolean) -> Unit,
    onClearStateForScreen: (String) -> Unit,
    onGoBack: () -> Unit
) {
    BackHandler(enabled = isDrawerOpen || showModulesPanel || showQuickAccessPanel || screenStackSize > 1) {
        when {
            // Overlays take priority: close them instead of navigating back
            showQuickAccessPanel -> onCloseQuickAccessPanel()
            showModulesPanel -> onCloseModulesPanel()
            isDrawerOpen -> onCloseDrawer()

            // Every other screen: sync sales-settings mode, clear screen state, then always go back
            else -> {
                when (currentScreen) {
                    "sales_garment_type" -> onSetSalesSettingsMode(true)
                    "sales_settings", "sales_lead", "sales_pricing_quotation" -> onSetSalesSettingsMode(false)
                }
                onClearStateForScreen(currentScreen)
                onGoBack() // single, guaranteed call for back button and back gesture on all pages
            }
        }
    }
}