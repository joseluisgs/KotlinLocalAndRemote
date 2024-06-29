package dev.joseluisgs.cache

import dev.joseluisgs.models.Tenista


const val TENISTAS_CACHE_SIZE = 5

interface TenistasCache : Cache<Long, Tenista>