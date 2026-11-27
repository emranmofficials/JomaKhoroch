package com.example.core.util

import com.example.core.localization.NumeralStyle
import kotlin.math.abs

object CurrencyFormatter {
    const val TAKA_SYMBOL = "৳"
    private val BANGLA_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toLocalizedDigits(text: String, style: NumeralStyle): String {
        if (style == NumeralStyle.ENGLISH) return normalizeDigitsToEnglish(text)
        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(BANGLA_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun normalizeDigitsToEnglish(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            val idx = BANGLA_DIGITS.indexOf(ch)
            if (idx >= 0) {
                sb.append(('0'.code + idx).toChar())
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats integer poisha into Bangladeshi Taka format with lakh/crore comma grouping.
     * Example: 12500000L -> "৳১,২৫,০০০" or "৳1,25,000".
     */
    fun formatPoisha(
        poisha: Long,
        style: NumeralStyle = NumeralStyle.BANGLA,
        showSign: Boolean = false,
        includeSymbol: Boolean = true
    ): String {
        val isNegative = poisha < 0L
        val absPoisha = abs(poisha)
        val wholeTaka = absPoisha / 100L
        val remPoisha = absPoisha % 100L

        val groupedWhole = formatBangladeshiGrouping(wholeTaka)
        val decimalPart = if (remPoisha == 0L) "" else ".${remPoisha.toString().padStart(2, '0')}"
        val signPrefix = when {
            isNegative -> "-"
            showSign && poisha > 0L -> "+"
            else -> ""
        }
        val symbol = if (includeSymbol) TAKA_SYMBOL else ""
        val raw = "$signPrefix$symbol$groupedWhole$decimalPart"
        return toLocalizedDigits(raw, style)
    }

    fun formatBangladeshiGrouping(whole: Long): String {
        val digits = whole.toString()
        if (digits.length <= 3) return digits
        val lastThree = digits.takeLast(3)
        val remaining = digits.dropLast(3)
        val sb = StringBuilder()
        var idx = remaining.length % 2
        if (idx > 0) {
            sb.append(remaining.substring(0, idx))
            if (remaining.length > idx) sb.append(',')
        }
        while (idx < remaining.length) {
            sb.append(remaining.substring(idx, idx + 2))
            idx += 2
            if (idx < remaining.length) sb.append(',')
        }
        sb.append(',').append(lastThree)
        return sb.toString()
    }

    /**
     * Parses a user-entered Taka string (Bangla or English digits) into integer poisha
     * strictly using integer math — zero floating-point arithmetic.
     * Returns null if invalid or negative.
     */
    fun parseTakaToPoisha(input: String): Long? {
        val cleaned = normalizeDigitsToEnglish(input)
            .replace(TAKA_SYMBOL, "")
            .replace(",", "")
            .trim()
        if (cleaned.isEmpty() || cleaned.startsWith("-")) return null
        val parts = cleaned.split(".")
        if (parts.size > 2) return null

        val wholeStr = parts[0].ifEmpty { "0" }
        if (!wholeStr.all { it.isDigit() }) return null
        val wholeTaka = wholeStr.toLongOrNull() ?: return null

        val poishaPart = if (parts.size == 2) {
            val frac = parts[1]
            if (frac.isEmpty()) 0L
            else if (!frac.all { it.isDigit() } || frac.length > 2) return null
            else if (frac.length == 1) (frac.toLongOrNull() ?: return null) * 10L
            else frac.toLongOrNull() ?: return null
        } else {
            0L
        }

        if (wholeTaka > Long.MAX_VALUE / 100L - 1L) return null
        return wholeTaka * 100L + poishaPart
    }

    /**
     * Parses quantity like "2", "0.5", "1.25" into integer millis (x1000) without floats.
     */
    fun parseQuantityToMillis(input: String): Long? {
        val cleaned = normalizeDigitsToEnglish(input).replace(",", "").trim()
        if (cleaned.isEmpty() || cleaned.startsWith("-")) return null
        val parts = cleaned.split(".")
        if (parts.size > 2) return null
        val wholeStr = parts[0].ifEmpty { "0" }
        if (!wholeStr.all { it.isDigit() }) return null
        val whole = wholeStr.toLongOrNull() ?: return null
        val fracMillis = if (parts.size == 2) {
            val frac = parts[1]
            if (frac.isEmpty()) 0L
            else if (!frac.all { it.isDigit() } || frac.length > 3) return null
            else frac.padEnd(3, '0').toLongOrNull() ?: return null
        } else 0L
        if (whole > Long.MAX_VALUE / 1000L - 1L) return null
        val total = whole * 1000L + fracMillis
        return if (total > 0L) total else null
    }

    fun formatQuantityMillis(millis: Long, style: NumeralStyle): String {
        val whole = millis / 1000L
        val rem = millis % 1000L
        val raw = if (rem == 0L) {
            whole.toString()
        } else {
            val trimmed = rem.toString().padStart(3, '0').trimEnd('0')
            "$whole.$trimmed"
        }
        return toLocalizedDigits(raw, style)
    }

    /**
     * Exact integer calculation of line item total in poisha: round((qtyMillis * unitPricePoisha) / 1000).
     */
    fun calculateItemTotalPoisha(quantityMillis: Long, unitPricePoisha: Long): Long {
        require(quantityMillis > 0L) { "Quantity must be positive" }
        require(unitPricePoisha >= 0L) { "Unit price cannot be negative" }
        return (quantityMillis * unitPricePoisha + 500L) / 1000L
    }

    fun poishaToPlainInput(poisha: Long): String {
        if (poisha == 0L) return ""
        val whole = poisha / 100L
        val rem = poisha % 100L
        return if (rem == 0L) whole.toString() else "$whole.${rem.toString().padStart(2, '0')}"
    }
}
