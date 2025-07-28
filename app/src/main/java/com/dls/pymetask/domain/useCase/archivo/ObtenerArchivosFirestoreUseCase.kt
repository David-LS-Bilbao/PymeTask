package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.repository.ArchivoRepository

class ObtenerArchivosFirestoreUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(): List<Archivo> {
        return repository.obtenerArchivosDesdeFirestore()
    }
}
