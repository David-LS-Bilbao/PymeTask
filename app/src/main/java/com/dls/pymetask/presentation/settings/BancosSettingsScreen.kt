package com.dls.pymetask.presentation.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dls.pymetask.data.remote.bank.auth.OAuthManager

/**
 * Helper para obtener Activity de forma segura (evita crashear en Previews).
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Pantalla de Ajustes -> Bancos: permite conectar (OAuth) o desconectar (borrar tokens).
 * - Recibe OAuthManager por parámetro (ya lo pasas desde PymeTaskAppRoot -> NavGraph).
 * - Lee el estado 'conectado' del VM.
 */
@Composable
fun BancosSettingsScreen(
    oauthManager: OAuthManager,                                     // 👈 se pasa por parámetro desde el NavGraph
    viewModel: BancosSettingsViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val activity = remember { ctx.findActivity() }                  // 👈 necesaria para abrir Custom Tabs
    val conectado by viewModel.conectado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bancos",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = if (conectado) "Estado: Conectado" else "Estado: No conectado",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!conectado) {
            // Botón de CONECTAR: lanza Custom Tab con OAuth
            Button(
                onClick = { activity?.let { oauthManager.startAuth(it) } },
                enabled = activity != null
            ) {
                Text("Conectar banco")
            }
        } else {
            // Botón de DESCONECTAR: borra tokens del TokenStore
            OutlinedButton(onClick = { viewModel.desconectar() }) {
                Text("Desconectar")
            }
        }
        // TODO (siguiente iteración): listado de cuentas del proveedor y selección por defecto
        // TODO: mostrar “Última sincronización” por cuenta
    }
}
