package mapper

import database.TenistaEntity
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.mapper.toTenistaEntity
import dev.joseluisgs.models.Tenista
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TenistaMapperTest {

    @Test
    fun `debe mapear TenistaDto to Tenista`() {
        val tenistaDto = TenistaDto(
            id = 1,
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
            { assertEquals(tenistaDto.id, tenista.id) },
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
            id = 1,
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
            { assertEquals(tenista.id, tenistaDto.id) },
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

    @Test
    fun `debe mapear TenistaEntity to Tenista`() {
        val tenistaEntity = TenistaEntity(
            id = 1,
            nombre = "Rafael Nadal",
            pais = "España",
            altura = 185,
            peso = 85,
            puntos = 9876,
            mano = "DIESTRO",
            fecha_nacimiento = "1986-06-03",
            created_at = LocalDateTime.now().toString(),
            updated_at = LocalDateTime.now().toString(),
            is_deleted = 1
        )

        val tenista = tenistaEntity.toTenista()

        assertAll(
            { assertEquals(tenistaEntity.id, tenista.id) },
            { assertEquals(tenistaEntity.nombre, tenista.nombre) },
            { assertEquals(tenistaEntity.pais, tenista.pais) },
            { assertEquals(tenistaEntity.altura, tenista.altura.toLong()) },
            { assertEquals(tenistaEntity.peso, tenista.peso.toLong()) },
            { assertEquals(tenistaEntity.puntos, tenista.puntos.toLong()) },
            { assertEquals(tenistaEntity.mano, tenista.mano.name) },
            { assertEquals(tenistaEntity.fecha_nacimiento, tenista.fechaNacimiento.toString()) },
            { assertEquals(tenistaEntity.created_at, tenista.createdAt.toString()) },
            { assertEquals(tenistaEntity.updated_at, tenista.updatedAt.toString()) },
            {
                val isDeleted = tenistaEntity.is_deleted == 1L
                assertEquals(isDeleted, tenista.isDeleted)
            }
        )

    }

    @Test
    fun `debe mapear Tenista to TenistaEntity`() {
        val tenista = Tenista(
            id = 1,
            nombre = "Roger Federer",
            pais = "Suiza",
            altura = 185,
            peso = 85,
            puntos = 8765,
            mano = Tenista.Mano.DIESTRO,
            fechaNacimiento = LocalDate.of(1981, 8, 8),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isDeleted = true
        )

        val tenistaEntity = tenista.toTenistaEntity()

        assertAll(
            { assertEquals(tenista.id, tenistaEntity.id) },
            { assertEquals(tenista.nombre, tenistaEntity.nombre) },
            { assertEquals(tenista.pais, tenistaEntity.pais) },
            { assertEquals(tenista.altura.toLong(), tenistaEntity.altura) },
            { assertEquals(tenista.peso.toLong(), tenistaEntity.peso) },
            { assertEquals(tenista.puntos.toLong(), tenistaEntity.puntos) },
            { assertEquals(tenista.mano.name, tenistaEntity.mano) },
            { assertEquals(tenista.fechaNacimiento.toString(), tenistaEntity.fecha_nacimiento) },
            { assertEquals(tenista.createdAt.toString(), tenistaEntity.created_at) },
            { assertEquals(tenista.updatedAt.toString(), tenistaEntity.updated_at) },
            {
                val isDeleted = if (tenista.isDeleted) 1L else 0L
                assertEquals(isDeleted, tenistaEntity.is_deleted)
            }
        )
    }
}