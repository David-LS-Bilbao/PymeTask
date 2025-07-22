package com.dls.pymetask.presentation.contactos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Contacto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarContactoScreen(
    navController: NavController,
    contactoId: String,
    viewModel: ContactoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val contactos by viewModel.contactos.collectAsState()
    val contacto = contactos.find { it.id == contactoId }

    var nombre by remember { mutableStateOf(contacto?.nombre ?: "") }
    var telefono by remember { mutableStateOf(contacto?.telefono ?: "") }
    var direccion by remember { mutableStateOf(contacto?.direccion ?: "") }
    var tipo by remember { mutableStateOf(contacto?.tipo ?: "Cliente") }

    if (contacto == null) {
        Text("Contacto no encontrado")
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Contacto") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onUpdateContacto(
                    contacto.copy(
                        nombre = nombre,
                        telefono = telefono,
                        direccion = direccion,
                        tipo = tipo
                    )
                )
                navController.popBackStack()
            }) {
                Icon(Icons.Default.Check, contentDescription = "Guardar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
            OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") })

            Text("Tipo")
            Row {
                listOf("Cliente", "Proveedor").forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = tipo == it, onClick = { tipo = it })
                        Text(it)
                    }
                }
            }
        }
    }
}


