package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object Formatters {
    private val localeId = Locale("in", "ID")
    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(localeId).apply {
        maximumFractionDigits = 0
    }

    fun formatRupiah(amount: Double): String {
        return try {
            val formatted = rupiahFormat.format(amount)
            // Replace standard "Rp" spacing if needed
            formatted.replace("Rp", "Rp ")
        } catch (_: Exception) {
            "Rp ${amount.toLong()}"
        }
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", localeId)
        return sdf.format(Date(timestamp))
    }

    fun generateInvoiceNumber(): String {
        val sdf = SimpleDateFormat("yyMMddHHmm", Locale.getDefault())
        val datePart = sdf.format(Date())
        val rand = Random.nextInt(100, 999)
        return "DZ-$datePart-$rand"
    }
}
