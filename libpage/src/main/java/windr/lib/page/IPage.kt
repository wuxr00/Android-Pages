package windr.lib.page

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup

/**
 * 页面*/
interface IPage {
    /**
     * 创建page视图，初始化
     */
    fun createPage(context: Context, parent: ViewGroup, args: Bundle? = null) = Unit

    fun getView(): View?

    /**
     * 页面显示（同步activity的onResume())*/
    fun show() = Unit

    /**
     * 页面隐藏（同步activity的onPause())*/
    fun hide() = Unit

    /**
     * 页面销毁*/
    fun destroy() = Unit

    /**
     * 页面即将退出*/
    fun onFinishing() = Unit

    /**
     * 用户按下返回键*/
    fun onBackPressed(): Boolean = false

}
