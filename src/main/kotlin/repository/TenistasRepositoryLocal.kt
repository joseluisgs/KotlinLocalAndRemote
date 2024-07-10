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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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


    override fun getAll(): Flow<Result<List<Tenista>, TenistaError>> = flow {
        logger.debug { "Obteniendo todos los tenistas de la bd" }
        emit(Ok(sqlClient.queries.selectAll().executeAsList().map { it.toTenista() }))
    }.flowOn(Dispatchers.IO)

    override fun getById(id: Long): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Obteniendo tenista por id: $id en la bd" }
        sqlClient.queries.selectById(id).executeAsOneOrNull()?.let {
            emit(Ok(it.toTenista()))
        } ?: emit(Err(TenistaError.NotFound(id)))
    }.flowOn(Dispatchers.IO)

    override fun save(t: Tenista): Flow<Result<Tenista, TenistaError>> = flow {
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
        emit(Ok(new))
    }.flowOn(Dispatchers.IO)

    override fun update(id: Long, t: Tenista): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Actualizando tenista por id: $id en la bd" }
        val timeStamp = LocalDateTime.now()
        emit(getById(id).first().andThen { tenista ->
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
            Ok(t.copy(createdAt = tenista.createdAt, updatedAt = timeStamp))
        })
    }.flowOn(Dispatchers.IO)

    override fun delete(id: Long): Flow<Result<Long, TenistaError>> = flow {
        logger.debug { "Borrando físico tenista por id: $id en la bd" }
        emit(getById(id).first().andThen {
            sqlClient.queries.delete(id)
            Ok(id)
        })
    }.flowOn(Dispatchers.IO)

    fun removeAll(): Flow<Result<Unit, TenistaError>> = flow {
        logger.debug { "Borrando todos los tenistas de la bd" }
        sqlClient.queries.removeAll()
        emit(Ok(Unit))
    }

    fun saveAll(tenistas: List<Tenista>): Flow<Result<Int, TenistaError>> = flow {
        logger.debug { "Guardando todos los tenistas en la bd local: ${tenistas.size}" }
        val timeStamp = LocalDateTime.now()
        sqlClient.queries.transaction {
            tenistas.forEach {
                sqlClient.queries.insert(it.copy(createdAt = timeStamp, updatedAt = timeStamp).toTenistaEntity())
            }
        }
        emit(Ok(tenistas.size))
    }
}