package service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import dev.joseluisgs.cache.TenistasCache
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.notifications.TenistasNotifications
import dev.joseluisgs.repository.TenistasRepositoryLocal
import dev.joseluisgs.repository.TenistasRepositoryRemote
import dev.joseluisgs.service.TenistasServiceImpl
import dev.joseluisgs.storage.TenistasStorageCsv
import dev.joseluisgs.storage.TenistasStorageJson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
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
    lateinit var cache: TenistasCache

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


    @Test
    @DisplayName("Obtener todos los tenistas localmente debe retornar lista de tenistas")
    fun `getAll debe retornar lista de tenistas localmente`() = runTest {
        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))

        val result = service.getAll(false).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(listOf(testTenista), result.value, "La lista de tenistas debe ser la esperada") }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
    }

    @Test
    @DisplayName("Obtener todos los tenistas remotamente debe retornar lista de tenistas")
    fun `getAll lista de tenistas remota`() = runTest {
        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { localRepository.saveAll(any()) } returns flowOf(Ok(1))
        coEvery { localRepository.removeAll() } returns flowOf(Ok(Unit))
        val result = service.getAll(true).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(listOf(testTenista), result.value, "La lista de tenistas debe ser la esperada") }
        )

        coVerify(atLeast = 1) { remoteRepository.getAll() }
        coVerify(atLeast = 1) { localRepository.saveAll(any()) }
        coVerify(atLeast = 1) { localRepository.removeAll() }
    }

    @Test
    @DisplayName("Obtener tenista por ID debe retornar tenista en cache")
    fun `getById debe retornar tenista en cache`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns testTenista

        val result = service.getById(id).first()

        assertAll("Debemos obtener tenista",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(testTenista, result.get(), "El tenista debe ser el esperado") }
        )

        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    @DisplayName("Obtener tenista por ID debe retornar tenista local si no está en cache")
    fun `getById debe retornar tenista local si no esta en cache`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns null
        coEvery { localRepository.getById(id) } returns flowOf(Ok(testTenista))
        every { cache.put(id, testTenista) } returns Unit

        val result = service.getById(id).first()

        assertAll("Debemos obtener tenista",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(testTenista, result.get(), "El tenista debe ser el esperado") }
        )

        verify(atLeast = 1) { cache.get(id) }
        coVerify(atLeast = 1) { localRepository.getById(id) }
        verify(atLeast = 1) { cache.put(id, testTenista) }
    }

    @Test
    @DisplayName("Obtener tenista por ID debe retornar tenista remoto si no está en cache ni local")
    fun `getById debe retornar tenista remoto si no esta en cache ni local`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns null
        coEvery { localRepository.getById(id) } returns flowOf((Err(TenistaError.NotFound(id))))
        coEvery { remoteRepository.getById(id) } returns flowOf(Ok(testTenista))
        every { cache.put(id, testTenista) } returns Unit
        coEvery { localRepository.save(testTenista) } returns flowOf(Ok(testTenista))

        val result = service.getById(id).first()

        assertAll("Debemos obtener tenista",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(testTenista, result.get(), "El tenista debe ser el esperado") }
        )

        verify(atLeast = 1) { cache.get(id) }
        coVerify(atLeast = 1) { localRepository.getById(id) }
        coVerify(atLeast = 1) { remoteRepository.getById(id) }
        verify(atLeast = 1) { cache.put(id, testTenista) }
        coVerify(atLeast = 1) { localRepository.save(testTenista) }
    }

    @Test
    @DisplayName("Obtener tenista por ID debe retornar error si no está en cache, local ni remoto")
    fun `getById debe retornar error si no esta en cache ni local ni remoto`() = runTest {
        val id = testTenista.id
        every { cache.get(id) } returns null
        coEvery { localRepository.getById(id) } returns flowOf((Err(TenistaError.NotFound(id))))
        coEvery { remoteRepository.getById(id) } returns flowOf((Err(TenistaError.RemoteError("Error"))))

        val result = service.getById(id).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.RemoteError("Error").message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        verify(atLeast = 1) { cache.get(id) }
        coVerify(atLeast = 1) { localRepository.getById(id) }
        coVerify(atLeast = 1) { remoteRepository.getById(id) }
    }

    @Test
    @DisplayName("Guardar tenista debe retornar tenista guardado")
    fun `save debe retornar tenista guardado`() = runTest {
        val newTenista = testTenista.copy(id = Tenista.NEW_ID, nombre = "Test New", puntos = 10)

        coEvery { localRepository.save(newTenista) } returns flowOf(Ok(newTenista))
        coEvery { remoteRepository.save(newTenista) } returns flowOf(Ok(newTenista))
        every { cache.put(newTenista.id, newTenista) } returns Unit
        coEvery { notificationsService.send(any()) } returns Unit

        val result = service.save(newTenista).first()

        assertAll("Debemos obtener tenista guardado",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(newTenista, result.get(), "El tenista guardado debe ser el esperado") }
        )

        coVerify(atLeast = 1) { localRepository.save(newTenista) }
        coVerify(atLeast = 1) { remoteRepository.save(newTenista) }
        verify(atLeast = 1) { cache.put(newTenista.id, newTenista) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
    }

    @Test
    @DisplayName("Guardar tenista debe retornar error si no se puede guardar por validación")
    fun `save debe retornar error si no se puede guardar por validacion`() = runTest {
        val newTenista = testTenista.copy(id = Tenista.NEW_ID, nombre = "Test New", puntos = -10)

        val result = service.save(newTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.ValidationError("Los puntos no pueden ser negativos").message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        coVerify(atLeast = 0) { localRepository.save(newTenista) }
        coVerify(atLeast = 0) { remoteRepository.save(newTenista) }
    }

    @Test
    @DisplayName("Guardar tenista debe retornar error si no se puede guardar remotamente")
    fun `save debe retornar error si no se puede guardar remotamente`() = runTest {
        val newTenista = testTenista.copy(id = Tenista.NEW_ID, nombre = "Test New", puntos = 10)

        coEvery { remoteRepository.save(newTenista) } returns flowOf((Err(TenistaError.RemoteError("Error"))))

        val result = service.save(newTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.RemoteError("Error").message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        coVerify(atLeast = 0) { localRepository.save(newTenista) }
        coVerify(atLeast = 1) { remoteRepository.save(newTenista) }
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar tenista actualizado")
    fun `update debe retornar tenista actualizado`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = 20)

        // Suponemos que está en la cache
        every { cache.get(updatedTenista.id) } returns updatedTenista
        coEvery { localRepository.update(updatedTenista.id, updatedTenista) } returns flowOf(Ok(updatedTenista))
        coEvery { remoteRepository.update(updatedTenista.id, updatedTenista) } returns flowOf(Ok(updatedTenista))
        every { cache.put(updatedTenista.id, updatedTenista) } returns Unit
        coEvery { notificationsService.send(any()) } returns Unit

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener tenista actualizado",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(updatedTenista, result.get(), "El tenista actualizado debe ser el esperado") }
        )

        coVerify(atLeast = 1) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 1) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 1) { cache.put(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
        verify(atLeast = 1) { cache.get(updatedTenista.id) }
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar error si no se puede actualizar por validación")
    fun `update debe retornar error si no se puede actualizar por validacion`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = -20)

        // Suponemos que está en la cache
        every { cache.get(updatedTenista.id) } returns updatedTenista

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.ValidationError("Los puntos no pueden ser negativos").message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        coVerify(atLeast = 0) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 0) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 1) { cache.get(updatedTenista.id) }
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar error si no se puede actualizar remotamente")
    fun `update debe retornar error si no se puede actualizar remotamente`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = 20)

        // Suponemos que está en la cache
        every { cache.get(updatedTenista.id) } returns updatedTenista
        coEvery {
            remoteRepository.update(
                updatedTenista.id,
                updatedTenista
            )
        } returns flowOf((Err(TenistaError.RemoteError("Error"))))

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.RemoteError("Error").message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        coVerify(atLeast = 0) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 1) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 1) { cache.get(updatedTenista.id) }
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar error porque el tenista no existe remotamente")
    fun `update debe retornar error porque el tenista no existe remotamente`() = runTest {
        val updatedTenista = testTenista.copy(nombre = "Test Update", puntos = 20)

        // Suponemos que está en la cache y local
        every { cache.get(updatedTenista.id) } returns null
        coEvery { localRepository.getById(updatedTenista.id) } returns flowOf(Err(TenistaError.NotFound(updatedTenista.id)))
        coEvery { remoteRepository.getById(updatedTenista.id) } returns flowOf(Err(TenistaError.RemoteError("Error")))

        val result = service.update(updatedTenista.id, updatedTenista).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.RemoteError("Error").message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        coVerify(atLeast = 0) { localRepository.update(updatedTenista.id, updatedTenista) }
        coVerify(atLeast = 0) { remoteRepository.update(updatedTenista.id, updatedTenista) }
        verify(atLeast = 0) { cache.get(updatedTenista.id) }
    }

    @Test
    @DisplayName("Borrar tenista debe retornar tenista borrado")
    fun `delete debe retornar tenista borrado`() = runTest {
        val id = testTenista.id

        // Suponemos que está en la cache
        every { cache.get(id) } returns testTenista
        coEvery { localRepository.delete(id) } returns flowOf(Ok(id))
        coEvery { remoteRepository.delete(id) } returns flowOf(Ok(id))
        every { cache.remove(id) } returns Unit
        coEvery { notificationsService.send(any()) } returns Unit

        val result = service.delete(id).first()

        assertAll("Debemos obtener tenista borrado",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(id, result.get(), "El ID del tenista borrado debe ser el esperado") }
        )

        coVerify(atLeast = 1) { localRepository.delete(id) }
        coVerify(atLeast = 1) { remoteRepository.delete(id) }
        verify(atLeast = 1) { cache.remove(id) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    @DisplayName("Borrar tenista debe retornar error si no se puede borrar remotamente")
    fun `delete debe retornar error si no se puede borrar remotamente`() = runTest {
        val id = testTenista.id

        // Suponemos que está en la cache
        every { cache.get(id) } returns testTenista
        coEvery { localRepository.delete(id) } returns flowOf(Ok(id))
        coEvery { remoteRepository.delete(id) } returns flowOf(Err(TenistaError.RemoteError("Error")))

        val result = service.delete(id).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.RemoteError("Error").message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        coVerify(atLeast = 1) { localRepository.delete(id) }
        coVerify(atLeast = 1) { remoteRepository.delete(id) }
        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    @DisplayName("Borrar tenista debe retornar error si no se puede borrar localmente")
    fun `delete debe retornar error si no se puede borrar localmente`() = runTest {
        val id = testTenista.id

        // Suponemos que está en la cache
        every { cache.get(id) } returns testTenista
        coEvery { remoteRepository.delete(id) } returns flowOf(Ok(id))
        coEvery { localRepository.delete(id) } returns flowOf((Err(TenistaError.NotFound(id))))

        val result = service.delete(id).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            {
                assertEquals(
                    TenistaError.NotFound(id).message,
                    result.error.message,
                    "El mensaje de error debe ser el esperado"
                )
            }
        )

        coVerify(atLeast = 1) { localRepository.delete(id) }
        verify(atLeast = 1) { cache.get(id) }
    }

    @Test
    @DisplayName("Importar debe devolver Ok para fichero CSV")
    fun `import debe devolver Ok para fichero CSV`() = runTest {
        val file = File("test.csv")

        coEvery { csvStorage.import(file) } returns flowOf(Ok(listOf(testTenista)))
        coEvery { localRepository.removeAll() } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.save(testTenista) } returns flowOf(Ok(testTenista))
        coEvery { localRepository.save(testTenista) } returns flowOf(Ok(testTenista))

        val result = service.import(file).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(1, result.value, "La lista de tenistas debe tener un tamaño de 1") }
        )

        coVerify(atLeast = 1) { csvStorage.import(file) }
        coVerify(atLeast = 1) { localRepository.removeAll() }
        coVerify(atLeast = 1) { remoteRepository.save(testTenista) }
    }

    @Test
    @DisplayName("Importar debe devolver Ok para fichero JSON")
    fun `import debe devolver Ok para fichero JSON`() = runTest {
        val file = File("test.json")
        coEvery { jsonStorage.import(file) } returns flowOf(Ok(listOf(testTenista)))
        coEvery { localRepository.removeAll() } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.save(testTenista) } returns flowOf(Ok(testTenista))
        coEvery { localRepository.save(testTenista) } returns flowOf(Ok(testTenista))

        val result = service.import(file).first()

        assertAll("Debemos obtener lista de tenistas",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(1, result.value, "La lista de tenistas debe tener un tamaño de 1") }
        )

        coVerify(atLeast = 1) { jsonStorage.import(file) }
        coVerify(atLeast = 1) { localRepository.removeAll() }
        coVerify(atLeast = 1) { remoteRepository.save(testTenista) }
    }

    @Test
    @DisplayName("Importar debe devolver error si no importar")
    fun `import debe devolver error si no se puede importar`() = runTest {
        val file = File("test.csv")

        coEvery { csvStorage.import(file) } returns flowOf(Err(TenistaError.StorageError("Error")))

        val result = service.import(file).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            { assertEquals("ERROR: Error", result.error.message, "El mensaje de error debe ser 'Error'") }
        )

        coVerify(atLeast = 1) { csvStorage.import(file) }
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero CSV de manera local")
    fun `export debe devolver Ok para fichero CSV de manera local`() = runTest {
        val file = File("test.csv")

        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { csvStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, false).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(1, result.value, "El valor del resultado debe ser 1") }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
        coVerify(atLeast = 1) { csvStorage.export(file, listOf(testTenista)) }
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero CSV de manera remota")
    fun `export debe devolver Ok para fichero CSV de manera remota`() = runTest {
        val file = File("test.csv")

        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { csvStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, true).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(1, result.value, "El valor del resultado debe ser 1") }
        )

        coVerify(atLeast = 1) { remoteRepository.getAll() }
        coVerify(atLeast = 1) { csvStorage.export(file, listOf(testTenista)) }
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero Json de manera local")
    fun `export debe devolver Ok para fichero Json de manera local`() = runTest {
        val file = File("test.json")

        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { jsonStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, false).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(1, result.value, "El valor del resultado debe ser 1") }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
        coVerify(atLeast = 1) { jsonStorage.export(file, listOf(testTenista)) }
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero Json de manera remota")
    fun `export debe devolver Ok para fichero Json remota`() = runTest {
        val file = File("test.json")

        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { jsonStorage.export(file, listOf(testTenista)) } returns flowOf(Ok(1))

        val result = service.export(file, true).first()

        assertAll("Debemos obtener Ok",
            { assertTrue(result.isOk, "El resultado debe ser Ok") },
            { assertEquals(1, result.value, "El valor del resultado debe ser 1") }
        )

        coVerify(atLeast = 1) { remoteRepository.getAll() }
        coVerify(atLeast = 1) { jsonStorage.export(file, listOf(testTenista)) }
    }

    @Test
    @DisplayName("Exportar debe devolver error si no se puede exportar")
    fun `export debe devolver error si no se puede exportar`() = runTest {
        val file = File("test.csv")

        coEvery { localRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery {
            csvStorage.export(
                file,
                listOf(testTenista)
            )
        } returns flowOf(Err(TenistaError.StorageError("Error")))

        val result = service.export(file, false).first()

        assertAll("Debemos obtener error",
            { assertTrue(result.isErr, "El resultado debe ser un error") },
            { assertEquals("ERROR: Error", result.error.message, "El mensaje de error debe ser 'Error'") }
        )

        coVerify(atLeast = 1) { localRepository.getAll() }
        coVerify(atLeast = 1) { csvStorage.export(file, listOf(testTenista)) }
    }

    @Test
    @DisplayName("Load data debe cargar los datos")
    fun `load data debe cargar los datos`() = runTest {
        coEvery { localRepository.removeAll() } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { localRepository.saveAll(any()) } returns flowOf(Ok(1))
        coEvery { notificationsService.send(any()) } returns Unit
        every { cache.clear() } returns Unit

        // Llamar al método que deseas probar
        service.loadData()

        // Verifica que las llamadas esperadas a los métodos se hayan realizado
        coVerify(atLeast = 1) { localRepository.removeAll() }
        coVerify(atLeast = 1) { remoteRepository.getAll() }
        coVerify(atLeast = 1) { localRepository.saveAll(any()) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
        verify(atLeast = 1) { cache.clear() }
    }

    @Test
    @DisplayName("Refresh debe actualizar los datos")
    fun `refresh debe actualizar los datos`() = runTest {
        // Define los comportamientos esperados de tus mocks
        coEvery { localRepository.removeAll() } returns flowOf(Ok(Unit))
        coEvery { remoteRepository.getAll() } returns flowOf(Ok(listOf(testTenista)))
        coEvery { localRepository.saveAll(any()) } returns flowOf(Ok(1))
        coEvery { notificationsService.send(any()) } returns Unit
        every { cache.clear() } returns Unit

        // Llamar al método que deseas probar
        val job = launch { service.refresh() }

        delay(1000) // Permitir que la corutina se ejecute por un tiempo específico y con ello se realicen las llamadas

        job.cancelAndJoin() // Cancelar la corutina después de la prueba

        // Verifica que las llamadas esperadas a los métodos se hayan realizado
        coVerify(atLeast = 1) { localRepository.removeAll() }
        coVerify(atLeast = 1) { remoteRepository.getAll() }
        coVerify(atLeast = 1) { localRepository.saveAll(any()) }
        coVerify(atLeast = 1) { notificationsService.send(any()) }
        verify(atLeast = 1) { cache.clear() }
    }
}