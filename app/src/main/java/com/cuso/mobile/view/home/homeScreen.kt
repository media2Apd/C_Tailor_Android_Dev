package com.cuso.mobile.view.others

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.viewmodel.HomeViewModel

@Composable
fun homeScreen(navController: NavController) {

    val viewModel: HomeViewModel = hiltViewModel()
    val user by viewModel.user.collectAsState()
    val org by viewModel.org.collectAsState()
    val tokens by viewModel.tokens.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User Info
        Text("👤 User Info", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        user?.let {
            Text("Name: ${it.firstName} ${it.lastName}", color = Color.Black)
            Text("Email: ${it.email}", color = Color.Black)
            Text("Role: ${it.role}", color = Color.Black)
            Text("Member ID: ${it.memberId}", color = Color.Black)
        } ?: Text("User data இல்ல", color = Color.Red)

        Spacer(Modifier.height(16.dp))

        // Organization Info
        Text("🏢 Organization Info", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        org?.let {
            Text("Name: ${it.name}", color = Color.Black)
            Text("Industry: ${it.industry}", color = Color.Black)
            Text("Email: ${it.email}", color = Color.Black)
            Text("Status: ${it.status}", color = Color.Black)
            Text("Business Type: ${it.businessType}", color = Color.Black)
        } ?: Text("Org data இல்ல", color = Color.Red)

        Spacer(Modifier.height(16.dp))

        // Token Info
        Text("🔑 Tokens", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        tokens?.let {
            Text("Access Token: ${it.accessToken.take(30)}...", color = Color.Black)
            Text("Refresh Token: ${it.refreshToken.take(30)}...", color = Color.Black)
        } ?: Text("Token data இல்ல", color = Color.Red)
    }
}