package windr.lib.page

import android.view.View

/**
 * page切换动画生成器使用的操作接口
 */
interface IPageSwitchGeneratorOperation {
    /**
     * 当前页面开始退出（拖动等）*/
    fun onPageStartExit(page: IPage)

    /**
     * 当前页面终止退出动作*/
    fun onCancelExit()

    /**
     * 当前页面执行退出*/
    fun onPageExit(page: IPage)

    /**
     * 获取隔层视图*/
    fun getCoverView(): View

    /**
     * 获取上一个页面*/
    fun getPreviousPage(): IPage?

    /**
     * 获取当前页面*/
    fun getCurrentPage(): IPage?
}