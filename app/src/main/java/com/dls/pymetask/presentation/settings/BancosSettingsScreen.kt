package com.dls.pymetask.presentation.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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


        val accounts by viewModel.accounts.collectAsState()
        val selectedId by viewModel.selectedAccountId.collectAsState()

        LaunchedEffect(conectado) {
            if (conectado) viewModel.cargarCuentas()
        }

        if (conectado) {
            Spacer(Modifier.height(8.dp))
            Text("Cuentas vinculadas", style = MaterialTheme.typography.titleMedium)
            if (accounts.isEmpty()) {
                Text("No se han encontrado cuentas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                accounts.forEach { acc ->
                    val checked = acc.accountId == selectedId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.seleccionarCuenta(acc.accountId) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = checked, onClick = { viewModel.seleccionarCuenta(acc.accountId) })
                        Spacer(Modifier.width(8.dp))
                        Text(acc.displayName ?: acc.accountId)
                        Spacer(Modifier.weight(1f))
                        Text(acc.currency ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

    }
}
