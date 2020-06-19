package com.github.nestorm001.autoclicker

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import com.github.nestorm001.autoclicker.service.FloatingClickService
import com.github.nestorm001.autoclicker.service.StopServiceListener


/**
 * Created on 2018/9/30.
 * By nesto
 */
class TouchAndDragListener(private val params: WindowManager.LayoutParams,
                           private val startDragDistance: Int = 10,
                           private val onTouch: Action?,
                           private val onDrag: Action?,
                           val floatingView: View,
                           val binView: View,
                           val vibrator: Vibrator,
                           val stopServiceListener: StopServiceListener
) : View.OnTouchListener {
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0.toFloat()
    private var initialTouchY: Float = 0.toFloat()
    private var isDrag = false

    private fun isDragging(event: MotionEvent): Boolean =
            ((Math.pow((event.rawX - initialTouchX).toDouble(), 2.0)
                    + Math.pow((event.rawY - initialTouchY).toDouble(), 2.0))
                    > startDragDistance * startDragDistance)

    var ABOVE = false
    override fun onTouch(v: View, event: MotionEvent): Boolean {

        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        if (isViewInBounds(binView, x, y)) {

            if (!ABOVE){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(20)
                }
                floatingView.animate().scaleX(0.5f).scaleY(0.5f).setDuration(400).setInterpolator(OvershootInterpolator()).start()
            }
            ABOVE = true
        } else {
            if (ABOVE){
                floatingView.animate().scaleX(1f).scaleY(1f).setDuration(400).setInterpolator(AnticipateInterpolator()).start()
            }
            ABOVE = false
        }


        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isDrag = false
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }


            MotionEvent.ACTION_MOVE -> {
              if(!(stopServiceListener as FloatingClickService).isOn){
                  if (!isDrag && isDragging(event)) {
                      isDrag = true
                      binView.visibility = View.VISIBLE
                      binView.animate().scaleX(1f).scaleY(1f).setDuration(400).setInterpolator(LinearOutSlowInInterpolator()).start()
                  }
                  if (!isDrag) return true
                  params.x = initialX + (event.rawX - initialTouchX).toInt()
                  params.y = initialY + (event.rawY - initialTouchY).toInt()

                  onDrag?.invoke()
              }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDrag) {
                    onTouch?.invoke()
                    return true
                } else {
                    if (ABOVE){
                        stopServiceListener.stopSwiperService()
                    }
                    binView.animate().scaleX(0f).scaleY(0f).setInterpolator(OvershootInterpolator()).start()
                }
            }
        }
        return false
    }
}

var outRect: Rect = Rect()
var location = IntArray(2)

private fun isViewInBounds(view: View, x: Int, y: Int): Boolean {
    view.getDrawingRect(outRect)
    view.getLocationOnScreen(location)
    outRect.offset(location[0], location[1])
    return outRect.contains(x, y)
}