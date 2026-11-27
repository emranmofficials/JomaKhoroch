package com.example.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.repository.PurchaseLineItemInput
import com.example.domain.accounting.LedgerTransactionType
import com.example.presentation.components.PaymentMethodSelector
import com.example.presentation.components.TransactionRowCard
import com.example.presentation.state.ShopWithBalance
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShopSheet(
    existing: com.example.data.local.entity.ShopEntity? = null,
    onSave: (
        existingId: Long,
        name: String,
        owner: String,
        phone: String,
        address: String,
        category: String,
        openingPoisha: Long,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val defaultShopCategory = if (language == AppLanguage.BANGLA) "মুদি" else "Grocery"
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var owner by remember(existing) { mutableStateOf(existing?.ownerName ?: "") }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }
    var shopCategory by remember(existing, language) {
        val raw = existing?.shopCategory?.trim().orEmpty()
        mutableStateOf(
            when {
                raw.isBlank() || raw == "মুদি / Grocery" -> defaultShopCategory
                else -> raw
            }
        )
    }
    var openingText by remember(existing) {
        mutableStateOf(existing?.let { CurrencyFormatter.poishaToPlainInput(it.openingBalancePoisha) } ?: "")
    }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (existing == null) strings.addShop else "${strings.edit}: ${existing.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMsg = null },
                label = { Text(strings.shopName) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_name_input")
            )
            OutlinedTextField(
                value = owner,
                onValueChange = { owner = it },
                label = { Text(strings.ownerName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.phoneNumber) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = shopCategory,
                    onValueChange = { shopCategory = it },
                    label = { Text(strings.shopCategory) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(strings.address) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = openingText,
                onValueChange = { openingText = it; errorMsg = null },
                label = { Text("${strings.openingBalance} (৳)") },
                prefix = { Text("৳ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notes) },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = strings.requiredFieldError
                        return@Button
                    }
                    val opening = if (openingText.isBlank()) 0L else CurrencyFormatter.parseTakaToPoisha(openingText)
                    if (opening == null || opening < 0L) {
                        errorMsg = strings.invalidAmountError
                        return@Button
                    }
                    onSave(existing?.id ?: 0L, name, owner, phone, address, shopCategory, opening, notes)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_shop_button")
            ) {
                Text(strings.save)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopPurchaseSheet(
    shops: List<ShopWithBalance>,
    initialShopId: Long?,
    expenseCategories: List<CategoryEntity>,
    onSave: (
        shopId: Long,
        totalPoisha: Long,
        paidPoisha: Long,
        categoryId: Long?,
        paymentMethod: String,
        note: String,
        dateMillis: Long,
        items: List<PurchaseLineItemInput>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current

    var selectedShopId by remember { mutableStateOf(initialShopId ?: shops.firstOrNull()?.shop?.id) }
    var isItemized by remember { mutableStateOf(false) }
    var quickAmountText by remember { mutableStateOf("") }
    var paidAmountText by remember { mutableStateOf("0") }
    var noteText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    val dateMillis = remember { System.currentTimeMillis() }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Item-based entry fields
    val lineItems = remember { mutableStateListOf<PurchaseLineItemInput>() }
    var itemName by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemUnit by remember { mutableStateOf("kg") }
    var itemUnitPrice by remember { mutableStateOf("") }

    val computedTotalPoisha = if (isItemized) {
        lineItems.sumOf { it.lineTotalPoisha }
    } else {
        CurrencyFormatter.parseTakaToPoisha(quickAmountText) ?: 0L
    }
    val parsedPaidPoisha = CurrencyFormatter.parseTakaToPoisha(paidAmountText.ifBlank { "0" }) ?: 0L
    val remainingDuePoisha = (computedTotalPoisha - parsedPaidPoisha).coerceAtLeast(0L)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.addPurchase, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Scrollable shop selector supporting unlimited shops
            if (shops.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shops, key = { it.shop.id }) { s ->
                        FilterChip(
                            selected = selectedShopId == s.shop.id,
                            onClick = { selectedShopId = s.shop.id },
                            label = { Text(s.shop.name) }
                        )
                    }
                }
            }

            // Method A (Quick Amount) vs Method B (Item-Based)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !isItemized,
                    onClick = { isItemized = false },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text(strings.quickEntry) }
                SegmentedButton(
                    selected = isItemized,
                    onClick = { isItemized = true },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text(strings.itemEntry) }
            }

            if (!isItemized) {
                OutlinedTextField(
                    value = quickAmountText,
                    onValueChange = { quickAmountText = it; errorMsg = null },
                    label = { Text(strings.totalPrice) },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_quick_total_input")
                )
            } else {
                // Add product row
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text(strings.productName) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = itemQty,
                                onValueChange = { itemQty = it },
                                label = { Text(strings.quantity) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = itemUnit,
                                onValueChange = { itemUnit = it },
                                label = { Text(strings.unit) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = itemUnitPrice,
                                onValueChange = { itemUnitPrice = it },
                                label = { Text(strings.unitPrice) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val qtyMillis = CurrencyFormatter.parseQuantityToMillis(itemQty)
                                val unitPrice = CurrencyFormatter.parseTakaToPoisha(itemUnitPrice)
                                if (itemName.isBlank() || qtyMillis == null || unitPrice == null || unitPrice <= 0L) {
                                    errorMsg = strings.invalidAmountError
                                    return@OutlinedButton
                                }
                                val lineTotal = CurrencyFormatter.calculateItemTotalPoisha(qtyMillis, unitPrice)
                                lineItems.add(
                                    PurchaseLineItemInput(
                                        productName = itemName.trim(),
                                        quantityMillis = qtyMillis,
                                        unit = itemUnit.trim().ifEmpty { "pcs" },
                                        unitPricePoisha = unitPrice,
                                        lineTotalPoisha = lineTotal
                                    )
                                )
                                itemName = ""
                                itemQty = "1"
                                itemUnitPrice = ""
                                errorMsg = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.addItem)
                        }
                    }
                }

                lineItems.forEachIndexed { idx, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(line.productName, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${CurrencyFormatter.formatQuantityMillis(line.quantityMillis, numerals)} ${line.unit} × ${
                                    CurrencyFormatter.formatPoisha(line.unitPricePoisha, numerals)
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatPoisha(line.lineTotalPoisha, numerals),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { lineItems.removeAt(idx) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove item", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = paidAmountText,
                onValueChange = { paidAmountText = it; errorMsg = null },
                label = { Text(strings.paidAmount) },
                prefix = { Text("৳ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_paid_input")
            )

            // Live Breakdown Box (Total / Paid / Remaining Due)
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(strings.totalPrice, style = MaterialTheme.typography.labelSmall)
                        Text(
                            CurrencyFormatter.formatPoisha(computedTotalPoisha, numerals),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(strings.remainingDue, style = MaterialTheme.typography.labelSmall, color = finance.payable)
                        Text(
                            CurrencyFormatter.formatPoisha(remainingDuePoisha, numerals),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold),
                            color = finance.payable
                        )
                    }
                }
            }

            PaymentMethodSelector(selected = paymentMethod, onSelect = { paymentMethod = it })

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(strings.description) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    val targetShopId = selectedShopId ?: run {
                        errorMsg = strings.requiredFieldError
                        return@Button
                    }
                    if (computedTotalPoisha <= 0L) {
                        errorMsg = strings.invalidAmountError
                        return@Button
                    }
                    val paid = CurrencyFormatter.parseTakaToPoisha(paidAmountText.ifBlank { "0" })
                    if (paid == null || paid < 0L) {
                        errorMsg = strings.invalidAmountError
                        return@Button
                    }
                    if (paid > computedTotalPoisha) {
                        errorMsg = strings.paidExceedsTotalError
                        return@Button
                    }
                    val defaultGroceryCat = expenseCategories.firstOrNull { it.nameEn == "Grocery" }?.id
                        ?: expenseCategories.firstOrNull()?.id
                    onSave(
                        targetShopId,
                        computedTotalPoisha,
                        paid,
                        defaultGroceryCat,
                        paymentMethod,
                        noteText,
                        dateMillis,
                        if (isItemized) lineItems.toList() else emptyList()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_shop_purchase_button")
            ) {
                Text(strings.save)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopPaymentSheet(
    shopsWithDue: List<ShopWithBalance>,
    initialShop: ShopWithBalance?,
    onPay: (shopId: Long, paymentPoisha: Long, paymentMethod: String, note: String, dateMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current

    var selected by remember { mutableStateOf(initialShop ?: shopsWithDue.firstOrNull()) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.recordPayment, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            if (shopsWithDue.isEmpty() && selected == null) {
                Text(
                    text = "${strings.settled} — ${strings.noShopsYet}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                if (initialShop == null && shopsWithDue.size > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(shopsWithDue, key = { it.shop.id }) { s ->
                            FilterChip(
                                selected = selected?.shop?.id == s.shop.id,
                                onClick = { selected = s },
                                label = { Text(s.shop.name) }
                            )
                        }
                    }
                }
                val currentShop = selected
                if (currentShop != null) {
                    val currentDue = currentShop.balance.outstandingDuePoisha
                    Surface(
                        color = finance.payableContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(currentShop.shop.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${strings.outstandingDue}: ${CurrencyFormatter.formatPoisha(currentDue, numerals)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = finance.payable
                                )
                            }
                            TextButton(onClick = {
                                amountText = CurrencyFormatter.poishaToPlainInput(currentDue)
                                errorMsg = null
                            }) {
                                Text(if (LocalLanguage.current == AppLanguage.BANGLA) "সম্পূর্ণ" else "Full")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; errorMsg = null },
                        label = { Text(strings.amount) },
                        prefix = { Text("৳ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_payment_amount_input")
                    )

                    PaymentMethodSelector(selected = paymentMethod, onSelect = { paymentMethod = it })

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(strings.description) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

                    Button(
                        onClick = {
                            val payPoisha = CurrencyFormatter.parseTakaToPoisha(amountText)
                            if (payPoisha == null || payPoisha <= 0L) {
                                errorMsg = strings.invalidAmountError
                                return@Button
                            }
                            if (payPoisha > currentDue) {
                                errorMsg = strings.paymentExceedsDueError
                                return@Button
                            }
                            onPay(currentShop.shop.id, payPoisha, paymentMethod, noteText, System.currentTimeMillis())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_shop_payment_button")
                    ) {
                        Text(strings.save)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopStatementSheet(
    shopWithBalance: ShopWithBalance,
    allTransactions: List<LedgerTransactionEntity>,
    onEditShop: () -> Unit,
    onAddPurchase: () -> Unit,
    onRecordPayment: () -> Unit,
    onTransactionClick: (LedgerTransactionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val shopTxs = remember(allTransactions, shopWithBalance.shop.id) {
        allTransactions.filter { it.shopId == shopWithBalance.shop.id }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(shopWithBalance.shop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(strings.shopStatement, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEditShop) {
                    Icon(Icons.Filled.Edit, contentDescription = strings.edit)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatementRow(strings.openingBalance, shopWithBalance.balance.openingBalancePoisha, numerals)
                    StatementRow(strings.addPurchase, shopWithBalance.balance.totalPurchasedPoisha, numerals)
                    StatementRow(
                        strings.recordPayment,
                        shopWithBalance.balance.totalPaidAtPurchasePoisha + shopWithBalance.balance.totalDuePaymentsPoisha,
                        numerals
                    )
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.outstandingDue, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            CurrencyFormatter.formatPoisha(shopWithBalance.balance.outstandingDuePoisha, numerals),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold),
                            color = if (shopWithBalance.balance.outstandingDuePoisha > 0L) finance.payable else finance.income
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAddPurchase, modifier = Modifier.weight(1f)) {
                    Text(strings.addPurchase)
                }
                Button(
                    onClick = onRecordPayment,
                    enabled = shopWithBalance.balance.outstandingDuePoisha > 0L,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.recordPayment)
                }
            }

            shopTxs.forEach { tx ->
                TransactionRowCard(
                    tx = tx,
                    category = null,
                    shop = shopWithBalance.shop,
                    onClick = { onTransactionClick(tx) }
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatementRow(label: String, poisha: Long, numerals: com.example.core.localization.NumeralStyle) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            CurrencyFormatter.formatPoisha(poisha, numerals),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.SemiBold)
        )
    }
}
