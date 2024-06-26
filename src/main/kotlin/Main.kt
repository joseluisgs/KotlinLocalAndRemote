package dev.joseluisgs

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import database.SqlDeLightManager
import database.createInMemoryDatabase
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.repository.TenistasRepositoryLocal
import dev.joseluisgs.storage.TenistasSerializationCsv
import dev.joseluisgs.storage.TenistasSerializationJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.io.path.Path

fun main(): Unit = runBlocking {
    println("Hola Tenistas!")
    val tenistasCsv = TenistasSerializationCsv()

    // Prueba de importación de tenistas desde CSV
    var misTenistas = listOf<Tenista>()
    tenistasCsv.import(Path("data", "tenistas.csv").toFile()).first()
        .onSuccess { tenistas ->
            println("Importados ${tenistas.size} tenistas")
            misTenistas = tenistas.toMutableList()
            println(tenistas)
        }.onFailure { error ->
            println(error.message)
        }

    // Prueba de exportación de tenistas a CSV
    tenistasCsv.export(Path("data", "tenistas_export.csv").toFile(), misTenistas).first()
        .onSuccess { size ->
            println("Exportados $size tenistas")
        }.onFailure { error ->
            println(error.message)
        }

    val tenistasJson = TenistasSerializationJson()
    // Prueba de exportación de tenistas a JSON
    tenistasJson.export(Path("data", "tenistas_export.json").toFile(), misTenistas).first()
        .onSuccess { size ->
            println("Exportados $size tenistas")
        }
        .onFailure { error ->
            println(error.message)
        }


    // Prueba de importación de tenistas desde JSON
    tenistasJson.import(Path("data", "tenistas_export.json").toFile()).first()
        .onSuccess { tenistas ->
            println("Importados ${tenistas.size} tenistas")
            println(tenistas)
        }.onFailure { error ->
            println(error.message)
        }

    // jugando con repositorio local
    val tenistasRepositoryLocal = TenistasRepositoryLocal(SqlDeLightManager(createInMemoryDatabase()))

    // Insertamos un tenista
    tenistasRepositoryLocal.save(misTenistas.first()).first()
        .onSuccess { tenista ->
            println("Tenista insertado: $tenista")
        }.onFailure { error ->
            println(error.message)
        }

    // Obtenemos todos los tenistas
    tenistasRepositoryLocal.getAll().first()
        .onSuccess { tenistas ->
            println("Tenistas obtenidos: $tenistas")
        }.onFailure { error ->
            println(error.message)
        }

    // Seleccionamos un tenista
    tenistasRepositoryLocal.getById(misTenistas.first().id).first()
        .onSuccess { tenista ->
            println("Tenista seleccionado: $tenista")
        }.onFailure { error ->
            println(error.message)
        }

    // Seleccionamos un tenista que no existe
    tenistasRepositoryLocal.getById(UUID.randomUUID()).first()
        .onSuccess { tenista ->
            println("Tenista seleccionado: $tenista")
        }.onFailure { error ->
            println(error.message)
        }

    val tenistaForUpdate = misTenistas.first().copy(nombre = "TEST", puntos = 100)
    // Actualizamos un tenista
    tenistasRepositoryLocal.update(misTenistas.first().id, tenistaForUpdate).first()
        .onSuccess { tenista ->
            println("Tenista actualizado: $tenista")
        }.onFailure { error ->
            println(error.message)
        }

    // Obtenemos todos los tenistas
    tenistasRepositoryLocal.getAll().first()
        .onSuccess { tenistas ->
            println("Tenistas obtenidos: $tenistas")
        }.onFailure { error ->
            println(error.message)
        }

    // Actualizamos un tenista que no existe
    tenistasRepositoryLocal.update(UUID.randomUUID(), tenistaForUpdate).first()
        .onSuccess { tenista ->
            println("Tenista actualizado: $tenista")
        }.onFailure { error ->
            println(error.message)
        }

    // Borramos un tenista
    tenistasRepositoryLocal.delete(misTenistas.first().id).first()
        .onSuccess { tenista ->
            println("Tenista borrado: $tenista")
        }.onFailure { error ->
            println(error.message)
        }

    // Obtenemos todos los tenistas
    tenistasRepositoryLocal.getAll().first()
        .onSuccess { tenistas ->
            println("Tenistas obtenidos: $tenistas")
        }.onFailure { error ->
            println(error.message)
        }

    // Borramos un tenista que no existe
    tenistasRepositoryLocal.delete(UUID.randomUUID()).first()
        .onSuccess { tenista ->
            println("Tenista borrado: $tenista")
        }.onFailure { error ->
            println(error.message)
        }

}