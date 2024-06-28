package dev.joseluisgs.rest

import com.github.michaelbull.result.Result
import de.jensklingenberg.ktorfit.http.*
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.error.TenistaError

// https://my-json-server.typicode.com/joseluisgs/KotlinLocalAndRemote
const val API_TENISTAS_URL = "https://my-json-server.typicode.com/joseluisgs/KotlinLocalAndRemote/"

interface TenistasApiRest {
    @GET("/tenistas")
    // fun getAll(): Flow<List<TenistaDto>> // Si queremos trabajar con Flows no poner suspend
    // suspend fun getAll(): List<TenistaDto> // Si no queremos trabajar con Flows y simplemente listas
    // suspend fun getAll(): Response<List<TenistaDto>> // Si queremos trabajar con Response
    suspend fun getAll(): Result<List<TenistaDto>, TenistaError.ApiError> // Si quieres puedes seguir usando Result, mira la implementación de KtorFitClient

    @GET("/tenistas/{id}")
    suspend fun getById(@Path("id") id: Long): Result<TenistaDto, TenistaError.ApiError>

    @POST("/tenistas")
    suspend fun save(@Body tenista: TenistaDto): Result<TenistaDto, TenistaError.ApiError>

    @PUT("/tenistas/{id}")
    suspend fun update(@Path("id") id: Long, @Body tenista: TenistaDto): Result<TenistaDto, TenistaError.ApiError>

    @DELETE("/tenistas/{id}")
    suspend fun delete(@Path("id") id: Long): Result<Unit, TenistaError.ApiError>
}