package com.dls.pymetask.utils


import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

import androidx.core.os.LocaleListCompat

/**
 * Aplica el idioma a nivel de app teniendo en cuenta la versión de Android:
 * - Android 13+ (Tiramisu): AppCompatDelegate.setApplicationLocales(...)
 * - Anteriores: LocaleManager.setLocale(context, code)
 */
fun applyAppLanguage(context: Context, code: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Per-app language oficial (no requiere recrear si el sistema lo aplica, pero suele ser aconsejable)
        val locales = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(locales)
    } else {
        // Nuestro fallback para versiones antiguas
        LocaleManager.setLocale(context, code)
    }
}
