package windr.lib.page

import android.view.View

/**
 * page切换动画生成器使用的操作接口
 */
interface IPageSwitchGeneratorOperation {
    fun onPageStartExit(page: IPage)
    fun onCancelExit()
    fun onPageExit(page: IPage)
    fun getCoverView(): View
    fun getPreviousPage(): IPage?
    fun getCurrentPage(): IPage?
}