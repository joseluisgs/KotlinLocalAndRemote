package dev.joseluisgs.storage

import com.github.michaelbull.result.Result
import kotlinx.coroutines.Deferred
import java.io.File

interface SerializationStorage<T, E> {
    suspend fun import(file: File): Deferred<Result<List<T>, E>>
    suspend fun export(file: File, data: List<T>): Deferred<Result<Int, E>>
}