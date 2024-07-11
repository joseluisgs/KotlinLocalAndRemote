package dev.joseluisgs.cache

interface Cache<K, T> {
    fun get(key: K): T?
    fun put(key: K, value: T)
    fun remove(key: K)
    fun clear()
    fun size(): Int
    fun keys(): Set<K>
    fun values(): Collection<T>
    fun containsKey(key: K): Boolean
    fun containsValue(value: T): Boolean
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
}