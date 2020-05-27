package windr.lib.page.exts

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import windr.lib.page.IPage
import java.util.*

/**
 * page的viewpager适配器
 */
class PagePagerAdapter<T : IPage>(private val viewPager: ViewPager) : PagerAdapter(),
    OnPageChangeListener {
    var pages: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val viewPageCache = HashMap<Int, IPage>()

    val currentPage: IPage?
        get() = if (pages.isEmpty()) null else pages[viewPager.currentItem]

    fun getPageAt(index: Int): IPage? = if (index < pages.size) pages[index] else null


    override fun getCount(): Int = if (finishing) 0 else pages.size


    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean = view === `object`


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val page = pages[position]

        var view = page.getView()
        if (view == null) {
            page.createPage(container.context, container)
            view = page.getView()
            viewPageCache[view.hashCode()] = page
        }
        container.addView(view)
        return view!!
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        val key = `object`.hashCode()
        val page = viewPageCache[key] ?: return POSITION_NONE
        val index = pages.indexOf(page)
        if (index < 0) {
            viewPageCache.remove(key)
            return POSITION_NONE
        }
        return index
    }

    private var currentPosition = 0
    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
    }

    override fun onPageSelected(position: Int) {
        pages[position].show()
        if (currentPosition < position) pages[currentPosition].hide()
        currentPosition = position
    }

    override fun onPageScrollStateChanged(state: Int) {}


    fun onShow() {
        currentPage?.show()
    }

    fun onHide() {
        currentPage?.hide()
    }

    private var finishing = false
    fun onDestroy() {
        finishing = true
        notifyDataSetChanged()
        pages.forEachIndexed { index, page ->
            page.onFinishing()
            if (index == currentPosition) page.hide()
            page.destroy()
        }
        pages = emptyList()
        viewPageCache.clear()
    }

    init {
        viewPager.adapter = this
        viewPager.addOnPageChangeListener(this)
    }
}