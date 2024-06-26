package dev.joseluisgs.repository

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista

interface TenistasRepository : Respository<Long, Tenista, TenistaError>