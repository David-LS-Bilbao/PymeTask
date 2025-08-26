package com.dls.pymetask.presentation.estadisticas

import com.dls.pymetask.domain.model.Movimiento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EstadisticasViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private lateinit var vm: EstadisticasViewModel

    private fun mov(
        cantidad: Double,
        ingreso: Boolean,
        cal: Calendar = Calendar.getInstance()
    ): Movimiento = Movimiento(
        id = UUID.randomUUID().toString(),
        titulo = "t",
        cantidad = cantidad,
        ingreso = ingreso,
        fecha = cal.timeInMillis,
        subtitulo = "",
        userId = "u1"
    )

    @Before fun setup() { vm = EstadisticasViewModel() }

    @Test
    fun setMovimientos_actualizaLista_enUi() = runTest {
        val lista = listOf(mov(100.0, true))
        vm.setMovimientos(lista)
        assertEquals(1, vm.ui.value.movimientos.size)
        assertEquals(100.0, vm.ui.value.movimientos.first().cantidad, 0.0)
    }

    @Test
    fun setPeriodo_actualizaPeriodo() = runTest {
        vm.setPeriodo(Periodo.SEMANA)
        assertEquals(Periodo.SEMANA, vm.ui.value.periodo)
    }

    @Test
    fun setFiltro_actualizaFiltro() = runTest {
        vm.setFiltro(FiltroTipo.GASTO)
        assertEquals(FiltroTipo.GASTO, vm.ui.value.filtro)
    }

    @Test
    fun setCustomRange_guardaRangoYFuerzaPersonalizado() = runTest {
        val desde = Calendar.getInstance().apply { set(2024, Calendar.JANUARY, 1, 0, 0, 0) }.timeInMillis
        val hasta = Calendar.getInstance().apply { set(2024, Calendar.JANUARY, 31, 23, 59, 59) }.timeInMillis
        val range = desde to hasta
        vm.setCustomRange(range)
        assertEquals(range, vm.ui.value.customRange)
        assertEquals(Periodo.PERSONALIZADO, vm.ui.value.periodo)
    }

    @Test
    fun mesPrevA_retrocedeMes_respetandoYear() = runTest {
        val iniYear = vm.ui.value.year
        val iniMonth0 = vm.ui.value.month0

        // expected with Calendar logic
        val cal = Calendar.getInstance().apply { set(iniYear, iniMonth0, 1); add(Calendar.MONTH, -1) }
        val expY = cal.get(Calendar.YEAR)
        val expM = cal.get(Calendar.MONTH)

        vm.mesPrevA()

        assertEquals(expY, vm.ui.value.year)
        assertEquals(expM, vm.ui.value.month0)
    }

    @Test
    fun mesNextA_avanzaMes_respetandoYear() = runTest {
        val iniYear = vm.ui.value.year
        val iniMonth0 = vm.ui.value.month0
        val cal = Calendar.getInstance().apply { set(iniYear, iniMonth0, 1); add(Calendar.MONTH, 1) }
        val expY = cal.get(Calendar.YEAR)
        val expM = cal.get(Calendar.MONTH)

        vm.mesNextA()

        assertEquals(expY, vm.ui.value.year)
        assertEquals(expM, vm.ui.value.month0)
    }

    @Test
    fun mesPrevB_y_mesNextB_modificanMesComparativa() = runTest {
        val yB0 = vm.ui.value.yearB
        val mB0 = vm.ui.value.month0B

        val calPrev = Calendar.getInstance().apply { set(yB0, mB0, 1); add(Calendar.MONTH, -1) }
        vm.mesPrevB()
        assertEquals(calPrev.get(Calendar.YEAR), vm.ui.value.yearB)
        assertEquals(calPrev.get(Calendar.MONTH), vm.ui.value.month0B)

        val calNext = Calendar.getInstance().apply { set(vm.ui.value.yearB, vm.ui.value.month0B, 1); add(Calendar.MONTH, 1) }
        vm.mesNextB()
        assertEquals(calNext.get(Calendar.YEAR), vm.ui.value.yearB)
        assertEquals(calNext.get(Calendar.MONTH), vm.ui.value.month0B)
    }

    @Test
    fun toggleModo_alternaEntreMesYComparar() = runTest {
        assertEquals(Modo.MES, vm.ui.value.modo)
        vm.toggleModo()
        assertEquals(Modo.COMPARAR, vm.ui.value.modo)
        vm.toggleModo()
        assertEquals(Modo.MES, vm.ui.value.modo)
    }

    @Test
    fun totales_enPeriodoMes_reflejanIngresosGastosDelMesActual() = runTest {
        // Construimos movimientos en el mes actual (de ui.year/ui.month0)
        val y = vm.ui.value.year
        val m = vm.ui.value.month0
        val calIn = Calendar.getInstance().apply { set(y, m, 10, 12, 0, 0) }
        val calOut = Calendar.getInstance().apply { set(y, (m + 1) % 12, 1, 12, 0, 0) } // fuera de mes

        val ingresoActual = mov(150.0, true, calIn)
        val gastoActual = mov(40.0, false, calIn)
        val ingresoFuera = mov(999.0, true, calOut)

        vm.setMovimientos(listOf(ingresoActual, gastoActual, ingresoFuera))
        vm.setPeriodo(Periodo.MES)

        val ui = vm.ui.value
        // Derivados expuestos por el UiState
        assertTrue(ui.listaA.any { it.id == ingresoActual.id })
        assertTrue(ui.listaA.any { it.id == gastoActual.id })
        assertTrue(ui.listaA.none { it.id == ingresoFuera.id })

        assertEquals(150.0, ui.totA.ingresos, 0.0)
        assertEquals(40.0, ui.totA.gastosAbs, 0.0)
        assertEquals(110.0, ui.totA.saldo, 0.0)
    }
}

/** Regla para usar TestDispatcher como Dispatchers.Main en tests */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
