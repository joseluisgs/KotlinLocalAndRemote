package repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.repository.TenistasRepositoryRemote
import dev.joseluisgs.rest.TenistasApiRest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class TenistasRepositoryRemoteTest {
    @MockK
    private lateinit var restClient: TenistasApiRest

    @InjectMockKs
    private lateinit var repository: TenistasRepositoryRemote

    private val testTenista = Tenista(
        id = 1,
        nombre = "Rafael Nadal",
        pais = "España",
        altura = 185,
        peso = 85,
        puntos = 10000,
        mano = Tenista.Mano.ZURDO,
        fechaNacimiento = LocalDate.of(1986, 6, 3),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        isDeleted = false
    )

    @Test
    fun `obtener todos los tenistas debe devolver una lista de tenistas`() = runTest {
        val tenistaDtoList = listOf(testTenista.toTenistaDto())
        val tenistaList = tenistaDtoList.map { it.toTenista() }

        coEvery { restClient.getAll() } returns Ok(tenistaDtoList)

        val result = repository.getAll().await()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser OK") },
            { assertEquals(tenistaList, result.value, "Las listas deben coincidir") },
            { assertEquals(tenistaList.size, result.value.size, "El tamaño de las listas debe coincidir") }
        )

        coVerify(atLeast = 1) { restClient.getAll() }
    }

    @Test
    fun `obtener todos los tenistas debe devolver error si falla`() = runTest {
        coEvery { restClient.getAll() } returns Err(TenistaError.ApiError(400, "Mensaje de error"))

        val result = repository.getAll().await()

        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Error") },
            {
                assertTrue(
                    result.error.message.contains("400 Mensaje de error"),
                    "El mensaje debe contener '400' y 'Mensaje de error'"
                )
            }
        )
    }

    @Test
    fun `obtener tenista por ID debe devolver un tenista`() = runTest {
        coEvery { restClient.getById(1L) } returns Ok(testTenista.toTenistaDto())

        val result = repository.getById(1L).await()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser OK") },
            { assertEquals(testTenista, result.value, "El tenista devuelto debe con el tenista de prueba") }
        )
        coVerify(atLeast = 1) { restClient.getById(1L) }
    }

    @Test
    fun `obtener tenista por ID debe devolver error si falla`() = runTest {
        coEvery { restClient.getById(1L) } returns Err(TenistaError.ApiError(404, "No encontrado"))

        val result = repository.getById(1L).await()

        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Error") },
            {
                assertTrue(
                    result.error.message.contains("404 No encontrado"),
                    "El mensaje debe contener '404' y 'No encontrado'"
                )
            }
        )
    }

    @Test
    fun `guardar un tenista debe salvar un tenista`() = runTest {
        coEvery { restClient.save(any()) } returns Ok(testTenista.toTenistaDto())

        val result = repository.save(testTenista).await()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser OK") },
            { assertEquals(testTenista, result.value, "El tenista guardado debe coincidir con el tenista de prueba") }
        )

        coVerify(atLeast = 1) { restClient.save(any()) }
    }

    @Test
    fun `guardar un tenista debe devolver error si falla`() = runTest {
        coEvery { restClient.save(any()) } returns Err(TenistaError.ApiError(400, "Mensaje de error"))

        val result = repository.save(testTenista).await()

        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Error") },
            {
                assertTrue(
                    result.error.message.contains("400 Mensaje de error"),
                    "El mensaje debe contener '400' y 'Mensaje de error'"
                )
            }
        )

        coVerify(atLeast = 1) { restClient.save(any()) }
    }

    @Test
    fun `actualizar tenista debe actualizar un tenista`() = runTest {
        coEvery { restClient.update(1L, any()) } returns Ok(testTenista.toTenistaDto())

        val result = repository.update(1L, testTenista).await()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser OK") },
            {
                assertEquals(
                    testTenista,
                    result.value,
                    "El tenista actualizado debe coincidir con el tenista de prueba"
                )
            }
        )

        coVerify(atLeast = 1) { restClient.update(1L, any()) }
    }

    @Test
    fun `actualizar tenista debe devolver error si falla`() = runTest {
        coEvery { restClient.update(1L, any()) } returns Err(TenistaError.ApiError(400, "Mensaje de error"))

        val result = repository.update(1L, testTenista).await()

        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Error") },
            {
                assertTrue(
                    result.error.message.contains("400 Mensaje de error"),
                    "El mensaje debe contener '400' y 'Mensaje de error'"
                )
            }
        )

        coVerify(atLeast = 1) { restClient.update(1L, any()) }
    }

    @Test
    fun `eliminar tenista debe devolver caso correcto`() = runTest {
        coEvery { restClient.delete(1L) } returns Ok(Unit)

        val result = repository.delete(1L).await()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser OK") },
            { assertEquals(result.value, 1L, "El ID del tenista eliminado debe coincidir con 1L") }
        )

        coVerify(atLeast = 1) { restClient.delete(1L) }
    }

    @Test
    fun `eliminar tenista debe devolver error si falla`() = runTest {
        coEvery { restClient.delete(1L) } returns Err(TenistaError.ApiError(400, "Mensaje de error"))

        val result = repository.delete(1L).await()

        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Error") },
            {
                assertTrue(
                    result.error.message.contains("400 Mensaje de error"),
                    "El mensaje debe contener '400' y 'Mensaje de error'"
                )
            }
        )

        coVerify(atLeast = 1) { restClient.delete(1L) }
    }
}
