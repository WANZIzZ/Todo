package com.wanzi.todo

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import com.jakewharton.rxbinding2.widget.RxTextView
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.wanzi.todo.base.BaseActivity
import com.wanzi.todo.base.Preferences
import com.wanzi.todo.bean.LoginResponse
import com.wanzi.todo.config.Config
import com.wanzi.todo.network.HttpResultFunction
import com.wanzi.todo.network.HttpSender
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    private var login: Boolean by Preferences(Config.LOGIN_KEY, false)

    private var username: String by Preferences(Config.USERNAME_KEY, "")
    private var password: String by Preferences(Config.PASSWORD_KEY, "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initToolbar()
        initView()

        if (login) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun initToolbar() {
        findViewById<Toolbar>(R.id.toolbar).title = "登录"
    }

    private fun initView() {
        RxTextView.textChanges(et_user).subscribe { til_user.error = null }
        RxTextView.textChanges(et_password).subscribe { til_password.error = null }

        et_user.setText(username)
        et_password.setText(password)
    }

    fun toRegister(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun checkContent(): Boolean {
        til_user.error = null
        til_password.error = null

        var cancel = false

        var focusView: View? = null

        val user = et_user.text.toString()
        val password = et_password.text.toString()

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

        return if (cancel) {
            focusView?.requestFocus()
            false
        } else {
            true
        }
    }

    fun login(view: View) {
        hideKeyboard()

        if (checkContent()) {
            username = et_user.text.toString()
            password = et_password.text.toString()

            HttpSender.instances.login(username, password)
                    .map(HttpResultFunction())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                    .subscribe(object : Observer<LoginResponse> {
                        override fun onComplete() {
                            progressBar.visibility = View.GONE
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }

                        override fun onSubscribe(d: Disposable) {
                            progressBar.visibility = View.VISIBLE
                        }

                        override fun onNext(t: LoginResponse) {
                            toast("登录成功")
                            login = true
                        }

                        override fun onError(e: Throwable) {
                            progressBar.visibility = View.GONE
                            if ("账号密码不匹配！" == e.message) {
                                til_user.error = e.message
                                til_password.error = e.message
                            } else {
                                toast("登录失败:${e.message}")
                            }
                        }

                    })
        }
    }
}
