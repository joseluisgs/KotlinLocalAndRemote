package dev.joseluisgs.notifications

interface Notifications<T> {
    suspend fun sendNotification(notification: Notification<T>)
}