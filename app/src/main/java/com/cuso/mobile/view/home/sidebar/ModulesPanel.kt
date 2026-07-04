//package com.cuso.mobile.view.home.sidebar
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.gestures.detectVerticalDragGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountBalance
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Inventory2
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.filled.ShoppingBag
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.lerp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.zIndex
//import kotlinx.coroutines.launch
//
//// ── Module descriptions (shown under each module title) ──
//private val moduleDescriptions = mapOf(
//    "Sales" to "Manage leads, customers, orders & quotes",
//    "Marketing" to "Campaigns, promotions & customer outreach",
//    "Finance" to "Invoices, payments, expenses & accounting",
//    "Inventory" to "Manage stock, products & warehouses",
//    "Logistics" to "Shipments, delivery & transportation",
//    "Services" to "Service requests, jobs & maintenance",
//    "HR" to "Employees, attendance & payroll",
//    "IT" to "IT assets, support & system management",
//    "Legal" to "Contracts, documents & compliance",
//    "Security" to "Access control & activity monitoring",
//    "Reports" to "Sales & finance reporting"
//)
//
//private data class FrequentModule(
//    val label: String,
//    val icon: ImageVector,
//    val bg: Color,
//    val tint: Color
//)
//
//private val frequentlyUsed = listOf(
//    FrequentModule("Sales", Icons.Default.ShoppingBag, Color(0xFF6C4FF6), Color.White),
//    FrequentModule("Inventory", Icons.Default.Inventory2, Color(0xFF10B981), Color.White),
//    FrequentModule("Finance", Icons.Default.AccountBalance, Color(0xFFF59E0B), Color.White)
//)
//
//private const val HALF_FRACTION = 0.55f
//private const val FULL_FRACTION = 0.96f
//
//@Composable
//fun ModulesPanel(
//    isOpen: Boolean,
//    onClose: () -> Unit,
//    onModuleCategoryClick: (menu: String, category: String) -> Unit
//) {
//    val density = LocalDensity.current
//    val heightFraction = remember { Animatable(HALF_FRACTION) }
//    val scope = rememberCoroutineScope()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var expandedModule by remember { mutableStateOf<String?>("Sales") }
//
//    LaunchedEffect(isOpen) {
//        if (isOpen) heightFraction.snapTo(HALF_FRACTION)
//    }
//
//    val allModules = remember {
//        SidebarConfig.getFullMenuItems().filter { it.label != "Home" }
//    }
//
//    val filteredModules = if (searchQuery.isBlank()) {
//        allModules
//    } else {
//        allModules.filter { it.label.contains(searchQuery, ignoreCase = true) }
//    }
//
//    val cornerRadius: Dp = lerp(
//        24.dp, 0.dp,
//        ((heightFraction.value - HALF_FRACTION) / (FULL_FRACTION - HALF_FRACTION)).coerceIn(0f, 1f)
//    )
//
//    AnimatedVisibility(
//        visible = isOpen,
//        enter = androidx.compose.animation.fadeIn(),
//        exit = androidx.compose.animation.fadeOut(),
//        modifier = Modifier.zIndex(10f)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.35f))
//                .clickable { onClose() }
//        )
//    }
//
//    AnimatedVisibility(
//        visible = isOpen,
//        enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
//        exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
//        modifier = Modifier
//            .fillMaxSize()
//            .zIndex(11f)
//    ) {
//        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
//            val maxHeightPx = with(density) { maxHeight.toPx() }
//            val panelHeight = maxHeight * heightFraction.value
//
//            Surface(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//                    .height(panelHeight),
//                color = Color.White,
//                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
//                shadowElevation = 16.dp
//            ) {
//                Column(modifier = Modifier.fillMaxSize()) {
//
//                    // ── Drag handle ──
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                            .pointerInput(Unit) {
//                                detectVerticalDragGestures(
//                                    onDragEnd = {
//                                        scope.launch {
//                                            val target = if (heightFraction.value > (HALF_FRACTION + FULL_FRACTION) / 2)
//                                                FULL_FRACTION else HALF_FRACTION
//                                            heightFraction.animateTo(target, tween(250))
//                                        }
//                                    },
//                                    onVerticalDrag = { change, dragAmount ->
//                                        change.consume()
//                                        scope.launch {
//                                            val deltaFraction = -dragAmount / maxHeightPx
//                                            val newValue = (heightFraction.value + deltaFraction)
//                                                .coerceIn(HALF_FRACTION - 0.05f, FULL_FRACTION)
//                                            heightFraction.snapTo(newValue)
//                                        }
//                                    }
//                                )
//                            },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .width(40.dp)
//                                .height(4.dp)
//                                .clip(RoundedCornerShape(50))
//                                .background(Color(0xFFD1D5DB))
//                        )
//                    }
//
//                    // ── Header ──
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 20.dp, vertical = 4.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text("Modules", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Close",
//                            tint = Color(0xFF111827),
//                            modifier = Modifier
//                                .size(22.dp)
//                                .clickable { onClose() }
//                        )
//                    }
//
//                    Spacer(Modifier.height(12.dp))
//
//                    // ── Search ──
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 20.dp)
//                            .height(46.dp)
//                            .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
//                            .padding(horizontal = 14.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
//                        Spacer(Modifier.width(8.dp))
//                        BasicTextField(
//                            value = searchQuery,
//                            onValueChange = { searchQuery = it },
//                            modifier = Modifier.fillMaxWidth(),
//                            singleLine = true,
//                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
//                            decorationBox = { inner ->
//                                if (searchQuery.isEmpty()) {
//                                    Text("Search modules...", fontSize = 14.sp, color = Color(0xFF9CA3AF))
//                                }
//                                inner()
//                            }
//                        )
//                    }
//
//                    Spacer(Modifier.height(20.dp))
//
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
//                    ) {
//                        if (searchQuery.isBlank()) {
//                            item {
//                                Text(
//                                    "FREQUENTLY USED",
//                                    fontSize = 11.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = Color(0xFF9CA3AF)
//                                )
//                                Spacer(Modifier.height(10.dp))
//                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                                    frequentlyUsed.forEach { fm ->
//                                        Column(
//                                            modifier = Modifier
//                                                .weight(1f)
//                                                .background(Color.White, RoundedCornerShape(14.dp))
//                                                .border1(Color(0xFFF0F0F0))
//                                                .clickable {
//                                                    val menu = allModules.find { it.label == fm.label }
//                                                    val firstCat = menu?.categories?.firstOrNull()
//                                                    if (menu != null && firstCat != null) {
//                                                        onModuleCategoryClick(menu.label, firstCat)
//                                                    }
//                                                }
//                                                .padding(vertical = 16.dp),
//                                            horizontalAlignment = Alignment.CenterHorizontally
//                                        ) {
//                                            Box(
//                                                modifier = Modifier
//                                                    .size(44.dp)
//                                                    .clip(RoundedCornerShape(12.dp))
//                                                    .background(fm.bg),
//                                                contentAlignment = Alignment.Center
//                                            ) {
//                                                Icon(fm.icon, contentDescription = fm.label, tint = fm.tint, modifier = Modifier.size(22.dp))
//                                            }
//                                            Spacer(Modifier.height(8.dp))
//                                            Text(fm.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
//                                        }
//                                    }
//                                }
//                                Spacer(Modifier.height(24.dp))
//                            }
//                        }
//
//                        item {
//                            Text(
//                                "ALL MODULES",
//                                fontSize = 11.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF9CA3AF)
//                            )
//                            Spacer(Modifier.height(10.dp))
//                        }
//
//                        items(filteredModules) { module ->
//                            val isExpanded = expandedModule == module.label
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(bottom = 10.dp)
//                                    .background(Color.White, RoundedCornerShape(14.dp))
//                                    .border1(Color(0xFFF0F0F0))
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .clickable {
//                                            expandedModule = if (isExpanded) null else module.label
//                                        }
//                                        .padding(horizontal = 16.dp, vertical = 14.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Icon(
//                                        painter = painterResource(id = module.icon),
//                                        contentDescription = module.label,
//                                        tint = Color(0xFF6B7280),
//                                        modifier = Modifier.size(20.dp)
//                                    )
//                                    Spacer(Modifier.width(12.dp))
//                                    Column(modifier = Modifier.weight(1f)) {
//                                        Text(module.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
//                                        moduleDescriptions[module.label]?.let {
//                                            Text(it, fontSize = 12.sp, color = Color(0xFF9CA3AF))
//                                        }
//                                    }
//                                    Icon(
//                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                                        contentDescription = null,
//                                        tint = Color(0xFF9CA3AF)
//                                    )
//                                }
//
//                                if (isExpanded && module.categories.isNotEmpty()) {
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(start = 48.dp, end = 16.dp, bottom = 12.dp)
//                                    ) {
//                                        module.categories.forEach { category ->
//                                            Text(
//                                                "•  $category",
//                                                fontSize = 13.sp,
//                                                color = Color(0xFF4B5563),
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .clickable {
//                                                        onModuleCategoryClick(module.label, category)
//                                                    }
//                                                    .padding(vertical = 6.dp)
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        item { Spacer(Modifier.height(100.dp)) }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// small helper for a subtle 1dp rounded border
//private fun Modifier.border1(color: Color): Modifier =
//    this.border(1.dp, color, RoundedCornerShape(14.dp))