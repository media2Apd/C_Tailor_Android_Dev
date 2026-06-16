package com.cuso.mobile.view.others

import android.view.RoundedCorner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.view.home.SettingsScreen
import com.cuso.mobile.viewmodel.HomeViewModel

@Composable
fun homeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    var currentScreen by remember { mutableStateOf("home") }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 50.dp)
    ) {
        TopNavBar(
            navController = navController,
            isSettingsOpen = currentScreen == "settings",
            onSettingsClick = {
                currentScreen = if (currentScreen == "settings") "home" else "settings"
            },
            onMenuItemClick = { screen ->  // ✅ handle menu navigation
                currentScreen = screen
            }
        )

        when (currentScreen) {
            "settings" -> SettingsScreen(navController)
            "home" -> {
                Column(
                    Modifier.fillMaxSize()
                        .background(lightGray)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(lightGray),
                            contentPadding=PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    )
                    {
                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(200.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )
                                {
                                    Box(
                                        Modifier.padding(10.dp)
                                            .width(50.dp)
                                            .height(50.dp)
                                            .background(Color.Green,shape=RoundedCornerShape(4.dp))
                                    ){
                                        Icon(
                                            Icons.Filled.Money,null, tint = Color(0xFF006400)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(200.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(200.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(200.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )

                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )

                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )

                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )

                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )

                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(2.dp))

                            }
                        }
                    }
                }
            }
            "sales" -> { /* sales content */ }
            "marketing" -> { /* marketing content */ }
            else -> { /* default */ }
        }
    }
}

@Composable
fun TopNavBar(
    navController: NavController,
    isSettingsOpen: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {}  // ✅ new param
) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMenu by remember { mutableStateOf("Home") }

    val menuItems = listOf(
        Pair(Icons.Default.Home, "Home"),
        Pair(Icons.Default.TrendingUp, "Sales"),
        Pair(Icons.Default.Campaign, "Marketing"),
        Pair(Icons.Default.AccountBalance, "Finance"),
        Pair(Icons.Default.Inventory, "Inventory"),
        Pair(Icons.Default.LocalShipping, "Logistics"),
        Pair(Icons.Default.MiscellaneousServices, "Services"),
        Pair(Icons.Default.People, "HR"),
        Pair(Icons.Filled.Monitor, "IT"),
        Pair(Icons.Default.Gavel, "Legal"),
        Pair(Icons.Default.Security, "Security"),
        Pair(Icons.Filled.BarChart, "Reports"),
    )

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()) {

        // ── Drawer Overlay ──
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { isDrawerOpen = false }
                    .zIndex(1f)
            )
        }

        // ── Side Drawer ──
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.zIndex(2f)
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(0.dp))
            ) {
                // ── Logo ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cuso_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(55.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "CUSO",
                            fontSize = 14.sp,
                            color = Color(0xFF3B3BF9),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Tailor",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(8.dp))

                // ── Menu Items ──
                menuItems.forEach { (icon, label) ->
                    val isSelected = selectedMenu == label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF3B3BF9) else Color.Transparent)
                            .clickable {
                                selectedMenu = label
                                isDrawerOpen = false
                                onMenuItemClick(label.lowercase())  // ✅ notify parent
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── User Profile ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B3BF9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "AD",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(
                            "Admin",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            "admin@cuso.com",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // ── Top Navbar ──
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 30.dp, end = 30.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Burger menu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(lightGray, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { isDrawerOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier.size(22.dp),
                        tint = Color.Black
                    )
                }

                // Search bar
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search anything",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }

                // Add button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(lightGray, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                }

                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(30.dp),
                        tint = Color.DarkGray
                    )
                }
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        modifier = Modifier.size(30.dp),
                        tint = Color.DarkGray
                    )
                }

                // ✅ Settings / Close toggle
                IconButton(
                    onClick = { onSettingsClick() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSettingsOpen) Icons.Default.Close
                        else Icons.Filled.Settings,
                        contentDescription = if (isSettingsOpen) "Close" else "Settings",
                        modifier = Modifier.size(30.dp),
                        tint = if (isSettingsOpen) Color.Red else Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF2F2F2))
        }
    }
}

sealed class DrawerItem {
    data class Logo(val imageRes: Int) : DrawerItem()
    data class Menu(val icon: ImageVector, val label: String) : DrawerItem()
}