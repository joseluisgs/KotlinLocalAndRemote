package dev.joseluisgs.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapBoth
import database.SqlDeLightManager
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaEntity
import dev.joseluisgs.models.Tenista
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.lighthousegames.logging.logging
import java.time.LocalDateTime
import java.util.*


private val logger = logging()

class TenistasRepositoryLocal(
    private val sqlClient: SqlDeLightManager,
) : TenistasRepository {

    private val db = sqlClient.databaseQueries // Acceso a la base de datos

    override fun getAll(): Flow<Result<List<Tenista>, TenistaError>> = flow {
        logger.debug { "Obteniendo todos los tenistas ordenados por puntos" }
        emit(Ok(db.selectAllOrderByPuntosDesc().executeAsList().map { it.toTenista() }))
    }.flowOn(Dispatchers.IO)

    override fun getById(id: UUID): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Obteniendo tenista por id: $id" }
        db.selectById(id.toString()).executeAsOneOrNull()?.let {
            emit(Ok(it.toTenista()))
        } ?: emit(Err(TenistaError.NotFound(id.toString())))
    }.flowOn(Dispatchers.IO)

    override fun save(t: Tenista): Flow<Result<Tenista, TenistaError>> = flow<Result<Tenista, TenistaError>> {
        logger.debug { "Guardando tenista: $t" }
        val timeSpam = LocalDateTime.now()
        db.insert(t.toTenistaEntity())
        emit(
            Ok(t.copy(createdAt = timeSpam, updatedAt = timeSpam))
        )
    }.flowOn(Dispatchers.IO)

    override fun update(id: UUID, t: Tenista): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Actualizando tenista por id: $id" }
        val timestamp = LocalDateTime.now()
        emit(getById(id).first().mapBoth(
            success = { tenista ->
                db.update(
                    id = id.toString(),
                    nombre = t.nombre,
                    pais = t.pais,
                    altura = t.altura.toLong(),
                    peso = t.peso.toLong(),
                    puntos = t.puntos.toLong(),
                    mano = t.mano.name,
                    fechaNacimiento = t.fechaNacimiento.toString(),
                    updatedAt = timestamp.toString()
                )
                Ok(t.copy(createdAt = tenista.createdAt, updatedAt = timestamp))
            },
            failure = { error -> Err(error) }
        ))

    }.flowOn(Dispatchers.IO)

    override fun delete(id: UUID): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Borrando lógico tenista por id: $id" }
        emit(getById(id).first().mapBoth(
            success = { tenista ->
                db.delete(id.toString())
                Ok(tenista.copy(updatedAt = LocalDateTime.now(), isDeleted = true))
            },
            failure = { error -> Err(error) }
        ))
    }.flowOn(Dispatchers.IO)
}