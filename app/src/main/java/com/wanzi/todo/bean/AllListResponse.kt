package com.wanzi.todo.bean

/**
 * 作者：Created by WZ on 2018-08-08 13:58
 * 邮箱：1253427499@qq.com
 */
data class AllListResponse(
        val type: Int,
        val doneList: List<DoneOrTodoList>,
        val todoList: List<DoneOrTodoList>
) {
    data class DoneOrTodoList(
            val date: Long,
            val todoList: List<TodoDetail>
    )
}