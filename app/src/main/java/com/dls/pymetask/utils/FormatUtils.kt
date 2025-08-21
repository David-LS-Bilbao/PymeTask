package com.dls.pymetask.presentation.util

import android.content.Context
import com.dls.pymetask.R
import com.dls.pymetask.data.preferences.CurrencyOption
import com.dls.pymetask.data.preferences.DateFormatOption
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

/** Formatea una fecha según la preferencia seleccionada. */
fun formatDate(context: Context, date: LocalDate, option: DateFormatOption): String {
    return when (option) {
        DateFormatOption.DMY_SLASH -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        DateFormatOption.DMY_TEXT  -> date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))
        DateFormatOption.RELATIVE  -> {
            val today = LocalDate.now()
            when (date) {
                today -> context.getString(R.string.today)      // "Hoy" / "Today" / "Aujourd’hui"
                today.plusDays(1) -> context.getString(R.string.tomorrow)
                else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
        }
    }
}

/** Formatea dinero con la moneda por defecto del usuario. */
fun formatMoney(amount: BigDecimal, currency: CurrencyOption, locale: Locale = Locale.getDefault()): String {
    val nf = NumberFormat.getCurrencyInstance(locale)
    nf.currency = Currency.getInstance(currency.iso) // aplica símbolo y formato de la ISO
    return nf.format(amount)
}
