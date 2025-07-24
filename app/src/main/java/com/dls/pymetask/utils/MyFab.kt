package com.dls.pymetask.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import com.dls.pymetask.R

object MyFab {
    @Composable
    fun Default(
        onClick: () -> Unit,
        icon: ImageVector = Icons.Default.Add,
        contentDescription: String = "Añadir",
       colorRes:Int = R.color.teal_200
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = colorResource(id = colorRes)
        ) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}