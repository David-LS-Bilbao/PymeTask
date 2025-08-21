package com.dls.pymetask.presentation.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dls.pymetask.R

/**
 * Pantalla de Instrucciones/Guía.
 * Renderiza un "markdown" muy sencillo a partir de un string localizado (R.string.instructions_markdown):
 * - Líneas que empiezan por "## " -> Título sección
 * - Líneas que empiezan por "- "  -> Viñetas
 * - Cualquier otra línea          -> Párrafo
 *
 * Mantiene el contenido en recursos por idioma para no depender de internet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsScreen(navController: NavController) {
    val md = stringResource(R.string.instructions_markdown)
    val bloques = remember(md) { parseSimpleMarkdown(md) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.instructions_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bloques) { block ->
                when (block) {
                    is Block.Header -> Text(
                        text = block.text,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    is Block.Bullet -> Text(
                        text = "• ${block.text}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    is Block.Paragraph -> Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/** Tipos de bloque que renderizamos. */
private sealed class Block {
    data class Header(val text: String) : Block()
    data class Bullet(val text: String) : Block()
    data class Paragraph(val text: String) : Block()
}

/**
 * Parser muy sencillo para nuestro "markdown".
 * No pretende cubrir casos complejos: solo títulos (##), viñetas (-) y párrafos.
 */
private fun parseSimpleMarkdown(md: String): List<Block> {
    if (md.isBlank()) return emptyList()
    val out = mutableListOf<Block>()
    md.lines().forEach { raw ->
        val line = raw.trim()
        when {
            line.startsWith("## ") -> out += Block.Header(line.removePrefix("## ").trim())
            line.startsWith("- ")  -> out += Block.Bullet(line.removePrefix("- ").trim())
            line.isBlank() -> {} // ignoramos líneas vacías
            else -> out += Block.Paragraph(line)
        }
    }
    return out
}
