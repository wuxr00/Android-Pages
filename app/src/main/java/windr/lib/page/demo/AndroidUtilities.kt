package windr.lib.page.demo

import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.io.RandomAccessFile
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Pattern

/**
 * @author tony
 * @version 2.1, 2016/11/30.
 * @since JDK1.7
 */
object AndroidUtilities {
    const val SAVE_FILE = "_android_i_"
    private const val SAVE_KEYBOARD_HEIGHT = "_keyboard_height"
    private var context: Context? = null
    var density = 1f
    var screenRefreshRate = 60f
    var devicePerformanceClass = 0
    private var statusBarHeight = 0

    /**
     * 屏幕宽度
     */
    var screenWidth = 0

    /**
     * 屏幕高度
     */
    var screenHeight = 0

    /**
     * app可显示屏幕宽度
     */
    var customScreenWidth = 0

    /**
     * app可显示屏幕高度（排除虚拟导航栏的区域）
     */
    var customScreenHeight = 0
    var navigationBarHeight = 0
    var keyboardHeight = 0
    var isNavigationBarShown = false
    var isKeyboardShown = false
    private val systemComponentsInsetListeners =
        HashSet<SystemComponentsInsetListener>()

    fun initUtilties(context: Context) {
        this@AndroidUtilities.context = context
        getStatusBarHeight()
        if (keyboardHeight == 0) {
            keyboardHeight = context.getSharedPreferences(SAVE_FILE, Context.MODE_PRIVATE)
                .getInt(SAVE_KEYBOARD_HEIGHT, 0)
        }
        if (context is Activity)
            refreshScreenSize(context)
        else _refreshScreenSize()
        checkScreenRefreshRate()

        if (context is AppCompatActivity)
            context.lifecycle.addObserver(object : LifecycleObserver {

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy(owner: LifecycleOwner) {
                    this@AndroidUtilities.context = null
                }
            })
    }

    fun refreshScreenSize(holder: Activity) {
        _refreshScreenSize()
        val contentView = holder.window.decorView.findViewById<View>(R.id.content)
        fun checkCustomViewHeight() {
            if (customScreenHeight != contentView.height)
                customScreenHeight = contentView.height
        }
        if (contentView.height == 0)
            contentView.post {
                checkCustomViewHeight()
            }
        else checkCustomViewHeight()
    }


    fun getStatusBarHeight(): Int {
        if (statusBarHeight == 0) {
            val resourceId = this.context?.resources?.getIdentifier(
                "status_bar_height"
                , "dimen"
                , "android"
            ) ?: 0
            if (resourceId > 0) {
                statusBarHeight = this.context?.resources?.getDimensionPixelSize(resourceId) ?: 0
            }
        }
        return statusBarHeight
    }


    private fun _refreshScreenSize() {
        try {
            (context?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
                ?.apply {
                    screenRefreshRate = refreshRate
                    val displayMetrics = DisplayMetrics()
                    getMetrics(displayMetrics)
                    customScreenWidth = displayMetrics.widthPixels
                    customScreenHeight = displayMetrics.heightPixels
                    density = displayMetrics.density
                    //                    Log.i(AndroidUtilities.class.getSimpleName(), "display metrics->" + displayMetrics.widthPixels + " - " + displayMetrics.heightPixels + " - " + displayMetrics.density);
                    getRealMetrics(displayMetrics)
                    screenWidth = displayMetrics.widthPixels
                    screenHeight = displayMetrics.heightPixels
                    //                    Log.i(AndroidUtilities.class.getSimpleName(), "display real metrics->" + displayMetrics.widthPixels + " - " + displayMetrics.heightPixels + " - " + displayMetrics.density);
                }
        } catch (e: Exception) {
        }
    }

    fun checkScreenRefreshRate(): Float {
        try {
            val display =
                (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            if (display != null)
                screenRefreshRate = display.refreshRate
        } catch (e: Exception) {
        }
        return screenRefreshRate
    }


    fun observeFullScreenModeSystemComponentsDisplay(holder: Activity) {
        CustomViewLayoutObserver(holder, true)
    }

    fun registerSystemComonentsObserver(componentsInsetListener: SystemComponentsInsetListener) {
        systemComponentsInsetListeners.add(componentsInsetListener)
    }

    fun unregisterSystemComonentsObserver(componentsInsetListener: SystemComponentsInsetListener) {
        systemComponentsInsetListeners.remove(componentsInsetListener)
    }

    fun clearSystemComonentsObserver() {
        systemComponentsInsetListeners.clear()
    }

    const val PERFORMANCE_CLASS_LOW = 0
    const val PERFORMANCE_CLASS_AVERAGE = 1
    const val PERFORMANCE_CLASS_HIGH = 2
    fun getDevicePerfomanceClass(context: Context): Int {
        if (devicePerformanceClass == -1) {
            var maxCpuFreq = -1
            try {
                val reader = RandomAccessFile(
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq",
                    "r"
                )
                val line = reader.readLine()
                if (line != null) {
                    maxCpuFreq = parseInt(line) / 1000
                }
                reader.close()
            } catch (ignore: Throwable) {
            }
            val androidVersion = Build.VERSION.SDK_INT
            val cpuCount = Runtime.getRuntime().availableProcessors()
            val memoryClass =
                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
            devicePerformanceClass =
                if (androidVersion < 21 || cpuCount <= 2 || memoryClass <= 100 || cpuCount <= 4 && maxCpuFreq != -1 && maxCpuFreq <= 1250 || cpuCount <= 4 && maxCpuFreq <= 1600 && memoryClass <= 128 && androidVersion <= 21 || cpuCount <= 4 && maxCpuFreq <= 1300 && memoryClass <= 128 && androidVersion <= 24) {
                    PERFORMANCE_CLASS_LOW
                } else if (cpuCount < 8 || memoryClass <= 160 || maxCpuFreq != -1 && maxCpuFreq <= 1650 || maxCpuFreq == -1 && cpuCount == 8 && androidVersion <= 23) {
                    PERFORMANCE_CLASS_AVERAGE
                } else {
                    PERFORMANCE_CLASS_HIGH
                }
            Log.i(
                AndroidUtilities::class.java.simpleName,
                "device performance info (cpu_count = $cpuCount, freq = $maxCpuFreq, memoryClass = $memoryClass, android version $androidVersion)"
            )
        }
        return devicePerformanceClass
    }

    var pattern = Pattern.compile("[\\-0-9]+")
    fun parseInt(charValue: CharSequence?): Int {
        if (charValue == null) {
            return 0
        }
        var value = 0
        try {
            val matcher = pattern.matcher(charValue)
            if (matcher.find()) {
                val num = matcher.group(0)
                value = num.toInt()
            }
        } catch (ignore: Exception) {
        }
        return value
    }

    private class CustomViewLayoutObserver(
        var holder: Activity?,
        private val isImmersiveStatusBar: Boolean
    ) : OnGlobalLayoutListener, View.OnApplyWindowInsetsListener {
        private var decor: View? = null
        private var contentView: ViewGroup? = null
        private var customView: View? = null
        private val tempBottomInsetHeight = 0

        init {
            if (holder != null) {
                decor = holder!!.window.decorView
                contentView = decor?.findViewById(R.id.content)
                customView = contentView?.getChildAt(0)
                decor?.viewTreeObserver?.addOnGlobalLayoutListener(this)
                contentView?.setOnApplyWindowInsetsListener(this)
                if (holder is AppCompatActivity)
                    (holder as AppCompatActivity).lifecycle.addObserver(object : LifecycleObserver {

                        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                        fun onDestroy(owner: LifecycleOwner) {
                            systemComponentsInsetListeners.clear()
                            decor?.viewTreeObserver?.removeOnGlobalLayoutListener(this@CustomViewLayoutObserver)
                            this@CustomViewLayoutObserver.holder = null
                        }
                    })
            }
        }

        override fun onApplyWindowInsets(
            v: View,
            insets: WindowInsets
        ): WindowInsets {
            Log.i(
                AndroidUtilities::class.java.simpleName,
                "onapplywindowinserts----------------------------------------->" + insets.systemWindowInsetBottom + " - " + insets
            )

//            tempBottomInsetHeight = insets.getSystemWindowInsetBottom();
            v.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0, 0))
            return insets
        }

        override fun onGlobalLayout() {
            if (holder?.isFinishing != false
                || holder?.isDestroyed != false
            ) {
                decor?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                holder = null
                return
            }
            if (decor == null || contentView == null || customView == null) return
            val navigationBar =
                decor!!.findViewById<View>(R.id.navigationBarBackground)
            Log.i(
                AndroidUtilities::class.java.simpleName,
                "onGlobalLayout-------------------->" + navigationBar
                        + " - " + contentView!!.height + " - " + customView!!.height
            )
            Log.i(
                AndroidUtilities::class.java.simpleName,
                "check navbar->" + (navigationBar != null && navigationBar.height > 0
                        && screenHeight - contentView!!.height == navigationBar.height)
                        + " - " + isNavigationBarShown
            )
            var screenInfoChanged = false
            if (navigationBar != null
                && navigationBar.height > 0
                && screenHeight - contentView!!.height == navigationBar.height
            ) {
                if (!isNavigationBarShown) {
                    isNavigationBarShown = true
                    navigationBarHeight = navigationBar.height
                    screenInfoChanged = true
                }
            } else {
                if (isNavigationBarShown) {
                    isNavigationBarShown = false
                    screenInfoChanged = true
                }
            }
            var keyboardStateChanged = false
            val customVisibleRect = Rect()
            customView!!.getWindowVisibleDisplayFrame(customVisibleRect)
            if (isImmersiveStatusBar) customVisibleRect.top = 0
            val tempContentHeightOffset = contentView!!.height - customVisibleRect.height()
            Log.i(
                AndroidUtilities::class.java.simpleName,
                "check keyboard->$isKeyboardShown - $keyboardHeight - $customVisibleRect - $tempContentHeightOffset"
            )
            if (tempContentHeightOffset == 0) {
                if (isKeyboardShown) {
                    isKeyboardShown = false //悬浮键盘暂无法判断
                    keyboardStateChanged = true
                    screenInfoChanged = true
                }
            } else {
                if (!isKeyboardShown) {
                    isKeyboardShown = true
                    keyboardStateChanged = true
                    screenInfoChanged = true
                }
                if (tempContentHeightOffset != keyboardHeight) {
                    keyboardStateChanged = true
                    screenInfoChanged = true
                    keyboardHeight = tempContentHeightOffset
                    customView!!.context.getSharedPreferences(SAVE_FILE, Context.MODE_PRIVATE)
                        .edit().putInt(
                            SAVE_KEYBOARD_HEIGHT,
                            keyboardHeight
                        ).apply()
                    //                    screenInfoChanged = true;
                }
            }
            if (screenInfoChanged) {
                if (holder != null)
                    refreshScreenSize(holder!!) //更新屏幕参数
                else {
                    _refreshScreenSize()
                    Log.i(
                        AndroidUtilities::class.java.simpleName,
                        "check screenheight->" + customScreenHeight + " - " + contentView!!.height
                    )

                    //某些手机上即使没有显示导航栏，通过获取的高度参数仍不是全屏的高度
                    if (customScreenHeight != contentView!!.height) customScreenHeight =
                        contentView!!.height
                }
            }
            Log.i(
                AndroidUtilities::class.java.simpleName,
                "check keyboardStateChanged->$keyboardStateChanged - $isKeyboardShown - $keyboardHeight"
            )
            if (screenInfoChanged) {
                for (listener in systemComponentsInsetListeners) {
                    listener.onComponentsShownStateChanged(
                        isKeyboardShown,
                        keyboardHeight,
                        isNavigationBarShown,
                        navigationBarHeight
                    )
                }
            }
        }

    }

    interface SystemComponentsInsetListener {
        fun onComponentsShownStateChanged(
            keyboardShown: Boolean,
            keyboardHeight: Int,
            navigationBarShown: Boolean,
            navigationBarHeight: Int
        )
    }
}

//todo========================================单位转换===========================================

/**
 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
 */
fun Float.toPX() = (this * AndroidUtilities.density + 0.5f).toInt()

fun Float.toPXF() = this * AndroidUtilities.density + 0.5f

fun Int.toPX() = (this * AndroidUtilities.density + 0.5f).toInt()

fun Int.toPXF() = this * AndroidUtilities.density + 0.5f

/**
 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
 */
fun Float.toDP() = (this / AndroidUtilities.density + 0.5f).toInt()

fun Float.toDPF() = this / AndroidUtilities.density + 0.5f

fun Int.toDP() = (this / AndroidUtilities.density + 0.5f).toInt()

fun Int.toDPF() = this / AndroidUtilities.density + 0.5f

//todo========================================沉浸式状态栏===========================================
/**
 * 适配popupwindow 沉浸式状态栏模式*/
@SuppressLint("DiscouragedPrivateApi")
fun PopupWindow.adjustImmersiveStatusBar() {
    fun onError() {
        try {
            val layoutInScreenMethod: Method =
                PopupWindow::class.java.getDeclaredMethod(
                    "setLayoutInScreenEnabled",
                    Boolean::class.javaPrimitiveType
                )
            layoutInScreenMethod.isAccessible = true
            layoutInScreenMethod.invoke(this, true)
        } catch (e1: java.lang.Exception) {
        }
    }
    try {
        val mLayoutInScreen: Field =
            PopupWindow::class.java.getDeclaredField("mLayoutInScreen")
        mLayoutInScreen.isAccessible = true
        mLayoutInScreen.set(this, true)
    } catch (e: NoSuchFieldException) {
        onError()
    } catch (e: IllegalAccessException) {
        onError()
    }
}

/**
 * 布局适配沉浸式状态栏后软键盘弹出*/
fun ViewGroup.adjustImmersiveStatusBar() {
    this.setOnApplyWindowInsetsListener { v, insets ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val appInsets = WindowInsets.Builder()
                .setSystemWindowInsets(Insets.of(0, 0, 0, insets.systemWindowInsetBottom)).build()
            v.onApplyWindowInsets(appInsets)
            appInsets
        } else {
            v.onApplyWindowInsets(
                insets.replaceSystemWindowInsets(
                    0,
                    0,
                    0,
                    insets.systemWindowInsetBottom
                )
            )
            insets
        }
    }
    this.fitsSystemWindows = true
}

/**
 * 设置沉浸式状态栏（会影响软键盘弹出模式）*/
fun Activity.setImmersiveStatusBar(@ColorInt color: Int = Color.TRANSPARENT) {
    this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    this.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    this.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    this.window.statusBarColor = color

    val parent = this.findViewById(R.id.content) as ViewGroup
    for (i in 0 until parent.childCount) {
        val childView = parent.getChildAt(i)
        if (childView is ViewGroup) {
            childView.setFitsSystemWindows(true)
            childView.clipToPadding = true
        }
    }
}

/**
 * 设置状态栏图片主题*/
fun Activity.setStatusBarTheme(lightMode: Boolean) {
    setMIUIStatusBarDarkIcon(this, lightMode)
    setMeizuStatusBarDarkIcon(this, lightMode)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.window.decorView.systemUiVisibility =
            if (lightMode) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}

/**
 * 修改 MIUI V6  以上状态栏颜色
 */
private fun setMIUIStatusBarDarkIcon(
    activity: Activity,
    darkIcon: Boolean
) {
    val clazz: Class<out Window?> = activity.window.javaClass
    try {
        val layoutParams =
            Class.forName("android.view.MiuiWindowManager\$LayoutParams")
        val field =
            layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
        val darkModeFlag = field.getInt(layoutParams)
        val extraFlagField = clazz.getMethod(
            "setExtraFlags",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        extraFlagField.invoke(activity.window, if (darkIcon) darkModeFlag else 0, darkModeFlag)
    } catch (e: java.lang.Exception) {
        //e.printStackTrace();
    }
}

/**
 * 修改魅族状态栏字体颜色 Flyme 4.0
 */
private fun setMeizuStatusBarDarkIcon(
    activity: Activity,
    darkIcon: Boolean
) {
    try {
        val lp = activity.window.attributes
        val darkFlag =
            WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
        val meizuFlags =
            WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
        darkFlag.isAccessible = true
        meizuFlags.isAccessible = true
        val bit = darkFlag.getInt(null)
        var value = meizuFlags.getInt(lp)
        value = if (darkIcon) {
            value or bit
        } else {
            value and bit.inv()
        }
        meizuFlags.setInt(lp, value)
        activity.window.attributes = lp
    } catch (e: java.lang.Exception) {
        //e.printStackTrace();
    }
}


//todo========================================log===========================================

fun Any.log(msg: CharSequence?, tag: String = "") {
    if (BuildConfig.DEBUG)
        Log.i(if (tag.isBlank()) this.javaClass.simpleName else tag, msg.toString())
}

fun Any.logW(msg: CharSequence?, tag: String = "") {
    if (BuildConfig.DEBUG)
        Log.w(if (tag.isBlank()) this.javaClass.simpleName else tag, msg.toString())
}

fun Any.logE(msg: CharSequence?, tag: String = "") {
    if (BuildConfig.DEBUG)
        Log.e(if (tag.isBlank()) this.javaClass.simpleName else tag, msg.toString())
}

//todo========================================动画===========================================

internal inline fun Animator.startAnimationFor(crossinline callback: Animator.(canceled: Boolean) -> Unit) {
    this.onAnimEnd(callback)
    this.start()
}

internal inline fun Animator.onAnimEnd(crossinline callback: Animator.(canceled: Boolean) -> Unit): Animator {
    this.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationCancel(animation: Animator) {
            super.onAnimationCancel(animation)
            callback.invoke(this@onAnimEnd, true)
        }

        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            callback.invoke(this@onAnimEnd, false)
        }
    })
    return this
}