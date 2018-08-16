package com.wanzi.todo.bean

/**
 * 作者：Created by WZ on 2018-08-06 16:39
 * 邮箱：1253427499@qq.com
 */
data class HttpResult<T>(
        val errorMsg: String,
        val errorCode: Int,
        val data: T?
)