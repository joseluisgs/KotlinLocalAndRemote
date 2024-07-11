package dev.joseluisgs.notification

import java.time.LocalDateTime

data class Notification<T>(
    val type: Type,
    val item: T? = null,
    val message: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    enum class Type {
        CREATE, UPDATE, DELETE, REFRESH
    }
}
