package com.example.elizarchat

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FinalConnectionTest {

    @Test
    fun testDirectConnection() = runBlocking {
        println("\n=== ПРЯМОЕ ПОДКЛЮЧЕНИЕ К СЕРВЕРУ ===")

        // Тест 1: Самый простой OkHttp запрос
        val simpleClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val urls = listOf(
            "http://195.133.145.249:3001/api/v1/health",  // Полный путь
            "http://195.133.145.249:3001/",               // Корень сервера
            AppConstants.API_BASE_URL + "/health"         // Через константы
        )

        urls.forEach { url ->
            println("\nПробуем URL: $url")
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build()

            try {
                val response = simpleClient.newCall(request).execute()
                println("Статус: ${response.code}")
                println("Тело: ${response.body?.string()?.take(200)}")
            } catch (e: Exception) {
                println("Ошибка: ${e.javaClass.simpleName} - ${e.message}")
            }
        }
    }

    @Serializable
    data class HealthResponse(
        val success: Boolean,
        val message: String,
        val timestamp: String
    )

    @Test
    fun testRetrofitWithFixedConfig() = runBlocking {
        println("\n=== ТЕСТ RETROFIT С ИСПРАВЛЕННОЙ КОНФИГУРАЦИЕЙ ===")

        try {
            // Создаем Retrofit с исправленной конфигурацией
            val retrofit = com.example.elizarchat.data.remote.config.RetrofitConfig
                .createUnauthenticatedRetrofit()

            // Простой интерфейс для теста
            val api = retrofit.create(TestApi::class.java)

            println("Попытка запроса к health endpoint...")
            val response = api.getHealth()

            println("\n✅ УСПЕХ! Сервер ответил:")
            println("   success: ${response.success}")
            println("   message: ${response.message}")
            println("   timestamp: ${response.timestamp}")

        } catch (e: Exception) {
            println("\n❌ ОШИБКА: ${e.javaClass.simpleName}")
            println("   Сообщение: ${e.message}")
            e.printStackTrace()
        }
    }

    interface TestApi {
        @retrofit2.http.GET("/health")
        suspend fun getHealth(): HealthResponse
    }
}