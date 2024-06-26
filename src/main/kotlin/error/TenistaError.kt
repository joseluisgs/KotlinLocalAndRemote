package dev.joseluisgs.error

sealed class TenistaError(val message: String) {
    class StorageError(message: String) : TenistaError("ERROR: $message")
    class NotFound(id: Long) : TenistaError("ERROR: No se ha encontrado el tenista con id $id")
}