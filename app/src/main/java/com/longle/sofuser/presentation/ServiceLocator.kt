package com.longle.sofuser.presentation

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.longle.sofuser.presentation.api.SofUserApi
import com.longle.sofuser.presentation.db.UserDb
import com.longle.sofuser.presentation.repository.UserRepository
import com.longle.sofuser.presentation.repository.inDb.DbUserRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */
interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                        app = context.applicationContext as Application,
                        useInMemoryDb = false
                    )
                }
                return instance!!
            }
        }

        /**
         * Allows tests to replace the default implementations.
         */
        @VisibleForTesting
        fun swap(locator: ServiceLocator) {
            instance = locator
        }
    }

    fun getRepository(): UserRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getUserApi(): SofUserApi
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application, val useInMemoryDb: Boolean) : ServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val db by lazy {
        UserDb.create(app, useInMemoryDb)
    }

    private val api by lazy {
        SofUserApi.create()
    }

    override fun getRepository(): UserRepository {
        return DbUserRepository(
            db = db,
            userApi = getUserApi(),
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getUserApi(): SofUserApi = api
}