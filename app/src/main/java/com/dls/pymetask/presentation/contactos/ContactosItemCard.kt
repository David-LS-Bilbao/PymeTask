
package com.dls.pymetask.presentation.contactos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // <-- i18n en Compose
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Contacto

/**
 * Tarjeta de contacto.
 * - i18n: tipo (Cliente/Proveedor) y contentDescription accesible.
 * - Se mantiene la firma y la lógica original.
 */
@Composable
fun ContactoItemCard(
    contacto: Contacto,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Evita acoplar el idioma a valores almacenados: normaliza a boolean.
    val isCliente = contacto.tipo.equals("Cliente", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCliente) Color(0xFFE3F2FD) else Color(0xFFFFF3E0)
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
                    contentDescription = stringResource(R.string.contact_photo_cd, contacto.nombre),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isCliente) Color(0xFF90CAF9) else Color(0xFFFFCC80)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contacto.nombre.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contacto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Tipo localizado
                Text(
                    text = if (isCliente)
                        stringResource(R.string.contact_type_client)
                    else
                        stringResource(R.string.contact_type_supplier),
                    fontSize = 14.sp
                ) }
        }
    }
}

