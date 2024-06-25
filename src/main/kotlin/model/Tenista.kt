package dev.joseluisgs.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Tenista(
    val id: UUID = UUID.randomUUID(),
    val nombre: String,
    val pais: String,
    val altura: Int,
    val peso: Int,
    val puntos: Int,
    val mano: Mano,
    val fechaNacimiento: LocalDate,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false,
) {
    enum class Mano {
        DIESTRO,
        ZURDO
    }
}