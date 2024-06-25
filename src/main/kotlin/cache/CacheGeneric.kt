package dev.joseluisgs.cache

import org.lighthousegames.logging.logging

private val logger = logging()

/**
 * Implementación de una cache simple
 * Con un tamaño máximo siguiendo el principio de LRU (Least Recently Used
 * @param size Tamaño máximo de la cache
 * @param K Tipo de la clave
 * @param T Tipo del valor
 */
open class CacheGeneric<K, T>(
    private val cacheSize: Int = 10
) : Cache<K, T> {
    // LinkedHashMap se inicializa con el flag true para el orden de acceso, lo que mantiene el orden basado en la última vez que se accedió a un elemento.
    // La función removeEldestEntry se anula para eliminar el elemento más antiguo cuando la LinkedHashMap excede el tamaño especificado.
    // La utilización de object en este contexto permite la creación concisa de una instancia anónima que sobrescribe el comportamiento de LinkedHashMap según tus necesidades específicas,
    // sin la sobrecarga de tener que definir una clase nueva por separado. Esto resulta en un código más limpio y fácil de mantener.
    private val cache = object : LinkedHashMap<K, T>(cacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, T>?): Boolean {
            return this.size > cacheSize
        }
    }

    override fun get(key: K): T? {
        logger.debug { "Obteniendo el valor de la clave: $key" }
        return cache[key]
    }


    override fun put(key: K, value: T) {
        logger.debug { "Añadiendo el valor de la clave: $key" }
        cache[key] = value
    }

    override fun remove(key: K) {
        logger.debug { "Eliminando el valor de la clave: $key" }
        cache.remove(key)
    }

    override fun clear() {
        logger.debug { "Limpiando la cache" }
        cache.clear()
    }

    override fun size(): Int {
        logger.debug { "Obteniendo el tamaño de la cache" }
        return cache.size
    }

    override fun keys(): Set<K> {
        logger.debug { "Obteniendo las claves de la cache" }
        return cache.keys
    }

    override fun values(): Collection<T> {
        logger.debug { "Obteniendo los valores de la cache" }
        return cache.values
    }

    override fun containsKey(key: K): Boolean {
        logger.debug { "Comprobando si existe la clave en la cache: $key" }
        return cache.containsKey(key)
    }

    override fun containsValue(value: T): Boolean {
        logger.debug { "Comprobando si existe el valor en la cache: $value" }
        return cache.containsValue(value)
    }


    override fun isEmpty(): Boolean {
        logger.debug { "Comprobando si la cache está vacía" }
        return cache.isEmpty()
    }

    override fun isNotEmpty(): Boolean {
        logger.debug { "Comprobando si la cache no está vacía" }
        return cache.isNotEmpty()
    }
}