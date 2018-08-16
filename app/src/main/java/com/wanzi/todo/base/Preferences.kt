package com.wanzi.todo.base

import android.content.Context
import android.content.SharedPreferences
import com.wanzi.todo.config.Config
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
/**
 * 作者：Created by WZ on 2018-08-08 14:54
 * 邮箱：1253427499@qq.com
 */
class Preferences<T>(private val name: String, private val default: T) : ReadWriteProperty<Any?, T> {

    companion object {

        lateinit var preferences: SharedPreferences

        fun setContext(context: Context) {
            preferences = context.getSharedPreferences(
                    Config.SHARED_NAME,
                    Context.MODE_PRIVATE
            )
        }

        fun clear() {
            preferences.edit().clear().apply()
        }
    }

    // getValue函数接收一个类的引用和一个属性的元数据
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = findPreferences(name, default)

    // setValue函数接收了一个被设置的值，如果这个属性是不可修改（val），就会只有一个getValue函数
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = putPreferences(name, value)

    private fun <U> findPreferences(name: String, default: U): U = preferences.run {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("类型错误")
        }
        res as U
    }

    private fun <U> putPreferences(name: String, value: U) = preferences.edit().run {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("类型错误")
        }.apply()
    }
}