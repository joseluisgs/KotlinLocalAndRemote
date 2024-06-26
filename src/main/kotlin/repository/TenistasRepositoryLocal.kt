package dev.joseluisgs.repository

import com.github.michaelbull.result.Result
import database.SqlDeLightManager
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import kotlinx.coroutines.flow.Flow
import org.lighthousegames.logging.logging
import java.util.*


private val logger = logging()

class TenistasRepositoryLocal(
    private val sqlClient: SqlDeLightManager,
) : TenistasRepository {

    private val db = sqlClient.databaseQueries // Acceso a la base de datos

    override fun getAll(): Flow<Result<List<Tenista>, TenistaError>> {
        logger.debug { "Obteniendo todos los tenistas" }
        TODO("Not yet implemented")
    }

    override fun getById(id: UUID): Flow<Result<Tenista, TenistaError>> {
        logger.debug { "Obteniendo tenista por id: $id" }
        TODO("Not yet implemented")
    }

    override fun save(t: Tenista): Flow<Result<Tenista, TenistaError>> {
        logger.debug { "Guardando tenista: $t" }
        TODO("Not yet implemented")
    }

    override fun update(id: UUID, t: Tenista): Flow<Result<Tenista, TenistaError>> {
        logger.debug { "Actualizando tenista por id: $id" }
        TODO("Not yet implemented")
    }

    override fun delete(id: UUID): Boolean {
        logger.debug { "Borrando tenista por id: $id" }
        TODO("Not yet implemented")
    }
}