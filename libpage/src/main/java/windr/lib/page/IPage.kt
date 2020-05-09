package windr.lib.page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup

interface IPage {
    /**
     * 创建page视图，初始化
     */
    fun createPage(context: Context, parent: ViewGroup, args: Bundle? = null) = Unit

    fun getView(): View?

    fun show() = Unit
    fun hide() = Unit
    fun destroy() = Unit
    fun onFinishing() = Unit
    fun onBackPressed(): Boolean = false

}
