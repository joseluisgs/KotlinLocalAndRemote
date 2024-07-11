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
import kotlinx.coroutines.withContext
import org.lighthousegames.logging.logging
import java.time.LocalDateTime

private val logger = logging()

class TenistasRepositoryRemote(private val restClient: TenistasApiRest) : TenistasRepository {

    override suspend fun getAll(): Result<List<Tenista>, TenistaError.RemoteError> = withContext(Dispatchers.IO) {
        logger.debug { "Obteniendo todos los tenistas de la api rest" }
        restClient.getAll().mapBoth(
            success = { Ok(it.map { dto -> dto.toTenista() }) },
            failure = { Err(TenistaError.RemoteError("${it.message}, no se ha podido obtener la lista de tenistas")) }
        )
    }

    override suspend fun getById(id: Long): Result<Tenista, TenistaError.RemoteError> = withContext(Dispatchers.IO) {
        logger.debug { "Obteniendo tenista por id: $id en la api rest" }
        restClient.getById(id).mapBoth(
            success = { Ok(it.toTenista()) },
            failure = { Err(TenistaError.RemoteError("${it.message}, no se ha podido obtener el tenista con id $id")) }
        )
    }

    override suspend fun save(t: Tenista): Result<Tenista, TenistaError.RemoteError> = withContext(Dispatchers.IO) {

        logger.debug { "Guardando tenista en la api rest" }
        val timeStamp = LocalDateTime.now()
        restClient.save(
            t.copy(id = Tenista.NEW_ID, createdAt = timeStamp, updatedAt = timeStamp).toTenistaDto()
        )
            .mapBoth(
                success = { Ok(it.toTenista()) },
                failure = { Err(TenistaError.RemoteError("${it.message}, no se ha podido guardar el tenista $t")) }
            )
    }

    override suspend fun update(id: Long, t: Tenista): Result<Tenista, TenistaError.RemoteError> =
        withContext(Dispatchers.IO) {

            logger.debug { "Actualizando tenista con id $id en la api rest" }
            val timeStamp = LocalDateTime.now()
            restClient.update(id, t.copy(updatedAt = timeStamp).toTenistaDto()).mapBoth(
                success = { Ok(it.toTenista()) },
                failure = { Err(TenistaError.RemoteError("${it.message}, no se ha podido actualizar el tenista con id $id")) }
            )
        }


    override suspend fun delete(id: Long): Result<Long, TenistaError.RemoteError> =
        withContext(Dispatchers.IO) {

            logger.debug { "Borrando tenista con id $id en la api rest" }
            restClient.delete(id).mapBoth(
                success = { Ok(id) },
                failure = { Err(TenistaError.RemoteError("${it.message}, no se ha podido guardar el tenista con id $id")) }
            )
        }
}