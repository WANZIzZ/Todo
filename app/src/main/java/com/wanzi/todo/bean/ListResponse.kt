package com.wanzi.todo.bean

/**
 * 作者：Created by WZ on 2018-08-10 10:10
 * 邮箱：1253427499@qq.com
 */
data class ListResponse(
        val curPage: Int,
        val datas: List<TodoDetail>,
        val offset: Int,
        val over: Boolean,
        val pageCount: Int,
        val size: Int,
        val total: Int
)