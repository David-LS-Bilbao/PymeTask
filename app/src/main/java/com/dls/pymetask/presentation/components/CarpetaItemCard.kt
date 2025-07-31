package com.dls.pymetask.presentation.components




import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.ArchivoUiModel

@Composable
fun CarpetaItemCard(
    archivo: ArchivoUiModel,
    onClick: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.blueCard))
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
                    contentDescription = "Carpeta",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = archivo.nombre.take(20),//
                    maxLines = 1,

                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

