package com.dls.pymetask.di

import android.content.Context
import com.dls.pymetask.data.remote.bank.BankApi
import com.dls.pymetask.data.remote.bank.BankRemoteDataSource
import com.dls.pymetask.data.remote.bank.BankRemoteDataSourceImpl
import com.dls.pymetask.data.repository.BankRepositoryImpl
import com.dls.pymetask.domain.repository.BankRepository
import com.dls.pymetask.domain.repository.MovimientoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
// import com.dls.pymetask.BuildConfig   // <- usa esto si pasas a BuildConfig

// ✅ Opción B: constante local para compilar ya (cámbiala cuando tengas el proveedor real)
private const val BANK_BASE_URL = "https://api.mockbank.local/"

@Module
@InstallIn(SingletonComponent::class)
object BankModule {

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

    @Provides @Singleton
    fun provideRetrofitBank(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            // .baseUrl(BuildConfig.BANK_BASE_URL) // <- Opción A (recomendada)
            .baseUrl(BANK_BASE_URL)                // <- Opción B (rápida)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    @Provides @Singleton
    fun provideBankApi(retrofit: Retrofit): BankApi =
        retrofit.create(BankApi::class.java)

    @Provides @Singleton
    fun provideBankRemoteDataSource(api: BankApi): BankRemoteDataSource =
        BankRemoteDataSourceImpl(api)

    @Provides @Singleton
    fun provideBankRepository(
        remote: BankRemoteDataSource,
        movimientoRepository: MovimientoRepository,
        @ApplicationContext context: Context
    ): BankRepository = BankRepositoryImpl(remote, movimientoRepository, context)
}

