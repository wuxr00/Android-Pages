package windr.lib.page.demo.pages

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import windr.lib.page.DefaultPage
import windr.lib.page.IPage
import windr.lib.page.exts.PagePagerAdapter

class MainPage : DefaultPage() {

    private lateinit var pagerAdapter: PagePagerAdapter<IPage>

    override fun createView(context: Context, parent: ViewGroup, args: Bundle?): View {
        return object : FrameLayout(context) {
            init {
                pagerAdapter = PagePagerAdapter(ViewPager(context).also {
                    this.addView(it)
                })

                pagerAdapter.pages = listOf(
                    PagerPage1()
                    , PagerPage2()
                    , PagerPage3()
                )
            }
        }
    }

    override fun onShow() {
        super.onShow()
        pagerAdapter.onShow()
    }

    override fun onHide() {
        pagerAdapter.onHide()
    }

    override fun onDestroy() {
        super.onDestroy()
        pagerAdapter.onDestroy()
    }
}