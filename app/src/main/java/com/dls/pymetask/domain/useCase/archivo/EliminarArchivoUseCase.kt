package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.repository.ArchivoRepository

class EliminarArchivoUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(id: String) {
        repository.eliminarArchivo(id)
    }
}
