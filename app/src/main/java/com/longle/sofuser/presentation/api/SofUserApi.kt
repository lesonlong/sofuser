package com.longle.sofuser.presentation.api

import android.util.Log
import com.longle.sofuser.presentation.vo.User
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * API communication setup
 */
interface SofUserApi {
    @GET("/users")
    @Headers("Accept:application/json;charset=utf-t", "Accept-Language:en")
    fun getUsers(
        @Query("page") page: Int,
        @Query("pagesize") pageSize: Int,
        @Query("site") site: String): Call<ListingResponse>

    @GET("/users/{userid}/reputation-history")
    fun getUserReputation(
        @Path("userid") userId: String,
        @Query("page") page: Int,
        @Query("pagesize") pageSize: Int,
        @Query("site") site: String): Call<ListingResponse>

    class ListingResponse(val items: List<User>)

    companion object {
        private const val BASE_URL = "https://api.stackexchange.com/2.2/"
        fun create(): SofUserApi = create(HttpUrl.parse(BASE_URL)!!)
        fun create(httpUrl: HttpUrl): SofUserApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SofUserApi::class.java)
        }
    }
}