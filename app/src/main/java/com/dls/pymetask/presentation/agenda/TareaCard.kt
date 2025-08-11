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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.utils.NotificationHelper
import com.dls.pymetask.utils.formatAsDayMonth

@Composable
fun TareaCard(
    isBlinking: Boolean,                 // ← controla si este item parpadea
    tarea: Tarea,
    onOpenTask: (String) -> Unit, // lambda para navegar (ej: "tarea_form?taskId=$id")
) {
    val context = LocalContext.current


    // Animación infinita: alterna 0f ⇄ 1f cada 600 ms
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

    // Colores de parpadeo (blanco ⇄ amarillo suave)
    val blinkA = Color.White
    val blinkB = Color(0xFFFFF59D) // Yellow 200 aprox.


    // Si parpadea, interpolamos entre A y B; si no, usamos el color normal del card.
    val containerColor = if (isBlinking) {
        lerp(blinkA, blinkB, pulse)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = if (tarea.completado) Color(0xFFD0F0C0) else containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

                // 1) Parar el tono en reproducción (si lo hubiera)
                NotificationHelper.stopAlarmSound()
                // 2) Cerrar la notificación activa (id=1 en tu helper)
                NotificationHelper.cancelActiveAlarmNotification(context)
                // 3) Navegar al detalle/edición de la tarea
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
                    contentDescription = "Estado",
                    tint = if (tarea.completado) Color(0xFF4CAF50) else Color.Gray
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Poppins)
                )
            }

            // 📝 Descripción
            if (tarea.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(tarea.descripcion, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔔 Icono de alarma
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (tarea.activarAlarma) {
                    Icon(
                        imageVector = Icons.Default.Alarm, // Puedes cambiar por otro como Icons.Default.Alarm
                        contentDescription = "Alarma activa",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}