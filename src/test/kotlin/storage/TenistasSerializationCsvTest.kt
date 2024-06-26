package storage

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.storage.TenistasSerializationCsv
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

class TenistasSerializationCsvTest {
    // importamos la clase a testear
    private val tenistasSerializationCsv = TenistasSerializationCsv()

    @TempDir // Inyectamos un directorio temporal
    lateinit var tempDir: Path

    @Test
    fun `import debe devolver error si el fichero no existe`(): Unit = runTest {
        val nonExistentFile = File(tempDir.toFile(), "fichero_no_existe.csv")

        val result = tenistasSerializationCsv.import(nonExistentFile).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.StorageError) },
            { assertTrue(result.error.message.contains("El fichero no existe")) }
        )
    }

    @Test
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

        val result = tenistasSerializationCsv.import(validFile).first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(2, result.value.size) },
            { assertEquals("Novak Djokovic", result.value[0].nombre) },
            { assertEquals("Daniil Medvedev", result.value[1].nombre) }
        )
    }

    @Test
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

        val result = tenistasSerializationCsv.export(file, tenistas).first()

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

        val result = tenistasSerializationCsv.export(invalidFile, tenistas).first()

        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.StorageError) },
            { assertTrue(result.error.message.contains("Error al acceder al fichero")) }
        )
    }

}