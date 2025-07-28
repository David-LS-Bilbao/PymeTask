package com.dls.pymetask.domain.usecase.archivo


import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.repository.ArchivoRepository

class GuardarArchivoUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(archivo: Archivo) {
        repository.guardarArchivoEnFirestore(archivo)
    }
}
