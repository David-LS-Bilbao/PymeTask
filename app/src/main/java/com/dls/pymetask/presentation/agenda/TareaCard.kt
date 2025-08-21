package com.dls.pymetask.presentation.agenda

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.utils.NotificationHelper
import com.dls.pymetask.utils.formatAsDayMonth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

/**
 * Card de una tarea en la Agenda.
 * - isBlinking: bandera general (p.ej. activada desde Ajustes); aquí la filtramos
 *   para que SOLO parpadee si la tarea está vencida y no completada.
 * - tarea: datos de la tarea a mostrar.
 * - onOpenTask: callback para abrir el formulario (id de la tarea).
 */
@Composable
fun TareaCard(
    isBlinking: Boolean,
    tarea: Tarea,
    onOpenTask: (String) -> Unit,
) {
    val context = LocalContext.current

    // 1) Determinar si el parpadeo debe estar activo:
    //    - Respeta la bandera que recibimos (isBlinking)
    //    - Solo si la tarea NO está completada
    //    - Solo si la fecha/hora ya han pasado
    val blinkEnabled = remember(isBlinking, tarea.completado, tarea.fecha, tarea.hora) {
        isBlinking && !tarea.completado && isOverdue(tarea)
    }

    // 2) Animación de parpadeo (la usaremos solo si blinkEnabled == true)
    val transition = rememberInfiniteTransition(label = "blink")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseFraction"
    )

    // 3) Colores del efecto (blanco ⇄ amarillo suave)
    val blinkA = Color.White
    val blinkB = Color(0xFFFFF59D) // Amarillo suave

    // 4) Color del contenedor:
    //    - Si debe parpadear: interpolamos entre A y B
    //    - Si no: usamos el surface por defecto (el verde de "completado" se aplica en Card)
    val containerColor = if (blinkEnabled) lerp(blinkA, blinkB, pulse) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        // Si la tarea está completada, forzamos un fondo verde suave; si no, el calculado arriba
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completado) Color(0xFFD0F0C0) else containerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Al abrir: detener sonido/nota de alarma y navegar
                NotificationHelper.stopAlarmSound()
                NotificationHelper.cancelActiveAlarmNotification(context)
                onOpenTask(tarea.id)
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🗓 Fecha y Hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tarea.fecha.formatAsDayMonth(),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = Poppins)
                )
                Text(
                    text = tarea.hora,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = Poppins)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ✅ Título y estado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Task,
                    contentDescription = stringResource(R.string.agenda_cd_task_status),
                    tint = if (tarea.completado) Color(0xFF4CAF50) else Color.Gray
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Poppins)
                )
            }

            // 📝 Descripción (opcional)
            if (tarea.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tarea.descripcion,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔔 Icono de alarma (solo si está activa)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (tarea.activarAlarma) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = stringResource(R.string.agenda_cd_alarm_active),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Comprueba si la tarea está vencida.
 * - Intenta parsear fecha "YYYY-MM-DD" y hora "HH:mm".
 * - Si la hora está vacía o no parsea, asume 23:59 de ese día (vencimiento al final del día).
 * - Devuelve true si ahora es posterior a (fecha + hora).
 */
private fun isOverdue(tarea: Tarea): Boolean {
    return try {
        val date = LocalDate.parse(tarea.fecha)
        val time = try {
            if (tarea.hora.isBlank()) LocalTime.of(23, 59) else LocalTime.parse(tarea.hora)
        } catch (_: DateTimeParseException) {
            LocalTime.of(23, 59)
        }
        val due = LocalDateTime.of(date, time)
        LocalDateTime.now().isAfter(due)
    } catch (_: Exception) {
        false // Si no se puede evaluar, no parpadea
    }
}



//
//package com.dls.pymetask.presentation.agenda
//
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Alarm
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Task
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.lerp
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.stringResource // <-- i18n en Compose
//import androidx.compose.ui.unit.dp
//import com.dls.pymetask.R
//import com.dls.pymetask.domain.model.Tarea
//import com.dls.pymetask.ui.theme.Poppins
//import com.dls.pymetask.utils.NotificationHelper
//import com.dls.pymetask.utils.formatAsDayMonth
//
///**
// * Card de una tarea en la Agenda.
// * - isBlinking: si true, aplica una animación sutil de parpadeo al fondo.
// * - tarea: datos de la tarea a mostrar.
// * - onOpenTask: callback para navegar (ej: "tarea_form?taskId=$id").
// */
//@Composable
//fun TareaCard(
//    isBlinking: Boolean,
//    tarea: Tarea,
//    onOpenTask: (String) -> Unit,
//) {
//    val context = LocalContext.current
//
//    // Animación infinita para el parpadeo del fondo
//    val transition = rememberInfiniteTransition(label = "blink")
//    val pulse by transition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = 600, easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ),
//        label = "pulseFraction"
//    )
//
//    // Colores de parpadeo (blanco ⇄ amarillo suave)
//    val blinkA = Color.White
//    val blinkB = Color(0xFFFFF59D) // Yellow 200 aprox.
//
//    // Color del contenedor (con parpadeo opcional o surface por defecto).
//    val containerColor = if (isBlinking) {
//        lerp(blinkA, blinkB, pulse)
//    } else {
//        MaterialTheme.colorScheme.surface
//    }
//
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(4.dp),
//        // Si la tarea está completada, fondo verde suave; si no, el calculado.
//        colors = CardDefaults.cardColors(
//            containerColor = if (tarea.completado) Color(0xFFD0F0C0) else containerColor
//        ),
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                // Al abrir: detiene cualquier alarma en curso y cierra notificación activa
//                NotificationHelper.stopAlarmSound()
//                NotificationHelper.cancelActiveAlarmNotification(context)
//                onOpenTask(tarea.id)
//            }
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            // 🗓 Fecha y Hora (texto de datos, no se localiza)
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = tarea.fecha.formatAsDayMonth(), // utiliza tu extensión (idealmente sensible al locale)
//                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = Poppins)
//                )
//                Text(
//                    text = tarea.hora,
//                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = Poppins)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            // ✅ Título y estado (icono con contentDescription localizado)
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Default.Task,
//                    contentDescription = stringResource(R.string.agenda_cd_task_status), // "Estado" / "Task status" / "Statut de la tâche"
//                    tint = if (tarea.completado) Color(0xFF4CAF50) else Color.Gray
//                )
//                Spacer(Modifier.width(8.dp))
//                Text(
//                    text = tarea.titulo,
//                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Poppins)
//                )
//            }
//
//            // 📝 Descripción (si la hay)
//            if (tarea.descripcion.isNotBlank()) {
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = tarea.descripcion,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // 🔔 Icono de alarma (solo si está activa) con contentDescription localizado
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                if (tarea.activarAlarma) {
//                    Icon(
//                        imageVector = Icons.Default.Alarm,
//                        contentDescription = stringResource(R.string.agenda_cd_alarm_active), // "Alarma activa"
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//    }
//}
