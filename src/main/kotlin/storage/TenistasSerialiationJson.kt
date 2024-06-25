package dev.joseluisgs.storage

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
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

class TenistasSerialiationJson : TenistasSerializationStorage {
    override fun import(file: File): Flow<Result<List<Tenista>, TenistaError>> = flow {
        logger.debug { "Importando Tenistas desde JSON asíncrono: $file" }

        if (!file.exists()) {
            emit(Err(TenistaError.StorageError("El fichero no existe ${file.absolutePath}")))
        }

        // Creamos el serializador de JSON
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }
        // Leemos el fichero y lo convertimos a Tenista
        val tenistas = json.decodeFromString<List<TenistaDto>>(file.readText()).map { it.toTenista() }
        emit(Ok(tenistas))
    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución a IO

    override fun export(file: File, data: List<Tenista>): Flow<Result<Int, TenistaError>> = flow {
        logger.debug { "Exportando Tenistas a JSON asíncrono: $file" }
        // También podemos usar Files.deleteIfExists(file.toPath()) para borrar el fichero
        // No es necesario comprobar si existe, ya que si no existe lo crea con el writeText
        /*if (file.exists()) {
            file.delete() // Borramos el fichero si existe
        }*/

        // Creamos el serializador de JSON, ignorando las propiedades desconocidas, nulas y con formato bonito ;)
        val json = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = false }
        try {
            // Escribimos el fichero con los datos serializados
            file.writeText(json.encodeToString<List<TenistaDto>>(data.map { it.toTenistaDto() }))
            emit(Ok(data.size))
        } catch (e: Exception) {
            logger.error(e) { "Error al escribir el fichero: ${file.absolutePath}" }
            emit(Err(TenistaError.StorageError("Error al escribir el fichero ${file.absolutePath}: ${e.message}")))
        }

    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución a IO
}