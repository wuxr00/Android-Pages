package windr.lib.page

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import windr.lib.page.CoverView.CoverHolder
import java.util.*

/**
 * page管理器
 */
class PageManager(
    @NonNull internal var holder: AppCompatActivity?
    , private var pageContainer: ViewGroup
) : CoroutineScope by MainScope(), RouteReceiver {
    private val TASK_TYPE_OPEN = 0
    private val TASK_TYPE_CLOSE = 1

    private val TOP = -1
    private val SECOND = -2

    private var finishing = false
    private val pageList = LinkedList<IPage>()
    private var holdBackStack = false
    private val pageSwitchAnimator by lazy { PageSwitchAnimator(this) }
    private val coverView by lazy {
        CoverView(holder!!, object : CoverHolder {
            override fun showCoverUnderTop() {
                moveCoverView(SECOND)
            }
        })
    }
    private var taskSort = 0
    private var isHandlingTask = false
    private val mainChannel by lazy { Channel<PageTask>() }

    private val waitingTasks: Queue<PageTask> = PriorityQueue(
        2,
        Comparator { o1: PageTask, o2: PageTask ->
            val typePriority = o1.type - o2.type
            if (typePriority == 0) o1.sort - o2.sort else typePriority
        }
    )

    private fun executeNext() {
        if (finishing) return
//        isHandlingTask = true
        launch { mainChannel.receive().run() }
    }

    fun holdBackStack(): PageManager {
        holdBackStack = true
        return this
    }

    private val holderLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate(owner: LifecycleOwner?) {
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume(owner: LifecycleOwner?) {
            pageList.peekLast()?.show()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause(owner: LifecycleOwner?) {
            pageList.peekLast()?.hide()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(owner: LifecycleOwner?) {
            finishing = true
            cancel()
            waitingTasks.clear()
            pageSwitchAnimator.destroy()
            for (i in pageList.indices) {
                val page = pageList[i]
                page.onFinishing()
                if (i == pageList.size - 1) page.hide()
                page.destroy()
            }
            pageContainer.removeAllViewsInLayout()
            pageList.clear()
            holder = null
        }
    }
    private val pageSwitchGeneratorOperation: IPageSwitchGeneratorOperation =
        object : IPageSwitchGeneratorOperation {
            override fun onPageStartExit(page: IPage) {
                isHandlingTask = true
                if (pageList.size < 2) return
                if (pageContainer.indexOfChild(coverView) != pageContainer.childCount - 2) (SECOND)
                val previousPage = pageList[pageList.size - 2]
                if (pageContainer.indexOfChild(previousPage.getView()) >= 0) return
                pageContainer.addView(previousPage.getView(), 0)
            }

            override fun onCancelExit() {
                if (pageList.size < 2) return
                val previousPage = pageList[pageList.size - 2]
                pageContainer.removeView(previousPage.getView())
                isHandlingTask = false
                executeNext()

            }

            override fun onPageExit(page: IPage) {
                pageList.remove(page)
                val previousPage = pageList.peekLast()
                endRemove(page, previousPage)
            }

            override fun getCoverView(): View {
                return coverView
            }

            override fun getPreviousPage(): IPage? {
                Log.i("testpagemanager", "getpreviouspage->$pageList ${pageList.size}")
                return if (pageList.size < 2) null else pageList[pageList.size - 2]
            }

            override fun getCurrentPage(): IPage? {
                return pageList.peekLast()
            }
        }

    private fun moveCoverView(index: Int) {
        (coverView.parent as? ViewGroup)?.removeView(coverView)
        if (pageContainer.childCount == 0) return
        if (pageContainer.measuredWidth != coverView.width || pageContainer.measuredHeight != coverView.height) coverView.setSize(
            pageContainer.measuredWidth,
            pageContainer.measuredHeight
        )
        coverView.visibility = View.VISIBLE
        when (index) {
            TOP -> pageContainer.addView(coverView)
            SECOND -> {
                pageContainer.addView(coverView, pageContainer.childCount - 1)
            }
            else -> pageContainer.addView(coverView, index)
        }
        Log.i("testcontainer", "after set cover--------->$index")
        for (i in 0 until pageContainer.childCount) {
            Log.i("testcontainer", "after set cover->" + pageContainer.getChildAt(i))
        }
    }

    init {
        holder!!.lifecycle.addObserver(holderLifecycleObserver)
    }

    fun setPageSwitchAnimationGenerator(pageSwitchAnimationGenerator: IPageSwitchAnimationGenerator) {
        pageSwitchAnimationGenerator.setPageSwitchOperationListener(pageSwitchGeneratorOperation)
        pageSwitchAnimator.switchAnimationGenerator = pageSwitchAnimationGenerator
    }

    fun addPage(newPage: IPage, args: Bundle?) {
        this.addPage(newPage, args, null)
    }

    fun addPage(newPage: IPage, args: Bundle?, enterAnimation: IPageAnimation?) {
//        if (newPage == null) return
        Log.i(
            PageManager::class.java.simpleName,
            """addPage-finishing=$finishing -isHandlingTask= $isHandlingTask -handlingBack= $isHandlingBackEvent
- $pageList"""
        )
        if (finishing) return

        launch {
            mainChannel.send(object : PageTask(TASK_TYPE_OPEN, taskSort++) {
                override fun run() {
                    doAdd(newPage, args, enterAnimation)
                }
            })
        }
        if (!isHandlingTask)
            executeNext()
    }

    private fun doAdd(
        newPage: IPage,
        args: Bundle?,
        enterAnimation: IPageAnimation?
    ) {
        isHandlingTask = true
        launch {
            moveCoverView(TOP)
            (newPage as? DefaultPage)?.pageManager = this@PageManager
            newPage.createPage(pageContainer.context, pageContainer, args)
            newPage.getView()?.run {
                if (this.layoutParams == null) this.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT
                )
                pageContainer.addView(this)
            }
            val currentPage = pageList.peekLast()
            pageList.addLast(newPage)
            pageSwitchAnimator.playEnterAnimation(currentPage, newPage, enterAnimation)
            endOpen(newPage, currentPage)
        }

    }

    private fun endOpen(newPage: IPage, currentPage: IPage?) {
        Log.i(
            PageManager::class.java.simpleName, """endOpen-finishing=$finishing
- $pageList
---------------------------------------------- """
        )
        if (finishing) return
        newPage.show()
        if (currentPage != null) {
            currentPage.hide()
            pageContainer.removeView(currentPage.getView())
        }
        isHandlingTask = false
        Log.i("PageLib", "onBackPressed= $isHandlingBackEvent - $isHandlingTask ")

        executeNext()
    }

    fun removePage(page: IPage) {
        this.removePage(page, null)
    }

    fun removePage(page: IPage, exitAnimation: IPageAnimation?) {
        Log.i(
            PageManager::class.java.simpleName,
            """removePage-finishing=$finishing -isHandlingTask= $isHandlingTask -handlingBack= $isHandlingBackEvent
- $pageList"""
        )
        if (finishing) return
        launch {
            mainChannel.send(object : PageTask(TASK_TYPE_CLOSE, taskSort++) {
                override fun run() {
                    doRemove(page, exitAnimation)
                }
            })
        }
        if (!isHandlingTask)
            executeNext()
    }

    private fun doRemove(page: IPage, exitAnimation: IPageAnimation?) {
        launch {
            var previousPage: IPage? = null
            async {
                val index = pageList.indexOf(page)
                if (index < 0) return@async
                if (index == pageList.size - 1) {
                    pageList.pollLast()
                    page.onFinishing()
                    previousPage = pageList.peekLast()?.also {
                        if (pageContainer.indexOfChild(it.getView()) < 0) {
                            pageContainer.addView(it.getView(), 0)
                        } else if (pageContainer.indexOfChild(coverView)
                            != pageContainer.childCount - 2
                        ) moveCoverView(SECOND)
                        pageSwitchAnimator.playExitAnimation(page, it, exitAnimation)
                    }
                } else {
                    pageList.removeAt(index)
                    page.onFinishing()
                }
            }.await()
            endRemove(page, previousPage)
        }
    }

    private fun endRemove(
        page: IPage,
        previousPage: IPage?
    ) {
        if (finishing) return
        onPageExit(page)
        previousPage?.show()
        isHandlingTask = false
        executeNext()
    }


    private fun onPageExit(page: IPage) {
        page.hide()
        page.destroy()
        pageContainer.removeView(page.getView())
        moveCoverView(SECOND)
    }

    fun removePage(page: IPage, keepFirst: Boolean = true): Boolean {
        if (keepFirst && pageList.size == 1) return false
        removePage(page)
        return true
    }

    fun handleTouchEvent(motionEvent: MotionEvent?): Boolean {
        return (isHandlingTask
                || isHandlingBackEvent)
    }

    private var isHandlingBackEvent = false
    fun onBackPressed(): Boolean {
        Log.i("PageLib", "onBackPressed= $isHandlingBackEvent - $isHandlingTask ")

        if (isHandlingBackEvent || isHandlingTask) return true
        if (finishing || pageList.isEmpty()) return false

        isHandlingBackEvent = true
        val topPage = pageList.peekLast()
        if (topPage.onBackPressed()) {
            isHandlingBackEvent = false
            return true
        } else if (holdBackStack) {
            isHandlingBackEvent = false
            return removePage(topPage, true)
        }
        isHandlingBackEvent = false

        return false
    }

    fun startActivity(intent: Intent?) {
        startActivityForResult(intent, -1)
    }

    fun startActivityForResult(intent: Intent?, requestCode: Int) {
        holder?.startActivityForResult(intent, requestCode)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        (pageList.peekLast() as? DefaultPage)?.onActivityResult(requestCode, resultCode, data)
            ?: false


    private abstract inner class PageTask internal constructor(var type: Int, var sort: Int) :
        Runnable

    override fun routeTo(page: IPage, args: Bundle?, pageAnimation: IPageAnimation?) {
        addPage(page, args, pageAnimation)
    }

    override fun close(page: IPage, pageAnimation: IPageAnimation?) {
        removePage(page, pageAnimation)
    }


    companion object

}

class PageShowBuilder(val page: IPage) {
    var args: Bundle? = null
    var pageAnimation: IPageAnimation? = null

    fun pageAnimation(pageAnimation: IPageAnimation) = apply {
        this.pageAnimation = pageAnimation
    }

    fun arguments(args: Bundle) = apply {
        this.args = args
    }

    fun build(pageManager: PageManager) {
        pageManager.addPage(page, args, pageAnimation)
    }
}


infix fun PageManager.Companion.add(page: IPage) = PageShowBuilder(page)
infix fun PageShowBuilder.withArguments(args: Bundle) = this.apply { arguments(args) }
infix fun PageShowBuilder.withAnimation(pageAnimation: IPageAnimation) =
    this.apply { pageAnimation(pageAnimation) }

infix fun PageShowBuilder.toPageManager(pageManager: PageManager) {
    this.build(pageManager)
}


class PageCloseBuilder(val page: IPage) {
    var stage = ""
    var pageAnimation: IPageAnimation? = null


    fun pageAnimation(pageAnimation: IPageAnimation) = apply {
        this.pageAnimation = pageAnimation
    }


    fun build(pageManager: PageManager) {
        pageManager.removePage(page, pageAnimation)
    }
}

infix fun PageManager.Companion.remove(page: IPage) = PageCloseBuilder(page)
infix fun PageCloseBuilder.withAnimation(pageAnimation: IPageAnimation) =
    this.apply { pageAnimation(pageAnimation) }

infix fun PageCloseBuilder.fromPageManager(pageManager: PageManager) {
    this.build(pageManager)
}