package com.wanzi.todo.config

import android.widget.Toast

/**
 * 作者：Created by WZ on 2018-08-06 15:12
 * 邮箱：1253427499@qq.com
 */
object Config {

    /**
     * baseUrl
     */
    val BASE_URL = "http://www.wanandroid.com/"

    /**
     * toast
     */
    var showToast: Toast? = null

    /**
     * Share preferences name
     */
    val SHARED_NAME = "preferences"
    val LOGIN_KEY = "login"
    val USERNAME_KEY = "username"
    val PASSWORD_KEY = "password"


    /**
     * NavigationView header imageView
     */
    val NAV_HEADER_IMG = "https://pic.qqtn.com/up/2016-7/2016071115340721509.gif"

    /**
     * type0
     */
    val TYPE_0 = 0 // 只用这一个
    val TYPE_1 = 1 // 工作
    val TYPE_2 = 2 // 学习
    val TYPE_3 = 3 // 生活

    val MAIN_ADD_REQUEST_CODE = 100
    val MAIN_UPDATE_REQUEST_CODE = 101

    val INTENT_NAME_TYPE = "type"
    val INTENT_NAME_TODODETAIL = "todoDetail"

    val BUNDLE_KEY_DONE = "done"
}