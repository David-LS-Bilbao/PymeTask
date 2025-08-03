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
    fun provideTareaRepository(): TareaRepository {
        return TareaRepositoryImpl(FirebaseFirestore.getInstance())
    }

    @Provides
    @Singleton
    fun provideTareaUseCases(repository: TareaRepository): TareaUseCases{

        return TareaUseCases(
           getTareas = GetTareas(repository),
            getTarea = GetTarea(repository),
            addTarea = AddTarea(repository),
            updateTarea = UpdateTarea(repository),
            deleteTarea = DeleteTarea(repository),
            eliminarTarea = EliminarTarea(repository)
        )
    }
}
