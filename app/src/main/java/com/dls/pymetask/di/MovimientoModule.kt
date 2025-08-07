package com.dls.pymetask.di

import android.content.Context
import com.dls.pymetask.data.repository.MovimientoRepositoryImpl
import com.dls.pymetask.domain.repository.MovimientoRepository
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
}
