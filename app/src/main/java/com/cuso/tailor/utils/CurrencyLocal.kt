package com.cuso.tailor.utils

import androidx.compose.runtime.compositionLocalOf

// Holds the current currency symbol for the whole Compose tree
val LocalCurrencySymbol = compositionLocalOf { "" }