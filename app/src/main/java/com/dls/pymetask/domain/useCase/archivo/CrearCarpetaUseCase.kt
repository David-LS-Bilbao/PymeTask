package com.dls.pymetask.domain.usecase.archivo

import com.dls.pymetask.domain.repository.ArchivoRepository

class CrearCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(nombre: String) {
        repository.crearCarpeta(nombre)
    }
}
