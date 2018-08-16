package com.wanzi.todo

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import com.jakewharton.rxbinding2.widget.RxTextView
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.wanzi.todo.base.BaseActivity
import com.wanzi.todo.bean.LoginResponse
import com.wanzi.todo.network.HttpResultFunction
import com.wanzi.todo.network.HttpSender
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initToolbar()
        initView()
    }

    private fun initToolbar() {
        findViewById<Toolbar>(R.id.toolbar).run {
            setSupportActionBar(this)
        }

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = "注册"
        }
    }

    private fun initView() {
        RxTextView.textChanges(et_user).subscribe { til_user.error = "" }
        RxTextView.textChanges(et_password).subscribe { til_password.error = "" }
        RxTextView.textChanges(et_verify_password).subscribe { til_verify_password.error = "" }
    }

    private fun checkContent(): Boolean {
        til_user.error = null
        til_password.error = null
        til_verify_password.error = null

        var cancel = false

        var focusView: View? = null

        val user = et_user.text.toString()
        val password = et_password.text.toString()
        val verifyPassword = et_verify_password.text.toString()

        if (user.isEmpty()) {
            til_user.error = "用户名不能为空"
            focusView = til_user
            cancel = true
        } else if (user.length < 6) {
            til_user.error = "用户名不能低于六位"
            focusView = til_user
            cancel = true
        }

        if (password.isEmpty()) {
            til_password.error = "密码不能为空"
            focusView = til_password
            cancel = true
        } else if (password.length < 6) {
            til_password.error = "密码不能低于六位"
            focusView = til_password
            cancel = true
        }

        if (verifyPassword.isEmpty()) {
            til_verify_password.error = "重复密码不能为空"
            focusView = til_verify_password
            cancel = true
        } else if (verifyPassword != password) {
            til_verify_password.error = "两次密码不相同"
            focusView = til_verify_password
            cancel = true
        }

        return if (cancel) {
            focusView?.requestFocus()
            false
        } else {
            true
        }
    }

    fun register(view: View) {
        hideKeyboard()

        if (checkContent()) {
            val user = et_user.text.toString()
            val password = et_password.text.toString()
            val verifyPassword = et_verify_password.text.toString()

            HttpSender.instances.register(user, password, verifyPassword)
                    .map(HttpResultFunction())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                    .subscribe(object : Observer<LoginResponse> {
                        override fun onComplete() {
                            progressBar.visibility = View.GONE
                            finish()
                        }

                        override fun onSubscribe(d: Disposable) {
                            progressBar.visibility = View.VISIBLE
                        }

                        override fun onNext(t: LoginResponse) {
                            toast("注册成功！")
                        }

                        override fun onError(e: Throwable) {
                            progressBar.visibility = View.GONE

                            when (e.message) {
                                "用户名已经被注册！" -> {
                                    til_user.error = e.message
                                }
                                "账号或者密码长度必须大于6位！" -> {
                                    if (user.length < 6 && password.length < 6) {
                                        til_user.error = e.message
                                        til_password.error = e.message
                                    } else if (user.length < 6) {
                                        til_user.error = e.message
                                    } else {
                                        til_password.error = e.message
                                    }
                                }
                                "两次输入的密码不一致！" -> {
                                    til_verify_password.error = e.message
                                }
                                else -> {
                                    toast("注册失败:${e.message}")
                                }
                            }
                        }
                    })
        }
    }
}
