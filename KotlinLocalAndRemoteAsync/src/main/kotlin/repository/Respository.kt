package dev.joseluisgs.repository

import com.github.michaelbull.result.Result
import dev.joseluisgs.model.Tenista
import kotlinx.coroutines.Deferred

interface Respository<ID, T, E> {
    suspend fun getAll(): Deferred<Result<List<Tenista>, E>>
    suspend fun getById(id: ID): Deferred<Result<T, E>>
    suspend fun save(t: T): Deferred<Result<T, E>>
    suspend fun update(id: ID, t: T): Deferred<Result<T, E>>
    suspend fun delete(id: ID): Deferred<Result<ID, E>>
}