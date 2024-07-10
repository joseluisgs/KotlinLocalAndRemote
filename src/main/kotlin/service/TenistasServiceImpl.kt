package dev.joseluisgs.service

import com.github.michaelbull.result.*
import dev.joseluisgs.cache.TenistasCache
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.notification.Notification
import dev.joseluisgs.notification.TenistasNotifications
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


class TenistasServiceImpl(
    private val localRepository: TenistasRepositoryLocal,
    private val remoteRepository: TenistasRepositoryRemote,
    private val cache: TenistasCache,
    private val csvStorage: TenistasStorageCsv,
    private val jsonStorage: TenistasStorageJson,
    private val notificationsService: TenistasNotifications,
    autoRefresh: Long = REFRESH_TIME,
) : TenistasService {

    val notifications: SharedFlow<Notification<TenistaDto>>
        get() = notificationsService.notifications


    override fun refresh() {
        // Lanzamos una corutina para que se ejecute en segundo plano, y así no llamarlo en el hilo principal
        val job = Job()
        val coroutineContext: CoroutineContext = Dispatchers.IO + job
        logger.debug { "Inicializando TenistasServiceImpl" }
        CoroutineScope(coroutineContext).launch {
            do {
                logger.debug { "Refrescando el repositorio local con los datos remotos " }
                loadData()
                delay(REFRESH_TIME)
            } while (true)
        }
    }

    override suspend fun loadData() {
        localRepository.removeAll().first() // Borramos los datos locales
            .andThen { remoteRepository.getAll().first() } // Obtenemos los datos remotos
            .andThen { localRepository.saveAll(it).first() }.also {
                // Enviamos la notificación de refresco
                notificationsService.send(
                    Notification(
                        type = Notification.Type.REFRESH,
                        message = "Nuevos datos disponibles: ${it.value}"
                    )
                )
                cache.clear() // Limpiamos la cache
            }
    }


    override fun getAll(fromRemote: Boolean): Flow<Result<List<Tenista>, TenistaError>> = flow {
        logger.debug { "Obteniendo todos los tenistas" }
        // Devolvemos los datos locales
        if (!fromRemote) {
            localRepository.getAll().first().mapBoth(
                success = { emit(Ok(it)) },
                failure = { emit(Err(it)) }
            )
        } else {
            localRepository.removeAll().first() // Borramos los datos locales
                .andThen { remoteRepository.getAll().first() } // Obtenemos los datos remotos
                .andThen { localRepository.saveAll(it).first() }
                .andThen { localRepository.getAll().first() }.mapBoth(
                    success = { emit(Ok(it)) },
                    failure = { emit(Err(it)) }
                ).also { cache.clear() }  // Limpiamos la cache en el caso de que sea remoto
        }
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
                    remoteRepository.getById(id).first().andThen { localRepository.save(it).first() }.mapBoth(
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
        tenista.validate()
            .andThen { remoteRepository.save(tenista).first() }
            .andThen { localRepository.save(it).first() }.mapBoth(
                success = {
                    // Enviamos la notificación de creación
                    cache.put(it.id, it)
                    notificationsService.send(
                        Notification(
                            type = Notification.Type.CREATE,
                            item = it.toTenistaDto(),
                            message = "Nuevo tenista creado con id: ${it.id}"
                        )
                    )
                    emit(Ok(it))
                },
                failure = { emit(Err(it)) }
            )
    }.flowOn(Dispatchers.IO)

    override fun update(id: Long, tenista: Tenista): Flow<Result<Tenista, TenistaError>> = flow {
        logger.debug { "Actualizando tenista por id: $id" }
        // validamos
        tenista.validate()
            .andThen { getById(id).first() } // No es porque va a fallar en remoto si no existe, es un ejemplo para concatenar cosas
            .andThen { remoteRepository.update(id, tenista).first() }
            .andThen { localRepository.update(id, tenista).first() }.mapBoth(
                success = {
                    // Enviamos la notificación de actualización
                    cache.put(it.id, it)
                    notificationsService.send(
                        Notification(
                            type = Notification.Type.UPDATE,
                            item = it.toTenistaDto(),
                            message = "Tenista actualizado con id: ${it.id}"
                        )
                    )
                    emit(Ok(it))
                },
                failure = { emit(Err(it)) }
            )
    }.flowOn(Dispatchers.IO)

    override fun delete(id: Long): Flow<Result<Long, TenistaError>> = flow {
        logger.debug { "Borrando tenista por id: $id" }

        getById(id).first()
            .andThen {
                remoteRepository.delete(id).first()
            } /// No es porque va a fallar en remoto si no existe, es un ejemplo para concatenar cosas
            .andThen { localRepository.delete(id).first() }.mapBoth(
                success = {
                    // Enviamos la notificación de borrado
                    notificationsService.send(
                        Notification(
                            type = Notification.Type.DELETE,
                            message = "Tenista borrado con id: $id"
                        )
                    )
                    cache.remove(id)
                    emit(Ok(id))
                },
                failure = { emit(Err(it)) }
            )
    }.flowOn(Dispatchers.IO)


    override fun import(file: File): Flow<Result<Int, TenistaError>> = flow {
        logger.debug { "Importando tenistas desde fichero: ${file.name}" }
        // Debemos saber si la extensión es CSV o JSON si no error
        when (file.extension.lowercase()) {
            "csv" -> emit(importCsv(file))
            "json" -> emit(importJson(file))
            else -> emit(Err(TenistaError.StorageError("Formato de fichero de importación no soportado. Solo CSV o JSON")))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun importCsv(file: File): Result<Int, TenistaError> {
        logger.debug { "Importando tenistas desde fichero CSV: ${file.name}" }
        // Leemos el fichero CSV
        return csvStorage.import(file).first().mapBoth(
            success = { saveAll(it) },
            failure = { Err(it) }
        )
    }

    private suspend fun importJson(file: File): Result<Int, TenistaError> {
        logger.debug { "Importando tenistas desde fichero JSON: ${file.name}" }
        // Leemos el fichero JSON
        return jsonStorage.import(file).first().mapBoth(
            success = { saveAll(it) },
            failure = { Err(it) }
        )
    }

    private suspend fun saveAll(tenistas: List<Tenista>): Result<Int, TenistaError> {
        logger.debug { "Guardando todos los tenistas: ${tenistas.size}" }
        // Recorro los tenistas y los guardo en remoto y local
        var contador = 0
        localRepository.removeAll().first().mapBoth(
            success = {
                tenistas.forEach { tenista ->
                    remoteRepository.save(tenista).first()
                        .andThen { localRepository.save(it).first() }
                        .andThen { Ok(contador++) }
                }
            },
            failure = { return Err(it) }
        )
        return Ok(contador)

    }

    override fun export(file: File, fromRemote: Boolean): Flow<Result<Int, TenistaError>> = flow {
        logger.debug { "Exportando tenistas a fichero: ${file.name}" }
        // Debemos saber si la extensión es CSV o JSON si no error
        when (file.extension.lowercase()) {
            "csv" -> emit(exportCsv(file, fromRemote))
            "json" -> emit(exportJson(file, fromRemote))
            else -> emit(Err(TenistaError.StorageError("Formato de fichero de exportación no soportado. Solo CSV o JSON")))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun exportCsv(file: File, fromRemote: Boolean = false): Result<Int, TenistaError> {
        logger.debug { "Exportando tenistas a fichero CSV: ${file.name}" }
        // Obtenemos los tenistas (podemos elegir sin remoto o local
        return if (fromRemote) {
            remoteRepository.getAll().first().andThen { tenistas ->
                csvStorage.export(file, tenistas).first()
            }
        } else {
            localRepository.getAll().first().andThen { tenistas ->
                csvStorage.export(file, tenistas).first()
            }
        }
    }

    private suspend fun exportJson(file: File, remote: Boolean = false): Result<Int, TenistaError> {
        logger.debug { "Exportando tenistas a fichero JSON: ${file.name}" }
        // Obtenemos los tenistas
        return if (remote) {
            remoteRepository.getAll().first().andThen { tenistas ->
                jsonStorage.export(file, tenistas).first()
            }
        } else {
            localRepository.getAll().first().andThen { tenistas ->
                jsonStorage.export(file, tenistas).first()
            }
        }
    }


}