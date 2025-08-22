package com.dls.pymetask.di


import com.dls.pymetask.data.repository.WeatherRepositoryImpl
import com.dls.pymetask.domain.repository.WeatherApi
import com.dls.pymetask.domain.repository.WeatherRepository
import com.dls.pymetask.domain.useCase.weather.GetWeatherUseCase
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * DI para la funcionalidad de tiempo (Open‑Meteo).
 */
@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    private const val BASE_URL = "https://api.open-meteo.com/"

    @Provides @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // BASIC para no saturar Logcat; puedes usar BODY en debug.
            level = HttpLoggingInterceptor.Level.BASIC
        }

    @Provides @Singleton
    fun provideOkHttp(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    @Provides @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides @Singleton
    fun provideRetrofit(okHttp: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)

    @Provides @Singleton
    fun provideWeatherRepository(api: WeatherApi): WeatherRepository =
        WeatherRepositoryImpl(api)

    @Provides @Singleton
    fun provideGetWeatherUseCase(repo: WeatherRepository): GetWeatherUseCase =
        GetWeatherUseCase(repo)
}
