package dev.joseluisgs.service

import com.github.michaelbull.result.Result
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.model.Tenista
import kotlinx.coroutines.flow.Flow
import java.io.File

const val REFRESH_TIME = 5000L // 5 segundos

interface TenistasService {
    fun getAll(fromRemote: Boolean): Flow<Result<List<Tenista>, TenistaError>>
    fun getById(id: Long): Flow<Result<Tenista, TenistaError>>
    fun save(tenista: Tenista): Flow<Result<Tenista, TenistaError>>
    fun update(id: Long, tenista: Tenista): Flow<Result<Tenista, TenistaError>>
    fun delete(id: Long): Flow<Result<Long, TenistaError>>
    fun import(file: File): Flow<Result<Int, TenistaError>>
    fun export(file: File, fromRemote: Boolean): Flow<Result<Int, TenistaError>>
    fun enableAutoRefresh()
    suspend fun loadData()
    fun disableAutoRefresh()
}