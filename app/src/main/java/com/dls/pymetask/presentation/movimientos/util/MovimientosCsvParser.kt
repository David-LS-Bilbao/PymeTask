package com.dls.pymetask.presentation.movimientos.util

import java.text.SimpleDateFormat
import java.util.Locale

data class ParsedCsvRow(
    val date: Long,
    val description: String,
    val amount: Double
)

/**
 * Parsea líneas CSV con 3 campos: fecha;concepto;importe
 * Reglas:
 * - Preferimos ';' como separador (formato ES habitual). Fallback a \t y después a ','.
 * - limit = 3 para no "romper" importes con comas/puntos.
 * - Normalizamos el importe detectando correctamente el separador decimal:
 *   · Si la última coma está a la derecha de la última punto → COMA es decimal: quitar puntos (miles) y ',' -> '.'
 *   · Si la última punto está a la derecha de la última coma → PUNTO es decimal: quitar comas (miles) y mantener '.'
 *   · Si solo hay comas → COMA decimal.
 *   · Si solo hay puntos → PUNTO decimal (si hay varios, dejamos solo el último como decimal).
 */
fun parseCsvLines(lines: List<String>): List<ParsedCsvRow> {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return lines.mapNotNull { line ->
        val parts = when {
            line.contains(';') -> line.split(';', limit = 3)
            line.contains('\t') -> line.split('\t', limit = 3)
            else -> line.split(',', limit = 3)
        }
        if (parts.size < 3) return@mapNotNull null

        val rawDate = parts[0].trim()
        val rawDesc = parts[1].trim()
        val rawAmount = parts[2].trim()

        try {
            val fecha = sdf.parse(rawDate)?.time ?: return@mapNotNull null
            val cantidad = normalizeAmount(rawAmount)
            ParsedCsvRow(date = fecha, description = rawDesc, amount = cantidad)
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Normaliza una cadena numérica con posibles símbolos, miles y separadores mixtos:
 * - Mantiene el signo.
 * - Elimina símbolos de moneda y espacios.
 * - Detecta el separador decimal de forma robusta.
 */
private fun normalizeAmount(input: String): Double {
    // Quita espacios, símbolos de moneda y caracteres no numéricos relevantes (excepto dígitos, +, -, ., ,)
    val cleaned = buildString(input.length) {
        for (ch in input.trim()) {
            when (ch) {
                '0','1','2','3','4','5','6','7','8','9','+','-','.',',' -> append(ch)
                else -> { /* skip currency symbols, spaces, etc. */ }
            }
        }
    }

    val lastComma = cleaned.lastIndexOf(',')
    val lastDot = cleaned.lastIndexOf('.')

    val normalized = when {
        // Ambos presentes: decide por el que esté más a la derecha
        lastComma != -1 && lastDot != -1 -> {
            if (lastComma > lastDot) {
                // COMA decimal -> quitar puntos (miles) y ',' -> '.'
                cleaned.replace(".", "").replace(',', '.')
            } else {
                // PUNTO decimal -> quitar comas (miles)
                cleaned.replace(",", "")
            }
        }
        // Solo comas -> COMA decimal
        lastComma != -1 -> cleaned.replace('.', ' ') // primero quita posibles miles con punto
            .replace(" ", "")                         // limpia espacios puestos arriba
            .replace(',', '.')
        // Solo puntos -> PUNTO decimal; si hay varios, elimina todos menos el último
        lastDot != -1 -> {
            if (cleaned.count { it == '.' } <= 1) {
                cleaned.replace(",", "")
            } else {
                // Mantén solo el último punto como decimal
                val sb = StringBuilder()
                var dotsLeft = cleaned.count { it == '.' }
                for (ch in cleaned) {
                    if (ch == '.') {
                        dotsLeft--
                        if (dotsLeft == 0) sb.append('.') // último punto (decimal)
                        // si no es el último, lo saltamos (era miles)
                    } else if (ch != ',') {
                        sb.append(ch) // también desechamos comas por si aparecen
                    }
                }
                sb.toString()
            }
        }
        // No hay ni punto ni coma -> entero
        else -> cleaned
    }

    return normalized.toDouble()
}
