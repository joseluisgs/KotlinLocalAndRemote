package repository

import database.DatabaseQueries
import database.SqlDeLightManager
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.mapper.toTenistaEntity
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.repository.TenistasRepositoryLocal
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class TenistasRepositoryLocalTest {

    @MockK
    private lateinit var databaseQueries: DatabaseQueries

    @InjectMockKs
    private lateinit var sqlClient: SqlDeLightManager

    // Al ser una propiedad lazy, se inicializa cuando se llama, por lo que ya estran listos los mocks
    private val repository: TenistasRepositoryLocal by lazy { TenistasRepositoryLocal(sqlClient) }


    private val testTenista = Tenista(
        id = UUID.randomUUID(),
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
    }

    @Test
    fun `getById debe devolver el tenista correspondiente`() = runBlocking {
        val id = testTenista.id
        coEvery {
            databaseQueries.selectById(id.toString()).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        val result = repository.getById(id).first()
        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.value) }
        )
    }

    @Test
    fun `getById debe devolver error si el tenista no existe`() = runBlocking {
        val id = UUID.randomUUID()
        coEvery {
            databaseQueries.selectById(id.toString()).executeAsOneOrNull()
        } returns null

        val result = repository.getById(id).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.NotFound(id.toString()).message, result.error.message) }
        )
    }

    @Test
    fun `save debe guardar el tenista y devolverlo`() = runBlocking {
        coEvery {
            databaseQueries.insert(testTenista.toTenistaEntity())
        } returns Unit

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
    }

    @Test
    fun `update debe actualizar el tenista y devolverlo`() = runBlocking {
        val id = testTenista.id
        val updatedTenista = testTenista.copy(nombre = "Updated Test")

        coEvery {
            databaseQueries.selectById(id.toString()).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        coEvery {
            databaseQueries.update(
                id = id.toString(),
                nombre = updatedTenista.nombre,
                pais = updatedTenista.pais,
                altura = updatedTenista.altura.toLong(),
                peso = updatedTenista.peso.toLong(),
                puntos = updatedTenista.puntos.toLong(),
                mano = updatedTenista.mano.name,
                fechaNacimiento = updatedTenista.fechaNacimiento.toString(),
                updatedAt = any()
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
    }

    @Test
    fun `update debe devolver error si el tenista no existe`() = runBlocking {
        val id = UUID.randomUUID()
        val updatedTenista = testTenista.copy(nombre = "Updated Test Fails")

        coEvery {
            databaseQueries.selectById(id.toString()).executeAsOneOrNull()
        } returns null

        val result = repository.update(id, updatedTenista).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.NotFound(id.toString()).message, result.error.message) }
        )
    }

    @Test
    fun `delete debe borrar lógicamente el tenista y devolverlo`() = runBlocking {
        val id = testTenista.id

        coEvery {
            databaseQueries.selectById(id.toString()).executeAsOneOrNull()
        } returns testTenista.toTenistaEntity()

        coEvery {
            databaseQueries.delete(id.toString())
        } returns Unit

        val result = repository.delete(id).first()
        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(testTenista.copy(updatedAt = result.value.updatedAt, isDeleted = true), result.value) }
        )
    }

    @Test
    fun `delete debe devolver error si el tenista no existe`() = runBlocking {
        val id = UUID.randomUUID()

        coEvery {
            databaseQueries.selectById(id.toString()).executeAsOneOrNull()
        } returns null

        val result = repository.delete(id).first()
        assertAll(
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.NotFound(id.toString()).message, result.error.message) }
        )
    }

}