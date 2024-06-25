package dev.joseluisgs.storage

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.models.Tenista
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
class TenistasSerializationCsv : TenistasSerializationStorage {
    override fun import(file: File): Flow<Result<List<Tenista>, TenistaError>> = flow {
        logger.debug { "Importando Tenistas desde CSV asíncrono: $file" }

        // Código de lectura del fichero
        if (!file.exists()) {
            emit(Err(TenistaError.StorageError("El fichero no existe ${file.absolutePath}")))
        }
        emit(readLines(file))
    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución


    override fun export(file: File, data: List<Tenista>): Flow<Result<Int, TenistaError>> = flow {
        logger.debug { "Exportando Tenistas a CSV asíncrono: $file" }

        // También podemos usar Files.deleteIfExists(file.toPath()) para borrar el fichero
        // No es necesario comprobar si existe, ya que si no existe lo crea con el writeText
        /*if (file.exists()) {
            file.delete() // Borramos el fichero si existe
        }*/
        try {
            // Código de escritura del fichero
            // Escribimos la cabecera
            file.writeText("id,nombre,pais,altura,peso,puntos,mano,fechaNacimiento,createdAt,updatedAt,deletedAt,isDeleted\n")
            // Escribimos los datos
            data.forEach { tenista ->
                file.appendText(
                    "${tenista.id},${tenista.nombre},${tenista.pais},${tenista.altura},${tenista.peso},${tenista.puntos},${tenista.mano},${tenista.fechaNacimiento},${tenista.createdAt},${tenista.updatedAt},${tenista.isDeleted}\n"
                )
            }
            emit(Ok(data.size))
        } catch (e: Exception) {
            logger.error(e) { "Error al escribir el fichero: ${file.absolutePath}" }
            emit(Err(TenistaError.StorageError("Error al escribir el fichero ${file.absolutePath}: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO) // Cambiamos el contexto de ejecución a IO


    private fun readLines(file: File): Result<List<Tenista>, TenistaError> {
        return try {
            // Código de lectura del fichero
            logger.debug { "Leyendo líneas del fichero: ${file.absolutePath}" }
            return Ok(file.readLines(Charsets.UTF_8)
                .drop(1) // Nos saltamos la cabecera
                .map { line -> line.split(",") } // Separamos por ,
                .map { parts -> parseLine(parts) } // Parseamos la línea
            )
        } catch (e: Exception) {
            logger.error(e) { "Error al leer el fichero: ${file.absolutePath}" }
            Err(TenistaError.StorageError("Error al leer el fichero ${file.absolutePath}: ${e.message}"))
        }
    }
}

private fun parseLine(parts: List<String>): Tenista {
    logger.debug { "Parseando línea: $parts" }
    return TenistaDto(
        id = parts[0],
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
