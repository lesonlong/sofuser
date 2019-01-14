package com.longle.sofuser.presentation.repository

import com.longle.sofuser.presentation.vo.User

/**
 * Common interface shared by the different repository implementations.
 * Note: this only exists for sample purposes - typically an app would implement a repo once, either
 * network+db, or network-only
 */
interface UserRepository {
    fun userList(): Listing<User>
    fun saveBookmard(bookmarked: Boolean, user: User?)
}