package com.dls.pymetask.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // <-- i18n en Compose
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.ArchivoUiModel

/**
 * Card de carpeta. Muestra icono + nombre y navega al tocar.
 * - i18n: contentDescription del icono localizado.
 * - UX: evita cortar el nombre con .take(20); usamos ellipsis.
 */
@Composable
fun CarpetaItemCard(
    archivo: ArchivoUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }, // Navega al contenido de la carpeta
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = stringResource(R.string.files_cd_folder), // "Carpeta/Folder/Dossier"
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = archivo.nombre,               // ← sin .take(20)
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,    // ← truncado elegante
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


