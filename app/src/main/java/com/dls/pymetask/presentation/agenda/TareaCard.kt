package com.dls.pymetask.presentation.agenda

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.utils.NotificationHelper
import com.dls.pymetask.utils.formatAsDayMonth

@Composable
fun TareaCard(
    tarea: Tarea,
   // onClick: () -> Unit,
    onOpenTask: (String) -> Unit, // lambda para navegar (ej: "tarea_form?taskId=$id")
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = if (tarea.completado) Color(0xFFD0F0C0) else MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                //onClick()
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
                    imageVector = Icons.Default.CheckCircle,
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
























//
//@Composable
//fun TareaCard(
//    tarea: Tarea,
//    onClick: () -> Unit,
//) {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (tarea.completado) Color(0xFFD0F0C0) else MaterialTheme.colorScheme.surface
//        ),
//        modifier = Modifier.fillMaxWidth()
//            .clickable { onClick() }
//    ) {
//        // Contenido de la tarjeta
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Default.CheckCircle,
//                    contentDescription = "Estado",
//                    tint = if (tarea.completado) Color(0xFF4CAF50) else Color.Gray
//                )
//                Spacer(Modifier.width(8.dp))
//                Text(
//                    text = tarea.titulo,
//                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Poppins)
//                )
//            }
//            if (tarea.descripcion.isNotBlank()) {
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(tarea.descripcion, style = MaterialTheme.typography.bodySmall)
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(
//                horizontalArrangement = Arrangement.End,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//
//            }
//        }
//    }
//}
