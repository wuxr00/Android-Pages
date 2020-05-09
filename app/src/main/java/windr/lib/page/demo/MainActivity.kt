package windr.lib.page.demo

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import windr.lib.page.*
import windr.lib.page.demo.pages.MainPage
import windr.lib.page.exts.DefaultDraggablePageSwitchGenerator

class MainActivity : AppCompatActivity() {

    private val pageManager by lazy {
        PageManager(this, layout).holdBackStack().apply {
            PageRouter.registerRouteReceiver(PageRouter.MAIN, this)
            setPageSwitchAnimationGenerator(DefaultDraggablePageSwitchGenerator(this@MainActivity))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidUtilities.initUtilties(this)
        setContentView(R.layout.activity_main)

        PageManager add MainPage() toPageManager pageManager


    }

    override fun onBackPressed() {
        if (!pageManager.onBackPressed())
            super.onBackPressed()
    }

/*
    val animation by lazy {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(
                    v1,
                    View.TRANSLATION_X,
                    0f,
                    -AndroidUtilities.customScreenWidth.toFloat()
                ).setDuration(1200)
                ,
                ObjectAnimator.ofFloat(
                    sv1,
                    View.TRANSLATION_X,
                    0f,
                    (v1.measuredWidth - sv1.measuredWidth).toFloat()
                ).setDuration(1000)
                ,
                ObjectAnimator.ofFloat(
                    sv1,
                    View.ALPHA,
                    0.3f,
                    1f
                ).setDuration(1500)
                ,
                ObjectAnimator.ofFloat(
                    v2,
                    View.TRANSLATION_X,
                    AndroidUtilities.customScreenWidth.toFloat(),
                    0f
                ).setDuration(1300)
                ,
                ObjectAnimator.ofFloat(
                    sv2,
                    View.TRANSLATION_X,
                    0f,
                    -(v1.measuredWidth - sv1.measuredWidth).toFloat()
                ).setDuration(1300)
                ,
                ObjectAnimator.ofFloat(
                    sv2,
                    View.ALPHA,
                    1f,
                    0.2f
                ).setDuration(1000)
            )
            onAnimEnd {
                isRestricted
                log("animation end........${this@apply}")
            }
            log("animation init........$this")
        }
    }


    fun prepare1(V: View) {
        log("animation prepare $animation")
        animation.setupStartValues()
        log("animation end prepare$animation")
    }

    fun start(V: View) {
        animation.start()
    }

    fun prepare2(V: View) {
        animation.setupEndValues()
    }

    fun reverse(V: View) {
        animation.reverse()
    }*/
}
