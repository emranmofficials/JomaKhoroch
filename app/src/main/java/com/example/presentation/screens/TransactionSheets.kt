package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.TransactionItemEntity
import com.example.domain.accounting.LedgerTransactionType
import com.example.presentation.components.PaymentMethodSelector
import com.example.presentation.components.iconForKey
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IncomeExpenseEntrySheet(
    initialType: LedgerTransactionType,
    categories: List<CategoryEntity>,
    onSave: (
        type: LedgerTransactionType,
        amountPoisha: Long,
        categoryId: Long?,
        paymentMethod: String,
        note: String,
        dateMillis: Long
    ) -> Unit,
    onAddCustomCategory: (nameBn: String, nameEn: String, type: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val isBn = language == AppLanguage.BANGLA

    var selectedType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    val todayMillis = remember { System.currentTimeMillis() }
    val yesterdayMillis = remember(todayMillis) { todayMillis - 86_400_000L }
    var dateMillis by remember { mutableLongStateOf(todayMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType.name }
    }
    var selectedCategoryId by remember(selectedType, filteredCategories) {
        mutableStateOf(filteredCategories.firstOrNull()?.id)
    }

    val isIncome = selectedType == LedgerTransactionType.INCOME
    val accentColor = if (isIncome) finance.income else finance.expense
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
            Text(
                text = if (isIncome) strings.addIncome else strings.addExpense,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedType == LedgerTransactionType.EXPENSE,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedType = LedgerTransactionType.EXPENSE
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text(strings.expenseLabel) }
                SegmentedButton(
                    selected = selectedType == LedgerTransactionType.INCOME,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedType = LedgerTransactionType.INCOME
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text(strings.incomeLabel) }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    errorText = null
                },
                label = { Text(strings.amount) },
                prefix = { Text("৳ ", fontWeight = FontWeight.Bold, color = accentColor) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                isError = errorText != null,
                supportingText = errorText?.let { err -> { Text(err) } },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_amount_input")
            )

            // 1-tap quick amount preset chips for ultra-fast entry
            val amountPresets = remember { listOf(100L, 500L, 1000L, 2000L, 5000L) }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(amountPresets, key = { it }) { preset ->
                    val label = "+৳${CurrencyFormatter.toLocalizedDigits(preset.toString(), numerals)}"
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val currentTaka = (CurrencyFormatter.parseTakaToPoisha(amountText) ?: 0L) / 100L
                            amountText = (currentTaka + preset).toString()
                            errorText = null
                        },
                        shape = RoundedCornerShape(50),
                        color = if (isIncome) finance.incomeContainer else finance.expenseContainer,
                        border = BorderStroke(1.dp, if (isIncome) finance.incomeBorder else finance.expenseBorder)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFont,
                                fontWeight = FontWeight.Bold
                            ),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Category chips (one-tap selection for <10-second flow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.category, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.addCustomCategory)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredCategories.forEach { cat ->
                    val label = if (language == AppLanguage.BANGLA) cat.nameBn else cat.nameEn
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategoryId = cat.id
                        },
                        label = { Text(label) },
                        leadingIcon = {
                            Icon(iconForKey(cat.iconKey), contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            Text(strings.paymentMethod, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            PaymentMethodSelector(selected = paymentMethod, onSelect = { paymentMethod = it })

            // 1-tap Today / Yesterday / Pick Date quick row
            val isToday = DateUtils.isSameDay(dateMillis, todayMillis)
            val isYesterday = DateUtils.isSameDay(dateMillis, yesterdayMillis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = isToday,
                    onClick = { dateMillis = todayMillis },
                    label = { Text(if (isBn) "আজ" else "Today") }
                )
                FilterChip(
                    selected = isYesterday,
                    onClick = { dateMillis = yesterdayMillis },
                    label = { Text(if (isBn) "গতকাল" else "Yesterday") }
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = DateUtils.formatDate(dateMillis, language, numerals),
                        maxLines = 1
                    )
                }
            }

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(strings.description) },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_note_input")
            )

            Button(
                onClick = {
                    val poisha = CurrencyFormatter.parseTakaToPoisha(amountText)
                    if (poisha == null || poisha <= 0L) {
                        errorText = strings.invalidAmountError
                        return@Button
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave(selectedType, poisha, selectedCategoryId, paymentMethod, noteText, dateMillis)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (finance.isDark) {
                        if (selectedType == LedgerTransactionType.INCOME) Color(0xFF137A4D) else Color(0xFFC43434)
                    } else accentColor,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("tx_save_button")
            ) {
                Text(strings.save, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = selectedType.name,
            onConfirm = { bn, en ->
                onAddCustomCategory(bn, en, selectedType.name)
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }
}

@Composable
fun AddCategoryDialog(
    type: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isBn = LocalLanguage.current == AppLanguage.BANGLA
    var nameBn by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addCustomCategory) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(if (isBn) "বাংলা নাম" else "Bangla Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (isBn) "ইংরেজি নাম" else "English Name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = nameBn.isNotBlank() || nameEn.isNotBlank(),
                onClick = { onConfirm(nameBn, nameEn) }
            ) { Text(strings.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailEditSheet(
    tx: LedgerTransactionEntity,
    itemsFlow: Flow<List<TransactionItemEntity>>,
    categories: List<CategoryEntity>,
    onUpdate: (
        newTotalPoisha: Long,
        newPaidPoisha: Long,
        newNote: String,
        newCategoryId: Long?,
        newPaymentMethod: String,
        newDate: Long,
        reason: String
    ) -> Unit,
    onDelete: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val lineItems by itemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val txType = LedgerTransactionType.valueOf(tx.type)

    var totalText by remember { mutableStateOf(CurrencyFormatter.poishaToPlainInput(tx.totalAmountPoisha)) }
    var paidText by remember { mutableStateOf(CurrencyFormatter.poishaToPlainInput(tx.paidAmountPoisha)) }
    var noteText by remember { mutableStateOf(tx.note) }
    var reasonText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(tx.paymentMethod) }
    var selectedCategoryId by remember { mutableStateOf(tx.categoryId) }
    var selectedDateMillis by remember { mutableLongStateOf(tx.transactionDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val matchingCategories = remember(categories, txType) {
        when (txType) {
            LedgerTransactionType.INCOME -> categories.filter { it.type == "INCOME" }
            LedgerTransactionType.EXPENSE, LedgerTransactionType.SHOP_PURCHASE -> categories.filter { it.type == "EXPENSE" }
            else -> emptyList()
        }
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
            Text(strings.editTransaction, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("${strings.date}: ${DateUtils.formatDate(selectedDateMillis, language, numerals)}")
            }

            if (lineItems.isNotEmpty()) {
                HorizontalDivider()
                Text(strings.itemEntry, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                lineItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.productName} (${CurrencyFormatter.formatQuantityMillis(item.quantityMillis, numerals)} ${item.unit})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = CurrencyFormatter.formatPoisha(item.lineTotalPoisha, numerals),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                HorizontalDivider()
            }

            OutlinedTextField(
                value = totalText,
                onValueChange = { totalText = it; errorMsg = null },
                label = { Text(if (txType == LedgerTransactionType.SHOP_PURCHASE) strings.totalPrice else strings.amount) },
                prefix = { Text("৳ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (txType == LedgerTransactionType.SHOP_PURCHASE) {
                OutlinedTextField(
                    value = paidText,
                    onValueChange = { paidText = it; errorMsg = null },
                    label = { Text(strings.paidAmount) },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (matchingCategories.isNotEmpty()) {
                Text(strings.category, style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    matchingCategories.forEach { cat ->
                        val label = if (language == AppLanguage.BANGLA) cat.nameBn else cat.nameEn
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(label) }
                        )
                    }
                }
            }

            PaymentMethodSelector(selected = paymentMethod, onSelect = { paymentMethod = it })

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(strings.description) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reasonText,
                onValueChange = { reasonText = it },
                label = { Text(strings.correctionReason) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.delete)
                }
                Button(
                    onClick = {
                        val total = CurrencyFormatter.parseTakaToPoisha(totalText)
                        val paid = if (txType == LedgerTransactionType.SHOP_PURCHASE) {
                            CurrencyFormatter.parseTakaToPoisha(paidText.ifBlank { "0" })
                        } else total
                        if (total == null || total <= 0L || paid == null || paid < 0L) {
                            errorMsg = strings.invalidAmountError
                            return@Button
                        }
                        if (txType == LedgerTransactionType.SHOP_PURCHASE && paid > total) {
                            errorMsg = strings.paidExceedsTotalError
                            return@Button
                        }
                        onUpdate(total, paid, noteText, selectedCategoryId, paymentMethod, selectedDateMillis, reasonText)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.save)
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(strings.deleteTransaction) },
            text = { Text(strings.correctionReason) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        confirmDelete = false
                        onDelete(reasonText.ifBlank { "Deleted by user" })
                    }
                ) { Text(strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(strings.cancel) }
            }
        )
    }
}
