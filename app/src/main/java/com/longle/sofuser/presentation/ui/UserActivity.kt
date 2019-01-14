package com.longle.sofuser.presentation.ui

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.longle.sofuser.GlideApp
import com.longle.sofuser.R
import com.longle.sofuser.presentation.ServiceLocator
import com.longle.sofuser.presentation.repository.NetworkState
import com.longle.sofuser.presentation.vo.User
import kotlinx.android.synthetic.main.activity_user.*


/**
 * A list activity that shows sof users.
 */
class UserActivity : AppCompatActivity() {

    private lateinit var model: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        model = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        model.showUsers()
    }

    private fun getViewModel(): UserViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@UserActivity)
                        .getRepository()
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(repo) as T
            }
        })[UserViewModel::class.java]
    }

    private fun initAdapter() {
        list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val glide = GlideApp.with(this)
        val adapter = UserAdapter(glide, retryCallback = {
            model.retry()
        }, bookmarkMethod = this::bookmarkCallback)
        list.adapter = adapter
        model.users.observe(this, Observer<PagedList<User>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            model.refresh()
        }
    }

    private fun bookmarkCallback(isChecked: Boolean, user: User?) {
        model.bookmarkMethod(isChecked, user)
    }
}
