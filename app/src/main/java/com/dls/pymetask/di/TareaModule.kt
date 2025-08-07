package com.dls.pymetask.di

import android.content.Context
import com.dls.pymetask.data.repository.TareaRepositoryImpl
import com.dls.pymetask.domain.repository.TareaRepository
import com.dls.pymetask.domain.useCase.tarea.AddTarea
import com.dls.pymetask.domain.useCase.tarea.DeleteTarea
import com.dls.pymetask.domain.useCase.tarea.EliminarTarea
import com.dls.pymetask.domain.useCase.tarea.GetTarea
import com.dls.pymetask.domain.useCase.tarea.GetTareas
import com.dls.pymetask.domain.useCase.tarea.TareaUseCases
import com.dls.pymetask.domain.useCase.tarea.UpdateTarea
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TareaModule {

    @Provides
    @Singleton
    fun provideTareaRepository(
        @ApplicationContext context: Context
    ): TareaRepository {
        return TareaRepositoryImpl(FirebaseFirestore.getInstance(),context)
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
