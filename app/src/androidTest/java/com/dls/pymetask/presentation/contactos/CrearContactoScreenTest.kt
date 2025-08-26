package com.dls.pymetask.presentation.contactos



import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.presentation.commons.UiText
import io.mockk.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CrearContactoScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavController
    private lateinit var viewModel: ContactoViewModel

    private val uiEvents = MutableSharedFlow<UiText>()
    private val isUploadingState = androidx.compose.runtime.mutableStateOf(false)

    // Constantes para evitar literales
    private val NOMBRE = "Cliente Demo"
    private val TELEFONO = "611222333"
    private val EMAIL = "cliente@demo.dev"
    private val DIRECCION = "Calle Demo 123"
    private val TIPO_PROVEEDOR = "Proveedor"

    @Before
    fun setup() {
        navController = mockk(relaxed = true)
        viewModel = mockk(relaxed = true)

        every { viewModel.uiEvent } returns uiEvents
        every { viewModel.isUploading } returns isUploadingState
        every { viewModel.onAddContacto(any(), any()) } just Awaits
    }

    private fun setContent() {
        composeRule.setContent {
            CrearContactoScreen(navController = navController, viewModel = viewModel)
        }
    }

    @Test
    fun botonGuardar_deshabilitado_hasta_nombreYTelefono() {
        setContent()

        // Por defecto deshabilitado
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_save))
            .assertIsNotEnabled()

        // Rellenamos sólo nombre -> sigue deshabilitado
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_field_name))
            .performTextInput(NOMBRE)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_save))
            .assertIsNotEnabled()

        // Añadimos teléfono -> habilitado
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_field_phone))
            .performTextInput(TELEFONO)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_save))
            .assertIsEnabled()
    }

    @Test
    fun guardar_contacto_llamaViewModelYVuelveAtras() {
        setContent()

        // Completar formulario
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_field_name))
            .performTextInput(NOMBRE)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_field_phone))
            .performTextInput(TELEFONO)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_field_address))
            .performTextInput(DIRECCION)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_field_email))
            .performTextInput(EMAIL)

        // Seleccionar tipo "Proveedor"
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contact_type_supplier))
            .performClick()

        // Guardar
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_save))
            .performClick()

        // Verificación sin literales: match sobre Contacto (el id es aleatorio en la Screen)
        verify {
            viewModel.onAddContacto(
                any(),
                match<Contacto> {
                    it.nombre == NOMBRE &&
                            it.telefono == TELEFONO &&
                            it.direccion == DIRECCION &&
                            it.email == EMAIL &&
                            it.tipo == TIPO_PROVEEDOR
                }
            )
        }
        verify { navController.popBackStack() }
    }
}
