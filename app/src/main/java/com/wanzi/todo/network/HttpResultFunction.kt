package com.wanzi.todo.network

import com.wanzi.todo.bean.HttpResult
import io.reactivex.functions.Function

/**
 * 作者：Created by WZ on 2018-08-06 16:47
 * 邮箱：1253427499@qq.com
 */
class HttpResultFunction<T> : Function<HttpResult<T>, T> {

    override fun apply(t: HttpResult<T>): T? {
        if (t.errorCode < 0) {
            throw ApiException(t.errorMsg)
        }
        return t.data
    }

    class ApiException constructor(message: String) : Exception(message)
}