package dev.joseluisgs.storage

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SerializationStorage<T, E> {
    fun import(file: File): Flow<Result<List<T>, E>>
    fun export(file: File, data: List<T>): Flow<Result<Int, E>>
}