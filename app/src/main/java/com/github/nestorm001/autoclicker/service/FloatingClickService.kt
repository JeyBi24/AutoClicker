package com.github.nestorm001.autoclicker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import android.widget.Toast
import com.github.nestorm001.autoclicker.R
import com.github.nestorm001.autoclicker.TouchAndDragListener
import com.github.nestorm001.autoclicker.dp2px
import com.github.nestorm001.autoclicker.logd
import java.util.*
import kotlin.concurrent.fixedRateTimer


/**
 * Created on 2018/9/28.
 * By nesto
 */
class FloatingClickService : Service(),StopServiceListener{
    private lateinit var manager: WindowManager
    private lateinit var view: View
    private lateinit var binView : View
    private lateinit var params: WindowManager.LayoutParams

    private lateinit var binParams: WindowManager.LayoutParams

    private var xForRecord = 0
    private var yForRecord = 0
    private val location = IntArray(2)
    private var startDragDistance: Int = 0
    private var timer: Timer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startDragDistance = dp2px(10f)

        val inflater = LayoutInflater.from(this)
        view = inflater.inflate(R.layout.widget, null)
        binView = inflater.inflate(R.layout.bin,null)
        //setting the layout parameters
        val overlayParam =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
        params = WindowManager.LayoutParams(
                convertDpToPixel(56f).toInt(),
                convertDpToPixel(56f).toInt(),
                overlayParam,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        binParams = WindowManager.LayoutParams(
                convertDpToPixel(48f).toInt(),
                convertDpToPixel(48f).toInt(),
                overlayParam,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        binParams.gravity = Gravity.BOTTOM or Gravity.CENTER
        binParams.y = convertDpToPixel(24f).toInt()

        //getting windows services and adding the floating view to it
        manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        manager.addView(binView,binParams)
        manager.addView(view, params)

        val vibrator = getSystemService(VIBRATOR_SERVICE)

        //adding an touchlistener to make drag movement of the floating widget
        view.setOnTouchListener(TouchAndDragListener(params, startDragDistance,
                { viewOnClick() },
                { manager.updateViewLayout(view, params) },view,binView,vibrator as Vibrator,this))

    }

    fun convertDpToPixel(dp: Float): Float {
        return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }


    var isOn = false

    private fun viewOnClick() {
        if (isOn) {
            view.setBackgroundResource(R.drawable.widget_background_disab)
            timer?.cancel()
        } else {
            view.setBackgroundResource(R.drawable.widget_background)
            timer = fixedRateTimer(initialDelay = 0,
                    period = 800) {
                view.getLocationOnScreen(location)
//                autoClickService?.click(location[0] + view.right + 10,
//                        location[1] + view.bottom + 10)
                autoClickService?.swipe(
                        Point(location[0] + view.right + 10,location[1] + view.bottom + 10),
                        Point(location[0] + view.right + 10,location[1] + view.bottom + 10)
                        )
            }
        }
        isOn = !isOn
        (view as TextView).text = if (isOn) "ON" else "OFF"

    }

    override fun onDestroy() {
        super.onDestroy()
        "FloatingClickService onDestroy".logd()
        timer?.cancel()
        manager.removeView(view)
        manager.removeView(binView)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        "FloatingClickService onConfigurationChanged".logd()
        val x = params.x
        val y = params.y
        params.x = xForRecord
        params.y = yForRecord
        xForRecord = x
        yForRecord = y
        manager.updateViewLayout(view, params)
    }

    override fun stopSwiperService() {
        stopSelf()
    }
}