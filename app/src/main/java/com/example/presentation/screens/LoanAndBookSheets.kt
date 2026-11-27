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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.core.localization.localizedBookName
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.NotebookEntity
import com.example.domain.accounting.LoanDirection
import com.example.presentation.components.PaymentMethodSelector
import com.example.presentation.state.LoanWithBalance
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonalLoanSheet(
    direction: LoanDirection,
    onSave: (
        personName: String,
        phone: String,
        principalPoisha: Long,
        loanDate: Long,
        dueDate: Long?,
        description: String,
        paymentMethod: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current

    var personName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDueDatePicker by remember { mutableStateOf(false) }
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
                text = if (direction == LoanDirection.GIVEN) strings.moneyGiven else strings.moneyBorrowed,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it; errorMsg = null },
                label = { Text(if (direction == LoanDirection.GIVEN) strings.personName else strings.lenderName) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_person_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(strings.phoneNumber) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; errorMsg = null },
                label = { Text(strings.amount) },
                prefix = { Text("৳ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_amount_input")
            )

            PaymentMethodSelector(selected = paymentMethod, onSelect = { paymentMethod = it })

            OutlinedButton(
                onClick = { showDueDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    dueDateMillis?.let { "${strings.dueDate}: ${DateUtils.formatDate(it, language, numerals)}" }
                        ?: "${strings.dueDate} (Optional)"
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.description) },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    if (personName.isBlank()) {
                        errorMsg = strings.requiredFieldError
                        return@Button
                    }
                    val poisha = CurrencyFormatter.parseTakaToPoisha(amountText)
                    if (poisha == null || poisha <= 0L) {
                        errorMsg = strings.invalidAmountError
                        return@Button
                    }
                    onSave(personName, phone, poisha, System.currentTimeMillis(), dueDateMillis, description, paymentMethod)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_loan_button")
            ) {
                Text(strings.save)
            }
            Spacer(Modifier.height(18.dp))
        }
    }

    if (showDueDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = state.selectedDateMillis
                    showDueDatePicker = false
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) { Text(strings.cancel) }
            }
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanRepaymentSheet(
    loanWithBalance: LoanWithBalance,
    allTransactions: List<LedgerTransactionEntity>,
    onRepay: (paymentPoisha: Long, paymentMethod: String, note: String, dateMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current

    val isGiven = loanWithBalance.balance.direction == LoanDirection.GIVEN
    val remaining = loanWithBalance.balance.remainingPoisha
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val history = remember(allTransactions, loanWithBalance.loan.id) {
        allTransactions.filter { it.loanId == loanWithBalance.loan.id }
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
            Text(loanWithBalance.loan.personName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Surface(
                color = if (isGiven) finance.receivableContainer else finance.payableContainer,
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
                        Text(
                            text = if (isGiven) strings.remainingReceivable else strings.remainingPayable,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = CurrencyFormatter.formatPoisha(remaining, numerals),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold),
                            color = if (isGiven) finance.receivable else finance.payable
                        )
                    }
                    if (remaining > 0L) {
                        TextButton(onClick = {
                            amountText = CurrencyFormatter.poishaToPlainInput(remaining)
                            errorMsg = null
                        }) { Text(if (language == AppLanguage.BANGLA) "সম্পূর্ণ" else "Full") }
                    }
                }
            }

            if (remaining > 0L) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMsg = null },
                    label = { Text(strings.amount) },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                        val pay = CurrencyFormatter.parseTakaToPoisha(amountText)
                        if (pay == null || pay <= 0L) {
                            errorMsg = strings.invalidAmountError
                            return@Button
                        }
                        if (pay > remaining) {
                            errorMsg = strings.paymentExceedsDueError
                            return@Button
                        }
                        onRepay(pay, paymentMethod, noteText, System.currentTimeMillis())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isGiven) strings.receiveRepayment else strings.makeRepayment)
                }
            }

            if (history.isNotEmpty()) {
                HorizontalDivider()
                Text(strings.repaymentHistory, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                history.forEach { tx ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = DateUtils.formatDate(tx.transactionDate, language, numerals),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (tx.note.isNotBlank()) {
                                Text(tx.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = CurrencyFormatter.formatPoisha(tx.paidAmountPoisha, numerals),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountBookSheet(
    onSave: (name: String, type: String, iconKey: String, openingPoisha: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("CUSTOM") }
    var openingText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val types = listOf(
        Triple("PERSONAL", strings.personalBook, "person"),
        Triple("FAMILY", strings.familyBook, "family"),
        Triple("BUSINESS", strings.businessBook, "business"),
        Triple("CUSTOM", strings.customBook, "book")
    )

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
            Text(strings.addBook, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMsg = null },
                label = { Text(strings.bookName) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_name_input")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                types.forEach { (typeKey, label, _) ->
                    FilterChip(
                        selected = selectedType == typeKey,
                        onClick = { selectedType = typeKey },
                        label = { Text(label) }
                    )
                }
            }
            OutlinedTextField(
                value = openingText,
                onValueChange = { openingText = it; errorMsg = null },
                label = { Text("${strings.openingBalance} (৳)") },
                prefix = { Text("৳ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
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
                    val icon = types.first { it.first == selectedType }.third
                    onSave(name, selectedType, icon, opening)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) { Text(strings.save) }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
fun DeleteBookPolicyDialog(
    book: NotebookEntity,
    onArchive: () -> Unit,
    onPermanentDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val displayName = localizedBookName(book.name, book.type, language)
    var typedName by remember { mutableStateOf("") }
    val nameMatches = typedName.trim().equals(displayName.trim(), ignoreCase = true) ||
        typedName.trim().equals(book.name.trim(), ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.deleteBookConfirmTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.deleteBookConfirmDesc, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = typedName,
                    onValueChange = { typedName = it },
                    label = { Text("${strings.typeBookNameToConfirm}: $displayName") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!book.isArchived) {
                    OutlinedButton(onClick = onArchive) { Text(strings.archiveBook) }
                }
                Button(
                    enabled = nameMatches,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = onPermanentDelete
                ) { Text(strings.deleteBookPermanently) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
