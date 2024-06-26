package dev.joseluisgs.rest

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.*
import dev.joseluisgs.dto.TenistaDto

interface TenistasRest {
    @GET("/tenistas")
    // fun getAll(): Flow<List<TenistaDto>> // Si queremos trabajar con Flows no poner suspend
    // suspend fun getAll(): List<TenistaDto> // Si no queremos trabajar con Flows y simplemente listas
    suspend fun getAll(): Response<List<TenistaDto>> // Es mejor trabajar con Response y los Flows en la capa de negocio

    @GET("/tenistas/{id}")
    suspend fun getById(@Path("id") id: Long): Response<TenistaDto>

    @POST("/tenistas")
    suspend fun save(@Body tenista: TenistaDto): Response<TenistaDto>

    @POST("/tenistas/{id}")
    suspend fun update(@Path("id") id: Long, @Body tenista: TenistaDto): Response<TenistaDto>

    @DELETE("/tenistas/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}