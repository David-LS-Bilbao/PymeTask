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
import com.dls.pymetask.data.local.AccountPrefs
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.BankRepository
import com.dls.pymetask.domain.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

@SuppressLint("AutoboxingStateCreation")
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MovimientosViewModel @Inject constructor(
    private val repository: MovimientoRepository,
    private val bankRepository: BankRepository, // <-- inyecta
    private val prefs: AccountPrefs


) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Flujo con todos los movimientos del usuario (todos los meses)
    private val _movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()
    private val _tipoSeleccionado = MutableStateFlow("Todos") // "Ingresos", "Gastos" o "Todos"
    val tipoSeleccionado: StateFlow<String> = _tipoSeleccionado.asStateFlow()
    fun setTipo(tipo: String) {
        _tipoSeleccionado.value = tipo
    }

    // Estado de sincronización para la UI
    var syncing by mutableStateOf(false)
        private set
    var lastSyncResult by mutableStateOf<String?>(null)
        private set


    val selectedAccountId = prefs.selectedAccountId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    /**
     * Sincroniza un MES concreto de una cuenta bancaria:
     * - accountId viene del proveedor (temporalmente, introduce un hardcode para probar).
     * - year/month0 definen el rango
     */
    fun syncBancoMes( year: Int, month0: Int) {
        val accountId = selectedAccountId.value ?: return
        viewModelScope.launch {
            syncing = true
            lastSyncResult = null
            val (fromMillis, toMillis) = monthBounds(year, month0)
            val result = bankRepository.syncAccount(accountId, fromMillis, toMillis)
            syncing = false
            lastSyncResult = result.fold(
                onSuccess = { count -> "Importadas/actualizadas: $count" },
                onFailure = { t -> "Error: ${t.message ?: t::class.java.simpleName}" }
            )
        }
    }

    /** Devuelve (inicio, fin) de mes en epoch millis */
    private fun monthBounds(year: Int, month0: Int): Pair<Long, Long> {
        val c1 = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month0)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val c2 = (c1.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.MONTH, 1)
            add(java.util.Calendar.MILLISECOND, -1)
        }
        return c1.timeInMillis to c2.timeInMillis
    }

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
    fun getMovimientoById(id: String): Movimiento? {
        return _movimientos.value.find { it.id == id }
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
    /** Genera N movimientos de demo y los guarda */
    fun generateDemo(context: Context, count: Int = 25) {
        viewModelScope.launch {
            try {
                syncing = true
                lastSyncResult = null

                val userId = com.dls.pymetask.utils.Constants.getUserIdSeguro(context) ?: ""
                val now = System.currentTimeMillis()
                val demo = (1..count).map {
                    val ingreso = (Math.random() > 0.5)
                    val days = (0..30).random()
                    val whenMs = now - days * 24L * 60 * 60 * 1000
                    Movimiento(
                        id = UUID.randomUUID().toString(),
                        titulo = if (ingreso) "Ingreso demo" else "Gasto demo",
                        subtitulo = "EUR",
                        cantidad = if (ingreso) (50..1200).random().toDouble() else (5..250).random().toDouble(),
                        ingreso = ingreso,
                        fecha = whenMs,
                        userId = userId
                    )
                }
                var countSaved = 0
                demo.forEach { repository.insertMovimiento(it); countSaved++ }
                lastSyncResult = "Generados $countSaved movimientos demo"
            } catch (t: Throwable) {
                lastSyncResult = "Error al generar demo: ${t.message}"
            } finally {
                syncing = false
            }
        }
    }
    /** Parser CSV sencillo (coma o punto y coma; coma decimal soportada) */
    private fun parseCsvInternal(context: Context, uri: Uri): List<Movimiento> {
        val cr = context.contentResolver
        val isr = InputStreamReader(cr.openInputStream(uri) ?: return emptyList())
        val br = BufferedReader(isr)
        val lines = br.readLines()
        if (lines.isEmpty()) return emptyList()

        val header = lines.first().lowercase()
        val sep = if (header.contains(";")) ";" else ","
        val cols = header.split(sep).map { it.trim() }
        val iDate = cols.indexOf("date")
        val iDesc = cols.indexOf("description")
        val iAmt  = cols.indexOf("amount")
        val iCur  = cols.indexOf("currency")

        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val tz = ZoneId.systemDefault()

        fun parseAmount(s: String): Double {
            val cleaned = s.replace("€", "").trim()
            // Soporta coma decimal: "1.234,56" -> "1234.56"
            val normalized = cleaned.replace(".", "").replace(",", ".")
            return normalized.toDoubleOrNull() ?: 0.0
        }

        return lines.drop(1).mapNotNull { row ->
            if (row.isBlank()) return@mapNotNull null
            val parts = row.split(sep)
            fun get(i: Int) = parts.getOrNull(i)?.trim().orEmpty()

            val d = if (iDate >= 0) get(iDate) else ""
            val desc = if (iDesc >= 0) get(iDesc) else "Movimiento"
            val amt = if (iAmt >= 0) parseAmount(get(iAmt)) else 0.0
            val cur = if (iCur >= 0) get(iCur) else "EUR"
            if (d.isBlank()) return@mapNotNull null

            val date = runCatching { LocalDate.parse(d, df) }.getOrNull() ?: return@mapNotNull null
            val millis = date.atStartOfDay(tz).toInstant().toEpochMilli()
            val ingreso = amt >= 0.0
            Movimiento(
                id = UUID.randomUUID().toString(),
                titulo = desc,
                subtitulo = cur,
                cantidad = abs(amt),
                ingreso = ingreso,
                fecha = millis,
                userId = "" // se rellena al guardar
            )
        }
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
    fun loadNextMonth(userId: String) = viewModelScope.launch {
        if (_noHayMas.value) return@launch

        // 1) Calculamos el mes objetivo (ahora - offset)
        val target = YearMonth.now(zona).minusMonths(monthOffset.toLong())

        // 2) Rango de fechas [from, to) en millis del mes "target"
        val from: Long = target.atDay(1).atStartOfDay(zona).toInstant().toEpochMilli()
        val to: Long = target.plusMonths(1).atDay(1).atStartOfDay(zona).toInstant().toEpochMilli()

        // 3) Si ya hemos pasado el movimiento más antiguo, finalizamos
        val earliest = earliestMillis
        if (earliest != null && to <= earliest) {
            _noHayMas.value = true
            return@launch
        }

        try {
            // 4) Pedimos los movimientos del mes
            val lista = repository.getMovimientosBetween(userId,
                from, to)

            if (lista.isNotEmpty()) {
                // Añadimos sección del mes y avanzamos offset
                _meses.value = _meses.value + MesSection(target.year, target.monthValue, lista)
                monthOffset++
            } else {
                // Mes vacío: saltamos al siguiente anterior
                monthOffset++
                if (monthOffset > 240) {
                    _noHayMas.value = true
                } else {
                    //loadNextMonth(userId)
                }
            }
        } catch (ex: IllegalStateException) {
            // Caso típico: falta índice en Firestore (lo estamos creando)
            _noHayMas.value = true
            lastSyncResult = ex.message
        }
    }





}
