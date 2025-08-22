package com.dls.pymetask.di



import android.app.Application
import android.content.Context
import android.location.Geocoder
import com.dls.pymetask.data.location.DefaultLocationClient
import com.dls.pymetask.data.location.LocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

/**
 * Proveedores de ubicación para inyección con Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides @Singleton
    fun provideLocationClient(
        app: Application,
        fused: FusedLocationProviderClient
    ): LocationClient = DefaultLocationClient(app, fused)


}
