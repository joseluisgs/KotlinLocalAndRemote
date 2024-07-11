package dev.joseluisgs.storage

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.model.Tenista

interface TenistasStorage : SerializationStorage<Tenista, TenistaError.StorageError>

