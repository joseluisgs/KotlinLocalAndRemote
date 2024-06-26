package repository

import database.SqlDeLightManager
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
    lateinit var sqlClient: SqlDeLightManager

    @InjectMockKs
    lateinit var repository: TenistasRepositoryLocal

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
            sqlClient.databaseQueries.selectAllOrderByPuntosDesc().executeAsList()
        } returns listOf(testTenista.toTenistaEntity())

        val result = repository.getAll().first()
        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(listOf(testTenista), (result.value)) }
        )
    }

}