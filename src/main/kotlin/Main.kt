package dev.joseluisgs

import com.github.michaelbull.result.mapBoth
import database.SqlDeLightManager
import database.createDatabase
import dev.joseluisgs.cache.TenistasCache
import dev.joseluisgs.models.Tenista
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
import java.time.LocalDate

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
    var tenistas = tenistasService.getAll().first().mapBoth(
        success = {
            println("Tenistas obtenidos: ${it.size}")
            println(it)
            it
        },
        failure = {
            println(it.message)
            emptyList()
        }
    )

    // Obtenemos un tenista por id que existes
    tenistasService.getById(1).first().mapBoth(
        success = { println("Tenista obtenido: $it") },
        failure = { println(it.message) }
    )

    // Volvemos a obtener el mismo tenista ahora debe estar en la cache
    tenistasService.getById(1).first().mapBoth(
        success = { println("Tenista obtenido: $it") },
        failure = { println(it.message) }
    )

    // Obtenemos un tenista por id que no existe
    tenistasService.getById(-1).first().mapBoth(
        success = { println("Tenista obtenido: $it") },
        failure = { println(it.message) }
    )

    // Guardamos un tenista
    val newTenista = tenistas.first().copy(
        id = Tenista.NEW_ID,
        nombre = "Test New",
        fechaNacimiento = LocalDate.parse("1986-06-03"),
        puntos = 10
    )
    tenistasService.save(newTenista).first().mapBoth(
        success = {
            println("Tenista guardado: $it")
            it
        },
        failure = { println(it.message) }
    )

    // Obtenemos todos los tenistas
    tenistas = tenistasService.getAll().first().mapBoth(
        success = {
            println("Tenistas obtenidos: ${it.size}")
            println(it)
            it
        },
        failure = {
            println(it.message)
            emptyList()
        }
    )


    // Actualizamos un tenista
    val updatedTenista = tenistas.first().copy(
        nombre = "Test Update",
        fechaNacimiento = LocalDate.parse("1986-06-03"),
        puntos = 20
    )
    tenistasService.update(updatedTenista.id, updatedTenista).first().mapBoth(
        success = { println("Tenista actualizado: $it") },
        failure = { println(it.message) }
    )

    // Obtenemos todos los tenistas
    tenistas = tenistasService.getAll().first().mapBoth(
        success = {
            println("Tenistas obtenidos: ${it.size}")
            println(it)
            it
        },
        failure = {
            println(it.message)
            emptyList()
        }
    )

    // Actualizamos un tenista que no existe
    tenistasService.update(-1, updatedTenista).first().mapBoth(
        success = { println("Tenista actualizado: $it") },
        failure = { println(it.message) }
    )

    // Borramos un tenista
    tenistasService.delete(updatedTenista.id).first().mapBoth(
        success = { println("Tenista borrado") },
        failure = { println(it.message) }
    )

    // Obtenemos todos los tenistas
    tenistas = tenistasService.getAll().first().mapBoth(
        success = {
            println("Tenistas obtenidos: ${it.size}")
            println(it)
            it
        },
        failure = {
            println(it.message)
            emptyList()
        }
    )

    // Borramos un tenista que no existe
    tenistasService.delete(-1).first().mapBoth(
        success = { println("Tenista borrado") },
        failure = { println(it.message) }
    )


    // Esperamos 3 segundos
    delay(3000)
    notificationJob.cancel() // Cancelamos la escucha de notificaciones
    println("👋👋 Adios Tenistas 👋👋")

}