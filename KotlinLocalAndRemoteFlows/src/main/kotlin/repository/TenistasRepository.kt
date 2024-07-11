package dev.joseluisgs.repository

import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.model.Tenista

interface TenistasRepository : Respository<Long, Tenista, TenistaError>