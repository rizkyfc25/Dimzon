package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethods
import com.example.data.model.TransactionStatuses
import com.example.ui.theme.DimzoneAmber
import com.example.ui.theme.DimzoneGold
import com.example.ui.theme.DimzoneGreen
import com.example.ui.theme.DimzoneRedPrimary
import com.example.ui.viewmodel.PosViewModel
import com.example.ui.viewmodel.ReportDateFilter
import com.example.util.Formatters

@Composable
fun ReportScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allItems by viewModel.allTransactionItems.collectAsStateWithLifecycle()
    val currentFilter by viewModel.reportDateFilter.collectAsStateWithLifecycle()

    // Filter valid transactions (only completed, not cancelled)
    val validTransactions = allTransactions.filter {
        it.status == TransactionStatuses.COMPLETED &&
                viewModel.isDateMatchingFilter(it.timestamp, currentFilter)
    }

    val validTxIds = validTransactions.map { it.id }.toSet()
    val filteredItems = allItems.filter { validTxIds.contains(it.transactionId) }

    val totalRevenue = validTransactions.sumOf { it.totalAmount }
    val totalTransactionsCount = validTransactions.size
    val totalDiscounts = validTransactions.sumOf { it.discountAmount }
    val avgOrderValue = if (totalTransactionsCount > 0) totalRevenue / totalTransactionsCount else 0.0

    // Payment methods breakdown
    val paymentMap = validTransactions.groupBy { it.paymentMethod }

    // Top selling products
    val topProducts = filteredItems
        .groupBy { it.productName }
        .map { (name, items) ->
            val totalSold = items.sumOf { it.quantity }
            val revenue = items.sumOf { it.subtotal }
            Pair(name, Pair(totalSold, revenue))
        }
        .sortedByDescending { it.second.first }
        .take(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("report_screen")
    ) {
        // Title
        Text(
            text = "Laporan & Analitik Penjualan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DimzoneRedPrimary
        )
        Text(
            text = "Pantau performa pendapatan dan pergerakan produk DIMZONE",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills Row (Hari ini, Kemarin, 7 hari terakhir, Bulan ini, Semua)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportDateFilter.values().forEach { filterOption ->
                val isSelected = currentFilter == filterOption
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setReportDateFilter(filterOption) },
                    label = {
                        Text(
                            filterOption.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DimzoneRedPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_${filterOption.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Total Pendapatan",
                value = Formatters.formatRupiah(totalRevenue),
                icon = Icons.Default.AttachMoney,
                iconTint = DimzoneGreen,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Jumlah Transaksi",
                value = "$totalTransactionsCount Pesanan",
                icon = Icons.Default.Receipt,
                iconTint = DimzoneAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Total Diskon Diberikan",
                value = Formatters.formatRupiah(totalDiscounts),
                icon = Icons.Default.Discount,
                iconTint = DimzoneRedPrimary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Rata-rata / Pesanan",
                value = Formatters.formatRupiah(avgOrderValue),
                icon = Icons.Default.TrendingUp,
                iconTint = DimzoneGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Ringkasan Metode Pembayaran
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = DimzoneRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ringkasan Metode Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethods.LIST.forEach { method ->
                    val txs = paymentMap[method] ?: emptyList()
                    val count = txs.size
                    val sum = txs.sumOf { it.totalAmount }
                    val progress = if (totalRevenue > 0) (sum / totalRevenue).toFloat() else 0f

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(method, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("($count tx)", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                Formatters.formatRupiah(sum),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = when (method) {
                                PaymentMethods.CASH -> DimzoneGreen
                                PaymentMethods.QRIS -> DimzoneAmber
                                PaymentMethods.TRANSFER -> Color(0xFF1976D2)
                                else -> DimzoneRedPrimary
                            },
                            trackColor = Color(0xFFEEEEEE)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Produk Paling Banyak Terjual (Top Selling Products)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = DimzoneAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Produk Paling Banyak Terjual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (topProducts.isEmpty()) {
                    Text("Belum ada data penjualan pada periode ini", color = Color.Gray, fontSize = 12.sp)
                } else {
                    topProducts.forEachIndexed { index, (productName, stats) ->
                        val (soldQty, itemRevenue) = stats
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = if (index == 0) DimzoneGold else Color(0xFFEEEEEE),
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (index == 0) Color.Black else Color.DarkGray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "Terjual: $soldQty porsi",
                                        fontSize = 11.sp,
                                        color = DimzoneAmber,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Text(
                                text = Formatters.formatRupiah(itemRevenue),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (index < topProducts.size - 1) {
                            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
