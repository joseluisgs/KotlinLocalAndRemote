package dev.joseluisgs.storage

import com.github.michaelbull.result.Result
import java.io.File

interface SerializationStorage<T, E> {
    suspend fun import(file: File): Result<List<T>, E>
    suspend fun export(file: File, data: List<T>): Result<Int, E>
}