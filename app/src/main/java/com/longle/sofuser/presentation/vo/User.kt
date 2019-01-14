package com.longle.sofuser.presentation.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "users",
    indices = [Index(value = ["page"], unique = false)]
)
data class User(
    @PrimaryKey
    @SerializedName("display_name")
    val display_name: String,
    @SerializedName("profile_image")
    val profile_image: String,
    @SerializedName("reputation")
    val reputation: Int,
    @SerializedName("bookmarked")
    var bookmarked: Boolean,
    @SerializedName("page")
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    var page: Int
) {
    var indexInResponse: Int = -1
}