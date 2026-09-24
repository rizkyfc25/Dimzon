package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionStatuses
import com.example.ui.theme.DimzoneAmber
import com.example.ui.theme.DimzoneGreen
import com.example.ui.theme.DimzoneRedPrimary
import com.example.ui.viewmodel.PosViewModel
import com.example.util.Formatters

@Composable
fun TransactionHistoryScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTxForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
    var detailItems by remember { mutableStateOf<List<TransactionItemEntity>>(emptyList()) }
    var txToCancel by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = allTransactions.filter { tx ->
        searchQuery.isBlank() ||
                tx.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                tx.customerName.contains(searchQuery, ignoreCase = true) ||
                tx.cashierName.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(selectedTxForDetail) {
        selectedTxForDetail?.let {
            detailItems = viewModel.getTransactionItems(it.id)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("transaction_history_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DimzoneRedPrimary
                )
                Text(
                    text = "${filteredTransactions.size} transaksi ditemukan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari no invoice, pelanggan, atau nama kasir...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Transaction list
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada riwayat transaksi", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("tx_history_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val isCancelled = tx.status == TransactionStatuses.CANCELLED

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTxForDetail = tx }
                            .testTag("tx_item_${tx.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCancelled) Color(0xFFFAFAFA) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.invoiceNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = if (isCancelled) DimzoneRedPrimary else DimzoneGreen,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = tx.status,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = Formatters.formatRupiah(tx.totalAmount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = if (isCancelled) Color.Gray else DimzoneRedPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Formatters.formatDateTime(tx.timestamp)} • Kasir: ${tx.cashierName}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Metode: ${tx.paymentMethod}",
                                    fontSize = 11.sp,
                                    color = DimzoneAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedTxForDetail?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTxForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Transaksi",
                        fontWeight = FontWeight.Bold,
                        color = DimzoneRedPrimary
                    )
                    Surface(
                        color = if (tx.status == TransactionStatuses.CANCELLED) DimzoneRedPrimary else DimzoneGreen,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = tx.status,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No. Faktur: ${tx.invoiceNumber}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Waktu: ${Formatters.formatDateTime(tx.timestamp)}", fontSize = 12.sp, color = Color.Gray)
                    Text("Kasir: ${tx.cashierName}", fontSize = 12.sp)
                    Text("Pelanggan: ${tx.customerName}", fontSize = 12.sp)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Item Pesanan:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    detailItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.productName} x${item.quantity}", fontSize = 12.sp)
                            Text(Formatters.formatRupiah(item.subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 12.sp, color = Color.Gray)
                        Text(Formatters.formatRupiah(tx.subtotal), fontSize = 12.sp)
                    }
                    if (tx.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon", fontSize = 12.sp, color = DimzoneRedPrimary)
                            Text("-${Formatters.formatRupiah(tx.discountAmount)}", fontSize = 12.sp, color = DimzoneRedPrimary)
                        }
                    }
                    if (tx.additionalFee > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Biaya/Pajak", fontSize = 12.sp, color = Color.Gray)
                            Text("+${Formatters.formatRupiah(tx.additionalFee)}", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL", fontWeight = FontWeight.Bold)
                        Text(
                            text = Formatters.formatRupiah(tx.totalAmount),
                            fontWeight = FontWeight.ExtraBold,
                            color = DimzoneRedPrimary,
                            fontSize = 16.sp
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bayar (${tx.paymentMethod})", fontSize = 12.sp)
                        Text(Formatters.formatRupiah(tx.amountPaid), fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kembalian", fontSize = 12.sp, color = DimzoneGreen)
                        Text(Formatters.formatRupiah(tx.changeAmount), fontSize = 12.sp, color = DimzoneGreen)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Cetak ulang struk
                    OutlinedButton(
                        onClick = {
                            viewModel.showReceiptFor(tx, detailItems)
                            selectedTxForDetail = null
                        },
                        modifier = Modifier.testTag("btn_reprint_receipt")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Struk", fontSize = 12.sp)
                    }

                    // Void / Cancel button if not yet cancelled
                    if (tx.status != TransactionStatuses.CANCELLED) {
                        Button(
                            onClick = {
                                txToCancel = tx
                                selectedTxForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DimzoneRedPrimary),
                            modifier = Modifier.testTag("btn_void_tx")
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Batalkan", fontSize = 12.sp)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTxForDetail = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Cancel Transaction Confirmation Dialog
    txToCancel?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToCancel = null },
            title = { Text("Batalkan Transaksi #${tx.invoiceNumber}?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Transaksi ini akan ditandai 'Dibatalkan' dan seluruh kuantitas produk yang dipesan akan otomatis dikembalikan ke stok.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelTransaction(tx.id)
                        txToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DimzoneRedPrimary)
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToCancel = null }) {
                    Text("Kembali")
                }
            }
        )
    }
}
