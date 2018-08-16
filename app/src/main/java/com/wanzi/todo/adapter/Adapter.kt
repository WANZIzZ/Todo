package com.wanzi.todo.adapter

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wanzi.todo.R
import com.wanzi.todo.bean.TodoDetail

/**
 * 作者：Created by WZ on 2018-08-10 14:23
 * 邮箱：1253427499@qq.com
 */
class Adapter(data: List<TodoDetail>) : BaseItemDraggableAdapter<TodoDetail, BaseViewHolder>(R.layout.item, data) {

    override fun convert(helper: BaseViewHolder, item: TodoDetail?) {
        item ?: return
        helper.setText(R.id.tv_item_date, item.dateStr)
                .setText(R.id.tv_item_title, item.title)
                .setText(R.id.tv_item_content, item.content)
                .setText(R.id.tv_item_complete_date, "完成：${item.completeDateStr}")

        // 当完成日期为空时，隐藏完成日期
        helper.getView<TextView>(R.id.tv_item_complete_date).visibility =
                if (item.completeDate == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

        // 当相邻两条记录为同一天时，隐藏后一条记录的日期
        helper.getView<TextView>(R.id.tv_item_date).visibility = if (helper.layoutPosition > 0) {
            if (item.dateStr == data[helper.layoutPosition - 1].dateStr) {
                View.GONE
            } else {
                View.VISIBLE
            }
        } else {
            View.VISIBLE
        }
    }
}