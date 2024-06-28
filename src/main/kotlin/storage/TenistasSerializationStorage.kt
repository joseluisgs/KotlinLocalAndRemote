package dev.joseluisgs.storage

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import java.io.File
import java.io.IOException

interface TenistasSerializationStorage : SerializationStorage<Tenista, TenistaError.StorageError> {

    // Implementación por defecto para un metodo de la interfaz
    fun ensureFileCanExists(file: File): Result<File, TenistaError.StorageError> {
        return try {
            if (file.exists() || file.createNewFile()) {
                Ok(file)
            } else {
                Err(TenistaError.StorageError("Error al crear el fichero ${file.absolutePath}"))
            }
        } catch (e: IOException) {
            Err(TenistaError.StorageError("Error al acceder al fichero ${file.absolutePath}: ${e.message}"))
        }
    }
}