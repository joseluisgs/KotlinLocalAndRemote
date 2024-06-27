package repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.repository.TenistasRepositoryRemote
import dev.joseluisgs.rest.TenistasApiRest
import io.ktor.client.statement.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue


// Extension function to simulate relaxed response
fun mockHttpResponse(): HttpResponse = mockk(relaxed = true)


@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    fun `getAll debe devolver una lista de tenistas`() = runTest {
        val tenistaDtoList = listOf(testTenista.toTenistaDto())
        val tenistaList = tenistaDtoList.map { it.toTenista() }

        coEvery { restClient.getAll() } returns Ok(tenistaDtoList)

        val result = repository.getAll().first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(tenistaList, result.value) },
            { assertEquals(tenistaList.size, result.value.size) }
        )

        coVerify(atLeast = 1) { restClient.getAll() }
    }

    @Test
    fun `getAll debe devolver error si falla`() = runTest {
        coEvery { restClient.getAll() } returns Err(TenistaError.ApiError(400, "Error message"))

        val result = repository.getAll().first()

        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.RemoteError) },
            { assertTrue(result.error.message.contains("400 Error message")) }
        )
    }

    @Test
    fun `getById debe devolver un tenista`() = runTest {

        coEvery { restClient.getById(1L) } returns Ok(testTenista.toTenistaDto())

        val result = repository.getById(1L).first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.value) }
        )
        coVerify(atLeast = 1) { restClient.getById(1L) }
    }

    @Test
    fun `getById debe devolver error si falla`() = runTest {
        coEvery { restClient.getById(1L) } returns Err(TenistaError.ApiError(404, "Not Found"))

        val result = repository.getById(1L).first()

        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.RemoteError) },
            { assertTrue(result.error.message.contains("404 Not Found")) }
        )
    }

    @Test
    fun `save debe salvar un tenista`() = runTest {
        coEvery { restClient.save(testTenista.toTenistaDto()) } returns Ok(testTenista.toTenistaDto())

        val result = repository.save(testTenista).first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.value) }
        )

        coVerify(atLeast = 1) { restClient.save(testTenista.toTenistaDto()) }
    }

    @Test
    fun `save debe devolver error si falla`() = runTest {

        coEvery { restClient.save(testTenista.toTenistaDto()) } returns Err(TenistaError.ApiError(400, "Error message"))

        val result = repository.save(testTenista).first()

        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.RemoteError) },
            { assertTrue(result.error.message.contains("400 Error message")) }
        )

        coVerify(atLeast = 1) { restClient.save(testTenista.toTenistaDto()) }
    }

    @Test
    fun `update debe actualizar un tenista`() = runTest {
        coEvery { restClient.update(1L, testTenista.toTenistaDto()) } returns Ok(testTenista.toTenistaDto())

        val result = repository.update(1L, testTenista).first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.value) }
        )

        coVerify(atLeast = 1) { restClient.update(1L, testTenista.toTenistaDto()) }
    }

    @Test
    fun `update debe devolver error si falla`() = runTest {
        coEvery {
            restClient.update(
                1L,
                testTenista.toTenistaDto()
            )
        } returns Err(TenistaError.ApiError(400, "Error message"))

        val result = repository.update(1L, testTenista).first()

        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.RemoteError) },
            { assertTrue(result.error.message.contains("400 Error message")) }
        )

        coVerify(atLeast = 1) { restClient.update(1L, testTenista.toTenistaDto()) }
    }

    @Test
    fun `delete debe devolver caso correcto`() = runTest {
        coEvery { restClient.delete(1L) } returns Ok(Unit)

        val result = repository.delete(1L).first()

        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(Unit, result.value) }
        )

        coVerify(atLeast = 1) { restClient.delete(1L) }
    }

    @Test
    fun `delete debe devolver error si falla`() = runTest {
        coEvery { restClient.delete(1L) } returns Err(TenistaError.ApiError(400, "Error message"))

        val result = repository.delete(1L).first()

        assertAll(
            { assertTrue(result.isErr) },
            { assertTrue(result.error is TenistaError.RemoteError) },
            { assertTrue(result.error.message.contains("400 Error message")) }
        )

        coVerify(atLeast = 1) { restClient.delete(1L) }
    }


}

