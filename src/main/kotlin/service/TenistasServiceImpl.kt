package dev.joseluisgs.service

import com.github.michaelbull.result.*
import dev.joseluisgs.cache.TenistasCache
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.notifications.Notification
import dev.joseluisgs.notifications.TenistasNotifications
import dev.joseluisgs.repository.TenistasRepositoryLocal
import dev.joseluisgs.repository.TenistasRepositoryRemote
import dev.joseluisgs.storage.TenistasStorageCsv
import dev.joseluisgs.storage.TenistasStorageJson
import dev.joseluisgs.validator.validate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.lighthousegames.logging.logging
import java.io.File
import kotlin.coroutines.CoroutineContext


private val logger = logging()
private const val REFRESH_TIME = 5000L // 5 segundos

class TenistasServiceImpl(
    private val localRepository: TenistasRepositoryLocal,
    private val remoteRepository: TenistasRepositoryRemote,
    private val cache: TenistasCache,
    private val csvStorage: TenistasStorageCsv,
    private val jsonStorage: TenistasStorageJson,
    private val notificationsService: TenistasNotifications

) : TenistasService {

    val notifications: SharedFlow<Notification<Tenista>>
        get() = notificationsService.notifications


    init {
        val job = Job()
        val coroutineContext: CoroutineContext = Dispatchers.IO + job
        logger.debug { "Inicializando TenistasServiceImpl" }
        // Iniciamos le refresh de los datos
        CoroutineScope(coroutineContext).launch {
            refresh()
        }
    }


    private suspend fun refresh() {
        // Lanzamos una corutina para que se ejecute en segundo plano
        do {
            logger.debug { "Refrescando el repositorio local con los datos remotos " }
            // Obtenemos los datos remotos
            localRepository.removeAll().first().andThen {
                remoteRepository.getAll().first()
            }.andThen {
                localRepository.saveAll(it).first()
            }.onFailure {
                logger.error { "Error al refrescar los datos: ${it.message}" }
            }.also {
                // Enviamos la notificación de refresco
                notificationsService.send(Notification(Notification.Type.REFRESH))
                // borramos la cache
                cache.clear()
            }
            delay(REFRESH_TIME)
        } while (true)
    }


    override fun getAll(): Flow<Result<List<Tenista>, TenistaError>> = flow {
        logger.debug { "Obteniendo todos los tenistas" }
        // Devolvemos los datos locales
        emit(localRepository.getAll().first())
    }.flowOn(Dispatchers.IO)

    override fun getById(id: Long): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Obteniendo tenista por id: $id" }
        // Buscamos en la cache, si está lo devolvemos
        cache.get(id)?.let {
            logger.debug { "Tenista encontrado en la cache: $it" }
            emit(Ok(it))
        } ?: run {
            // Buscamos en el repositorio local, y si esta lo metemos en la cache y lo devolvemos
            localRepository.getById(id).first().mapBoth(
                success = {
                    cache.put(it.id, it)
                    emit(Ok(it))
                },
                failure = {
                    // Si no está en local, lo buscamos en remoto, lo guardamos en local y lo metemos en la cache
                    remoteRepository.getById(id).first().andThen {
                        localRepository.save(it).first()
                    }.mapBoth(
                        success = {
                            cache.put(it.id, it)
                            emit(Ok(it))
                        },
                        failure = { emit(Err(it)) }
                    )
                }
            )
        }
    }.flowOn(Dispatchers.IO)

    override fun save(tenista: Tenista): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Guardando tenista: $tenista" }
        // validamos
        tenista.validate().andThen {
            // Guardamos en remoto
            remoteRepository.save(tenista).first()
        }.andThen {
            // Guardamos en local
            localRepository.save(it).first()
        }.mapBoth(
            success = {
                // Enviamos la notificación de creación
                cache.put(it.id, it)
                notificationsService.send(Notification(Notification.Type.CREATE, it))
                emit(Ok(it))
            },
            failure = { emit(Err(it)) }
        )
    }.flowOn(Dispatchers.IO)

    override fun update(id: Long, tenista: Tenista): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Actualizando tenista por id: $id" }
        // validamos
        tenista.validate().andThen {
            // Actualizamos en remoto
            remoteRepository.update(id, tenista).first()
        }.andThen {
            // Actualizamos en local
            localRepository.update(id, it).first()
        }.mapBoth(
            success = {
                // Enviamos la notificación de actualización
                cache.put(it.id, it)
                notificationsService.send(Notification(Notification.Type.UPDATE, it))
                emit(Ok(it))
            },
            failure = { emit(Err(it)) }
        )
    }.flowOn(Dispatchers.IO)

    override fun delete(id: Long): Flow<Result<Unit, TenistaError>> = flow {
        logger.debug { "Borrando tenista por id: $id" }
        // Borramos en remoto
        remoteRepository.delete(id).first().andThen {
            // Borramos en local
            localRepository.delete(id).first()
        }.mapBoth(
            success = {
                // Enviamos la notificación de borrado
                notificationsService.send(Notification(Notification.Type.DELETE))
                emit(Ok(Unit))
            },
            failure = { emit(Err(it)) }
        )
    }.flowOn(Dispatchers.IO)


    override fun import(file: File): Flow<Result<Int, TenistaError>> {
        TODO("Not yet implemented")
    }

    override fun export(file: File): Flow<Result<Int, TenistaError>> {
        TODO("Not yet implemented")
    }
}