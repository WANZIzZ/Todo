package com.wanzi.todo.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.wanzi.todo.MainActivity
import com.wanzi.todo.R
import com.wanzi.todo.UpdateActivity
import com.wanzi.todo.adapter.Adapter
import com.wanzi.todo.bean.ListResponse
import com.wanzi.todo.bean.TodoDetail
import com.wanzi.todo.config.Config
import com.wanzi.todo.network.HttpResultFunction
import com.wanzi.todo.network.HttpSender
import com.wanzi.todo.toast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment.*

/**
 * 作者：Created by WZ on 2018-08-09 16:20
 * 邮箱：1253427499@qq.com
 */
class MainFragment @SuppressLint("ValidFragment")
private constructor() : Fragment() {

    private var DONE = false

    var currentPage = 1                // 当前页，从1开始
    var currentType = Config.TYPE_0    // 当前类型
    var isOver = true                  // 是否结束（没有下一页）

    val data = ArrayList<TodoDetail>()

    val adapter: Adapter by lazy {
        Adapter(data)
    }

    companion object {
        fun newInstance(done: Boolean): MainFragment {
            val bundle = Bundle()
            bundle.putBoolean(Config.BUNDLE_KEY_DONE, done)
            return MainFragment().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DONE = arguments?.getBoolean(Config.BUNDLE_KEY_DONE) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rv.run {
            layoutManager = LinearLayoutManager(activity)
            this.adapter = this@MainFragment.adapter
        }

        adapter.run {
            bindToRecyclerView(rv)
            setEmptyView(R.layout.fragment_empty, rv)

            ItemDragAndSwipeCallback(this).run {
                setSwipeMoveFlags(ItemTouchHelper.START)
                ItemTouchHelper(this).attachToRecyclerView(rv)
            }

            // 开启滑动删除
            enableSwipeItem()
            setOnItemSwipeListener(onItemSwipeListener)

            onItemClickListener = this@MainFragment.onItemClickListener
            setOnLoadMoreListener(onLoadMoreListener, rv)
        }

        swipe_Refresh.run {
            setOnRefreshListener {
                loadData()
            }
        }

        loadData()
    }

    fun loadData() {
        currentPage = 1

        HttpSender.instances
                .run {
                    if (DONE) {
                        listDone(currentType, currentPage)
                    } else {
                        listNotDo(currentType, currentPage)
                    }
                }
                .map(HttpResultFunction())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(object : Observer<ListResponse> {
                    override fun onComplete() {
                        if (swipe_Refresh.isRefreshing) swipe_Refresh.isRefreshing = false
                    }

                    override fun onSubscribe(d: Disposable) {
                        swipe_Refresh.isRefreshing = true
                        adapter.setEnableLoadMore(false)
                    }

                    override fun onNext(t: ListResponse) {
                        adapter.run {
                            replaceData(t.datas)
                            isOver = t.over
                            if (!isOver) {
                                setEnableLoadMore(true)
                                currentPage++
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (swipe_Refresh.isRefreshing) swipe_Refresh.isRefreshing = false
                    }

                })

    }

    private val onLoadMoreListener = BaseQuickAdapter.RequestLoadMoreListener {
        if (isOver) {
            adapter.run {
                loadMoreEnd()
                setEnableLoadMore(false)
            }
        } else {
            HttpSender.instances.run {
                if (DONE) {
                    listDone(currentType, currentPage)
                } else {
                    listNotDo(currentType, currentPage)
                }
            }.map(HttpResultFunction())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                    .subscribe({
                        isOver = it.over
                        if (!isOver) {
                            currentPage++
                        }

                        adapter.run {
                            addData(it.datas)
                            loadMoreComplete()
                        }
                    }, {
                        activity?.toast("加载更多数据失败，${it.message}")
                        adapter.run {
                            loadMoreFail()
                            setEnableLoadMore(false)
                        }
                    })
        }
    }

    private val onItemSwipeListener = object : OnItemSwipeListener {
        override fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
        }

        override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
            val item = adapter.data[pos]
            // 执行删除
            HttpSender.instances.delete(item.id)
                    .map(HttpResultFunction())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@MainFragment)))
                    .subscribe({
                        activity?.toast("删除成功")
                    }, {
                        if (it.message == "The mapper function returned a null value.") {
                            activity?.toast("删除成功")
                        } else {
                            activity?.toast("删除失败，${it.message}")
                        }
                    })
            // 刷新后一条数据
            if (adapter.data.size > 1) {
                adapter.notifyItemChanged(pos + 1)
            }
        }

        override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}

        override fun onItemSwipeMoving(canvas: Canvas?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, isCurrentlyActive: Boolean) {}

    }

    private val onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
        BottomSheetDialog(context!!)

        val todoDetail = adapter.data[position] as TodoDetail

        MaterialDialog.Builder(context!!)
                .items(if (todoDetail.status == 0) R.array.notdo_options else R.array.done_options)
                .title("请选择")
                .itemsCallback { _, _, _, text ->
                    if (text.contains("状态")) {
                        updateStatus(position)
                    } else {
                        startActivityForResult(Intent(activity, UpdateActivity::class.java)
                                .putExtra(Config.INTENT_NAME_TODODETAIL, todoDetail)
                                , Config.MAIN_UPDATE_REQUEST_CODE)
                    }
                }
                .show()
    }

    private fun updateStatus(position: Int) {
        val todoDetail = adapter.data[position] as TodoDetail

        HttpSender.instances.done(todoDetail.id, if (todoDetail.status == 0) 1 else 0)
                .map(HttpResultFunction())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe({
                    activity?.toast("更新状态成功！")
                    adapter.remove(position)
                    // 将另一个fragment中的数据刷新
                    (activity as MainActivity).run {
                        when (currentIndex) {
                            R.id.nav_done -> {
                                if (notDoFragment?.currentType == todoDetail.type) {
                                    notDoFragment?.loadData()
                                }
                            }
                            R.id.nav_notdo -> {
                                if (doneFragment?.currentType == todoDetail.type) {
                                    doneFragment?.loadData()
                                }
                            }
                        }
                    }

                }, {
                    activity?.toast("更新状态失败，${it.message}")
                })
    }
}