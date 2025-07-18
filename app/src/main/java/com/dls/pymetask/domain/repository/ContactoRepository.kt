package com.dls.pymetask.domain.repository

import com.dls.pymetask.domain.model.Contacto

interface ContactoRepository {
    suspend fun getAllContactos(): List<Contacto>
    suspend fun deleteContactoById(id: String)
    // Otros métodos como addContacto(contacto) o updateContacto(contacto) se pueden agregar después
}
