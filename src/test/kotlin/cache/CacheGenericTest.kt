package cache

import dev.joseluisgs.cache.CacheGeneric
import dev.joseluisgs.models.Tenista
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class CacheGenericTest {

    private fun createRandomTenista(): Tenista {
        return Tenista(
            id = Random().nextLong(),
            nombre = "Tenista",
            pais = "Pais",
            altura = 180,
            peso = 75,
            puntos = 1000,
            mano = Tenista.Mano.DIESTRO,
            fechaNacimiento = LocalDate.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isDeleted = false
        )
    }

    @Test
    fun `debe devolver unm elemento existente en la cache en base a su clave`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()

        cache.put(tenista1.id, tenista1)

        assertAll(
            { assertNotNull(cache.get(tenista1.id)) },
            { assertEquals(tenista1, cache.get(tenista1.id)) }
        )
    }

    @Test
    fun `debe devolver null si el elemento no existe en la cache en base a su clave`() {
        val cache = CacheGeneric<UUID, Tenista>(2)
        val nonExistingId = UUID.randomUUID()

        assertNull(cache.get(nonExistingId))
    }

    @Test
    fun `debe introducir elementos en la cache`() {
        val cache = CacheGeneric<Long, Tenista>(3)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)
        cache.put(tenista2.id, tenista2)

        assertAll(
            { assertEquals(2, cache.size()) },
            { assertEquals(tenista1, cache.get(tenista1.id)) },
            { assertEquals(tenista2, cache.get(tenista2.id)) }
        )
    }

    @Test
    fun `debe eliminar un tenista si el limite se supera al introducir`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()
        val tenista3 = createRandomTenista()
        val tenista4 = createRandomTenista()

        // Añadimos 3 elementos, Se eliminará el 2 y 3 y se quedará el 1 y el 4 al ser el menos usado
        cache.put(tenista1.id, tenista1)
        cache.put(tenista2.id, tenista2)
        cache.put(tenista1.id, tenista1)
        cache.put(tenista3.id, tenista3)
        cache.put(tenista4.id, tenista4)
        cache.put(tenista1.id, tenista1)

        assertAll(
            { assertEquals(2, cache.size()) },
            { assertFalse(cache.containsKey(tenista2.id)) },
            { assertFalse(cache.containsKey(tenista3.id)) },
            { assertEquals(tenista1, cache.get(tenista1.id)) },
            { assertEquals(tenista4, cache.get(tenista4.id)) }
        )
    }

    @Test
    fun `debe eliminar los elementos de la cache`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)
        cache.put(tenista2.id, tenista2)

        cache.clear()

        assertAll(
            { assertEquals(0, cache.size()) },
            { assertFalse(cache.containsKey(tenista1.id)) },
            { assertFalse(cache.containsKey(tenista2.id)) }
        )
    }

    @Test
    fun `debe devolver las claves y valores correctos`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)
        cache.put(tenista2.id, tenista2)

        assertAll(
            { assertEquals(setOf(tenista1.id, tenista2.id), cache.keys()) },
            { assertEquals(listOf(tenista1, tenista2), cache.values().toList()) }
        )
    }

    @Test
    fun `debe comprobar si existe o no un valor en base a su clave`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()

        cache.put(tenista1.id, tenista1)

        assertAll(
            { assertTrue(cache.containsKey(tenista1.id)) },
            { assertFalse(cache.containsKey(-99)) }
        )
    }

    @Test
    fun `debe comprobar si existe un valor`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)

        assertAll(
            { assertTrue(cache.containsValue(tenista1)) },
            { assertFalse(cache.containsValue(tenista2)) }
        )
    }

    @Test
    fun `debe comprobar si esta vacía`() {
        val cache = CacheGeneric<Long, Tenista>(2)

        assertAll(
            { assertTrue(cache.isEmpty()) },
            { assertFalse(cache.isNotEmpty()) }
        )

        val tenista1 = createRandomTenista()
        cache.put(tenista1.id, tenista1)

        assertAll(
            { assertFalse(cache.isEmpty()) },
            { assertTrue(cache.isNotEmpty()) }
        )
    }
}