package windr.lib.page.demo.pages

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.flow.flow
import windr.lib.page.*
import windr.lib.page.demo.AndroidUtilities
import windr.lib.page.demo.R
import windr.lib.page.demo.onAnimEnd
import windr.lib.page.demo.toPX

class BlueAnimPage : DefaultPage() {
    override fun createView(context: Context, parent: ViewGroup, args: Bundle?): View =
        object : FrameLayout(context) {
            val back: ImageView
            val textView: TextView
            val redButton: Button
            val redAnimButton: Button
            val greenButton: Button
            val greenAnimButton: Button
            val blueButton: Button
            val blueAnimButton: Button

            init {
                val sort = args?.getInt("data") ?: 1
                setBackgroundColor(Color.RED)
                addView(ImageView(getContext()).apply {
                    back = this
                    background = ShapeDrawable(OvalShape()).apply {
                        setBackgroundColor(Color.argb(200, 0, 0, 0))
                    }
                    setImageDrawable(getContext().getDrawable(R.drawable.ic_close))
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setOnClickListener {
                        PageRouter close this@BlueAnimPage onStage PageRouter.MAIN
                    }
                }, LayoutParams(45.toPX(), 45.toPX()))
                addView(
                    TextView(getContext()).also {
                        textView = it
                        it.setTextColor(Color.BLACK)
                        it.text = "page blue anim $sort"
                    })
                addView(Button(getContext()).also {
                    redButton = it
                    it.text = "start red page"
                    it.setOnClickListener {

                        PageRouter show
                                RedPage() withArguments
                                Bundle().apply { putInt("data", sort + 1) } onStage
                                PageRouter.MAIN
                    }
                })
                addView(Button(getContext()).also {
                    redAnimButton = it
                    it.text = "red from right"
                    it.setOnClickListener {

                        PageRouter show RedAnimPage() withArguments
                                Bundle().apply { putInt("data", sort + 1) } withAnimation
                                object : IPageAnimation {
                                    override fun prepareAnimation(
                                        topPage: IPage,
                                        previousPage: IPage?
                                    ) = flow<Animator> {
                                        topPage.getView()?.translationX =
                                            AndroidUtilities.customScreenWidth.toFloat()
                                        emit(AnimatorSet().apply {
                                            val anim = ObjectAnimator.ofFloat(
                                                topPage.getView(),
                                                View.TRANSLATION_X,
                                                topPage.getView()!!.translationX,
                                                0f
                                            ).setDuration(350)
                                            if (previousPage?.getView() != null)
                                                playTogether(
                                                    anim,
                                                    ObjectAnimator.ofFloat(
                                                        previousPage.getView(),
                                                        View.TRANSLATION_X,
                                                        0f,
                                                        -AndroidUtilities.customScreenWidth.toFloat()
                                                    ).setDuration(350)
                                                )
                                            else playTogether(anim)
                                        }.onAnimEnd { canceled ->
                                            if (canceled) topPage.getView()?.translationX = 0f
                                            previousPage?.getView()?.translationX = 0f
                                        })
                                    }

                                } onStage PageRouter.MAIN

                    }
                })
                addView(Button(getContext()).also {
                    greenButton = it
                    it.text = "start green page"
                    it.setOnClickListener {

                        PageRouter show
                                GreenPage() withArguments
                                Bundle().apply { putInt("data", sort + 1) } onStage
                                PageRouter.MAIN
                    }
                })
                addView(Button(getContext()).also {
                    greenAnimButton = it
                    it.text = "green from left"
                    it.setOnClickListener {

                        PageRouter show GreenAnimPage() withArguments
                                Bundle().apply { putInt("data", sort + 1) } withAnimation
                                object : IPageAnimation {
                                    override fun prepareAnimation(
                                        topPage: IPage,
                                        previousPage: IPage?
                                    ) = flow<Animator> {
                                        topPage.getView()?.translationX =
                                            -AndroidUtilities.customScreenWidth.toFloat()
                                        emit(AnimatorSet().apply {
                                            val anim = ObjectAnimator.ofFloat(
                                                topPage.getView(),
                                                View.TRANSLATION_X,
                                                topPage.getView()!!.translationX,
                                                0f
                                            ).setDuration(350)
                                            if (previousPage?.getView() != null)
                                                playTogether(
                                                    anim,
                                                    ObjectAnimator.ofFloat(
                                                        previousPage.getView(),
                                                        View.TRANSLATION_X,
                                                        0f,
                                                        AndroidUtilities.customScreenWidth.toFloat()
                                                    ).setDuration(350)
                                                )
                                            else playTogether(anim)
                                        }.onAnimEnd { canceled ->
                                            if (canceled) topPage.getView()?.translationX = 0f
                                            previousPage?.getView()?.translationX = 0f
                                        })
                                    }
                                } onStage PageRouter.MAIN
                    }
                })
                addView(Button(getContext()).also {
                    blueButton = it
                    it.text = "start blue page"
                    it.setOnClickListener {

                        PageRouter show
                                BluePage() withArguments
                                Bundle().apply { putInt("data", sort + 1) } onStage
                                PageRouter.MAIN
                    }
                })
                addView(Button(getContext()).also {
                    blueAnimButton = it
                    it.text = "blue from back"
                    it.setOnClickListener {

                        PageRouter show BlueAnimPage() withArguments
                                Bundle().apply { putInt("data", sort + 1) } withAnimation
                                object : IPageAnimation {
                                    override fun prepareAnimation(
                                        topPage: IPage,
                                        previousPage: IPage?
                                    ) = flow<Animator> {
                                        topPage.getView()?.translationY =
                                            -AndroidUtilities.customScreenWidth.toFloat()
                                        emit(AnimatorSet().apply {
                                            if (previousPage?.getView() != null) {
                                                playTogether(
                                                    ObjectAnimator.ofFloat(
                                                        topPage.getView(),
                                                        View.TRANSLATION_X,
                                                        0f,
                                                        AndroidUtilities.customScreenWidth * 2 / 3f,
                                                        0f
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        topPage.getView(),
                                                        View.TRANSLATION_Z,
                                                        -10f,
                                                        0f
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        topPage.getView(),
                                                        View.SCALE_X,
                                                        0.7f,
                                                        1f
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        topPage.getView(),
                                                        View.SCALE_Y,
                                                        0.7f,
                                                        1f
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        previousPage.getView(),
                                                        View.TRANSLATION_X,
                                                        0f,
                                                        -AndroidUtilities.customScreenWidth.toFloat()
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        previousPage.getView(),
                                                        View.SCALE_X,
                                                        1f,
                                                        0.7f
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        previousPage.getView(),
                                                        View.SCALE_Y,
                                                        1f,
                                                        0.7f
                                                    ).setDuration(350)
                                                )

                                            } else {
                                                playTogether(
                                                    ObjectAnimator.ofFloat(
                                                        topPage.getView(),
                                                        View.TRANSLATION_Z,
                                                        -10f,
                                                        0f
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        topPage.getView(),
                                                        View.SCALE_X,
                                                        0.7f,
                                                        1f
                                                    ).setDuration(350),
                                                    ObjectAnimator.ofFloat(
                                                        topPage.getView(),
                                                        View.SCALE_Y,
                                                        0.7f,
                                                        1f
                                                    ).setDuration(350)
                                                )
                                            }

                                        }.onAnimEnd { canceled ->
                                            if (canceled) {
                                                topPage.getView()?.translationX = 0f
                                                topPage.getView()?.translationZ = 0f
                                                topPage.getView()?.scaleX = 1f
                                                topPage.getView()?.scaleY = 1f
                                            }
                                            previousPage?.getView()?.translationX = 0f
                                            previousPage?.getView()?.scaleX = 1f
                                            previousPage?.getView()?.scaleY = 1f
                                        })
                                    }
                                } onStage PageRouter.MAIN
                    }
                })
            }

            override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
                back.layout(
                    16.toPX(),
                    AndroidUtilities.getStatusBarHeight() + 16.toPX(),
                    back.measuredWidth + 16.toPX(),
                    AndroidUtilities.getStatusBarHeight() + 16.toPX() + back.measuredHeight
                )

                val centerX = right / 2
                val centerY = bottom / 2
                val row3Top = centerY - 8.toPX() - redButton.measuredHeight
                val row2Top = row3Top - 8.toPX() - redButton.measuredHeight
                val row1Top = row2Top - 8.toPX() - redButton.measuredHeight
                val column2Left = centerX + 8.toPX()
                val column1Left = centerX - 8.toPX() - redButton.measuredWidth

                redButton.layout(
                    column1Left
                    , row1Top
                    , column1Left + redButton.measuredWidth
                    , row1Top + redButton.measuredHeight
                )
                redAnimButton.layout(
                    column2Left
                    , row1Top
                    , column2Left + redAnimButton.measuredWidth
                    , row1Top + redAnimButton.measuredHeight
                )
                greenButton.layout(
                    column1Left
                    , row2Top
                    , column1Left + greenButton.measuredWidth
                    , row2Top + greenButton.measuredHeight
                )
                greenAnimButton.layout(
                    column2Left
                    , row2Top
                    , column2Left + greenAnimButton.measuredWidth
                    , row2Top + greenAnimButton.measuredHeight
                )
                blueButton.layout(
                    column1Left
                    , row3Top
                    , column1Left + blueButton.measuredWidth
                    , row3Top + blueButton.measuredHeight
                )
                blueAnimButton.layout(
                    column2Left
                    , row3Top
                    , column2Left + blueAnimButton.measuredWidth
                    , row3Top + blueAnimButton.measuredHeight
                )


                textView.layout(
                    -textView.measuredWidth / 2 + centerX
                    , centerY + 8.toPX()
                    , textView.measuredWidth / 2 + centerX
                    , centerY + 8.toPX() + textView.measuredHeight
                )
            }

        }
}