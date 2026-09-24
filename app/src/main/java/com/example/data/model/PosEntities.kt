package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

object Categories {
    const val ALL = "Semua"
    const val ORI = "Dimsum Ori"
    const val MENTAI = "Dimsum Mentai"
    const val MIX = "Dimsum Mix"
    const val BAKAR = "Dimsum Bakar"
    const val ADDON = "Add On"

    val LIST = listOf(ALL, ORI, MENTAI, MIX, BAKAR, ADDON)
}

object Roles {
    const val ADMIN = "Admin"
    const val KASIR = "Kasir"
}

object PaymentMethods {
    const val CASH = "Cash"
    const val QRIS = "QRIS"
    const val TRANSFER = "Transfer"
    const val EWALLET = "E-Wallet"

    val LIST = listOf(CASH, QRIS, TRANSFER, EWALLET)
}

object TransactionStatuses {
    const val COMPLETED = "Selesai"
    const val CANCELLED = "Dibatalkan"
}

object StockChangeTypes {
    const val SALE = "Penjualan"
    const val RESTOCK = "Restock"
    const val ADJUSTMENT = "Penyesuaian"
    const val DAMAGED = "Rusak/Basi"
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val fullName: String,
    val role: String = Roles.KASIR,
    val pin: String = "1234"
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    val costPrice: Double = 0.0,
    val stock: Int = 0,
    val minStockAlert: Int = 10,
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val description: String = ""
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val cashierId: Long = 1,
    val cashierName: String,
    val customerName: String = "Pelanggan",
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val discountPercent: Double = 0.0,
    val additionalFee: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String = PaymentMethods.CASH,
    val amountPaid: Double,
    val changeAmount: Double,
    val notes: String = "",
    val status: String = TransactionStatuses.COMPLETED,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transaction_items")
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val productCategory: String,
    val price: Double,
    val quantity: Int,
    val subtotal: Double,
    val itemNote: String = ""
)

@Entity(tableName = "stock_history")
data class StockHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val changeAmount: Int, // e.g. -2 or +20
    val resultingStock: Int,
    val reasonType: String,
    val notes: String = "",
    val performedBy: String = "Kasir",
    val timestamp: Long = System.currentTimeMillis()
)

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val note: String = ""
) {
    val subtotal: Double get() = product.price * quantity
}
