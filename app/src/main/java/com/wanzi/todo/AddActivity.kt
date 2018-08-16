package com.wanzi.todo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.AdapterView
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.wanzi.todo.base.BaseActivity
import com.wanzi.todo.bean.TodoDetail
import com.wanzi.todo.config.Config
import com.wanzi.todo.network.HttpResultFunction
import com.wanzi.todo.network.HttpSender
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add.*
import java.util.*

class AddActivity : BaseActivity() {

    private var currentType = Config.TYPE_0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        initView()
    }

    private fun initView() {
        findViewById<Toolbar>(R.id.toolbar).run {
            setSupportActionBar(this)
        }

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = "添加Todo"
        }

        tv_date.run {
            text = getDate()
            setOnClickListener {
                setDate()
            }
        }

        fab_add.run {
            setOnClickListener {
                if (checkContent()) {
                    addTodo()
                }
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                currentType = p2
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDate() {
        DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { datePicker, var2, var3, var4 ->
                    val month =
                            if ((var3 + 1) < 10)
                                "0${var3 + 1}"
                            else
                                "${var3 + 1}"

                    val day =
                            if (var4 < 10)
                                "0$var4"
                            else
                                "$var4"

                    tv_date.text = "$var2-$month-$day"
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ).run {
            datePicker.minDate = Date().time
            show()
        }
    }

    private fun checkContent(): Boolean {
        til_title.error = null
        til_content.error = null

        var cancel = false

        var focusView: View? = null

        val title = et_title.text.toString()

        if (title.isEmpty()) {
            til_title.error = "标题不能为空"
            focusView = til_title
            cancel = true
        }

        return if (cancel) {
            focusView?.requestFocus()
            false
        } else {
            true
        }
    }

    private fun addTodo() {
        HttpSender.instances.add(et_title.text.toString(), et_content.text.toString(), tv_date.text.toString(), currentType)
                .map(HttpResultFunction())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(object : Observer<TodoDetail> {
                    override fun onComplete() {
                        enableInteraction()
                        progressBar.visibility = View.GONE

                        setResult(Config.MAIN_ADD_REQUEST_CODE, Intent().putExtra(Config.INTENT_NAME_TYPE, currentType))
                        finish()
                    }

                    override fun onSubscribe(d: Disposable) {
                        disableInteraction()
                        progressBar.visibility = View.VISIBLE
                    }

                    override fun onNext(t: TodoDetail) {
                        toast("添加Todo成功！")
                    }

                    override fun onError(e: Throwable) {
                        enableInteraction()
                        progressBar.visibility = View.GONE
                        toast("添加Todo失败，:${e.message}")
                    }

                })
    }

}
