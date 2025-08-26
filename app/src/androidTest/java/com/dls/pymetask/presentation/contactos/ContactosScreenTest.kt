package com.dls.pymetask.presentation.contactos



import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.presentation.commons.UiText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactosScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavController
    private lateinit var viewModel: ContactoViewModel

    private val uiEvents = MutableSharedFlow<UiText>()
    private val contactosState = androidx.compose.runtime.mutableStateListOf<Contacto>()
    private val isUploadingState = androidx.compose.runtime.mutableStateOf(false)

    // Constantes para evitar literales
    private val TEST_ID_1 = "c1"
    private val TEST_NOMBRE_1 = "Alicia"
    private val TEST_TEL_1 = "600111222"
    private val TEST_TIPO_1 = "Cliente"

    private val TEST_ID_2 = "c2"
    private val TEST_NOMBRE_2 = "Bob"
    private val TEST_TEL_2 = "700333444"
    private val TEST_TIPO_2 = "Proveedor"

    @Before
    fun setup() {
        navController = mockk(relaxed = true)
        viewModel = mockk(relaxed = true)

        every { viewModel.uiEvent } returns uiEvents
        every { viewModel.contactos } returns contactosState
        every { viewModel.isUploading } returns isUploadingState
        every { viewModel.getContactos(any()) } answers { /* no-op */ }
    }

    private fun setContent() {
        composeRule.setContent {
            ContactosScreen(navController = navController, viewModel = viewModel)
        }
    }

    @Test
    fun muestraEmptyCuandoNoHayContactos() {
        contactosState.clear()
        setContent()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.contacts_empty_hint)
        ).assertIsDisplayed()
    }

    @Test
    fun muestraLoaderCuandoIsUploadingTrue() {
        contactosState.add(Contacto(id = TEST_ID_1, nombre = TEST_NOMBRE_1, telefono = TEST_TEL_1, tipo = TEST_TIPO_1))
        isUploadingState.value = true

        setContent()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun filtraPorNombreYTelefono() {
        contactosState.clear()
        contactosState.addAll(
            listOf(
                Contacto(id = TEST_ID_1, nombre = TEST_NOMBRE_1, telefono = TEST_TEL_1, tipo = TEST_TIPO_1),
                Contacto(id = TEST_ID_2, nombre = TEST_NOMBRE_2, telefono = TEST_TEL_2, tipo = TEST_TIPO_2),
            )
        )
        setContent()

        // Escribimos "Alicia" en el buscador
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_search_placeholder))
            .performTextInput(TEST_NOMBRE_1)

        composeRule.onNodeWithText(TEST_NOMBRE_1).assertIsDisplayed()
        composeRule.onNodeWithText(TEST_NOMBRE_2).assertDoesNotExist()
    }

    @Test
    fun clicEnCardNavegaADetalle() {
        contactosState.clear()
        contactosState.add(Contacto(id = TEST_ID_1, nombre = TEST_NOMBRE_1, telefono = TEST_TEL_1, tipo = TEST_TIPO_1))
        setContent()

        composeRule.onNodeWithText(TEST_NOMBRE_1, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify { navController.navigate("detalle_contacto/$TEST_ID_1") }
    }

    @Test
    fun fabNavegaACrearContacto() {
        setContent()

        // Intentamos por CD "Añadir" (btn_add). Si tu FAB no la expone, añade un testTag en tu FAB y cámbialo aquí.
        val cdAdd = composeRule.activity.getString(R.string.btn_add)
        val clicked = runCatching {
            composeRule.onNodeWithContentDescription(cdAdd, useUnmergedTree = true).performClick()
        }.isSuccess || runCatching {
            // Fallback si añades testTag("fab_add_contact")
            composeRule.onNodeWithTag("fab_add_contact").performClick()
        }.isSuccess

        org.junit.Assert.assertTrue("No se pudo clicar el FAB de añadir", clicked)
        verify { navController.navigate("crear_contacto") }
    }
}
