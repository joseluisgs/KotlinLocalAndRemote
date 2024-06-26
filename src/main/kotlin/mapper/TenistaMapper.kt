package dev.joseluisgs.mapper

import database.TenistaEntity
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

/**
 * Mapeo de TenistaEntity a Tenista usando funciones de extensión
 * En base a los campos de TenistaEntity se crea un Tenista
 */
fun TenistaEntity.toTenista(): Tenista {
    return Tenista(
        id = java.util.UUID.fromString(this.id),
        nombre = this.nombre,
        pais = this.pais,
        altura = this.altura.toInt(),
        peso = this.peso.toInt(),
        puntos = this.puntos.toInt(),
        mano = Tenista.Mano.valueOf(this.mano),
        fechaNacimiento = java.time.LocalDate.parse(this.fechaNacimiento),
        createdAt = parse(this.createdAt),
        updatedAt = parse(this.updatedAt),
        isDeleted = this.isDeleted.toInt() == 1
    )
}

/**
 * Mapeo de Tenista a TenistaEntity usando funciones de extensión
 * En base a los campos de Tenista se crea un TenistaEntity
 */
fun Tenista.toTenistaEntity(): TenistaEntity {
    return TenistaEntity(
        id = this.id.toString(),
        nombre = this.nombre,
        pais = this.pais,
        altura = this.altura.toLong(),
        peso = this.peso.toLong(),
        puntos = this.puntos.toLong(),
        mano = this.mano.name,
        fechaNacimiento = this.fechaNacimiento.toString(),
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString(),
        isDeleted = if (this.isDeleted) 1L else 0L
    )
}
