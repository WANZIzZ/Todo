package com.wanzi.todo.bean

import java.io.Serializable

/**
 * 作者：Created by WZ on 2018-08-13 15:57
 * 邮箱：1253427499@qq.com
 */
data class TodoDetail(
        val completeDate: Long?,
        val completeDateStr: String,
        val content: String,
        val date: Long,
        val dateStr: String,
        val id: Int,
        val status: Int,
        val title: String,
        val type: Int,
        val userId: Int
) : Serializable