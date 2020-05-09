package windr.lib.page

import android.animation.Animator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface IPageAnimation {

    /**
     * 准备执行相关动画（将界面视图放到开始位置）
     */
    fun prepareAnimation(topPage:IPage,previousPage:IPage?): Flow<Animator>

}