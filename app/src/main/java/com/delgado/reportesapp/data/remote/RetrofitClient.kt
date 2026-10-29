package com.delgado.reportesapp.data.remote

import com.delgado.reportesapp.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente singleton de Retrofit configurado para conectar con la API REST de Render.com.
 * Incluye un tiempo de espera de 120 segundos para tolerar el inicio en frío (Cold Start) de Render Free.
 */
object RetrofitClient {

    private var currentBaseUrl: String = Constants.BASE_URL

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Permite cambiar la URL base dinámicamente si fuera necesario (ej. Render vs Localhost).
     */
    fun updateBaseUrl(newBaseUrl: String) {
        if (newBaseUrl.isNotBlank()) {
            currentBaseUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
        }
    }
}
