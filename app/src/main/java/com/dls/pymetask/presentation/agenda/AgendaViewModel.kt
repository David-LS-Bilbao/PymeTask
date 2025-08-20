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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val tareaUseCases: TareaUseCases,
    private val alarmUtils: AlarmUtils,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas

    var tareaActual: Tarea? by mutableStateOf(null)
    private set

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // obtenemos el userId UNA sola vez
    private val userId: String = getUserIdSeguro(context)
        ?: throw IllegalStateException("Usuario no autenticado")

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarTareas() {
        viewModelScope.launch {
            _loading.value = true
            val lista = tareaUseCases.getTareas(userId)
            _tareas.value = lista
                .sortedWith(compareBy(
                    { it.fecha.toLocalDateOrNull() ?: LocalDate.MAX },
                    { it.hora.toLocalTimeOrNull() ?: LocalTime.MAX }
                ))
            _loading.value = false
        }
    }
    /**
     * Establece una tarea como seleccionada (para editar).
     */

    fun seleccionarTarea(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            tareaActual = tareaUseCases.getTarea(id, userId)
            Log.d("AgendaViewModel", "✅ tarea cargada: ${tareaActual?.titulo}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun guardarTarea(tarea: Tarea) {
        viewModelScope.launch {
            // Siempre guardamos con el userId
            val base = tarea.copy(userId = userId)

            // Si el usuario marca la tarea como completada, forzamos activarAlarma = false
            // y cancelamos cualquier alarma previa asociada a esa tarea.
            val efectiva = if (base.completado) {
                // Desactivar alarma al completar
                alarmUtils.cancelarAlarma(base.id)
                base.copy(activarAlarma = false)
            } else {
                base
            }

            // Persistimos en Firestore
            tareaUseCases.addTarea(efectiva, userId)

            // Gestionar la alarma en función del estado final:
            if (efectiva.activarAlarma) {
                // Programación idempotente (usa setExact y tu lógica de lead time si la tienes)
                alarmUtils.programarAlarma(efectiva)
            } else {
                // Aseguramos que no quede ninguna alarma huérfana
                alarmUtils.cancelarAlarma(efectiva.id)
            }

            cargarTareas()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun eliminarTareaPorId(id: String) {
        viewModelScope.launch {
            // Primero cancelar alarma asociada (si había)
            alarmUtils.cancelarAlarma(id)
            // Luego borrar en Firestore
            tareaUseCases.deleteTarea(id, userId)
            cargarTareas()
        }
    }


    fun limpiarTareaActual() {
        tareaActual = null
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


