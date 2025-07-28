package com.dls.pymetask.domain.usecase.archivo

import android.net.Uri
import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.repository.ArchivoRepository
import jakarta.inject.Inject

class SubirArchivoUseCase @Inject constructor(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(uri: Uri, nombre: String): Archivo {
        return repository.subirArchivo(uri, nombre)
    }
}
