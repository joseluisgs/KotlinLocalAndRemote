package dev.joseluisgs.repository

import com.github.michaelbull.result.Result
import dev.joseluisgs.model.Tenista

interface Respository<ID, T, E> {
    suspend fun getAll(): Result<List<Tenista>, E>
    suspend fun getById(id: ID): Result<T, E>
    suspend fun save(t: T): Result<T, E>
    suspend fun update(id: ID, t: T): Result<T, E>
    suspend fun delete(id: ID): Result<ID, E>
}