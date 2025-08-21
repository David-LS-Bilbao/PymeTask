
package com.dls.pymetask.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dls.pymetask.R
import com.dls.pymetask.data.preferences.ThemeMode

/* ----------------------------- Fuentes ----------------------------- */
// Poppins para títulos y Roboto para textos
val Poppins = FontFamily(Font(R.font.poppins_semibold))
val Roboto = FontFamily(Font(R.font.roboto_regular, FontWeight.Normal))

/* --------------------------- Tipografía base --------------------------- */
// Tipografías base de PymeTask. Escalaremos estos estilos en tiempo de ejecución.
val PymeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    )
)

/* --------------------------- Color Schemes --------------------------- */
// Tema oscuro (mantengo tu definición original)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// Tema claro principal de la app (el que realmente usas). Dejo solo este para evitar duplicidades.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    error = Color(0xFFDC2626),
    onError = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,              // super importante: tarjetas limpias
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0), // gris claro para contenedores
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = Color(0xFF2563EB)     // elevación M3 sin teñir en exceso
)

/* ------------------------------ Shapes ------------------------------ */
private val PymeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

/* ------------------- Escalado de tipografías global ------------------- */
/**
 * Escala las fuentes del Typography base por el factor indicado, sin cambiar la familia ni el peso.
 * Solo escalamos `fontSize` para evitar sorpresas con `lineHeight` no especificadas.
 */
private fun Typography.scaled(factor: Float): Typography {
    // Helper local para evitar repetir código
    fun TextStyle.s(): TextStyle = copy(fontSize = (if (fontSize.value > 0f) fontSize * factor else fontSize))

    val base = this
    return base.copy(
        displayLarge = base.displayLarge.s(),
        bodyLarge = base.bodyLarge.s(),
        bodyMedium = base.bodyMedium.s(),
        labelLarge = base.labelLarge.s()
        // Si usas otros estilos (title*, headline*, etc.), añádelos aquí con .s()
    )
}

/* ------------------------------ Theme ------------------------------- */
/**
 * Tema de PymeTask.
 *
 * @param themeMode   Modo de tema: LIGHT / DARK / SYSTEM (persistido en tus prefs)
 * @param textScaleFactor  Factor global de tamaño de fuente (0.9f small, 1.0f medium, 1.15f large)
 *                         Este parámetro permite que Ajustes cambie el tamaño tipográfico de toda la app.
 */
@Composable
fun PymeTaskTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    textScaleFactor: Float = 1.0f, // <-- NUEVO: factor recibido desde ViewModel de ajustes
    content: @Composable () -> Unit
) {
    // 1) Resolución del modo oscuro según tus prefs/sistema
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // 2) Elegimos el esquema de colores: evitas dynamic* a propósito (coherencia visual)
    val colorScheme = if (darkTheme) DarkColorScheme else LightColors

    // 3) Calculamos la tipografía escalada solo si hace falta
    val typographyScaled = if (textScaleFactor == 1.0f) PymeTypography else PymeTypography.scaled(textScaleFactor)

    // 4) Aplicamos el tema Material 3
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typographyScaled,
        shapes = PymeShapes,
        content = content
    )
}




//package com.dls.pymetask.ui.theme
//
//import android.os.Build
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Shapes
//import androidx.compose.material3.Typography
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.dynamicDarkColorScheme
//import androidx.compose.material3.dynamicLightColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.dls.pymetask.R
//import com.dls.pymetask.data.preferences.ThemeMode
//
//
//val Poppins = FontFamily(Font(R.font.poppins_semibold))
//val Roboto = FontFamily(
//    Font(R.font.roboto_regular, FontWeight.Normal),
//
//)
//
//val PymeTypography = Typography(
//    displayLarge = TextStyle(
//        fontFamily = Poppins,
//        fontWeight = FontWeight.SemiBold,
//        fontSize = 22.sp
//    ),
//    bodyLarge = TextStyle(
//        fontFamily = Roboto,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp
//    ),
//    bodyMedium = TextStyle(
//        fontFamily = Roboto,
//        fontWeight = FontWeight.Normal,
//        fontSize = 14.sp
//    ),
//    labelLarge = TextStyle(
//        fontFamily = Roboto,
//        fontWeight = FontWeight.Medium,
//        fontSize = 16.sp
//    )
//)
//
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = BluePrimary,
//    onPrimary = OnBluePrimary,
//    primaryContainer = BlueContainer,
//    onPrimaryContainer = OnBlueContainer,
//
//    secondary = BlueGreySecondary,
//    onSecondary = OnBlueGreySecondary,
//    secondaryContainer = SecondaryContainer,
//    onSecondaryContainer = OnSecondaryContainer,
//
//    background = BackgroundLight,
//    onBackground = OnBackgroundLight,
//
//    surface = SurfaceLight,
//    onSurface = OnSurfaceLight,
//
//    error = ErrorRed,
//    onError = OnError
//)
//
//
//
//// Theme.kt
//private val LightColors = lightColorScheme(
//    primary = Color(0xFF2563EB),
//    onPrimary = Color.White,
//    secondary = Color(0xFF0EA5E9),
//    onSecondary = Color.White,
//    error = Color(0xFFDC2626),
//    onError = Color.White,
//    background = Color(0xFFF8FAFC),
//    onBackground = Color(0xFF0F172A),
//    surface = Color.White,              // <- clave: BLANCO
//    onSurface = Color(0xFF0F172A),
//    surfaceVariant = Color(0xFFE2E8F0), // gris muy claro
//    onSurfaceVariant = Color(0xFF475569),
//    surfaceTint = Color(0xFF2563EB)     // ok para elevación (no tiñe si Card usa 0dp tonal)
//)
//
//private val PymeShapes = Shapes(
//    small = RoundedCornerShape(10.dp),
//    medium = RoundedCornerShape(16.dp),
//    large = RoundedCornerShape(24.dp)
//)
//
//@Composable
//fun PymeTaskTheme(
//    themeMode: ThemeMode = ThemeMode.SYSTEM,
//    content: @Composable () -> Unit
//) {
//    val darkTheme = when (themeMode) {
//        ThemeMode.LIGHT -> false
//        ThemeMode.DARK -> true
//        ThemeMode.SYSTEM -> isSystemInDarkTheme()
//    }
//
//    // 👉 evita dynamicLightColorScheme(...) para no heredar morados del sistema
//    val colorScheme = if (darkTheme) DarkColorScheme /* tu dark */ else LightColors
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = PymeTypography,
//        shapes = PymeShapes,
//        content = content
//    )
//}
//
