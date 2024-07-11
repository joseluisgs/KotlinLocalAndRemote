package dev.joseluisgs.storage

import com.github.michaelbull.result.*
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.model.Tenista
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.lighthousegames.logging.logging
import java.io.File

private val logger = logging()

/**
 * Implementación de la serialización de Tenista en CSV asíncrono en base a Flow
 */
class TenistasStorageCsv : TenistasStorage {
    override fun import(file: File): Flow<Result<List<Tenista>, TenistaError.StorageError>> = flow {
        logger.debug { "Importando Tenistas desde CSV asíncrono: $file" }

        // Código de lectura del fichero
        emit(readLines(file))

    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución


    override fun export(file: File, data: List<Tenista>): Flow<Result<Int, TenistaError.StorageError>> = flow {
        logger.debug { "Exportando Tenistas a CSV asíncrono: $file" }

        file.ensureFileCanExists()
            .onFailure { error ->
                emit(Err(error))
            }
            .onSuccess { file ->
                // Código de escritura del fichero
                // Escribimos la cabecera
                file.writeText("id,nombre,pais,altura,peso,puntos,mano,fechaNacimiento,createdAt,updatedAt,deletedAt,isDeleted\n")
                // Escribimos los datos
                data
                    .map { it.toTenistaDto() }
                    .forEach { tenista ->
                        file.appendText(
                            "${tenista.id},${tenista.nombre},${tenista.pais},${tenista.altura},${tenista.peso},${tenista.puntos},${tenista.mano},${tenista.fechaNacimiento},${tenista.createdAt},${tenista.updatedAt},${tenista.isDeleted}\n"
                        )
                    }
                emit(Ok(data.size))
            }
    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución a IO


    private fun readLines(file: File): Result<List<Tenista>, TenistaError.StorageError> {
        if (!file.exists()) {
            return Err(TenistaError.StorageError("ERROR: El fichero no existe ${file.absolutePath}"))
        }
        return try {
            // Código de lectura del fichero
            logger.debug { "Leyendo líneas del fichero: ${file.absolutePath}" }
            Ok(file.readLines(Charsets.UTF_8)
                .drop(1) // Nos saltamos la cabecera
                .map { line -> line.split(",") } // Separamos por ,
                // hacemos un trim para quitar espacios en blanco
                .map { fila -> fila.map { it.trim() } }
                .map { items -> parseLine(items) } // Parseamos la línea
            )
        } catch (e: Exception) {
            logger.error(e) { "Error al leer el fichero: ${file.absolutePath}" }
            Err(TenistaError.StorageError("ERROR al leer el fichero ${file.absolutePath}: ${e.message}"))
        }

    }

    private fun parseLine(parts: List<String>): Tenista {
        logger.debug { "Parseando línea: $parts" }
        return TenistaDto(
            id = parts[0].toLong(),
            nombre = parts[1],
            pais = parts[2],
            altura = parts[3].toInt(),
            peso = parts[4].toInt(),
            puntos = parts[5].toInt(),
            mano = parts[6],
            fechaNacimiento = parts[7],
            // Estos campos son opcionales y pueden ser nulos o no estar en el CSV
            createdAt = parts.getOrNull(8),
            updatedAt = parts.getOrNull(9),
            isDeleted = parts.getOrNull(10)?.toBoolean() ?: false
        ).toTenista()
    }


}


