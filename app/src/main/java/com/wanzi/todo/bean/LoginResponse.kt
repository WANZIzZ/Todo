package com.wanzi.todo.bean

/**
 * 作者：Created by WZ on 2018-08-06 14:55
 * 邮箱：1253427499@qq.com
 */
data class LoginResponse(
        val collectIds: List<Int>,
        val email: String,
        val icon: String,
        val id: Int,
        val password: String,
        val type: Int,
        val username: String
)