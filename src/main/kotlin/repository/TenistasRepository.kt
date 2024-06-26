package dev.joseluisgs.repository

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import java.util.*

interface TenistasRepository : Respository<UUID, Tenista, TenistaError>