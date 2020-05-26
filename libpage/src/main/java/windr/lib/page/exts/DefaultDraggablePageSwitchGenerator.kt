package windr.lib.page.exts

import android.animation.*
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import windr.lib.page.IPage
import windr.lib.page.IPageSwitchAnimationGenerator
import windr.lib.page.IPageSwitchGeneratorOperation
import kotlin.math.abs

/**
 * 上一页左滑退出，下一页从右滑入，可拖动关闭页面的动画生产器
 */
class DefaultDraggablePageSwitchGenerator(context: Context) : IPageSwitchAnimationGenerator {
    var operationListener: IPageSwitchGeneratorOperation? = null
    private var isHandlingAnimation = false
    private var downX = 0f
    private var downY = 0f
    private var startTime: Long = 0
    private var invalidPointerId = 0
    private var topPage: IPage? = null
    private var previousPage: IPage? = null
    private var previousPageStartTranslation = 0f
    private var handlePageMoving = false
    private val ALLOWED_AREA_PERCENTAGE = 0.2f
    private val EXIT_LINE_PERCENTAGE = 0.5f
    private val TOP_VIEW_ELEVATION = 20f
    private var areaWidth = 0f
    private var handleByPage = false

    init {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            .apply {
                val displayMetrics = DisplayMetrics()
                getMetrics(displayMetrics)
                areaWidth = displayMetrics.widthPixels.toFloat()
            }
    }

    fun handleTouchEvent(motionEvent: MotionEvent): Boolean {
        Log.i(
            "testtouchpage", "touch ->" + isHandlingAnimation
                    + " - " + motionEvent.action
                    + " - " + previousPage
        )
        if (isHandlingAnimation) return true
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                previousPage = operationListener!!.getPreviousPage()
                if (previousPage == null) return false
                handlePageMoving = false
                handleByPage = false
                downX = motionEvent.rawX
                topPage = operationListener!!.getCurrentPage()
                if (topPage == null
                    || (topPage is SlidablePage
                            && ((topPage as SlidablePage).notDraggable(motionEvent)))
                ) {
                    topPage = null
                    previousPage = null
                    return false
                }
                val isInAllowedArea: Boolean =
                    downX < (topPage?.getView()?.measuredWidth ?: 0) * ALLOWED_AREA_PERCENTAGE
                if (!isInAllowedArea) {
                    topPage = null
                    previousPage = null
                    return false
                }
                invalidPointerId = motionEvent.getPointerId(0)
                downY = motionEvent.rawY
                startTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                if (topPage == null || previousPage == null) return false
                val moveX = motionEvent.x
                val distanceX = moveX - downX
                if (!handlePageMoving) {
                    val moveY = motionEvent.y
                    val distanceY = moveY - downY
                    Log.i(
                        "testtouchpage", "touch move->"
                                + distanceX
                                + " - " + distanceY
                                + " - " + (topPage?.getView()?.measuredHeight ?: 0) / 12f
                                + " - " + distanceX / distanceY
                    )
                    if (abs(distanceX) > 10 && abs(distanceX / distanceY) > 2f) {
                        handlePageMoving = true
                        downX = moveX
                        operationListener!!.onPageStartExit(topPage!!)
                        previousPage?.getView()?.apply {
                            translationX =
                                -(measuredWidth / 3f)
                                    .also { previousPageStartTranslation = it }
                        }
                        topPage?.getView()?.elevation = TOP_VIEW_ELEVATION
                    } else if (abs(distanceY) > 150) {
                        topPage = null
                        previousPage = null
                    }
                } else {
                    if (abs(distanceX) < 20)
                        startTime = System.currentTimeMillis()
                    if (topPage is SlidablePage && (topPage as? SlidablePage)?.handleDrag(
                            distanceX,
                            previousPage
                        ) == true
                    ) {
                        handleByPage = true
                        return true
                    }
                    translatePage(0f.coerceAtLeast(distanceX))
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE ->
                if (handlePageMoving) {
                    endDragging()
                    return true
                }
        }
        return false
    }

    private fun endDragging() {
        topPage?.getView()?.run {
            isHandlingAnimation = true
            val isExit = booleanArrayOf(false)
            val timePeriod = System.currentTimeMillis() - startTime
            val speed: Float = translationX / timePeriod
            if (topPage !is SlidablePage || (topPage as? SlidablePage)?.handleDragEnd(
                    translationX,
                    speed,
                    previousPage
                ) == false
            ) {
                run {
                    if ((speed > 3
                                || translationX
                                > measuredWidth * EXIT_LINE_PERCENTAGE).also {
                            isExit[0] = it
                        }
                    ) {
                        ValueAnimator.ofFloat(
                            translationX
                            , measuredWidth.toFloat()
                        ).apply {
                            duration =
                                300.coerceAtMost((((measuredWidth - translationX) / speed).toLong()).toInt())
                                    .toLong()
                            interpolator = AccelerateInterpolator()
                        }
                    } else {
                        ValueAnimator.ofFloat(
                            translationX
                            , 0f
                        ).apply {
                            duration = 200
                            interpolator = DecelerateInterpolator()
                        }
                    }
                }.apply {
                    addUpdateListener { animation: ValueAnimator ->
                        translatePage(
                            animation.animatedValue as Float
                        )
                    }
                }.p_startAnim {
                    if (isExit[0]) {
                        previousPage?.getView()?.translationX = 0F
                        operationListener!!.onPageExit(topPage!!)
                    } else {
                        translationX = 0F
                        elevation = 0F
                        operationListener!!.onCancelExit()
                    }
                    topPage = null
                    previousPage = null
                    isHandlingAnimation = false
                }
            }
            handlePageMoving = false
        }
    }

    private fun translatePage(distance: Float) {
        topPage?.getView()?.translationX = distance
        previousPage?.getView()?.translationX = previousPageStartTranslation + distance / 3
    }

    override fun setPageSwitchOperationListener(operationListener: IPageSwitchGeneratorOperation) {
        this.operationListener = operationListener
        //        operationListener.getCoverView().setBackgroundColor(Color.argb());
    }

    override fun prepareNextPageEnter(currentPage: IPage?, nextPage: IPage) =
        if (currentPage == null) null else flow<Animator> {

            isHandlingAnimation = true
            if (nextPage is SlidablePage)
                nextPage.beforeEnterAnimation()
                    .collect {
                        emit(it ?: getDefaultEnterAnimation(nextPage, currentPage))
                    }
            else emit(getDefaultEnterAnimation(nextPage, currentPage))
        }

    private fun getDefaultEnterAnimation(
        nextPage: IPage,
        currentPage: IPage?
    ): AnimatorSet {
        return AnimatorSet().apply {
            nextPage.getView()?.run {
                this.translationX = areaWidth
                this.elevation = TOP_VIEW_ELEVATION
                val nextAnim = ObjectAnimator.ofFloat(
                    this, View.TRANSLATION_X
                    , this.translationX, 0f
                ).setDuration(400)
                Log.i("testpageanimdef", "enter-> $areaWidth")
                if (currentPage == null)
                    this@apply.playTogether(nextAnim)
                else
                    this@apply.playTogether(
                        nextAnim,
                        ObjectAnimator.ofFloat(
                            currentPage.getView(), View.TRANSLATION_X
                            , 0f, -(areaWidth / 3f)
                        ).setDuration(400)
                    )
                this@apply.interpolator = DecelerateInterpolator()
                this@apply.p_onAnimEnd {
                    this@run.translationX = 0f
                    this@run.elevation = 0F
                    currentPage?.getView()?.translationX = 0F
                    isHandlingAnimation = false
                    Log.i("testpageanimdef", "enter end-> ")
                }
            }
        }
    }


    override fun preparePageExit(currentPage: IPage, previousPage: IPage?) = flow<Animator> {
        isHandlingAnimation = true
        if (currentPage is SlidablePage)
            currentPage.beforeExitAnimation()
                .collect {
                    emit(it ?: getDefaultExitAnimation(currentPage, previousPage))
                }
        else emit(getDefaultExitAnimation(currentPage, previousPage))
    }

    fun getDefaultExitAnimation(currentPage: IPage, previousPage: IPage?): AnimatorSet {
        return AnimatorSet().apply {
            currentPage.getView()?.run {
                val previousStartTranslationX = -(areaWidth / 3f)
                previousPage?.getView()?.translationX = previousStartTranslationX
                elevation = TOP_VIEW_ELEVATION
                val currentAnim = ObjectAnimator.ofFloat(
                    this, View.TRANSLATION_X
                    , this.translationX, areaWidth
                ).setDuration(400)
                if (previousPage == null)
                    this@apply.playTogether(currentAnim)
                else
                    this@apply.playTogether(
                        currentAnim,
                        ObjectAnimator.ofFloat(
                            previousPage.getView(), View.TRANSLATION_X
                            , previousStartTranslationX, 0f
                        ).setDuration(400)
                    )
                this@apply.interpolator = AccelerateDecelerateInterpolator()
                this@apply.p_onAnimEnd {
                    previousPage?.getView()?.translationX = 0F
                    isHandlingAnimation = false
                    postDelayed({
                        this@run.translationX = 0f
                    }, 10)
                }
            }
        }
    }

}

interface SlidablePage {
    fun notDraggable(motionEvent: MotionEvent?): Boolean = false
    fun handleDrag(translationX: Float, previousPage: IPage?): Boolean = false
    fun handleDragEnd(translationX: Float, speed: Float, previousPage: IPage?): Boolean = false
    fun beforeEnterAnimation() = flow<Animator?> { emit(null) }
    fun beforeExitAnimation() = flow<Animator?> { emit(null) }
}

internal inline fun Animator.p_startAnim(crossinline callback: Animator.() -> Unit) {
    this.p_onAnimEnd(callback)
    this.start()
}

internal inline fun Animator.p_onAnimEnd(crossinline callback: Animator.() -> Unit) {
    this.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationCancel(animation: Animator) {
            super.onAnimationCancel(animation)
            endAnimation()
        }

        private fun endAnimation() {
            callback.invoke(this@p_onAnimEnd)
        }

        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            endAnimation()
        }
    })
}