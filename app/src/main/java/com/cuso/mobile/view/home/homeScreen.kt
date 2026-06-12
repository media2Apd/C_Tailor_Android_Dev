package com.cuso.mobile.view.others

import android.graphics.drawable.Icon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.zIndex
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.ui.layout.layout

@Composable
fun homeScreen(navController: NavController) {

    val viewModel: HomeViewModel = hiltViewModel()
    val user by viewModel.user.collectAsState()
    val org by viewModel.org.collectAsState()
    val tokens by viewModel.tokens.collectAsState()

    val isLoggedOut by viewModel.isLoggedOut.collectAsState()

//
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState())
//    ) {
//
//
//
//        // User Info
//        Text("👤 User Info", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
//        Spacer(Modifier.height(8.dp))
//        user?.let {
//            Text("Name: ${it.firstName} ${it.lastName}", color = Color.Black)
//            Text("Email: ${it.email}", color = Color.Black)
//            Text("Role: ${it.role}", color = Color.Black)
//            Text("Member ID: ${it.memberId}", color = Color.Black)
//        } ?: Text("User data இல்ல", color = Color.Red)
//
//        Spacer(Modifier.height(16.dp))
//
//        // Organization Info
//        Text("🏢 Organization Info", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
//        Spacer(Modifier.height(8.dp))
//        org?.let {
//            Text("Name: ${it.name}", color = Color.Black)
//            Text("Industry: ${it.industry}", color = Color.Black)
//            Text("Email: ${it.email}", color = Color.Black)
//            Text("Status: ${it.status}", color = Color.Black)
//            Text("Business Type: ${it.businessType}", color = Color.Black)
//        } ?: Text("Org data இல்ல", color = Color.Red)
//
//        Spacer(Modifier.height(16.dp))
//
//        // Token Info
//        Text("🔑 Tokens", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
//        Spacer(Modifier.height(8.dp))
//        tokens?.let {
//            Text("Access Token: ${it.accessToken.take(30)}...", color = Color.Black)
//            Text("Refresh Token: ${it.refreshToken.take(30)}...", color = Color.Black)
//        } ?: Text("Token data இல்ல", color = Color.Red)
//
//        // Navigate to login when logged out
//        LaunchedEffect(isLoggedOut) {
//            if (isLoggedOut) {
//                navController.navigate("login") {
//                    popUpTo(0) { inclusive = true }
//                }
//            }
//        }

        TopNavBar(navController)
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout", color = Color.White, fontSize = 16.sp)
        }
//    }
}

@Composable
fun TopNavBar(navController: NavController) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        // Overlay
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { isDrawerOpen = false }
                    .zIndex(1f)
            )
        }

        // Drawer
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.zIndex(2f)
        ) {
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .border(0.5.dp, Color.LightGray, RoundedCornerShape(0.dp))
            ) {
                // Drawer header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Menu", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    IconButton(onClick = { isDrawerOpen = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                HorizontalDivider()

                // Drawer items
                listOf(
                    Pair(Icons.Default.Home, "Home"),
                    Pair(Icons.Default.Dashboard, "Dashboard"),
                    Pair(Icons.Default.People, "Members"),
                    Pair(Icons.Default.CalendarMonth, "Calendar"),
                    Pair(Icons.Default.BarChart, "Reports"),
                ).forEach { (icon, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.Gray)
                        Text(label, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.weight(1f))
                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Gray)
                    Text("Logout", fontSize = 14.sp)
                }
            }
        }

        // Main content
        Column {
            // Top navbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Burger menu
                IconButton(onClick = { isDrawerOpen = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }

                // Search bar
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = Color.Gray, modifier = Modifier.size(18.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) Text("Search...",
                                color = Color.Gray, fontSize = 14.sp)
                            inner()
                        }
                    )
                    VerticalDivider(modifier = Modifier.height(18.dp).width(0.5.dp))
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add",
                            modifier = Modifier.size(18.dp))
                    }
                }

                // Right icons
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
            HorizontalDivider()
        }
    }
}