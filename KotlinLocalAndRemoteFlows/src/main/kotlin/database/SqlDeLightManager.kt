package database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.joseluisgs.database.AppDatabase
import org.lighthousegames.logging.logging

private val logger = logging()

class SqlDeLightManager(
    val queries: DatabaseQueries
)

fun createInMemoryDatabase(): DatabaseQueries {
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).let { driver ->
        AppDatabase.Schema.create(driver)
        AppDatabase(driver)
    }.databaseQueries
}

fun createDatabase(path: String = "database.db"): DatabaseQueries {
    return JdbcSqliteDriver("jdbc:sqlite:$path").let { driver ->
        AppDatabase.Schema.create(driver)
        AppDatabase(driver)
    }.databaseQueries
}

