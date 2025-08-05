package com.dls.pymetask.utils



import android.annotation.SuppressLint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
fun String.formatAsDayMonth(): String {
    return try {
        val date = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val day = date.dayOfMonth
        val month = date.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES"))
        "$day de $month"
    } catch (e: Exception) {
        this // En caso de error, se devuelve tal cual
    }
}
