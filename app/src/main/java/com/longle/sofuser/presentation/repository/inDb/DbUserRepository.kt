package com.longle.sofuser.presentation.repository.inDb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.annotation.MainThread
import androidx.paging.toLiveData
import com.longle.sofuser.presentation.api.SofUserApi
import com.longle.sofuser.presentation.db.UserDb
import com.longle.sofuser.presentation.repository.Listing
import com.longle.sofuser.presentation.repository.NetworkState
import com.longle.sofuser.presentation.repository.UserRepository
import com.longle.sofuser.presentation.vo.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 */
class DbUserRepository(
    val db: UserDb,
    private val userApi: SofUserApi,
    private val ioExecutor: Executor,
    private val networkPageSize: Int = DEFAULT_NETWORK_PAGE_SIZE,
    private val website: String = WEBSITE) : UserRepository {

    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 30
        private const val WEBSITE = "stackoverflow"
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertResultIntoDb(body: SofUserApi.ListingResponse?) {
        body!!.items.let { posts ->
            db.runInTransaction {
                val user = db.users().getLastUser()
                val items = posts.mapIndexed { index, child ->
                    child.indexInResponse = (user?.indexInResponse ?: 0) + index
                    child.page = (user?.page ?: 0) + 1
                    child
                }
                db.users().insert(items)
            }
        }
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        userApi.getUsers(1, networkPageSize, website).enqueue(
            object : Callback<SofUserApi.ListingResponse> {
                override fun onFailure(call: Call<SofUserApi.ListingResponse>, t: Throwable) {
                    // retrofit calls this on main thread so safe to call set value
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(
                    call: Call<SofUserApi.ListingResponse>,
                    response: Response<SofUserApi.ListingResponse>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            db.users().deleteAll()
                            insertResultIntoDb(response.body())
                        }
                        // since we are in bg thread now, post the result.
                        networkState.postValue(NetworkState.LOADED)
                    }
                }
            }
        )
        return networkState
    }

    /**
     * Returns a Listing for the given user.
     */
    @MainThread
    override fun userList(): Listing<User> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = UserBoundaryCallback(
            webservice = userApi,
            website = website,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor,
            networkPageSize = networkPageSize)
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refresh()
        }

        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
        val livePagedList = db.users().users().toLiveData(
            pageSize = 30,
            boundaryCallback = boundaryCallback)

        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    override fun saveBookmard(bookmarked: Boolean, user: User?) {
        ioExecutor.execute {
            db.runInTransaction {
                user?.let { db.users().update(bookmarked, user.indexInResponse) }
            }
        }
    }
}

