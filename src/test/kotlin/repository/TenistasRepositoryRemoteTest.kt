package repository

import io.ktor.client.statement.*
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith


// Extension function to simulate relaxed response
fun mockHttpResponse(): HttpResponse = mockk(relaxed = true)


@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenistasRepositoryRemoteTest {
    /*@MockK
    private lateinit var tenistasApiRest: TenistasApiRest

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
    fun `getAll success`() = runTest {

        coEvery { tenistasApiRest.getAll() } returns Response.success(listOf(testTenista.toTenistaDto()))
        // Execute
        val result = repository.getAll().first()

        // Verify
        assertAll(
            { assertTrue(result.isOk) },
            { assertEquals(listOf(testTenista), result.value) }
        )
    }*/

}

