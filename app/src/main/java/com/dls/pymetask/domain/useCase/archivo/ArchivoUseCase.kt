package com.dls.pymetask.domain.useCase.archivo

import android.net.Uri
import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.repository.ArchivoRepository
import jakarta.inject.Inject

data class ArchivoUseCase(
    val crearCarpeta: CrearCarpetaUseCase,
    val eliminarCarpeta: EliminarCarpetaUseCase,
    val obtenerArchivos: ObtenerArchivosFirestoreUseCase,
    val renombrarArchivo: RenombrarArchivoUseCase,
    val subirArchivo: SubirArchivoUseCase,
    val guardarArchivo: GuardarArchivoUseCase,
    val eliminarArchivo: EliminarArchivoUseCase,
    val obtenerPorCarpeta: ObtenerArchivosPorCarpetaUseCase,
    val eliminarArchivosPorCarpeta: EliminarArchivosPorCarpetaUseCase
)

class CrearCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(nombre: String) {
        repository.crearCarpeta(nombre)
    }
}
class EliminarArchivosPorCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpetaId: String) {
        repository.eliminarArchivosPorCarpeta(carpetaId)
    }
}
class EliminarArchivoUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(id: String) {
        repository.eliminarArchivo(id)
    }
}
class EliminarCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpetaId: String) {
        repository.eliminarCarpeta(carpetaId)
    }
}
class GuardarArchivoUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(archivo: Archivo) {
        repository.guardarArchivoEnFirestore(archivo)
    }
}
class ListarArchivosUseCase @Inject constructor(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpeta: String = "archivos"): List<Archivo> {
        return repository.listarArchivos(carpeta)
    }
}
class ObtenerArchivosFirestoreUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(): List<Archivo> {
        return repository.obtenerArchivosDesdeFirestore()
    }
}
class ObtenerArchivosPorCarpetaUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(carpetaId: String): List<Archivo> {
        return repository.obtenerArchivosPorCarpeta(carpetaId)
    }
}
class RenombrarArchivoUseCase(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(id: String, nuevoNombre: String) {
        repository.renombrarArchivo(id, nuevoNombre)
    }
//    suspend operator fun invoke(id: String) {
//        repository.eliminarArchivo(id)
//    }
}
class SubirArchivoUseCase @Inject constructor(
    private val repository: ArchivoRepository
) {
    suspend operator fun invoke(uri: Uri, nombre: String): Archivo {
        return repository.subirArchivo(uri, nombre)
    }
}