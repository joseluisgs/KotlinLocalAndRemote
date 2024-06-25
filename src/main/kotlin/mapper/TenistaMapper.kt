package dev.joseluisgs.mapper

import dev.joseluisgs.dto.TenistaDto
import dev.joseluisgs.models.Tenista
import java.time.LocalDateTime.now
import java.time.LocalDateTime.parse

/**
 * Mapeo de Tenista a TenistaDto y viceversa
 */


/**
 * Mapeo de TenistaDto a Tenista usando funciones de extensión
 * En base a los campos de TenistaDto se crea un Tenista
 */
fun TenistaDto.toTenista(): Tenista {
    return Tenista(
        id = java.util.UUID.fromString(this.id),
        nombre = this.nombre,
        pais = this.pais,
        altura = this.altura,
        peso = this.peso,
        puntos = this.puntos,
        mano = Tenista.Mano.valueOf(this.mano),
        fechaNacimiento = java.time.LocalDate.parse(this.fechaNacimiento),
        createdAt = this.createdAt?.let { parse(it) } ?: now(),
        updatedAt = this.updatedAt?.let { parse(it) } ?: now(),
        isDeleted = this.isDeleted ?: false
    )
}

/**
 * Mapeo de Tenista a TenistaDto usando funciones de extensión
 * En base a los campos de Tenista se crea un TenistaDto
 */
fun Tenista.toTenistaDto(): TenistaDto {
    return TenistaDto(
        id = this.id.toString(),
        nombre = this.nombre,
        pais = this.pais,
        altura = this.altura,
        peso = this.peso,
        puntos = this.puntos,
        mano = this.mano.name,
        fechaNacimiento = this.fechaNacimiento.toString(),
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString(),
        isDeleted = this.isDeleted
    )
}