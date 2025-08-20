package com.dls.pymetask.presentation.movimientos

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@SuppressLint("AutoboxingStateCreation")
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MovimientosViewModel @Inject constructor(
    private val repository: MovimientoRepository,

) : ViewModel() {

    // Flujo con todos los movimientos del usuario (todos los meses)
    private val _movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()

    // Estado de sincronización para la UI
    var syncing by mutableStateOf(false)
        private set
    var lastSyncResult by mutableStateOf<String?>(null)
        private set


    init { loadMovimientos()}

    private fun loadMovimientos() {
        viewModelScope.launch {
            repository.getMovimientos().collect { lista ->
                _movimientos.value = lista
            }
        }
    }
    fun addMovimiento(mov: Movimiento) {
        viewModelScope.launch {
            try {
                repository.insertMovimiento(mov)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun updateMovimiento(updated: Movimiento) {
        viewModelScope.launch {
            try {
                repository.updateMovimiento(updated) // persistencia
                _movimientos.value = _movimientos.value.map {
                    if (it.id == updated.id) updated else it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMovimiento(id: String) {
        viewModelScope.launch {
            repository.deleteMovimiento(id)
        }
    }
    /** Importa un CSV con cabeceras: date,description,amount,currency (separador , o ;) */
    fun importCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                syncing = true
                lastSyncResult = null

                val userId = com.dls.pymetask.utils.Constants.getUserIdSeguro(context) ?: ""
                val movs = parseCsvInternal(context, uri)
                var count = 0
                movs.forEach { m ->
                    repository.insertMovimiento(m.copy(userId = userId))
                    count++
                }
                lastSyncResult = "Importados $count movimientos"
            } catch (t: Throwable) {
                lastSyncResult = "Error al importar CSV: ${t.message}"
            } finally {
                syncing = false
            }
        }
    }
    /** Parser CSV sencillo (coma o punto y coma; coma decimal soportada) */

    // Parser universal de CSV bancario español → List<Movimiento>
// Solo usa columnas necesarias; ignora el resto.
    private fun parseCsvInternal(context: Context, uri: Uri): List<Movimiento> {

        // ---------- 1) Cargar texto con encoding robusto ----------
        val cr = context.contentResolver
        val text: String = runCatching {
            cr.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        }.getOrNull()
            ?: runCatching {
                cr.openInputStream(uri)?.bufferedReader(Charsets.ISO_8859_1)?.use { it.readText() }
            }.getOrNull()
            ?: runCatching {
                cr.openInputStream(uri)?.bufferedReader(java.nio.charset.Charset.forName("windows-1252"))?.use { it.readText() }            }.getOrNull()
            ?: return emptyList()

        val allLines = text.split('\n').map { it.trimEnd('\r') }
        if (allLines.isEmpty()) return emptyList()

        // ---------- 2) Encontrar línea de cabeceras y delimitador ----------
        fun looksLikeHeader(line: String): Boolean {
            val low = normalize(line)
            val hasFecha = low.contains("fecha")
            val hasAmountish = listOf("importe", "cargo", "abono", "amount", "concepto", "movimiento", "descripcion", "descripción")
                .any { low.contains(it) }
            return hasFecha && hasAmountish
        }

        val headerIndex = allLines.indexOfFirst(::looksLikeHeader).takeIf { it >= 0 } ?: return emptyList()
        val headerLine = allLines[headerIndex]

        val delimiter: Char = when {
            headerLine.contains(';') -> ';'
            headerLine.contains('\t') -> '\t'
            else -> ',' // por defecto
        }

        // ---------- 3) Construir mapa de cabeceras normalizadas ----------
        val headersRaw = splitKeepingEmpty(headerLine, delimiter)
        val headers = headersRaw.map { normalize(it) }

        fun idx(vararg aliases: String): Int {
            val targets = aliases.map { normalize(it) }
            return headers.indexOfFirst { h -> targets.any { t -> h == t || h.contains(t) } }
        }

        // Columnas típicas (muchos bancos usan variantes)
        val iFecha      = idx("fecha", "fecha operacion", "fecha operación", "fecha valor", "value date", "date")
        val iConcepto   = idx("concepto", "movimiento", "descripcion", "descripción", "detalle", "mas datos", "más datos", "narrativa", "description")
        val iImporte    = idx("importe", "importe eur", "importe (€)", "amount", "importe total")
        val iCargo      = idx("cargo", "debito", "débito", "debe")          // alternativa a importe
        val iAbono      = idx("abono", "credito", "crédito", "haber")       // alternativa a importe
        val iDivisa     = idx("divisa", "moneda", "currency")               // opcional

        if ((iFecha == -1) || (iImporte == -1 && iCargo == -1 && iAbono == -1)) {
            // Sin fecha o sin ninguna forma de importe → nada que hacer
            return emptyList()
        }

        // ---------- 4) Auxiliares de parseo ----------
        val tz = ZoneId.systemDefault()
        val dfISO = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dfES1 = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dfES2 = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")

        fun parseDate(s: String): Long? {
            val t = s.trim()
            return when {
                t.contains('-') && t.count { it == '-' } == 2 ->
                    runCatching { LocalDate.parse(t, dfISO) }.getOrNull()
                        ?.atStartOfDay(tz)?.toInstant()?.toEpochMilli()
                t.contains('/') ->
                    runCatching { LocalDate.parse(t, dfES1) }.getOrNull()
                        ?.atStartOfDay(tz)?.toInstant()?.toEpochMilli()
                        ?: runCatching { LocalDate.parse(t, dfES2) }.getOrNull()
                            ?.atStartOfDay(tz)?.toInstant()?.toEpochMilli()
                else -> null
            }
        }

        fun parseEuroAmount(raw0: String?): Double {
            if (raw0.isNullOrBlank()) return 0.0
            var raw = raw0.trim()
            // Quitar símbolo y espacios
            raw = raw.replace("€", "", ignoreCase = true).replace(" ", "")
            // Paréntesis = negativo: (123,45) → -123,45
            var negative = false
            if (raw.startsWith("(") && raw.endsWith(")")) {
                negative = true
                raw = raw.removePrefix("(").removeSuffix(")")
            }
            // Si hay coma, asumimos formato ES → quitar puntos miles y cambiar coma por punto
            raw = if (raw.contains(',')) raw.replace(".", "").replace(",", ".") else raw
            // A veces el signo va detrás: "123,45-" → "-123,45"
            if (raw.endsWith("-")) {
                negative = true
                raw = raw.removeSuffix("-")
            }
            val v = raw.toDoubleOrNull() ?: 0.0
            return if (negative) -v else v
        }

        // ---------- 5) Recorrer filas de datos ----------
        val dataLines = allLines.drop(headerIndex + 1)
        val out = mutableListOf<Movimiento>()

        for (line in dataLines) {
            if (line.isBlank()) continue
            val parts = splitKeepingEmpty(line, delimiter)
            if (parts.size < 2) continue // muy corta / decorativa

            val fechaStr = when {
                iFecha >= 0 -> parts.getOrNull(iFecha).orEmpty()
                else -> "" // imposible aquí, ya validado arriba
            }
            val millis = parseDate(fechaStr) ?: continue

            // Determinar importe usando prioridad: Cargo/Abono → Importe
            val cargo = if (iCargo >= 0) parseEuroAmount(parts.getOrNull(iCargo)) else null
            val abono = if (iAbono >= 0) parseEuroAmount(parts.getOrNull(iAbono)) else null
            val importe = when {
                cargo != null && cargo != 0.0 -> -kotlin.math.abs(cargo) // cargo siempre negativo
                abono != null && abono != 0.0 -> kotlin.math.abs(abono)  // abono siempre positivo
                else -> parseEuroAmount(parts.getOrNull(iImporte))
            }

            // Descripción: concepto + (más datos) si existen
            val concept = if (iConcepto >= 0) parts.getOrNull(iConcepto).orEmpty().trim() else ""
            val divisa  = if (iDivisa   >= 0) parts.getOrNull(iDivisa).orEmpty().trim().ifBlank { "EUR" } else "EUR"

            // Construir Movimiento (solo campos que usa tu modelo)
            out += Movimiento(
                id = java.util.UUID.randomUUID().toString(),
                titulo = concept.ifEmpty { "Movimiento" },
                subtitulo = divisa,                          // usamos divisa si viene; si no, EUR
                cantidad = kotlin.math.abs(importe),
                ingreso = importe >= 0.0,
                fecha = millis,
                userId = "" // se rellena al guardar
            )
        }

        return out
    }

    /** Normaliza texto: minúsculas, sin acentos, trim */
    private fun normalize(s: String): String {
        val n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        return n.replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    /** Split sencillo que preserva campos vacíos y soporta delimitador ; , o tab.
     *  (Para CSVs bancarios sin comillas con separadores dentro del texto) */
    private fun splitKeepingEmpty(line: String, delimiter: Char): List<String> {
        // Si quieres algo 100% CSV (con comillas y separadores dentro), mete una lib;
        // para bancos españoles típicos, esto es suficiente y ligero.
        val out = ArrayList<String>()
        var cur = StringBuilder()
        for (c in line) {
            if (c == delimiter) {
                out += cur.toString()
                cur = StringBuilder()
            } else {
                cur.append(c)
            }
        }
        out += cur.toString()
        return out
    }

// ====== MODELO DE SECCIÓN PARA LA UI ======
    /** Agrupa movimientos por mes (year-month) para pintarlos con cabecera */
    @Suppress("DEPRECATION")
    data class MesSection(
        val year: Int,
        val month: Int, // 1..12
        val items: List<Movimiento>
    ) {
        /** "Agosto 2025" en español */
        fun title(): String {
            val ym = YearMonth.of(year, month)
            val nombre = ym.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            return "${nombre.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es","ES")) else it.toString() }} $year"
        }
    }

    // ====== ESTADO DE LA PAGINACIÓN ======
    private val _meses = MutableStateFlow<List<MesSection>>(emptyList())
    val meses = _meses.asStateFlow()

    private var monthOffset = 0 // 0 = mes actual, 1 = anterior, etc.
    private var earliestMillis: Long? = null
    private val zona = ZoneId.systemDefault()

    private val _noHayMas = MutableStateFlow(false)
    val noHayMas = _noHayMas.asStateFlow()

    /** Llamar una vez (por ejemplo en LaunchedEffect de la pantalla) */
    fun startMonthPaging(userId: String) = viewModelScope.launch {
        // Obtenemos el movimiento más antiguo UNA sola vez para saber cuándo parar
        earliestMillis = repository.getEarliestMovimientoMillis(userId)
        _meses.value = emptyList()
        monthOffset = 0
        _noHayMas.value = false
        loadNextMonth(userId) // carga el mes actual
    }

    /** Carga el mes correspondiente a monthOffset y, si está vacío y aún queda, avanza */

    fun loadNextMonth(userId: String) {
        viewModelScope.launch {
            var nextOffset = monthOffset  // empezamos en el offset actual

            while (true) {
                val target = YearMonth.now(zona).minusMonths(nextOffset.toLong())

                val from: Long = target.atDay(1).atStartOfDay(zona).toInstant().toEpochMilli()
                val to: Long   = target.plusMonths(1).atDay(1).atStartOfDay(zona).toInstant().toEpochMilli()

                val earliest = earliestMillis
                if (earliest != null && to <= earliest) {
                    _noHayMas.value = true
                    return@launch
                }

                // Tipamos explícitamente para ayudar al compilador
                val lista: List<Movimiento> =
                    repository.getMovimientosBetween(userId, from, to)

                if (lista.isNotEmpty()) {
                    _meses.value = _meses.value + MesSection(target.year, target.monthValue, lista)
                    monthOffset = nextOffset + 1  // el siguiente "Mostrar más" pedirá el inmediatamente anterior
                    return@launch
                } else {
                    // Salta al mes anterior y sigue buscando
                    nextOffset++
                    if (nextOffset > 240) { // límite de seguridad (20 años)
                        _noHayMas.value = true
                        return@launch
                    }
                }
            }
        }
    }
}
