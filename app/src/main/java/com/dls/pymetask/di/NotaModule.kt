// di/NotaModule.kt
package com.dls.pymetask.di

import com.dls.pymetask.data.repository.NotaRepositoryImpl
import com.dls.pymetask.domain.repository.NotaRepository
import com.dls.pymetask.domain.useCase.nota.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotaModule {

    @Provides
    @Singleton
    fun provideNotaRepository(): NotaRepository {
        return NotaRepositoryImpl(FirebaseFirestore.getInstance())
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

        )
    }
}