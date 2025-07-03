package com.ritesh.cashiro.di

import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.Gson
import com.ritesh.cashiro.data.currency.api.CurrencyApi
import com.ritesh.cashiro.data.currency.cache.CurrencyCacheManager
import com.ritesh.cashiro.data.currency.repository.CurrencyRepository
import com.ritesh.cashiro.data.currency.repository.CurrencyRepositoryImpl
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
object CurrencyApiModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("primary")
    fun providePrimaryRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("fallback")
    fun provideFallbackRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://latest.currency-api.pages.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("primary")
    fun providePrimaryCurrencyApi(@Named("primary") retrofit: Retrofit): CurrencyApi {
        return retrofit.create(CurrencyApi::class.java)
    }

    @Provides
    @Singleton
    @Named("fallback")
    fun provideFallbackCurrencyApi(@Named("fallback") retrofit: Retrofit): CurrencyApi {
        return retrofit.create(CurrencyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideCurrencyCacheManager(
        @ApplicationContext context: Context,
        gson: Gson
    ): CurrencyCacheManager {
        return CurrencyCacheManager(context, gson)
    }

    @Provides
    @Singleton
    fun provideCurrencyRepository(
        @Named("primary") primaryApi: CurrencyApi,
        @Named("fallback") fallbackApi: CurrencyApi,
        cacheManager: CurrencyCacheManager,
        connectivityManager: ConnectivityManager
    ): CurrencyRepository {
        return CurrencyRepositoryImpl(primaryApi, fallbackApi, cacheManager, connectivityManager)
    }
}

