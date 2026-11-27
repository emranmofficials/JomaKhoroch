package com.example.infrastructure

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.NumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.ShopEntity
import com.example.domain.accounting.FinancialSummary
import com.example.domain.accounting.ShopBalanceSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

data class ReportExportPayload(
    val bookName: String,
    val periodLabel: String,
    val summary: FinancialSummary,
    val periodIncomePoisha: Long,
    val periodExpensePoisha: Long,
    val transactions: List<LedgerTransactionEntity>,
    val categoriesById: Map<Long, CategoryEntity>,
    val shopsById: Map<Long, ShopEntity>,
    val shopBalances: List<Pair<ShopEntity, ShopBalanceSummary>>,
    val language: AppLanguage,
    val numeralStyle: NumeralStyle
)

/**
 * Generates offline PDF statements (with bundled Hind Siliguri font for crisp Bangla/English)
 * and UTF-8 BOM CSV exports written directly to user-selected SAF streams.
 */
object ReportExporter {

    suspend fun writeCsv(payload: ReportExportPayload, output: OutputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sb = StringBuilder()
                // UTF-8 BOM so Excel displays Bengali characters cleanly
                sb.append('\uFEFF')
                sb.appendLine("JomaKhoroch Financial Statement,${escapeCsv(payload.bookName)},${escapeCsv(payload.periodLabel)}")
                sb.appendLine("Metric,Amount (BDT)")
                sb.appendLine("Period Income,${CurrencyFormatter.poishaToPlainInput(payload.periodIncomePoisha).ifEmpty { "0" }}")
                sb.appendLine("Period Expense,${CurrencyFormatter.poishaToPlainInput(payload.periodExpensePoisha).ifEmpty { "0" }}")
                sb.appendLine("Current Cash Balance,${CurrencyFormatter.poishaToPlainInput(payload.summary.currentCashBalancePoisha).ifEmpty { "0" }}")
                sb.appendLine("Total Shop Dues,${CurrencyFormatter.poishaToPlainInput(payload.summary.totalShopPayablePoisha).ifEmpty { "0" }}")
                sb.appendLine("Total Receivable,${CurrencyFormatter.poishaToPlainInput(payload.summary.totalPersonalReceivablePoisha).ifEmpty { "0" }}")
                sb.appendLine("Total Personal Payable,${CurrencyFormatter.poishaToPlainInput(payload.summary.totalPersonalPayablePoisha).ifEmpty { "0" }}")
                sb.appendLine()
                sb.appendLine("Date,Type,Category/Shop,Payment Method,Total (BDT),Paid (BDT),Due (BDT),Note")

                for (tx in payload.transactions) {
                    val dateStr = DateUtils.formatDate(tx.transactionDate, AppLanguage.ENGLISH, NumeralStyle.ENGLISH)
                    val label = tx.shopId?.let { payload.shopsById[it]?.name }
                        ?: tx.categoryId?.let { payload.categoriesById[it]?.nameEn }
                        ?: tx.type
                    val total = CurrencyFormatter.poishaToPlainInput(tx.totalAmountPoisha).ifEmpty { "0" }
                    val paid = CurrencyFormatter.poishaToPlainInput(tx.paidAmountPoisha).ifEmpty { "0" }
                    val due = CurrencyFormatter.poishaToPlainInput(tx.dueAmountPoisha).ifEmpty { "0" }
                    sb.appendLine(
                        listOf(
                            escapeCsv(dateStr),
                            escapeCsv(tx.type),
                            escapeCsv(label),
                            escapeCsv(tx.paymentMethod),
                            total,
                            paid,
                            due,
                            escapeCsv(tx.note)
                        ).joinToString(",")
                    )
                }

                output.write(sb.toString().toByteArray(Charsets.UTF_8))
                output.flush()
            }
        }

    suspend fun writePdf(
        context: Context,
        payload: ReportExportPayload,
        output: OutputStream
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 36f
            val typeface = runCatching {
                ResourcesCompat.getFont(context, R.font.hind_siliguri)
            }.getOrNull() ?: Typeface.SANS_SERIF

            val headerBgPaint = Paint().apply { color = Color.parseColor("#176B45") }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 18f
                this.typeface = Typeface.create(typeface, Typeface.BOLD)
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D9F0E4")
                textSize = 11f
                this.typeface = typeface
            }
            val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#176B45")
                textSize = 13f
                this.typeface = Typeface.create(typeface, Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#202824")
                textSize = 10.5f
                this.typeface = typeface
            }
            val dividerPaint = Paint().apply {
                color = Color.parseColor("#D2DBD5")
                strokeWidth = 1f
            }

            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = page.canvas
            var y = 0f

            // Header Banner
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 84f, headerBgPaint)
            val appTitle = if (payload.language == AppLanguage.BANGLA) "জমাখরচ (JomaKhoroch) — আর্থিক বিবরণী" else "JomaKhoroch — Financial Statement"
            canvas.drawText(appTitle, margin, 36f, titlePaint)
            canvas.drawText("${payload.bookName}  •  ${payload.periodLabel}", margin, 60f, subPaint)
            y = 110f

            fun fmt(poisha: Long) = CurrencyFormatter.formatPoisha(poisha, payload.numeralStyle)

            // Summary block
            canvas.drawText(
                if (payload.language == AppLanguage.BANGLA) "সারসংক্ষেপ" else "Financial Summary",
                margin,
                y,
                sectionPaint
            )
            y += 20f

            val summaryLines = listOf(
                (if (payload.language == AppLanguage.BANGLA) "সময়কালের আয়:" else "Period Income:") to fmt(payload.periodIncomePoisha),
                (if (payload.language == AppLanguage.BANGLA) "সময়কালের ব্যয়:" else "Period Expense:") to fmt(payload.periodExpensePoisha),
                (if (payload.language == AppLanguage.BANGLA) "বর্তমান নগদ ব্যালেন্স:" else "Current Cash Balance:") to fmt(payload.summary.currentCashBalancePoisha),
                (if (payload.language == AppLanguage.BANGLA) "মোট দোকানের বাকি:" else "Total Shop Dues:") to fmt(payload.summary.totalShopPayablePoisha),
                (if (payload.language == AppLanguage.BANGLA) "মোট ব্যক্তিগত পাওনা:" else "Total Personal Receivable:") to fmt(payload.summary.totalPersonalReceivablePoisha),
                (if (payload.language == AppLanguage.BANGLA) "মোট ব্যক্তিগত দেনা:" else "Total Personal Payable:") to fmt(payload.summary.totalPersonalPayablePoisha)
            )
            for ((k, v) in summaryLines) {
                canvas.drawText(k, margin, y, bodyPaint)
                canvas.drawText(v, 360f, y, bodyPaint)
                y += 18f
            }

            y += 8f
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
            y += 22f

            // Shop Dues Summary if any
            val shopsWithDue = payload.shopBalances.filter { it.second.outstandingDuePoisha > 0L }
            if (shopsWithDue.isNotEmpty()) {
                canvas.drawText(
                    if (payload.language == AppLanguage.BANGLA) "দোকান অনুযায়ী বাকি" else "Outstanding Shop Dues",
                    margin,
                    y,
                    sectionPaint
                )
                y += 18f
                for ((shop, bal) in shopsWithDue) {
                    if (y > pageHeight - 60f) {
                        document.finishPage(page)
                        pageNumber++
                        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                        canvas = page.canvas
                        y = 48f
                    }
                    canvas.drawText(shop.name.take(36), margin, y, bodyPaint)
                    canvas.drawText(fmt(bal.outstandingDuePoisha), 360f, y, bodyPaint)
                    y += 16f
                }
                y += 10f
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
                y += 22f
            }

            // Transactions Table
            canvas.drawText(
                if (payload.language == AppLanguage.BANGLA) "লেনদেনের তালিকা" else "Transactions Ledger",
                margin,
                y,
                sectionPaint
            )
            y += 18f
            canvas.drawText(if (payload.language == AppLanguage.BANGLA) "তারিখ" else "Date", margin, y, bodyPaint)
            canvas.drawText(if (payload.language == AppLanguage.BANGLA) "বিবরণ / ধরন" else "Details", 140f, y, bodyPaint)
            canvas.drawText(if (payload.language == AppLanguage.BANGLA) "পরিমাণ" else "Amount", 430f, y, bodyPaint)
            y += 8f
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
            y += 16f

            for (tx in payload.transactions) {
                if (y > pageHeight - 48f) {
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    y = 48f
                }
                val dateText = DateUtils.formatDate(tx.transactionDate, payload.language, payload.numeralStyle)
                val catName = tx.shopId?.let { payload.shopsById[it]?.name }
                    ?: tx.categoryId?.let { cId ->
                        payload.categoriesById[cId]?.let {
                            if (payload.language == AppLanguage.BANGLA) it.nameBn else it.nameEn
                        }
                    } ?: tx.type
                val detail = if (tx.note.isNotBlank()) "$catName - ${tx.note}" else catName
                canvas.drawText(dateText.take(18), margin, y, bodyPaint)
                canvas.drawText(detail.take(38), 140f, y, bodyPaint)
                canvas.drawText(fmt(tx.totalAmountPoisha), 430f, y, bodyPaint)
                y += 16f
            }

            document.finishPage(page)
            document.writeTo(output)
            document.close()
            output.flush()
        }
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n')
        return if (needsQuotes) "\"${value.replace("\"", "\"\"")}\"" else value
    }
}
