package dev.joseluisgs.service

import com.github.michaelbull.result.Result
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.model.Tenista
import java.io.File

const val REFRESH_TIME = 5000L // 5 segundos

interface TenistasService {
    suspend fun getAll(fromRemote: Boolean): Result<List<Tenista>, TenistaError>
    suspend fun getById(id: Long): Result<Tenista, TenistaError>
    suspend fun save(tenista: Tenista): Result<Tenista, TenistaError>
    suspend fun update(id: Long, tenista: Tenista): Result<Tenista, TenistaError>
    suspend fun delete(id: Long): Result<Long, TenistaError>
    suspend fun import(file: File): Result<Int, TenistaError>
    suspend fun export(file: File, fromRemote: Boolean): Result<Int, TenistaError>
    fun enableAutoRefresh()
    suspend fun loadData()
    fun disableAutoRefresh()
}