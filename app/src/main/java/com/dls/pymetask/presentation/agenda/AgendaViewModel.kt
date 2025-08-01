package com.dls.pymetask.presentation.agenda


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.usecase.tarea.TareaUseCases
import com.dls.pymetask.utils.AlarmUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val tareaUseCases: TareaUseCases,
    private val firestore: FirebaseFirestore,
    private val alarmUtils: AlarmUtils
) : ViewModel() {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas.asStateFlow()
    private val _tareaActual = MutableStateFlow<Tarea?>(null)
    val tareaActual: StateFlow<Tarea?> = _tareaActual.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private val _uiState = MutableStateFlow(Tarea())
    val uiState: StateFlow<Tarea> = _uiState.asStateFlow()

    init {
        cargarTareas()
    }

    /**
     * Carga todas las tareas del usuario autenticado.
     */
    fun cargarTareas() {
        viewModelScope.launch {
            tareaUseCases.obtenerTareas(userId).collect { lista ->
                _tareas.value = lista.sortedBy { it.fecha }
            }
        }
    }

    /**
     * Establece una tarea como seleccionada (para editar).
     */
    fun seleccionarTarea(tarea: Tarea?) {
        _tareaActual.value = tarea
    }

    /**
     * Guarda o actualiza una tarea.
     */


    @SuppressLint("ScheduleExactAlarm")
    fun guardarTarea(tarea: Tarea, activarAlarma: Boolean) {
        val docRef = if (tarea.id.isNotBlank()) {
            firestore.collection("tareas").document(tarea.id) // actualización
        } else {
            firestore.collection("tareas").document() // nueva tarea
        }

        val tareaConId = if (tarea.id.isBlank()) {
            tarea.copy(id = docRef.id)
        } else {
            tarea
        }

        docRef.set(tareaConId)
            .addOnSuccessListener {
                Log.d("GuardarTarea", "Tarea guardada con ID: ${tareaConId.id}")
                _uiState.value = Tarea() // limpiar
            }
            .addOnFailureListener {
                Log.e("GuardarTarea", "Error al guardar", it)
            }
    }





    /**
     * Elimina una tarea por su ID.
     */
    fun eliminarTarea(tareaId: String) {
        viewModelScope.launch {
            _loading.value = true
            tareaUseCases.eliminarTarea(tareaId)
            _loading.value = false
        }
    }


    fun cargarTarea(id: String) {
        _loading.value = true

        firestore.collection("tareas").document(id).get()
            .addOnSuccessListener { doc ->
                val tarea = doc.toObject(Tarea::class.java)
                if (tarea != null) {
                    val tareaConId = tarea.copy(id = doc.id) // ✅ Asignar ID del documento
                    _uiState.value = tareaConId
                    Log.d("CargarTarea", "Tarea cargada correctamente: ${tareaConId.titulo}")
                } else {
                    Log.w("CargarTarea", "El documento existe pero no se pudo convertir a Tarea")
                }
                _loading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("CargarTarea", "Error al cargar la tarea con ID $id", e)
                _loading.value = false
            }
    }




    fun reiniciarFormulario() {
        _uiState.value = Tarea() // una nueva instancia vacía
    }

    fun actualizarTitulo(nuevo: String) {
        _uiState.update { it.copy(titulo = nuevo) }
    }

    fun actualizarDescripcion(nuevo: String) {
        _uiState.update { it.copy(descripcion = nuevo) }
    }

    fun actualizarDescripcionLarga(nuevo: String) {
        _uiState.update { it.copy(descripcionLarga = nuevo) }
    }

    fun actualizarFecha(nueva: String) {
        _uiState.update { it.copy(fecha = nueva) }
    }

    fun actualizarHora(nueva: String) {
        _uiState.update { it.copy(hora = nueva) }
    }

    fun actualizarCompletado(valor: Boolean) {
        _uiState.update { it.copy(completado = valor) }
    }

    fun actualizarActivarAlarma(valor: Boolean) {
        _uiState.update { it.copy(activarAlarma = valor) }
    }





}


