package dev.joseluisgs.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object para Tenista
 * Usado para la representación de los datos de Tenista
 */
@Serializable
data class TenistaDto(
    val id: Long,
    val nombre: String,
    val pais: String,
    val altura: Int,
    val peso: Int,
    val puntos: Int,
    val mano: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean? = false,
)