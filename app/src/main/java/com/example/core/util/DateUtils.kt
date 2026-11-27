package com.example.core.util

import com.example.core.localization.AppLanguage
import com.example.core.localization.NumeralStyle
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private val MONTHS_BN = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    private val MONTHS_EN = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    private val WEEKDAYS_BN = arrayOf(
        "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"
    )
    private val WEEKDAYS_EN = arrayOf(
        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    )

    fun formatDate(
        epochMillis: Long,
        language: AppLanguage,
        numeralStyle: NumeralStyle
    ): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val monthLabel = if (language == AppLanguage.BANGLA) MONTHS_BN[month] else MONTHS_EN[month]
        return CurrencyFormatter.toLocalizedDigits("$day $monthLabel $year", numeralStyle)
    }

    fun formatDateWithWeekday(
        epochMillis: Long,
        language: AppLanguage,
        numeralStyle: NumeralStyle
    ): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val dowIdx = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
        val monthLabel = if (language == AppLanguage.BANGLA) MONTHS_BN[month] else MONTHS_EN[month]
        val dayName = if (language == AppLanguage.BANGLA) WEEKDAYS_BN[dowIdx] else WEEKDAYS_EN[dowIdx]
        return CurrencyFormatter.toLocalizedDigits("$day $monthLabel $year ($dayName)", numeralStyle)
    }

    fun formatTimeOfDay(
        epochMillis: Long,
        language: AppLanguage,
        numeralStyle: NumeralStyle
    ): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
        val minute = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val timeStr = CurrencyFormatter.toLocalizedDigits("$hour12:$minute", numeralStyle)
        return if (language == AppLanguage.BANGLA) {
            val period = when {
                hour24 < 5 -> "রাত"
                hour24 < 12 -> "সকাল"
                hour24 < 15 -> "দুপুর"
                hour24 < 18 -> "বিকাল"
                hour24 < 20 -> "সন্ধ্যা"
                else -> "রাত"
            }
            "$period $timeStr"
        } else {
            val period = if (hour24 < 12) "AM" else "PM"
            "$timeStr $period"
        }
    }

    fun formatDateTime(
        epochMillis: Long,
        language: AppLanguage,
        numeralStyle: NumeralStyle
    ): String {
        val dateStr = formatDate(epochMillis, language, numeralStyle)
        val timeStr = formatTimeOfDay(epochMillis, language, numeralStyle)
        return "$dateStr, $timeStr"
    }

    fun formatMonthYear(
        yearMonth: String,
        language: AppLanguage,
        numeralStyle: NumeralStyle
    ): String {
        val parts = yearMonth.split("-")
        if (parts.size != 2) return yearMonth
        val year = parts[0].toIntOrNull() ?: return yearMonth
        val monthIdx = (parts[1].toIntOrNull() ?: 1).coerceIn(1, 12) - 1
        val monthLabel = if (language == AppLanguage.BANGLA) MONTHS_BN[monthIdx] else MONTHS_EN[monthIdx]
        return CurrencyFormatter.toLocalizedDigits("$monthLabel $year", numeralStyle)
    }

    fun currentYearMonth(nowMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", y, m)
    }

    fun shiftYearMonth(yearMonth: String, deltaMonths: Int): String {
        val parts = yearMonth.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        return String.format(
            Locale.US,
            "%04d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        )
    }

    fun startOfDay(epochMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun endOfDay(epochMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun startOfWeek(epochMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startOfDay(epochMillis)
            add(Calendar.DAY_OF_YEAR, -6)
        }.timeInMillis
    }

    fun monthRange(yearMonth: String): Pair<Long, Long> {
        val parts = yearMonth.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = startCal.timeInMillis
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    fun yearRange(year: Int): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    fun isSameDay(a: Long, b: Long): Boolean {
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    fun yearOf(epochMillis: Long): Int {
        return Calendar.getInstance().apply { timeInMillis = epochMillis }.get(Calendar.YEAR)
    }
}
