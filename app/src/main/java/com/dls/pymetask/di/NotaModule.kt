// di/NotaModule.kt
package com.dls.pymetask.di

import android.content.Context
import com.dls.pymetask.data.repository.NotaRepositoryImpl
import com.dls.pymetask.domain.repository.NotaRepository
import com.dls.pymetask.domain.useCase.nota.AddNota
import com.dls.pymetask.domain.useCase.nota.DeleteNota
import com.dls.pymetask.domain.useCase.nota.EliminarNota
import com.dls.pymetask.domain.useCase.nota.GetNota
import com.dls.pymetask.domain.useCase.nota.GetNotas
import com.dls.pymetask.domain.useCase.nota.NotaUseCases
import com.dls.pymetask.domain.useCase.nota.UpdateNota
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotaModule {

    @Provides
    @Singleton
    fun provideNotaRepository(
        @ApplicationContext context: Context
    ): NotaRepository {
        return NotaRepositoryImpl(
            FirebaseFirestore.getInstance(),context)
    }

    @Provides
    @Singleton
    fun provideNotaUseCases(repository: NotaRepository): NotaUseCases {
        return NotaUseCases(
            getNotas = GetNotas(repository),
            getNota = GetNota(repository),
            addNota = AddNota(repository),
            updateNota = UpdateNota(repository),
            deleteNota = DeleteNota(repository),
            eliminarNota = EliminarNota(repository)
        )
    }
}