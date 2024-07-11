package dev.joseluisgs.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import database.SqlDeLightManager
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaEntity
import dev.joseluisgs.model.Tenista
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.lighthousegames.logging.logging
import java.time.LocalDateTime


private val logger = logging()

class TenistasRepositoryLocal(
    private val sqlClient: SqlDeLightManager,
) : TenistasRepository {

    init {
        logger.debug { "Inicializando el repositorio local" }
        sqlClient.queries.removeAll()
    }


    override suspend fun getAll(): Deferred<Result<List<Tenista>, TenistaError>> = withContext(Dispatchers.IO) {
        logger.debug { "Obteniendo todos los tenistas de la bd" }
        async { Ok(sqlClient.queries.selectAll().executeAsList().map { it.toTenista() }) }
    }

    override suspend fun getById(id: Long): Deferred<Result<Tenista, TenistaError>> =
        withContext(Dispatchers.IO) {
            async {
                logger.debug { "Obteniendo tenista por id: $id en la bd" }
                sqlClient.queries.selectById(id).executeAsOneOrNull()?.let {
                    Ok(it.toTenista())
                } ?: Err(TenistaError.NotFound(id))
            }
        }

    override suspend fun save(t: Tenista): Deferred<Result<Tenista, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Guardando tenista: $t en la bd" }
            val timeStamp = LocalDateTime.now()
            // Hacemos una transacción para poder obtener el id del tenista guardado
            sqlClient.queries.transaction {
                sqlClient.queries.insert(
                    t.copy(createdAt = timeStamp, updatedAt = timeStamp, id = Tenista.NEW_ID).toTenistaEntity()
                )
            }
            // Consultamos el tenista guardado (segun la implementación de SQLDelight usamos transactions)
            val new = sqlClient.queries.selectLastInserted().executeAsOne().toTenista()
            Ok(new)
        }

    }

    override suspend fun update(id: Long, t: Tenista): Deferred<Result<Tenista, TenistaError>> =
        withContext(Dispatchers.IO) {
            async {
                logger.debug { "Actualizando tenista por id: $id en la bd" }
                val timeStamp = LocalDateTime.now()
                getById(id).await()
                    .andThen {
                        sqlClient.queries.update(
                            id = id,
                            nombre = t.nombre,
                            pais = t.pais,
                            altura = t.altura.toLong(),
                            peso = t.peso.toLong(),
                            puntos = t.puntos.toLong(),
                            mano = t.mano.name,
                            fecha_nacimiento = t.fechaNacimiento.toString(),
                            updated_at = timeStamp.toString()
                        )
                        Ok(t.copy(id = id, createdAt = it.createdAt, updatedAt = timeStamp))
                    }
            }
        }

    override suspend fun delete(id: Long): Deferred<Result<Long, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Borrando físico tenista por id: $id en la bd" }
            getById(id).await()
                .andThen {
                    sqlClient.queries.delete(id)
                    Ok(id)
                }
        }
    }

    suspend fun removeAll(): Deferred<Result<Unit, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Borrando todos los tenistas de la bd" }
            sqlClient.queries.removeAll()
            Ok(Unit)
        }
    }

    suspend fun saveAll(tenistas: List<Tenista>): Deferred<Result<Int, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Guardando todos los tenistas en la bd local: ${tenistas.size}" }
            val timeStamp = LocalDateTime.now()
            sqlClient.queries.transaction {
                tenistas.forEach {
                    sqlClient.queries.insert(it.copy(createdAt = timeStamp, updatedAt = timeStamp).toTenistaEntity())
                }
            }
            Ok(tenistas.size)
        }
    }
}