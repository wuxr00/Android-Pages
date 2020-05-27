package windr.lib.page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * 默认页面基类*/
abstract class DefaultPage : IPage {
    var pageManager: PageManager? = null
    var arguments: Bundle? = null
    var isShown = false
        private set
    var contentView: View? = null
        protected set
    protected var isFinishing = false

    final override fun createPage(
        context: Context,
        parent: ViewGroup,
        args: Bundle?
    ) {
        contentView = createView(context, parent, args)
    }

    protected abstract fun createView(
        context: Context,
        parent: ViewGroup,
        args: Bundle?
    ): View

    protected fun getContext() = pageManager?.holder

    override fun getView(): View? {
        return contentView
    }

    final override fun show() {
        Log.i(DefaultPage::class.java.simpleName, "show=" + toString())
        isShown = true
        onShow()
    }

    final override fun hide() {
        Log.i(DefaultPage::class.java.simpleName, "hide=" + toString())
        isShown = false
        onHide()
    }

    final override fun destroy() {
        Log.i(DefaultPage::class.java.simpleName, "destroy=" + toString())
        onDestroy()
        pageManager = null
        if (arguments != null) arguments!!.clear()
        arguments = null
    }

    fun removePage() {
        pageManager?.removePage(this)
    }

    protected open fun onShow() {}
    protected open fun onHide() {}
    protected open fun onDestroy() {}

    override fun onFinishing() {
        isFinishing = true
    }

    /**
     * activity结果返回接收处理*/
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean = false

}