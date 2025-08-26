package com.dls.pymetask.presentation.contactos



import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
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

class EditarContactoScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavController
    private lateinit var viewModel: ContactoViewModel

    private val uiEvents = MutableSharedFlow<UiText>()
    private val contactosState = androidx.compose.runtime.mutableStateListOf<Contacto>()
    private val isUploadingState = androidx.compose.runtime.mutableStateOf(false)

    private val TEST_ID = "cid-123"
    private val NOMBRE_ORIG = "Proveedor X"
    private val TEL_ORIG = "699888777"
    private val EMAIL_ORIG = "prov@x.com"
    private val DIRE_ORIG = "C/ Uno 1"
    private val TIPO_ORIG = "Proveedor"

    private val NOMBRE_NEW = "Proveedor Y"

    @Before
    fun setup() {
        navController = mockk(relaxed = true)
        viewModel = mockk(relaxed = true)

        every { viewModel.uiEvent } returns uiEvents
        every { viewModel.contactos } returns contactosState
        every { viewModel.isUploading } returns isUploadingState
        every { viewModel.getContactos(any()) } answers { /* no-op */ }
        every { viewModel.onUpdateContacto(any(), any()) } just Awaits
    }

    private fun setContent(id: String = TEST_ID) {
        composeRule.setContent {
            EditarContactoScreen(
                navController = navController,
                contactoId = id,
                viewModel = viewModel
            )
        }
    }

    @Test
    fun muestraLoaderCuandoNoHayContactos() {
        contactosState.clear()
        setContent()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun muestraNotFoundSiIdNoExiste() {
        contactosState.clear()
        contactosState.add(Contacto(TEST_ID, NOMBRE_ORIG, TEL_ORIG, TIPO_ORIG, DIRE_ORIG, null, EMAIL_ORIG))
        setContent(id = "otro-id")

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_not_found))
            .assertIsDisplayed()
    }

    @Test
    fun editarYGuardar_llamaViewModelYVuelve() {
        contactosState.clear()
        val contacto = Contacto(TEST_ID, NOMBRE_ORIG, TEL_ORIG, TIPO_ORIG, DIRE_ORIG, null, EMAIL_ORIG)
        contactosState.add(contacto)
        setContent()

        // Espera a que los campos se inicialicen
        composeRule.onNodeWithText(NOMBRE_ORIG).assertIsDisplayed()

        // Cambiar nombre
        composeRule.onNodeWithText(NOMBRE_ORIG).performTextClearance()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_field_name))
            .performTextInput(NOMBRE_NEW)

        // Guardar
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.contacts_save_changes))
            .performClick()

        verify {
            viewModel.onUpdateContacto(
                any(),
                match<Contacto> { it.id == TEST_ID && it.nombre == NOMBRE_NEW }
            )
        }
        verify { navController.popBackStack() }
    }
}
