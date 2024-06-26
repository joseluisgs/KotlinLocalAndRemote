package database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.joseluisgs.database.AppDatabase
import org.lighthousegames.logging.logging

private val logger = logging()

class SqlDeLightManager {
    val databaseQueries: DatabaseQueries by lazy { initQueries() }

    private fun initQueries(): DatabaseQueries {
        logger.debug { "SqlDeLightClient - InMemory" }
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).let { driver ->
            // Creamos la base de datos
            logger.debug { "Creando Tablas (si es necesario)" }
            AppDatabase.Schema.create(driver)
            AppDatabase(driver)
        }.databaseQueries
    }

}