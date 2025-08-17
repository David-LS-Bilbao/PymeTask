package com.dls.pymetask.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dls.pymetask.R
import com.dls.pymetask.data.preferences.ThemeMode


val Poppins = FontFamily(Font(R.font.poppins_semibold))
val Roboto = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),

)



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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = OnBluePrimary,
    primaryContainer = BlueContainer,
    onPrimaryContainer = OnBlueContainer,

    secondary = BlueGreySecondary,
    onSecondary = OnBlueGreySecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    background = BackgroundLight,
    onBackground = OnBackgroundLight,

    surface = SurfaceLight,
    onSurface = OnSurfaceLight,

    error = ErrorRed,
    onError = OnError
)



// Theme.kt
private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    error = Color(0xFFDC2626),
    onError = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,              // <- clave: BLANCO
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0), // gris muy claro
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = Color(0xFF2563EB)     // ok para elevación (no tiñe si Card usa 0dp tonal)
)

private val PymeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun PymeTaskTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // 👉 evita dynamicLightColorScheme(...) para no heredar morados del sistema
    val colorScheme = if (darkTheme) DarkColorScheme /* tu dark */ else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PymeTypography,
        shapes = PymeShapes,
        content = content
    )
}















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
//    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = PymeTypography,
//        content = content
//    )
//}
