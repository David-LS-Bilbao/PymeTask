package com.dls.pymetask.di


import android.app.Application
import android.content.Context
import android.location.Geocoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

/**
 * Provee un Geocoder con el locale del dispositivo.
 * (El texto final lo internacionalizamos en la UI; aquí solo resolvemos city.)
 */
@Module
@InstallIn(SingletonComponent::class)
object GeocoderModule {

    @Provides @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder =
        Geocoder(context, Locale.getDefault())
}
