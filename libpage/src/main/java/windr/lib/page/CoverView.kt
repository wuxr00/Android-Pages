package windr.lib.page

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager

/**
 * 两个页面之间的隔层视图，阻挡触摸事件传递到下层页面，可设置背景色*/
class CoverView(context: Context, var coverHolder: CoverHolder) :
    View(context) {
    var coverWidth: Int = 0
    var coverHeight: Int = 0
    fun setSize(coverWidth: Int, coverHeight: Int) {
        this.coverWidth = coverWidth
        this.coverHeight = coverHeight
    }

    fun showCoverUnderTop() {
        coverHolder.showCoverUnderTop()
    }

    override fun onMeasure(coverWidthMeasureSpec: Int, coverHeightMeasureSpec: Int) {
        setMeasuredDimension(coverWidth, coverHeight)
    }

    interface CoverHolder {
        fun showCoverUnderTop()
    }

    init {
        isClickable = true
        val manager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getRealMetrics(displayMetrics)
        coverWidth = displayMetrics.widthPixels
        coverHeight = displayMetrics.heightPixels
        //        setBackgroundColor(Color.argb(10, 0, 0, 0));
    }
}