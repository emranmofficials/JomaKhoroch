package com.example.domain.accounting

import kotlin.math.max

/**
 * Explicit ledger entry types enforcing strict separation between:
 * - Income & Expense
 * - Shop credit purchases & Shop due payments (no double-counting)
 * - Personal Lending (Receivables) & Personal Borrowing (Payables)
 */
enum class LedgerTransactionType {
    INCOME,
    EXPENSE,
    SHOP_PURCHASE,
    SHOP_DUE_PAYMENT,
    LENDING_GIVEN,
    LENDING_REPAYMENT,
    BORROWING_TAKEN,
    BORROWING_REPAYMENT
}

enum class LoanDirection {
    GIVEN,
    BORROWED
}

enum class PaymentMethod {
    CASH,
    MOBILE_BANKING,
    BANK
}

enum class BudgetStatus {
    HEALTHY,
    WARNING,
    EXCEEDED
}

data class LedgerEntryRecord(
    val id: Long,
    val notebookId: Long,
    val type: LedgerTransactionType,
    val totalAmountPoisha: Long,
    val paidAmountPoisha: Long,
    val dueAmountPoisha: Long,
    val categoryId: Long?,
    val shopId: Long?,
    val loanId: Long?,
    val transactionDate: Long,
    val isDeleted: Boolean = false
)

data class ShopOpeningRecord(
    val shopId: Long,
    val openingBalancePoisha: Long
)

data class LoanPrincipalRecord(
    val loanId: Long,
    val direction: LoanDirection,
    val principalPoisha: Long
)

data class FinancialSummary(
    val openingBalancePoisha: Long,
    val totalIncomePoisha: Long,
    val totalExpensePoisha: Long,
    val totalShopPayablePoisha: Long,
    val totalPersonalReceivablePoisha: Long,
    val totalPersonalPayablePoisha: Long,
    val currentCashBalancePoisha: Long
) {
    val totalReceivablePoisha: Long get() = totalPersonalReceivablePoisha
    val totalPayablePoisha: Long get() = totalShopPayablePoisha + totalPersonalPayablePoisha
    val netSavingsPoisha: Long get() = totalIncomePoisha - totalExpensePoisha
}

data class ShopBalanceSummary(
    val shopId: Long,
    val openingBalancePoisha: Long,
    val totalPurchasedPoisha: Long,
    val totalPaidAtPurchasePoisha: Long,
    val totalDuePaymentsPoisha: Long,
    val outstandingDuePoisha: Long
)

data class LoanBalanceSummary(
    val loanId: Long,
    val direction: LoanDirection,
    val principalPoisha: Long,
    val totalRepaidPoisha: Long,
    val remainingPoisha: Long,
    val isSettled: Boolean
)

data class BudgetProgress(
    val limitPoisha: Long,
    val spentPoisha: Long,
    val remainingPoisha: Long,
    val usagePercent: Int,
    val status: BudgetStatus
)

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errorCode: String) : ValidationResult()
}

/**
 * Pure-Kotlin, integer-only (poisha) Accounting Engine.
 * All financial metrics across Dashboard, Shops, Loans, Budgets, and Reports
 * are calculated exclusively through these verified accounting rules.
 */
object AccountingEngine {

    fun validatePositiveAmount(amountPoisha: Long): ValidationResult {
        return if (amountPoisha > 0L) ValidationResult.Valid
        else ValidationResult.Invalid("AMOUNT_MUST_BE_POSITIVE")
    }

    fun validateNonNegativeAmount(amountPoisha: Long): ValidationResult {
        return if (amountPoisha >= 0L) ValidationResult.Valid
        else ValidationResult.Invalid("AMOUNT_CANNOT_BE_NEGATIVE")
    }

    fun validateShopPurchase(totalPoisha: Long, paidPoisha: Long): ValidationResult {
        if (totalPoisha <= 0L) return ValidationResult.Invalid("AMOUNT_MUST_BE_POSITIVE")
        if (paidPoisha < 0L) return ValidationResult.Invalid("AMOUNT_CANNOT_BE_NEGATIVE")
        if (paidPoisha > totalPoisha) return ValidationResult.Invalid("PAID_EXCEEDS_TOTAL")
        return ValidationResult.Valid
    }

    fun validateSettlement(paymentPoisha: Long, currentOutstandingPoisha: Long): ValidationResult {
        if (paymentPoisha <= 0L) return ValidationResult.Invalid("AMOUNT_MUST_BE_POSITIVE")
        if (paymentPoisha > currentOutstandingPoisha) {
            return ValidationResult.Invalid("PAYMENT_EXCEEDS_DUE")
        }
        return ValidationResult.Valid
    }

    fun computeShopBalance(
        shop: ShopOpeningRecord,
        entries: List<LedgerEntryRecord>
    ): ShopBalanceSummary {
        var totalPurchased = 0L
        var totalPaidAtPurchase = 0L
        var totalDueCreated = 0L
        var totalDuePaid = 0L

        for (entry in entries) {
            if (entry.isDeleted || entry.shopId != shop.shopId) continue
            when (entry.type) {
                LedgerTransactionType.SHOP_PURCHASE -> {
                    totalPurchased += entry.totalAmountPoisha
                    totalPaidAtPurchase += entry.paidAmountPoisha
                    totalDueCreated += entry.dueAmountPoisha
                }
                LedgerTransactionType.SHOP_DUE_PAYMENT -> {
                    totalDuePaid += entry.paidAmountPoisha
                }
                else -> Unit
            }
        }

        val outstanding = max(0L, shop.openingBalancePoisha + totalDueCreated - totalDuePaid)
        return ShopBalanceSummary(
            shopId = shop.shopId,
            openingBalancePoisha = shop.openingBalancePoisha,
            totalPurchasedPoisha = totalPurchased,
            totalPaidAtPurchasePoisha = totalPaidAtPurchase,
            totalDuePaymentsPoisha = totalDuePaid,
            outstandingDuePoisha = outstanding
        )
    }

    fun computeLoanBalance(
        loan: LoanPrincipalRecord,
        entries: List<LedgerEntryRecord>
    ): LoanBalanceSummary {
        var repaid = 0L
        val targetType = if (loan.direction == LoanDirection.GIVEN) {
            LedgerTransactionType.LENDING_REPAYMENT
        } else {
            LedgerTransactionType.BORROWING_REPAYMENT
        }

        for (entry in entries) {
            if (entry.isDeleted || entry.loanId != loan.loanId) continue
            if (entry.type == targetType) {
                repaid += entry.paidAmountPoisha
            }
        }

        val remaining = max(0L, loan.principalPoisha - repaid)
        return LoanBalanceSummary(
            loanId = loan.loanId,
            direction = loan.direction,
            principalPoisha = loan.principalPoisha,
            totalRepaidPoisha = repaid,
            remainingPoisha = remaining,
            isSettled = remaining == 0L
        )
    }

    /**
     * Computes complete book-level financial summary from active ledger entries.
     *
     * Rules strictly enforced:
     * 1. Expense = EXPENSE.total + SHOP_PURCHASE.total (SHOP_DUE_PAYMENT is NOT an expense -> no double-counting)
     * 2. Income = INCOME.total (BORROWING_TAKEN and LENDING_REPAYMENT are NOT income)
     * 3. Lending is NOT an expense; Borrowing is NOT income
     * 4. Cash reflects actual cash inflows/outflows (opening + inflows - outflows)
     * 5. Shop payables and personal receivables/payables remain strictly isolated
     */
    fun computeBookSummary(
        openingBalancePoisha: Long,
        shops: List<ShopOpeningRecord>,
        loans: List<LoanPrincipalRecord>,
        entries: List<LedgerEntryRecord>
    ): FinancialSummary {
        var totalIncome = 0L
        var totalExpense = 0L
        var cashDelta = 0L

        for (entry in entries) {
            if (entry.isDeleted) continue
            when (entry.type) {
                LedgerTransactionType.INCOME -> {
                    totalIncome += entry.totalAmountPoisha
                    cashDelta += entry.paidAmountPoisha
                }
                LedgerTransactionType.EXPENSE -> {
                    totalExpense += entry.totalAmountPoisha
                    cashDelta -= entry.paidAmountPoisha
                }
                LedgerTransactionType.SHOP_PURCHASE -> {
                    // Expense is recognized at purchase; cash decreases only by paidAmount
                    totalExpense += entry.totalAmountPoisha
                    cashDelta -= entry.paidAmountPoisha
                }
                LedgerTransactionType.SHOP_DUE_PAYMENT -> {
                    // Liability settlement only: cash decreases, expense does NOT increase again
                    cashDelta -= entry.paidAmountPoisha
                }
                LedgerTransactionType.LENDING_GIVEN -> {
                    // Cash decreases, receivable increases, expense does NOT increase
                    cashDelta -= entry.paidAmountPoisha
                }
                LedgerTransactionType.LENDING_REPAYMENT -> {
                    // Cash increases, receivable decreases, income does NOT increase
                    cashDelta += entry.paidAmountPoisha
                }
                LedgerTransactionType.BORROWING_TAKEN -> {
                    // Cash increases, payable increases, income does NOT increase
                    cashDelta += entry.paidAmountPoisha
                }
                LedgerTransactionType.BORROWING_REPAYMENT -> {
                    // Cash decreases, payable decreases, expense does NOT increase
                    cashDelta -= entry.paidAmountPoisha
                }
            }
        }

        val totalShopPayable = shops.sumOf { shop ->
            computeShopBalance(shop, entries).outstandingDuePoisha
        }

        var totalPersonalReceivable = 0L
        var totalPersonalPayable = 0L
        for (loan in loans) {
            val summary = computeLoanBalance(loan, entries)
            if (loan.direction == LoanDirection.GIVEN) {
                totalPersonalReceivable += summary.remainingPoisha
            } else {
                totalPersonalPayable += summary.remainingPoisha
            }
        }

        return FinancialSummary(
            openingBalancePoisha = openingBalancePoisha,
            totalIncomePoisha = totalIncome,
            totalExpensePoisha = totalExpense,
            totalShopPayablePoisha = totalShopPayable,
            totalPersonalReceivablePoisha = totalPersonalReceivable,
            totalPersonalPayablePoisha = totalPersonalPayable,
            currentCashBalancePoisha = openingBalancePoisha + cashDelta
        )
    }

    /**
     * Computes period-filtered Income and Expense totals (e.g., for Monthly Summary or Reports).
     */
    fun computePeriodIncomeExpense(
        entries: List<LedgerEntryRecord>,
        startMillis: Long,
        endMillis: Long
    ): Pair<Long, Long> {
        var income = 0L
        var expense = 0L
        for (entry in entries) {
            if (entry.isDeleted) continue
            if (entry.transactionDate !in startMillis..endMillis) continue
            when (entry.type) {
                LedgerTransactionType.INCOME -> income += entry.totalAmountPoisha
                LedgerTransactionType.EXPENSE,
                LedgerTransactionType.SHOP_PURCHASE -> expense += entry.totalAmountPoisha
                else -> Unit
            }
        }
        return income to expense
    }

    fun computeBudgetProgress(limitPoisha: Long, spentPoisha: Long): BudgetProgress {
        if (limitPoisha <= 0L) {
            return BudgetProgress(0L, spentPoisha, 0L, 0, BudgetStatus.HEALTHY)
        }
        val remaining = limitPoisha - spentPoisha
        val percent = ((spentPoisha * 100L) / limitPoisha).toInt()
        val status = when {
            spentPoisha > limitPoisha -> BudgetStatus.EXCEEDED
            percent >= 80 -> BudgetStatus.WARNING
            else -> BudgetStatus.HEALTHY
        }
        return BudgetProgress(
            limitPoisha = limitPoisha,
            spentPoisha = spentPoisha,
            remainingPoisha = remaining,
            usagePercent = percent,
            status = status
        )
    }
}
