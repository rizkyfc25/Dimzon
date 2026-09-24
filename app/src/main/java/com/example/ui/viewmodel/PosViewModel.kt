package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Categories
import com.example.data.model.PaymentMethods
import com.example.data.model.ProductEntity
import com.example.data.model.Roles
import com.example.data.model.StockChangeTypes
import com.example.data.model.StockHistoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionStatuses
import com.example.data.model.UserEntity
import com.example.data.repository.PosRepository
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ReportDateFilter(val label: String) {
    TODAY("Hari Ini"),
    YESTERDAY("Kemarin"),
    LAST_7_DAYS("7 Hari Terakhir"),
    THIS_MONTH("Bulan Ini"),
    ALL("Semua")
}

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = PosRepository(database)

    // Current User
    private val _currentUser = MutableStateFlow(
        UserEntity(id = 1, username = "admin", fullName = "DIMZONE Admin", role = Roles.ADMIN, pin = "1234")
    )
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<ProductEntity>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock History
    val allStockHistory: StateFlow<List<StockHistoryEntity>> = repository.allStockHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactionItems: StateFlow<List<TransactionItemEntity>> = repository.allTransactionItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter & Search states
    private val _selectedCategory = MutableStateFlow(Categories.ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered Products for POS
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        activeProducts,
        _selectedCategory,
        _searchQuery
    ) { products, category, query ->
        products.filter { p ->
            val matchCategory = category == Categories.ALL || p.category.equals(category, ignoreCase = true)
            val matchQuery = query.isBlank() || p.name.contains(query, ignoreCase = true) || p.description.contains(query, ignoreCase = true)
            matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart state
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Payment & Discount state
    private val _paymentMethod = MutableStateFlow(PaymentMethods.CASH)
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _discountPercent = MutableStateFlow(0.0)
    val discountPercent: StateFlow<Double> = _discountPercent.asStateFlow()

    private val _manualDiscount = MutableStateFlow(0.0)
    val manualDiscount: StateFlow<Double> = _manualDiscount.asStateFlow()

    private val _taxOrFeePercent = MutableStateFlow(0.0) // e.g. 10% PB1
    val taxOrFeePercent: StateFlow<Double> = _taxOrFeePercent.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    private val _customerName = MutableStateFlow("Pelanggan")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _amountPaidInput = MutableStateFlow("")
    val amountPaidInput: StateFlow<String> = _amountPaidInput.asStateFlow()

    // Dialog & Feedback state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _lastCompletedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val lastCompletedTransaction: StateFlow<TransactionEntity?> = _lastCompletedTransaction.asStateFlow()

    private val _lastCompletedItems = MutableStateFlow<List<TransactionItemEntity>>(emptyList())
    val lastCompletedItems: StateFlow<List<TransactionItemEntity>> = _lastCompletedItems.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

    // Report filter
    private val _reportDateFilter = MutableStateFlow(ReportDateFilter.TODAY)
    val reportDateFilter: StateFlow<ReportDateFilter> = _reportDateFilter.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    // Calculations
    val subtotal: Double
        get() = _cartItems.value.sumOf { it.subtotal }

    val discountAmount: Double
        get() {
            val fromPercent = (subtotal * _discountPercent.value) / 100.0
            return fromPercent + _manualDiscount.value
        }

    val additionalFee: Double
        get() {
            val taxableAmount = (subtotal - discountAmount).coerceAtLeast(0.0)
            return (taxableAmount * _taxOrFeePercent.value) / 100.0
        }

    val totalAmount: Double
        get() = (subtotal - discountAmount + additionalFee).coerceAtLeast(0.0)

    val amountPaid: Double
        get() = _amountPaidInput.value.toDoubleOrNull() ?: 0.0

    val changeAmount: Double
        get() = if (paymentMethod.value == PaymentMethods.CASH) {
            (amountPaid - totalAmount).coerceAtLeast(0.0)
        } else {
            0.0
        }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun toggleRole() {
        val newRole = if (_currentUser.value.role == Roles.ADMIN) Roles.KASIR else Roles.ADMIN
        _currentUser.value = _currentUser.value.copy(
            role = newRole,
            fullName = if (newRole == Roles.ADMIN) "DIMZONE Admin" else "DIMZONE Kasir"
        )
    }

    // Cart Operations
    fun addToCart(product: ProductEntity) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            if (existing.quantity + 1 > product.stock) {
                _errorMessage.value = "Stok ${product.name} tidak mencukupi (Tersedia: ${product.stock})"
                return
            }
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            if (product.stock < 1) {
                _errorMessage.value = "Stok ${product.name} habis!"
                return
            }
            currentList.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = currentList
        // Auto update cash amount if cash
        if (_paymentMethod.value != PaymentMethods.CASH) {
            _amountPaidInput.value = totalAmount.toLong().toString()
        }
    }

    fun decrementQuantity(productId: Long) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            if (item.quantity > 1) {
                currentList[index] = item.copy(quantity = item.quantity - 1)
            } else {
                currentList.removeAt(index)
            }
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun updateCartItemNote(productId: Long, note: String) {
        _cartItems.value = _cartItems.value.map {
            if (it.product.id == productId) it.copy(note = note) else it
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountPercent.value = 0.0
        _manualDiscount.value = 0.0
        _taxOrFeePercent.value = 0.0
        _orderNotes.value = ""
        _customerName.value = "Pelanggan"
        _amountPaidInput.value = ""
    }

    fun setDiscountPercent(percent: Double) {
        _discountPercent.value = percent
    }

    fun setManualDiscount(amount: Double) {
        _manualDiscount.value = amount
    }

    fun setTaxOrFeePercent(percent: Double) {
        _taxOrFeePercent.value = percent
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
        if (method != PaymentMethods.CASH) {
            _amountPaidInput.value = totalAmount.toLong().toString()
        }
    }

    fun setAmountPaidInput(input: String) {
        _amountPaidInput.value = input.filter { it.isDigit() }
    }

    fun setExactCash() {
        _amountPaidInput.value = totalAmount.toLong().toString()
    }

    fun setQuickCash(amount: Double) {
        _amountPaidInput.value = amount.toLong().toString()
    }

    fun setOrderNotes(notes: String) {
        _orderNotes.value = notes
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun closeReceiptDialog() {
        _showReceiptDialog.value = false
    }

    fun showReceiptFor(transaction: TransactionEntity, items: List<TransactionItemEntity>) {
        _lastCompletedTransaction.value = transaction
        _lastCompletedItems.value = items
        _showReceiptDialog.value = true
    }

    fun processPayment() {
        if (_cartItems.value.isEmpty()) {
            _errorMessage.value = "Keranjang belanja masih kosong!"
            return
        }

        val total = totalAmount
        val paid = if (_paymentMethod.value == PaymentMethods.CASH) amountPaid else total

        if (_paymentMethod.value == PaymentMethods.CASH && paid < total) {
            _errorMessage.value = "Uang pembayaran kurang! (Kurang: ${Formatters.formatRupiah(total - paid)})"
            return
        }

        viewModelScope.launch {
            val invoiceNumber = Formatters.generateInvoiceNumber()
            val user = _currentUser.value

            val transaction = TransactionEntity(
                invoiceNumber = invoiceNumber,
                cashierId = user.id,
                cashierName = user.fullName,
                customerName = _customerName.value.ifBlank { "Pelanggan" },
                subtotal = subtotal,
                discountAmount = discountAmount,
                discountPercent = _discountPercent.value,
                additionalFee = additionalFee,
                totalAmount = total,
                paymentMethod = _paymentMethod.value,
                amountPaid = paid,
                changeAmount = if (_paymentMethod.value == PaymentMethods.CASH) (paid - total).coerceAtLeast(0.0) else 0.0,
                notes = _orderNotes.value,
                status = TransactionStatuses.COMPLETED,
                timestamp = System.currentTimeMillis()
            )

            val items = _cartItems.value.map {
                TransactionItemEntity(
                    transactionId = 0,
                    productId = it.product.id,
                    productName = it.product.name,
                    productCategory = it.product.category,
                    price = it.product.price,
                    quantity = it.quantity,
                    subtotal = it.subtotal,
                    itemNote = it.note
                )
            }

            val result = repository.processTransaction(transaction, items)
            result.onSuccess { txId ->
                val finalTx = transaction.copy(id = txId)
                _lastCompletedTransaction.value = finalTx
                _lastCompletedItems.value = items.map { it.copy(transactionId = txId) }
                _showReceiptDialog.value = true
                _successMessage.value = "Transaksi #${finalTx.invoiceNumber} berhasil disimpan!"
                clearCart()
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Gagal memproses pembayaran"
            }
        }
    }

    // Cancel Transaction (with Stock Rollback)
    fun cancelTransaction(transactionId: Long) {
        viewModelScope.launch {
            val result = repository.cancelTransaction(transactionId, _currentUser.value.fullName)
            result.onSuccess {
                _successMessage.value = "Transaksi berhasil dibatalkan dan stok dikembalikan."
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Gagal membatalkan transaksi"
            }
        }
    }

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItemEntity> {
        return repository.getTransactionItems(transactionId)
    }

    // Product Management (Admin)
    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.addProduct(product)
            _successMessage.value = "Produk ${product.name} berhasil ditambahkan"
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _successMessage.value = "Produk ${product.name} berhasil diperbarui"
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _successMessage.value = "Produk ${product.name} berhasil dihapus"
        }
    }

    fun toggleProductActive(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(isActive = !product.isActive))
        }
    }

    // Stock Management
    fun restockProduct(productId: Long, addedStock: Int, reason: String, notes: String) {
        viewModelScope.launch {
            val ok = repository.restockProduct(
                productId = productId,
                addedStock = addedStock,
                reasonType = reason,
                notes = notes,
                performedBy = _currentUser.value.fullName
            )
            if (ok) {
                _successMessage.value = "Stok berhasil disesuaikan ($addedStock unit)"
            } else {
                _errorMessage.value = "Gagal memperbarui stok"
            }
        }
    }

    // Reports filter
    fun setReportDateFilter(filter: ReportDateFilter) {
        _reportDateFilter.value = filter
    }

    fun isDateMatchingFilter(timestamp: Long, filter: ReportDateFilter): Boolean {
        val now = Calendar.getInstance()
        val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when (filter) {
            ReportDateFilter.TODAY -> {
                now.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR)
            }
            ReportDateFilter.YESTERDAY -> {
                val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                yesterday.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                        yesterday.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR)
            }
            ReportDateFilter.LAST_7_DAYS -> {
                val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                timestamp >= sevenDaysAgo.timeInMillis
            }
            ReportDateFilter.THIS_MONTH -> {
                now.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == txCal.get(Calendar.MONTH)
            }
            ReportDateFilter.ALL -> true
        }
    }
}
