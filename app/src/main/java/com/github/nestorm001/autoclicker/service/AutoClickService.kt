package com.github.nestorm001.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Point
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.github.nestorm001.autoclicker.MainActivity
import com.github.nestorm001.autoclicker.bean.Event
import com.github.nestorm001.autoclicker.logd


/**
 * Created on 2018/9/28.
 * By nesto
 */

var autoClickService: AutoClickService? = null

class AutoClickService : AccessibilityService() {

    internal val events = mutableListOf<Event>()

    override fun onInterrupt() {
        // NO-OP
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // NO-OP
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        "onServiceConnected".logd()
        autoClickService = this
        startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun swipe(from : Point,to : Point){



        val displayMetrics = resources.displayMetrics

        val middleYValue = displayMetrics.heightPixels / 2
        val leftSideOfScreen = displayMetrics.widthPixels / 4
        val rightSizeOfScreen = leftSideOfScreen * 3


        val builder = GestureDescription.Builder()

        val path = Path()

        path.moveTo(leftSideOfScreen.toFloat(), middleYValue.toFloat())



            path.lineTo(leftSideOfScreen.toFloat()*4, middleYValue.toFloat())

            builder.addStroke(StrokeDescription(path,200,500))

        val gestureDescription = builder.build()
        dispatchGesture(gestureDescription, null, null)

    }

    private val mStrokes: ArrayList<StrokeDescription> = ArrayList()


    fun click(x: Int, y: Int) {
        "click $x $y".logd()
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        val builder = GestureDescription.Builder()
        val gestureDescription = builder
                .addStroke(GestureDescription.StrokeDescription(path, 10, 10))
                .build()
        dispatchGesture(gestureDescription, null, null)
    }

    fun run(newEvents: MutableList<Event>) {
        events.clear()
        events.addAll(newEvents)
        events.toString().logd()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val builder = GestureDescription.Builder()
        events.forEach { builder.addStroke(it.onEvent()) }
        dispatchGesture(builder.build(), null, null)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        "AutoClickService onUnbind".logd()
        autoClickService = null
        return super.onUnbind(intent)
    }


    override fun onDestroy() {
        "AutoClickService onDestroy".logd()
        autoClickService = null
        super.onDestroy()
    }
}