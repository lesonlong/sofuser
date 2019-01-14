package com.longle.sofuser.presentation.repository.inDb

import androidx.paging.PagedList
import androidx.annotation.MainThread
import com.longle.sofuser.paging.PagingRequestHelper
import com.longle.sofuser.presentation.api.SofUserApi
import com.longle.sofuser.presentation.util.createStatusLiveData
import com.longle.sofuser.presentation.vo.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class UserBoundaryCallback(
    private val website: String,
    private val webservice: SofUserApi,
    private val handleResponse: (SofUserApi.ListingResponse?) -> Unit,
    private val ioExecutor: Executor,
    private val networkPageSize: Int)
    : PagedList.BoundaryCallback<User>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice.getUsers(
                page = 1,
                pageSize = networkPageSize,
                site = website)
                .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: User) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice.getUsers(
                page = itemAtEnd.page + 1,
                pageSize = networkPageSize,
                site = website)
                .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        response: Response<SofUserApi.ListingResponse>,
        it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            handleResponse(response.body())
            it.recordSuccess()
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: User) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<SofUserApi.ListingResponse> {
        return object : Callback<SofUserApi.ListingResponse> {
            override fun onFailure(
                call: Call<SofUserApi.ListingResponse>,
                t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                call: Call<SofUserApi.ListingResponse>,
                response: Response<SofUserApi.ListingResponse>) {
                insertItemsIntoDb(response, it)
            }
        }
    }
}