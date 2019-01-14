package com.longle.sofuser.presentation.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.longle.sofuser.presentation.vo.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts : List<User>)

    @Query("SELECT * FROM users ORDER BY indexInResponse ASC")
    fun users() : DataSource.Factory<Int, User>

//    @Query("SELECT * FROM users ORDER BY indexInResponse ASC")
//    fun usersByBookmarked() : DataSource.Factory<Int, User>

    @Query("DELETE FROM users")
    fun deleteAll()

    @Query("SELECT * FROM users ORDER BY indexInResponse DESC LIMIT 1")
    fun getLastUser() : User?


    @Query("UPDATE users SET bookmarked = :bookmarked WHERE indexInResponse =:indexInResponse")
    fun update(bookmarked: Boolean, indexInResponse: Int)
}