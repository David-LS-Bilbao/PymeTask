package com.dls.pymetask.rules


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Regla JUnit que sustituye el Dispatcher Main por uno de pruebas.
 * Así los ViewModels/Repos que usen Dispatchers.Main funcionan en JVM tests.
 */
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestRule {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            // Antes del test: redirige Dispatchers.Main al dispatcher de pruebas
            Dispatchers.setMain(dispatcher)
            try {
                // Ejecuta el test
                base.evaluate()
            } finally {
                // Después del test: restaura Dispatchers.Main
                Dispatchers.resetMain()
            }
        }
    }
}