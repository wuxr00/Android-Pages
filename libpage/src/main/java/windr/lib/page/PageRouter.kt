package windr.lib.page

import android.os.Bundle

/**
 * 方便添加/删除页面的工具类 */
object PageRouter {
    val MAIN = "_main"

    private val receivers =
        HashMap<String, RouteReceiver>()

    fun registerRouteReceiver(stage: String, receiver: RouteReceiver) {
        receivers[stage] = receiver
    }


    fun showPage(
        stage: String,
        page: IPage,
        args: Bundle? = null,
        pageAnimation: IPageAnimation? = null
    ) {
        receivers[stage]?.routeTo(page, args, pageAnimation)
    }

    fun closePage(
        stage: String
        , page: IPage
        , pageAnimation: IPageAnimation? = null
    ) {
        receivers[stage]?.close(page, pageAnimation)
    }

    class Builder(val page: IPage) {
        var stage = ""
        var args: Bundle? = null
        var pageAnimation: IPageAnimation? = null

        fun stage(stage: String) = apply {
            this.stage = stage
        }

        fun pageAnimation(pageAnimation: IPageAnimation) = apply {
            this.pageAnimation = pageAnimation
        }

        fun arguments(args: Bundle) = apply {
            this.args = args
        }

        fun build() {
            showPage(stage, page, args, pageAnimation)
        }
    }

    class CloseBuilder(val page: IPage) {
        var stage = ""
        var pageAnimation: IPageAnimation? = null

        fun stage(stage: String) = apply {
            this.stage = stage
        }

        fun pageAnimation(pageAnimation: IPageAnimation) = apply {
            this.pageAnimation = pageAnimation
        }


        fun build() {
            closePage(stage, page, pageAnimation)
        }
    }

}


interface RouteReceiver {
    fun routeTo(page: IPage, args: Bundle? = null, pageAnimation: IPageAnimation? = null)
    fun close(page: IPage, pageAnimation: IPageAnimation? = null)

}


/**
 * 添加页面*/
infix fun PageRouter.show(page: IPage) = PageRouter.Builder(page)
infix fun PageRouter.Builder.withArguments(args: Bundle) = this.apply { arguments(args) }
infix fun PageRouter.Builder.withAnimation(pageAnimation: IPageAnimation) =
    this.apply { pageAnimation(pageAnimation) }

/**
 * 在哪一个管理器上添加页面*/
infix fun PageRouter.Builder.onStage(stage: String) = this.apply {
    stage(stage)
    build()
}

/**
 * 关闭/删除页面*/
infix fun PageRouter.close(page: IPage) = PageRouter.CloseBuilder(page)
infix fun PageRouter.CloseBuilder.withAnimation(pageAnimation: IPageAnimation) =
    this.apply { pageAnimation(pageAnimation) }

/**
 * 在哪一个管理器上删除页面*/
infix fun PageRouter.CloseBuilder.onStage(stage: String) = this.apply {
    stage(stage)
    build()
}