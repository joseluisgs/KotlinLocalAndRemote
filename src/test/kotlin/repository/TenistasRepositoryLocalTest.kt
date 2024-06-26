package repository

import app.cash.sqldelight.TransactionWithoutReturn
import database.DatabaseQueries
import database.SqlDeLightManager
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenistaEntity
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.repository.TenistasRepositoryLocal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenistasRepositoryLocalTest {

    @MockK
    private lateinit var databaseQueries: DatabaseQueries

    @InjectMockKs
    private lateinit var sqlClient: SqlDeLightManager

    // Al ser una propiedad lazy, se inicializa cuando se llama, por lo que ya estran listos los mocks
    private val repository: TenistasRepositoryLocal by lazy { TenistasRepositoryLocal(sqlClient) }

    @BeforeAll
    fun setUpAll() {
        // Es qe hay un init que se hace nada mas crear el objeto
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
    fun `getAll debe devolver todos los tenistas`() = runBlocking {
        coEvery {
            databaseQueries.selectAllOrderByPuntosDesc().executeAsList()
        } returns listOf(testTenista.toTenistaEntity())

        val result = repository.getAll().first()
        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(listOf(testTenista), result.value) }
        )

        coVerify(atLeast = 1) { databaseQueries.selectAllOrderByPuntosDesc() }
    }

    @Test
    fun `getById debe devolver el tenista correspondiente`() = runBlocking {
        val id = testTenista.id
        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        val result = repository.getById(id).first()
        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.value) }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
    }

    @Test
    fun `getById debe devolver error si el tenista no existe`() = runBlocking {
        val id = 1L
        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns null

        val result = repository.getById(id).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.NotFound(id).message, result.error.message) }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
    }

    @Test
    fun `save debe guardar el tenista y devolverlo`() = runBlocking {

        // Cuidado que estamos usando una transacción
        coEvery {
            databaseQueries.transaction(
                noEnclosing = false,
                any<TransactionWithoutReturn.() -> Unit>()
            )
        } coAnswers {
            it.invocation.args[0]
        }

        coEvery {
            databaseQueries.insert(testTenista.toTenistaEntity())
        } returns Unit

        coEvery {
            databaseQueries.selectLastInserted().executeAsOne()
        } returns testTenista.toTenistaEntity()

        val result = repository.save(testTenista).first()

        assertAll(
            { assertTrue(result.isOk) },
            {
                assertEquals(
                    testTenista.copy(createdAt = result.value.createdAt, updatedAt = result.value.updatedAt),
                    result.value
                )
            }
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
    fun `update debe actualizar el tenista y devolverlo`() = runBlocking {
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
            { assertTrue(result.isOk) },
            {
                assertEquals(
                    updatedTenista.copy(createdAt = testTenista.createdAt, updatedAt = result.value.updatedAt),
                    result.value
                )
            }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 1) { databaseQueries.update(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `update debe devolver error si el tenista no existe`() = runBlocking {
        val id = 1L
        val updatedTenista = testTenista.copy(nombre = "Updated Test Fails")

        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns null

        val result = repository.update(id, updatedTenista).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.NotFound(id).message, result.error.message) }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 0) { databaseQueries.update(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `delete debe borrar lógicamente el tenista y devolverlo`() = runBlocking {
        val id = testTenista.id

        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        coEvery {
            databaseQueries.delete(id)
        } returns Unit

        val result = repository.delete(id).first()
        assertAll(
            { assertTrue(result.isOk) },
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 1) { databaseQueries.delete(id) }
    }

    @Test
    fun `delete debe devolver error si el tenista no existe`() = runBlocking {
        val id = 1L

        coEvery {
            databaseQueries.selectById(id).executeAsOneOrNull()
        } returns null

        val result = repository.delete(id).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.NotFound(id).message, result.error.message) }
        )

        coVerify(atLeast = 1) { databaseQueries.selectById(id) }
        coVerify(atLeast = 0) { databaseQueries.delete(id) }
    }

}