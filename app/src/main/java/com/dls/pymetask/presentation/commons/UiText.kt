package com.dls.pymetask.presentation.commons


import android.content.Context
import androidx.annotation.StringRes

/**
 * Wrapper de texto para poder emitir mensajes localizables desde los ViewModel.
 * - StringResource: referencia a un string con argumentos (como lista).
 * - DynamicString: texto ya resuelto (p. ej., de backend).
 */
sealed class UiText {

    /**
     * Refiere a un recurso string con argumentos opcionales.
     * NOTA: usamos List<Any> (no vararg) para cumplir con K2 en data classes.
     */
    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText()

    /** Texto dinámico ya resuelto (no localizable). */
    data class DynamicString(val value: String) : UiText()

    /**
     * Convierte el UiText a String con el contexto actual.
     * Si hay args, los expande como vararg al getString(...).
     */
    fun asString(context: Context): String = when (this) {
        is StringResource -> if (args.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *args.toTypedArray())
        }
        is DynamicString -> value
    }

    companion object {
        /**
         * Helper para crear un UiText con vararg en el llamador (cómodo en los ViewModel).
         * Internamente lo guardamos como List<Any> para cumplir con K2.
         */
        fun res(@StringRes id: Int, vararg args: Any): UiText =
            StringResource(id, args.toList())
    }
}
