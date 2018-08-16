package com.wanzi.todo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
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
import kotlinx.android.synthetic.main.activity_update.*
import java.util.*

class UpdateActivity : BaseActivity() {

    private lateinit var todoDetail: TodoDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        todoDetail = intent.getSerializableExtra(Config.INTENT_NAME_TODODETAIL) as TodoDetail

        initView()
    }

    private fun initView() {
        findViewById<Toolbar>(R.id.toolbar).run {
            setSupportActionBar(this)
        }

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = "更新Todo"
        }

        tv_date.run {
            text = todoDetail.dateStr
            setOnClickListener {
                setDate()
            }
        }

        fab_add.run {
            setOnClickListener {
                if (checkContent()) {
                    update()
                }
            }
        }

        et_title.setText(todoDetail.title)
        et_content.setText(todoDetail.content)
    }

    private fun update() {
        HttpSender.instances.update(todoDetail.id, et_title.text.toString(), et_content.text.toString(), tv_date.text.toString(),
                todoDetail.status, todoDetail.type)
                .map(HttpResultFunction())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(object : Observer<TodoDetail> {
                    override fun onComplete() {
                        enableInteraction()
                        progressBar.visibility = View.GONE

                        setResult(Config.MAIN_UPDATE_REQUEST_CODE)
                        finish()
                    }

                    override fun onSubscribe(d: Disposable) {
                        disableInteraction()
                        progressBar.visibility = View.VISIBLE
                    }

                    override fun onNext(t: TodoDetail) {
                        toast("更新Todo成功！")
                    }

                    override fun onError(e: Throwable) {
                        enableInteraction()
                        progressBar.visibility = View.GONE
                        toast("更新Todo失败，:${e.message}")
                    }

                })
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
}
