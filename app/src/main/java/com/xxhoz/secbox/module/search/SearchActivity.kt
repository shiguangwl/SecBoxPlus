package com.xxhoz.secbox.module.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.gyf.immersionbar.ktx.immersionBar
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivitySearchBinding
import com.xxhoz.secbox.module.player.AGVideoActivity
import com.xxhoz.secbox.module.start.StartViewModel

class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    override fun getPageName(): String = PageName.SEARCH
    private val viewModel: StartViewModel by viewModels()
    override val inflater: (inflater: LayoutInflater) -> ActivitySearchBinding
        get() = ActivitySearchBinding::inflate

    companion object{
        fun startActivity(context: Context){
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
    }


}
