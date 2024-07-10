package dev.joseluisgs.di

import database.SqlDeLightManager
import database.createDatabase
import dev.joseluisgs.cache.TENISTAS_CACHE_SIZE
import dev.joseluisgs.cache.TenistasCache
import dev.joseluisgs.cache.TenistasCacheImpl
import dev.joseluisgs.notification.TenistasNotifications
import dev.joseluisgs.repository.TenistasRepositoryLocal
import dev.joseluisgs.repository.TenistasRepositoryRemote
import dev.joseluisgs.rest.API_TENISTAS_URL
import dev.joseluisgs.rest.TenistasApiRest
import dev.joseluisgs.rest.getKtorFitClient
import dev.joseluisgs.service.REFRESH_TIME
import dev.joseluisgs.service.TenistasServiceImpl
import dev.joseluisgs.storage.TenistasStorageCsv
import dev.joseluisgs.storage.TenistasStorageJson
import org.koin.dsl.module

val appModule = module {
    // Cache
    single<TenistasCache> { TenistasCacheImpl(getProperty("cache.size", TENISTAS_CACHE_SIZE)) }

    // Storage
    single { TenistasStorageCsv() }
    single { TenistasStorageJson() }

    // Database
    single { createDatabase(getProperty("database.name", "tenistas.db")) }
    single { SqlDeLightManager(get()) }

    // ApiRest
    single { getKtorFitClient(getProperty("api.rest", API_TENISTAS_URL)).create<TenistasApiRest>() }

    // Repositories
    single { TenistasRepositoryLocal(get()) }
    single { TenistasRepositoryRemote(get()) }

    // Notifications
    single { TenistasNotifications() }

    // Service
    single {
        TenistasServiceImpl(
            localRepository = get(),
            remoteRepository = get(),
            cache = get(),
            csvStorage = get(),
            jsonStorage = get(),
            notificationsService = get(),
            autoRefresh = getProperty("auto.refresh", REFRESH_TIME)
        )
    }

}