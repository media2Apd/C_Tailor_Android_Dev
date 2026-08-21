package com.cuso.mobile.utils

import androidx.compose.runtime.compositionLocalOf

// Provided at the root (MainActivity) using AppLoadingManager.busyState,
// read by reusable input composables (AppTextField, AppDropdown, AppButton)
// so they auto-disable app-wide whenever any API call is in flight.
val LocalIsAppBusy = compositionLocalOf { false }