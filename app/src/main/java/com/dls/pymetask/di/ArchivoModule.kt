package com.dls.pymetask.di

import android.content.Context
import com.dls.pymetask.data.repository.ArchivoRepositoryImpl
import com.dls.pymetask.domain.repository.ArchivoRepository
import com.dls.pymetask.domain.useCase.archivo.ArchivoUseCase
import com.dls.pymetask.domain.useCase.archivo.CrearCarpetaUseCase
import com.dls.pymetask.domain.useCase.archivo.EliminarArchivoUseCase
import com.dls.pymetask.domain.useCase.archivo.EliminarArchivosPorCarpetaUseCase
import com.dls.pymetask.domain.useCase.archivo.EliminarCarpetaUseCase
import com.dls.pymetask.domain.useCase.archivo.GuardarArchivoUseCase
import com.dls.pymetask.domain.useCase.archivo.ListarArchivosUseCase
import com.dls.pymetask.domain.useCase.archivo.ObtenerArchivosFirestoreUseCase
import com.dls.pymetask.domain.useCase.archivo.ObtenerArchivosPorCarpetaUseCase
import com.dls.pymetask.domain.useCase.archivo.ObtenerNombreCarpetaUseCase
import com.dls.pymetask.domain.useCase.archivo.RenombrarArchivoUseCase
import com.dls.pymetask.domain.useCase.archivo.SubirArchivoUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): ArchivoRepository = ArchivoRepositoryImpl(storage, firestore, context)

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
        eliminarArchivosPorCarpeta: EliminarArchivosPorCarpetaUseCase,
        repo: ArchivoRepository
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
            eliminarArchivosPorCarpeta = eliminarArchivosPorCarpeta,
            obtenerNombreCarpeta = ObtenerNombreCarpetaUseCase(repo)
        )
    }




}
