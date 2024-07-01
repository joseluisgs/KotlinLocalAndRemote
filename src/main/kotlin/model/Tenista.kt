package dev.joseluisgs.models

import java.time.LocalDate
import java.time.LocalDateTime

data class Tenista(
    val id: Long = NEW_ID,
    val nombre: String,
    val pais: String,
    val altura: Int,
    val peso: Int,
    val puntos: Int,
    val mano: Mano,
    val fechaNacimiento: LocalDate,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isDeleted: Boolean = false,
) {

    companion object {
        val NEW_ID = 0L
    }

    enum class Mano {
        DIESTRO,
        ZURDO
    }

}