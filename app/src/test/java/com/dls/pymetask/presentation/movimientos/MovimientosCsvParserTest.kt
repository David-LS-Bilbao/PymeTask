package com.dls.pymetask.presentation.movimientos

import com.dls.pymetask.presentation.movimientos.util.parseCsvLines
import org.junit.Assert
import org.junit.Test

/**
 * Si parseCsvInternal(...) es 'internal' o 'private' en tu VM,
 * muévelo a un helper en 'presentation/movimientos/util' para poder probarlo.
 * Aquí asumo una función parseCsvLines(lines: List<String>): List<ParsedCsvRow>.
 */
class MovimientosCsvParserTest {

    @Test
    fun `parseCsvLines reconoce comas y puntos como decimal y extrae signo correcto`() {
        val lines = listOf(
            "01/08/2025;Ingreso nómina;1.234,56",   // ES: miles con punto, decimales con coma
            "03/08/2025;Pago factura;-123.45"      // EN: decimales con punto negativo
        )
        val rows = parseCsvLines(lines) // <-- implementa o exporta desde tu helper
        Assert.assertEquals(2, rows.size)
        Assert.assertEquals(1234.56, rows[0].amount, 0.001)
        Assert.assertEquals(-123.45, rows[1].amount, 0.001)
    }
}