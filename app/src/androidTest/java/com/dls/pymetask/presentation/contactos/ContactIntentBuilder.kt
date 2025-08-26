package com.dls.pymetask.presentation.contactos

import android.content.Intent
import android.net.Uri

object ContactIntentBuilder {
    fun callIntent(number: String): Intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
    fun smsIntent(number: String, body: String? = null): Intent =
        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")).apply {
            if (!body.isNullOrBlank()) putExtra("sms_body", body)
        }
    fun emailIntent(to: String, subject: String? = null, body: String? = null): Intent =
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$to")).apply {
            if (!subject.isNullOrBlank()) putExtra(Intent.EXTRA_SUBJECT, subject)
            if (!body.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, body)
        }
    // Variante común para WhatsApp: ACTION_VIEW + wa.me
    fun whatsappIntent(numberInternational: String, text: String? = null): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$numberInternational")).apply {
            if (!text.isNullOrBlank()) putExtra("chat", text)
            setPackage("com.whatsapp") // asegura que use WhatsApp si está instalado
        }
}