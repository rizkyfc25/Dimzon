package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.model.ProductEntity
import com.example.data.model.StockChangeTypes
import com.example.data.model.StockHistoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionStatuses
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

class PosRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val transactionItemDao = database.transactionItemDao()
    private val stockHistoryDao = database.stockHistoryDao()
    private val userDao = database.userDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allStockHistory: Flow<List<StockHistoryEntity>> = stockHistoryDao.getAllHistory()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allTransactionItems: Flow<List<TransactionItemEntity>> = transactionItemDao.getAllItems()

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItemEntity> {
        return transactionItemDao.getItemsByTransactionId(transactionId)
    }

    suspend fun addProduct(product: ProductEntity): Long {
        val newId = productDao.insert(product)
        if (product.stock > 0) {
            stockHistoryDao.insert(
                StockHistoryEntity(
                    productId = newId,
                    productName = product.name,
                    changeAmount = product.stock,
                    resultingStock = product.stock,
                    reasonType = StockChangeTypes.RESTOCK,
                    notes = "Stok awal produk baru",
                    performedBy = "Admin"
                )
            )
        }
        return newId
    }

    suspend fun updateProduct(product: ProductEntity) {
        val old = productDao.getProductById(product.id)
        productDao.update(product)
        if (old != null && old.stock != product.stock) {
            val diff = product.stock - old.stock
            stockHistoryDao.insert(
                StockHistoryEntity(
                    productId = product.id,
                    productName = product.name,
                    changeAmount = diff,
                    resultingStock = product.stock,
                    reasonType = StockChangeTypes.ADJUSTMENT,
                    notes = "Perubahan manual data produk",
                    performedBy = "Admin"
                )
            )
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product)
    }

    suspend fun restockProduct(
        productId: Long,
        addedStock: Int,
        reasonType: String,
        notes: String,
        performedBy: String
    ): Boolean {
        return try {
            val product = productDao.getProductById(productId) ?: return false
            val newStock = (product.stock + addedStock).coerceAtLeast(0)
            productDao.updateStock(productId, newStock)
            stockHistoryDao.insert(
                StockHistoryEntity(
                    productId = productId,
                    productName = product.name,
                    changeAmount = addedStock,
                    resultingStock = newStock,
                    reasonType = reasonType,
                    notes = notes,
                    performedBy = performedBy
                )
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun processTransaction(
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ): Result<Long> {
        return try {
            database.withTransaction {
                // Verify all stocks first
                for (item in items) {
                    val product = productDao.getProductById(item.productId)
                        ?: return@withTransaction Result.failure(Exception("Produk ${item.productName} tidak ditemukan"))
                    if (product.stock < item.quantity) {
                        return@withTransaction Result.failure(
                            Exception("Stok ${product.name} tidak cukup (Tersisa: ${product.stock}, diminta: ${item.quantity})")
                        )
                    }
                }

                // Insert transaction
                val transactionId = transactionDao.insert(transaction)

                // Insert items with transactionId
                val itemsWithId = items.map { it.copy(transactionId = transactionId) }
                transactionItemDao.insertAll(itemsWithId)

                // Decrement stock and log history
                for (item in items) {
                    val product = productDao.getProductById(item.productId)!!
                    val newStock = product.stock - item.quantity
                    productDao.updateStock(product.id, newStock)
                    stockHistoryDao.insert(
                        StockHistoryEntity(
                            productId = product.id,
                            productName = product.name,
                            changeAmount = -item.quantity,
                            resultingStock = newStock,
                            reasonType = StockChangeTypes.SALE,
                            notes = "Transaksi #${transaction.invoiceNumber}",
                            performedBy = transaction.cashierName
                        )
                    )
                }

                Result.success(transactionId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelTransaction(transactionId: Long, performedBy: String): Result<Unit> {
        return try {
            database.withTransaction {
                val transaction = transactionDao.getTransactionById(transactionId)
                    ?: return@withTransaction Result.failure(Exception("Transaksi tidak ditemukan"))
                if (transaction.status == TransactionStatuses.CANCELLED) {
                    return@withTransaction Result.failure(Exception("Transaksi sudah dibatalkan sebelumnya"))
                }

                val items = transactionItemDao.getItemsByTransactionId(transactionId)
                // Rollback stocks
                for (item in items) {
                    val product = productDao.getProductById(item.productId)
                    if (product != null) {
                        val restoredStock = product.stock + item.quantity
                        productDao.updateStock(product.id, restoredStock)
                        stockHistoryDao.insert(
                            StockHistoryEntity(
                                productId = product.id,
                                productName = product.name,
                                changeAmount = item.quantity,
                                resultingStock = restoredStock,
                                reasonType = StockChangeTypes.ADJUSTMENT,
                                notes = "Pembatalan transaksi #${transaction.invoiceNumber}",
                                performedBy = performedBy
                            )
                        )
                    }
                }

                transactionDao.updateStatus(transactionId, TransactionStatuses.CANCELLED)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addUser(user: UserEntity): Long {
        return userDao.insert(user)
    }

    suspend fun ensureInitialData() {
        AppDatabase.populateInitialData(database)
    }
}
