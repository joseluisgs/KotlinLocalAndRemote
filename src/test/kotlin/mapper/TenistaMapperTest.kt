package mapper

import database.TenistaEntity
import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.mapper.toTenista
import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.mapper.toTenistaEntity
import dev.joseluisgs.model.Tenista
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TenistaMapperTest {

    @Test
    @DisplayName("ear TenistaDto a Tenista")
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
            { assertEquals(tenistaDto.id, tenista.id, "El id debe ser el mismo") },
            { assertEquals(tenistaDto.nombre, tenista.nombre, "El nombre debe ser el mismo") },
            { assertEquals(tenistaDto.pais, tenista.pais, "El país debe ser el mismo") },
            { assertEquals(tenistaDto.altura, tenista.altura, "La altura debe ser la misma") },
            { assertEquals(tenistaDto.peso, tenista.peso, "El peso debe ser el mismo") },
            { assertEquals(tenistaDto.puntos, tenista.puntos, "Los puntos deben ser los mismos") },
            { assertEquals(tenistaDto.mano, tenista.mano.name, "La mano debe ser la misma") },
            {
                assertEquals(
                    tenistaDto.fechaNacimiento,
                    tenista.fechaNacimiento.toString(),
                    "La fecha de nacimiento debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenistaDto.createdAt,
                    tenista.createdAt.toString(),
                    "La fecha de creación debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenistaDto.updatedAt,
                    tenista.updatedAt.toString(),
                    "La fecha de actualización debe ser la misma"
                )
            },
            { assertEquals(tenistaDto.isDeleted, tenista.isDeleted, "El estado de eliminado debe ser el mismo") }
        )
    }

    @Test
    @DisplayName("Debe mapear Tenista a TenistaDto")
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
            { assertEquals(tenista.id, tenistaDto.id, "El id debe ser el mismo") },
            { assertEquals(tenista.nombre, tenistaDto.nombre, "El nombre debe ser el mismo") },
            { assertEquals(tenista.pais, tenistaDto.pais, "El país debe ser el mismo") },
            { assertEquals(tenista.altura, tenistaDto.altura, "La altura debe ser la misma") },
            { assertEquals(tenista.peso, tenistaDto.peso, "El peso debe ser el mismo") },
            { assertEquals(tenista.puntos, tenistaDto.puntos, "Los puntos deben ser los mismos") },
            { assertEquals(tenista.mano.name, tenistaDto.mano, "La mano debe ser la misma") },
            {
                assertEquals(
                    tenista.fechaNacimiento.toString(),
                    tenistaDto.fechaNacimiento,
                    "La fecha de nacimiento debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenista.createdAt.toString(),
                    tenistaDto.createdAt,
                    "La fecha de creación debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenista.updatedAt.toString(),
                    tenistaDto.updatedAt,
                    "La fecha de actualización debe ser la misma"
                )
            },
            { assertEquals(tenista.isDeleted, tenistaDto.isDeleted, "El estado de eliminado debe ser el mismo") }
        )
    }

    @Test
    @DisplayName("Debe mapear TenistaEntity a Tenista")
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
            { assertEquals(tenistaEntity.id, tenista.id, "El id debe ser el mismo") },
            { assertEquals(tenistaEntity.nombre, tenista.nombre, "El nombre debe ser el mismo") },
            { assertEquals(tenistaEntity.pais, tenista.pais, "El país debe ser el mismo") },
            { assertEquals(tenistaEntity.altura, tenista.altura.toLong(), "La altura debe ser la misma") },
            { assertEquals(tenistaEntity.peso, tenista.peso.toLong(), "El peso debe ser el mismo") },
            { assertEquals(tenistaEntity.puntos, tenista.puntos.toLong(), "Los puntos deben ser los mismos") },
            { assertEquals(tenistaEntity.mano, tenista.mano.name, "La mano debe ser la misma") },
            {
                assertEquals(
                    tenistaEntity.fecha_nacimiento,
                    tenista.fechaNacimiento.toString(),
                    "La fecha de nacimiento debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenistaEntity.created_at,
                    tenista.createdAt.toString(),
                    "La fecha de creación debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenistaEntity.updated_at,
                    tenista.updatedAt.toString(),
                    "La fecha de actualización debe ser la misma"
                )
            },
            {
                val isDeleted = tenistaEntity.is_deleted == 1L
                assertEquals(isDeleted, tenista.isDeleted, "El estado de eliminado debe ser el mismo")
            }
        )
    }

    @Test
    @DisplayName("Debe mapear Tenista a TenistaEntity")
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
            { assertEquals(tenista.id, tenistaEntity.id, "El id debe ser el mismo") },
            { assertEquals(tenista.nombre, tenistaEntity.nombre, "El nombre debe ser el mismo") },
            { assertEquals(tenista.pais, tenistaEntity.pais, "El país debe ser el mismo") },
            { assertEquals(tenista.altura.toLong(), tenistaEntity.altura, "La altura debe ser la misma") },
            { assertEquals(tenista.peso.toLong(), tenistaEntity.peso, "El peso debe ser el mismo") },
            { assertEquals(tenista.puntos.toLong(), tenistaEntity.puntos, "Los puntos deben ser los mismos") },
            { assertEquals(tenista.mano.name, tenistaEntity.mano, "La mano debe ser la misma") },
            {
                assertEquals(
                    tenista.fechaNacimiento.toString(),
                    tenistaEntity.fecha_nacimiento,
                    "La fecha de nacimiento debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenista.createdAt.toString(),
                    tenistaEntity.created_at,
                    "La fecha de creación debe ser la misma"
                )
            },
            {
                assertEquals(
                    tenista.updatedAt.toString(),
                    tenistaEntity.updated_at,
                    "La fecha de actualización debe ser la misma"
                )
            },
            {
                val isDeleted = if (tenista.isDeleted) 1L else 0L
                assertEquals(isDeleted, tenistaEntity.is_deleted, "El estado de eliminado debe ser el mismo")
            }
        )
    }
}
