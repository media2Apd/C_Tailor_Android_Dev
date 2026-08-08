package com.cuso.mobile.view.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens

/**
 * Standard App Logo used in headers or small sections.
 * Scales dynamically based on screen size.
 */
@Composable
fun AppLogo() {
    val tokens = LocalAppTokens.current

    // Adaptive size: 50dp for mobile, 70dp for tablets
    val logoSize = if (tokens.isTablet) 70.dp else 50.dp

    Column {
        Image(
            painter = painterResource(id = R.drawable.cuso_logo),
            contentDescription = "Standard App logo",
            modifier = Modifier.size(logoSize)
        )
    }
}

/**
 * Large Branding Logo used on the Login/Welcome screens.
 * Responsive sizing to maintain visual balance across devices.
 */
@Composable
fun CusoAppLogo() {
    val tokens = LocalAppTokens.current

    // Adaptive size: 100dp for mobile, 140dp for tablets
    val logoSize = if (tokens.isTablet) 140.dp else 100.dp

    Column {
        Image(
            painter = painterResource(id = R.drawable.cuso_tailor_logo),
            contentDescription = "Cuso Tailor Branding logo",
            modifier = Modifier.size(logoSize)
        )
    }
}