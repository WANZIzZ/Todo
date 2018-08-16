package com.wanzi.todo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.wanzi.todo.config.Config
import okhttp3.Cookie
import java.text.SimpleDateFormat
import java.util.*

/**
 * 作者：Created by WZ on 2018-08-06 15:36
 * 邮箱：1253427499@qq.com
 */

fun log(content: String) {
    Log.i("Wanzi", content)
}

fun Context.toast(content: String) {
    Config.showToast?.apply {
        setText(content)
        show()
    } ?: apply {
        Toast.makeText(this.applicationContext, content, Toast.LENGTH_SHORT).apply {
            Config.showToast = this
        }.show()
    }
}

fun getDate(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    return simpleDateFormat.format(Date().time)
}
