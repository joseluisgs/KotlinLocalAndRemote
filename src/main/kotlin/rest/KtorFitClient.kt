package dev.joseluisgs.rest

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.builtin.FlowConverterFactory
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.logging


private val logger = logging()

class KtorFitClient {
    // https://my-json-server.typicode.com/joseluisgs/KotlinLocalAndRemote
    private val API_URL = "https://my-json-server.typicode.com/joseluisgs/KotlinLocalAndRemote/"
    val rest = Ktorfit.Builder()
        .httpClient {
            install(ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true; encodeDefaults = false })
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
        .baseUrl(API_URL)
        .converterFactories(FlowConverterFactory()) // Necesario para trabajar con Flows si queremos
        .build()
        .create<TenistasRest>()
}