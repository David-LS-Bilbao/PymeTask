package com.dls.pymetask.di

import android.content.Context
import com.dls.pymetask.BuildConfig
import com.dls.pymetask.data.remote.bank.BankApi
import com.dls.pymetask.data.remote.bank.BankRemoteDataSource
import com.dls.pymetask.data.remote.bank.BankRemoteDataSourceImpl
import com.dls.pymetask.data.remote.bank.auth.*
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
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BankModule {

    // ---------- TokenStore ----------
    @Provides @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore =
        EncryptedTokenStore(context)

    // ---------- OAuth (token endpoint) ----------
    @Provides @Singleton @Named("oauth")
    fun provideOAuthRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.OAUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideOAuthApi(@Named("oauth") oauthRetrofit: Retrofit): OAuthApi =
        oauthRetrofit.create(OAuthApi::class.java)

    @Provides @Singleton
    fun provideOAuthManager(
        @ApplicationContext context: Context,
        oauthApi: OAuthApi,
        tokenStore: TokenStore
    ): OAuthManager = OAuthManager(context, oauthApi, tokenStore)

    // ---------- Data API (bank) ----------
    @Provides @Singleton @Named("data")
    fun provideOkHttpForData(
        tokenStore: TokenStore,
        oauthApi: OAuthApi
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val bearer = BearerInterceptor(tokenStore)
        val authenticator = OAuthAuthenticator(tokenStore, oauthApi)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(bearer)       // añade Authorization si hay token
            .authenticator(authenticator) // refresh en 401
            .build()
    }

    @Provides @Singleton @Named("data")
    fun provideDataRetrofit(@Named("data") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BANK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    @Provides @Singleton
    fun provideBankApi(@Named("data") dataRetrofit: Retrofit): BankApi =
        dataRetrofit.create(BankApi::class.java)

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
















//package com.dls.pymetask.di
//import android.content.Context
//import com.dls.pymetask.BuildConfig
//import com.dls.pymetask.data.remote.bank.BankApi
//import com.dls.pymetask.data.remote.bank.BankRemoteDataSource
//import com.dls.pymetask.data.remote.bank.BankRemoteDataSourceImpl
//import com.dls.pymetask.data.remote.bank.auth.*
//import com.dls.pymetask.data.repository.BankRepositoryImpl
//import com.dls.pymetask.domain.repository.BankRepository
//import com.dls.pymetask.domain.repository.MovimientoRepository
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import javax.inject.Singleton
//@Module
//@InstallIn(SingletonComponent::class)
//object BankModule {
//
//    // ---------- TokenStore ----------
//    @Provides @Singleton
//    fun provideTokenStore(@ApplicationContext context: Context): TokenStore =
//        EncryptedTokenStore(context)
//
//    // ---------- OAuth Retrofit ----------
//    @Provides @Singleton
//    fun provideOAuthRetrofit(): Retrofit =
//        Retrofit.Builder()
//            .baseUrl(BuildConfig.OAUTH_BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//    @Provides @Singleton
//    fun provideOAuthApi(oauthRetrofit: Retrofit): OAuthApi =
//        oauthRetrofit.create(OAuthApi::class.java)
//
//    @Provides @Singleton
//    fun provideOAuthManager(
//        @ApplicationContext context: Context,
//        oauthApi: OAuthApi,
//        tokenStore: TokenStore
//    ): OAuthManager = OAuthManager(context, oauthApi, tokenStore)
//
//    // ---------- Data API OkHttp + Retrofit ----------
//    @Provides @Singleton
//    fun provideOkHttpForData(
//        tokenStore: TokenStore,
//        oauthApi: OAuthApi
//    ): OkHttpClient {
//        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
//        val bearer = BearerInterceptor(tokenStore)
//        val authenticator = OAuthAuthenticator(tokenStore, oauthApi)
//
//        return OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor(bearer)          // añade Authorization si hay token
//            .authenticator(authenticator)    // intenta refresh si 401
//            .build()
//    }
//
//    @Provides @Singleton
//    fun provideDataRetrofit(client: OkHttpClient): Retrofit =
//        Retrofit.Builder()
//            .baseUrl(BuildConfig.BANK_BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//
//    @Provides @Singleton
//    fun provideBankApi(dataRetrofit: Retrofit): BankApi =
//        dataRetrofit.create(BankApi::class.java)
//
//    @Provides @Singleton
//    fun provideBankRemoteDataSource(api: BankApi): BankRemoteDataSource =
//        BankRemoteDataSourceImpl(api)
//
//    @Provides @Singleton
//    fun provideBankRepository(
//        remote: BankRemoteDataSource,
//        movimientoRepository: MovimientoRepository,
//        @ApplicationContext context: Context
//    ): BankRepository = BankRepositoryImpl(remote, movimientoRepository, context)
//}






