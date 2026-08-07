package com.cuso.mobile.view.others

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg

@Suppress("UNUSED_PARAMETER")
@Composable
fun PrivacyPolicy(navController: NavController){
    Box(
        modifier = Modifier
            .background(whiteBg)
            .fillMaxSize()
    ) {
        Column(
            //  Tell the column to expand so it can center its content on the screen
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Its PrivacyPolicy page", color = blackTitle)
        }
    }
}