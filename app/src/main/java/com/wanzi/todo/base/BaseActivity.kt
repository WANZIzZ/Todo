package com.wanzi.todo.base

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * 作者：Created by WZ on 2018-08-07 11:02
 * 邮箱：1253427499@qq.com
 */
abstract class BaseActivity : AppCompatActivity() {

    /**
     * 隐藏键盘
     */
    protected fun hideKeyboard() {
        currentFocus?.let {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * 禁止交互
     */
    protected fun disableInteraction() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * 允许交互
     */
    protected fun enableInteraction() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}