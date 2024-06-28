package dev.joseluisgs.notifications

import java.time.LocalDateTime

data class Notification<T>(
    val type: Type,
    val item: T,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    enum class Type {
        CREATE, UPDATE, DELETE, ERROR, REFRESH
    }
}
