package com.wanzi.todo

import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ViewTarget
import com.wanzi.todo.base.Preferences
import com.wanzi.todo.config.Config
import com.wanzi.todo.fragment.MainFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var glide: ViewTarget<ImageView, Drawable>
    private val username: String by Preferences(Config.USERNAME_KEY, "")
    var currentIndex = 0   // 当前Fragment
    var doneFragment: MainFragment? = null
    var notDoFragment: MainFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        toolbar.run {
            setSupportActionBar(this)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        drawer_layout.run {
            val toggle = ActionBarDrawerToggle(
                    this@MainActivity,
                    this,
                    toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            )

            this.addDrawerListener(toggle)
            toggle.syncState()
        }

        glide = Glide
                .with(this)
                .load(Config.NAV_HEADER_IMG)
                .apply(RequestOptions.bitmapTransform(CircleCrop())) // 圆形处理
                .into(nav_view.getHeaderView(0).findViewById(R.id.iv_nav_header))

        nav_view.run {
            getHeaderView(0).findViewById<TextView>(R.id.tv_nav_header).text = username

            setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_exit -> {
                        Preferences.clear()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }
                return@setNavigationItemSelectedListener true
            }
        }

        bottom_nav.run {
            setOnNavigationItemSelectedListener {
                setFragment(it.itemId)
                return@setOnNavigationItemSelectedListener when (it.itemId) {
                    R.id.nav_done -> {
                        currentIndex = R.id.nav_done
                        spinner.setSelection(doneFragment?.currentType ?: Config.TYPE_0)
                        true
                    }
                    R.id.nav_notdo -> {
                        currentIndex = R.id.nav_notdo
                        spinner.setSelection(notDoFragment?.currentType ?: Config.TYPE_0)
                        true
                    }
                    else -> false
                }
            }

            selectedItemId = R.id.nav_done
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (currentIndex) {
                    R.id.nav_done -> {
                        doneFragment?.run {
                            if (currentType != p2) {
                                currentType = p2
                                loadData()
                            }
                        }
                    }
                    R.id.nav_notdo -> {
                        notDoFragment?.run {
                            if (currentType != p2) {
                                currentType = p2
                                loadData()
                            }
                        }
                    }
                }
            }

        }
    }

    private fun setFragment(index: Int) {
        supportFragmentManager.beginTransaction().apply {
            doneFragment ?: run {
                MainFragment.newInstance(true).run {
                    doneFragment = this
                    add(R.id.frame_layout, this)
                }
            }
            notDoFragment ?: run {
                MainFragment.newInstance(false).run {
                    notDoFragment = this
                    add(R.id.frame_layout, this)
                }
            }

            hideFragment(this)

            when (index) {
                R.id.nav_done -> {
                    doneFragment?.run {
                        show(this)
                    }
                }
                R.id.nav_notdo -> {
                    notDoFragment?.run {
                        show(this)
                    }
                }
            }
        }.commit()
    }

    private fun hideFragment(fragmentTransaction: FragmentTransaction) {
        doneFragment?.run {
            fragmentTransaction.hide(this)
        }
        notDoFragment?.run {
            fragmentTransaction.hide(this)
        }
    }

    override fun onStart() {
        super.onStart()
        glide.onStart()
    }

    override fun onStop() {
        super.onStop()
        glide.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        glide.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_add)?.isVisible = currentIndex != R.id.nav_done
        invalidateOptionsMenu()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_add) {
            startActivityForResult(Intent(this, AddActivity::class.java), Config.MAIN_ADD_REQUEST_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Config.MAIN_ADD_REQUEST_CODE -> {
                // 当在未完成Fragment中并且类型和添加类型相同时，刷新
                if (currentIndex == R.id.nav_notdo) {
                    val type = data?.getIntExtra(Config.INTENT_NAME_TYPE, 0) ?: Config.TYPE_0
                    notDoFragment?.run {
                        if (type == currentType) {
                            loadData()
                        }
                    }
                }
            }
            Config.MAIN_UPDATE_REQUEST_CODE -> {
                if (currentIndex == R.id.nav_notdo) {
                    notDoFragment?.loadData()
                }
            }
        }
    }
}
