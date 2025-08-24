package com.dls.pymetask.di

import android.content.Context
import com.dls.pymetask.data.repository.MovimientoRepositoryImpl
import com.dls.pymetask.domain.repository.MovimientoRepository
import com.dls.pymetask.domain.useCase.movimiento.AddMovimiento
import com.dls.pymetask.domain.useCase.movimiento.DeleteMovimiento
import com.dls.pymetask.domain.useCase.movimiento.GetEarliestMovimientoMillis
import com.dls.pymetask.domain.useCase.movimiento.GetMovimientos
import com.dls.pymetask.domain.useCase.movimiento.GetMovimientosBetween
import com.dls.pymetask.domain.useCase.movimiento.MovimientoUseCases
import com.dls.pymetask.domain.useCase.movimiento.UpdateMovimiento
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MovimientoModule {

    @Provides
    @Singleton
    fun provideMovimientoRepository(
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): MovimientoRepository {
        return MovimientoRepositoryImpl(firestore, context)
    }
    @Provides
    @Singleton
    fun provideMovimientoUseCases(
        repo: MovimientoRepository
    ): MovimientoUseCases {
        return MovimientoUseCases(
            getMovimientos = GetMovimientos(repo),
            getMovimientosBetween = GetMovimientosBetween(repo),
            getEarliestMovimientoMillis = GetEarliestMovimientoMillis(repo),
            addMovimiento = AddMovimiento(repo),
            updateMovimiento = UpdateMovimiento(repo),
            deleteMovimiento = DeleteMovimiento(repo)
        )
    }
}


