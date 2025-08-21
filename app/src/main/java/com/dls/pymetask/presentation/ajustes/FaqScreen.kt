package com.dls.pymetask.presentation.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dls.pymetask.R

/**
 * Pantalla de Preguntas Frecuentes (FAQ).
 * - Carga preguntas/respuestas desde arrays de strings localizados (ES/EN/FR).
 * - Buscador por texto.
 * - Tarjetas expandibles para cada pregunta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(navController: NavController) {
    val preguntas = stringArrayResource(R.array.faq_questions)
    val respuestas = stringArrayResource(R.array.faq_answers)

    // Emparejamos hasta el mínimo común por si hay desajustes.
    val items = remember(preguntas, respuestas) {
        preguntas.zip(respuestas).map { (q, a) -> FaqItem(q, a) }
    }

    var query by remember { mutableStateOf("") }
    val filtrados = remember(items, query) {
        if (query.isBlank()) items
        else items.filter {
            it.question.contains(query, ignoreCase = true) ||
                    it.answer.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.faq_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ExpandMore, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            // Buscador simple
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search)) },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtrados) { item ->
                    FaqCard(item = item)
                }
            }
        }
    }
}

/** Modelo simple para un ítem de FAQ. */
data class FaqItem(val question: String, val answer: String)

/**
 * Tarjeta expandible con pregunta y respuesta.
 * El estado de expansión se guarda por tarjeta.
 */
@Composable
private fun FaqCard(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
