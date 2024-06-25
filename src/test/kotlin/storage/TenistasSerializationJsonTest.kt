package storage

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.storage.TenistasSerializationJson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.util.*
import kotlin.test.Test

class TenistasSerializationJsonTest {
    // importamos la clase a testear
    private val tenistasSerializationJson = TenistasSerializationJson()

    @TempDir // Inyectamos un directorio temporal
    lateinit var tempDir: Path

    @Test
    fun `import debe devolver error si el fichero no existe`(): Unit = runTest {
        val nonExistentFile = File(tempDir.toFile(), "fichero_no_existe.csv")

        val result = tenistasSerializationJson.import(nonExistentFile).firstOrNull()
        assertAll(
            { assertTrue(result != null) },
            { assertTrue(result!!.isErr) },
            { assertTrue(result!!.error is TenistaError.StorageError) },
            { assertTrue(result!!.error.message.contains("El fichero no existe")) }
        )
    }

    @Test
    fun `import debe devolver una lista si el fichero existe`() = runTest {
        val validFile = File(tempDir.toFile(), "tenistas.json").apply {
            writeText(
                """
                [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "nombre": "Novak Djokovic",
                        "pais": "Serbia",
                        "altura": 188,
                        "peso": 77,
                        "puntos": 12030,
                        "mano": "DIESTRO",
                        "fechaNacimiento": "1987-05-22",
                        "createdAt": "2024-06-25T17:49:35.094296800",
                        "updatedAt": "2024-06-25T17:49:35.094296800"
                    },
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440001",
                        "nombre": "Daniil Medvedev",
                        "pais": "Rusia",
                        "altura": 198,
                        "peso": 83,
                        "puntos": 10370,
                        "mano": "DIESTRO",
                        "fechaNacimiento": "1996-02-11",
                        "createdAt": "2024-06-25T17:49:35.094296800",
                        "updatedAt": "2024-06-25T17:49:35.094296800"
                    }
               ]
               """.trimIndent()
            )
        }

        val result = tenistasSerializationJson.import(validFile).firstOrNull()

        assertAll(
            { assertTrue(result != null) },
            { assertTrue(result!!.isOk) },
            { assertEquals(2, result!!.value.size) },
            { assertEquals("Novak Djokovic", result!!.value[0].nombre) },
            { assertEquals("Daniil Medvedev", result!!.value[1].nombre) }
        )
    }

    @Test
    fun `export debe escribir tenistas en un fichero`() = runTest {
        val file = File(tempDir.toFile(), "tenistas_export.json")
        val tenistas = listOf(
            Tenista(
                id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                nombre = "Rafael Nadal",
                pais = "Spain",
                altura = 185,
                peso = 85,
                puntos = 10000,
                mano = Tenista.Mano.ZURDO,
                fechaNacimiento = LocalDate.parse("1981-08-08"),
            ),
            Tenista(
                id = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
                nombre = "Roger Federer",
                pais = "Switzerland",
                altura = 185,
                peso = 85,
                puntos = 9000,
                mano = Tenista.Mano.DIESTRO,
                fechaNacimiento = LocalDate.parse("1981-08-07"),
            )
        )

        val result = tenistasSerializationJson.export(file, tenistas).firstOrNull()

        assertAll(
            { assertTrue(result != null) },
            { assertTrue(result!!.isOk) },
            { assertEquals(2, result!!.value) },
            { assertTrue(file.exists()) },
        )

        val content = file.readText()
        assertAll(
            { assertTrue(content.contains("Rafael Nadal")) },
            { assertTrue(content.contains("Roger Federer")) }
        )
    }

    @Test
    fun `export debe devolver error si no existe fichero`() = runTest {
        val invalidFile = File("/invalid/path/tenistas_export.json")
        val tenistas = listOf(
            Tenista(
                id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                nombre = "Rafael Nadal",
                pais = "Spain",
                altura = 185,
                peso = 85,
                puntos = 10000,
                mano = Tenista.Mano.ZURDO,
                fechaNacimiento = LocalDate.parse("1981-08-08"),
            )
        )

        val result = tenistasSerializationJson.export(invalidFile, tenistas).firstOrNull()

        assertAll(
            { assertTrue(result != null) },
            { assertTrue(result!!.isErr) },
            { assertTrue(result!!.error is TenistaError.StorageError) },
            { assertTrue(result!!.error.message.contains("Error al acceder al fichero")) }
        )
    }

}