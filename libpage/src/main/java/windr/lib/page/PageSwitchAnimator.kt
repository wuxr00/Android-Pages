package windr.lib.page

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.NonNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * page切换动画
 */
class PageSwitchAnimator(@NonNull private var manager: PageManager?) :
    CoroutineScope by CoroutineScope(manager!!.coroutineContext) {
    var switchAnimationGenerator: IPageSwitchAnimationGenerator? = null
    val pageAnimationCache = HashMap<Int, Animator>()


    suspend fun playEnterAnimation(
        currentPage: IPage?,
        enterPage: IPage,
        pageAnimation: IPageAnimation?
    ) {
        var cacheAnimator = false
        var flow = pageAnimation?.prepareAnimation(enterPage, currentPage)
        if (flow != null)
            cacheAnimator = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        else flow = switchAnimationGenerator?.prepareNextPageEnter(currentPage, enterPage)
        flow?.collect {
            if (cacheAnimator)
                pageAnimationCache[enterPage.hashCode()] = it
            pWaitfor(enterPage.getView()!!).pToplay(it)
        }
    }


    suspend fun playExitAnimation(
        currentPage: IPage,
        previousPage: IPage?,
        pageAnimation: IPageAnimation?
    ) {
        var fromAnimator = pageAnimationCache.remove(currentPage.hashCode())
        var overrideFlow = pageAnimation?.prepareAnimation(currentPage, previousPage)
        val reversed = fromAnimator != null && overrideFlow == null
        if (!reversed)
            (overrideFlow ?: switchAnimationGenerator?.preparePageExit(currentPage, previousPage))
                ?.collect { fromAnimator = it }
        pToplay(fromAnimator, reversed)

    }


    fun destroy() {
        manager = null
        switchAnimationGenerator = null
        pageAnimationCache.clear()
    }

    private suspend fun pWaitfor(view: View): PageSwitchAnimator {
        val channel = Channel<Unit>()
        view.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(
                        this
                    )
                    launch { channel.send(Unit) }
                }
            })
        channel.receive()
        return this
    }

    private suspend fun pToplay(animator: Animator?, reversed: Boolean = false) {
        val channel = Channel<Unit>()
        if (animator != null) {
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    launch { channel.send(Unit) }
                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    launch { channel.send(Unit) }
                }
            })
            if (!reversed)
                animator.start()
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                AnimatorSet().run {
                    playTogether(animator)
                    reverse()
                }
            else launch { channel.send(Unit) }
        } else launch { channel.send(Unit) }
        channel.receive()
    }
}
