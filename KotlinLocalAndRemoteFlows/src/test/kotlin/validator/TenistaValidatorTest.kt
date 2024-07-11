package validator

import com.github.michaelbull.result.Ok
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.validator.validate
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertAll
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenistaValidatorTest {

    @Test
    @DisplayName("Validar devuelve Ok cuando el Tenista")
    fun `validar devuelve Ok cuando el Tenista es válido`() {
        val tenista = Tenista(1, "Juan", "España", 180, 75, 100, Tenista.Mano.DIESTRO, LocalDate.of(1990, 5, 20))
        val result = tenista.validate()
        assertEquals(Ok(tenista), result, "El resultado debería ser Ok con el tenista válido")
    }

    @Test
    @DisplayName("Validar devuelve error de validación cuando el nombre está vacío")
    fun `validar devuelve error de validación cuando el nombre está vacío`() {
        val tenista = Tenista(1, "", "España", 180, 75, 100, Tenista.Mano.DIESTRO, LocalDate.of(1990, 5, 20))
        val result = tenista.validate()
        assertAll(
            { assertTrue(result.isErr, "El resultado debería ser un error cuando el nombre está vacío") },
            {
                assertEquals(
                    TenistaError.ValidationError("El nombre no puede estar vacío").message,
                    (result.error).message,
                    "El mensaje de error debería ser 'El nombre no puede estar vacío'"
                )
            }
        )
    }

    @Test
    @DisplayName("Validar devuelve error de validación cuando el país está vacío")
    fun `validar devuelve error de validación cuando el país está vacío`() {
        val tenista = Tenista(1, "Juan", "", 180, 75, 100, Tenista.Mano.DIESTRO, LocalDate.of(1990, 5, 20))
        val result = tenista.validate()
        assertAll(
            { assertTrue(result.isErr, "El resultado debería ser un error cuando el país está vacío") },
            {
                assertEquals(
                    TenistaError.ValidationError("El país no puede estar vacío").message,
                    (result.error).message,
                    "El mensaje de error debería ser 'El país no puede estar vacío'"
                )
            }
        )
    }

    @Test
    @DisplayName("Validar devuelve error de validación cuando la altura es menor o igual a 0")
    fun `validar devuelve error de validación cuando la altura es menor o igual a 0`() {
        val tenista = Tenista(1, "Juan", "España", 0, 75, 100, Tenista.Mano.DIESTRO, LocalDate.of(1990, 5, 20))
        val result = tenista.validate()
        assertAll(
            { assertTrue(result.isErr, "El resultado debería ser un error cuando la altura es menor o igual a 0") },
            {
                assertEquals(
                    TenistaError.ValidationError("La altura no puede ser menor o igual a 0").message,
                    (result.error).message,
                    "El mensaje de error debería ser 'La altura no puede ser menor o igual a 0'"
                )
            }
        )
    }

    @Test
    @DisplayName("Validar devuelve error de validación cuando el peso es menor o igual a 0")
    fun `validar devuelve error de validación cuando el peso es menor o igual a 0`() {
        val tenista = Tenista(1, "Juan", "España", 180, 0, 100, Tenista.Mano.DIESTRO, LocalDate.of(1990, 5, 20))
        val result = tenista.validate()
        assertAll(
            { assertTrue(result.isErr, "El resultado debería ser un error cuando el peso es menor o igual a 0") },
            {
                assertEquals(
                    TenistaError.ValidationError("El peso no puede ser menor o igual a 0").message,
                    (result.error).message,
                    "El mensaje de error debería ser 'El peso no puede ser menor o igual a 0'"
                )
            }
        )
    }

    @Test
    @DisplayName("Validar devuelve error de validación cuando los puntos son negativos")
    fun `validar devuelve error de validación cuando los puntos son negativos`() {
        val tenista = Tenista(1, "Juan", "España", 180, 75, -10, Tenista.Mano.DIESTRO, LocalDate.of(1990, 5, 20))
        val result = tenista.validate()
        assertAll(
            { assertTrue(result.isErr, "El resultado debería ser un error cuando los puntos son negativos") },
            {
                assertEquals(
                    TenistaError.ValidationError("Los puntos no pueden ser negativos").message,
                    (result.error).message,
                    "El mensaje de error debería ser 'Los puntos no pueden ser negativos'"
                )
            }
        )
    }

    @Test
    @DisplayName("Validar devuelve error de validación cuando la fecha de nacimiento es posterior a la fecha actual")
    fun `validar devuelve error de validación cuando la fecha de nacimiento es posterior a la fecha actual`() {
        val tenista = Tenista(1, "Juan", "España", 180, 75, 100, Tenista.Mano.DIESTRO, LocalDate.now().plusDays(1))
        val result = tenista.validate()
        assertAll(
            {
                assertTrue(
                    result.isErr,
                    "El resultado debería ser un error cuando la fecha de nacimiento es posterior a la fecha actual"
                )
            },
            {
                assertEquals(
                    TenistaError.ValidationError("La fecha de nacimiento no puede ser posterior a la actual").message,
                    (result.error).message,
                    "El mensaje de error debería ser 'La fecha de nacimiento no puede ser posterior a la actual'"
                )
            }
        )
    }
}
