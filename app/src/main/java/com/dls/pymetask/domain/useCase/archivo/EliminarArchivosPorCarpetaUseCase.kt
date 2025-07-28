package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.repository.ArchivoRepository

class EliminarArchivosPorCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpetaId: String) {
        repository.eliminarArchivosPorCarpeta(carpetaId)
    }
}
