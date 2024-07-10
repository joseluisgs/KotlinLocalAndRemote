package dev.joseluisgs.notification

interface Notifications<T> {
    suspend fun send(notification: Notification<T>)
}