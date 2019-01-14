package com.longle.sofuser.presentation.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import com.longle.sofuser.presentation.repository.UserRepository
import com.longle.sofuser.presentation.vo.User

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val currPage = MutableLiveData<Boolean>()
    private val repoResult = map(currPage) {
        repository.userList()
    }
    val users = switchMap(repoResult) { it.pagedList }!!
//    val usersBookmarked = switchMap(repoResult) { it.pagedBookmarkedList }!!
    val networkState = switchMap(repoResult) { it.networkState }!!
    val refreshState = switchMap(repoResult) { it.refreshState }!!

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun showUsers() {
        currPage.value = true
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun bookmarkMethod(bookmarked: Boolean, user: User?) {
        repository.saveBookmard(bookmarked, user)
    }
}
