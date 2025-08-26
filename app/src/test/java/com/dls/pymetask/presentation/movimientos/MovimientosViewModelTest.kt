package com.dls.pymetask.presentation.movimientos

import app.cash.turbine.test
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.useCase.movimiento.AddMovimiento
import com.dls.pymetask.domain.useCase.movimiento.DeleteMovimiento
import com.dls.pymetask.domain.useCase.movimiento.GetEarliestMovimientoMillis
import com.dls.pymetask.domain.useCase.movimiento.GetMovimientos
import com.dls.pymetask.domain.useCase.movimiento.GetMovimientosBetween
import com.dls.pymetask.domain.useCase.movimiento.MovimientoUseCases
import com.dls.pymetask.domain.useCase.movimiento.UpdateMovimiento
import com.dls.pymetask.fakes.FakeMovimientoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientosViewModelTest {

    private lateinit var vm: MovimientosViewModel
    private lateinit var repo: FakeMovimientoRepository
    private val user = "userA"

    private fun mov(fecha: Long, ingreso: Boolean = true, userId: String = user) =
        Movimiento(
            id = UUID.randomUUID().toString(),
            titulo = "t",
            subtitulo = "",
            cantidad = 10.0,
            ingreso = ingreso,
            fecha = fecha,
            userId = userId
        )

    @Before
    fun setUp() {
        repo = FakeMovimientoRepository()
        val use = MovimientoUseCases(
            getMovimientos = GetMovimientos(repo),
            getMovimientosBetween = GetMovimientosBetween(repo),
            getEarliestMovimientoMillis = GetEarliestMovimientoMillis(repo),
            addMovimiento = AddMovimiento(repo),
            updateMovimiento = UpdateMovimiento(repo),
            deleteMovimiento = DeleteMovimiento(repo)
        )
        vm = MovimientosViewModel(use)
    }

    @Test
    fun `startMonthPaging inicializa y loadNextMonth crea primera seccion`() = runTest {
        // Crea datos de mes actual
        val now = YearMonth.now(ZoneId.systemDefault())
        val from = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val mid = from + 86_400_000 // +1 día

        repo.insertMovimiento(mov(mid)) // uno en el mes actual

        vm.meses.test {
            // Estado inicial
            Assert.assertEquals(emptyList<MovimientosViewModel.MesSection>(), awaitItem())

            // Arranca paginación → debe emitir al menos una sección con el mes actual
            vm.startMonthPaging(user)
            val secciones = awaitItem()
            val s0 = secciones.first()
            Assert.assertEquals(now.year, s0.year)
            Assert.assertEquals(now.monthValue, s0.month)
            // contiene el movimiento insertado
            Assert.assertEquals(1, s0.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNextMonth avanza offset saltando meses vacios hasta encontrar datos`() = runTest {
        val now = YearMonth.now(ZoneId.systemDefault())
        val twoMonthsAgo = now.minusMonths(2)
        val from2 =
            twoMonthsAgo.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        repo.insertMovimiento(mov(from2 + 3_600_000)) // movimiento hace 2 meses

        vm.startMonthPaging(user) // crea (o no) la sección actual según datos
        vm.loadNextMonth(user)    // mes -1 (vacío)
        vm.loadNextMonth(user)    // mes -2 (debe encontrar datos)

        val sections = vm.meses.value
        val last = sections.last()
        Assert.assertEquals(twoMonthsAgo.year, last.year)
        Assert.assertEquals(twoMonthsAgo.monthValue, last.month)
        Assert.assertEquals(1, last.items.size)
    }
}