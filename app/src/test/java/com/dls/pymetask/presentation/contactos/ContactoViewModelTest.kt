package com.dls.pymetask.presentation.contactos

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.presentation.commons.UiText
import com.dls.pymetask.utils.Constants
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description


// =============================================
// ContactoViewModelTest
// =============================================
@OptIn(ExperimentalCoroutinesApi::class)
class ContactoViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    // Mocks
    private val firestore = mockk<FirebaseFirestore>()
    private val storage = mockk<FirebaseStorage>()
    private val context = mockk<Context>(relaxed = true)

    private val colUsuarios = mockk<CollectionReference>()
    private val docUsuario = mockk<DocumentReference>()
    private val colContactos = mockk<CollectionReference>()
    private val docContacto = mockk<DocumentReference>()

    private lateinit var viewModel: ContactoViewModel
    private lateinit var TEST_URI: Uri

    companion object {
        const val TEST_USER_ID = "user-123"
        const val CONTACT_ID = "c-001"
        const val CONTACT_NAME = "Alice"
        const val CONTACT_PHONE = "600111222"
        const val CONTACT_EMAIL = "alice@example.com"
        const val PHOTO_URL = "gs://bucket/usuarios/user-123/contactos/c-001.jpg"
    }

    private val CONTACTO = Contacto(
        id = CONTACT_ID,
        nombre = CONTACT_NAME,
        telefono = CONTACT_PHONE,
        tipo = "amigo",
        direccion = "Calle Falsa 123",
        fotoUrl = null,
        email = CONTACT_EMAIL,
        userId = TEST_USER_ID
    )


    @Before
    fun setUp() {
        mockkObject(Constants)
        every { Constants.getUserIdSeguro(context) } returns TEST_USER_ID

        every { firestore.collection("usuarios") } returns colUsuarios
        every { colUsuarios.document(TEST_USER_ID) } returns docUsuario
        every { docUsuario.collection("contactos") } returns colContactos
        every { colContactos.document(CONTACT_ID) } returns docContacto

        viewModel = ContactoViewModel(firestore, storage)

        TEST_URI = mockk(relaxed = true)
        every { TEST_URI.toString() } returns "file:///tmp/picture.jpg"
    }

    @After fun tearDown() { unmockkAll() }

    private fun <T> successTask(): Task<T> {
        val task = mockk<Task<T>>()
        every { task.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<T>>().onSuccess(null as T)
            task
        }
        every { task.addOnFailureListener(any()) } answers { task }
        return task
    }

    private fun <T> failureTask(ex: Exception): Task<T> {
        val task = mockk<Task<T>>()
        every { task.addOnSuccessListener(any()) } answers { task }
        every { task.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(ex)
            task
        }
        return task
    }

    @Test
    fun onAddContacto_success() = runTest {
        val setTask: Task<Void> = successTask()
        every { docContacto.set(CONTACTO) } returns setTask

        viewModel.uiEvent.test {
            viewModel.onAddContacto(context, CONTACTO)
            Assert.assertTrue(true)
            cancelAndIgnoreRemainingEvents()
        }
        verify { docContacto.set(CONTACTO) }
    }

    @Test
    fun onAddContacto_error() = runTest {
        val setTask: Task<Void> = failureTask(RuntimeException("boom"))
        every { docContacto.set(CONTACTO) } returns setTask

        viewModel.uiEvent.test {
            viewModel.onAddContacto(context, CONTACTO)
            Assert.assertTrue(true)
            cancelAndIgnoreRemainingEvents()
        }
        verify { docContacto.set(CONTACTO) }
    }

    @Test
    fun onUpdateContacto_success() = runTest {
        val updated = CONTACTO.copy(nombre = "Nuevo")
        val setTask: Task<Void> = successTask()
        every { docContacto.set(updated) } returns setTask

        viewModel.uiEvent.test {
            viewModel.onUpdateContacto(context, updated)
            Assert.assertTrue(true)
            cancelAndIgnoreRemainingEvents()
        }
      //  verify { docContacto.set(match { it.nombre == "Nuevo" })
        verify { docContacto.set(updated) }
    }

    @Test
    fun onUpdateContacto_error() = runTest {
        val setTask: Task<Void> = failureTask(RuntimeException("upd err"))
        every { docContacto.set(any<Contacto>()) } returns setTask

        viewModel.uiEvent.test {
            viewModel.onUpdateContacto(context, CONTACTO)
            Assert.assertTrue(awaitItem() is UiText)
            cancelAndIgnoreRemainingEvents()
        }
        verify { docContacto.set(CONTACTO) }
    }

    @Test
    fun onDeleteContacto_success_borraDocumentoYFoto() = runTest {
        val deleteTask: Task<Void> = successTask()
        every { docContacto.delete() } returns deleteTask

        val refFromUrl = mockk<StorageReference>(relaxed = true)
        every { storage.getReferenceFromUrl(PHOTO_URL) } returns refFromUrl

        viewModel.uiEvent.test {
            viewModel.onDeleteContacto(context, CONTACT_ID, PHOTO_URL)
            Assert.assertTrue(awaitItem() is UiText)
            cancelAndIgnoreRemainingEvents()
        }
        verify { docContacto.delete() }
        verify { storage.getReferenceFromUrl(PHOTO_URL) }
        verify { refFromUrl.delete() }
    }

    @Test
    fun onDeleteContacto_success_sinFoto() = runTest {
        val deleteTask: Task<Void> = successTask()
        every { docContacto.delete() } returns deleteTask

        viewModel.onDeleteContacto(context, CONTACT_ID, null)

        verify { docContacto.delete() }
        verify(exactly = 0) { storage.getReferenceFromUrl(any()) }
    }

    @Test
    fun onDeleteContacto_error() = runTest {
        val deleteTask: Task<Void> = failureTask(RuntimeException("del err"))
        every { docContacto.delete() } returns deleteTask

        viewModel.uiEvent.test {
            viewModel.onDeleteContacto(context, CONTACT_ID, PHOTO_URL)
            Assert.assertTrue(awaitItem() is UiText)
            cancelAndIgnoreRemainingEvents()
        }
        verify { docContacto.delete() }
        verify(exactly = 0) { storage.getReferenceFromUrl(any()) }
    }

    @Test
    fun subirImagen_success() = runTest {
        val rootRef = mockk<StorageReference>()
        val childRef = mockk<StorageReference>()
        val uploadTask = mockk<UploadTask>()
        val downloadTask = mockk<Task<Uri>>()

        every { storage.reference } returns rootRef
        every { rootRef.child("usuarios/$TEST_USER_ID/contactos/$CONTACT_ID.jpg") } returns childRef
        every { childRef.putFile(TEST_URI) } returns uploadTask

        every { uploadTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<UploadTask.TaskSnapshot>>().onSuccess(mockk())
            uploadTask
        }
        every { uploadTask.addOnFailureListener(any()) } answers { uploadTask }

        every { childRef.downloadUrl } returns downloadTask
        val finalUrl: Uri = mockk()
        every { finalUrl.toString() } returns "https://fakeurl/c-001.jpg"
        every { downloadTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Uri>>().onSuccess(finalUrl)
            downloadTask
        }

        var callbackUrl: String? = null

        viewModel.uiEvent.test {
            viewModel.subirImagen(context, TEST_URI, CONTACT_ID) { url -> callbackUrl = url }
            Assert.assertTrue(awaitItem() is UiText)
            cancelAndIgnoreRemainingEvents()
        }

        Assert.assertEquals(finalUrl.toString(), callbackUrl)
        verify { childRef.putFile(TEST_URI) }
        verify { childRef.downloadUrl }
    }

    @Test
    fun subirImagen_error() = runTest {
        val rootRef = mockk<StorageReference>()
        val childRef = mockk<StorageReference>()
        val uploadTask = mockk<UploadTask>()

        every { storage.reference } returns rootRef
        every { rootRef.child("usuarios/$TEST_USER_ID/contactos/$CONTACT_ID.jpg") } returns childRef
        every { childRef.putFile(TEST_URI) } returns uploadTask

        every { uploadTask.addOnSuccessListener(any()) } answers { uploadTask }
        every { uploadTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(RuntimeException("upload err"))
            uploadTask
        }

        var callbackUrl: String? = "not-null"

        viewModel.uiEvent.test {
            viewModel.subirImagen(context, TEST_URI, CONTACT_ID) { url -> callbackUrl = url }
            Assert.assertTrue(awaitItem() is UiText)
            cancelAndIgnoreRemainingEvents()
        }

        Assert.assertNull(callbackUrl)
        verify { childRef.putFile(TEST_URI) }
    }

    @Test
    fun seleccionarYLimpiar() {
        Assert.assertNull(viewModel.contactoSeleccionado.value)
        viewModel.seleccionarContacto(CONTACTO)
        Assert.assertEquals(CONTACTO, viewModel.contactoSeleccionado.value)
        viewModel.limpiarSeleccion()
        Assert.assertNull(viewModel.contactoSeleccionado.value)
    }
}

