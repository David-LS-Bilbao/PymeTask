package com.dls.pymetask.di

import com.dls.pymetask.data.repository.ArchivoRepositoryImpl
import com.dls.pymetask.domain.repository.ArchivoRepository
import com.dls.pymetask.domain.usecase.archivo.ArchivoUseCase
import com.dls.pymetask.domain.usecase.archivo.CrearCarpetaUseCase
import com.dls.pymetask.domain.usecase.archivo.EliminarArchivoUseCase
import com.dls.pymetask.domain.usecase.archivo.EliminarArchivosPorCarpetaUseCase
import com.dls.pymetask.domain.usecase.archivo.EliminarCarpetaUseCase
import com.dls.pymetask.domain.usecase.archivo.GuardarArchivoUseCase
import com.dls.pymetask.domain.usecase.archivo.ListarArchivosUseCase
import com.dls.pymetask.domain.usecase.archivo.ObtenerArchivosFirestoreUseCase
import com.dls.pymetask.domain.usecase.archivo.ObtenerArchivosPorCarpetaUseCase
import com.dls.pymetask.domain.usecase.archivo.RenombrarArchivoUseCase
import com.dls.pymetask.domain.usecase.archivo.SubirArchivoUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArchivoModule {

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideArchivoRepository(
        storage: FirebaseStorage,
        firestore: FirebaseFirestore
    ): ArchivoRepository = ArchivoRepositoryImpl(storage, firestore)

    @Provides
    fun provideListarArchivosUseCase(
        repository: ArchivoRepository
    ): ListarArchivosUseCase = ListarArchivosUseCase(repository)

    @Provides
    fun provideSubirArchivoUseCase(
        repository: ArchivoRepository
    ): SubirArchivoUseCase = SubirArchivoUseCase(repository)

    @Provides
    fun provideGuardarArchivoUseCase(
        repository: ArchivoRepository
    ): GuardarArchivoUseCase = GuardarArchivoUseCase(repository)

    @Provides
    fun provideObtenerArchivosFirestoreUseCase(
        repository: ArchivoRepository
    ): ObtenerArchivosFirestoreUseCase = ObtenerArchivosFirestoreUseCase(repository)

    @Provides
    fun provideCrearCarpetaUseCase(
        repository: ArchivoRepository
    ): CrearCarpetaUseCase = CrearCarpetaUseCase(repository)

    @Provides
    fun provideObtenerArchivosPorCarpetaUseCase(
        repository: ArchivoRepository
    ): ObtenerArchivosPorCarpetaUseCase = ObtenerArchivosPorCarpetaUseCase(repository)

    @Provides
    fun provideEliminarArchivoUseCase(
        repository: ArchivoRepository
    ): EliminarArchivoUseCase = EliminarArchivoUseCase(repository)
    @Provides
    fun provideEliminarCarpetaUseCase(
        repository: ArchivoRepository
    ): EliminarCarpetaUseCase = EliminarCarpetaUseCase(repository)
    @Provides
    fun provideRenombrarArchivoUseCase(
        repository: ArchivoRepository
    ): RenombrarArchivoUseCase = RenombrarArchivoUseCase(repository)

    @Provides
    fun provideEliminarArchivosPorCarpetaUseCase(
        repository: ArchivoRepository
    ): EliminarArchivosPorCarpetaUseCase = EliminarArchivosPorCarpetaUseCase(repository)


    @Provides
    fun provideArchivoUseCase(
        crearCarpeta: CrearCarpetaUseCase,
        eliminarCarpeta: EliminarCarpetaUseCase,
        obtenerArchivos: ObtenerArchivosFirestoreUseCase,
        renombrarArchivo: RenombrarArchivoUseCase,
        subirArchivo: SubirArchivoUseCase,
        guardarArchivo: GuardarArchivoUseCase,
        eliminarArchivo: EliminarArchivoUseCase,
        obtenerPorCarpeta: ObtenerArchivosPorCarpetaUseCase,
        eliminarArchivosPorCarpeta: EliminarArchivosPorCarpetaUseCase
    ): ArchivoUseCase {
        return ArchivoUseCase(
            crearCarpeta = crearCarpeta,
            eliminarCarpeta = eliminarCarpeta,
            obtenerArchivos = obtenerArchivos,
            renombrarArchivo = renombrarArchivo,
            subirArchivo = subirArchivo,
            guardarArchivo = guardarArchivo,
            eliminarArchivo = eliminarArchivo,
            obtenerPorCarpeta = obtenerPorCarpeta,
            eliminarArchivosPorCarpeta = eliminarArchivosPorCarpeta
        )
    }




}
