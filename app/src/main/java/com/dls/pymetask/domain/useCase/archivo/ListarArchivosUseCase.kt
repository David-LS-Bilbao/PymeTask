package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.repository.ArchivoRepository
import jakarta.inject.Inject

class ListarArchivosUseCase @Inject constructor(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpeta: String = "archivos"): List<Archivo> {
        return repository.listarArchivos(carpeta)
    }
}
