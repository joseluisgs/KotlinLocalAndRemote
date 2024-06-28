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
                            item = it,
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
            .andThen { getById(id).first() }
            .andThen { remoteRepository.update(id, tenista).first() }
            .andThen { localRepository.update(id, tenista).first() }.mapBoth(
                success = {
                    // Enviamos la notificación de actualización
                    cache.put(it.id, it)
                    notificationsService.send(
                        Notification(
                            type = Notification.Type.UPDATE,
                            item = it,
                            message = "Tenista actualizado con id: ${it.id}"
                        )
                    )
                    emit(Ok(it))
                },
                failure = { emit(Err(it)) }
            )
    }.flowOn(Dispatchers.IO)

    override fun delete(id: Long): Flow<Result<Unit, TenistaError>> = flow {
        logger.debug { "Borrando tenista por id: $id" }

        getById(id).first()
            .andThen { remoteRepository.delete(id).first() }
            .andThen { localRepository.delete(id).first() }.mapBoth(
                success = {
                    // Enviamos la notificación de borrado
                    notificationsService.send(
                        Notification(
                            type = Notification.Type.DELETE,
                            message = "Tenista borrado con id: $id"
                        )
                    )
                    emit(Ok(Unit))
                },
                failure = { emit(Err(it)) }
            )
    }.flowOn(Dispatchers.IO)


    override fun import(file: File): Flow<Result<Int, TenistaError>> = flow {
        logger.debug { "Importando tenistas desde fichero: ${file.name}" }
        // Debemos saber si la extensión es CSV o JSON si no error
        when (file.extension.lowercase()) {
            "csv" -> importCsv(file)
            "json" -> importJson(file)
            else -> emit(Err(TenistaError.StorageError("Formato de fichero de importación no soportado")))
        }
    }.flowOn(Dispatchers.IO)

    private fun importCsv(file: File) {
        TODO("Not yet implemented")
    }

    private fun importJson(file: File) {
        TODO("Not yet implemented")
    }

    override fun export(file: File): Flow<Result<Int, TenistaError>> {
        logger.debug { "Exportando tenistas a fichero: ${file.name}" }
        // Debemos saber si la extensión es CSV o JSON si no error
        return when (file.extension.lowercase()) {
            "csv" -> exportCsv(file)
            "json" -> exportJson(file)
            else -> flow { emit(Err(TenistaError.StorageError("Formato de fichero de exportación no soportado"))) }
        }.flowOn(Dispatchers.IO)
    }

    private fun exportCsv(file: File): Flow<Result<Int, TenistaError>> {
        TODO("Not yet implemented")
    }

    private fun exportJson(file: File): Flow<Result<Int, TenistaError>> {
        TODO("Not yet implemented")
    }
}