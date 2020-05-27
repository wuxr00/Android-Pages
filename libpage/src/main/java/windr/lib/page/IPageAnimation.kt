package windr.lib.page

import android.animation.Animator
import kotlinx.coroutines.flow.Flow

/**
 * 页面切换动画提供者接口*/
interface IPageAnimation {

    /**
     * 准备执行相关动画（将界面视图放到开始位置）
     */
    fun prepareAnimation(topPage: IPage, previousPage: IPage?): Flow<Animator>

}