package dev.joseluisgs.cache

import dev.joseluisgs.model.Tenista

/**
 * Implementación de una cache simple para Tenistas basada en la Generica
 * Con un tamaño máximo siguiendo el principio de LRU (Least Recently Used) o el primero que entra es el primero que sale (FIFO)
 * @param size Tamaño máximo de la cache
 */

class TenistasCacheImpl(
    private val size: Int = 10
) : CacheGeneric<Long, Tenista>(size), TenistasCache
