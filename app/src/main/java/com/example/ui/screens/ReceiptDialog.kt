package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.ui.theme.DimzoneAmber
import com.example.ui.theme.DimzoneGreen
import com.example.ui.theme.DimzoneRedPrimary
import com.example.util.Formatters

@Composable
fun ReceiptDialog(
    transaction: TransactionEntity,
    items: List<TransactionItemEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sukses",
                            tint = DimzoneGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transaksi Berhasil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DimzoneGreen
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_receipt_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Paper Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo DIMZONE
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DimzoneRedPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_dimzone_logo),
                                contentDescription = "Logo DIMZONE",
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DIMZONE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = DimzoneRedPrimary
                        )
                        Text(
                            text = "Authentic Dimsum & Grill",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Jl. Dimsum Lezat No. 88, Telp: 0812-3456-7890",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFDCDCDC))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Transaction Info
                        ReceiptInfoRow(label = "No. Transaksi", value = transaction.invoiceNumber)
                        ReceiptInfoRow(
                            label = "Waktu",
                            value = Formatters.formatDateTime(transaction.timestamp)
                        )
                        ReceiptInfoRow(label = "Kasir", value = transaction.cashierName)
                        ReceiptInfoRow(label = "Pelanggan", value = transaction.customerName)
                        ReceiptInfoRow(
                            label = "Metode Bayar",
                            value = transaction.paymentMethod,
                            valueColor = DimzoneAmber
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFDCDCDC))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Items list
                        Text(
                            text = "RINCIAN PESANAN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        items.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.productName} x${item.quantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(item.subtotal),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "@${Formatters.formatRupiah(item.price)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    if (item.itemNote.isNotBlank()) {
                                        Text(
                                            text = "(${item.itemNote})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DimzoneAmber
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFDCDCDC))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Financial Breakdown
                        ReceiptInfoRow(
                            label = "Subtotal",
                            value = Formatters.formatRupiah(transaction.subtotal)
                        )
                        if (transaction.discountAmount > 0) {
                            ReceiptInfoRow(
                                label = "Diskon",
                                value = "-${Formatters.formatRupiah(transaction.discountAmount)}",
                                valueColor = DimzoneRedPrimary
                            )
                        }
                        if (transaction.additionalFee > 0) {
                            ReceiptInfoRow(
                                label = "Biaya / Pajak",
                                value = "+${Formatters.formatRupiah(transaction.additionalFee)}"
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = Formatters.formatRupiah(transaction.totalAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = DimzoneRedPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        ReceiptInfoRow(
                            label = "Bayar (${transaction.paymentMethod})",
                            value = Formatters.formatRupiah(transaction.amountPaid)
                        )
                        ReceiptInfoRow(
                            label = "Kembalian",
                            value = Formatters.formatRupiah(transaction.changeAmount),
                            valueColor = DimzoneGreen
                        )

                        if (transaction.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Catatan: ${transaction.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFDCDCDC))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Terima kasih telah berbelanja di DIMZONE!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Nikmati kehangatan & kelezatan dimsum asli kami.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine("=== STRUK DIMZONE ===")
                                appendLine("Invoice: ${transaction.invoiceNumber}")
                                appendLine("Waktu: ${Formatters.formatDateTime(transaction.timestamp)}")
                                appendLine("Kasir: ${transaction.cashierName}")
                                appendLine("--------------------------------")
                                items.forEach {
                                    appendLine("${it.productName} x${it.quantity} = ${Formatters.formatRupiah(it.subtotal)}")
                                }
                                appendLine("--------------------------------")
                                appendLine("TOTAL: ${Formatters.formatRupiah(transaction.totalAmount)}")
                                appendLine("Metode: ${transaction.paymentMethod}")
                                appendLine("Bayar: ${Formatters.formatRupiah(transaction.amountPaid)}")
                                appendLine("Kembali: ${Formatters.formatRupiah(transaction.changeAmount)}")
                                appendLine("Terima kasih telah berbelanja di DIMZONE!")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk DIMZONE")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_receipt_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Mencetak Struk #${transaction.invoiceNumber} ke printer kasir...",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DimzoneRedPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("print_receipt_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Struk")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("new_order_button")
                ) {
                    Text("Pesanan Baru / Selesai", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReceiptInfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}
