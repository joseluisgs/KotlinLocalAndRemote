package storage

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.storage.TenistasStorageJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.Test

class TenistasSerializationJsonTest {
    // importamos la clase a testear
    private val tenistasSerializationJson = TenistasStorageJson()

    @TempDir // Inyectamos un directorio temporal
    lateinit var tempDir: Path

    @Test
    fun `import debe devolver error si el fichero no existe`(): Unit = runTest {
        val nonExistentFile = File(tempDir.toFile(), "fichero_no_existe.csv")

        val result = tenistasSerializationJson.import(nonExistentFile).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.StorageError) },
            { assertTrue(result.error.message.contains("El fichero no existe")) }
        )
    }

    @Test
    fun `import debe devolver una lista si el fichero existe`() = runTest {
        val validFile = File(tempDir.toFile(), "tenistas.json").apply {
            writeText(
                """
                [
                    {
                        "id": "1",
                        "nombre": "Novak Djokovic",
                        "pais": "Serbia",
                        "altura": 188,
                        "peso": 77,
                        "puntos": 12030,
                        "mano": "DIESTRO",
                        "fecha_nacimiento": "1987-05-22",
                        "created_at": "2024-06-25T17:49:35.094296800",
                        "updated_at": "2024-06-25T17:49:35.094296800"
                    },
                    {
                        "id": "2",
                        "nombre": "Daniil Medvedev",
                        "pais": "Rusia",
                        "altura": 198,
                        "peso": 83,
                        "puntos": 10370,
                        "mano": "DIESTRO",
                        "fecha_nacimiento": "1996-02-11",
                        "created_at": "2024-06-25T17:49:35.094296800",
                        "updated_at": "2024-06-25T17:49:35.094296800"
                    }
               ]
               """.trimIndent()
            )
        }

        val result = tenistasSerializationJson.import(validFile).first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(2, result.value.size) },
            { assertEquals("Novak Djokovic", result.value[0].nombre) },
            { assertEquals("Daniil Medvedev", result.value[1].nombre) }
        )
    }

    @Test
    fun `export debe escribir tenistas en un fichero`() = runTest {
        val file = File(tempDir.toFile(), "tenistas_export.json")
        val tenistas = listOf(
            Tenista(
                id = 1,
                nombre = "Rafael Nadal",
                pais = "Spain",
                altura = 185,
                peso = 85,
                puntos = 10000,
                mano = Tenista.Mano.ZURDO,
                fechaNacimiento = LocalDate.parse("1981-08-08"),
            ),
            Tenista(
                id = 2,
                nombre = "Roger Federer",
                pais = "Switzerland",
                altura = 185,
                peso = 85,
                puntos = 9000,
                mano = Tenista.Mano.DIESTRO,
                fechaNacimiento = LocalDate.parse("1981-08-07"),
            )
        )

        val result = tenistasSerializationJson.export(file, tenistas).first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(2, result.value) },
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
                id = 1,
                nombre = "Rafael Nadal",
                pais = "Spain",
                altura = 185,
                peso = 85,
                puntos = 10000,
                mano = Tenista.Mano.ZURDO,
                fechaNacimiento = LocalDate.parse("1981-08-08"),
            )
        )

        val result = tenistasSerializationJson.export(invalidFile, tenistas).first()

        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.StorageError) },
            { assertTrue(result.error.message.contains("Error al acceder al fichero")) }
        )
    }

}