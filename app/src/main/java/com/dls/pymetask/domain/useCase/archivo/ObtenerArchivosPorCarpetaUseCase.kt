package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.repository.ArchivoRepository

class ObtenerArchivosPorCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpetaId: String): List<Archivo> {
        return repository.obtenerArchivosPorCarpeta(carpetaId)
    }
}
