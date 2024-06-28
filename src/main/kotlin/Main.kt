package dev.joseluisgs

import database.SqlDeLightManager
import database.createDatabase
import dev.joseluisgs.cache.TenistasCache
import dev.joseluisgs.notifications.Notification
import dev.joseluisgs.notifications.TenistasNotifications
import dev.joseluisgs.repository.TenistasRepositoryLocal
import dev.joseluisgs.repository.TenistasRepositoryRemote
import dev.joseluisgs.rest.API_TENISTAS_URL
import dev.joseluisgs.rest.getKtorFitClient
import dev.joseluisgs.service.TenistasServiceImpl
import dev.joseluisgs.storage.TenistasStorageCsv
import dev.joseluisgs.storage.TenistasStorageJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.lighthousegames.logging.logging

private val logger = logging()

fun main(): Unit = runBlocking {
    println("🎾🎾 Hola Tenistas! 🎾🎾")

    // Iniciamos el servicio de Tenistas
    val tenistasService = TenistasServiceImpl(
        localRepository = TenistasRepositoryLocal(SqlDeLightManager(createDatabase("tenistas.db"))),
        remoteRepository = TenistasRepositoryRemote(getKtorFitClient(API_TENISTAS_URL).create()),
        cache = TenistasCache(size = 5),
        csvStorage = TenistasStorageCsv(),
        jsonStorage = TenistasStorageJson(),
        notificationsService = TenistasNotifications()
    )

    // Iniciamos la escucha de notificaciones de tenistas en una corrutina para que se ejecute en segundo plano
    val notificationJob = launch {
        tenistasService.notifications.distinctUntilChanged()
            //.onEach {
            //    logger.debug { "Notificación recibida: ${it.type} ${it.item}" }
            //}
            .collect {
                when (it.type) {
                    Notification.Type.CREATE -> println("🟢 Notificación de creación de tenista: ${it.item}")
                    Notification.Type.UPDATE -> println("🟠 Notificación de actualización de tenista: ${it.item}")
                    Notification.Type.DELETE -> println("🔴 Notificación de borrado de tenista: ${it.item}")
                    Notification.Type.REFRESH -> println("🔵 Notificación de refresco de tenistas")
                }

            }
    }

    delay(2000)

    // Obtenemos todos los tenistas
    val tenistas = tenistasService.getAll().first()
    println("Tenistas: $tenistas")


    // Esperamos 3 segundos
    delay(3000)
    notificationJob.cancel() // Cancelamos la escucha de notificaciones
    println("👋👋 Adios Tenistas 👋👋")

}