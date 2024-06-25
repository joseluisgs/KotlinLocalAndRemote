package dev.joseluisgs.dto

/**
 * Data Transfer Object para Tenista
 * Usado para la representación de los datos de Tenista
 */
data class TenistaDto(
    val id: String,
    val nombre: String,
    val pais: String,
    val altura: Int,
    val peso: Int,
    val puntos: Int,
    val mano: String,
    val fechaNacimiento: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isDeleted: Boolean? = false,
)