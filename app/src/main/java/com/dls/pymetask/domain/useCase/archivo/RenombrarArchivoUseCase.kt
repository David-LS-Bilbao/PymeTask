package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.repository.ArchivoRepository

class RenombrarArchivoUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(id: String, nuevoNombre: String) {
        repository.renombrarArchivo(id, nuevoNombre)
    }
    suspend operator fun invoke(id: String) {
        repository.eliminarArchivo(id)
    }
}
