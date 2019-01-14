package com.longle.sofuser.presentation.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.longle.sofuser.presentation.vo.User

/**
 * Database schema used by the DbUserRepository
 */
@Database(
        entities = arrayOf(User::class),
        version = 1,
        exportSchema = false
)
abstract class UserDb : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory : Boolean): UserDb {
            val databaseBuilder = if(useInMemory) {
                Room.inMemoryDatabaseBuilder(context, UserDb::class.java)
            } else {
                Room.databaseBuilder(context, UserDb::class.java, "user.db")
            }
            return databaseBuilder
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }

    abstract fun users(): UserDao
}