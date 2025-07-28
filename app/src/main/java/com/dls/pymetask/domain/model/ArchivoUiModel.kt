package com.dls.pymetask.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ArchivoUiModel(
    val id: String,
    val nombre: String,
    val tipo: String,
    val icono: ImageVector,
    val fechaFormateada: String,
    val url: String
)
