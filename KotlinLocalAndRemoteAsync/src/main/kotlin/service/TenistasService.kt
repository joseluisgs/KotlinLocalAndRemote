package dev.joseluisgs.service

import com.github.michaelbull.result.Result
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.model.Tenista
import kotlinx.coroutines.Deferred
import java.io.File

const val REFRESH_TIME = 5000L // 5 segundos

interface TenistasService {
    suspend fun getAll(fromRemote: Boolean): Deferred<Result<List<Tenista>, TenistaError>>
    suspend fun getById(id: Long): Deferred<Result<Tenista, TenistaError>>
    suspend fun save(tenista: Tenista): Deferred<Result<Tenista, TenistaError>>
    suspend fun update(id: Long, tenista: Tenista): Deferred<Result<Tenista, TenistaError>>
    suspend fun delete(id: Long): Deferred<Result<Long, TenistaError>>
    suspend fun import(file: File): Deferred<Result<Int, TenistaError>>
    suspend fun export(file: File, fromRemote: Boolean): Deferred<Result<Int, TenistaError>>
    fun enableAutoRefresh()
    suspend fun loadData()
    fun disableAutoRefresh()
}