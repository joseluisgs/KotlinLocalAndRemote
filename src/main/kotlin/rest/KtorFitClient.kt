package dev.joseluisgs.rest

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.internal.TypeData
import dev.joseluisgs.error.TenistaError
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.logging


private val logger = logging()

fun getKtorFitClient(apiRestUrl: String = "http://localhost:8080/api/v1/") = Ktorfit.Builder()
    .httpClient {
        install(ContentNegotiation) {
            json(Json { isLenient = true; ignoreUnknownKeys = true; encodeDefaults = false })
        }
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
    .baseUrl(apiRestUrl)
    // .converterFactories(FlowConverterFactory()) // Necesario para trabajar con Flows si queremos
    .converterFactories(ResultConverterFactory()) // Necesario para trabajar con Result si queremos
    .build()


// Ahora podemos usar Result y Kotlin.Flow directamente en el cliente Mira la interfaz KtorFitRest
class ResultConverterFactory : Converter.Factory {

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type == com.github.michaelbull.result.Result::class) {

            return object : Converter.SuspendResponseConverter<HttpResponse, Any> {
                override suspend fun convert(response: HttpResponse): Any {
                    return try {
                        Ok<Any>(response.body(typeData.typeArgs.first().typeInfo))
                    } catch (ex: Throwable) {
                        Err(TenistaError.RemoteError(ex.message ?: "Error desconocido"))
                    }
                }
            }
        }
        return null
    }
}