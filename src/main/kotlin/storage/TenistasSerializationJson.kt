package dev.joseluisgs.storage

import com.github.michaelbull.result.*
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.models.Tenista
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.logging
import java.io.File

private val logger = logging()

class TenistasSerializationJson : TenistasSerializationStorage {
    override fun import(file: File): Flow<Result<List<Tenista>, TenistaError.StorageError>> = flow {
        logger.debug { "Importando Tenistas desde JSON asíncrono: $file" }
        // Si el fichero no existe, devolvemos un error
        if (!file.exists()) {
            emit(Err(TenistaError.StorageError("El fichero no existe ${file.absolutePath}")))
        } else {
            // Leemos el fichero y emitimos el resultado
            emit(readLines(file))
        }
    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución a IO

    private fun readLines(file: File): Result<List<Tenista>, TenistaError.StorageError> = try {
        // Creamos el serializador de JSON
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = false; isLenient = true }
        // Leemos el fichero y lo convertimos a Tenista
        val tenistas = json.decodeFromString<List<TenistaDto>>(file.readText()).map { it.toTenista() }
        Ok(tenistas) // Devolvemos los tenistas
    } catch (e: Exception) {
        logger.error(e) { "Error al leer el fichero: ${file.absolutePath}" }
        Err(TenistaError.StorageError("ERROR al leer el fichero ${file.absolutePath}: ${e.message}"))
    }


    override fun export(file: File, data: List<Tenista>): Flow<Result<Int, TenistaError.StorageError>> = flow {
        logger.debug { "Exportando Tenistas a JSON asíncrono: $file" }
        // Comprobamos que el fichero puede ser creado
        ensureFileCanExists(file)
            // En caso de error, emitimos el error
            .onFailure { error ->
                emit(Err(error))
            }
            // En caso de éxito, escribimos el fichero
            .onSuccess { file ->
                // Creamos el serializador de JSON con prettyPrint
                val json = Json { ignoreUnknownKeys = true; encodeDefaults = false; prettyPrint = true }
                // Convertimos los Tenistas a DTO y los serializamos
                val tenistasDto = data.map { it.toTenistaDto() }
                // Escribimos el fichero
                file.writeText(json.encodeToString(tenistasDto))
                emit(Ok(data.size))
            }

    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución a IO
}