package com.dls.pymetask.presentation.agenda


import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.useCase.tarea.TareaUseCases
import com.dls.pymetask.utils.AlarmUtils
import com.dls.pymetask.utils.getUserIdSeguro
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
     * Carga todas las tareas del usuario autenticado
     * ordenadas por fecha y hora.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarTareas(context: Context) {
        viewModelScope.launch {
            _loading.value = true
            val userId = getUserIdSeguro(context)

            if (userId == null) {
                Log.e("AgendaViewModel", "❌ Usuario no autenticado en este dispositivo.")
                _loading.value = false
                return@launch
            }

            val tareasCrudas = tareaUseCases.getTareas()

            val tareasOrdenadas = tareasCrudas.sortedWith(compareBy(
                { it.fecha.toLocalDateOrNull() ?: LocalDate.MAX },
                { it.hora.toLocalTimeOrNull() ?: LocalTime.MAX }
            ))

            _tareas.value = tareasOrdenadas
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun guardarTarea(context: Context, tarea: Tarea) {
        viewModelScope.launch {

          // val userId = FirebaseAuth.getInstance().currentUser?.uid

            // obtenemos el id del usuario autenticado
            val userId = getUserIdSeguro(context)

            if (userId == null) {
                Log.e("AgendaViewModel", "❌ Usuario no autenticado en este dispositivo.")
                return@launch
            }
            val tareaConUsuario = tarea.copy(userId = userId)
            tareaUseCases.addTarea(tareaConUsuario)

            if (tareaConUsuario.activarAlarma) {
                alarmUtils.programarAlarma(tareaConUsuario)
            }
            cargarTareas(context)
        }

    }

    fun limpiarTareaActual() {
        tareaActual = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun eliminarTareaPorId(context: Context, id: String) {
        viewModelScope.launch {
            tareaUseCases.deleteTarea(id)
            cargarTareas(context)
        }
    }
    fun actualizarFecha(nuevaFecha: String) {
        tareaActual = tareaActual?.copy(fecha = nuevaFecha)

    }
    fun actualizarHora(nuevaHora: String) {
        tareaActual = tareaActual?.copy(hora = nuevaHora)
    }
}




@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDateOrNull(): LocalDate? =
    try {
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (e: Exception) {
        null
    }

@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalTimeOrNull(): LocalTime? =
    try {
        LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        null
}


