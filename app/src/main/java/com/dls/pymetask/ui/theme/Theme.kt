package com.dls.pymetask.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PymeTypography,
        content = content
    )
}




//@Composable
//fun PymeTaskTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//        val context = LocalContext.current
//        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//      }
//      darkTheme -> DarkColorScheme
//      else -> LightColorScheme
//    }
//
//    MaterialTheme(
//      colorScheme = colorScheme,
//      typography = PymeTypography,
//      content = content
//    )
//}