package com.dls.pymetask.domain.usecase

import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.domain.repository.ContactoRepository

class GetContactosUseCase(private val repository: ContactoRepository) {
    suspend operator fun invoke(): List<Contacto> {
        return repository.getAllContactos()
    }
}

class DeleteContactoUseCase(private val repository: ContactoRepository) {
    suspend operator fun invoke(id: String) {
        repository.deleteContactoById(id)
    }
}
