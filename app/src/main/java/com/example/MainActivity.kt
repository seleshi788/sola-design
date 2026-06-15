package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.InventoryItem
import com.example.data.Message
import com.example.data.Order
import com.example.ui.AppLanguage
import com.example.ui.LocaleStrings
import com.example.ui.SolaTab
import com.example.ui.SolaViewModel
import java.text.SimpleDateFormat
import java.util.*

// CMYK & Print Design Color Theme
val SolaDarkBg = Color(0xFF101216)
val SolaDarkSurface = Color(0xFF181C24)
val SolaCardBorder = Color(0xFF282F3E)
val SolaCyan = Color(0xFF00E5FF)
val SolaMagenta = Color(0xFFFF2A6D)
val SolaYellow = Color(0xFFFFD200)
val SolaGreen = Color(0xFF20C997)
val SolaTextLight = Color(0xFFE2E8F0)
val SolaTextDim = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolaTheme {
                SolaAppContent()
            }
        }
    }
}

@Composable
fun SolaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = SolaYellow,
            secondary = SolaCyan,
            tertiary = SolaMagenta,
            background = SolaDarkBg,
            surface = SolaDarkSurface,
            onBackground = SolaTextLight,
            onSurface = SolaTextLight
        ),
        content = content
    )
}

@Composable
fun SolaAppContent(viewModel: SolaViewModel = viewModel()) {
    val language by viewModel.language.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SolaDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            SolaHeader(
                language = language,
                isAdminMode = isAdminMode,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onAdminToggle = { viewModel.isAdminMode.value = !viewModel.isAdminMode.value }
            )
        },
        bottomBar = {
            SolaBottomNav(
                language = language,
                activeTab = activeTab,
                onTabSelect = { viewModel.activeTab.value = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SolaDarkBg)
        ) {
            // Main content based on active tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
            ) {
                // If there's low stock and we are staff or looking at stock, show a beautiful automated header alert
                if (lowStockItems.isNotEmpty()) {
                    LowStockAlertBanner(language = language, lowStockItems = lowStockItems, onClick = {
                        viewModel.activeTab.value = SolaTab.INVENTORY
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (activeTab) {
                    SolaTab.TRACK -> TrackAndManageScreen(viewModel = viewModel, language = language, isAdminMode = isAdminMode)
                    SolaTab.NEW_ORDER -> NewOrderScreen(viewModel = viewModel, language = language)
                    SolaTab.INVENTORY -> InventoryScreen(viewModel = viewModel, language = language, isAdminMode = isAdminMode)
                    SolaTab.SOCIALS -> SocialsAndPromosScreen(language = language)
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun SolaHeader(
    language: AppLanguage,
    isAdminMode: Boolean,
    onLanguageToggle: () -> Unit,
    onAdminToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(0.dp)),
        colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Print Shop Identity Logo with CMYK gradient
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SolaCyan, SolaMagenta, SolaYellow)
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = LocaleStrings.get("app_name", language),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = SolaYellow,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = if (language == AppLanguage.ENGLISH) "STUDIO & ADVERT" else "ዲዛይንና ማስታወቂያ",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = SolaCyan,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Header buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switch Toggle
                    Button(
                        onClick = onLanguageToggle,
                        colors = ButtonDefaults.buttonColors(containerColor = SolaCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("lang_toggle")
                    ) {
                        Text(
                            text = if (language == AppLanguage.ENGLISH) "አማርኛ" else "ENG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolaYellow
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Admin icon switcher
                    IconButton(
                        onClick = onAdminToggle,
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isAdminMode) SolaMagenta.copy(alpha = 0.2f) else SolaCardBorder,
                                CircleShape
                            )
                            .testTag("admin_toggle")
                    ) {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.Build else Icons.Default.Person,
                            contentDescription = "Toggle Staff Mode",
                            tint = if (isAdminMode) SolaMagenta else SolaTextDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Slogan / Sola status ribbon
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocaleStrings.get("motto", language),
                    fontSize = 10.sp,
                    color = SolaTextDim,
                    fontWeight = FontWeight.Light
                )

                if (isAdminMode) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SolaMagenta),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ENGLISH) "STAFF MODE" else "ሰራተኛ ሁነታ",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SolaGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ENGLISH) "CLIENT ONLINE" else "ደንበኛ",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolaGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SolaBottomNav(
    language: AppLanguage,
    activeTab: SolaTab,
    onTabSelect: (SolaTab) -> Unit
) {
    NavigationBar(
        containerColor = SolaDarkSurface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val tabs = listOf(
            Triple(SolaTab.TRACK, Icons.Default.Search, "track_tab"),
            Triple(SolaTab.NEW_ORDER, Icons.Default.Create, "new_order_tab"),
            Triple(SolaTab.INVENTORY, Icons.Default.List, "inventory_tab"),
            Triple(SolaTab.SOCIALS, Icons.Default.Share, "socials_tab")
        )

        tabs.forEach { (tab, icon, resourceKey) ->
            NavigationBarItem(
                selected = activeTab == tab,
                onClick = { onTabSelect(tab) },
                icon = { Icon(imageVector = icon, contentDescription = LocaleStrings.get(resourceKey, language)) },
                label = { Text(text = LocaleStrings.get(resourceKey, language), fontSize = 11.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SolaYellow,
                    selectedTextColor = SolaYellow,
                    unselectedIconColor = SolaTextDim,
                    unselectedTextColor = SolaTextDim,
                    indicatorColor = SolaCardBorder
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun LowStockAlertBanner(
    language: AppLanguage,
    lowStockItems: List<InventoryItem>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .border(1.dp, SolaMagenta, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C101B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SolaMagenta.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Low Stock Warning",
                    tint = SolaMagenta,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocaleStrings.get("low_stock_warning", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SolaMagenta
                )
                Text(
                    text = if (language == AppLanguage.ENGLISH) {
                        "${lowStockItems.size} custom items are critically low on supplies. Click to review."
                    } else {
                        "${lowStockItems.size} የህትመት እቃዎች ከደህንነት መጠን በታች ናቸው። ለመሙላት ይጫኑ።"
                    },
                    fontSize = 12.sp,
                    color = SolaTextLight
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View Stock",
                tint = SolaTextDim
            )
        }
    }
}

@Composable
fun TrackAndManageScreen(
    viewModel: SolaViewModel,
    language: AppLanguage,
    isAdminMode: Boolean
) {
    val searchQuery by viewModel.trackingSearchQuery.collectAsState()
    val orders by viewModel.searchResults.collectAsState()
    val selectedOrderId by viewModel.selectedOrderId.collectAsState()

    var activeOrder = orders.find { it.id == selectedOrderId }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isAdminMode) {
            // Admin management header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SolaMagenta.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LocaleStrings.get("admin_title", language),
                        fontWeight = FontWeight.Bold,
                        color = SolaMagenta,
                        fontSize = 16.sp
                    )
                    Text(
                        text = LocaleStrings.get("admin_desc", language),
                        fontSize = 12.sp,
                        color = SolaTextDim
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search Bar Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SolaCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = LocaleStrings.get("search_label", language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = SolaYellow
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.trackingSearchQuery.value = it },
                    placeholder = { Text(text = LocaleStrings.get("search_hint", language), fontSize = 12.sp, color = SolaTextDim) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SolaDarkBg,
                        unfocusedContainerColor = SolaDarkBg,
                        focusedTextColor = SolaTextLight,
                        unfocusedTextColor = SolaTextLight,
                        focusedIndicatorColor = SolaCyan,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_order_input"),
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "", tint = SolaCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.trackingSearchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = SolaTextDim)
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order list & details layout
        if (activeOrder == null) {
            // View order count subtitle
            Text(
                text = "${LocaleStrings.get("orders_count", language)} (${orders.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SolaTextLight,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (orders.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = LocaleStrings.get("no_orders_found", language),
                            textAlign = TextAlign.Center,
                            color = SolaTextDim,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                orders.forEach { order ->
                    OrderCompactRow(
                        order = order,
                        language = language,
                        onClick = { viewModel.selectOrder(order.id) },
                        isAdminMode = isAdminMode,
                        onDelete = { viewModel.deleteOrder(order) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // Detailed order tracker screen
            DetailedOrderTracker(
                order = activeOrder,
                language = language,
                isAdminMode = isAdminMode,
                viewModel = viewModel,
                onBack = { viewModel.selectOrder(null) }
            )
        }
    }
}

@Composable
fun OrderCompactRow(
    order: Order,
    language: AppLanguage,
    onClick: () -> Unit,
    isAdminMode: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when (order.status) {
                "Submitted" -> SolaTextDim
                "Accepted" -> SolaCyan
                "Designing" -> SolaMagenta
                "Printing" -> SolaYellow
                "Finished", "Ready" -> SolaGreen
                else -> SolaCardBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.getFormattedId(),
                        fontWeight = FontWeight.Bold,
                        color = SolaYellow,
                        fontSize = 15.sp
                    )
                    Text(
                        text = order.orderTitle,
                        color = SolaTextLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                // Status Badge
                StatusBadge(status = order.status, language = language)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Client: ${order.clientName}",
                    fontSize = 12.sp,
                    color = SolaTextDim
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Birr ${String.format("%,.1f", order.totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SolaCyan
                    )

                    if (isAdminMode) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Printing Order",
                                tint = SolaMagenta,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, language: AppLanguage) {
    val (color, textEnglish, textAmharic) = when (status) {
        "Submitted" -> Triple(Color.LightGray, "Submitted", "የቀረበ")
        "Accepted" -> Triple(SolaCyan, "Accepted", "ተቀባይነት አግኝቷል")
        "Designing" -> Triple(SolaMagenta, "Designing", "በዲዛይን ላይ")
        "Printing" -> Triple(SolaYellow, "Printing", "በህትመት ላይ")
        "Finishing" -> Triple(Color(0xFFFF8A65), "Finishing", "በእሽግ ላይ")
        "Finished", "Ready" -> Triple(SolaGreen, "Ready", "ለርክክብ ዝግጁ")
        "Delivered" -> Triple(Color.Gray, "Delivered", "የተረከቡት")
        else -> Triple(SolaTextDim, status, status)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color)
    ) {
        Text(
            text = if (language == AppLanguage.ENGLISH) textEnglish else textAmharic,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun DetailedOrderTracker(
    order: Order,
    language: AppLanguage,
    isAdminMode: Boolean,
    viewModel: SolaViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.activeMessages.collectAsState()
    var messageText by remember { mutableStateFlowOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Back Button Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = SolaDarkSurface),
                border = BorderStroke(1.dp, SolaCardBorder),
                modifier = Modifier.testTag("back_button_tracker")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SolaYellow)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (language == AppLanguage.ENGLISH) "Back" else "ተመለስ", color = SolaYellow)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Identity Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LocaleStrings.get("order_details", language),
                        fontWeight = FontWeight.Bold,
                        color = SolaCyan,
                        fontSize = 14.sp
                    )
                    Text(
                        text = order.getFormattedId(),
                        fontWeight = FontWeight.Bold,
                        color = SolaYellow,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OrderDetailItem(label = LocaleStrings.get("order_title", language), value = order.orderTitle)
                OrderDetailItem(label = LocaleStrings.get("client_name", language), value = order.clientName)
                OrderDetailItem(label = LocaleStrings.get("client_phone", language), value = order.clientPhone)
                OrderDetailItem(label = LocaleStrings.get("category", language), value = order.orderCategory)
                OrderDetailItem(label = LocaleStrings.get("quantity", language), value = order.quantity.toString())
                OrderDetailItem(label = LocaleStrings.get("total_price", language), value = "${order.totalPrice} Birr")

                if (order.notes.isNotEmpty()) {
                    OrderDetailItem(label = LocaleStrings.get("notes", language), value = order.notes)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = LocaleStrings.get("order_status_title", language) + ": ",
                        fontWeight = FontWeight.Bold,
                        color = SolaTextLight,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = order.status, language = language)
                }

                // If Admin Mode, show buttons to change production status
                if (isAdminMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SolaDarkBg),
                        border = BorderStroke(1.dp, SolaMagenta)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "PRODUCTION TEAM PIPELINE ACTIONS",
                                color = SolaMagenta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (order.status == "Submitted") {
                                    Button(
                                        onClick = { viewModel.acceptOrder(order) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SolaCyan),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "Accept Order", color = Color.Black)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.advanceProductionStatus(order) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SolaYellow),
                                        modifier = Modifier.weight(1f),
                                        enabled = order.status != "Delivered"
                                    ) {
                                        Text(text = "Next Phase >>", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Production Direct Chat Room
        Text(
            text = LocaleStrings.get("recent_messages", language),
            fontWeight = FontWeight.Bold,
            color = SolaTextLight,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SolaCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // AI Operator Warning hint
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SolaCyan.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "", tint = SolaCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocaleStrings.get("ai_active", language),
                            fontSize = 10.sp,
                            color = SolaCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Chat area inside Sola Card
                Column(
                    modifier = Modifier
                        .heightIn(min = 100.dp, max = 220.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    if (messages.isEmpty()) {
                        Text(
                            text = "No messages yet.",
                            color = SolaTextDim,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                        )
                    } else {
                        messages.forEach { msg ->
                            val isMe = if (isAdminMode) {
                                msg.sender == "Production"
                            } else {
                                msg.sender == "Client"
                            }
                            ChatBubble(message = msg, isMe = isMe)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input sender box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        placeholder = { Text(text = LocaleStrings.get("message_placeholder", language), fontSize = 11.sp, color = SolaTextDim) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SolaDarkBg,
                            unfocusedContainerColor = SolaDarkBg,
                            focusedTextColor = SolaTextLight,
                            unfocusedTextColor = SolaTextLight,
                            focusedIndicatorColor = SolaYellow
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                // In Client mode: client is sender. Sola AI auto replies.
                                // In Admin mode: staff is sender.
                                viewModel.sendMessage(messageText, isClientSender = !isAdminMode, order = order)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(SolaYellow, RoundedCornerShape(8.dp))
                            .testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, isMe: Boolean) {
    val align = if (isMe) Alignment.End else Alignment.Start
    val bg = if (isMe) SolaCyan.copy(alpha = 0.15f) else SolaCardBorder
    val textCol = SolaTextLight
    val senderLabel = if (message.sender == "Production") "Sola Graphics Team" else "Client"

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = senderLabel,
                fontSize = 9.sp,
                color = if (message.sender == "Production") SolaYellow else SolaCyan,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                fontSize = 8.sp,
                color = SolaTextDim
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = bg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = message.text,
                color = textCol,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun OrderDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = SolaTextDim, fontSize = 12.sp, fontWeight = FontWeight.Normal)
        Text(text = value, color = SolaTextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
fun NewOrderScreen(viewModel: SolaViewModel, language: AppLanguage) {
    var name by remember { mutableStateFlowOf("") }
    var phone by remember { mutableStateFlowOf("") }
    var title by remember { mutableStateFlowOf("") }
    var quantity by remember { mutableStateFlowOf("200") }
    var notes by remember { mutableStateFlowOf("") }

    val categories = listOf("Business Cards", "Banners", "Posters/Flyers", "Brochures")
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    // Dynamic price estimation calculation live!
    val estimatedPrice = remember(selectedCategoryIndex, quantity) {
        val qty = quantity.toIntOrNull() ?: 0
        val basePrice = when (selectedCategoryIndex) {
            0 -> 3.0 // Each business card: 3 Birr
            1 -> 250.0 // Flex banner roll: 250 Birr per linear meter
            2 -> 5.0 // Poster flyers: 5 Birr per sheet
            3 -> 12.0 // Brochure: 12 Birr per spiral pack
            else -> 10.0
        }
        qty * basePrice
    }

    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SolaCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = LocaleStrings.get("new_order_tab", language).uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = SolaYellow
            )
            Text(
                text = LocaleStrings.get("order_design", language),
                fontSize = 11.sp,
                color = SolaTextDim,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Form inputs
            SolaInput(
                value = name,
                onValueChange = { name = it },
                label = LocaleStrings.get("client_name", language),
                tag = "client_name_input"
            )

            SolaInput(
                value = phone,
                onValueChange = { phone = it },
                label = LocaleStrings.get("client_phone", language),
                keyboardType = KeyboardType.Phone,
                tag = "client_phone_input"
            )

            SolaInput(
                value = title,
                onValueChange = { title = it },
                label = LocaleStrings.get("order_title", language),
                tag = "order_title_input"
            )

            // Category selector spinner
            Text(
                text = LocaleStrings.get("category", language),
                fontSize = 12.sp,
                color = SolaCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEachIndexed { idx, category ->
                    val isSel = selectedCategoryIndex == idx
                    Card(
                        modifier = Modifier
                            .clickable { selectedCategoryIndex = idx }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) SolaYellow else SolaDarkBg
                        ),
                        border = BorderStroke(1.dp, if (isSel) SolaYellow else SolaCardBorder)
                    ) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.Black else SolaTextLight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SolaInput(
                value = quantity,
                onValueChange = { quantity = it },
                label = LocaleStrings.get("quantity", language),
                keyboardType = KeyboardType.Number,
                tag = "quantity_input"
            )

            SolaInput(
                value = notes,
                onValueChange = { notes = it },
                label = LocaleStrings.get("notes", language),
                singleLine = false,
                tag = "notes_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful interactive price output box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SolaDarkBg),
                border = BorderStroke(1.dp, SolaCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocaleStrings.get("total_price", language),
                        fontWeight = FontWeight.Bold,
                        color = SolaCyan,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Birr ${String.format("%,.1f", estimatedPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = SolaYellow,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || title.isBlank()) {
                        Toast.makeText(context, LocaleStrings.get("validation_error", language), Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.createOrder(
                            clientName = name,
                            clientPhone = phone,
                            title = title,
                            category = categories[selectedCategoryIndex],
                            quantity = quantity.toIntOrNull() ?: 200,
                            totalPrice = estimatedPrice,
                            notes = notes
                        )
                        Toast.makeText(context, "Sola print job filed successfully!", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_order_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SolaGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = LocaleStrings.get("submit_order", language),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SolaInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    tag: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(text = label, fontSize = 11.sp, color = SolaTextDim, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SolaDarkBg,
                unfocusedContainerColor = SolaDarkBg,
                focusedTextColor = SolaTextLight,
                unfocusedTextColor = SolaTextLight,
                focusedIndicatorColor = SolaYellow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag)
        )
    }
}

@Composable
fun InventoryScreen(
    viewModel: SolaViewModel,
    language: AppLanguage,
    isAdminMode: Boolean
) {
    val items by viewModel.allInventoryItems.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LocaleStrings.get("inventory_list", language),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = SolaCyan
                )
                Text(
                    text = if (language == AppLanguage.ENGLISH) {
                        "Sola Graphics live stock monitoring system. When production advancements are processed, stock automatically decreases."
                    } else {
                        "የሶላ ግራፊክስ የእቃዎች ክምችት መቆጣጠሪያ። የምርት ሂደቱ ሲያልፍ፣ እቃዎች በራሳቸው ይቀንሳሉ።"
                    },
                    fontSize = 11.sp,
                    color = SolaTextDim,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inventory Stock Items Rows
        items.forEach { item ->
            InventoryItemRow(
                item = item,
                language = language,
                isAdminMode = isAdminMode,
                onRefill = { viewModel.updateStock(item.id, if (item.category == "Paper" || item.category == "Accessories") 250 else 10) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun InventoryItemRow(
    item: InventoryItem,
    language: AppLanguage,
    isAdminMode: Boolean,
    onRefill: () -> Unit
) {
    val isLow = item.isLowStock()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLow) Color(0xFF2E131C) else SolaDarkSurface
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isLow) SolaMagenta else SolaCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle with category visual colors
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (item.category) {
                            "Ink" -> SolaCyan.copy(alpha = 0.2f)
                            "Paper" -> SolaYellow.copy(alpha = 0.2f)
                            "Vinyl Roll" -> SolaMagenta.copy(alpha = 0.2f)
                            else -> SolaTextDim.copy(alpha = 0.2f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.category) {
                        "Ink" -> Icons.Default.Done
                        "Paper" -> Icons.Default.List
                        "Vinyl Roll" -> Icons.Default.Build
                        else -> Icons.Default.Star
                    },
                    contentDescription = "",
                    tint = when (item.category) {
                        "Ink" -> SolaCyan
                        "Paper" -> SolaYellow
                        "Vinyl Roll" -> SolaMagenta
                        else -> SolaTextLight
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == AppLanguage.ENGLISH) item.nameEnglish else item.nameAmharic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SolaTextLight
                )
                Text(
                    text = "${LocaleStrings.get("category_label", language)} ${item.category}",
                    fontSize = 11.sp,
                    color = SolaTextDim
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = LocaleStrings.get("current_stock", language) + " ",
                        fontSize = 11.sp,
                        color = SolaTextDim
                    )
                    Text(
                        text = "${item.currentStock} ${item.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isLow) SolaMagenta else SolaGreen
                    )
                    Text(
                        text = " | ${LocaleStrings.get("min_required", language)} ${item.minimumStockLevel}",
                        fontSize = 11.sp,
                        color = SolaTextDim
                    )
                }
            }

            // Low alert badge or refill click
            if (isLow) {
                Box(
                    modifier = Modifier
                        .background(SolaMagenta.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, SolaMagenta, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = LocaleStrings.get("low_stock_badge", language),
                        fontSize = 9.sp,
                        color = SolaMagenta,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (isAdminMode) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRefill,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SolaCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .testTag("refill_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Buy stock",
                        tint = SolaCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SocialsAndPromosScreen(language: AppLanguage) {
    val context = LocalContext.current
    
    // Addis Ababa social channels linkage
    val socials = listOf(
        Triple("Telegram Channel", "https://t.me/SolaGraphicsAddis", SolaCyan),
        Triple("Facebook Page", "https://facebook.com/SolaGraphicsAddis", Color(0xFF1877F2)),
        Triple("Instagram Shop", "https://instagram.com/SolaGraphicsAddis", SolaMagenta)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SolaCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LocaleStrings.get("social_title", language),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = SolaYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LocaleStrings.get("social_subtitle", language),
                    fontSize = 12.sp,
                    color = SolaTextLight
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large promo banner link cards
        socials.forEach { (name, url, color) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Link copied to clipboard: $url", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .shadow(4.dp, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "", tint = color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SolaTextLight)
                        Text(text = url, fontSize = 11.sp, color = SolaTextDim)
                    }
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Open Link", tint = SolaTextDim)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Our premium services list decoration (M3 visual pairs)
        Text(
            text = LocaleStrings.get("services_title", language),
            fontWeight = FontWeight.Bold,
            color = SolaCyan,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SolaDarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                listOf("service_1", "service_2", "service_3", "service_4").forEach { serviceKey ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "",
                            tint = SolaYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocaleStrings.get(serviceKey, language),
                            fontSize = 12.sp,
                            color = SolaTextLight
                        )
                    }
                }
            }
        }
    }
}

// Simple dynamic state encapsulation
fun <T> mutableStateFlowOf(value: T): MutableState<T> {
    return mutableStateOf(value)
}
