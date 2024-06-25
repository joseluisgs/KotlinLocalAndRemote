package dev.joseluisgs.error

sealed class TenistaError(val message: String) {
    class StorageError(message: String) : TenistaError(message)
}