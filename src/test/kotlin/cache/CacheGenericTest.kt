package cache

import dev.joseluisgs.cache.CacheGeneric
import dev.joseluisgs.models.Tenista
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
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
    @DisplayName("Debe devolver un elemento existente en la cache en base a su clave")
    fun `debe devolver un elemento existente en la cache en base a su clave`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()

        cache.put(tenista1.id, tenista1)

        assertAll("comprobar cache obtiene el elemento correcto",
            { assertNotNull(cache.get(tenista1.id), "El elemento no debería ser nulo") },
            { assertEquals(tenista1, cache.get(tenista1.id), "El elemento devuelto debería ser igual al insertado") }
        )
    }

    @Test
    @DisplayName("Debe devolver null si el elemento no existe en la cache en base a su clave")
    fun `debe devolver null si el elemento no existe en la cache en base a su clave`() {
        val cache = CacheGeneric<UUID, Tenista>(2)
        val nonExistingId = UUID.randomUUID()

        assertNull(cache.get(nonExistingId), "El resultado debería ser null para un elemento no existente")
    }

    @Test
    @DisplayName("Debe introducir elementos en la cache")
    fun `debe introducir elementos en la cache`() {
        val cache = CacheGeneric<Long, Tenista>(3)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)
        cache.put(tenista2.id, tenista2)

        assertAll("comprobación de los elementos insertados en la cache",
            { assertEquals(2, cache.size(), "El tamaño de la cache debería ser 2") },
            {
                assertEquals(
                    tenista1,
                    cache.get(tenista1.id),
                    "El primer elemento obtenido debería ser igual al insertado"
                )
            },
            {
                assertEquals(
                    tenista2,
                    cache.get(tenista2.id),
                    "El segundo elemento obtenido debería ser igual al insertado"
                )
            }
        )
    }

    @Test
    @DisplayName("Debe eliminar un tenista si el límite se supera al introducir")
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

        assertAll("comprobar cache tras superar el límite",
            { assertEquals(2, cache.size(), "El tamaño de la cache debería ser 2") },
            { assertFalse(cache.containsKey(tenista2.id), "La cache no debería contener el segundo elemento") },
            { assertFalse(cache.containsKey(tenista3.id), "La cache no debería contener el tercer elemento") },
            { assertEquals(tenista1, cache.get(tenista1.id), "El primer elemento debería estar presente en la cache") },
            { assertEquals(tenista4, cache.get(tenista4.id), "El cuarto elemento debería estar presente en la cache") }
        )
    }

    @Test
    @DisplayName("Debe eliminar los elementos de la cache")
    fun `debe eliminar los elementos de la cache`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)
        cache.put(tenista2.id, tenista2)

        cache.clear()

        assertAll("comprobación tras limpiar la cache",
            { assertEquals(0, cache.size(), "El tamaño de la cache debería ser 0 tras limpiar") },
            {
                assertFalse(
                    cache.containsKey(tenista1.id),
                    "La cache no debería contener el primer elemento tras limpiar"
                )
            },
            {
                assertFalse(
                    cache.containsKey(tenista2.id),
                    "La cache no debería contener el segundo elemento tras limpiar"
                )
            }
        )
    }

    @Test
    @DisplayName("Debe devolver las claves y valores correctos")
    fun `debe devolver las claves y valores correctos`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)
        cache.put(tenista2.id, tenista2)

        assertAll("comprobar claves y valores de la cache",
            { assertEquals(setOf(tenista1.id, tenista2.id), cache.keys(), "Las claves deberían coincidir") },
            { assertEquals(listOf(tenista1, tenista2), cache.values().toList(), "Los valores deberían coincidir") }
        )
    }

    @Test
    @DisplayName("Debe comprobar si existe o no un valor en base a su clave")
    fun `debe comprobar si existe o no un valor en base a su clave`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()

        cache.put(tenista1.id, tenista1)

        assertAll("comprobación de existencia de claves",
            { assertTrue(cache.containsKey(tenista1.id), "La cache debería contener la clave del primer tenista") },
            { assertFalse(cache.containsKey(-99), "La cache no debería contener la clave -99") }
        )
    }

    @Test
    @DisplayName("Debe comprobar si existe un valor")
    fun `debe comprobar si existe un valor`() {
        val cache = CacheGeneric<Long, Tenista>(2)
        val tenista1 = createRandomTenista()
        val tenista2 = createRandomTenista()

        cache.put(tenista1.id, tenista1)

        assertAll("comprobación de existencia de valores",
            { assertTrue(cache.containsValue(tenista1), "La cache debería contener al primer tenista") },
            { assertFalse(cache.containsValue(tenista2), "La cache no debería contener al segundo tenista") }
        )
    }

    @Test
    @DisplayName("Debe comprobar si está vacía")
    fun `debe comprobar si esta vacía`() {
        val cache = CacheGeneric<Long, Tenista>(2)

        assertAll("comprobar si la cache está vacía",
            { assertTrue(cache.isEmpty(), "La cache debería estar vacía inicialmente") },
            { assertFalse(cache.isNotEmpty(), "La cache no debería estar llena inicialmente") }
        )

        val tenista1 = createRandomTenista()
        cache.put(tenista1.id, tenista1)

        assertAll("comprobar si la cache está llena tras añadir un elemento",
            { assertFalse(cache.isEmpty(), "La cache no debería estar vacía tras añadir un elemento") },
            { assertTrue(cache.isNotEmpty(), "La cache debería estar llena tras añadir un elemento") }
        )
    }
}