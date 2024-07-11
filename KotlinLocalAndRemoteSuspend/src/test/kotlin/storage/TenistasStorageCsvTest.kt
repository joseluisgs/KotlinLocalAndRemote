package storage

import dev.joseluisgs.model.Tenista
import dev.joseluisgs.storage.TenistasStorageCsv
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.Test

class TenistasStorageCsvTest {
    private val tenistasStorageCsv = TenistasStorageCsv()

    @TempDir
    lateinit var tempDir: Path

    @Test
    @DisplayName("Importar debe devolver error si el fichero no existe")
    fun `import debe devolver error si el fichero no existe`(): Unit = runTest {
        val nonExistentFile = File(tempDir.toFile(), "fichero_no_existe.csv")

        val result = tenistasStorageCsv.import(nonExistentFile)
        assertAll(
            {
                assertTrue(result.isErr, "El resultado debe ser un error")
            },
            {
                assertTrue(
                    result.error.message.contains("El fichero no existe"),
                    "El mensaje debe indicar que el fichero no existe"
                )
            }
        )
    }

    @Test
    @DisplayName("Importar debe devolver una lista si el fichero existe")
    fun `import debe devolver una lista si el fichero existe`() = runTest {
        val validFile = File(tempDir.toFile(), "tenistas.csv").apply {
            writeText(
                """
                id,nombre,pais,altura,peso,puntos,mano,fecha_nacimiento
                1,Novak Djokovic,Serbia,188,77,12030,DIESTRO,1987-05-22
                2,Daniil Medvedev,Rusia,198,83,10370,DIESTRO,1996-02-11
                """.trimIndent()
            )
        }

        val result = tenistasStorageCsv.import(validFile)

        assertAll(
            {
                assertTrue(result.isOk, "El resultado debe ser una lista correcta")
            },
            {
                assertEquals(2, result.value.size, "El tamaño de la lista debe ser 2")
            },
            {
                assertEquals("Novak Djokovic", result.value[0].nombre, "El primer tenista debe ser Novak Djokovic")
            },
            {
                assertEquals("Daniil Medvedev", result.value[1].nombre, "El segundo tenista debe ser Daniil Medvedev")
            }
        )
    }

    @Test
    @DisplayName("Exportar debe escribir tenistas en un fichero")
    fun `export debe escribir tenistas en un fichero`() = runTest {
        val file = File(tempDir.toFile(), "tenistas_export.csv")
        val tenistas = listOf(
            Tenista(
                id = 1L,
                nombre = "Rafael Nadal",
                pais = "Spain",
                altura = 185,
                peso = 85,
                puntos = 10000,
                mano = Tenista.Mano.ZURDO,
                fechaNacimiento = LocalDate.parse("1981-08-08"),
            ),
            Tenista(
                id = 2L,
                nombre = "Roger Federer",
                pais = "Switzerland",
                altura = 185,
                peso = 85,
                puntos = 9000,
                mano = Tenista.Mano.DIESTRO,
                fechaNacimiento = LocalDate.parse("1981-08-07"),
            )
        )

        val result = tenistasStorageCsv.export(file, tenistas)

        assertAll(
            {
                assertTrue(result.isOk, "El resultado debe ser exitoso")
            },
            {
                assertEquals(2, result.value, "El número de tenistas exportados debe ser 2")
            },
            {
                assertTrue(file.exists(), "El fichero debe existir")
            },
        )

        val content = file.readText()
        assertAll(
            {
                assertTrue(content.contains("Rafael Nadal"), "El contenido debe contener a Rafael Nadal")
            },
            {
                assertTrue(content.contains("Roger Federer"), "El contenido debe contener a Roger Federer")
            }
        )
    }

    @Test
    @DisplayName("Exportar debe devolver error si no existe")
    fun `export debe devolver error si no existe fichero`() = runTest {
        val invalidFile = File("/invalid/path/tenistas_export.csv")
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

        val result = tenistasStorageCsv.export(invalidFile, tenistas)

        assertAll(
            {
                assertTrue(result.isErr, "El resultado debe ser un error")
            },
            {
                assertTrue(
                    result.error.message.contains("Error al acceder al fichero"),
                    "El mensaje debe indicar error al acceder al fichero"
                )
            }
        )
    }
}
