package windr.lib.page

import android.animation.Animator
import kotlinx.coroutines.flow.Flow

/**
 * 供外部提供page切换动画接口
 */
interface IPageSwitchAnimationGenerator {
    fun setPageSwitchOperationListener(operationListener: IPageSwitchGeneratorOperation)

    fun prepareNextPageEnter(currentPage: IPage?, nextPage: IPage): Flow<Animator>?

    fun preparePageExit(currentPage: IPage, previousPage: IPage?): Flow<Animator>?

}