package mapper

import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.models.Tenista
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TenistaMapperTest {

    @Test
    fun `debe mapear TenistaDto to Tenista`() {
        val tenistaDto = TenistaDto(
            id = UUID.randomUUID().toString(),
            nombre = "Rafael Nadal",
            pais = "España",
            altura = 185,
            peso = 85,
            puntos = 9876,
            mano = "DIESTRO",
            fechaNacimiento = "1986-06-03",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            isDeleted = false
        )

        val tenista = tenistaDto.toTenista()

        assertAll(
            { assertEquals(UUID.fromString(tenistaDto.id), tenista.id) },
            { assertEquals(tenistaDto.nombre, tenista.nombre) },
            { assertEquals(tenistaDto.pais, tenista.pais) },
            { assertEquals(tenistaDto.altura, tenista.altura) },
            { assertEquals(tenistaDto.peso, tenista.peso) },
            { assertEquals(tenistaDto.puntos, tenista.puntos) },
            { assertEquals(tenistaDto.mano, tenista.mano.name) },
            { assertEquals(tenistaDto.fechaNacimiento, tenista.fechaNacimiento.toString()) },
            { assertEquals(tenistaDto.createdAt, tenista.createdAt.toString()) },
            { assertEquals(tenistaDto.updatedAt, tenista.updatedAt.toString()) },
            { assertEquals(tenistaDto.isDeleted, tenista.isDeleted) }
        )

    }

    @Test
    fun `debe mapear Tenista to TenistaDto`() {
        val tenista = Tenista(
            id = UUID.randomUUID(),
            nombre = "Roger Federer",
            pais = "Suiza",
            altura = 185,
            peso = 85,
            puntos = 8765,
            mano = Tenista.Mano.DIESTRO,
            fechaNacimiento = LocalDate.of(1981, 8, 8),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isDeleted = false
        )

        val tenistaDto = tenista.toTenistaDto()

        assertAll(
            { assertEquals(tenista.id.toString(), tenistaDto.id) },
            { assertEquals(tenista.nombre, tenistaDto.nombre) },
            { assertEquals(tenista.pais, tenistaDto.pais) },
            { assertEquals(tenista.altura, tenistaDto.altura) },
            { assertEquals(tenista.peso, tenistaDto.peso) },
            { assertEquals(tenista.puntos, tenistaDto.puntos) },
            { assertEquals(tenista.mano.name, tenistaDto.mano) },
            { assertEquals(tenista.fechaNacimiento.toString(), tenistaDto.fechaNacimiento) },
            { assertEquals(tenista.createdAt.toString(), tenistaDto.createdAt) },
            { assertEquals(tenista.updatedAt.toString(), tenistaDto.updatedAt) },
            { assertEquals(tenista.isDeleted, tenistaDto.isDeleted) }
        )
    }
}