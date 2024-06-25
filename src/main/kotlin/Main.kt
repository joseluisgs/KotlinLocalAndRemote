package dev.joseluisgs

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.storage.TenistasSerializationCsv
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path

fun main() = runBlocking {
    println("Hola Tenistas!")
    val tenistasCsv = TenistasSerializationCsv()

    // Prueba de importación de tenistas desde CSV
    var misTenistas = listOf<Tenista>()
    tenistasCsv.import(Path("data", "tenistas.csv").toFile()).collect { it ->
        it.onSuccess { tenistas ->
            println("Importados ${tenistas.size} tenistas")
            misTenistas = tenistas.toMutableList()
            println(tenistas)
        }
        it.onFailure { error ->
            println(error)
        }
    }

    // Prueba de exportación de tenistas a CSV
    tenistasCsv.export(Path("data", "tenistas_export.csv").toFile(), misTenistas).collect { it ->
        it.onSuccess { size ->
            println("Exportados $size tenistas")
        }
        it.onFailure { error ->
            println(error)
        }
    }
}