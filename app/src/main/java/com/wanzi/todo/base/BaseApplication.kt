package com.wanzi.todo.base

import android.app.Application

/**
 * 作者：Created by WZ on 2018-08-08 15:19
 * 邮箱：1253427499@qq.com
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Preferences.setContext(applicationContext)
    }
}