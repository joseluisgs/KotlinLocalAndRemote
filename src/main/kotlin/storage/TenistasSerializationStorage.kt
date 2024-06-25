package dev.joseluisgs.storage

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista

interface TenistasSerializationStorage : SerializationStorage<Tenista, TenistaError>