package service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import dev.joseluisgs.cache.Cache
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import dev.joseluisgs.notifications.TenistasNotifications
import dev.joseluisgs.repository.TenistasRepositoryLocal
import dev.joseluisgs.repository.TenistasRepositoryRemote
import dev.joseluisgs.service.TenistasServiceImpl
import dev.joseluisgs.storage.TenistasStorageCsv
import dev.joseluisgs.storage.TenistasStorageJson
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test

/*
 val tenistasService = TenistasServiceImpl(
        localRepository = TenistasRepositoryLocal(SqlDeLightManager(createDatabase("tenistas.db"))),
        remoteRepository = TenistasRepositoryRemote(getKtorFitClient(API_TENISTAS_URL).create()),
        cache = TenistasCache(size = 5),
        csvStorage = TenistasStorageCsv(),
        jsonStorage = TenistasStorageJson(),
        notificationsService = TenistasNotifications()
    )
 */

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenistasServiceImplTest {
    @MockK
    lateinit var localRepository: TenistasRepositoryLocal

    @MockK
    lateinit var remoteRepository: TenistasRepositoryRemote

    @MockK
    lateinit var cache: Cache<Long, Tenista>

    @MockK
    lateinit var csvStorage: TenistasStorageCsv

    @MockK
    lateinit var jsonStorage: TenistasStorageJson

    @MockK
    lateinit var notificationsService: TenistasNotifications

    @InjectMockKs
    lateinit var service: TenistasServiceImpl

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

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `getAll debe retornar lista de tenistas localmente`() = runTest {
        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))

        val result = service.getAll(false).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk) },
            { assertEquals(listOf(testTenista), result.value) }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
    }

    @Test
    fun `getAll debe retornar lista de tenistas remota`() = runTest {
        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))

        val result = service.getAll(true).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk) },
            { assertEquals(listOf(testTenista), result.value) }
        )

        coVerify(atLeast = 1) { remoteRepository.getAll() }
    }

    @Test
    fun `getById debe retornar tenista en cache`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns testTenista

        val result = service.getById(id).first()

        assertAll("Debemos obtener tenista",
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.get()) }
        )

        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    fun `getById debe retornar tenista local si no esta en cache`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns null
        coEvery { localRepository.getById(id) } returns flowOf(Ok(testTenista))
        every { cache.put(id, testTenista) } returns Unit

        val result = service.getById(id).first()

        assertAll("Debemos obtener tenista",
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.get()) }
        )

        verify(atLeast = 1) { cache.get(id) }
        coVerify(atLeast = 1) { localRepository.getById(id) }
        verify(atLeast = 1) { cache.put(id, testTenista) }
    }

    @Test
    fun `getById debe retornar tenista remoto si no esta en cache ni local`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns null
        coEvery { localRepository.getById(id) } returns flowOf((Err(TenistaError.NotFound(id))))
        coEvery { remoteRepository.getById(id) } returns flowOf(Ok(testTenista))
        every { cache.put(id, testTenista) } returns Unit
        coEvery { localRepository.save(testTenista) } returns flowOf(Ok(testTenista))

        val result = service.getById(id).first()

        assertAll("Debemos obtener tenista",
            { assertTrue(result.isOk) },
            { assertEquals(testTenista, result.get()) }
        )

        verify(atLeast = 1) { cache.get(id) }
        coVerify(atLeast = 1) { localRepository.getById(id) }
        coVerify(atLeast = 1) { remoteRepository.getById(id) }
        verify(atLeast = 1) { cache.put(id, testTenista) }
        coVerify(atLeast = 1) { localRepository.save(testTenista) }
    }

    @Test
    fun `getById debe retornar error si no esta en cache ni local ni remoto`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns null
        coEvery { localRepository.getById(id) } returns flowOf((Err(TenistaError.NotFound(id))))
        coEvery { remoteRepository.getById(id) } returns flowOf((Err(TenistaError.RemoteError("Error"))))

        val result = service.getById(id).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.RemoteError("Error").message, result.error.message) }
        )

        verify(atLeast = 1) { cache.get(id) }
        coVerify(atLeast = 1) { localRepository.getById(id) }
        coVerify(atLeast = 1) { remoteRepository.getById(id) }
    }

    // Save
    @Test
    fun `save debe retornar tenista guardado`() = runTest {
        val newTenista = testTenista.copy(id = Tenista.NEW_ID, nombre = "Test New", puntos = 10)

        coEvery { localRepository.save(newTenista) } returns flowOf(Ok(newTenista))
        coEvery { remoteRepository.save(newTenista) } returns flowOf(Ok(newTenista))
        every { cache.put(newTenista.id, newTenista) } returns Unit
        coEvery { notificationsService.send(any()) } returns Unit

        val result = service.save(newTenista).first()

        assertAll("Debemos obtener tenista guardado",
            { assertTrue(result.isOk) },
            { assertEquals(newTenista, result.get()) }
        )

        coVerify(atLeast = 1) { localRepository.save(newTenista) }
        coVerify(atLeast = 1) { remoteRepository.save(newTenista) }
        verify(atLeast = 1) { cache.put(newTenista.id, newTenista) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
    }

    @Test
    fun `save debe retornar error si no se puede guardar por validación`() = runTest {
        val newTenista = testTenista.copy(id = Tenista.NEW_ID, nombre = "Test New", puntos = -10)


        val result = service.save(newTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            {
                assertEquals(
                    TenistaError.ValidationError("Los puntos no pueden ser negativos").message,
                    result.error.message
                )
            }
        )

        coVerify(atLeast = 0) { localRepository.save(newTenista) }
        coVerify(atLeast = 0) { remoteRepository.save(newTenista) }
    }

    @Test
    fun `save debe retornar error si no se puede guardar remotamente`() = runTest {
        val newTenista = testTenista.copy(id = Tenista.NEW_ID, nombre = "Test New", puntos = 10)

        coEvery { remoteRepository.save(newTenista) } returns flowOf((Err(TenistaError.RemoteError("Error"))))

        val result = service.save(newTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.RemoteError("Error").message, result.error.message) }
        )

        coVerify(atLeast = 0) { localRepository.save(newTenista) }
        coVerify(atLeast = 1) { remoteRepository.save(newTenista) }
    }

    // update
    @Test
    fun `update debe retornar tenista actualizado`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = 20)

        // Suponemos que esta en la cache
        every { cache.get(updatedTenista.id) } returns updatedTenista
        coEvery { localRepository.update(updatedTenista.id, updatedTenista) } returns flowOf(Ok(updatedTenista))
        coEvery { remoteRepository.update(updatedTenista.id, updatedTenista) } returns flowOf(Ok(updatedTenista))
        every { cache.put(updatedTenista.id, updatedTenista) } returns Unit
        coEvery { notificationsService.send(any()) } returns Unit

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener tenista actualizado",
            { assertTrue(result.isOk) },
            { assertEquals(updatedTenista, result.get()) }
        )

        coVerify(atLeast = 1) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 1) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 1) { cache.put(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
        verify(atLeast = 1) { cache.get(updatedTenista.id) }
    }

    @Test
    fun `update debe retornar error si no se puede actualizar por validación`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = -20)

        // Suponemos que esta en la cache
        every { cache.get(updatedTenista.id) } returns updatedTenista

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            {
                assertEquals(
                    TenistaError.ValidationError("Los puntos no pueden ser negativos").message,
                    result.error.message
                )
            }
        )

        coVerify(atLeast = 0) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 0) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 1) { cache.get(updatedTenista.id) }
    }

    @Test
    fun `update debe retornar error si no se puede actualizar remotamente`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = 20)

        // Suponemos que esta en la cache
        every { cache.get(updatedTenista.id) } returns updatedTenista
        coEvery {
            remoteRepository.update(
                updatedTenista.id,
                updatedTenista
            )
        } returns flowOf((Err(TenistaError.RemoteError("Error"))))

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.RemoteError("Error").message, result.error.message) }
        )

        coVerify(atLeast = 0) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 1) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 1) { cache.get(updatedTenista.id) }
    }

    @Test
    fun `update debe retornar error porque el tenista no existe remotamente`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = 20)

        // Suponemos que esta en la cache y local
        every { cache.get(updatedTenista.id) } returns null
        coEvery { localRepository.getById(updatedTenista.id) } returns flowOf(Err(TenistaError.NotFound(updatedTenista.id)))
        coEvery { remoteRepository.getById(updatedTenista.id) } returns flowOf(Err(TenistaError.RemoteError("Error")))

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.RemoteError("Error").message, result.error.message) }
        )

        coVerify(atLeast = 0) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 0) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 0) { cache.get(updatedTenista.id) }

    }

    // delete

    @Test
    fun `delete debe retornar tenista borrado`() = runTest {
        val id = testTenista.id

        // Suponemos que esta en la cache
        every { cache.get(id) } returns testTenista
        coEvery { localRepository.delete(id) } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.delete(id) } returns flowOf(Ok(Unit))
        every { cache.remove(id) } returns Unit
        coEvery { notificationsService.send(any()) } returns Unit

        val result = service.delete(id).first()

        assertAll("Debemos obtener tenista borrado",
            { assertTrue(result.isOk) },
            { assertEquals(Unit, result.get()) }
        )

        coVerify(atLeast = 1) { localRepository.delete(id) }
        coVerify(atLeast = 1) { remoteRepository.delete(id) }
        verify(atLeast = 1) { cache.remove(id) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    fun `delete debe retornar error si no se puede borrar remotamente`() = runTest {
        val id = testTenista.id

        // Suponemos que esta en la cache
        every { cache.get(id) } returns testTenista
        coEvery { localRepository.delete(id) } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.delete(id) } returns flowOf((Err(TenistaError.RemoteError("Error"))))

        val result = service.delete(id).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.RemoteError("Error").message, result.error.message) }
        )

        coVerify(atLeast = 1) { localRepository.delete(id) }
        coVerify(atLeast = 1) { remoteRepository.delete(id) }
        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    fun `delete debe retornar error si no se puede borrar localmente`() = runTest {
        val id = testTenista.id

        // Suponemos que esta en la cache
        every { cache.get(id) } returns testTenista
        coEvery { remoteRepository.delete(id) } returns flowOf(Ok(Unit))
        coEvery { localRepository.delete(id) } returns flowOf((Err(TenistaError.NotFound(id))))

        val result = service.delete(id).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.NotFound(id).message, result.error.message) }
        )

        coVerify(atLeast = 1) { localRepository.delete(id) }
        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    fun `import debe devolver Ok para fichero CSV`() = runTest {
        val file = File("test.csv")

        coEvery { csvStorage.import(file) } returns flowOf(Ok(listOf(testTenista)))
        coEvery { localRepository.removeAll() } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.save(testTenista) } returns flowOf(Ok(testTenista))
        coEvery { localRepository.save(testTenista) } returns flowOf(Ok(testTenista))

        val result = service.import(file).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk) },
            { assertEquals(listOf(testTenista).size, result.value) }
        )

        coVerify(atLeast = 1) { csvStorage.import(file) }
        coVerify(atLeast = 1) { localRepository.removeAll() }
        coVerify(atLeast = 1) { remoteRepository.save(testTenista) }
    }

    @Test
    fun `import debe devolver Ok para fichero JSON`() = runTest {
        val file = File("test.json")

        coEvery { jsonStorage.import(file) } returns flowOf(Ok(listOf(testTenista)))
        coEvery { localRepository.removeAll() } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.save(testTenista) } returns flowOf(Ok(testTenista))
        coEvery { localRepository.save(testTenista) } returns flowOf(Ok(testTenista))

        val result = service.import(file).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk) },
            { assertEquals(listOf(testTenista).size, result.value) }
        )

        coVerify(atLeast = 1) { jsonStorage.import(file) }
        coVerify(atLeast = 1) { localRepository.removeAll() }
        coVerify(atLeast = 1) { remoteRepository.save(testTenista) }
    }

    @Test
    fun `import debe devolver error si no se puede importar`() = runTest {
        val file = File("test.csv")

        coEvery { csvStorage.import(file) } returns flowOf((Err(TenistaError.StorageError("Error"))))

        val result = service.import(file).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.StorageError("Error").message, result.error.message) }
        )

        coVerify(atLeast = 1) { csvStorage.import(file) }
    }


    @Test
    fun `export debe devolver Ok para fichero CSV de manera local`() = runTest {
        val file = File("test.csv")

        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { csvStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, false).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk) },
            { assertEquals(1, result.value) }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
        coVerify(atLeast = 1) { csvStorage.export(file, listOf(testTenista)) }
    }

    @Test
    fun `export debe devolver Ok para fichero CSV de manera remota`() = runTest {
        val file = File("test.csv")

        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { csvStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, true).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk) },
            { assertEquals(1, result.value) }
        )

        coVerify(atLeast = 1) { remoteRepository.getAll() }
        coVerify(atLeast = 1) { csvStorage.export(file, listOf(testTenista)) }
    }

    @Test
    fun `export debe devolver Ok para fichero Json de manera local`() = runTest {
        val file = File("test.json")

        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { jsonStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, false).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk) },
            { assertEquals(1, result.value) }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
        coVerify(atLeast = 1) { jsonStorage.export(file, listOf(testTenista)) }
    }

    @Test
    fun `export debe devolver Ok para fichero Json remota`() = runTest {
        val file = File("test.json")

        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { jsonStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, true).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk) },
            { assertEquals(1, result.value) }
        )

        coVerify(atLeast = 1) { remoteRepository.getAll() }
        coVerify(atLeast = 1) { jsonStorage.export(file, listOf(testTenista)) }
    }

    @Test
    fun `export debe devolver error si no se puede exportar`() = runTest {
        val file = File("test.csv")

        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery {
            csvStorage.export(
                file,
                listOf(testTenista)
            )
        } returns flowOf((Err(TenistaError.StorageError("Error"))))

        val result = service.export(file, false).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr) },
            { assertEquals(TenistaError.StorageError("Error").message, result.error.message) }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
        coVerify(atLeast = 1) { csvStorage.export(file, listOf(testTenista)) }
    }

}