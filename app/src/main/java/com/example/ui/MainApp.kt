package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Roles
import com.example.data.model.UserEntity
import com.example.ui.screens.PosCashierScreen
import com.example.ui.screens.ProductManagementScreen
import com.example.ui.screens.ReceiptDialog
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.StockManagementScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.DimzoneAmber
import com.example.ui.theme.DimzoneGold
import com.example.ui.theme.DimzoneGreen
import com.example.ui.theme.DimzoneRedPrimary
import com.example.ui.viewmodel.PosViewModel

enum class MainDestination(
    val title: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
) {
    POS("Kasir POS", Icons.Default.PointOfSale),
    HISTORY("Riwayat", Icons.Default.Receipt),
    STOCK("Stok", Icons.Default.Inventory),
    PRODUCTS("Produk", Icons.Default.Fastfood, adminOnly = true),
    REPORTS("Laporan", Icons.Default.Assessment, adminOnly = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: PosViewModel
) {
    var currentDestination by remember { mutableStateOf(MainDestination.POS) }
    var showUserSwitchDialog by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val showReceipt by viewModel.showReceiptDialog.collectAsStateWithLifecycle()
    val lastTx by viewModel.lastCompletedTransaction.collectAsStateWithLifecycle()
    val lastItems by viewModel.lastCompletedItems.collectAsStateWithLifecycle()

    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val lowStockCount = allProducts.count { it.stock <= it.minStockAlert }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    // Filter available navigation items based on role
    val visibleDestinations = MainDestination.values().filter { dest ->
        !dest.adminOnly || currentUser.role == Roles.ADMIN
    }

    // Auto navigate back to POS if Kasir tries to stay on Admin-only screen
    LaunchedEffect(currentUser.role) {
        if (currentUser.role != Roles.ADMIN && currentDestination.adminOnly) {
            currentDestination = MainDestination.POS
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 800.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_dimzone_logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DIMZONE",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = DimzoneRedPrimary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "POS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DimzoneAmber
                                    )
                                }
                                Text(
                                    text = "Authentic Dimsum & Grill",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    actions = {
                        // User Role & Account Switcher Chip
                        Surface(
                            modifier = Modifier
                                .clickable { showUserSwitchDialog = true }
                                .padding(end = 8.dp)
                                .testTag("role_switcher_chip"),
                            shape = RoundedCornerShape(20.dp),
                            color = if (currentUser.role == Roles.ADMIN) DimzoneRedPrimary else DimzoneAmber
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentUser.role == Roles.ADMIN) Icons.Default.SupervisorAccount else Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = currentUser.fullName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Role: ${currentUser.role}",
                                        color = Color(0xFFFFF9C4),
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Ganti",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        visibleDestinations.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    if (dest == MainDestination.STOCK && lowStockCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = DimzoneAmber) {
                                                    Text(lowStockCount.toString())
                                                }
                                            }
                                        ) {
                                            Icon(dest.icon, contentDescription = dest.title)
                                        }
                                    } else {
                                        Icon(dest.icon, contentDescription = dest.title)
                                    }
                                },
                                label = { Text(dest.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DimzoneRedPrimary,
                                    selectedTextColor = DimzoneRedPrimary,
                                    indicatorColor = Color(0xFFFFEBEE)
                                ),
                                modifier = Modifier.testTag("nav_item_${dest.name}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Navigation Rail for Wide Screens (Tablet / Desktop)
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight().testTag("navigation_rail"),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        visibleDestinations.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    if (dest == MainDestination.STOCK && lowStockCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = DimzoneAmber) {
                                                    Text(lowStockCount.toString())
                                                }
                                            }
                                        ) {
                                            Icon(dest.icon, contentDescription = dest.title)
                                        }
                                    } else {
                                        Icon(dest.icon, contentDescription = dest.title)
                                    }
                                },
                                label = { Text(dest.title, fontSize = 11.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = DimzoneRedPrimary,
                                    selectedTextColor = DimzoneRedPrimary,
                                    indicatorColor = Color(0xFFFFEBEE)
                                ),
                                modifier = Modifier.testTag("rail_item_${dest.name}")
                            )
                        }
                    }
                }

                // Main Content View
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentDestination) {
                        MainDestination.POS -> PosCashierScreen(viewModel = viewModel)
                        MainDestination.HISTORY -> TransactionHistoryScreen(viewModel = viewModel)
                        MainDestination.STOCK -> StockManagementScreen(viewModel = viewModel)
                        MainDestination.PRODUCTS -> ProductManagementScreen(viewModel = viewModel)
                        MainDestination.REPORTS -> ReportScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Receipt Dialog
    if (showReceipt && lastTx != null) {
        ReceiptDialog(
            transaction = lastTx!!,
            items = lastItems,
            onDismiss = viewModel::closeReceiptDialog
        )
    }

    // User / Role Switcher Dialog
    if (showUserSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showUserSwitchDialog = false },
            title = {
                Text(
                    text = "Pilih Pengguna / Hak Akses",
                    fontWeight = FontWeight.Bold,
                    color = DimzoneRedPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ganti akun pengguna untuk menguji hak akses Admin vs Kasir:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    allUsers.forEach { user ->
                        val isCurrent = user.id == currentUser.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUser(user)
                                    showUserSwitchDialog = false
                                }
                                .testTag("select_user_${user.username}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (user.role == Roles.ADMIN) Icons.Default.SupervisorAccount else Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = if (user.role == Roles.ADMIN) DimzoneRedPrimary else DimzoneAmber
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Username: ${user.username}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Surface(
                                    color = if (user.role == Roles.ADMIN) DimzoneRedPrimary else DimzoneAmber,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = user.role,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    // Quick Toggle Button
                    Button(
                        onClick = {
                            viewModel.toggleRole()
                            showUserSwitchDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DimzoneAmber),
                        modifier = Modifier.fillMaxWidth().testTag("btn_toggle_role")
                    ) {
                        Text("Beralih Cepat ke ${if (currentUser.role == Roles.ADMIN) "Kasir" else "Admin"}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserSwitchDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
