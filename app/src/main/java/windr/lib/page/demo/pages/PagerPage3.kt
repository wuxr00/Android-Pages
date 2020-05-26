package windr.lib.page.demo.pages

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import windr.lib.page.*
import windr.lib.page.demo.AndroidUtilities
import windr.lib.page.demo.toPX

class PagerPage3 : DefaultPage() {
    override fun createView(context: Context, parent: ViewGroup, args: Bundle?): View =
        object : FrameLayout(context) {
            val textView: TextView
            val button: Button

            init {
                setBackgroundColor(Color.BLUE)
                addView(
                    TextView(getContext()).also {
                        textView = it
                        it.setTextColor(Color.BLACK)
                        it.text = "pager page 3"
                    })
                addView(Button(getContext()).also {
                    button = it
                    button.text = "start blue page"
                    button.setOnClickListener {

                        PageRouter show
                                BluePage() withArguments
                                Bundle().apply { putInt("data", 1) } onStage
                                PageRouter.MAIN
                    }
                })
            }

            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                setMeasuredDimension(
                    AndroidUtilities.customScreenWidth,
                    AndroidUtilities.customScreenHeight
                )
                textView.measure(
                    MeasureSpec.makeMeasureSpec(
                        AndroidUtilities.customScreenWidth,
                        MeasureSpec.AT_MOST
                    ), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                button.measure(
                    MeasureSpec.makeMeasureSpec(
                        AndroidUtilities.customScreenWidth,
                        MeasureSpec.AT_MOST
                    ), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
            }
            override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//                super.onLayout(changed, left, top, right, bottom)
                val centerX =measuredWidth / 2
                val centerY = measuredHeight / 2
                button.layout(
                    -button.measuredWidth / 2 + centerX
                    , centerY - 8.toPX() - button.measuredHeight
                    , button.measuredWidth / 2 + centerX
                    , centerY - 8.toPX()
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