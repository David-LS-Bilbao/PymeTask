package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.repository.ArchivoRepository

class EliminarCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpetaId: String) {
        repository.eliminarCarpeta(carpetaId)
    }
}
