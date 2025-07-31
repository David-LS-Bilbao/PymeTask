package com.dls.pymetask.di

import com.dls.pymetask.data.repository.TareaRepositoryImpl
import com.dls.pymetask.domain.repository.TareaRepository
import com.dls.pymetask.domain.usecase.tarea.*
import com.dls.pymetask.domain.usecase.tarea.TareaUseCases
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TareaModule {

    @Provides
    @Singleton
    fun provideTareaRepository(
        firestore: FirebaseFirestore
    ): TareaRepository = TareaRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideTareaUseCases(
        repository: TareaRepository
    ): TareaUseCases = TareaUseCases(
        guardarTarea = GuardarTarea(repository),
        eliminarTarea = EliminarTarea(repository),
        obtenerTareas = ObtenerTareas(repository),
        obtenerTareaPorId = ObtenerTareaPorId(repository)
    )
}
