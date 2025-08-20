package com.dls.pymetask.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Aplica el idioma a la app y refresca la UI.
 * - Android 13+: AppCompat recrea automáticamente las actividades.
 * - Android 12-: recreamos la Activity visible.
 */
fun setLanguageAndRefresh(context: Context, code: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Per-app language: el propio AppCompat fuerza recreation.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    } else {
        // Compat < 13: aplica al contexto y recrea la Activity actual
        LocaleManager.setLocale(context, code)
        context.findActivity()?.recreate()
    }
}

/** Busca la Activity real a partir de cualquier Context (incl. Compose) */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
