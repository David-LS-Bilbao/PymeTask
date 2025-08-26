package com.dls.pymetask.presentation.contactos

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactIntentBuilderInstrumentedTest {

    @Test fun call_intent_ok() {
        val i = ContactIntentBuilder.callIntent("600111222")
        assertEquals(Intent.ACTION_DIAL, i.action)
        assertEquals("tel:600111222", i.dataString)
    }

    @Test fun sms_intent_ok() {
        val i = ContactIntentBuilder.smsIntent("600111222", "hola")
        assertEquals(Intent.ACTION_SENDTO, i.action)
        assertEquals("smsto:600111222", i.dataString)
        assertEquals("hola", i.getStringExtra("sms_body"))
    }

    @Test fun email_intent_ok() {
        val i = ContactIntentBuilder.emailIntent("alice@example.com", "Asunto", "Cuerpo")
        assertEquals(Intent.ACTION_SENDTO, i.action)
        assertEquals("mailto:alice@example.com", i.dataString)
        assertEquals("Asunto", i.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Cuerpo", i.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test fun whatsapp_intent_ok() {
        val i = ContactIntentBuilder.whatsappIntent("+34600111222", "Hola")
        assertEquals(Intent.ACTION_VIEW, i.action)
        assertEquals("https://wa.me/+34600111222", i.dataString)
        assertEquals("com.whatsapp", i.`package`)
    }
}
