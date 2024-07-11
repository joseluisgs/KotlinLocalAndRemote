package dev.joseluisgs.storage

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.joseluisgs.error.TenistaError
import java.io.File
import java.io.IOException

// Implementación por defecto para un metodo que usaremos

fun File.ensureFileCanExists(): Result<File, TenistaError.StorageError> {
    return try {
        if (this.exists() || this.createNewFile()) {
            Ok(this)
        } else {
            Err(TenistaError.StorageError("Error al crear el fichero ${this.absolutePath}"))
        }
    } catch (e: IOException) {
        Err(TenistaError.StorageError("Error al acceder al fichero ${this.absolutePath}: ${e.message}"))
    }
}