package dev.joseluisgs.error

sealed class TenistaError(val message: String) {
    class StorageError(message: String) : TenistaError(message)
    class NotFound(message: String) : TenistaError(message)
    class RemoteError(message: String) : TenistaError(message)
}