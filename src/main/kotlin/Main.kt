package dev.joseluisgs

import com.github.michaelbull.result.mapBoth
import dev.joseluisgs.di.appModule
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.notifications.Notification
import dev.joseluisgs.service.TenistasServiceImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.fileProperties
import org.koin.test.verify.verify
import org.lighthousegames.logging.logging
import java.time.LocalDate
import kotlin.io.path.Path

private val logger = logging()

@OptIn(KoinExperimentalAPI::class)
fun main() {
    // Inicializamos Koin
    startKoin {
        // declare used logger
        printLogger()
        fileProperties("/config.properties")
        appModule.verify(extraTypes = listOf(Boolean::class, app.cash.sqldelight.db.SqlDriver::class))
        modules(appModule)
    }

    App().run()

}

class App : KoinComponent {
    fun run(): Unit = runBlocking {
        println("🎾🎾 Hola Tenistas! 🎾🎾")

        // Iniciamos el servicio de Tenistas
        /*val tenistasService = TenistasServiceImpl(
            localRepository = TenistasRepositoryLocal(SqlDeLightManager(createDatabase("tenistas.db"))),
            remoteRepository = TenistasRepositoryRemote(getKtorFitClient(API_TENISTAS_URL).create()),
            cache = TenistasCacheImpl(size = 5),
            csvStorage = TenistasStorageCsv(),
            jsonStorage = TenistasStorageJson(),
            notificationsService = TenistasNotifications(),
            autoRefresh = true // Activamos el refresco automático
        )*/

        val tenistasService: TenistasServiceImpl by inject()

        // Iniciamos la escucha de notificaciones de tenistas en una corrutina para que se ejecute en segundo plano
        val notificationJob = launch {
            println("🔊 Escuchando notificaciones de tenistas 🔊")
            tenistasService.notifications.distinctUntilChanged()
                //.onEach {
                //    logger.debug { "Notificación recibida: ${it.type} ${it.item}" }
                //}
                .collect {
                    when (it.type) {
                        Notification.Type.CREATE -> println("🟢 Notificación de creación de tenista: ${it.message} -> ${it.item}")
                        Notification.Type.UPDATE -> println("🟠 Notificación de actualización de tenista: ${it.message} -> ${it.item}")
                        Notification.Type.DELETE -> println("🔴 Notificación de borrado de tenista: ${it.message}")
                        Notification.Type.REFRESH -> println("🔵 Notificación de refresco de tenistas: ${it.message}")
                    }

                }
        }

        // Iniciamos el refresco automático de tenistas (podróas hacerlo con una corrutina, pero ya esta implementada dentro, es para que veas otra forma
        // Si no sería como las notificaciones
        // Si no quieres refresh automático, usa loadData para cargar los datos por primera vez o getAll para obtenerlos
        tenistasService.refresh()

        delay(2000)

        // Obtenemos todos los tenistas
        var tenistas = tenistasService.getAll(false).first().mapBoth(
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
        tenistas = tenistasService.getAll(false).first().mapBoth(
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
        tenistas = tenistasService.getAll(false).first().mapBoth(
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
        tenistasService.getAll(false).first().mapBoth(
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
        delay(5000)
        println("🔇 Desactivamos la escucha de notificaciones de tenistas 🔇")
        notificationJob.cancel() // Cancelamos la escucha de notificaciones, porque ya no nos interesa

        // Pruebas de ficheros CSV Import
        val csvImport = Path("data", "tenistas2.csv").toFile()
        tenistasService.import(csvImport).first().mapBoth(
            success = { println("Tenistas importados desde cvs: $it") },
            failure = { println(it.message) }
        )

        // Pruebas de ficheros JSON Import
        val jsonImport = Path("data", "tenistas3.json").toFile()
        tenistasService.import(jsonImport).first().mapBoth(
            success = { println("Tenistas importados desde json: $it") },
            failure = { println(it.message) }
        )


        // Pruebas de ficheros CSV Export
        val csvExport = Path("data", "tenistas_export.csv").toFile()
        tenistasService.export(csvExport, fromRemote = true).first().mapBoth(
            success = { println("Tenistas exportados a cvs: $it") },
            failure = { println(it.message) }
        )

        // Pruebas de ficheros JSON Export
        val jsonExport = Path("data", "tenistas_export.json").toFile()
        tenistasService.export(jsonExport, fromRemote = true).first().mapBoth(
            success = { println("Tenistas exportados a json: $it") },
            failure = { println(it.message) }
        )

        // Consultamos todos los tenistas para hacerles las consultas
        tenistas = tenistasService.getAll(true).first().mapBoth(
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

        // tenistas ordenados con ranking, es decir, por puntos de mayor a menor
        println("Tenistas ordenados por ranking")
        tenistas.sortedByDescending { it.puntos }.forEachIndexed { index, tenista ->
            println("Ranking ${index + 1}: ${tenista.nombre} -> ${tenista.puntos}")
        }

        // Media de altura de los tenistas
        val mediaAltura = tenistas.map { it.altura }.average()
        println("Media de altura de los tenistas: $mediaAltura")

        // Media de peso de los tenistas
        val mediaPeso = tenistas.map { it.peso }.average()
        println("Media de peso de los tenistas: $mediaPeso")

        // Tenista más alto
        val tenistaMasAlto = tenistas.maxByOrNull { it.altura }
        println("Tenista más alto: $tenistaMasAlto")

        // Tenista más bajo
        val tenistaMasBajo = tenistas.minByOrNull { it.altura }
        println("Tenista más bajo: $tenistaMasBajo")

        // Tenistas españoles
        val tenistasEspanoles = tenistas.filter { it.pais == "España" }
        println("Tenistas españoles: ${tenistasEspanoles.size}")

        // Tenistas agrupados por pais
        val tenistasPorPais = tenistas.groupBy { it.pais }
        tenistasPorPais.forEach { (pais, tenistas) ->
            println("Tenistas de $pais: ${tenistas.size}")
        }

        // Número de tenistas agrupados por pais y ordenados por puntos descendente
        val tenistasPorPaisOrdenados = tenistas.groupBy { it.pais }
            .mapValues { it.value.sortedByDescending { tenista -> tenista.puntos } }
        tenistasPorPaisOrdenados.forEach { (pais, tenistas) ->
            println("Tenistas de $pais: ${tenistas.size}")
            tenistas.forEach { println(it) }
        }

        // Puntuación total de los tenistas agrupados por pais
        val puntuacionTotalPorPais = tenistas.groupBy { it.pais }
            .mapValues { it.value.sumOf { tenista -> tenista.puntos } }
        puntuacionTotalPorPais.forEach { (pais, puntos) ->
            println("Puntuación total de los tenistas de $pais: $puntos")
        }

        // pais con puntuación total más alta (cogemos el resultado anterior)
        val paisMasPuntuacion = puntuacionTotalPorPais.maxByOrNull { it.value }
        println("País con más puntuación total: ${paisMasPuntuacion?.key} -> ${paisMasPuntuacion?.value}")


        println("👋👋 Adios Tenistas 👋👋")

    }
}