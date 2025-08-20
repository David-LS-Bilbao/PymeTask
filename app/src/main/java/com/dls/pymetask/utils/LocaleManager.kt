package com.dls.pymetask.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Utilidad para aplicar un Locale (idioma) a un Context.
 * - Llamar cuando el usuario cambie el idioma en Ajustes.
 * - Llamar al iniciar la app para restaurar el idioma guardado.
 */
object LocaleManager {

    /**
     * Aplica el locale indicado (ej. "es", "en", "fr") y devuelve un contexto actualizado.
     */
    fun setLocale(base: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: se recomienda setLocales, pero para compatibilidad mantenemos una asignación directa.
            config.setLocales(android.os.LocaleList.forLanguageTags(langCode))
        } else {
            config.setLocale(locale)
        }
        return base.createConfigurationContext(config)
    }
}
