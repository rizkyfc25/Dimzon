package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProductEntity
import com.example.data.model.StockChangeTypes
import com.example.data.model.StockHistoryEntity
import com.example.ui.theme.DimzoneAmber
import com.example.ui.theme.DimzoneGreen
import com.example.ui.theme.DimzoneRedPrimary
import com.example.ui.viewmodel.PosViewModel
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagementScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val stockHistory by viewModel.allStockHistory.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Stok Produk, 1: Riwayat Perubahan Stok
    var productForRestock by remember { mutableStateOf<ProductEntity?>(null) }

    val lowStockProducts = allProducts.filter { it.stock <= it.minStockAlert }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("stock_management_screen")
    ) {
        // Low Stock Warning Banner
        if (lowStockProducts.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("low_stock_warning_banner"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF3E0),
                border = androidx.compose.foundation.BorderStroke(1.dp, DimzoneAmber)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = DimzoneAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Peringatan: ${lowStockProducts.size} Produk Hampir / Sudah Habis!",
                            fontWeight = FontWeight.Bold,
                            color = DimzoneAmber,
                            fontSize = 14.sp
                        )
                        Text(
                            text = lowStockProducts.joinToString(", ") { "${it.name} (${it.stock})" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Tabs: Stok Saat Ini vs Riwayat Perubahan
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Stok Produk (${allProducts.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Inventory, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat Perubahan Stok", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Tab 0: Current Stock List
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("current_stock_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allProducts, key = { it.id }) { product ->
                    val isCritical = product.stock <= 0
                    val isWarning = product.stock in 1..product.minStockAlert

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${product.category} • Harga: ${Formatters.formatRupiah(product.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = when {
                                            isCritical -> DimzoneRedPrimary
                                            isWarning -> DimzoneAmber
                                            else -> DimzoneGreen
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isCritical -> "HABIS (0)"
                                                isWarning -> "MENIPIS (${product.stock})"
                                                else -> "AMAN (${product.stock})"
                                            },
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Batas Min: ${product.minStockAlert}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Quick Restock Button
                            Button(
                                onClick = { productForRestock = product },
                                colors = ButtonDefaults.buttonColors(containerColor = DimzoneRedPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_restock_${product.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restock", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Stock History Logs
            if (stockHistory.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada riwayat mutasi stok", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().testTag("stock_history_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stockHistory, key = { it.id }) { log ->
                        val isPositive = log.changeAmount >= 0
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = log.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = when (log.reasonType) {
                                                StockChangeTypes.SALE -> Color(0xFFE3F2FD)
                                                StockChangeTypes.RESTOCK -> Color(0xFFE8F5E9)
                                                StockChangeTypes.DAMAGED -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF3E0)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = log.reasonType,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (log.reasonType) {
                                                    StockChangeTypes.SALE -> Color(0xFF1565C0)
                                                    StockChangeTypes.RESTOCK -> DimzoneGreen
                                                    StockChangeTypes.DAMAGED -> DimzoneRedPrimary
                                                    else -> DimzoneAmber
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${Formatters.formatDateTime(log.timestamp)} • Oleh: ${log.performedBy}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    if (log.notes.isNotBlank()) {
                                        Text(
                                            text = "Ket: ${log.notes}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isPositive) "+${log.changeAmount}" else "${log.changeAmount}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (isPositive) DimzoneGreen else DimzoneRedPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Sisa: ${log.resultingStock}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Restock Dialog
    productForRestock?.let { product ->
        var addedQtyText by remember { mutableStateOf("20") }
        var reasonType by remember { mutableStateOf(StockChangeTypes.RESTOCK) }
        var notes by remember { mutableStateOf("Restock rutin supplier") }
        var reasonDropdownExpanded by remember { mutableStateOf(false) }

        val reasonOptions = listOf(
            StockChangeTypes.RESTOCK,
            StockChangeTypes.ADJUSTMENT,
            StockChangeTypes.DAMAGED
        )

        AlertDialog(
            onDismissRequest = { productForRestock = null },
            title = {
                Text(
                    text = "Restock: ${product.name}",
                    fontWeight = FontWeight.Bold,
                    color = DimzoneRedPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Stok saat ini: ${product.stock} unit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = addedQtyText,
                        onValueChange = { addedQtyText = it.filter { ch -> ch.isDigit() || ch == '-' } },
                        label = { Text("Jumlah Perubahan (+ / -)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_restock_amount")
                    )

                    // Reason Dropdown
                    ExposedDropdownMenuBox(
                        expanded = reasonDropdownExpanded,
                        onExpandedChange = { reasonDropdownExpanded = !reasonDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = reasonType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jenis Alasan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = reasonDropdownExpanded,
                            onDismissRequest = { reasonDropdownExpanded = false }
                        ) {
                            reasonOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        reasonType = opt
                                        reasonDropdownExpanded = false
                                        if (opt == StockChangeTypes.DAMAGED && !addedQtyText.startsWith("-")) {
                                            addedQtyText = "-5"
                                        }
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Keterangan / Catatan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = addedQtyText.toIntOrNull() ?: 0
                        if (qty != 0) {
                            viewModel.restockProduct(product.id, qty, reasonType, notes)
                        }
                        productForRestock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DimzoneRedPrimary),
                    modifier = Modifier.testTag("btn_confirm_restock")
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { productForRestock = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
