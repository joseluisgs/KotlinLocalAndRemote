package repository

import app.cash.sqldelight.TransactionWithoutReturn
import database.DatabaseQueries
import database.SqlDeLightManager
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenistaEntity
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.repository.TenistasRepositoryLocal
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class TenistasRepositoryLocalTest {

    @MockK
    private lateinit var databaseQueries: DatabaseQueries

    @InjectMockKs
    private lateinit var sqlClient: SqlDeLightManager

    private val repository: TenistasRepositoryLocal by lazy { TenistasRepositoryLocal(sqlClient) }

    @BeforeEach
    fun setUp() {
        coEvery { databaseQueries.removeAll() } returns Unit
    }

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
    @DisplayName("getAll debe devolver todos los tenistas")
    fun `getAll debe devolver todos los tenistas`() = runTest {
        coEvery {
            databaseQueries.selectAll().executeAsList()
        } returns listOf(testTenista.toTenistaEntity())

        val result = repository.getAll().first()
        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(listOf(testTenista), result.value, "Los tenistas deben ser iguales") }
        )

        coVerify(atLeast = 1) { databaseQueries.selectAll() }
    }

    @Test
    @DisplayName("getById debe devolver el tenista correspondiente")
    fun `getById debe devolver el tenista correspondiente`() = runTest {
        val id = testTenista.id
        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        val result = repository.getById(id).first()
        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(testTenista, result.value, "El tenista debe ser igual") }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
    }

    @Test
    @DisplayName("getById debe devolver error si el tenista no existe")
    fun `getById debe devolver error si el tenista no existe`() = runTest {
        val id = 1L
        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns null

        val result = repository.getById(id).first()
        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Err") },
            {
                assertEquals(
                    TenistaError.NotFound(id).message,
                    result.error.message,
                    "El mensaje de error debe ser igual"
                )
            }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
    }

    @Test
    @DisplayName("save debe guardar el tenista y devolverlo")
    fun `save debe guardar el tenista y devolverlo`() = runTest {
        coEvery {
            databaseQueries.transaction(
                noEnclosing = false,
                any<TransactionWithoutReturn.() -> Unit>()
            )
        } coAnswers {
            it.invocation.args[0]
        }

        coEvery {
            databaseQueries.insert(any())
        } returns Unit

        coEvery {
            databaseQueries.selectLastInserted().executeAsOne()
        } returns testTenista.toTenistaEntity()

        val result = repository.save(testTenista).first()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(testTenista, result.value, "El tenista guardado debe ser el mismo") }
        )

        coVerify(atLeast = 1) {
            databaseQueries.transaction(
                noEnclosing = false,
                any<TransactionWithoutReturn.() -> Unit>()
            )
        }

        coVerify(atLeast = 1) { databaseQueries.selectLastInserted() }
    }

    @Test
    @DisplayName("update debe actualizar el tenista y devolverlo")
    fun `update debe actualizar el tenista y devolverlo`() = runTest {
        val id = testTenista.id
        val updatedTenista = testTenista.copy(nombre = "Updated Test")

        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        coEvery {
            databaseQueries.update(
                id = id,
                nombre = updatedTenista.nombre,
                pais = updatedTenista.pais,
                altura = updatedTenista.altura.toLong(),
                peso = updatedTenista.peso.toLong(),
                puntos = updatedTenista.puntos.toLong(),
                mano = updatedTenista.mano.name,
                fecha_nacimiento = updatedTenista.fechaNacimiento.toString(),
                updated_at = any()
            )
        } returns Unit

        val result = repository.update(id, updatedTenista).first()
        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            {
                assertEquals(
                    updatedTenista.copy(createdAt = testTenista.createdAt, updatedAt = result.value.updatedAt),
                    result.value, "El tenista actualizado debe ser el mismo"
                )
            }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 1) { databaseQueries.update(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("update debe devolver error si el tenista no existe")
    fun `update debe devolver error si el tenista no existe`() = runTest {
        val id = 1L
        val updatedTenista = testTenista.copy(nombre = "Updated Test Fails")

        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns null

        val result = repository.update(id, updatedTenista).first()
        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Err") },
            {
                assertEquals(
                    TenistaError.NotFound(id).message,
                    result.error.message,
                    "El mensaje de error debe ser igual"
                )
            }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 0) { databaseQueries.update(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("delete debe borrar lógicamente el tenista y devolverlo")
    fun `delete debe borrar lógicamente el tenista y devolverlo`() = runTest {
        val id = testTenista.id

        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        coEvery {
            databaseQueries.delete(id)
        } returns Unit

        val result = repository.delete(id).first()
        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser Ok") }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 1) { databaseQueries.delete(id) }
    }

    @Test
    @DisplayName("delete debe devolver error si el tenista no existe")
    fun `delete debe devolver error si el tenista no existe`() = runTest {
        val id = 1L

        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns null

        val result = repository.delete(id).first()
        assertAll(
            { assertTrue(result.isErr, "El resultado debe ser Err") },
            {
                assertEquals(
                    TenistaError.NotFound(id).message,
                    result.error.message,
                    "El mensaje de error debe ser igual"
                )
            }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 0) { databaseQueries.delete(id) }
    }

    @Test
    @DisplayName("removeAll debe ejecutarse correctamente")
    fun `test removeAll debe ejecutarse `() = runTest {
        coEvery { databaseQueries.removeAll() } just Runs

        val result = repository.removeAll().first()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser Ok") }
        )

        coVerify(atLeast = 1) { databaseQueries.removeAll() }
    }

    @Test
    @DisplayName("saveAll debe almacenar una lista de tenistas")
    fun `test saveAll almacena una lista de tenistas`() = runTest {
        val tenistas = listOf(
            testTenista,
            testTenista.copy(id = 2, nombre = "Roger Federer"),
            testTenista.copy(id = 3, nombre = "Novak Djokovic")
        )

        coEvery {
            databaseQueries.transaction(
                noEnclosing = false,
                any<TransactionWithoutReturn.() -> Unit>()
            )
        } coAnswers {
            it.invocation.args[0]
        }

        coEvery {
            databaseQueries.selectLastInserted().executeAsOne()
        } returns testTenista.toTenistaEntity()

        val result = repository.saveAll(tenistas).first()

        assertAll(
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(tenistas.size, result.value, "El número de tenistas guardados debe ser el mismo") }
        )

        coVerify(atLeast = 1) {
            databaseQueries.transaction(
                noEnclosing = false,
                any<TransactionWithoutReturn.() -> Unit>()
            )
        }
    }
}
