package com.dls.pymetask.domain.usecase

import app.cash.turbine.test
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.useCase.movimiento.*
import com.dls.pymetask.fakes.FakeMovimientoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientoUseCasesTest {

    private lateinit var repo: FakeMovimientoRepository
    private lateinit var use: MovimientoUseCases
    private val u1 = "userA"

    @Before
    fun setUp() {
        repo = FakeMovimientoRepository()
        val getAll = GetMovimientos(repo)
        val getBetween = GetMovimientosBetween(repo)
        val getEarliest = GetEarliestMovimientoMillis(repo)
        val add = AddMovimiento(repo)
        val update = UpdateMovimiento(repo)
        val delete = DeleteMovimiento(repo)
        use = MovimientoUseCases(
            getMovimientos = getAll,
            getMovimientosBetween = getBetween,
            getEarliestMovimientoMillis = getEarliest,
            addMovimiento = add,
            updateMovimiento = update,
            deleteMovimiento = delete
        )
    }

    private fun mov(
        id: String = UUID.randomUUID().toString(),
        titulo: String = "t",
        cantidad: Double = 10.0,
        ingreso: Boolean = true,
        fecha: Long,
        userId: String = u1
    ) = Movimiento(id, titulo, "", cantidad, ingreso, fecha, userId)

    @Test
    fun `insert-update-delete y flujo getMovimientos`() = runTest {
        val m1 = mov(fecha = 1_700_000_000_000)
        val m2 = mov(fecha = 1_710_000_000_000)

        use.getMovimientos().test {
            // vacío inicial
            assertEquals(emptyList<Movimiento>(), awaitItem())

            // insert 1
            use.addMovimiento(m1)
            assertEquals(listOf(m1), awaitItem())

            // insert 2
            use.addMovimiento(m2)
            assertEquals(listOf(m1, m2), awaitItem())

            // update m1
            val m1u = m1.copy(titulo = "nuevo")
            use.updateMovimiento(m1u)
            assertEquals(listOf(m1u, m2), awaitItem())

            // delete m2
            use.deleteMovimiento(m2.id)
            assertEquals(listOf(m1u), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getBetween devuelve rango correcto y ordenado desc por fecha`() = runTest {
        val mOld = mov(fecha = 1_600_000_000_000)
        val mMid = mov(fecha = 1_700_000_000_000)
        val mNew = mov(fecha = 1_800_000_000_000)
        use.addMovimiento(mOld); use.addMovimiento(mMid); use.addMovimiento(mNew)

        val res = use.getMovimientosBetween(u1, 1_650_000_000_000, 1_850_000_000_000)
        assertEquals(listOf(mNew, mMid), res) // desc
    }

    @Test
    fun `getEarliest da el más antiguo del usuario`() = runTest {
        val m1 = mov(fecha = 5)
        val m2 = mov(fecha = 2)
        val m3 = mov(fecha = 9)
        use.addMovimiento(m1); use.addMovimiento(m2); use.addMovimiento(m3)

        val earliest = use.getEarliestMovimientoMillis(u1)
        assertEquals(2L, earliest)
    }
}
