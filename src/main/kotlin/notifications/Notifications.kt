package dev.joseluisgs.notifications

interface Notifications<T> {
    suspend fun send(notification: Notification<T>)
}