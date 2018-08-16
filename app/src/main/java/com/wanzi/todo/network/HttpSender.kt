package com.wanzi.todo.network

import com.google.gson.Gson
import com.wanzi.todo.base.Preferences
import com.wanzi.todo.config.Config
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 作者：Created by WZ on 2018-08-06 15:09
 * 邮箱：1253427499@qq.com
 */
class HttpSender private constructor() {

    private val SAVE_USER_LOGIN_KEY = "user/login"

    private var api: API

    companion object {
        val instances: API by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HttpSender().api
        }
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    private val client = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(15L, TimeUnit.SECONDS)
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>?) {
                    url ?: return
                    cookies ?: return

                    if (url.toString().contains(SAVE_USER_LOGIN_KEY)) {
                        val cookieJson = Gson().toJson(cookies.map { it.toString() })
                        var cookie by Preferences(url.host(), cookieJson)
                        @Suppress("UNUSED_VALUE")
                        cookie = cookieJson
                    }
                }

                override fun loadForRequest(url: HttpUrl?): MutableList<Cookie> {
                    url ?: return ArrayList()

                    val cookie by Preferences(url.host(), "")
                    if (cookie.isEmpty()) return ArrayList()

                    return Gson().fromJson(cookie, ArrayList<String>()::class.java)
                            .map {
                                Cookie.parse(url, it)!!
                            } as MutableList
                }

            })
            .build()!!

    init {
        api = Retrofit
                .Builder()
                .baseUrl(Config.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(API::class.java)
    }
}