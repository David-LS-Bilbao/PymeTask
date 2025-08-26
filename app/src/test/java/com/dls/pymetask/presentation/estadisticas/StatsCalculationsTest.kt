package com.dls.pymetask.presentation.estadisticas



import com.dls.pymetask.domain.model.Movimiento
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class StatsCalculationsTest {

    private fun mov(
        cantidad: Double,
        ingreso: Boolean,
        year: Int,
        month: Int,
        day: Int
    ) = Movimiento(
        id = UUID.randomUUID().toString(),
        titulo = "test",
        cantidad = cantidad,
        ingreso = ingreso,
        fecha = Calendar.getInstance().apply { set(year, month, day) }.timeInMillis,
        subtitulo = "",
        userId = "user1"
    )

    @Test
    fun totalesMes_calculaCorrecto() {
        val lista = listOf(
            mov(100.0, true, 2023, 0, 1),
            mov(50.0, false, 2023, 0, 2)
        )
        val totales = totalesMes(lista)
        assertEquals(100.0, totales.ingresos, 0.0)
        assertEquals(50.0, totales.gastosAbs, 0.0)
        assertEquals(50.0, totales.saldo, 0.0)
    }

    @Test
    fun seriesDiarias_generadoCorrecto() {
        val lista = listOf(
            mov(100.0, true, 2023, Calendar.JANUARY, 1),
            mov(50.0, false, 2023, Calendar.JANUARY, 1),
            mov(20.0, false, 2023, Calendar.JANUARY, 2)
        )
        val serie = seriesDiarias(lista, 2023, Calendar.JANUARY)
        assertEquals(31, serie.dias)
        assertEquals(100.0, serie.ingresosPorDia[0], 0.0)
        assertEquals(50.0, serie.gastosPorDia[0], 0.0)
        assertEquals(50.0, serie.saldoAcumulado[0], 0.0)
        assertEquals(30.0, serie.saldoAcumulado[1], 0.0)
    }

    @Test
    fun filtrarPorTipo_ingresosSolo() {
        val lista = listOf(
            mov(100.0, true, 2023, 0, 1),
            mov(50.0, false, 2023, 0, 1)
        )
        val soloIngresos = lista.filtrarPorTipo(FiltroTipo.INGRESO)
        assertEquals(1, soloIngresos.size)
        assertTrue(soloIngresos.first().ingreso)
    }
}
