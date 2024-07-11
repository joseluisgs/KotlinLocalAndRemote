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
import kotlinx.coroutines.flow.SharedFlow
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
    private val autoRefresh: Long = REFRESH_TIME,
) : TenistasService {

    // Propiedad para las notificaciones
    val notifications: SharedFlow<Notification<TenistaDto>>
        get() = notificationsService.notifications

    // Para el trabajo en segundo plano
    private var job: Job? = null
    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + (job ?: Job())


    override fun enableAutoRefresh() {
        logger.debug { "Refrescando el repositorio local con los datos remotos " }
        if (job?.isActive == true) {
            logger.debug { "El trabajo en segundo plano ya está activo" }
            return
        }
        job = Job()
        logger.debug { "Iniciando trabajo en segundo plano para refrescar los datos" }
        CoroutineScope(coroutineContext).launch {
            do {
                loadData()
                delay(autoRefresh)
            } while (true)
        }
    }

    override fun disableAutoRefresh() {
        if (job?.isActive == true) {
            logger.debug { "Desactivando el trabajo en segundo plano" }
            job?.cancel()
            job = null
        } else {
            logger.debug { "El trabajo en segundo plano no está activo" }
        }
    }

    override suspend fun loadData() {
        logger.debug { "Cargando datos en repositorio local con datos remotos" }
        localRepository.removeAll().await()
            .andThen { remoteRepository.getAll().await() } // Obtenemos los datos remotos
            .andThen { localRepository.saveAll(it).await() }.also {
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


    override suspend fun getAll(fromRemote: Boolean): Deferred<Result<List<Tenista>, TenistaError>> =
        withContext(Dispatchers.IO) {
            async {
                logger.debug { "Obteniendo todos los tenistas" }
                // Devolvemos los datos locales
                if (!fromRemote) {
                    localRepository.getAll().await().mapBoth(
                        success = { Ok(it) },
                        failure = { Err(it) }
                    )
                } else {
                    localRepository.removeAll().await() // Borramos los datos locales
                        .andThen { remoteRepository.getAll().await() } // Obtenemos los datos remotos
                        .andThen { localRepository.saveAll(it).await() }
                        .andThen { localRepository.getAll().await() }.mapBoth(
                            success = { Ok(it) },
                            failure = { Err(it) }
                        ).also { cache.clear() }  // Limpiamos la cache en el caso de que sea remoto
                }
            }
        }

    override suspend fun getById(id: Long): Deferred<Result<Tenista, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Obteniendo tenista por id: $id" }
            // Buscamos en la cache, si está lo devolvemos
            cache.get(id)?.let {
                logger.debug { "Tenista encontrado en la cache: $it" }
                Ok(it)
            } ?: run {
                // Buscamos en el repositorio local, y si esta lo metemos en la cache y lo devolvemos
                localRepository.getById(id).await().mapBoth(
                    success = {
                        cache.put(it.id, it)
                        Ok(it)
                    },
                    failure = {
                        // Si no está en local, lo buscamos en remoto, lo guardamos en local y lo metemos en la cache
                        remoteRepository.getById(id).await().andThen { localRepository.save(it).await() }.mapBoth(
                            success = {
                                cache.put(it.id, it)
                                Ok(it)
                            },
                            failure = { Err(it) }
                        )
                    }
                )
            }
        }
    }

    override suspend fun save(tenista: Tenista): Deferred<Result<Tenista, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Guardando tenista: $tenista" }
            // validamos
            tenista.validate()
                .andThen { remoteRepository.save(tenista).await() }
                .andThen { localRepository.save(it).await() }.mapBoth(
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
                        Ok(it)
                    },
                    failure = { Err(it) }
                )
        }
    }

    override suspend fun update(id: Long, tenista: Tenista): Deferred<Result<Tenista, TenistaError>> =
        withContext(Dispatchers.IO) {
            async {
                logger.debug { "Actualizando tenista por id: $id" }
                // validamos
                tenista.validate()
                    .andThen { getById(id).await() } // No es porque va a fallar en remoto si no existe, es un ejemplo para concatenar cosas
                    .andThen { remoteRepository.update(id, tenista).await() }
                    .andThen { localRepository.update(id, tenista).await() }
                    .mapBoth(
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
                            Ok(it)
                        },
                        failure = { Err(it) }
                    )
            }
        }

    override suspend fun delete(id: Long): Deferred<Result<Long, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Borrando tenista por id: $id" }

            getById(id).await()
                .andThen {
                    remoteRepository.delete(id).await()
                } /// No es porque va a fallar en remoto si no existe, es un ejemplo para concatenar cosas
                .andThen { localRepository.delete(id).await() }.mapBoth(
                    success = {
                        // Enviamos la notificación de borrado
                        notificationsService.send(
                            Notification(
                                type = Notification.Type.DELETE,
                                message = "Tenista borrado con id: $id"
                            )
                        )
                        cache.remove(id)
                        Ok(id)
                    },
                    failure = { Err(it) }
                )
        }
    }


    override suspend fun import(file: File): Deferred<Result<Int, TenistaError>> = withContext(Dispatchers.IO) {
        async {
            logger.debug { "Importando tenistas desde fichero: ${file.name}" }
            // Debemos saber si la extensión es CSV o JSON si no error
            when (file.extension.lowercase()) {
                "csv" -> importCsv(file)
                "json" -> importJson(file)
                else -> Err(TenistaError.StorageError("Formato de fichero de importación no soportado. Solo CSV o JSON"))
            }
        }
    }

    // No hace falta poner el Deferred, solo en el caso de que se necesite
    private suspend fun importCsv(file: File): Result<Int, TenistaError> {
        logger.debug { "Importando tenistas desde fichero CSV: ${file.name}" }
        // Leemos el fichero CSV
        return csvStorage.import(file).await().mapBoth(
            success = { saveAll(it) },
            failure = { Err(it) }
        )
    }

    private suspend fun importJson(file: File): Result<Int, TenistaError> {
        logger.debug { "Importando tenistas desde fichero JSON: ${file.name}" }
        // Leemos el fichero JSON
        return jsonStorage.import(file).await().mapBoth(
            success = { saveAll(it) },
            failure = { Err(it) }
        )
    }

    private suspend fun saveAll(tenistas: List<Tenista>): Result<Int, TenistaError> {
        logger.debug { "Guardando todos los tenistas: ${tenistas.size}" }
        // Recorro los tenistas y los guardo en remoto y local
        var contador = 0
        localRepository.removeAll().await().mapBoth(
            success = {
                tenistas.forEach { tenista ->
                    remoteRepository.save(tenista).await()
                        .andThen { localRepository.save(it).await() }
                        .andThen { Ok(contador++) }
                }
            },
            failure = { return Err(it) }
        )
        return Ok(contador)

    }

    override suspend fun export(file: File, fromRemote: Boolean): Deferred<Result<Int, TenistaError>> =
        withContext(Dispatchers.IO) {
            async {
                logger.debug { "Exportando tenistas a fichero: ${file.name}" }
                // Debemos saber si la extensión es CSV o JSON si no error
                when (file.extension.lowercase()) {
                    "csv" -> exportCsv(file, fromRemote)
                    "json" -> exportJson(file, fromRemote)
                    else -> Err(TenistaError.StorageError("Formato de fichero de exportación no soportado. Solo CSV o JSON"))
                }
            }
        }

    private suspend fun exportCsv(file: File, fromRemote: Boolean = false): Result<Int, TenistaError> {
        logger.debug { "Exportando tenistas a fichero CSV: ${file.name}" }
        // Obtenemos los tenistas (podemos elegir sin remoto o local
        return if (fromRemote) {
            remoteRepository.getAll().await().andThen { tenistas ->
                csvStorage.export(file, tenistas).await()
            }
        } else {
            localRepository.getAll().await().andThen { tenistas ->
                csvStorage.export(file, tenistas).await()
            }
        }
    }

    private suspend fun exportJson(file: File, remote: Boolean = false): Result<Int, TenistaError> {
        logger.debug { "Exportando tenistas a fichero JSON: ${file.name}" }
        // Obtenemos los tenistas
        return if (remote) {
            remoteRepository.getAll().await().andThen { tenistas ->
                jsonStorage.export(file, tenistas).await()
            }
        } else {
            localRepository.getAll().await().andThen { tenistas ->
                jsonStorage.export(file, tenistas).await()
            }
        }
    }


}