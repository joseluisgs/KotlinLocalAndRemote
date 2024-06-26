package dev.joseluisgs.repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface Respository<ID, T, E> {
    fun getAll(): Flow<Result<List<T>, E>>
    fun getById(id: ID): Flow<Result<T, E>>
    fun save(t: T): Flow<Result<T, E>>
    fun update(id: ID, t: T): Flow<Result<T, E>>
    fun delete(id: ID): Boolean
}