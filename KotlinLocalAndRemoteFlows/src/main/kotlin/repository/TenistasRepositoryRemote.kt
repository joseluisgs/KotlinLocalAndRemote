package dev.joseluisgs.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapBoth
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.rest.TenistasApiRest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.lighthousegames.logging.logging
import java.time.LocalDateTime

private val logger = logging()

class TenistasRepositoryRemote(private val restClient: TenistasApiRest) : TenistasRepository {

    override fun getAll(): Flow<Result<List<Tenista>, TenistaError.RemoteError>> = flow {
        logger.debug { "Obteniendo todos los tenistas de la api rest" }
        restClient.getAll().mapBoth(
            success = { emit(Ok(it.map { dto -> dto.toTenista() })) },
            failure = { emit(Err(TenistaError.RemoteError("${it.message}, no se ha podido obtener la lista de tenistas"))) }
        )

    }.flowOn(Dispatchers.IO)

    override fun getById(id: Long): Flow<Result<Tenista, TenistaError.RemoteError>> = flow {
        logger.debug { "Obteniendo tenista con id $id de la api rest" }
        restClient.getById(id).mapBoth(
            success = { emit(Ok(it.toTenista())) },
            failure = { emit(Err(TenistaError.RemoteError("${it.message}, no se ha podido obtener el tenista con id $id"))) }
        )

    }.flowOn(Dispatchers.IO)

    override fun save(t: Tenista): Flow<Result<Tenista, TenistaError.RemoteError>> = flow {
        logger.debug { "Guardando tenista en la api rest" }
        val timeStamp = LocalDateTime.now()
        restClient.save(t.copy(id = Tenista.NEW_ID, createdAt = timeStamp, updatedAt = timeStamp).toTenistaDto())
            .mapBoth(
                success = { emit(Ok(it.toTenista())) },
                failure = { emit(Err(TenistaError.RemoteError("${it.message}, no se ha podido guardar el tenista $t"))) }
            )
    }.flowOn(Dispatchers.IO)

    override fun update(id: Long, t: Tenista): Flow<Result<Tenista, TenistaError.RemoteError>> = flow {
        logger.debug { "Actualizando tenista con id $id en la api rest" }
        val timeStamp = LocalDateTime.now()
        restClient.update(id, t.copy(updatedAt = timeStamp).toTenistaDto()).mapBoth(
            success = { emit(Ok(it.toTenista())) },
            failure = { emit(Err(TenistaError.RemoteError("${it.message}, no se ha podido actualizar el tenista con id $id"))) }
        )
    }.flowOn(Dispatchers.IO)

    override fun delete(id: Long): Flow<Result<Long, TenistaError.RemoteError>> = flow {
        logger.debug { "Borrando tenista con id $id en la api rest" }
        restClient.delete(id).mapBoth(
            success = { emit(Ok(id)) },
            failure = { emit(Err(TenistaError.RemoteError("${it.message}, no se ha podido guardar el tenista con id $id"))) }
        )
    }.flowOn(Dispatchers.IO)
}