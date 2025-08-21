
package com.dls.pymetask.presentation.agenda

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <-- IMPORT para i18n en Compose
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.utils.MyFab
import com.dls.pymetask.utils.NotificationHelper

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    navController: NavController,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    // -- Contexto de Android para toasts/recursos
    val context = LocalContext.current

    // -- Observa el estado de la lista de tareas y el loading desde el ViewModel
    val tareas by viewModel.tareas.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    // -- Lifecycle para recargar tareas al volver a la pantalla
    val lifecycleOwner = LocalLifecycleOwner.current

    // -- Estado del diálogo de opciones de alarma
    var showDialog by remember { mutableStateOf(false) }

    // -- Si showDialog es true, muestra el diálogo para configurar tono/anticipación
    if (showDialog) AlarmOptionsDialog(
        initialToneUri = PreferencesHelper.getToneUri(context),
        initialLeadMinutes = PreferencesHelper.getLeadMinutes(context),
        onDismiss = { showDialog = false },
        onPickRingtone = { /* TODO: abrir selector de tonos */ },
        onLeadTimeChange = { minutes ->
            PreferencesHelper.saveLeadMinutes(context, minutes)
        }
    )

    // -- Efecto que observa eventos del ciclo de vida: al hacer ON_RESUME, recarga tareas
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarTareas()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ========================= DISEÑO DE PANTALLA =========================
    Scaffold(
        topBar = {
            TopAppBar(
                // -- Título localizado: "Agenda"
                title = {
                    Text(
                        text = stringResource(R.string.agenda_title),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                // -- Botón de navegación atrás: usa string localizado para contentDescription
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    // -- Acción: Detener alarma actual (sonido + notificación)
                    IconButton(
                        onClick = {
                            // 1) Detiene el sonido de alarma si está sonando
                            NotificationHelper.stopAlarmSound()
                            // 2) Cierra la notificación de alarma activa (si la hay)
                            NotificationHelper.cancelActiveAlarmNotification(context)
                            // Nota: esto NO desprograma alarmas futuras.
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsOff,
                            contentDescription = stringResource(R.string.agenda_stop_alarm)
                        )
                    }
                    // -- Acción: abrir diálogo de opciones de alarma (tono/anticipación)
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.agenda_alarm_options)
                        )
                    }
                }
            )
        },
        // -- FAB para crear una nueva tarea: navega al formulario
        floatingActionButton = {
            MyFab.Default(
                onClick = { navController.navigate("tarea_form") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when {
                // -- Muestra indicador de carga mientras se obtienen las tareas
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // -- Estado vacío: no hay tareas aún (texto localizado)
                tareas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stringResource(R.string.agenda_empty))
                        Log.d("AgendaScreen", "No tasks")
                    }
                }
                // -- Lista con tareas ordenadas por fecha y hora
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        // Ordena las tareas antes de pintarlas
                        val tareasOrdenadas = tareas.sortedWith(compareBy({ it.fecha }, { it.hora }))
                        items(tareasOrdenadas, key = { it.id }) { tarea ->
                            // -- Tarjeta de tarea; al pulsar, selecciona y navega a edición
                            TareaCard(tarea = tarea, isBlinking = true) {
                                viewModel.seleccionarTarea(tarea.id)
                                navController.navigate("tarea_form?taskId=${tarea.id}")
                                Log.d("AgendaScreen", "Tarea seleccionada: ${tarea.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}
