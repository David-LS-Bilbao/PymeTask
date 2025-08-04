package com.dls.pymetask.presentation.agenda


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.usecase.tarea.TareaUseCases
import com.dls.pymetask.utils.AlarmUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val tareaUseCases: TareaUseCases,
    private val alarmUtils: AlarmUtils
) : ViewModel() {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas

    var tareaActual: Tarea? by mutableStateOf(null)
    private set

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    /**
     * Carga todas las tareas del usuario autenticado.
     */
    fun cargarTareas() {
        viewModelScope.launch {
            _loading.value = true
            _tareas.value = tareaUseCases.getTareas()
            _loading.value = false
        }
    }

    /**
     * Establece una tarea como seleccionada (para editar).
     */
    fun seleccionarTarea(id: String) {

        if (id == "tareas" || id.isBlank()) {
            Log.e("AgendaViewModel", "⚠️ ID inválido al seleccionar tarea: $id")
            return
        }
        viewModelScope.launch {
            tareaActual = tareaUseCases.getTarea(id)
            Log.d("AgendaViewModel", "✅ tarea cargada: ${tareaActual?.titulo}")
        }

    }
    fun guardarTarea(tarea: Tarea) {
        viewModelScope.launch {
          //  val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Log.e("AgendaViewModel", "❌ Usuario no autenticado en este dispositivo.")
                return@launch
            }



            val tareaConUsuario = tarea.copy(userId = userId)
            tareaUseCases.addTarea(tareaConUsuario)

            if (tareaConUsuario.activarAlarma) {
                alarmUtils.programarAlarma(tareaConUsuario)
            }

            cargarTareas()
        }
    }

    fun limpiarTareaActual() {
        tareaActual = null
    }

    fun eliminarTareaPorId(id: String) {
        viewModelScope.launch {
            tareaUseCases.deleteTarea(id)
            cargarTareas()
        }
    }
    fun actualizarFecha(nuevaFecha: String) {
        tareaActual = tareaActual?.copy(fecha = nuevaFecha)

    }
    fun actualizarHora(nuevaHora: String) {
        tareaActual = tareaActual?.copy(hora = nuevaHora)
    }




}


