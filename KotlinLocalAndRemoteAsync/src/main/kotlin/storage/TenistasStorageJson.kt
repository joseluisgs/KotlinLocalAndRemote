package dev.joseluisgs.storage

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapBoth
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.model.Tenista
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.logging
import java.io.File

private val logger = logging()


class TenistasStorageJson : TenistasStorage {
    override suspend fun import(file: File): Deferred<Result<List<Tenista>, TenistaError.StorageError>> =
        withContext(Dispatchers.IO) {
            async {
                logger.debug { "Importando Tenistas desde JSON asíncrono: $file" }
                // Si el fichero no existe, devolvemos un error
                // Leemos el fichero y emitimos el resultado
                readLines(file)

            }
        }

    private fun readLines(file: File): Result<List<Tenista>, TenistaError.StorageError> {
        // Creamos el serializador de JSON
        if (!file.exists()) {
            return Err(TenistaError.StorageError("El fichero no existe ${file.absolutePath}"))
        }

        return try {
            val json = Json { ignoreUnknownKeys = true; encodeDefaults = false; isLenient = true }
            // Leemos el fichero y lo convertimos a Tenista
            val tenistas = json.decodeFromString<List<TenistaDto>>(file.readText()).map { it.toTenista() }
            Ok(tenistas) // Devolvemos los tenistas
        } catch (e: Exception) {
            logger.error(e) { "Error al leer el fichero: ${file.absolutePath}" }
            Err(TenistaError.StorageError("No se puede leer el fichero ${file.absolutePath}: ${e.message}"))
        }

    }


    override suspend fun export(file: File, data: List<Tenista>): Deferred<Result<Int, TenistaError.StorageError>> =
        withContext(Dispatchers.IO) {
            async {
                logger.debug { "Exportando Tenistas a JSON asíncrono: $file" }
                // Comprobamos que el fichero puede ser creado
                file.ensureFileCanExists().mapBoth(
                    // En caso de error, emitimos el error
                    // En caso de éxito, escribimos el fichero
                    success = { file ->
                        // Creamos el serializador de JSON con prettyPrint
                        val json = Json { ignoreUnknownKeys = true; encodeDefaults = false; prettyPrint = true }
                        // Convertimos los Tenistas a DTO y los serializamos
                        val tenistasDto = data.map { it.toTenistaDto() }
                        // Escribimos el fichero
                        file.writeText(json.encodeToString(tenistasDto))
                        Ok(data.size)
                    },
                    failure = { error -> Err(error) },
                )
            }
        }
}