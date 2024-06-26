package dev.joseluisgs.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.rest.KtorFitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.lighthousegames.logging.logging

private val logger = logging()

class TenistasRepositoryRemote(
    private val restClient: KtorFitClient
) : TenistasRepository {
    override fun getAll(): Flow<Result<List<Tenista>, TenistaError>> = flow {
        logger.debug { "Obteniendo todos los tenistas de la api rest" }
        val result = restClient.rest.getAll()
        if (result.isSuccessful && result.body() != null) {
            emit(Ok(result.body()?.map { it.toTenista() } ?: emptyList()))
        } else {
            emit(Err(TenistaError.RemoteError("No se han podido obtener la lista de tenistas ${result.errorBody()}")))
        }

    }.flowOn(Dispatchers.IO)

    override fun getById(id: Long): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Obteniendo tenista con id $id de la api rest" }
        val result = restClient.rest.getById(id)
        if (result.isSuccessful && result.body() != null) {
            emit(Ok(result.body()!!.toTenista()))
        } else {
            emit(Err(TenistaError.RemoteError("No se ha podido obtener el tenista con id $id ${result.errorBody()}")))
        }
    }.flowOn(Dispatchers.IO)


    override fun save(t: Tenista): Flow<Result<Tenista, TenistaError>> = flow<Result<Tenista, TenistaError>> {
        logger.debug { "Guardando tenista en la api rest" }
        val result = restClient.rest.save(t.toTenistaDto())
        if (result.isSuccessful && result.body() != null) {
            emit(Ok(result.body()!!.toTenista()))
        } else {
            emit(Err(TenistaError.RemoteError("No se ha podido guardar el tenista ${result.errorBody()}")))
        }
    }.flowOn(Dispatchers.IO)

    override fun update(id: Long, t: Tenista): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Actualizando tenista con id $id en la api rest" }
        val result = restClient.rest.update(id, t.toTenistaDto())
        if (result.isSuccessful && result.body() != null) {
            emit(Ok(result.body()!!.toTenista()))
        } else {
            emit(Err(TenistaError.RemoteError("No se ha podido actualizar el tenista con id $id ${result.errorBody()}")))
        }
    }.flowOn(Dispatchers.IO)


    override fun delete(id: Long): Flow<Result<Unit, TenistaError>> = flow {
        logger.debug { "Borrando tenista con id $id en la api rest" }
        val result = restClient.rest.delete(id)
        if (result.isSuccessful && result.body() != null) {
            emit(Ok(Unit))
        } else {
            emit(Err(TenistaError.RemoteError("No se ha podido borrar el tenista con id $id ${result.errorBody()}")))
        }
    }.flowOn(Dispatchers.IO)
}