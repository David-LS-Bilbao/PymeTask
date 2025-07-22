package com.dls.pymetask.presentation.contactos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.domain.model.Contacto

@Composable
fun ContactoItemCard(
    contacto: Contacto,
    onClick: () -> Unit,
    onDeleteClick: (Contacto) -> Unit

) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contacto.tipo == "Cliente") Color(0xFFE3F2FD) else Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto o inicial
            if (!contacto.fotoUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(contacto.fotoUrl),
                    contentDescription = "Foto de ${contacto.nombre}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (contacto.tipo == "Cliente") Color(0xFF90CAF9) else Color(0xFFFFCC80)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contacto.nombre.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(contacto.nombre, style = MaterialTheme.typography.titleMedium)
                Text("📞 ${contacto.telefono}", fontSize = 13.sp)
                Text("📧 ${contacto.email}", fontSize = 13.sp)
            }
        }
    }
}