package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CartItem
import com.example.data.model.Categories
import com.example.data.model.PaymentMethods
import com.example.data.model.ProductEntity
import com.example.ui.theme.DimzoneAmber
import com.example.ui.theme.DimzoneGold
import com.example.ui.theme.DimzoneGreen
import com.example.ui.theme.DimzoneRedPrimary
import com.example.ui.viewmodel.PosViewModel
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosCashierScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showMobileCartSheet by remember { mutableStateOf(false) }
    var itemForNoteDialog by remember { mutableStateOf<CartItem?>(null) }
    var showDiscountDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("pos_cashier_screen")
    ) {
        val isWideScreen = maxWidth >= 800.dp

        if (isWideScreen) {
            // Tablet / Desktop layout: 2 panes side-by-side
            Row(modifier = Modifier.fillMaxSize()) {
                // Left pane: Search, Categories, Product Catalog (60-65% width)
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    TopSearchAndFilterBar(
                        searchQuery = searchQuery,
                        onSearchChange = viewModel::setSearchQuery,
                        selectedCategory = selectedCategory,
                        onCategorySelect = viewModel::setCategory
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ProductCatalogGrid(
                        products = products,
                        cartItems = cartItems,
                        onAddToCart = viewModel::addToCart,
                        onDecrement = viewModel::decrementQuantity,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Right pane: Sticky Cart & Payment Checkout Panel
                Surface(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CartContent(
                        cartItems = cartItems,
                        viewModel = viewModel,
                        onEditNote = { itemForNoteDialog = it },
                        onOpenDiscount = { showDiscountDialog = true }
                    )
                }
            }
        } else {
            // Mobile Phone Layout: Product Catalog + Floating Bottom Checkout Bar
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = if (cartItems.isNotEmpty()) 80.dp else 8.dp)
                ) {
                    TopSearchAndFilterBar(
                        searchQuery = searchQuery,
                        onSearchChange = viewModel::setSearchQuery,
                        selectedCategory = selectedCategory,
                        onCategorySelect = viewModel::setCategory
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ProductCatalogGrid(
                        products = products,
                        cartItems = cartItems,
                        onAddToCart = viewModel::addToCart,
                        onDecrement = viewModel::decrementQuantity,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Floating Mobile Cart Summary Bar
                if (cartItems.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = DimzoneRedPrimary,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMobileCartSheet = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("open_cart_sheet_bar"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = DimzoneGold, contentColor = Color.Black) {
                                            Text(cartItems.sumOf { it.quantity }.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Keranjang",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${cartItems.sumOf { it.quantity }} Dimsum Dipilih",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(viewModel.totalAmount),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { showMobileCartSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DimzoneRedPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Bayar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Note Editor Dialog
    itemForNoteDialog?.let { item ->
        var noteText by remember { mutableStateOf(item.note) }
        AlertDialog(
            onDismissRequest = { itemForNoteDialog = null },
            title = { Text("Catatan untuk ${item.product.name}") },
            text = {
                Column {
                    Text(
                        "Contoh: Saos sambal pisah, bakar agak gosong, tanpa nori, dll.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxWidth().testTag("note_input"),
                        placeholder = { Text("Tulis catatan khusus...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCartItemNote(item.product.id, noteText)
                        itemForNoteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DimzoneRedPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForNoteDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Discount Dialog
    if (showDiscountDialog) {
        val currentPercent by viewModel.discountPercent.collectAsStateWithLifecycle()
        var customNominal by remember { mutableStateOf(viewModel.manualDiscount.value.toLong().toString().takeIf { it != "0" } ?: "") }

        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text("Atur Diskon Pesanan", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pilih Persentase Diskon:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.0, 5.0, 10.0, 15.0).forEach { pct ->
                            FilterChip(
                                selected = currentPercent == pct && customNominal.isBlank(),
                                onClick = {
                                    viewModel.setDiscountPercent(pct)
                                    viewModel.setManualDiscount(0.0)
                                    customNominal = ""
                                },
                                label = { Text(if (pct == 0.0) "0%" else "${pct.toInt()}%") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Atau Masukkan Diskon Nominal (Rp):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customNominal,
                        onValueChange = {
                            customNominal = it.filter { ch -> ch.isDigit() }
                            val num = customNominal.toDoubleOrNull() ?: 0.0
                            viewModel.setManualDiscount(num)
                            if (num > 0) viewModel.setDiscountPercent(0.0)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Contoh: 5000") },
                        modifier = Modifier.fillMaxWidth().testTag("discount_nominal_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDiscountDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DimzoneRedPrimary)
                ) {
                    Text("Selesai")
                }
            }
        )
    }

    // Mobile Bottom Sheet for Cart Checkout
    if (showMobileCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMobileCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.testTag("mobile_cart_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keranjang & Pembayaran",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DimzoneRedPrimary
                    )
                    IconButton(onClick = { showMobileCartSheet = false }) {
                        Icon(Icons.Default.Clear, contentDescription = "Tutup")
                    }
                }

                CartContent(
                    cartItems = cartItems,
                    viewModel = viewModel,
                    onEditNote = { itemForNoteDialog = it },
                    onOpenDiscount = { showDiscountDialog = true },
                    onPaymentCompleted = { showMobileCartSheet = false }
                )
            }
        }
    }
}

@Composable
fun TopSearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search TextField with Keyboard Actions
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("product_search_input"),
            placeholder = { Text("Cari dimsum favorit (Ori, Mentai, Chili Oil, dll)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = DimzoneRedPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Categories.LIST.forEach { category ->
                val isSelected = category.equals(selectedCategory, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = {
                        Text(
                            text = category,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DimzoneRedPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }
    }
}

@Composable
fun ProductCatalogGrid(
    products: List<ProductEntity>,
    cartItems: List<CartItem>,
    onAddToCart: (ProductEntity) -> Unit,
    onDecrement: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tidak ada produk yang cocok",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = modifier.fillMaxSize().testTag("product_grid"),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products, key = { it.id }) { product ->
                val inCartItem = cartItems.find { it.product.id == product.id }
                ProductCard(
                    product = product,
                    inCartQuantity = inCartItem?.quantity ?: 0,
                    onAddToCart = { onAddToCart(product) },
                    onDecrement = { onDecrement(product.id) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    inCartQuantity: Int,
    onAddToCart: () -> Unit,
    onDecrement: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isLowStock = product.stock in 1..product.minStockAlert

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock) { onAddToCart() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (inCartQuantity > 0) Color(0xFFFFF8F7) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF0ECE9)),
                contentAlignment = Alignment.Center
            ) {
                // Dimsum Banner / Image
                Image(
                    painter = painterResource(id = R.drawable.img_dimsum_banner),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Stock Badge Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    when {
                        isOutOfStock -> {
                            Surface(
                                color = DimzoneRedPrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "HABIS",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        isLowStock -> {
                            Surface(
                                color = DimzoneAmber,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Sisa ${product.stock}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        else -> {
                            Surface(
                                color = Color(0xCC000000),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Stok ${product.stock}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // In-Cart Badge overlay
                if (inCartQuantity > 0) {
                    Surface(
                        color = DimzoneRedPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = inCartQuantity.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Info Section
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Formatters.formatRupiah(product.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DimzoneRedPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fast +/- Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (inCartQuantity > 0) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFEEEEEE), CircleShape)
                                .testTag("btn_minus_${product.id}")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = inCartQuantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = onAddToCart,
                        enabled = !isOutOfStock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (inCartQuantity > 0) DimzoneRedPrimary else DimzoneAmber
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_add_${product.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                        if (inCartQuantity == 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pesan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartContent(
    cartItems: List<CartItem>,
    viewModel: PosViewModel,
    onEditNote: (CartItem) -> Unit,
    onOpenDiscount: () -> Unit,
    onPaymentCompleted: () -> Unit = {}
) {
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val discountPercent by viewModel.discountPercent.collectAsStateWithLifecycle()
    val manualDiscount by viewModel.manualDiscount.collectAsStateWithLifecycle()
    val taxOrFeePercent by viewModel.taxOrFeePercent.collectAsStateWithLifecycle()
    val amountPaidInput by viewModel.amountPaidInput.collectAsStateWithLifecycle()
    val customerName by viewModel.customerName.collectAsStateWithLifecycle()
    val orderNotes by viewModel.orderNotes.collectAsStateWithLifecycle()

    val subtotal = viewModel.subtotal
    val discount = viewModel.discountAmount
    val additionalFee = viewModel.additionalFee
    val total = viewModel.totalAmount
    val paid = viewModel.amountPaid
    val change = viewModel.changeAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("cart_content_panel")
    ) {
        // Cart Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = DimzoneRedPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pesanan (${cartItems.sumOf { it.quantity }})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (cartItems.isNotEmpty()) {
                TextButton(
                    onClick = viewModel::clearCart,
                    modifier = Modifier.testTag("clear_cart_button")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Batal", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color(0xFFEEEEEE))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada dimsum dipilih", color = Color.Gray)
                    Text("Sentuh menu di kiri untuk menambah", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        } else {
            // Scrollable Section: Cart Items + Details + Payment Method + Quick Cash
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cart Items List
                cartItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${Formatters.formatRupiah(item.product.price)} x ${item.quantity}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = Formatters.formatRupiah(item.subtotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DimzoneRedPrimary
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Fast +/- row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.decrementQuantity(item.product.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
                                    }
                                    Text(text = item.quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    IconButton(
                                        onClick = { viewModel.addToCart(item.product) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // Note Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = "Catatan: ${item.note}",
                                        fontSize = 11.sp,
                                        color = DimzoneAmber,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                TextButton(
                                    onClick = { onEditNote(item) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = DimzoneAmber)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(if (item.note.isBlank()) "+ Catatan" else "Ubah", fontSize = 11.sp, color = DimzoneAmber)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Name Input
                OutlinedTextField(
                    value = customerName,
                    onValueChange = viewModel::setCustomerName,
                    label = { Text("Nama Pelanggan / No. Meja") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Discount and PB1 tax buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenDiscount,
                        modifier = Modifier.weight(1f).testTag("btn_open_discount")
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (discount > 0) "Diskon: -${Formatters.formatRupiah(discount)}" else "+ Diskon",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val isTaxActive = taxOrFeePercent > 0
                    FilterChip(
                        selected = isTaxActive,
                        onClick = { viewModel.setTaxOrFeePercent(if (isTaxActive) 0.0 else 10.0) },
                        label = { Text("Pajak PB1 10%", fontSize = 11.sp) },
                        modifier = Modifier.testTag("pb1_tax_chip")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Method Selector
                Text("Metode Pembayaran:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethods.LIST.forEach { method ->
                        val isSelected = paymentMethod == method
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setPaymentMethod(method) },
                            label = { Text(method, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DimzoneRedPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).testTag("payment_chip_$method")
                        )
                    }
                }

                // If CASH: Quick Cash buttons & Input
                if (paymentMethod == PaymentMethods.CASH) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Uang Diterima:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = amountPaidInput,
                        onValueChange = viewModel::setAmountPaidInput,
                        placeholder = { Text("Masukkan nominal rupiah...") },
                        leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, color = DimzoneRedPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("cash_paid_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Cash Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::setExactCash,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp).testTag("quick_cash_exact")
                        ) {
                            Text("Uang Pas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        listOf(20000.0, 50000.0, 100000.0, 200000.0).forEach { cashNominal ->
                            OutlinedButton(
                                onClick = { viewModel.setQuickCash(cashNominal) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp).testTag("quick_cash_${cashNominal.toLong()}")
                            ) {
                                Text(Formatters.formatRupiah(cashNominal), fontSize = 11.sp)
                            }
                        }
                    }

                    // Kembalian Highlight
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (paid >= total) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (paid >= total) "Kembalian:" else "Kurang Bayar:",
                            fontWeight = FontWeight.Bold,
                            color = if (paid >= total) DimzoneGreen else DimzoneRedPrimary
                        )
                        Text(
                            text = if (paid >= total) Formatters.formatRupiah(change) else Formatters.formatRupiah(total - paid),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (paid >= total) DimzoneGreen else DimzoneRedPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Bottom Summary & "BAYAR" action
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(8.dp))

            // Subtotal, Discount, Fee breakdown
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = Color.Gray, fontSize = 12.sp)
                Text(Formatters.formatRupiah(subtotal), fontSize = 12.sp)
            }
            if (discount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Diskon", color = DimzoneRedPrimary, fontSize = 12.sp)
                    Text("-${Formatters.formatRupiah(discount)}", color = DimzoneRedPrimary, fontSize = 12.sp)
                }
            }
            if (additionalFee > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pajak PB1 (10%)", color = Color.Gray, fontSize = 12.sp)
                    Text("+${Formatters.formatRupiah(additionalFee)}", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TOTAL BAYAR", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(
                    text = Formatters.formatRupiah(total),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = DimzoneRedPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val canPay = when (paymentMethod) {
                PaymentMethods.CASH -> paid >= total && total > 0
                else -> total > 0
            }

            Button(
                onClick = {
                    viewModel.processPayment()
                    onPaymentCompleted()
                },
                enabled = canPay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DimzoneRedPrimary,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_bayar_checkout")
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (paymentMethod == PaymentMethods.CASH && paid < total && total > 0) {
                        "UANG KURANG (${Formatters.formatRupiah(total - paid)})"
                    } else {
                        "BAYAR SEKARANG (${Formatters.formatRupiah(total)})"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
