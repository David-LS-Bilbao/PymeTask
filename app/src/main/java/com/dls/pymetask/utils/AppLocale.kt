// utils/AppLocale.kt
package com.dls.pymetask.utils

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLocale {
    /** Aplica el idioma y fuerza una recreación limpia de la Activity visible. */
    fun applyAndReload(activity: Activity, langTag: String) {
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (current != langTag) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langTag))
            // Forzamos el repintado inmediato sin salir de la app
            activity.recreate()
        }
    }
}
